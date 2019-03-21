package com.remotearthsolutions.expensetracker.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.remotearthsolutions.expensetracker.R;
import com.remotearthsolutions.expensetracker.activities.ApplicationObject;
import com.remotearthsolutions.expensetracker.adapters.CategoryListViewAdapter;
import com.remotearthsolutions.expensetracker.contracts.CategoryFragmentContract;
import com.remotearthsolutions.expensetracker.databaseutils.DatabaseClient;
import com.remotearthsolutions.expensetracker.databaseutils.daos.CategoryDao;
import com.remotearthsolutions.expensetracker.databaseutils.models.CategoryModel;
import com.remotearthsolutions.expensetracker.viewmodels.CategoryViewModel;

import java.util.List;

public class CategoryFragment extends BaseFragment implements CategoryFragmentContract.View, OptionBottomSheetFragment.Callback {

    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    private CategoryListViewAdapter adapter;
    private CategoryViewModel viewModel;
    private CategoryModel selectedCategory;
    private int limitOfCategory;
    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public CategoryFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        floatingActionButton = view.findViewById(R.id.addcategory);
        recyclerView = view.findViewById(R.id.cat_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        CategoryDao categoryDao = DatabaseClient.getInstance(getContext()).getAppDatabase().categoryDao();
        viewModel = new CategoryViewModel(this, categoryDao);
        viewModel.showCategories();

        viewModel.getNumberOfItem().observe(getViewLifecycleOwner(), (Integer count) -> limitOfCategory = count);

        floatingActionButton.setOnClickListener(v -> {
            if (limitOfCategory < 20 ||
                    ((ApplicationObject) ((Activity) context).getApplication()).isPremium()) {
                selectedCategory = null;
                onClickEditBtn();
            } else {
                showAlert("Attention", "You need to be premium user to add more categories", "Ok", null, null);
            }
        });

        return view;
    }

    @Override
    public void showCategories(List<CategoryModel> categories) {

        adapter = new CategoryListViewAdapter(categories);
        adapter.setOnItemClickListener(categoryModel -> {

            selectedCategory = categoryModel;
            OptionBottomSheetFragment optionBottomSheetFragment = new OptionBottomSheetFragment();
            optionBottomSheetFragment.setCallback(CategoryFragment.this, OptionBottomSheetFragment.OptionsFor.CATEGORY);
            optionBottomSheetFragment.show(getChildFragmentManager(), OptionBottomSheetFragment.class.getName());

        });
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onClickAddAmountBtn() {
        //THis is not need for category. need to refactor this somehow so this method will not be needed to implement here.
    }

    @Override
    public void onClickEditBtn() {
        FragmentManager fm = getChildFragmentManager();
        final AddCategoryDialogFragment categoryDialogFragment = AddCategoryDialogFragment.newInstance("Update Category");
        categoryDialogFragment.setCategory(selectedCategory);
        categoryDialogFragment.setCallback(categoryModel1 -> {
            //viewModel.showCategories();
            categoryDialogFragment.dismiss();

        });
        categoryDialogFragment.show(fm, AddCategoryDialogFragment.class.getName());
    }

    @Override
    public void onClickDeleteBtn() {

        if (selectedCategory.getNotremovable() == 1) {
            showToast("You cannot delete this expense category");
            return;
        }
        showAlert("Warning", "Deleting this category will remove expenses related to this also. Are you sure, You want to Delete?", "Yes", "Not now", new Callback() {
            @Override
            public void onOkBtnPressed() {
                viewModel.deleteCategory(selectedCategory);
                Toast.makeText(context, "Category Deleted Successfully", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelBtnPressed() {

            }
        });
    }

    @Override
    public Context getContext() {
        return context;
    }
}
