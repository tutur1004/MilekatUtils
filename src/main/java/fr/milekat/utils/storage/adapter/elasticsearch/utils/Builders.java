package fr.milekat.utils.storage.adapter.elasticsearch.utils;

import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class Builders {
    @NotNull
    public static TermQuery.Builder getTermBuilder(@NotNull String tagName, @NotNull Object tagValue) {
        TermQuery.Builder termQuery = new TermQuery.Builder();
        if (tagValue instanceof String value) {
            termQuery.field(tagName).value(value);
        } else if (tagValue instanceof Boolean value) {
            termQuery.field(tagName).value(value);
        } else if (tagValue instanceof Integer value) {
            termQuery.field(tagName).value(value);
        } else if (tagValue instanceof Long value) {
            termQuery.field(tagName).value(value);
        } else if (tagValue instanceof Double value) {
            termQuery.field(tagName).value(value);
        }
        return termQuery;
    }
}
