package com.application.fingerprinttestapp;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Executor;


public class FullscreenActivity extends AppCompatActivity {

    private static final boolean AUTO_HIDE = true;


    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;


    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;


    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final View.OnTouchListener mDelayHideTouchListener = (view, motionEvent) -> {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (AUTO_HIDE) {
                    delayedHide(AUTO_HIDE_DELAY_MILLIS);
                }
                break;
            case MotionEvent.ACTION_UP:
                view.performClick();
                break;
            default:
                break;
        }
        return false;
    };


    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.messageRelHardware);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI


        try {
            fingerPrintFunction();
        }catch (Exception exception){
            exception.printStackTrace();
        }






    }

    @Override
    protected void onRestart() {
        super.onRestart();
        try {
            hide();
            fingerPrintFunction();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    BiometricPrompt biometricPrompt;

    private void fingerPrintFunction(){

        //(Depreciated: API 28)
        // 1: ANDROID VERSION SHOULD BE GREATER OR EQUAL TO MARSHMALLOW
        // 2: DEVICE HAS FINGERPRINT SCANNER
        // 3: HAVE PERMISSION TO USE FINGERPRINT SCANNER IN THE APP
        // 4: LOCK SCREEN IS SECURED WITH AT LEAST 1 TYPE OF LOCK
        // 5: AT LEAST 1 FINGERPRINT IS REGISTERED

        /*
        *
        * TODO
        *  1: add dependencies -> implementations 'androidx.biometric:biometric:1.1.0' ///// and then change it to the recent version!
        *  2: add permissions to use biometric
        *  3: add a BiometricManager and check if the user can use the fingerprint sensor or not
        *  4: Show the data accordingly to the UI
        *  5: Now after we have completed checking if we can use the scanner or not. We need to create our biometric dialog box
        *  6: Create an executor
        *  7: Create a Biometric Prompt, which will give the result of the authentication and if we can login or not
        *  8: Create the biometric dialog and promptInfo
        *  9: Authenticate the biometric prompt by passing the promptInfo
        *
        * */
        String message = "";
        int textColor = getResources().getColor(R.color.biometric_error_color,getTheme());
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                message = getResources().getString(R.string.message_touch_sensor);
                textColor = getResources().getColor(R.color.light_blue_600, getTheme());
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                message = getResources().getString(R.string.message_no_fingerprint_enrolled);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                message = getResources().getString(R.string.message_fingerprint_sensor_hardware_unavailable);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                message = getResources().getString(R.string.message_no_fingerprint_hardware);
                break;
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                message = getResources().getString(R.string.message_some_error_occurred);
                break;
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                message = getResources().getString(R.string.message_some_error_occurred);
                break;
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                message = getResources().getString(R.string.message_some_error_occurred);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG));
        }
        ((TextView)findViewById(R.id.messageRelHardware)).setText(message);
        ((TextView)findViewById(R.id.messageRelHardware)).setTextColor(textColor);

        //Creating executor
        Executor executor = ContextCompat.getMainExecutor(this);

        //Creating Biometric Prompt (just this much will create the biometric prompt)
        biometricPrompt = new BiometricPrompt(FullscreenActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override //This method is called when there is an error while the authentication
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),"ERROR!",Toast.LENGTH_SHORT).show();
            }

            @Override //This method is called when the authentication is successful
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(),"SUCCESS!",Toast.LENGTH_SHORT).show();
            }

            @Override //This method is called when we have failed the authentication
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(),"FAILED!",Toast.LENGTH_SHORT).show();
            }
        });


        //Create the Biometric Dialog
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("LOGIN")
                .setDescription("TOUCH THE SENSOR")
                .setNegativeButtonText("CANCEL").build();

        biometricPrompt.authenticate(promptInfo);

    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }


    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}