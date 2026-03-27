package com.devx.flashtrack.data.local

import androidx.room.*
import com.devx.flashtrack.data.local.dao.*
import com.devx.flashtrack.data.local.entity.*

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

        suspend fun prepopulate(db: AppDatabase) {
            val catDao = db.categoryDao()
            val accDao = db.accountDao()
            val txnDao = db.transactionDao()

            // Default categories
            val cats = listOf(
                CategoryEntity(name = "Groceries",        iconName = "shopping_basket",   colorHex = "#FF7043", isDefault = true, type = "EXPENSE",  keywords = "bigbasket,grofers,blinkit,zepto,dmart"),
                CategoryEntity(name = "Food & Dining",    iconName = "restaurant",         colorHex = "#FF5722", isDefault = true, type = "EXPENSE",  keywords = "zomato,swiggy,restaurant,cafe,food"),
                CategoryEntity(name = "Shopping",         iconName = "shopping_bag",       colorHex = "#E91E63", isDefault = true, type = "EXPENSE",  keywords = "amazon,flipkart,myntra,ajio"),
                CategoryEntity(name = "Bills & Utilities",iconName = "receipt_long",       colorHex = "#9C27B0", isDefault = true, type = "EXPENSE",  keywords = "electricity,water,gas,internet"),
                CategoryEntity(name = "Entertainment",    iconName = "movie",              colorHex = "#673AB7", isDefault = true, type = "EXPENSE",  keywords = "netflix,hotstar,spotify,youtube"),
                CategoryEntity(name = "Travel",           iconName = "flight",             colorHex = "#3F51B5", isDefault = true, type = "EXPENSE",  keywords = "makemytrip,irctc,ola,uber"),
                CategoryEntity(name = "Medical",          iconName = "local_hospital",     colorHex = "#F44336", isDefault = true, type = "EXPENSE",  keywords = "pharmacy,hospital,doctor,medicine"),
                CategoryEntity(name = "Education",        iconName = "school",             colorHex = "#2196F3", isDefault = true, type = "EXPENSE",  keywords = "udemy,coursera,book"),
                CategoryEntity(name = "Fuel",             iconName = "local_gas_station",  colorHex = "#FF9800", isDefault = true, type = "EXPENSE",  keywords = "petrol,diesel,fuel"),
                CategoryEntity(name = "EMI / Loans",      iconName = "account_balance",    colorHex = "#607D8B", isDefault = true, type = "EXPENSE",  keywords = "emi,loan"),
                CategoryEntity(name = "Salary",           iconName = "payments",           colorHex = "#4CAF50", isDefault = true, type = "INCOME",   keywords = "salary,stipend,payroll"),
                CategoryEntity(name = "Freelance",        iconName = "work",               colorHex = "#8BC34A", isDefault = true, type = "INCOME",   keywords = "freelance,project,contract"),
                CategoryEntity(name = "Investment Returns",iconName= "trending_up",        colorHex = "#00BCD4", isDefault = true, type = "INCOME",   keywords = "dividend,interest,mutual fund"),
                CategoryEntity(name = "Rent Income",      iconName = "home",               colorHex = "#009688", isDefault = true, type = "INCOME",   keywords = "rent,rental"),
                CategoryEntity(name = "Transfer",         iconName = "swap_horiz",         colorHex = "#78909C", isDefault = true, type = "BOTH",     keywords = "transfer,neft,imps,upi"),
                CategoryEntity(name = "Other",            iconName = "category",           colorHex = "#9E9E9E", isDefault = true, type = "BOTH",     keywords = "")
            )
            cats.forEach { catDao.insertCategory(it) }

            // Default accounts
            val cashId   = accDao.insertAccount(AccountEntity(name = "Cash",      type = AccountType.CASH,   balance = 5000.0,  colorHex = "#4CAF50", iconName = "payments",                 isDefault = true))
            val bankId   = accDao.insertAccount(AccountEntity(name = "SBI Bank",  type = AccountType.BANK,   balance = 45000.0, colorHex = "#1565C0", iconName = "account_balance"))
            val gpayId   = accDao.insertAccount(AccountEntity(name = "GPay",      type = AccountType.WALLET, balance = 2500.0,  colorHex = "#4285F4", iconName = "account_balance_wallet"))

            // Sample transactions (current month)
            val now = System.currentTimeMillis()
            val day = 86_400_000L
            val sampleTxns = listOf(
                TransactionEntity(type = TransactionType.INCOME,  amount = 65000.0, categoryId = 11, accountId = bankId,  title = "Monthly Salary",       notes = "October salary",      date = now - 10 * day),
                TransactionEntity(type = TransactionType.EXPENSE, amount = 3200.0,  categoryId = 1,  accountId = bankId,  title = "BigBasket Groceries",   tags  = "essentials",          date = now - 8  * day),
                TransactionEntity(type = TransactionType.EXPENSE, amount = 899.0,   categoryId = 5,  accountId = bankId,  title = "Netflix Subscription",  tags  = "netflix,subscription", date = now - 7  * day),
                TransactionEntity(type = TransactionType.EXPENSE, amount = 450.0,   categoryId = 2,  accountId = gpayId, title = "Zomato – Biryani",      tags  = "zomato,food",         date = now - 5  * day),
                TransactionEntity(type = TransactionType.EXPENSE, amount = 1200.0,  categoryId = 4,  accountId = bankId,  title = "Electricity Bill",      tags  = "bills",               date = now - 4  * day),
                TransactionEntity(type = TransactionType.EXPENSE, amount = 350.0,   categoryId = 9,  accountId = cashId, title = "Petrol",                 tags  = "fuel",                date = now - 3  * day),
                TransactionEntity(type = TransactionType.EXPENSE, amount = 2599.0,  categoryId = 3,  accountId = bankId,  title = "Amazon – Earphones",    tags  = "amazon,shopping",     date = now - 2  * day),
                TransactionEntity(type = TransactionType.INCOME,  amount = 8500.0,  categoryId = 12, accountId = cashId, title = "Freelance Project",      tags  = "freelance",           date = now - 1  * day),
                TransactionEntity(type = TransactionType.EXPENSE, amount = 180.0,   categoryId = 2,  accountId = gpayId, title = "Swiggy – Pizza",         tags  = "swiggy,food",         date = now - 12 * 3600 * 1000)
            )
            sampleTxns.forEach { txnDao.insertTransaction(it) }
        }
    }
}

class Converters {
    @TypeConverter fun accountTypeToString(v: AccountType): String      = v.name
    @TypeConverter fun stringToAccountType(v: String): AccountType      = AccountType.valueOf(v)
    @TypeConverter fun txnTypeToString(v: TransactionType): String      = v.name
    @TypeConverter fun stringToTxnType(v: String): TransactionType      = TransactionType.valueOf(v)
    @TypeConverter fun debtTypeToString(v: DebtType): String            = v.name
    @TypeConverter fun stringToDebtType(v: String): DebtType            = DebtType.valueOf(v)
    @TypeConverter fun reminderIntervalToString(v: ReminderInterval): String = v.name
    @TypeConverter fun stringToReminderInterval(v: String): ReminderInterval = ReminderInterval.valueOf(v)
}
