package com.printxpress.ui.colorpicker;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.printxpress.R;
import java.util.ArrayList;
import java.util.List;

public class ColorSwatchAdapter extends RecyclerView.Adapter<ColorSwatchAdapter.SwatchViewHolder> {

    private List<Integer> colors = new ArrayList<>();
    private int selectedColor = Color.TRANSPARENT;
    private OnColorClickListener listener;

    public interface OnColorClickListener {
        void onColorClick(int color);
    }

    public ColorSwatchAdapter(List<Integer> colors, OnColorClickListener listener) {
        if (colors != null) {
            this.colors = colors;
        }
        this.listener = listener;
    }

    public void setColors(List<Integer> colors) {
        this.colors = colors != null ? colors : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSelectedColor(int color) {
        this.selectedColor = color;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SwatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color_swatch, parent, false);
        return new SwatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SwatchViewHolder holder, int position) {
        int color = colors.get(position);
        holder.bind(color, color == selectedColor, listener);
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    static class SwatchViewHolder extends RecyclerView.ViewHolder {
        private final View colorView;
        private final View selectionIndicator;

        public SwatchViewHolder(@NonNull View itemView) {
            super(itemView);
            colorView = itemView.findViewById(R.id.viewSwatchColor);
            selectionIndicator = itemView.findViewById(R.id.viewSelectionIndicator);
        }

        public void bind(int color, boolean isSelected, OnColorClickListener listener) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);
            if (Color.red(color) > 240 && Color.green(color) > 240 && Color.blue(color) > 240) {
                drawable.setStroke(2, Color.parseColor("#CCCCCC"));
            } else {
                drawable.setStroke(1, Color.parseColor("#33000000"));
            }
            colorView.setBackground(drawable);

            if (isSelected) {
                selectionIndicator.setVisibility(View.VISIBLE);
                GradientDrawable indicatorDrawable = new GradientDrawable();
                indicatorDrawable.setShape(GradientDrawable.OVAL);
                indicatorDrawable.setStroke(4, Color.parseColor("#3B82F6"));
                indicatorDrawable.setColor(Color.TRANSPARENT);
                selectionIndicator.setBackground(indicatorDrawable);
            } else {
                selectionIndicator.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onColorClick(color);
                }
            });
        }
    }
}
