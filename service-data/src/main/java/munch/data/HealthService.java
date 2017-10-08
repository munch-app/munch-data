package munch.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.munch.hibernate.utils.TransactionProvider;
import munch.restful.server.JsonCall;
import munch.restful.server.JsonService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 20/8/2017
 * Time: 8:53 AM
 * Project: munch-core
 */
@Singleton
public final class HealthService implements JsonService {

    private final TransactionProvider provider;

    @Inject
    public HealthService(TransactionProvider provider) {
        this.provider = provider;
    }

    @Override
    public void route() {
        GET("/health/check", this::check);
    }

    private JsonNode check(JsonCall call) {
        return provider.reduce(em -> {
            em.createQuery("SELECT p.cycleNo FROM PlaceEntity p")
                    .setMaxResults(1)
                    .getResultList();
            return Meta200;
        });
    }
}
