package co.zerep.hearforme;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.view.MenuUtils;

import co.zerep.hearforme.languages.Languages;
import co.zerep.hearforme.languages.language.Language;
import co.zerep.hearforme.settings.SettingsController;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private GestureDetector mGestureDetector;
    private AudioManager mAudioManager;
    private TextView mTextView;
    private ImageView mInputLanguageFlag;
    private ImageView mRightArrow;
    private ImageView mOutputLanguageFlag;
    private LayoutInflater mInflater;
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

        mInflater = LayoutInflater.from(this);
        setContentView(mInflater.inflate(R.layout.activity_main, null));

        mTextView = (TextView) findViewById(R.id.main_view_text);
        mTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        // TODO: Smooth scrolling

        mInputLanguageFlag = (ImageView) findViewById(R.id.input_language_flag);
        mOutputLanguageFlag = (ImageView) findViewById(R.id.output_language_flag);
        mRightArrow = (ImageView) findViewById(R.id.right_arrow);

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
        Log.d(TAG, "INITIAL INPUT LANGUAGE: " + mInputLanguage);
        Log.d(TAG, "INITIAL OUTPUT LANGUAGE: " + mOutputLanguage);

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

    private void setInputLanguage(Language lang) {
        Log.d(TAG, "INPUT LANGUAGE CHANGED: " + lang);
        SettingsController.setInputLanguage(lang);
        mInputLanguage = lang;
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
        Log.d(TAG, "OUTPUT LANGUAGE CHANGED: " + lang);
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

    public void showResults(String recognizedText) {
        mTextView.setText(mTextView.getText() + " " + recognizedText);
    }
}
