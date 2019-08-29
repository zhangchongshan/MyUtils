package com.example.myutils.util.sqlutils;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by zhangchongshan on 2019/7/30. 序列化
 */
public class MyImage implements Serializable {

    public int realID;

    public USImage usImage;
    public Patient patient;
    public Bitmap bitmap;
    public String annotate=new String();
    public boolean upsidedown=false;
    public int expandType=0;
    public String pwDiameter="";
    public String pwFlow="";
    public String pwSpeed="";

    public MyImage(USImage image){
        this.usImage=image;
    }
    public MyImage(USImage image, Patient patient, Bitmap bitmap, String annotate){
        this.usImage=image;
        this.patient=patient;
        this.bitmap=bitmap;
        this.annotate=annotate;
    }
}
