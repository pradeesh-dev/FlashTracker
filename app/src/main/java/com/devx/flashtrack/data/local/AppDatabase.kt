package com.devx.flashtrack.data.local

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.devx.flashtrack.data.local.dao.*
import com.devx.flashtrack.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@TypeConverters(Converters::class)
@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        CategoryEntity::class,
        DebtEntity::class,
        ReminderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun debtDao(): DebtDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        const val DATABASE_NAME = "flashtrack_db"

        fun prepopulate(db: AppDatabase) {
            CoroutineScope(Dispatchers.IO).launch {
                val categoryDao = db.categoryDao()
                val accountDao = db.accountDao()
                val transactionDao = db.transactionDao()

                // Default categories
                val defaultCategories = listOf(
                    CategoryEntity(name = "Groceries", iconName = "shopping_basket", colorHex = "#FF7043", isDefault = true, type = "EXPENSE", keywords = "bigbasket,grofers,blinkit,zepto,dmart"),
                    CategoryEntity(name = "Food & Dining", iconName = "restaurant", colorHex = "#FF5722", isDefault = true, type = "EXPENSE", keywords = "zomato,swiggy,restaurant,cafe,food"),
                    CategoryEntity(name = "Shopping", iconName = "shopping_bag", colorHex = "#E91E63", isDefault = true, type = "EXPENSE", keywords = "amazon,flipkart,myntra,ajio,meesho"),
                    CategoryEntity(name = "Bills & Utilities", iconName = "receipt_long", colorHex = "#9C27B0", isDefault = true, type = "EXPENSE", keywords = "electricity,water,gas,internet,broadband"),
                    CategoryEntity(name = "Entertainment", iconName = "movie", colorHex = "#673AB7", isDefault = true, type = "EXPENSE", keywords = "netflix,hotstar,amazon prime,spotify,youtube"),
                    CategoryEntity(name = "Travel", iconName = "flight", colorHex = "#3F51B5", isDefault = true, type = "EXPENSE", keywords = "makemytrip,goibibo,irctc,ola,uber,rapido"),
                    CategoryEntity(name = "Medical", iconName = "local_hospital", colorHex = "#F44336", isDefault = true, type = "EXPENSE", keywords = "pharmacy,hospital,doctor,medicine,1mg,pharmeasy"),
                    CategoryEntity(name = "Education", iconName = "school", colorHex = "#2196F3", isDefault = true, type = "EXPENSE", keywords = "udemy,coursera,unacademy,byju,book"),
                    CategoryEntity(name = "Fuel", iconName = "local_gas_station", colorHex = "#FF9800", isDefault = true, type = "EXPENSE", keywords = "petrol,diesel,fuel,hp,bharat petroleum"),
                    CategoryEntity(name = "EMI / Loans", iconName = "account_balance", colorHex = "#607D8B", isDefault = true, type = "EXPENSE", keywords = "emi,loan,bajaj,hdfc,icici"),
                    CategoryEntity(name = "Salary", iconName = "payments", colorHex = "#4CAF50", isDefault = true, type = "INCOME", keywords = "salary,stipend,payroll"),
                    CategoryEntity(name = "Freelance", iconName = "work", colorHex = "#8BC34A", isDefault = true, type = "INCOME", keywords = "freelance,project,contract"),
                    CategoryEntity(name = "Investment Returns", iconName = "trending_up", colorHex = "#00BCD4", isDefault = true, type = "INCOME", keywords = "dividend,interest,mutual fund,stocks"),
                    CategoryEntity(name = "Rent Income", iconName = "home", colorHex = "#009688", isDefault = true, type = "INCOME", keywords = "rent,rental"),
                    CategoryEntity(name = "Transfer", iconName = "swap_horiz", colorHex = "#78909C", isDefault = true, type = "BOTH", keywords = "transfer,neft,imps,upi"),
                    CategoryEntity(name = "Other", iconName = "category", colorHex = "#9E9E9E", isDefault = true, type = "BOTH", keywords = "")
                )
                defaultCategories.forEach { categoryDao.insertCategory(it) }

                // Default accounts
                val defaultAccounts = listOf(
                    AccountEntity(name = "Cash", type = AccountType.CASH, balance = 5000.0, colorHex = "#4CAF50", iconName = "payments", isDefault = true),
                    AccountEntity(name = "SBI Bank", type = AccountType.BANK, balance = 45000.0, colorHex = "#1565C0", iconName = "account_balance"),
                    AccountEntity(name = "GPay Wallet", type = AccountType.WALLET, balance = 2500.0, colorHex = "#4285F4", iconName = "account_balance_wallet")
                )
                val accountIds = defaultAccounts.map { accountDao.insertAccount(it) }

                // Sample transactions for the current month
                val now = System.currentTimeMillis()
                val dayMs = 86_400_000L
                val sampleTxns = listOf(
                    TransactionEntity(type = TransactionType.INCOME, amount = 65000.0, categoryId = 11, accountId = accountIds[1], title = "Monthly Salary", notes = "October salary credited", date = now - 10 * dayMs),
                    TransactionEntity(type = TransactionType.EXPENSE, amount = 3200.0, categoryId = 1, accountId = accountIds[1], title = "Groceries - BigBasket", tags = "essentials", date = now - 8 * dayMs),
                    TransactionEntity(type = TransactionType.EXPENSE, amount = 899.0, categoryId = 5, accountId = accountIds[1], title = "Netflix Subscription", tags = "netflix,subscription", date = now - 7 * dayMs),
                    TransactionEntity(type = TransactionType.EXPENSE, amount = 450.0, categoryId = 2, accountId = accountIds[2], title = "Zomato - Biryani", tags = "zomato,food", date = now - 5 * dayMs),
                    TransactionEntity(type = TransactionType.EXPENSE, amount = 1200.0, categoryId = 4, accountId = accountIds[1], title = "Electricity Bill", tags = "bills", date = now - 4 * dayMs),
                    TransactionEntity(type = TransactionType.EXPENSE, amount = 350.0, categoryId = 9, accountId = accountIds[0], title = "Petrol", tags = "fuel", date = now - 3 * dayMs),
                    TransactionEntity(type = TransactionType.EXPENSE, amount = 2599.0, categoryId = 3, accountId = accountIds[1], title = "Amazon Purchase", tags = "amazon,shopping", date = now - 2 * dayMs),
                    TransactionEntity(type = TransactionType.INCOME, amount = 8500.0, categoryId = 12, accountId = accountIds[0], title = "Freelance Project", tags = "freelance", date = now - 1 * dayMs),
                    TransactionEntity(type = TransactionType.EXPENSE, amount = 180.0, categoryId = 2, accountId = accountIds[2], title = "Swiggy - Pizza", tags = "swiggy,food", date = now - 12 * 3600 * 1000)
                )
                sampleTxns.forEach { transactionDao.insertTransaction(it) }
            }
        }
    }
}

class Converters {
    @TypeConverter fun fromAccountType(v: AccountType): String = v.name
    @TypeConverter fun toAccountType(v: String): AccountType = AccountType.valueOf(v)
    @TypeConverter fun fromTransactionType(v: TransactionType): String = v.name
    @TypeConverter fun toTransactionType(v: String): TransactionType = TransactionType.valueOf(v)
    @TypeConverter fun fromDebtType(v: DebtType): String = v.name
    @TypeConverter fun toDebtType(v: String): DebtType = DebtType.valueOf(v)
    @TypeConverter fun fromReminderInterval(v: ReminderInterval): String = v.name
    @TypeConverter fun toReminderInterval(v: String): ReminderInterval = ReminderInterval.valueOf(v)
}
