// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TDO;
import org.lfenergy.compas.scl2007b4.model.TLNodeType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DOAdapterTest {

    @Test
    void elementXPath_should_return_expected_xpath_value() {
        // Given
        LNodeTypeAdapter lNodeAdapter = mock(LNodeTypeAdapter.class);
        TLNodeType tlNodeType = mock(TLNodeType.class);
        when(lNodeAdapter.getParentAdapter()).thenReturn(null);
        when(lNodeAdapter.getCurrentElem()).thenReturn(tlNodeType);
        TDO tdo = new TDO();
        tdo.setName("doName");
        when(tlNodeType.getDO()).thenReturn(List.of(tdo));
        DOAdapter doAdapter = new DOAdapter(lNodeAdapter, tdo);
        // When
        String result = doAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("DO[@name=\"doName\" and not(@type)]");
    }
}