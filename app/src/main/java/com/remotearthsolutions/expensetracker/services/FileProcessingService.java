package com.remotearthsolutions.expensetracker.services;

import android.app.Activity;
import com.remotearthsolutions.expensetracker.databaseutils.models.dtos.CategoryExpense;

import java.util.List;

public interface FileProcessingService {
    void writeOnCsvFile(Activity activity, String content);
    List<CategoryExpense> readFromCsvFile(Activity activity);
    List<?> loadTableData(String tableName, String hashString);
    void shareFile(Activity activity);
}
