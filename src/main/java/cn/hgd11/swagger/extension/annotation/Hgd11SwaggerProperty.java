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
public @interface Hgd11SwaggerProperty {

    /**
     * A brief description of this property.
     */
    String description() default "";

    /**
     * The name of the property.<br/>
     * Maybe we can consider it as a key
     *
     * @return the overridden property name
     */
    String name();

    /**
     * 属性的索引，须保证每一个Hgd11SwaggerResponseProperty对象的索引唯一
     * @return
     */
    String index();


    String[] children() default {};

    /**
     * Limits the acceptable values for this parameter.
     * <p>
     * There are three ways to describe the allowable values:
     * <ol>
     * <li>To set a list of values, provide a comma-separated list.
     * For example: {@code first, second, third}.</li>
     * <li>To set a range of values, start the value with "range", and surrounding by square
     * brackets include the minimum and maximum values, or round brackets for exclusive minimum and maximum values.
     * For example: {@code range[1, 5]}, {@code range(1, 5)}, {@code range[1, 5)}.</li>
     * <li>To set a minimum/maximum value, use the same format for range but use "infinity"
     * or "-infinity" as the second value. For example, {@code range[1, infinity]} means the
     * minimum allowable value of this parameter is 1.</li>
     * </ol>
     */
    String allowableValues() default "";

    /**
     * Allows for filtering a property from the API documentation. See io.swagger.core.filter.SwaggerSpecFilter.
     */
    String access() default "";

    /**
     * Currently not in use.
     */
    String notes() default "";

    /**
     * The data type of the parameter.
     * <p>
     * This can be the class name or a primitive. The value will override the data type as read from the class
     * property.
     */
    String dataType() default "string";

    /**
     * Specifies if the parameter is required or not.
     */
    boolean required() default false;

    /**
     * Allows explicitly ordering the property in the model.
     */
    int position() default 0;

    /**
     * A sample value for the property.
     */
    String example() default "";

    /**
     * Specifies a reference to the corresponding type definition, overrides any other metadata specified
     */

    String reference() default "";

    /**
     * Allows passing an empty value
     *
     * @since 1.5.11
     */
    boolean allowEmptyValue() default false;

    /**
     * The format of the {@link #dataType}
     */
    String format() default "";
}
