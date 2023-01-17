package gaarason.database.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import gaarason.database.appointment.FinalVariable;
import gaarason.database.exception.JsonProcessException;
import gaarason.database.lang.Nullable;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;

/**
 * @author xt
 */
public class JsonUtils {

    /**
     * jackson 一般对象
     */
    private static final ObjectMapper MAPPER = intMapper();

    /**
     * 获取json对象, 每次均会返回相同配置的全新引用对象(防止同地址对象属性被修改的情况)
     * 应避免重复调用
     * @return ObjectMapper
     */
    public static ObjectMapper getMapper() {
        return intMapper();
    }

    /**
     * 初始化
     * 支持LocalTime系列
     * @return ObjectMapper
     */
    private static ObjectMapper intMapper() {
        ObjectMapper mapper = new ObjectMapper()
            // 确定解析器是否允许使用未加引号的字段名（Javascript允许，但JSON规范不允许）
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            // 在将java对象序列化到json字符时, 如果值非null,则总是存在其属性
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            // 在将json字符反序列到java对象时, 支持单个json字符到java集合的映射
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            // 在将json字符反序列到java对象时, 支持json数组(仅含单个json字符的时候)到java单个属性的映射 (与 ACCEPT_SINGLE_VALUE_AS_ARRAY 相反)
            .enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
            // 空字符串反序列化为 null 对象 (此设置只用于 POJO 或一些结构化的值（比如 java.util.Maps、java.util.Collection）)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            // 下划线转小驼峰
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            // 当java对象和json字符, 不是完美匹配时, 不需要抛出异常
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class,
            new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(FinalVariable.Timestamp.DEFAULT_DATE_TIME_FORMAT)));
        javaTimeModule.addSerializer(LocalDate.class,
            new LocalDateSerializer(DateTimeFormatter.ofPattern(FinalVariable.Timestamp.DEFAULT_DATE_FORMAT)));
        javaTimeModule.addSerializer(LocalTime.class,
            new LocalTimeSerializer(DateTimeFormatter.ofPattern(FinalVariable.Timestamp.DEFAULT_TIME_FORMAT)));

        javaTimeModule.addDeserializer(LocalDateTime.class, new CustomizeLocalDateTimeDeserializer(
            DateTimeFormatter.ofPattern(FinalVariable.Timestamp.DEFAULT_DATE_TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDate.class,
            new LocalDateDeserializer(DateTimeFormatter.ofPattern(FinalVariable.Timestamp.DEFAULT_DATE_FORMAT)));
        javaTimeModule.addDeserializer(LocalTime.class,
            new LocalTimeDeserializer(DateTimeFormatter.ofPattern(FinalVariable.Timestamp.DEFAULT_TIME_FORMAT)));

        // Date序列化和反序列化
        javaTimeModule.addSerializer(Date.class, new JsonSerializer<Date>() {
            @Override
            public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
                SimpleDateFormat formatter = new SimpleDateFormat(FinalVariable.Timestamp.DEFAULT_DATE_TIME_FORMAT);
                String formattedDate = formatter.format(date);
                jsonGenerator.writeString(formattedDate);
            }
        });

        javaTimeModule.addDeserializer(Date.class, new JsonDeserializer<Date>() {
            @Nullable
            @Override
            public Date deserialize(JsonParser parser, DeserializationContext context)
                throws IOException {
                if (parser.hasToken(JsonToken.VALUE_STRING) || parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
                    String dateString = parser.getText();
                    if (!dateString.contains("-")) {
                        return new Date(Long.parseLong(dateString));
                    }
                    SimpleDateFormat format = new SimpleDateFormat(FinalVariable.Timestamp.DEFAULT_DATE_TIME_FORMAT);
                    try {
                        return format.parse(dateString);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (parser.isExpectedStartArrayToken()) {
                    JsonToken t = parser.nextToken();
                    if (t == JsonToken.END_ARRAY) {
                        return null;
                    }
                    if (context.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS) &&
                        (t == JsonToken.VALUE_STRING || t == JsonToken.VALUE_EMBEDDED_OBJECT ||
                            t == JsonToken.VALUE_NUMBER_INT)) {
                        final Date parsed = deserialize(parser, context);
                        if (parser.nextToken() != JsonToken.END_ARRAY) {
                            throw new RuntimeException();
                        }
                        return parsed;
                    }
                }
                throw new RuntimeException();
            }
        });
        // 注册新的模块到objectMapper
        mapper.registerModule(javaTimeModule);
        return mapper;
    }

    /**
     * 内部专用, 获取json对象
     * @return ObjectMapper
     */
    static ObjectMapper getTheMapper() {
        return MAPPER;
    }

    /**
     * 对象 转 json字符串
     * @param obj 对象
     * @return json 字符串
     * @throws JsonProcessException 序列化异常
     */
    public static String objectToJson(@Nullable Object obj) throws JsonProcessException {
        if (null == obj) {
            return "{}";
        }
        if (obj.getClass().isArray() && Array.getLength(obj) == 0) {
            return "[]";
        } else if (obj instanceof Collection && ((Collection<?>) obj).isEmpty()) {
            return "[]";
        }
        if (ObjectUtils.isEmpty(obj)) {
            return "{}";
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new JsonProcessException(obj, e);
        }
    }

    /**
     * json字符串 转 对象
     * @param json json 字符串
     * @param valueTypeRef 对象类型 eg: new TypeReference<ResultVO<List<List<String>>>>() {}
     * @param <T> 对象类型
     * @return 对象
     * @throws JsonProcessException 序列化异常
     */
    public static <T> T jsonToObject(@Nullable String json, TypeReference<T> valueTypeRef) throws JsonProcessException {
        if (ObjectUtils.isEmpty(json)) {
            return jsonToObject("{}", valueTypeRef);
        }
        try {
            return MAPPER.readValue(json, valueTypeRef);
        } catch (Throwable e) {
            throw new JsonProcessException(json, valueTypeRef, e);
        }
    }

    /**
     * json字符串 转 对象
     * @param json json 字符串
     * @param valueTypeRef 对象类型
     * @param <T> 对象类型
     * @return 对象
     * @throws JsonProcessException 序列化异常
     */
    public static <T> T jsonToObject(@Nullable String json, Class<T> valueTypeRef) throws JsonProcessException {
        if (ObjectUtils.isEmpty(json)) {
            return jsonToObject("{}", valueTypeRef);
        }
        try {
            return MAPPER.readValue(json, valueTypeRef);
        } catch (Throwable e) {
            throw new JsonProcessException(json, valueTypeRef, e);
        }
    }

    /**
     * json字符串 转 对象
     * @param json json 字符串
     * @param type 对象类型
     * @param <T> 对象类型
     * @return 对象
     * @throws JsonProcessException 序列化异常
     */
    public static <T> T jsonToObject(@Nullable String json, Type type) throws JsonProcessException {
        if (ObjectUtils.isEmpty(json)) {
            return jsonToObject("{}", type);
        }
        try {
            return MAPPER.readValue(json, new TypeReference<T>() {
                @Override
                public Type getType() {
                    return type;
                }
            });
        } catch (Throwable e) {
            throw new JsonProcessException(json, type, e);
        }
    }

    /**
     * 通过json完成对象间的转化
     * @param obj 原对象
     * @param type 目标类型
     * @param <T> 类型
     * @return 新对象
     * @throws JsonProcessException 序列化异常
     */
    public static <T> T ObjectToObject(@Nullable Object obj, Type type) throws JsonProcessException {
        return jsonToObject(objectToJson(obj), type);
    }

    /**
     * 时间反序列化
     * 增加对时间戳的支持
     */
    static class CustomizeLocalDateTimeDeserializer extends LocalDateTimeDeserializer {

        public CustomizeLocalDateTimeDeserializer(DateTimeFormatter formatter) {
            super(formatter);
        }

        @Override
        @Nullable
        public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {

            if (parser.hasTokenId(JsonTokenId.ID_STRING) || parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
                // 如果是13位, 那么可能是 1659110400000 这种的时间戳
                String text = parser.getText();
                if (text.length() == 13) {
                    // 是否是数字
                    boolean flag = true;
                    char[] chars = text.toCharArray();
                    for (int i = 0; i < 13; i++) {
                        flag &= Character.isDefined(chars[i]);
                    }
                    if (flag) {
                        long timestamp = Long.parseLong(text);
                        // 强制时区 +8
                        return LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.ofHours(8));
                    }
                }
                return _fromString(parser, context, text);
            }

            if (parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
                _throwNoNumericTimestampNeedTimeZone(parser, context);
            }

            // 30-Sep-2020, tatu: New! "Scalar from Object" (mostly for XML)
            if (parser.isExpectedStartObjectToken()) {
                return _fromString(parser, context, context.extractScalarFromObject(parser, this, handledType()));
            }
            if (parser.isExpectedStartArrayToken()) {
                JsonToken t = parser.nextToken();
                if (t == JsonToken.END_ARRAY) {
                    return null;
                }
                if ((t == JsonToken.VALUE_STRING || t == JsonToken.VALUE_EMBEDDED_OBJECT) &&
                    context.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
                    final LocalDateTime parsed = deserialize(parser, context);
                    if (parser.nextToken() != JsonToken.END_ARRAY) {
                        handleMissingEndArrayForSingle(parser, context);
                    }
                    return parsed;
                }
                if (t == JsonToken.VALUE_NUMBER_INT) {
                    LocalDateTime result;

                    int year = parser.getIntValue();
                    int month = parser.nextIntValue(-1);
                    int day = parser.nextIntValue(-1);
                    int hour = parser.nextIntValue(-1);
                    int minute = parser.nextIntValue(-1);

                    t = parser.nextToken();
                    if (t == JsonToken.END_ARRAY) {
                        result = LocalDateTime.of(year, month, day, hour, minute);
                    } else {
                        int second = parser.getIntValue();
                        t = parser.nextToken();
                        if (t == JsonToken.END_ARRAY) {
                            result = LocalDateTime.of(year, month, day, hour, minute, second);
                        } else {
                            int partialSecond = parser.getIntValue();
                            if (partialSecond < 1_000 &&
                                !context.isEnabled(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS))
                                partialSecond *= 1_000_000; // value is milliseconds, convert it to nanoseconds
                            if (parser.nextToken() != JsonToken.END_ARRAY) {
                                throw context.wrongTokenException(parser, handledType(), JsonToken.END_ARRAY,
                                    "Expected array to end");
                            }
                            result = LocalDateTime.of(year, month, day, hour, minute, second, partialSecond);
                        }
                    }
                    return result;
                }
                context.reportInputMismatch(handledType(),
                    "Unexpected token (%s) within Array, expected VALUE_NUMBER_INT", t);
            }
            if (parser.hasToken(JsonToken.VALUE_EMBEDDED_OBJECT)) {
                return (LocalDateTime) parser.getEmbeddedObject();
            }

            return _handleUnexpectedToken(context, parser, "Expected array or string.");
        }
    }
}