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
        holder.ivThumb.setImageResource(getDesignDrawableRes(design));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTemplateClick(design);
        });
    }

    private int getDesignDrawableRes(SampleDesign design) {
        if (design.imageRes > 0 && design.imageRes != android.R.drawable.ic_menu_agenda
                && design.imageRes != android.R.drawable.ic_menu_edit
                && design.imageRes != android.R.drawable.ic_menu_gallery
                && design.imageRes != android.R.drawable.ic_menu_view
                && design.imageRes != android.R.drawable.ic_menu_manage
                && design.imageRes != android.R.drawable.ic_menu_compass) {
            return design.imageRes;
        }

        switch ((int) design.categoryId) {
            case 1: return R.drawable.ic_cat_business_card;
            case 2: return R.drawable.ic_cat_flyer;
            case 3: return R.drawable.ic_cat_poster;
            case 4: return R.drawable.ic_cat_banner;
            case 5: return R.drawable.ic_cat_sticker;
            case 6: return R.drawable.ic_cat_mug;
            case 7: return R.drawable.ic_cat_tshirt;
            case 8: return R.drawable.ic_cat_brochure;
            case 9: return R.drawable.ic_cat_calendar;
            default: return R.drawable.ic_cat_custom;
        }
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
