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

class DataAttributeRefTest {

    public static final String CTL_MODEL = "ctlModel";
    @Test
    void testGetObjRef(){
        String expected = "IEDLDTM/prelnclass1.do.sdo1.sdo2.da.bda1.bda2";
        DataAttributeRef dataAttributeRef = DTO.createDataAttributeRef("pre","lnclass","1");
        String objRef = dataAttributeRef.getObjRef("IED","LDTM");
        assertEquals(expected,objRef);
    }

    @Test
    void testGetObjRefWhenLLN0(){
        // given
        DataAttributeRef dataAttributeRef = DTO.createDataAttributeRef("pre","LLN0","1");
        // when
        String objRef = dataAttributeRef.getObjRef("IED","LDTM");
        // then
        assertEquals("IEDLDTM/LLN0.do.sdo1.sdo2.da.bda1.bda2",objRef);
    }

    @Test
    void testGetDataAttributes(){
        // given
        DataAttributeRef dataAttributeRef = DTO.createDataAttributeRef("pre","lnclass","1");
        // when
        String dataAttributes = dataAttributeRef.getDataAttributes();
        // then
        String expected = "do.sdo1.sdo2.da.bda1.bda2";
        assertEquals(expected, dataAttributes);
    }

    @Test
    void testAddDoStructName(){
        // given
        DataAttributeRef dataAttributeRef = DTO.createDataAttributeRef("pre","lnclass","1");
        dataAttributeRef.setDaName(new DaTypeName());
        // when
        dataAttributeRef.addDoStructName("added_sdo");
        // then
        String expected = "do.sdo1.sdo2.added_sdo";
        assertEquals(expected, dataAttributeRef.getDoRef());
    }

    @Test
    void testAddDoStructNameWhenNoDoName(){
        // given
        DataAttributeRef dataAttributeRef = DTO.createDataAttributeRef("pre","lnclass","1");
        dataAttributeRef.setDaName(new DaTypeName());
        dataAttributeRef.setDoName(new DoTypeName());
        // when & then
        assertThrows(IllegalArgumentException.class, () -> dataAttributeRef.addDoStructName("added_sdo"));
    }

    @Test
    void testAddDaStructName(){
        // given
        DataAttributeRef dataAttributeRef = DTO.createDataAttributeRef("pre","lnclass","1");
        // when
        dataAttributeRef.addDaStructName("added_bda");
        // then
        String expected = "da.bda1.bda2.added_bda";
        assertEquals(expected, dataAttributeRef.getDaRef());
    }

    @Test
    void testAddDaStructNameWhenNoDoName(){
        // given
        DataAttributeRef dataAttributeRef = DTO.createDataAttributeRef("pre","lnclass","1");
        dataAttributeRef.setDaName(new DaTypeName());
        dataAttributeRef.setDoName(new DoTypeName());
        // when & then
        assertThrows(IllegalArgumentException.class, () -> dataAttributeRef.addDaStructName("added_sda"));
    }

    @Test
    void testSetDaiValues(){
        // given
        DataAttributeRef dataAttributeRef = DTO.createDataAttributeRef("pre","lnclass","1");
        TVal val = new TVal();
        val.setValue("test");
        // when
        dataAttributeRef.setDaiValues(List.of(val));
        // then
        assertEquals(Map.of(0L, "test"), dataAttributeRef.getDaName().getDaiValues());
    }

    @Test
    void testSetDaiValuesWhenMultipleVal(){
        // given
        DataAttributeRef dataAttributeRef = DTO.createDataAttributeRef("pre","lnclass","1");
        TVal val1 = new TVal();
        val1.setValue("test1");
        val1.setSGroup(0L);
        TVal val2 = new TVal();
        val2.setValue("test2");
        val2.setSGroup(1L);
        // when
        dataAttributeRef.setDaiValues(List.of(val1, val2));
        // then
        assertEquals(Map.of(0L, "test1", 1L, "test2"), dataAttributeRef.getDaName().getDaiValues());
    }

    @Test
    void testCopyFrom() {
        DataAttributeRef dataAttributeRef = DTO.createDataAttributeRef("pre","lnclass","1");
        DataAttributeRef dataAttributeRef_b = DataAttributeRef.copyFrom(dataAttributeRef);

        assertAll("COPY FROM",
                () -> assertEquals(dataAttributeRef_b.getLnClass(), dataAttributeRef.getLnClass()),
                () -> assertEquals(dataAttributeRef_b.getPrefix(), dataAttributeRef.getPrefix()),
                () -> assertEquals(dataAttributeRef_b.getDaName(), dataAttributeRef.getDaName()),
                () -> assertEquals(dataAttributeRef_b.getDoName(), dataAttributeRef.getDoName()),
                () -> assertEquals(dataAttributeRef_b.getLnInst(), dataAttributeRef.getLnInst()),
                () -> assertEquals(dataAttributeRef_b.getLnType(), dataAttributeRef.getLnType()),
                () -> assertArrayEquals(dataAttributeRef_b.getSdoNames().toArray(new String[0]),
                        dataAttributeRef.getSdoNames().toArray(new String[0])),
                () -> assertArrayEquals(dataAttributeRef_b.getBdaNames().toArray(new String[0]),
                        dataAttributeRef.getBdaNames().toArray(new String[0])),
                () -> assertEquals(dataAttributeRef_b.getType(), dataAttributeRef.getType()),
                () -> assertTrue(dataAttributeRef.isValImport())
        );
        DataAttributeRef dataAttributeRef_t = new DataAttributeRef();
        assertNull(dataAttributeRef_t.getFc());
        assertNull(dataAttributeRef_t.getCdc());
        assertNull(dataAttributeRef_t.getBType());
        assertNull(dataAttributeRef_t.getType());
        assertTrue(dataAttributeRef_t.getBdaNames().isEmpty());
        assertTrue(dataAttributeRef_t.getSdoNames().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> dataAttributeRef_t.setCdc(TPredefinedCDCEnum.WYE));
        assertThrows(IllegalArgumentException.class, () -> dataAttributeRef_t.setBType("BType"));
        assertThrows(IllegalArgumentException.class, () -> dataAttributeRef_t.setType("Type"));
        assertThrows(IllegalArgumentException.class, () -> dataAttributeRef_t.setFc(TFCEnum.BL));
    }

    /**
     * Known Fc = CF, DC, SG, SP, ST , SE
     */
    @ParameterizedTest
    @MethodSource("daParametersProviderUpdatable")
    void isUpdatable_case0(String testName, String daName, TFCEnum fc, boolean valImport) {
        //Given
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setDoName(new DoTypeName(MOD_DO_NAME));
        dataAttributeRef.setDaName(new DaTypeName(daName));
        dataAttributeRef.setFc(fc);
        dataAttributeRef.setValImport(valImport);
        //When
        boolean isDataAttributeRefUpdatable = dataAttributeRef.isUpdatable();
        // Then
        assertThat(isDataAttributeRefUpdatable).isTrue();
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
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setDoName(new DoTypeName(MOD_DO_NAME));
        dataAttributeRef.setDaName(new DaTypeName(daName));
        dataAttributeRef.setFc(fc);
        dataAttributeRef.setValImport(valImport);
        //When
        boolean isDataAttributeRefUpdatable = dataAttributeRef.isUpdatable();
        // Then
        assertThat(isDataAttributeRefUpdatable).isFalse();
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
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        DaTypeName da1 = createDa("da1", TFCEnum.CF, true, Map.of(10L, "a value"));
        dataAttributeRef.setDaName(da1);
        // When
        Optional<String> firstValue = dataAttributeRef.findFirstValue();
        // Then
        assertThat(firstValue).hasValue("a value");
    }

    @Test
    void findFirstValue_should_return_first_value(){
        // Given
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        DaTypeName da1 = createDa("da1", TFCEnum.CF, true,
            Map.of(1L, "value 1", 0L, "value 0"));
        dataAttributeRef.setDaName(da1);
        // When
        Optional<String> firstValue = dataAttributeRef.findFirstValue();
        // Then
        assertThat(firstValue).hasValue("value 0");
    }

    @Test
    void findFirstValue_should_return_empty_optional(){
        // Given
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        DaTypeName da1 = createDa("da1", TFCEnum.CF, true, Map.of());
        dataAttributeRef.setDaName(da1);
        // When
        Optional<String> firstValue = dataAttributeRef.findFirstValue();
        // Then
        assertThat(firstValue).isNotPresent();
    }

    @Test
    void setVal_should_add_reference_val(){
        // Given
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        DaTypeName da1 = createDa("da1", TFCEnum.CF, true, Map.of());
        dataAttributeRef.setDaName(da1);
        // When
        dataAttributeRef.setVal("value");
        // Then
        assertThat(dataAttributeRef.getDaName().getDaiValues()).hasSize(1)
                .isEqualTo(Map.of(0L, "value"));
    }

    @Test
    void setVal_should_replace_reference_val(){
        // Given
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        DaTypeName da1 = createDa("da1", TFCEnum.CF, true, Map.of(0L, "oldValue"));
        dataAttributeRef.setDaName(da1);
        // When
        dataAttributeRef.setVal("newValue");
        // Then
        assertThat(dataAttributeRef.getDaName().getDaiValues()).hasSize(1)
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
        DataAttributeRef expected = new DataAttributeRef(lnAdapter, doName, daName);
        // Then
        assertThat(expected).extracting(DataAttributeRef::getLnType, DataAttributeRef::getLnClass, DataAttributeRef::getPrefix, DataAttributeRef::getLnInst)
                .containsExactly("T1", TLLN0Enum.LLN_0.value(), "P1", "1");
        assertThat(expected.getDoName()).isEqualTo(new DoTypeName("do"));
        assertThat(expected.getDaName()).isEqualTo(new DaTypeName("da"));
    }


    @Test
    void constructor_from_TFCDA_should_create_new_instance_with_same_attributes() {
        //Given
        TFCDA tfcda = newFcda("ldInst", "lnClass", "lnInst", "prefix", "DoName.sdo", "daName.bda", TFCEnum.ST);
        //When
        DataAttributeRef dataAttributeRef = new DataAttributeRef(tfcda);
        //Then
        assertThat(dataAttributeRef.getLNRef())
                .isEqualTo("prefixlnClasslnInst.DoName.sdo.daName.bda");
        DaTypeName expectedDa = new DaTypeName("daName.bda");
        expectedDa.setFc(TFCEnum.ST);
        assertThat(dataAttributeRef.getDaName()).isEqualTo(expectedDa);
    }

    @Test
    void constructor_from_TFCDA_should_ignore_blank_DaName() {
        //Given
        TFCDA tfcda = newFcda("ldInst", "lnClass", "lnInst", "prefix", "DoName.sdo", "", TFCEnum.ST);
        //When
        DataAttributeRef dataAttributeRef = new DataAttributeRef(tfcda);
        //Then
        assertThat(dataAttributeRef.isDaNameDefined()).isFalse();
        assertThat(dataAttributeRef.getFc()).isNull();
    }

    @Test
    void constructor_from_TFCDA_should_ignore_blank_fc() {
        //Given
        TFCDA tfcda = newFcda("ldInst", "lnClass", "lnInst", "prefix", "DoName.sdo", "daName.bda", null);
        //When
        DataAttributeRef dataAttributeRef = new DataAttributeRef(tfcda);
        //Then
        assertThat(dataAttributeRef.getLNRef())
                .isEqualTo("prefixlnClasslnInst.DoName.sdo.daName.bda");
        assertThat(dataAttributeRef.getFc()).isNull();
    }
}
