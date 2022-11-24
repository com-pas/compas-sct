// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum FcdaCandidates {
    SINGLETON;

    private static final String FCDA_CONSTRAINTS_FILE_NAME = "FcdaCandidates.csv";
    private static final char SEPARATOR = ';';
    public static final int HEADER_LINES = 5;

    private Set<FcdaCandidate> candidates;

    /**
     * Check if resumedDataTemplate is contains
     *
     * @see #contains(ResumedDataTemplate)
     */
    public boolean contains(ResumedDataTemplate resumedDataTemplate) {
        return contains(resumedDataTemplate.getLnClass(),
            resumedDataTemplate.getDoName().toStringWithoutInst(),
            resumedDataTemplate.getDaName().toString(),
            resumedDataTemplate.getFc().value());
    }

    boolean contains(String lnClass, String doName, String daName, String fc) {
        if (StringUtils.isBlank(lnClass)
            || StringUtils.isBlank(doName)
            || StringUtils.isBlank(daName)
            || StringUtils.isBlank(fc)) {
            throw new IllegalArgumentException("parameters must not be blank");
        }

        if (candidates == null) {
            // using a HashSet because "HashSet.contains" is faster than "ArrayList.contains"
            candidates = new HashSet<>(readFcdaCandidatesFile());
        }
        return candidates.contains(new FcdaCandidate(lnClass, doName, daName, fc));
    }

    private List<FcdaCandidate> readFcdaCandidatesFile() {
        try (InputStreamReader inputStreamReader = new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream(FCDA_CONSTRAINTS_FILE_NAME), StandardCharsets.UTF_8)) {
            ColumnPositionMappingStrategy<FcdaCandidate> columnPositionMappingStrategy = new ColumnPositionMappingStrategy<>();
            columnPositionMappingStrategy.setType(FcdaCandidate.class);
            return new CsvToBeanBuilder<FcdaCandidate>(inputStreamReader)
                .withSeparator(SEPARATOR)
                .withIgnoreLeadingWhiteSpace(true)
                .withSkipLines(HEADER_LINES)
                .withType(FcdaCandidate.class)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                .withMappingStrategy(columnPositionMappingStrategy)
                .build()
                .parse();
        } catch (IOException e) {
            throw new UncheckedIOException("Error when closing file " + FCDA_CONSTRAINTS_FILE_NAME, e);
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    public static class FcdaCandidate {
        @CsvBindByPosition(position = 0)
        private String lnClass;
        @CsvBindByPosition(position = 1)
        private String doName;
        @CsvBindByPosition(position = 2)
        private String daName;
        @CsvBindByPosition(position = 3)
        private String fc;
    }
}
