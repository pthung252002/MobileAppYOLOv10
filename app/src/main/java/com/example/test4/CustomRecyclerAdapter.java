package com.example.test4;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CustomRecyclerAdapter extends RecyclerView.Adapter<CustomRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<String> items;
    private int textColor;

    public CustomRecyclerAdapter(Context context, List<String> items, int textColor) {
        this.context = context;
        this.items = items;
        this.textColor = textColor;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (items != null && !items.isEmpty()) {
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append("\u2022 "); // Unicode của ký tự bullet
            builder.append(items.get(position));
            holder.textView.setText(builder);
        } else {
            holder.textView.setText("No data available");  // Cung cấp một giá trị mặc định khi danh sách trống
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }
    }

    // Phương thức để xóa dữ liệu
    public void clearData() {
        if (items != null) {
            items.clear();
            notifyDataSetChanged();
        }
    }
}
