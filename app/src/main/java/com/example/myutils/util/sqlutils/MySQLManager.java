package com.example.myutils.util.sqlutils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import com.example.myutils.util.MyPreferences;
import com.example.myutils.util.bitmaputil.BitmapAndStringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by zhangchongshan on 2019/7/29.
 */
public class MySQLManager extends SQLiteOpenHelper {

    private final static String dbName="DB_Name";
    private final static int version=1;

    private static final String TABLE_NAME = "duman_data_table";

    //表里面的内容
//    private static final String PATIENT = "_patient";
    private static final String ID = "_id";
    private static final String RealID = "_realid";
    private static final String PATIENT_ID="_patient_id";
    private static final String PATIENT_NAME="_patient_name";
    private static final String PATIENT_BIRTH="_patient_birth";
    private static final String PATIENT_SEX="_patient_sex";
    private static final String PATIENT_AGE="_patient_age";
    private static final String PATIENT_DATE="_patient_date";
    private static final String PATIENT_ANNOTATE="_patient_annotate";
    private static final String USIMAGE_DATA="_patient_data";
    private static final String IMAGE_DATA="_patient_data1";
    private static final String IMAGE_UPSIDEDOWN="_upsidedown";
    private static final String IMAGE_EXPAND="_expand_type";
    private static final String PW_DIAMETER="_pw_diameter";
    private static final String PW_FLOW="_pw_flow";
    private static final String PW_SPEED="_pw_speed";

    private int dbLength;

    private static MySQLManager instance=null;
    public synchronized static MySQLManager getInstance(Context context){
        if(instance==null){
            if (context!=null){
                instance=new MySQLManager(context);
            }
        }
        return instance;
    }

    private MySQLManager(Context context) {
        super(context, dbName, null, version);
        MyPreferences preferences=MyPreferences.getInstance(null);
        dbLength=preferences.getInt("duman_db_length",0);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        String create_sql = "CREATE TABLE if not exists ["+TABLE_NAME+"]"+
                "("+ID+" integer primary key autoincrement,"+RealID+" integer,"+
                PATIENT_ID+" text,"+PATIENT_NAME+" text,"+PATIENT_BIRTH+" text,"+PATIENT_SEX+" text,"+
                PATIENT_AGE+" text,"+PATIENT_DATE+" text,"+
                PATIENT_ANNOTATE+" text,"+IMAGE_DATA+" text,"+USIMAGE_DATA+" text,"+
                IMAGE_UPSIDEDOWN+" text,"+IMAGE_EXPAND+" text,"+
                PW_DIAMETER+" text,"+PW_FLOW+" text,"+PW_SPEED+" text"+")";

        db.execSQL(create_sql);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }

    public void refresh(Context context){
        instance=null;
        getInstance(context);
    }
    /*增加操作*/
    public  synchronized long insert(final MyImage myImage){
        try {

            USImage image=myImage.usImage;
            Bitmap bitmap=myImage.bitmap;
            Patient patient=myImage.patient;
            byte data[]=object2Bytes(image);
            String bitStr= BitmapAndStringUtils.convertIconToString(bitmap);

            Date dob = patient.getDOB();
            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String birth=format.format(dob);

            format=new SimpleDateFormat("yyyy-MM-dd");
            String patientDate=format.format(image.rawImage.timeCap);


            Date cur = new Date();
            format=new SimpleDateFormat("yyyy");
            int barthYear= Integer.parseInt(format.format(dob));
            int nowYear= Integer.parseInt(format.format(cur));
            int delYear=(nowYear-barthYear)/4;
            long delDay = (cur.getTime() - dob.getTime())/1000/60/60/24-delYear;
            int age = (int) (delDay/365);
            String ageStr=""+age;
            String upsidedown=""+0;
            if (myImage.upsidedown){
                upsidedown=""+1;
            }

            ContentValues cv=new ContentValues();
            dbLength++;
//                    realid = dbLength;
            cv.put(RealID,dbLength);
            cv.put(PATIENT_ID,patient.getID());
            cv.put(PATIENT_NAME,patient.getName());
            cv.put(PATIENT_SEX,""+patient.getSex());
            cv.put(PATIENT_BIRTH,birth);
            cv.put(PATIENT_DATE,patientDate);
            cv.put(PATIENT_AGE,ageStr);
            cv.put(USIMAGE_DATA,data);
            cv.put(IMAGE_DATA,bitStr);
            cv.put(PATIENT_ANNOTATE,myImage.annotate);
            cv.put(IMAGE_UPSIDEDOWN,upsidedown);
            cv.put(IMAGE_EXPAND,""+myImage.expandType);
            cv.put(PW_DIAMETER,myImage.pwDiameter);
            cv.put(PW_FLOW,myImage.pwFlow);
            cv.put(PW_SPEED,myImage.pwSpeed);

            SQLiteDatabase database = MySQLManager.this.getWritableDatabase();
//            database.execSQL("insert into "+TABLE_NAME+" ("+PATIENT+") values(?)", new Object[] { data });
            database.insert(TABLE_NAME,null,cv);
            database.close();


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        myImage.realID=dbLength;
        images.add(myImage);

        MyPreferences preferences=MyPreferences.getInstance(null);
        preferences.putInt("duman_db_length",dbLength);
        return dbLength;
    }

    /*删除操作*/
    public void delete(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = RealID + "=?";
        String[] whereValue = { Integer.toString(id) };
        db.delete(TABLE_NAME, where, whereValue);


        for (MyImage image:images){
            if (image.realID==id){
                images.remove(image);
                break;
            }
        }
    }

    private void updateRealID(int realID){
        for (int i=realID;i<dbLength;i++){
            SQLiteDatabase db=this.getReadableDatabase();
            String where=RealID+"=?";
            String[] whereValue={Integer.toString(i+1)};
            ContentValues cv=new ContentValues();
            cv.put(RealID,i);
            db.update(TABLE_NAME,cv,where,whereValue);
//            db.notifyAll();
            db.close();
//            images.get(i+1).realID=i;
        }
    }

    ArrayList<MyImage> images = new ArrayList<MyImage>();
    public ArrayList<MyImage> getAllObject() {
        if (images.size()>0){
            ArrayList<MyImage> returnArr= new ArrayList<>();
            returnArr.addAll(images);
            return returnArr;
        }
        SQLiteDatabase database = this.getReadableDatabase();
        if (database.isDbLockedByCurrentThread()){
            database.notifyAll();
        }
        Cursor cursor= database.rawQuery("select * from " + TABLE_NAME, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                byte data[] = cursor.getBlob(cursor.getColumnIndex(USIMAGE_DATA));
                try {
                    ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(data);
                    ObjectInputStream inputStream = new ObjectInputStream(arrayInputStream);
                    USImage image = (USImage) inputStream.readObject();

                    String patientID=cursor.getString(cursor.getColumnIndex(PATIENT_ID));
                    String patientName=cursor.getString(cursor.getColumnIndex(PATIENT_NAME));
                    String patientSex=cursor.getString(cursor.getColumnIndex(PATIENT_SEX));
                    String patientBirth=cursor.getString(cursor.getColumnIndex(PATIENT_BIRTH));

                    Patient patient=new Patient();
                    patient.setID(patientID);
                    patient.setName(patientName);
                    patient.setSex(Integer.valueOf(patientSex));
                    SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date birth=format.parse(patientBirth);
                    patient.setDOB(birth);

                    String bitStr=cursor.getString(cursor.getColumnIndex(IMAGE_DATA));
                    Bitmap bitmap = BitmapAndStringUtils.convertStringToIcon(bitStr);

                    String annotate=cursor.getString(cursor.getColumnIndex(PATIENT_ANNOTATE));

                    MyImage myImage=new MyImage(image,patient,bitmap,annotate);
                    String upsidedown=cursor.getString(cursor.getColumnIndex(IMAGE_UPSIDEDOWN));
                    int upsidedownIn= Integer.parseInt(upsidedown);
                    if (upsidedownIn==1){
                        myImage.upsidedown=true;
                    }else {
                        myImage.upsidedown=false;
                    }
                    String expandStr=cursor.getString(cursor.getColumnIndex(IMAGE_EXPAND));
                    int expand= Integer.parseInt(expandStr);
                    myImage.expandType=expand;
                    String pwDiameter=cursor.getString(cursor.getColumnIndex(PW_DIAMETER));
                    String pwFlow=cursor.getString(cursor.getColumnIndex(PW_FLOW));
                    String pwSpeed=cursor.getString(cursor.getColumnIndex(PW_SPEED));
                    myImage.pwDiameter=pwDiameter;
                    myImage.pwFlow=pwFlow;
                    myImage.pwSpeed=pwSpeed;

                    int realID=cursor.getInt(cursor.getColumnIndex(RealID));
                    myImage.realID=realID;

                    images.add(myImage);
                    inputStream.close();
                    arrayInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
//        dbLength=images.size()-1;
//        Collections.reverse(images);
        if (dbLength<images.size()-1){
            dbLength=images.size()-1;
        }
        ArrayList<MyImage> returnArr= new ArrayList<>();
        returnArr.addAll(images);
//        Collections.reverse(returnArr);
        return returnArr;
//        return images;
    }

    public ArrayList<MyImage> getSelectObject(String selectIndex) {

        ArrayList<MyImage> images=new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        if (database.isDbLockedByCurrentThread()){
            database.notifyAll();
        }
        Cursor cursor=null;
        if (selectIndex!=null&&!selectIndex.isEmpty()){
            String where=" where "+PATIENT_ID+" like ?"
                    +" or "+PATIENT_NAME+" like ?"
                    +" or "+PATIENT_ANNOTATE+" like ?"
                    +" or "+PATIENT_AGE+" like ?"
                    +" or "+PATIENT_DATE+" like ?";
            cursor = database.rawQuery("select * from "+TABLE_NAME+where,
                    new String[]{
                            "%"+selectIndex+"%",
                            "%"+selectIndex+"%",
                            "%"+selectIndex+"%",
                            "%"+selectIndex+"%",
                            "%"+selectIndex+"%",
                    });
        }
        if (cursor != null) {
            while (cursor.moveToNext()) {
                byte data[] = cursor.getBlob(cursor.getColumnIndex(USIMAGE_DATA));
                try {
                    ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(data);
                    ObjectInputStream inputStream = new ObjectInputStream(arrayInputStream);
                    USImage image = (USImage) inputStream.readObject();

                    String patientID=cursor.getString(cursor.getColumnIndex(PATIENT_ID));
                    String patientName=cursor.getString(cursor.getColumnIndex(PATIENT_NAME));
                    String patientSex=cursor.getString(cursor.getColumnIndex(PATIENT_SEX));
                    String patientBirth=cursor.getString(cursor.getColumnIndex(PATIENT_BIRTH));

                    Patient patient=new Patient();
                    patient.setID(patientID);
                    patient.setName(patientName);
                    patient.setSex(Integer.valueOf(patientSex));
                    SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date birth=format.parse(patientBirth);
                    patient.setDOB(birth);

                    String bitStr=cursor.getString(cursor.getColumnIndex(IMAGE_DATA));
                    Bitmap bitmap = BitmapAndStringUtils.convertStringToIcon(bitStr);

                    String annotate=cursor.getString(cursor.getColumnIndex(PATIENT_ANNOTATE));

                    MyImage myImage=new MyImage(image,patient,bitmap,annotate);
                    String upsidedown=cursor.getString(cursor.getColumnIndex(IMAGE_UPSIDEDOWN));
                    int upsidedownIn= Integer.parseInt(upsidedown);
                    if (upsidedownIn==1){
                        myImage.upsidedown=true;
                    }else {
                        myImage.upsidedown=false;
                    }
                    String expandStr=cursor.getString(cursor.getColumnIndex(IMAGE_EXPAND));
                    int expand= Integer.parseInt(expandStr);
                    myImage.expandType=expand;
                    String pwDiameter=cursor.getString(cursor.getColumnIndex(PW_DIAMETER));
                    String pwFlow=cursor.getString(cursor.getColumnIndex(PW_FLOW));
                    String pwSpeed=cursor.getString(cursor.getColumnIndex(PW_SPEED));
                    myImage.pwDiameter=pwDiameter;
                    myImage.pwFlow=pwFlow;
                    myImage.pwSpeed=pwSpeed;

                    int realID=cursor.getInt(cursor.getColumnIndex(RealID));
                    myImage.realID=realID;

                    images.add(myImage);
                    inputStream.close();
                    arrayInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return images;
    }

    /*修改操作*/
    public void update(MyImage myImage,int index){
        try {
            USImage image=myImage.usImage;
            byte imageData[] = object2Bytes(image);
            Bitmap bitmap =myImage.bitmap;
            String bitStr=BitmapAndStringUtils.convertIconToString(bitmap);



            ContentValues cv=new ContentValues();
            cv.put(USIMAGE_DATA,imageData);
            cv.put(IMAGE_DATA,bitStr);
            Patient patient=myImage.patient;
            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String birth=format.format(patient.getDOB());

            format=new SimpleDateFormat("yyyy-MM-dd");
            String patientDate=format.format(image.rawImage.timeCap);


            Date dob=patient.getDOB();
            Date cur = new Date();
            format=new SimpleDateFormat("yyyy");
            int barthYear= Integer.parseInt(format.format(dob));
            int nowYear= Integer.parseInt(format.format(cur));
            int delYear=(nowYear-barthYear)/4;
            long delDay = (cur.getTime() - dob.getTime())/1000/60/60/24-delYear;
            int age = (int) (delDay/365);
            String ageStr=""+age;
            String upsidedown=""+0;
            if (myImage.upsidedown){
                upsidedown=""+1;
            }

            cv.put(PATIENT_ID,patient.getID());
            cv.put(PATIENT_NAME,patient.getName());
            cv.put(PATIENT_SEX,patient.getSex());
            cv.put(PATIENT_BIRTH,birth);
            cv.put(PATIENT_ANNOTATE,myImage.annotate);
            cv.put(PATIENT_DATE,patientDate);
            cv.put(PATIENT_AGE,ageStr);
            cv.put(IMAGE_UPSIDEDOWN,upsidedown);
            cv.put(IMAGE_EXPAND,""+myImage.expandType);
            cv.put(PW_DIAMETER,myImage.pwDiameter);
            cv.put(PW_FLOW,myImage.pwFlow);
            cv.put(PW_SPEED,myImage.pwSpeed);

            String where = RealID + " =?";
            String[] whereValue = {Integer.toString(index)};
            SQLiteDatabase database = this.getWritableDatabase();
            database.update(TABLE_NAME,cv,where,whereValue);
//            database.notifyAll();
            database.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i=0;i<images.size();i++){
            MyImage image=images.get(i);
            if (image.realID==index){
                images.remove(i);
                images.add(i,myImage);
            }
        }
//        images.remove(index);
//        images.add(index,myImage);
    }

    //删除表
    public void ClearTable(){
        String sql="delete from "+TABLE_NAME;
        SQLiteDatabase database=this.getWritableDatabase();
        database.execSQL(sql);
//        database.notifyAll();
        database.close();

        images.clear();
        dbLength=0;
        MyPreferences preferences=MyPreferences.getInstance(null);
        preferences.putInt("duman_db_length",dbLength);
    }
    private byte[] object2Bytes(Object object){
        byte data[]=null;
        try {
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(arrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            data= arrayOutputStream.toByteArray();
            objectOutputStream.close();
            arrayOutputStream.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}

