package gaarason.database.utils;

import gaarason.database.exception.CloneNotSupportedRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class CopyUtil {

    /**
     * 通过序列化对对象进行递归copy
     * @param original 源对象
     * @param <T> 对象所属的类
     * @return 全新的对象
     * @throws CloneNotSupportedRuntimeException 克隆异常
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T original){
        try{
            ByteArrayOutputStream bis = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bis);
            oos.writeObject(original);
            oos.flush();
            ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bis.toByteArray()));
            return  (T) input.readObject();
        }catch (Throwable e){
            throw new CloneNotSupportedRuntimeException(e.getMessage(), e);
        }
    }
}
