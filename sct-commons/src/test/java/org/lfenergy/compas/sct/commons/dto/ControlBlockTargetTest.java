// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TClientLN;

import java.util.List;

import static org.lfenergy.compas.scl2007b4.model.TControlWithIEDName.IEDName;

class ControlBlockTargetTest {

    private static final String AP_REF = "AP_REF";
    private static final String IED_NAME = "IED_NAME";
    private static final String LD_INST = "LD_INST";
    private static final String LN_INST = "LN_INST";
    private static final String LN_CLASS = "LN_CLASS";
    private static final String PREFIX = "PREFIX";
    private static final String DESC = "DESC";

    @Test
    void constructor_without_desc_parameter_should_set_desc_to_null() {
        //Given : constructor parameters
        //When
        ControlBlockTarget controlBlockTarget = new ControlBlockTarget(
            AP_REF, IED_NAME, LD_INST, LN_INST, LN_CLASS, PREFIX);
        //Then
        Assertions.assertThat(controlBlockTarget).extracting(
                ControlBlockTarget::apRef, ControlBlockTarget::iedName, ControlBlockTarget::ldInst,
                ControlBlockTarget::lnInst, ControlBlockTarget::lnClass, ControlBlockTarget::prefix, ControlBlockTarget::desc)
            .containsExactly(AP_REF, IED_NAME, LD_INST, LN_INST, LN_CLASS, PREFIX, null);
    }

    @Test
    void toTClientLn_should_map_object_to_TClientLN() {
        //Given
        ControlBlockTarget controlBlockTarget = createControlBlockTarget();
        //When
        TClientLN tClientLN = controlBlockTarget.toTClientLn();
        //Then
        Assertions.assertThat(tClientLN).extracting(TClientLN::getApRef, TClientLN::getIedName, TClientLN::getLdInst,
                TClientLN::getLnInst, TClientLN::getLnClass, TClientLN::getPrefix, TClientLN::getDesc)
            .containsExactly(AP_REF, IED_NAME, LD_INST, LN_INST, List.of(LN_CLASS), PREFIX, DESC);
    }

    @Test
    void toTClientLn_should_return_empty_lnInst_when_object_lnInst_is_null() {
        //Given
        ControlBlockTarget controlBlockTarget = new ControlBlockTarget(
            AP_REF, IED_NAME, LD_INST, null, LN_CLASS, PREFIX, DESC);
        //When
        TClientLN tClientLN = controlBlockTarget.toTClientLn();
        //Then
        Assertions.assertThat(tClientLN.getLnInst()).isEmpty();
    }

    @Test
    void toIedName_should_map_object_to_IEDName() {
        //Given
        ControlBlockTarget controlBlockTarget = createControlBlockTarget();
        //When
        IEDName iedName = controlBlockTarget.toIedName();
        //Then
        Assertions.assertThat(iedName).extracting(
                IEDName::getApRef, IEDName::getValue, IEDName::getLdInst,
                IEDName::getLnInst, IEDName::getLnClass, IEDName::getPrefix)
            .containsExactly(AP_REF, IED_NAME, LD_INST, LN_INST, List.of(LN_CLASS), PREFIX);
    }

    @Test
    void toIedName_should_return_null_lnInst_when_object_lnInst_is_empty() {
        //Given
        ControlBlockTarget controlBlockTarget = new ControlBlockTarget(
            AP_REF, IED_NAME, LD_INST, "", LN_CLASS, PREFIX, DESC);
        //When
        IEDName iedName = controlBlockTarget.toIedName();
        //Then
        Assertions.assertThat(iedName.getLnInst()).isNull();
    }

    @Test
    void from_TClientLN_should_create_ControlBlockTarget() {
        //Given
        TClientLN tClientLN = createTClientLN();
        //When
        ControlBlockTarget controlBlockTarget = ControlBlockTarget.from(tClientLN);
        //Then
        Assertions.assertThat(controlBlockTarget).extracting(
                ControlBlockTarget::apRef, ControlBlockTarget::iedName, ControlBlockTarget::ldInst,
                ControlBlockTarget::lnInst, ControlBlockTarget::lnClass, ControlBlockTarget::prefix, ControlBlockTarget::desc)
            .containsExactly(AP_REF, IED_NAME, LD_INST, LN_INST, LN_CLASS, PREFIX, DESC);
    }

    @Test
    void from_IEDName_should_create_ControlBlockTarget() {
        //Given
        IEDName iedName = createIedName();
        //When
        ControlBlockTarget controlBlockTarget = ControlBlockTarget.from(iedName);
        //Then
        Assertions.assertThat(controlBlockTarget).extracting(
                ControlBlockTarget::apRef, ControlBlockTarget::iedName, ControlBlockTarget::ldInst,
                ControlBlockTarget::lnInst, ControlBlockTarget::lnClass, ControlBlockTarget::prefix, ControlBlockTarget::desc)
            .containsExactly(AP_REF, IED_NAME, LD_INST, LN_INST, LN_CLASS, PREFIX, null);
    }

    @Test
    void equalsTClientLn_should_return_true() {
        //Given
        TClientLN tClientLN = createTClientLN();
        ControlBlockTarget controlBlockTarget = createControlBlockTarget();
        //When
        boolean result = controlBlockTarget.equalsTClientLn(tClientLN);
        //Then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    void equalsTClientLn_when_lnInst_is_blank_should_return_true() {
        //Given
        TClientLN tClientLN = createTClientLN();
        tClientLN.setLnInst("");
        ControlBlockTarget controlBlockTarget = new ControlBlockTarget(
            AP_REF, IED_NAME, LD_INST, null, LN_CLASS, PREFIX, DESC);
        //When
        boolean result = controlBlockTarget.equalsTClientLn(tClientLN);
        //Then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    void equalsTClientLn_should_return_false() {
        //Given
        TClientLN tClientLN = createTClientLN();
        tClientLN.setDesc("changed desc");
        ControlBlockTarget controlBlockTarget = createControlBlockTarget();
        //When
        boolean result = controlBlockTarget.equalsTClientLn(tClientLN);
        //Then
        Assertions.assertThat(result).isFalse();
    }

    @Test
    void equalsIedName_should_return_true() {
        //Given
        IEDName iedName = createIedName();
        ControlBlockTarget controlBlockTarget = createControlBlockTarget();
        //When
        boolean result = controlBlockTarget.equalsIedName(iedName);
        //Then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    void equalsIedName_when_lnInst_is_blank_should_return_true() {
        //Given
        IEDName iedName = createIedName();
        iedName.setLnInst(null);
        ControlBlockTarget controlBlockTarget = new ControlBlockTarget(
            AP_REF, IED_NAME, LD_INST, "", LN_CLASS, PREFIX, DESC);
        //When
        boolean result = controlBlockTarget.equalsIedName(iedName);
        //Then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    void equalsIedName_should_return_false() {
        //Given
        IEDName iedName = createIedName();
        iedName.setValue("changed value");
        ControlBlockTarget controlBlockTarget = createControlBlockTarget();
        //When
        boolean result = controlBlockTarget.equalsIedName(iedName);
        //Then
        Assertions.assertThat(result).isFalse();
    }

    private static ControlBlockTarget createControlBlockTarget() {
        return new ControlBlockTarget(
            AP_REF, IED_NAME, LD_INST, LN_INST, LN_CLASS, PREFIX, DESC);
    }

    private static TClientLN createTClientLN() {
        TClientLN tClientLN = new TClientLN();
        tClientLN.setApRef(AP_REF);
        tClientLN.setIedName(IED_NAME);
        tClientLN.setLdInst(LD_INST);
        tClientLN.setLnInst(LN_INST);
        tClientLN.getLnClass().add(LN_CLASS);
        tClientLN.setPrefix(PREFIX);
        tClientLN.setDesc(DESC);
        return tClientLN;
    }

    private static IEDName createIedName() {
        IEDName iedName = new IEDName();
        iedName.setApRef(AP_REF);
        iedName.setValue(IED_NAME);
        iedName.setLdInst(LD_INST);
        iedName.setLnInst(LN_INST);
        iedName.getLnClass().add(LN_CLASS);
        iedName.setPrefix(PREFIX);
        return iedName;
    }
}
