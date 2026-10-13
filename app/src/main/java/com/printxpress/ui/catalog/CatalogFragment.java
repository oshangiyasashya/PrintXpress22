package com.printxpress.ui.catalog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.printxpress.R;
import com.printxpress.data.entity.Category;
import com.printxpress.util.ImageLoader;
import com.printxpress.data.entity.Order;
import com.printxpress.data.entity.Product;
import com.printxpress.data.entity.Promotion;
import com.printxpress.data.entity.SampleDesign;
import com.printxpress.data.repository.CatalogRepository;
import com.printxpress.data.repository.CommonRepository;
import com.printxpress.data.repository.OrderRepository;
import com.printxpress.data.repository.UserRepository;
import com.printxpress.util.GenericAdapter;
import com.printxpress.util.SessionManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.lifecycle.ViewModelProvider;

public class CatalogFragment extends Fragment {
    private CatalogViewModel catalogViewModel;

    private GenericAdapter<Category> categoriesAdapter;
    private GenericAdapter<Promotion> promoPagerAdapter;
    private GenericAdapter<Promotion> offersAdapter;
    private GenericAdapter<Product> popularAdapter;
    private GenericAdapter<SampleDesign> designGalleryAdapter;
    private GenericAdapter<Order> recentOrdersAdapter;

    private TextView tvGreeting, tvViewAllProducts;
    private ImageView ivNotificationBell, ivProfileAvatar;
    private EditText etSearchCatalog;
    private ViewPager2 viewPagerPromos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_catalog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        catalogViewModel = new ViewModelProvider(this).get(CatalogViewModel.class);

        tvGreeting = view.findViewById(R.id.tvGreeting);
        ivNotificationBell = view.findViewById(R.id.ivNotificationBell);
        ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar);
        etSearchCatalog = view.findViewById(R.id.etSearchCatalog);
        viewPagerPromos = view.findViewById(R.id.viewPagerPromos);
        tvViewAllProducts = view.findViewById(R.id.tvViewAllProducts);

        // 1. User Info, DP, Bell, and Avatar Clicks
        catalogViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvGreeting.setText("Hello, " + user.fullName + " 👋");
                if (!TextUtils.isEmpty(user.profileImageUri)) {
                    ImageLoader.loadCircle(requireContext(), user.profileImageUri, ivProfileAvatar, R.drawable.ic_profile);
                }
            }
        });

        if (ivNotificationBell != null) {
            ivNotificationBell.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.notificationsFragment));
        }

        if (ivProfileAvatar != null) {
            ivProfileAvatar.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.profileFragment));
        }

        // 2. Functional Search Bar
        ImageView ivSearchIconAction = view.findViewById(R.id.ivSearchIconAction);
        ImageView ivClearSearchCatalog = view.findViewById(R.id.ivClearSearchCatalog);

        Runnable performSearch = () -> {
            String query = etSearchCatalog != null ? etSearchCatalog.getText().toString().trim() : "";
            if (!TextUtils.isEmpty(query)) {
                Bundle bundle = new Bundle();
                bundle.putLong("categoryId", -1);
                bundle.putString("categoryName", "Search: " + query);
                bundle.putString("searchQuery", query);
                Navigation.findNavController(view).navigate(R.id.productsFragment, bundle);
            }
        };

        if (ivSearchIconAction != null) {
            ivSearchIconAction.setOnClickListener(v -> performSearch.run());
        }

        if (ivClearSearchCatalog != null && etSearchCatalog != null) {
            ivClearSearchCatalog.setOnClickListener(v -> {
                etSearchCatalog.setText("");
                ivClearSearchCatalog.setVisibility(View.GONE);
            });

            etSearchCatalog.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ivClearSearchCatalog.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }

        if (etSearchCatalog != null) {
            etSearchCatalog.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    performSearch.run();
                    return true;
                }
                return false;
            });
        }

        // 3. View All Products (All categories)
        if (tvViewAllProducts != null) {
            tvViewAllProducts.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putLong("categoryId", -1);
                bundle.putString("categoryName", "All Products");
                Navigation.findNavController(v).navigate(R.id.productsFragment, bundle);
            });
        }

        // 4. Promo Banners Carousel (ViewPager2)
        if (viewPagerPromos != null) {
            promoPagerAdapter = new GenericAdapter<>(R.layout.item_promo_card,
                (v, promo) -> {
                    TextView tvTitle = v.findViewById(R.id.tvPromoTitle);
                    TextView tvDesc = v.findViewById(R.id.tvPromoDescription);
                    TextView tvBadge = v.findViewById(R.id.tvPromoBadge);
                    if (tvTitle != null) tvTitle.setText(promo.title);
                    if (tvDesc != null) tvDesc.setText(promo.description);
                    if (tvBadge != null) tvBadge.setText("SAVE " + (int)promo.discountPercent + "%");
                },
                promo -> {
                    Bundle bundle = new Bundle();
                    bundle.putLong("categoryId", -1);
                    bundle.putString("categoryName", promo.title);
                    Navigation.findNavController(view).navigate(R.id.productsFragment, bundle);
                }
            );
            viewPagerPromos.setAdapter(promoPagerAdapter);
        }

        // 5. Categories Horizontal Grid
        RecyclerView rvCategories = view.findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoriesAdapter = new GenericAdapter<>(R.layout.item_service_tile,
            (v, category) -> {
                TextView tvName = v.findViewById(R.id.tvServiceName);
                ImageView ivIcon = v.findViewById(R.id.ivServiceIcon);
                tvName.setText(category.name);
                ivIcon.setImageResource(category.iconRes);
            },
            category -> {
                Bundle bundle = new Bundle();
                bundle.putLong("categoryId", category.id);
                bundle.putString("categoryName", category.name);
                Navigation.findNavController(view).navigate(R.id.productsFragment, bundle);
            }
        );
        rvCategories.setAdapter(categoriesAdapter);

        // 6. Popular Products Horizontal List
        RecyclerView rvPopular = view.findViewById(R.id.rvPopularProducts);
        rvPopular.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        popularAdapter = new GenericAdapter<>(R.layout.item_product_card_compact,
            (v, product) -> {
                TextView tvName = v.findViewById(R.id.tvProductName);
                TextView tvPrice = v.findViewById(R.id.tvProductPrice);
                ImageView ivThumb = v.findViewById(R.id.ivProductThumb);
                if (tvName != null) tvName.setText(product.name);
                if (tvPrice != null) tvPrice.setText("Rs. " + product.basePrice);
                if (ivThumb != null) ivThumb.setImageResource(product.imageRes);
            },
            product -> {
                Bundle bundle = new Bundle();
                bundle.putLong("productId", product.id);
                Navigation.findNavController(view).navigate(R.id.productDetailFragment, bundle);
            }
        );
        rvPopular.setAdapter(popularAdapter);

        // 7. Seasonal Offers List
        RecyclerView rvOffers = view.findViewById(R.id.rvSeasonalOffers);
        rvOffers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        offersAdapter = new GenericAdapter<>(
            (v, promo) -> {
                TextView tvMain = v.findViewById(R.id.tvMain);
                TextView tvSub = v.findViewById(R.id.tvSub);
                tvMain.setText(promo.title);
                tvSub.setText(promo.description + " (-" + (int)promo.discountPercent + "%)");
            },
            promo -> {}
        );
        rvOffers.setAdapter(offersAdapter);

        // 8. Design Gallery & Inspiration
        RecyclerView rvDesignGallery = view.findViewById(R.id.rvDesignGallery);
        if (rvDesignGallery != null) {
            rvDesignGallery.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            designGalleryAdapter = new GenericAdapter<>(R.layout.item_product_card_compact,
                (v, design) -> {
                    TextView tvName = v.findViewById(R.id.tvProductName);
                    TextView tvSub = v.findViewById(R.id.tvProductPrice);
                    ImageView ivThumb = v.findViewById(R.id.ivProductThumb);
                    if (tvName != null) tvName.setText(design.title);
                    if (tvSub != null) tvSub.setText("Template");
                    if (ivThumb != null) ivThumb.setImageResource(design.imageRes);
                },
                design -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("templateTitle", design.title);
                    bundle.putLong("categoryId", design.categoryId);
                    Navigation.findNavController(view).navigate(R.id.universalTemplateEditorFragment, bundle);
                }
            );
            rvDesignGallery.setAdapter(designGalleryAdapter);
        }

        // 9. My Recent Orders Strip
        RecyclerView rvRecentOrders = view.findViewById(R.id.rvRecentOrders);
        if (rvRecentOrders != null) {
            rvRecentOrders.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            recentOrdersAdapter = new GenericAdapter<>(R.layout.item_order_card_compact,
                (v, order) -> {
                    TextView tvNum = v.findViewById(R.id.tvOrderNum);
                    TextView tvBadge = v.findViewById(R.id.tvOrderBadge);
                    TextView tvPrice = v.findViewById(R.id.tvOrderPrice);
                    TextView tvDate = v.findViewById(R.id.tvOrderDate);
                    if (tvNum != null) tvNum.setText("Order #" + order.id);
                    if (tvBadge != null) tvBadge.setText(order.status);
                    if (tvPrice != null) tvPrice.setText("Rs. " + order.totalPrice);
                    if (tvDate != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                        tvDate.setText(sdf.format(new Date(order.createdAt)));
                    }
                },
                order -> {
                    Bundle bundle = new Bundle();
                    bundle.putLong("orderId", order.id);
                    Navigation.findNavController(view).navigate(R.id.orderDetailsFragment, bundle);
                }
            );
            rvRecentOrders.setAdapter(recentOrdersAdapter);
        }

        // 10. Help & Support banner click
        View btnHelpSupport = view.findViewById(R.id.btnHelpSupport);
        if (btnHelpSupport != null) {
            btnHelpSupport.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.infoFragment));
        }

        // Live Data Subscriptions
        catalogViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) categoriesAdapter.setItems(categories);
        });

        catalogViewModel.getActivePromotions().observe(getViewLifecycleOwner(), promos -> {
            if (promos != null) {
                offersAdapter.setItems(promos);
                if (promoPagerAdapter != null) promoPagerAdapter.setItems(promos);
            }
        });

        catalogViewModel.getPopularProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                int limit = Math.min(products.size(), 8);
                popularAdapter.setItems(products.subList(0, limit));
            }
        });

        catalogViewModel.getSampleDesigns().observe(getViewLifecycleOwner(), designs -> {
            if (designs != null && designGalleryAdapter != null) {
                designGalleryAdapter.setItems(designs);
            }
        });

        catalogViewModel.getRecentOrders().observe(getViewLifecycleOwner(), orders -> {
            if (orders != null && recentOrdersAdapter != null) {
                int limit = Math.min(orders.size(), 5);
                recentOrdersAdapter.setItems(orders.subList(0, limit));
            }
        });
    }
}
