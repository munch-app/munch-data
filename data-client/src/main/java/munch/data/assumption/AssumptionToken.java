package munch.data.assumption;

/**
 * Created by: Fuxing
 * Date: 17/3/2018
 * Time: 7:09 PM
 * Project: munch-data
 */
public abstract class AssumptionToken {
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public abstract String getType();

    @Override
    public String toString() {
        return getType() + "[" + text + "]";
    }
}
