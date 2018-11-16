package com.machao.learning.spring.dao.impl;

import com.machao.learning.spring.dao.ILogDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class LogDaoImpl implements ILogDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void addLog(String name) {
        String sqlStr = "INSERT INTO t_logs(NAME) VALUES(?);";
        int updateResult = jdbcTemplate.update(sqlStr, name);
        System.out.println("t_logs updateResult: " + updateResult);
    }
}
