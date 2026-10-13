package com.printxpress;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.printxpress.ui.admin.AdminDashboardActivity;
import com.printxpress.util.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private View layoutOnboarding;
    private LinearLayout layoutSplashContent;
    private ViewPager2 viewPagerOnboarding;
    private Button btnSkip, btnNext;

    public static class OnboardingSlide {
        public String title;
        public String description;
        public int imageRes;

        public OnboardingSlide(String title, String description, int imageRes) {
            this.title = title;
            this.description = description;
            this.imageRes = imageRes;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);
        layoutSplashContent = findViewById(R.id.layoutSplashContent);
        layoutOnboarding = findViewById(R.id.layoutOnboarding);
        viewPagerOnboarding = findViewById(R.id.viewPagerOnboarding);
        btnSkip = findViewById(R.id.btnSkipOnboarding);
        btnNext = findViewById(R.id.btnNextOnboarding);

        ImageView logo = findViewById(R.id.ivSplashLogo);
        if (logo != null) {
            logo.setAlpha(0f);
            logo.animate().alpha(1f).scaleX(1.1f).scaleY(1.1f).setDuration(800).start();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing()) return;

            if (sessionManager.isFirstLaunch() && !sessionManager.isLoggedIn()) {
                setupOnboarding();
            } else {
                proceedToApp();
            }
        }, 1200);
    }

    private void setupOnboarding() {
        layoutSplashContent.setVisibility(View.GONE);
        layoutOnboarding.setVisibility(View.VISIBLE);

        List<OnboardingSlide> slides = new ArrayList<>();
        slides.add(new OnboardingSlide(
                "Design Your Vision",
                "Choose from hundreds of customizable templates or upload your own studio artworks seamlessly.",
                R.drawable.printxpress_logo
        ));
        slides.add(new OnboardingSlide(
                "High Precision Printing",
                "State-of-the-art digital offset & merchandise presses delivering vibrant, professional results.",
                R.drawable.ic_cat_brochure
        ));
        slides.add(new OnboardingSlide(
                "Doorstep Delivery",
                "Express shipping across Sri Lanka or quick studio pickup at your convenience.",
                R.drawable.ic_cat_banner
        ));

        viewPagerOnboarding.setAdapter(new OnboardingAdapter(slides));

        btnSkip.setOnClickListener(v -> finishOnboarding());
        btnNext.setOnClickListener(v -> {
            int current = viewPagerOnboarding.getCurrentItem();
            if (current < slides.size() - 1) {
                viewPagerOnboarding.setCurrentItem(current + 1);
            } else {
                finishOnboarding();
            }
        });
    }

    private void finishOnboarding() {
        sessionManager.setFirstLaunchCompleted();
        proceedToApp();
    }

    private void proceedToApp() {
        Intent nextIntent;
        if (sessionManager.isLoggedIn() && sessionManager.isAdminOrStaff()) {
            nextIntent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
        } else {
            nextIntent = new Intent(SplashActivity.this, MainActivity.class);
        }
        startActivity(nextIntent);
        finish();
    }

    private static class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {
        private final List<OnboardingSlide> slides;

        public OnboardingAdapter(List<OnboardingSlide> slides) {
            this.slides = slides;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding_slide, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            OnboardingSlide slide = slides.get(position);
            holder.tvTitle.setText(slide.title);
            holder.tvDesc.setText(slide.description);
            holder.ivImage.setImageResource(slide.imageRes);
        }

        @Override
        public int getItemCount() {
            return slides.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;
            TextView tvTitle, tvDesc;

            ViewHolder(View itemView) {
                super(itemView);
                ivImage = itemView.findViewById(R.id.ivOnboardingImage);
                tvTitle = itemView.findViewById(R.id.tvOnboardingTitle);
                tvDesc = itemView.findViewById(R.id.tvOnboardingDescription);
            }
        }
    }
}
