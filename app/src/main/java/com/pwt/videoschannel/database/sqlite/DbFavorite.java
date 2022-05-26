package com.pwt.videoschannel.database.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pwt.videoschannel.models.Post;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DbFavorite extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "favorite.db";
    private static final String TABLE_FAVORITE = "favorite";
    private static final String KEY_ID = "_id";
    private static final String KEY_POST_ID = "id";
    private static final String KEY_POST_TITLE = "title";
    private static final String KEY_POST_CATEGORY = "labels";
    private static final String KEY_POST_CONTENT = "content";
    private static final String KEY_POST_DATE = "published";

    public DbFavorite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_FAVORITE + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_POST_ID + " TEXT,"
                + KEY_POST_TITLE + " TEXT,"
                + KEY_POST_CATEGORY + " TEXT,"
                + KEY_POST_CONTENT + " TEXT,"
                + KEY_POST_DATE + " TEXT"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITE);
        onCreate(db);
    }

    public void AddToFavorite(Post post) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_POST_ID, post.id);
        values.put(KEY_POST_TITLE, post.title);
        values.put(KEY_POST_CATEGORY, String.valueOf(post.labels));
        values.put(KEY_POST_CONTENT, post.content);
        values.put(KEY_POST_DATE, post.published);
        db.insert(TABLE_FAVORITE, null, values);
        db.close();
    }

    public List<Post> getAllData() {
        List<Post> dataList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_FAVORITE + " ORDER BY _id DESC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Post obj = new Post();
                obj.set_id(Integer.parseInt(cursor.getString(0)));
                obj.setId(cursor.getString(1));
                obj.setTitle(cursor.getString(2));
                obj.setLabels(Collections.singletonList(cursor.getString(3)));
                obj.setContent(cursor.getString(4));
                obj.setPublished(cursor.getString(5));
                dataList.add(obj);
            } while (cursor.moveToNext());
        }
        return dataList;
    }

    public List<Post> getFavRow(String id) {
        List<Post> dataList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_FAVORITE + " WHERE id=" + id;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Post obj = new Post();
                obj.set_id(Integer.parseInt(cursor.getString(0)));
                obj.setId(cursor.getString(1));
                obj.setTitle(cursor.getString(2));
                obj.setLabels(Collections.singletonList(cursor.getString(3)));
                obj.setContent(cursor.getString(4));
                obj.setPublished(cursor.getString(5));
                dataList.add(obj);
            } while (cursor.moveToNext());
        }
        return dataList;
    }

    public void RemoveFav(Post contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITE, KEY_POST_ID + " = ?", new String[]{String.valueOf(contact.id)});
        db.close();
    }

}
