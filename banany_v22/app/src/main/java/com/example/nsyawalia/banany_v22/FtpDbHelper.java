package com.example.nsyawalia.banany_v22;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;

public abstract class  FtpDbHelper extends SQLiteOpenHelper {
    public static final String COLUMN_HOST_NAME = "hostName";
    public static final String COLUMN_LOCAL_DIR = "localDirectory";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_PORT = "port";
    public static final String COLUMN_REMOTE_DIR = "remoteDirectory";
    public static final String COLUMN_SERVER_NAME = "serverName";
    public static final String COLUMN_USERNAME = "username";
    public static final String TABLE_NAME = "ftpServer";


}
