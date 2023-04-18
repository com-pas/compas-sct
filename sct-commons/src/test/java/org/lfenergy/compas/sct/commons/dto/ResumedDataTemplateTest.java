// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.scl.ied.LNAdapter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.lfenergy.compas.sct.commons.testhelpers.DataTypeUtils.createDa;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.MOD_DO_NAME;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.STVAL_DA_NAME;
import static org.lfenergy.compas.sct.commons.util.SclConstructorHelper.newFcda;

class ResumedDataTemplateTest {

    public static final String CTL_MODEL = "ctlModel";
    @Test
    void testGetObjRef(){
        String expected = "IEDLDTM/prelnclass1.do.sdo1.sdo2.da.bda1.bda2";
        ResumedDataTemplate resumedDataTemplate = DTO.createRTT("pre","lnclass","1");
        String objRef = resumedDataTemplate.getObjRef("IED","LDTM");
        assertEquals(expected,objRef);
    }

    @Test
    void testGetObjRefWhenLLN0(){
        // given
        ResumedDataTemplate resumedDataTemplate = DTO.createRTT("pre","LLN0","1");
        // when
        String objRef = resumedDataTemplate.getObjRef("IED","LDTM");
        // then
        assertEquals("IEDLDTM/LLN0.do.sdo1.sdo2.da.bda1.bda2",objRef);
    }

    @Test
    void testGetDataAttributes(){
        // given
        ResumedDataTemplate resumedDataTemplate = DTO.createRTT("pre","lnclass","1");
        // when
        String dataAttributes = resumedDataTemplate.getDataAttributes();
        // then
        String expected = "do.sdo1.sdo2.da.bda1.bda2";
        assertEquals(expected, dataAttributes);
    }

    @Test
    void testAddDoStructName(){
        // given
        ResumedDataTemplate resumedDataTemplate = DTO.createRTT("pre","lnclass","1");
        resumedDataTemplate.setDaName(new DaTypeName());
        // when
        resumedDataTemplate.addDoStructName("added_sdo");
        // then
        String expected = "do.sdo1.sdo2.added_sdo";
        assertEquals(expected, resumedDataTemplate.getDoRef());
    }

    @Test
    void testAddDoStructNameWhenNoDoName(){
        // given
        ResumedDataTemplate resumedDataTemplate = DTO.createRTT("pre","lnclass","1");
        resumedDataTemplate.setDaName(new DaTypeName());
        resumedDataTemplate.setDoName(new DoTypeName());
        // when & then
        assertThrows(IllegalArgumentException.class, () -> resumedDataTemplate.addDoStructName("added_sdo"));
    }

    @Test
    void testAddDaStructName(){
        // given
        ResumedDataTemplate resumedDataTemplate = DTO.createRTT("pre","lnclass","1");
        // when
        resumedDataTemplate.addDaStructName("added_bda");
        // then
        String expected = "da.bda1.bda2.added_bda";
        assertEquals(expected, resumedDataTemplate.getDaRef());
    }

    @Test
    void testAddDaStructNameWhenNoDoName(){
        // given
        ResumedDataTemplate resumedDataTemplate = DTO.createRTT("pre","lnclass","1");
        resumedDataTemplate.setDaName(new DaTypeName());
        resumedDataTemplate.setDoName(new DoTypeName());
        // when & then
        assertThrows(IllegalArgumentException.class, () -> resumedDataTemplate.addDaStructName("added_sda"));
    }

    @Test
    void testSetDaiValues(){
        // given
        ResumedDataTemplate resumedDataTemplate = DTO.createRTT("pre","lnclass","1");
        TVal val = new TVal();
        val.setValue("test");
        // when
        resumedDataTemplate.setDaiValues(List.of(val));
        // then
        assertEquals(Map.of(0L, "test"), resumedDataTemplate.getDaName().getDaiValues());
    }

    @Test
    void testSetDaiValuesWhenMultipleVal(){
        // given
        ResumedDataTemplate resumedDataTemplate = DTO.createRTT("pre","lnclass","1");
        TVal val1 = new TVal();
        val1.setValue("test1");
        val1.setSGroup(0L);
        TVal val2 = new TVal();
        val2.setValue("test2");
        val2.setSGroup(1L);
        // when
        resumedDataTemplate.setDaiValues(List.of(val1, val2));
        // then
        assertEquals(Map.of(0L, "test1", 1L, "test2"), resumedDataTemplate.getDaName().getDaiValues());
    }

    @Test
    void testCopyFrom() {
        ResumedDataTemplate rDtt = DTO.createRTT("pre","lnclass","1");
        ResumedDataTemplate rDtt_b = ResumedDataTemplate.copyFrom(rDtt);

        assertAll("COPY FROM",
                () -> assertEquals(rDtt_b.getLnClass(), rDtt.getLnClass()),
                () -> assertEquals(rDtt_b.getPrefix(), rDtt.getPrefix()),
                () -> assertEquals(rDtt_b.getDaName(), rDtt.getDaName()),
                () -> assertEquals(rDtt_b.getDoName(), rDtt.getDoName()),
                () -> assertEquals(rDtt_b.getLnInst(), rDtt.getLnInst()),
                () -> assertEquals(rDtt_b.getLnType(), rDtt.getLnType()),
                () -> assertArrayEquals(rDtt_b.getSdoNames().toArray(new String[0]),
                        rDtt.getSdoNames().toArray(new String[0])),
                () -> assertArrayEquals(rDtt_b.getBdaNames().toArray(new String[0]),
                        rDtt.getBdaNames().toArray(new String[0])),
                () -> assertEquals(rDtt_b.getType(), rDtt.getType()),
                () -> assertTrue(rDtt.isValImport())
        );
        ResumedDataTemplate rDtt_t = new ResumedDataTemplate();
        assertNull(rDtt_t.getFc());
        assertNull(rDtt_t.getCdc());
        assertNull(rDtt_t.getBType());
        assertNull(rDtt_t.getType());
        assertTrue(rDtt_t.getBdaNames().isEmpty());
        assertTrue(rDtt_t.getSdoNames().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> rDtt_t.setCdc(TPredefinedCDCEnum.WYE));
        assertThrows(IllegalArgumentException.class, () -> rDtt_t.setBType("BType"));
        assertThrows(IllegalArgumentException.class, () -> rDtt_t.setType("Type"));
        assertThrows(IllegalArgumentException.class, () -> rDtt_t.setFc(TFCEnum.BL));
    }

    /**
     * Known Fc = CF, DC, SG, SP, ST , SE
     */
    @ParameterizedTest
    @MethodSource("daParametersProviderUpdatable")
    void isUpdatable_case0(String testName, String daName, TFCEnum fc, boolean valImport) {
        //Given
        ResumedDataTemplate rDTT = new ResumedDataTemplate();
        rDTT.setDoName(new DoTypeName(MOD_DO_NAME));
        rDTT.setDaName(new DaTypeName(daName));
        rDTT.setFc(fc);
        rDTT.setValImport(valImport);
        //When
        boolean isRdttUpdatable = rDTT.isUpdatable();
        // Then
        assertThat(isRdttUpdatable).isTrue();
    }

    private static Stream<Arguments> daParametersProviderUpdatable() {
        return Stream.of(
                Arguments.of("should return true when Mod", STVAL_DA_NAME, TFCEnum.CF, true),
                Arguments.of("should return true when Mod", STVAL_DA_NAME, TFCEnum.CF, false),
                Arguments.of("should return true when Mod", STVAL_DA_NAME, TFCEnum.MX, true),
                Arguments.of("should return true when Mod", STVAL_DA_NAME, TFCEnum.MX, false),
                Arguments.of("should return true when Mod", STVAL_DA_NAME, null, true),
                Arguments.of("should return true when Mod", STVAL_DA_NAME, null, false),
                Arguments.of("should return true when Mod", CTL_MODEL, TFCEnum.CF, true)
        );
    }

    @ParameterizedTest
    @MethodSource("daParametersProviderNotUpdatable")
    void isUpdatable_case1(String testName, String daName, TFCEnum fc, boolean valImport) {
        //Given
        ResumedDataTemplate rDTT = new ResumedDataTemplate();
        rDTT.setDoName(new DoTypeName(MOD_DO_NAME));
        rDTT.setDaName(new DaTypeName(daName));
        rDTT.setFc(fc);
        rDTT.setValImport(valImport);
        //When
        boolean isRdttUpdatable = rDTT.isUpdatable();
        // Then
        assertThat(isRdttUpdatable).isFalse();
    }

    private static Stream<Arguments> daParametersProviderNotUpdatable() {
        return Stream.of(
                Arguments.of("should return false when Mod", CTL_MODEL, TFCEnum.CF, false),
                Arguments.of("should return false when Mod", CTL_MODEL, TFCEnum.MX, true),
                Arguments.of("should return false when Mod", CTL_MODEL, TFCEnum.MX, false),
                Arguments.of("should return false when Mod", CTL_MODEL, null, true),
                Arguments.of("should return false when Mod", CTL_MODEL, null, false)
        );
    }

    @Test
    void findFirstValue_should_return_value(){
        // Given
        ResumedDataTemplate rDTT = new ResumedDataTemplate();
        DaTypeName da1 = createDa("da1", TFCEnum.CF, true, Map.of(10L, "a value"));
        rDTT.setDaName(da1);
        // When
        Optional<String> firstValue = rDTT.findFirstValue();
        // Then
        assertThat(firstValue).hasValue("a value");
    }

    @Test
    void findFirstValue_should_return_first_value(){
        // Given
        ResumedDataTemplate rDTT = new ResumedDataTemplate();
        DaTypeName da1 = createDa("da1", TFCEnum.CF, true,
            Map.of(1L, "value 1", 0L, "value 0"));
        rDTT.setDaName(da1);
        // When
        Optional<String> firstValue = rDTT.findFirstValue();
        // Then
        assertThat(firstValue).hasValue("value 0");
    }

    @Test
    void findFirstValue_should_return_empty_optional(){
        // Given
        ResumedDataTemplate rDTT = new ResumedDataTemplate();
        DaTypeName da1 = createDa("da1", TFCEnum.CF, true, Map.of());
        rDTT.setDaName(da1);
        // When
        Optional<String> firstValue = rDTT.findFirstValue();
        // Then
        assertThat(firstValue).isNotPresent();
    }

    @Test
    void setVal_should_add_reference_val(){
        // Given
        ResumedDataTemplate rDTT = new ResumedDataTemplate();
        DaTypeName da1 = createDa("da1", TFCEnum.CF, true, Map.of());
        rDTT.setDaName(da1);
        // When
        rDTT.setVal("value");
        // Then
        assertThat(rDTT.getDaName().getDaiValues()).hasSize(1)
                .isEqualTo(Map.of(0L, "value"));
    }

    @Test
    void setVal_should_replace_reference_val(){
        // Given
        ResumedDataTemplate rDTT = new ResumedDataTemplate();
        DaTypeName da1 = createDa("da1", TFCEnum.CF, true, Map.of(0L, "oldValue"));
        rDTT.setDaName(da1);
        // When
        rDTT.setVal("newValue");
        // Then
        assertThat(rDTT.getDaName().getDaiValues()).hasSize(1)
                .isEqualTo(Map.of(0L, "newValue"));
    }

    @Test
    void constructor_should_create_new_instance() {
        // Given
        TLN tln = new TLN();
        tln.setLnType("T1");
        tln.getLnClass().add(TLLN0Enum.LLN_0.value());
        tln.setPrefix("P1");
        tln.setInst("1");
        LNAdapter lnAdapter = new LNAdapter(null, tln);
        DoTypeName doName = new DoTypeName("do");
        DaTypeName daName = new DaTypeName("da");
        // When
        ResumedDataTemplate expected = new ResumedDataTemplate(lnAdapter, doName, daName);
        // Then
        assertThat(expected).extracting(ResumedDataTemplate::getLnType, ResumedDataTemplate::getLnClass, ResumedDataTemplate::getPrefix, ResumedDataTemplate::getLnInst)
                .containsExactly("T1", TLLN0Enum.LLN_0.value(), "P1", "1");
        assertThat(expected.getDoName()).isEqualTo(new DoTypeName("do"));
        assertThat(expected.getDaName()).isEqualTo(new DaTypeName("da"));
    }


    @Test
    void constructor_from_TFCDA_should_create_new_instance_with_same_attributes() {
        //Given
        TFCDA tfcda = newFcda("ldInst", "lnClass", "lnInst", "prefix", "DoName.sdo", "daName.bda", TFCEnum.ST);
        //When
        ResumedDataTemplate resumedDataTemplate = new ResumedDataTemplate(tfcda);
        //Then
        assertThat(resumedDataTemplate.getLNRef())
                .isEqualTo("prefixlnClasslnInst.DoName.sdo.daName.bda");
        DaTypeName expectedDa = new DaTypeName("daName.bda");
        expectedDa.setFc(TFCEnum.ST);
        assertThat(resumedDataTemplate.getDaName()).isEqualTo(expectedDa);
    }

    @Test
    void constructor_from_TFCDA_should_ignore_blank_DaName() {
        //Given
        TFCDA tfcda = newFcda("ldInst", "lnClass", "lnInst", "prefix", "DoName.sdo", "", TFCEnum.ST);
        //When
        ResumedDataTemplate resumedDataTemplate = new ResumedDataTemplate(tfcda);
        //Then
        assertThat(resumedDataTemplate.isDaNameDefined()).isFalse();
        assertThat(resumedDataTemplate.getFc()).isNull();
    }

    @Test
    void constructor_from_TFCDA_should_ignore_blank_fc() {
        //Given
        TFCDA tfcda = newFcda("ldInst", "lnClass", "lnInst", "prefix", "DoName.sdo", "daName.bda", null);
        //When
        ResumedDataTemplate resumedDataTemplate = new ResumedDataTemplate(tfcda);
        //Then
        assertThat(resumedDataTemplate.getLNRef())
                .isEqualTo("prefixlnClasslnInst.DoName.sdo.daName.bda");
        assertThat(resumedDataTemplate.getFc()).isNull();
    }
}
