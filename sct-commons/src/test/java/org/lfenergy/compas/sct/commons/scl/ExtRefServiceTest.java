/*
 * // SPDX-FileCopyrightText: 2023 RTE FRANCE
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.lfenergy.compas.sct.commons.scl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.createExtRefExample;


class ExtRefServiceTest {

    ExtRefService extRefService;

    @BeforeEach
    void setUp() {
        extRefService = new ExtRefService();
    }

    @Test
    void getExtRefs_should_return_extRefs() {
        //Given
        TLDevice tlDevice = new TLDevice();
        tlDevice.setLN0(new LN0());
        TInputs tInputs = new TInputs();
        tlDevice.getLN0().setInputs(tInputs);
        TExtRef tExtRef1 = new TExtRef();
        TExtRef tExtRef2 = new TExtRef();
        tInputs.getExtRef().add(tExtRef1);
        tInputs.getExtRef().add(tExtRef2);
        //When
        Stream<TExtRef> result = extRefService.getExtRefs(tlDevice);
        //Then
        assertThat(result).hasSize(2);
    }

    @ParameterizedTest
    @MethodSource("provideLDevices")
    void getExtRefs_should_return_empty_stream(TLDevice tlDevice) {
        //Given : parameters
        //When
        Stream<TExtRef> result = extRefService.getExtRefs(tlDevice);
        //Then
        assertThat(result).isEmpty();
    }

    private static Stream<Arguments> provideLDevices() {
        TLDevice tlDeviceWithoutLn0 = new TLDevice();
        tlDeviceWithoutLn0.setLN0(new LN0());
        return Stream.of(
                Arguments.of(named("LDevice without LN0 should return empty stream", tlDeviceWithoutLn0)),
                Arguments.of(named("LDevice with empty Inputs should return empty stream", new TLDevice()))
        );
    }

    @Test
    void clearExtRefBinding_should_remove_binding() {
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

    @ParameterizedTest
    @MethodSource("provideExtRefsFedBySameControlBlock")
    void isExtRefFeedBySameControlBlock_should_return_true(TExtRef tExtRef1, TExtRef tExtRef2) {
        // Given : parameter
        // When
        boolean result = extRefService.isExtRefFeedBySameControlBlock(tExtRef1, tExtRef2);
        // Then
        assertThat(result).isTrue();
    }

    private static Stream<Arguments> provideExtRefsFedBySameControlBlock() {
        TExtRef tExtRefLnClass = createExtRefExample("CB_1", TServiceType.GOOSE);
        tExtRefLnClass.getSrcLNClass().add(TLLN0Enum.LLN_0.value());

        return Stream.of(
                Arguments.of(createExtRefExample("CB_1", TServiceType.GOOSE), createExtRefExample("CB_1", TServiceType.GOOSE)),
                Arguments.of(tExtRefLnClass, createExtRefExample("CB_1", TServiceType.GOOSE)),
                Arguments.of(createExtRefExample("CB_1", TServiceType.GOOSE), tExtRefLnClass)
        );
    }

    @ParameterizedTest
    @MethodSource("provideExtRefsToCompare")
    void isExtRefFeedBySameControlBlock_should_return_false(TExtRef tExtRef1, TExtRef tExtRef2) {
        // Given : parameters
        // When
        boolean result = extRefService.isExtRefFeedBySameControlBlock(tExtRef1, tExtRef2);
        // Then
        assertThat(result).isFalse();
    }

    private static Stream<Arguments> provideExtRefsToCompare() {
        TExtRef tExtRefLnClass = createExtRefExample("CB_1", TServiceType.GOOSE);
        tExtRefLnClass.getSrcLNClass().add("XXX");
        TExtRef tExtRefIedName = createExtRefExample("CB_1", TServiceType.GOOSE);
        tExtRefIedName.setIedName("IED_XXX");
        TExtRef tExtRefLdInst = createExtRefExample("CB_1", TServiceType.GOOSE);
        tExtRefLdInst.setSrcLDInst("LD_XXX");
        TExtRef tExtRefLnInst = createExtRefExample("CB_1", TServiceType.GOOSE);
        tExtRefLnInst.setSrcLNInst("X");
        TExtRef tExtRefPrefix = createExtRefExample("CB_1", TServiceType.GOOSE);
        tExtRefPrefix.setSrcPrefix("X");

        return Stream.of(
                Arguments.of(named("ExtRef is not fed by same CB when different ServiceType", createExtRefExample("CB_1", TServiceType.GOOSE)),
                        createExtRefExample("CB_1", TServiceType.SMV)),
                Arguments.of(named("ExtRef is not fed by same CB when different SrcCBName", createExtRefExample("CB_1", TServiceType.GOOSE)),
                        createExtRefExample("CB_2", TServiceType.GOOSE)),
                Arguments.of(named("ExtRef is not fed by same CB when different SrcLnClass", createExtRefExample("CB_1", TServiceType.GOOSE)),
                        tExtRefLnClass),
                Arguments.of(named("ExtRef is not fed by same CB when different IedName", createExtRefExample("CB_1", TServiceType.GOOSE)),
                        tExtRefIedName),
                Arguments.of(named("ExtRef is not fed by same CB when different SrcLdInst", createExtRefExample("CB_1", TServiceType.GOOSE)),
                        tExtRefLdInst),
                Arguments.of(named("ExtRef is not fed by same CB when different SrcLnInst", createExtRefExample("CB_1", TServiceType.GOOSE)),
                        tExtRefLnInst),
                Arguments.of(named("ExtRef is not fed by same CB when different SrcPrefix", createExtRefExample("CB_1", TServiceType.GOOSE)),
                        tExtRefPrefix)
        );
    }

    @Test
    void filterDuplicatedExtRefs_should_remove_duplicated_extrefs() {
        // Given
        TExtRef tExtRefLnClass = createExtRefExample("CB_Name1", TServiceType.GOOSE);
        tExtRefLnClass.getSrcLNClass().add(TLLN0Enum.LLN_0.value());
        TExtRef tExtRef = createExtRefExample("CB_Name1", TServiceType.GOOSE);
        List<TExtRef> tExtRefList = List.of(tExtRef, tExtRefLnClass, createExtRefExample("CB", TServiceType.GOOSE),
                createExtRefExample("CB", TServiceType.GOOSE));
        // When
        List<TExtRef> result = extRefService.filterDuplicatedExtRefs(tExtRefList);
        // Then
        assertThat(result).hasSizeLessThan(tExtRefList.size())
                .hasSize(2);
    }

    @Test
    void filterDuplicatedExtRefs_should_not_remove_not_duplicated_extrefs() {
        // Given
        TExtRef tExtRefIedName = createExtRefExample("CB_1", TServiceType.GOOSE);
        tExtRefIedName.setIedName("IED_XXX");
        TExtRef tExtRefLdInst = createExtRefExample("CB_1", TServiceType.GOOSE);
        tExtRefLdInst.setSrcLDInst("LD_XXX");
        TExtRef tExtRefLnInst = createExtRefExample("CB_1", TServiceType.GOOSE);
        tExtRefLnInst.setSrcLNInst("X");
        TExtRef tExtRefPrefix = createExtRefExample("CB_1", TServiceType.GOOSE);
        tExtRefPrefix.setSrcPrefix("X");
        List<TExtRef> tExtRefList = List.of(tExtRefIedName, tExtRefLdInst, tExtRefLnInst, tExtRefPrefix,
                createExtRefExample("CB_1", TServiceType.GOOSE), createExtRefExample("CB_1", TServiceType.SMV));
        // When
        List<TExtRef> result = extRefService.filterDuplicatedExtRefs(tExtRefList);
        // Then
        assertThat(result).hasSameSizeAs(tExtRefList)
                .hasSize(6);
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

}
