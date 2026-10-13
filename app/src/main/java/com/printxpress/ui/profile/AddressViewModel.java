package com.printxpress.ui.profile;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.printxpress.data.entity.Address;
import com.printxpress.data.repository.CommonRepository;
import com.printxpress.util.SessionManager;
import java.util.List;

public class AddressViewModel extends AndroidViewModel {

    private final CommonRepository commonRepository;
    private final SessionManager sessionManager;
    private final MutableLiveData<String> actionMessage = new MutableLiveData<>();

    public AddressViewModel(@NonNull Application application) {
        super(application);
        commonRepository = new CommonRepository(application);
        sessionManager = new SessionManager(application);
    }

    public LiveData<String> getActionMessage() {
        return actionMessage;
    }

    public long getCurrentUserId() {
        return sessionManager.getUserId();
    }

    public LiveData<List<Address>> getAddresses() {
        return commonRepository.getAddressesByUser(sessionManager.getUserId());
    }

    public void addAddress(Address address) {
        commonRepository.insertAddress(address, new CommonRepository.InsertCallback() {
            @Override
            public void onSuccess(long id) {
                actionMessage.postValue("Address added successfully!");
            }

            @Override
            public void onError(String error) {
                actionMessage.postValue("Failed to add address: " + error);
            }
        });
    }

    public void deleteAddress(long addressId) {
        commonRepository.deleteAddress(addressId, () -> actionMessage.postValue("Address removed"));
    }

    public void setDefaultAddress(long addressId) {
        commonRepository.setDefaultAddress(sessionManager.getUserId(), addressId, () -> actionMessage.postValue("Default address updated"));
    }
}
