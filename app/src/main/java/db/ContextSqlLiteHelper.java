package db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContextSqlLiteHelper extends SQLiteOpenHelper {
    private static final String EMPTY_DB_SCRIPT_MESSAGE = "Cannot create database with empty scripts' collection.";
    private String[] onCreateScripts;

    public ContextSqlLiteHelper(Context context, String name, int version, String[] scripts){
        //always set SQLiteDatabase.CursorFactory to null
        super(context, name, null, version);

        onCreateScripts = scripts;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create structure of database
        for (int i = 0; i<onCreateScripts.length; i++)
            db.execSQL(onCreateScripts[i]);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
