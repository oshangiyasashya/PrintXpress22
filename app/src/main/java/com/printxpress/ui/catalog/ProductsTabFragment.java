package com.printxpress.ui.catalog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.printxpress.R;
import com.printxpress.data.entity.Product;
import com.printxpress.util.GenericAdapter;
import java.util.ArrayList;
import java.util.List;

public class ProductsTabFragment extends Fragment {
    private ProductsViewModel viewModel;
    private GenericAdapter<Product> adapter;
    private List<Product> allProducts = new ArrayList<>();
    private long categoryId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProductsViewModel.class);

        if (getArguments() != null) {
            categoryId = getArguments().getLong("categoryId", -1);
        }

        EditText etSearch = view.findViewById(R.id.etSearchProduct);
        RecyclerView rv = view.findViewById(R.id.rvProductsList);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new GenericAdapter<>(
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
        rv.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        long targetCat = categoryId != -1 ? categoryId : 1;
        viewModel.getProductsByCategory(targetCat).observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                allProducts = products;
                adapter.setItems(products);
            }
        });
    }

    private void filterProducts(String query) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            if (p.name.toLowerCase().contains(query.toLowerCase()) || p.description.toLowerCase().contains(query.toLowerCase())) {
                filtered.add(p);
            }
        }
        adapter.setItems(filtered);
    }
}
