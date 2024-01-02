package com.libanux.medicaldashboard_backend.transactions.repositories

import com.libanux.medicaldashboard_backend.consumer.models.ConsumerCount
import com.libanux.medicaldashboard_backend.consumer.repositories.ConsumerDataAccessService
import com.libanux.medicaldashboard_backend.items.ItemActionFailedException
import com.libanux.medicaldashboard_backend.items.models.Item
import com.libanux.medicaldashboard_backend.transactions.models.*
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import java.sql.Connection
import java.sql.Date
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import javax.annotation.Nullable

@Repository
class TransactionDataAccessService(
    private val jdbc: JdbcTemplate,
    private val transactionTemplate: TransactionTemplate): JdbcRepository{

    override fun insertPullTransaction(pull: TransactionPull) {
        try {
          insertPullTransactionWithTransaction(pull, transactionTemplate)
        } catch (ex: DataAccessException) {
            throw TransactionActionFailedException(ex.message)
        }
    }

    private fun insertPullTransactionWithTransaction(pull: TransactionPull, transactionTemplate: TransactionTemplate) {

        transactionTemplate.execute { status: TransactionStatus ->
            try {
                val updateQuantityForEachItem = "CALL UpdateItemQuantity(?,?);"
                val insertTransactionSql = "CALL InsertPullTransaction(?,?,?,?,?);"

                for (item in pull.item) {
                    jdbc.update(
                        updateQuantityForEachItem,
                        item.id,
                        item.pullQuantity
                    )

                    jdbc.update(
                        insertTransactionSql,
                        item.id,
                        pull.consumer?.id,
                        pull.transactionType,
                        item.pullQuantity,
                        pull.patientReportLink
                    )
                }
            } catch (ex: DataAccessException) {
                status.setRollbackOnly()
                if (ex.message.toString().contains("Insufficient quantity available for item."))
                         throw TransactionActionFailedException("Insufficient quantity available for item")
                else
                    throw TransactionActionFailedException("Error occurred,please try again.")
            }
            null
        }
    }

    override fun insertPushTransaction(push: TransactionPush) {

        transactionTemplate.execute { status: TransactionStatus ->
            try {
                val insertTransactionSql = "CALL PushTransaction(?,?,?,?);"


                    jdbc.update(
                        insertTransactionSql,
                        push.user?.id,
                        push.item?.id,
                        push.item?.pullQuantity,
                        push.location
                    )
            } catch (ex: DataAccessException) {
                status.setRollbackOnly()
                if (ex.message.toString().contains("User or item does not exist."))
                    throw TransactionActionFailedException("User or item does not exist.")
                else
                    throw TransactionActionFailedException("Error occurred,please try again.")
            }
            null
        }
    }

    override fun getPushTransaction(pageNumber: Int, pageSize: Int): Pair<List<TransactionPush>, Int> {
        val sql = "CALL GetPushTransactions(?,?);"
        val transactions: MutableList<TransactionPush> = mutableListOf()
        var totalItems = 0

        jdbc.query(sql, { rs, _ ->
            if (rs.isFirst) {
                totalItems = rs.getInt("totalItems")
            }

            val transactionId = rs.getLong(1)

            val transactionPush = TransactionPush(
                transactionId,
                ItemTransaction(
                    rs.getLong(4),
                    rs.getString(5),
                    rs.getInt(9)
                ),
                UserTransaction(
                    rs.getLong(2),
                    rs.getString(3)
                ),
                "push",
                rs.getString(7),
                rs.getString(6)
            )

            transactions.add(transactionPush)
        }, pageNumber, pageSize)

        return Pair(transactions, totalItems)
    }

    override fun getPullTransaction(pageNumber: Int, pageSize: Int): Pair<List<TransactionPullParsing>, Int> {
        val sql = "CALL GetPullTransactions(?,?);"
        val transactions: MutableList<TransactionPullParsing> = mutableListOf()
        var totalItems = 0

        jdbc.query(sql, { rs, _ ->
            if (rs.isFirst) {
                totalItems = rs.getInt("totalItems")
            }

            val transactionId = rs.getLong(1)

            val transactionPull = TransactionPullParsing(
                transactionId,
                ItemTransaction(
                    rs.getLong(4),
                    rs.getString(5),
                    rs.getInt(8)
                ),
                ConsumerTransaction(
                    rs.getLong(2),
                    rs.getString(3)
                ),
                "pull",
                rs.getString(6),
                rs.getString(9)
            )

            transactions.add(transactionPull)
        }, pageNumber, pageSize)

        return Pair(transactions, totalItems)
    }

    override fun updatePullTransaction(pull: TransactionPullParsing) {
        transactionTemplate.execute {
            val updateQuantitySql = "CALL UpdateItemQuantityTransaction(?, ?);"

            jdbc.update(
                updateQuantitySql,
                pull.item?.id,
                pull.item?.pullQuantity
            )

            val updateTransactionSql = "CALL UpdatePullTransaction(?, ?, ?, ?,?)"
            jdbc.update(
                updateTransactionSql,
                pull.id,
                pull.item?.id,
                pull.consumer?.id,
                pull.patientReportLink,
                pull.item?.pullQuantity
            )
            null
        }
    }

    override fun updatePushTransaction(push: TransactionPush) {
        transactionTemplate.execute {
            val updateQuantitySql = "CALL UpdateItemQuantityTransaction(?, ?)"

            jdbc.update(
                updateQuantitySql,
                push.item?.id,
                push.item?.pullQuantity
            )

            val updateTransactionSql = "CALL UpdatePushTransaction(?, ?, ?,?)"
            jdbc.update(
                updateTransactionSql,
                push.id,
                push.item?.id,
                push.location,
                push.item?.pullQuantity
            )
            null
        }
    }

    override fun deletePushTransaction(transactionId: Long) {
        try {
            val sql = "CALL DeletePushTransaction(?)"
            executeDeleteTransaction(sql, transactionId)
        }catch (ex:RuntimeException){
            throw TransactionActionFailedException(ex.message)
        }

    }

    override fun deletePullTransaction(transactionId: Long) {
        try {
            val sql = "CALL DeletePullTransaction(?)"
            executeDeleteTransaction(sql, transactionId)
        } catch (ex:RuntimeException) {
            throw TransactionActionFailedException(ex.message)
        }

    }


    private fun executeDeleteTransaction(sql: String, transactionId: Long): String {
        val rowsAffected = jdbc.update(sql, transactionId)
        return if (rowsAffected > 0) {
            "Transaction deleted successfully."
        } else {
            throw RuntimeException("Error deleting transaction")
        }
    }

    override fun getPushCount(): TransactionPushCount {
        val sql = """
            CALL GetTransactionPushCount();
        """.trimIndent()

        return jdbc.queryForObject(sql, CountMapper())!!
    }

    override fun getPullCount(): TransactionPullCount {
        val sql = """
            CALL GetTransactionPullCount();
        """.trimIndent()

        return jdbc.queryForObject(sql, CountMapper1())!!
    }

    override fun getMonthForTransactionWithItemAndConsumerID(itemId: Long, consumerID: Long?): Timestamp? {
        try {
            val sql = "{call GetLastTransactionDateByItemAndConsumer(?, ?)}"

            val result = jdbc.execute(sql) { callableStatement ->
                callableStatement.setLong(1, itemId)
                callableStatement.setLong(2, consumerID ?: 0) // Use 0 as default if consumerID is null

                val resultSet = callableStatement.executeQuery()
                if (resultSet.next()) {
                    println(resultSet.getTimestamp(1))
                    resultSet.getTimestamp(1)
                } else {
                    println("its null")

                    null
                }
            }

            return result
        } catch (ex: DataAccessException) {
            throw TransactionActionFailedException(ex.message.toString())
        }
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

    private class ItemValidityRowMapper : RowMapper<Int> {
        @Throws(SQLException::class)
        override fun mapRow(resultSet: ResultSet, i: Int): Int {
            return  resultSet.getInt("item_validity")
        }
    }


    private class CountMapper : RowMapper<TransactionPushCount> {
        @Nullable
        @Throws(SQLException::class)
        override fun mapRow(resultSet: ResultSet, i: Int): TransactionPushCount {
            return  TransactionPushCount(resultSet.getInt("pushCount"))
        }
    }

    private class DateRowMapper : RowMapper<Timestamp> {
        @Nullable
        @Throws(SQLException::class)
        override fun mapRow(resultSet: ResultSet, i: Int): Timestamp {
            return  resultSet.getTimestamp("last_transaction_date")
        }
    }

    private class CountMapper1 : RowMapper<TransactionPullCount> {
        @Nullable
        @Throws(SQLException::class)
        override fun mapRow(resultSet: ResultSet, i: Int): TransactionPullCount {
            return  TransactionPullCount(resultSet.getInt("pullCount"))
        }
    }

}