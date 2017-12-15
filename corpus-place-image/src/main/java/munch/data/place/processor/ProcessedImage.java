package munch.data.place.processor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import munch.data.place.collector.CollectedImage;
import munch.finn.FinnLabel;

import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 4:38 PM
 * Project: munch-data
 */
public class ProcessedImage {
    private CollectedImage image;
    private FinnLabel finnLabel;

    public CollectedImage getImage() {
        return image;
    }

    public void setImage(CollectedImage image) {
        this.image = image;
    }

    public FinnLabel getFinnLabel() {
        return finnLabel;
    }

    public void setFinnLabel(FinnLabel finnLabel) {
        this.finnLabel = finnLabel;
    }

    /**
     * @param label is image that label
     * @param min   is image label higher then min
     * @return true is all condition is met
     */
    @JsonIgnore
    public boolean isOutput(String label, float min) {
        Map.Entry<String, Float> max = getFinnLabel().getMaxOutput();
        if (!max.getKey().equals(label)) return false;
        return max.getValue() > min;
    }
}
