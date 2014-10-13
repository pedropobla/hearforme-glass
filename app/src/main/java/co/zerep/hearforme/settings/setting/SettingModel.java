package co.zerep.hearforme.settings.setting;

public class SettingModel {
    private String id;
    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SettingModel(String id, String value) {
        this.id = id;
        this.value = value;
    }
}
