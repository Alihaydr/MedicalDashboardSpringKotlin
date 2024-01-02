package com.libanux.medicaldashboard_backend.user.repositories

import com.libanux.medicaldashboard_backend.user.model.UserLogin
import com.libanux.medicaldashboard_backend.user.model.UserSignUp
import java.util.Optional

interface JdbcRepository  {

    fun signUpUser(user:UserSignUp):Int

    fun signInUser(username: String): UserSignUp?

    fun getUserByPhoneNumberAndUsername(user:UserSignUp): List<Int>

    fun findByUsername(username:String):Optional<UserSignUp>

}