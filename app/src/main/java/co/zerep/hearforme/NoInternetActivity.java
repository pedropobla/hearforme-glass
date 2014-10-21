package co.zerep.hearforme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardBuilder;


public class NoInternetActivity extends Activity implements NetworkStateReceiver.NetworkStateReceiverListener {
    // TODO: Create a thread that continuously checks (poll) for internet connection, for faster response time

    private static final String TAG = NoInternetActivity.class.getSimpleName();

    private NetworkStateReceiver mNetworkStateReceiver;
    private CardBuilder mAlertCard;
    private AudioManager mAudioManager;
    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAlertCard = new CardBuilder(this, CardBuilder.Layout.ALERT)
                .setText(R.string.no_internet_alert)
                .setFootnote(R.string.no_internet_alert_description)
                .setIcon(R.drawable.ic_cloud_sad_big);
        setContentView(mAlertCard.getView());

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = createGestureDetector(this);

        mNetworkStateReceiver = new NetworkStateReceiver();
        mNetworkStateReceiver.addListener(this);
    }

    @Override
    protected void onDestroy() {
        if (mNetworkStateReceiver != null) {
            mNetworkStateReceiver.removeListener(this);
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mNetworkStateReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        if (mNetworkStateReceiver != null) {
            try {
                unregisterReceiver(mNetworkStateReceiver);
            } catch (IllegalArgumentException iae) {
                Log.e(TAG, "Tried to unregister an already-unregistered receiver.");
            }
        }
        super.onPause();
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
    public void networkAvailable() {
        // It has no sense to show a no-internet message if the user has internet. Go away!
        this.finish();
    }

    @Override
    public void networkUnavailable() {
        // This method has not been implemented on purpose.
    }

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.TAP) {
                    mAudioManager.playSoundEffect(Sounds.TAP);
                    Log.d(TAG, "Opening wifi settings...");
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    return true;
                }
                return false;
            }
        });
        return gestureDetector;
    }
}
