package co.zerep.hearforme.languages.language;

import android.graphics.drawable.Drawable;

public class Language {
    public static enum Type {INPUT, OUTPUT};

    private Type type;
    private String code;
    private String name;
    private Drawable flag;
    private int menuId;
    private boolean translatable;

    public Language(Type type, String code, String name, Drawable flag, int menuId, boolean translatable) {
        this.type = type;
        this.code = code;
        this.name = name;
        this.flag = flag;
        this.menuId = menuId;
        this.translatable = translatable;
    }

    public Type getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Drawable getFlag() {
        return flag;
    }

    public int getMenuId() {
        return menuId;
    }

    public boolean isTranslatable() {
        return translatable;
    }

    @Override
    public String toString() {
        return code;
    }
}
