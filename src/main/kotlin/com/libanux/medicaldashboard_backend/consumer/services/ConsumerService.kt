package com.libanux.medicaldashboard_backend.consumer.services

import com.libanux.medicaldashboard_backend.consumer.exceptions.ConsumerActionFailedException
import com.libanux.medicaldashboard_backend.consumer.models.Consumer
import com.libanux.medicaldashboard_backend.consumer.models.ConsumerCount
import com.libanux.medicaldashboard_backend.consumer.repositories.ConsumerDataAccessService
import com.libanux.medicaldashboard_backend.firebase.FirebaseService
import com.libanux.medicaldashboard_backend.global.Global
import com.libanux.medicaldashboard_backend.user.exceptions.UserPNAndUnExistException
import com.libanux.medicaldashboard_backend.user.exceptions.UserSingUpFailedException
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ConsumerService(private val consumerDataAccessService: ConsumerDataAccessService, private val firebaseService:FirebaseService) {

    fun insertConsumer(consumer: Consumer, document:MultipartFile){
        try {

            try {
                if(consumerDataAccessService.getConsumerByPhoneNumber(consumer.phoneNumber).isNotEmpty())
                    throw ConsumerActionFailedException("phone number already exist.")

                val documentUrl = firebaseService.upload(document,"")
                consumer.documents = documentUrl
                consumerDataAccessService.insertConsumer(consumer)
            }catch (ex: UserSingUpFailedException){
                throw UserSingUpFailedException(ex.message)
            } catch (ex: UserPNAndUnExistException){
                throw UserSingUpFailedException(ex.message)
            }

        }catch (ex: ConsumerActionFailedException){
            throw ConsumerActionFailedException(ex.localizedMessage)
        }
    }

    fun getConsumerPerPage(pageNumber:Int): Pair<List<Consumer>, Int> {
        try {
            if(pageNumber < 0){
                throw ConsumerActionFailedException("pageNumber cannot be negative!")
            }
            return consumerDataAccessService.getAllConsumerPerPage(pageNumber, Global.pageSize)
        }catch (ex:ConsumerActionFailedException){
            throw ConsumerActionFailedException(ex.localizedMessage)
        }
    }

    fun deleteUserById(consumerId:Int){
        try {
            consumerDataAccessService.deleteConsumerById(consumerId)
        }catch (ex:ConsumerActionFailedException){
            throw ConsumerActionFailedException(ex.localizedMessage)
        }
    }

    fun getConsumerById(consumerId: Int): Consumer {
        try {
            return consumerDataAccessService.getConsumerById(consumerId)
        }catch (ex:ConsumerActionFailedException){
            throw ConsumerActionFailedException(ex.message.toString())
        }
    }

    fun getConsumerBySearchKey(searchKey:String): Pair<List<Consumer>, Int> {
        try {

            return consumerDataAccessService.getAllConsumerBySearchKey(searchKey, Global.pageSize)
        }catch (ex:ConsumerActionFailedException){
            throw ConsumerActionFailedException(ex.localizedMessage)
        }
    }

     fun getConsumerCount(): ConsumerCount {
        return consumerDataAccessService.getConsumerCount()
    }
}