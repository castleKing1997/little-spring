package com.spring;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MiniApplicationContext {

    private Class configClass;

    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>(); // 单例池
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public MiniApplicationContext(Class configClass) {
        this.configClass = configClass;
        // 解析配置类
        // ComponentScan注解-->扫描路径
        scan(configClass);
        // 创建单例Bean
        Enumeration<String> beanNames = beanDefinitionMap.keys();
        while (beanNames.hasMoreElements()) {
            String beanName = beanNames.nextElement();
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanName, beanDefinition);
            }
        }
    }

    public Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getClazz();
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            // 依赖注入
            for (Field declaredField:clazz.getDeclaredFields()) {
                if (declaredField.isAnnotationPresent(Autowired.class)) {
                    Object bean = getBean(declaredField.getName());
                    declaredField.setAccessible(true);
                    declaredField.set(instance, bean);
                }
            }
            // Aware
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }
            // BeanPostProcessor:before
            for (BeanPostProcessor beanPostProcessor:beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }
            // init
            if (instance instanceof InitializingBean) {
                try {
                    ((InitializingBean) instance).afterPropertiesSet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // BeanPostProcessor:before
            for (BeanPostProcessor beanPostProcessor:beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }
            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void scan(Class configClass) {
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        String path = componentScanAnnotation.value();
        path = path.replace(".", "/");
        // 扫描
        // 类加载器:
        // Bootstrap--->jre/lib
        // Ext--------->jre/ext/lib
        // App--------->classpath
        ClassLoader classLoader = MiniApplicationContext.class.getClassLoader();
        // 获取资源目录
        URL resource = classLoader.getResource(path);
        File file = new File(resource.getFile());
        if (file.isDirectory()) {
            // 扫描资源目录
            File[] files = file.listFiles();
            for (File f : files) {
                String fileName = f.getAbsolutePath();
                String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                className = className.replace("\\", ".");
                Class<?> clazz = null;
                try {
                    clazz = classLoader.loadClass(className);
                    // 如果是Bean
                    if (clazz.isAnnotationPresent(Component.class)) {
                        // 准备BeanPostProcessor
                        if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                            BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                            beanPostProcessorList.add(instance);
                        }
                        // 生成BeanDefinition
                        Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                        String beanName = componentAnnotation.value();

                        BeanDefinition beanDefinition = new BeanDefinition();
                        beanDefinition.setClazz(clazz);
                        if (clazz.isAnnotationPresent(Scope.class)) {
                            Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                            beanDefinition.setScope(scopeAnnotation.value());
                        } else {
                            beanDefinition.setScope("singleton");
                        }
                        beanDefinitionMap.put(beanName, beanDefinition);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Object getBean(String beanName) {
        if (beanDefinitionMap.containsKey(beanName)) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            Object bean;
            if (beanDefinition.getScope().equals("singleton")) {
                bean = singletonObjects.get(beanName);
            } else {
                bean = createBean(beanName, beanDefinition);
            }
            return bean;
        } else {
            throw new NullPointerException();
        }
    }
}
