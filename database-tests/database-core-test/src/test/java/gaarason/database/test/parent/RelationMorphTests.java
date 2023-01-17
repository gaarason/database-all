package gaarason.database.test.parent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.contract.eloquent.Record;
import gaarason.database.test.models.morph.entity.Comment;
import gaarason.database.test.models.morph.entity.Image;
import gaarason.database.test.models.morph.entity.Post;
import gaarason.database.test.models.morph.entity.SuperRelation;
import gaarason.database.test.parent.base.BaseTests;
import gaarason.database.util.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Arrays;
import java.util.List;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
abstract public class RelationMorphTests extends BaseTests {

    protected static final Comment.Model commentModel = new Comment.Model();

    protected static final Image.Model imageModel = new Image.Model();

    protected static final Post.Model postModel = new Post.Model();

    protected static final SuperRelation.Model superRelationModel = new SuperRelation.Model();

    @Override
    protected GaarasonDataSource getGaarasonDataSource() {
        return commentModel.getGaarasonDataSource();
    }

    @Override
    protected List<TABLE> getInitTables() {
        return Arrays.asList(TABLE.comment, TABLE.image, TABLE.post, TABLE.super_relation);
    }

    @Test
    public void 多态一对一() {
        Post post = postModel.findOrFail(1).with("comment.comment").with(Post::getComments).toObject();
        Assert.assertNull(post.getComment());
        Assert.assertTrue(post.getComments().isEmpty());

        Post post3 = postModel.findOrFail(3).with("comment.comment.comment").with(Post::getComments).toObject();
        Assert.assertNotNull(post3.getComment());
        Assert.assertNotNull(post3.getComment().getComment());
        Assert.assertNotNull(post3.getComment().getComment().getComment());
        Assert.assertFalse(post3.getComments().isEmpty());
        Assert.assertEquals(1, post3.getComment().getId().intValue());
        Assert.assertEquals(2, post3.getComment().getComment().getId().intValue());
        Assert.assertEquals(3, post3.getComment().getComment().getComment().getId().intValue());
    }

    @Test
    public void 多态反向一对一() {
        Comment comment2 = commentModel.findOrFail(2).with(Comment::getPost).with(Comment::getPcomment).toObject();
        Assert.assertNull(comment2.getPost());
        Assert.assertNotNull(comment2.getPcomment());
        Assert.assertEquals(1, comment2.getPcomment().getId().intValue());

        Comment comment1 = commentModel.findOrFail(1).with(Comment::getPost).toObject();
        Assert.assertNotNull(comment1.getPost());
        Assert.assertEquals(3, comment1.getPost().getId().intValue());

        Comment comment3 = commentModel.findOrFail(3).with(Comment::getPost).with("pcomment.pcomment").toObject();
        Assert.assertNull(comment3.getPost());
        Assert.assertNotNull(comment3.getPcomment());
        Assert.assertEquals(2, comment3.getPcomment().getId().intValue());
        Assert.assertNotNull(comment3.getPcomment().getPcomment());
        Assert.assertEquals(1, comment3.getPcomment().getPcomment().getId().intValue());
    }

    @Test
    public void 多态多对多() {
        Post post = postModel.findOrFail(3).with(Post::getImages).toObject();
        Assert.assertFalse(ObjectUtils.isEmpty(post.getImages()));
        Assert.assertEquals(6, post.getImages().size());

        Post post2 = postModel.findOrFail(3).with(Post::getImagesWithMorph).toObject();
        Assert.assertFalse(ObjectUtils.isEmpty(post2.getImagesWithMorph()));
        Assert.assertEquals(2, post2.getImagesWithMorph().size());
        Assert.assertEquals("url3333333333333", post2.getImagesWithMorph().get(0).getUrl());
        Assert.assertEquals("url44444", post2.getImagesWithMorph().get(1).getUrl());

        Post post3 = postModel.findOrFail(3).with(Post::getImagesWithLocalMorph).toObject();
        Assert.assertFalse(ObjectUtils.isEmpty(post3.getImagesWithLocalMorph()));
        Assert.assertEquals(4, post3.getImagesWithLocalMorph().size());

        Post post4 = postModel.findOrFail(3).with(Post::getImagesWithTargetMorph).toObject();
        Assert.assertFalse(ObjectUtils.isEmpty(post4.getImagesWithTargetMorph()));
        Assert.assertEquals(3, post4.getImagesWithTargetMorph().size());
        Assert.assertEquals("url3333333333333", post4.getImagesWithTargetMorph().get(0).getUrl());
        Assert.assertEquals("url44444", post4.getImagesWithTargetMorph().get(1).getUrl());
        Assert.assertEquals("url8888", post4.getImagesWithTargetMorph().get(2).getUrl());
    }

    @Test
    public void 多态_attach_HasOneOrMany_单个() {
        String str = "sasTTTT";
        Record<Post, Long> postRecord = postModel.findOrFail(1).with(Post::getComment);
        Post post = postRecord.toObject();
        Assert.assertNull(post.getComment());

        Record<Comment, Long> commentRecord = commentModel.newRecord();
        commentRecord.getEntity().setContent(str);
        commentRecord.save();

        postRecord.bind(Post::getComment).attach(commentRecord);

        Post post2 = postRecord.toObject();
        Assert.assertNotNull(post2.getComment());
        Assert.assertEquals(str, post2.getComment().getContent());
    }

    @Test
    public void 多态_attach_HasOneOrMany_多个() {
        String str1 = "cddd";
        String str2 = "33333333333";
        Record<Post, Long> postRecord = postModel.findOrFail(1).with(Post::getComments);
        Post post = postRecord.toObject();
        Assert.assertTrue(post.getComments().isEmpty());

        Record<Comment, Long> commentRecord1 = commentModel.newRecord();
        commentRecord1.getEntity().setContent(str1);
        commentRecord1.save();

        Record<Comment, Long> commentRecord2 = commentModel.newRecord();
        commentRecord2.getEntity().setContent(str2);
        commentRecord2.save();

        // 借助 orm插入后, 会回填自增主键的能力
        postRecord.bind(Post::getComments)
            .attach(Arrays.asList(commentRecord1.getEntity().getId(), commentRecord2.getEntity().getId()));

        Post post2 = postRecord.toObject();
        Assert.assertFalse(post2.getComments().isEmpty());
        Assert.assertEquals(2, post2.getComments().size());
        Assert.assertEquals(str1, post2.getComments().get(0).getContent());
        Assert.assertEquals(str2, post2.getComments().get(1).getContent());
    }

    @Test
    public void 多态_detach_HasOneOrMany_单个() {
        Record<Post, Long> postRecord = postModel.findOrFail(3).with(Post::getComment);
        Post post = postRecord.toObject();
        Assert.assertNotNull(post.getComment());

        postRecord.bind(Post::getComment).detach(88);

        Post post2 = postRecord.toObject();
        Assert.assertNotNull(post2.getComment());

        postRecord.bind(Post::getComment).detach();

        Post post3 = postRecord.toObject();
        Assert.assertNull(post3.getComment());
    }

    @Test
    public void 多态_detach_HasOneOrMany_多个() {
        Record<Post, Long> postRecord = postModel.findOrFail(3).with(Post::getComments);
        Post post = postRecord.toObject();
        Assert.assertFalse(post.getComments().isEmpty());

        postRecord.bind(Post::getComment).detach(88);

        Post post2 = postRecord.toObject();
        Assert.assertFalse(post2.getComments().isEmpty());

        postRecord.bind(Post::getComment).detach();

        Post post3 = postRecord.toObject();
        Assert.assertTrue(post3.getComments().isEmpty());
    }

    @Test
    public void 多态_sync_HasOneOrMany_多个() {
        Record<Post, Long> postRecord = postModel.findOrFail(3).with(Post::getComments);
        Post post = postRecord.toObject();
        Assert.assertEquals(1, post.getComments().size());
        Assert.assertEquals(1, post.getComments().get(0).getId().intValue());

        postRecord.bind(Post::getComments).sync(commentModel.findMany(1, 3));

        Post post2 = postRecord.toObject();
        Assert.assertEquals(2, post2.getComments().size());
        Assert.assertEquals(1, post2.getComments().get(0).getId().intValue());
        Assert.assertEquals(3, post2.getComments().get(1).getId().intValue());
    }

    @Test
    public void 多态_toggle_HasOneOrMany_多个() {
        Record<Post, Long> postRecord = postModel.findOrFail(3).with(Post::getComments);
        Post post = postRecord.toObject();
        Assert.assertEquals(1, post.getComments().size());
        Assert.assertEquals(1, post.getComments().get(0).getId().intValue());

        postRecord.bind(Post::getComments).toggle(commentModel.findMany(1, 2, 3));

        Post post2 = postRecord.toObject();
        Assert.assertEquals(2, post2.getComments().size());
        Assert.assertEquals(2, post2.getComments().get(0).getId().intValue());
        Assert.assertEquals(3, post2.getComments().get(1).getId().intValue());

        postRecord.bind(Post::getComments).toggle(Arrays.asList(1, 2));

        Post post3 = postRecord.toObject();
        Assert.assertEquals(2, post3.getComments().size());
        Assert.assertEquals(1, post3.getComments().get(0).getId().intValue());
        Assert.assertEquals(3, post3.getComments().get(1).getId().intValue());
    }

    @Test
    public void 多态_attach_BelongsTo() {
        Record<Comment, Long> commentRecord = commentModel.findOrFail(1).with(Comment::getPost);
        Comment comment = commentRecord.toObject();
        Assert.assertEquals(3, comment.getPost().getId().intValue());

        commentRecord.bind(Comment::getPost).attach(1);
        Comment comment2 = commentRecord.toObject();
        Assert.assertEquals(1, comment2.getPost().getId().intValue());
    }

    @Test
    public void 多态_detach_BelongsTo() {
        Record<Comment, Long> commentRecord = commentModel.findOrFail(1).with(Comment::getPost);
        Comment comment = commentRecord.toObject();
        Assert.assertEquals(3, comment.getPost().getId().intValue());

        commentRecord.bind(Comment::getPost).detach(1);

        Comment comment2 = commentRecord.toObject();
        Assert.assertEquals(3, comment2.getPost().getId().intValue());

        commentRecord.bind(Comment::getPost).detach(3);

        Comment comment3 = commentRecord.toObject();
        Assert.assertNull(comment3.getPost());
    }

    @Test
    public void 多态_sync_BelongsTo() {
        Record<Comment, Long> commentRecord = commentModel.findOrFail(1).with(Comment::getPost);
        Comment comment = commentRecord.toObject();
        Assert.assertEquals(3, comment.getPost().getId().intValue());

        commentRecord.bind(Comment::getPost).sync(1);

        Comment comment2 = commentRecord.toObject();
        Assert.assertEquals(1, comment2.getPost().getId().intValue());
    }

    @Test
    public void 多态_toggle_BelongsTo() {
        Record<Comment, Long> commentRecord = commentModel.findOrFail(1).with(Comment::getPost);
        Comment comment = commentRecord.toObject();
        Assert.assertEquals(3, comment.getPost().getId().intValue());

        commentRecord.bind(Comment::getPost).toggle(1);

        Comment comment2 = commentRecord.toObject();
        Assert.assertEquals(1, comment2.getPost().getId().intValue());

        commentRecord.bind(Comment::getPost).toggle(1);

        Comment comment3 = commentRecord.toObject();
        Assert.assertNull(comment3.getPost());
    }

    @Test
    public void 多态_attach_BelongsToMany() {
        Record<Post, Long> postRecord = postModel.findOrFail(3).with(Post::getImagesWithMorph);
        Post post = postRecord.toObject();
        Assert.assertEquals(2, post.getImagesWithMorph().size());
        Assert.assertEquals(3, post.getImagesWithMorph().get(0).getId().intValue());
        Assert.assertEquals(4, post.getImagesWithMorph().get(1).getId().intValue());

        postRecord.bind(Post::getImagesWithMorph).attach(Arrays.asList(4,7,8));

        Post post2 = postRecord.toObject();
        Assert.assertEquals(4, post2.getImagesWithMorph().size());
    }

    @Test
    public void 多态_detach_BelongsToMany() {
        Record<Post, Long> postRecord = postModel.findOrFail(3).with(Post::getImagesWithMorph);
        Post post = postRecord.toObject();
        Assert.assertEquals(2, post.getImagesWithMorph().size());
        Assert.assertEquals(3, post.getImagesWithMorph().get(0).getId().intValue());
        Assert.assertEquals(4, post.getImagesWithMorph().get(1).getId().intValue());

        postRecord.bind(Post::getImagesWithMorph).detach(Arrays.asList(4,7,8));

        Post post2 = postRecord.toObject();
        Assert.assertEquals(1, post2.getImagesWithMorph().size());
    }

    @Test
    public void 多态_sync_BelongsToMany() {
        Record<Post, Long> postRecord = postModel.findOrFail(3).with(Post::getImagesWithMorph);
        Post post = postRecord.toObject();
        Assert.assertEquals(2, post.getImagesWithMorph().size());
        Assert.assertEquals(3, post.getImagesWithMorph().get(0).getId().intValue());
        Assert.assertEquals(4, post.getImagesWithMorph().get(1).getId().intValue());

        postRecord.bind(Post::getImagesWithMorph).sync(Arrays.asList(4,7,8));

        Post post2 = postRecord.toObject();
        Assert.assertEquals(3, post2.getImagesWithMorph().size());
    }

    @Test
    public void 多态_toggle_BelongsToMany() {
        Record<Post, Long> postRecord = postModel.findOrFail(3).with(Post::getImagesWithMorph);
        Post post = postRecord.toObject();
        Assert.assertEquals(2, post.getImagesWithMorph().size());
        Assert.assertEquals(3, post.getImagesWithMorph().get(0).getId().intValue());
        Assert.assertEquals(4, post.getImagesWithMorph().get(1).getId().intValue());

        postRecord.bind(Post::getImagesWithMorph).toggle(Arrays.asList(4,7,8));

        Post post2 = postRecord.toObject();
        Assert.assertEquals(3, post2.getImagesWithMorph().size());
        Assert.assertEquals(3, post2.getImagesWithMorph().get(0).getId().intValue());
        Assert.assertEquals(7, post2.getImagesWithMorph().get(1).getId().intValue());
        Assert.assertEquals(8, post2.getImagesWithMorph().get(2).getId().intValue());
    }

}
