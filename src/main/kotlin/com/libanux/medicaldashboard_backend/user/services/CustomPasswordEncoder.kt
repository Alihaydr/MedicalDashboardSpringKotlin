package com.libanux.medicaldashboard_backend.user.services

import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class CustomPasswordEncoder: PasswordEncoder {
    override fun encode(rawPassword: CharSequence?): String {
        return BCrypt.hashpw(rawPassword.toString(), BCrypt.gensalt(10))

    }

    override fun matches(rawPassword: CharSequence?, encodedPassword: String?): Boolean {
        return BCrypt.checkpw(rawPassword.toString(), encodedPassword)
    }
}