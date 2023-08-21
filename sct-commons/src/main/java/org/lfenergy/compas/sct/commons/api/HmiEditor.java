// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.api;

import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TFCDA;
import org.lfenergy.compas.scl2007b4.model.TReportControl;

import java.util.List;

/**
 * Service class that will be used to manage elements related to the HMI {@link TReportControl <em>Report Control Blocks</em>}.
 *
 * @see <a href="https://github.com/com-pas/compas-sct/issues/258" target="_blank">Issue !258</a>
 */
public interface HmiEditor {

    /**
     * Create the DataSet and ReportControl Blocks for the HMI with the given FCDAs.
     *
     * @param fcdas List of FCDA for which we must create the DataSet and ReportControl Blocks
     */
    void createAllHmiReportControlBlocks(SCL scd, List<TFCDA> fcdas);
}
