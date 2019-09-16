package cn.hgd11.swagger.extension.converter;

import cn.hgd11.swagger.extension.annotation.*;
import cn.hgd11.swagger.extension.config.Hgd11SwaggerPathMethodMapping;
import cn.hgd11.swagger.extension.entity.Hgd11SwaggerSchema;
import cn.hgd11.swagger.extension.entity.MethodEntity;
import cn.hgd11.swagger.extension.entity.Parameter;
import cn.hgd11.swagger.extension.entity.RefDesc;
import cn.hgd11.swagger.extension.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import springfox.documentation.spring.web.json.Json;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**************************************
 * Copyright (C), Navinfo
 * Package:
 * @author: 尚村山夫
 * @date: Created in 2019/9/7 16:54
 * @description:
 **************************************/
public class Hgd11SwaggerConverter {

    private static final String REF_PREFFIX = "#/definitions/";

    private static final String REF = "$ref";

    private Environment environment;

    private Hgd11SwaggerPathMethodMapping swaggerExtConfig;

    public Hgd11SwaggerConverter() {
    }

    public Hgd11SwaggerConverter(Environment environment, Hgd11SwaggerPathMethodMapping swaggerExtConfig) {
        this.environment = environment;
        this.swaggerExtConfig = swaggerExtConfig;
    }

    public Object write(Object o) throws IOException, HttpMessageNotWritableException {
        ResponseEntity<Json> responseEntity = null;
        if (o.getClass() == ResponseEntity.class) {
            // 说明要转换的是swagger的结果集
            Map<MethodEntity, Method> pathMetohdMap = swaggerExtConfig.getPathMethodMap();

            /*
             * 获取待整改的数据，并将其转换为com.alibaba.fastjson.JSONObject
             * 在转换时，要指定Feature.DisableSpecialKeyDetect，因为在待整改的数据结果中含有"$","#"这样的特殊字符
             */
            responseEntity = (ResponseEntity<Json>) o;
            Json json = responseEntity.getBody();
            assert json != null;
            String value = json.value();
            JSONObject rawSwaggerJson = JSONObject.parseObject(value, Feature.DisableSpecialKeyDetect);
            // 对rawSwaggerJson结构进行初始化，这步骤看起来很鸡肋，却是必要的
            initRawSwaggerJson(rawSwaggerJson);
            Json jsonO = convert(pathMetohdMap, rawSwaggerJson);
            responseEntity = new ResponseEntity<>(jsonO, HttpStatus.OK);
        }

        return responseEntity == null ? o : responseEntity;
    }

    private Json convert(Map<MethodEntity, Method> pathMethodMap, JSONObject rawSwaggerJson) {
        Json o;// 结果集封装在paths中
        JSONObject paths = rawSwaggerJson.getJSONObject("paths");
        JSONObject definitions = rawSwaggerJson.getJSONObject("definitions");

        if (definitions == null) {
            // 在原有的数据结构中，有可能还没有定义过实体
            definitions = new JSONObject();
            rawSwaggerJson.put("definitions", definitions);
        }
        // 修改definitions
        definitions(pathMethodMap, definitions);

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
                    Method method = pathMethodMap.get(methodEntity);

                    if (method != null) {
                        response(e2, method);
                        parameter(e2, method);
                    }
                }

            }
        }
        o = new Json(rawSwaggerJson.toJSONString());
        return o;
    }

    private void definitions(Map<MethodEntity, Method> pathMetohdMap, JSONObject definitions) {
        // 将pathMethodMap中的方法整理为definitions中的对应
        for (Map.Entry<MethodEntity, Method> e : pathMetohdMap.entrySet()) {
            Method method = e.getValue();
            // 在方法上有没有Hgd11SwaggerModel注解
            Hgd11SwaggerModel hgd11SwaggerModel = method.getAnnotation(Hgd11SwaggerModel.class);
            if (hgd11SwaggerModel != null) {
                definitionRef(definitions, hgd11SwaggerModel);
            }

            java.lang.reflect.Parameter[] parameters = method.getParameters();
            for (java.lang.reflect.Parameter parameter : parameters) {
                Hgd11SwaggerParameter hgd11SwaggerParameter = parameter.getAnnotation(Hgd11SwaggerParameter.class);
                if (hgd11SwaggerParameter != null) {
                    Hgd11SwaggerModel hgd11SwaggerModelInParam = hgd11SwaggerParameter.model();
                    definitionRef(definitions, hgd11SwaggerModelInParam);
                }
            }
        }
    }

    private void definitionRef(JSONObject definitions, Hgd11SwaggerModel hgd11SwaggerModel) {
        Hgd11SwaggerSchema schema = new Hgd11SwaggerSchema();
        schema.setDescription(hgd11SwaggerModel.description());
        schema.setTitle(StringUtils.getOrDefault(hgd11SwaggerModel.title(), hgd11SwaggerModel.index()));
        schema.setType(hgd11SwaggerModel.type());
        Map<String, Object> properties = new HashMap<>();
        List<String> required = new ArrayList<>();
        schema.setProperties(properties);
        schema.setRequired(required);

        Hgd11SwaggerProperties hgd11SwaggerProperties = hgd11SwaggerModel.properties();
        Hgd11SwaggerProperty[] hgd11SwaggerPropertsSun = hgd11SwaggerProperties.value();
        generatorProperty(properties, required, hgd11SwaggerPropertsSun);

        definitions.put(
            StringUtils.upperFirstChar(StringUtils.getOrDefault(hgd11SwaggerModel.title(), hgd11SwaggerModel.index())),
            schema);
    }

    private void parameter(Map.Entry<String, Object> e2, Method method) {
        /*
         * 如果该请求没有加载对应的方法，则不进行任何操作
         * 这可能是由于启动加载时出错
         */
        JSONObject ref = new JSONObject();

        JSONObject pathValue = (JSONObject) e2.getValue();
        JSONArray parameters = pathValue.getJSONArray("parameters");
        if(parameters==null){
            parameters=new JSONArray();
            pathValue.put("parameters",parameters);
        }

        java.lang.reflect.Parameter[] methodParameters = method.getParameters();
        for (java.lang.reflect.Parameter tmp : methodParameters) {
            Hgd11SwaggerParameter hgd11SwaggerParameter = tmp.getAnnotation(Hgd11SwaggerParameter.class);
            Hgd11SwaggerParameterRef hgd11SwaggerParameterRef = tmp.getAnnotation(Hgd11SwaggerParameterRef.class);

            String title;

            String parameterName = tmp.getName();

            Parameter parameter = new Parameter();
            if (hgd11SwaggerParameter != null) {
                Hgd11SwaggerModel hgd11SwaggerParameterModel = hgd11SwaggerParameter.model();
                parameter.setDescription(hgd11SwaggerParameter.description());
                parameter.setIn(hgd11SwaggerParameter.in());
                parameter
                    .setName(hgd11SwaggerParameter.name().isEmpty() ? parameterName : hgd11SwaggerParameter.name());
                parameter.setRequired(hgd11SwaggerParameter.required());
                ref.put(REF, REF_PREFFIX + StringUtils.upperFirstChar(
                    StringUtils.getOrDefault(hgd11SwaggerParameterModel.title(), hgd11SwaggerParameterModel.index())));
            } else if (hgd11SwaggerParameterRef != null) {
                Hgd11SwaggerModelRef hgd11SwaggerModelRef = hgd11SwaggerParameterRef.model();
                parameter.setDescription(hgd11SwaggerParameterRef.description());
                parameter.setIn(hgd11SwaggerParameterRef.in());
                parameter.setName(
                    hgd11SwaggerParameterRef.name().isEmpty() ? parameterName : hgd11SwaggerParameterRef.name());
                parameter.setRequired(hgd11SwaggerParameterRef.required());
                ref.put(REF, REF_PREFFIX + StringUtils.upperFirstChar(hgd11SwaggerModelRef.ref()));
            } else {
                continue;
            }

            parameter.setSchema(ref);
            addParameter(parameters, parameter);
        }
    }

    private void addParameter(JSONArray parameters, Parameter parameter) {
        boolean hasAdded = false;
        for (int i = 0; i < parameters.size(); i++) {
            JSONObject exit = parameters.getJSONObject(i);
            if (exit.getString("name").equals(parameter.getName())) {
                // 说明需要将添加了Hgd11SwaggerParameter或Hgd11SwaggerParameterRef注解的参数替换原来Swagger自己扫描到的参数
                parameters.add(parameter);
                parameters.remove(i);
                hasAdded = true;
                break;
            }
        }

        if (!hasAdded) {
            parameters.add(parameter);
        }
    }

    private void response(Map.Entry<String, Object> e2, Method method) {
        /*
         * 如果该请求没有加载对应的方法，则不进行任何操作
         * 这可能是由于启动加载时出错
         */
        JSONObject ref = new JSONObject();

        Hgd11SwaggerModelRef hgd11SwaggerModelRef = method.getAnnotation(Hgd11SwaggerModelRef.class);
        Hgd11SwaggerModel hgd11SwaggerModel = method.getAnnotation(Hgd11SwaggerModel.class);

        String title;

        JSONObject pathValue = (JSONObject) e2.getValue();
        JSONObject responses = pathValue.getJSONObject("responses");
        JSONObject seccessJobj = responses.getJSONObject("200");

        if (hgd11SwaggerModelRef != null) {
            title = StringUtils.upperFirstChar(hgd11SwaggerModelRef.ref());
        } else if (hgd11SwaggerModel != null) {
            title = StringUtils
                .upperFirstChar(StringUtils.getOrDefault(hgd11SwaggerModel.title(), hgd11SwaggerModel.index()));
        } else {
            return;
        }

        ref.put(REF, REF_PREFFIX + title);
        seccessJobj.put("schema", ref);
    }

    private void generatorProperty(Map<String, Object> properties, List<String> required,
        Hgd11SwaggerProperty[] value) {
        // 生成Hgd11SwaggerResponseProperty中名称与本身实例的映射关系
        Map<String, Hgd11SwaggerProperty> indexHgd11Map = new HashMap<>();

        // 子节点不在最外层进行遍历
        List<String> children = new ArrayList<>();

        /*
         * 1-> 添加indexHgd11Map映射
         * 2-> 找出所有子节点
         */
        for (Hgd11SwaggerProperty hgd11SwaggerProperty : value) {
            String index = hgd11SwaggerProperty.index();
            indexHgd11Map.put(index, hgd11SwaggerProperty);

            String[] child = hgd11SwaggerProperty.children();
            children.addAll(Arrays.asList(child));
        }

        // 执行整改结果
        for (Hgd11SwaggerProperty hgd11SwaggerProperty : value) {
            String index = hgd11SwaggerProperty.index();
            if (children.contains(index)) {
                // 子节点不参与最外层遍历
                continue;
            }
            gggg(properties, required, hgd11SwaggerProperty, indexHgd11Map);
        }
    }

    /**
     * 递归整改结果集
     *
     * @param properties           归外层结果对象
     * @param hgd11SwaggerProperty 当前属性对应的Hgd11SwaggerResponseProperty对象,如：<br/>
     *                             '@Hgd11SwaggerResponseProperty(name = "name", index = "deptName", description = "部门名称", example = "开发部")'<br/>
     * @param indexHgd11Map        Hgd11SwaggerResponseProperty对应索引及其实例的Map集合
     */
    private void gggg(Map<String, Object> properties, List<String> required, Hgd11SwaggerProperty hgd11SwaggerProperty,
        Map<String, Hgd11SwaggerProperty> indexHgd11Map) {
        String[] childArray = hgd11SwaggerProperty.children();
        if (childArray.length == 0) {
            RefDesc refDesc = new RefDesc();
            refDesc.setDescription(hgd11SwaggerProperty.description());
            refDesc.setType(hgd11SwaggerProperty.dataType());
            refDesc.setExample(hgd11SwaggerProperty.example());
            refDesc.setFormat(hgd11SwaggerProperty.format());
            String name = hgd11SwaggerProperty.name();
            if (hgd11SwaggerProperty.required()) {
                required.add(name);
            }
            properties.put(name, refDesc);
        } else {
            Hgd11SwaggerSchema childSchema = new Hgd11SwaggerSchema();
            Map<String, Object> childMap = new HashMap<>();
            List<String> childrenQuired = new ArrayList<>();
            childSchema.setProperties(childMap);
            childSchema.setRequired(childrenQuired);
            for (String childIndex : childArray) {
                Hgd11SwaggerProperty childProperty = indexHgd11Map.get(childIndex);
                gggg(childMap, childrenQuired, childProperty, indexHgd11Map);
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
            String path = pathse.getKey();
            if (!"/test/testSwager".equals(path)) {
                continue;
            }
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
                // ********************responses*********************
                // ********************responses*********************
                // ********************responses*********************

                pathsValue.put(pathsValuee.getKey(), pathsValueeValue);
            }
            paths.put(path, pathsValue);
        }
        rawSwaggerJson.put("paths", paths);
    }
}
