package com.libanux.medicaldashboard_backend.user.model


data class UserSignUp (
    val id:Long,
    val username:String,
    val role:String,
    val phoneNumber:String,
    var password:String,
    val birthDay:String,
    val imageUrl:String
)

data class UserSignUpResponse (
    val message:String
)