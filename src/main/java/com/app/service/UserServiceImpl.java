package com.app.service;

import com.spring.*;

@Component("userService")
@Scope("prototype")
public class UserServiceImpl implements BeanNameAware, InitializingBean, UserService {

    @Autowired
    private OrderService orderService;

    private String beanName;
    private String name;

    @Override
    public void test() {
        System.out.println(orderService);
        System.out.println(beanName);
        System.out.println(name);
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("beanName="+beanName);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
