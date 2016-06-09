package com.example.jungle.myapplication;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class MainActivity extends AppCompatActivity {


    private Button btnStart;
    private Button btnStop;
    private Button btnBlueTooth;
    private Button btnPicPlay;
    private Button btnPause;
    private Button btnClear;
    private Spinner spinRadius;
    private TextView txtLeftFlag;
    private TextView txtLeft;
    private TextView txtInformation;
    private TextView txtRight ;
    private TextView txtRightFlag;
    private BluetoothAdapter bluetoothAdapter;
    private  Map<String,String> nameAndAdress=null;
    private  String serverName=null;
    private  String serverAdress=null;
    private  String[] deviceNames=null;
    private MyMainActivityBroadcastReceiver myBroadcastReceiver;
    private Intent myServiceIntent;
    private  Intent intentSendMsg;
    String ACTION_FROM_SERVER;
    String ACTION_FROM_MAIN_ACTIVITY;
    private  Intent intentPic;
    //标志位
    private Boolean BTN_PAUSE_FALG = false;

    int i =0;
    int j =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        btnStart = (Button)findViewById(R.id.btnStart);
        btnStop  = (Button)findViewById(R.id.btnStop);
        btnBlueTooth  = (Button)findViewById(R.id.btnBlueTooth);
        btnPicPlay =  (Button)findViewById(R.id.btnPicPlay);
        btnPause = (Button) findViewById(R.id.btnPause);
        btnClear =  (Button)findViewById(R.id.btnClear);

        spinRadius= (Spinner) findViewById(R.id.spinRadius);
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(
                this,R.array.gesture_radius,R.layout.support_simple_spinner_dropdown_item);
        arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinRadius.setAdapter(arrayAdapter);
        spinRadius.setSelection(4,true);//默认距离30cm
        spinRadius.setOnItemSelectedListener(new RadiusItemselectedListener());

       // spinRadius.setOnItemClickListener();
//        txtLeftFlag = (TextView)findViewById(R.id.txtLeftFlag);
        txtLeft = (TextView)findViewById(R.id.txtLeft);
        txtInformation = (TextView)findViewById(R.id.txtInformation);
        txtRight = (TextView)findViewById(R.id.txtRight);
//        txtRightFlag = (TextView)findViewById(R.id.txtRightFlag);
        //初始化

        ACTION_FROM_SERVER = this.getResources().getString(R.string.ACTION_FROM_SERVER);
        //本页发送广播
        ACTION_FROM_MAIN_ACTIVITY = this.getResources().getString(R.string.ACTION_FROM_MAIN_ACTIVITY);
//        intentSendMsg = new Intent(ACTION_FROM_MAIN_ACTIVITY);


        nameAndAdress  = new HashMap<String, String>();
        //注册广播
        myBroadcastReceiver = new MyMainActivityBroadcastReceiver();

        registerReceiver(myBroadcastReceiver,new IntentFilter(ACTION_FROM_SERVER));
        //服务端intent
        myServiceIntent = new Intent(MainActivity.this,MyService.class);
        //
        //启动server---------//
        startService(myServiceIntent);
        //添加事件
        btnStart.setOnClickListener(new BtnStartOnclickListener());
        btnStop.setOnClickListener(new BtnStopOnclickListener());
        btnBlueTooth.setOnClickListener(new BtnBlueToothOnclickListener());
        btnPicPlay.setOnClickListener(new BtnPicPlayOnclikListener());
        btnPause.setOnClickListener(new BtnConPauseOnclikListener());
        btnClear.setOnClickListener(new BtnClearOnclikListener());

//  new activity
        intentPic = new Intent(MainActivity.this,PicPlayActivity.class);


    }
//手势空间半径选择
    private class RadiusItemselectedListener implements AdapterView.OnItemSelectedListener{
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            String str = (String) parent.getItemAtPosition(position);
            sendBroadcast(new Intent(ACTION_FROM_MAIN_ACTIVITY).putExtra("GestureRoomRadius", Integer.parseInt(str)));
//            txtInformation.append("正在更改手势空间半径...\n");
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    /**
     * [图片浏览]按键listener
     */
//
    private  class BtnPicPlayOnclikListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
           if(intentPic!=null){
               startActivity(intentPic);
           }

        }

    }
//暂停按键
    private  class  BtnConPauseOnclikListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Boolean boolFlag =  btnPause.getText().equals("暂停界面");
            if(boolFlag!=null){
                if(boolFlag == true){
                    BTN_PAUSE_FALG=true;
                    btnPause.setTag(true);
                    btnPause.setText("已暂停");
                    txtInformation.append("已暂停,再次点击开启\n");
                }
                if(boolFlag==false){
                    BTN_PAUSE_FALG=false;
                    btnPause.setTag(false);
                    btnPause.setText("暂停界面");
                    txtInformation.append("已开启数据动态显示\n");
                }
            }
        }

    }

    /**
     * 清空界面
     */
    private  class BtnClearOnclikListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {

            txtLeft.setText("");
            txtRight.setText("");
            txtInformation.setText("");
        }
    }

    /**
     * [开始按钮]listener
     */
//
    private class BtnStartOnclickListener implements View.OnClickListener{
         @Override
        public void onClick(View v) {
             sendBroadcast(new Intent(ACTION_FROM_MAIN_ACTIVITY).putExtra("ControlOnSystem",true));
             txtInformation.append("成功开启全局控制\n");
             btnStart.setText("已开启");
    }
    }

    /**
     * [结束按钮]listener
     */
//
    private class BtnStopOnclickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            sendBroadcast(new Intent(ACTION_FROM_MAIN_ACTIVITY).putExtra("ContrlOnSystem",false));
            txtInformation.append("成功关闭全局控制\n");
            btnStart.setText("开启全局");
        }
}

    /**
     * 蓝牙选择按钮listener
     */
//蓝牙选择按钮listener
    private class BtnBlueToothOnclickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            //获取蓝牙
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(bluetoothAdapter!=null){

                txtInformation.append("蓝牙设备可用...\n");
                //蓝牙不可用，则打开蓝牙
                if(!bluetoothAdapter.isEnabled()){

                    //如果正在搜索，则关闭搜索
                    if(bluetoothAdapter.isDiscovering()){

                        bluetoothAdapter.cancelDiscovery();
                    }
                    txtInformation.append("\n请再次点击[选择蓝牙]...\n");
                    //打开蓝牙
                    bluetoothAdapter.enable();
                }
                //蓝牙可用，则继续执行
                if (bluetoothAdapter.isEnabled()) {
                    //获取已绑定蓝牙设备
                    Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();

                    ArrayList<String> nameList= new ArrayList<>();

                    if(devices.size()>0){

                        Iterator iterator = devices.iterator();

                        while(iterator.hasNext()){

                            BluetoothDevice device = (BluetoothDevice) iterator.next();
                            txtInformation.append(device.getName()+"\n");
                            nameAndAdress.put(device.getName(),device.getAddress());
                            nameList.add(device.getName());
                        }

                        int i = 0;
                        deviceNames = new String[nameList.size()];
                        txtInformation.append("共找到 "+nameList.size()+" 个已绑定蓝牙..."+"\n");
                        for (String str:nameList) {
                            deviceNames[i++] = str;
                        }

                        if(deviceNames!=null){

                            AlertDialog.Builder myChoice = new  AlertDialog.Builder(MainActivity.this);
                            myChoice.setTitle("请选择" );
                            myChoice.setItems(deviceNames, new DialogChoiceListener());
                            myChoice.show();

                        }
                    }
                }
            }
            //找不到蓝牙
            else{

                txtInformation.append("无可用蓝牙设备\n");
            }


        }
    }

    /**
     * 弹出蓝牙选择框
     */
//
    private  class DialogChoiceListener implements  DialogInterface.OnClickListener{
        @Override
        public void onClick(DialogInterface dialog, int which) {

            serverName = deviceNames[which];
            serverAdress = nameAndAdress.get(serverName);
            txtInformation.append("已选择:"+deviceNames[which]+"\n");
            btnBlueTooth.setText("已选择");
            txtInformation.append("正在启动蓝牙，请稍等......\n");
            //发送蓝牙地址给server
            sendBroadcast(new Intent(ACTION_FROM_MAIN_ACTIVITY).putExtra("BlueToothAdress",serverAdress));
            //关闭选择窗口
            dialog.dismiss();
        }
    }

    /**
     * 主界面中的广播接收器
     */
//
    public  class  MyMainActivityBroadcastReceiver extends BroadcastReceiver{
        ArrayList<String> arrayListLeft= new ArrayList<>();
        ArrayList<String> arrayListRight= new ArrayList<>();
        Boolean leftMove=false;
        Boolean rightMove=false;

        /**
         * @param context
         * @param intent
         * 复写的方法
         */
//
        @Override
        public void onReceive(Context context, Intent intent) {

            String str = intent.getStringExtra("ServerInformation");
            String[] strArr = intent.getStringArrayExtra("ServerInfoPointStrArr");
//            MyPoint point = intent.getParcelableExtra("ServerInfoPoint");
            leftMove = intent.getBooleanExtra("ServerGestureLeftMove",false);
            rightMove= intent.getBooleanExtra("ServerGestureRightMove",false);
            //手势控制
            if(leftMove==true){
                    txtInformation.append("[左滑]\n");
            }
            else if(rightMove==true){
                    txtInformation.append("[右滑]\n");
            }

            if(str!=null){
                txtInformation.append(str+"\n");
            }
            if(strArr != null&&BTN_PAUSE_FALG==false){
                arrayListLeft.add(strArr[0]);
                arrayListRight.add(strArr[1]);

            }

            if(arrayListLeft.size()>27&&arrayListRight.size()>27){

                String txt1="";
                String txt2="";

                for (String streach1:arrayListLeft
                     ) {
                    txt1+=streach1+"\n";
                }
                for (String streach2:arrayListRight
                     ) {
                    txt2+=streach2+"\n";
                }
                txtLeft.setText(txt1);
                txtRight.setText(txt2);
                //每次清除，保证文本长度

                arrayListLeft.remove(0);
                arrayListRight.remove(0);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();



    }

    //
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //停止服务-注销广播
        unregisterReceiver(myBroadcastReceiver);
        //stopService(myServiceIntent);
    }
}

