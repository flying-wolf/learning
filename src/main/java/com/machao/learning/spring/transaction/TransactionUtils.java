package com.machao.learning.spring.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TransactionUtils {

    @Autowired
    private DataSourceTransactionManager transactionManager;

    private TransactionStatus transactionStatus;

    public void begin() {
        this.transactionStatus = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
        System.out.println("开启了事务！");
    }

    public void commit() {
        if (this.transactionStatus != null){
            this.transactionManager.commit(this.transactionStatus);
            System.out.println("提交了事务！");
        }
    }

    public void rollback() {
        if (this.transactionStatus != null){
            this.transactionManager.rollback(this.transactionStatus);
            System.out.println("回滚了事务！");
        }
    }

}
