// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Getter;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TFCDA;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;
import org.lfenergy.compas.sct.commons.dto.FcdaForDataSetsCreation;

import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is a helper method to load FCDA from a CSV files for use with
 * {@link org.lfenergy.compas.sct.commons.HmiService#createAllHmiReportControlBlocks(SCL, List)}
 * {@link org.lfenergy.compas.sct.commons.ExtRefService#createDataSetAndControlBlocks(SCL, Set)}
 * {@link org.lfenergy.compas.sct.commons.ExtRefService#createDataSetAndControlBlocks(SCL, String, Set)}
 * {@link org.lfenergy.compas.sct.commons.ExtRefService#createDataSetAndControlBlocks(SCL, String, String, Set)}
 * Use the getter to access the list of parsed FCDA.
 *
 * @see CsvUtils
 */
public class FcdaCsvHelper {

    @Getter
    private final List<TFCDA> fcdaForHmiReportControls;

    @Getter
    private final Set<FcdaForDataSetsCreation> fcdaForDataSets;

    /**
     * Constructor
     * Provide the CSV files as a Reader. For example, you can create a reader like this :
     * <code>new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName), StandardCharsets.UTF_8);</code>
     *
     * @param csvSourceForHmiReportControl        a reader that provides the FCDA datas for HMI ReportControl Blocks as CSV
     * @param csvSourceForDataSetAndControlBlocks a reader that provides the FCDA datas for DataSets and Control Blocks creation as CSV
     */
    public FcdaCsvHelper(Reader csvSourceForHmiReportControl, Reader csvSourceForDataSetAndControlBlocks) {
        fcdaForHmiReportControls = CsvUtils.parseRows(csvSourceForHmiReportControl, FcdaForHmiReportControl.class).stream()
                .map(fcdaForHmiReportControl ->
                        SclConstructorHelper.newFcda(fcdaForHmiReportControl.ldInst, fcdaForHmiReportControl.lnClass, fcdaForHmiReportControl.lnInst, fcdaForHmiReportControl.prefix, fcdaForHmiReportControl.doName, null, fcdaForHmiReportControl.fc)
                )
                .toList();
        fcdaForDataSets = new HashSet<>(CsvUtils.parseRows(csvSourceForDataSetAndControlBlocks, FcdaForDataSetsCreation.class));
    }

    public static class FcdaForHmiReportControl {
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

   /* @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @EqualsAndHashCode
    public static class FcdaForDataSets {
        @CsvBindByPosition(position = 0)
        private String lnClass;
        @CsvBindByPosition(position = 1)
        private String doName;
        @CsvBindByPosition(position = 2)
        private String daName;
        @CsvBindByPosition(position = 3)
        private String fc;
    } */

}
