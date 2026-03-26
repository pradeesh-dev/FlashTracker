package com.devx.flashtrack.data.repository

import com.devx.flashtrack.data.local.dao.*
import com.devx.flashtrack.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val debtDao: DebtDao,
    private val reminderDao: ReminderDao
) {
    // ─── Accounts ──────────────────────────────────────────────────────────
    fun getAllAccounts(): Flow<List<AccountEntity>> = accountDao.getAllAccounts()
    fun getTotalBalance(): Flow<Double?> = accountDao.getTotalBalance()
    fun getTotalAvailableCredit(): Flow<Double?> = accountDao.getTotalAvailableCredit()
    suspend fun insertAccount(account: AccountEntity): Long = accountDao.insertAccount(account)
    suspend fun updateAccount(account: AccountEntity) = accountDao.updateAccount(account)
    suspend fun deleteAccount(account: AccountEntity) = accountDao.deleteAccount(account)
    suspend fun getAccountById(id: Long): AccountEntity? = accountDao.getAccountById(id)

    // ─── Transactions ──────────────────────────────────────────────────────
    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    fun getRecentTransactions(limit: Int = 10): Flow<List<TransactionEntity>> =
        transactionDao.getRecentTransactions(limit)
    fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByDateRange(start, end)
    fun getTotalExpenseInRange(start: Long, end: Long): Flow<Double?> =
        transactionDao.getTotalExpenseInRange(start, end)
    fun getTotalIncomeInRange(start: Long, end: Long): Flow<Double?> =
        transactionDao.getTotalIncomeInRange(start, end)
    fun getCategoryWiseSpending(start: Long, end: Long): Flow<List<CategorySpending>> =
        transactionDao.getCategoryWiseSpending(start, end)

    suspend fun insertTransaction(t: TransactionEntity): Long {
        val id = transactionDao.insertTransaction(t)
        // Update account balance
        when (t.type) {
            TransactionType.EXPENSE -> accountDao.updateBalance(t.accountId, -t.amount)
            TransactionType.INCOME -> accountDao.updateBalance(t.accountId, t.amount)
            TransactionType.TRANSFER -> {
                accountDao.updateBalance(t.accountId, -t.amount)
                t.toAccountId?.let { accountDao.updateBalance(it, t.amount) }
            }
        }
        return id
    }

    suspend fun deleteTransaction(t: TransactionEntity) {
        transactionDao.deleteTransaction(t)
        // Reverse balance impact
        when (t.type) {
            TransactionType.EXPENSE -> accountDao.updateBalance(t.accountId, t.amount)
            TransactionType.INCOME -> accountDao.updateBalance(t.accountId, -t.amount)
            TransactionType.TRANSFER -> {
                accountDao.updateBalance(t.accountId, t.amount)
                t.toAccountId?.let { accountDao.updateBalance(it, -t.amount) }
            }
        }
    }

    suspend fun updateTransaction(old: TransactionEntity, new: TransactionEntity) {
        deleteTransaction(old)
        insertTransaction(new)
    }

    // ─── Categories ────────────────────────────────────────────────────────
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    fun getExpenseCategories(): Flow<List<CategoryEntity>> = categoryDao.getExpenseCategories()
    fun getIncomeCategories(): Flow<List<CategoryEntity>> = categoryDao.getIncomeCategories()
    suspend fun insertCategory(cat: CategoryEntity): Long = categoryDao.insertCategory(cat)
    suspend fun updateCategory(cat: CategoryEntity) = categoryDao.updateCategory(cat)
    suspend fun deleteCategory(cat: CategoryEntity) = categoryDao.deleteCategory(cat)
    suspend fun getCategoryById(id: Long): CategoryEntity? = categoryDao.getCategoryById(id)

    // ─── Debts ─────────────────────────────────────────────────────────────
    fun getAllDebts(): Flow<List<DebtEntity>> = debtDao.getAllDebts()
    fun getActiveDebts(): Flow<List<DebtEntity>> = debtDao.getActiveDebts()
    fun getTotalLent(): Flow<Double?> = debtDao.getTotalLent()
    fun getTotalBorrowed(): Flow<Double?> = debtDao.getTotalBorrowed()
    suspend fun insertDebt(debt: DebtEntity): Long = debtDao.insertDebt(debt)
    suspend fun updateDebt(debt: DebtEntity) = debtDao.updateDebt(debt)
    suspend fun deleteDebt(debt: DebtEntity) = debtDao.deleteDebt(debt)

    suspend fun settleDebt(debt: DebtEntity, amount: Double) {
        val newSettled = debt.settledAmount + amount
        val isSettled = newSettled >= debt.amount
        debtDao.updateDebt(debt.copy(settledAmount = newSettled, isSettled = isSettled))
    }

    // ─── Reminders ─────────────────────────────────────────────────────────
    fun getAllReminders(): Flow<List<ReminderEntity>> = reminderDao.getAllReminders()
    fun getActiveReminders(): Flow<List<ReminderEntity>> = reminderDao.getActiveReminders()
    suspend fun insertReminder(r: ReminderEntity): Long = reminderDao.insertReminder(r)
    suspend fun updateReminder(r: ReminderEntity) = reminderDao.updateReminder(r)
    suspend fun deleteReminder(r: ReminderEntity) = reminderDao.deleteReminder(r)

    // ─── Helpers ───────────────────────────────────────────────────────────
    fun currentMonthRange(): Pair<Long, Long> {
        val now = LocalDate.now()
        val start = now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = now.withDayOfMonth(now.lengthOfMonth())
            .atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return start to end
    }
}
