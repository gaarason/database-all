package gaarason.database.contract.support;

/**
 * ID 生成器
 */
public interface IdGenerator<K> {

    /**
     * 生成主键值
     * @return 主键值
     */
    K nextId();

    /**
     * long 雪花ID
     */
    interface SnowFlakesID extends IdGenerator<Long> {

    }

    /**
     * char 36
     */
    interface UUID36 extends IdGenerator<String> {

    }

    /**
     * char 32
     */
    interface UUID32 extends IdGenerator<String> {

    }

    /**
     * 自定义
     */
    interface Custom extends IdGenerator<Object> {

    }

    /**
     * 自动判断,
     */
    interface Auto extends IdGenerator<Object> {

    }

    /**
     * 永不自动
     */
    interface Never extends IdGenerator<Object> {

    }


}
