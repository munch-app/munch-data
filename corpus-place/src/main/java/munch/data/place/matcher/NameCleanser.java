package munch.data.place.matcher;

import javax.inject.Singleton;

/**
 * Created By: Fuxing Loh
 * Date: 8/10/2017
 * Time: 9:07 PM
 * Project: munch-data
 */
@Singleton
public final class NameCleanser {

    public String clean(String name) {
        // Fix Location in name?
        return new BoxedString(name)
                .removePostfix()
                .finish();
    }

    private class BoxedString {
        private String text;

        public BoxedString(String text) {
            this.text = text.toLowerCase();
        }

        public BoxedString removePostfix() {
            this.text = text.replaceAll("pte\\.? ?ltd\\.?", "");
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
