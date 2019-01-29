package com.remotearthsolutions.expensetracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.remotearthsolutions.expensetracker.R;
import com.remotearthsolutions.expensetracker.adapters.AccountListAdapter;
import com.remotearthsolutions.expensetracker.contracts.AccountDialogContract;
import com.remotearthsolutions.expensetracker.databaseutils.DatabaseClient;
import com.remotearthsolutions.expensetracker.databaseutils.daos.AccountDao;
import com.remotearthsolutions.expensetracker.databaseutils.models.dtos.AccountIncome;
import com.remotearthsolutions.expensetracker.viewmodels.AccountDialogViewModel;
import com.remotearthsolutions.expensetracker.viewmodels.viewmodel_factory.AccountDialogViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class AccountDialogFragment extends DialogFragment implements AccountDialogContract.View {

    private AccountDialogViewModel presenter;
    private AccountListAdapter accountListAdapter;
    private List<AccountIncome> accountslist;
    private RecyclerView accountrecyclerView;
    private AccountDialogFragment.Callback callback;

    public AccountDialogFragment() {
    }

    public static AccountDialogFragment newInstance(String title) {
        AccountDialogFragment frag = new AccountDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    public void setCallback(AccountDialogFragment.Callback callback) {
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_account, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        accountrecyclerView = view.findViewById(R.id.accountrecyclearView);
        accountrecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        accountrecyclerView.setLayoutManager(llm);

        AccountDao accountDao = DatabaseClient.getInstance(getContext()).getAppDatabase().accountDao();
        presenter = ViewModelProviders.of(this,
                new AccountDialogViewModelFactory(this, accountDao)).
                get(AccountDialogViewModel.class);
        presenter.loadAccounts();


    }

    public void loadAccountlIST() {

        accountslist = new ArrayList<>();
        accountslist.add(new AccountIncome(1,"Cash", "currenncy",100));
        accountslist.add(new AccountIncome(2,"Bank", "bank",235));
        accountslist.add(new AccountIncome(3,"Loan", "load",100));

    }

    @Override
    public void onAccountFetchSuccess(List<AccountIncome> accounts) {
        loadAccountlIST();
        accountListAdapter = new AccountListAdapter(accountslist);
        accountListAdapter.setOnItemClickListener(new AccountListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AccountIncome account) {
                callback.onSelectAccount(account);
            }
        });
        accountrecyclerView.setAdapter(accountListAdapter);
    }

    @Override
    public void onAccountFetchFailure() {

    }

    public interface Callback {
        void onSelectAccount(AccountIncome accountIncome);
    }
}