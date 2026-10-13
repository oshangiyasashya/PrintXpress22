package com.printxpress.ui.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.printxpress.R;
import com.printxpress.data.entity.SavedDesign;
import com.printxpress.util.ImageLoader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SavedDesignsFragment extends Fragment {
    private SavedDesignViewModel viewModel;
    private SavedDesignsAdapter adapter;
    private View layoutEmpty;
    private RecyclerView rv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saved_designs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SavedDesignViewModel.class);

        rv = view.findViewById(R.id.rvSavedDesigns);
        layoutEmpty = view.findViewById(R.id.layoutEmptySavedDesigns);
        View btnExplore = view.findViewById(R.id.btnExploreTemplatesEmpty);

        if (btnExplore != null) {
            btnExplore.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.productsFragment));
        }

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SavedDesignsAdapter(new SavedDesignsAdapter.SavedDesignListener() {
            @Override
            public void onOrder(SavedDesign design) {
                Bundle bundle = new Bundle();
                bundle.putLong("productId", 1);
                bundle.putDouble("basePrice", 2500.0);
                bundle.putInt("quantity", 100);
                bundle.putString("designData", "Template: " + design.name);
                bundle.putString("previewImagePath", design.filePath);
                Navigation.findNavController(requireView()).navigate(R.id.checkoutFragment, bundle);
            }

            @Override
            public void onDelete(SavedDesign design) {
                new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Design")
                    .setMessage("Are you sure you want to delete \"" + design.name + "\"?")
                    .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteDesign(design.id))
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });
        rv.setAdapter(adapter);

        viewModel.getSavedDesigns().observe(getViewLifecycleOwner(), designs -> {
            if (designs != null && !designs.isEmpty()) {
                adapter.setList(designs);
                rv.setVisibility(View.VISIBLE);
                if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
            } else {
                adapter.setList(new ArrayList<>());
                rv.setVisibility(View.GONE);
                if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getActionMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class SavedDesignsAdapter extends RecyclerView.Adapter<SavedDesignsAdapter.ViewHolder> {
        public interface SavedDesignListener {
            void onOrder(SavedDesign design);
            void onDelete(SavedDesign design);
        }

        private List<SavedDesign> list = new ArrayList<>();
        private final SavedDesignListener listener;

        public SavedDesignsAdapter(SavedDesignListener listener) {
            this.listener = listener;
        }

        public void setList(List<SavedDesign> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_design_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SavedDesign design = list.get(position);
            holder.tvName.setText(design.name);

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            holder.tvDate.setText("Saved: " + sdf.format(new Date(design.createdAt)));

            // Load thumbnail using ImageLoader
            if (design.filePath != null && !design.filePath.isEmpty()) {
                ImageLoader.getInstance().load(design.filePath, holder.ivPreview);
            } else {
                holder.ivPreview.setImageResource(R.drawable.bg_order_gradient);
            }

            holder.btnOrder.setOnClickListener(v -> listener.onOrder(design));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(design));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivPreview;
            TextView tvName, tvDate, tvBadge;
            MaterialButton btnOrder;
            Button btnDelete;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivPreview = itemView.findViewById(R.id.ivSavedDesignPreview);
                tvName = itemView.findViewById(R.id.tvSavedDesignName);
                tvDate = itemView.findViewById(R.id.tvSavedDesignDate);
                tvBadge = itemView.findViewById(R.id.tvSavedDesignBadge);
                btnOrder = itemView.findViewById(R.id.btnOrderSavedDesign);
                btnDelete = itemView.findViewById(R.id.btnDeleteSavedDesign);
            }
        }
    }
}
