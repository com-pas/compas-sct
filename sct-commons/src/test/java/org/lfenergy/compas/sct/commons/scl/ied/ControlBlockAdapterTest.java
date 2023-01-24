// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TControl;
import org.lfenergy.compas.scl2007b4.model.TControlWithIEDName;
import org.lfenergy.compas.scl2007b4.model.TGSEControl;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.findLn;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.findLn0;

class ControlBlockAdapterTest {

    @Test
    void addTargetIfNotExists_should_add_target(){
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-ln-adapter/scd_with_ln.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        LN0Adapter ln0 = findLn0(sclRootAdapter, "IED_NAME1", "LD_INST11");
        ln0.createDataSetIfNotExists("datSet", ControlBlockEnum.GSE);
        ControlBlockAdapter controlBlockAdapter = ln0.createControlBlockIfNotExists("cbName", "cbId", "datSet", ControlBlockEnum.GSE);
        LNAdapter targetLn = findLn(sclRootAdapter, "IED_NAME2", "LD_INST21", "ANCR", "1", "prefix");
        // When
        controlBlockAdapter.addTargetIfNotExists(targetLn);

        // Then
        TControl tControl = controlBlockAdapter.getCurrentElem();
        assertThat(tControl).isInstanceOf(TGSEControl.class);
        assertThat(((TGSEControl) tControl).getIEDName())
            .hasSize(1)
            .first()
            .extracting(TControlWithIEDName.IEDName::getApRef, TControlWithIEDName.IEDName::getValue,
                TControlWithIEDName.IEDName::getLdInst, TControlWithIEDName.IEDName::getLnInst, TControlWithIEDName.IEDName::getLnClass,
                TControlWithIEDName.IEDName::getPrefix)
            .containsExactly("AP_NAME", "IED_NAME2", "LD_INST21", "1", List.of("ANCR"), "prefix");
    }

}
