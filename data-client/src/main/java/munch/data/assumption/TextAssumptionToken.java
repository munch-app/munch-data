package munch.data.assumption;

/**
 * Created by: Fuxing
 * Date: 17/3/2018
 * Time: 7:09 PM
 * Project: munch-data
 */
public class TextAssumptionToken extends AssumptionToken {
    public TextAssumptionToken(String text) {
        setText(text);
    }

    @Override
    public String getType() {
        return "text";
    }


}
