// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Getter;
import org.lfenergy.compas.scl2007b4.model.TFCDA;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;

import java.io.Reader;
import java.util.List;

/**
 * This class is a helper method to load FCDA from a CSV file for use with {@link org.lfenergy.compas.sct.commons.scl.HmiService#createAllHmiReportControlBlocks}
 * Use the getter to access the list of parsed FCDA.
 *
 * @see CsvUtils
 * @see org.lfenergy.compas.sct.commons.scl.HmiService#createAllHmiReportControlBlocks
 */
public class FcdaCsvHelper {

    @Getter
    private final List<TFCDA> fcdas;

    /**
     * Constructor
     * Provide the CSV file as a Reader. For example, you can create a reader like this :
     * <code>new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName), StandardCharsets.UTF_8);</code>
     *
     * @param csvSource a reader that provides the data as CSV
     */
    public FcdaCsvHelper(Reader csvSource) {
        fcdas = CsvUtils.parseRows(csvSource, Row.class).stream()
                .map(row ->
                        SclConstructorHelper.newFcda(row.ldInst, row.lnClass, row.lnInst, row.prefix, row.doName, null, row.fc)
                )
                .toList();
    }

    public static class Row {
        @CsvBindByPosition(position = 0)
        private String ldInst;
        @CsvBindByPosition(position = 1)
        private String prefix;
        @CsvBindByPosition(position = 2)
        private String lnClass;
        @CsvBindByPosition(position = 3)
        private String lnInst;
        @CsvBindByPosition(position = 4)
        private String doName;
        @CsvBindByPosition(position = 5)
        private TFCEnum fc;
    }

}
