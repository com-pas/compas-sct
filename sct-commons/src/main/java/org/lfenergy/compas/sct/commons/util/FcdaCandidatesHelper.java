// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public enum FcdaCandidatesHelper implements Predicate<ResumedDataTemplate> {
    SINGLETON;

    private static final String FCDA_CONSTRAINTS_FILE_NAME = "FcdaCandidates.csv";
    public static final int NUMBER_OF_COLUMNS = 4;
    private static final int COLUMN_LN_CLASS = 0;
    private static final int COLUMN_DO_NAME = 1;
    private static final int COLUMN_DA_NAME = 2;
    private static final int COLUMN_FC = 3;
    private static final String DELIMITER = ";";
    private static final String COMMENT = "#";

    private Set<FcdaCandidate> fcdaCandidates;

    /**
     * Alias of isFcdaCandidate(ResumedDataTemplate) to implement Predicate
     * @see #isFcdaCandidate(ResumedDataTemplate)
     */
    @Override
    public boolean test(ResumedDataTemplate resumedDataTemplate) {
        return isFcdaCandidate(resumedDataTemplate);
    }

    /**
     * Check if resumedDataTemplate is isFcdaCandidate
     * @see #isFcdaCandidate(ResumedDataTemplate)
     */
    public boolean isFcdaCandidate(ResumedDataTemplate resumedDataTemplate) {
        return isFcdaCandidate(resumedDataTemplate.getLnClass(),
            resumedDataTemplate.getDoName().toStringWithoutInst(),
            resumedDataTemplate.getDaName().toString(),
            resumedDataTemplate.getFc().value());
    }

    private boolean isFcdaCandidate(String lnClass, String doName, String daName, String fc) {
        if (StringUtils.isBlank(lnClass)
            || StringUtils.isBlank(doName)
            || StringUtils.isBlank(daName)
            || StringUtils.isBlank(fc)) {
            throw new IllegalArgumentException("parameters must not be blank");
        }

        if (fcdaCandidates == null) {
            fcdaCandidates = readFcdaCandidatesFile();
        }
        return fcdaCandidates.contains(new FcdaCandidate(lnClass, doName, daName, fc));
    }

    private Set<FcdaCandidate> readFcdaCandidatesFile() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream inputStream = classloader.getResourceAsStream(FCDA_CONSTRAINTS_FILE_NAME);
             Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            return bufferedReader.lines()
                .map(this::lineToFcdaCandidate)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private FcdaCandidate lineToFcdaCandidate(String rawLine) {
        String line = rawLine.trim();
        if (line.startsWith(COMMENT)){
            return null;
        }
        String[] cells = line.split(DELIMITER);
        if (cells.length != NUMBER_OF_COLUMNS) {
            return null;
        }
        return new FcdaCandidate(
            cells[COLUMN_LN_CLASS],
            cells[COLUMN_DO_NAME],
            cells[COLUMN_DA_NAME],
            cells[COLUMN_FC]);
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    private static class FcdaCandidate {
        private final String lnClass;
        private final String doName;
        private final String daName;
        private final String fc;
    }
}
