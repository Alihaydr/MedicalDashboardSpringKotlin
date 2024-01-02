package com.libanux.medicaldashboard_backend.items.services

import com.libanux.medicaldashboard_backend.consumer.exceptions.ConsumerActionFailedException
import com.libanux.medicaldashboard_backend.consumer.models.ConsumerCount
import com.libanux.medicaldashboard_backend.global.Global
import com.libanux.medicaldashboard_backend.items.ItemActionFailedException
import com.libanux.medicaldashboard_backend.items.models.Category
import com.libanux.medicaldashboard_backend.items.models.Item
import com.libanux.medicaldashboard_backend.items.models.ItemCount
import com.libanux.medicaldashboard_backend.items.repositories.ItemDataAccessService
import com.libanux.medicaldashboard_backend.user.model.UserSignUp
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

@Service
class ItemService(private val itemDataAccessService: ItemDataAccessService) {

    fun insertItem(item: Item, userDetails: UserSignUp){
        try {
            try {
                if(itemDataAccessService.getItemByName(item.name).isNotEmpty())
                    throw ItemActionFailedException("medicin already exist.")

                itemDataAccessService.insertItem(item, userDetails)
            } catch (ex: ItemActionFailedException){
                throw ItemActionFailedException(ex.message)
            } catch (ex: ItemActionFailedException){
                throw ItemActionFailedException(ex.message)
            }

        }catch (ex: ItemActionFailedException){
            throw ItemActionFailedException(ex.localizedMessage)
        }
    }

    fun getItemsPerPage(pageNumber:Int): Pair<List<Item>, Int> {
        try {
            if(pageNumber < 0){
                throw ConsumerActionFailedException("pageNumber cannot be negative!")
            }
            return itemDataAccessService.getItemsPerPage(pageNumber, Global.pageSize)
        }catch (ex: ItemActionFailedException){
            throw ItemActionFailedException(ex.localizedMessage)
        }
    }

    fun getCategories(): List<Category> {
        try {
            return itemDataAccessService.getCategories()
        }catch (ex: ItemActionFailedException){
            throw ItemActionFailedException(ex.localizedMessage)
        }
    }

    fun getItemByCategory(categoryId:Int): Item {
        try {
            return itemDataAccessService.getItemByCategory(categoryId)
        } catch (ex: ItemActionFailedException){
            throw ItemActionFailedException(ex.localizedMessage)
        }
    }

    fun deleteItemById(itemId:Int){
        try {
            itemDataAccessService.deleteItemById(itemId)
        }catch (ex:ItemActionFailedException){
            throw ItemActionFailedException(ex.localizedMessage)
        }
    }

    fun updateItem(item:Item){
        try {
            itemDataAccessService.updateItem(item)
        }catch (ex:ItemActionFailedException){
            throw ItemActionFailedException(ex.localizedMessage)
        }
    }

    fun getItemCount(): ItemCount {
        return itemDataAccessService.getItemCount()
    }


    fun itemValidity(itemId:Int): Int {
        try {
            return itemDataAccessService.getItemValidity(itemId)
        }catch (ex:ItemActionFailedException){
            throw ItemActionFailedException(ex.message.toString())
        }
    }
}