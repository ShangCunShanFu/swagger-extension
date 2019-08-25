package cn.hgd11.swagger.extension.entity;

import lombok.Data;

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

    private String type;
    private String title;
    private String description;
    private Map<String,Object> properties;

    public Hgd11SwaggerSchema() {
    }
}
