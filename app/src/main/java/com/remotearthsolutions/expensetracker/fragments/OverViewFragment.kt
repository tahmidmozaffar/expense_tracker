package com.remotearthsolutions.expensetracker.fragments

import android.content.Context
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.remotearthsolutions.expensetracker.R
import com.remotearthsolutions.expensetracker.adapters.OverviewListAdapter
import com.remotearthsolutions.expensetracker.databaseutils.models.dtos.CategoryExpense
import com.remotearthsolutions.expensetracker.databaseutils.models.dtos.CategoryOverviewItemDto
import com.remotearthsolutions.expensetracker.databinding.FragmentOverviewBinding
import com.remotearthsolutions.expensetracker.utils.*
import com.remotearthsolutions.expensetracker.viewmodels.AllTransactionsViewModel
import com.remotearthsolutions.expensetracker.views.XYMarkerView
import org.koin.android.ext.android.inject
import java.util.*


class OverViewFragment : BaseFragment(), OnChartValueSelectedListener {
    private lateinit var binding: FragmentOverviewBinding
    private val viewModel: AllTransactionsViewModel by inject()
    private lateinit var listOfCategoryWithExpense: ArrayList<CategoryOverviewItemDto>
    private var map: MutableMap<Int, Int> = HashMap()
    private var mContext: Context? = null
    private lateinit var adapter: OverviewListAdapter
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOverviewBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val maxWidthOfBar = Utils.getDeviceScreenSize(requireActivity())
            ?.width?.minus(requireContext().resources.getDimension(R.dimen.dp_50))
        val height = Utils.getDeviceScreenSize(requireContext())?.height?.div(5)
        val param = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height!!)
        binding.barChart.layoutParams = param

        binding.recyclerView.setHasFixedSize(true)
        val llm = LinearLayoutManager(mContext)
        binding.recyclerView.layoutManager = llm

        viewModel.getAllCategory()
        viewModel.listOfCategoryLiveData.observe(viewLifecycleOwner) {
            listOfCategoryWithExpense = ArrayList()
            it.forEachIndexed { index, ctg ->

                val item = CategoryOverviewItemDto()
                item.categoryIcon = ctg.icon
                item.categoryName = ctg.name
                listOfCategoryWithExpense.add(item)
                map[ctg.id] = index
            }
        }

        setupChart()

        viewModel.chartDataRequirementLiveData.observe(viewLifecycleOwner) {

            val currencySymbol = Utils.getCurrency(requireContext())
            val dateFormat = SharedPreferenceUtils.getInstance(view.context)
                ?.getString(Constants.PREF_TIME_FORMAT, Constants.KEY_DATE_MONTH_YEAR_DEFAULT)
                ?: Constants.KEY_DATE_MONTH_YEAR_DEFAULT

            binding.barChart.clear()

            when (it.selectedFilterBtnId) {
                R.id.dailyRangeBtn -> {
                    val totalExpense = generateGraph(it, "MMM dd", Calendar.DAY_OF_MONTH)
                    val avg = Utils.formatDecimalValues(totalExpense / 24.0)
                    binding.totalCountView.setInfo(it.filteredList.size.toString())

                    binding.dailyAvgView.setTitle("Hourly(avg.)")
                    binding.dailyAvgView.setInfo("$avg $currencySymbol")
                }
                R.id.weeklyRangeBtn -> {
                    val totalExpense = generateGraph(it, "MMM dd", Calendar.DAY_OF_MONTH)
                    val avg = Utils.formatDecimalValues(totalExpense / 7.0)
                    binding.totalCountView.setInfo(it.filteredList.size.toString())

                    binding.dailyAvgView.setTitle("Daily(avg.)")
                    binding.dailyAvgView.setInfo("$avg $currencySymbol")
                }
                R.id.monthlyRangeBtn -> {
                    val totalExpense = generateGraph(it, "MMM dd", Calendar.DAY_OF_MONTH)
                    val noOfDays = DateTimeUtils.daysBetween(it.startTime, it.endTime)

                    val avg = Utils.formatDecimalValues(totalExpense / (noOfDays + 1))
                    binding.totalCountView.setInfo(it.filteredList.size.toString())

                    binding.dailyAvgView.setTitle("Daily(avg.)")
                    binding.dailyAvgView.setInfo("$avg $currencySymbol")
                }
                R.id.yearlyRangeBtn -> {
                    val totalExpense = generateGraph(it, "MMM", Calendar.MONTH)
                    val avg = Utils.formatDecimalValues(totalExpense / 12.0)
                    binding.totalCountView.setInfo(it.filteredList.size.toString())
                    binding.dailyAvgView.setTitle("Monthly(avg.)")
                    binding.dailyAvgView.setInfo("$avg $currencySymbol")
                }
            }

            listOfCategoryWithExpense.forEach { item ->
                item.totalExpenseOfCateogry = 0.0
            }
            var sum = 0.0
            it.filteredList.forEach { exp ->
                val index = map[exp.categoryId]
                index?.let {
                    val item = listOfCategoryWithExpense[index]
                    item.totalExpenseOfCateogry += exp.totalAmount
                    sum += exp.totalAmount
                }
            }

            val sortedList =
                listOfCategoryWithExpense.sortedWith(compareByDescending { item -> item.totalExpenseOfCateogry })

            val expensesByCategory = HashMap<String, List<CategoryExpense>>()

            sortedList.forEach { catModel ->

                val expensesOfCategory =
                    viewModel.expenseListLiveData.value?.filter { categoryExpense -> categoryExpense.categoryName == catModel.categoryName }
                expensesByCategory[catModel.categoryName!!] = expensesOfCategory!!
            }


            adapter =
                OverviewListAdapter(
                    sortedList,
                    expensesByCategory,
                    sum,
                    maxWidthOfBar!!.toInt(),
                    currencySymbol,
                    dateFormat
                )
            adapter.setOnItemClickListener(object : OverviewListAdapter.OnItemClickListener {
                override fun onItemClick(position: Int, categoryName: String) {
                    val lastSelectedItemPosition = adapter.getLastSelectedItemPosition()
                    adapter.setSelectedItem(position, categoryName)

                    adapter.notifyItemChanged(lastSelectedItemPosition)
                    adapter.notifyItemChanged(position)
                    binding.recyclerView.scrollToPosition(position)
                }
            })
            binding.recyclerView.adapter = adapter
        }

    }

    private fun generateGraph(
        it: AllTransactionsViewModel.ChartDataRequirement,
        dateFormat: String,
        calendarValueType: Int
    ): Double {
        val endTime = DateTimeUtils.getDate(it.endTime, dateFormat)

        val cal = Calendar.getInstance()
        cal.timeInMillis = it.startTime

        val xVals = ArrayList<String>()
        val barEntry = ArrayList<BarEntry>()
        var i = 0

        //add previous date of the starting day to show in the graph
        if (calendarValueType != Calendar.MONTH) {
            cal.add(calendarValueType, -1)
            val date = DateTimeUtils.getDate(cal.timeInMillis, dateFormat)
            xVals.add(date)
            barEntry.add(BarEntry(i.toFloat(), 0f))
            i++
        }
        //.......

        cal.timeInMillis = it.startTime
        while (true) {
            val date1 = DateTimeUtils.getDate(cal.timeInMillis, dateFormat)

            xVals.add(date1)
            barEntry.add(BarEntry(i.toFloat(), 0f))
            i++
            if (date1 == endTime)
                break
            cal.add(calendarValueType, 1)
        }

        val xAxisFormatter: ValueFormatter = DayAxisValueFormatter(xVals)
        binding.barChart.xAxis.valueFormatter = xAxisFormatter

        val mv = XYMarkerView(requireContext(), xAxisFormatter)
        mv.chartView = binding.barChart // For bounds control
        binding.barChart.marker = mv // Set the marker to the chart

        var sum = 0.0
        it.filteredList.forEach { exp ->
            sum += exp.totalAmount
            val date1 = DateTimeUtils.getDate(exp.datetime, dateFormat)
            val pos = xVals.indexOf(date1)
            if (pos >= 0) {
                val entry = barEntry[pos]
                entry.y += exp.totalAmount.toFloat()
            }
        }

        val barDataset = BarDataSet(barEntry, "")
        barDataset.setDrawValues(false)

        val data = BarData(barDataset)
        barDataset.setColors(*ColorTemplate.COLORFUL_COLORS)
        binding.barChart.data = data

        return sum
    }

    private fun setupChart() {
        binding.barChart.description.isEnabled = false
        binding.barChart.setPinchZoom(true)
        binding.barChart.setDrawBarShadow(false)
        binding.barChart.setDrawGridBackground(false)
        binding.barChart.setOnChartValueSelectedListener(this)

        binding.barChart.setDrawValueAboveBar(true)

        val xAxis = binding.barChart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // only intervals of 1 day
        xAxis.labelCount = 7

        val axisTextColor = ContextCompat.getColor(requireContext(), R.color.catAccItemTextColor)
        xAxis.textColor = axisTextColor
        binding.barChart.axisLeft.textColor = axisTextColor
        binding.barChart.axisRight.textColor = axisTextColor

        binding.barChart.axisLeft.setDrawGridLines(false)
        binding.barChart.animateY(500)
        binding.barChart.legend.isEnabled = false
        binding.barChart.setFitBars(true)
    }

    override fun onNothingSelected() {
    }

    private val onValueSelectedRectF = RectF()
    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e == null) return

        val bounds = onValueSelectedRectF
        binding.barChart.getBarBounds(e as BarEntry?, bounds)
        val position: MPPointF = binding.barChart.getPosition(e, AxisDependency.LEFT)
        MPPointF.recycleInstance(position)
    }
}
