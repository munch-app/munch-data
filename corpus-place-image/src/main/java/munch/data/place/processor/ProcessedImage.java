package munch.data.place.processor;

import munch.data.place.collector.CollectedImage;
import munch.finn.FinnLabel;

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
}
