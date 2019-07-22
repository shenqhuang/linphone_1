package com.iwa.shenq_huang.linphone_example;

import org.linphone.core.AVPFMode;
import org.linphone.core.Address;
import org.linphone.core.AuthInfo;
import org.linphone.core.AuthMethod;
import org.linphone.core.Call;
import org.linphone.core.CallLog;
import org.linphone.core.CallParams;
import org.linphone.core.CallStats;
import org.linphone.core.ChatMessage;
import org.linphone.core.ChatRoom;
import org.linphone.core.ConfiguringState;
import org.linphone.core.Content;
import org.linphone.core.Core;
import org.linphone.core.CoreException;
import org.linphone.core.CoreListener;



import org.linphone.core.Call;
import org.linphone.core.CallLog;
import org.linphone.core.CallStats;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.linphone.core.EcCalibratorStatus;
import org.linphone.core.Event;
import org.linphone.core.Factory;
import org.linphone.core.Friend;
import org.linphone.core.FriendList;
import org.linphone.core.GlobalState;
import org.linphone.core.InfoMessage;
import org.linphone.core.PresenceModel;
import org.linphone.core.ProxyConfig;
import org.linphone.core.PublishState;
import org.linphone.core.RegistrationState;
import org.linphone.core.SubscriptionState;
import org.linphone.core.Transports;
import org.linphone.core.VersionUpdateCheckResult;
import org.linphone.mediastream.Log;
import org.linphone.mediastream.video.AndroidVideoWindowImpl;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration.AndroidCamera;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Camera;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.SurfaceView;


//public class LinphoneMiniManager implements CoreListener {
//public class LinphoneMiniManager extends Service implements CoreListener {
public class LinphoneMiniManager extends Service implements CoreListener {
    private static LinphoneMiniManager mInstance;
    private Context mContext;
    private Core mLinphoneCore;

    private Timer mTimer;
    //Core core;

    public LinphoneMiniManager(Context c) {
        mContext = c;

        /*
        Factory.instance().setDebugMode(true, "Linphone Mini");
        //Factory.instance().setDebugMode(true, "Linphone");
// You must provide the Android app context as createCore last param !
        //core = Factory.instance().createCore(null, null, c);


        try {
            String basePath = mContext.getFilesDir().getAbsolutePath();
            copyAssetsFromPackage(basePath);
            //mLinphoneCore = Factory.instance().createLinphoneCore(this, basePath + "/.linphonerc", basePath + "/linphonerc", null, mContext);
            //mLinphoneCore = Factory.instance().createLinphoneCore(this, basePath + "/.linphonerc", basePath + "/linphonerc", null, mContext);
            mLinphoneCore = Factory.instance().createCore( basePath + "/.linphonerc", basePath + "/linphonerc",mContext);

            initLinphoneCoreValues(basePath);

            setUserAgent();
            setFrontCamAsDefault();
            startIterate();
            mInstance = this;
            mLinphoneCore.setNetworkReachable(true); // Let's assume it's true
        } catch (IOException e) {
        }
        */



        onCreate();
        isReady();

    }
/*
    public void ReSetContent(Context in_content){
        mContext = in_content;
    }
*/


    //上面的改为
    private Factory lcFactory;
    public static boolean isReady() {
        return mInstance != null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //mContext = this;
        lcFactory = Factory.instance();
        lcFactory.setDebugMode(true, "lilinaini 1");
        Factory.instance().setDebugMode(true, "Linphone");
        // You must provide the Android app context as createCore last param !
        //Core core = Factory.instance().createCore(null, null, this);

        try {
            String basePath = mContext.getFilesDir().getAbsolutePath();
            copyAssetsFromPackage(basePath);
            //mLinphoneCore = lcFactory.createCore(this, basePath + "/.linphonerc", basePath + "/linphonerc", null, mContext);// 原始的
            //mLinphoneCore = Factory.instance().createCore( basePath + "/.linphonerc", basePath + "/linphonerc",mContext);
            mLinphoneCore = Factory.instance().createCore( basePath + "/.linphonerc", basePath + "/.linphonerc",mContext);
            //mLinphoneCore = Factory.instance().createCore( basePath + "/linphonerc", basePath + "/linphonerc",mContext);
            //mLinphoneCore = Factory.instance().createCore( null, null,mContext);


            initLinphoneCoreValues(basePath);

            setUserAgent();
            setFrontCamAsDefault();
            startIterate();
            mInstance = this;
            mLinphoneCore.setNetworkReachable(true); // Let's assume it's true
            //mLinphoneCore.start();



        } catch (IOException e) { }

    }


    public static Core getLC() {
        if(null==mInstance){
            return null;
        }
        return mInstance.mLinphoneCore;

        //setVideoWindow
    }

    public void updateCall() {
        Call lCall = mLinphoneCore.getCurrentCall();
        if (lCall == null) {
            Log.e("Trying to updateCall while not in call: doing nothing");
            return;
        }
        mLinphoneCore.updateCall(lCall, null);
    }






//===============================================================================================================================================================================================================================

    public static LinphoneMiniManager getInstance() {
        return mInstance;
    }

    public void destroy() {
        try {
            mTimer.cancel();
            mInstance.destroy();
        }
        catch (RuntimeException e) {
        }
        finally {
            mLinphoneCore = null;
            mInstance = null;
        }
    }

    private void startIterate() {
        TimerTask lTask = new TimerTask() {
            @Override
            public void run() {
                mLinphoneCore.iterate();
            }
        };

        /*use schedule instead of scheduleAtFixedRate to avoid iterate from being call in burst after cpu wake up*/
        mTimer = new Timer("LinphoneMini scheduler");
        mTimer.schedule(lTask, 0, 20);
    }

    private void setUserAgent() {
        try {
            String versionName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
            if (versionName == null) {
                versionName = String.valueOf(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode);
            }
            mLinphoneCore.setUserAgent("LinphoneMiniAndroid", versionName);
        } catch (NameNotFoundException e) {
        }
    }

    private void setFrontCamAsDefault() {
        int camId = 0;
        AndroidCamera[] cameras = AndroidCameraConfiguration.retrieveCameras();
        for (AndroidCamera androidCamera : cameras) {
            if (androidCamera.frontFacing)
                camId = androidCamera.id;
        }
        mLinphoneCore.setVideoDevice(String.valueOf(camId));
    }

    private void copyAssetsFromPackage(String basePath) throws IOException {
        LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.oldphone_mono, basePath + "/oldphone_mono.wav");
        LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.ringback, basePath + "/ringback.wav");
        LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.toy_mono, basePath + "/toy_mono.wav");
        LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.linphonerc_default, basePath + "/.linphonerc");
        LinphoneMiniUtils.copyFromPackage(mContext, R.raw.linphonerc_factory, new File(basePath + "/linphonerc").getName());
        LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.lpconfig, basePath + "/lpconfig.xsd");
        LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.rootca, basePath + "/rootca.pem");
    }

    private void initLinphoneCoreValues(String basePath) {
        //mLinphoneCore.setContext(mContext);

        mLinphoneCore.setRing( basePath + "/oldphone_mono.wav");//null gai
        //mLinphoneCore.setRing(null);
        mLinphoneCore.setRootCa(basePath + "/rootca.pem");
        mLinphoneCore.setPlayFile(basePath + "/toy_mono.wav");
        mLinphoneCore.setChatDatabasePath(basePath + "/linphone-history.db");

        int availableCores = Runtime.getRuntime().availableProcessors();
        //mLinphoneCore.setCpuCount(availableCores);
    }

    /*
    @Override
    public void globalState(Core lc, GlobalState state, String message) {
        Log.d("Global state: " + state + "(" + message + ")");
    }

    @Override
    public void callState(Core lc, Call call, Call.State cstate,
                          String message) {
        Log.d("Call state: " + cstate + "(" + message + ")");
    }


    @Override
    public void registrationState(Core lc, LinphoneProxyConfig cfg,
                                  RegistrationState cstate, String smessage) {
        Log.d("Registration state: " + cstate + "(" + smessage + ")");
    }

    @Override
    public void messageReceived(Core lc, ChatRoom cr,
                                ChatMessage message) {
        Log.d("Message received from " + cr.getPeerAddress().asString() + " : " + message.getText() + "(" + message.getExternalBodyUrl() + ")");
    }

    @Override
    public void isComposingReceived(Core lc, ChatRoom cr) {
        Log.d("Composing received from " + cr.getPeerAddress().asString());
    }
*/



    @Override
    public void onGlobalStateChanged(Core core, GlobalState globalState, String s) {
        String a = "";
    }

    @Override
    public void onRegistrationStateChanged(Core core, ProxyConfig proxyConfig, RegistrationState registrationState, String s) {
        String a  ="";
        //Log.e("lilin Registration state: " + registrationState + "(" + s + ")");
/*
        //Log.e("lilin Registration state: " + cstate + "(" + smessage + ")");
        Intent intent = new Intent(MainActivity.RECEIVE_MAIN_ACTIVITY);
        intent.putExtra("action", "reg_state");
        intent.putExtra("data", s);
        sendBroadcast(intent);
        */
    }




/*
    public void registrationState(Core lc, ProxyConfig cfg,
                                  RegistrationState cstate, String smessage) {
        //Log.e("lilin Registration state: " + cstate + "(" + smessage + ")");
        Intent intent = new Intent(MainActivity.RECEIVE_MAIN_ACTIVITY);
        intent.putExtra("action", "reg_state");
        intent.putExtra("data", smessage);
        sendBroadcast(intent);

    }
*/
/*
    @Override
    public void registrationState(LinphoneCore lc, LinphoneProxyConfig cfg,
                                  RegistrationState cstate, String smessage) {
        Log.e("lilin Registration state: " + cstate + "(" + smessage + ")");
    }
    */



    @Override
    public void onCallStateChanged(Core core, Call call, Call.State state, String s) {
        String a = "";
    }

    @Override
    public void onNotifyPresenceReceived(Core core, Friend friend) {
        String a = "";
    }

    @Override
    public void onNotifyPresenceReceivedForUriOrTel(Core core, Friend friend, String s, PresenceModel presenceModel) {
        String a = "";
    }

    @Override
    public void onNewSubscriptionRequested(Core core, Friend friend, String s) {
        String a = "";
    }

    @Override
    public void onAuthenticationRequested(Core core, AuthInfo authInfo, AuthMethod authMethod) {
        String a = "";
    }

    @Override
    public void onCallLogUpdated(Core core, CallLog callLog) {
        String a = "";
    }

    @Override
    public void onMessageReceived(Core core, ChatRoom chatRoom, ChatMessage chatMessage) {
        String a = "";
    }

    @Override
    public void onMessageReceivedUnableDecrypt(Core core, ChatRoom chatRoom, ChatMessage chatMessage) {
        String a = "";
    }

    @Override
    public void onIsComposingReceived(Core core, ChatRoom chatRoom) {
        String a = "";
    }

    @Override
    public void onDtmfReceived(Core core, Call call, int i) {
        String a = "";
    }

    @Override
    public void onReferReceived(Core core, String s) {
        String a = "";
    }

    @Override
    public void onCallEncryptionChanged(Core core, Call call, boolean b, String s) {
        String a = "";
    }

    @Override
    public void onTransferStateChanged(Core core, Call call, Call.State state) {
        String a = "";
    }

    @Override
    public void onBuddyInfoUpdated(Core core, Friend friend) {
        String a = "";
    }

    @Override
    public void onCallStatsUpdated(Core core, Call call, CallStats callStats) {
        String a = "";
    }

    @Override
    public void onInfoReceived(Core core, Call call, InfoMessage infoMessage) {
        String a = "";
    }

    @Override
    public void onSubscriptionStateChanged(Core core, Event event, SubscriptionState subscriptionState) {
        String a = "";
    }

    @Override
    public void onNotifyReceived(Core core, Event event, String s, Content content) {
        Log.d("Notify received: " + event + " -> " );
    }

    @Override
    public void onSubscribeReceived(Core core, Event event, String s, Content content) {
        String a = "";
    }

    @Override
    public void onPublishStateChanged(Core core, Event event, PublishState publishState) {
        String a = "";
    }

    @Override
    public void onConfiguringStatus(Core core, ConfiguringState configuringState, String s) {
        String a = "";
        Log.d("Configuration state: " + configuringState + "(" + s + ")");
    }

    @Override
    public void onNetworkReachable(Core core, boolean b) {
        String a = "";
    }

    @Override
    public void onLogCollectionUploadStateChanged(Core core, Core.LogCollectionUploadState logCollectionUploadState, String s) {
        String a = "";
    }

    @Override
    public void onLogCollectionUploadProgressIndication(Core core, int i, int i1) {
        String a = "";
    }

    @Override
    public void onFriendListCreated(Core core, FriendList friendList) {
        String a = "";
    }

    @Override
    public void onFriendListRemoved(Core core, FriendList friendList) {
        String a = "";
    }

    @Override
    public void onCallCreated(Core core, Call call) {
        String a = "";
    }

    @Override
    public void onVersionUpdateCheckResultReceived(Core core, VersionUpdateCheckResult versionUpdateCheckResult, String s, String s1) {
        String a = "";
    }

    @Override
    public void onChatRoomStateChanged(Core core, ChatRoom chatRoom, ChatRoom.State state) {
        String a = "";
    }

    @Override
    public void onQrcodeFound(Core core, String s) {
        String a = "";
    }

    @Override
    public void onEcCalibrationResult(Core core, EcCalibratorStatus ecCalibratorStatus, int i) {
        String a = "";
    }

    @Override
    public void onEcCalibrationAudioInit(Core core) {
        String a = "";
    }

    @Override
    public void onEcCalibrationAudioUninit(Core core) {
        String a = "";
    }

    public void lilin_reg(String sipAddress,String password,String port) throws CoreException{

        //sipAddress="sip:test2@172.22.123.16"
        //password="test"


        Address address = mLinphoneCore.createAddress(sipAddress);
        String username = address.getUsername();
        String domain = address.getDomain();
        ProxyConfig[] proxyConfigList = mLinphoneCore.getProxyConfigList();
        for (ProxyConfig linphoneProxyConfig : proxyConfigList) {
            mLinphoneCore.removeProxyConfig(linphoneProxyConfig);
        }//删除原来的  

        //======================================================================
        String identity = sipAddress;
        String UserName = identity.substring(4,identity.indexOf("@"));
        //String proxy = "sip:" + "172.22.123.16";
        String proxy = "sip:" + "sip.linphone.org";
/*


        String strIdetify = sipAddress;
        String strPassword = password;
        if (strPassword != null) {
            // create authentication structure from identity and add to core
            mLinphoneCore.addAuthInfo(Factory.instance().createAuthInfo("test2", null, password, null, null, "172.22.123.16"));
        }
        ProxyConfig proxyCfg = mLinphoneCore.createProxyConfig();
        proxyCfg.setExpires(2000);
        mLinphoneCore.addProxyConfig(proxyCfg);
        mLinphoneCore.setDefaultProxyConfig(proxyCfg);
        mLinphoneCore.getDefaultProxyConfig().edit();
        mLinphoneCore.getDefaultProxyConfig().enableRegister(true);
        mLinphoneCore.getDefaultProxyConfig().done();
        //mLinphoneCore.getDefaultProxyConfig().edit();
        //mLinphoneCore.getDefaultProxyConfig().enableRegister(true);
        //mLinphoneCore.getDefaultProxyConfig().done();
*/


        Address proxyAddress = Factory.instance().createAddress(proxy);
        Address identityAddress = Factory.instance().createAddress(identity);
        if (proxyAddress == null || identityAddress == null) {
            throw new CoreException("Proxy or Identity address is null.");
        }

        ProxyConfig mProxyConfig = mLinphoneCore.createProxyConfig();
        mProxyConfig.setIdentityAddress(identityAddress);
        mProxyConfig.setServerAddr(proxyAddress.asStringUriOnly());
        mProxyConfig.setAvpfMode(AVPFMode.Disabled);
        mProxyConfig.setAvpfRrInterval(0);
        mProxyConfig.enableQualityReporting(false);
        mProxyConfig.setQualityReportingCollector(null);
        mProxyConfig.setQualityReportingInterval(0);
        //        mProxyConfig.setRoute(proxyAddress.asStringUriOnly());
        mProxyConfig.enableRegister(true);

        //Factory mAuthInfo = Factory.instance();
        //AuthInfo mAuthInfo =  Factory.instance().createAuthInfo(UserName, null, password, null, null, "172.22.123.16");
        AuthInfo mAuthInfo =  Factory.instance().createAuthInfo(UserName, null, password, null, null, "sip.linphone.org");

        Transports transports = mLinphoneCore.getTransports();
        transports.setUdpPort(-1);
        transports.setTlsPort(-1);
        transports.setTcpPort(Integer.parseInt(port));
        mLinphoneCore.setTransports(transports);

        mLinphoneCore.addProxyConfig(mProxyConfig);
        mLinphoneCore.addAuthInfo((AuthInfo) mAuthInfo);
        mLinphoneCore.setDefaultProxyConfig(mProxyConfig);
        mLinphoneCore.addListener(getCoreListener());
        mLinphoneCore.start();


/*
        Factory lcFactory = Factory.instance();

        //mLinphoneCore.addAuthInfo(lcFactory.createAuthInfo(username, password, null, domain+":"+port));// 原始的
        //mLinphoneCore.addAuthInfo(lcFactory.createAuthInfo(username,"test2",password,null,port,domain));
        //mLinphoneCore.addAuthInfo(lcFactory.createAuthInfo(username,null,password,null,port,domain));
        mLinphoneCore.addAuthInfo(Factory.instance().createAuthInfo("test2", null, password, null, null, "172.22.123.16"));

        // create proxy config
        //LinphoneProxyConfig proxyCfg = mLinphoneCore.createProxyConfig(sipAddress, domain+":"+port, null, true);//原始的
        ProxyConfig proxyCfg = mLinphoneCore.createProxyConfig();
        proxyCfg.enablePublish(true);
        proxyCfg.setExpires(2000);
        mLinphoneCore.addProxyConfig(proxyCfg); // add it to linphone
        mLinphoneCore.setDefaultProxyConfig(proxyCfg);//注册一次就好了  下次启动就不用注册
        mLinphoneCore.start();
*/

    }

/*
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    */

    String TAG  ="tag";
    //private LinphoneCallback mCallback;
    //private Callback mCallback;
    private RegistrationState mRegistrationState = RegistrationState.None;


    private CoreListener getCoreListener() {
        return new CoreListener() {
            @Override
            public void onGlobalStateChanged(Core core, GlobalState globalState, String s) {
                Log.d(TAG, "Core listener - Global State Changed: " + s);
                //mCallback.onGlobalStateChanged(globalState);
            }

            @Override
            public void onRegistrationStateChanged(Core core, ProxyConfig proxyConfig,
                                                   RegistrationState registrationState,
                                                   String state) {
                Log.d(TAG, "Core listener - On Registration State Changed: " +
                        state);

                if (registrationState != mRegistrationState) {
                    //mCallback.onRegistrationStateChanged(registrationState);


                    Intent intent = new Intent(((MainActivity)mContext).RECEIVE_MAIN_ACTIVITY);
                    intent.putExtra("action", "reg_state");
                    intent.putExtra("data", state);
                    mContext.sendBroadcast(intent);

                }
                mRegistrationState = registrationState;

            }

            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String s) {
                Log.d(TAG, "Core listener - On Call State Changed");
                //mInstance.onCallStateChanged();
            }

            @Override
            public void onNotifyPresenceReceived(Core core, Friend friend) {
                Log.d(TAG, "Core listener - On Notify Presence Received");
            }

            @Override
            public void onNotifyPresenceReceivedForUriOrTel(Core core, Friend friend, String s,
                                                            PresenceModel presenceModel) {
                Log.d(TAG, "Core listener - On Notify Presence Received For Uri Or Tel");
            }

            @Override
            public void onNewSubscriptionRequested(Core core, Friend friend, String s) {
                Log.d(TAG, "Core listener - On New Subscription Requested");
            }

            @Override
            public void onAuthenticationRequested(Core core, AuthInfo authInfo, AuthMethod
                    authMethod) {
                Log.d(TAG, "Core listener - On Authentication Requested");
            }

            @Override
            public void onCallLogUpdated(Core core, CallLog callLog) {
                Log.d(TAG, "Core listener - On Call Log Updated");
            }

            @Override
            public void onMessageReceived(Core core, ChatRoom chatRoom, ChatMessage chatMessage) {
                Log.d(TAG, "Core listener - On Message Received");
            }

            @Override
            public void onMessageReceivedUnableDecrypt(Core core, ChatRoom chatRoom, ChatMessage
                    chatMessage) {
                Log.d(TAG, "Core listener - On Message Received Unable Decrypt");
            }

            @Override
            public void onIsComposingReceived(Core core, ChatRoom chatRoom) {
                Log.d(TAG, "Core listener - On Is Composing Received");
            }

            @Override
            public void onDtmfReceived(Core core, Call call, int i) {
                Log.d(TAG, "Core listener - On Dtmf Received");
            }

            @Override
            public void onReferReceived(Core core, String s) {
                Log.d(TAG, "Core listener - On Refer Received");
            }

            @Override
            public void onCallEncryptionChanged(Core core, Call call, boolean b, String s) {
                Log.d(TAG, "Core listener - On Call Encrypted Changed");
            }

            @Override
            public void onTransferStateChanged(Core core, Call call, Call.State state) {
                Log.d(TAG, "Core listener - On Transfer State Changed");
            }

            @Override
            public void onBuddyInfoUpdated(Core core, Friend friend) {
                Log.d(TAG, "Core listener - On Buddy Info Updated");
            }

            @Override
            public void onCallStatsUpdated(Core core, Call call, CallStats callStats) {
                Log.d(TAG, "Core listener - On Call Stats Updated");
            }

            @Override
            public void onInfoReceived(Core core, Call call, InfoMessage infoMessage) {
                Log.d(TAG, "Core listener - On Info Received");
            }

            @Override
            public void onSubscriptionStateChanged(Core core, Event event, SubscriptionState
                    subscriptionState) {
                Log.d(TAG, "Core listener - On Subscription State Changed");
            }

            @Override
            public void onNotifyReceived(Core core, Event event, String s, Content content) {
                Log.d(TAG, "Core listener - On Notify Received");
            }

            @Override
            public void onSubscribeReceived(Core core, Event event, String s, Content content) {
                Log.d(TAG, "Core listener - On Subscribe Received");
            }

            @Override
            public void onPublishStateChanged(Core core, Event event, PublishState publishState) {
                Log.d(TAG, "Core listener - On Publish State Changed");
            }

            @Override
            public void onConfiguringStatus(Core core, ConfiguringState configuringState, String
                    s) {
                Log.d(TAG, "Core listener - On Configuring Status");
            }

            @Override
            public void onNetworkReachable(Core core, boolean b) {
                Log.d(TAG, "Core listener - On Network Reachable");
            }

            @Override
            public void onLogCollectionUploadStateChanged(Core core, Core
                    .LogCollectionUploadState logCollectionUploadState, String s) {
                Log.d(TAG, "Core listener - On Log Collection Upload StateChanged");
            }

            @Override
            public void onLogCollectionUploadProgressIndication(Core core, int i, int i1) {
                Log.d(TAG, "Core listener - On Log Collection Upload ProgressIndication");
            }

            @Override
            public void onFriendListCreated(Core core, FriendList friendList) {
                Log.d(TAG, "Core listener - On Friend List Created");
            }

            @Override
            public void onFriendListRemoved(Core core, FriendList friendList) {
                Log.d(TAG, "Core listener - On Friend List Removed");
            }

            @Override
            public void onCallCreated(Core core, Call call) {
                Log.d(TAG, "Core listener - On Call Created");
            }

            @Override
            public void onVersionUpdateCheckResultReceived(Core core, VersionUpdateCheckResult
                    versionUpdateCheckResult, String s, String s1) {
                Log.d(TAG, "Core listener - On Version Update Check ResultReceived");
            }

            @Override
            public void onChatRoomStateChanged(Core core, ChatRoom chatRoom, ChatRoom.State state) {
                Log.d(TAG, "Core listener - On Chat Room State Changed");
            }

            @Override
            public void onQrcodeFound(Core core, String s) {
                Log.d(TAG, "Core listener - On QR Code Found");
            }

            @Override
            public void onEcCalibrationResult(Core core, EcCalibratorStatus ecCalibratorStatus,
                                              int i) {
                Log.d(TAG, "Core listener - On EC Calibration Result");
            }

            @Override
            public void onEcCalibrationAudioInit(Core core) {
                Log.d(TAG, "Core listener - On EC Calibration Audio Init");
            }

            @Override
            public void onEcCalibrationAudioUninit(Core core) {
                Log.d(TAG, "Core listener - On EC Calibration Audio Uninit");
            }


        };
    }

    // 打電話
    public void lilin_call(String username, String host, boolean isVideoCall) throws CoreException {
        Address address = mLinphoneCore.interpretUrl(username + "@" + host);
        address.setDisplayName(username);

        //LinphoneCallParams params = mLinphoneCore.createCallParams(null);
        CallParams params = mLinphoneCore.createCallParams(null);

        if (isVideoCall) {// 要視訊電話
            //params.setVideoEnabled(true);
            params.enableVideo(true);

            params.setAudioBandwidthLimit(40);
            params.enableLowBandwidth(false);
        } else {// 語音電話
            //params.setVideoEnabled(false);
            params.enableVideo(false);
        }
        Call call = mLinphoneCore.inviteAddressWithParams(address, params);
        if (call == null) {
            Log.e("lilin error", "Could not place call to " + username);
            return;
        }
    }

    // 接電話
    public void lilin_jie() throws CoreException {
        //instance.getLC().setVideoPolicy(true, instance.getLC().getVideoAutoAcceptPolicy());/*設定初始話視訊電話，設定了這個你撥號的時候就預設為使用視訊發起通話了*/
        //getLC().setVideoActivationPolicy(getLC().getVideoActivationPolicy());/*設定自動接聽視訊通話的請求，也就是說只要是視訊通話來了，直接就接通，不用按鍵確定，這是我們的業務流，不用理會*/

        /*這是允許視訊通話，這個選了false就徹底不能接聽或者撥打視訊電話了*/

        //getLC().enableVideo(true, true);
        getLC().enableVideoCapture(true);
        getLC().enableVideoPreview(true);


        Call currentCall = getLC().getCurrentCall();
        if (currentCall != null) {
            CallParams params = getLC().createCallParams(currentCall);
            getLC().acceptCallWithParams(currentCall, params);
        }
    }
    /*
        public boolean lilin_getVideoEnabled() {

            CallParams remoteParams = null;
            if(mLinphoneCore != null){
                remoteParams = mLinphoneCore.getCurrentCall().getRemoteParams();
            }
            //return remoteParams != null && remoteParams.getUsedVideoPayloadType().isUsable();
            return remoteParams != null ;
            //return remoteParams != null && remoteParams.getVideoEnabled();
        }
    */
    public boolean lilin_getVideoEnabled() {
        CallParams remoteParams = mLinphoneCore.getCurrentCall().getRemoteParams();
        //return remoteParams != null && remoteParams.getVideoEnabled();
        return remoteParams != null;
    }

/*
    @Override
    public void displayStatus(Core lc, String message) {
        Log.e("lilin  displayStatus: " + message);
        if (message.indexOf("Call terminated") != -1) {
            Intent intent = new Intent(VideoActivity.RECEIVE_VIDEO_ACTIVITY);
            intent.putExtra("action", "end");
            sendBroadcast(intent);
        }
    }
*/




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 挂断当前通话
     */
    public void hangUp() {
        Call currentCall = mLinphoneCore.getCurrentCall();
        if (currentCall != null) {
            mLinphoneCore.terminateCall(currentCall);
        } else if (mLinphoneCore.isInConference()) {
            mLinphoneCore.terminateConference();
        } else {
            mLinphoneCore.terminateAllCalls();
        }
    }


}
