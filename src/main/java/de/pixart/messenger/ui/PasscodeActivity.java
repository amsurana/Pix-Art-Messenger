package de.pixart.messenger.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.pixart.messenger.Config;
import de.pixart.messenger.R;
import de.pixart.messenger.utils.StringUtils;
import me.drakeet.support.toast.ToastCompat;

/**
 * Created by Amit S.
 */
public class PasscodeActivity extends AppCompatActivity {

    private static final String PASSCODE_PREFS_FILE = "prefsFile";
    private static final String PASSCODE_KEY = "k2m3";

    private static final String TAG = "PasscodeActivity";
    private PinLockView mPinLockView;

    private String intermediatePin = "";

    private PinLockListener mPinLockListener = new PinLockListener() {
        @Override
        public void onComplete(String pin) {
            if (pin != null && pin.length() == 6) {
                processPin(pin);
            }

        }

        @Override
        public void onEmpty() {

        }

        @Override
        public void onPinChange(int pinLength, String intermediatePin) {
//            if (pinLength == 6) {
//                processPin(intermediatePin);
//            }
        }
    };


    private IndicatorDots mIndicatorDots;
    private boolean update = false;
    private boolean validated = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_passcode);

        initPasscodeView();

        if (!hasPasscode()) {
            ToastCompat.makeText(this, "Set new Passcode first", Toast.LENGTH_SHORT).show();
            findViewById(R.id.change).setVisibility(View.INVISIBLE);
        }
    }

    private boolean hasPasscode() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(PASSCODE_PREFS_FILE, Context.MODE_PRIVATE);
        return prefs.contains(PASSCODE_KEY);
    }

    private void processPin(String pin) {
        if (!hasPasscode() || update) {
            if (intermediatePin == null || intermediatePin.length() == 0) {
                if (update && !validated) {
                    if (isSame(pin)) {
                        ToastCompat.makeText(this, "Please enter new passcode now", Toast.LENGTH_SHORT).show();
                        validated = true;
                    } else {
                        ToastCompat.makeText(this, "Old passcode does not match.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    intermediatePin = pin;
                    ToastCompat.makeText(this, "Please re-enter the Passcode to Verify", Toast.LENGTH_SHORT).show();
                }
                mPinLockView.resetPinLockView();
                return;
            }
            if (StringUtils.equals(pin, intermediatePin)) {
                ToastCompat.makeText(this, "Passcode saved. Enter the code again", Toast.LENGTH_SHORT).show();
                savePin(pin);
                restartThisScreen();
                return;
            }
        } else if (isSame(pin)) {
            nextScreen();
        } else {
            ToastCompat.makeText(this, "Passcode does not match. Enter the code again", Toast.LENGTH_SHORT).show();
            mPinLockView.resetPinLockView();
        }
    }

    private void restartThisScreen() {
        Intent intent = new Intent(this, PasscodeActivity.class);
        startActivity(intent);
        finish();
    }

    private void savePin(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bt = md.digest(pin.getBytes());
            String str = new String(bt);
            SharedPreferences prefs = getApplicationContext().getSharedPreferences(PASSCODE_PREFS_FILE, Context.MODE_PRIVATE);
            prefs.edit().putString(PASSCODE_KEY, str).apply();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    private boolean isSame(String pin) {
        try {
            SharedPreferences prefs = getApplicationContext().getSharedPreferences(PASSCODE_PREFS_FILE, Context.MODE_PRIVATE);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bt = md.digest(pin.getBytes());
            String str = new String(bt);
            return prefs.getString(PASSCODE_KEY, "").equalsIgnoreCase(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void initPasscodeView() {
        mPinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
        mPinLockView.setPinLockListener(mPinLockListener);
        mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);
        mPinLockView.attachIndicatorDots(mIndicatorDots);

        findViewById(R.id.change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update = true;
                mPinLockView.resetPinLockView();
                ToastCompat.makeText(PasscodeActivity.this, "Please enter old Passcode first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void nextScreen() {
        String PREF_FIRST_START = "FirstStart";
        SharedPreferences FirstStart = getApplicationContext().getSharedPreferences(PREF_FIRST_START, Context.MODE_PRIVATE);
        long FirstStartTime = FirstStart.getLong(PREF_FIRST_START, 0);
        Log.d(Config.LOGTAG, "All permissions granted, starting " + getString(R.string.app_name) + "(" + FirstStartTime + ")");
        Intent intent = new Intent(this, ConversationsActivity.class);
        intent.putExtra(PREF_FIRST_START, FirstStartTime);
        startActivity(intent);
        overridePendingTransition(R.animator.fade_in, R.animator.fade_out);
        finish();
    }
}
