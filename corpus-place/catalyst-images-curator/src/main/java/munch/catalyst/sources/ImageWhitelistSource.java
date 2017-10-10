package munch.catalyst.sources;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import corpus.blob.ImageMapper;
import corpus.data.CorpusData;
import munch.corpus.docs.GoogleSheet;
import munch.corpus.docs.SheetNotFound;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 6/8/2017
 * Time: 2:03 PM
 * Project: munch-corpus
 */
@Singleton
public final class ImageWhitelistSource {
    private static final Logger logger = LoggerFactory.getLogger(ImageWhitelistSource.class);

    private final String docId;
    private final String indexId;

    private final ImageMapper imageMapper;
    private final List<WhitelistSource> defaultSources;

    private List<WhitelistSource> whitelistSources;

    @Inject
    public ImageWhitelistSource(Config config, ImageMapper imageMapper) {
        this.docId = config.getString("sheet.docId");
        this.indexId = config.getString("sheet.indexId");
        this.imageMapper = imageMapper;
        this.defaultSources = ImmutableList.of(new FacebookSource(imageMapper));
    }

    public void sync() throws IOException, SheetNotFound {
        GoogleSheet googleSheet = new GoogleSheet(docId, indexId);

        final int sourceColumn = googleSheet.getHeaderColumn("SOURCE");
        final int idColumn = googleSheet.getHeaderColumn("ID");
        final int boostColumn = googleSheet.getHeaderColumn("BOOST");

        this.whitelistSources = googleSheet.getRows().stream()
                .map(ss -> create(ss.get(sourceColumn), ss.get(idColumn), ss.get(boostColumn)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Add all default sources
        this.whitelistSources.addAll(defaultSources);
    }

    /**
     * @param corpusData corpus data to extract
     * @return List of SourcedImage if found
     */
    public List<SourcedImage> collect(CorpusData corpusData) {
        for (WhitelistSource source : whitelistSources) {
            // On one match: will return result
            List<SourcedImage> images = source.extract(corpusData);
            if (!images.isEmpty()) return images;
        }

        // Else return empty list
        return Collections.emptyList();
    }

    private WhitelistSource create(String source, String id, String boost) {
        if (StringUtils.isAnyBlank(source, id)) return null;

        switch (source.toLowerCase()) {
            case "article":
                return new ArticleSource(id, parseBoost(boost), imageMapper);
            case "instagram":
                return new InstagramSource(id, parseBoost(boost));
            default:
                return null;
        }
    }

    private static double parseBoost(String boost) {
        if (StringUtils.isBlank(boost)) return 1.0;

        try {
            return Double.parseDouble(boost);
        } catch (NumberFormatException ignored) {
            logger.info("Failed to parse boost into double, boost: {}", boost);
            return 1.0;
        }
    }
}
