package com.libanux.medicaldashboard_backend.transactions.services

import com.libanux.medicaldashboard_backend.consumer.exceptions.ConsumerActionFailedException
import com.libanux.medicaldashboard_backend.firebase.FirebaseService
import com.libanux.medicaldashboard_backend.global.Global
import com.libanux.medicaldashboard_backend.items.services.ItemService
import com.libanux.medicaldashboard_backend.transactions.models.*
import com.libanux.medicaldashboard_backend.transactions.repositories.TransactionActionFailedException
import com.libanux.medicaldashboard_backend.transactions.repositories.TransactionDataAccessService
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class TransactionService(
    private val transactionDataAccessService: TransactionDataAccessService,
    private val firebaseService: FirebaseService
) {


    fun pullTransaction(pullTransactionPull: TransactionPull, report: MultipartFile){
        try {
            // take all items and consumer id
            checkIfConsumerTakeSameItemInSameMonth(pullTransactionPull.item,
                pullTransactionPull.consumer?.id)

            // insert to firebase the report
            val reportUrl = firebaseService.upload(report,"")
            pullTransactionPull.patientReportLink = reportUrl
            transactionDataAccessService.insertPullTransaction(pullTransactionPull)
        } catch (ex:TransactionActionFailedException){
            throw  TransactionActionFailedException(ex.message.toString())
        }
    }


    private fun checkIfConsumerTakeSameItemInSameMonth(item: MutableSet<ItemTransaction>,
                                                       consumerId: Long?) {
        val currentDate = LocalDate.now()

        for (itemN in item) {
            val itemValidity = transactionDataAccessService.getItemValidity(itemN.id.toInt())
            val dateOfTransaction = transactionDataAccessService
                .getMonthForTransactionWithItemAndConsumerID(itemN.id, consumerId)

            val convertedDate = convertTimestampToDate(dateOfTransaction)
            val originalFormat = "yyyy-MM-dd HH:mm:ss"
            val extractedDate = extractDateFromDateAndTime(convertedDate, originalFormat)
            val calculatedDate = extractedDate!!.minusDays(itemValidity.toLong())

            if (calculatedDate.isAfter(currentDate)) {
                throw TransactionActionFailedException("Item ${itemN.name} already taken within validity period.")
            }
        }
    }


    fun convertTimestampToDate(timestamp: Timestamp?): String {
        val localDateTime = timestamp?.toLocalDateTime()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return localDateTime?.format(formatter) ?: "Timestamp is null or invalid"
    }

    fun extractDateFromDateAndTime(dateTimeString: String, originalFormat: String): LocalDate? {
        return try {
            val formatter = DateTimeFormatter.ofPattern(originalFormat)
            val dateTime = LocalDateTime.parse(dateTimeString, formatter)
            val extractedDate = dateTime.toLocalDate()
            extractedDate
        } catch (ex: Exception) {
            println("Error extracting date: ${ex.localizedMessage}")
            null
        }
    }


    fun pushTransaction(pushTransaction: TransactionPush){
        try {
            transactionDataAccessService.insertPushTransaction(pushTransaction)
        }catch (ex:TransactionActionFailedException){
            throw  TransactionActionFailedException(ex.message.toString())
        }
    }

    fun getTransactionsPushPerPage(pageNumber:Int): Pair<List<TransactionPush>, Int> {
        try {
            if(pageNumber < 0){
                throw TransactionActionFailedException("pageNumber cannot be negative!")
            }
            return transactionDataAccessService.getPushTransaction(pageNumber, Global.pageSize)
        }catch (ex: ConsumerActionFailedException){
            throw ConsumerActionFailedException(ex.localizedMessage)
        }
    }

    fun getTransactionsPullPerPage(pageNumber:Int): Pair<List<TransactionPullParsing>, Int> {
        try {
            if(pageNumber < 0){
                throw TransactionActionFailedException("pageNumber cannot be negative!")
            }
            return transactionDataAccessService.getPullTransaction(pageNumber, Global.pageSize)
        }catch (ex: ConsumerActionFailedException){
            throw ConsumerActionFailedException(ex.localizedMessage)
        }
    }

    fun deleteTransactionPushById(transactionId:Long) {
        try {
            return transactionDataAccessService.deletePushTransaction(transactionId)
        }catch (ex: TransactionActionFailedException){
            throw TransactionActionFailedException(ex.localizedMessage)
        }
    }

    fun deleteTransactionPullById(transactionId:Long) {
        try {
            return transactionDataAccessService.deletePullTransaction(transactionId)
        }catch (ex: TransactionActionFailedException){
            throw TransactionActionFailedException(ex.localizedMessage)
        }
    }


    fun updateTransactionPush(transactionPush: TransactionPush) {
        try {
            transactionDataAccessService.updatePushTransaction(transactionPush)
        }catch (ex: TransactionActionFailedException){
            throw TransactionActionFailedException(ex.localizedMessage)
        }
    }

    fun updateTransactionPull(transactionPullParsing: TransactionPullParsing) {
        try {
            transactionDataAccessService.updatePullTransaction(transactionPullParsing)
        }catch (ex: TransactionActionFailedException){
            throw TransactionActionFailedException(ex.localizedMessage)
        }
    }

    fun getPullCount(): TransactionPullCount {
        return transactionDataAccessService.getPullCount()
    }

    fun getPushCount(): TransactionPushCount {
        return transactionDataAccessService.getPushCount()
    }
}