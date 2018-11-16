package com.machao.learning.spring.aop;

import com.machao.learning.spring.annotation.LearnTransaction;
import com.machao.learning.spring.transaction.TransactionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class TransactionAop {
    private final static String EXCUTION_STR = "execution(* com.machao.learning.spring.service.*.*.*(..))";

    @Autowired
    private TransactionUtils transactionUtils;


    @Around(EXCUTION_STR)
    private void around(ProceedingJoinPoint point) throws Throwable {
        // 1. 开启事务
        if (isTransaction(point))
            transactionUtils.commit();
        // 2. 执行目标方法
        point.proceed();
        // 3. 提交事务
        transactionUtils.commit();
    }

    @AfterThrowing(EXCUTION_STR)
    private void afterThrowing() {
        transactionUtils.rollback();
    }


    private boolean isTransaction(ProceedingJoinPoint point) throws NoSuchMethodException {
        // 1. 获取目标方法名
        String targetMethodName = point.getSignature().getName();
        // 2. 获取目标方法参数类型
        Class<?>[] targetParamTypes = ((MethodSignature) point.getSignature()).getParameterTypes();
        // 3. 获取目标类
        Class<?> targetClass = point.getTarget().getClass();
        // 4. 获取目标方法
        Method targetMethod = targetClass.getDeclaredMethod(targetMethodName, targetParamTypes);
        // 5. 获取目标方法的事务Annotation
        LearnTransaction annotation = targetMethod.getDeclaredAnnotation(LearnTransaction.class);
        // 6. 判断目标方法是否加了事务注解
        return (annotation != null);
    }
}
