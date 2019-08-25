package cn.hgd11.swagger.extension.annotation;

import java.lang.annotation.*;

/**************************************
 * Copyright (C), Navinfo
 * Package:
 * @author: 尚村山夫
 * @date: Created in 2019/8/14 13:58
 * @description:
 **************************************/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Hgd11SwaggerResponseProperties {
    Hgd11SwaggerResponseProperty[] value();
}
