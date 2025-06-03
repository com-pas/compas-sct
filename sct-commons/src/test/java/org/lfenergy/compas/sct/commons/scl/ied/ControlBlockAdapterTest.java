// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.scl.ln.LN0Adapter;
import org.lfenergy.compas.sct.commons.scl.ln.LNAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.findLn;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.findLn0;

class ControlBlockAdapterTest {

    @Test
    void getName_should_return_name(){
        // Given
        TGSEControl tgseControl = new TGSEControl();
        tgseControl.setName("cbName");
        ControlBlockAdapter controlBlockAdapter = new ControlBlockAdapter(null, tgseControl);
        // When
        String result = controlBlockAdapter.getName();
        // Then
        assertThat(result).isEqualTo("cbName");
    }

    @Test
    @Tag("issue-321")
    void addTargetIfNotExists_should_add_target(){
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-ln-adapter/scd_with_ln.xml");
        LN0Adapter ln0 = findLn0(scd, "IED_NAME1", "LD_INST11");
        // When
        ln0.createDataSetIfNotExists("datSet", ControlBlockEnum.GSE);
        // When
        ControlBlockAdapter controlBlockAdapter = ln0.createControlBlockIfNotExists("cbName", "cbId", "datSet", ControlBlockEnum.GSE);
        LNAdapter targetLn = findLn(scd, "IED_NAME2", "LD_INST21", "ANCR", "1", "prefix");
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

    @Test
    void addTargetIfNotExists_should_create_rptEnabled_of_reportControl() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-ln-adapter/scd_with_ln.xml");
        LN0Adapter ln0 = findLn0(scd, "IED_NAME1", "LD_INST11");
        TDataSet newDataSet = new TDataSet();
        newDataSet.setName("datSet");
        ln0.getCurrentElem().getDataSet().add(newDataSet);
        TReportControl tReportControl = new TReportControl();
        tReportControl.setRptID("rptId");
        tReportControl.setName("cbName");
        tReportControl.setDatSet("datSet");
        ln0.getCurrentElem().getReportControl().add(tReportControl);
        ControlBlockAdapter controlBlockAdapter = new ControlBlockAdapter(ln0, tReportControl);
        LNAdapter targetLn = findLn(scd, "IED_NAME2", "LD_INST21", "ANCR", "1", "prefix");
        // When
        controlBlockAdapter.addTargetIfNotExists(targetLn);
        // Then
        TControl tControl = controlBlockAdapter.getCurrentElem();
        assertThat(tControl).isInstanceOf(TReportControl.class);
        assertThat(((TReportControl) tControl).getRptEnabled())
                .extracting(TRptEnabled::getMax)
                .isEqualTo(4L);
        assertThat(((TReportControl) tControl).getRptEnabled().getClientLN())
                .singleElement()
                .extracting(TClientLN::getApRef, TClientLN::getIedName, TClientLN::getLdInst, TClientLN::getLnClass, TClientLN::getLnInst, TClientLN::getPrefix, TClientLN::getDesc)
                .containsExactly("AP_NAME", "IED_NAME2", "LD_INST21", List.of("ANCR"), "1", "prefix", "");
    }

}
