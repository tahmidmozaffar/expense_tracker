package com.remotearthsolutions.expensetracker.di.modules

import android.content.Context
import com.remotearthsolutions.expensetracker.R
import com.remotearthsolutions.expensetracker.contracts.AccountContract
import com.remotearthsolutions.expensetracker.contracts.AccountDialogContract
import com.remotearthsolutions.expensetracker.contracts.LoginContract
import com.remotearthsolutions.expensetracker.contracts.MainContract
import com.remotearthsolutions.expensetracker.databaseutils.AppDatabase
import com.remotearthsolutions.expensetracker.databaseutils.DatabaseClient
import com.remotearthsolutions.expensetracker.services.FacebookServiceImpl
import com.remotearthsolutions.expensetracker.services.FileProcessingServiceImp
import com.remotearthsolutions.expensetracker.services.FirebaseServiceImpl
import com.remotearthsolutions.expensetracker.services.GoogleServiceImpl
import com.remotearthsolutions.expensetracker.utils.Constants
import com.remotearthsolutions.expensetracker.utils.SharedPreferenceUtils
import com.remotearthsolutions.expensetracker.viewmodels.AccountDialogViewModel
import com.remotearthsolutions.expensetracker.viewmodels.AccountViewModel
import com.remotearthsolutions.expensetracker.viewmodels.AllTransactionsViewModel
import com.remotearthsolutions.expensetracker.viewmodels.LoginViewModel
import com.remotearthsolutions.expensetracker.viewmodels.mainview.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModels = module {

    viewModel { (context: Context, view: MainContract.View) ->
        MainViewModel(
            view,
            FirebaseServiceImpl(context),
            provideDb(context).accountDao(),
            provideDb(context).expenseDao(),
            provideDb(context).categoryDao(),
            provideDb(context).categoryExpenseDao(),
            FileProcessingServiceImp(context)
        )
    }

    viewModel { (view: AccountDialogContract.View) ->
        AccountDialogViewModel(view, provideDb(get()).accountDao())
    }

    viewModel { (context: Context, view: AccountContract.View) ->
        AccountViewModel(
            context,
            view,
            provideDb(context).accountDao(),
            provideDb(context).expenseDao()
        )
    }

    viewModel {
        AllTransactionsViewModel(
            provideDb(get()).categoryExpenseDao(),
            provideDb(get()).expenseDao(),
            provideDb(get()).categoryDao(),
            provideDb(get()).accountDao(),
            getUserPreferredTimeFormat(get())
        )
    }

    viewModel { (context: Context, view: LoginContract.View) ->
        LoginViewModel(
            context,
            view,
            GoogleServiceImpl(context),
            FacebookServiceImpl(context),
            FirebaseServiceImpl(context)
        )
    }
}

private fun provideDb(context: Context): AppDatabase {
    return DatabaseClient.getInstance(context).appDatabase
}

private fun getUserPreferredTimeFormat(context: Context): String {
    return SharedPreferenceUtils.getInstance(context)!!
        .getString(
            Constants.PREF_TIME_FORMAT,
            context.resources.getString(R.string.default_time_format)
        )
}