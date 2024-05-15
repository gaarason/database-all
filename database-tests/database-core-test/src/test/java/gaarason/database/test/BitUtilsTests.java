package gaarason.database.test;

import gaarason.database.util.BitUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
public class BitUtilsTests {

    @Test
    public void packAndUnpack() {
        long pack = BitUtils.pack(0, 1, 2);
        Assert.assertEquals(7, pack);

        List<Long> unpack = BitUtils.unpack(pack);
        Assert.assertEquals(0, unpack.get(0).intValue());
        Assert.assertEquals(1, unpack.get(1).intValue());
        Assert.assertEquals(2, unpack.get(2).intValue());
    }

    @Test
    public void pack() {
        long pack = BitUtils.pack(0, 1, 2);
        Assert.assertEquals(7, pack);

        long pack2 = BitUtils.pack(1, 2);
        Assert.assertEquals(6, pack2);

        long pack3 = BitUtils.pack(1, 3);
        Assert.assertEquals(10, pack3);
    }

    @Test
    public void unpack() {
        List<Long> unpack = BitUtils.unpack(7);
        Assert.assertEquals(3, unpack.size());
        Assert.assertEquals(0, unpack.get(0).intValue());
        Assert.assertEquals(1, unpack.get(1).intValue());
        Assert.assertEquals(2, unpack.get(2).intValue());

        List<Long> unpack2 = BitUtils.unpack(10);
        Assert.assertEquals(2, unpack2.size());
        Assert.assertEquals(1, unpack2.get(0).intValue());
        Assert.assertEquals(3, unpack2.get(1).intValue());
    }

    @Test
    public void setOptions() {
        long pack = BitUtils.setOptions(0L, 1L);
        pack = BitUtils.setOptions(pack, 1);
        pack = BitUtils.setOptions(pack, 1);
        Assert.assertEquals(2, pack);

        pack = BitUtils.setOptions(pack, 2, 3);
        Assert.assertEquals(14, pack);
    }

    @Test
    public void unsetOptions() {
        long pack = BitUtils.unsetOptions(14, 1);
        pack = BitUtils.unsetOptions(pack, 1);
        pack = BitUtils.unsetOptions(pack, 1);
        Assert.assertEquals(12, pack);

        pack = BitUtils.unsetOptions(pack, 2, 3);
        Assert.assertEquals(0, pack);
    }

    @Test
    public void checkOptionSet() {
        Assert.assertFalse(BitUtils.checkOptionSet(14, 0));
        Assert.assertTrue(BitUtils.checkOptionSet(14, 1));
        Assert.assertTrue(BitUtils.checkOptionSet(14, 2));
        Assert.assertTrue(BitUtils.checkOptionSet(14, 3));
        Assert.assertFalse(BitUtils.checkOptionSet(14, 4));
    }

}
