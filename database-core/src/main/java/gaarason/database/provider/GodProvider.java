package gaarason.database.provider;

import gaarason.database.core.Container;
import gaarason.database.exception.AbnormalParameterException;
import gaarason.database.exception.ClassNotFoundException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GodProvider {

    /**
     * 持有容器map
     * 应该不会内存溢出, 毕竟没人一直 new Container
     */
    public static final Map<String, Container> CONTAINER_SET = new ConcurrentHashMap<>();

    /**
     * 持有容器对象
     * @param identification 唯一标识
     * @param container 容器对象
     */
    public static synchronized void add(String identification, Container container) {

        if (CONTAINER_SET.containsKey(identification)) {
            throw new AbnormalParameterException("容器唯一标识[" + identification + "]已存在, 请更换, 或先执行移除[remove]操作");
        }

        CONTAINER_SET.put(identification, container);
    }

    /**
     * 获取容器
     * @param identification 唯一标识
     * @return 容器对象
     */
    public static Container get(String identification) {
        Container container = CONTAINER_SET.get(identification);
        if (container == null) {
            throw new ClassNotFoundException("Container hashCode[" + identification + "]");
        }
        return container;
    }

    /**
     * 移除指定容器
     * @param identification 唯一标识
     */
    public static synchronized void remove(String identification) {
        CONTAINER_SET.remove(identification);
    }

}
