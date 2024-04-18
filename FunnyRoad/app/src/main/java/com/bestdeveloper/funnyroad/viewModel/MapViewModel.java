package com.bestdeveloper.funnyroad.viewModel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MapViewModel extends AndroidViewModel {
    private FirebaseFirestore firebase;
    private MutableLiveData<String> encodedPathLiveData = new MutableLiveData<>();

    public MapViewModel(Application application){
        super(application);
        firebase = FirebaseFirestore.getInstance();
    }

    public void setRouteMakerLiveData(String path) {
        encodedPathLiveData.setValue(path);
    }
    public MutableLiveData<String> getPath() {
        return encodedPathLiveData;
    }

    public void saveRoute(String path) {
        Map<String, String> mapToSave = new HashMap<>();
        if(path != null) {
            mapToSave.put("path", path);
            firebase.collection("generated_routes")
                    .add(mapToSave)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("firebase", "DocumentSnapshot added with ID: " + documentReference.getId());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("firebase", "Error adding document", e);
                        }
                    });
        }
        else{
            Log.w("firebase", "There is no routes to save!");
        }
    }
}
