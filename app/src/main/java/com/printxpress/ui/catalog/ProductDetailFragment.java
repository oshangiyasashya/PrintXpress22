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
import com.printxpress.data.entity.Product;
import java.util.Locale;

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

        // Slide-up entrance animation for card
        View cardImage = view.findViewById(R.id.cardProductImage);
        if (cardImage != null) {
            cardImage.setTranslationY(120f);
            cardImage.setAlpha(0f);
            cardImage.animate().translationY(0f).alpha(1f).setDuration(450).start();
        }

        viewModel.getActionMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getProduct(productId).observe(getViewLifecycleOwner(), product -> {
            if (product != null) {
                if (tvName != null) tvName.setText(product.name);
                if (tvPrice != null) tvPrice.setText(String.format(Locale.getDefault(), "Starting at Rs. %,.2f", product.basePrice));
                if (tvDesc != null) tvDesc.setText(product.description);
                if (tvMaterial != null) tvMaterial.setText("Material: " + product.material);
                if (tvSizes != null) tvSizes.setText("Sizes: " + product.sizes);

                if (ivImage != null) {
                    ivImage.setImageResource(getProductDrawableRes(product));
                }

                if (btnCustomize != null) {
                    btnCustomize.setOnClickListener(v -> {
                        Bundle bundle = new Bundle();
                        bundle.putLong("productId", product.id);
                        bundle.putString("templateTitle", product.name);
                        bundle.putLong("categoryId", product.categoryId);
                        bundle.putDouble("basePrice", product.basePrice);
                        Navigation.findNavController(v).navigate(R.id.universalTemplateEditorFragment, bundle);
                    });
                }

                if (btnAddToCart != null) {
                    btnAddToCart.setOnClickListener(v -> {
                        viewModel.addToCart(product, 100, "", 0.0);
                        Toast.makeText(getContext(), "Added " + product.name + " to cart! 🛒", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private int getProductDrawableRes(Product product) {
        if (product.imageRes > 0 && product.imageRes != android.R.drawable.ic_menu_agenda
                && product.imageRes != android.R.drawable.ic_menu_edit
                && product.imageRes != android.R.drawable.ic_menu_gallery
                && product.imageRes != android.R.drawable.ic_menu_view
                && product.imageRes != android.R.drawable.ic_menu_manage
                && product.imageRes != android.R.drawable.ic_menu_compass) {
            return product.imageRes;
        }

        switch ((int) product.categoryId) {
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
}
