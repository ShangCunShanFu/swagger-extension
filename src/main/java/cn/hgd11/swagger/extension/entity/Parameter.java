package cn.hgd11.swagger.extension.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**************************************
 * Copyright (C), Navinfo
 * Package: 
 * @author: 尚村山夫
 * @date: Created in 2019/8/21 14:41
 * @description:
 **************************************/
@Data
public class Parameter {

    private String description;
    private String in;
    private String name;
    private boolean required;
    private JSONObject schema;
//    private Hgd11SwaggerSchema schema;
}
