package com.machao.learning.spring.service.impl;

import com.machao.learning.spring.annotation.LearnTransaction;
import com.machao.learning.spring.dao.IUserDao;
import com.machao.learning.spring.service.ILogService;
import com.machao.learning.spring.service.IUserService;
import com.machao.learning.spring.transaction.TransactionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserServiceImpl implements IUserService {

    @Autowired
    private IUserDao userDaoImpl;

    @Autowired
    private ILogService logServiceImpl;

    @Autowired
    private TransactionUtils transactionUtils;

    @Override
    public void addUser() {
        try {
            // 1.开启事务
            transactionUtils.begin();
            userDaoImpl.addUser("addUser", 23);
            //int i = 1/0;
            userDaoImpl.addUser("addUsers", 22);
            // 2.提交事务
            transactionUtils.commit();
        } catch (Exception e) {
            e.printStackTrace();
            // 3.回滚事务
            transactionUtils.rollback();
        }
    }

    @Transactional
    @Override
    public void saveUser() {
        userDaoImpl.addUser("saveUser", 23);
        int i = 1/0;
        userDaoImpl.addUser("saveUsers", 22);
    }

    @LearnTransaction
    @Override
    public void createUser() {
        userDaoImpl.addUser("createUser", 23);
        int i = 1/0;
        userDaoImpl.addUser("createUsers", 22);
    }

    @Transactional
    @Override
    public void insertUser() {
        // 添加日志
        logServiceImpl.addLog();
        userDaoImpl.addUser("insertUser", 23);
        int i = 1/0;
        userDaoImpl.addUser("insertUser", 22);
    }
}
