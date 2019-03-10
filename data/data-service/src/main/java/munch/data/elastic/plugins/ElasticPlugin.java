package munch.data.elastic.plugins;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import munch.data.elastic.ElasticObject;

/**
 * Created by: Fuxing
 * Date: 2019-03-10
 * Time: 19:20
 * Project: munch-data
 */
public interface ElasticPlugin {
    void serialize(ElasticObject object, ObjectNode node);

    /**
     * You can add as many plugins as you like, plugin order is not specified
     */
    class Module extends AbstractModule {
        @Override
        protected void configure() {
            Multibinder<ElasticPlugin> binder = Multibinder.newSetBinder(binder(), ElasticPlugin.class);
            binder.addBinding().to(GeometryPlugin.class);
            binder.addBinding().to(TimingPlugin.class);
            binder.addBinding().to(SuggestPlacePlugin.class);
            binder.addBinding().to(SuggestCountryPlugin.class);
        }
    }
}
