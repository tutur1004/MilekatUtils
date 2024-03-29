package fr.milekat.utils.storage.adapter.elasticsearch.utils;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import org.jetbrains.annotations.NotNull;

public class Builders {
    @NotNull
    public static BoolQuery.Builder getBuilder(@NotNull String tagName, @NotNull Object tagValue) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        if (tagValue instanceof String) {
            String value = (String) tagValue;
            boolQuery.must(mu -> mu.match(ma -> ma.field(tagName).query(value)));
        } else if (tagValue instanceof Boolean) {
            Boolean value = (Boolean) tagValue;
            boolQuery.must(mu -> mu.match(ma -> ma.field(tagName).query(value)));
        } else if (tagValue instanceof Integer) {
            Integer value = (Integer) tagValue;
            boolQuery.must(mu -> mu.match(ma -> ma.field(tagName).query(value)));
        } else if (tagValue instanceof Long) {
            Long value = (Long) tagValue;
            boolQuery.must(mu -> mu.match(ma -> ma.field(tagName).query(value)));
        } else if (tagValue instanceof Double) {
            Double value = (Double) tagValue;
            boolQuery.must(mu -> mu.match(ma -> ma.field(tagName).query(value)));
        }
        return boolQuery;
    }
}
