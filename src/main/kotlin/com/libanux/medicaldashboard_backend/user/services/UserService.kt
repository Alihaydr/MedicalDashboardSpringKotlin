package com.libanux.medicaldashboard_backend.user.services

import com.libanux.medicaldashboard_backend.security.jwtfilterchain.JWTService
import com.libanux.medicaldashboard_backend.user.exceptions.LoginFailedException
import com.libanux.medicaldashboard_backend.user.exceptions.UserPNAndUnExistException
import com.libanux.medicaldashboard_backend.user.exceptions.UserSingUpFailedException
import com.libanux.medicaldashboard_backend.user.model.UserLogin
import com.libanux.medicaldashboard_backend.user.model.UserLoginResponse
import com.libanux.medicaldashboard_backend.user.model.UserSignUp
import com.libanux.medicaldashboard_backend.user.model.UserSignUpResponse
import com.libanux.medicaldashboard_backend.user.repositories.UserDataAccessService
import org.springframework.stereotype.Service

@Service
class UserService(private val userJdbc:UserDataAccessService,
                  private val passwordEncoder: CustomPasswordEncoder,
                  private val jwtService: JWTService
) {

    fun loginUser(userLogin: UserLogin): UserLoginResponse{
        val user:UserSignUp? = userJdbc.signInUser(userLogin.username)
        if(user?.username != null){
            if (passwordEncoder.matches(userLogin.password, user.password)){
                val token:String = jwtService.generateJWT(user)

                return UserLoginResponse(getExpirationTime(token),token)
            }else{
                throw LoginFailedException("Wrong password!")
            }
        } else{
            throw LoginFailedException("Wrong username!")
        }
    }

    fun signUpUser(user: UserSignUp) {
        try {
            if(userJdbc.getUserByPhoneNumberAndUsername(user).isNotEmpty())
                throw UserPNAndUnExistException("Username or phone number already exist.")

            //TODO password encoding
            val encodedPassword = passwordEncoder.encode(user.password)
            user.password = encodedPassword
            userJdbc.signUpUser(user)
        }catch (ex:UserSingUpFailedException){
            throw UserSingUpFailedException(ex.message)
        } catch (ex:UserPNAndUnExistException){
            throw UserSingUpFailedException(ex.message)
        }
    }

    fun getUserById(id:Int): UserSignUpResponse {
        // perform insert to database
        return UserSignUpResponse("Successfully Added")
    }


    fun getExpirationTime(token: String?): Long {
        return try {
            jwtService.extractExpirationTime(token)
        } catch (e: RuntimeException) {
            throw RuntimeException(e.message)
        }
    }
}