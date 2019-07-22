package com.iwa.shenq_huang.linphone_example;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;

import org.linphone.core.Core;
import org.linphone.core.CoreListener;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.VideoDefinition;
import org.linphone.mediastream.video.AndroidVideoWindowImpl;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration;
import org.linphone.mediastream.video.display.GL2JNIView;

public class VideoActivity extends Activity implements View.OnClickListener {

    private VideoActivityReceiver mReceiver;
    public static final String RECEIVE_VIDEO_ACTIVITY = "receive_video_activity";

    //private  GLSurfaceView mRenderingView;
    private SurfaceView  mRenderingView,mPreviewView;
    private AndroidVideoWindowImpl mAndroidVideoWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        init();

        //广播
        IntentFilter intentFilter = new IntentFilter(RECEIVE_VIDEO_ACTIVITY);
        mReceiver = new VideoActivityReceiver();
        registerReceiver(mReceiver, intentFilter);

        fixZOrder(mRenderingView, mPreviewView);



        mAndroidVideoWindow = new AndroidVideoWindowImpl(mRenderingView, mPreviewView, new AndroidVideoWindowImpl.VideoWindowListener() {
            public void onVideoRenderingSurfaceReady(AndroidVideoWindowImpl vw, SurfaceView surface) {
                mRenderingView = surface;// 撥打過去Step1
                //LinphoneMiniManager.getLC().setVideoWindow(vw);

                //LinphoneMiniManager.getLC().setNativeVideoWindowId(vw);
                //LinphoneMiniManager.getLC().setNativeVideoWindowId(1);//前置攝像頭-1，後置攝像頭-0
                //LinphoneMiniManager.getLC().setNativeVideoWindowId(vw);

                LinphoneMiniManager.getLC().setNativeVideoWindowId(vw);
                //LinphoneMiniManager.getLC().enableVideoCapture(true);


                //LinphoneMiniManager.getLC().setVideoActivationPolicy();
/*
                try{
                    camera_To = Camera.open();
                }catch(RuntimeException e){
                    Log.e("tag", "init_camera: " + e);
                    return;
                }
                Camera.Parameters param;
                param = camera_To.getParameters();
                //modify parameter
                param.setPreviewFrameRate(20);
                param.setPreviewSize(376, 644);
                camera_To.setParameters(param);
                try {
                    camera_To.setPreviewDisplay(mRenderingView.getHolder());
                    camera_To.startPreview();
                }
                catch (Exception e){
                }
*/
            }

            public void onVideoRenderingSurfaceDestroyed(AndroidVideoWindowImpl vw) {
                Core linphoneCore = LinphoneMiniManager.getLC();//掛掉Step1
                if (linphoneCore != null) {
                    //linphoneCore.setVideoWindow(null);
                    linphoneCore.setNativeVideoWindowId(null);
                }
            }

            public void onVideoPreviewSurfaceReady(AndroidVideoWindowImpl vw, SurfaceView surface) {
                mPreviewView = surface;// 撥打過去Step2
                //LinphoneMiniManager.getLC().setPreviewWindow(mPreviewView);

                //LinphoneMiniManager.getLC().setNativeVideoWindowId(mPreviewView);//前置攝像頭-1，後置攝像頭-0
                LinphoneMiniManager.getLC().setNativePreviewWindowId(mPreviewView);
                LinphoneMiniManager.getLC().enableVideoPreview(true);

/*
                Camera camera;
                try{
                    camera = Camera.open();
                }catch(RuntimeException e){
                    Log.e("tag", "init_camera: " + e);
                    return;
                }
                Camera.Parameters param;
                param = camera.getParameters();
                //modify parameter
                param.setPreviewFrameRate(20);
                param.setPreviewSize(176, 144);
                camera.setParameters(param);
                try {
                    //camera.setPreviewDisplay(surface.getHolder());
                    camera.setPreviewDisplay(mPreviewView.getHolder());
                    camera.startPreview();
                }
                catch (Exception e){
                    }

*/

            }

            public void onVideoPreviewSurfaceDestroyed(AndroidVideoWindowImpl vw) {
                //掛掉Step2
                //LinphoneMiniManager.getLC().setPreviewWindow(null);
                LinphoneMiniManager.getLC().setNativePreviewWindowId(null);
            }
        });

    }

    private void fixZOrder(SurfaceView rendering, SurfaceView preview) {
        rendering.setZOrderOnTop(false);
        preview.setZOrderOnTop(true);
        preview.setZOrderMediaOverlay(true); // Needed to be able to display control layout over
    }



    private void init() {
        ((Button) findViewById(R.id.id_video_gua)).setOnClickListener(this);
        ((Button) findViewById(R.id.id_video_mute)).setOnClickListener(this);
        ((Button) findViewById(R.id.id_video_speaker)).setOnClickListener(this);
        ((Button) findViewById(R.id.id_video_qiev)).setOnClickListener(this);
        mRenderingView = (SurfaceView) findViewById(R.id.id_video_rendering);
        mPreviewView = (SurfaceView) findViewById(R.id.id_video_preview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRenderingView != null) {
            ((GLSurfaceView) mRenderingView).onResume();
        }

/*
        if (mAndroidVideoWindow != null) {
            LinphoneMiniManager.getLC().setNativeVideoWindowId(mAndroidVideoWindow);
        }
        */

        if (mAndroidVideoWindow != null) {

            synchronized(mAndroidVideoWindow) {
                //LinphoneMiniManager.getLC().setVideoWindow(mAndroidVideoWindow);
                LinphoneMiniManager.getLC().setNativeVideoWindowId(mAndroidVideoWindow);
                //LinphoneMiniManager.getLC().setNativeVideoWindowId(1);
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

/*
        if (mAndroidVideoWindow != null) {

            LinphoneMiniManager.getLC().setNativeVideoWindowId(null);
        }

        */

        if (mAndroidVideoWindow != null) {

            synchronized(mAndroidVideoWindow) {
                //LinphoneMiniManager.getLC().setVideoWindow(null);
                LinphoneMiniManager.getLC().setNativeVideoWindowId(null);
            }
        }


        if (mRenderingView != null) {
            ((GLSurfaceView) mRenderingView).onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        mPreviewView = null;
        mRenderingView = null;

        if (mAndroidVideoWindow != null) {
            mAndroidVideoWindow.release();
            mAndroidVideoWindow = null;
        }
    }

    public class VideoActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            switch (action) {
                case "end":
                    VideoActivity.this.finish();
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        LinphoneMiniManager instance = LinphoneMiniManager.getInstance();
        switch (view.getId()) {
            case R.id.id_video_gua:
                instance.hangUp();
                finish();
                break;
            case R.id.id_video_mute:

                break;
            case R.id.id_video_speaker:

                break;
            case R.id.id_video_qiev:
                switchCamera(instance);
                break;
            default:
                break;
        }
    }


    public void switchCamera(LinphoneMiniManager l) {
        try {
            String a = l.getLC().getVideoDevice();
            String Num = l.getLC().getVideoDevice().replace("Android","");
            int videoDeviceId = Integer.parseInt(Num);
            videoDeviceId = (videoDeviceId + 1) % AndroidCameraConfiguration.retrieveCameras().length;
            Toast.makeText(getApplicationContext(), "默認Toast樣"+mPreviewView+"式"+videoDeviceId,   Toast.LENGTH_SHORT).show();
            //前置攝像頭-1，後置攝像頭-0
            if(Num.equals("0")){
                l.getLC().setVideoDevice("1");
            }else{
                l.getLC().setVideoDevice("0");
            }

            l.updateCall();
            // previous call will cause graph reconstruction -> regive preview
            // window
            if (mPreviewView != null) {
                //l.getLC().setPreviewWindow(mPreviewView);
                l.getLC().setNativePreviewWindowId(mPreviewView);
            }

        } catch (ArithmeticException ae) { Log.e("tag","Cannot swtich camera : no camera"); }
    }

}
