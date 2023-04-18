// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TFCDA;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;

import java.util.List;

public final class HmiService {

    private HmiService() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Create the DataSet and ReportControl Blocks for the HMI with the given FCDAs.
     *
     * @param fcdas List of FCDA for which we must create the DataSet and ReportControl Blocks
     */
    public static void createAllHmiReportControlBlocks(SCL scd, List<TFCDA> fcdas) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        sclRootAdapter.streamIEDAdapters()
                .flatMap(IEDAdapter::streamLDeviceAdapters)
                .forEach(lDeviceAdapter -> lDeviceAdapter.createHmiReportControlBlocks(fcdas));
    }

}
