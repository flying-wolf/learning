package com.machao.learning.spring.test;

import com.machao.learning.spring.service.IUserService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TransactionTest {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        IUserService userService = (IUserService) context.getBean("userServiceImpl");
//        userService.addUser();
//        userService.saveUser();
//        userService.createUser();
        userService.insertUser();
    }
}
