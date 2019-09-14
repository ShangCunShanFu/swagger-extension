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
@Target(ElementType.PARAMETER)
@Documented
public @interface Hgd11SwaggerParameter {
    /**
     * A brief description of this model.
     * @return
     */
    String description() default "";

    /**
     * 参数的位置，有效的参数为{@code path}, {@code query}, {@code body},
     * {@code header} or {@code form}.
     * @return
     */
    String in() default "body";

    /**
     * 实例的名称
     * @return
     */
    String name() default "";

    /**
     * 是否为必传项
     * @return
     */
    boolean required() default true;

    /**
     * The model of the parameter.
     * @return
     */
    Hgd11SwaggerModel model();
}
