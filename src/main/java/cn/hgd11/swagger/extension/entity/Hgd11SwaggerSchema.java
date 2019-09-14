package cn.hgd11.swagger.extension.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**************************************
 * Copyright (C), Navinfo
 * Package: 
 * @author: 尚村山夫
 * @date: Created in 2019/8/17 11:14
 * @description:
 **************************************/
@Data
public class Hgd11SwaggerSchema {

    public static final String REF_PREFIX="#/definitions/";

    private String type;
    private String title;
    private String description;
    private List<String> required;
    private Map<String,Object> properties;

    public Hgd11SwaggerSchema() {
    }
}
