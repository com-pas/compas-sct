// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TDurationInMilliSec;
import org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings.SettingsOrError;
import org.lfenergy.compas.sct.commons.scl.PrivateService;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.ControlBlockAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;
import org.lfenergy.compas.sct.commons.util.ControlBlockNetworkSettingsCsvHelper;
import org.lfenergy.compas.sct.commons.util.CsvUtils;
import org.lfenergy.compas.sct.commons.util.PrivateEnum;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.lfenergy.compas.sct.commons.dto.ControlBlockNetworkSettings.Settings;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.findControlBlock;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.findIed;

class ControlBlockNetworkSettingsTest {

    private ControlBlockNetworkSettings controlBlockNetworkSettings;

    @BeforeEach
    public void setUp() {
        String fileName = "ControlBlockCommunicationTemplates.csv";
        InputStream inputStream = Objects.requireNonNull(CsvUtils.class.getClassLoader().getResourceAsStream(fileName), "Resource not found: " + fileName);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        controlBlockNetworkSettings = new ControlBlockNetworkSettingsCsvHelper(inputStreamReader);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            ";01.00;009.001;BCU;A;1;BAY_INTERNAL;300;4;10;2000",
            "GOOSE;;009.001;BCU;A;1;BAY_INTERNAL;300;4;10;2000",
            "GOOSE;01.00;;BCU;A;1;BAY_INTERNAL;300;4;10;2000",
            "GOOSE;01.00;009.001;;A;1;BAY_INTERNAL;300;4;10;2000",
            "GOOSE;01.00;009.001;BCU;;1;BAY_INTERNAL;300;4;10;2000",
            "GOOSE;01.00;009.001;BCU;A;;BAY_INTERNAL;300;4;10;2000",
            "GOOSE;01.00;009.001;BCU;A;1;;300;4;10;2000"
    })
    void constructor_when_csv_has_blank_criteria_cells_should_throw_exception(String row) {
        //Given
        StringReader stringReader = new StringReader(row);
        //When & Then
        assertThatThrownBy(() -> new ControlBlockNetworkSettingsCsvHelper(stringReader))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("At least one criteria is null in row ControlBlockNetworkSettingsCsvHelper.Row")
                .hasMessageContaining("=null,");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "GOOSE;01.00;009.001;BCU;A;XXX;BAY_INTERNAL;300;4;10;2000",
            "GOOSE;01.00;009.001;BCU;A;1;BAY_INTERNAL;XXX;4;10;2000",
            "GOOSE;01.00;009.001;BCU;A;1;BAY_INTERNAL;300;XXX;10;2000",
            "GOOSE;01.00;009.001;BCU;A;1;BAY_INTERNAL;300;4;XXX;2000",
            "GOOSE;01.00;009.001;BCU;A;1;BAY_INTERNAL;300;4;10;XXX"
    })
    void constructor_when_csv_has_malformed_numbers_should_throw_exception(String row) {
        //Given
        StringReader stringReader = new StringReader(row);
        //When & Then
        assertThatThrownBy(() -> new ControlBlockNetworkSettingsCsvHelper(stringReader))
                .isInstanceOf(NumberFormatException.class)
                .hasMessage("For input string: \"XXX\"");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "GOOSE;01.00;009.001;BCU;A;1;BAY_INTERNAL;4096;4;10;2000", // VlanId > MAX_VLAN_ID
            "GOOSE;01.00;009.001;BCU;A;1;BAY_INTERNAL;-1;4;10;2000", // VlanId < 0
            "GOOSE;01.00;009.001;BCU;A;1;BAY_INTERNAL;300;8;10;2000", // VlanPriority > MAX_VLAN_PRIORITY
            "GOOSE;01.00;009.001;BCU;A;1;BAY_INTERNAL;300;-1;10;2000" // VlanPriority < 0
    })
    void constructor_when_csv_has_numbers_our_out_of_bound_should_throw_exception(String row) {
        //Given
        StringReader stringReader = new StringReader(row);
        //When & Then
        assertThatThrownBy(() -> new ControlBlockNetworkSettingsCsvHelper(stringReader))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be between 0 and ");
    }

    @Test
    void constructor_when_unsupported_cbType_should_throw_exception() {
        //Given
        StringReader stringReader = new StringReader("CUSTOM_CB_TYPE;01.00;009.001;BCU;A;1;BAY_INTERNAL;1;4;10;2000");
        //When & Then
        assertThatThrownBy(() -> new ControlBlockNetworkSettingsCsvHelper(stringReader))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported Control Block Type : CUSTOM_CB_TYPE");
    }

    @Test
    void getNetworkSettings_should_return_settings_for_bay_internal_controlBlock() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        ControlBlockAdapter controlBlockAdapter = findControlBlock(sclRootAdapter, "IED_NAME2", "LD_INST21", "CB_LD_INST21_GSI", ControlBlockEnum.GSE);

        //When
        SettingsOrError settingsOrError = controlBlockNetworkSettings.getNetworkSettings(controlBlockAdapter);

        //Then
        assertThat(settingsOrError.errorMessage()).isNull();
        Settings networkSettings = settingsOrError.settings();
        assertThat(networkSettings)
                .extracting(Settings::vlanId, Settings::vlanPriority)
                .containsExactly(300, (byte) 4);
        assertThat(networkSettings.minTime()).extracting(TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier, TDurationInMilliSec::getValue)
                .containsExactly("s", "m", new BigDecimal("10"));
        assertThat(networkSettings.maxTime()).extracting(TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier, TDurationInMilliSec::getValue)
                .containsExactly("s", "m", new BigDecimal("2000"));
    }

    @Test
    void getNetworkSettings_should_return_settings_for_bay_external_controlBlock() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        ControlBlockAdapter controlBlockAdapter = findControlBlock(sclRootAdapter, "IED_NAME3", "LD_INST31", "CB_LD_INST31_GSE", ControlBlockEnum.GSE);

        //When
        SettingsOrError settingsOrError = controlBlockNetworkSettings.getNetworkSettings(controlBlockAdapter);

        //Then
        assertThat(settingsOrError.errorMessage()).isNull();
        Settings networkSettings = settingsOrError.settings();
        assertThat(networkSettings)
                .extracting(Settings::vlanId, Settings::vlanPriority)
                .containsExactly(301, (byte) 5);
        assertThat(networkSettings.minTime()).extracting(TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier, TDurationInMilliSec::getValue)
                .containsExactly("s", "m", new BigDecimal("15"));
        assertThat(networkSettings.maxTime()).extracting(TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier, TDurationInMilliSec::getValue)
                .containsExactly("s", "m", new BigDecimal("5000"));
    }

    @Test
    void getNetworkSettings_should_return_vlanId_null_when_column_contains_none() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        ControlBlockAdapter controlBlockAdapter = findControlBlock(sclRootAdapter, "IED_NAME2", "LD_INST21", "CB_LD_INST21_SVI", ControlBlockEnum.SAMPLED_VALUE);

        //When
        SettingsOrError settingsOrError = controlBlockNetworkSettings.getNetworkSettings(controlBlockAdapter);

        //Then
        assertThat(settingsOrError.errorMessage()).isNull();
        Settings networkSettings = settingsOrError.settings();
        assertThat(networkSettings.vlanId()).isNull();
        assertThat(networkSettings.vlanPriority()).isNull();
    }

    @Test
    void getNetworkSettings_should_return_null_when_row_not_found_in_csv_file() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        findIed(sclRootAdapter, "IED_NAME2").getCompasSystemVersion().get().setMainSystemVersion("99.99");
        ControlBlockAdapter controlBlockAdapter = findControlBlock(sclRootAdapter, "IED_NAME2", "LD_INST21", "CB_LD_INST21_GSI", ControlBlockEnum.GSE);

        //When
        SettingsOrError settingsOrError = controlBlockNetworkSettings.getNetworkSettings(controlBlockAdapter);

        //Then
        assertThat(settingsOrError.errorMessage()).isEqualTo("No row found with these criteria Criteria[controlBlockEnum=GSE, systemVersionWithoutV=99.99.009.001, iedType=BCU, iedRedundancy=A, iedSystemVersionInstance=1, isBayInternal=true]");
        assertThat(settingsOrError.settings()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = PrivateEnum.class, mode = EnumSource.Mode.INCLUDE, names = {"COMPAS_ICDHEADER", "COMPAS_SYSTEM_VERSION"})
    void getNetworkSettings_should_return_null_when_missing_ied_private(PrivateEnum missingPrivate) {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        PrivateService.removePrivates(findIed(sclRootAdapter, "IED_NAME2").getCurrentElem(), missingPrivate);
        ControlBlockAdapter controlBlockAdapter = findControlBlock(sclRootAdapter, "IED_NAME2", "LD_INST21", "CB_LD_INST21_GSI", ControlBlockEnum.GSE);

        //When
        SettingsOrError settingsOrError = controlBlockNetworkSettings.getNetworkSettings(controlBlockAdapter);

        //Then
        assertThat(settingsOrError.errorMessage()).isEqualTo("No private %s found in this IED".formatted(missingPrivate.getPrivateType()));
        assertThat(settingsOrError.settings()).isNull();
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCompasAttributes")
    void getNetworkSettings_should_return_null_when_missing_ied_private_attributes(Consumer<IEDAdapter> transformIedPrivate) {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_controlblock_network_configuration.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        ControlBlockAdapter controlBlockAdapter = findControlBlock(sclRootAdapter, "IED_NAME2", "LD_INST21", "CB_LD_INST21_GSI", ControlBlockEnum.GSE);
        IEDAdapter iedAdapter = findIed(sclRootAdapter, "IED_NAME2");
        transformIedPrivate.accept(iedAdapter);

        //When
        SettingsOrError settingsOrError = controlBlockNetworkSettings.getNetworkSettings(controlBlockAdapter);

        //Then
        assertThat(settingsOrError.errorMessage()).startsWith("No row found with these criteria ");
        assertThat(settingsOrError.settings()).isNull();
    }

    private static Stream<Arguments> provideInvalidCompasAttributes() {
        return Stream.of(
                Arguments.of((Consumer<IEDAdapter>) iedAdapter -> iedAdapter.getCompasICDHeader().get().setIEDType(null)),
                Arguments.of((Consumer<IEDAdapter>) iedAdapter -> iedAdapter.getCompasICDHeader().get().setIEDredundancy(null)),
                Arguments.of((Consumer<IEDAdapter>) iedAdapter -> iedAdapter.getCompasICDHeader().get().setIEDSystemVersioninstance(null)),
                Arguments.of((Consumer<IEDAdapter>) iedAdapter -> iedAdapter.getCompasSystemVersion().get().setMainSystemVersion(null)),
                Arguments.of((Consumer<IEDAdapter>) iedAdapter -> iedAdapter.getCompasSystemVersion().get().setMinorSystemVersion(null)),
                Arguments.of((Consumer<IEDAdapter>) iedAdapter -> iedAdapter.getCompasSystemVersion().get().setMinorSystemVersion("1")) // Invalid format for MinorSystemVersion
        );
    }

}
