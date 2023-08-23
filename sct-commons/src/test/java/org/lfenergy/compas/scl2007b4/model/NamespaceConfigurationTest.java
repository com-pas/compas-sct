// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.scl2007b4.model;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.sct.commons.util.PrivateUtils;
import org.lfenergy.compas.sct.commons.testhelpers.MarshallerWrapper;

import static org.assertj.core.api.Assertions.assertThat;

class NamespaceConfigurationTest {

    @Test
    void marshalling_SCL_and_Compas_Privates_should_set_correct_prefix() {
        // Given
        SCL scl = createValidScl();
        TPrivate aCompasPrivate = PrivateUtils.createPrivate(TCompasSclFileType.SCD);
        scl.getPrivate().add(aCompasPrivate);
        // When
        String result = MarshallerWrapper.marshall(scl);
        // Then
        assertThat(result)
                .containsPattern("(?s)<SCL[^>]* xmlns=\"http://www\\.iec\\.ch/61850/2003/SCL\"")
                .containsPattern("(?s)<SCL[^>]* xmlns:compas=\"https://www\\.lfenergy\\.org/compas/extension/v1\"");
    }

    private static SCL createValidScl() {
        SCL scl = new SCL();
        scl.setVersion("2007");
        scl.setRevision("B");
        scl.setRelease((short) 4);
        THeader tHeader = new THeader();
        tHeader.setId("headerId");
        scl.setHeader(tHeader);
        return scl;
    }
}
