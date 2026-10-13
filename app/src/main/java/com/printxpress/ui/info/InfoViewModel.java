package com.printxpress.ui.info;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.printxpress.data.entity.Faq;
import com.printxpress.data.entity.SupportQuery;
import com.printxpress.data.repository.CommonRepository;
import com.printxpress.util.SessionManager;
import java.util.List;

public class InfoViewModel extends AndroidViewModel {

    private final CommonRepository commonRepository;
    private final SessionManager sessionManager;
    private final MutableLiveData<String> actionMessage = new MutableLiveData<>();

    public InfoViewModel(@NonNull Application application) {
        super(application);
        commonRepository = new CommonRepository(application);
        sessionManager = new SessionManager(application);
    }

    public LiveData<String> getActionMessage() {
        return actionMessage;
    }

    public LiveData<List<Faq>> getFaqs() {
        return commonRepository.getAllFaqs();
    }

    public LiveData<List<SupportQuery>> getChatMessages() {
        return commonRepository.getSupportQueriesByUserAsc(sessionManager.getUserId());
    }

    public void sendMessage(String message) {
        if (message == null || message.trim().isEmpty()) return;

        long userId = sessionManager.getUserId();
        long now = System.currentTimeMillis();

        String lower = message.toLowerCase();
        String reply;
        if (lower.contains("order") || lower.contains("track") || lower.contains("status")) {
            reply = "You can track your order live under 'Orders' tab using your PX- tracking code. Our standard turnaround is 24-48 business hours.";
        } else if (lower.contains("bleed") || lower.contains("margin") || lower.contains("dpi") || lower.contains("resolution")) {
            reply = "For best print results, keep designs at 300 DPI, add a 2mm bleed margin, and use CMYK color profile.";
        } else if (lower.contains("pickup") || lower.contains("studio") || lower.contains("store") || lower.contains("location")) {
            reply = "Our Colombo 03 studio is open Mon-Sat 8:30 AM - 6:30 PM. Express print-on-demand pickup is ready within 24 hours!";
        } else if (lower.contains("card") || lower.contains("payment") || lower.contains("cod")) {
            reply = "We accept Visa, Mastercard, American Express, Cash on Delivery, and Studio Counter payments (Cash / LANKAQR).";
        } else {
            reply = "Thank you for reaching out to PrintXpress! A print technician has received your ticket and will follow up shortly.";
        }

        SupportQuery query = new SupportQuery(userId, "Customer Ticket", message.trim(), now);
        query.reply = reply;

        commonRepository.insertSupportQuery(query, new CommonRepository.InsertCallback() {
            @Override
            public void onSuccess(long id) {
                // LiveData updates automatically
            }

            @Override
            public void onError(String error) {
                actionMessage.postValue("Could not send message: " + error);
            }
        });
    }
}
