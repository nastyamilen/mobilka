package com.example.mobiiilkaaaaa;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "match3_game.db";
    private static final int DATABASE_VERSION = 1;

    // Таблица результатов
    public static final String TABLE_SCORES = "scores";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SCORE = "score";
    public static final String COLUMN_DATE = "date";

    // SQL запрос для создания таблицы
    private static final String DATABASE_CREATE = "create table "
            + TABLE_SCORES + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_SCORE
            + " integer not null, " + COLUMN_DATE
            + " text not null);";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
        onCreate(db);
    }

    // Добавление нового результата
    public long addScore(int score) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCORE, score);
        
        // Текущая дата и время
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String date = dateFormat.format(new Date());
        values.put(COLUMN_DATE, date);
        
        // Вставляем запись
        long id = db.insert(TABLE_SCORES, null, values);
        db.close();
        
        return id;
    }

    // Получение лучшего результата
    public int getBestScore() {
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selectQuery = "SELECT MAX(" + COLUMN_SCORE + ") FROM " + TABLE_SCORES;
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        int bestScore = 0;
        if (cursor.moveToFirst()) {
            bestScore = cursor.getInt(0);
        }
        
        cursor.close();
        db.close();
        
        return bestScore;
    }

    // Получение всех результатов
    public List<ScoreRecord> getAllScores() {
        List<ScoreRecord> scoreList = new ArrayList<>();
        
        String selectQuery = "SELECT * FROM " + TABLE_SCORES + " ORDER BY " + COLUMN_SCORE + " DESC";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                ScoreRecord score = new ScoreRecord();
                score.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                score.setScore(cursor.getInt(cursor.getColumnIndex(COLUMN_SCORE)));
                score.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
                
                scoreList.add(score);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        
        return scoreList;
    }

    // Класс для хранения записи о результате
    public static class ScoreRecord {
        private long id;
        private int score;
        private String date;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }
}
