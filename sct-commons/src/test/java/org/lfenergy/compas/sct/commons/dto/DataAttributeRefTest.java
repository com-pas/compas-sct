// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.scl.ln.LNAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.LnKey;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.lfenergy.compas.sct.commons.testhelpers.DataTypeUtils.createDa;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.MOD_DO_NAME;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.STVAL_DA_NAME;
import static org.lfenergy.compas.sct.commons.util.SclConstructorHelper.newFcda;

class DataAttributeRefTest {

    @Test
    void getObjRef_whenCalledWithAnyLNClassRef_should_return_expected_value(){
        // Given
        String expected = "IEDLDTM/prelnclass1.do.sdo1.sdo2.da.bda1.bda2";
        DataAttributeRef dataAttributeRef = DTO.createDataAttributeRef("pre","lnclass","1");
        // When
        String objRef = dataAttributeRef.getObjRef("IED","LDTM");
        // Then
        assertThat(objRef).isEqualTo(expected);
    }

    @Test
    void getObjRef_whenCalledWithLLN0Ref_should_return_expected_value(){
        // given
        DataAttributeRef dataAttributeRef = DTO.createDataAttributeRef("pre","LLN0","1");
        // when
        String objRef = dataAttributeRef.getObjRef("IED","LDTM");
        // then
        assertThat(objRef).isEqualTo("IEDLDTM/LLN0.do.sdo1.sdo2.da.bda1.bda2");
    }

    @Test
    void getDataAttributes_should_return_expected_value(){
        // given
        DataAttributeRef dataAttributeRef = DTO.createDataAttributeRef("pre","lnclass","1");
        // when
        String dataAttributes = dataAttributeRef.getDataAttributes();
        // then
        String expected = "do.sdo1.sdo2.da.bda1.bda2";
        assertThat(dataAttributes).isEqualTo(expected);
    }

    @Test
    void addDoStructName_when_called_shouldSetDoRefValue(){
        // given
        DataAttributeRef dataAttributeRef = DTO.createDataAttributeRef("pre","lnclass","1");
        dataAttributeRef.setDaName(new DaTypeName());
        assertThat(dataAttributeRef.getDoRef()).isEqualTo("do.sdo1.sdo2");
        // when
        dataAttributeRef.addDoStructName("added_sdo");
        // then
        String expected = "do.sdo1.sdo2.added_sdo";
        assertThat(dataAttributeRef.getDoRef()).isEqualTo(expected);
    }

    @Test
    void addDoStructName_when_called_with_unDefinedDoName_should_throw_exception(){
        // given
        DataAttributeRef dataAttributeRef = DTO.createDataAttributeRef("pre","lnclass","1");
        dataAttributeRef.setDaName(new DaTypeName());
        dataAttributeRef.setDoName(new DoTypeName());
        // when & then
        assertThatCode(() -> dataAttributeRef.addDoStructName("added_sdo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DO name must be defined before adding DO StructName");
    }

    @Test
    void addDaStructName_when_called_shouldSetDaRefValue(){
        // given
        DataAttributeRef dataAttributeRef = DTO.createDataAttributeRef("pre","lnclass","1");
        // when
        dataAttributeRef.addDaStructName("added_bda");
        // then
        String expected = "da.bda1.bda2.added_bda";
        assertThat(dataAttributeRef.getDaRef()).isEqualTo(expected);
    }

    @Test
    void addDaStructName_when_called_with_unDefinedDaName_should_throw_exception(){
        // given
        DataAttributeRef dataAttributeRef = DTO.createDataAttributeRef("pre","lnclass","1");
        dataAttributeRef.setDaName(new DaTypeName());
        dataAttributeRef.setDoName(new DoTypeName());
        // when & then
        assertThatCode(() -> dataAttributeRef.addDaStructName("added_sda"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DA name must be defined before adding DA StructName");

    }

    @Test
    void setDaiValues_when_called_shouldFillSGroupAndValue(){
        // given
        DataAttributeRef dataAttributeRef = DTO.createDataAttributeRef("pre","lnclass","1");
        TVal val = new TVal();
        val.setValue("test");
        // when
        dataAttributeRef.setDaiValues(List.of(val));
        // then
        assertThat(dataAttributeRef.getDaName().getDaiValues()).isEqualTo(Map.of(0L, "test"));
    }

    @Test
    void setDaiValues_when_called_withMultipleVal_shouldFillSGroupAndValue(){
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
        assertThat(dataAttributeRef.getDaName().getDaiValues()).isEqualTo(Map.of(0L, "test1", 1L, "test2"));
    }

    @Test
    @Tag("issue-321")
    void testCopyFrom() {
        // Given
        DataAttributeRef dataAttributeRef = DTO.createDataAttributeRef("pre","lnclass","1");
        // When
        DataAttributeRef dataAttributeRef_b = DataAttributeRef.copyFrom(dataAttributeRef);
        // Then
        assertAll("COPY FROM",
                () -> assertThat(dataAttributeRef.getLnClass()).isEqualTo(dataAttributeRef_b.getLnClass()),
                () -> assertThat(dataAttributeRef.getPrefix()).isEqualTo(dataAttributeRef_b.getPrefix()),
                () -> assertThat(dataAttributeRef.getDaName()).isEqualTo(dataAttributeRef_b.getDaName()),
                () -> assertThat(dataAttributeRef.getDoName()).isEqualTo(dataAttributeRef_b.getDoName()),
                () -> assertThat(dataAttributeRef.getLnInst()).isEqualTo(dataAttributeRef_b.getLnInst()),
                () -> assertThat(dataAttributeRef.getLnType()).isEqualTo(dataAttributeRef_b.getLnType()),
                () -> assertArrayEquals(dataAttributeRef_b.getSdoNames().toArray(new String[0]),
                        dataAttributeRef.getSdoNames().toArray(new String[0])),
                () -> assertArrayEquals(dataAttributeRef_b.getBdaNames().toArray(new String[0]),
                        dataAttributeRef.getBdaNames().toArray(new String[0])),
                () -> assertThat(dataAttributeRef.getType()).isEqualTo(dataAttributeRef_b.getType()),
                () -> assertThat(dataAttributeRef.isValImport()).isTrue()
        );
        // Given
        DataAttributeRef dataAttributeRef_t = new DataAttributeRef();
        assertThat(dataAttributeRef_t.getFc()).isNull();
        assertThat(dataAttributeRef_t.getCdc()).isNull();
        assertThat(dataAttributeRef_t.getBType()).isNull();
        assertThat(dataAttributeRef_t.getType()).isNull();
        assertThat(dataAttributeRef_t.getBdaNames()).isEmpty();
        assertThat(dataAttributeRef_t.getSdoNames()).isEmpty();
        // When Then
        assertThatCode(() -> dataAttributeRef_t.setCdc(TPredefinedCDCEnum.WYE))
                .isInstanceOf(IllegalArgumentException.class);
        // When Then
        assertThatCode(() -> dataAttributeRef_t.setBType("BType"))
                .isInstanceOf(IllegalArgumentException.class);
        // When Then
        assertThatCode(() -> dataAttributeRef_t.setType("Type"))
                .isInstanceOf(IllegalArgumentException.class);
        // When Then
        assertThatCode(() -> dataAttributeRef_t.setFc(TFCEnum.BL))
                .isInstanceOf(IllegalArgumentException.class);
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
                Arguments.of("should return true when Mod", "ctlModel", TFCEnum.CF, true)
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
                Arguments.of("should return false when Mod", "ctlModel", TFCEnum.CF, false),
                Arguments.of("should return false when Mod", "ctlModel", TFCEnum.MX, true),
                Arguments.of("should return false when Mod", "ctlModel", TFCEnum.MX, false),
                Arguments.of("should return false when Mod", "ctlModel", null, true),
                Arguments.of("should return false when Mod", "ctlModel", null, false)
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

    @Test
    void test_updateDataRef_withLN() {
        //Given
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        TLN ln = new TLN();
        ln.setLnType("LnTypeAny");
        ln.setInst("1");
        ln.getLnClass().add(TSystemLNGroupEnum.LGOS.value());
        // When
        DataAttributeRef result = DataAttributeRef.updateDataRef(ln, dataAttributeRef);
        // Then
        assertThat(result).extracting(DataAttributeRef::getLnType,
                        DataAttributeRef::getPrefix,
                        DataAttributeRef::getLnClass,
                        DataAttributeRef::getLnInst)
                .containsExactly("LnTypeAny", "", "LGOS", "1");
    }

    @Test
    void test_updateDataRef_withLN0() {
        //Given
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        LN0 ln0 = new LN0();
        ln0.setLnType("LnType0");
        ln0.getLnClass().add(TLLN0Enum.LLN_0.value());
        // When
        DataAttributeRef result = DataAttributeRef.updateDataRef(ln0, dataAttributeRef);
        // Then
        assertThat(result).extracting(DataAttributeRef::getLnType,
                        DataAttributeRef::getPrefix,
                        DataAttributeRef::getLnClass,
                        DataAttributeRef::getLnInst)
                .containsExactly("LnType0", "", "LLN0", null);
    }
}
