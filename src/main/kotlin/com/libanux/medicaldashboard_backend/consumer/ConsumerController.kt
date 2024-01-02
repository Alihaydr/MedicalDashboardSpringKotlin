package com.libanux.medicaldashboard_backend.consumer

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.libanux.medicaldashboard_backend.apiresponse.ResponseHandler
import com.libanux.medicaldashboard_backend.consumer.exceptions.ConsumerActionFailedException
import com.libanux.medicaldashboard_backend.consumer.models.Consumer
import com.libanux.medicaldashboard_backend.consumer.services.ConsumerService
import com.libanux.medicaldashboard_backend.user.model.UserErrorLogin
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/consumer")
@CrossOrigin
class ConsumerController(private val service:ConsumerService) {

    @PostMapping("/new")
    fun insertConsumer(
        @RequestParam("document") document:MultipartFile,
        @RequestParam("consumer") consumerJson: String
    ): ResponseEntity<Any> {
        val objectMapper = ObjectMapper()
        val consumer:Consumer
        return try {
            consumer = objectMapper.readValue(consumerJson, Consumer::class.java)
            println(consumer)
            service.insertConsumer(consumer, document)
            ResponseHandler.generateResponse(
                "Success", HttpStatus.OK, "Successfully add new consumer.")

        } catch (e: JsonProcessingException) {
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(e.localizedMessage))
        }catch (ex:ConsumerActionFailedException) {
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.localizedMessage))
        }

    }

    @GetMapping("/all/{pageNumber}")
    fun getConsumersPerPage(@PathVariable pageNumber:Int): ResponseEntity<Any>{
        return try {
            val consumers = service.getConsumerPerPage(pageNumber)
            ResponseHandler.generateResponse(
                "Success", HttpStatus.OK, consumers)
        }catch (ex:ConsumerActionFailedException){
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.localizedMessage))
        }
    }

    @GetMapping("/all")
    fun getConsumersBySearchKey(@RequestParam searchKey:String): ResponseEntity<Any>{
        return try {
            val consumers = service.getConsumerBySearchKey(searchKey)
            ResponseHandler.generateResponse(
                "Success", HttpStatus.OK, consumers)
        }catch (ex:ConsumerActionFailedException){
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.localizedMessage))
        }
    }

    @DeleteMapping("/delete/{id}")
    fun deleteConsumerById(@PathVariable id:Int) : ResponseEntity<Any>{
        return try {
            service.deleteUserById(id)
            ResponseHandler.generateResponse(
                "Success", HttpStatus.OK, "Consumer deleted successfully")
        }catch (ex:ConsumerActionFailedException){
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.message.toString()))
        }
    }

    @GetMapping("/{id}")
    fun getConsumerById(@PathVariable id:Int): ResponseEntity<Any>{
        return try {
            val consumer = service.getConsumerById(id)
            ResponseHandler.generateResponse(
                "Success", HttpStatus.OK, consumer)
        }catch (ex:ConsumerActionFailedException){
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.localizedMessage))
        }
    }

    @GetMapping("/count")
    fun getConsumerCount(): ResponseEntity<Any>{
        return try {
            val consumer = service.getConsumerCount()
            ResponseHandler.generateResponse(
                "Success", HttpStatus.OK, consumer)
        }catch (ex:ConsumerActionFailedException){
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.localizedMessage))
        }
    }

}