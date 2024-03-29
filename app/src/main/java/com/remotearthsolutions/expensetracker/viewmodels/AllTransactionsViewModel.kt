package com.remotearthsolutions.expensetracker.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.remotearthsolutions.expensetracker.R
import com.remotearthsolutions.expensetracker.databaseutils.daos.AccountDao
import com.remotearthsolutions.expensetracker.databaseutils.daos.CategoryDao
import com.remotearthsolutions.expensetracker.databaseutils.daos.CategoryExpenseDao
import com.remotearthsolutions.expensetracker.databaseutils.daos.ExpenseDao
import com.remotearthsolutions.expensetracker.databaseutils.models.CategoryModel
import com.remotearthsolutions.expensetracker.databaseutils.models.dtos.CategoryExpense
import com.remotearthsolutions.expensetracker.utils.DateTimeUtils
import com.remotearthsolutions.expensetracker.utils.DateTimeUtils.getDate
import com.remotearthsolutions.expensetracker.utils.Utils
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class AllTransactionsViewModel(
    private val categoryExpenseDao: CategoryExpenseDao,
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
    private var dateFormat: String,
    private val currencyString: String
) : ViewModel() {
    private val disposable = CompositeDisposable()
    private var dateRangeBtnId = 0
    var chartDataRequirementLiveData = MutableLiveData<ChartDataRequirement>()
    var listOfCategoryLiveData = MutableLiveData<List<CategoryModel>>()
    val expenseListLiveData = MutableLiveData<List<CategoryExpense>>()

    fun loadFilterExpense(startTime: Long, endTime: Long, btnId: Int) {
        var monthHeaderIndex = ArrayList<Int>()
        disposable.add(
            categoryExpenseDao.getExpenseWithinRange(startTime, endTime)
                .subscribeOn(Schedulers.io())
                .subscribe { listOfFilterExpense: List<CategoryExpense> ->

                    val dailyTotal = HashMap<String, Double>()
                    listOfFilterExpense.forEach {
                        val dateStr = getDate(it.datetime, dateFormat)
                        if (dailyTotal.containsKey(dateStr)) {
                            dailyTotal[dateStr] = dailyTotal[dateStr]!! + it.totalAmount
                        } else {
                            dailyTotal[dateStr] = it.totalAmount
                        }
                    }

                    val expenseList: MutableList<CategoryExpense> = ArrayList()
                    if (listOfFilterExpense.isNotEmpty()) {
                        var previousDate = listOfFilterExpense[0].datetime
                        var previousMonth = getDate(previousDate, DateTimeUtils.mmmm)
                        if (btnId != R.id.nextDateBtn && btnId != R.id.previousDateBtn) {
                            dateRangeBtnId = btnId
                        }
                        if (dateRangeBtnId == R.id.yearlyRangeBtn) {
                            val monthHeader =
                                CategoryExpense()
                            monthHeader.isHeader = true
                            monthHeader.categoryName = previousMonth
                            expenseList.add(monthHeader)
                            monthHeaderIndex.add(0)
                        }
                        if (dateRangeBtnId != R.id.dailyRangeBtn) {
                            val header =
                                CategoryExpense()
                            header.isHeader = true
                            header.isDateSection = true
                            val dateStr = getDate(previousDate, dateFormat)
                            header.categoryName =
                                "$dateStr ($currencyString ${
                                    Utils.formatDecimalValues(
                                        dailyTotal[dateStr]!!
                                    )
                                } )"
                            expenseList.add(header)
                            monthHeaderIndex.add(0)
                        }
                        var sum = 0.0
                        for (i in listOfFilterExpense.indices) {
                            val expense = listOfFilterExpense[i]
                            if (dateRangeBtnId == R.id.yearlyRangeBtn) {
                                val monthName = getDate(expense.datetime, DateTimeUtils.mmmm)

                                if (monthName != previousMonth) {
                                    val index = monthHeaderIndex[monthHeaderIndex.size - 1]
                                    val header = expenseList[index]
                                    header.totalAmount = sum
                                    sum = 0.0

                                    val monthHeader =
                                        CategoryExpense()
                                    monthHeader.isHeader = true
                                    monthHeader.categoryName = monthName
                                    expenseList.add(monthHeader)
                                    monthHeaderIndex.add(expenseList.size - 1)
                                    previousMonth = monthName
                                }
                                sum += expense.totalAmount
                            }
                            if (dateRangeBtnId != R.id.dailyRangeBtn) {
                                if (getDate(expense.datetime, dateFormat) !=
                                    getDate(previousDate, dateFormat)
                                ) {
                                    val dummy =
                                        CategoryExpense()
                                    dummy.isHeader = true
                                    dummy.isDateSection = true
                                    val dateStr = getDate(expense.datetime, dateFormat)
                                    dummy.categoryName =
                                        "$dateStr ($currencyString ${
                                            Utils.formatDecimalValues(
                                                dailyTotal[dateStr]!!
                                            )
                                        } )"
                                    previousDate = expense.datetime
                                    expenseList.add(dummy)
                                }
                            }
                            expenseList.add(expense)
                        }
                        if (sum > 0) {
                            val index = monthHeaderIndex[monthHeaderIndex.size - 1]
                            val header = expenseList[index]
                            header.totalAmount = sum
                        }
                    }
                    expenseListLiveData.postValue(expenseList)
                    this.chartDataRequirementLiveData.postValue(
                        ChartDataRequirement(
                            startTime,
                            endTime,
                            dateRangeBtnId,
                            listOfFilterExpense
                        )
                    )
                }
        )
    }

    fun getAllCategory() {
        disposable.add(
            categoryDao.allCategories
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result, error ->
                    if (error == null) {
                        listOfCategoryLiveData.value = result
                    }
                }
        )
    }

    fun updateDateFormat(updatedDateFormat: String) {
        this.dateFormat = updatedDateFormat
    }

    fun deleteSelectedExpense(
        expenseToDelete: ArrayList<CategoryExpense>,
        callback: () -> Unit,
        onError: () -> Unit
    ) {
        val tobeUpdatedAccounts = HashMap<Int, Double>()
        val tobeDeletedExpenseIdList = ArrayList<Int>()

        disposable.add(
            Completable.fromAction {
                expenseToDelete.forEach {
                    tobeDeletedExpenseIdList.add(it.expenseId)
                    if (tobeUpdatedAccounts.containsKey(it.accountId)) {
                        val prevVal = tobeUpdatedAccounts[it.accountId]!!
                        tobeUpdatedAccounts[it.accountId] = prevVal + it.totalAmount
                    } else {
                        tobeUpdatedAccounts[it.accountId] = it.totalAmount
                    }
                }
                expenseDao.delete(tobeDeletedExpenseIdList)
                for ((id, amount) in tobeUpdatedAccounts) {
                    val acc = accountDao.getAccountById(id).blockingGet()
                    acc.amount += amount
                    accountDao.updateAccount(acc)
                }
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ // onComplete
                    callback.invoke()
                }, {
                    if (BuildConfig.DEBUG) {
                        it.printStackTrace()
                    }
                    FirebaseCrashlytics.getInstance().recordException(it)
                    onError.invoke()
                })
        )
    }

    data class ChartDataRequirement(
        val startTime: Long,
        val endTime: Long,
        val selectedFilterBtnId: Int,
        val filteredList: List<CategoryExpense>
    )
}
