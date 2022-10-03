// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TDO;
import org.lfenergy.compas.scl2007b4.model.TLNodeType;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DOAdapterTest {

    @Test
    void elementXPath() {
        // Given
        LNodeTypeAdapter lNodeAdapter = Mockito.mock(LNodeTypeAdapter.class);
        TLNodeType tlNodeType = Mockito.mock(TLNodeType.class);
        Mockito.when(lNodeAdapter.getParentAdapter()).thenReturn(null);
        Mockito.when(lNodeAdapter.getCurrentElem()).thenReturn(tlNodeType);

        TDO tdo = new TDO();
        tdo.setName("doName");
        Mockito.when(tlNodeType.getDO()).thenReturn(List.of(tdo));
        DOAdapter doAdapter = new DOAdapter(lNodeAdapter, tdo);
        // When
        String result = doAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("DO[@name=\"doName\" and not(@type)]");
    }
}