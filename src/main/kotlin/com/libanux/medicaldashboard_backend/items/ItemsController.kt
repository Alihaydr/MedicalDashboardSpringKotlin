package com.libanux.medicaldashboard_backend.items

import com.libanux.medicaldashboard_backend.apiresponse.ResponseHandler
import com.libanux.medicaldashboard_backend.consumer.exceptions.ConsumerActionFailedException
import com.libanux.medicaldashboard_backend.items.models.Item
import com.libanux.medicaldashboard_backend.items.services.ItemService
import com.libanux.medicaldashboard_backend.user.model.UserErrorLogin
import com.libanux.medicaldashboard_backend.user.model.UserSignUp
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.security.Provider.Service

@RestController
@RequestMapping("/api/v1/items")
@CrossOrigin
class ItemsController(private val service:ItemService) {

    @PostMapping("/new")
    fun insertItem(
        @RequestBody item:Item
    ): ResponseEntity<Any> {
            val authentication = SecurityContextHolder.getContext().authentication

            if (authentication != null && authentication.isAuthenticated) {
            val userDetails = authentication.principal as UserSignUp

                return try {
                    service.insertItem(item, userDetails)
                    ResponseHandler.generateResponse(
                        "Success", HttpStatus.OK, "Successfully add new item."
                    )
                } catch (ex: ItemActionFailedException) {
                    ResponseHandler.generateResponse(
                        "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.localizedMessage)
                    )
                }

            }

        return ResponseHandler.generateResponse(
            "Error", HttpStatus.UNAUTHORIZED, "User not authenticated"
        )
    }

    @GetMapping("/all/{pageNumber}")
    fun getItemsPerPage(@PathVariable pageNumber:Int): ResponseEntity<Any> {
        return try {
            val items = service.getItemsPerPage(pageNumber)
            ResponseHandler.generateResponse(
                "Success", HttpStatus.OK, items)
        }catch (ex: ItemActionFailedException){
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.localizedMessage)
            )
        }
    }

    @DeleteMapping("/delete/{id}")
    fun deleteItemById(@PathVariable id:Int) : ResponseEntity<Any> {
        return try {
            service.deleteItemById(id)
            ResponseHandler.generateResponse(
                "Success", HttpStatus.OK, "Item deleted successfully")
        }catch (ex: ItemActionFailedException){
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.message.toString())
            )
        }
    }

    @PutMapping("/udpate")
    fun updateItem(@RequestBody item: Item): ResponseEntity<Any> {
        return try {
            service.updateItem(item)
            ResponseHandler.generateResponse(
                "Success", HttpStatus.OK, "Item updated successfully")
        }catch (ex: ItemActionFailedException){
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.localizedMessage)
            )
        }
    }

    @GetMapping("/count")
    fun getItemCount(): ResponseEntity<Any>{
        return try {
            val item = service.getItemCount()
            ResponseHandler.generateResponse(
                "Success", HttpStatus.OK, item)
        }catch (ex:ConsumerActionFailedException){
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.localizedMessage))
        }
    }


    @GetMapping("/categories/all")
    fun getCategories(): ResponseEntity<Any>{
        return try {
            val categories = service.getCategories()
            ResponseHandler.generateResponse(
                "Success", HttpStatus.OK, categories)
        }catch (ex:ConsumerActionFailedException){
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.localizedMessage))
        }
    }
}