package com.remotearthsolutions.expensetracker.adapters.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.remotearthsolutions.expensetracker.R;
import com.remotearthsolutions.expensetracker.adapters.CategoryListAdapter;
import com.remotearthsolutions.expensetracker.entities.Category;

public class CategoryViewHolder extends RecyclerView.ViewHolder {

    private ImageView categoryImageIv;
    private TextView categoryNameTv;
    private Category category;
    private int position;

    public CategoryViewHolder(View view, final CategoryListAdapter.OnItemClickListener listener) {
        super(view);
        categoryImageIv = view.findViewById(R.id.eventtitle);
        categoryNameTv = view.findViewById(R.id.eventdate);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(category, position);
            }
        });
    }

    public void bind(Category category, int position) {
        this.category = category;
        this.position = position;
        categoryImageIv.setImageResource(category.getCategoryImage());
        categoryNameTv.setText(category.getCategoryName());
    }

}
