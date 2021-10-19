package com.app.service;

import com.app.service.UserService;
import com.spring.BeanPostProcessor;
import com.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


@Component
public class MiniBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (beanName.equals("userService")) {
            ((UserServiceImpl) bean).setName("user");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (beanName.equals("userService")) {
            Object proxyInstance = Proxy.newProxyInstance(MiniBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("Proxy Method");
                    return method.invoke(bean, args);
                }
            });
            return  proxyInstance;
        }
        return bean;
    }
}
