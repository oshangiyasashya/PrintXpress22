package com.printxpress.ui.admin;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.printxpress.R;
import com.printxpress.data.entity.Promotion;
import com.printxpress.data.entity.SupportQuery;
import com.printxpress.util.GenericAdapter;
import java.util.List;

public class AdminMarketingFragment extends Fragment {

    private AdminViewModel viewModel;
    private RecyclerView rvMarketing;
    private String currentTab = "TICKETS";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_marketing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        rvMarketing = view.findViewById(R.id.rvAdminMarketing);
        ChipGroup chipGroup = view.findViewById(R.id.chipGroupMarketingTab);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddMarketingItem);

        rvMarketing.setLayoutManager(new LinearLayoutManager(getContext()));

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipTabPromotions) currentTab = "PROMOS";
            else if (checkedId == R.id.chipTabNotifications) currentTab = "NOTIFS";
            else if (checkedId == R.id.chipTabFaqs) currentTab = "FAQS";
            else currentTab = "TICKETS";

            loadCurrentTab();
        });

        fabAdd.setOnClickListener(v -> {
            if ("PROMOS".equals(currentTab)) showAddPromoDialog();
            else showBroadcastNotifDialog();
        });

        loadCurrentTab();
    }

    private void loadCurrentTab() {
        if ("PROMOS".equals(currentTab)) {
            viewModel.getAllPromotions().observe(getViewLifecycleOwner(), promos -> {
                if (promos != null) {
                    GenericAdapter<Promotion> adapter = new GenericAdapter<>(
                        (v, promo) -> {
                            TextView tvMain = v.findViewById(R.id.tvMain);
                            TextView tvSub = v.findViewById(R.id.tvSub);
                            tvMain.setText("🎉 " + promo.title + " (" + (int) promo.discountPercent + "% OFF)");
                            tvSub.setText(promo.description);
                        },
                        promo -> {
                            new AlertDialog.Builder(requireContext())
                                .setTitle("Delete Promotion")
                                .setMessage("Delete promo " + promo.title + "?")
                                .setPositiveButton("Delete", (d, w) -> viewModel.deletePromotion(promo.id))
                                .setNegativeButton("Cancel", null)
                                .show();
                        }
                    );
                    adapter.setItems(promos);
                    rvMarketing.setAdapter(adapter);
                }
            });
        } else {
            viewModel.getAllSupportTickets().observe(getViewLifecycleOwner(), tickets -> {
                if (tickets != null) {
                    GenericAdapter<SupportQuery> adapter = new GenericAdapter<>(
                        (v, query) -> {
                            TextView tvMain = v.findViewById(R.id.tvMain);
                            TextView tvSub = v.findViewById(R.id.tvSub);
                            String status = TextUtils.isEmpty(query.reply) ? "PENDING ✉" : "REPLIED ✓";
                            tvMain.setText("Ticket #" + query.id + " [" + status + "]: " + query.subject);
                            tvSub.setText("Msg: " + query.message + (query.reply != null ? "\nReply: " + query.reply : "\nTap to reply"));
                        },
                        query -> showReplyDialog(query)
                    );
                    adapter.setItems(tickets);
                    rvMarketing.setAdapter(adapter);
                }
            });
        }
    }

    private void showAddPromoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Create New Promotion / Coupon");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etTitle = new EditText(getContext());
        etTitle.setHint("Promo Title / Coupon Code (e.g. SAVE20)");
        layout.addView(etTitle);

        final EditText etDesc = new EditText(getContext());
        etDesc.setHint("Description");
        layout.addView(etDesc);

        final EditText etDiscount = new EditText(getContext());
        etDiscount.setHint("Discount Percentage (e.g. 20)");
        etDiscount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etDiscount);

        builder.setView(layout);

        builder.setPositiveButton("Publish Promo", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String discStr = etDiscount.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(discStr)) {
                Toast.makeText(getContext(), "Please enter title and discount", Toast.LENGTH_SHORT).show();
                return;
            }

            double disc = Double.parseDouble(discStr);
            long now = System.currentTimeMillis();
            long thirtyDays = 30L * 24 * 60 * 60 * 1000;
            Promotion promo = new Promotion(title, desc, disc, 1, now, now + thirtyDays);
            viewModel.addPromotion(promo);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showBroadcastNotifDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Broadcast Notification to Customers");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etTitle = new EditText(getContext());
        etTitle.setHint("Notification Title");
        layout.addView(etTitle);

        final EditText etMessage = new EditText(getContext());
        etMessage.setHint("Message Content...");
        etMessage.setLines(3);
        layout.addView(etMessage);

        builder.setView(layout);

        builder.setPositiveButton("Send Broadcast", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String msg = etMessage.getText().toString().trim();

            if (!title.isEmpty() && !msg.isEmpty()) {
                viewModel.sendBroadcastNotification(title, msg, null);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showReplyDialog(SupportQuery query) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Reply to Ticket #" + query.id);

        final EditText input = new EditText(getContext());
        input.setHint("Type official technician reply...");
        if (!TextUtils.isEmpty(query.reply)) {
            input.setText(query.reply);
        }
        builder.setView(input);

        builder.setPositiveButton("Send Reply", (dialog, which) -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty()) {
                viewModel.replyToTicket(query.id, text);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
