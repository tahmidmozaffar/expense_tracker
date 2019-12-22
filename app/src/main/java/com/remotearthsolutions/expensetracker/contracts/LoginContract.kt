package com.remotearthsolutions.expensetracker.contracts

import com.google.firebase.auth.FirebaseUser

interface LoginContract {
    interface View : BaseView {
        fun initializeView()
        fun onLoginSuccess(user: FirebaseUser?)
        fun onLoginFailure()
        fun loadUserEmails()
    }
}