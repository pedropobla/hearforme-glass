package co.zerep.hearforme.languages;

import android.content.res.Resources;
import android.content.res.TypedArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.zerep.hearforme.HearForMe;
import co.zerep.hearforme.R;
import co.zerep.hearforme.languages.language.Language;

public class Languages {
    private static Resources res = HearForMe.getContext().getResources();
    private static TypedArray inputLanguagesCodes =
            res.obtainTypedArray(R.array.input_languages_codes);
    private static TypedArray inputLanguagesNames =
            res.obtainTypedArray(R.array.input_languages_names);
    private static TypedArray inputLanguagesFlags =
            res.obtainTypedArray(R.array.input_languages_flags);
    private static TypedArray inputLanguagesIds =
            res.obtainTypedArray(R.array.input_languages_ids);
    private static TypedArray inputLanguagesTranslatables =
            res.obtainTypedArray(R.array.input_languages_translatables);

    private static List<Language> INPUT_LANGUAGES = new ArrayList<Language>();
    private static List<Language> OUTPUT_LANGUAGES = new ArrayList<Language>();
    private static Map<String,Language> LANGUAGES_IN_CODE = new HashMap<String,Language>();
    private static Map<String,Language> LANGUAGES_OUT_CODE = new HashMap<String,Language>();
    private static Map<Integer,Language> LANGUAGES_MENU_ID = new HashMap<Integer, Language>();

    public static Language IN_ENGLISH_USA = new Language(Language.Type.INPUT, "eng-USA",
            res.getString(R.string.engUSA), res.getDrawable(R.drawable.ic_flag_engusa),
            R.id.input_engUSA, true);
    public static Language OUT_SPANISH = new Language(Language.Type.OUTPUT, "es",
            res.getString(R.string.spaESP), res.getDrawable(R.drawable.ic_flag_spaesp),
            R.id.output_spaESP, false);

    static {
        for (int i = 0; i < inputLanguagesCodes.length(); ++i) {
            INPUT_LANGUAGES.add(new Language(Language.Type.INPUT, inputLanguagesCodes.getString(i),
                    inputLanguagesNames.getString(i), inputLanguagesFlags.getDrawable(i),
                    inputLanguagesIds.getResourceId(i, 0),
                    inputLanguagesTranslatables.getBoolean(i, false)));
        }

        OUTPUT_LANGUAGES.add(OUT_SPANISH);

        Collections.sort(INPUT_LANGUAGES);
        Collections.sort(OUTPUT_LANGUAGES);

        for(Language lang : INPUT_LANGUAGES) {
            LANGUAGES_IN_CODE.put(lang.getCode(), lang);
            LANGUAGES_MENU_ID.put(lang.getMenuId(), lang);
        }

        for(Language lang : OUTPUT_LANGUAGES) {
            LANGUAGES_OUT_CODE.put(lang.getCode(), lang);
            LANGUAGES_MENU_ID.put(lang.getMenuId(), lang);
        }
    }

    public static List<Language> getInputLanguages() {
        return Collections.unmodifiableList(INPUT_LANGUAGES);
    }

    public static List<Language> getOutputLanguages() {
        return Collections.unmodifiableList(OUTPUT_LANGUAGES);
    }

    public static Language getInputLanguageFromCode(String code) {
        return LANGUAGES_IN_CODE.get(code);
    }

    public static Language getOutputLanguageFromCode(String code) {
        return LANGUAGES_OUT_CODE.get(code);
    }

    public static Language getLanguageFromMenuId(int menuId) {
        return LANGUAGES_MENU_ID.get(menuId);
    }
}
