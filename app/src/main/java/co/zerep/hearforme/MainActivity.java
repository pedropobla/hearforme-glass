package co.zerep.hearforme;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.SpeechKit;

public class MainActivity extends Activity implements Recognizer.Listener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SPEECH_KIT_APP_ID = "NMDPTRIAL_pedropobla20141006104322";
    private static final byte[] SPEECH_KIT_APP_KEY = {(byte)0x37, (byte)0xfe, (byte)0x63, (byte)0x6d, (byte)0xe8, (byte)0x5c, (byte)0xe0, (byte)0x78, (byte)0x98, (byte)0x72, (byte)0xa5, (byte)0xf4, (byte)0x33, (byte)0xc3, (byte)0x38, (byte)0xb3, (byte)0x73, (byte)0xbc, (byte)0xb8, (byte)0x40, (byte)0x54, (byte)0x65, (byte)0xd2, (byte)0x3f, (byte)0xaf, (byte)0xc2, (byte)0xb6, (byte)0x4c, (byte)0x35, (byte)0xdc, (byte)0x99, (byte)0x6d, (byte)0xdb, (byte)0xfc, (byte)0xda, (byte)0xc3, (byte)0x58, (byte)0xbb, (byte)0x3b, (byte)0xf1, (byte)0x2d, (byte)0xe1, (byte)0xe5, (byte)0x61, (byte)0xa4, (byte)0x1d, (byte)0x14, (byte)0x18, (byte)0xd9, (byte)0xcc, (byte)0x58, (byte)0x2d, (byte)0x31, (byte)0x2a, (byte)0x6d, (byte)0xe0, (byte)0xec, (byte)0x23, (byte)0x2c, (byte)0x22, (byte)0xff, (byte)0xc5, (byte)0x8f, (byte)0xda};
    private static final String SPEECH_KIT_URL = "sslsandbox.nmdp.nuancemobility.net";
    private static final int SPEECH_KIT_PORT = 443;

    private GestureDetector mGestureDetector;
    private AudioManager mAudioManager;
    private TextView mTextView;
    private SpeechKit mSpeechKit;
    private Recognizer mRecognizer;
    private Settings mSettings;
    private String mInputLanguage;
    private String mOutputLanguage;
    private final int DEFAULT_INPUT_LANGUAGE = R.string.engUSA_code;
    private final int DEFAULT_OUTPUT_LANGUAGE = R.string.spaESP_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ensure screen stays on.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mSettings = new Settings(this);

        if (!mSettings.hasInputLanguage()) mSettings.createInputLanguage(DEFAULT_INPUT_LANGUAGE);
        if (!mSettings.hasOutputLanguage()) mSettings.createOutputLanguage(DEFAULT_OUTPUT_LANGUAGE);

        mInputLanguage = mSettings.getInputLanguage();
        mOutputLanguage = mSettings.getOutputLanguage();
        Log.d(TAG, "INITIAL INPUT LANGUAGE: " + mInputLanguage);
        Log.d(TAG, "INITIAL OUTPUT LANGUAGE: " + mOutputLanguage);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = createGestureDetector(this);

        mTextView = (TextView) findViewById(R.id.hello_world_text);



        mSpeechKit = SpeechKit.initialize(this,
                SPEECH_KIT_APP_ID,
                SPEECH_KIT_URL,
                SPEECH_KIT_PORT,
                true,
                SPEECH_KIT_APP_KEY);
        mSpeechKit.connect();

        mRecognizer = mSpeechKit.createRecognizer(Recognizer.RecognizerType.Dictation,
                Recognizer.EndOfSpeechDetection.Short,
                mInputLanguage, this, new Handler());

        mRecognizer.start();
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
        return gestureDetector;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.input_engUSA:
                mSettings.setInputLanguage(R.string.engUSA_code);
                mInputLanguage = mSettings.getInputLanguage();
                Log.d(TAG, "INPUT LANGUAGE CHANGED: " + mInputLanguage);
                return true;
            case R.id.input_spaESP:
                mSettings.setInputLanguage(R.string.spaESP_code);
                mInputLanguage = mSettings.getInputLanguage();
                Log.d(TAG, "INPUT LANGUAGE CHANGED: " + mInputLanguage);
                return true;
            case R.id.output_engUSA:
                mSettings.setOutputLanguage(R.string.engUSA_code);
                mOutputLanguage = mSettings.getOutputLanguage();
                Log.d(TAG, "OUTPUT LANGUAGE CHANGED: " + mOutputLanguage);
                return true;
            case R.id.output_spaESP:
                mSettings.setOutputLanguage(R.string.spaESP_code);
                mOutputLanguage = mSettings.getOutputLanguage();
                Log.d(TAG, "OUTPUT LANGUAGE CHANGED: " + mOutputLanguage);
                return true;
            default:
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
    public void onRecordingBegin(Recognizer recognizer) {
        // TODO: Show some feedback to the user
        Log.d(TAG, "onRecordingBegin");
    }

    @Override
    public void onRecordingDone(Recognizer recognizer) {
        // TODO: Show some feedback to the user
        Log.d(TAG, "onRecordingDone");
    }

    @Override
    public void onResults(Recognizer recognizer, Recognition recognition) {
        Log.d(TAG, "onResults");
        if (recognition.getResultCount() > 0) {
            Log.d(TAG, "RECOGNIZED TEXT: " + recognition.getResult(0).getText());
            mTextView.setText(mTextView.getText() + " " + recognition.getResult(0).getText());
            // TODO: Record again
        }
    }

    @Override
    public void onError(Recognizer recognizer, SpeechError speechError) {
        Log.d(TAG, "onERROR: " + speechError.getErrorDetail());
    }

}
