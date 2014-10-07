package co.zerep.hearforme;

import android.content.Context;

public class Settings {
    private static final int INPUT_LANGUAGE_ID = R.string.input_language;
    private static final int OUTPUT_LANGUAGE_ID = R.string.output_language;

    private Context context;
    private SettingsDataSource settingsData;

    public Settings(Context context) {
        this.context = context;
        this.settingsData = new SettingsDataSource(context);
        settingsData.open();
    }

    @Override
    protected void finalize() throws Throwable {
        settingsData.close();
        settingsData = null;
        context = null;
        super.finalize();
    }

    public boolean hasInputLanguage() {
        return has(INPUT_LANGUAGE_ID);
    }

    public boolean hasOutputLanguage() {
        return has(OUTPUT_LANGUAGE_ID);
    }

    public void createInputLanguage(int value) {
        create(INPUT_LANGUAGE_ID, value);
    }

    public void createOutputLanguage(int value) {
        create(OUTPUT_LANGUAGE_ID, value);
    }

    public String getInputLanguage() {
        return context.getString(get(INPUT_LANGUAGE_ID));
    }

    public String getOutputLanguage() {
        return context.getString(get(OUTPUT_LANGUAGE_ID));
    }

    public void setInputLanguage(int value) {
        set(INPUT_LANGUAGE_ID, value);
    }

    public void setOutputLanguage(int value) {
        set(OUTPUT_LANGUAGE_ID, value);
    }

    private boolean has(int id) {
        return settingsData.existsSetting(id);
    }

    private void create(int id, int value) {
        settingsData.createSetting(id, value);
    }

    private int get(int id) {
        return settingsData.getSettingById(id).getValue();
    }

    private void set(int id, int newValue) {
        settingsData.updateSetting(id, newValue);
    }
}
