package cn.hgd11.swagger.extension.aop;

import cn.hgd11.swagger.extension.converter.Hgd11SwaggerConverter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**************************************
 * Copyright (C), Navinfo
 * Package: 
 * @author: 尚村山夫
 * @date: Created in 2019/9/3 13:25
 * @description:
 **************************************/
@Aspect
@Configuration
public class Hgd11SwaggerAspect {

    @Autowired
    private Hgd11SwaggerConverter hgd11SwaggerConverter;

    @Pointcut("execution(public * springfox.documentation.swagger2.web.Swagger2Controller.getDocumentation(..))")
    public void getDocumentation() { }
    @Around("getDocumentation()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = new Object();

        Object write;
        try {
            result = joinPoint.proceed();
            write = hgd11SwaggerConverter.write(result);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
        return write == null ? result : write;
    }
}
