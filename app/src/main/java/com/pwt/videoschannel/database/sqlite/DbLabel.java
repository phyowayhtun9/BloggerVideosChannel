package com.pwt.videoschannel.database.sqlite;

import static com.pwt.videoschannel.Config.LABELS_SORTING;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pwt.videoschannel.models.Category;

import java.util.ArrayList;
import java.util.List;

public class DbLabel extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "label.db";
    public static final String TABLE_LABEL = "label";
    public static final String ID = "id";
    public static final String LABEL_NAME = "term";
    public static final String LABEL_IMAGE = "image";
    private final SQLiteDatabase db;

    public DbLabel(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTableCategory(db, TABLE_LABEL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LABEL);
        createTableCategory(db, TABLE_LABEL);
    }

    public void truncateTableCategory(String table) {
        db.execSQL("DROP TABLE IF EXISTS " + table);
        createTableCategory(db, table);
    }

    private void createTableCategory(SQLiteDatabase db, String table) {
        String CREATE_TABLE = "CREATE TABLE " + table + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + LABEL_NAME + " TEXT,"
                + LABEL_IMAGE + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.disableWriteAheadLogging();
    }

    public void addListCategory(List<Category> categories, String table) {
        for (Category category : categories) {
            addOneCategory(db, category, table);
        }
        getAllCategory(table);
    }

    public void addOneCategory(SQLiteDatabase db, Category category, String table) {
        ContentValues values = new ContentValues();
        values.put(LABEL_NAME, category.term);
        values.put(LABEL_IMAGE, category.image);
        db.insert(table, null, values);
    }

    public List<Category> getAllCategory(String table) {
        return getAllCategories(table);
    }

    private List<Category> getAllCategories(String table) {
        List<Category> list;
        Cursor cursor;
        cursor = db.rawQuery("SELECT * FROM " + table + " ORDER BY " + LABELS_SORTING, null);
        list = getAllCategoryFormCursor(cursor);
        return list;
    }

    @SuppressLint("Range")
    private List<Category> getAllCategoryFormCursor(Cursor cursor) {
        List<Category> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.term = cursor.getString(cursor.getColumnIndex(LABEL_NAME));
                category.image = cursor.getString(cursor.getColumnIndex(LABEL_IMAGE));
                list.add(category);
            } while (cursor.moveToNext());
        }
        return list;
    }

//    public void AddToFavorite(Post post) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(KEY_POST_ID, post.id);
//        values.put(KEY_POST_TITLE, post.title);
//        db.insert(TABLE_NAME, null, values);
//        db.close();
//    }
//
//    public List<Post> getAllData() {
//        List<Post> dataList = new ArrayList<>();
//        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY _id DESC";
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        if (cursor.moveToFirst()) {
//            do {
//                Post obj = new Post();
//                obj.set_id(Integer.parseInt(cursor.getString(0)));
//                obj.setId(cursor.getString(1));
//                obj.setTitle(cursor.getString(2));
//                obj.setLabels(Collections.singletonList(cursor.getString(3)));
//                obj.setContent(cursor.getString(4));
//                obj.setPublished(cursor.getString(5));
//                dataList.add(obj);
//            } while (cursor.moveToNext());
//        }
//        return dataList;
//    }
//
//    public List<Post> getFavRow(String id) {
//        List<Post> dataList = new ArrayList<>();
//        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE id=" + id;
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        if (cursor.moveToFirst()) {
//            do {
//                Post obj = new Post();
//                obj.set_id(Integer.parseInt(cursor.getString(0)));
//                obj.setId(cursor.getString(1));
//                obj.setTitle(cursor.getString(2));
//                obj.setLabels(Collections.singletonList(cursor.getString(3)));
//                obj.setContent(cursor.getString(4));
//                obj.setPublished(cursor.getString(5));
//                dataList.add(obj);
//            } while (cursor.moveToNext());
//        }
//        return dataList;
//    }
//
//    public void RemoveFav(Post contact) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete(TABLE_NAME, KEY_POST_ID + " = ?", new String[]{String.valueOf(contact.id)});
//        db.close();
//    }

}
