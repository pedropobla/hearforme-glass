package co.zerep.hearforme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.view.MenuUtils;

import co.zerep.hearforme.languages.Languages;
import co.zerep.hearforme.languages.language.Language;
import co.zerep.hearforme.settings.SettingsController;

public class MainActivity extends Activity
        implements NetworkStateReceiver.NetworkStateReceiverListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_NO_INTERNET_ALERT = 0;

    private boolean running = true;

    private GestureDetector mGestureDetector;
    private AudioManager mAudioManager;

    private ScrollView mScrollView;
    private TextView mTextView;

    private ImageView mStatusBar;
    private ImageView mStatusBackground;
    private TextView mStatusMessage;

    private ImageView mInputLanguageFlag;
    private ImageView mRightArrow;
    private ImageView mOutputLanguageFlag;

    private View mVolumeBarLeft;
    private View mVolumeBarRight;

    private ContinuousRecognizer mRecognizer;
    private Thread mRecognizerThread;
    private Language mInputLanguage;
    private Language mOutputLanguage;
    private ConnectivityManager mConnectivityManager;
    private NetworkStateReceiver mNetworkStateReceiver;
    private final Language DEFAULT_INPUT_LANGUAGE = Languages.IN_ENGLISH_USA;
    private final Language DEFAULT_OUTPUT_LANGUAGE = Languages.OUT_SPANISH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ensure screen stays on.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mNetworkStateReceiver = new NetworkStateReceiver();
        mNetworkStateReceiver.addListener(this);

        setContentView(R.layout.activity_main);

        mScrollView = (ScrollView) findViewById(R.id.main_view_scroll);
        mScrollView.setSmoothScrollingEnabled(true);

        mTextView = (TextView) findViewById(R.id.main_view_text);
        mTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        mStatusBar = (ImageView) findViewById(R.id.status_bar);
        mStatusBackground = (ImageView) findViewById(R.id.status_background);
        mStatusMessage = (TextView) findViewById(R.id.main_status_text);

        mInputLanguageFlag = (ImageView) findViewById(R.id.input_language_flag);
        mRightArrow = (ImageView) findViewById(R.id.right_arrow);
        mOutputLanguageFlag = (ImageView) findViewById(R.id.output_language_flag);

        mVolumeBarLeft = findViewById(R.id.volume_bar_left);
        mVolumeBarRight = findViewById(R.id.volume_bar_right);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = createGestureDetector(this);

        mRecognizer = new ContinuousRecognizer(this);
        mRecognizerThread = new Thread(mRecognizer);

        if (!SettingsController.hasInputLanguage())
            SettingsController.createInputLanguage(DEFAULT_INPUT_LANGUAGE);
        if (!SettingsController.hasOutputLanguage())
            SettingsController.createOutputLanguage(DEFAULT_OUTPUT_LANGUAGE);

        setInputLanguage(SettingsController.getInputLanguage());
        setOutputLanguage(SettingsController.getOutputLanguage());

        mRecognizerThread.start();
    }

    @Override
    protected void onPause() {
        running = false;
        if (mRecognizer != null) {
            mRecognizer.pause();
        }
        if (mNetworkStateReceiver != null) {
            try {
                unregisterReceiver(mNetworkStateReceiver);
            } catch (IllegalArgumentException iae) {
                Log.e(TAG, "Tried to unregister an already-unregistered receiver.");
            }
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        running = true;
        mStatusBar.setVisibility(View.GONE);
        if (mNetworkStateReceiver != null) {
            registerReceiver(mNetworkStateReceiver,
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (mRecognizer != null) {
            mRecognizer.resume();
        }
    }

    @Override
    protected void onDestroy() {
        if (mRecognizerThread != null) {
            mRecognizer.terminate();
            try {
                mRecognizerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (mNetworkStateReceiver != null) {
            mNetworkStateReceiver.removeListener(this);
        }
        super.onDestroy();
    }
    
    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        Menu in = menu.addSubMenu(R.string.input_language).setIcon(R.drawable.ic_input_language);
        for (Language lang : Languages.getInputLanguages()) {
            MenuItem item = in.add(Menu.NONE, lang.getMenuId(), Menu.NONE,
                    lang.getName()).setIcon(lang.getFlag());
            if (item.getItemId() == mInputLanguage.getMenuId()) {
                MenuUtils.setInitialMenuItem(in, item);
            }
        }

        if (mInputLanguage.isTranslatable()) {
            Menu out = menu.addSubMenu(R.string.output_language).setIcon(R.drawable.ic_output_language);
            for (Language lang : Languages.getOutputLanguages()) {
                MenuItem item = out.add(Menu.NONE, lang.getMenuId(), Menu.NONE,
                        lang.getName()).setIcon(lang.getFlag());
                if (item.getItemId() == mInputLanguage.getMenuId()) {
                    MenuUtils.setInitialMenuItem(out, item);
                }
            }
        } else {
            MenuItem item = menu.add(Menu.NONE, R.string.output_language, Menu.NONE, R.string.output_language).
                    setIcon(R.drawable.ic_output_language).setEnabled(false);
            MenuUtils.setDescription(item, R.string.output_language_not_available);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Language lang = Languages.getLanguageFromMenuId(item.getItemId());
        if (lang != null) {
            if (lang.getType() == Language.Type.INPUT) {
                setInputLanguage(lang);
            } else if(lang.getType() == Language.Type.OUTPUT) {
                setOutputLanguage(lang);
            }
            invalidateOptionsMenu();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Send generic motion events to the gesture detector
     */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NO_INTERNET_ALERT) {
            if (isNetworkAvailable()) {
                Log.d(TAG, "Network is back available.");
                overlayAlert(true, R.string.overlay_success);
            } else {
                Log.d(TAG, "Network is still unavailable.");
                overlayAlert(false, R.string.overlay_failure);
            }
        }
    }

    @Override
    public void networkAvailable() {
        Log.d(TAG, "Network available.");
    }

    @Override
    public void networkUnavailable() {
        // TODO: Avoid showing this alert very often
        Log.d(TAG, "Network unavailable.");
        mAudioManager.playSoundEffect(Sounds.ERROR);
        Intent noInternetIntent = new Intent(this, NoInternetActivity.class);
        startActivityForResult(noInternetIntent, REQUEST_NO_INTERNET_ALERT);
        overridePendingTransition(R.anim.open_animation, R.anim.open_animation);
    }

    public void onResults(final String recognizedText) {
        if (!running) return;
        mTextView.append(recognizedText + " ");
        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
        mAudioManager.playSoundEffect(Sounds.SUCCESS);
        mStatusBackground.setVisibility(View.GONE);
        mStatusMessage.setVisibility(View.GONE);
        mStatusBar.setVisibility(View.GONE);
    }

    public void onError(final int errorCode, final String errorDetail, final String suggestion) {
        if (!running) return;
        mStatusBackground.setVisibility(View.VISIBLE);
        mStatusMessage.setText(suggestion);
        mStatusMessage.setVisibility(View.VISIBLE);
        mStatusBar.setVisibility(View.GONE);
        mAudioManager.playSoundEffect(Sounds.DISALLOWED);
    }

    public void onRecordingBegin() {
        if (!running) return;
        mStatusBackground.setVisibility(View.GONE);
        mStatusMessage.setVisibility(View.GONE);
        mStatusBar.setVisibility(View.VISIBLE);
        mStatusBar.setImageDrawable(null);
        mStatusBar.setBackgroundResource(R.color.green);
        mAudioManager.playSoundEffect(Sounds.TAP);
    }

    public void onAudioLevelChanged(float level) {
        final int height = (int) Math.max(0, (((level - 28.0) / 60.0) * 360));
        mVolumeBarLeft.post(new Runnable() {
            @Override
            public void run() {
                final ViewGroup.LayoutParams params = mVolumeBarLeft.getLayoutParams();
                params.height = height;
                mVolumeBarLeft.setLayoutParams(params);
            }
        });
        mVolumeBarRight.post(new Runnable() {
            @Override
            public void run() {
                final ViewGroup.LayoutParams params = mVolumeBarRight.getLayoutParams();
                params.height = height;
                mVolumeBarRight.setLayoutParams(params);
            }
        });
    }

    public void onRecordingDone() {
        if (!running) return;
        mStatusBackground.setVisibility(View.GONE);
        mStatusMessage.setVisibility(View.GONE);
        mStatusBar.setVisibility(View.VISIBLE);
        mStatusBar.setBackground(null);
        mStatusBar.setImageResource(R.drawable.progress_bar);
        mAudioManager.playSoundEffect(Sounds.TAP);
    }

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.TAP) {
                    mAudioManager.playSoundEffect(Sounds.TAP);
                    openOptionsMenu();
                    return true;
                }
                return false;
            }
        });
        gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
            @Override
            public boolean onScroll(float displacement, float delta, float velocity) {
                mScrollView.smoothScrollBy(0, (int) delta / 2);
                return true;
            }
        });
        return gestureDetector;
    }

    private void setInputLanguage(Language lang) {
        Log.d(TAG, "Input language changed: " + lang);
        SettingsController.setInputLanguage(lang);
        mInputLanguage = lang;
        mRecognizer.stopRecording();
        mRecognizer.setLangCode(lang.getCode());
        mInputLanguageFlag.setImageDrawable(lang.getFlag());
        if(!lang.isTranslatable()) setOutputLanguage(Languages.NONE); // TODO: Check null
    }

    private void setOutputLanguage(Language lang) {
        SettingsController.setOutputLanguage(lang);
        mOutputLanguage = lang;
        if (lang == Languages.NONE) {
            mOutputLanguageFlag.setImageDrawable(mInputLanguage.getFlag());
            mRightArrow.setVisibility(View.GONE);
            mOutputLanguageFlag.setVisibility(View.GONE);
        }
        else {
            mOutputLanguageFlag.setImageDrawable(lang.getFlag());
            mRightArrow.setVisibility(View.VISIBLE);
            mOutputLanguageFlag.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "Output language changed: " + lang);
    }

    private void overlayAlert(boolean success, int textId) {
        Intent overlayIntent = new Intent(this, OverlayAlertActivity.class);
        overlayIntent.putExtra("success", success);
        overlayIntent.putExtra("textId", textId);
        startActivity(overlayIntent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
