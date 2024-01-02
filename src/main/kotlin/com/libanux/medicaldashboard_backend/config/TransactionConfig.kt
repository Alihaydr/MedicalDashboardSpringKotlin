package com.libanux.medicaldashboard_backend.config

import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
class TransactionConfig {

//    @Bean
//    fun transactionManager(dataSource: DataSource): PlatformTransactionManager {
//        val transactionManager = DataSourceTransactionManager(dataSource)
//        transactionManager.defaultTimeout = 5 // Set the default timeout if needed
//        transactionManager.isolationLevel = java.sql.Connection.TRANSACTION_READ_COMMITTED // Set your desired isolation level here
//        return transactionManager
//    }
}