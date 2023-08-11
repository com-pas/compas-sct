// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObjectReferenceTest {

    @Test
    void constructor_whenCalled_shouldFillValues() {
        // Given
        String ref = "IED_NAME_LD_NAME/PR_LN_INST.doi.sdoi.sdai.bdai.bda";
        // When
        ObjectReference objRef = new ObjectReference(ref);
        objRef.init();
        // Then
        assertThat(objRef.getLdName()).isEqualTo("IED_NAME_LD_NAME");
        assertThat(objRef.getLNodeName()).isEqualTo("PR_LN_INST");
        assertThat(objRef.getDataAttributes()).isEqualTo("doi.sdoi.sdai.bdai.bda");
    }

    @Test
    void constructor_whenCalledWithInvalidLdName_shouldReturnException() {
        // Given
        String ref0 = "IED_NAME_LD_NAMEPR_LN_INST.doi.sdoi.sdai.bdai.bda";
        // When Then
        assertThatThrownBy(() ->new ObjectReference(ref0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Malformed ObjRef : IED_NAME_LD_NAMEPR_LN_INST.doi.sdoi.sdai.bdai.bda");

    }

    @Test
    void constructor_whenCalledWithInvalidLNodeName_shouldReturnException() {
        // Given
        String ref1 = "IED_NAME_LD_NAME/PR_LN_INST";
        // When Then
        assertThatThrownBy(() ->new ObjectReference(ref1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Malformed ObjRef : IED_NAME_LD_NAME/PR_LN_INST");

    }

}