package com.machao.learning.spring.service.impl;

import com.machao.learning.spring.dao.ILogDao;
import com.machao.learning.spring.service.ILogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LogServiceImpl implements ILogService {

    @Autowired
    private ILogDao logDaoImpl;

    @Transactional(propagation = Propagation.NESTED)
    @Override
    public void addLog() {
        String logStr = "insert log " + System.currentTimeMillis();
        logDaoImpl.addLog(logStr);
    }
}
