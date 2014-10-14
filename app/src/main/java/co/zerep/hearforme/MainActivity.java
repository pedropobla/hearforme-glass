package co.zerep.hearforme;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import co.zerep.hearforme.languages.Languages;
import co.zerep.hearforme.languages.language.Language;
import co.zerep.hearforme.settings.SettingsController;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private GestureDetector mGestureDetector;
    private AudioManager mAudioManager;
    private TextView mTextView;
    private ContinuousRecognizer mRecognizer;
    private Thread mRecognizerThread;
    private Language mInputLanguage;
    private Language mOutputLanguage;
    private final Language DEFAULT_INPUT_LANGUAGE = Languages.IN_ENGLISH_USA;
    private final Language DEFAULT_OUTPUT_LANGUAGE = Languages.OUT_SPANISH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ensure screen stays on.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        if (!SettingsController.hasInputLanguage())
            SettingsController.createInputLanguage(DEFAULT_INPUT_LANGUAGE);
        if (!SettingsController.hasOutputLanguage())
            SettingsController.createOutputLanguage(DEFAULT_OUTPUT_LANGUAGE);

        mInputLanguage = SettingsController.getInputLanguage();
        mOutputLanguage = SettingsController.getOutputLanguage();
        Log.d(TAG, "INITIAL INPUT LANGUAGE: " + mInputLanguage);
        Log.d(TAG, "INITIAL OUTPUT LANGUAGE: " + mOutputLanguage);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = createGestureDetector(this);

        mTextView = (TextView) findViewById(R.id.hello_world_text);

        mRecognizer = new ContinuousRecognizer(this, mInputLanguage.getCode());
        mRecognizerThread = new Thread(mRecognizer);
        mRecognizerThread.start();
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
        if (mRecognizerThread != null) {
            mRecognizer.terminate();
            try {
                mRecognizerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
    
    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        Menu in = menu.addSubMenu(R.string.input_language).setIcon(R.drawable.ic_input_language);
        for (Language lang : Languages.getInputLanguages()) {
            in.add(Menu.NONE, lang.getMenuId(), Menu.NONE, lang.getName()).setIcon(lang.getFlag());
        }

        if (mInputLanguage.isTranslatable()) {
            Menu out = menu.addSubMenu(R.string.output_language).setIcon(R.drawable.ic_output_language);
            for (Language lang : Languages.getOutputLanguages()) {
                out.add(Menu.NONE, lang.getMenuId(), Menu.NONE, lang.getName()).setIcon(lang.getFlag());
            }
        } else {
            menu.add(Menu.NONE, R.string.output_language, Menu.NONE, R.string.output_language).
                    setIcon(R.drawable.ic_output_language).setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Language lang = Languages.getLanguageFromMenuId(item.getItemId());
        if (lang != null) {
            if (lang.getType() == Language.Type.INPUT) {
                SettingsController.setInputLanguage(lang);
                mInputLanguage = SettingsController.getInputLanguage();
                if(!mInputLanguage.isTranslatable()) mOutputLanguage = null; // TODO: Check null
                Log.d(TAG, "INPUT LANGUAGE CHANGED: " + mInputLanguage);
            } else if(lang.getType() == Language.Type.OUTPUT) {
                SettingsController.setOutputLanguage(lang);
                mOutputLanguage = SettingsController.getOutputLanguage();
                Log.d(TAG, "OUTPUT LANGUAGE CHANGED: " + mOutputLanguage);
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

    public void showResults(String recognizedText) {
        mTextView.setText(mTextView.getText() + " " + recognizedText);
    }
}
