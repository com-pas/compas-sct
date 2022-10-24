// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TServiceType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ExtRefBindingInfoTest {

    @Test
    void testConstruction() {
        ExtRefBindingInfo bindingInfo = DTO.createExtRefBindingInfo_Remote();
        ExtRefBindingInfo bindingInfo_bis = DTO.createExtRefBindingInfo_Remote();
        ExtRefBindingInfo bindingInfo_ter = new ExtRefBindingInfo(DTO.createExtRef());
        ExtRefBindingInfo bindingInfo_qt = new ExtRefBindingInfo();

        assertEquals(bindingInfo, bindingInfo_ter);
        assertEquals(bindingInfo, bindingInfo_bis);
        assertNotEquals(null, bindingInfo);
        assertNotEquals(bindingInfo, bindingInfo_qt);
        bindingInfo_qt.setDaName(bindingInfo.getDaName());
        assertNotEquals(bindingInfo, bindingInfo_qt);

        bindingInfo_qt.setDoName(bindingInfo.getDoName());
        assertNotEquals(bindingInfo, bindingInfo_qt);

        bindingInfo_qt.setIedName(bindingInfo.getIedName());
        assertNotEquals(bindingInfo, bindingInfo_qt);

        bindingInfo_qt.setLdInst(bindingInfo.getLdInst());
        assertNotEquals(bindingInfo, bindingInfo_qt);

        bindingInfo_qt.setLnInst(bindingInfo.getLnInst());
        assertNotEquals(bindingInfo, bindingInfo_qt);

        bindingInfo_qt.setLnClass(bindingInfo.getLnClass());
        assertNotEquals(bindingInfo, bindingInfo_qt);

        bindingInfo_qt.setPrefix(bindingInfo.getPrefix());
        assertNotEquals(bindingInfo, bindingInfo_qt);

        bindingInfo_qt.setServiceType(bindingInfo.getServiceType());
        assertEquals(bindingInfo, bindingInfo_qt);
        assertEquals(bindingInfo.hashCode(), bindingInfo_qt.hashCode());
    }

    @Test
    void testIsValid() {
        ExtRefBindingInfo bindingInfo = DTO.createExtRefBindingInfo_Remote();
        assertTrue(bindingInfo.isValid());
        bindingInfo.setLnClass("PIOC");
        bindingInfo.setLnInst("");
        assertFalse(bindingInfo.isValid());
        bindingInfo.setDoName(new DoTypeName("do.sdo1.sdoError"));
        assertFalse(bindingInfo.isValid());
        bindingInfo.setDaName(new DaTypeName(""));
        assertFalse(bindingInfo.isValid());
        bindingInfo.setLnClass("");
        assertFalse(bindingInfo.isValid());
        bindingInfo.setLdInst("");
        assertFalse(bindingInfo.isValid());
        bindingInfo.setIedName("");
        assertFalse(bindingInfo.isValid());

    }

    @Test
    void testIsWrappedIn() {
        TExtRef extRef = DTO.createExtRef();
        ExtRefBindingInfo bindingInfo = new ExtRefBindingInfo();
        assertFalse(bindingInfo.isWrappedIn(extRef));

        bindingInfo.setIedName(extRef.getIedName());
        assertFalse(bindingInfo.isWrappedIn(extRef));

        bindingInfo.setLdInst(extRef.getLdInst());
        assertFalse(bindingInfo.isWrappedIn(extRef));

        if (!extRef.getLnClass().isEmpty()) {
            bindingInfo.setLnClass(extRef.getLnClass().get(0));
            assertFalse(bindingInfo.isWrappedIn(extRef));

            bindingInfo.setLnInst(extRef.getLnInst());
            assertFalse(bindingInfo.isWrappedIn(extRef));

            bindingInfo.setPrefix(extRef.getPrefix());
            assertFalse(bindingInfo.isWrappedIn(extRef));
        }

        bindingInfo.setServiceType(extRef.getServiceType());
        assertTrue(bindingInfo.isWrappedIn(extRef));
    }

    @Test
    void testIsNull() {
        ExtRefBindingInfo bindingInfo = new ExtRefBindingInfo();
        assertTrue(bindingInfo.isNull());

        bindingInfo.setServiceType(TServiceType.REPORT);
        assertFalse(bindingInfo.isNull());
    }

    @Test
    void compareTo_should_return_0_when_ExtRefBindingInfo_the_same() {
        // Given
        ExtRefBindingInfo bindingInfo1 = DTO.createExtRefBindingInfo_Remote();
        ExtRefBindingInfo bindingInfo2 = DTO.createExtRefBindingInfo_Remote();

        // When
        int result = bindingInfo1.compareTo(bindingInfo2);

        // Then
        assertThat(result).isZero();
    }

    @Test
    void compareTo_should_return_positive_int_when_first_ExtRefBindingInfo_is_major_in_alphabetical_order() {
        // Given
        ExtRefBindingInfo bindingInfo1 = DTO.createExtRefBindingInfo_Remote();
        ExtRefBindingInfo bindingInfo2 = DTO.createExtRefBindingInfo_Source();

        // When
        int result = bindingInfo1.compareTo(bindingInfo2);

        // Then
        assertThat(result).isPositive();
    }

    @Test
    void compareTo_should_return_negative_int_when_first_ExtRefBindingInfo_is_minor_in_alphabetical_order() {
        // Given
        ExtRefBindingInfo bindingInfo1 = DTO.createExtRefBindingInfo_Source();
        ExtRefBindingInfo bindingInfo2 = DTO.createExtRefBindingInfo_Remote();

        // When
        int result = bindingInfo1.compareTo(bindingInfo2);

        // Then
        assertThat(result).isNegative();
    }
}