// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import lombok.experimental.UtilityClass;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

/**
 * Utility class to parse CSV files.
 * This utility class intention is to normalize all CSV inputs in the project:
 * - Separator is {@link CsvUtils#SEPARATOR}.
 * - Lines starting with {@link CsvUtils#COMMENT_PREFIX} will be ignored. Allow to write copyright and headers at the beginning of the file for example.
 * - blank lines are ignored
 */
@UtilityClass
public class CsvUtils {
    private static final char SEPARATOR = ';';
    private static final String COMMENT_PREFIX = "#";

    /**
     * Read CSV from a resource
     *
     * @param resourcePath path of the resource
     * @param charset      charset of the resource
     * @param targetClass  Each row will be mapped to this class.
     * @return list of rows, mapped as targetClass
     */
    public static <T> List<T> parseRows(String resourcePath, Charset charset, Class<T> targetClass) {
        InputStream inputStream = Objects.requireNonNull(CsvUtils.class.getClassLoader().getResourceAsStream(resourcePath), "Resource not found: " + resourcePath);
        InputStreamReader csvReader = new InputStreamReader(inputStream, charset);
        return parseRows(csvReader, targetClass);
    }

    /**
     * Read CSV from a Reader.
     * Reader will be automatically closed when the method returns or throw an exception.
     * @param csvSource   CSV input
     * @param targetClass Each row will be mapped to this class.
     * @return list of rows, mapped as targetClass
     */
    public static <T> List<T> parseRows(Reader csvSource, Class<T> targetClass) {
        ColumnPositionMappingStrategy<T> columnPositionMappingStrategy = new ColumnPositionMappingStrategy<>();
        columnPositionMappingStrategy.setType(targetClass);
        try (csvSource) {
            return new CsvToBeanBuilder<T>(csvSource)
                .withType(targetClass)
                .withSeparator(SEPARATOR)
                .withIgnoreLeadingWhiteSpace(true)
                .withIgnoreEmptyLine(true)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                .withFilter(line -> line != null && line.length > 0 && (line[0] == null || !line[0].stripLeading().startsWith(COMMENT_PREFIX)))
                .withMappingStrategy(columnPositionMappingStrategy)
                .build()
                .parse();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
