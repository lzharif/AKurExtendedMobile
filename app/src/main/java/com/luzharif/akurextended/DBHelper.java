package com.luzharif.akurextended;

/**
 * Created by LuZharif on 02/11/2015.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "DatabaseAlatUkur.db";
    public static final String DOSAKU_TABLE_NAME = "log";
    public static final String DOSAKU_COLUMN_ID = "id";
    public static final String DOSAKU_COLUMN_TANGGAL = "tanggal";
    public static final String DOSAKU_COLUMN_BULAN = "bulan";
    public static final String DOSAKU_COLUMN_TAHUN = "tahun";
    private HashMap hp;

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table logdosaku " +
                        "(id integer primary key, tanggal integer, bulan integer, tahun integer)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS logdosaku");
        onCreate(db);
    }

    public boolean insertDosa (int tanggal, int bulan, int tahun)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("tanggal", tanggal);
        contentValues.put("bulan", bulan);
        contentValues.put("tahun", tahun);
        db.insert("logdosaku", null, contentValues);
        return true;
    }

    public int[] hitungBanyakDosaTanggal(int bulan, int tahun){
        int[] banyakDosa = new int[31];
        int itanggal = 1;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            for(itanggal = 1; itanggal < 32; itanggal++) {
                String selectQuery = ("select COUNT(*) FROM logdosaku WHERE tanggal =" + itanggal +
                        " and bulan =" + bulan +" and tahun =" + tahun);
                Cursor cursor =  db.rawQuery(selectQuery, null);
                if (cursor.moveToLast())
                    banyakDosa[itanggal] = cursor.getInt(3);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return banyakDosa;
    }

    public int[] hitungBanyakDosaBulan(int tahun){
        int[] banyakDosa = new int[12];
        int ibulan = 1;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            for(ibulan = 1; ibulan < 13; ibulan++) {
                String selectQuery = ("select COUNT(*) FROM logdosaku WHERE bulan =" + ibulan +
                        " and tahun =" + tahun);
                Cursor cursor =  db.rawQuery(selectQuery, null);
                if (cursor.moveToLast())
                    banyakDosa[ibulan] = cursor.getInt(3);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return banyakDosa;
    }

    public int[] hitungBanyakDosaTahun(){
        int[] banyakDosa = new int[20];
        int itahun = 2015;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            for(itahun = 2015; itahun < 2026; itahun++) {
                String selectQuery = ("select COUNT(*) FROM logdosaku WHERE tahun =" + itahun);
                Cursor cursor =  db.rawQuery(selectQuery, null);
                if (cursor.moveToLast())
                    banyakDosa[itahun] = cursor.getInt(3);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return banyakDosa;
    }

    public void resetDosa() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DOSAKU_TABLE_NAME, null, null);
    }

//    public ArrayList<String> getAllCotacts()
//    {
//        ArrayList<String> array_list = new ArrayList<String>();
//
//        //hp = new HashMap();
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor res =  db.rawQuery( "select * from contacts", null );
//        res.moveToFirst();
//
//        while(res.isAfterLast() == false){
//            array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)));
//            res.moveToNext();
//        }
//        return array_list;
//    }
}