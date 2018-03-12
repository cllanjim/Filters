package com.helloworld.bartender.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.helloworld.bartender.FilterableCamera.FCamera;
import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.FilterableCamera.Filters.OriginalFilter;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by 김현식 on 2018-02-05.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private static String DB_NAME = "filter_list.db";    //sqlite
    private static final int DB_VERSION = 1;
    public static final String TABLE_MAIN_NAME = "list";
    public static final String TYPE1_TABLE_NAME = "OriginalFilter";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FILTER_NAME = "name";
    public static final String COLUMN_FILTER_TYPE = "type";
    public Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(" CREATE TABLE " + TABLE_MAIN_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FILTER_NAME + " TEXT NOT NULL, " +
                COLUMN_FILTER_TYPE + " TEXT NOT NULL);"
        );

        String query = " CREATE TABLE " + TYPE1_TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, ";

        for (OriginalFilter.ValueType valueType : OriginalFilter.ValueType.values()) {
            query += valueType.toString() + " INTEGER NOT NULL, ";
        }
        query += "FOREIGN KEY(" + COLUMN_ID + ") REFERENCES " + TABLE_MAIN_NAME + "(" + COLUMN_ID + ") ON DELETE CASCADE); ";

        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //you can implement here migration process

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAIN_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TYPE1_TABLE_NAME);
        this.onCreate(db);
    }

    public void saveFilter(FCameraFilter filter) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        ContentValues valuesForMain = new ContentValues();

        //id가 존재하고 type이 다르면 기존 type테이블에 있는 튜플 삭제
        if (filter.getId() != null) {
            String type = findTypeWithId(db, filter.getId());
            if (!filter.getClass().getSimpleName().equals(type)) {
                String sql = "DELETE FROM " + type + " WHERE " + COLUMN_ID + "='" + filter.getId().toString() + "'";
                db.execSQL(sql);
            }
        }

        valuesForMain.put(COLUMN_ID, filter.getId());
        valuesForMain.put(COLUMN_FILTER_NAME, filter.getName());
        valuesForMain.put(COLUMN_FILTER_TYPE, filter.getClass().getSimpleName());

        //id null이면 튜플 생성, id가 존재하면 튜플 update
        int lastInsertedId = (int)db.replace(TABLE_MAIN_NAME, null, valuesForMain);

        Log.d("lastinsertedid",String.valueOf(lastInsertedId));

        //type에 따라 테이블과 속성이 바뀜
        switch (filter.getClass().getSimpleName()) {
            case TYPE1_TABLE_NAME:
                values.put(COLUMN_ID,lastInsertedId);
                for (OriginalFilter.ValueType valueType : OriginalFilter.ValueType.values()) {
                    values.put(valueType.toString(), filter.getValueWithType(valueType));
                }
                db.replace(TYPE1_TABLE_NAME, null, values);
                break;
            default:
                break;
        }
        db.close();

    }

    public void pasteFilter(FCameraFilter receivedFilter) {
        FCameraFilter newFilter = null;
        switch (receivedFilter.getClass().getSimpleName()) {
            case TYPE1_TABLE_NAME:
                newFilter = new OriginalFilter(mContext, null);
                for(OriginalFilter.ValueType valueType : OriginalFilter.ValueType.values()){
                    newFilter.setValueWithType(valueType,receivedFilter.getValueWithType(valueType));
                }
                newFilter.setName("CopyOf"+receivedFilter.getName());
                saveFilter(newFilter);
            default:
                break;
        }
    }


    //정렬
    public List<FCameraFilter> getFilterList(String option) {
        String query;
        if (option.equals("")) {
            query = "SELECT * FROM " + TABLE_MAIN_NAME;
        } else {
            query = "SELECT * FROM " + TABLE_MAIN_NAME + " ORDER BY " + option;
        }
        List<FCameraFilter> filterLinkedList = new LinkedList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            int i = 0;
            do {
                String type = cursor.getString(cursor.getColumnIndex(COLUMN_FILTER_TYPE));
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_FILTER_NAME));
                switch (type) {
                    case TYPE1_TABLE_NAME:
                        FCameraFilter filter = new OriginalFilter(mContext, id);
                        filter.setName(name);
                        Cursor typeCursor = db.rawQuery("SELECT * FROM " + TYPE1_TABLE_NAME + " WHERE " + COLUMN_ID + "=" + id, null);
                        typeCursor.moveToFirst();
                        for (OriginalFilter.ValueType valueType : OriginalFilter.ValueType.values()) {
                            filter.setValueWithType(valueType, typeCursor.getInt(typeCursor.getColumnIndex(valueType.toString())));
                        }
                        filterLinkedList.add(filter);
                        break;
                    default:
                        break;
                }
                i++;
            } while (cursor.moveToNext());
            Log.d("x", String.valueOf(i) + "repeated this");

        }
        return filterLinkedList;
    }


    public void deleteFilterRecord(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_MAIN_NAME + " WHERE _id='" + id + "'");
    }

    public String findTypeWithId(SQLiteDatabase db, int id) {
        Cursor cs = db.rawQuery("SELECT * FROM " + TABLE_MAIN_NAME + " WHERE " + COLUMN_ID + "='" + id + "'", null);
        cs.moveToFirst();
        String type = cs.getString(cs.getColumnIndex(COLUMN_FILTER_TYPE));
        return type;
    }

    public void enalbeFk() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.rawQuery("PRAGMA foreign_keys = ON", null);
        db.close();

    }


}
