// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.lfenergy.compas.sct.commons.testhelpers.SclHelper.findInputs;

class InputsAdapterForDemoTest {
    public static final String NO_VALID_SOURCE_DA = "--NO_VALID_SOURCE_DA--";
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    public void setUpStreams() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.out.println(outContent.toString(StandardCharsets.UTF_8));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideUpdateAllSourceDataSetsAndControlBlocks")
    void updateAllSourceDataSetsAndControlBlocks_should_filter_expected_value(String... expected) throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        InputsAdapter inputsAdapter = findInputs(sclRootAdapter, "IED_NAME1", "LD_INST11");
        // When
        List<SclReportItem> sclReportItems = inputsAdapter.updateAllSourceDataSetsAndControlBlocks();
        // Then
        assertThat(sclReportItems).isEmpty();
        assertThat(Arrays.stream(outContent.toString(StandardCharsets.UTF_8).split("\n")).filter(StringUtils::isNotBlank))
            .contains(expected);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideUpdateAllSourceDataSetsAndControlBlocksIgnoredExtRef")
    void updateAllSourceDataSetsAndControlBlocks_should_ignore_extref(String... unexpectedExtRefDesc) throws Exception {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scd-extref-create-dataset-and-controlblocks/scd_create_dataset_and_controlblocks_success.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        InputsAdapter inputsAdapter = findInputs(sclRootAdapter, "IED_NAME1", "LD_INST11");
        // When
        List<SclReportItem> sclReportItems = inputsAdapter.updateAllSourceDataSetsAndControlBlocks();
        // Then
        assertThat(sclReportItems).isEmpty();
        assertThat(outContent.toString())
            .doesNotContain(unexpectedExtRefDesc);
    }

    public static Stream<Arguments> provideUpdateAllSourceDataSetsAndControlBlocks() {
        return Stream.of(
            Arguments.of(named("should define if signal is internal or external to a Bay",
                new String[]{
                    "ExtRef.desc=test bay internal,isBayInternal=true,"
                        + "ST#DoName.daNameST",
                    "ExtRef.desc=test bay external,isBayInternal=false,"
                        + "ST#DoName.daNameST"})),
            Arguments.of(named("should keep only fc = ST or MX source DA",
                new String[]{
                    "ExtRef.desc=test daName ST,isBayInternal=true,"
                        + "ST#DoName.daNameST",
                    "ExtRef.desc=test daName MX,isBayInternal=true,"
                        + "MX#DoName.daNameMX",
                    "ExtRef.desc=test daName BL,isBayInternal=true,"
                        + NO_VALID_SOURCE_DA})),
            Arguments.of(named("for GOOSE and SMV, should keep only valid fcda candidates",
                new String[]{
                    "ExtRef.desc=test ServiceType is GOOSE, no daName and DO contains ST and MX, but only ST is FCDA candidate,isBayInternal=true,"
                        + "ST#OtherDoName.daNameST",
                    "ExtRef.desc=test ServiceType is SMV, no daName and DO contains ST and MX, but only ST is FCDA candidate,isBayInternal=true,"
                        + "ST#OtherDoName.daNameST"})),
            Arguments.of(named("for Report, should ignore source DA",
                new String[]{
                    "ExtRef.desc=test ServiceType is Report_daReportST_1,isBayInternal=true,"
                        + "ST#DoName.daReportST"})),
            Arguments.of(named("should ignore instance number when checking FCDA Candidate file",
                new String[]{
                    "ExtRef.desc=test no daName and doName with instance number,isBayInternal=true,"
                        + "ST#DoWithInst1.daNameST",
                    "ExtRef.desc=test no daName and doName with instance number and SDO,isBayInternal=true,"
                        + "ST#DoWithInst2.subDo.daNameST"}))
        );
    }

    public static Stream<Arguments> provideUpdateAllSourceDataSetsAndControlBlocksIgnoredExtRef() {
        return Stream.of(
            Arguments.of(named("should ignore binding internal to IED",
                new String[]{"test ignore internal binding"})),
            Arguments.of(named("should ignore extref with missing binding attributes",
                new String[]{"test ignore missing bindings attributes"})),
            Arguments.of(named("should ignore extref when compas:Flow.FlowStatus is neither ACTIVE nor UNTESTED",
                new String[]{"test ignore when compas:Flow.FlowStatus is neither ACTIVE nor UNTESTED"}))
        );
    }
}
