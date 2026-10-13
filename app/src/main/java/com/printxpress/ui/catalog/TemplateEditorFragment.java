package com.printxpress.ui.catalog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.printxpress.R;
import com.printxpress.data.entity.SavedDesign;
import com.printxpress.data.repository.CommonRepository;
import com.printxpress.ui.profile.SavedDesignViewModel;
import com.printxpress.util.SessionManager;

public class TemplateEditorFragment extends Fragment {
    private SavedDesignViewModel savedDesignViewModel;
    private EditText etCompany, etFullName, etJobTitle, etPhone, etEmail;
    private String templateTitle = "Business Card";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_template_editor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            templateTitle = getArguments().getString("templateTitle", "Business Card");
        }

        etCompany = view.findViewById(R.id.etCompany);
        etFullName = view.findViewById(R.id.etFullName);
        etJobTitle = view.findViewById(R.id.etJobTitle);
        etPhone = view.findViewById(R.id.etPhone);
        etEmail = view.findViewById(R.id.etEmail);

        TextView prevCompanyName = view.findViewById(R.id.prevCompanyName);
        TextView prevFullName = view.findViewById(R.id.prevFullName);
        TextView prevJobTitle = view.findViewById(R.id.prevJobTitle);
        TextView prevPhone = view.findViewById(R.id.prevPhone);
        TextView prevEmail = view.findViewById(R.id.prevEmail);

        setupTextWatchers(etCompany, prevCompanyName);
        setupTextWatchers(etFullName, prevFullName);
        setupTextWatchers(etJobTitle, prevJobTitle);
        setupTextWatchers(etPhone, prevPhone);
        setupTextWatchers(etEmail, prevEmail);

        savedDesignViewModel = new ViewModelProvider(this).get(SavedDesignViewModel.class);

        MaterialButton btnSaveAndOrder = view.findViewById(R.id.btnSaveAndOrder);
        btnSaveAndOrder.setOnClickListener(this::saveAndProceed);
    }

    private void setupTextWatchers(EditText editText, TextView textView) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                textView.setText(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void saveAndProceed(View view) {
        String company = etCompany.getText().toString().trim();
        String name = etFullName.getText().toString().trim();
        String title = etJobTitle.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            Toast.makeText(getContext(), "Name and phone are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String designSummary = "Business Card: " + company + " | " + name + " (" + title + ") | " + phone + " | " + email;
        SessionManager sessionManager = new SessionManager(requireContext());
        long userId = sessionManager.getUserId();

        SavedDesign savedDesign = new SavedDesign(userId, templateTitle + " - " + name, "/templates/business_card_" + System.currentTimeMillis() + ".pdf", System.currentTimeMillis());

        savedDesignViewModel.insertDesign(savedDesign, new CommonRepository.InsertCallback() {
            @Override
            public void onSuccess(long id) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Design saved successfully!", Toast.LENGTH_SHORT).show();
                    Bundle bundle = new Bundle();
                    bundle.putLong("productId", 1); // Standard Business Card productId
                    bundle.putDouble("basePrice", 1500.0);
                    bundle.putInt("quantity", 100);
                    bundle.putString("designData", designSummary);
                    Navigation.findNavController(view).navigate(R.id.checkoutFragment, bundle);
                });
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to save design: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
