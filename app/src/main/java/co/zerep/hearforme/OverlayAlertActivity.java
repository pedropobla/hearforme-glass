package co.zerep.hearforme;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardBuilder;

public class OverlayAlertActivity extends Activity {
    private static final String TAG = OverlayAlertActivity.class.getSimpleName();

    private GestureDetector mGestureDetector;
    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = createGestureDetector(this);

        // This activity will be closed automatically after some time by running this thread
        Runnable closeActivity = new Runnable() {
            @Override
            public void run() {
                finish();
            }
        };

        boolean success = getIntent().getExtras().getBoolean("success");
        int iconId; // icon to show in the overlay alert
        int textId; // text to show in the overlay alert
        int soundId; // sound to play when the alert is shown
        int activeTime; // duration (in ms) of the alert. The activity will be finished afterwards.

        if (success) {
            soundId = Sounds.SUCCESS;
            textId = R.string.overlay_success;
            iconId = R.drawable.ic_done_150_green_trans;
            activeTime = R.integer.overlay_successTime;
        } else {
            soundId = Sounds.DISALLOWED;
            textId = R.string.overlay_failure;
            iconId = R.drawable.ic_no_150_red_trans;
            activeTime = R.integer.overlay_failureTime;
        }

        CardBuilder mAlertCard = new CardBuilder(this, CardBuilder.Layout.ALERT)
                .setText(textId)
                .setIcon(iconId);
        setContentView(mAlertCard.getView());

        mAudioManager.playSoundEffect(soundId);

        new Handler().postDelayed(closeActivity,
                getResources().getInteger(activeTime));
    }

    @Override
    protected void onPause(){
        super.onPause();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                // Any gesture performed on the touchpad will close the overlay message activity
                finish();
                return true;
            }
        });
        return gestureDetector;
    }
}
