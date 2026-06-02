package com.zomato.recommendation.infrastructure.dataset;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class LocalDatasetReader {

    private static final Logger log = LoggerFactory.getLogger(LocalDatasetReader.class);

    public Optional<RawDataset> readFirstCsv(Path rawDirectory) throws IOException {
        if (!Files.isDirectory(rawDirectory)) {
            return Optional.empty();
        }

        try (Stream<Path> paths = Files.list(rawDirectory)) {
            Optional<Path> csvFile = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".csv"))
                    .sorted()
                    .findFirst();

            if (csvFile.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(readCsv(csvFile.get()));
        }
    }

    public RawDataset readCsv(Path csvPath) throws IOException {
        log.info("Reading raw dataset from {}", csvPath.toAbsolutePath());
        try (Reader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            List<String> headers = parser.getHeaderNames();
            Map<String, String> headerMap = ColumnMapping.resolveHeaderMap(headers);

            if (!headerMap.containsKey(ColumnMapping.FIELD_NAME) || !headerMap.containsKey(ColumnMapping.FIELD_CITY)) {
                throw new IllegalArgumentException(
                        "CSV missing required columns (name, city). Found headers: " + headers);
            }

            List<Map<String, String>> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                Map<String, String> row = new LinkedHashMap<>();
                for (String header : headers) {
                    row.put(header, record.get(header));
                }
                rows.add(row);
            }

            return new RawDataset(csvPath.getFileName().toString(), headerMap, rows);
        }
    }

    public record RawDataset(String sourceName, Map<String, String> headerMap, List<Map<String, String>> rows) {
    }
}
