package co.zerep.hearforme;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class SettingsDataSource {
    private SQLiteDatabase db;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_VALUE};

    public SettingsDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Setting createSetting(int id, int defaultValue) {
        ContentValues settingDescription = new ContentValues();
        settingDescription.put(MySQLiteHelper.COLUMN_ID, id);
        settingDescription.put(MySQLiteHelper.COLUMN_VALUE, defaultValue);
        db.insert(MySQLiteHelper.TABLE_SETTINGS, null, settingDescription);
        Cursor cursor = db.query(MySQLiteHelper.TABLE_SETTINGS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = ?", new String[] {String.valueOf(id)},
                null, null, null);
        cursor.moveToFirst();
        Setting newSetting = cursorToSetting(cursor);
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

    public void deleteSetting(Setting setting) {
        int id = setting.getId();
        db.delete(MySQLiteHelper.TABLE_SETTINGS, MySQLiteHelper.COLUMN_ID + " = " + id, null);
    }

    public boolean existsSetting(int id) {
        Cursor cursor = db.query(MySQLiteHelper.TABLE_SETTINGS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + id, null,
                null, null, null);
        cursor.moveToFirst();
        boolean result = !cursor.isAfterLast();
        cursor.close();
        return result;
    }

    public Setting getSettingById(int id) {
        Cursor cursor = db.query(MySQLiteHelper.TABLE_SETTINGS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + id, null,
                null, null, null);
        cursor.moveToFirst();
        Setting setting = cursorToSetting(cursor);
        cursor.close();
        return setting;
    }

    public List<Setting> getAllSettings() {
        List<Setting> settings = new ArrayList<Setting>();
        Cursor cursor = db.query(MySQLiteHelper.TABLE_SETTINGS,
                allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Setting setting = cursorToSetting(cursor);
            settings.add(setting);
            cursor.moveToNext();
        }
        cursor.close();
        return settings;
    }

    private Setting cursorToSetting(Cursor cursor) {
        Setting setting = new Setting();
        setting.setId(cursor.getInt(0));
        setting.setValue(cursor.getInt(1));
        return setting;
    }
}
