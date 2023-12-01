// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import lombok.Getter;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.dto.FcdaForDataSetsCreation;

import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is a helper method to load FCDA from a CSV files for use with
 * {@link org.lfenergy.compas.sct.commons.ControlBlockService#createDataSetAndControlBlocks(SCL, Set)}
 * {@link org.lfenergy.compas.sct.commons.ControlBlockService#createDataSetAndControlBlocks(SCL, String, Set)}
 * {@link org.lfenergy.compas.sct.commons.ControlBlockService#createDataSetAndControlBlocks(SCL, String, String, Set)}
 * Use the getter to access the list of parsed FCDA.
 *
 * @see CsvUtils
 */
public class FcdaCsvHelper {

    @Getter
    private final Set<FcdaForDataSetsCreation> fcdaForDataSets;

    /**
     * Constructor
     * Provide the CSV files as a Reader. For example, you can create a reader like this :
     * <code>new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName), StandardCharsets.UTF_8);</code>
     *
     * @param csvSourceForDataSetAndControlBlocks a reader that provides the FCDA datas for DataSets and Control Blocks creation as CSV
     */
    public FcdaCsvHelper(Reader csvSourceForDataSetAndControlBlocks) {
        fcdaForDataSets = new HashSet<>(CsvUtils.parseRows(csvSourceForDataSetAndControlBlocks, FcdaForDataSetsCreation.class));
    }


}
