package fr.milekat.utils.storage.adapter.elasticsearch.features;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch.transform.GetTransformStatsResponse;
import co.elastic.clients.elasticsearch.transform.PivotGroupBy;
import co.elastic.clients.elasticsearch.transform.PutTransformRequest;
import fr.milekat.utils.storage.StorageLoader;
import fr.milekat.utils.storage.adapter.elasticsearch.utils.Mapping;
import fr.milekat.utils.storage.adapter.elasticsearch.utils.TransformType;
import fr.milekat.utils.storage.exceptions.StorageLoadException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

@SuppressWarnings("unused")
public class Transforms {
    private final ElasticsearchClient client;
    private final TransformType transformType;
    private final String transformId;
    private final String indexSource;
    private final String indexDestination;
    private final Map<String, Class<?>> pivotGroups = new HashMap<>();
    private final List<String> latestFields = new ArrayList<>();
    private final String sortedField;

    @SuppressWarnings("unused")
    public Transforms(@NotNull ElasticsearchClient client,
                      @NotNull String indexSource,
                      @NotNull String indexDestination,
                      @NotNull Map<String, Class<?>> pivotGroups) throws StorageLoadException {
        this.transformType = TransformType.PIVOT;
        this.client = client;
        if (pivotGroups.isEmpty()) {
            throw new StorageLoadException("Pivot groups can't be empty !");
        }
        String field = pivotGroups.keySet().stream().findFirst().get();
        this.transformId = (indexDestination + "-" + field).toLowerCase(Locale.ROOT);
        this.indexSource = indexSource;
        this.indexDestination = indexDestination;
        this.pivotGroups.putAll(pivotGroups);
        this.sortedField = null;
        load();
    }

    @SuppressWarnings("unused")
    public Transforms(@NotNull ElasticsearchClient client,
                      @NotNull String indexSource,
                      @NotNull String indexDestination,
                      @NotNull List<String> latestFields,
                      @NotNull String sortedField) throws StorageLoadException {
        this.transformType = TransformType.LATEST;
        this.client = client;
        this.transformId = (indexSource + "-" + indexDestination).toLowerCase(Locale.ROOT);
        this.indexSource = indexSource;
        this.indexDestination = indexDestination;
        this.latestFields.addAll(latestFields);
        this.sortedField = sortedField;
        load();
    }

    public void load() throws StorageLoadException {
        StorageLoader.getStorageLogger().debug("Transform '" + transformId + "' loading...");
        // Check if transform is present if not create it
        if (!isTransformExist()) {
            createTransform();
        }
        // Check if transform is started if not start it
        if (!isTransformStarted()) {
            startTransform();
        }
        StorageLoader.getStorageLogger().debug("Transform '" + transformId + "' loaded and started !");
    }

    /**
     * Check if transform is present on Elasticsearch server
     * @return true if transform is present
     */
    private boolean isTransformExist() {
        try {
            StorageLoader.getStorageLogger().debug("Check if transform '" + transformId + "' is present...");
            client.transform().getTransform(t -> t.transformId(transformId));
            StorageLoader.getStorageLogger().debug("Transform '" + transformId + "' is present !");
            return true;
        } catch (ElasticsearchException | IOException exception) {
            StorageLoader.getStorageLogger().debug("Transform '" + transformId + "' is not present !");
            return false;
        }
    }

    /**
     * Check if transform is started
     * @return true if transform is started
     */
    private boolean isTransformStarted() {
        try {
            StorageLoader.getStorageLogger().debug("Check if transform '" + transformId + "' is started...");
            GetTransformStatsResponse getStatsResponse = client.transform()
                    .getTransformStats(t -> t.transformId(transformId));
            if (!getStatsResponse.transforms().get(0).state().equalsIgnoreCase("started")) {
                StorageLoader.getStorageLogger().debug("Transform '" + transformId + "' is not started !");
                return false;
            } else {
                StorageLoader.getStorageLogger().debug("Transform '" + transformId + "' is started !");
                return true;
            }
        } catch (ElasticsearchException |IOException exception) {
            StorageLoader.getStorageLogger().debug("Transform '" + transformId + "' is not started !");
            return false;
        }
    }

    /**
     * Create transform on Elasticsearch server
     * @throws StorageLoadException if transform create error
     */
    private void createTransform() throws StorageLoadException {
        PutTransformRequest.Builder builder = new PutTransformRequest.Builder()
                .source(s -> s.index(indexSource))
                .transformId(transformId)
                .dest(d -> d.index(indexDestination))
                .sync(s -> s.time(st -> st.field("@timestamp").delay(d -> d.time("3s"))))
                .frequency(f -> f.time("2s"));

        if (transformType.equals(TransformType.PIVOT)) {
            Map<String, PivotGroupBy> pivotGroups = new HashMap<>(this.pivotGroups.entrySet()
                    .stream()
                    .collect(HashMap::new,
                            (m, v) -> m.putAll(Mapping.getMappingPivotGroup(
                                    "tags." + v.getKey(), v.getValue(), v.getKey())),
                            HashMap::putAll
                    ));
            builder.description("Aggregation based on '" + indexSource + "' with field: " +
                    String.join(", ", pivotGroups.keySet()));
            builder.pivot(p -> p
                    .groupBy(pivotGroups)
                    .aggregations("amount", new Aggregation.Builder()
                            .sum(s -> s.field("operation"))
                            .build()
                    )
            );
        } else if (transformType.equals(TransformType.LATEST)) {
            builder.latest(l -> l
                    .uniqueKey(this.latestFields)
                    .sort(this.sortedField)
            );
            builder.description("Aggregation based on '" + indexSource + "' with field: " + sortedField);
        }

        try {
            StorageLoader.getStorageLogger().debug("Transform '" + transformId + "' creating...");
            client.transform().putTransform(builder.build());
            StorageLoader.getStorageLogger().debug("Transform '" + transformId + "' created !");
        } catch (ElasticsearchException | IOException exception) {
            throw new StorageLoadException("Transform create error: " + exception.getMessage());
        }
    }

    /**
     * Start transform on Elasticsearch server
     * @throws StorageLoadException if transform start error
     */
    private void startTransform() throws StorageLoadException {
        try {
            StorageLoader.getStorageLogger().debug("Transform '" + transformId + "' is not started, starting...");
            client.transform().startTransform(t -> t.transformId(transformId));
            StorageLoader.getStorageLogger().debug("Transform '" + transformId + "' started !");
        } catch (ElasticsearchException |IOException exception) {
            throw new StorageLoadException("Transform start error: " + exception.getMessage());
        }
    }
}
