package gaarason.database.contract.support;

import gaarason.database.exception.SerializeException;

import java.io.*;
import java.util.Base64;

/**
 * 序列化接口
 */
public interface ExtendedSerializable extends Externalizable {

    /**
     * 反序列化到指定对象
     * @param bytes 序列化byte[]
     * @param <T> 对象类型
     * @return 指定对象
     */
    @SuppressWarnings("unchecked")
    static <T extends ExtendedSerializable> T deserialize(byte[] bytes) {
        try (ByteArrayInputStream b = new ByteArrayInputStream(bytes); ObjectInputStream o = new ObjectInputStream(b)) {
            return (T) o.readObject();
        } catch (Throwable e) {
            throw new SerializeException(e);
        }
    }

    /**
     * 反序列化到指定对象
     * @param serializeStr 序列化String
     * @param <T> 对象类型
     * @return 指定对象
     */
    static <T extends ExtendedSerializable> T deserialize(String serializeStr) {
        byte[] bytes = Base64.getUrlDecoder().decode(serializeStr);
        return deserialize(bytes);
    }

    /**
     * 序列化到 byte[]
     * @return byte[]
     */
    default byte[] serialize() {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream(); ObjectOutputStream o = new ObjectOutputStream(b)) {
            o.writeObject(this);
            return b.toByteArray();
        } catch (Throwable e) {
            throw new SerializeException(e);
        }
    }

    /**
     * 序列化到 String
     * @return String
     */
    default String serializeToString() {
        return Base64.getUrlEncoder().encodeToString(serialize());
    }
}
