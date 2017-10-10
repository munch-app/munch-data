package munch.catalyst;

import catalyst.utils.exception.DateCompareUtils;
import corpus.data.CorpusData;
import corpus.field.AbstractKey;

import java.time.Duration;

/**
 * Created by: Fuxing
 * Date: 15/8/2017
 * Time: 3:48 PM
 * Project: munch-corpus
 */
public final class ImageCuratorKey extends AbstractKey {
    public static final RefreshedMillis refreshedMillis = new RefreshedMillis();
    public static final Image image = new Image();

    private ImageCuratorKey(String key, boolean multi) {
        super("Global.Munch.ImageCurator." + key, multi);
    }

    public static final class RefreshedMillis extends AbstractKey {
        private RefreshedMillis() {
            super("Global.Munch.ImageCurator.refreshedMillis", false);
        }

        public long getMillis(CorpusData data) {
            if (data == null) return System.currentTimeMillis() - Duration.ofDays(10).toMillis();

            return get(data)
                    .map(field -> Long.parseLong(field.getValue()))
                    .orElseThrow(NullPointerException::new);
        }

        public boolean afterDays(CorpusData data, int days) {
            return DateCompareUtils.after(getMillis(data), Duration.ofDays(days));
        }
    }

    public static final class Image extends AbstractKey {
        private Image() {
            super("Global.Munch.ImageCurator.image", true);
        }

        public boolean isField(CorpusData.Field field) {
            return field.getKey().equals(getKey());
        }

        public String getImageKey(CorpusData.Field field) {
            return field.getMetadata().get("imageKey");
        }
    }
}
