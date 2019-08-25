//package cn.hgd11.swagger.extension.config;
//
//import org.springframework.beans.BeansException;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.stereotype.Component;
//
///**************************************
// * Copyright (C), Navinfo
// * Package:
// * @author: 尚村山夫
// * @date: Created in 2019/8/14 09:31
// * @description:
// **************************************/
//@Component
//public class SpringContext implements ApplicationContextAware {
//    /**
//     * Spring应用上下文
//     */
//    private static ApplicationContext applicationContext;
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        SpringContext.applicationContext=applicationContext;
//    }
//
//    /**
//     * 获取Spring应用上下文
//     * @return
//     */
//    public static ApplicationContext getApplicationContext() {
//        return applicationContext;
//    }
//
//
//    public static Object getBean(String beanName){
//        return applicationContext.getBean(beanName);
//    }
//
//    public static <T> T getBeanByClassType(Class<T> clazz){
//        return applicationContext.getBean(clazz);
//    }
//}
