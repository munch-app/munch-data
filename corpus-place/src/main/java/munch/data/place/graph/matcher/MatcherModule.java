package munch.data.place.graph.matcher;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * Created by: Fuxing
 * Date: 31/3/2018
 * Time: 2:25 AM
 * Project: munch-data
 */
public class MatcherModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<Matcher> matcherBinder = Multibinder.newSetBinder(binder(), Matcher.class);
        matcherBinder.addBinding().to(PhoneMatcher.class);
        matcherBinder.addBinding().to(NameMatcher.class);
        matcherBinder.addBinding().to(SpatialMatcher.class);
        matcherBinder.addBinding().to(LocationMatcher.class);

        Multibinder<Searcher> searcherBinder = Multibinder.newSetBinder(binder(), Searcher.class);
        searcherBinder.addBinding().to(PhoneMatcher.class);
        searcherBinder.addBinding().to(SpatialMatcher.class);
        searcherBinder.addBinding().to(LocationMatcher.class);
    }
}
