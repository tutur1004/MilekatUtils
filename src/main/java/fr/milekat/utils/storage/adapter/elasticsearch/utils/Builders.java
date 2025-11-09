package fr.milekat.utils.storage.adapter.elasticsearch.utils;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class Builders {
    @NotNull
    public static BoolQuery.Builder getBuilder(@NotNull String tagName, @NotNull Object tagValue) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        if (tagValue instanceof String value) {
            boolQuery.must(mu -> mu.match(ma -> ma.field(tagName).query(value)));
        } else if (tagValue instanceof Boolean value) {
            boolQuery.must(mu -> mu.match(ma -> ma.field(tagName).query(value)));
        } else if (tagValue instanceof Integer value) {
            boolQuery.must(mu -> mu.match(ma -> ma.field(tagName).query(value)));
        } else if (tagValue instanceof Long value) {
            boolQuery.must(mu -> mu.match(ma -> ma.field(tagName).query(value)));
        } else if (tagValue instanceof Double value) {
            boolQuery.must(mu -> mu.match(ma -> ma.field(tagName).query(value)));
        }
        return boolQuery;
    }
}
