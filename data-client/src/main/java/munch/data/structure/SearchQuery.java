package munch.data.structure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
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
    private Double radius;

    private Filter filter;
    private Sort sort;

    private Trigger trigger;

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
     * @return radius if latLng query is used
     */
    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
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

    public Trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
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
        private List<Container> containers;
        private Location location;

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

        public List<Container> getContainers() {
            return containers;
        }

        public void setContainers(List<Container> containers) {
            this.containers = containers;
        }

        /**
         * Location is Polygon, latLng is user location
         * <p>
         * location.name = for display
         * location.latLng = currently provide no functions
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

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Price {
            private String name;
            private Double min;
            private Double max;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

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
                        "name='" + name + '\'' +
                        ", min=" + min +
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

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Hour {
            private String name;

            private String day;
            private String open;
            private String close;

            /**
             * @return name of hour filter
             */
            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

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

            public String getOpen() {
                return open;
            }

            public void setOpen(String open) {
                this.open = open;
            }

            public String getClose() {
                return close;
            }

            public void setClose(String close) {
                this.close = close;
            }

            @Override
            public String toString() {
                return "Hour{" +
                        "name='" + name + '\'' +
                        ", day='" + day + '\'' +
                        ", open='" + open + '\'' +
                        ", close='" + close + '\'' +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "Filter{" +
                    "price=" + price +
                    ", tag=" + tag +
                    ", hour=" + hour +
                    ", containers=" + containers +
                    ", location=" + location +
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
         * @see Sort#TYPE_MUNCH_RANK
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Trigger {
        private int querySearch;
        private int placeClick;
        private int placeImpression;
        private int placePosition;

        public int getQuerySearch() {
            return querySearch;
        }

        public void setQuerySearch(int querySearch) {
            this.querySearch = querySearch;
        }

        public int getPlaceClick() {
            return placeClick;
        }

        public void setPlaceClick(int placeClick) {
            this.placeClick = placeClick;
        }

        public int getPlaceImpression() {
            return placeImpression;
        }

        public void setPlaceImpression(int placeImpression) {
            this.placeImpression = placeImpression;
        }

        public int getPlacePosition() {
            return placePosition;
        }

        public void setPlacePosition(int placePosition) {
            this.placePosition = placePosition;
        }

        @Override
        public String toString() {
            return "Trigger{" +
                    "querySearch=" + querySearch +
                    ", placeClick=" + placeClick +
                    ", placeImpression=" + placeImpression +
                    ", placePosition=" + placePosition +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "SearchQuery{" +
                "from=" + from +
                ", size=" + size +
                ", query='" + query + '\'' +
                ", latLng='" + latLng + '\'' +
                ", radius=" + radius +
                ", filter=" + filter +
                ", sort=" + sort +
                ", trigger=" + trigger +
                '}';
    }
}
