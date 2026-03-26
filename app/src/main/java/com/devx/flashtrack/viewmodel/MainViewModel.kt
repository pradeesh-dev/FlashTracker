package com.devx.flashtrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devx.flashtrack.data.local.entity.*
import com.devx.flashtrack.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo: AppRepository
) : ViewModel() {

    // ─── Selected month for analysis ─────────────────────────────────────────
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    // ─── Accounts ────────────────────────────────────────────────────────────
    val accounts = repo.getAllAccounts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalBalance = repo.getTotalBalance().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalCredit = repo.getTotalAvailableCredit().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // ─── Recent transactions (home) ───────────────────────────────────────────
    val recentTransactions = repo.getRecentTransactions(15).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTransactions = repo.getAllTransactions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─── Categories ───────────────────────────────────────────────────────────
    val categories = repo.getAllCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─── Debts ────────────────────────────────────────────────────────────────
    val debts = repo.getAllDebts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalLent = repo.getTotalLent().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalBorrowed = repo.getTotalBorrowed().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // ─── Reminders ────────────────────────────────────────────────────────────
    val reminders = repo.getAllReminders().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─── Monthly stats ────────────────────────────────────────────────────────
    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyExpense = _selectedMonth.flatMapLatest { ym ->
        val (s, e) = monthRange(ym)
        repo.getTotalExpenseInRange(s, e)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyIncome = _selectedMonth.flatMapLatest { ym ->
        val (s, e) = monthRange(ym)
        repo.getTotalIncomeInRange(s, e)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val categorySpending = _selectedMonth.flatMapLatest { ym ->
        val (s, e) = monthRange(ym)
        repo.getCategoryWiseSpending(s, e)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyTransactions = _selectedMonth.flatMapLatest { ym ->
        val (s, e) = monthRange(ym)
        repo.getTransactionsByDateRange(s, e)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current month home stats always using current month
    val currentMonthExpense = flow {
        val (s, e) = repo.currentMonthRange()
        emitAll(repo.getTotalExpenseInRange(s, e))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentMonthIncome = flow {
        val (s, e) = repo.currentMonthRange()
        emitAll(repo.getTotalIncomeInRange(s, e))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // ─── Balance visibility ───────────────────────────────────────────────────
    private val _showBalance = MutableStateFlow(true)
    val showBalance: StateFlow<Boolean> = _showBalance.asStateFlow()
    fun toggleBalance() { _showBalance.value = !_showBalance.value }

    // ─── Actions ──────────────────────────────────────────────────────────────
    fun addTransaction(t: TransactionEntity) = viewModelScope.launch { repo.insertTransaction(t) }
    fun deleteTransaction(t: TransactionEntity) = viewModelScope.launch { repo.deleteTransaction(t) }

    fun addAccount(a: AccountEntity) = viewModelScope.launch { repo.insertAccount(a) }
    fun updateAccount(a: AccountEntity) = viewModelScope.launch { repo.updateAccount(a) }
    fun deleteAccount(a: AccountEntity) = viewModelScope.launch { repo.deleteAccount(a) }

    fun addCategory(c: CategoryEntity) = viewModelScope.launch { repo.insertCategory(c) }
    fun updateCategory(c: CategoryEntity) = viewModelScope.launch { repo.updateCategory(c) }
    fun deleteCategory(c: CategoryEntity) = viewModelScope.launch { repo.deleteCategory(c) }

    fun addDebt(d: DebtEntity) = viewModelScope.launch { repo.insertDebt(d) }
    fun settleDebt(d: DebtEntity, amount: Double) = viewModelScope.launch { repo.settleDebt(d, amount) }
    fun deleteDebt(d: DebtEntity) = viewModelScope.launch { repo.deleteDebt(d) }

    fun addReminder(r: ReminderEntity) = viewModelScope.launch { repo.insertReminder(r) }
    fun toggleReminder(r: ReminderEntity) = viewModelScope.launch { repo.updateReminder(r.copy(isActive = !r.isActive)) }
    fun deleteReminder(r: ReminderEntity) = viewModelScope.launch { repo.deleteReminder(r) }

    fun selectMonth(ym: YearMonth) { _selectedMonth.value = ym }

    // ─── Helpers ─────────────────────────────────────────────────────────────
    private fun monthRange(ym: YearMonth): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val start = ym.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end   = ym.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
        return start to end
    }

    fun getCategoryById(id: Long) = categories.value.find { it.id == id }
    fun getAccountById(id: Long) = accounts.value.find { it.id == id }

    fun formatAmount(amount: Double): String = "₹${"%,.0f".format(amount)}"
    fun formatAmountFull(amount: Double): String = "₹${"%,.2f".format(amount)}"
}
