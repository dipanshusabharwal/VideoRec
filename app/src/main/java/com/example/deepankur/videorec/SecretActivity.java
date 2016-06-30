package com.example.deepankur.videorec;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import java.util.ArrayList;

/**
 * Created by deepankur on 3/3/16.
 */
public class SecretActivity extends FragmentActivity implements KEY_IDS, Session.SessionListener {
    Session mSession;
    RelativeLayout rl1, rl2;
    ArrayList<Subscriber> mSubscriberList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret);
        mSubscriberList = new ArrayList<>();
        rl1 = (RelativeLayout) findViewById(R.id.RL1);
        rl2 = (RelativeLayout) findViewById(R.id.RL2);
        sessionConnect();
    }

    String TAG = "SecretActivity";

    private void attachSubscriberView(Subscriber subscriber) {
        Log.d(TAG, " attachSubscriberView");
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                getResources().getDisplayMetrics().widthPixels, getResources()
                .getDisplayMetrics().heightPixels);
        RelativeLayout relativeLayout;
        if (mSubscriberList.size() == 1) {
            relativeLayout = rl1;
        } else {
            relativeLayout = rl2;
        }
        relativeLayout.removeView(subscriber.getView());
        relativeLayout.addView(subscriber.getView(), layoutParams);
        subscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
    }

    private void sessionConnect() {
        if (mSession == null) {
            mSession = new Session(SecretActivity.this, API_KEY, SESSION_ID);
        }
        mSession.setSessionListener(this);
        mSession.connect(SECRET_TOKEN);
    }

    /**
     * SessionListeners Implementation
     *
     * @param session
     */
    @Override
    public void onConnected(Session session) {
        showToast("connected to Session");
    }

    @Override
    public void onDisconnected(Session session) {
        showToast("disconnected From Session");
        showDialog(this);
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        showToast("another client publishing");
        Log.d(TAG," onStreamReceived");
        startSubscribing(stream);
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void createLayout() {
        RelativeLayout relativeLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(100, 100);
        relativeLayout.setLayoutParams(layoutParams);
        rl1.addView(relativeLayout);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSession.disconnect();
        finish();
        onBackPressed();
    }

    private void startSubscribing(Stream stream) {
        final Subscriber subscriber = new Subscriber(this, stream);
        mSubscriberList.add(subscriber);
        subscriber.setVideoListener(new SubscriberKit.VideoListener() {
            @Override
            public void onVideoDataReceived(SubscriberKit subscriberKit) {
                Log.d(TAG," onVideoDataReceived");
                attachSubscriberView(subscriber);
            }

            @Override
            public void onVideoDisabled(SubscriberKit subscriberKit, String s) {
                showToast("subscriber Video Disabled");
            }

            @Override
            public void onVideoEnabled(SubscriberKit subscriberKit, String s) {
                showToast("subscriber Video enabled");
            }

            @Override
            public void onVideoDisableWarning(SubscriberKit subscriberKit) {

            }

            @Override
            public void onVideoDisableWarningLifted(SubscriberKit subscriberKit) {

            }
        });
        mSession.subscribe(subscriber);
    }


    public void showDialog(Activity activity) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.alert_dialog);
        Button ok = (Button) dialog.findViewById(R.id.Ok_action);
        Button cancel = (Button) dialog.findViewById(R.id.cancel_action);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionConnect();
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                onBackPressed();
            }
        });
        dialog.show();
    }
}
