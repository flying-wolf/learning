package com.machao.learning.spring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AspectAopDemo {

    // 切入点表达式
    private final static String EXECUTION_STR = "execution(* com.machao.learning.spring.aop..*Test*.*(..))";


    /**
     * 前置通知
     */
    @Before(EXECUTION_STR)
    public void before() {
        System.out.println("AOP前置通知！");
    }

    /**
     * 后置通知
     */
    @After(EXECUTION_STR)
    public void after() {
        System.out.println("AOP后置通知！");
    }

    /**
     * 运行通知
     */
    @AfterReturning(EXECUTION_STR)
    public void afterReturning() {
        System.out.println("AOP运行通知！");
    }

    /**
     * 异常通知
     */
    @AfterThrowing(EXECUTION_STR)
    public void afterThrowing() {
        System.out.println("AOP异常通知！");
    }

    /**
     * 环绕通知
     */
    @Around(EXECUTION_STR)
    public void around(ProceedingJoinPoint point) throws Throwable {
        System.out.println("AOP环绕通知，开始！");
        point.proceed();
        System.out.println("AOP环绕通知，结束！");
    }
}
