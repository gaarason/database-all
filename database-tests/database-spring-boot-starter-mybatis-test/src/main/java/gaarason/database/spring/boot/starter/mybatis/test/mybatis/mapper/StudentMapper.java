package gaarason.database.spring.boot.starter.mybatis.test.mybatis.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface StudentMapper {

    @Update("update student set age = #{age} where id = #{id}")
    int updateAgeById(@Param("id") Integer id, @Param("age") Integer age);

}