package com.devx.flashtrack.data.repository

import com.devx.flashtrack.data.local.dao.*
import com.devx.flashtrack.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val accountDao:     AccountDao,
    private val transactionDao: TransactionDao,
    private val categoryDao:    CategoryDao,
    private val debtDao:        DebtDao,
    private val reminderDao:    ReminderDao
) {
    // ── Accounts ────────────────────────────────────────────────────────────
    fun getAllAccounts(): Flow<List<AccountEntity>>   = accountDao.getAllAccounts()
    fun getTotalBalance(): Flow<Double?>             = accountDao.getTotalBalance()
    fun getTotalAvailableCredit(): Flow<Double?>     = accountDao.getTotalAvailableCredit()
    suspend fun insertAccount(a: AccountEntity): Long = accountDao.insertAccount(a)
    suspend fun updateAccount(a: AccountEntity)       = accountDao.updateAccount(a)
    suspend fun deleteAccount(a: AccountEntity)       = accountDao.deleteAccount(a)
    suspend fun getAccountById(id: Long)              = accountDao.getAccountById(id)

    // ── Transactions ─────────────────────────────────────────────────────────
    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    fun getRecentTransactions(n: Int = 15): Flow<List<TransactionEntity>> = transactionDao.getRecentTransactions(n)
    fun getTransactionsByDateRange(s: Long, e: Long) = transactionDao.getTransactionsByDateRange(s, e)
    fun getTotalExpenseInRange(s: Long, e: Long)     = transactionDao.getTotalExpenseInRange(s, e)
    fun getTotalIncomeInRange(s: Long, e: Long)      = transactionDao.getTotalIncomeInRange(s, e)
    fun getCategoryWiseSpending(s: Long, e: Long)    = transactionDao.getCategoryWiseSpending(s, e)

    suspend fun insertTransaction(t: TransactionEntity): Long {
        val id = transactionDao.insertTransaction(t)
        when (t.type) {
            TransactionType.EXPENSE  -> accountDao.updateBalance(t.accountId, -t.amount)
            TransactionType.INCOME   -> accountDao.updateBalance(t.accountId,  t.amount)
            TransactionType.TRANSFER -> {
                accountDao.updateBalance(t.accountId, -t.amount)
                t.toAccountId?.let { accountDao.updateBalance(it, t.amount) }
            }
        }
        return id
    }

    suspend fun deleteTransaction(t: TransactionEntity) {
        transactionDao.deleteTransaction(t)
        when (t.type) {
            TransactionType.EXPENSE  -> accountDao.updateBalance(t.accountId,  t.amount)
            TransactionType.INCOME   -> accountDao.updateBalance(t.accountId, -t.amount)
            TransactionType.TRANSFER -> {
                accountDao.updateBalance(t.accountId,  t.amount)
                t.toAccountId?.let { accountDao.updateBalance(it, -t.amount) }
            }
        }
    }

    // ── Categories ───────────────────────────────────────────────────────────
    fun getAllCategories(): Flow<List<CategoryEntity>>  = categoryDao.getAllCategories()
    fun getExpenseCategories(): Flow<List<CategoryEntity>> = categoryDao.getExpenseCategories()
    fun getIncomeCategories(): Flow<List<CategoryEntity>>  = categoryDao.getIncomeCategories()
    suspend fun insertCategory(c: CategoryEntity): Long    = categoryDao.insertCategory(c)
    suspend fun updateCategory(c: CategoryEntity)          = categoryDao.updateCategory(c)
    suspend fun deleteCategory(c: CategoryEntity)          = categoryDao.deleteCategory(c)

    // ── Debts ────────────────────────────────────────────────────────────────
    fun getAllDebts(): Flow<List<DebtEntity>>   = debtDao.getAllDebts()
    fun getActiveDebts(): Flow<List<DebtEntity>> = debtDao.getActiveDebts()
    fun getTotalLent(): Flow<Double?>           = debtDao.getTotalLent()
    fun getTotalBorrowed(): Flow<Double?>       = debtDao.getTotalBorrowed()
    suspend fun insertDebt(d: DebtEntity): Long = debtDao.insertDebt(d)
    suspend fun updateDebt(d: DebtEntity)       = debtDao.updateDebt(d)
    suspend fun deleteDebt(d: DebtEntity)       = debtDao.deleteDebt(d)

    suspend fun settleDebt(debt: DebtEntity, amount: Double) {
        val newSettled = debt.settledAmount + amount
        debtDao.updateDebt(debt.copy(
            settledAmount = newSettled,
            isSettled     = newSettled >= debt.amount
        ))
    }

    // ── Reminders ────────────────────────────────────────────────────────────
    fun getAllReminders(): Flow<List<ReminderEntity>>    = reminderDao.getAllReminders()
    fun getActiveReminders(): Flow<List<ReminderEntity>> = reminderDao.getActiveReminders()
    suspend fun insertReminder(r: ReminderEntity): Long  = reminderDao.insertReminder(r)
    suspend fun updateReminder(r: ReminderEntity)        = reminderDao.updateReminder(r)
    suspend fun deleteReminder(r: ReminderEntity)        = reminderDao.deleteReminder(r)

    // ── Helpers ──────────────────────────────────────────────────────────────
    fun monthRange(ym: YearMonth = YearMonth.now()): Pair<Long, Long> {
        val zone  = ZoneId.systemDefault()
        val start = ym.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end   = ym.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
        return start to end
    }
}
