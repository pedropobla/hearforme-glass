package co.zerep.hearforme;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = MySQLiteHelper.class.getSimpleName();
    private static final String DB_NAME = "settings.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_SETTINGS = "settings";
    public static final String COLUMN_ID = "_setting_id";
    public static final String COLUMN_VALUE = "setting_value";

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_SETTINGS + "("
            + COLUMN_ID + " integer primary key, "
            + COLUMN_VALUE + " integer not null);";

    public MySQLiteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.w(TAG,
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        onCreate(sqLiteDatabase);
    }
}
