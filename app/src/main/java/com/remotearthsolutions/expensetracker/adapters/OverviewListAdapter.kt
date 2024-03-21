package com.remotearthsolutions.expensetracker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.remotearthsolutions.expensetracker.R
import com.remotearthsolutions.expensetracker.adapters.viewholder.OverviewItemViewHolder
import com.remotearthsolutions.expensetracker.databaseutils.models.dtos.CategoryExpense
import com.remotearthsolutions.expensetracker.databaseutils.models.dtos.CategoryOverviewItemDto


class OverviewListAdapter(
    private val listOfItems: List<CategoryOverviewItemDto>,
    private val allExpenses: List<CategoryExpense>?,
    private val totalExpense: Double,
    private val maxWidthOfBar: Int,
    private val currencySymbol: String
) :
    RecyclerView.Adapter<OverviewItemViewHolder>() {

    private var selectedItemPosition: Int = -1
    private var selectedItemCategoryName: String = ""
    private var listener: OnItemClickListener? =
        null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverviewItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.view_overview_expense_item, parent, false)
        return OverviewItemViewHolder(v, listener!!, currencySymbol, inflater)
    }

    override fun getItemCount(): Int {
        return listOfItems.size
    }

    override fun onBindViewHolder(holder: OverviewItemViewHolder, position: Int) {
        val item = listOfItems[position]
        holder.bind(
            item,
            totalExpense,
            maxWidthOfBar,
            allExpenses,
            position,
            selectedItemCategoryName
        )
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.listener = listener
    }

    fun setSelectedItem(position: Int, categoryName: String) {
        selectedItemPosition = position
        selectedItemCategoryName = categoryName
    }

    fun getLastSelectedItemPosition(): Int = selectedItemPosition

    interface OnItemClickListener {
        fun onItemClick(position: Int, categoryName: String)
    }
}