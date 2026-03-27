package com.devx.flashtrack.di

import android.content.Context
import androidx.room.Room
import com.devx.flashtrack.data.local.AppDatabase
import com.devx.flashtrack.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val prefs = context.getSharedPreferences("flashtrack_prefs", Context.MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("db_initialized", false).not()

        val db = Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

        if (isFirstRun) {
            prefs.edit().putBoolean("db_initialized", true).apply()
            CoroutineScope(Dispatchers.IO).launch {
                AppDatabase.prepopulate(db)
            }
        }
        return db
    }

    @Provides fun provideAccountDao(db: AppDatabase): AccountDao         = db.accountDao()
    @Provides fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()
    @Provides fun provideCategoryDao(db: AppDatabase): CategoryDao       = db.categoryDao()
    @Provides fun provideDebtDao(db: AppDatabase): DebtDao               = db.debtDao()
    @Provides fun provideReminderDao(db: AppDatabase): ReminderDao       = db.reminderDao()
}
