package com.libanux.medicaldashboard_backend.user.repositories

import com.libanux.medicaldashboard_backend.user.exceptions.LoginFailedException
import com.libanux.medicaldashboard_backend.user.exceptions.UserSingUpFailedException
import com.libanux.medicaldashboard_backend.user.model.UserSignUp
import org.springframework.dao.DataAccessException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.Date
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import javax.annotation.Nullable


@Repository
class UserDataAccessService(private val jdbcTemplate: JdbcTemplate) : JdbcRepository {

    override fun signUpUser(user: UserSignUp): Int {
        try {
            val sql = """
                CALL InsertUser(?,?,?,?,?,?);
             """.trimIndent()

            return jdbcTemplate.update(sql, user.username,user.password, user.role,
                user.phoneNumber,
//                "yyyy-[m]m-[d]d".
                Date.valueOf(user.birthDay), user.imageUrl)
        }catch (ex:DataAccessException){
            throw UserSingUpFailedException(ex.message)
        }

    }

    override fun signInUser(username: String): UserSignUp? {
        val sql = """
                CALL GetUserByUsername(?);
        """.trimIndent()

        return try {
            return jdbcTemplate.queryForObject(sql, UsernamePassworddMapper(),username)
        } catch (ex: EmptyResultDataAccessException) {
            throw LoginFailedException(ex.localizedMessage)
        }
    }

    override fun getUserByPhoneNumberAndUsername(user: UserSignUp): List<Int> {
        val sql = "CALL GetUserById(?, ?);"

        return try {
            return jdbcTemplate.query(sql, IdMapper(), user.username, user.phoneNumber)
        } catch (ex: EmptyResultDataAccessException) {
            return listOf()
        }
    }

    override fun findByUsername(username: String): Optional<UserSignUp> {
        val sql = """
                CALL GetUserByUsername(?);
        """.trimIndent()

        return try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, UsernamePassworddMapper(),username))
        } catch (ex: EmptyResultDataAccessException) {
            throw LoginFailedException(ex.localizedMessage)
        }
    }

    private class IdMapper : RowMapper<Int> {
        @Nullable
        @Throws(SQLException::class)
        override fun mapRow(resultSet: ResultSet, i: Int): Int {
            return  resultSet.getInt("id")
        }
    }

    private class UsernamePassworddMapper : RowMapper<UserSignUp> {
        @Nullable
        @Throws(SQLException::class)
        override fun mapRow(resultSet: ResultSet, i: Int): UserSignUp {
            return  UserSignUp(
                resultSet.getLong("id"),
                resultSet.getString("username"),
                resultSet.getString("role"),
                resultSet.getString("phone_number"),
                resultSet.getString("password"),
                resultSet.getDate("birthday").toString(),
                resultSet.getString("image_url"),
            )
        }
    }
}