package com.printxpress.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.printxpress.R;
import com.printxpress.data.entity.Category;
import com.printxpress.data.entity.Product;
import com.printxpress.data.entity.SampleDesign;
import com.printxpress.util.GenericAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminCatalogFragment extends Fragment {

    private AdminViewModel viewModel;
    private RecyclerView rvCatalog;
    private EditText etSearch;
    private ChipGroup chipGroupTab;

    private List<Product> allProductsList = new ArrayList<>();
    private List<Category> allCategoriesList = new ArrayList<>();
    private List<SampleDesign> allTemplatesList = new ArrayList<>();

    private String currentTab = "PRODUCTS";
    private String currentSearch = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_catalog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        rvCatalog = view.findViewById(R.id.rvAdminCatalog);
        etSearch = view.findViewById(R.id.etCatalogSearch);
        chipGroupTab = view.findViewById(R.id.chipGroupCatalogTab);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddCatalogItem);

        rvCatalog.setLayoutManager(new LinearLayoutManager(getContext()));

        fabAdd.setOnClickListener(v -> {
            if ("CATEGORIES".equals(currentTab)) {
                showAddCategoryDialog();
            } else if ("TEMPLATES".equals(currentTab)) {
                showAddTemplateDialog();
            } else {
                showAddProductDialog();
            }
        });

        chipGroupTab.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipTabCategories) currentTab = "CATEGORIES";
            else if (checkedId == R.id.chipTabTemplates) currentTab = "TEMPLATES";
            else currentTab = "PRODUCTS";

            renderCurrentTab();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().trim().toLowerCase(Locale.ROOT);
                renderCurrentTab();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        observeData();
    }

    private void observeData() {
        viewModel.getAllProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                allProductsList = products;
                if ("PRODUCTS".equals(currentTab)) renderCurrentTab();
            }
        });

        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                allCategoriesList = categories;
                if ("CATEGORIES".equals(currentTab)) renderCurrentTab();
            }
        });

        viewModel.getAllSampleTemplates().observe(getViewLifecycleOwner(), templates -> {
            if (templates != null) {
                allTemplatesList = templates;
                if ("TEMPLATES".equals(currentTab)) renderCurrentTab();
            }
        });
    }

    private void renderCurrentTab() {
        if ("CATEGORIES".equals(currentTab)) {
            renderCategories();
        } else if ("TEMPLATES".equals(currentTab)) {
            renderTemplates();
        } else {
            renderProducts();
        }
    }

    private void renderProducts() {
        List<Product> filtered = new ArrayList<>();
        for (Product p : allProductsList) {
            if (currentSearch.isEmpty() || (p.name != null && p.name.toLowerCase(Locale.ROOT).contains(currentSearch))) {
                filtered.add(p);
            }
        }
        rvCatalog.setAdapter(new ProductsAdapter(filtered));
    }

    private void renderCategories() {
        List<Category> filtered = new ArrayList<>();
        for (Category c : allCategoriesList) {
            if (currentSearch.isEmpty() || (c.name != null && c.name.toLowerCase(Locale.ROOT).contains(currentSearch))) {
                filtered.add(c);
            }
        }
        GenericAdapter<Category> adapter = new GenericAdapter<>(
            (v, category) -> {
                TextView tvMain = v.findViewById(R.id.tvMain);
                TextView tvSub = v.findViewById(R.id.tvSub);
                tvMain.setText("📁 Category #" + category.id + ": " + category.name);
                tvSub.setText("Tap to view details");
            },
            null
        );
        adapter.setItems(filtered);
        rvCatalog.setAdapter(adapter);
    }

    private void renderTemplates() {
        List<SampleDesign> filtered = new ArrayList<>();
        for (SampleDesign t : allTemplatesList) {
            if (currentSearch.isEmpty() || (t.title != null && t.title.toLowerCase(Locale.ROOT).contains(currentSearch))) {
                filtered.add(t);
            }
        }
        GenericAdapter<SampleDesign> adapter = new GenericAdapter<>(
            (v, template) -> {
                TextView tvMain = v.findViewById(R.id.tvMain);
                TextView tvSub = v.findViewById(R.id.tvSub);
                tvMain.setText("🎨 Template #" + template.id + ": " + template.title);
                tvSub.setText("Category ID: " + template.categoryId);
            },
            null
        );
        adapter.setItems(filtered);
        rvCatalog.setAdapter(adapter);
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add New Category");

        final EditText etName = new EditText(getContext());
        etName.setHint("Category Name (e.g. Stickers & Labels)");
        builder.setView(etName);

        builder.setPositiveButton("Create Category", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                Category cat = new Category(name, android.R.drawable.ic_menu_agenda);
                viewModel.addCategory(cat);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showAddTemplateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add New Sample Template");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etTitle = new EditText(getContext());
        etTitle.setHint("Template Title");
        layout.addView(etTitle);

        final Spinner spinnerCategory = new Spinner(getContext());
        List<String> catNames = new ArrayList<>();
        for (Category c : allCategoriesList) catNames.add(c.name);
        if (catNames.isEmpty()) catNames.add("Default Category");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, catNames);
        spinnerCategory.setAdapter(adapter);
        layout.addView(spinnerCategory);

        builder.setView(layout);

        builder.setPositiveButton("Save Template", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            if (!title.isEmpty()) {
                long catId = 1L;
                int pos = spinnerCategory.getSelectedItemPosition();
                if (pos >= 0 && pos < allCategoriesList.size()) {
                    catId = allCategoriesList.get(pos).id;
                }
                SampleDesign design = new SampleDesign(catId, title, android.R.drawable.ic_menu_gallery);
                viewModel.addSampleTemplate(design);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add New Product");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etName = new EditText(getContext());
        etName.setHint("Product Name");
        layout.addView(etName);

        final EditText etDesc = new EditText(getContext());
        etDesc.setHint("Description");
        layout.addView(etDesc);

        final EditText etPrice = new EditText(getContext());
        etPrice.setHint("Base Price (Rs.)");
        etPrice.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etPrice);

        final EditText etMaterial = new EditText(getContext());
        etMaterial.setHint("Material Specs (e.g. 350gsm Matte)");
        layout.addView(etMaterial);

        final EditText etSizes = new EditText(getContext());
        etSizes.setHint("Dimensions (e.g. 90x55 mm)");
        layout.addView(etSizes);

        final Spinner spinnerCat = new Spinner(getContext());
        List<String> catNames = new ArrayList<>();
        for (Category c : allCategoriesList) catNames.add(c.name);
        if (catNames.isEmpty()) catNames.add("General Category");
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, catNames);
        spinnerCat.setAdapter(catAdapter);
        layout.addView(spinnerCat);

        builder.setView(layout);

        builder.setPositiveButton("Save Product", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String material = etMaterial.getText().toString().trim();
            String sizes = etSizes.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr)) {
                Toast.makeText(getContext(), "Please fill in product name and price", Toast.LENGTH_SHORT).show();
                return;
            }

            long catId = 1L;
            int pos = spinnerCat.getSelectedItemPosition();
            if (pos >= 0 && pos < allCategoriesList.size()) {
                catId = allCategoriesList.get(pos).id;
            }

            double price = Double.parseDouble(priceStr);
            Product p = new Product(catId, name, desc, price, android.R.drawable.ic_menu_agenda, material, sizes);
            viewModel.addProduct(p);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {
        private final List<Product> items;

        public ProductsAdapter(List<Product> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_product_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Product product = items.get(position);
            holder.tvName.setText(product.name);
            holder.tvMaterial.setText((product.material != null ? product.material : "") + " • " + (product.sizes != null ? product.sizes : ""));
            holder.tvPrice.setText(String.format(Locale.getDefault(), "Base Price: Rs. %,.2f", product.basePrice));

            holder.switchActive.setOnCheckedChangeListener(null);
            holder.switchActive.setChecked(product.isActive);
            holder.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                viewModel.toggleProductActive(product.id, isChecked);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvMaterial, tvPrice;
            SwitchMaterial switchActive;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvProductName);
                tvMaterial = itemView.findViewById(R.id.tvProductMaterial);
                tvPrice = itemView.findViewById(R.id.tvProductPrice);
                switchActive = itemView.findViewById(R.id.switchProductActive);
            }
        }
    }
}
