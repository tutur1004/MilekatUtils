package fr.milekat.utils.storage.adapter.sql.utils;

import fr.milekat.utils.storage.adapter.sql.connection.SQLDataBaseClient;
import fr.milekat.utils.storage.exceptions.StorageLoadException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Schema {
    private final SQLDataBaseClient DB;
    private final String PREFIX;

    public Schema(SQLDataBaseClient DB, InputStream schemaFile, String prefix) throws StorageLoadException {
        this.DB = DB;
        this.PREFIX = prefix;
        this.applySQLSchema(schemaFile);

    }

    private void applySQLSchema(InputStream schemaFile) throws StorageLoadException {
        List<String> statements;
        //  Read schema file
        try (InputStream schemaFileIS = schemaFile) {
            if (schemaFileIS == null) {
                throw new StorageLoadException("Missing schema file");
            }
            statements = this.getQueries(schemaFileIS).stream()
                    .map(this::formatQuery)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new StorageLoadException("Error while reading schema file");
        }
        //  Apply Schema
        try (Connection connection = DB.getConnection();
             Statement s = connection.createStatement()) {
            connection.setAutoCommit(false);
            for (String query : statements) {
                s.addBatch(query);
            }
            s.executeBatch();
        } catch (Exception exception) {
            if (!exception.getMessage().contains("already exists")) {
                throw new StorageLoadException("Error while loading schema file");
            }
        }
    }
    /**
     * Convert file input stream into List of SQL queries
     */
    private @NotNull List<String> getQueries(InputStream is) throws IOException {
        List<String> queries = new LinkedList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("--") || line.startsWith("#")) {
                    continue;
                }

                sb.append(line);

                // check for end of declaration
                if (line.endsWith(";")) {
                    sb.deleteCharAt(sb.length() - 1);

                    String result = sb.toString().trim();
                    if (!result.isEmpty()) {
                        queries.add(result);
                    }

                    // reset
                    sb = new StringBuilder();
                }
            }
        }

        return queries;
    }

    /**
     * Format query by replacing {prefix}
     */
    @Contract(pure = true)
    private @NotNull String formatQuery(@NotNull String query) {
        return query.replaceAll("\\{prefix}", PREFIX);
    }
}
