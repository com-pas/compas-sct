// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.sstation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TBay;
import org.lfenergy.compas.scl2007b4.model.TCompasICDHeader;
import org.lfenergy.compas.scl2007b4.model.TFunction;
import org.lfenergy.compas.scl2007b4.model.TLNode;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.PrivateService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FunctionAdapterTest {

    private FunctionAdapter functionAdapter;

    @BeforeEach
    void setUp() {
        TBay tBay = new TBay();
        BayAdapter bayAdapter = new BayAdapter(null, tBay);
        TFunction tFunction = new TFunction();
        tFunction.setName("functionName");
        tBay.getFunction().add(tFunction);
        functionAdapter = new FunctionAdapter(bayAdapter, tFunction);
    }

    @Test
    void amChildElementRef_should_succeed() {
        // Given : setUp
        // When
        boolean result = functionAdapter.amChildElementRef();
        // Then
        assertThat(result).isTrue();
    }

    @Test
    void amChildElementRef_when_parent_does_not_contain_function_should_be_false() {
        // Given : setUp
        functionAdapter.getParentAdapter().getCurrentElem().getFunction().clear();
        // When
        boolean result = functionAdapter.amChildElementRef();
        // Then
        assertThat(result).isFalse();
    }

    @Test
    void elementXPath() {
        // Given : setUp
        // When
        String result = functionAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("Function[@name=\"functionName\"]");
    }

    @Test
    void updateLNodeIedNames_when_one_private_should_succeed() {
        // Given
        TLNode tlNode = new TLNode();
        TCompasICDHeader tCompasICDHeader = new TCompasICDHeader();
        tCompasICDHeader.setIEDName("iedName1");
        tlNode.getPrivate().add(PrivateService.createPrivate(tCompasICDHeader));
        functionAdapter.getCurrentElem().getLNode().add(tlNode);
        // When
        functionAdapter.updateLNodeIedNames();
        // Then
        assertThat(functionAdapter.getCurrentElem().getLNode())
            .hasSize(1)
            .map(TLNode::getIedName).containsExactly("iedName1");
    }

    @Test
    void updateLNodeIedNames_when_multiples_privates_should_succeed() {
        // Given
        TLNode tlNode = new TLNode();
        TCompasICDHeader tCompasICDHeader1 = new TCompasICDHeader();
        tCompasICDHeader1.setIEDName("iedName1");
        TCompasICDHeader tCompasICDHeader2 = new TCompasICDHeader();
        tCompasICDHeader2.setIEDName("iedName2");
        TCompasICDHeader tCompasICDHeader3 = new TCompasICDHeader();
        tCompasICDHeader3.setIEDName("iedName3");
        tlNode.getPrivate().add(PrivateService.createPrivate(tCompasICDHeader1));
        tlNode.getPrivate().add(PrivateService.createPrivate(tCompasICDHeader2));
        tlNode.getPrivate().add(PrivateService.createPrivate(tCompasICDHeader3));
        functionAdapter.getCurrentElem().getLNode().add(tlNode);
        // When
        functionAdapter.updateLNodeIedNames();
        // Then
        assertThat(functionAdapter.getCurrentElem().getLNode())
            .hasSize(3)
            .map(TLNode::getIedName)
            .containsExactly("iedName1", "iedName2", "iedName3");
    }

    @Test
    void updateLNodeIedNames_when_no_private_should_throw_exception() {
        // Given
        TLNode tlNode = new TLNode();
        functionAdapter.getCurrentElem().getLNode().add(tlNode);
        // When & Then
        assertThatThrownBy(() -> functionAdapter.updateLNodeIedNames()).isInstanceOf(ScdException.class);
    }

}
