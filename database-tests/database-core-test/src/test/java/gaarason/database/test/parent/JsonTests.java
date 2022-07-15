package gaarason.database.test.parent;

import gaarason.database.contract.connection.GaarasonDataSource;
import gaarason.database.test.models.normal.JsonTestModel;
import gaarason.database.test.parent.base.BaseTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Map;

@Slf4j
@FixMethodOrder(MethodSorters.JVM)
abstract public class JsonTests extends BaseTests {

    protected static JsonTestModel jsonTestModel = new JsonTestModel();

    protected GaarasonDataSource getGaarasonDataSource() {
        return jsonTestModel.getGaarasonDataSource();
    }


    /**
     * // 定义
     * @Json private Map<Object, Object> json;
     * @Json private List<Object> json;
     * <p>
     * <p>
     * // 查询 条件
     * where("json.key[0].key", "sss")     ->   	where json->'$.key[0].key' = "sss"
     * where("json.key.0.key", "sss")
     * <p>
     * // 查询 结果
     * select("json")
     * select("json.key.0.key as cccc")			->		select json->'$.key.0.key' as cccc
     * <p>
     * // 指定更新
     * data("json.key.0.key", "sss")
     * <p>
     * //
     */
    @Test
    public void 原生JSON操作() {
        final Map<String, Object> map = jsonTestModel.newQuery().selectFunction("JSON_EXTRACT",
            "json_array_column, \"$[1]\"", "xxxx").firstOrFail().toMap();
        System.out.println(map);
    }

    @Test
    public void 新增() {

    }

    @Test
    public void 查询() {

    }

    @Test
    public void 查询_指定简单值() {

    }

    @Test
    public void 更新_全部() {

    }

    @Test
    public void 更新_单个简单值() {

    }

    @Test
    public void 更新_单个复杂值() {

    }

    @Test
    public void 更新_移除指定值() {

    }

}
