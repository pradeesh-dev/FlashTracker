package com.devx.flashtrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devx.flashtrack.data.local.entity.*
import com.devx.flashtrack.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo: AppRepository
) : ViewModel() {

    // ── Month selector ───────────────────────────────────────────────────────
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    // ── Balance visibility ───────────────────────────────────────────────────
    private val _showBalance = MutableStateFlow(true)
    val showBalance: StateFlow<Boolean> = _showBalance.asStateFlow()

    // ── Accounts ─────────────────────────────────────────────────────────────
    val accounts = repo.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalBalance = repo.getTotalBalance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val totalCredit = repo.getTotalAvailableCredit()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // ── Transactions ─────────────────────────────────────────────────────────
    val recentTransactions = repo.getRecentTransactions(15)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allTransactions = repo.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Categories ────────────────────────────────────────────────────────────
    val categories = repo.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Debts ─────────────────────────────────────────────────────────────────
    val debts = repo.getAllDebts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalLent = repo.getTotalLent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val totalBorrowed = repo.getTotalBorrowed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // ── Reminders ─────────────────────────────────────────────────────────────
    val reminders = repo.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Current-month home stats ──────────────────────────────────────────────
    private val nowRange = repo.monthRange(YearMonth.now())

    val currentMonthExpense = repo.getTotalExpenseInRange(nowRange.first, nowRange.second)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val currentMonthIncome = repo.getTotalIncomeInRange(nowRange.first, nowRange.second)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    // ── Selected-month analytics (reacts to month picker) ────────────────────
    val monthlyExpense = _selectedMonth.flatMapLatest { ym ->
        val (s, e) = repo.monthRange(ym)
        repo.getTotalExpenseInRange(s, e)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val monthlyIncome = _selectedMonth.flatMapLatest { ym ->
        val (s, e) = repo.monthRange(ym)
        repo.getTotalIncomeInRange(s, e)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val categorySpending = _selectedMonth.flatMapLatest { ym ->
        val (s, e) = repo.monthRange(ym)
        repo.getCategoryWiseSpending(s, e)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val monthlyTransactions = _selectedMonth.flatMapLatest { ym ->
        val (s, e) = repo.monthRange(ym)
        repo.getTransactionsByDateRange(s, e)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Actions ───────────────────────────────────────────────────────────────
    fun toggleBalance()   { _showBalance.value = !_showBalance.value }
    fun selectMonth(ym: YearMonth) { _selectedMonth.value = ym }

    fun addTransaction(t: TransactionEntity)    = viewModelScope.launch { repo.insertTransaction(t) }
    fun deleteTransaction(t: TransactionEntity) = viewModelScope.launch { repo.deleteTransaction(t) }

    fun addAccount(a: AccountEntity)    = viewModelScope.launch { repo.insertAccount(a) }
    fun updateAccount(a: AccountEntity) = viewModelScope.launch { repo.updateAccount(a) }
    fun deleteAccount(a: AccountEntity) = viewModelScope.launch { repo.deleteAccount(a) }

    fun addCategory(c: CategoryEntity)    = viewModelScope.launch { repo.insertCategory(c) }
    fun updateCategory(c: CategoryEntity) = viewModelScope.launch { repo.updateCategory(c) }
    fun deleteCategory(c: CategoryEntity) = viewModelScope.launch { repo.deleteCategory(c) }

    fun addDebt(d: DebtEntity)                    = viewModelScope.launch { repo.insertDebt(d) }
    fun settleDebt(d: DebtEntity, amt: Double)    = viewModelScope.launch { repo.settleDebt(d, amt) }
    fun deleteDebt(d: DebtEntity)                 = viewModelScope.launch { repo.deleteDebt(d) }

    fun addReminder(r: ReminderEntity)    = viewModelScope.launch { repo.insertReminder(r) }
    fun toggleReminder(r: ReminderEntity) = viewModelScope.launch { repo.updateReminder(r.copy(isActive = !r.isActive)) }
    fun deleteReminder(r: ReminderEntity) = viewModelScope.launch { repo.deleteReminder(r) }

    // ── Lookup helpers ────────────────────────────────────────────────────────
    fun getCategoryById(id: Long) = categories.value.find { it.id == id }
    fun getAccountById(id: Long)  = accounts.value.find  { it.id == id }
}
