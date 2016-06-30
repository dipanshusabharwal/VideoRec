package com.example.deepankur.videorec;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.Connection;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

public class MainActivity extends AppCompatActivity implements KEY_IDS, Session.SessionListener, PublisherKit.PublisherListener, SubscriberKit.VideoListener {

    private static final String TAG = "My Own Activity";

    Session mSession;
    Publisher mPublisher;
    Subscriber mSubscriber;
    Button publishBtn, unPublishBtn, unSubscribeBtn, secretBtn;
    RelativeLayout publisherRl, subscriberRl, mainRl;
    TextView textView;
    EditText editText;
    Button sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        publishBtn = (Button) findViewById(R.id.publishBTN);
        unPublishBtn = (Button) findViewById(R.id.unpublishBTN);
        unSubscribeBtn = (Button) findViewById(R.id.unsubscribeBTN);
        secretBtn = (Button) findViewById(R.id.secretBTN);

        publisherRl = (RelativeLayout) findViewById(R.id.publisherRL);
        subscriberRl = (RelativeLayout) findViewById(R.id.subscriberRL);
        mainRl = (RelativeLayout) findViewById(R.id.mainRL);

        publishBtn.setOnClickListener(clickListener);
        unPublishBtn.setOnClickListener(clickListener);
        unSubscribeBtn.setOnClickListener(clickListener);
        secretBtn.setOnClickListener(clickListener);

        textView = (TextView) findViewById(R.id.textView);
        editText = (EditText) findViewById(R.id.editText);
        sendBtn = (Button) findViewById(R.id.sendBTN);
        sendBtn.setOnClickListener(clickListener);
        sessionConnect();


    }


    private void attachPublisherView(Publisher publisher) {
        mPublisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
        mPublisher.setPublishAudio(true);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                320, 240);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
//                RelativeLayout.TRUE);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
//                RelativeLayout.TRUE);
        publisherRl.addView(mPublisher.getView(), layoutParams);
    }

    private void attachSubscriberView(Subscriber subscriber) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                getResources().getDisplayMetrics().widthPixels, getResources()
                .getDisplayMetrics().heightPixels);
        subscriberRl.removeView(mSubscriber.getView());
        subscriberRl.addView(mSubscriber.getView(), layoutParams);
        subscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.publishBTN:
                    startPublishing();
                    break;
                case R.id.unpublishBTN:
                    stopPublishing();
                    break;
                case R.id.unsubscribeBTN:
                    stopSubscribing();
                    break;
                case R.id.secretBTN:
                    launchSecretActivity();
                    break;
                case R.id.sendBTN:
                    sendChatMessage(editText.getText().toString());
                    break;
            }
        }
    };

    private void sendChatMessage(String message) {
        mSession.sendSignal("chat", message);
        editText.setText("");

    }

    private void launchSecretActivity() {
        Intent i = new Intent(this, SecretActivity.class);
        startActivity(i);
    }

    private void stopSubscribing() {
        enableButton(unSubscribeBtn, false);
        if (subscriberRl.getChildAt(0) != null) {
            ViewGroup parent = (ViewGroup) subscriberRl.getChildAt(0).getParent();
            if (parent != null) {
                parent.removeView(subscriberRl.getChildAt(0));
            }
        }
        mSession.unsubscribe(mSubscriber);
    }

    private void stopPublishing() {
        if (publisherRl.getChildAt(0) != null) {
            ViewGroup parent = (ViewGroup) publisherRl.getChildAt(0).getParent();
            if (parent != null) {
                parent.removeView(publisherRl.getChildAt(0));
            }
        }
        mSession.unpublish(mPublisher);
    }

    private void startSubscribing(Stream stream) {
        unSubscribeBtn.setEnabled(true);
        if (mSubscriber == null) {
            mSubscriber = new Subscriber(MainActivity.this, stream);
            mSubscriber.setVideoListener(this);
        }
        mSession.subscribe(mSubscriber);
    }

    private void startPublishing() {
        if (mPublisher == null) {
            mPublisher = new Publisher(MainActivity.this, "publisher");
            mPublisher.setPublisherListener(this);
        }

        attachPublisherView(mPublisher);
        mSession.publish(mPublisher);
    }

    String textViewString = "";

    private void sessionConnect() {
        if (mSession == null) {
            mSession = new Session(MainActivity.this, API_KEY, SESSION_ID);
        }
        mSession.setSessionListener(this);
        mSession.setSignalListener(new Session.SignalListener() {
            @Override
            public void onSignalReceived(Session session, String s, String s1, Connection connection) {
                String myConnectionId = session.getConnection().getConnectionId();
                String theirConnectionId = connection.getConnectionId();
                Log.d(TAG, "s :" + s + " s1" + s1);
                if (!theirConnectionId.equals(myConnectionId)) {
                    textView.setText( s1);
                }
            }
        });
        mSession.connect(TOKEN);
    }

    @Override
    public void onConnected(Session session) {
        showToast("connected to Session");
        enableButton(publishBtn, true);
    }

    @Override
    public void onDisconnected(Session session) {
        showToast("disconnected From Session");
        showDialog(this);
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        showToast("another client publishing");
        startSubscribing(stream);
        enableButton(unSubscribeBtn, true);
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        showToast("Client Stopped Publishing");
        enableButton(unSubscribeBtn, false);
        stopSubscribing();
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        showToast("Error Connecting !");
        publishBtn.setEnabled(false);
    }

    /**
     * PublisherListener Implementation
     *
     * @param publisherKit
     * @param stream
     */
    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Toast.makeText(this, "publishing", Toast.LENGTH_SHORT).show();
        enableButton(publishBtn, false);
        enableButton(unPublishBtn, true);
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Toast.makeText(this, "stopped publishing", Toast.LENGTH_SHORT).show();
        enableButton(publishBtn, true);
        enableButton(unPublishBtn, false);
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Toast.makeText(this, "error Publishing", Toast.LENGTH_SHORT).show();
        publishBtn.setEnabled(true);
        unPublishBtn.setEnabled(false);
        unSubscribeBtn.setEnabled(false);
    }

    /**
     * VideoListener Implementation
     *
     * @param subscriberKit
     */
    @Override
    public void onVideoDataReceived(SubscriberKit subscriberKit) {
        attachSubscriberView(mSubscriber);
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

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void enableButton(View view, boolean state) {
        view.setEnabled(state);
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
                finish();
                System.exit(0);
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        System.exit(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            stopSubscribing();
            stopPublishing();
            mSession.disconnect();
        } catch (NullPointerException e) {
            Log.d(TAG, "" + e);
        }

    }
}
