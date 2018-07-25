package com.android.mlexperiments;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button mTextrecg;
    Button mFence;
    Button mSnapshot;
    Button mTensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextrecg = (Button) findViewById(R.id.textRecog);
        mTextrecg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), TextRecogActivity.class);
                view.getContext().startActivity(intent);
            }
        });
        mFence = (Button)findViewById(R.id.fence);
        mFence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AwarenessActivity.class);
                view.getContext().startActivity(intent);
            }
        });
        mSnapshot = (Button)findViewById(R.id.snapshot);
        mSnapshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SnapshotActivity.class);
                view.getContext().startActivity(intent);
            }
        });

        mTensor = (Button)findViewById(R.id.tf);
        mTensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), TFActivity.class);
                view.getContext().startActivity(intent);
            }
        });
    }
}
