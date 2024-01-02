package com.libanux.medicaldashboard_backend.security.jwtfilterchain

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.libanux.medicaldashboard_backend.user.model.UserSignUp
import com.libanux.medicaldashboard_backend.user.repositories.UserDataAccessService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.Date
import java.util.Optional

@Component
class JwtRequestFilter(

    private val repository: UserDataAccessService,
    private val service:JWTService

) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val tokenHeader = request.getHeader("Authorization")
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            try {
                val token = tokenHeader.substring(7)
                val decodedToken = JWT.decode(token)
                if (decodedToken.expiresAt.before(Date())) {
                    response.status = HttpServletResponse.SC_FORBIDDEN
                    response.writer.write("Token has expired")
                    return
                } else {
                    val username: String = service.getUsername(token)
                    val userExist: Optional<UserSignUp> = repository.findByUsername(username)
                    if (userExist.isPresent()) {
                        val roleClaim = decodedToken.getClaim("role").asString()
                        val grantedAuthority = SimpleGrantedAuthority("ROLE_$roleClaim")

                        val user: UserSignUp = userExist.get()
                        val authenticationToken = UsernamePasswordAuthenticationToken(user, null, listOf(grantedAuthority))
                        authenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)
//                        val authentication = authenticationManager.authenticate(authenticationToken)
                        SecurityContextHolder.getContext().authentication = authenticationToken
                    } else {
                        println("Not found!!!!!!!!!!!")
                    }
                }
            } catch (ignored: JWTDecodeException) {
                response.status = HttpServletResponse.SC_FORBIDDEN
                response.writer.write("Token Error")
            }
        }
        filterChain.doFilter(request, response)
    }
}