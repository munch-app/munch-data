package munch.data.database;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import munch.data.Place;
import munch.data.database.hibernate.PojoUserType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;

/**
 * Created by: Fuxing
 * Date: 16/8/2017
 * Time: 10:06 PM
 * Project: munch-core
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@TypeDefs(value = {
        @TypeDef(name = "placeData", typeClass = PlaceEntity.PlaceUserType.class),
})
@Table(indexes = {
        // Cluster name for placeId
        @Index(name = "index_munch_place_entity_cycle_no", columnList = "cycleNo")
})
public final class PlaceEntity implements AbstractEntity<Place> {

    private Long cycleNo;
    private String placeId;
    private Place data;

    @Id
    @Column(columnDefinition = "CHAR(36)", nullable = false, updatable = false)
    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    @Column(nullable = false)
    public Long getCycleNo() {
        return cycleNo;
    }

    public void setCycleNo(Long cycleNo) {
        this.cycleNo = cycleNo;
    }

    @Type(type = "placeData")
    @Column(nullable = false)
    public Place getData() {
        return data;
    }

    public void setData(Place data) {
        this.data = data;
    }

    public final static class PlaceUserType extends PojoUserType<Place> {
        public PlaceUserType() {
            super(Place.class);
        }
    }
}
