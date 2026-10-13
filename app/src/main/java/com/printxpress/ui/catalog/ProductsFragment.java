package com.printxpress.ui.catalog;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.printxpress.R;
import com.printxpress.data.entity.Category;
import com.printxpress.data.entity.Product;
import com.printxpress.ui.upload.UploadArtworkActivity;
import com.printxpress.util.GenericAdapter;
import com.printxpress.util.TemplateAdapter;
import java.util.List;

public class ProductsFragment extends Fragment {
    private ProductsViewModel viewModel;
    private GenericAdapter<Product> productAdapter;
    private TemplateAdapter templateAdapter;
    private ChipGroup chipGroupCategories;
    private TextView tvCategoryName;

    private long selectedCategoryId = -1; // -1 means All Products
    private String selectedCategoryName = "All Products";
    private String searchQuery = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProductsViewModel.class);

        tvCategoryName = view.findViewById(R.id.tvCategoryName);
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
        Button btnBlankCanvas = view.findViewById(R.id.btnBlankCanvas);
        Button btnUploadOwn = view.findViewById(R.id.btnUploadOwn);

        if (getArguments() != null) {
            selectedCategoryId = getArguments().getLong("categoryId", -1);
            selectedCategoryName = getArguments().getString("categoryName", "All Products");
            searchQuery = getArguments().getString("searchQuery", null);
        }

        tvCategoryName.setText(selectedCategoryName);

        // Blank Canvas & Upload buttons
        btnBlankCanvas.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("templateTitle", "Blank Canvas");
            bundle.putLong("categoryId", selectedCategoryId > 0 ? selectedCategoryId : 1L);
            Navigation.findNavController(view).navigate(R.id.universalTemplateEditorFragment, bundle);
        });

        btnUploadOwn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), UploadArtworkActivity.class);
            intent.putExtra("productName", selectedCategoryName);
            intent.putExtra("productId", selectedCategoryId > 0 ? selectedCategoryId : 1L);
            intent.putExtra("basePrice", 2500.0);
            intent.putExtra("quantity", 1);
            startActivity(intent);
        });

        // Templates Grid (2 columns)
        RecyclerView rvTemplates = view.findViewById(R.id.rvTemplatesGrid);
        rvTemplates.setLayoutManager(new GridLayoutManager(getContext(), 2));
        templateAdapter = new TemplateAdapter(design -> {
            Bundle bundle = new Bundle();
            bundle.putString("templateTitle", design.title);
            bundle.putLong("categoryId", design.categoryId);
            Navigation.findNavController(view).navigate(R.id.universalTemplateEditorFragment, bundle);
        });
        rvTemplates.setAdapter(templateAdapter);

        // Products List
        RecyclerView rvProducts = view.findViewById(R.id.rvProducts);
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        productAdapter = new GenericAdapter<>(
            (v, product) -> {
                TextView tvMain = v.findViewById(R.id.tvMain);
                TextView tvSub = v.findViewById(R.id.tvSub);
                tvMain.setText(product.name);
                tvSub.setText("Material: " + product.material + " | Sizes: " + product.sizes + "\nStarting from: Rs. " + product.basePrice);
            },
            product -> {
                Bundle bundle = new Bundle();
                bundle.putLong("productId", product.id);
                Navigation.findNavController(view).navigate(R.id.productDetailFragment, bundle);
            }
        );
        rvProducts.setAdapter(productAdapter);

        setupCategoryChips();
        loadProductsAndTemplates();
    }

    private void setupCategoryChips() {
        if (chipGroupCategories == null) return;
        chipGroupCategories.removeAllViews();

        // 1. "All Products" Chip
        Chip allChip = new Chip(requireContext());
        allChip.setText("All Products");
        allChip.setCheckable(true);
        allChip.setChecked(selectedCategoryId == -1);
        allChip.setOnClickListener(v -> {
            selectedCategoryId = -1;
            selectedCategoryName = "All Products";
            tvCategoryName.setText(selectedCategoryName);
            loadProductsAndTemplates();
        });
        chipGroupCategories.addView(allChip);

        // 2. Dynamic Categories from DB
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories == null) return;
            for (Category category : categories) {
                Chip chip = new Chip(requireContext());
                chip.setText(category.name);
                chip.setCheckable(true);
                chip.setChecked(selectedCategoryId == category.id);
                chip.setOnClickListener(v -> {
                    selectedCategoryId = category.id;
                    selectedCategoryName = category.name;
                    tvCategoryName.setText(selectedCategoryName);
                    loadProductsAndTemplates();
                });
                chipGroupCategories.addView(chip);
            }
        });
    }

    private void loadProductsAndTemplates() {
        if (!TextUtils.isEmpty(searchQuery)) {
            tvCategoryName.setText("Search: " + searchQuery);
            viewModel.searchProducts(searchQuery).observe(getViewLifecycleOwner(), products -> {
                if (products != null) productAdapter.setItems(products);
            });
            viewModel.getAllSampleDesigns().observe(getViewLifecycleOwner(), designs -> {
                if (designs != null) templateAdapter.setTemplates(designs);
            });
            return;
        }

        if (selectedCategoryId == -1) {
            // Load ALL Products
            viewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
                if (products != null) productAdapter.setItems(products);
            });
            viewModel.getAllSampleDesigns().observe(getViewLifecycleOwner(), designs -> {
                if (designs != null) templateAdapter.setTemplates(designs);
            });
        } else {
            // Load Specific Category
            viewModel.getProductsByCategory(selectedCategoryId).observe(getViewLifecycleOwner(), products -> {
                if (products != null) productAdapter.setItems(products);
            });
            viewModel.getSamplesByCategory(selectedCategoryId).observe(getViewLifecycleOwner(), designs -> {
                if (designs != null) templateAdapter.setTemplates(designs);
            });
        }
    }
}
