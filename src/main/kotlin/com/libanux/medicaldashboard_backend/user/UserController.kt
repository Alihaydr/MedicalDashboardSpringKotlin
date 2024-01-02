package com.libanux.medicaldashboard_backend.user

import com.libanux.medicaldashboard_backend.apiresponse.ResponseHandler
import com.libanux.medicaldashboard_backend.user.exceptions.LoginFailedException
import com.libanux.medicaldashboard_backend.user.exceptions.UserSingUpFailedException
import com.libanux.medicaldashboard_backend.user.model.*
import com.libanux.medicaldashboard_backend.user.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
@CrossOrigin
class UserController(val userService:UserService) {


    @PostMapping("/signin")
    fun loginUser(@RequestBody userCredentials:UserLogin): ResponseEntity<Any> {
        try{
            println(userCredentials)
            val response:UserLoginResponse = userService.loginUser(userCredentials)
            return ResponseHandler.generateResponse(
                "Successfully login!", HttpStatus.OK, response)
        }catch (ex: LoginFailedException){
            return ResponseHandler.generateResponse(
                "Error login!", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.message.toString()))
        }
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @PostMapping("/signup")
    fun loginUser(@RequestBody userSignUp:UserSignUp): ResponseEntity<Any> {
        return try{
            val userSignUpRes = userService.signUpUser(userSignUp)
            ResponseHandler.generateResponse(
                "Successfully add new admin!", HttpStatus.OK, userSignUpRes)
        }catch (ex: UserSingUpFailedException){
            ResponseHandler.generateResponse(
                "Error", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.localizedMessage))
        }
    }

    @GetMapping("/{id}")
    fun getUserInformation(@PathVariable("id") id:Int): ResponseEntity<Any> {
        try{
            val response:UserSignUpResponse = userService.getUserById(id)
            return ResponseHandler.generateResponse(
                "Successfully login!", HttpStatus.OK, response)
        }catch (ex: LoginFailedException){
            return ResponseHandler.generateResponse(
                "Error login!", HttpStatus.BAD_REQUEST, UserErrorLogin(ex.message.toString()))
        }
    }
}