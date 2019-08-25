package cn.hgd11.swagger.extension.entity;

import lombok.Data;

/**************************************
 * Copyright (C), Navinfo
 * Package: 
 * @author: 尚村山夫
 * @date: Created in 2019/8/17 11:22
 * @description:
 **************************************/
@Data
public class RefDesc {

    /**
     * A brief description of this property.
     */
    private String description;

    /**
     * The data type of the parameter.
     * <p>
     * This can be the class name or a primitive. The value will override the data type as read from the class
     * property.
     */
    private String type;

    /**
     * A sample value for the property.
     */
    private String example;

    /**
     * The format of the {@link #type}
     */
    private String format;
}
