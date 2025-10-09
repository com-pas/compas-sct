// SPDX-FileCopyrightText: 2025 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class LNodeServiceTest {

    private LNodeService lNodeService;

    @BeforeEach
    void setUp() {
        lNodeService = new LNodeService();
    }

    @Test
    void getLNodes_should_return_all_lnodes() {
        //Given
        TBay tBay = createBay();
        //When
        Stream<TLNode> lNodes = lNodeService.getLNodes(tBay);
        //Then
        assertThat(lNodes)
                .extracting(TLNode::getLdInst)
                .containsExactlyInAnyOrder("LDInst1", "LDInst2", "LDInst3");
    }

    @Test
    void getFilteredLNodes_should_return_lnodes() {
        //Given
        TBay tBay = createBay();
        //When
        Stream<TLNode> lNodes = lNodeService.getFilteredLNodes(tBay, tLNode -> tLNode.getLdInst().equals("LDInst2") || tLNode.getLdInst().equals("LDInst3"));
        //Then
        assertThat(lNodes)
                .extracting(TLNode::getLdInst)
                .containsExactlyInAnyOrder("LDInst2", "LDInst3");
    }

    @Test
    void findLNode_should_return_lnode() {
        //Given
        TBay tBay = createBay();
        //When
        Optional<TLNode> lNodes = lNodeService.findLNode(tBay, tLNode -> tLNode.getLdInst().equals("LDInst2"));
        //Then
        assertThat(lNodes)
                .map(TLNode::getLdInst)
                .hasValue("LDInst2");
    }

    @Test
    void matchesLnode_should_match_lnode() {
        //Given
        TLNode lNode = createLNode(1);
        //When
        boolean matches = lNodeService.matchesLnode(lNode, "IED1", "LDInst1", "LNClass1", "LNInst1", "Prefix1");
        //Then
        assertThat(matches).isTrue();
    }

    @Test
    void matchesLnode_on_LN_should_match_lnode() {
        //Given
        TLNode lNode = createLNode(1);
        TLN tln = new TLN();
        tln.getLnClass().add("LNClass1");
        tln.setInst("LNInst1");
        tln.setPrefix("Prefix1");
        //When
        boolean matches = lNodeService.matchesLnode(lNode, "IED1", "LDInst1", tln);
        //Then
        assertThat(matches).isTrue();
    }

    @Test
    void matchesLnode_on_LN0_should_match_lnode() {
        //Given
        TLNode lNode = new TLNode();
        lNode.setIedName("IED1");
        lNode.setLdInst("LDInst1");
        lNode.getLnClass().add("LLN0");
        LN0 ln0 = new LN0();
        //When
        boolean matches = lNodeService.matchesLnode(lNode, "IED1", "LDInst1", ln0);
        //Then
        assertThat(matches).isTrue();
    }

    private static TBay createBay() {
        TBay tBay = new TBay();
        tBay.setName("BayA");
        TFunction tFunction1 = new TFunction();
        tFunction1.getLNode().addAll(List.of(createLNode(1), createLNode(2)));
        TFunction tFunction2 = new TFunction();
        tFunction2.getLNode().add(createLNode(3));
        tBay.getFunction().addAll(List.of(tFunction1, tFunction2));
        return tBay;
    }

    private static TLNode createLNode(int index) {
        TLNode tLNode = new TLNode();
        tLNode.setIedName("IED" + index);
        tLNode.setLdInst("LDInst" + index);
        tLNode.getLnClass().add("LNClass" + index);
        tLNode.setLnInst("LNInst" + index);
        tLNode.setPrefix("Prefix" + index);
        return tLNode;
    }
}
