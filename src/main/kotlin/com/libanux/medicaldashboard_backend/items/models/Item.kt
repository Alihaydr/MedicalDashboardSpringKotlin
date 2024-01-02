package com.libanux.medicaldashboard_backend.items.models

data class Item(
    val id: Long = 0,
    val name: String = "",
    val notes: String? = null,
    val variants: MutableList<String>? = mutableListOf(),
    val quantity: Int? = 0,
    val expiryDate: String? = "",
    val location: String? = "",
    val categories: MutableSet<Category> = mutableSetOf()
)

data class ItemCount(val itemCount: Int)