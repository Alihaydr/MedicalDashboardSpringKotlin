package com.libanux.medicaldashboard_backend.security.jwtfilterchain

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.libanux.medicaldashboard_backend.user.model.UserLogin
import com.libanux.medicaldashboard_backend.user.model.UserSignUp
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*


@Service
class JWTService {

    @Value("\${jwt.algorithm.key}")
    private val algorithmKey: String? = null

    @Value("\${jwt.issuer}")
    private val issuer: String? = null

    @Value("\${jwt.expiredInSeconds}")
    private val expiredInSeconds = 0
    private var algorithm: Algorithm? = null
    @PostConstruct
    fun postConstruct() {
        algorithm = Algorithm.HMAC256(algorithmKey)
    }

    fun generateJWT(user: UserSignUp): String {
        return JWT.create()
            .withClaim(ID_KEY, user.id)
            .withClaim(USERNAME_KEY, user.username)
            .withClaim(ROLE_KEY, user.role)
            .withExpiresAt(Date(System.currentTimeMillis() + 1000L * expiredInSeconds))
            .withIssuer(issuer)
            .sign(algorithm)
    }

    fun generateVerificationJWT(user: UserLogin): String {
        return JWT.create()
            .withClaim(USERNAME_KEY, user.username)
            .withExpiresAt(Date(System.currentTimeMillis() + 1000L * expiredInSeconds))
            .withIssuer(issuer)
            .sign(algorithm)
    }

    fun getUsername(token: String?): String {
        return JWT.decode(token)
            .getClaim(USERNAME_KEY).asString()
    }

    fun extractExpirationTime(token: String?): Long {
        return try {
            val decodedJWT = JWT.decode(token)
            decodedJWT.expiresAt.time // Returns expiration time in milliseconds
        } catch (e: Exception) {
            throw RuntimeException("expired token or invalid signature")
        }
    }

    companion object {
        private const val USERNAME_KEY = "username"
        private const val ID_KEY = "id"
        private const val ROLE_KEY = "role"
    }
}