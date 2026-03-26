package com.devx.flashtrack.data.local.dao

import androidx.room.*
import com.devx.flashtrack.data.local.entity.*
import kotlinx.coroutines.flow.Flow

// ─── AccountDao ───────────────────────────────────────────────────────────────

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY isDefault DESC, name ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("UPDATE accounts SET balance = balance + :amount WHERE id = :id")
    suspend fun updateBalance(id: Long, amount: Double)

    @Query("SELECT SUM(balance) FROM accounts WHERE type != 'CREDIT_CARD'")
    fun getTotalBalance(): Flow<Double?>

    @Query("SELECT SUM(availableCredit) FROM accounts WHERE type = 'CREDIT_CARD'")
    fun getTotalAvailableCredit(): Flow<Double?>
}

// ─── TransactionDao ───────────────────────────────────────────────────────────

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC")
    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getTransactionsByCategory(categoryId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 10): Flow<List<TransactionEntity>>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate
    """)
    fun getTotalExpenseInRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE type = 'INCOME' AND date BETWEEN :startDate AND :endDate
    """)
    fun getTotalIncomeInRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("""
        SELECT categoryId, SUM(amount) as total FROM transactions
        WHERE type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate
        GROUP BY categoryId ORDER BY total DESC
    """)
    fun getCategoryWiseSpending(startDate: Long, endDate: Long): Flow<List<CategorySpending>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?
}

data class CategorySpending(val categoryId: Long, val total: Double)

// ─── CategoryDao ──────────────────────────────────────────────────────────────

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY isDefault DESC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories WHERE type IN ('EXPENSE', 'BOTH') ORDER BY isDefault DESC, name ASC")
    fun getExpenseCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type IN ('INCOME', 'BOTH') ORDER BY isDefault DESC, name ASC")
    fun getIncomeCategories(): Flow<List<CategoryEntity>>
}

// ─── DebtDao ──────────────────────────────────────────────────────────────────

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY date DESC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE isSettled = 0 ORDER BY date DESC")
    fun getActiveDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE type = :type AND isSettled = 0 ORDER BY date DESC")
    fun getDebtsByType(type: DebtType): Flow<List<DebtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)

    @Query("SELECT SUM(amount - settledAmount) FROM debts WHERE type = 'LENT' AND isSettled = 0")
    fun getTotalLent(): Flow<Double?>

    @Query("SELECT SUM(amount - settledAmount) FROM debts WHERE type = 'BORROWED' AND isSettled = 0")
    fun getTotalBorrowed(): Flow<Double?>
}

// ─── ReminderDao ──────────────────────────────────────────────────────────────

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY nextDueDate ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isActive = 1 ORDER BY nextDueDate ASC")
    fun getActiveReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderEntity?
}
