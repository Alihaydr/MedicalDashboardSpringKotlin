package com.libanux.medicaldashboard_backend.transactions

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.libanux.medicaldashboard_backend.apiresponse.ResponseHandler
import com.libanux.medicaldashboard_backend.consumer.exceptions.ConsumerActionFailedException
import com.libanux.medicaldashboard_backend.transactions.models.TransactionPull
import com.libanux.medicaldashboard_backend.transactions.models.TransactionPullParsing
import com.libanux.medicaldashboard_backend.transactions.models.TransactionPush
import com.libanux.medicaldashboard_backend.transactions.repositories.TransactionActionFailedException
import com.libanux.medicaldashboard_backend.transactions.services.TransactionService
import com.libanux.medicaldashboard_backend.user.model.UserErrorLogin
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/transactions")
@CrossOrigin
class TransactionController(private val service:TransactionService) {

    @PostMapping("/pull/new")
    fun insertConsumer(
        @RequestParam("report") report: MultipartFile,
        @RequestParam("transaction") transactionJson: String
    ): ResponseEntity<Any> {
        val objectMapper = ObjectMapper()
        val transaction: TransactionPull
        return try {
            transaction = objectMapper.readValue(transactionJson, TransactionPull::class.java)
            println(transaction)
            service.pullTransaction(transaction,report)
            ResponseHandler.generateResponse(
                "Success", HttpStatus.OK, "Successfully pulling the transaction.")
        } catch (e: JsonProcessingException) {
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(e.message.toString())
            )
        }catch (ex: TransactionActionFailedException) {
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.message.toString())
            )
        }

    }


    @GetMapping("/push/all/{pageNumber}")
    fun getpushPerPage(@PathVariable pageNumber:Int): ResponseEntity<Any>{
        return try {
            val transactionsPush = service.getTransactionsPushPerPage(pageNumber)
            ResponseHandler.generateResponse(
                "Success", HttpStatus.OK, transactionsPush)
        }catch (ex:TransactionActionFailedException){
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.localizedMessage))
        }
    }


    @GetMapping("/pull/all/{pageNumber}")
    fun getpullPerPage(@PathVariable pageNumber:Int): ResponseEntity<Any>{
        return try {
            val transactionsPull = service.getTransactionsPullPerPage(pageNumber)
            ResponseHandler.generateResponse(
                "Success", HttpStatus.OK, transactionsPull)
        }catch (ex:TransactionActionFailedException){
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.localizedMessage))
        }
    }

    @DeleteMapping("/push/{id}")
    fun deleteTransactionPushById(@PathVariable id:Long): ResponseEntity<Any>{
        return try {
            service.deleteTransactionPushById(id)
            ResponseHandler.generateResponse(
                "Success", HttpStatus.OK, "Transaction deleted successfully.")
        }catch (ex:TransactionActionFailedException){
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.localizedMessage))
        }
    }

    @DeleteMapping("/pull/{id}")
    fun deleteTransactionPullById(@PathVariable id:Long): ResponseEntity<Any>{
        return try {
            service.deleteTransactionPullById(id)
            ResponseHandler.generateResponse(
                "Success", HttpStatus.OK, "Transaction deleted successfully.")
        }catch (ex:TransactionActionFailedException){
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.localizedMessage))
        }
    }

    @PutMapping("/update/push")
    fun updateTransactionPush(@RequestBody transactionPush: TransactionPush): ResponseEntity<Any>{
        return try {
            service.updateTransactionPush(transactionPush)
            ResponseHandler.generateResponse(
                "Success", HttpStatus.OK, "Transaction updated successfully.")
        }catch (ex:TransactionActionFailedException){
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.localizedMessage))
        }
    }

    @PutMapping("update/pull")
    fun updateTransactionPull(@RequestBody transactionPullParsing: TransactionPullParsing): ResponseEntity<Any>{
        return try {
            service.updateTransactionPull(transactionPullParsing)
            ResponseHandler.generateResponse(
                "Success", HttpStatus.OK, "Transaction updated successfully.")
        }catch (ex:TransactionActionFailedException){
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.localizedMessage))
        }
    }

    @GetMapping("/pull/count")
    fun getPullCount(): ResponseEntity<Any>{
        return try {
            val pull = service.getPullCount()
            ResponseHandler.generateResponse(
                "Success", HttpStatus.OK, pull)
        }catch (ex:ConsumerActionFailedException){
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.localizedMessage))
        }
    }

    @GetMapping("/push/count")
    fun getPushCount(): ResponseEntity<Any>{
        return try {
            val push = service.getPushCount()
            ResponseHandler.generateResponse(
                "Success", HttpStatus.OK, push)
        }catch (ex:ConsumerActionFailedException){
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.localizedMessage))
        }
    }

}