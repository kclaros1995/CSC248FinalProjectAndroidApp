package com.kevinclaros.claroscse248parkinggarage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class DatabaseHelper extends SQLiteOpenHelper {
    ParkingInTakeActivity parkingInTakeActivity;
    public static final String DATABASE_NAME = "vehicle1.db";
    public static final String TABLE_NAME = "vehicle_table";
    public static final String COL_1 = "vehicleType";
    public static final String COL_2 = "licensePlate";
    public static final String COL_3 = "firstName";
    public static final String COL_4 = "lastName";
    public static final String COL_5 = "date";
    public static final String COL_6 = "time";
    public static String COL_7 = "parkingSpot";
    public static final String COL_8 = "paymentPlan";


    public static int parkingSpotCounter = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 8);


    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + TABLE_NAME + "  (ticketNumber INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " vehicleType TEXT, licensePlate TEXT, " +
                "firstName TEXT, lastName TEXT,date TEXT, time TEXT,parkingSpot INTGER, paymentPlan TEXT, spotNumberCounter INTEGER)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String vehicleType,
                              String licensePlate, String firstName, String lastName, String paymentPlan) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, vehicleType);
        contentValues.put(COL_2, licensePlate);
        contentValues.put(COL_3, firstName);
        contentValues.put(COL_4, lastName);
        contentValues.put(COL_5, new SimpleDateFormat("MM-dd-yyyy").format(new Date()));
        contentValues.put(COL_6, String.valueOf(getTime()));
        contentValues.put(COL_8, paymentPlan);
        contentValues.put("parkingSpot", getParkingSpotCounter());


        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1)
            return false;
        else
            return true;

    }

    public static LocalTime getTime() {
        LocalTime dt = LocalTime.now();
        return dt;
    }

    public Cursor getCurrentVehicle(String licensePlate) {
        String query = "select * from " + TABLE_NAME + " where " + COL_2 + " = '" + licensePlate + "'";
        SQLiteDatabase sql = this.getReadableDatabase();
        Cursor cur = sql.rawQuery(query, null);
        if (!cur.moveToFirst())
            cur.moveToFirst();
        return cur;
    }


    public String getParkingSpots(){
        COL_7 = String.valueOf(parkingSpotCounter++);
        return COL_7;
    }


    public Cursor getParkingSpot2() {
        String query = "select " +  COL_7 + " from " +  TABLE_NAME + " where " +  COL_7 + " = " + "(select max( " + COL_7 + " ) from " + TABLE_NAME + ")";
        SQLiteDatabase sql = this.getReadableDatabase();
        Cursor cur = sql.rawQuery(query, null);
        if(!cur.moveToFirst())
            cur.moveToFirst();
        return cur;
    }

    public int getParkingSpotCounter(){
        COL_7 = String.valueOf(parkingSpotCounter++);
        parkingSpotCounter = Integer.parseInt(COL_7);
        return parkingSpotCounter++;
    }

    public String getCol_7(){
        return COL_7;
    }



    public Cursor getAllVehicles() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        return res;
    }

    public Integer removeVehicles(String licensePlate) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COL_2 + "=?", new String[]{licensePlate});
    }

    public boolean addData(String item){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, item);

        long result = db.insert(TABLE_NAME, null, contentValues);

        if(result == -1){
            return false;
        }else{
            return true;
        }
    }
    public Cursor getListContent(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return data;
    }

}
