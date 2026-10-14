package com.printxpress.ui.catalog;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import java.io.File;
import java.io.FileOutputStream;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.printxpress.R;
import com.printxpress.data.entity.SavedDesign;
import com.printxpress.data.repository.CommonRepository;
import com.printxpress.ui.colorpicker.ColorPickerBottomSheet;
import com.printxpress.ui.profile.SavedDesignViewModel;
import com.printxpress.util.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class UniversalTemplateEditorFragment extends Fragment {
    private SavedDesignViewModel savedDesignViewModel;
    private CardView cardCanvasPreview;
    private ViewGroup canvasRelativeLayout;
    private LinearLayout containerDynamicFields;
    private ImageView prevLogo;
    private View selectedView = null;
    private View centerGuideline;
    private LinearLayout styleInspectorPanel, floatingToolbar;
    private TextView tvInspectorTitle, tvTemplateSectionTitle;
    private Slider materialSliderFontSize;
    private boolean isBold = false, isItalic = false, isUnderline = false;

    private String templateTitle = "Custom Design Template";
    private long categoryId = 1;
    private CategoryConfig categoryConfig;
    private TemplateLayout templateLayout;
    private ActivityResultLauncher<String> imagePickerLauncher, bgPickerLauncher;
    private String uploadedImagePath = "";
    private ScaleGestureDetector scaleGestureDetector;
    private ImageView ivCanvasBackground;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bgPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null && ivCanvasBackground != null) {
                ivCanvasBackground.setImageURI(uri);
                ivCanvasBackground.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Background image applied successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private final List<DynamicFieldHolder> fieldHolders = new ArrayList<>();

    private static class DynamicFieldHolder {
        public TemplateField field;
        public EditText editText;
        public TextInputLayout textInputLayout;
        public SwitchCompat switchCompat;
        public TextView previewTextView;

        public DynamicFieldHolder(TemplateField field, EditText editText, TextInputLayout textInputLayout, SwitchCompat switchCompat, TextView previewTextView) {
            this.field = field;
            this.editText = editText;
            this.textInputLayout = textInputLayout;
            this.switchCompat = switchCompat;
            this.previewTextView = previewTextView;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_universal_template_editor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            templateTitle = getArguments().getString("templateTitle", "Custom Design Template");
            categoryId = getArguments().getLong("categoryId", 1);
        }

        if ("Blank Canvas".equals(templateTitle) || "Custom Upload".equals(templateTitle)) {
            switch ((int) categoryId) {
                case 2: templateTitle = "Event Showcase Poster"; break;
                case 3: templateTitle = "Grand Opening Banner"; break;
                case 4: templateTitle = "Restaurant Menu Flyer"; break;
                case 5: templateTitle = "Logo Sticker"; break;
                case 6: templateTitle = "Name Mug"; break;
                case 7: templateTitle = "Typography Slogan Tee"; break;
                default: templateTitle = "Minimalist Corporate Card"; break;
            }
        }

        categoryConfig = CategoryConfig.getConfigForCategory(categoryId);
        templateLayout = TemplateLayout.getLayoutForTemplate(templateTitle);
        savedDesignViewModel = new ViewModelProvider(this).get(SavedDesignViewModel.class);

        TextView tvEditorTitle = view.findViewById(R.id.tvEditorTitle);
        tvEditorTitle.setText("Customize: " + templateTitle);

        tvTemplateSectionTitle = view.findViewById(R.id.tvTemplateSectionTitle);
        if (tvTemplateSectionTitle != null) {
            tvTemplateSectionTitle.setText(templateTitle + " Details");
        }

        cardCanvasPreview = view.findViewById(R.id.cardCanvasPreview);
        canvasRelativeLayout = view.findViewById(R.id.canvasRelativeLayout);
        ivCanvasBackground = view.findViewById(R.id.ivCanvasBackground);
        prevLogo = view.findViewById(R.id.prevLogo);
        centerGuideline = view.findViewById(R.id.centerGuideline);
        styleInspectorPanel = view.findViewById(R.id.styleInspectorPanel);
        floatingToolbar = view.findViewById(R.id.floatingToolbar);
        tvInspectorTitle = view.findViewById(R.id.tvInspectorTitle);
        materialSliderFontSize = view.findViewById(R.id.materialSliderFontSize);
        containerDynamicFields = view.findViewById(R.id.containerDynamicFields);

        // Dynamically adjust canvas preview dimensions based on CategoryConfig aspect ratio
        ViewGroup.LayoutParams params = cardCanvasPreview.getLayoutParams();
        int baseWidth = getResources().getDisplayMetrics().widthPixels - (int)(32 * getResources().getDisplayMetrics().density);
        params.height = (int) (baseWidth / categoryConfig.canvasAspectRatio);
        cardCanvasPreview.setLayoutParams(params);

        setupInteractiveElement(prevLogo, "Logo / Image");

        // Build dynamic form fields and preview text views from templateLayout
        buildDynamicFields();

        Button btnAddTextBox = view.findViewById(R.id.btnAddTextBox);
        btnAddTextBox.setOnClickListener(v -> addCustomTextBox("Custom Text", "Enter text here"));

        // Floating Toolbar actions
        view.findViewById(R.id.btnToolDelete).setOnClickListener(v -> {
            if (selectedView != null && selectedView != prevLogo) {
                selectedView.setVisibility(View.GONE);
                clearSelection();
                Toast.makeText(getContext(), "Element deleted/hidden", Toast.LENGTH_SHORT).show();
            }
        });
        view.findViewById(R.id.btnToolBringFront).setOnClickListener(v -> {
            if (selectedView != null) {
                selectedView.bringToFront();
                Toast.makeText(getContext(), "Brought to front", Toast.LENGTH_SHORT).show();
            }
        });
        view.findViewById(R.id.btnToolCenter).setOnClickListener(v -> {
            if (selectedView != null) {
                selectedView.setX((cardCanvasPreview.getWidth() - selectedView.getWidth()) / 2f);
                Toast.makeText(getContext(), "Aligned to center", Toast.LENGTH_SHORT).show();
            }
        });

        setupStylingControls(view);

        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                uploadedImagePath = uri.toString();
                prevLogo.setImageURI(uri);
                Toast.makeText(getContext(), "Custom image uploaded successfully!", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnUploadCustomImage = view.findViewById(R.id.btnUploadCustomImage);
        btnUploadCustomImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Scale seekbar & Pinch-to-resize
        SeekBar seekBarScale = view.findViewById(R.id.seekBarScale);
        seekBarScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float scale = 0.5f + (progress / 100.0f);
                prevLogo.setScaleX(scale);
                prevLogo.setScaleY(scale);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        scaleGestureDetector = new ScaleGestureDetector(requireContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float factor = detector.getScaleFactor();
                float newScale = prevLogo.getScaleX() * factor;
                if (newScale >= 0.4f && newScale <= 3.0f) {
                    prevLogo.setScaleX(newScale);
                    prevLogo.setScaleY(newScale);
                }
                return true;
            }
        });

        // Background image upload button
        Button btnUploadBgImage = view.findViewById(R.id.btnUploadBgImage);
        if (btnUploadBgImage != null) {
            btnUploadBgImage.setOnClickListener(v -> bgPickerLauncher.launch("image/*"));
        }

        // Color selection buttons
        view.findViewById(R.id.btnColorBlue).setOnClickListener(v -> {
            if (ivCanvasBackground != null) ivCanvasBackground.setVisibility(View.GONE);
            cardCanvasPreview.setCardBackgroundColor(Color.parseColor("#172554"));
        });
        view.findViewById(R.id.btnColorBlack).setOnClickListener(v -> {
            if (ivCanvasBackground != null) ivCanvasBackground.setVisibility(View.GONE);
            cardCanvasPreview.setCardBackgroundColor(Color.parseColor("#0F172A"));
        });
        view.findViewById(R.id.btnColorRed).setOnClickListener(v -> {
            if (ivCanvasBackground != null) ivCanvasBackground.setVisibility(View.GONE);
            cardCanvasPreview.setCardBackgroundColor(Color.parseColor("#B91C1C"));
        });
        view.findViewById(R.id.btnColorOrange).setOnClickListener(v -> {
            if (ivCanvasBackground != null) ivCanvasBackground.setVisibility(View.GONE);
            cardCanvasPreview.setCardBackgroundColor(Color.parseColor("#C2410C"));
        });

        Button btnCustomCanvasColor = view.findViewById(R.id.btnCustomCanvasColor);
        if (btnCustomCanvasColor != null) {
            btnCustomCanvasColor.setOnClickListener(v -> {
                ColorPickerBottomSheet colorPicker = ColorPickerBottomSheet.newInstance(Color.BLUE, color -> {
                    if (ivCanvasBackground != null) ivCanvasBackground.setVisibility(View.GONE);
                    cardCanvasPreview.setCardBackgroundColor(color);
                });
                colorPicker.show(getParentFragmentManager(), "CanvasColorPicker");
            });
        }

        // Logo selection buttons
        view.findViewById(R.id.btnLogoStar).setOnClickListener(v -> {
            uploadedImagePath = "";
            prevLogo.setImageResource(android.R.drawable.star_on);
        });
        view.findViewById(R.id.btnLogoCrown).setOnClickListener(v -> {
            uploadedImagePath = "";
            prevLogo.setImageResource(android.R.drawable.ic_menu_agenda);
        });
        view.findViewById(R.id.btnLogoPrint).setOnClickListener(v -> {
            uploadedImagePath = "";
            prevLogo.setImageResource(R.drawable.ic_launcher_foreground_1024);
        });

        MaterialButton btnSaveAndOrder = view.findViewById(R.id.btnSaveAndOrder);
        btnSaveAndOrder.setOnClickListener(this::validateAndSave);
    }

    private void buildDynamicFields() {
        containerDynamicFields.removeAllViews();
        fieldHolders.clear();

        float density = getResources().getDisplayMetrics().density;
        int cat = (int) categoryId;

        if (prevLogo != null) {
            prevLogo.setImageResource(R.drawable.ic_launcher_foreground_1024);
            cardCanvasPreview.post(() -> {
                int cardWidth = cardCanvasPreview.getWidth();
                int cardHeight = cardCanvasPreview.getHeight();
                int logoWidth = prevLogo.getWidth() > 0 ? prevLogo.getWidth() : (int) (48 * density);

                if (cardWidth > 0) {
                    if (cat == 2 || cat == 5 || cat == 7) {
                        prevLogo.setX((cardWidth - logoWidth) / 2f);
                        prevLogo.setY(16f * density);
                    } else if (cat == 6) {
                        prevLogo.setX(20f * density);
                        prevLogo.setY((cardHeight - logoWidth) / 2f);
                    } else if (cat == 9 || templateTitle.toLowerCase().contains("calendar")) {
                        prevLogo.setX(20f * density);
                        prevLogo.setY(16f * density);
                    } else if (cat == 8 || templateTitle.toLowerCase().contains("brochure")) {
                        float panelWidth = cardWidth / 3f;
                        prevLogo.setX(panelWidth + ((panelWidth - logoWidth) / 2f));
                        prevLogo.setY(12f * density);
                    } else if (cat == 3) {
                        prevLogo.setX(16f * density);
                        prevLogo.setY(16f * density);
                    } else {
                        prevLogo.setX(cardWidth - logoWidth - (16 * density));
                        prevLogo.setY(16f * density);
                    }
                }
            });
        }

        if (cat == 8 || templateTitle.toLowerCase().contains("brochure") || templateTitle.toLowerCase().contains("tri-fold")) {
            cardCanvasPreview.post(() -> {
                int cardWidth = cardCanvasPreview.getWidth();
                if (cardWidth > 0) {
                    float panelWidth = cardWidth / 3f;

                    View fold1 = new View(getContext());
                    ViewGroup.LayoutParams fold1Params = new ViewGroup.LayoutParams((int)(1.5f * density), ViewGroup.LayoutParams.MATCH_PARENT);
                    fold1.setLayoutParams(fold1Params);
                    fold1.setBackgroundColor(Color.parseColor("#475569"));
                    fold1.setX(panelWidth);
                    canvasRelativeLayout.addView(fold1, 0);

                    View fold2 = new View(getContext());
                    ViewGroup.LayoutParams fold2Params = new ViewGroup.LayoutParams((int)(1.5f * density), ViewGroup.LayoutParams.MATCH_PARENT);
                    fold2.setLayoutParams(fold2Params);
                    fold2.setBackgroundColor(Color.parseColor("#475569"));
                    fold2.setX(panelWidth * 2f);
                    canvasRelativeLayout.addView(fold2, 0);
                }
            });
        }

        float currentYDp = 16f;

        for (int i = 0; i < templateLayout.fields.size(); i++) {
            TemplateField field = templateLayout.fields.get(i);
            TextView tvPreview = new TextView(getContext());
            tvPreview.setText(field.defaultValue);

            float xDp = 16f;

            if (cat == 2) {
                tvPreview.setGravity(Gravity.CENTER_HORIZONTAL);
                if (i == 0) {
                    tvPreview.setTextColor(Color.parseColor("#F59E0B"));
                    tvPreview.setTextSize(22f);
                    tvPreview.setTypeface(null, Typeface.BOLD);
                    currentYDp = 80f;
                } else if (i == 1) {
                    tvPreview.setTextColor(Color.WHITE);
                    tvPreview.setTextSize(13f);
                    tvPreview.setTypeface(null, Typeface.ITALIC);
                    currentYDp += 32f;
                } else if (i == 2) {
                    tvPreview.setTextColor(Color.parseColor("#38BDF8"));
                    tvPreview.setTextSize(12f);
                    tvPreview.setTypeface(null, Typeface.BOLD);
                    currentYDp += 32f;
                } else {
                    tvPreview.setTextColor(Color.parseColor("#E2E8F0"));
                    tvPreview.setTextSize(11f);
                    currentYDp += 22f;
                }
                xDp = 16f;

            } else if (cat == 3) {
                xDp = 80f;
                if (i == 0) {
                    tvPreview.setTextColor(Color.WHITE);
                    tvPreview.setTextSize(18f);
                    tvPreview.setTypeface(null, Typeface.BOLD);
                    currentYDp = 14f;
                } else if (i == 1) {
                    tvPreview.setTextColor(Color.parseColor("#F97316"));
                    tvPreview.setTextSize(24f);
                    tvPreview.setTypeface(null, Typeface.BOLD);
                    currentYDp += 28f;
                } else if (i == 2) {
                    tvPreview.setTextColor(Color.parseColor("#38BDF8"));
                    tvPreview.setTextSize(12f);
                    tvPreview.setTypeface(null, Typeface.BOLD);
                    currentYDp += 34f;
                } else {
                    tvPreview.setTextColor(Color.parseColor("#E2E8F0"));
                    tvPreview.setTextSize(11f);
                    currentYDp += 20f;
                }

            } else if (cat == 5) {
                tvPreview.setGravity(Gravity.CENTER_HORIZONTAL);
                if (i == 0) {
                    tvPreview.setTextColor(Color.WHITE);
                    tvPreview.setTextSize(18f);
                    tvPreview.setTypeface(null, Typeface.BOLD);
                    currentYDp = 85f;
                } else {
                    tvPreview.setTextColor(Color.parseColor("#38BDF8"));
                    tvPreview.setTextSize(12f);
                    currentYDp += 28f;
                }
                xDp = 16f;

            } else if (cat == 6) {
                xDp = 80f;
                if (i == 0) {
                    tvPreview.setTextColor(Color.parseColor("#FBBF24"));
                    tvPreview.setTextSize(22f);
                    tvPreview.setTypeface(null, Typeface.BOLD);
                    currentYDp = 36f;
                } else {
                    tvPreview.setTextColor(Color.WHITE);
                    tvPreview.setTextSize(13f);
                    tvPreview.setTypeface(null, Typeface.ITALIC);
                    currentYDp += 32f;
                }

            } else if (cat == 7) {
                tvPreview.setGravity(Gravity.CENTER_HORIZONTAL);
                if (i == 0) {
                    tvPreview.setTextColor(Color.WHITE);
                    tvPreview.setTextSize(24f);
                    tvPreview.setTypeface(null, Typeface.BOLD);
                    currentYDp = 95f;
                } else {
                    tvPreview.setTextColor(Color.parseColor("#EA580C"));
                    tvPreview.setTextSize(14f);
                    tvPreview.setTypeface(null, Typeface.BOLD);
                    currentYDp += 36f;
                }
                xDp = 16f;

            } else if (cat == 8 || templateTitle.toLowerCase().contains("brochure") || templateTitle.toLowerCase().contains("tri-fold")) {
                float cardWidthDp = (getResources().getDisplayMetrics().widthPixels / density) - 32f;
                float panelWidthDp = cardWidthDp / 3f;

                if (i == 0) {
                    xDp = 10f;
                    currentYDp = 14f;
                    tvPreview.setTextColor(Color.parseColor("#38BDF8"));
                    tvPreview.setTextSize(12f);
                    tvPreview.setTypeface(null, Typeface.BOLD);
                } else if (i == 1) {
                    xDp = 10f;
                    currentYDp = 38f;
                    tvPreview.setTextColor(Color.WHITE);
                    tvPreview.setTextSize(9.5f);
                } else if (i == 2) {
                    xDp = 10f;
                    currentYDp = 56f;
                    tvPreview.setTextColor(Color.WHITE);
                    tvPreview.setTextSize(9.5f);
                } else if (i == 3) {
                    xDp = 10f;
                    currentYDp = 74f;
                    tvPreview.setTextColor(Color.WHITE);
                    tvPreview.setTextSize(9.5f);
                } else if (i == 4) {
                    xDp = 10f;
                    currentYDp = 92f;
                    tvPreview.setTextColor(Color.parseColor("#94A3B8"));
                    tvPreview.setTextSize(9.5f);
                } else if (i == 5) {
                    xDp = panelWidthDp + 8f;
                    currentYDp = 24f;
                    tvPreview.setTextColor(Color.WHITE);
                    tvPreview.setTextSize(15f);
                    tvPreview.setTypeface(null, Typeface.BOLD);
                } else if (i == 6) {
                    xDp = panelWidthDp + 8f;
                    currentYDp = 52f;
                    tvPreview.setTextColor(Color.parseColor("#FF9F1C"));
                    tvPreview.setTextSize(10f);
                    tvPreview.setTypeface(null, Typeface.BOLD);
                } else if (i == 7) {
                    xDp = panelWidthDp + 8f;
                    currentYDp = 86f;
                    tvPreview.setTextColor(Color.parseColor("#94A3B8"));
                    tvPreview.setTextSize(9.5f);
                } else if (i == 8) {
                    xDp = (panelWidthDp * 2f) + 8f;
                    currentYDp = 14f;
                    tvPreview.setTextColor(Color.parseColor("#38BDF8"));
                    tvPreview.setTextSize(12f);
                    tvPreview.setTypeface(null, Typeface.BOLD);
                } else {
                    xDp = (panelWidthDp * 2f) + 8f;
                    currentYDp = 38f;
                    tvPreview.setTextColor(Color.WHITE);
                    tvPreview.setTextSize(9.5f);
                }
            } else if (cat == 9 || templateTitle.toLowerCase().contains("calendar")) {
                float cardWidthDp = (getResources().getDisplayMetrics().widthPixels / density) - 32f;
                float halfWidthDp = cardWidthDp / 2f;

                if (i == 0) {
                    xDp = 16f;
                    currentYDp = 75f;
                    tvPreview.setTextColor(Color.parseColor("#FBBF24"));
                    tvPreview.setTextSize(18f);
                    tvPreview.setTypeface(null, Typeface.BOLD);
                } else if (i == 1) {
                    xDp = 16f;
                    currentYDp = 102f;
                    tvPreview.setTextColor(Color.parseColor("#94A3B8"));
                    tvPreview.setTextSize(10f);
                    tvPreview.setTypeface(null, Typeface.BOLD);
                } else if (i == 2) {
                    xDp = halfWidthDp + 10f;
                    currentYDp = 14f;
                    tvPreview.setTextColor(Color.WHITE);
                    tvPreview.setTextSize(10f);
                    tvPreview.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                    tvPreview.setLineSpacing(4f, 1.2f);
                } else {
                    xDp = 16f;
                    currentYDp = 142f;
                    tvPreview.setTextColor(Color.parseColor("#E2E8F0"));
                    tvPreview.setTextSize(9.5f);
                }
            } else {
                xDp = 16f;
                if (i == 0) {
                    tvPreview.setTextColor(Color.WHITE);
                    tvPreview.setTextSize(18f);
                    tvPreview.setTypeface(null, Typeface.BOLD);
                    currentYDp = 16f;
                } else if (i == 1) {
                    tvPreview.setTextColor(Color.parseColor("#38BDF8"));
                    tvPreview.setTextSize(12f);
                    currentYDp += 28f;
                } else if (i == 2) {
                    tvPreview.setTextColor(Color.parseColor("#94A3B8"));
                    tvPreview.setTextSize(11f);
                    tvPreview.setTypeface(null, Typeface.BOLD);
                    currentYDp += 22f;
                } else {
                    tvPreview.setTextColor(Color.parseColor("#E2E8F0"));
                    tvPreview.setTextSize(10f);
                    if (i == 3) {
                        currentYDp += 30f;
                    } else {
                        currentYDp += 18f;
                    }
                }
            }

            tvPreview.setX(xDp * density);
            tvPreview.setY(currentYDp * density);

            canvasRelativeLayout.addView(tvPreview);
            setupInteractiveElement(tvPreview, field.label);

            LinearLayout rowLayout = new LinearLayout(getContext());
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 0, 0, 12);
            rowLayout.setLayoutParams(rowParams);

            TextInputLayout til = new TextInputLayout(getContext(), null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
            LinearLayout.LayoutParams tilParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            til.setLayoutParams(tilParams);
            til.setHint(field.label + (field.required ? " *" : ""));

            TextInputEditText editText = new TextInputEditText(til.getContext());
            editText.setText(field.defaultValue);
            editText.setMaxLines(1);
            if ("phone".equals(field.inputType)) {
                editText.setInputType(InputType.TYPE_CLASS_PHONE);
            } else if ("email".equals(field.inputType)) {
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            } else if ("url".equals(field.inputType)) {
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
            }
            til.addView(editText);

            SwitchCompat sw = new SwitchCompat(getContext());
            sw.setChecked(true);
            sw.setPadding(24, 0, 0, 0);
            sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
                tvPreview.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            });

            editText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    tvPreview.setText(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });

            rowLayout.addView(til);
            rowLayout.addView(sw);
            containerDynamicFields.addView(rowLayout);

            fieldHolders.add(new DynamicFieldHolder(field, editText, til, sw, tvPreview));
        }
    }

    private void addCustomTextBox(String label, String defaultText) {
        TemplateField customField = new TemplateField("custom_" + System.currentTimeMillis(), label, "Enter text", defaultText, "text", false, 50);
        templateLayout.fields.add(customField);

        TextView tvPreview = new TextView(getContext());
        tvPreview.setText(defaultText);
        tvPreview.setTextColor(Color.WHITE);
        tvPreview.setTextSize(16f);
        tvPreview.setX(24f);
        tvPreview.setY(150f);

        canvasRelativeLayout.addView(tvPreview);
        setupInteractiveElement(tvPreview, label);

        LinearLayout rowLayout = new LinearLayout(getContext());
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, 12);
        rowLayout.setLayoutParams(rowParams);

        TextInputLayout til = new TextInputLayout(getContext(), null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
        LinearLayout.LayoutParams tilParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        til.setLayoutParams(tilParams);
        til.setHint(label);

        TextInputEditText editText = new TextInputEditText(til.getContext());
        editText.setText(defaultText);
        editText.setMaxLines(1);
        til.addView(editText);

        SwitchCompat sw = new SwitchCompat(getContext());
        sw.setChecked(true);
        sw.setPadding(24, 0, 0, 0);
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tvPreview.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvPreview.setText(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        rowLayout.addView(til);
        rowLayout.addView(sw);
        containerDynamicFields.addView(rowLayout);

        fieldHolders.add(new DynamicFieldHolder(customField, editText, til, sw, tvPreview));
        Toast.makeText(getContext(), "Added custom text box", Toast.LENGTH_SHORT).show();
    }

    private void setupInteractiveElement(View v, String elementName) {
        final float[] dX = new float[1];
        final float[] dY = new float[1];

        v.setOnTouchListener((view, event) -> {
            if (v == prevLogo) {
                scaleGestureDetector.onTouchEvent(event);
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.performClick();
                    selectElement(view, elementName);
                    if (view.getParent() != null) {
                        view.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    dX[0] = view.getX() - event.getRawX();
                    dY[0] = view.getY() - event.getRawY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float newX = event.getRawX() + dX[0];
                    float newY = event.getRawY() + dY[0];
                    float centerX = cardCanvasPreview.getWidth() / 2f;
                    float viewCenterX = newX + (view.getWidth() / 2f);

                    if (Math.abs(viewCenterX - centerX) < 15f) {
                        newX = centerX - (view.getWidth() / 2f);
                        if (centerGuideline != null) centerGuideline.setVisibility(View.VISIBLE);
                    } else {
                        if (centerGuideline != null) centerGuideline.setVisibility(View.GONE);
                    }

                    view.setX(newX);
                    view.setY(newY);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (view.getParent() != null) {
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                    if (centerGuideline != null) centerGuideline.setVisibility(View.GONE);
                    return true;
                default:
                    return false;
            }
        });
    }

    private void selectElement(View view, String name) {
        if (selectedView != null) {
            selectedView.setBackground(null);
        }
        selectedView = view;
        selectedView.setBackgroundResource(R.drawable.selection_border);
        styleInspectorPanel.setVisibility(View.VISIBLE);
        floatingToolbar.setVisibility(View.VISIBLE);
        tvInspectorTitle.setText("Styling: " + name);

        if (view instanceof TextView && materialSliderFontSize != null) {
            TextView tv = (TextView) view;
            materialSliderFontSize.setValue(Math.max(10f, Math.min(40f, tv.getTextSize())));
        }
    }

    private void clearSelection() {
        if (selectedView != null) {
            selectedView.setBackground(null);
            selectedView = null;
        }
        styleInspectorPanel.setVisibility(View.GONE);
        floatingToolbar.setVisibility(View.GONE);
        if (centerGuideline != null) centerGuideline.setVisibility(View.GONE);
    }

    private void setupStylingControls(View view) {
        if (materialSliderFontSize != null) {
            materialSliderFontSize.addOnChangeListener((slider, value, fromUser) -> {
                if (selectedView instanceof TextView && fromUser) {
                    ((TextView) selectedView).setTextSize(value);
                }
            });
        }

        view.findViewById(R.id.btnFontSans).setOnClickListener(v -> applyFont(Typeface.SANS_SERIF));
        view.findViewById(R.id.btnFontSerif).setOnClickListener(v -> applyFont(Typeface.SERIF));
        view.findViewById(R.id.btnFontMono).setOnClickListener(v -> applyFont(Typeface.MONOSPACE));
        view.findViewById(R.id.btnFontCondensed).setOnClickListener(v -> applyFont(Typeface.create("sans-serif-condensed", Typeface.NORMAL)));
        view.findViewById(R.id.btnFontLight).setOnClickListener(v -> applyFont(Typeface.create("sans-serif-light", Typeface.NORMAL)));
        view.findViewById(R.id.btnFontMedium).setOnClickListener(v -> applyFont(Typeface.create("sans-serif-medium", Typeface.NORMAL)));
        view.findViewById(R.id.btnFontCasual).setOnClickListener(v -> applyFont(Typeface.create("casual", Typeface.NORMAL)));
        view.findViewById(R.id.btnFontDefault).setOnClickListener(v -> applyFont(Typeface.DEFAULT));

        view.findViewById(R.id.btnStyleBold).setOnClickListener(v -> {
            isBold = !isBold;
            applyTextStyle();
        });
        view.findViewById(R.id.btnStyleItalic).setOnClickListener(v -> {
            isItalic = !isItalic;
            applyTextStyle();
        });
        view.findViewById(R.id.btnStyleUnderline).setOnClickListener(v -> {
            isUnderline = !isUnderline;
            applyTextStyle();
        });

        view.findViewById(R.id.btnAlignLeft).setOnClickListener(v -> applyAlignment(Gravity.START));
        view.findViewById(R.id.btnAlignCenter).setOnClickListener(v -> applyAlignment(Gravity.CENTER));
        view.findViewById(R.id.btnAlignRight).setOnClickListener(v -> applyAlignment(Gravity.END));

        // JavaFX-style ColorPickerBottomSheet integration for text color
        Button btnOpenColorPicker = view.findViewById(R.id.btnOpenColorPicker);
        if (btnOpenColorPicker != null) {
            btnOpenColorPicker.setOnClickListener(v -> {
                if (selectedView == null || !(selectedView instanceof TextView)) {
                    Toast.makeText(getContext(), "Please select a text element first", Toast.LENGTH_SHORT).show();
                    return;
                }
                ColorPickerBottomSheet colorPicker = ColorPickerBottomSheet.newInstance(Color.WHITE, color -> ((TextView) selectedView).setTextColor(color));
                colorPicker.show(getParentFragmentManager(), "TextColorPicker");
            });
        }
    }

    private void applyFont(Typeface tf) {
        if (selectedView instanceof TextView) {
            ((TextView) selectedView).setTypeface(tf, ((TextView) selectedView).getTypeface().getStyle());
        }
    }

    private void applyTextStyle() {
        if (selectedView instanceof TextView) {
            int style = Typeface.NORMAL;
            if (isBold && isItalic) style = Typeface.BOLD_ITALIC;
            else if (isBold) style = Typeface.BOLD;
            else if (isItalic) style = Typeface.ITALIC;

            TextView tv = (TextView) selectedView;
            tv.setTypeface(tv.getTypeface(), style);
            if (isUnderline) {
                tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            } else {
                tv.setPaintFlags(tv.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
            }
        }
    }

    private void applyAlignment(int gravity) {
        if (selectedView instanceof TextView) {
            ((TextView) selectedView).setGravity(gravity);
        }
    }

    private void validateAndSave(View view) {
        boolean isValid = true;
        StringBuilder summaryBuilder = new StringBuilder(categoryConfig.categoryName + " (" + templateTitle + ")");

        for (DynamicFieldHolder holder : fieldHolders) {
            holder.textInputLayout.setError(null);
            String val = holder.editText.getText().toString().trim();

            if (holder.field.required && TextUtils.isEmpty(val)) {
                holder.textInputLayout.setError(holder.field.label + " is required");
                isValid = false;
            }

            if ("phone".equals(holder.field.inputType) && !TextUtils.isEmpty(val)) {
                String slPhoneRegex = "^(\\+94|0)?7[0-9]{8}$";
                if (!val.matches(slPhoneRegex)) {
                    holder.textInputLayout.setError("Invalid phone format");
                    isValid = false;
                }
            }

            if ("email".equals(holder.field.inputType) && !TextUtils.isEmpty(val)) {
                if (!Patterns.EMAIL_ADDRESS.matcher(val).matches()) {
                    holder.textInputLayout.setError("Invalid email format");
                    isValid = false;
                }
            }

            if ("url".equals(holder.field.inputType) && !TextUtils.isEmpty(val)) {
                if (!Patterns.WEB_URL.matcher(val).matches()) {
                    holder.textInputLayout.setError("Invalid URL format");
                    isValid = false;
                }
            }

            summaryBuilder.append(" | ").append(holder.field.label).append(": ").append(val);
        }

        if (!isValid) {
            Toast.makeText(getContext(), "Please correct the errors in the form", Toast.LENGTH_SHORT).show();
            return;
        }

        clearSelection();

        String designSummary = summaryBuilder.toString();
        SessionManager sessionManager = new SessionManager(requireContext());
        long userId = sessionManager.getUserId();

        String savedPath = "";
        try {
            Bitmap bitmap = Bitmap.createBitmap(cardCanvasPreview.getWidth(), cardCanvasPreview.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            cardCanvasPreview.draw(canvas);

            File designsDir = new File(requireContext().getFilesDir(), "saved_designs");
            if (!designsDir.exists()) designsDir.mkdirs();
            File designFile = new File(designsDir, "design_" + System.currentTimeMillis() + ".png");
            FileOutputStream fos = new FileOutputStream(designFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            savedPath = designFile.getAbsolutePath();
        } catch (Exception e) {
            savedPath = TextUtils.isEmpty(uploadedImagePath) ? "" : uploadedImagePath;
        }

        final String finalPreviewPath = savedPath;
        String designJson = "{\"title\":\"" + templateTitle + "\", \"categoryId\":" + categoryId + "}";
        SavedDesign savedDesign = new SavedDesign(userId, templateTitle, finalPreviewPath, designJson, System.currentTimeMillis());

        savedDesignViewModel.insertDesign(savedDesign, new CommonRepository.InsertCallback() {
            @Override
            public void onSuccess(long id) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Custom design saved & verified!", Toast.LENGTH_SHORT).show();
                    Bundle bundle = new Bundle();
                    bundle.putLong("productId", categoryConfig.categoryId);
                    bundle.putDouble("basePrice", 2500.0);
                    bundle.putInt("quantity", 100);
                    bundle.putString("designData", designSummary);
                    bundle.putString("previewImagePath", finalPreviewPath);
                    Navigation.findNavController(requireView()).navigate(R.id.checkoutFragment, bundle);
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
