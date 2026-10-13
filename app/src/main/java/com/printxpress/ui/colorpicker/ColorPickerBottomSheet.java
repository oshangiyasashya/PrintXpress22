package com.printxpress.ui.colorpicker;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.printxpress.R;

public class ColorPickerBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_INITIAL_COLOR = "initial_color";

    private int initialColor = Color.BLACK;
    private int selectedColor = Color.BLACK;

    private View viewCurrentColor, viewNewColor;
    private MaterialButton btnTabPalette, btnTabCustom;
    private LinearLayout layoutPaletteContainer, layoutCustomContainer;

    private RecyclerView recyclerRecentColors, recyclerPaletteColors;
    private ColorSwatchAdapter recentAdapter, paletteAdapter;

    private SaturationBrightnessView viewSaturationBrightness;
    private HueSliderView viewHueSlider;
    private TextView btnSubTabHsb, btnSubTabRgb, btnSubTabWeb;
    private LinearLayout layoutHsbInputs, layoutRgbInputs, layoutWebInputs;
    private TextView tvHsbValues, tvRgbValues;
    private SeekBar seekHsbH, seekHsbS, seekHsbB;
    private SeekBar seekRgbR, seekRgbG, seekRgbB;
    private EditText etWebHex;

    private boolean isUpdatingFromCode = false;
    private OnColorSelectedListener listener;

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    public static ColorPickerBottomSheet newInstance(int initialColor, OnColorSelectedListener listener) {
        ColorPickerBottomSheet fragment = new ColorPickerBottomSheet();
        fragment.listener = listener;
        Bundle args = new Bundle();
        args.putInt(ARG_INITIAL_COLOR, initialColor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            initialColor = getArguments().getInt(ARG_INITIAL_COLOR, Color.BLACK);
            selectedColor = initialColor;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_color_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewCurrentColor = view.findViewById(R.id.viewCurrentColor);
        viewNewColor = view.findViewById(R.id.viewNewColor);

        btnTabPalette = view.findViewById(R.id.btnTabPalette);
        btnTabCustom = view.findViewById(R.id.btnTabCustom);
        layoutPaletteContainer = view.findViewById(R.id.layoutPaletteContainer);
        layoutCustomContainer = view.findViewById(R.id.layoutCustomContainer);

        recyclerRecentColors = view.findViewById(R.id.recyclerRecentColors);
        recyclerPaletteColors = view.findViewById(R.id.recyclerPaletteColors);

        viewSaturationBrightness = view.findViewById(R.id.viewSaturationBrightness);
        viewHueSlider = view.findViewById(R.id.viewHueSlider);

        btnSubTabHsb = view.findViewById(R.id.btnSubTabHsb);
        btnSubTabRgb = view.findViewById(R.id.btnSubTabRgb);
        btnSubTabWeb = view.findViewById(R.id.btnSubTabWeb);

        layoutHsbInputs = view.findViewById(R.id.layoutHsbInputs);
        layoutRgbInputs = view.findViewById(R.id.layoutRgbInputs);
        layoutWebInputs = view.findViewById(R.id.layoutWebInputs);

        tvHsbValues = view.findViewById(R.id.tvHsbValues);
        tvRgbValues = view.findViewById(R.id.tvRgbValues);

        seekHsbH = view.findViewById(R.id.seekHsbH);
        seekHsbS = view.findViewById(R.id.seekHsbS);
        seekHsbB = view.findViewById(R.id.seekHsbB);

        seekRgbR = view.findViewById(R.id.seekRgbR);
        seekRgbG = view.findViewById(R.id.seekRgbG);
        seekRgbB = view.findViewById(R.id.seekRgbB);

        etWebHex = view.findViewById(R.id.etWebHex);

        setupPreviewStrips();
        setupTabs();
        setupPaletteAdapters();
        setupCustomColorPickerControls();

        view.findViewById(R.id.btnCancelColor).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btnUseColor).setOnClickListener(v -> {
            ColorUtils.addRecentColor(selectedColor);
            if (listener != null) {
                listener.onColorSelected(selectedColor);
            }
            dismiss();
        });

        updateColorState(initialColor, true);
    }

    private void setupPreviewStrips() {
        GradientDrawable currentDrawable = new GradientDrawable();
        currentDrawable.setShape(GradientDrawable.RECTANGLE);
        currentDrawable.setCornerRadius(8f);
        currentDrawable.setColor(initialColor);
        viewCurrentColor.setBackground(currentDrawable);

        updateNewColorPreview();
    }

    private void updateNewColorPreview() {
        GradientDrawable newDrawable = new GradientDrawable();
        newDrawable.setShape(GradientDrawable.RECTANGLE);
        newDrawable.setCornerRadius(8f);
        newDrawable.setColor(selectedColor);
        viewNewColor.setBackground(newDrawable);
    }

    private void setupTabs() {
        btnTabPalette.setOnClickListener(v -> {
            btnTabPalette.setBackgroundTintList(requireContext().getColorStateList(android.R.color.holo_blue_dark));
            btnTabPalette.setTextColor(Color.WHITE);
            btnTabCustom.setBackgroundColor(Color.TRANSPARENT);
            btnTabCustom.setTextColor(Color.parseColor("#3B82F6"));

            layoutPaletteContainer.setVisibility(View.VISIBLE);
            layoutCustomContainer.setVisibility(View.GONE);
        });

        btnTabCustom.setOnClickListener(v -> {
            btnTabCustom.setBackgroundTintList(requireContext().getColorStateList(android.R.color.holo_blue_dark));
            btnTabCustom.setTextColor(Color.WHITE);
            btnTabPalette.setBackgroundColor(Color.TRANSPARENT);
            btnTabPalette.setTextColor(Color.parseColor("#3B82F6"));

            layoutCustomContainer.setVisibility(View.VISIBLE);
            layoutPaletteContainer.setVisibility(View.GONE);
        });
    }

    private void setupPaletteAdapters() {
        recyclerRecentColors.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recentAdapter = new ColorSwatchAdapter(ColorUtils.getRecentColors(), color -> updateColorState(color, true));
        recyclerRecentColors.setAdapter(recentAdapter);

        recyclerPaletteColors.setLayoutManager(new GridLayoutManager(getContext(), 7));
        paletteAdapter = new ColorSwatchAdapter(ColorUtils.getDefaultPaletteColors(), color -> updateColorState(color, true));
        recyclerPaletteColors.setAdapter(paletteAdapter);
    }

    private void setupCustomColorPickerControls() {
        btnSubTabHsb.setOnClickListener(v -> {
            layoutHsbInputs.setVisibility(View.VISIBLE);
            layoutRgbInputs.setVisibility(View.GONE);
            layoutWebInputs.setVisibility(View.GONE);
            setSubTabActive(btnSubTabHsb, btnSubTabRgb, btnSubTabWeb);
        });

        btnSubTabRgb.setOnClickListener(v -> {
            layoutHsbInputs.setVisibility(View.GONE);
            layoutRgbInputs.setVisibility(View.VISIBLE);
            layoutWebInputs.setVisibility(View.GONE);
            setSubTabActive(btnSubTabRgb, btnSubTabHsb, btnSubTabWeb);
        });

        btnSubTabWeb.setOnClickListener(v -> {
            layoutHsbInputs.setVisibility(View.GONE);
            layoutRgbInputs.setVisibility(View.GONE);
            layoutWebInputs.setVisibility(View.VISIBLE);
            setSubTabActive(btnSubTabWeb, btnSubTabHsb, btnSubTabRgb);
        });

        viewHueSlider.setOnHueChangedListener(hue -> {
            if (isUpdatingFromCode) return;
            float[] hsv = new float[3];
            ColorUtils.colorToHsv(selectedColor, hsv);
            hsv[0] = hue;
            int newCol = ColorUtils.hsvToColor(hsv);
            updateColorState(newCol, true);
        });

        viewSaturationBrightness.setOnSaturationBrightnessChangedListener((saturation, brightness) -> {
            if (isUpdatingFromCode) return;
            float[] hsv = new float[3];
            ColorUtils.colorToHsv(selectedColor, hsv);
            hsv[1] = saturation;
            hsv[2] = brightness;
            int newCol = ColorUtils.hsvToColor(hsv);
            updateColorState(newCol, true);
        });

        SeekBar.OnSeekBarChangeListener hsbListener = new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isUpdatingFromCode || !fromUser) return;
                float h = seekHsbH.getProgress();
                float s = seekHsbS.getProgress() / 100f;
                float b = seekHsbB.getProgress() / 100f;
                int newCol = Color.HSVToColor(new float[]{h, s, b});
                updateColorState(newCol, true);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        };
        seekHsbH.setOnSeekBarChangeListener(hsbListener);
        seekHsbS.setOnSeekBarChangeListener(hsbListener);
        seekHsbB.setOnSeekBarChangeListener(hsbListener);

        SeekBar.OnSeekBarChangeListener rgbListener = new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isUpdatingFromCode || !fromUser) return;
                int r = seekRgbR.getProgress();
                int g = seekRgbG.getProgress();
                int b = seekRgbB.getProgress();
                int newCol = Color.rgb(r, g, b);
                updateColorState(newCol, true);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        };
        seekRgbR.setOnSeekBarChangeListener(rgbListener);
        seekRgbG.setOnSeekBarChangeListener(rgbListener);
        seekRgbB.setOnSeekBarChangeListener(rgbListener);

        etWebHex.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdatingFromCode) return;
                String hex = s.toString().trim();
                if (ColorUtils.isValidHexColor(hex)) {
                    int col = ColorUtils.parseColor(hex, selectedColor);
                    updateColorState(col, true);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setSubTabActive(TextView active, TextView inactive1, TextView inactive2) {
        active.setTextColor(Color.parseColor("#3B82F6"));
        active.setTextSize(13f);
        inactive1.setTextColor(Color.parseColor("#6B7280"));
        inactive1.setTextSize(12f);
        inactive2.setTextColor(Color.parseColor("#6B7280"));
        inactive2.setTextSize(12f);
    }

    private void updateColorState(int color, boolean updateCustomViews) {
        selectedColor = color;
        updateNewColorPreview();

        if (paletteAdapter != null) paletteAdapter.setSelectedColor(color);
        if (recentAdapter != null) recentAdapter.setSelectedColor(color);

        if (updateCustomViews) {
            isUpdatingFromCode = true;

            float[] hsv = new float[3];
            ColorUtils.colorToHsv(color, hsv);

            viewHueSlider.setHue(hsv[0]);
            viewSaturationBrightness.setHue(hsv[0]);
            viewSaturationBrightness.setSaturationBrightness(hsv[1], hsv[2]);

            seekHsbH.setProgress((int) hsv[0]);
            seekHsbS.setProgress((int) (hsv[1] * 100f));
            seekHsbB.setProgress((int) (hsv[2] * 100f));
            tvHsbValues.setText(String.format("H: %d°  S: %d%%  B: %d%%", (int) hsv[0], (int) (hsv[1] * 100), (int) (hsv[2] * 100)));

            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);

            seekRgbR.setProgress(r);
            seekRgbG.setProgress(g);
            seekRgbB.setProgress(b);
            tvRgbValues.setText(String.format("R: %d  G: %d  B: %d", r, g, b));

            String hex = ColorUtils.colorToHex(color);
            if (!hex.equalsIgnoreCase(etWebHex.getText().toString().trim())) {
                etWebHex.setText(hex);
            }

            isUpdatingFromCode = false;
        }
    }
}
