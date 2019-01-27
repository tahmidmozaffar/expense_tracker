package com.remotearthsolutions.expensetracker.presenters;

import com.remotearthsolutions.expensetracker.contracts.CategoryFragmentContract;
import com.remotearthsolutions.expensetracker.databaseutils.daos.CategoryDao;
import com.remotearthsolutions.expensetracker.databaseutils.models.CategoryModel;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CategoryFragmentPresenter {

    private CategoryFragmentContract.View view;
    private CategoryDao categoryDao;
    private CompositeDisposable disposable = new CompositeDisposable();


    public CategoryFragmentPresenter(CategoryFragmentContract.View view, CategoryDao categoryDao) {
        this.view = view;
        this.categoryDao = categoryDao;
    }

    public void showCategories() {
        disposable.add(categoryDao.getAllCategories()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        categories -> view.showCategories(categories)
                ));
    }

    public void updateCategory(CategoryModel categoryModel) {

        Completable.fromAction(() -> categoryDao.updateCategory(categoryModel)).observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> showCategories());

    }

    public void deleteCategory(CategoryModel categoryModel) {

        Completable.fromAction(() -> categoryDao.deleteCategory(categoryModel)).observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> showCategories());

    }
}
