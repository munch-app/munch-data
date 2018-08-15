package munch.data;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.ConfigFactory;
import corpus.data.CatalystClient;
import corpus.data.CorpusClient;
import corpus.data.DocumentClient;

import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 15/8/18
 * Time: 9:53 PM
 * Project: munch-data
 */
public class FakeV2CatalystModule extends AbstractModule {

    @Provides
    @Singleton
    CorpusClient provideCorpus() {
        return new CorpusClient(ConfigFactory.load().getConfig("fakev2catalyst"));
    }

    @Provides
    @Singleton
    CatalystClient provideCatalyst() {
        return new CatalystClient(ConfigFactory.load().getConfig("fakev2catalyst"));
    }

    @Provides
    @Singleton
    DocumentClient provideDocument() {
        return new DocumentClient(ConfigFactory.load().getConfig("fakev2catalyst"));
    }
}
