package gaarason.database.contract.connection;

import javax.sql.DataSource;
import java.util.List;

public interface GaarasonDataSource extends DataSource {

    /**
     * 是否在事物中
     * @return
     */
    boolean isInTransaction();

    /**
     * 设置进入事物标记
     */
    void setInTransaction();

    /**
     * 移除计入事物标记
     */
    void setOutTransaction();

    /**
     * 获取读写
     * @return
     */
    boolean isWrite();

    /**
     * 设置读写
     */
    void setWrite(boolean bool);

    /**
     * 得到 DataSource
     * @return DataSource
     */
    DataSource getRealDataSource();


    /**
     * 获取主要连接(写)
     * @return
     */
    List<DataSource> getMasterDataSourceList();

    /**
     * 获取从连接(读)
     * @return
     */
    List<DataSource> getSlaveDataSourceList();
}
