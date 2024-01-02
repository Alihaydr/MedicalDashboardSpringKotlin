package com.libanux.medicaldashboard_backend.items.repositories

import com.libanux.medicaldashboard_backend.items.models.Category
import com.libanux.medicaldashboard_backend.items.models.Item
import com.libanux.medicaldashboard_backend.items.models.ItemCount
import com.libanux.medicaldashboard_backend.user.model.UserSignUp
import org.springframework.security.core.userdetails.UserDetails

interface JdbcRepository {

    fun insertItem(item:Item, userDetails: UserSignUp)

    fun getItemsPerPage(pageNumber: Int, pageSize: Int): Pair<List<Item>, Int>

    fun getItemById(itemId:Int):Item

    fun updateItem(item:Item)

    fun deleteItemById(itemId:Int)

    fun getCategories():List<Category>

    fun getItemByCategory(categoryId:Int):Item

    fun getItemByName(name:String): List<Int>
    fun getItemCount(): ItemCount
    fun getItemValidity(itemId: Int): Int?
}