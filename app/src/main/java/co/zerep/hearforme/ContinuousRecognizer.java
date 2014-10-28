package co.zerep.hearforme;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.SpeechKit;

import java.util.Timer;
import java.util.TimerTask;

public class ContinuousRecognizer implements Runnable, Recognizer.Listener {

    private enum Status {RECORDING, PROCESSING, IDLE}
    private static final String TAG = ContinuousRecognizer.class.getSimpleName();

    private volatile boolean running = true, active = true;

    private static final String SPEECH_KIT_APP_ID = PrivateKeys.SPEECH_KIT_APP_ID;
    private static final byte[] SPEECH_KIT_APP_KEY = PrivateKeys.SPEECH_KIT_APP_KEY;
    private static final String SPEECH_KIT_URL = "sslsandbox.nmdp.nuancemobility.net";
    private static final int SPEECH_KIT_PORT = 443;

    private SpeechKit speechKit;
    private String langCode;
    private Recognizer recognizers[];
    private Status status[];
    private MainActivity mainActivity;
    private Timer audioLevelTimer;

    public ContinuousRecognizer(final MainActivity mainActivity) {
        this.speechKit = SpeechKit.initialize(mainActivity.getApplication().getApplicationContext(),
                SPEECH_KIT_APP_ID,
                SPEECH_KIT_URL,
                SPEECH_KIT_PORT,
                true,
                SPEECH_KIT_APP_KEY);
        speechKit.connect();
        this.mainActivity = mainActivity;
        this.langCode = "";
        this.recognizers = new Recognizer[2];
        this.status = new Status[] {Status.IDLE, Status.IDLE};
        audioLevelTimer = new Timer();
        audioLevelTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!active) return;
                if (status[0] == Status.RECORDING) {
                    mainActivity.onAudioLevelChanged(recognizers[0].getAudioLevel());
                } else if (status[1] == Status.RECORDING) {
                    mainActivity.onAudioLevelChanged(recognizers[1].getAudioLevel());
                } else {
                    mainActivity.onAudioLevelChanged(0);
                }
            }
        }, 0, mainActivity.getResources().getInteger(R.integer.audio_level_check_rate));
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    @Override
    public void onRecordingBegin(Recognizer recognizer) {
        if (recognizer == recognizers[0]) {
            Log.d(TAG, "onRecordingBegin 0");
            status[0] = Status.RECORDING;
        }
        else if (recognizer == recognizers[1]) {
            Log.d(TAG, "onRecordingBegin 1");
            status[1] = Status.RECORDING;
        }
        mainActivity.onRecordingBegin();
    }

    @Override
    public void onRecordingDone(Recognizer recognizer) {
        if (recognizer == recognizers[0]){
            Log.d(TAG, "onRecordingDone 0");
            status[0] = Status.PROCESSING;
        }
        else if (recognizer == recognizers[1]) {
            Log.d(TAG, "onRecordingDone 1");
            status[1] = Status.PROCESSING;
        }
        mainActivity.onRecordingDone();
    }

    @Override
    public void onResults(Recognizer recognizer, Recognition recognition) {
        if (recognition.getResultCount() > 0) {
            Log.d(TAG, "RECOGNIZED TEXT: " + recognition.getResult(0).getText());
            mainActivity.onResults(recognition.getResult(0).getText());
        }
        if (recognizer == recognizers[0]) {
            Log.d(TAG, "onResults 0");
            status[0] = Status.IDLE;
        }
        else if (recognizer == recognizers[1]) {
            Log.d(TAG, "onResults 1");
            status[1] = Status.IDLE;
        }
    }

    @Override
    public void onError(Recognizer recognizer, SpeechError speechError) {
        if (recognizer == recognizers[0]) {
            Log.d(TAG, "onError 0");
            status[0] = Status.IDLE;
        }
        else if (recognizer == recognizers[1]) {
            Log.d(TAG, "onError 1");
            status[1] = Status.IDLE;
        }
        Log.d(TAG, "Error " + speechError.getErrorCode() + ": " + speechError.getErrorDetail());
        Log.d(TAG, "Suggestion: " + speechError.getSuggestion());
        mainActivity.onError(speechError.getErrorCode(), speechError.getErrorDetail(),
                speechError.getSuggestion());
    }

    public void stopRecording() {
        for (Recognizer recognizer : recognizers) {
            if (recognizer != null) {
                recognizer.stopRecording();
            }
        }
    }

    public void pause() {
        Log.d(TAG, "Pause.");
        active = false;
        for (Recognizer recognizer : recognizers) {
            if (recognizer != null) {
                recognizer.stopRecording();
                recognizer.cancel();
            }
        }
        recognizers = new Recognizer[2];
        status = new Status[] {Status.IDLE, Status.IDLE};
    }

    public void resume() {
        Log.d(TAG, "Resume.");
        active = true;
    }

    public void terminate() {
        Log.d(TAG, "Terminate.");
        active = false;
        running = false;
        for (Recognizer recognizer : recognizers) {
            if (recognizer != null) {
                recognizer.stopRecording();
                recognizer.cancel();
            }
        }
        speechKit.cancelCurrent();
        speechKit.release();
    }

    @Override
    public void run() {
        while(running) {
            if (active) {
                if (status[0] == Status.IDLE && status[1] != Status.RECORDING) {
                    startRecording(0);
                }
//                else if (status[1] == Status.IDLE && status[0] != Status.RECORDING) {
//                    startRecording(1);
//                }
            }
        }
    }

    private void startRecording(int index) {
        status[index] = Status.RECORDING;
        recognizers[index] = null;
        recognizers[index] = speechKit.createRecognizer(Recognizer.RecognizerType.Dictation,
                Recognizer.EndOfSpeechDetection.Short,
                langCode, this, new Handler(Looper.getMainLooper()));
        recognizers[index].start();
        Log.d(TAG, "Recognizer " + index + " started.");
    }
}
