package co.zerep.hearforme.languages;

import android.content.res.Resources;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import co.zerep.hearforme.HearForMe;
import co.zerep.hearforme.R;
import co.zerep.hearforme.languages.language.Language;

public class Languages {
    private static Resources res = HearForMe.getContext().getResources();
    private static Set<Language> INPUT_LANGUAGES = new HashSet<Language>();
    private static Set<Language> OUTPUT_LANGUAGES = new HashSet<Language>();
    private static Map<String,Language> LANGUAGES_IN_CODE = new HashMap<String,Language>();
    private static Map<String,Language> LANGUAGES_OUT_CODE = new HashMap<String,Language>();
    private static Map<Integer,Language> LANGUAGES_MENU_ID = new HashMap<Integer, Language>();

    public static Language IN_SPANISH_ESP = new Language(Language.Type.INPUT, "spa-ESP",
            res.getString(R.string.spaESP), res.getDrawable(R.drawable.ic_flag_esp),
            R.id.input_spaESP, true);
    public static Language IN_ENGLISH_USA = new Language(Language.Type.INPUT, "eng-USA",
            res.getString(R.string.engUSA), res.getDrawable(R.drawable.ic_flag_usa),
            R.id.input_engUSA, true);
    public static Language IN_CANTONESE = new Language(Language.Type.INPUT, "yue-CHN",
            res.getString(R.string.yueCHN), res.getDrawable(R.drawable.ic_flag_hk),
            R.string.yueCHN, false);
    public static Language OUT_SPANISH = new Language(Language.Type.OUTPUT, "es",
            res.getString(R.string.spaESP), res.getDrawable(R.drawable.ic_flag_esp),
            R.id.output_spaESP, false);

    static {
        INPUT_LANGUAGES.add(IN_SPANISH_ESP);
        INPUT_LANGUAGES.add(IN_ENGLISH_USA);
        INPUT_LANGUAGES.add(IN_CANTONESE);
        OUTPUT_LANGUAGES.add(OUT_SPANISH);

        for(Language lang : INPUT_LANGUAGES) {
            LANGUAGES_IN_CODE.put(lang.getCode(), lang);
            LANGUAGES_MENU_ID.put(lang.getMenuId(), lang);
        }

        for(Language lang : OUTPUT_LANGUAGES) {
            LANGUAGES_OUT_CODE.put(lang.getCode(), lang);
            LANGUAGES_MENU_ID.put(lang.getMenuId(), lang);
        }
    }

    public static Set<Language> getInputLanguages() {
        return Collections.unmodifiableSet(INPUT_LANGUAGES);
    }

    public static Set<Language> getOutputLanguages() {
        return Collections.unmodifiableSet(OUTPUT_LANGUAGES);
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
