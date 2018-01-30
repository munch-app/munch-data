package munch.data.place.text;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 3:56 PM
 * Project: munch-data
 */
public class CollectedText {
    public enum From {
        Place,
        Instagram,
        Article
    }

    private From from;
    private String uniqueId;

    private String content;

    public From getFrom() {
        return from;
    }

    public void setFrom(From from) {
        this.from = from;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
