// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.ReflectionSupport;
import org.lfenergy.compas.scl2007b4.model.*;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SclConstructorHelperTest {

    @Test
    void constructor_should_throw_exception() {
        // Given
        Class<SclConstructorHelper> testedClass = SclConstructorHelper.class;
        // When & Then
        assertThatThrownBy(() -> ReflectionSupport.newInstance(testedClass))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void newP_should_create_new_instance_with_given_parameters() {
        //Given
        String pType = "pType";
        String pValue = "pValue";
        //When
        TP tp = SclConstructorHelper.newP(pType, pValue);
        //Then
        assertThat(tp).extracting(TP::getType, TP::getValue)
            .containsExactly(pType, pValue);
    }

    @Test
    void newDurationInMilliSec_should_create_new_instance_with_given_parameters() {
        //Given
        BigDecimal value = new BigDecimal(5);
        String unit = "unit";
        String multiplier = "multiplier";
        //When
        TDurationInMilliSec durationInMilliSec = SclConstructorHelper.newDurationInMilliSec(value, unit, multiplier);
        //Then
        assertThat(durationInMilliSec).extracting(TDurationInMilliSec::getValue, TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier)
            .containsExactly(value, unit, multiplier);
    }

    @Test
    void newDurationInMilliSec_with_value_only_should_create_new_instance_with_given_value_and_default_unit_and_multiplier() {
        //Given
        long value = 5L;
        //When
        TDurationInMilliSec durationInMilliSec = SclConstructorHelper.newDurationInMilliSec(value);
        //Then
        assertThat(durationInMilliSec).extracting(TDurationInMilliSec::getValue, TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier)
                .containsExactly(new BigDecimal(value), "s", "m");
    }

    @Test
    void newDurationInMilliSec_should_clone_given_instance() {
        //Given
        BigDecimal value = new BigDecimal(5);
        String unit = "unit";
        String multiplier = "multiplier";
        TDurationInMilliSec input = SclConstructorHelper.newDurationInMilliSec(value, unit, multiplier);
        //When
        TDurationInMilliSec durationInMilliSec = SclConstructorHelper.newDurationInMilliSec(input);
        //Then
        assertThat(durationInMilliSec).extracting(TDurationInMilliSec::getValue, TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier)
                .containsExactly(value, unit, multiplier);
        assertThat(durationInMilliSec).isNotSameAs(input);
    }

    @Test
    void newAddress_should_create_new_instance_with_given_parameters() {
        //Given
        List<TP> listOfP = List.of(
                SclConstructorHelper.newP("type1", "value1"),
                SclConstructorHelper.newP("type2", "value2"));
        //When
        TAddress tAddress = SclConstructorHelper.newAddress(listOfP);
        //Then
         assertThat(tAddress.getP())
             .containsExactlyInAnyOrder(listOfP.toArray(new TP[0]));
    }

    @Test
    void newConnectedAp_should_create_new_instance_with_given_parameters() {
        //Given
        String iedName = "iedName";
        String apName = "apName";
        //When
        TConnectedAP tConnectedAP = SclConstructorHelper.newConnectedAp(iedName, apName);
        //Then
        assertThat(tConnectedAP).extracting(TConnectedAP::getIedName, TConnectedAP::getApName)
            .containsExactly(iedName, apName);
    }

    @Test
    void newVal_should_create_new_instance_with_no_sGroup() {
        //Given
        String value = "value";
        //When
        TVal tVal = SclConstructorHelper.newVal(value);
        //Then
        assertThat(tVal).extracting(TVal::getValue, TVal::isSetSGroup)
            .containsExactly(value, false);
    }

    @Test
    void newVal_should_create_new_instance_with_sGroup() {
        //Given
        String value = "value";
        long sGroup = 1;
        //When
        TVal tVal = SclConstructorHelper.newVal(value, sGroup);
        //Then
        assertThat(tVal).extracting(TVal::getValue, TVal::getSGroup)
                .containsExactly(value, sGroup);
    }

    @Test
    void newFcda_should_create_new_instance_with_given_parameters() {
        //Given
        String ldinst = "ldinst";
        String lnClass = "lnClass";
        String lnInst = "lnInst";
        String prefix = "prefix";
        String doName = "doName";
        String daName = "daName";
        TFCEnum fc = TFCEnum.ST;
        //When
        TFCDA tfcda = SclConstructorHelper.newFcda(ldinst, lnClass, lnInst, prefix, doName, daName, fc);
        //Then
        assertThat(tfcda).extracting(TFCDA::getLdInst, TFCDA::getLnClass, TFCDA::getLnInst, TFCDA::getPrefix, TFCDA::getDoName, TFCDA::getDaName, TFCDA::getFc)
                .containsExactly(ldinst, List.of(lnClass), lnInst, prefix, doName, daName, fc);
    }

    @Test
    void newFcda_should_when_blank_lnClass_should_not_set_lnClass() {
        //Given
        String ldinst = "ldinst";
        String lnClass = "";
        String lnInst = "lnInst";
        String prefix = "prefix";
        String doName = "doName";
        String daName = "daName";
        TFCEnum fc = TFCEnum.ST;
        //When
        TFCDA tfcda = SclConstructorHelper.newFcda(ldinst, lnClass, lnInst, prefix, doName, daName, fc);
        //Then
        assertThat(tfcda.isSetLnClass()).isFalse();
    }
}
