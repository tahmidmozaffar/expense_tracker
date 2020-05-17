package com.remotearthsolutions.expensetracker.activities.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.remotearthsolutions.expensetracker.BuildConfig
import com.remotearthsolutions.expensetracker.R
import com.remotearthsolutions.expensetracker.activities.ApplicationObject
import com.remotearthsolutions.expensetracker.activities.BaseActivity
import com.remotearthsolutions.expensetracker.activities.CurrencySelectionActivity
import com.remotearthsolutions.expensetracker.activities.LoginActivity
import com.remotearthsolutions.expensetracker.activities.helpers.FragmentLoader
import com.remotearthsolutions.expensetracker.contracts.MainContract
import com.remotearthsolutions.expensetracker.databaseutils.DatabaseClient
import com.remotearthsolutions.expensetracker.databaseutils.models.dtos.CategoryExpense
import com.remotearthsolutions.expensetracker.fragments.HomeFragment
import com.remotearthsolutions.expensetracker.fragments.OverViewFragment
import com.remotearthsolutions.expensetracker.fragments.ViewShadeFragment
import com.remotearthsolutions.expensetracker.fragments.addexpensescreen.ExpenseFragment
import com.remotearthsolutions.expensetracker.fragments.addexpensescreen.Purpose
import com.remotearthsolutions.expensetracker.fragments.main.MainFragment
import com.remotearthsolutions.expensetracker.services.FileProcessingServiceImp
import com.remotearthsolutions.expensetracker.services.FirebaseServiceImpl
import com.remotearthsolutions.expensetracker.services.PurchaseListener
import com.remotearthsolutions.expensetracker.utils.*
import com.remotearthsolutions.expensetracker.viewmodels.mainview.MainViewModel
import com.remotearthsolutions.expensetracker.viewmodels.viewmodel_factory.BaseViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import org.parceler.Parcels

class MainActivity : BaseActivity(), MainContract.View {

    private var toggle: ActionBarDrawerToggle? = null
    private var backPressedTime: Long = 0
    private var purchaseListener: PurchaseListener? = null
    private var preferencesChangeListener: PreferencesChangeListener? = null
    private lateinit var inAppPurchaseCallback: InAppPurchaseCallback

    lateinit var viewModel: MainViewModel
    lateinit var checkoutUtils: CheckoutUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkoutUtils = CheckoutUtils.getInstance(this)!!
        checkoutUtils.start()
        inAppPurchaseCallback = InAppPurchaseCallback(this)
        purchaseListener = PurchaseListener(this, inAppPurchaseCallback)
        preferencesChangeListener = PreferencesChangeListener(this)

        val db = DatabaseClient.getInstance(this).appDatabase

        viewModel =
            ViewModelProviders.of(this, BaseViewModelFactory {
                MainViewModel(
                    this,
                    FirebaseServiceImpl(this),
                    db.accountDao(),
                    db.expenseDao(),
                    db.categoryDao(),
                    db.categoryExpenseDao(),
                    FileProcessingServiceImp()
                )
            }).get(MainViewModel::class.java)

        val userStr = SharedPreferenceUtils.getInstance(this)?.getString(Constants.KEY_USER, "")
        viewModel.checkAuthectication(userStr!!)

        Handler().postDelayed({
            InAppUpdateUtils().requestUpdateApp(this@MainActivity)
        }, 2000)

        if (BuildConfig.DEBUG) {
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(MainActivity::class.java.name, "getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }
                    // Get new Instance ID token
                    val token = task.result?.token
                    Log.d(MainActivity::class.java.name, "Firebase Token: $token")
                })
        }
    }

    override fun onStart() {
        super.onStart()
        checkoutUtils.start()
        checkoutUtils.createPurchaseFlow(purchaseListener)

        if (preferencesChangeListener == null) {
            preferencesChangeListener = PreferencesChangeListener(this)
        }
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(preferencesChangeListener)
    }

    override fun onStop() {
        super.onStop()
        checkoutUtils.stop()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(preferencesChangeListener)
    }

    override fun onResume() {
        super.onResume()
        (application as ApplicationObject).activityResumed()
    }

    override fun onPause() {
        super.onPause()
        (application as ApplicationObject).activityPaused()
    }

    override fun initializeView() {
        setupActionBar()
        val navigationItemSelectionListener = NavigationItemSelectionListener(
            this,
            (application as ApplicationObject).adProductId
        )
        nav_view.setNavigationItemSelectedListener(navigationItemSelectionListener)
        val homeNavItem = nav_view.menu.getItem(0)
        navigationItemSelectionListener.onNavigationItemSelected(homeNavItem)
        homeNavItem.isChecked = true
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar)
        toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        toggle?.syncState()
        drawer_layout.addDrawerListener(toggle!!)
    }

    override fun goBackToLoginScreen() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onLogoutSuccess() {
        SharedPreferenceUtils.getInstance(this)?.putString(Constants.KEY_USER, "")
        goBackToLoginScreen()
    }

    override fun startLoadingApp() {
        viewModel.init(this)
        checkoutUtils.start()
        checkoutUtils.load(inAppPurchaseCallback, (application as ApplicationObject).adProductId)
    }

    override fun showTotalExpense(amount: String?) {
        val str = "${getString(R.string.expense)}: $amount"
        totalExpenseAmountTv.text = str
    }

    override fun showTotalBalance(amount: String?) {
        val str = "${getString(R.string.balance)}: $amount"
        totalAccountAmountTv.text = str
    }

    override fun stayOnCurrencyScreen() {
        val intent = Intent(this, CurrencySelectionActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun setBalanceTextColor(colorId: Int) {
        totalAccountAmountTv.setTextColor(ContextCompat.getColor(this, colorId))
    }

    fun onBackButtonPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            val t = System.currentTimeMillis()
            if (t - backPressedTime > 2000) {
                backPressedTime = t
                Toast.makeText(
                    this,
                    getString(R.string.press_once_again_to_close_app),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                CheckoutUtils.clearInstance()
                finish()
            }
        }
    }

    fun showBackButton() {
        toggle?.isDrawerIndicatorEnabled = false
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        toggle?.syncState()
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun hideBackButton() {
        toggle?.isDrawerIndicatorEnabled = true
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        toggle?.syncState()
        toolbar.setNavigationOnClickListener {
            mDrawerLayout.openDrawer(GravityCompat.START)
        }

        //hide background blur
        val fragment = supportFragmentManager.findFragmentByTag(ViewShadeFragment::class.java.name)
        if (fragment != null) {
            FragmentLoader.remove(this, fragment, null, 1)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle?.syncState()
    }

    fun openAddExpenseScreen(
        categoryExpense: CategoryExpense?,
        title: String = getString(R.string.add_expense),
        purpose: Purpose = Purpose.ADD
    ) {
        //blur back screen
        FragmentLoader.load(this, ViewShadeFragment(), null, ViewShadeFragment::class.java.name, 1)

        supportActionBar!!.title = title
        val expenseFragment =
            ExpenseFragment()
        expenseFragment.purpose = purpose
        val wrappedCategoryExpense = Parcels.wrap(categoryExpense)
        val bundle = Bundle()
        bundle.putParcelable(Constants.CATEGORYEXPENSE_PARCEL, wrappedCategoryExpense)
        expenseFragment.arguments = bundle
        FragmentLoader.load(this, expenseFragment, title, ExpenseFragment::class.java.name)
        showBackButton()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        CheckoutUtils.getInstance(this)?.checkout?.onActivityResult(requestCode, resultCode, data)
    }

    val mToolbar: Toolbar
        get() = toolbar

    val mDrawerLayout: DrawerLayout
        get() = drawer_layout

    fun updateSummary(startTime: Long, endTime: Long) {
        viewModel.updateSummary(startTime, endTime)
    }

    fun updateSummary() {
        viewModel.updateSummary()
    }

    fun refreshChart() {
        val fragment =
            supportFragmentManager.findFragmentByTag(MainFragment::class.java.name) as MainFragment?
        fragment?.refreshChart()
    }

    fun onUpdateCategory() {
        updateSummary()
        val mainFragment =
            supportFragmentManager.findFragmentByTag(MainFragment::class.java.name) as MainFragment?
        val homeFragment =
            mainFragment?.childFragmentManager?.findViewPagerFragmentByTag<HomeFragment>(
                R.id.viewpager,
                0
            )
        homeFragment?.init()
        val overViewFragment =
            mainFragment?.childFragmentManager?.findViewPagerFragmentByTag<OverViewFragment>(
                R.id.viewpager,
                2
            )
        overViewFragment?.onUpdateCategory()
        mainFragment?.refreshChart()
    }

    companion object {
        @JvmField
        var addedExpenseCount = 1
    }
}
