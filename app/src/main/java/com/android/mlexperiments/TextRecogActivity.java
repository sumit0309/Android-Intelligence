package com.android.mlexperiments;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.IOException;
import java.util.List;

public class TextRecogActivity extends AppCompatActivity {
    Button browse;
    ImageView imageView;
    TextView tv;
    private static final String  TAG = "TextRecogActivity";
    private static final int RESULT_LOAD_IMAGE = 1111;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_recog);
        browse = (Button)findViewById(R.id.browse);
         imageView = (ImageView) findViewById(R.id.img);
         tv = (TextView) findViewById(R.id.tview);
         tv.setMovementMethod(new ScrollingMovementMethod());
        browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv.setText("");
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

            Uri selectedImage = data.getData();
            runTextRecognition(selectedImage);
            Log.d(TAG,"Uri is -->" + selectedImage.toString());
            imageView.setImageURI(selectedImage);
        }
    }

    private void runTextRecognition(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (IOException e) {
            Log.d(TAG,"Exception converting to bitmap");
            e.printStackTrace();
        }

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextDetector detector = FirebaseVision.getInstance()
                .getVisionTextDetector();
        detector.detectInImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText texts) {
                                processTextRecognitionResult(texts);
                                Log.d(TAG, "Text recognition success");
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                Log.d(TAG, "Text recognition failure");
                                e.printStackTrace();
                            }
                        });
    }

    private void processTextRecognitionResult(FirebaseVisionText texts) {
        List<FirebaseVisionText.Block> blocks = texts.getBlocks();
        if (blocks.size() == 0) {
            Log.d(TAG,"No text found");
            return;
        }
        String val = "";
        int blksize = blocks.size();
        for (int i = 0; i < blksize; i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            int lineSize = lines.size();
            for (int j = 0; j < lineSize; j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                int elemSize = elements.size();
                for (int k = 0; k < elemSize; k++) {
                    val+=elements.get(k).getText().trim() + " ";
                    Log.d(TAG, " the values-->" + val);
                }
                val.trim();
                val+="\n";
            }
        }
        tv.setText(val);
    }
}
