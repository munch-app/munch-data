package munch.data.place.collector;

import com.google.common.collect.ImmutableSet;
import corpus.data.CorpusData;
import corpus.field.FieldUtils;

import javax.inject.Singleton;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by: Fuxing
 * Date: 14/12/2017
 * Time: 4:27 PM
 * Project: munch-data
 */
@Singleton
public final class ArticleCollector extends AbstractCollector {
    private static final Set<String> ARTICLE_SOURCE_IDS = ImmutableSet.of(
            "danielfooddiary.com", "sethlui.com", "ladyironchef.com",
            "misstamchiak.com", "sgfoodonfoot.com", "camemberu.com",
            "ieatandeat.com", "aspirantsg.com", "ms-skinnyfat.com", "six-and-seven.com"
    );

    @Override
    public List<CollectedImage> collect(String placeId, List<CorpusData> list) {
        return list.stream()
                .filter(data -> data.getCorpusName().equals("Global.MunchArticle.Article"))
                .filter(data -> ARTICLE_SOURCE_IDS.contains(FieldUtils.getValue(data, "Article.templateId")))
                .flatMap(data -> data.getFields().stream())
                .filter(field -> field.getKey().equals("Article.image"))
                .map(field -> mapField(field, CollectedImage.From.Article))
                .collect(Collectors.toList());
    }
}
