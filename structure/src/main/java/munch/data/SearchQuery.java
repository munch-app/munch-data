package munch.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashSet;
import java.util.Set;

/**
 * This is a structure for place query
 * If distance and polygon is both present, distance will be used
 * <p>
 * <p>
 * Created By: Fuxing Loh
 * Date: 20/4/2017
 * Time: 8:34 PM
 * Project: munch-core
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SearchQuery {
    private Integer from;
    private Integer size;

    private String query;
    private String latLng;
    private Location location;

    private Filter filter;
    private Sort sort;

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * Data that affects query
     * 1. Place Name
     * 2. Location Name - Bishan
     * 3. Amenities (A) - Wheelchair Friendly
     * 4. Speciality (A) - Xiao Long Bao
     * 5. Occasion (A) - Birthday Celebration
     * 6. Establishment (A) - Restaurant
     * 7. Cuisine (A) - Chinese
     *
     * @return query string
     */
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Location is Polygon, latLng is user location
     *
     * @return latLng of user location
     */
    public String getLatLng() {
        return latLng;
    }

    public void setLatLng(String latLng) {
        this.latLng = latLng;
    }

    /**
     * Location is Polygon, latLng is user location
     * <p>
     * location.name = for display
     * location.center = currently provide no functions
     * location.points = polygon points
     * <p>
     * For ad-hoc location, should be generated here as well
     *
     * @return Location for searchQuery
     * @see Location
     */
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Optional
     *
     * @return Place additional filters
     */
    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    /**
     * Add sort by distance
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Filter {
        private Price price;
        private Tag tag;
        private Hour hour;

        public Price getPrice() {
            return price;
        }

        public void setPrice(Price price) {
            this.price = price;
        }

        public Tag getTag() {
            return tag;
        }

        public void setTag(Tag tag) {
            this.tag = tag;
        }

        public Hour getHour() {
            return hour;
        }

        public void setHour(Hour hour) {
            this.hour = hour;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Price {
            private Double min;
            private Double max;

            public Double getMin() {
                return min;
            }

            public void setMin(Double min) {
                this.min = min;
            }

            public Double getMax() {
                return max;
            }

            public void setMax(Double max) {
                this.max = max;
            }

            @Override
            public String toString() {
                return "Price{" +
                        "min=" + min +
                        ", max=" + max +
                        '}';
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Tag {
            private Set<String> positives;
            private Set<String> negatives;

            public Set<String> getPositives() {
                return positives;
            }

            public void setPositives(Set<String> positives) {
                this.positives = positives;
            }

            public Set<String> getNegatives() {
                return negatives;
            }

            public void setNegatives(Set<String> negatives) {
                this.negatives = negatives;
            }

            @Override
            public String toString() {
                return "Tag{" +
                        "positives=" + positives +
                        ", negatives=" + negatives +
                        '}';
            }
        }

        public static class Hour {
            private String day;
            private String time;

            /**
             * @return day which it is open
             * @see Place.Hour#day
             */
            public String getDay() {
                return day;
            }

            public void setDay(String day) {
                this.day = day;
            }

            /**
             * @return time which it is open
             * @see Place.Hour#open
             * @see Place.Hour#close
             */
            public String getTime() {
                return time;
            }

            public void setTime(String time) {
                this.time = time;
            }

            @Override
            public String toString() {
                return "Hour{" +
                        "day='" + day + '\'' +
                        ", time='" + time + '\'' +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "Filter{" +
                    "price=" + price +
                    ", tag=" + tag +
                    ", hour=" + hour +
                    '}';
        }
    }

    /**
     * Ordinal sort
     * Only a single sort type can be used
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Sort {
        public static final String TYPE_MUNCH_RANK = "munch_rank";
        public static final String TYPE_PRICE_LOWEST = "price_lowest";
        public static final String TYPE_PRICE_HIGHEST = "price_highest";
        public static final String TYPE_DISTANCE_NEAREST = "distance_nearest";
        public static final String TYPE_RATING_HIGHEST = "rating_highest";

        private String type;

        /**
         * @return sort types
         * @see Sort#TYPE_PRICE_LOWEST
         * @see Sort#TYPE_PRICE_HIGHEST
         * @see Sort#TYPE_DISTANCE_NEAREST
         * @see Sort#TYPE_RATING_HIGHEST
         */
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "Sort{" +
                    "type='" + type + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "SearchQuery{" +
                "from=" + from +
                ", size=" + size +
                ", query='" + query + '\'' +
                ", location=" + location +
                ", filter=" + filter +
                ", sort=" + sort +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final SearchQuery query;

        private Builder() {
            this.query = new SearchQuery();
        }

        public Builder query(String text) {
            query.setQuery(text);
            return this;
        }

        public Builder location(Location location) {
            query.setLocation(location);
            return this;
        }

        public Builder tag(String tag, boolean positive) {
            if (query.getFilter() == null) query.setFilter(new Filter());
            if (query.getFilter().getTag() == null) query.getFilter().setTag(new Filter.Tag());

            if (positive) {
                if (query.getFilter().getTag().getPositives() == null)
                    query.getFilter().getTag().setPositives(new HashSet<>());
                query.getFilter().getTag().getPositives().add(tag);
            } else {
                if (query.getFilter().getTag().getNegatives() == null)
                    query.getFilter().getTag().setNegatives(new HashSet<>());
                query.getFilter().getTag().getNegatives().add(tag);
            }
            return this;
        }

        public Builder tag(String tag) {
            return tag(tag, true);
        }

        public SearchQuery build() {
            return query;
        }
    }
}
