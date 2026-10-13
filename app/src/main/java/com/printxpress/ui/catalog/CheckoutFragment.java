package com.printxpress.ui.catalog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.printxpress.R;
import com.printxpress.data.entity.Address;
import com.printxpress.data.entity.Order;
import com.printxpress.data.entity.OrderItem;
import com.printxpress.data.entity.PaymentMethod;
import com.printxpress.util.ImageLoader;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CheckoutFragment extends Fragment {
    private CheckoutViewModel viewModel;

    private long productId = 1;
    private double basePrice = 2500.0;
    private int quantity = 100;
    private String designData = "Standard Print";
    private String previewImagePath = "";

    private ImageView ivPreview;
    private TextView tvItemTitle, tvItemDetails, tvItemQuantity;
    private RadioGroup rgDelivery, rgPaymentMethod;
    private RadioButton rbDelivery, rbPickup, rbPaymentCard, rbPaymentCod, rbPaymentPickup;
    private LinearLayout layoutDeliveryAddress;
    private View cardPickupInfo;
    private Spinner spinnerSavedAddresses, spinnerSavedCards;
    private TextInputLayout tilManualAddress;
    private TextInputEditText etManualAddress, etSpecialInstructions;
    private TextView tvPriceSubtotal, tvPriceDelivery, tvPriceTotal;
    private Button btnPlaceOrder;

    private List<Address> savedAddressesList = new ArrayList<>();
    private List<PaymentMethod> savedCardsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);

        if (getArguments() != null) {
            productId = getArguments().getLong("productId", 1);
            basePrice = getArguments().getDouble("basePrice", 2500.0);
            quantity = getArguments().getInt("quantity", 100);
            designData = getArguments().getString("designData", "Standard Print");
            previewImagePath = getArguments().getString("previewImagePath", "");
        }

        initViews(view);
        setupItemDisplay();
        setupDeliveryToggle();
        setupPaymentToggle();
        observeViewModel();
        updatePricing(true);

        view.findViewById(R.id.btnAddNewAddressCheckout).setOnClickListener(v -> showAddNewAddressDialog());
        btnPlaceOrder.setOnClickListener(v -> handlePlaceOrder());
    }

    private void initViews(View view) {
        ivPreview = view.findViewById(R.id.ivCheckoutDesignPreview);
        tvItemTitle = view.findViewById(R.id.tvCheckoutItemTitle);
        tvItemDetails = view.findViewById(R.id.tvCheckoutItemDetails);
        tvItemQuantity = view.findViewById(R.id.tvCheckoutQuantity);

        rgDelivery = view.findViewById(R.id.rgDelivery);
        rbDelivery = view.findViewById(R.id.rbDelivery);
        rbPickup = view.findViewById(R.id.rbPickup);

        layoutDeliveryAddress = view.findViewById(R.id.layoutDeliveryAddress);
        cardPickupInfo = view.findViewById(R.id.cardPickupInfo);
        spinnerSavedAddresses = view.findViewById(R.id.spinnerSavedAddresses);
        tilManualAddress = view.findViewById(R.id.tilManualAddress);
        etManualAddress = view.findViewById(R.id.etManualAddress);

        rgPaymentMethod = view.findViewById(R.id.rgPaymentMethod);
        rbPaymentCard = view.findViewById(R.id.rbPaymentCard);
        rbPaymentCod = view.findViewById(R.id.rbPaymentCod);
        rbPaymentPickup = view.findViewById(R.id.rbPaymentPickup);
        spinnerSavedCards = view.findViewById(R.id.spinnerSavedCards);

        etSpecialInstructions = view.findViewById(R.id.etSpecialInstructions);
        tvPriceSubtotal = view.findViewById(R.id.tvPriceSubtotal);
        tvPriceDelivery = view.findViewById(R.id.tvPriceDelivery);
        tvPriceTotal = view.findViewById(R.id.tvPriceTotal);
        btnPlaceOrder = view.findViewById(R.id.btnPlaceOrder);
    }

    private void setupItemDisplay() {
        if (!TextUtils.isEmpty(previewImagePath)) {
            ImageLoader.getInstance().load(previewImagePath, ivPreview);
        }
        tvItemTitle.setText("Print Product #" + productId);
        tvItemDetails.setText(designData);
        tvItemQuantity.setText("Order Quantity: " + quantity + " units");
    }

    private void setupDeliveryToggle() {
        rgDelivery.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isDelivery = checkedId == R.id.rbDelivery;
            if (isDelivery) {
                layoutDeliveryAddress.setVisibility(View.VISIBLE);
                cardPickupInfo.setVisibility(View.GONE);
                rbPaymentPickup.setVisibility(View.GONE);
                if (rbPaymentPickup.isChecked()) {
                    rbPaymentCard.setChecked(true);
                }
            } else {
                layoutDeliveryAddress.setVisibility(View.GONE);
                cardPickupInfo.setVisibility(View.VISIBLE);
                rbPaymentPickup.setVisibility(View.VISIBLE);
            }
            updatePricing(isDelivery);
        });
    }

    private void setupPaymentToggle() {
        rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPaymentCard) {
                spinnerSavedCards.setVisibility(View.VISIBLE);
            } else {
                spinnerSavedCards.setVisibility(View.GONE);
            }
        });
    }

    private void updatePricing(boolean isDelivery) {
        double subtotal = basePrice * quantity;
        double deliveryFee = isDelivery ? 350.0 : 0.0;
        double total = subtotal + deliveryFee;

        tvPriceSubtotal.setText(String.format(Locale.getDefault(), "Rs. %,.2f", subtotal));
        tvPriceDelivery.setText(isDelivery ? String.format(Locale.getDefault(), "Rs. %,.2f", deliveryFee) : "FREE (Pickup)");
        tvPriceTotal.setText(String.format(Locale.getDefault(), "Rs. %,.2f", total));
    }

    private void observeViewModel() {
        viewModel.getSavedAddresses().observe(getViewLifecycleOwner(), addresses -> {
            savedAddressesList = addresses != null ? addresses : new ArrayList<>();
            List<String> displayList = new ArrayList<>();

            if (savedAddressesList.isEmpty()) {
                displayList.add("No saved addresses - Enter below");
                tilManualAddress.setVisibility(View.VISIBLE);
            } else {
                tilManualAddress.setVisibility(View.GONE);
                int defaultIndex = 0;
                for (int i = 0; i < savedAddressesList.size(); i++) {
                    Address a = savedAddressesList.get(i);
                    String label = a.label + ": " + a.line1 + ", " + a.city;
                    if (a.isDefault) {
                        label += " (Default)";
                        defaultIndex = i;
                    }
                    displayList.add(label);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, displayList);
                spinnerSavedAddresses.setAdapter(adapter);
                spinnerSavedAddresses.setSelection(defaultIndex);
            }
        });

        viewModel.getSavedCards().observe(getViewLifecycleOwner(), cards -> {
            savedCardsList = cards != null ? cards : new ArrayList<>();
            List<String> cardLabels = new ArrayList<>();

            if (savedCardsList.isEmpty()) {
                cardLabels.add("Default Corporate Visa (•••• 4242)");
            } else {
                int defaultIndex = 0;
                for (int i = 0; i < savedCardsList.size(); i++) {
                    PaymentMethod c = savedCardsList.get(i);
                    String label = c.cardType + " (" + c.cardNumberMasked + ")";
                    if (c.isDefault) {
                        label += " - Default";
                        defaultIndex = i;
                    }
                    cardLabels.add(label);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, cardLabels);
                spinnerSavedCards.setAdapter(adapter);
                spinnerSavedCards.setSelection(defaultIndex);
            }
        });

        viewModel.getIsPlacingOrder().observe(getViewLifecycleOwner(), isPlacing -> {
            btnPlaceOrder.setEnabled(!Boolean.TRUE.equals(isPlacing));
            btnPlaceOrder.setText(Boolean.TRUE.equals(isPlacing) ? "Placing Order..." : "Confirm & Place Order");
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getOrderPlacedId().observe(getViewLifecycleOwner(), orderId -> {
            if (orderId != null && orderId > 0) {
                new AlertDialog.Builder(requireContext())
                    .setTitle("Order Placed Successfully! 🎉")
                    .setMessage("Your print order #" + orderId + " has been verified and sent to production.\n\nEstimated delivery within 48 hours.")
                    .setPositiveButton("View My Orders", (dialog, which) -> {
                        Navigation.findNavController(requireView()).navigate(R.id.ordersFragment);
                    })
                    .setCancelable(false)
                    .show();
            }
        });
    }

    private void showAddNewAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add New Shipping Address");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etLabel = new EditText(requireContext());
        etLabel.setHint("Address Label (e.g. Home, Office)");
        layout.addView(etLabel);

        final EditText etStreet = new EditText(requireContext());
        etStreet.setHint("Street Address & Building");
        layout.addView(etStreet);

        final EditText etCity = new EditText(requireContext());
        etCity.setHint("City (e.g. Colombo, Kandy)");
        layout.addView(etCity);

        final EditText etPostal = new EditText(requireContext());
        etPostal.setHint("Postal Code (5 digits)");
        etPostal.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(etPostal);

        builder.setView(layout);

        builder.setPositiveButton("Save Address", (dialog, which) -> {
            String label = etLabel.getText().toString().trim();
            String street = etStreet.getText().toString().trim();
            String city = etCity.getText().toString().trim();
            String postal = etPostal.getText().toString().trim();

            if (TextUtils.isEmpty(label) || TextUtils.isEmpty(street) || TextUtils.isEmpty(city)) {
                Toast.makeText(getContext(), "Please fill in all address details", Toast.LENGTH_SHORT).show();
                return;
            }

            long userId = viewModel.getCurrentUserId();
            Address address = new Address(userId, label, street, city, "Colombo", postal, true);
            viewModel.addAddress(address);
            Toast.makeText(getContext(), "Address saved!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void handlePlaceOrder() {
        boolean isDelivery = rgDelivery.getCheckedRadioButtonId() == R.id.rbDelivery;
        Long selectedAddressId = null;
        String shippingAddressText = "";

        if (isDelivery) {
            if (!savedAddressesList.isEmpty() && spinnerSavedAddresses.getSelectedItemPosition() >= 0 && spinnerSavedAddresses.getSelectedItemPosition() < savedAddressesList.size()) {
                Address chosen = savedAddressesList.get(spinnerSavedAddresses.getSelectedItemPosition());
                selectedAddressId = chosen.id;
                shippingAddressText = chosen.line1 + ", " + chosen.city + ", " + chosen.postalCode;
            } else {
                shippingAddressText = etManualAddress.getText() != null ? etManualAddress.getText().toString().trim() : "";
                if (TextUtils.isEmpty(shippingAddressText)) {
                    etManualAddress.setError("Shipping address is required for delivery");
                    Toast.makeText(getContext(), "Please provide a shipping address", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } else {
            shippingAddressText = "PrintXpress Flagship Studio, Colombo 03";
        }

        String paymentMethod = "CARD";
        String paymentStatus = "PAID";
        int checkedPaymentId = rgPaymentMethod.getCheckedRadioButtonId();

        if (checkedPaymentId == R.id.rbPaymentCod) {
            paymentMethod = "COD";
            paymentStatus = "PENDING";
        } else if (checkedPaymentId == R.id.rbPaymentPickup) {
            paymentMethod = "PICKUP";
            paymentStatus = "PENDING";
        }

        double subtotal = basePrice * quantity;
        double deliveryFee = isDelivery ? 350.0 : 0.0;
        double total = subtotal + deliveryFee;
        String notes = etSpecialInstructions.getText() != null ? etSpecialInstructions.getText().toString().trim() : "";

        long userId = viewModel.getCurrentUserId();
        long now = System.currentTimeMillis();

        Order order = new Order(userId, "PENDING", isDelivery ? "DELIVERY" : "PICKUP", selectedAddressId, now + 172800000L, total, notes, now);
        order.shippingAddressText = shippingAddressText;
        order.paymentMethod = paymentMethod;
        order.paymentStatus = paymentStatus;
        order.trackingNumber = "PX-" + (100000 + (now % 900000));

        OrderItem item = new OrderItem(0, productId, quantity, "", designData, previewImagePath, basePrice);

        boolean isFromCart = getArguments() != null && getArguments().getBoolean("isFromCart", false);
        viewModel.placeOrder(order, Collections.singletonList(item), isFromCart);
    }
}
