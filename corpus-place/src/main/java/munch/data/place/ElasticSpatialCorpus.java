package munch.data.place;

import com.typesafe.config.Config;
import munch.data.place.elastic.SpatialClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 5/12/2017
 * Time: 11:27 PM
 * Project: munch-data
 */
@Singleton
public final class ElasticSpatialCorpus extends ElasticCorpus {
    private static final Logger logger = LoggerFactory.getLogger(ElasticSpatialCorpus.class);

    @Inject
    public ElasticSpatialCorpus(Config config, SpatialClient elasticClient) {
        super(logger, config.getStringList("place.spatial"), elasticClient);
    }
}
