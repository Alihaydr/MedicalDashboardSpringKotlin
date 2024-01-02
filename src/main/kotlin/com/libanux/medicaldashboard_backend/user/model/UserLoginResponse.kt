package com.libanux.medicaldashboard_backend.user.model

data class UserLoginResponse(val expireAt:Long, val token:String)
data class UserErrorLogin(val error:String)