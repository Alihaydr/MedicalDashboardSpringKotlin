package com.libanux.medicaldashboard_backend.transactions.models

data class TransactionPull(
    val id:Long = 0,
    val item:MutableSet<ItemTransaction> = mutableSetOf(),
    val consumer:ConsumerTransaction? = null,
    val transactionType:String = "pull",
    val transactionDate:String? = "",
    var patientReportLink:String = ""
)

data class TransactionPullParsing(
    val id:Long = 0,
    val item:ItemTransaction? = null,
    val consumer:ConsumerTransaction? = null,
    val transactionType:String = "pull",
    val transactionDate:String? = "",
    var patientReportLink:String? = ""
)


data class TransactionPush(
    val id:Long = 0,
    val item:ItemTransaction? = null,
    val user:UserTransaction? = null,
    val transactionType:String = "push",
    val transactionDate:String? = "",
    val location:String? = ""
)

data class UserTransaction(val id: Long, val adminName:String?)
data class ItemTransaction(val id: Long=0, val name:String? = "", val pullQuantity: Int? =0)
data class ConsumerTransaction(val id: Long=0, val name:String? = "")


data class TransactionPullCount(val count:Int)
data class TransactionPushCount(val count:Int)