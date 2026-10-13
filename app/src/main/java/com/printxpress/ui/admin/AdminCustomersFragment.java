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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.printxpress.R;
import com.printxpress.data.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminCustomersFragment extends Fragment {

    private AdminViewModel viewModel;
    private RecyclerView rvCustomers;
    private EditText etCustomerSearch;
    private ChipGroup chipGroupFilter;

    private List<User> allUsersList = new ArrayList<>();
    private String currentFilter = "ALL";
    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_customers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        rvCustomers = view.findViewById(R.id.rvAdminCustomers);
        etCustomerSearch = view.findViewById(R.id.etCustomerSearch);
        chipGroupFilter = view.findViewById(R.id.chipGroupCustomerFilter);
        FloatingActionButton fabAddStaff = view.findViewById(R.id.fabAddStaff);

        rvCustomers.setLayoutManager(new LinearLayoutManager(getContext()));

        fabAddStaff.setOnClickListener(v -> showAddStaffDialog());

        etCustomerSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim().toLowerCase(Locale.ROOT);
                filterAndDisplayUsers();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipCustomerActive) currentFilter = "ACTIVE";
            else if (checkedId == R.id.chipCustomerBlocked) currentFilter = "BLOCKED";
            else if (checkedId == R.id.chipStaffOnly) currentFilter = "STAFF";
            else currentFilter = "ALL";

            filterAndDisplayUsers();
        });

        viewModel.getAllUsers().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                allUsersList = users;
                filterAndDisplayUsers();
            }
        });
    }

    private void filterAndDisplayUsers() {
        List<User> filtered = new ArrayList<>();
        for (User u : allUsersList) {
            boolean matchesFilter = "ALL".equals(currentFilter) ||
                    ("ACTIVE".equals(currentFilter) && !u.isBlocked) ||
                    ("BLOCKED".equals(currentFilter) && u.isBlocked) ||
                    ("STAFF".equals(currentFilter) && ("STAFF".equalsIgnoreCase(u.role) || "SUPER_ADMIN".equalsIgnoreCase(u.role)));

            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    (u.fullName != null && u.fullName.toLowerCase(Locale.ROOT).contains(currentSearchQuery)) ||
                    (u.email != null && u.email.toLowerCase(Locale.ROOT).contains(currentSearchQuery)) ||
                    (u.phone != null && u.phone.contains(currentSearchQuery));

            if (matchesFilter && matchesSearch) {
                filtered.add(u);
            }
        }
        rvCustomers.setAdapter(new CustomersAdapter(filtered));
    }

    private void showAddStaffDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Create Staff / Admin Account");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText inputName = new EditText(getContext());
        inputName.setHint("Full Name");
        layout.addView(inputName);

        final EditText inputEmail = new EditText(getContext());
        inputEmail.setHint("Email Address");
        inputEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(inputEmail);

        final EditText inputPhone = new EditText(getContext());
        inputPhone.setHint("Phone (e.g. 0771234567)");
        inputPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(inputPhone);

        final EditText inputPass = new EditText(getContext());
        inputPass.setHint("Temporary Password");
        inputPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputPass);

        final Spinner spinnerRole = new Spinner(getContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"STAFF", "SUPER_ADMIN"});
        spinnerRole.setAdapter(adapter);
        layout.addView(spinnerRole);

        builder.setView(layout);

        builder.setPositiveButton("Create Account", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String phone = inputPhone.getText().toString().trim();
            String pass = inputPass.getText().toString().trim();
            String role = spinnerRole.getSelectedItem().toString();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
                Toast.makeText(getContext(), "Please fill in required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.createAdminAccount(name, email, phone, pass, role);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private class CustomersAdapter extends RecyclerView.Adapter<CustomersAdapter.ViewHolder> {
        private final List<User> items;

        public CustomersAdapter(List<User> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_customer_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User user = items.get(position);
            holder.tvName.setText(user.fullName != null ? user.fullName : "User #" + user.id);
            holder.tvEmailPhone.setText((user.email != null ? user.email : "") + " • " + (user.phone != null ? user.phone : ""));
            holder.tvRoleBadge.setText(user.role != null ? user.role : "CUSTOMER");

            holder.btnBlock.setText(user.isBlocked ? "Unblock" : "Block");
            holder.btnBlock.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                    .setTitle(user.isBlocked ? "Unblock User" : "Block User")
                    .setMessage("Are you sure you want to " + (user.isBlocked ? "unblock" : "block") + " " + user.fullName + "?")
                    .setPositiveButton("Confirm", (d, w) -> viewModel.setUserBlocked(user.id, !user.isBlocked))
                    .setNegativeButton("Cancel", null)
                    .show();
            });

            holder.btnReset.setOnClickListener(v -> {
                final EditText etNewPass = new EditText(getContext());
                etNewPass.setHint("Enter new password");
                new AlertDialog.Builder(requireContext())
                    .setTitle("Reset Password for " + user.fullName)
                    .setView(etNewPass)
                    .setPositiveButton("Reset", (d, w) -> {
                        String pass = etNewPass.getText().toString().trim();
                        if (!pass.isEmpty()) viewModel.resetUserPassword(user.id, pass);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            });

            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Customer")
                    .setMessage("Permanently remove " + user.fullName + " from active records?")
                    .setPositiveButton("Delete", (d, w) -> viewModel.softDeleteUser(user.id))
                    .setNegativeButton("Cancel", null)
                    .show();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvEmailPhone, tvRoleBadge;
            MaterialButton btnBlock, btnReset, btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvCustomerName);
                tvEmailPhone = itemView.findViewById(R.id.tvCustomerEmailPhone);
                tvRoleBadge = itemView.findViewById(R.id.tvUserRoleBadge);
                btnBlock = itemView.findViewById(R.id.btnBlockUnblock);
                btnReset = itemView.findViewById(R.id.btnResetPassword);
                btnDelete = itemView.findViewById(R.id.btnDeleteUser);
            }
        }
    }
}
