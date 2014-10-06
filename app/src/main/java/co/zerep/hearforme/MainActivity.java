package co.zerep.hearforme;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.nuance.nmdp.speechkit.Recognition;
import com.nuance.nmdp.speechkit.Recognizer;
import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.SpeechKit;

public class MainActivity extends Activity implements Recognizer.Listener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SpeechKitAppId = "NMDPTRIAL_pedropobla20141006104322";
    private static final byte[] SpeechKitApplicationKey = {(byte)0x37, (byte)0xfe, (byte)0x63, (byte)0x6d, (byte)0xe8, (byte)0x5c, (byte)0xe0, (byte)0x78, (byte)0x98, (byte)0x72, (byte)0xa5, (byte)0xf4, (byte)0x33, (byte)0xc3, (byte)0x38, (byte)0xb3, (byte)0x73, (byte)0xbc, (byte)0xb8, (byte)0x40, (byte)0x54, (byte)0x65, (byte)0xd2, (byte)0x3f, (byte)0xaf, (byte)0xc2, (byte)0xb6, (byte)0x4c, (byte)0x35, (byte)0xdc, (byte)0x99, (byte)0x6d, (byte)0xdb, (byte)0xfc, (byte)0xda, (byte)0xc3, (byte)0x58, (byte)0xbb, (byte)0x3b, (byte)0xf1, (byte)0x2d, (byte)0xe1, (byte)0xe5, (byte)0x61, (byte)0xa4, (byte)0x1d, (byte)0x14, (byte)0x18, (byte)0xd9, (byte)0xcc, (byte)0x58, (byte)0x2d, (byte)0x31, (byte)0x2a, (byte)0x6d, (byte)0xe0, (byte)0xec, (byte)0x23, (byte)0x2c, (byte)0x22, (byte)0xff, (byte)0xc5, (byte)0x8f, (byte)0xda};

    private Context context;
    private SpeechKit sk;
    private TextView txtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtView = (TextView) findViewById(R.id.hello_world_text);
        context = getApplication().getApplicationContext();
        Log.d(TAG, "Context = " + context.toString());
        sk = SpeechKit.initialize(context,
                SpeechKitAppId,
                "sslsandbox.nmdp.nuancemobility.net",
                443,
                true,
                SpeechKitApplicationKey);
        Log.d(TAG, "sk initialized");
        sk.connect();
        Log.d(TAG, "sk connected");
        Handler handler = new Handler();
        Log.d(TAG, "handled instantiated");
        Recognizer recognizer = sk.createRecognizer(Recognizer.RecognizerType.Dictation,
                Recognizer.EndOfSpeechDetection.Short,
                "eng-USA", this, handler);
        recognizer.start();
        Log.d(TAG, "recognizer instantiated");
    }

    @Override
    protected void onDestroy() {
       super.onDestroy();
    }

    @Override
    public void onRecordingBegin(Recognizer recognizer) {
        Log.d(TAG, "onRecordingBegin");
    }

    @Override
    public void onRecordingDone(Recognizer recognizer) {
        Log.d(TAG, "onRecordingDone");
    }

    @Override
    public void onResults(Recognizer recognizer, Recognition recognition) {
        Log.d(TAG, "onResults starts here!!!");
        if (recognition.getResultCount() > 0) {
            Log.d(TAG, "LKAJSDHFLKAJSDHFLKASJDHFLKASJHDFLKASJDHFLKJASDHF " + recognition.getResult(0).getText());
            // do something with topResult...
        }
        Log.d(TAG, "onResults ends here!!!");
    }

    @Override
    public void onError(Recognizer recognizer, SpeechError speechError) {
        Log.d(TAG, "onERROR: " + speechError.getErrorDetail());
    }
}
