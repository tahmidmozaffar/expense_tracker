package com.remotearthsolutions.expensetracker.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.remotearthsolutions.expensetracker.R
import com.remotearthsolutions.expensetracker.adapters.IconListAdapter
import com.remotearthsolutions.expensetracker.databaseutils.DatabaseClient
import com.remotearthsolutions.expensetracker.databaseutils.models.AccountModel
import com.remotearthsolutions.expensetracker.utils.CategoryIcons.allIcons
import com.remotearthsolutions.expensetracker.utils.Utils.getDeviceScreenSize
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_add_update_category_account.view.*

class AddUpdateAccountDialogFragment : DialogFragment() {
    private lateinit var mView: View
    private var accountModel: AccountModel? = null
    private var selectedIcon: String? = null
    private lateinit var iconListAdapter: IconListAdapter
    private lateinit var mContext: Context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    fun setAccountModel(accountModel: AccountModel?) {
        this.accountModel = accountModel
        if (accountModel != null) {
            selectedIcon = accountModel.icon
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_update_category_account, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        if (accountModel != null) {
            mView.header.text = getString(R.string.update_account)
            mView.okBtn.text = getString(R.string.update)
            mView.nameEdtxt.setText(accountModel!!.name)
            mView.nameEdtxt.setSelection(mView.nameEdtxt.text.length)
        } else {
            mView.header.text = getString(R.string.add_account)
            mView.okBtn.text = getString(R.string.add)
        }
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            getDeviceScreenSize(mContext)!!.height / 2
        )
        mView.accountrecyclearView.layoutParams = params
        mView.accountrecyclearView.setHasFixedSize(true)
        val gridLayoutManager = GridLayoutManager(mContext, 4)
        mView.accountrecyclearView.layoutManager = gridLayoutManager
        val alliconList = allIcons
        iconListAdapter = IconListAdapter(alliconList, gridLayoutManager)
        iconListAdapter.setSelectedIcon(if (selectedIcon != null) selectedIcon else "")
        iconListAdapter.setOnItemClickListener(object : IconListAdapter.OnItemClickListener {
            override fun onItemClick(icon: String?) {
                selectedIcon = icon
                iconListAdapter.setSelectedIcon(if (selectedIcon != null) selectedIcon else "")
                iconListAdapter.notifyDataSetChanged()
            }
        })
        mView.accountrecyclearView.adapter = iconListAdapter
        mView.okBtn.setOnClickListener { saveAccount() }
    }

    private fun saveAccount() {
        val accountName = mView.nameEdtxt.text.toString().trim { it <= ' ' }
        if (accountName.isEmpty()) {
            mView.nameEdtxt.error = getString(R.string.enter_a_name_for_account)
            mView.nameEdtxt.requestFocus()
            return
        }
        if (selectedIcon == null || selectedIcon!!.isEmpty()) {
            Toast.makeText(
                activity,
                getString(R.string.select_an_icon_for_the_account),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val accountDao =
            DatabaseClient.getInstance(mContext)?.appDatabase?.accountDao()
        if (accountModel == null) {
            accountModel = AccountModel()
        }
        accountModel!!.name = accountName
        accountModel!!.icon = selectedIcon
        val compositeDisposable = CompositeDisposable()
        compositeDisposable.add(Completable.fromAction {
            if (accountModel?.id!! > 0) {
                accountDao?.updateAccount(accountModel!!)
            } else {
                accountDao?.addAccount(accountModel!!)
            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {}
        )
        dismiss()
    }
}