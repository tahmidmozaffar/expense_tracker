package com.remotearthsolutions.expensetracker.fragments.addexpensescreen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.work.Data
import com.remotearthsolutions.expensetracker.R
import com.remotearthsolutions.expensetracker.activities.main.MainActivity
import com.remotearthsolutions.expensetracker.contracts.BaseView
import com.remotearthsolutions.expensetracker.contracts.ExpenseFragmentContract
import com.remotearthsolutions.expensetracker.databaseutils.models.*
import com.remotearthsolutions.expensetracker.databaseutils.models.dtos.CategoryExpense
import com.remotearthsolutions.expensetracker.databinding.FragmentAddExpenseBinding
import com.remotearthsolutions.expensetracker.fragments.*
import com.remotearthsolutions.expensetracker.utils.AnalyticsManager
import com.remotearthsolutions.expensetracker.utils.Constants
import com.remotearthsolutions.expensetracker.utils.DateTimeUtils.currentTime
import com.remotearthsolutions.expensetracker.utils.DateTimeUtils.getCalendarFromDateString
import com.remotearthsolutions.expensetracker.utils.DateTimeUtils.getCurrentDate
import com.remotearthsolutions.expensetracker.utils.DateTimeUtils.getTimeInMillisFromDateStr
import com.remotearthsolutions.expensetracker.utils.NumpadManager
import com.remotearthsolutions.expensetracker.utils.SharedPreferenceUtils
import com.remotearthsolutions.expensetracker.utils.Utils.getCurrency
import com.remotearthsolutions.expensetracker.utils.workmanager.WorkManagerEnqueuer
import com.remotearthsolutions.expensetracker.utils.workmanager.WorkRequestType
import com.remotearthsolutions.expensetracker.viewmodels.ExpenseFragmentViewModel
import com.remotearthsolutions.expensetracker.viewmodels.NotesViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.parceler.Parcels
import java.util.*
import kotlin.math.abs

class ExpenseFragment : BaseFragment(), ExpenseFragmentContract.View {

    private lateinit var binding: FragmentAddExpenseBinding
    var purpose: Purpose? = null
    private val viewModel: ExpenseFragmentViewModel by viewModel { parametersOf(this) }
    private val notesViewModel: NotesViewModel by inject()
    private var categoryExpense: CategoryExpense? = null
    private var prevExpense: CategoryExpense? = null
    private lateinit var mContext: Context
    private lateinit var mResources: Resources
    private lateinit var format: String
    private var isRepeatEnabled = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mResources = context.resources
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        format = SharedPreferenceUtils.getInstance(requireActivity())!!.getString(
            Constants.PREF_TIME_FORMAT, Constants.KEY_DATE_MONTH_YEAR_DEFAULT
        )
        binding = FragmentAddExpenseBinding.inflate(layoutInflater, container, false)
        val repeatTypes = getResourceStringArray(R.array.repeatType)
        binding.repeatTypeSpnr.adapter =
            ArrayAdapter(requireContext(), R.layout.repeat_type_spinner_item, repeatTypes)

        val currencySymbol = getCurrency(mContext)
        binding.inputdigit.hint = "$currencySymbol 0"
        val numPadFragment =
            childFragmentManager.findFragmentById(R.id.numpadContainer) as NumpadFragment?
        val numpadManager = NumpadManager()
        numpadManager.attachDisplay(binding.inputdigit)
        numpadManager.attachDeleteButton(binding.deleteBtn)
        numPadFragment!!.setListener(numpadManager)

        viewModel.init()
        notesViewModel.notesLiveData.observe(viewLifecycleOwner) {
            notesViewModel.notes = it
        }
        notesViewModel.getAllNotes()

        val args = arguments
        if (args != null) {
            categoryExpense =
                Parcels.unwrap<CategoryExpense>(args.getParcelable(Constants.CATEGORYEXPENSE_PARCEL))
            if (categoryExpense != null) {
                prevExpense = categoryExpense!!.copy()
                Helpers.updateUI(binding, categoryExpense, format)
            } else {
                viewModel.setDefaultCategory()
            }

            if (categoryExpense != null && categoryExpense!!.accountIcon != null) {
                Helpers.updateAccountBtn(binding, categoryExpense)
            } else {
                val accountId = SharedPreferenceUtils.getInstance(mContext)!!.getInt(
                    Constants.KEY_SELECTED_ACCOUNT_ID, 1
                )
                viewModel.setDefaultSourceAccount(accountId)
            }
        }

        purpose?.let {
            if (it == Purpose.UPDATE) {
                binding.enableRepeatBtn.visibility = View.GONE
            }
        }

        registerBackButton()
        return binding.root
    }

    @SuppressLint("InflateParams")
    override fun defineClickListener() {
        binding.fromAccountBtn.setOnClickListener {
            val fm = childFragmentManager
            val accountDialogFragment: AccountDialogFragment =
                AccountDialogFragment.newInstance(getResourceString(R.string.select_account))
            accountDialogFragment.setCallback(object : AccountDialogFragment.Callback {
                override fun onSelectAccount(accountIncome: AccountModel) {
                    categoryExpense!!.setAccount(accountIncome)
                    binding.fromAccountBtn.update(
                        accountIncome.name!!, accountIncome.icon!!
                    )
                    accountDialogFragment.dismiss()
                    SharedPreferenceUtils.getInstance(mContext)!!.putInt(
                        Constants.KEY_SELECTED_ACCOUNT_ID, accountIncome.id
                    )
                }
            })
            accountDialogFragment.show(fm, AccountDialogFragment::class.java.name)
        }
        binding.toCategoryBtn.setOnClickListener {
            val fm = childFragmentManager
            val categoryDialogFragment: CategoryDialogFragment =
                CategoryDialogFragment.newInstance(getResourceString(R.string.select_category))
            categoryDialogFragment.setCategory(categoryExpense?.categoryId!!)
            categoryDialogFragment.setCallback(object : CategoryDialogFragment.Callback {
                override fun onSelectCategory(category: CategoryModel?) {
                    binding.toCategoryBtn.update(
                        category?.name!!, category.icon!!
                    )
                    categoryDialogFragment.dismiss()
                    categoryExpense!!.setCategory(category)
                }
            })
            categoryDialogFragment.show(fm, CategoryDialogFragment::class.java.name)
        }
        binding.dateTv.text = getCurrentDate(format)
        binding.singleEntryView.setOnClickListener {
            val datePickerDialogFragment: DatePickerDialogFragment =
                DatePickerDialogFragment.newInstance("")
            val cal = getCalendarFromDateString(format, binding.dateTv.text.toString())
            datePickerDialogFragment.setInitialDate(
                cal[Calendar.DAY_OF_MONTH], cal[Calendar.MONTH], cal[Calendar.YEAR]
            )
            datePickerDialogFragment.setCallback(object : DatePickerDialogFragment.Callback {
                override fun onSelectDate(date: String?) {
                    binding.dateTv.text = date
                    datePickerDialogFragment.dismiss()
                }
            })
            datePickerDialogFragment.show(
                childFragmentManager, DatePickerDialogFragment::class.java.name
            )
        }
        binding.okBtn.setOnClickListener {
            var expenseStr = binding.inputdigit.text.toString()
            if (expenseStr == getResourceString(R.string.point)) {
                expenseStr = ""
            }
            val amount = try {
                if (expenseStr.isNotEmpty()) expenseStr.toDouble() else 0.0
            } catch (e: NumberFormatException) {
                Toast.makeText(
                    mContext,
                    getResourceString(R.string.make_sure_enter_valid_number),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (abs(amount) == 0.0) {
                showToast(getResourceString(R.string.please_enter_an_amount))
                return@setOnClickListener
            }

            val expenseModel = ExpenseModel()
            expenseModel.id = categoryExpense?.expenseId!!
            expenseModel.amount = amount
            expenseModel.datetime = getTimeInMillisFromDateStr(
                binding.dateTv.text.toString() + " " + currentTime,
                format + " " + Constants.KEY_HOUR_MIN_SEC
            )
            expenseModel.categoryId = categoryExpense!!.categoryId
            expenseModel.source = categoryExpense!!.accountId
            expenseModel.note =
                if (categoryExpense!!.note == null || categoryExpense!!.note == "null") "" else categoryExpense!!.note

            if (binding.repeatEntryView.visibility != View.VISIBLE) {
                viewModel.addExpense(expenseModel)
            } else {
                val period = binding.numberTv.text.toString().toInt()
                val repeatType = binding.repeatTypeSpnr.selectedItemPosition
                val repeatCount = binding.timesTv.text.toString().toInt()
                val nextDate = getTimeInMillisFromDateStr(
                    binding.dateTv.text.toString() + " " + currentTime,
                    format + " " + Constants.KEY_HOUR_MIN_SEC
                ) + (2 * 60 * 1000)
                viewModel.scheduleExpense(expenseModel, period, repeatType, repeatCount, nextDate)
                with(AnalyticsManager) { logEvent(EXPENSE_TYPE_SCHEDULED) }
            }
        }

        binding.expenseNoteEdtxt.setOnClickListener {
            DialogHelper.showExpenseNoteInput(
                mContext,
                layoutInflater,
                binding,
                categoryExpense,
                notesViewModel
            )
        }
        binding.expenseDeleteBtn.setOnClickListener {
            if (categoryExpense!!.expenseId > 0) {
                showAlert(getResourceString(R.string.attention),
                    getResourceString(R.string.are_you_sure_you_want_to_delete_this_expense_entry),
                    getResourceString(R.string.yes),
                    getResourceString(R.string.not_now),
                    null,
                    object : BaseView.Callback {
                        override fun onOkBtnPressed() {
                            viewModel.deleteExpense(categoryExpense)
                        }

                        override fun onCancelBtnPressed() {}
                    })
            } else {
                (mContext as Activity?)!!.onBackPressed()
            }
        }
        binding.enableRepeatBtn.setOnClickListener {
            isRepeatEnabled = !isRepeatEnabled
            if (isRepeatEnabled) {
                binding.enableRepeatBtn.setImageResource(R.drawable.ic_repeat)
                binding.repeatEntryView.visibility = View.VISIBLE
                (mContext as MainActivity?)?.supportActionBar?.title =
                    getResourceString(R.string.schedule_expense)
            } else {
                binding.enableRepeatBtn.setImageResource(R.drawable.ic_single)
                binding.repeatEntryView.visibility = View.GONE
                (mContext as MainActivity?)?.supportActionBar?.title =
                    getResourceString(R.string.add_expense)
            }
        }
        binding.numberTv.setOnClickListener {
            DialogHelper.getInputFor(binding.numberTv, mContext, layoutInflater)
        }
        binding.timesTv.setOnClickListener {
            DialogHelper.getInputFor(binding.timesTv, mContext, layoutInflater)
        }
    }

    override fun onExpenseAdded(amount: Double) {
        binding.inputdigit.setText("")
        binding.expenseNoteEdtxt.setText("")
        Toast.makeText(activity, getResourceString(R.string.successfully_added), Toast.LENGTH_SHORT)
            .show()
        var mutableAmount = amount

        purpose?.let {
            if (it == Purpose.UPDATE) {
                if (categoryExpense?.accountId == prevExpense!!.accountId) {
                    mutableAmount += (prevExpense!!.totalAmount * -1)
                } else {
                    viewModel.updateAccountAmount(
                        prevExpense!!.accountId, prevExpense!!.totalAmount * -1
                    )
                }
            }
        }
        viewModel.updateAccountAmount(categoryExpense?.accountId!!, mutableAmount)
        val mainActivity = mContext as MainActivity?
        mainActivity!!.updateSummary()
        mainActivity.refreshChart()
        //TODO: Temporary disabled. Logic has a problem. revisit the implementation
        //Helpers.requestToReviewApp(mainActivity, viewModel)
        MainActivity.addedExpenseCount++
        with(AnalyticsManager) {
            logEvent(
                EXPENSE_TYPE_DEFAULT, mapOf(
                    "expense_category" to categoryExpense?.categoryName!!,
                    "account" to categoryExpense?.accountName!!,
                    "icon" to categoryExpense?.categoryIcon!!
                )
            )
        }

        categoryExpense?.note?.let {
            saveNote(it)
        }
        categoryExpense?.note = ""
    }

    override fun onExpenseDeleted(categoryExpense: CategoryExpense?) {
        Toast.makeText(
            activity,
            getResourceString(R.string.successfully_deleted_expense_entry),
            Toast.LENGTH_SHORT
        ).show()
        (mContext as Activity?)!!.onBackPressed()
        viewModel.updateAccountAmount(
            this.categoryExpense?.accountId!!, categoryExpense?.totalAmount!! * -1
        )
        with(AnalyticsManager) {
            logEvent(EXPENSE_DELETED)
        }
        val mainActivity = mContext as MainActivity?
        mainActivity!!.updateSummary()
    }

    override fun setSourceAccount(account: AccountModel?) {
        if (categoryExpense == null) {
            categoryExpense = CategoryExpense()
        }
        categoryExpense!!.setAccount(account!!)
        binding.fromAccountBtn.update(
            account.name!!, account.icon!!
        )
        SharedPreferenceUtils.getInstance(mContext)!!.putInt(
            Constants.KEY_SELECTED_ACCOUNT_ID, account.id
        )
    }

    override fun showDefaultCategory(categoryModel: CategoryModel?) {
        if (categoryExpense == null) {
            categoryExpense = CategoryExpense()
        }
        categoryExpense!!.setCategory(categoryModel!!)
        binding.toCategoryBtn.update(
            categoryModel.name!!, categoryModel.icon!!
        )
    }

    override fun onScheduleExpense(scheduledExpenseModel: ScheduledExpenseModel) {
        binding.inputdigit.setText("")
        binding.expenseNoteEdtxt.setText("")
        showToast(getResourceString(R.string.expense_scheduled))

        val delay = scheduledExpenseModel.nextoccurrencedate - Calendar.getInstance().timeInMillis
        val data = Data.Builder()
        data.putLong(ExpenseScheduler.SCHEDULED_EXPENSE_ID, scheduledExpenseModel.id)
        val workRequestId = WorkManagerEnqueuer().enqueue<AddScheduledExpenseWorker>(
            requireContext(), WorkRequestType.ONETIME, delay, data.build()
        )
        viewModel.saveWorkerId(WorkerIdModel(scheduledExpenseModel.id, workRequestId))

        categoryExpense?.note?.let {
            saveNote(it)
        }
        categoryExpense?.note = ""
    }

    private fun saveNote(note: String) {
        val str = note.trim()
        var isUnique = true

        for (nt in notesViewModel.notes) {
            if (str == nt) {
                isUnique = false
                break
            }
        }

        if (isUnique) {
            notesViewModel.addNote(NoteModel(str))
            notesViewModel.notes.add(str)
        }
    }
}
