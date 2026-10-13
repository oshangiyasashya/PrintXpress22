package com.printxpress.ui.profile;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.printxpress.data.entity.Notification;
import com.printxpress.data.repository.CommonRepository;
import com.printxpress.util.SessionManager;
import java.util.List;

public class NotificationViewModel extends AndroidViewModel {

    private final CommonRepository commonRepository;
    private final SessionManager sessionManager;

    public NotificationViewModel(@NonNull Application application) {
        super(application);
        commonRepository = new CommonRepository(application);
        sessionManager = new SessionManager(application);
    }

    public LiveData<List<Notification>> getNotifications() {
        return commonRepository.getNotificationsByUser(sessionManager.getUserId());
    }

    public LiveData<Integer> getUnreadCount() {
        return commonRepository.getUnreadNotificationCount(sessionManager.getUserId());
    }

    public void markAsRead(long notificationId) {
        commonRepository.markNotificationRead(notificationId);
    }

    public void markAllAsRead() {
        commonRepository.markAllNotificationsRead(sessionManager.getUserId());
    }

    public void deleteNotification(long notificationId) {
        commonRepository.deleteNotification(notificationId);
    }
}
