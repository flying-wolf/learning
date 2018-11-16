package com.machao.learning.spring.aop;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class AopTest {

    public void methodA(){
        System.out.println("run Method A");
    }

    public void methodB(){
        System.out.println("run Method B");
    }

    public void methodC() throws Exception {
        System.out.println("run Method C");
        throw new Exception("报错啦！");
    }

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
        AopTest aopTest = (AopTest) context.getBean("aopTest");
        aopTest.methodA();
        System.out.println();
        aopTest.methodB();
        System.out.println();
        aopTest.methodC();
    }
}
