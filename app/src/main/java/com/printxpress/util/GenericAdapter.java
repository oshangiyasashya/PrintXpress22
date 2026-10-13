package com.printxpress.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.printxpress.R;
import java.util.ArrayList;
import java.util.List;

public class GenericAdapter<T> extends RecyclerView.Adapter<GenericAdapter.ViewHolder> {
    private List<T> items = new ArrayList<>();
    private final int layoutResId;
    private final BindCallback<T> bindCallback;
    private final ClickCallback<T> clickCallback;

    public interface BindCallback<T> { void bind(View view, T item); }
    public interface ClickCallback<T> { void onClick(T item); }

    public GenericAdapter(BindCallback<T> bindCallback, ClickCallback<T> clickCallback) {
        this(R.layout.item_generic, bindCallback, clickCallback);
    }

    public GenericAdapter(int layoutResId, BindCallback<T> bindCallback, ClickCallback<T> clickCallback) {
        this.layoutResId = layoutResId;
        this.bindCallback = bindCallback;
        this.clickCallback = clickCallback;
    }

    public void setItems(List<T> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T item = items.get(position);
        bindCallback.bind(holder.itemView, item);
        holder.itemView.setOnClickListener(v -> {
            if (clickCallback != null) clickCallback.onClick(item);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View view) { super(view); }
    }
}
