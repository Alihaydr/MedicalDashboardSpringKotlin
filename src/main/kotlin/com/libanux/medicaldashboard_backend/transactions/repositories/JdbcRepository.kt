package com.libanux.medicaldashboard_backend.transactions.repositories

import com.libanux.medicaldashboard_backend.items.models.Item
import com.libanux.medicaldashboard_backend.transactions.models.*
import java.sql.Timestamp

interface JdbcRepository {

    fun insertPullTransaction(pull:TransactionPull)

    fun insertPushTransaction(pull:TransactionPush)

    fun getPushTransaction(pageNumber:Int, pageSize:Int):Pair<List<TransactionPush>, Int>

    fun getPullTransaction(pageNumber:Int, pageSize:Int): Pair<List<TransactionPullParsing>, Int>

    fun updatePullTransaction(pull:TransactionPullParsing)

    fun updatePushTransaction(pull:TransactionPush)

    fun deletePushTransaction(transactionId:Long)

    fun deletePullTransaction(transactionId:Long)
    fun getPushCount(): TransactionPushCount
    fun getPullCount(): TransactionPullCount

    fun getMonthForTransactionWithItemAndConsumerID(itemId: Long, consumerID: Long?): Timestamp?

    fun getItemValidity(itemId: Int): Int
}