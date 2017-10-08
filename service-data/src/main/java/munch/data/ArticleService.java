package munch.data;

import com.google.inject.Singleton;
import munch.data.database.ArticleEntity;
import munch.restful.server.JsonCall;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created By: Fuxing Loh
 * Date: 17/4/2017
 * Time: 4:13 PM
 * Project: munch-core
 */
@Singleton
public final class ArticleService extends AbstractService<Article, ArticleEntity> {

    @Inject
    public ArticleService() {
        super("/places/:placeId/articles", Article.class, ArticleEntity.class);
    }

    @Override
    public void route() {
        GET("/places/:placeId/articles/list", this::list);
        super.route();
    }

    @Override
    protected ArticleEntity newEntity(Article data, long cycleNo) {
        ArticleEntity entity = new ArticleEntity();
        entity.setCycleNo(cycleNo);
        entity.setPlaceId(data.getPlaceId());
        entity.setArticleId(data.getArticleId());
        entity.setSortKey(data.getSortKey());
        entity.setData(data);
        return entity;
    }

    @Override
    protected Function<ArticleEntity, String> getKeyMapper() {
        return ArticleEntity::getArticleId;
    }

    @Override
    protected List<ArticleEntity> getList(List<String> keys) {
        return provider.reduce(em -> em.createQuery(
                "FROM ArticleEntity WHERE articleId IN (:keys)", ArticleEntity.class)
                .setParameter("keys", keys)
                .getResultList());
    }

    private List<Article> list(JsonCall call) {
        String placeId = call.pathString("placeId");
        int size = call.queryInt("size");
        String maxSortKey = call.queryString("maxSortKey", null);

        if (maxSortKey == null) {
            return provider.reduce(em -> em.createQuery("FROM ArticleEntity WHERE " +
                    "placeId = :placeId ORDER BY sortKey DESC", ArticleEntity.class)
                    .setParameter("placeId", placeId)
                    .setMaxResults(size)
                    .getResultList())
                    .stream()
                    .map(ArticleEntity::getData)
                    .collect(Collectors.toList());
        }

        return provider.reduce(em -> em.createQuery("FROM ArticleEntity WHERE " +
                "placeId = :placeId AND sortKey < :maxSortKey ORDER BY sortKey DESC", ArticleEntity.class)
                .setParameter("placeId", placeId)
                .setParameter("maxSortKey", maxSortKey)
                .setMaxResults(size)
                .getResultList())
                .stream()
                .map(ArticleEntity::getData)
                .collect(Collectors.toList());
    }
}
