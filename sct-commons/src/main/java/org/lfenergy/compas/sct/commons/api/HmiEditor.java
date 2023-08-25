// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.api;

import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TFCDA;

import java.util.List;

public interface HmiEditor {

    /**
     * Create the DataSet and ReportControl Blocks for the HMI with the given FCDAs.
     *
     * @param fcdas List of FCDA for which we must create the DataSet and ReportControl Blocks
     */
    void createAllHmiReportControlBlocks(SCL scd, List<TFCDA> fcdas);
}
