package cn.hgd11.swagger.extension.message;

import cn.hgd11.swagger.extension.annotation.Hgd11SwaggerModel;
import cn.hgd11.swagger.extension.annotation.Hgd11SwaggerResponseProperties;
import cn.hgd11.swagger.extension.annotation.Hgd11SwaggerResponseProperty;
import cn.hgd11.swagger.extension.config.Hgd11SwaggerExtConfig;
import cn.hgd11.swagger.extension.entity.Hgd11SwaggerSchema;
import cn.hgd11.swagger.extension.entity.MethodEntity;
import cn.hgd11.swagger.extension.entity.RefDesc;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import springfox.documentation.spring.web.json.Json;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author ：尚村山夫
 * @date ：Created in 2019/8/13 22:52
 * @modified By：
 */
public class Hgd11FastJsonHttpMessageConverter extends FastJsonHttpMessageConverter {

    private Environment environment;

    private Hgd11SwaggerExtConfig swaggerExtConfig;

    public Hgd11FastJsonHttpMessageConverter() {
    }

    public Hgd11FastJsonHttpMessageConverter(Environment environment, Hgd11SwaggerExtConfig swaggerExtConfig) {
        this.environment = environment;
        this.swaggerExtConfig = swaggerExtConfig;
    }

    @Override
    public void write(Object o, Type type, MediaType contentType, HttpOutputMessage outputMessage)
        throws IOException, HttpMessageNotWritableException {
        if (o instanceof Json) {
            // 说明要转换的是swagger的结果集
            Map<MethodEntity, Method> pathMetohdMap = swaggerExtConfig.getPathMetohdMap();

            /*
             * 获取待整改的数据，并将其转换为com.alibaba.fastjson.JSONObject
             * 在转换时，要指定Feature.DisableSpecialKeyDetect，因为在待整改的数据结果中含有"$","#"这样的特殊字符
             */
            Json json = (Json) o;
            String value = json.value();
            JSONObject rawSwaggerJson = JSONObject.parseObject(value, Feature.DisableSpecialKeyDetect);
            // 对rawSwaggerJson结构进行初始化，这步骤看起来很鸡肋，却是必要的
            initRawSwaggerJson(rawSwaggerJson);

            // 结果集封装在paths中
            JSONObject paths = rawSwaggerJson.getJSONObject("paths");
            if (paths != null) {
                for (Map.Entry<String, Object> e : paths.entrySet()) {
                    // 遍历出路径
                    MethodEntity methodEntity = new MethodEntity();
                    methodEntity.setUri(e.getKey());
                    JSONObject path = (JSONObject) e.getValue();
                    if (path == null || path.isEmpty()) {
                        continue;
                    }

                    for (Map.Entry<String, Object> e2 : path.entrySet()) {
                        /*
                         * 遍历出该路径下有几种请求方式
                         * 如:   /api/v1/test
                         * 路径下有GET POST两种请求
                         */
                        // 从环境配置中获取该请求对应的方法
                        methodEntity.setRequestMethod(e2.getKey());
                        Method method = pathMetohdMap.get(methodEntity);

                        if (method != null) {
                            /*
                             * 如果该请求没有加载对应的方法，则不进行任何操作
                             * 这可能是由于启动加载时出错
                             */
                            Hgd11SwaggerModel hgd11SwaggerModel = method.getAnnotation(Hgd11SwaggerModel.class);
                            if (hgd11SwaggerModel == null) {
                                continue;
                            }
                            JSONObject pathValue = (JSONObject) e2.getValue();
                            JSONObject responses = pathValue.getJSONObject("responses");
                            JSONObject seccessJobj = responses.getJSONObject("200");

                            Hgd11SwaggerSchema schema = new Hgd11SwaggerSchema();
                            schema.setDescription(hgd11SwaggerModel.description());
                            schema.setTitle(hgd11SwaggerModel.title());
                            schema.setType(hgd11SwaggerModel.type());
                            Map<String, Object> properties = new HashMap<>();
                            schema.setProperties(properties);

                            Hgd11SwaggerResponseProperties hgd11SwaggerProperties = hgd11SwaggerModel.properties();
                            Hgd11SwaggerResponseProperty[] hgd11SwaggerPropertsSun = hgd11SwaggerProperties.value();

                            // 生成Hgd11SwaggerResponseProperty中名称与本身实例的映射关系
                            Map<String, Hgd11SwaggerResponseProperty> indexHgd11Map = new HashMap<>();

                            // 子节点不在最外层进行遍历
                            List<String> children = new ArrayList<>();

                            /*
                             * 1-> 添加indexHgd11Map映射
                             * 2-> 找出所有子节点
                             */
                            for (Hgd11SwaggerResponseProperty hgd11SwaggerProperty : hgd11SwaggerPropertsSun) {
                                String index = hgd11SwaggerProperty.index();
                                indexHgd11Map.put(index, hgd11SwaggerProperty);

                                String[] child = hgd11SwaggerProperty.child();
                                children.addAll(Arrays.asList(child));
                            }

                            // 执行整改结果
                            for (Hgd11SwaggerResponseProperty hgd11SwaggerProperty : hgd11SwaggerPropertsSun) {
                                String index = hgd11SwaggerProperty.index();
                                if (children.contains(index)) {
                                    // 子节点不参与最外层遍历
                                    continue;
                                }
                                gggg(properties, hgd11SwaggerProperty, indexHgd11Map);
                            }
                            seccessJobj.put("schema", schema);
                        }
                    }

                }
            }
            o = new Json(rawSwaggerJson.toJSONString());
        }
        super.write(o, type, contentType, outputMessage);
    }

    /**
     * 递归整改结果集
     *
     * @param properties           归外层结果对象
     * @param hgd11SwaggerProperty 当前属性对应的Hgd11SwaggerResponseProperty对象,如：<br/>
     *                             '@Hgd11SwaggerResponseProperty(name = "name", index = "deptName", description = "部门名称", example = "开发部")'<br/>
     * @param indexHgd11Map        Hgd11SwaggerResponseProperty对应索引及其实例的Map集合
     */
    private void gggg(Map<String, Object> properties, Hgd11SwaggerResponseProperty hgd11SwaggerProperty,
        Map<String, Hgd11SwaggerResponseProperty> indexHgd11Map) {
        String[] childArray = hgd11SwaggerProperty.child();
        if (childArray.length == 0) {
            RefDesc refDesc = new RefDesc();
            refDesc.setDescription(hgd11SwaggerProperty.description());
            refDesc.setType(hgd11SwaggerProperty.dataType());
            refDesc.setExample(hgd11SwaggerProperty.example());
            refDesc.setFormat(hgd11SwaggerProperty.format());
            String name = hgd11SwaggerProperty.name();
            properties.put(name, refDesc);
        } else {
            Hgd11SwaggerSchema childSchema = new Hgd11SwaggerSchema();
            Map<String, Object> childMap = new HashMap<>();
            childSchema.setProperties(childMap);
            for (String childIndex : childArray) {
                Hgd11SwaggerResponseProperty childProperty = indexHgd11Map.get(childIndex);
                gggg(childMap, childProperty, indexHgd11Map);
            }
            properties.put(hgd11SwaggerProperty.name(), childSchema);
        }
    }

    /**
     * 初始化wawSwaggerJson
     *
     * @param rawSwaggerJson 待初始化的结构
     */
    private void initRawSwaggerJson(JSONObject rawSwaggerJson) {
        // paths:value
        JSONObject paths = JSONObject.parseObject(rawSwaggerJson.getString("paths"), Feature.DisableSpecialKeyDetect);
        for (Map.Entry<String, Object> pathse : paths.entrySet()) {
            // /api/v1/project(uri):value
            JSONObject pathsValue =
                JSONObject.parseObject(String.valueOf(pathse.getValue()), Feature.DisableSpecialKeyDetect);
            for (Map.Entry<String, Object> pathsValuee : pathsValue.entrySet()) {
                // post|get|delete|put:value
                JSONObject pathsValueeValue =
                    JSONObject.parseObject(String.valueOf(pathsValuee.getValue()), Feature.DisableSpecialKeyDetect);
                JSONObject responses =
                    JSONObject.parseObject(pathsValueeValue.getString("responses"), Feature.DisableSpecialKeyDetect);
                // 200|500|400|401:value
                // 从中拿出200结果集
                JSONObject seccessJobj =
                    JSONObject.parseObject(responses.getString("200"), Feature.DisableSpecialKeyDetect);
                // 从中拿出scheme
                JSONObject schema =
                    JSONObject.parseObject(seccessJobj.getString("schema"), Feature.DisableSpecialKeyDetect);
                // ***************************the end***************************
                seccessJobj.put("schema", schema);
                responses.put("200", seccessJobj);
                pathsValueeValue.put("responses", responses);
                pathsValue.put(pathsValuee.getKey(), pathsValueeValue);
            }
            paths.put(pathse.getKey(), pathsValue);
        }
        rawSwaggerJson.put("paths", paths);
    }
}