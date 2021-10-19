package com.spring;

import com.app.AppConfig;
import com.app.service.UserService;
import org.junit.Test;

public class SpringTest {

    @Test
    public void ApplicationContextTest() {
        // 获取配置类
        MiniApplicationContext ac = new MiniApplicationContext(AppConfig.class);
        // 获取bean对象
        ((UserService) ac.getBean("userService")).test();
        // assert ac.getBean("userService")!=ac.getBean("userService");
    }

}
