package com.printxpress.ui.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.printxpress.R;
import com.printxpress.data.entity.PaymentMethod;
import com.printxpress.data.entity.User;
import com.printxpress.util.ImageLoader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    private ProfileViewModel viewModel;
    private User currentUser;
    private TextView tvUserName, tvUserEmail, tvUserPhone;
    private ImageView ivProfileAvatar;
    private ActivityResultLauncher<String> dpPickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dpPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null && currentUser != null) {
                saveProfileImage(uri);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvUserPhone = view.findViewById(R.id.tvUserPhone);
        ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar);
        View tvChangeDpPrompt = view.findViewById(R.id.tvChangeDpPrompt);

        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        Button btnChangePassword = view.findViewById(R.id.btnChangePassword);
        Button btnAddresses = view.findViewById(R.id.btnAddresses);
        Button btnSavedDesigns = view.findViewById(R.id.btnSavedDesigns);
        Button btnNotifications = view.findViewById(R.id.btnNotifications);
        Button btnPaymentMethods = view.findViewById(R.id.btnPaymentMethods);
        Button btnAboutUs = view.findViewById(R.id.btnAboutUs);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        View.OnClickListener pickDpListener = v -> showPhotoOptionsDialog();
        if (ivProfileAvatar != null) ivProfileAvatar.setOnClickListener(pickDpListener);
        if (tvChangeDpPrompt != null) tvChangeDpPrompt.setOnClickListener(pickDpListener);

        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;
                if (tvUserName != null) tvUserName.setText(user.fullName);
                if (tvUserEmail != null) tvUserEmail.setText("Email: " + user.email + " (Verified ✓)");
                if (tvUserPhone != null) tvUserPhone.setText("Phone: " + user.phone);

                if (user.profileImageUri != null && !user.profileImageUri.isEmpty() && ivProfileAvatar != null) {
                    ImageLoader.getInstance().load(user.profileImageUri, ivProfileAvatar);
                } else if (ivProfileAvatar != null) {
                    ivProfileAvatar.setImageResource(R.drawable.ic_profile);
                }
            }
        });

        viewModel.getActionMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLogoutTrigger().observe(getViewLifecycleOwner(), loggedOut -> {
            if (Boolean.TRUE.equals(loggedOut)) {
                Navigation.findNavController(requireView()).navigate(R.id.loginFragment);
            }
        });

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        }

        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }

        btnAddresses.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.addressesFragment));
        btnSavedDesigns.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.savedDesignsFragment));
        btnNotifications.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.notificationsFragment));
        btnPaymentMethods.setOnClickListener(v -> showPaymentMethodsDialog());

        btnAboutUs.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("About PrintXpress")
                .setMessage("PrintXpress is Sri Lanka's premier digital printing and custom merchandise studio.\n\nStudio Location: Colombo 03, Sri Lanka\nHotline: +94 11 2345678\nEmail: support@printxpress.lk\nHours: Mon - Sat: 8:30 AM - 6:30 PM")
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
        });

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out of PrintXpress?")
                .setPositiveButton("Logout", (dialog, which) -> viewModel.logout())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
        });
    }

    private void showPhotoOptionsDialog() {
        if (currentUser == null) return;
        String[] options;
        if (!TextUtils.isEmpty(currentUser.profileImageUri)) {
            options = new String[]{"Upload / Change Photo", "Remove Photo"};
        } else {
            options = new String[]{"Upload Photo"};
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Profile Photo Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        dpPickerLauncher.launch("image/*");
                    } else if (which == 1) {
                        viewModel.removeAvatar(currentUser);
                        if (ivProfileAvatar != null) {
                            ivProfileAvatar.setImageResource(R.drawable.ic_profile);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveProfileImage(Uri uri) {
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            if (is == null) return;
            File avatarFile = new File(requireContext().getFilesDir(), "avatar_" + currentUser.id + ".jpg");
            FileOutputStream fos = new FileOutputStream(avatarFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.flush();
            fos.close();
            is.close();

            viewModel.updateAvatarPath(currentUser, avatarFile.getAbsolutePath());
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to save profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showChangePasswordDialog() {
        if (currentUser == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Change Password");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etCurrentPass = new EditText(requireContext());
        etCurrentPass.setHint("Current Password");
        etCurrentPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etCurrentPass);

        final EditText etNewPass = new EditText(requireContext());
        etNewPass.setHint("New Password (min 6 characters)");
        etNewPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNewPass);

        final EditText etConfirmPass = new EditText(requireContext());
        etConfirmPass.setHint("Confirm New Password");
        etConfirmPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etConfirmPass);

        builder.setView(layout);

        builder.setPositiveButton("Update Password", (dialog, which) -> {
            String currentPass = etCurrentPass.getText().toString();
            String newPass = etNewPass.getText().toString();
            String confirmPass = etConfirmPass.getText().toString();

            viewModel.changePassword(currentUser, currentPass, newPass, confirmPass);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showEditProfileDialog() {
        if (currentUser == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Profile");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText inputName = new EditText(requireContext());
        inputName.setHint("Full Name");
        inputName.setText(currentUser.fullName);
        layout.addView(inputName);

        final EditText inputPhone = new EditText(requireContext());
        inputPhone.setHint("Phone (e.g. 0712345678)");
        inputPhone.setText(currentUser.phone);
        inputPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(inputPhone);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String phone = inputPhone.getText().toString().trim();
            viewModel.updateProfile(currentUser, name, phone);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showPaymentMethodsDialog() {
        if (currentUser == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_payment_methods, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        RecyclerView rv = dialogView.findViewById(R.id.rvPaymentMethodsDialog);
        TextView tvEmpty = dialogView.findViewById(R.id.tvEmptyPaymentMethods);
        Button btnAdd = dialogView.findViewById(R.id.btnAddNewCard);
        Button btnClose = dialogView.findViewById(R.id.btnClosePaymentDialog);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        PaymentAdapter adapter = new PaymentAdapter(new PaymentAdapter.PaymentListener() {
            @Override
            public void onSetDefault(PaymentMethod card) {
                viewModel.setDefaultPayment(card.id);
            }

            @Override
            public void onDelete(PaymentMethod card) {
                new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Card")
                    .setMessage("Remove " + card.cardNumberMasked + "?")
                    .setPositiveButton("Delete", (d, w) -> viewModel.deletePayment(card))
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });
        rv.setAdapter(adapter);

        viewModel.getPaymentMethods().observe(getViewLifecycleOwner(), cards -> {
            if (cards != null && !cards.isEmpty()) {
                adapter.setList(cards);
                tvEmpty.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
            } else {
                adapter.setList(new ArrayList<>());
                tvEmpty.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
            }
        });

        btnAdd.setOnClickListener(v -> showAddCardDialog());
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showAddCardDialog() {
        if (currentUser == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_payment_method, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        TextInputEditText etName = view.findViewById(R.id.etCardHolderName);
        TextInputEditText etNumber = view.findViewById(R.id.etCardNumber);
        TextInputEditText etExpiry = view.findViewById(R.id.etCardExpiry);
        TextInputEditText etCvv = view.findViewById(R.id.etCardCvv);
        CheckBox cbDefault = view.findViewById(R.id.cbSetDefaultCard);
        Button btnSave = view.findViewById(R.id.btnSaveCard);
        Button btnCancel = view.findViewById(R.id.btnCancelAddCard);

        btnSave.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String num = etNumber.getText() != null ? etNumber.getText().toString().trim().replaceAll("\\s+", "") : "";
            String expiry = etExpiry.getText() != null ? etExpiry.getText().toString().trim() : "";
            String cvv = etCvv.getText() != null ? etCvv.getText().toString().trim() : "";

            if (TextUtils.isEmpty(name)) {
                etName.setError("Name is required");
                return;
            }
            if (num.length() != 16 || !num.matches("^[0-9]+$")) {
                etNumber.setError("Enter a valid 16-digit card number");
                return;
            }
            if (!expiry.matches("^(0[1-9]|1[0-2])\\/[0-9]{2}$")) {
                etExpiry.setError("Format must be MM/YY");
                return;
            }
            if (cvv.length() < 3 || cvv.length() > 4) {
                etCvv.setError("CVV must be 3-4 digits");
                return;
            }

            String cardType = "VISA";
            if (num.startsWith("5")) cardType = "MASTERCARD";
            else if (num.startsWith("3")) cardType = "AMEX";

            String masked = "•••• •••• •••• " + num.substring(12);

            PaymentMethod card = new PaymentMethod(currentUser.id, name, masked, cardType, expiry, cbDefault.isChecked());
            viewModel.addPayment(card);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private static class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {
        public interface PaymentListener {
            void onSetDefault(PaymentMethod card);
            void onDelete(PaymentMethod card);
        }

        private List<PaymentMethod> list = new ArrayList<>();
        private final PaymentListener listener;

        public PaymentAdapter(PaymentListener listener) {
            this.listener = listener;
        }

        public void setList(List<PaymentMethod> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_method_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PaymentMethod card = list.get(position);
            holder.tvCardTypeBadge.setText(card.cardType);
            holder.tvCardMaskedNumber.setText(card.cardNumberMasked);
            holder.tvCardHolderName.setText(card.cardHolderName);
            holder.tvCardExpiry.setText("Exp: " + card.expiryDate);

            if (card.isDefault) {
                holder.tvDefaultCardBadge.setVisibility(View.VISIBLE);
                holder.btnSetDefaultPayment.setVisibility(View.GONE);
            } else {
                holder.tvDefaultCardBadge.setVisibility(View.GONE);
                holder.btnSetDefaultPayment.setVisibility(View.VISIBLE);
                holder.btnSetDefaultPayment.setOnClickListener(v -> listener.onSetDefault(card));
            }

            holder.btnDeletePayment.setOnClickListener(v -> listener.onDelete(card));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCardTypeBadge, tvCardMaskedNumber, tvDefaultCardBadge, tvCardHolderName, tvCardExpiry;
            Button btnSetDefaultPayment, btnDeletePayment;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCardTypeBadge = itemView.findViewById(R.id.tvCardTypeBadge);
                tvCardMaskedNumber = itemView.findViewById(R.id.tvCardMaskedNumber);
                tvDefaultCardBadge = itemView.findViewById(R.id.tvDefaultCardBadge);
                tvCardHolderName = itemView.findViewById(R.id.tvCardHolderName);
                tvCardExpiry = itemView.findViewById(R.id.tvCardExpiry);
                btnSetDefaultPayment = itemView.findViewById(R.id.btnSetDefaultPayment);
                btnDeletePayment = itemView.findViewById(R.id.btnDeletePayment);
            }
        }
    }
}
