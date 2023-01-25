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
import static org.lfenergy.compas.scl2007b4.model.TSampledValueControl.SmvOpts;

class SMVControlBlockTest {

    private static final String ID = UUID.randomUUID().toString();
    private static final String DATASET_REF = "DATASET_REF";
    private static final String NAME = "NAME";
    private static final String DESC = "DESCRIPTION";

    @Test
    void constructor_should_fill_default_values() {
        // Given : NAME, DATASET_REF, ID constants
        // When
        SMVControlBlock smvControlBlock = new SMVControlBlock(NAME, ID, DATASET_REF);
        // Then
        assertThat(smvControlBlock)
            .extracting(ControlBlock::getName, ControlBlock::getDataSetRef, ControlBlock::getId, SMVControlBlock::isMulticast, SMVControlBlock::getSmpRate,
                SMVControlBlock::getNofASDU, SMVControlBlock::getSmpMod, ControlBlock::getConfRev, SMVControlBlock::getSecurityEnable)
            .containsExactly(NAME, DATASET_REF, ID, true, 4800L, 2L, TSmpMod.SMP_PER_SEC, 10000L, TPredefinedTypeOfSecurityEnum.NONE);

        assertThat(smvControlBlock.getSmvOpts())
            .extracting(
                SmvOpts::isRefreshTime, SmvOpts::isSampleSynchronized, SmvOpts::isSampleRate, SmvOpts::isDataSet, SmvOpts::isSecurity,
                SmvOpts::isTimestamp, SmvOpts::isSynchSourceId)
            .containsExactly(false, true, true, false, false, false, false);
    }

    @Test
    void constructor_should_copy_all_data_from_TSampledValueControl() {
        // Given

        TSampledValueControl tSampledValueControl = createTSampledValueControl();
        // When
        SMVControlBlock smvControlBlock = new SMVControlBlock(tSampledValueControl);
        // Then
        assertThat(smvControlBlock)
            .extracting(ControlBlock::getName,
                ControlBlock::getDataSetRef,
                ControlBlock::getId,
                ControlBlock::getConfRev,
                ControlBlock::getDesc,
                SMVControlBlock::isMulticast,
                SMVControlBlock::getSmpRate,
                SMVControlBlock::getNofASDU,
                SMVControlBlock::getSmpMod,
                SMVControlBlock::getSecurityEnable)
            .containsExactly(NAME, DATASET_REF, ID, 5L, DESC, false, 200L, 10L, TSmpMod.SMP_PER_PERIOD, TPredefinedTypeOfSecurityEnum.NONE);
        assertThat(smvControlBlock.getTargets()).hasSize(1);
        assertThat(smvControlBlock.getProtocol()).isNotNull();
    }

    @Test
    void getServiceType_should_return_GOOSE() {
        // Given
        SMVControlBlock smvControlBlock = new SMVControlBlock(NAME, ID, DATASET_REF);
        // When
        TServiceType serviceType = smvControlBlock.getServiceType();
        // Then
        assertThat(serviceType).isEqualTo(TServiceType.SMV);
    }

    @Test
    void getControlBlockEnum_should_return_GSE() {
        // Given
        SMVControlBlock smvControlBlock = new SMVControlBlock(NAME, ID, DATASET_REF);
        // When
        ControlBlockEnum controlBlockEnum = smvControlBlock.getControlBlockEnum();
        // Then
        assertThat(controlBlockEnum).isEqualTo(ControlBlockEnum.SAMPLED_VALUE);
    }

    @Test
    void toTControl_should_return_TSampledValueControl() {
        // Given
        SMVControlBlock smvControlBlock = createSmvControlBlock();

        // When
        TSampledValueControl tSampledValueControl = smvControlBlock.toTControl();
        // Then
        assertThat(tSampledValueControl)
            .extracting(TControl::getName, TControl::getDatSet, TSampledValueControl::getSmvID, TControlWithIEDName::getConfRev, TUnNaming::getDesc,
                TSampledValueControl::isMulticast, TSampledValueControl::getSmpRate, TSampledValueControl::getNofASDU, TSampledValueControl::getSmpMod,
                TSampledValueControl::getSecurityEnable)
            .containsExactly(NAME, DATASET_REF, ID, 5L, DESC, false, 100L, 10L, TSmpMod.SMP_PER_PERIOD, TPredefinedTypeOfSecurityEnum.SIGNATURE_AND_ENCRYPTION);
        assertThat(tSampledValueControl.getProtocol())
            .extracting(TProtocol::getValue, TProtocol::isMustUnderstand)
            .containsExactly("PROTO", true);
        assertThat(tSampledValueControl.getIEDName()).hasSize(1);
    }

    @Test
    void addToLN_should_add_ControlBlock_to_given_LN0() {
        // Given
        SMVControlBlock smvControlBlock = new SMVControlBlock(NAME, ID, DATASET_REF);
        TLN0 ln0 = new TLN0();
        // When
        TSampledValueControl tSampledValueControl = smvControlBlock.addToLN(ln0);
        // Then
        assertThat(ln0.getSampledValueControl()).hasSize(1)
            .first()
            .isSameAs(tSampledValueControl);
        assertThat(tSampledValueControl).extracting(TControl::getName, TControl::getDatSet, TSampledValueControl::getSmvID)
            .containsExactly(NAME, DATASET_REF, ID);
    }

    @Test
    void addToLN_when_parameter_is_LN_should_throw_exception() {
        // Given
        SMVControlBlock smvControlBlock = new SMVControlBlock(NAME, ID, DATASET_REF);
        TLN ln = new TLN();
        // When & Then
        assertThatThrownBy(() -> smvControlBlock.addToLN(ln))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("provideCorrectValueForValidateSecurityEnabledValue")
    void validateSecurityEnabledValue_should_not_throw_exception(TServices tServices, TPredefinedTypeOfSecurityEnum securityEnable) {
        // Given
        SMVControlBlock smvControlBlock = create();
        smvControlBlock.setSecurityEnable(securityEnable);
        // When & Then
        assertThatCode(() -> smvControlBlock.validateSecurityEnabledValue(tServices))
            .doesNotThrowAnyException();
    }

    private static Stream<Arguments> provideCorrectValueForValidateSecurityEnabledValue() {
        TServices nullGSESettings = new TServices();
        TServices nullMcSecurity = new TServices();
        nullMcSecurity.setSMVSettings(new TSMVSettings());

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
        SMVControlBlock smvControlBlock = create();
        smvControlBlock.setSecurityEnable(securityEnable);
        // When & Then
        assertThatThrownBy(() -> smvControlBlock.validateSecurityEnabledValue(tServices))
            .isInstanceOf(ScdException.class);
    }

    private static Stream<Arguments> provideInvalidValueForValidateSecurityEnabledValue() {
        TServices nullGSESettings = new TServices();
        TServices nullMcSecurity = new TServices();
        nullMcSecurity.setSMVSettings(new TSMVSettings());

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
        tServices.setSMVSettings(new TSMVSettings());
        tServices.getSMVSettings().setMcSecurity(new TMcSecurity());
        tServices.getSMVSettings().getMcSecurity().setSignature(isSignature);
        tServices.getSMVSettings().getMcSecurity().setEncryption(isEncryption);
        return tServices;
    }

    private SMVControlBlock create() {

        SMVControlBlock smvControlBlock = new SMVControlBlock(NAME, ID, DATASET_REF);
        smvControlBlock.setId(ID);
        smvControlBlock.setDataSetRef(DATASET_REF);
        smvControlBlock.setConfRev(1L);
        smvControlBlock.setName(NAME);
        SmvOpts smvOpts = new SmvOpts();

        smvControlBlock.setSmvOpts(smvOpts);
        TProtocol protocol = new TProtocol();
        protocol.setValue("PROTO");
        protocol.setMustUnderstand(true);
        smvControlBlock.setProtocol(protocol);

        smvControlBlock.setDesc(DESC);

        return smvControlBlock;
    }

    private static SMVControlBlock createSmvControlBlock() {
        SMVControlBlock smvControlBlock = new SMVControlBlock(NAME, ID, DATASET_REF);
        smvControlBlock.setConfRev(5L);
        TProtocol protocol = new TProtocol();
        protocol.setValue("PROTO");
        protocol.setMustUnderstand(true);
        smvControlBlock.setProtocol(protocol);
        smvControlBlock.setDesc(DESC);
        smvControlBlock.setMulticast(false);
        smvControlBlock.setSmpRate(100L);
        smvControlBlock.setNofASDU(10L);
        smvControlBlock.setSmpMod(TSmpMod.SMP_PER_PERIOD);
        smvControlBlock.setSecurityEnable(TPredefinedTypeOfSecurityEnum.SIGNATURE_AND_ENCRYPTION);
        smvControlBlock.getTargets().add(
            new ControlBlockTarget("AP_REF", DTO.HOLDER_LD_INST, "", DTO.HOLDER_LN_INST, DTO.HOLDER_LN_CLASS, DTO.HOLDER_LN_PREFIX));
        return smvControlBlock;
    }

    private static TSampledValueControl createTSampledValueControl() {
        TSampledValueControl tSampledValueControl = new TSampledValueControl();
        tSampledValueControl.setName(NAME);
        tSampledValueControl.setDatSet(DATASET_REF);
        tSampledValueControl.setSmvID(ID);
        tSampledValueControl.setConfRev(5L);
        tSampledValueControl.setDesc(DESC);
        tSampledValueControl.setMulticast(false);
        tSampledValueControl.setSmpRate(200L);
        tSampledValueControl.setNofASDU(10L);
        tSampledValueControl.setSmpMod(TSmpMod.SMP_PER_PERIOD);
        tSampledValueControl.getIEDName().add(new TControlWithIEDName.IEDName());
        tSampledValueControl.setProtocol(new TProtocol());
        return tSampledValueControl;
    }
}
