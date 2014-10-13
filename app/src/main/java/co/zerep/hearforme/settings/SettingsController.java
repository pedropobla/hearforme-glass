package co.zerep.hearforme.settings;

import android.content.Context;

import co.zerep.hearforme.HearForMe;
import co.zerep.hearforme.R;
import co.zerep.hearforme.languages.Languages;
import co.zerep.hearforme.languages.language.Language;
import co.zerep.hearforme.settings.setting.SettingDataSource;

public class SettingsController {
    private static final String INPUT_LANGUAGE_ID = "input_language";
    private static final String OUTPUT_LANGUAGE_ID = "output_language";

    private static Context context = HearForMe.getContext();
    private static SettingDataSource settingsData;

    static {
        settingsData = new SettingDataSource(context);
        settingsData.open();
    }

    public static boolean hasInputLanguage() {
        return has(INPUT_LANGUAGE_ID);
    }

    public static boolean hasOutputLanguage() {
        return has(OUTPUT_LANGUAGE_ID);
    }

    public static void createInputLanguage(Language lang) {
        create(INPUT_LANGUAGE_ID, lang.getCode());
    }

    public static void createOutputLanguage(Language lang) {
        create(OUTPUT_LANGUAGE_ID, lang.getCode());
    }

    public static Language getInputLanguage() {
        return Languages.getInputLanguageFromCode(get(INPUT_LANGUAGE_ID));
    }

    public static Language getOutputLanguage() {
        return Languages.getOutputLanguageFromCode(get(OUTPUT_LANGUAGE_ID));
    }

    public static void setInputLanguage(Language lang) {
        set(INPUT_LANGUAGE_ID, lang.getCode());
    }

    public static void setOutputLanguage(Language lang) {
        set(OUTPUT_LANGUAGE_ID, lang.getCode());
    }

    private static boolean has(String id) {
        return settingsData.existsSetting(id);
    }

    private static void create(String id, String value) {
        settingsData.createSetting(id, value);
    }

    private static String get(String id) {
        return settingsData.getSettingById(id).getValue();
    }

    private static void set(String id, String newValue) {
        settingsData.updateSetting(id, newValue);
    }
}
