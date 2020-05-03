package com.jaehyun.skkugraduation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLRemoteModel;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;
import com.jaehyun.skkugraduation.CameraTensorflow.DetectorActivity;
import com.jaehyun.skkugraduation.CameraTensorflow.GuideItem;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    private RecyclerView resultRecyclerView;
    private ResultAdapter resultAdapter;
    private LinearLayout linearLayout;

    private Button completeButton;

    //private FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    private FirebaseAutoMLRemoteModel remoteModel;
    private FirebaseVisionImageLabeler labeler;
    private FirebaseAutoMLLocalModel localModel;
    private static final String AUTOML_NAME = "dog_0501";
    //private static final String AUTOML_NAME = "dog_2020427133539";
    private static final float THRESHOLD = 0.5f;
    private boolean isdownload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        recyclerViewItemClick();
        setAutoMl();
    }

    private void init(){
        linearLayout = findViewById(R.id.progress_layout);
        resultRecyclerView = findViewById(R.id.main_result_recylverview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,1, GridLayoutManager.VERTICAL,false);
        resultAdapter = new ResultAdapter(this);
        resultRecyclerView.setLayoutManager(linearLayoutManager);
        resultRecyclerView.setAdapter(resultAdapter);

        completeButton =  findViewById(R.id.main_complete_button);

        completeButton.setOnClickListener(v->{
            //GuideItem.cropBitmapList.get(0);
            finishAffinity();
            System.runFinalizersOnExit(true);
            System.exit(0);
        });

        /*completeButton.setOnClickListener(v->{
            finishAffinity();
            System.runFinalization();
            System.exit(0);
            Toast.makeText(this,"완료되었습니다.",Toast.LENGTH_LONG).show();
        });

         */


    }

    private void recyclerViewItemClick(){
        resultAdapter.setItemClick(new ResultAdapter.ItemClick() {
            @Override
            public void onClick(View view, int position) {
                GuideItem.currentStep = position;
                GuideItem.inpectLike = GuideItem.SINGLE_INSPECT;

                Intent intent = new Intent(MainActivity.this, DetectorActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
    }

    private void setAutoMl(){
        remoteModel = new FirebaseAutoMLRemoteModel.Builder(AUTOML_NAME).build();

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        FirebaseModelManager.getInstance().download(remoteModel, conditions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("modeldownload", "Success");
                        linearLayout.setVisibility(View.GONE);
                        isdownload = true;
                        setLabeler();
                        getLabel(GuideItem.cropBitmapList.get(0));
                        // Success.
                    }
                });
    }

    private void getLabel(Bitmap bitmap){
        if(isdownload){
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

            labeler.processImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                            int count = 0;
                            for (FirebaseVisionImageLabel label: labels) {
                                String text = label.getText();
                                float confidence = label.getConfidence();
                                resultAdapter.titles.clear();
                                resultAdapter.titles.add(text);
                                resultAdapter.titles.add(text + " crop");
                                resultAdapter.notifyDataSetChanged();
                                //if(count == 0)Toast.makeText(MainActivity.this,"Name : " + text + " Confidence : " + String.valueOf(confidence), Toast.LENGTH_SHORT).show();

                                count ++;
                                Log.d("##############", text + String.valueOf(confidence));
                            }
                            if(labels.size() == 0){
                                Log.d("###########", "nodog matched");
                                resultAdapter.titles.clear();
                                resultAdapter.titles.add("not found");
                                resultAdapter.titles.add("not found" + " crop");
                                resultAdapter.notifyDataSetChanged();
                            }



                            // Task completed successfully
                            // ...
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("##############", "failure");
                            // Task failed with an exception
                            // ...
                        }
                    });
        }else {
            Toast.makeText(this,"not downloaded yet", Toast.LENGTH_SHORT).show();
        }

    }



    private void setLabeler(){
        try {
            FirebaseVisionOnDeviceAutoMLImageLabelerOptions options =
                    new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(remoteModel)
                            .setConfidenceThreshold(THRESHOLD)  // Evaluate your model in the Firebase console
                            // to determine an appropriate value.
                            .build();
            labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options);
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }
    }

    private void setLabelerwithLocal(){
        FirebaseModelManager.getInstance().isModelDownloaded(remoteModel)
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean isDownloaded) {
                        FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder optionsBuilder;
                        if (isDownloaded) {
                            optionsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(remoteModel);
                        } else {
                            optionsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel);
                        }
                        FirebaseVisionOnDeviceAutoMLImageLabelerOptions options = optionsBuilder
                                .setConfidenceThreshold(THRESHOLD)  // Evaluate your model in the Firebase console
                                // to determine an appropriate threshold.
                                .build();

                        //FirebaseVisionImageLabeler labeler;
                        try {
                            labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options);
                        } catch (FirebaseMLException e) {
                            // Error.
                            e.printStackTrace();
                        }
                    }
                });
    }


}
