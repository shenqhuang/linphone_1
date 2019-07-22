package com.iwa.shenq_huang.linphone_example;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.linphone.core.CoreException;

public class MainActivity extends AppCompatActivity {


    String test666666 = "test";
    int xx = 123;

    private LinphoneMiniManager mManager;
    TextView id_text_status;
    Button id_btn_boda,id_btn_jie,id_btn_vda;


        String UserName_To = "test";//對方帳號
        String UserName = "test2";//自己帳號

/*
        String UserName_To = "test2";//對方帳號
        String UserName = "test";//自己帳號
*/

    void test(){
        xx = 53434344;
    }

    void test22222(){
        xx = 53434344;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        test();
        if(xx != 1){
            Toast.makeText(MainActivity.this,"Hello",Toast.LENGTH_SHORT).show();
        }


        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            // 版本5
            UserName = "test2";
            UserName_To = "test";

        } else{
            UserName = "test";
            UserName_To = "test";
        }


        //广播
        IntentFilter intentFilter = new IntentFilter(RECEIVE_MAIN_ACTIVITY);
        mReceiver = new MainActivityReceiver();
        registerReceiver(mReceiver, intentFilter);

        mManager = new LinphoneMiniManager(this);

        id_text_status = findViewById(R.id.id_text_status);
        id_btn_boda = findViewById(R.id.id_btn_boda);
        id_btn_boda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 語音撥打
                try {
                    mManager.lilin_call(UserName_To,"172.22.123.16",false);
                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }
        });

        id_btn_jie = findViewById(R.id.id_btn_jie);
        id_btn_jie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 接電話
                try {
                    mManager.lilin_jie();

                    if (mManager.lilin_getVideoEnabled()) { //启动视频
                        startActivity(new Intent(MainActivity.this, VideoActivity.class));
                    }


                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }
        });

        id_btn_vda = findViewById(R.id.id_btn_vda);
        id_btn_vda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 視訊電話(撥電話)
                try {
                    //instance.lilin_call(id_etext_dail.getText().toString(), host, true);
                    mManager.lilin_call(UserName_To,"172.22.123.16",true);
                    startActivity(new Intent(MainActivity.this, VideoActivity.class));
                } catch (CoreException e1) {
                    Log.e("MainActivity", e1.getMessage());
                }
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        ((Button)this.findViewById(R.id.id_btn_reg)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String sipAddress="sip:" + UserName + "@172.22.123.16",password="test";
                String sipAddress="sip:" + UserName + "@sip.linphone.org",password="test111";
                try {
                    mManager.lilin_reg(sipAddress, password,"5060");
                } catch (CoreException e) {
                    e.printStackTrace();
                }


            }
        });


    }

    protected void onDestroy() {
        mManager.destroy();
        super.onDestroy();

    }

    //===============================================================

    public class MainActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            switch (action) {
                case "reg_state":
                    id_text_status.setText(intent.getStringExtra("data"));
                    break;
                default:
                    break;
            }
        }
    }

    public static final String RECEIVE_MAIN_ACTIVITY = "receive_main_activity";
    private MainActivityReceiver mReceiver;





}
