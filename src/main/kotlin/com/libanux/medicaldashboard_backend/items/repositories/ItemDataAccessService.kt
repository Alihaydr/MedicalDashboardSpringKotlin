package com.libanux.medicaldashboard_backend.items.repositories

import com.libanux.medicaldashboard_backend.consumer.models.ConsumerCount
import com.libanux.medicaldashboard_backend.consumer.repositories.ConsumerDataAccessService
import com.libanux.medicaldashboard_backend.items.ItemActionFailedException
import com.libanux.medicaldashboard_backend.items.models.Category
import com.libanux.medicaldashboard_backend.items.models.Item
import com.libanux.medicaldashboard_backend.items.models.ItemCount
import com.libanux.medicaldashboard_backend.transactions.models.ItemTransaction
import com.libanux.medicaldashboard_backend.transactions.models.TransactionPush
import com.libanux.medicaldashboard_backend.transactions.models.UserTransaction
import com.libanux.medicaldashboard_backend.transactions.services.TransactionService
import com.libanux.medicaldashboard_backend.user.model.UserSignUp
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Repository
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import java.sql.Date
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import javax.annotation.Nullable

@Repository
class ItemDataAccessService(private val jdbc:JdbcTemplate,
                            private val transactionService: TransactionService,
                            private val transactionTemplate: TransactionTemplate) : JdbcRepository {

    override fun insertItem(item: Item, userDetails: UserSignUp) {

        transactionTemplate.execute { status: TransactionStatus ->
            try {
                val insertItemSql = "CALL InsertItemWithCategories(?,?,?,?,?,?,?);"

                val variantsString = item.variants?.joinToString(",")

                val keyHolder = GeneratedKeyHolder()

                jdbc.update({ connection ->
                    val ps = connection.prepareStatement(insertItemSql, arrayOf("id"))
                    ps.setString(1, item.name)
                    ps.setString(2, item.notes)
                    ps.setString(3, variantsString)
                    ps.setInt(4, item.quantity!!)
                    ps.setDate(5, Date.valueOf(item.expiryDate))
                    ps.setString(6, item.location)
                    ps.setString(7, item.categories.map { it.id }.joinToString(","))
                    ps
                }, keyHolder)

                val queryToGetLastId = "SELECT id FROM item WHERE name = ?;"
                val generatedItemId = jdbc.queryForObject(queryToGetLastId, Long::class.java, item.name)

                val pushTransaction = TransactionPush(
                    0,
                    ItemTransaction(generatedItemId, "",item.quantity),
                    UserTransaction(userDetails.id,""),
                    "", null,
                    item.location
                )

                    transactionService.pushTransaction(pushTransaction)
            } catch (ex: DataAccessException) {
                status.setRollbackOnly()
                throw ItemActionFailedException(ex.message)
            }
                null
        }
    }



    override fun getItemsPerPage(pageNumber: Int, pageSize: Int): Pair<List<Item>, Int> {
        val sql = "CALL GetItemsPerPage(?,?);"
        val itemsMap = mutableMapOf<Long, Item>() // Use a map to track items by ID
        var totalItems = 0
        var variantsArray:MutableList<String> = mutableListOf()

        jdbc.query(sql, { rs, _ ->
            if (rs.isFirst) {
                totalItems = rs.getInt("totalItems")
            }

            val itemId = rs.getLong(1)

            val variants = rs.getString(4) ?: ""
            variantsArray = variants.split(",").toMutableList()

            val item = itemsMap.getOrPut(itemId) {
                Item(
                    itemId,
                    rs.getString(2),
                    rs.getString(3),
                    variantsArray,
                    rs.getInt(5),
                    rs.getString(6),
                    rs.getString(7),
                    mutableSetOf()
                )
            }

        }, pageNumber, pageSize)

        val items = itemsMap.values.toList()
        return Pair(items, totalItems)
    }

    override fun getItemById(itemId: Int): Item {
        TODO("Not yet implemented")
    }

    override fun updateItem(item: Item) {
        try {
            val insertItemSql = "CALL UpdateItem(?,?,?,?,?,?,?)"

            val variantsString = item.variants?.joinToString(",")

            jdbc.update(
                insertItemSql,
                item.name,
                item.notes,
                variantsString,
                item.quantity,
                Date.valueOf(item.expiryDate),
                item.location
//                item.categories.map { it.id }.joinToString(",")
            )
        } catch (ex: DataAccessException) {
            throw ItemActionFailedException(ex.message)
        }
    }

    override fun deleteItemById(itemId: Int) {
        try {
            val deleteItemSql = "CALL DeleteItemById(?)"

            jdbc.update(
                deleteItemSql,
                itemId
            )
        } catch (ex: DataAccessException) {
            throw ItemActionFailedException(ex.message)
        }
    }

    override fun getCategories(): List<Category> {
        try {
            val deleteItemSql = "CALL GetCategories();"

            return jdbc.query(deleteItemSql) { rs, _ ->
                Category(
                    rs.getLong("id"),
                    rs.getString("category_name")
                )
            }
        } catch (ex: DataAccessException) {
            throw ItemActionFailedException(ex.message)
        }
    }

    override fun getItemByCategory(categoryId: Int): Item {
        TODO("Not yet implemented")
    }

    override fun getItemByName(name: String): List<Int> {
        return try {
            val sql = """
                    CALL GetItemByName(?)
                """.trimIndent()

            jdbc.query(sql, IdMapper(), name)
        }catch (ex:DataAccessException){
            listOf()
        }
    }

    override fun getItemCount(): ItemCount {
        val sql = """
            CALL getItemCount();
        """.trimIndent()

        return jdbc.queryForObject(sql, CountMapper())!!
    }

    override fun getItemValidity(itemId: Int): Int {
        try {
            val sql = """
                CALL GetItemValidity(?);
            """.trimIndent()

            return jdbc.queryForObject(sql, ItemValidityRowMapper(),itemId) ?: 0
        }catch (ex:DataAccessException){
            throw ItemActionFailedException(ex.message.toString())
        }
    }

    private class IdMapper : RowMapper<Int> {
        @Nullable
        @Throws(SQLException::class)
        override fun mapRow(resultSet: ResultSet, i: Int): Int {
            return  resultSet.getInt("id")
        }
    }

    private class ItemValidityRowMapper : RowMapper<Int> {
        @Throws(SQLException::class)
        override fun mapRow(resultSet: ResultSet, i: Int): Int {
            return  resultSet.getInt("item_validity_output")
        }
    }

    private class CountMapper : RowMapper<ItemCount> {
        @Nullable
        @Throws(SQLException::class)
        override fun mapRow(resultSet: ResultSet, i: Int): ItemCount {
            return ItemCount(resultSet.getInt("itemCount"))
        }
    }
}