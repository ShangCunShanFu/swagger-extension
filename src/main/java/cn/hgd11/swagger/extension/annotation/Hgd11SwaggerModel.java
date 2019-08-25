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
public @interface Hgd11SwaggerModel {
    /**
     * A brief description of this model.
     * @return
     */
    String description() default "";

    /**
     * The title of the model
     * @return
     */
    String title() default "";

    /**
     * The type of the model,eg:object
     * @return
     */
    String type() default "";

    /**
     * The properties of the model.
     * @return
     */
    Hgd11SwaggerResponseProperties properties();
}
