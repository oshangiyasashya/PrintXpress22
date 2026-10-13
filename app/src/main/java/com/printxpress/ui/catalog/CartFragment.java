package com.printxpress.ui.catalog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.printxpress.R;
import com.printxpress.data.entity.CartItem;
import com.printxpress.data.repository.CartRepository;
import com.printxpress.util.SessionManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.lifecycle.ViewModelProvider;

public class CartFragment extends Fragment {
    private CartViewModel cartViewModel;
    private RecyclerView rvCartItems;
    private View layoutEmptyCart, cardCheckoutBar;
    private TextView tvCartSummaryCount, tvCartSubtotalAmount;
    private MaterialButton btnCheckoutAll;
    private Button btnClearCart, btnExploreProductsEmpty;
    private CartItemAdapter adapter;
    private List<CartItem> currentCartItems = new ArrayList<>();
    private double currentTotal = 0.0;
    private int currentUnits = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        rvCartItems = view.findViewById(R.id.rvCartItems);
        layoutEmptyCart = view.findViewById(R.id.layoutEmptyCart);
        cardCheckoutBar = view.findViewById(R.id.cardCheckoutBar);
        tvCartSummaryCount = view.findViewById(R.id.tvCartSummaryCount);
        tvCartSubtotalAmount = view.findViewById(R.id.tvCartSubtotalAmount);
        btnCheckoutAll = view.findViewById(R.id.btnCheckoutAll);
        btnClearCart = view.findViewById(R.id.btnClearCart);
        btnExploreProductsEmpty = view.findViewById(R.id.btnExploreProductsEmpty);

        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartItemAdapter(new CartItemAdapter.CartItemListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQuantity) {
                cartViewModel.updateQuantity(item, newQuantity);
            }

            @Override
            public void onDelete(CartItem item) {
                new AlertDialog.Builder(requireContext())
                    .setTitle("Remove Item")
                    .setMessage("Remove \"" + item.productName + "\" from your cart?")
                    .setPositiveButton("Remove", (d, w) -> cartViewModel.deleteItem(item))
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });
        rvCartItems.setAdapter(adapter);

        cartViewModel.getActionMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            if (items != null && !items.isEmpty()) {
                currentCartItems = items;
                adapter.setList(items);
                rvCartItems.setVisibility(View.VISIBLE);
                cardCheckoutBar.setVisibility(View.VISIBLE);
                layoutEmptyCart.setVisibility(View.GONE);

                double total = 0.0;
                int totalUnits = 0;
                for (CartItem ci : items) {
                    total += (ci.basePrice * ci.quantity);
                    totalUnits += ci.quantity;
                }
                currentTotal = total;
                currentUnits = totalUnits;

                tvCartSummaryCount.setText("Subtotal (" + items.size() + " items, " + totalUnits + " units):");
                tvCartSubtotalAmount.setText(String.format(Locale.getDefault(), "Rs. %,.2f", total));
            } else {
                currentCartItems = new ArrayList<>();
                adapter.setList(new ArrayList<>());
                rvCartItems.setVisibility(View.GONE);
                cardCheckoutBar.setVisibility(View.GONE);
                layoutEmptyCart.setVisibility(View.VISIBLE);
                currentTotal = 0.0;
                currentUnits = 0;
            }
        });

        btnClearCart.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Clear Cart")
                .setMessage("Are you sure you want to remove all items from your cart?")
                .setPositiveButton("Clear All", (d, w) -> cartViewModel.clearCart())
                .setNegativeButton("Cancel", null)
                .show();
        });

        btnCheckoutAll.setOnClickListener(v -> {
            if (currentCartItems.isEmpty()) return;

            StringBuilder summary = new StringBuilder("Cart Order (" + currentCartItems.size() + " items):\n");
            for (CartItem ci : currentCartItems) {
                summary.append("• ").append(ci.productName).append(" x ").append(ci.quantity).append("\n");
            }

            Bundle bundle = new Bundle();
            bundle.putLong("productId", currentCartItems.get(0).productId);
            bundle.putDouble("basePrice", currentTotal / (currentUnits > 0 ? currentUnits : 1));
            bundle.putInt("quantity", currentUnits > 0 ? currentUnits : 1);
            bundle.putString("designData", summary.toString());
            bundle.putBoolean("isFromCart", true);
            if (!TextUtils.isEmpty(currentCartItems.get(0).artworkUrl)) {
                bundle.putString("previewImagePath", currentCartItems.get(0).artworkUrl);
            }
            Navigation.findNavController(v).navigate(R.id.checkoutFragment, bundle);
        });

        btnExploreProductsEmpty.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.productsFragment));
    }

    private static class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {
        public interface CartItemListener {
            void onQuantityChanged(CartItem item, int newQuantity);
            void onDelete(CartItem item);
        }

        private List<CartItem> list = new ArrayList<>();
        private final CartItemListener listener;

        public CartItemAdapter(CartItemListener listener) {
            this.listener = listener;
        }

        public void setList(List<CartItem> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CartItem item = list.get(position);
            holder.tvName.setText(item.productName);

            String specs = !TextUtils.isEmpty(item.customText) ? item.customText : (!TextUtils.isEmpty(item.selectedOptions) ? item.selectedOptions : "Custom Specifications");
            holder.tvDetails.setText(specs);
            holder.tvUnitPrice.setText(String.format(Locale.getDefault(), "Rs. %,.2f / unit", item.basePrice));
            holder.tvTotalPrice.setText(String.format(Locale.getDefault(), "Rs. %,.2f", item.basePrice * item.quantity));
            holder.tvQty.setText(String.valueOf(item.quantity));

            if (!TextUtils.isEmpty(item.artworkUrl)) {
                File file = new File(item.artworkUrl);
                if (file.exists()) {
                    Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                    if (bmp != null) holder.ivThumb.setImageBitmap(bmp);
                    else holder.ivThumb.setImageResource(R.drawable.bg_order_gradient);
                } else {
                    holder.ivThumb.setImageResource(R.drawable.bg_order_gradient);
                }
            } else {
                holder.ivThumb.setImageResource(R.drawable.bg_order_gradient);
            }

            int step = item.quantity >= 50 ? 25 : (item.quantity >= 10 ? 5 : 1);

            holder.btnMinus.setOnClickListener(v -> {
                int newQty = Math.max(1, item.quantity - step);
                listener.onQuantityChanged(item, newQty);
            });

            holder.btnPlus.setOnClickListener(v -> {
                int newQty = item.quantity + step;
                listener.onQuantityChanged(item, newQty);
            });

            holder.btnRemove.setOnClickListener(v -> listener.onDelete(item));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivThumb;
            TextView tvName, tvDetails, tvUnitPrice, tvTotalPrice, tvQty;
            MaterialButton btnMinus, btnPlus;
            ImageButton btnRemove;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivThumb = itemView.findViewById(R.id.ivCartItemImage);
                tvName = itemView.findViewById(R.id.tvCartItemName);
                tvDetails = itemView.findViewById(R.id.tvCartItemDetails);
                tvUnitPrice = itemView.findViewById(R.id.tvCartItemUnitPrice);
                tvTotalPrice = itemView.findViewById(R.id.tvCartItemTotalPrice);
                tvQty = itemView.findViewById(R.id.tvCartItemQty);
                btnMinus = itemView.findViewById(R.id.btnQtyMinus);
                btnPlus = itemView.findViewById(R.id.btnQtyPlus);
                btnRemove = itemView.findViewById(R.id.btnRemoveCartItem);
            }
        }
    }
}
