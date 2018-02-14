package munch.data.hour.tokens;

import java.util.Objects;

/**
 * Created to remove false positive from hour tokens
 * TODO: Not implemented yet due to the complexity of this structure
 * <p>
 * Created by: Fuxing
 * Date: 14/2/18
 * Time: 3:17 PM
 * Project: munch-data
 */
public class DateToken {


    public final String text;

    private DateToken(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateToken dateToken = (DateToken) o;
        return Objects.equals(text, dateToken.text);
    }

    @Override
    public int hashCode() {

        return Objects.hash(text);
    }

    @Override
    public String toString() {
        return "(Date: " + text + ")";
    }
}
