// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.ReflectionSupport;
import org.lfenergy.compas.scl2007b4.model.TAddress;
import org.lfenergy.compas.scl2007b4.model.TConnectedAP;
import org.lfenergy.compas.scl2007b4.model.TDurationInMilliSec;
import org.lfenergy.compas.scl2007b4.model.TP;

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
    void testNewDurationInMilliSec_should_create_new_instance_with_given_parameters() {
        //Given
        long value = 5L;
        //When
        TDurationInMilliSec durationInMilliSec = SclConstructorHelper.newDurationInMilliSec(value);
        //Then
        assertThat(durationInMilliSec).extracting(TDurationInMilliSec::getValue, TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier)
            .containsExactly(new BigDecimal(value), "s", "m");
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
}
