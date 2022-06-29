package com.example.facedetection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button btn_picImage, btn_imgProcess;
    private ImageView img_pickedImg;
    private boolean isImagePicked = false;
    private final int PICK_IMAGE = 101;
    InputImage inputImage;
    ImageLabeler labeler;
    FaceDetector detector;
    FaceDetectorOptions highAccuracyOpts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_picImage = findViewById(R.id.btn_picImage);
        btn_imgProcess = findViewById(R.id.btn_imgProcess);
        img_pickedImg = findViewById(R.id.img_pickedImg);

        highAccuracyOpts = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();
        
        detector = FaceDetection.getClient(highAccuracyOpts);
        labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

        btn_imgProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Task<List<Face>> result = 
                        detector.process(inputImage)
                                .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        Toast.makeText(MainActivity.this, "process finish", Toast.LENGTH_SHORT).show();
                                        if(faces.isEmpty()) {
                                            Toast.makeText(MainActivity.this, "no face detected", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // 顔を検出したときの処理
                                            Bitmap visualizationresult = drawDetectionResult(inputImage.getBitmapInternal(), faces);
                                            img_pickedImg.setImageBitmap(visualizationresult);
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        
                                    }
                                });
            }
        });

        btn_picImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri pickedImage = data.getData();
            img_pickedImg.setImageURI(pickedImage);

            try {
                inputImage = InputImage.fromFilePath(this, pickedImage);
                isImagePicked = true;
                Toast.makeText(this, "InputImage set Success", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap drawDetectionResult(Bitmap bitmap, List<Face> faces) {
        Bitmap outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(outputBitmap);
        Paint paint = new Paint();

        for(Face face: faces) {
            Rect rect = face.getBoundingBox();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(8f);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(rect, paint);
        }

        return outputBitmap;
    }
}