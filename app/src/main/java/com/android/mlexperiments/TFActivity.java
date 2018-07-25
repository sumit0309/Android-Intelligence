package com.android.mlexperiments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.os.TraceCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.mlexperiments.views.DrawModel;
import com.android.mlexperiments.views.DrawView;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


public class TFActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    private static final int SIZE = 28;
    private static final String INPUT = "input";
    private static final String OUTPUT = "output";
    private static final String MODEL = "file:///android_asset/mnist_model_graph.pb";
    private static final String LABEL = "file:///android_asset/graph_label_strings.txt";
    private TensorFlowInferenceInterface mInference;
    private Button clearBtn, classBtn;
    private TextView resText;
    private static final String  TAG = "TFActivity";
    private static final int RESULT_LOAD_IMAGE = 1112;
    private static final int MAX_RESULTS = 3;
    private static final float THRESHOLD = 0.1f;
    // views related
    private DrawModel drawModel;
    private DrawView drawView;
    private PointF mTmpPiont = new PointF();

    private float mLastX;
    private float mLastY;
    private List<String> labels;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tf);
        mInference = new TensorFlowInferenceInterface(getAssets(), MODEL);
        labels = new ArrayList<>();
        labels.add("0");
        labels.add("1");
        labels.add("2");
        labels.add("3");
        labels.add("4");
        labels.add("5");
        labels.add("6");
        labels.add("7");
        labels.add("8");
        labels.add("9");

        // The shape of the output is [N, NUM_CLASSES], where N is the batch size.

        //get drawing view
        drawView = (DrawView)findViewById(R.id.draw);
        drawModel = new DrawModel(SIZE, SIZE);

        drawView.setModel(drawModel);
        drawView.setOnTouchListener(this);

        //clear button
        clearBtn = (Button)findViewById(R.id.btn_clear);
        clearBtn.setOnClickListener(this);

        //class button
        classBtn = (Button)findViewById(R.id.btn_class);
        classBtn.setOnClickListener(this);
        resText = (TextView)findViewById(R.id.tfRes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawView.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        drawView.onPause();
    }

    private void runImageRecognition(float [] pix) {
        // Copy the input data into TensorFlow.
        mInference.feed(INPUT, pix, new long[]{SIZE * SIZE});

        // Run the inference call.
        mInference.run( new String[]{OUTPUT}, false);
        int numClasses =
                (int) mInference.graph().operation(OUTPUT).output(0).shape().size(1);
        float outputs[] = new float[numClasses];
        // Copy the output Tensor back into the output array.
        mInference.fetch(OUTPUT, outputs);

        for (int i = 0; i < outputs.length; ++i) {
            System.out.println(outputs[i]);
            System.out.println(labels.get(i));
            if (outputs[i] > THRESHOLD ) {
                resText.setText("Result: " +labels.get(i));
            }
        }

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_clear) {
            drawModel.clear();
            drawView.reset();
            drawView.invalidate();

            resText.setText("Result: ");
        }
        else if(view.getId() == R.id.btn_class){

            float pixels[] = drawView.getPixelData();
            runImageRecognition(pixels);


        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            processTouchDown(motionEvent);
            return true;

        } else if (action == MotionEvent.ACTION_MOVE) {
            processTouchMove(motionEvent);
            return true;

        } else if (action == MotionEvent.ACTION_UP) {
            processTouchUp();
            drawView.invalidate();
            return true;
        }
        return false;
    }
    private void processTouchDown(MotionEvent event) {
        mLastX = event.getX();
        mLastY = event.getY();
        drawView.calcPos(mLastX, mLastY, mTmpPiont);
        float lastConvX = mTmpPiont.x;
        float lastConvY = mTmpPiont.y;
        drawModel.startLine(lastConvX, lastConvY);
    }

    private void processTouchMove(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        drawView.calcPos(x, y, mTmpPiont);
        float newConvX = mTmpPiont.x;
        float newConvY = mTmpPiont.y;
        drawModel.addLineElem(newConvX, newConvY);

        mLastX = x;
        mLastY = y;
        drawView.invalidate();
    }

    private void processTouchUp() {
        drawModel.endLine();
    }
}
