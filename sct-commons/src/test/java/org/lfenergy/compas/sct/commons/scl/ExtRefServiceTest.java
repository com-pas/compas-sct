/*
 * // SPDX-FileCopyrightText: 2023 RTE FRANCE
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.lfenergy.compas.sct.commons.scl;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.util.PrivateEnum;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


class ExtRefServiceTest {

    private static final ObjectFactory objectFactory = new ObjectFactory();
    ExtRefService extRefService = new ExtRefService();

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideTAnyLns")
    void getExtRefs(String testCase, TInputs tInputs, int size) {
        //Given
        //When
        //Then
        assertThat(extRefService.getExtRefs(tInputs)).hasSize(size);
    }

    private static Stream<Arguments> provideTAnyLns() {
        TInputs tInputsEmpty = new TInputs();
        TInputs tInputs = new TInputs();
        TExtRef tExtRef1 = new TExtRef();
        TExtRef tExtRef2 = new TExtRef();
        tInputs.getExtRef().add(tExtRef1);
        tInputs.getExtRef().add(tExtRef2);

        return Stream.of(
                Arguments.of("Ln without Inputs node should return empty stream", null, 0),
                Arguments.of("Ln with empty Inputs should return empty stream", tInputsEmpty, 0),
                Arguments.of("Ln0 with Inputs node should return stream 2 extrefs", tInputs, 2));
    }

    @Test
    void getMatchingCompasFlows() {
        //Given
        TInputs tInputs = new TInputs();
        TExtRef tExtRef1 = createExtRef("Desc_1", "IED_Name_1", "LD_INST_1");
        tInputs.getExtRef().add(tExtRef1);
        TCompasFlow tCompasFlow1 = createCompasFlow("Desc_1", "IED_Name_1", "LD_INST_1");
        TCompasFlow tCompasFlow2 = createCompasFlow("Desc_2", "IED_Name_2", "LD_INST_2");
        tInputs.getPrivate().add(createPrivateCompasFlow(List.of(tCompasFlow1, tCompasFlow2)));
        //When
        Stream<TCompasFlow> matchingCompasFlows = extRefService.getMatchingCompasFlows(tInputs, tExtRef1);
        //Then
        assertThat(matchingCompasFlows).hasSize(1)
                .map(TCompasFlow::getDataStreamKey, TCompasFlow::getExtRefiedName)
                .containsExactly(Tuple.tuple("Desc_1", "IED_Name_1"));
    }

    @Test
    void getMatchingTextRef() {
        //Given
        TLN tln = new TLN();
        TInputs tInputs = new TInputs();
        TExtRef tExtRef1 = createExtRef("Desc_1", "IED_Name_1", "LD_INST_1");
        TExtRef tExtRef2 = createExtRef("Desc_2", "IED_Name_2", "LD_INST_2");
        tInputs.getExtRef().add(tExtRef1);
        tInputs.getExtRef().add(tExtRef2);
        TCompasFlow tCompasFlow = createCompasFlow("Desc_1", "IED_Name_1", "LD_INST_1");
        tln.setInputs(tInputs);
        //When
        Stream<TExtRef> tExtRefStream = extRefService.getMatchingExtRef(tInputs, tCompasFlow);
        //Then
        assertThat(tExtRefStream).hasSize(1)
                .map(TExtRef::getIedName, TExtRef::getLdInst, TExtRef::getDesc)
                .containsExactly(Tuple.tuple("IED_Name_1", "LD_INST_1", "Desc_1"));
    }

    @Test
    void getMatchingTextRef_success_when_lnclass_null() {
        //Given
        TLN tln = new TLN();
        TInputs tInputs = new TInputs();
        TExtRef tExtRef1 = createExtRef("Desc_1", "IED_Name_1", "LD_INST_1");
        tExtRef1.getLnClass().clear();
        TExtRef tExtRef2 = createExtRef("Desc_2", "IED_Name_2", "LD_INST_2");
        tInputs.getExtRef().add(tExtRef1);
        tInputs.getExtRef().add(tExtRef2);
        TCompasFlow tCompasFlow = createCompasFlow("Desc_1", "IED_Name_1", "LD_INST_1");
        tCompasFlow.setExtReflnClass(null);
        tln.setInputs(tInputs);
        //When
        Stream<TExtRef> tExtRefStream = extRefService.getMatchingExtRef(tInputs, tCompasFlow);
        //Then
        assertThat(tExtRefStream).hasSize(1)
                .map(TExtRef::getIedName, TExtRef::getLdInst, TExtRef::getDesc)
                .containsExactly(Tuple.tuple("IED_Name_1", "LD_INST_1", "Desc_1"));
    }

    @Test
    void clearBinding() {
        //Given
        TExtRef tExtRef = createExtRef("Desc_1", "IED_Name_1", "LD_INST_1");
        //When
        extRefService.clearExtRefBinding(tExtRef);
        //Then
        assertThat(tExtRef)
                .extracting(TExtRef::getIedName, TExtRef::getLdInst, TExtRef::getLnInst)
                .containsOnlyNulls();
        assertThat(tExtRef.getDesc()).isEqualTo("Desc_1");
    }

    @Test
    void clearCompasFlowBinding() {
        //Given
        TCompasFlow compasFlow = createCompasFlow("Desc_1", "IED_Name_1", "LD_INST_1");
        //When
        extRefService.clearCompasFlowBinding(compasFlow);
        //Then
        assertThat(compasFlow)
                .extracting(TCompasFlow::getExtRefiedName, TCompasFlow::getExtRefldinst, TCompasFlow::getExtReflnClass, TCompasFlow::getExtReflnInst, TCompasFlow::getExtRefprefix)
                .containsOnlyNulls();
        assertThat(compasFlow.getDataStreamKey()).isEqualTo("Desc_1");
    }

    private static TPrivate createPrivateCompasFlow(List<TCompasFlow> compasFlows) {
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType(PrivateEnum.COMPAS_FLOW.getPrivateType());
        tPrivate.getContent().addAll(compasFlows.stream().map(objectFactory::createFlow).toList());
        return tPrivate;
    }

    private TExtRef createExtRef(String desc, String iedName, String ldInst) {
        TExtRef tExtRef1 = new TExtRef();
        tExtRef1.setDesc(desc);
        tExtRef1.setIedName(iedName);
        tExtRef1.setLdInst(ldInst);
        tExtRef1.getLnClass().add("LN");
        tExtRef1.setLnInst("1");
        return tExtRef1;
    }

    private TCompasFlow createCompasFlow(String dataStreamKey, String extRefIedName, String extRefLdInst) {
        TCompasFlow tCompasFlow = new TCompasFlow();
        tCompasFlow.setDataStreamKey(dataStreamKey);
        tCompasFlow.setExtRefiedName(extRefIedName);
        tCompasFlow.setExtRefldinst(extRefLdInst);
        tCompasFlow.setExtReflnClass("LN");
        tCompasFlow.setExtReflnInst("1");
        return tCompasFlow;
    }

}