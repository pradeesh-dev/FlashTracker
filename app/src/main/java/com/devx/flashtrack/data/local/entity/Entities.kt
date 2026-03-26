package com.devx.flashtrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

// ─── Account ────────────────────────────────────────────────────────────────

enum class AccountType { BANK, WALLET, CASH, CREDIT_CARD }

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: Double = 0.0,
    val currency: String = "INR",
    val colorHex: String = "#4CAF50",
    val iconName: String = "account_balance",
    val isDefault: Boolean = false,
    // Credit card specific
    val creditLimit: Double = 0.0,
    val availableCredit: Double = 0.0,
    val billingCycleStart: Int = 1,  // day of month
    val billingCycleEnd: Int = 30,
    val dueDate: Int = 15,           // day of month
    val rewardPoints: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

// ─── Transaction ─────────────────────────────────────────────────────────────

enum class TransactionType { EXPENSE, INCOME, TRANSFER }

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val amount: Double,
    val categoryId: Long,
    val accountId: Long,
    val toAccountId: Long? = null,   // for transfers
    val title: String,
    val notes: String = "",
    val tags: String = "",           // comma-separated
    val receiptPath: String? = null,
    val date: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null, // DAILY, WEEKLY, MONTHLY, YEARLY
    val recurringEndDate: Long? = null,
    val parentTransactionId: Long? = null, // for recurring children
    val createdAt: Long = System.currentTimeMillis()
)

// ─── Category ────────────────────────────────────────────────────────────────

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val type: String = "BOTH",       // EXPENSE, INCOME, BOTH
    val budget: Double = 0.0,        // monthly budget (0 = no budget)
    val keywords: String = "",       // comma-separated for auto-categorization
    val createdAt: Long = System.currentTimeMillis()
)

// ─── Debt / IOU ──────────────────────────────────────────────────────────────

enum class DebtType { LENT, BORROWED }

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: DebtType,
    val personName: String,
    val personContact: String = "",
    val amount: Double,
    val settledAmount: Double = 0.0,
    val notes: String = "",
    val date: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val isSettled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// ─── Reminder ────────────────────────────────────────────────────────────────

enum class ReminderInterval { DAILY, WEEKLY, MONTHLY, YEARLY, ONCE }

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val amount: Double = 0.0,
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val interval: ReminderInterval,
    val nextDueDate: Long,
    val isActive: Boolean = true,
    val notificationId: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
