package com.remotearthsolutions.expensetracker.databaseutils;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.remotearthsolutions.expensetracker.databaseutils.daos.AccountDao;
import com.remotearthsolutions.expensetracker.databaseutils.daos.CategoryDao;
import com.remotearthsolutions.expensetracker.databaseutils.daos.CategoryExpenseDao;
import com.remotearthsolutions.expensetracker.databaseutils.daos.ExpenseDao;
import com.remotearthsolutions.expensetracker.databaseutils.models.AccountModel;
import com.remotearthsolutions.expensetracker.databaseutils.models.CategoryModel;
import com.remotearthsolutions.expensetracker.databaseutils.models.ExpenseModel;


@Database(entities = {CategoryModel.class, AccountModel.class, ExpenseModel.class}, version = 1, exportSchema = false)

public abstract class AppDatabase extends RoomDatabase {
    public abstract CategoryDao categoryDao();
    public abstract AccountDao accountDao();
    public abstract ExpenseDao expenseDao();
    public abstract CategoryExpenseDao categoryExpenseDao();
}