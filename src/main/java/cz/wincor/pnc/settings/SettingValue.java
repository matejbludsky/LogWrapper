package cz.wincor.pnc.settings;

/**
 * @author matej.bludsky
 * 
 *         POJO for settings
 */

public class SettingValue {

    private String representation = null;
    private Object value = null;

    public SettingValue(String representation, Object value) {
        super();
        this.representation = representation;
        this.value = value;
    }

    public String getRepresentation() {
        return representation;
    }

    public void setRepresentation(String representation) {
        this.representation = representation;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

}
