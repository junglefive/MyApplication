package com.example.jungle.myapplication;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.example.jungle.myclass.myclass.MyPoint;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

/**
 *
 */
public class MyService extends Service {
    private  String ACTION_FROM_SERVER;
    private  String ACTION_FROM_MAIN_ACTIVITY;
    private  Intent mIntent;
    private static  final int OUT = 0;
    private static final int LEFT  = 1;
    private static final int RIGHT = 2;
    private static final int NONE  = 3;
    private static  final int CENTER = 4;

    private  String blueToothAdress=null;
    private  BluetoothAdapter myAdapter = null;
    private  BluetoothSocket mySoket = null;
    private  BluetoothDevice  myDevice = null;
    private InputStream myInputStream = null;
    private UUID MY_UUID;
    private Handler myHander=null;
    private int gesture_IN =NONE;
    private int RADIUS=30;//手势空间半径
    private  GetDataThread getDataThread = null;
    //广播
    MySeverBroadcastReceiver mySeverBroadcastReceiver=null;
    //标志位
    private  Boolean GestureDetected =false;
    private  Boolean ControlOnSystemFlag;
    private  int leftCount = 0;
    private  int rightCount =0;

    /**
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, final int flags, int startId) {
        //常量
        ACTION_FROM_SERVER = this.getResources().getString(R.string.ACTION_FROM_SERVER);
        ACTION_FROM_MAIN_ACTIVITY = this.getResources().getString(R.string.ACTION_FROM_MAIN_ACTIVITY);
        MY_UUID = UUID.fromString(this.getResources().getString(R.string.MY_UUID));
        mIntent = new Intent(ACTION_FROM_SERVER);

        getDataThread = new GetDataThread();
        //蓝牙
        myAdapter = BluetoothAdapter.getDefaultAdapter();
        //注册广播接收器
         mySeverBroadcastReceiver = new MySeverBroadcastReceiver();
        registerReceiver(mySeverBroadcastReceiver,new IntentFilter(ACTION_FROM_MAIN_ACTIVITY));
        //标志位
       ControlOnSystemFlag =false;
//
        //新建一个hander处理手势
        myHander = new Handler(){
            @Override
            public void handleMessage(Message msg) {


                switch (msg.what){
                    case LEFT:{
                        if(gesture_IN == LEFT){
                            leftCount++;
                        }
                        else if(gesture_IN==RIGHT){
                            //滑动优先------//
                            GestureDetected = true;

//                          sendBroadcast(new Intent(ACTION_FROM_SERVER).putExtra("ServerInformation","左滑\n"));
                            if(ControlOnSystemFlag ==false){
                                sendBroadcast(new Intent(ACTION_FROM_SERVER).putExtra("ServerGestureLeftMove",true));
                            }
                            //
                            if(ControlOnSystemFlag==true){
                                //Left
                                execShellCmd("input swipe 500 500 100 500 100");
                                Toast.makeText(MyService.this,"left",Toast.LENGTH_SHORT).show();
                            }
                            //
                            gesture_IN=NONE;
                        }
                        else {
                            gesture_IN = LEFT;
                        }
                        break;
                    }
                    case RIGHT:{
                        if(gesture_IN == RIGHT){
                            rightCount++;
                        }
                        else if(gesture_IN==LEFT){
                            //滑动优先------//
                            GestureDetected = true;
//                           sendBroadcast(new Intent(ACTION_FROM_SERVER).putExtra("ServerInformation","右滑\n"));
                            //
                            if(ControlOnSystemFlag==false){
                                sendBroadcast(new Intent(ACTION_FROM_SERVER).putExtra("ServerGestureRightMove",true));
                            }
                            //
                            if(ControlOnSystemFlag==true){
                                //Right
                                execShellCmd("input swipe 100 500 500 500 100");
                                Toast.makeText(MyService.this,"right",Toast.LENGTH_SHORT).show();
                            }
                            //
                            gesture_IN=NONE;

                        }
                        else {
                            gesture_IN = RIGHT;
                        }
                        break;
                    }

                    case CENTER:{
                        //计数置0
                        rightCount =0;
                        leftCount =0;
                        break;
                    }
                    case OUT:{
                        //
                        if(leftCount>2 && GestureDetected == false){
                            //execShellCmd("input keyevent 4");
                            sendBroadcast(new Intent(ACTION_FROM_SERVER).putExtra("ServerGestureLeftClick",true));
                            sendBroadcast(new Intent(ACTION_FROM_SERVER).putExtra("ServerInformation","[左击]"));
                        }
                        if(rightCount>2 && GestureDetected == false){
                            //execShellCmd("input keyevent 3");
                            sendBroadcast(new Intent(ACTION_FROM_SERVER).putExtra("ServerGestureRightClick",true));
                            sendBroadcast(new Intent(ACTION_FROM_SERVER).putExtra("ServerInformation","[右击]"));
                        }
                        //恢复标志位
                        rightCount =0;
                        leftCount =0;
                        gesture_IN =NONE;
                        //滑动优先------//
                        GestureDetected = false;
                        break;
                    }
                    default:{ gesture_IN=NONE;break;}

                }

                super.handleMessage(msg);
            }
        };
        return super.onStartCommand(intent, flags, startId);

    }

    /**
     * 服务端广播接收器
     */
//
    public  class  MySeverBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Boolean boolControlOnSystem = null;
            String address = intent.getStringExtra("BlueToothAdress");
            boolControlOnSystem = intent.getBooleanExtra("ControlOnSystem",false);
            int radius= intent.getIntExtra("GestureRoomRadius",-1);//默认半径30
            if(radius != -1 ){
                RADIUS = radius;
                sendBroadcast(new Intent(ACTION_FROM_SERVER).putExtra("ServerInformation","已设置手势空间半径："+RADIUS));
            }


            if(address != null){
                //设置蓝牙地址

                blueToothAdress = address;

                if(myInputStream == null){
                    sendBroadcast(new Intent(ACTION_FROM_SERVER).putExtra("ServerInformation","正在启动连接...."));
                    //连接蓝牙
                    connectBlueTooth();
                }
                else {

                    sendBroadcast(new Intent(ACTION_FROM_SERVER).putExtra("ServerInformation","蓝牙已连接...."));
                }
            }
            if(boolControlOnSystem !=null){

                if(boolControlOnSystem==true){

                    ControlOnSystemFlag = true;
                }
                else{

                    ControlOnSystemFlag = false;
                }

            }


        }
    }

    /**
     * 新线程接收蓝牙数据
     */
//
    //---------------------------------------------//
    private class GetDataThread extends  Thread{

        @Override
        public void run() {

            if(myInputStream != null){

                BufferedReader br = new BufferedReader(new InputStreamReader(myInputStream));
                MyPoint point = new MyPoint();
                while (true){

                    int i=0;
                    int j=0;

                    try {

                         Thread.sleep(5);
//                        sendBroadcast(new Intent(ACTION_FROM_SERVER).putExtra("ServerInformation","接收数据中..."));
                        //缓冲数据放在这里实时更新
                        String str1 = br.readLine();

                        if(str1!=null){
                            i=Integer.parseInt(str1);
//                            sendBroadcast(new Intent(ACTION_FROM_SERVER).putExtra("ServerInformation",str1+"\n"));
                            if(10000<i&&i<20000)
                            {
                                //如果i>RADIUS则置为RADIUS
                                i=i-10000;
                                if(i>RADIUS){
                                    i=RADIUS;
                                }

                                String str2 = br.readLine();
                                if(str2!=null){
                                    j =Integer.parseInt(str2) ;
                                    if(j>20000){
                                        //如果i>RADIUS则置为RADIUS
                                        j = j-20000;
                                        if(j>RADIUS){
                                            j = RADIUS;
                                        }
                                        //-----记录下来--//
                                        point.setLeft(i);
                                        point.setRight(j);

                                        //检测到手离开检测区域，则标志位置为NONE
                                        if(point.getLeft()== RADIUS &&point.getRight()== RADIUS){
                                           // gesture_IN =NONE;
                                            Message msg1 = new Message();
                                            msg1.what = OUT;
                                            msg1.obj = point;
                                            myHander.sendMessage(msg1);

                                        }

                                        else if(point.getRight()< RADIUS &&point.getLeft()== RADIUS){
                                                Message msg1 = new Message();
                                                msg1.what = RIGHT;
                                                msg1.obj = point;
                                                myHander.sendMessage(msg1);
                                        }
                                        else if(point.getLeft()< RADIUS &&point.getRight()== RADIUS){
                                                Message msg2 = new Message();
                                                msg2.what = LEFT;
                                                msg2.obj = point;
                                                myHander.sendMessage(msg2);
                                        }
                                        else if(point.getLeft()< RADIUS &&point.getRight()< RADIUS){
                                            Message msg2 = new Message();
                                            msg2.what = CENTER;
                                            msg2.obj = point;
                                            myHander.sendMessage(msg2);
                                        }

                                        //发送实时数据给UI-----//
                                        String[] strArr=new String[]{String.valueOf(point.getLeft()), String.valueOf(point.getRight())};
                                        sendBroadcast(new Intent(ACTION_FROM_SERVER).putExtra("ServerInfoPointStrArr",strArr ));


                                    }

                                }

                            }
                        }



                    }
                    catch (IOException e){
                        sendBroadcast(new Intent(ACTION_FROM_SERVER).putExtra("ServerInformation","IO异常..."));
                        }
                    catch (InterruptedException ie){
                        sendBroadcast(new Intent(ACTION_FROM_SERVER).putExtra("ServerInformation","中断异常..."));
                    }

                }

            }
        }
    }

    /**
     * 连接蓝牙函数
     */
    //--连接蓝牙------//
    public void  connectBlueTooth(){

     if(myAdapter != null){

         if(myAdapter.isDiscovering()){
             //如果蓝牙在搜索，则关闭搜索
             myAdapter.cancelDiscovery();
         }
         //确保蓝牙地址为非null
         if(blueToothAdress != null){

             myDevice = myAdapter.getRemoteDevice(blueToothAdress);
             //txtInformation.append("已设置"+serverName+"地址："+serverAdress+"\n");
         }
         //连接soket
         if(myDevice != null){
             try{
                 mySoket = myDevice.createRfcommSocketToServiceRecord(MY_UUID);
                 mySoket.connect();
                 //os = clientSoket.getOutputStream();
                 myInputStream = mySoket.getInputStream();
             }
             catch (IOException e){
                 sendBroadcast(new Intent(ACTION_FROM_SERVER).putExtra("ServerInformation","蓝牙连接失败..."));
             }
         }
         //
         if(myInputStream!=null) {
             sendBroadcast(new Intent(ACTION_FROM_SERVER).putExtra("ServerInformation","蓝牙连接成功..."));
             if(getDataThread == null){

                 getDataThread = new GetDataThread();
             }
             if(! getDataThread.isAlive()){
                 //开启处理蓝牙数据线程
                 getDataThread.start();
             }

         }



     }


    }

    /**
     * 执行shell命令
     *
     * @param cmd
     */
    private void execShellCmd(String cmd) {

        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            Process process = Runtime.getRuntime().exec("su");
            sendBroadcast(new Intent(ACTION_FROM_SERVER).putExtra("ServerInformation","root..."));
            // 获取输出流
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
            sendBroadcast(new Intent(ACTION_FROM_SERVER).putExtra("ServerInformation","获取Root权限失败..."));
        }
    }




    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
//关闭流
    @Override
    public void onDestroy() {
        try {
            myInputStream.close();
            mySoket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        unregisterReceiver(mySeverBroadcastReceiver);
        super.onDestroy();
    }
}
