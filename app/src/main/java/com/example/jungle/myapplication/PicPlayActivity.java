package com.example.jungle.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PicPlayActivity extends AppCompatActivity {

    ImageView imageView;
    BroadcastReceiver myReceiver;
    private  String ACTION_FROM_SERVER;
    String MY_PATH = "/AMyPicture";
    String[] imagesAdress;
    int indexPicture=0;
    int len=0;
    float x1=0;
    float x2=0;
    //用于缩放的bitmap
    Bitmap currentBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_play);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        imageView = (ImageView)findViewById(R.id.imageView);
        ACTION_FROM_SERVER = this.getResources().getString(R.string.ACTION_FROM_SERVER);
        myReceiver = new BroadcastReceiver() {
            Boolean leftMove =false;
            Boolean rightMove=false;
            Boolean leftClick= false;
            Boolean rightClick =false;
            @Override
            public void onReceive(Context context, Intent intent) {
                leftMove = intent.getBooleanExtra("ServerGestureLeftMove",false);
                rightMove= intent.getBooleanExtra("ServerGestureRightMove",false);
                leftClick = intent.getBooleanExtra("ServerGestureLeftClick",false);
                rightClick =intent.getBooleanExtra("ServerGestureRightClick",false);
                //手势控制
                if(leftMove == true){
                    showNextPicture();
                    //Toast.makeText(PicPlayActivity.this,"左滑",Toast.LENGTH_SHORT).show();
                }
                else if(rightMove == true){
                    showPreviousPicture();
//                    Toast.makeText(PicPlayActivity.this,"右滑",Toast.LENGTH_SHORT).show();
                }
                else if(leftClick == true){
//                    Toast.makeText(PicPlayActivity.this,"缩小",Toast.LENGTH_SHORT).show();
                    showLitterPicture();
                }
                else  if(rightClick == true){
//                    Toast.makeText(PicPlayActivity.this,"放大",Toast.LENGTH_SHORT).show();
                    showBiggerPicture();

                }

            }
        };
        registerReceiver(myReceiver, new IntentFilter(ACTION_FROM_SERVER));

        imagesAdress = getFilesAdress();

        if(imagesAdress != null){
            len = imagesAdress.length;
            Bitmap bitmap = getLoadBitmap(imagesAdress[0]);
            imageView.setImageBitmap(bitmap);
        }
        else {

            Toast.makeText(PicPlayActivity.this,"文件夹中没有图片" ,Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            x1 = event.getX();

        }


        if(event.getAction()==MotionEvent.ACTION_UP){
            x2=event.getX();
            //左滑
            if(x1-x2>50){
                showNextPicture();
            }

            else if(x2-x1>50){
                //右滑
                showPreviousPicture();
            }
        }


        return super.onTouchEvent(event);
    }

    /**
     * 显示下一张图片
     */
//
    private  void showNextPicture(){
        if(indexPicture==len-1){
            indexPicture=0;
        }
        else {
            indexPicture++;
        }
        imageView.setImageBitmap(getLoadBitmap(imagesAdress[indexPicture]));
        currentBitmap =null;
    }

    /**
     * 显示上一张图片
     */
//
    private  void showPreviousPicture(){
        if(indexPicture==0){
            indexPicture=len-1;

        }
        else {
            indexPicture--;
        }
        imageView.setImageBitmap(getLoadBitmap(imagesAdress[indexPicture]));
        currentBitmap =null;

    }

    /**
     * 放大当前图片
     */
    //
  //-----------------------------------------------------------//
    private  void showBiggerPicture(){
        Matrix matrix = new Matrix();
        matrix.postScale(1.2f,1.2f);
        Bitmap bitmap=null;
        if(currentBitmap == null){
            bitmap = getLoadBitmap(imagesAdress[indexPicture]);
        }
        else {
            bitmap= currentBitmap;
        }
        Bitmap newBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        imageView.setImageBitmap(newBitmap);
        currentBitmap = newBitmap;
    }

    /**
     * 缩小当前图片
     */
//
    private  void showLitterPicture(){
        Matrix matrix = new Matrix();
        matrix.postScale(0.8f,0.8f);
        Bitmap bitmap=null;
        if(currentBitmap == null){
            bitmap = getLoadBitmap(imagesAdress[indexPicture]);
        }
        else {
            bitmap= currentBitmap;
        }
        Bitmap newBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        imageView.setImageBitmap(newBitmap);
        currentBitmap = newBitmap;
    }

    /**
     * @return String[]
     * 获取所有图片路径
     */
//-------------------------------------//
    public String[]  getFilesAdress(){
        File[] files = null;
        ArrayList<String> adressList= new ArrayList<>();
        String[] adress;
        int i=0;
        try {
            files = new File(getMyPicturePath()).listFiles();
//            Toast.makeText(PicPlayActivity.this,files[0].toString(),Toast.LENGTH_SHORT).show();

        }catch (Exception e){Toast.makeText(PicPlayActivity.this,"异常" ,Toast.LENGTH_SHORT).show();}

    if(files !=null ){

        for (File f:files){
            if(f.toString().endsWith(".jpg")||f.toString().endsWith(".png")) {
                adressList.add(f.toString());
            }
            }
        adress = new String[adressList.size()];
        for (String str:adressList) {
            adress[i++] = str;
        }
        return  adress;
    }
       else  return  null;
    }

    /**
     * @return Sting
     * 获取文件夹路径
     */
//
//√------------
    public  String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取根目录
//            Toast.makeText(PicPlayActivity.this,sdDir.toString() ,Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(PicPlayActivity.this,"没有内存卡" ,Toast.LENGTH_SHORT).show();
        }
        if(sdDir!=null){
            return sdDir.toString();
        }
        else return  null;

    }

    /**
     * @return String
     * 获取图片路径
     */
//
//-------------------
    public String getMyPicturePath() {
        String sdPath = getSDPath();
        if(sdPath!=null){
            String filePath = sdPath+MY_PATH;
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
                if(file.exists()){
                    try {
                        FileOutputStream fop = new FileOutputStream(filePath.toString()+"/"+"default.jpg");
                        ColorDrawable drawable = new ColorDrawable(Color.BLUE);
                        Bitmap bitmap = Bitmap.createBitmap(500,300,Bitmap.Config.RGB_565 );
                        Canvas canvas = new Canvas(bitmap);
                        drawable.draw(canvas);
                        canvas.drawColor(Color.GRAY);
                        if(fop!=null){
                            bitmap.compress(Bitmap.CompressFormat.JPEG,90,fop);
                            fop.close();
                        }

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            else {
                Toast.makeText(PicPlayActivity.this,"请将图片放入："+file.toString() ,Toast.LENGTH_SHORT).show();
            }

            return filePath;
        }


        else return  "/storage/sdcard1/Amypicture";
    }

    /**
     * @param url
     * @return bitmap
     * 获取图片bitmap
     */
//
    public Bitmap getLoadBitmap(String url){

        try{
            FileInputStream fis = new FileInputStream(url);
//            Toast.makeText(PicPlayActivity.this,"找到图片",Toast.LENGTH_SHORT).show();
            return BitmapFactory.decodeStream(fis);

        }catch (IOException ioe){
            Toast.makeText(PicPlayActivity.this,"没有找到图片",Toast.LENGTH_SHORT).show();
            return  null;
        }

    }



    @Override
    protected void onDestroy() {
        //注销广播
        unregisterReceiver(myReceiver);
        super.onDestroy();
    }

}

