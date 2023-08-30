// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TFCDA;
import org.lfenergy.compas.sct.commons.api.HmiEditor;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;

import java.util.List;

public class HmiService implements HmiEditor {

    @Override
    public void createAllHmiReportControlBlocks(SCL scd, List<TFCDA> fcdas) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        sclRootAdapter.streamIEDAdapters()
                .flatMap(IEDAdapter::streamLDeviceAdapters)
                .forEach(lDeviceAdapter -> lDeviceAdapter.createHmiReportControlBlocks(fcdas));
    }

}
