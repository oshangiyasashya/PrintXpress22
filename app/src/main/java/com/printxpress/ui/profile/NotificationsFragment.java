package com.printxpress.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.printxpress.R;
import com.printxpress.data.entity.Notification;
import com.printxpress.util.GenericAdapter;

public class NotificationsFragment extends Fragment {
    private NotificationViewModel viewModel;
    private GenericAdapter<Notification> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        RecyclerView rv = view.findViewById(R.id.rvNotifications);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GenericAdapter<>(
            (v, notification) -> {
                TextView tvMain = v.findViewById(R.id.tvMain);
                TextView tvSub = v.findViewById(R.id.tvSub);
                if (tvMain != null) tvMain.setText(notification.title);
                if (tvSub != null) tvSub.setText(notification.message);
                if (notification.isRead) {
                    v.setAlpha(0.6f);
                } else {
                    v.setAlpha(1.0f);
                }
            },
            notification -> {
                if (!notification.isRead) {
                    viewModel.markAsRead(notification.id);
                }
            }
        );
        rv.setAdapter(adapter);

        viewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            if (notifications != null) adapter.setItems(notifications);
        });
    }
}
