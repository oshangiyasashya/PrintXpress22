package com.printxpress.ui.catalog;

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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.printxpress.R;
import com.printxpress.util.ImageLoader;

public class ProductDetailFragment extends Fragment {

    private ProductDetailViewModel viewModel;
    private long productId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProductDetailViewModel.class);

        if (getArguments() != null) {
            productId = getArguments().getLong("productId", -1);
        }

        TextView tvName = view.findViewById(R.id.tvProductName);
        TextView tvPrice = view.findViewById(R.id.tvProductPrice);
        TextView tvDesc = view.findViewById(R.id.tvProductDescription);
        TextView tvMaterial = view.findViewById(R.id.tvProductMaterial);
        TextView tvSizes = view.findViewById(R.id.tvProductSizes);
        ImageView ivImage = view.findViewById(R.id.ivProductImage);
        Button btnCustomize = view.findViewById(R.id.btnCustomize);
        Button btnAddToCart = view.findViewById(R.id.btnAddToCart);

        viewModel.getActionMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getProduct(productId).observe(getViewLifecycleOwner(), product -> {
            if (product != null) {
                tvName.setText(product.name);
                tvPrice.setText("Starting at Rs. " + product.basePrice);
                tvDesc.setText(product.description);
                tvMaterial.setText("Material: " + product.material);
                tvSizes.setText("Sizes: " + product.sizes);

                ImageLoader.loadRes(ivImage, product.imageRes);

                btnCustomize.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putLong("productId", product.id);
                    bundle.putDouble("basePrice", product.basePrice);
                    Navigation.findNavController(v).navigate(R.id.designFragment, bundle);
                });

                if (btnAddToCart != null) {
                    btnAddToCart.setOnClickListener(v -> {
                        viewModel.addToCart(product, 100, "", 0.0);
                    });
                }
            }
        });
    }
}
