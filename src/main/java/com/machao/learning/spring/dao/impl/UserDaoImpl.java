package com.machao.learning.spring.dao.impl;

import com.machao.learning.spring.dao.IUserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserDaoImpl implements IUserDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void addUser(String userName, int age) {
        String sqlStr = "INSERT INTO t_users(name, age) VALUES(?,?);";
        int updateResult = jdbcTemplate.update(sqlStr, userName, age);
        System.out.println("insert user updateResult: " + updateResult);
    }
}
