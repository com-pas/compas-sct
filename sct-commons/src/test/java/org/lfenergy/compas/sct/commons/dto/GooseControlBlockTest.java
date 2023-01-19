// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class GooseControlBlockTest {
    private static final String ID = UUID.randomUUID().toString();
    private static final String DATASET_REF = "DATASET_REF";
    private static final String NAME = "NAME";
    private static final String DESC = "DESCRIPTION";

    @Test
    void constructor_should_fill_default_values() {
        // Given : NAME, DATASET_REF, ID constants
        // When
        GooseControlBlock gooseControlBlock = new GooseControlBlock(NAME, ID, DATASET_REF);
        // Then
        assertThat(gooseControlBlock)
            .extracting(ControlBlock::getName, ControlBlock::getDataSetRef, ControlBlock::getId, GooseControlBlock::isFixedOffs, ControlBlock::getConfRev,
                GooseControlBlock::getSecurityEnable)
            .containsExactly(NAME, DATASET_REF, ID, false, 10000L, TPredefinedTypeOfSecurityEnum.NONE);
    }

    @Test
    void constructor_should_copy_all_data_from_TGSEControl() {
        // Given
        TGSEControl tgseControl = new TGSEControl();
        tgseControl.setName(NAME);
        tgseControl.setDatSet(DATASET_REF);
        tgseControl.setAppID(ID);
        tgseControl.setConfRev(5L);
        tgseControl.setDesc(DESC);
        tgseControl.setFixedOffs(true);
        tgseControl.getIEDName().add(new TControlWithIEDName.IEDName());
        tgseControl.setProtocol(new TProtocol());

        // When
        GooseControlBlock gooseControlBlock = new GooseControlBlock(tgseControl);
        // Then
        assertThat(gooseControlBlock)
            .extracting(ControlBlock::getName,
                ControlBlock::getDataSetRef,
                ControlBlock::getId,
                ControlBlock::getConfRev,
                ControlBlock::getDesc,
                GooseControlBlock::isFixedOffs,
                GooseControlBlock::getSecurityEnable)
            .containsExactly(NAME, DATASET_REF, ID, 5L, DESC, true, TPredefinedTypeOfSecurityEnum.NONE);
        assertThat(gooseControlBlock.getTargets()).hasSize(1);
        assertThat(gooseControlBlock.getProtocol()).isNotNull();
    }

    @Test
    void getServiceType_should_return_GOOSE() {
        // Given
        GooseControlBlock gooseControlBlock = new GooseControlBlock(NAME, ID, DATASET_REF);
        // When
        TServiceType serviceType = gooseControlBlock.getServiceType();
        // Then
        assertThat(serviceType).isEqualTo(TServiceType.GOOSE);
    }

    @Test
    void getControlBlockEnum_should_return_GSE() {
        // Given
        GooseControlBlock gooseControlBlock = new GooseControlBlock(NAME, ID, DATASET_REF);
        // When
        ControlBlockEnum controlBlockEnum = gooseControlBlock.getControlBlockEnum();
        // Then
        assertThat(controlBlockEnum).isEqualTo(ControlBlockEnum.GSE);
    }

    @ParameterizedTest
    @MethodSource("provideCorrectValueForValidateSecurityEnabledValue")
    void validateSecurityEnabledValue_should_not_throw_exception(TServices tServices, TPredefinedTypeOfSecurityEnum securityEnable) {
        // Given
        GooseControlBlock gooseControlBlock = create();
        gooseControlBlock.setSecurityEnable(securityEnable);
        // When & Then
        assertThatCode(() -> gooseControlBlock.validateSecurityEnabledValue(tServices))
            .doesNotThrowAnyException();
    }

    private static Stream<Arguments> provideCorrectValueForValidateSecurityEnabledValue() {
        TServices nullGSESettings = new TServices();
        TServices nullMcSecurity = new TServices();
        nullMcSecurity.setGSESettings(new TGSESettings());

        return Stream.of(
            Arguments.of(null, TPredefinedTypeOfSecurityEnum.NONE),
            Arguments.of(nullGSESettings, TPredefinedTypeOfSecurityEnum.NONE),
            Arguments.of(nullMcSecurity, TPredefinedTypeOfSecurityEnum.NONE),
            Arguments.of(newTServices(true, false), TPredefinedTypeOfSecurityEnum.NONE),
            Arguments.of(newTServices(true, false), TPredefinedTypeOfSecurityEnum.SIGNATURE),
            Arguments.of(newTServices(false, true), TPredefinedTypeOfSecurityEnum.NONE),
            Arguments.of(newTServices(true, true), TPredefinedTypeOfSecurityEnum.NONE),
            Arguments.of(newTServices(true, true), TPredefinedTypeOfSecurityEnum.SIGNATURE),
            Arguments.of(newTServices(true, true), TPredefinedTypeOfSecurityEnum.SIGNATURE_AND_ENCRYPTION)
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidValueForValidateSecurityEnabledValue")
    void validateSecurityEnabledValue_should_throw_exception(TServices tServices, TPredefinedTypeOfSecurityEnum securityEnable) {
        // Given
        GooseControlBlock gooseControlBlock = create();
        gooseControlBlock.setSecurityEnable(securityEnable);
        // When & Then
        assertThatThrownBy(() -> gooseControlBlock.validateSecurityEnabledValue(tServices))
            .isInstanceOf(ScdException.class);
    }

    private static Stream<Arguments> provideInvalidValueForValidateSecurityEnabledValue() {
        TServices nullGSESettings = new TServices();
        TServices nullMcSecurity = new TServices();
        nullMcSecurity.setGSESettings(new TGSESettings());

        return Stream.of(
            Arguments.of(null, TPredefinedTypeOfSecurityEnum.SIGNATURE),
            Arguments.of(null, TPredefinedTypeOfSecurityEnum.SIGNATURE_AND_ENCRYPTION),
            Arguments.of(nullGSESettings, TPredefinedTypeOfSecurityEnum.SIGNATURE),
            Arguments.of(nullGSESettings, TPredefinedTypeOfSecurityEnum.SIGNATURE_AND_ENCRYPTION),
            Arguments.of(nullMcSecurity, TPredefinedTypeOfSecurityEnum.SIGNATURE),
            Arguments.of(nullMcSecurity, TPredefinedTypeOfSecurityEnum.SIGNATURE_AND_ENCRYPTION),
            Arguments.of(newTServices(false, false), TPredefinedTypeOfSecurityEnum.SIGNATURE),
            Arguments.of(newTServices(false, false), TPredefinedTypeOfSecurityEnum.SIGNATURE_AND_ENCRYPTION),
            Arguments.of(newTServices(true, false), TPredefinedTypeOfSecurityEnum.SIGNATURE_AND_ENCRYPTION),
            Arguments.of(newTServices(false, true), TPredefinedTypeOfSecurityEnum.SIGNATURE),
            Arguments.of(newTServices(false, true), TPredefinedTypeOfSecurityEnum.SIGNATURE_AND_ENCRYPTION)
        );
    }

    public static TServices newTServices(boolean isSignature, boolean isEncryption){
        TServices tServices = new TServices();
        tServices.setGSESettings(new TGSESettings());
        tServices.getGSESettings().setMcSecurity(new TMcSecurity());
        tServices.getGSESettings().getMcSecurity().setSignature(isSignature);
        tServices.getGSESettings().getMcSecurity().setEncryption(isEncryption);
        return tServices;
    }

    @Test
    void toTControl_should_return_TGSEControl() {
        // Given
        GooseControlBlock gooseControlBlock = new GooseControlBlock(NAME, ID, DATASET_REF);
        gooseControlBlock.setConfRev(5L);
        gooseControlBlock.setFixedOffs(false);
        TProtocol protocol = new TProtocol();
        protocol.setValue("PROTO");
        protocol.setMustUnderstand(true);
        gooseControlBlock.setProtocol(protocol);
        gooseControlBlock.setDesc(DESC);
        gooseControlBlock.setType(TGSEControlTypeEnum.GSSE);
        gooseControlBlock.setSecurityEnable(TPredefinedTypeOfSecurityEnum.SIGNATURE_AND_ENCRYPTION);
        gooseControlBlock.getTargets().add(
            new ControlBlockTarget("AP_REF", DTO.HOLDER_LD_INST, "", DTO.HOLDER_LN_INST, DTO.HOLDER_LN_CLASS, DTO.HOLDER_LN_PREFIX));

        // When
        TGSEControl tgseControl = gooseControlBlock.toTControl();
        // Then
        assertThat(tgseControl)
            .extracting(TControl::getName, TControl::getDatSet, TGSEControl::getAppID, TControlWithIEDName::getConfRev, TGSEControl::isFixedOffs,
                TUnNaming::getDesc, TGSEControl::getType, TGSEControl::getSecurityEnable)
            .containsExactly(NAME, DATASET_REF, ID, 5L, false, DESC, TGSEControlTypeEnum.GSSE, TPredefinedTypeOfSecurityEnum.SIGNATURE_AND_ENCRYPTION);
        assertThat(tgseControl.getProtocol())
            .extracting(TProtocol::getValue, TProtocol::isMustUnderstand)
            .containsExactly("PROTO", true);
        assertThat(tgseControl.getIEDName()).hasSize(1);
    }

    @Test
    void addToLN_should_add_ControlBlock_to_given_LN0() {
        // Given
        GooseControlBlock gooseControlBlock = new GooseControlBlock(NAME, ID, DATASET_REF);
        TLN0 ln0 = new TLN0();
        // When
        TGSEControl tgseControl = gooseControlBlock.addToLN(ln0);
        // Then
        assertThat(ln0.getGSEControl()).hasSize(1)
            .first()
            .isSameAs(tgseControl);
        assertThat(tgseControl).extracting(TControl::getName, TControl::getDatSet, TGSEControl::getAppID)
            .containsExactly(NAME, DATASET_REF, ID);
    }

    @Test
    void addToLN_when_parameter_is_LN_should_throw_exception() {
        // Given
        GooseControlBlock gooseControlBlock = new GooseControlBlock(NAME, ID, DATASET_REF);
        TLN ln = new TLN();
        // When & Then
        assertThatThrownBy(() -> gooseControlBlock.addToLN(ln))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private GooseControlBlock create(){
        GooseControlBlock gooseControlBlock = new GooseControlBlock(NAME, ID, DATASET_REF);
        gooseControlBlock.setConfRev(1L);

        gooseControlBlock.setFixedOffs(false);
        TProtocol protocol = new TProtocol();
        protocol.setValue("PROTO");
        protocol.setMustUnderstand(true);
        gooseControlBlock.setProtocol(protocol);

        gooseControlBlock.setDesc(DESC);

        return gooseControlBlock;
    }
}
