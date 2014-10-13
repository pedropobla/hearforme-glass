package co.zerep.hearforme.settings.setting;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SettingDataSource {
    static class MySQLiteHelper extends SQLiteOpenHelper {

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

    private SQLiteDatabase db;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_VALUE};

    public SettingDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public SettingModel createSetting(int id, int defaultValue) {
        ContentValues settingDescription = new ContentValues();
        settingDescription.put(MySQLiteHelper.COLUMN_ID, id);
        settingDescription.put(MySQLiteHelper.COLUMN_VALUE, defaultValue);
        db.insert(MySQLiteHelper.TABLE_SETTINGS, null, settingDescription);
        Cursor cursor = db.query(MySQLiteHelper.TABLE_SETTINGS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = ?", new String[] {String.valueOf(id)},
                null, null, null);
        cursor.moveToFirst();
        SettingModel newSetting = cursorToSetting(cursor);
        cursor.close();
        return newSetting;
    }

    public int updateSetting(int id, int newValue) {
        ContentValues settingDescription = new ContentValues();
        settingDescription.put(MySQLiteHelper.COLUMN_ID, id);
        settingDescription.put(MySQLiteHelper.COLUMN_VALUE, newValue);
        return db.update(MySQLiteHelper.TABLE_SETTINGS, settingDescription,
                MySQLiteHelper.COLUMN_ID + " = ?", new String[] {String.valueOf(id)});
    }

    public boolean existsSetting(int id) {
        Cursor cursor = db.query(MySQLiteHelper.TABLE_SETTINGS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = ?", new String[] {String.valueOf(id)},
                null, null, null);
        cursor.moveToFirst();
        boolean result = !cursor.isAfterLast();
        cursor.close();
        return result;
    }

    public SettingModel getSettingById(int id) {
        Cursor cursor = db.query(MySQLiteHelper.TABLE_SETTINGS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = ?", new String[] {String.valueOf(id)},
                null, null, null);
        cursor.moveToFirst();
        SettingModel setting = cursorToSetting(cursor);
        cursor.close();
        return setting;
    }

    private SettingModel cursorToSetting(Cursor cursor) {
        SettingModel setting = new SettingModel();
        setting.setId(cursor.getInt(0));
        setting.setValue(cursor.getInt(1));
        return setting;
    }
}
