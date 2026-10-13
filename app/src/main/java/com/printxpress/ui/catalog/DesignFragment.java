package com.printxpress.ui.catalog;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.printxpress.R;

public class DesignFragment extends Fragment {
    private long productId;
    private double basePrice;
    private String mockedFilePath = "";
    private TextView tvFileName;
    private ActivityResultLauncher<String> filePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                mockedFilePath = uri.toString();
                if (tvFileName != null) {
                    tvFileName.setText("Selected: " + (uri.getLastPathSegment() != null ? uri.getLastPathSegment() : "artwork.png"));
                }
                Toast.makeText(getContext(), "Custom artwork file attached!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_design, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getLong("productId", -1);
            basePrice = getArguments().getDouble("basePrice", 0.0);
        }

        RadioGroup rgDesignType = view.findViewById(R.id.rgDesignType);
        LinearLayout layoutUpload = view.findViewById(R.id.layoutUpload);
        LinearLayout layoutText = view.findViewById(R.id.layoutText);
        Button btnUploadFile = view.findViewById(R.id.btnUploadFile);
        tvFileName = view.findViewById(R.id.tvFileName);
        EditText etCustomText = view.findViewById(R.id.etCustomText);
        EditText etQuantity = view.findViewById(R.id.etQuantity);
        Button btnProceed = view.findViewById(R.id.btnProceed);

        rgDesignType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbUpload) {
                layoutUpload.setVisibility(View.VISIBLE);
                layoutText.setVisibility(View.GONE);
            } else {
                layoutUpload.setVisibility(View.GONE);
                layoutText.setVisibility(View.VISIBLE);
            }
        });

        btnUploadFile.setOnClickListener(v -> filePickerLauncher.launch("image/*"));

        btnProceed.setOnClickListener(v -> {
            String qtyStr = etQuantity.getText().toString();
            if (TextUtils.isEmpty(qtyStr)) {
                etQuantity.setError("Required");
                return;
            }
            int quantity = Integer.parseInt(qtyStr);
            if (quantity <= 0) {
                etQuantity.setError("Must be > 0");
                return;
            }

            String designData = "";
            if (rgDesignType.getCheckedRadioButtonId() == R.id.rbUpload) {
                if (TextUtils.isEmpty(mockedFilePath)) {
                    Toast.makeText(getContext(), "Please select an artwork file", Toast.LENGTH_SHORT).show();
                    return;
                }
                designData = "Custom Upload: " + mockedFilePath;
            } else {
                String text = etCustomText.getText().toString().trim();
                if (TextUtils.isEmpty(text)) {
                    etCustomText.setError("Required");
                    return;
                }
                designData = "Print Text: " + text;
            }

            Bundle bundle = new Bundle();
            bundle.putLong("productId", productId);
            bundle.putDouble("basePrice", basePrice);
            bundle.putInt("quantity", quantity);
            bundle.putString("designData", designData);
            bundle.putString("previewImagePath", mockedFilePath);
            Navigation.findNavController(v).navigate(R.id.checkoutFragment, bundle);
        });
    }
}
