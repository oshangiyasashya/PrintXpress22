package com.printxpress.ui.admin;

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
import com.printxpress.data.entity.AdminActivityLog;
import com.printxpress.util.GenericAdapter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminActivityLogFragment extends Fragment {

    private AdminViewModel viewModel;
    private RecyclerView rvLogs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_activity_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
        rvLogs = view.findViewById(R.id.rvAdminActivityLogs);
        rvLogs.setLayoutManager(new LinearLayoutManager(getContext()));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

        viewModel.getAllActivityLogs().observe(getViewLifecycleOwner(), logs -> {
            if (logs != null) {
                GenericAdapter<AdminActivityLog> adapter = new GenericAdapter<>(
                    (v, log) -> {
                        TextView tvName = v.findViewById(R.id.tvLogAdminName);
                        TextView tvTime = v.findViewById(R.id.tvLogTimestamp);
                        TextView tvAction = v.findViewById(R.id.tvLogAction);
                        TextView tvDetails = v.findViewById(R.id.tvLogDetails);

                        tvName.setText(log.adminName != null ? log.adminName : "Admin #" + log.adminUserId);
                        tvTime.setText(sdf.format(new Date(log.timestamp)));
                        tvAction.setText(log.actionName);
                        tvDetails.setText(log.details != null ? log.details : "");
                    },
                    null
                );
                adapter.setItems(logs);
                rvLogs.setAdapter(adapter);
            }
        });
    }
}
