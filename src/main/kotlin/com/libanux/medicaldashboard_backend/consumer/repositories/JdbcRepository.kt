package com.libanux.medicaldashboard_backend.consumer.repositories

import com.libanux.medicaldashboard_backend.consumer.models.Consumer
import com.libanux.medicaldashboard_backend.consumer.models.ConsumerCount
import java.sql.Timestamp

interface JdbcRepository {

    fun insertConsumer(consumer: Consumer)
    fun getConsumerById(consumerId:Int) : Consumer
    fun getConsumerByPhoneNumber(phoneNumber:String?) : List<Int>
    fun updateConsumer(consumer: Consumer)
    fun isConsumerValid(consumerId: Int) : Timestamp
    fun getAllConsumerPerPage(pageNumber:Int, pageSize:Int): Pair<List<Consumer>, Int>
    fun deleteConsumerById(consumerId:Int)

    fun getAllConsumerBySearchKey(searchKey:String,pageSize:Int): Pair<List<Consumer>, Int>

    fun getConsumerCount(): ConsumerCount?
}