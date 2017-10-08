package munch.data;

import com.google.inject.Singleton;
import munch.data.database.PlaceEntity;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;

/**
 * Created By: Fuxing Loh
 * Date: 17/4/2017
 * Time: 4:13 PM
 * Project: munch-core
 */
@Singleton
public final class PlaceService extends AbstractService<Place, PlaceEntity> {

    @Inject
    public PlaceService() {
        super("/places", Place.class, PlaceEntity.class);
    }


    @Override
    protected PlaceEntity newEntity(Place data, long cycleNo) {
        PlaceEntity entity = new PlaceEntity();
        entity.setCycleNo(cycleNo);
        entity.setPlaceId(data.getId());
        entity.setData(data);
        return entity;
    }

    @Override
    protected Function<PlaceEntity, String> getKeyMapper() {
        return PlaceEntity::getPlaceId;
    }

    @Override
    protected List<PlaceEntity> getList(List<String> keys) {
        return provider.reduce(em -> em.createQuery(
                "FROM PlaceEntity WHERE id IN (:keys)", PlaceEntity.class)
                .setParameter("keys", keys)
                .getResultList());
    }
}
