package com.printxpress.ui.profile;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.printxpress.data.entity.SavedDesign;
import com.printxpress.data.repository.CommonRepository;
import com.printxpress.util.SessionManager;
import java.util.List;

public class SavedDesignViewModel extends AndroidViewModel {

    private final CommonRepository commonRepository;
    private final SessionManager sessionManager;
    private final MutableLiveData<String> actionMessage = new MutableLiveData<>();

    public SavedDesignViewModel(@NonNull Application application) {
        super(application);
        commonRepository = new CommonRepository(application);
        sessionManager = new SessionManager(application);
    }

    public LiveData<String> getActionMessage() {
        return actionMessage;
    }

    public LiveData<List<SavedDesign>> getSavedDesigns() {
        return commonRepository.getSavedDesigns(sessionManager.getUserId());
    }

    public LiveData<SavedDesign> getDesignById(long id) {
        return commonRepository.getSavedDesignById(id);
    }

    public void deleteDesign(long id) {
        commonRepository.deleteSavedDesign(id, () -> actionMessage.postValue("Design deleted"));
    }

    public void insertDesign(SavedDesign design, CommonRepository.InsertCallback callback) {
        commonRepository.insertSavedDesign(design, callback);
    }
}
