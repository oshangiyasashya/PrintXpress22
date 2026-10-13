package com.printxpress.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.printxpress.R;
import com.printxpress.data.entity.Address;
import com.printxpress.util.GenericAdapter;

public class AddressesFragment extends Fragment {
    private AddressViewModel viewModel;
    private GenericAdapter<Address> adapter;
    private TextView tvEmptyAddresses;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_addresses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddressViewModel.class);

        tvEmptyAddresses = view.findViewById(R.id.tvEmptyAddresses);
        MaterialButton btnAddAddress = view.findViewById(R.id.btnAddAddress);
        if (btnAddAddress != null) {
            btnAddAddress.setOnClickListener(v -> showAddAddressDialog());
        }

        RecyclerView rv = view.findViewById(R.id.rvAddresses);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new GenericAdapter<>(R.layout.item_address_card,
            (v, address) -> {
                TextView tvLabel = v.findViewById(R.id.tvAddressLabel);
                TextView tvLines = v.findViewById(R.id.tvAddressLines);
                TextView tvDefaultBadge = v.findViewById(R.id.tvDefaultBadge);
                Button btnSetDefault = v.findViewById(R.id.btnSetDefaultAddress);
                Button btnDelete = v.findViewById(R.id.btnDeleteAddress);

                if (tvLabel != null) tvLabel.setText(address.label);
                if (tvLines != null) {
                    String fullAddress = address.line1;
                    if (!TextUtils.isEmpty(address.city)) fullAddress += ", " + address.city;
                    if (!TextUtils.isEmpty(address.postalCode)) fullAddress += " (" + address.postalCode + ")";
                    tvLines.setText(fullAddress);
                }

                if (tvDefaultBadge != null) {
                    tvDefaultBadge.setVisibility(address.isDefault ? View.VISIBLE : View.GONE);
                }

                if (btnSetDefault != null) {
                    btnSetDefault.setVisibility(address.isDefault ? View.GONE : View.VISIBLE);
                    btnSetDefault.setOnClickListener(v1 -> viewModel.setDefaultAddress(address.id));
                }

                if (btnDelete != null) {
                    btnDelete.setOnClickListener(v1 -> {
                        new AlertDialog.Builder(requireContext())
                            .setTitle("Delete Address")
                            .setMessage("Are you sure you want to delete '" + address.label + "'?")
                            .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteAddress(address.id))
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .show();
                    });
                }
            },
            address -> {}
        );
        rv.setAdapter(adapter);

        viewModel.getAddresses().observe(getViewLifecycleOwner(), addresses -> {
            if (addresses != null) {
                adapter.setItems(addresses);
                if (tvEmptyAddresses != null) {
                    tvEmptyAddresses.setVisibility(addresses.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
        });

        viewModel.getActionMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Delivery Address");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        final EditText etLabel = new EditText(requireContext());
        etLabel.setHint("Address Label (e.g. Home, Office, Studio)");
        layout.addView(etLabel);

        final EditText etLine1 = new EditText(requireContext());
        etLine1.setHint("Street Address / Line 1");
        layout.addView(etLine1);

        final EditText etCity = new EditText(requireContext());
        etCity.setHint("City / District");
        layout.addView(etCity);

        final EditText etPostal = new EditText(requireContext());
        etPostal.setHint("Postal Code (e.g. 00300)");
        layout.addView(etPostal);

        final CheckBox cbDefault = new CheckBox(requireContext());
        cbDefault.setText("Set as default shipping address");
        layout.addView(cbDefault);

        builder.setView(layout);

        builder.setPositiveButton("Save Address", (dialog, which) -> {
            String label = etLabel.getText().toString().trim();
            String line1 = etLine1.getText().toString().trim();
            String city = etCity.getText().toString().trim();
            String postal = etPostal.getText().toString().trim();
            boolean isDef = cbDefault.isChecked();

            if (TextUtils.isEmpty(label)) label = "Shipping Address";
            if (TextUtils.isEmpty(line1)) {
                Toast.makeText(getContext(), "Street address is required", Toast.LENGTH_SHORT).show();
                return;
            }

            long userId = viewModel.getCurrentUserId();
            Address newAddress = new Address(userId, label, line1, "", city, postal, isDef);
            viewModel.addAddress(newAddress);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
