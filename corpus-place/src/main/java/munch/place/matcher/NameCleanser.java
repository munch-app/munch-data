package munch.place.matcher;

import java.util.List;

/**
 * Created By: Fuxing Loh
 * Date: 8/10/2017
 * Time: 9:07 PM
 * Project: munch-data
 */
public final class NameCleanser {

    public String clean(String name) {
        // TODO Clean Name Sheet
        // TODO Location in Name Sheet

        return box(name)
                .removeLocation(null)
                .removePostfix(null)
                .finish();
    }

    public BoxedString box(String name) {
        return new BoxedString(name);
    }

    public class BoxedString {
        private String text;

        public BoxedString(String text) {
            this.text = text.toLowerCase();
        }

        public BoxedString removeLocation(List<String> texts) {
            // TODO location name
            return this;
        }

        public BoxedString removePostfix(List<String> texts) {
            // TODO postfix
            return this;
        }

        /**
         * @return trimmed cleaned text
         */
        public String finish() {
            // Trim all to 1 spacing
            return text.replaceAll(" {2,}", " ").trim();
        }
    }
}
