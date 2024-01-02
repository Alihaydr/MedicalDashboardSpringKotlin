package com.libanux.medicaldashboard_backend.consumer.models

import com.libanux.medicaldashboard_backend.items.models.Item

data class Consumer(
    val id: Long? = 0,
    val name: String? = "",
    val phoneNumber: String? = "",
    val birthdate: String? = "",
    var documents: String? = "",
    var location:String? = "",
    val items: MutableList<Item> = mutableListOf()
)


data class ConsumerCount(val count: Int)