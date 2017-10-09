package munch.data.clients;

import com.typesafe.config.Config;
import munch.data.structure.Tag;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 10/10/2017
 * Time: 2:16 AM
 * Project: munch-data
 */
@Singleton
public class TagClient {

    @Inject
    public TagClient(Config config) {
        // Search and DynamoDB
    }

    public Tag get(String id) {
        return null;
    }


    public void put(Tag tag) {

    }

    public void delete(String id) {

    }
}
