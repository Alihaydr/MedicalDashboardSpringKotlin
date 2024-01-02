package com.libanux.medicaldashboard_backend.apiresponse

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

object ResponseHandler {
    fun generateResponse(message: String, status: HttpStatus, responseObj: Any): ResponseEntity<Any> {
        val map: MutableMap<String, Any> = HashMap()
        map["message"] = message
        map["status"] = status.value()
        map["data"] = responseObj
        return ResponseEntity(map, status)
    }
}