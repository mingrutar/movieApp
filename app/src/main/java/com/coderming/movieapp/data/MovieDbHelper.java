package com.coderming.movieapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.coderming.movieapp.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by linna on 6/1/2016.
 */
public class MovieDbHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = MovieDbHelper.class.getSimpleName();
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "movies.db";

    private Context mContext;
    public MovieDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(LOG_TAG, "++++ MovieDbHelper: onCtreate called");
        try {
            InputStream is = mContext.getResources().openRawResource(R.raw.create_db);
            InputStreamReader isr = new InputStreamReader(is);
//            int len = is.available() / 2;
//            char[] buffer = new char[len];
//            isr.read(buffer, 0, len);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                line = line.trim();
                if (!line.startsWith("--") && !line.isEmpty()) {
                    sb.append(line);
                    sb.append(" ");
                }
                line = br.readLine();
            }
            String[] queries = sb.toString().split(";");
            for (String queryStr : queries) {
                try {
    //                Log.v(LOG_TAG, queryStr);
                    if (!queryStr.trim().isEmpty()) {
                        db.execSQL(queryStr + ";");
                    }
                } catch (SQLiteException sqlex) {
                    Log.e(LOG_TAG, "onCreate caught SQLiteException", sqlex);
                }
            }
        } catch (IOException ioex ) {
            Log.e(LOG_TAG, "failed to create db", ioex);
            throw new RuntimeException(ioex);
        } finally {
//            if (db.isOpen())
//                db.close();
        }
        Log.v(LOG_TAG, "onCreate finished");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // not in real world
        onCreate(db);

    }

}
