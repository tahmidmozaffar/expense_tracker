package com.remotearthsolutions.expensetracker.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.razerdp.widget.animatedpieview.AnimatedPieView;
import com.razerdp.widget.animatedpieview.AnimatedPieViewConfig;
import com.remotearthsolutions.expensetracker.R;
import com.remotearthsolutions.expensetracker.activities.MainActivity;
import com.remotearthsolutions.expensetracker.adapters.CategoryListAdapter;
import com.remotearthsolutions.expensetracker.contracts.HomeFragmentContract;
import com.remotearthsolutions.expensetracker.databaseutils.DatabaseClient;
import com.remotearthsolutions.expensetracker.databaseutils.daos.AccountDao;
import com.remotearthsolutions.expensetracker.databaseutils.daos.CategoryDao;
import com.remotearthsolutions.expensetracker.databaseutils.models.CategoryModel;
import com.remotearthsolutions.expensetracker.entities.ExpeneChartData;
import com.remotearthsolutions.expensetracker.utils.*;
import com.remotearthsolutions.expensetracker.viewmodels.HomeFragmentViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class HomeFragment extends Fragment implements ChartManagerImpl.ChartView, HomeFragmentContract.View, View.OnClickListener {

    private CategoryListAdapter adapter;
    private AnimatedPieView mAnimatedPieView;
    private RecyclerView recyclerView;
    private HomeFragmentViewModel viewModel;
    private ImageView addCategory, nextDate, previousDate;
    private TextView showdate;
    private Button dailyButton, weeklyButton, monthlyButton, yearlyButton;
    private int cDay, cMonth, cYear;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        addCategory = view.findViewById(R.id.addcategoryinhome);
        showdate = view.findViewById(R.id.showdateinhome);
        nextDate = view.findViewById(R.id.nextdatebutton);
        previousDate = view.findViewById(R.id.previousdatebutton);
        dailyButton = view.findViewById(R.id.daily);
        weeklyButton = view.findViewById(R.id.weekly);
        monthlyButton = view.findViewById(R.id.monthly);
        yearlyButton = view.findViewById(R.id.yearly);

        addCategory.setOnClickListener(this);
        nextDate.setOnClickListener(this);
        previousDate.setOnClickListener(this);
        dailyButton.setOnClickListener(this);
        weeklyButton.setOnClickListener(this);
        monthlyButton.setOnClickListener(this);
        yearlyButton.setOnClickListener(this);


        mAnimatedPieView = view.findViewById(R.id.animatedpie);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(llm);

        CategoryDao categoryDao = DatabaseClient.getInstance(getContext()).getAppDatabase().categoryDao();
        AccountDao accountDao = DatabaseClient.getInstance(getContext()).getAppDatabase().accountDao();
        viewModel = new HomeFragmentViewModel(this, categoryDao, accountDao);
        viewModel.init();
        viewModel.loadExpenseChart();

        BottomNavigationView navigation = view.findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        showdate.setText(DateTimeUtils.getCurrentDate(DateTimeUtils.dd_MM_yyyy));

        return view;
    }

    @Override
    public void loadChartConfig(AnimatedPieViewConfig config) {
        mAnimatedPieView.applyConfig(config).start();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                Toast.makeText(getActivity(), "Clicked On Bottom menu 1", Toast.LENGTH_LONG).show();
                return true;
            case R.id.navigation_dashboard:
                Toast.makeText(getActivity(), "Clicked On Bottom Menu 2", Toast.LENGTH_LONG).show();
                return true;
            case R.id.navigation_notifications:
                Toast.makeText(getActivity(), "Clicked On Bottom Menu 3", Toast.LENGTH_LONG).show();
                return true;
        }
        return false;
    };

    @Override
    public void showCategories(List<CategoryModel> categories) {

        adapter = new CategoryListAdapter(categories);
        adapter.setOnItemClickListener(new CategoryListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CategoryModel category) {
                ((MainActivity) getActivity()).openAddExpenseScreen(category);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void loadExpenseChart(List<ExpeneChartData> listOfCategoryWithAmount) {

        ChartManager chartManager = new ChartManagerImpl();
        chartManager.initPierChart();
        chartManager.loadExpensePieChart(this, listOfCategoryWithAmount);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.addcategoryinhome) {

            FragmentManager fm = getChildFragmentManager();
            final AddCategoryDialogFragment categoryDialogFragment = AddCategoryDialogFragment.newInstance("Add Category");
            categoryDialogFragment.setCallback(new AddCategoryDialogFragment.Callback() {
                @Override
                public void onCategoryAdded(CategoryModel categoryModel) {

                    categoryDialogFragment.dismiss();

                }
            });
            categoryDialogFragment.show(fm, AddCategoryDialogFragment.class.getName());

        } else if (v.getId() == R.id.nextdatebutton) {

            SimpleDateFormat sdf = new SimpleDateFormat(DateTimeUtils.dd_MM_yyyy);
            try {

                Date getCurrentDate = sdf.parse(DateTimeUtils.getCurrentDate(DateTimeUtils.dd_MM_yyyy));
                Date getNextDate = sdf.parse(DateTimeUtils.getDate(DateTimeUtils.dd_MM_yyyy, +1));

                if (getNextDate.compareTo(getCurrentDate) > 0) {
                    showdate.setText(DateTimeUtils.getCurrentDate(DateTimeUtils.dd_MM_yyyy));
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else if (v.getId() == R.id.previousdatebutton) {

            String previousdate = DateTimeUtils.getDate(DateTimeUtils.dd_MM_yyyy, -1);
            showdate.setText(previousdate);
        } else if (v.getId() == R.id.daily) {

            DatePicker datePicker = new DatePicker(getActivity());
            cDay = datePicker.getDayOfMonth();
            cMonth = datePicker.getMonth() + 1;
            cYear = datePicker.getYear();


            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                    showdate.setText(dayOfMonth + "/" + month + "/" + year);

                }
            }, cYear, cMonth, cDay);
            datePickerDialog.show();
        } else if (v.getId() == R.id.weekly) {


        } else if (v.getId() == R.id.monthly) {
            DatePicker datePicker = new DatePicker(getActivity());
            cDay = datePicker.getDayOfMonth();
            cMonth = datePicker.getMonth() + 1;
            cYear = datePicker.getYear();


            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                    showdate.setText(month + "/" + year);

                }
            }, cYear, cMonth, cDay);
            datePickerDialog.show();

        } else if (v.getId() == R.id.yearly) {
            DatePicker datePicker = new DatePicker(getActivity());
            cDay = datePicker.getDayOfMonth();
            cMonth = datePicker.getMonth() + 1;
            cYear = datePicker.getYear();

            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                    showdate.setText(year);

                }
            }, cYear, cMonth, cDay);
            datePickerDialog.show();

        }


    }
}
