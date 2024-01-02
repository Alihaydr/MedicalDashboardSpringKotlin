package com.libanux.medicaldashboard_backend.consumer.repositories

import com.libanux.medicaldashboard_backend.consumer.exceptions.ConsumerActionFailedException
import com.libanux.medicaldashboard_backend.consumer.models.Consumer
import com.libanux.medicaldashboard_backend.consumer.models.ConsumerCount
import com.libanux.medicaldashboard_backend.items.models.Category
import com.libanux.medicaldashboard_backend.items.models.Item
import org.springframework.dao.DataAccessException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.Date
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import javax.annotation.Nullable

@Repository
class ConsumerDataAccessService(
    private val jdbc:JdbcTemplate
)
    : JdbcRepository {

    override fun insertConsumer(consumer: Consumer) {
        val sql ="""
            CALL InsertConsumer(?, ?, ?, ?, ?);
        """.trimIndent()

        try {
            jdbc.update(sql, consumer.name,
                consumer.phoneNumber,
                Date.valueOf(consumer.birthdate),
                consumer.documents,
                consumer.location
            )
        } catch (ex:DataAccessException){
            throw ConsumerActionFailedException(ex.localizedMessage)
        }

    }

    override fun getConsumerById(consumerId: Int): Consumer {
        val sql = "CALL GetConsumerItemsById(?)"

        val consumers = mutableMapOf<Long, Consumer>()

        jdbc.query(sql, { rs, _ ->
            val currentConsumerId = rs.getLong("consumer_id")
            val currentItemId = rs.getLong("item_id")
            val currentCategoryId = rs.getLong("category_id")


            val variantsString = rs.getString("variants")
            val variantsArray = variantsString.split(",")

            val birthdate = rs.getDate("birthdate")?.toString() ?: "N/A"

            val consumer = consumers.getOrPut(currentConsumerId) {
                Consumer(
                    currentConsumerId,
                    rs.getString("consumer_name"),
                    rs.getString("phone_number"),
                    birthdate,
                    rs.getString("documents"),
                    rs.getString("location"),
                    mutableListOf()
                )
            }

            var item = consumer.items.find { it.id == currentItemId }
            if (item == null) {
                item = Item(
                    currentItemId,
                    rs.getString("item_name"),
                    rs.getString("notes"),
                    variantsArray.toMutableList(),
                    rs.getInt("quantity"),
                    rs.getString("expiry_date"),
                    rs.getString("location"),
                    mutableSetOf()
                )
                consumer.items.add(item)
            }

            val category = Category(
                currentCategoryId,
                rs.getString("category_name")
            )
            if (!item.categories.contains(category)) {
                item.categories.add(category)
            }

        }, consumerId)

        return consumers.values.firstOrNull() ?: Consumer()
    }




    override fun getConsumerByPhoneNumber(phoneNumber: String?): List<Int> {
        val sql = "CALL GetConsumerByPhoneNumber(?);"

        return try {
            return jdbc.query(sql, IdMapper(), phoneNumber)
        } catch (ex: EmptyResultDataAccessException) {
            return listOf()
        }
    }

    override fun updateConsumer(consumer: Consumer) {
        val sql = """
            CALL UpdateConsumerById
            (?, ?, ?, ?, ?, ?)
         """.trimIndent()


        jdbc.update(sql,
            consumer.id,
            consumer.name,
            consumer.phoneNumber,
            consumer.birthdate,
            consumer.documents
        )
    }

    override fun isConsumerValid(consumerId: Int): Timestamp {
        TODO("Not yet implemented")
    }

    override fun getAllConsumerPerPage(pageNumber: Int, pageSize: Int): Pair<List<Consumer>,Int> {
        val sql = "CALL GetAllConsumersWithPaging(?, ?);"
        val consumers = mutableListOf<Consumer>()
        var totalItems = 0

        jdbc.query(sql, { rs, _ ->
            if (rs.isFirst) {
                totalItems = rs.getInt("totalItems")
            }

            val birthdate = rs.getDate("birthdate")?.toString() ?: "N/A"

            consumers.add(
                Consumer(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("phone_number"),
                    birthdate,
                    rs.getString("documents"),
                    rs.getString("location"),
                    mutableListOf()
                )
            )
        }, pageNumber, pageSize)

        return Pair(consumers,totalItems)
    }

    override fun deleteConsumerById(consumerId: Int) {
        try {
            val sql ="""
                CALL DeleteConsumerById(?)
            """.trimIndent()
            jdbc.update(sql, consumerId)
        }catch (ex:DataAccessException){
            throw ConsumerActionFailedException(ex.localizedMessage)
        }
    }

    override fun getAllConsumerBySearchKey(searchKey: String, pageSize: Int): Pair<List<Consumer>, Int> {
        val sql = "CALL FilterConsumerData(?, ?);"
        val consumers = mutableListOf<Consumer>()
        var totalItems = 0

        jdbc.query(sql, { rs, _ ->
            if (rs.isFirst) {
                totalItems = rs.getInt("totalItems")
            }

            val birthdate = rs.getDate("birthdate")?.toString() ?: "N/A"
            consumers.add(
                Consumer(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("phone_number"),
                    birthdate,
                    rs.getString("documents"),
                    rs.getString("location"),
                    mutableListOf()
                )
            )
        }, searchKey, pageSize)

        return Pair(consumers,totalItems)
    }

    override fun getConsumerCount(): ConsumerCount {
        val sql = """
            CALL GetConsumerCount();
        """.trimIndent()

        return jdbc.queryForObject(sql, CountMapper())!!
    }


    private class IdMapper : RowMapper<Int> {
        @Nullable
        @Throws(SQLException::class)
        override fun mapRow(resultSet: ResultSet, i: Int): Int {
            return  resultSet.getInt("id")
        }
    }

    private class CountMapper : RowMapper<ConsumerCount> {
        @Nullable
        @Throws(SQLException::class)
        override fun mapRow(resultSet: ResultSet, i: Int): ConsumerCount {
            return  ConsumerCount(resultSet.getInt("consumerCount"))
        }
    }
}