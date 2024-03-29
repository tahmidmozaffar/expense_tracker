package com.remotearthsolutions.expensetracker.fragments.main

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.navigation.NavigationBarView
import com.remotearthsolutions.expensetracker.R
import com.remotearthsolutions.expensetracker.activities.main.MainActivity
import com.remotearthsolutions.expensetracker.databinding.FragmentMainBinding
import com.remotearthsolutions.expensetracker.fragments.AllExpenseFragment
import com.remotearthsolutions.expensetracker.fragments.BaseFragment
import com.remotearthsolutions.expensetracker.fragments.HomeFragment
import com.remotearthsolutions.expensetracker.fragments.OverViewFragment
import com.remotearthsolutions.expensetracker.fragments.accounts.AccountsFragment
import com.remotearthsolutions.expensetracker.utils.Constants
import com.remotearthsolutions.expensetracker.utils.SharedPreferenceUtils
import com.remotearthsolutions.expensetracker.utils.Utils
import com.remotearthsolutions.expensetracker.utils.findViewPagerFragmentByTag
import com.remotearthsolutions.expensetracker.viewmodels.MainFragmentViewModel
import com.remotearthsolutions.expensetracker.views.PeriodButton
import org.koin.android.ext.android.inject

class MainFragment : BaseFragment(),
    DateFilterButtonClickListener.Callback {
    private var binding: FragmentMainBinding? = null
    private var pagerAdapter: MainFragmentPagerAdapter? = null
    private var actionBar: ActionBar? = null
    private var tabTitles: Array<String>? = null
    private var selectedPeriodBtn: PeriodButton? = null
    private lateinit var mContext: Context
    private lateinit var mResources: Resources
    val viewModel: MainFragmentViewModel by inject()
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mResources = mContext.resources
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_main,
            container,
            false
        )
        return binding?.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        registerBackButton(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                (activity as MainActivity).onBackButtonPressed()
            }
        })

        tabTitles = arrayOf(
            getString(R.string.title_home),
            getString(R.string.title_transaction),
            getString(R.string.title_overview),
            getString(R.string.title_accounts)
        )

        pagerAdapter = MainFragmentPagerAdapter(childFragmentManager)

        binding!!.viewpager.offscreenPageLimit = 4
        binding!!.viewpager.adapter = pagerAdapter
        binding!!.viewpager.addOnPageChangeListener(viewPagerPageChangeListener)
        binding!!.navigation.setOnItemSelectedListener(mOnNavigationItemSelectedListener)
        val dateFilterButtonClickListener = DateFilterButtonClickListener(this)
        binding!!.nextDateBtn.setOnClickListener(dateFilterButtonClickListener)
        binding!!.previousDateBtn.setOnClickListener(dateFilterButtonClickListener)
        binding!!.dailyRangeBtn.setOnClickListener(dateFilterButtonClickListener)
        binding!!.weeklyRangeBtn.setOnClickListener(dateFilterButtonClickListener)
        binding!!.monthlyRangeBtn.setOnClickListener(dateFilterButtonClickListener)
        binding!!.yearlyRangeBtn.setOnClickListener(dateFilterButtonClickListener)

        val text = getString(R.string.current_expense)
        binding!!.currentExpenseAmountTv.text = "$text: ${Utils.formatDecimalValues(0.0)}"
        viewModel.currentExpense.observe(viewLifecycleOwner) {
            binding!!.currentExpenseAmountTv.text = "$text: ${Utils.formatDecimalValues(it)}"
        }


        Handler(Looper.getMainLooper()).postDelayed({
            val period = SharedPreferenceUtils.getInstance(mContext)!!.getString(
                Constants.PREF_PERIOD,
                mResources.getString(R.string.daily)
            )
            when (listOf(*mResources.getStringArray(R.array.TimePeriod)).indexOf(period)) {
                0 -> {
                    selectedPeriodBtn = binding!!.dailyRangeBtn
                }
                1 -> {
                    selectedPeriodBtn = binding!!.weeklyRangeBtn
                }
                2 -> {
                    selectedPeriodBtn = binding!!.monthlyRangeBtn
                }
                3 -> {
                    selectedPeriodBtn = binding!!.yearlyRangeBtn
                }
                else -> {
                    selectedPeriodBtn = binding!!.dailyRangeBtn
                    SharedPreferenceUtils.getInstance(mContext)!!.putString(
                        Constants.PREF_PERIOD,
                        resources.getString(R.string.daily)
                    )
                }
            }
            selectedPeriodBtn?.performClick()
        }, 500)
    }

    private val mOnNavigationItemSelectedListener =
        NavigationBarView.OnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    binding!!.viewpager.setCurrentItem(0, true)
                    actionBar?.title = tabTitles?.get(0)
                }
                R.id.navigation_transaction -> {
                    binding!!.viewpager.setCurrentItem(1, true)
                    actionBar?.title = tabTitles?.get(1)
                }
                R.id.navigation_overview -> {
                    binding!!.viewpager.setCurrentItem(2, true)
                    actionBar?.title = tabTitles?.get(2)
                }
                R.id.navigation_accounts -> {
                    binding!!.viewpager.setCurrentItem(3, true)
                    actionBar?.title = tabTitles?.get(3)
                    val accountsFragment =
                        childFragmentManager.findViewPagerFragmentByTag<AccountsFragment>(
                            R.id.viewpager,
                            3
                        )
                    accountsFragment?.onUpdateAccount(null)
                }
            }
            true
        }

    private val viewPagerPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            actionBar?.title = tabTitles?.get(position)
            val activity = requireActivity() as MainActivity
            activity.selectedTabPosition = position
            activity.updateTitle()
            when (position) {
                0 -> {
                    binding!!.dateRangeContainer.visibility = View.VISIBLE
                    val homeFragment =
                        childFragmentManager.findViewPagerFragmentByTag<HomeFragment>(
                            R.id.viewpager,
                            0
                        )
                    homeFragment?.refreshPage()
                    binding!!.navigation.selectedItemId = R.id.navigation_home
                }
                1 -> {
                    binding!!.dateRangeContainer.visibility = View.VISIBLE
                    binding!!.navigation.selectedItemId = R.id.navigation_transaction
                }
                2 -> {
                    binding!!.dateRangeContainer.visibility = View.VISIBLE
                    binding!!.navigation.selectedItemId = R.id.navigation_overview
                }
                3 -> {
                    binding!!.dateRangeContainer.visibility = View.GONE
                    binding!!.navigation.selectedItemId = R.id.navigation_accounts
                }
            }
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }

    override fun onDateChanged(
        btnId: Int,
        date: String?,
        startTime: Long,
        endTime: Long
    ) {
        if (btnId != R.id.nextDateBtn && btnId != R.id.previousDateBtn) {
            Helper.resetDateRangeBtns(binding)
        }
        when (btnId) {
            R.id.dailyRangeBtn -> {
                selectedPeriodBtn = binding!!.dailyRangeBtn
            }
            R.id.weeklyRangeBtn -> {
                selectedPeriodBtn = binding!!.weeklyRangeBtn
            }
            R.id.monthlyRangeBtn -> {
                selectedPeriodBtn = binding!!.monthlyRangeBtn
            }
            R.id.yearlyRangeBtn -> {
                selectedPeriodBtn = binding!!.yearlyRangeBtn
            }
        }
        selectedPeriodBtn?.setIsSelected(true)

        binding!!.dateTv.text = date
        val homeFragment =
            childFragmentManager.findViewPagerFragmentByTag<HomeFragment>(R.id.viewpager, 0)
        val allExpenseFragment =
            childFragmentManager.findViewPagerFragmentByTag<AllExpenseFragment>(R.id.viewpager, 1)
        if (homeFragment != null && allExpenseFragment != null) {
            homeFragment.updateChartView(startTime, endTime)
            allExpenseFragment.updateFilterListWithDate(
                startTime,
                endTime,
                btnId
            )
        }
        val mainActivity = mContext as MainActivity

        mainActivity.viewModel.startTime = startTime
        mainActivity.viewModel.endTime = endTime
        mainActivity.updateSummary()

    }

    fun refreshChart() {
        if (selectedPeriodBtn != null) {
            selectedPeriodBtn?.performClick()
        }
    }

    class MainFragmentPagerAdapter(fm: FragmentManager?) :
        FragmentPagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> {
                    val homeFragment = HomeFragment()
                    homeFragment
                }
                1 -> {
                    val allExpenseFragment = AllExpenseFragment()
                    allExpenseFragment
                }
                2 -> {
                    val overViewFragment = OverViewFragment()
                    overViewFragment
                }
                3 -> {
                    val accountsFragment =
                        AccountsFragment()
                    accountsFragment
                }
                else -> {
                    Fragment()
                }
            }
        }

        override fun getCount(): Int {
            return 4
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return null
        }
    }
}
