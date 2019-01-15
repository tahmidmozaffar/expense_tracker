package com.remotearthsolutions.expensetracker.utils;

import com.remotearthsolutions.expensetracker.entities.ExpeneChartData;

import java.util.List;

public interface ChartManager {

    void initPierChart();
    void loadExpensePieChart(ChartManagerImpl.ChartView chartView, List<ExpeneChartData> data);

}
