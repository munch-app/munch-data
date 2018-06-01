package munch.data.tag;

import java.util.Set;

/**
 * Created by: Fuxing
 * Date: 1/6/18
 * Time: 11:18 AM
 * Project: munch-data
 */
public final class TagConfig {
    private String tagId;

    private Predict predict;
    private Place place;
    private Count count;

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public Predict getPredict() {
        return predict;
    }

    public void setPredict(Predict predict) {
        this.predict = predict;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public Count getCount() {
        return count;
    }

    public void setCount(Count count) {
        this.count = count;
    }

    public static class Predict {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public String toString() {
            return "Predict{" +
                    "enabled=" + enabled +
                    '}';
        }
    }

    public static class Place {
        private Integer level;
        private Double order;

        private Set<String> remapping;

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }

        public Double getOrder() {
            return order;
        }

        public void setOrder(Double order) {
            this.order = order;
        }

        public Set<String> getRemapping() {
            return remapping;
        }

        public void setRemapping(Set<String> remapping) {
            this.remapping = remapping;
        }

        @Override
        public String toString() {
            return "Place{" +
                    "level=" + level +
                    ", order=" + order +
                    ", remapping=" + remapping +
                    '}';
        }
    }

    public static class Count {
        private Long predicted;
        private Long imageless;
        private Long total;

        public Long getPredicted() {
            return predicted;
        }

        public void setPredicted(Long predicted) {
            this.predicted = predicted;
        }

        public Long getImageless() {
            return imageless;
        }

        public void setImageless(Long imageless) {
            this.imageless = imageless;
        }

        public Long getTotal() {
            return total;
        }

        public void setTotal(Long total) {
            this.total = total;
        }

        @Override
        public String toString() {
            return "Count{" +
                    "predicted=" + predicted +
                    ", imageless=" + imageless +
                    ", total=" + total +
                    '}';
        }
    }
}
