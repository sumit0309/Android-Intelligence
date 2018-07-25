package com.android.mlexperiments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.DetectedActivityResponse;
import com.google.android.gms.awareness.snapshot.HeadphoneStateResponse;
import com.google.android.gms.awareness.snapshot.HeadphoneStateResult;
import com.google.android.gms.awareness.snapshot.LocationResponse;
import com.google.android.gms.awareness.snapshot.PlacesResponse;
import com.google.android.gms.awareness.snapshot.PlacesResult;
import com.google.android.gms.awareness.snapshot.WeatherResponse;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class SnapshotActivity extends AppCompatActivity {
    private static String TAG = "SnapshotActivity";
    private static final int MY_PERMISSION_LOCATION = 1;
    private static String mResult;
    private TextView tv ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        tv = (TextView) findViewById(R.id.tvi);
        mResult = "";
        printSnapshot();
    }
    /**
     * Uses the snapshot API to print out some contextual information the device is "aware" of.
     */
    private void printSnapshot() {

        // Each type of contextual information in the snapshot API has a corresponding "get" method.
        //  For instance, this is how to get the user's current Activity.
        Awareness.getSnapshotClient(this).getDetectedActivity()
                .addOnSuccessListener(new OnSuccessListener<DetectedActivityResponse>() {
                    @Override
                    public void onSuccess(DetectedActivityResponse dar) {
                        ActivityRecognitionResult arr = dar.getActivityRecognitionResult();
                        // getMostProbableActivity() is good enough for basic Activity detection.
                        // To work within a threshold of confidence,
                        // use ActivityRecognitionResult.getProbableActivities() to get a list of
                        // potential current activities, and check the confidence of each one.
                        DetectedActivity probableActivity = arr.getMostProbableActivity();

                        int confidence = probableActivity.getConfidence();
                        String activityStr = probableActivity.toString();

                        mResult+="Activity: " + activityStr
                                + "\n\n" ;
                        tv.setText(mResult);
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Could not detect activity: " + e);
                    }
                });

        // Pulling headphone state is similar, but doesn't involve analyzing confidence.
        Awareness.getSnapshotClient(this).getHeadphoneState()
                .addOnSuccessListener(new OnSuccessListener<HeadphoneStateResponse>() {
                    @Override
                    public void onSuccess(HeadphoneStateResponse headphoneStateResponse) {
                        HeadphoneState headphoneState = headphoneStateResponse.getHeadphoneState();
                        boolean pluggedIn = headphoneState.getState() == HeadphoneState.PLUGGED_IN;
                        String stateStr =
                                "Headphones are " + (pluggedIn ? "plugged in" : "unplugged");
                        mResult += stateStr + "\n\n";
                        tv.setText(mResult);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Could not get headphone state: " + e);
                    }
                });

        // Some of the data available via Snapshot API requires permissions that must be checked
        // at runtime.  Weather snapshots are a good example of this.  Since weather is protected
        // by a runtime permission, and permission request callbacks will happen asynchronously,
        // the easiest thing to do is put weather snapshot code in its own method.  That way it
        // can be called from here when permission has already been granted on subsequent runs,
        // and from the permission request callback code when permission is first granted.
        checkAndRequestWeatherPermissions();
    }
    /**
     * Helper method to handle requesting the runtime permissions required for weather snapshots.
     *
     * @return true if the permission has already been granted, false otherwise.
     */
    private void checkAndRequestWeatherPermissions() {
        if (ContextCompat.checkSelfPermission(
                SnapshotActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.i(TAG, "Permission previously denied and app shouldn't ask again.  Skipping" +
                        " weather snapshot.");
            } else {
                ActivityCompat.requestPermissions(
                        SnapshotActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_LOCATION
                );
            }
        } else {
            getWeatherSnapshot();
            getPlacesSnapshot();
            getLocationsSnapshot();
        }
    }
    private void getPlacesSnapshot() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Awareness.getSnapshotClient(this).getPlaces().addOnSuccessListener(new OnSuccessListener<PlacesResponse>() {
                @Override
                public void onSuccess(PlacesResponse placesResponse) {
                    List<PlaceLikelihood> placeLikelihood =
                            placesResponse.getPlaceLikelihoods();
                    int size = placeLikelihood.size();
                    for (int i = 0; i < size; i++) {
                        mResult += "Place: " + placeLikelihood.get(i).getPlace().getName()
                                + " Likelihood: " +  placeLikelihood.get(i).getLikelihood() + "\n";
                    }
                    mResult+="\n";
                    tv.setText(mResult);
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Could not get places: " + e);
                        }
                    });
        }
    }

    private void getLocationsSnapshot() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Awareness.getSnapshotClient(this).getLocation().addOnSuccessListener(new OnSuccessListener<LocationResponse>() {
                @Override
                public void onSuccess(LocationResponse locationResponse) {
                    Location loc = locationResponse.getLocation();
                    mResult += "Latitude: " + loc.getLatitude() + ", Longitutude: " + loc.getLongitude()
                            + ", Altitude: " + loc.getAltitude() + ", Accuracy: " + loc.getAccuracy() + "\n\n";
                    tv.setText(mResult);

                }
            })

                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Could not get places: " + e);
                                }
                            });
        }
    }
    private void getWeatherSnapshot() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Awareness.getSnapshotClient(this).getWeather()
                    .addOnSuccessListener(new OnSuccessListener<WeatherResponse>() {
                        @Override
                        public void onSuccess(WeatherResponse weatherResponse) {
                            Weather weather = weatherResponse.getWeather();
                            weather.getConditions();
                            mResult += "Weather: " + weather + "\n\n";
                            tv.setText(mResult);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Could not get weather: " + e);
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getWeatherSnapshot();
                    getPlacesSnapshot();
                    getLocationsSnapshot();
                } else {
                    Log.i(TAG, "Location permission denied.  Weather snapshot skipped.");
                }
            }
        }
    }
}
