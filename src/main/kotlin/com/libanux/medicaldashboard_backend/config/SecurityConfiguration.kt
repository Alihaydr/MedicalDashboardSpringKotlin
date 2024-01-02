package com.libanux.medicaldashboard_backend.config

import com.libanux.medicaldashboard_backend.security.jwtfilterchain.JwtRequestFilter
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.access.intercept.AuthorizationFilter

@Configuration
@EnableWebSecurity
class SecurityConfiguration(
    private val jwtRequestFilter: JwtRequestFilter
) {

    private val logger = LoggerFactory.getLogger(SecurityConfiguration::class.java)

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
    ): DefaultSecurityFilterChain {
        http
            .csrf { it.disable() }
            .addFilterBefore(jwtRequestFilter, AuthorizationFilter::class.java)
            .authorizeHttpRequests {
                it
                    .requestMatchers("/","/api/auth", "api/auth/refresh", "/error")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/user/signin")
                    .permitAll()
                    .requestMatchers("/api/user**","/api/v1/consumer**", "/api/v1/item**","/api/v1/transactions**")
                    .hasAuthority("ROLE_superadmin")
                    .anyRequest()
                    .fullyAuthenticated()
            }

        return http.build()
    }

}