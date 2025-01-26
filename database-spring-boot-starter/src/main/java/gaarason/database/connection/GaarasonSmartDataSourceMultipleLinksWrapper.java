package gaarason.database.connection;

import gaarason.database.core.Container;
import gaarason.database.util.ObjectUtils;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GaarasonSmartDataSourceMultipleLinksWrapper extends GaarasonSmartDataSourceWrapper {

    /**
     * 每组链接 (写连接, 读链接)
     */
    protected final Map<String, List<List<DataSource>>> dataSourceMap;


    public GaarasonSmartDataSourceMultipleLinksWrapper(Map<String, List<List<DataSource>>> dataSourceMap, Container container) {
        // 不再使用原数据结构
        super(Collections.emptyList(), Collections.emptyList(), container);
        // 使用新的
        this.dataSourceMap = dataSourceMap;
    }

    @Override
    protected DataSource getRealDataSource(boolean isWriteOrTransaction) {
        // 选择合适的链接
        Object databaseLink = "";
//        databaseLink = getHttpServletRequest().getAttribute("databaseLink");

        List<List<DataSource>> lists = dataSourceMap.get(String.valueOf(databaseLink));

        List<DataSource> masterDataSourceList = lists.get(0);
        List<DataSource> slaveDataSourceList = lists.size() > 1 ? lists.get(1) : Collections.emptyList();
        boolean hasSlave = !ObjectUtils.isEmpty(slaveDataSourceList);

        if (!hasSlave || isWriteOrTransaction) {
            return masterDataSourceList.get(ThreadLocalRandom.current().nextInt(masterDataSourceList.size()));
        } else {
            return slaveDataSourceList.get(ThreadLocalRandom.current().nextInt(slaveDataSourceList.size()));
        }
    }

    /**
     * 2个写链接, 通过名字区分
     */
    public static GaarasonSmartDataSourceMultipleLinksWrapper build(DataSource dataSource1, DataSource dataSource2, Container container) {
        Map<String, List<List<DataSource>>> dataSourceMap = new HashMap<>();
        List<List<DataSource>> listOfName1 = dataSourceMap.computeIfAbsent("name1", k -> new ArrayList<>());
        listOfName1.add(0, Collections.singletonList(dataSource1));

        List<List<DataSource>> listOfName2 = dataSourceMap.computeIfAbsent("name2", k -> new ArrayList<>());
        listOfName2.add(0, Collections.singletonList(dataSource2));

        return new GaarasonSmartDataSourceMultipleLinksWrapper(dataSourceMap, container);
    }


    /**
     * 示例, 获取当前 web 线程的 request
     * @return request
     */
//    public static HttpServletRequest getHttpServletRequest() {
//        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        if(requestAttributes == null) {
//            throw new BusinessHPException("No HttpServletRequest");
//        }
//        return requestAttributes.getRequest();
//    }
}
