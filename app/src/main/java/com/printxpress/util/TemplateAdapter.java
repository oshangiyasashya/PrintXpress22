package com.printxpress.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.printxpress.R;
import com.printxpress.data.entity.SampleDesign;
import java.util.ArrayList;
import java.util.List;

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.ViewHolder> {
    private List<SampleDesign> templates = new ArrayList<>();
    private final OnTemplateClickListener listener;

    public interface OnTemplateClickListener {
        void onTemplateClick(SampleDesign design);
    }

    public TemplateAdapter(OnTemplateClickListener listener) {
        this.listener = listener;
    }

    public void setTemplates(List<SampleDesign> templates) {
        this.templates = templates;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_template, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SampleDesign design = templates.get(position);
        holder.tvTitle.setText(design.title);
        holder.ivThumb.setImageResource(design.imageRes);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTemplateClick(design);
        });
    }

    @Override
    public int getItemCount() {
        return templates.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvTitle, tvDesc;

        ViewHolder(View view) {
            super(view);
            ivThumb = view.findViewById(R.id.ivTemplate);
            tvTitle = view.findViewById(R.id.tvTemplateTitle);
            tvDesc = view.findViewById(R.id.tvTemplateDesc);
        }
    }
}
