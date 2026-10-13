package com.printxpress.ui.info;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.printxpress.data.entity.Faq;
import com.printxpress.data.entity.SupportQuery;
import com.printxpress.util.GenericAdapter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InfoFragment extends Fragment {
    private InfoViewModel viewModel;
    private GenericAdapter<Faq> faqAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(InfoViewModel.class);

        Button btnSupportQuery = view.findViewById(R.id.btnSupportQuery);
        btnSupportQuery.setOnClickListener(v -> showLiveSupportChatDialog());

        RecyclerView rvFaqs = view.findViewById(R.id.rvFaqs);
        rvFaqs.setLayoutManager(new LinearLayoutManager(getContext()));
        faqAdapter = new GenericAdapter<>(
            (v2, faq) -> {
                TextView tvMain = v2.findViewById(R.id.tvMain);
                TextView tvSub = v2.findViewById(R.id.tvSub);
                tvMain.setText("Q: " + faq.question);
                tvSub.setText("A: " + faq.answer);
            },
            faq -> {}
        );
        rvFaqs.setAdapter(faqAdapter);

        viewModel.getFaqs().observe(getViewLifecycleOwner(), faqs -> {
            if (faqs != null) faqAdapter.setItems(faqs);
        });

        viewModel.getActionMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLiveSupportChatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        View chatView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_support_chat, null);
        builder.setView(chatView);

        AlertDialog dialog = builder.create();

        RecyclerView rvChat = chatView.findViewById(R.id.rvChatMessages);
        TextView tvEmptyChat = chatView.findViewById(R.id.tvEmptyChat);
        EditText etInput = chatView.findViewById(R.id.etChatMessageInput);
        MaterialButton btnSend = chatView.findViewById(R.id.btnSendChatMessage);
        ImageButton btnClose = chatView.findViewById(R.id.btnCloseChat);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        rvChat.setLayoutManager(layoutManager);

        ChatAdapter adapter = new ChatAdapter();
        rvChat.setAdapter(adapter);

        viewModel.getChatMessages().observe(getViewLifecycleOwner(), queries -> {
            if (queries != null && !queries.isEmpty()) {
                adapter.setList(queries);
                rvChat.setVisibility(View.VISIBLE);
                tvEmptyChat.setVisibility(View.GONE);
                rvChat.scrollToPosition(queries.size() - 1);
            } else {
                adapter.setList(new ArrayList<>());
                rvChat.setVisibility(View.GONE);
                tvEmptyChat.setVisibility(View.VISIBLE);
            }
        });

        btnSend.setOnClickListener(v -> {
            String message = etInput.getText().toString().trim();
            if (TextUtils.isEmpty(message)) {
                return;
            }
            etInput.setText("");
            viewModel.sendMessage(message);
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
        private List<SupportQuery> list = new ArrayList<>();
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        public void setList(List<SupportQuery> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SupportQuery q = list.get(position);

            // User outgoing message
            holder.tvUserMessage.setText(q.message);
            holder.tvUserTime.setText(timeFormat.format(new Date(q.createdAt)));

            // Support incoming reply
            if (!TextUtils.isEmpty(q.reply)) {
                holder.layoutAgentBubble.setVisibility(View.VISIBLE);
                holder.tvAgentReply.setText(q.reply);
                holder.tvAgentTime.setText(timeFormat.format(new Date(q.repliedAt > 0 ? q.repliedAt : q.createdAt + 60000L)));
            } else {
                holder.layoutAgentBubble.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            View layoutUserBubble, layoutAgentBubble;
            TextView tvUserMessage, tvUserTime, tvAgentReply, tvAgentTime;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                layoutUserBubble = itemView.findViewById(R.id.layoutUserBubble);
                layoutAgentBubble = itemView.findViewById(R.id.layoutAgentBubble);
                tvUserMessage = itemView.findViewById(R.id.tvUserMessage);
                tvUserTime = itemView.findViewById(R.id.tvUserTime);
                tvAgentReply = itemView.findViewById(R.id.tvAgentReply);
                tvAgentTime = itemView.findViewById(R.id.tvAgentTime);
            }
        }
    }
}
