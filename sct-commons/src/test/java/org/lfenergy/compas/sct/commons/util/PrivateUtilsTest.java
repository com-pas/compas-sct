// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.util;

import jakarta.xml.bind.JAXBElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.support.ReflectionSupport;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.PrivateLinkedToStds;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.icd.IcdHeader;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class PrivateUtilsTest {

    private TPrivate privateSCD;
    private TPrivate privateICD;
    private ObjectFactory objectFactory;

    @BeforeEach
    void setUp() {
        objectFactory = new ObjectFactory();
        privateSCD = objectFactory.createTPrivate();
        privateSCD.setType(PrivateEnum.COMPAS_SCL_FILE_TYPE.getPrivateType());
        privateSCD.getContent().add(objectFactory.createSclFileType(TCompasSclFileType.SCD));
        privateICD = objectFactory.createTPrivate();
        privateICD.setType(PrivateEnum.COMPAS_SCL_FILE_TYPE.getPrivateType());
        privateICD.getContent().add(objectFactory.createSclFileType(TCompasSclFileType.ICD));
    }

    @Test
    void class_should_not_be_instantiable() {
        // Given
        Constructor<?>[] constructors = PrivateUtils.class.getDeclaredConstructors();
        assertThat(constructors).hasSize(1);
        Constructor<?> constructor = constructors[0];
        constructor.setAccessible(true);
        // When & Then
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .getCause().isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void extractCompasPrivates_should_return_privates_value() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.getPrivate().add(privateSCD);
        baseElement.getPrivate().add(privateICD);
        // Given : setUp
        // When
        List<TCompasSclFileType> result = PrivateUtils.extractCompasPrivates(baseElement, TCompasSclFileType.class).toList();
        //Then
        assertThat(result)
                .hasSize(2)
                .containsExactly(TCompasSclFileType.SCD, TCompasSclFileType.ICD);
    }

    @Test
    void extractCompasPrivates_when_no_privates_match_class_should_return_empty_list() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.getPrivate().add(privateSCD);
        // When
        List<TCompasICDHeader> result = PrivateUtils.extractCompasPrivates(baseElement, TCompasICDHeader.class).toList();
        //Then
        assertThat(result).isEmpty();
    }

    @Test
    void extractCompasPrivates_when_class_is_not_compas_class_should_throw_exception() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.getPrivate().add(privateSCD);
        // When
        Stream<Object> compasPrivates = PrivateUtils.extractCompasPrivates(baseElement, Object.class);
        // Then
        assertThatCode(compasPrivates::toList)
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Class java.lang.Object is not mapped to a compas type. See org.lfenergy.compas.sct.commons.util.PrivateEnum");
    }

    @Test
    void extractCompasPrivates_when_content_not_match_private_type_should_throw_exception() {
        // Given : setUp
        privateSCD.setType(PrivateEnum.COMPAS_BAY.getPrivateType());
        TBaseElement baseElement = new SCL();
        baseElement.getPrivate().add(privateSCD);
        Class<?> compasClass = PrivateEnum.COMPAS_BAY.getCompasClass();

        Stream<?> stream = PrivateUtils.extractCompasPrivates(baseElement, compasClass);
        // When & Then
        assertThatCode(stream::toList)
                .isInstanceOf(ClassCastException.class)
                .hasMessage("Cannot cast org.lfenergy.compas.scl2007b4.model.TCompasSclFileType to org.lfenergy.compas.scl2007b4.model.TCompasBay");
    }

    @Test
    void extractCompasPrivates_on_base_element_should_return_privates_value() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.getPrivate().add(privateSCD);
        baseElement.getPrivate().add(privateICD);
        // When
        List<TCompasSclFileType> result = PrivateUtils.extractCompasPrivates(baseElement, TCompasSclFileType.class).toList();
        // Then
        assertThat(result)
                .hasSize(2)
                .containsExactly(TCompasSclFileType.SCD, TCompasSclFileType.ICD);
    }

    @Test
    void extractCompasPrivates_on_base_element_should_not_set_private() {
        // Given
        TBaseElement baseElement = new SCL();
        // When
        PrivateUtils.extractCompasPrivates(baseElement, TCompasSclFileType.class);
        // Then
        assertThat(baseElement.isSetPrivate()).isFalse();
    }

    @Test
    void extractCompasPrivate_on_base_element_should_return_private_value() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.getPrivate().add(privateSCD);
        // When
        Optional<TCompasSclFileType> result = PrivateUtils.extractCompasPrivate(baseElement, TCompasSclFileType.class);
        //Then
        assertThat(result).isPresent()
                .hasValue(TCompasSclFileType.SCD);
    }

    @Test
    void extractCompasPrivate_on_base_element_should_return_empty() {
        // Given
        TBaseElement baseElement = new SCL();
        // When
        Optional<TCompasBay> result = PrivateUtils.extractCompasPrivate(baseElement, TCompasBay.class);
        //Then
        assertThat(result).isNotPresent();
    }

    @Test
    void extractCompasPrivate_on_base_element_should_throw_exception() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.getPrivate().add(privateSCD);
        baseElement.getPrivate().add(privateICD);
        // When & Then
        assertThatCode(() -> PrivateUtils.extractCompasPrivate(baseElement, TCompasSclFileType.class))
                .isInstanceOf(ScdException.class)
                .hasMessage("Expecting maximum 1 element of type class org.lfenergy.compas.scl2007b4.model.TCompasSclFileType in private COMPAS-SclFileType, but got more");
    }

    @Test
    void extractCompasICDHeader_should_return_private_value() {
        // Given
        privateSCD = objectFactory.createTPrivate();
        privateSCD.setType(PrivateEnum.COMPAS_ICDHEADER.getPrivateType());
        TCompasICDHeader tCompasICDHeader = objectFactory.createTCompasICDHeader();
        privateSCD.getContent().add(objectFactory.createICDHeader(tCompasICDHeader));
        // When
        Optional<TCompasICDHeader> optionalResult = PrivateUtils.extractCompasICDHeader(privateSCD);
        //Then
        assertThat(optionalResult).isPresent().get().matches(result -> result == tCompasICDHeader);
    }

    @ParameterizedTest
    @MethodSource("createPrivateTestSources")
    void createPrivate_should_return_private_new_private(Object compasElement) throws InvocationTargetException, IllegalAccessException {
        // Given
        PrivateEnum privateEnum = PrivateEnum.fromClass(compasElement.getClass());
        assertThat(privateEnum).isNotNull();
        Optional<Method> optionalCreatePrivateMethod = ReflectionSupport.findMethod(PrivateUtils.class, "createPrivate", compasElement.getClass());
        assertThat(optionalCreatePrivateMethod).isPresent();
        Method createPrivateMethod = optionalCreatePrivateMethod.get();
        // When
        Object result = createPrivateMethod.invoke(null, compasElement);
        //Then
        assertThat(result).isInstanceOf(TPrivate.class);
        TPrivate resultPrivate = (TPrivate) result;
        assertThat(resultPrivate).isNotNull()
                .hasFieldOrPropertyWithValue("type", privateEnum.getPrivateType());
        assertThat(resultPrivate.getContent()).hasSize(1).first().satisfies(content -> assertThat(content).isInstanceOf(JAXBElement.class));
        JAXBElement<?> content = (JAXBElement<?>) resultPrivate.getContent().getFirst();
        assertThat(content.isNil()).isFalse();
        assertThat(content.getValue()).isNotNull().isInstanceOf(compasElement.getClass())
                .isEqualTo(compasElement);
    }

    public static Stream<Object> createPrivateTestSources() {
        return Stream.of(new TCompasBay(),
                new TCompasICDHeader(),
                TCompasSclFileType.SCD,
                new TCompasSystemVersion());
    }

    @Test
    void createStringPrivate_should_return_new_private() {
        //GIVEN
        String privateType = "COMPAS-LNodeStatus";
        String compasLNodeStatus = "on";
        //WHEN
        TPrivate result = PrivateUtils.createStringPrivate(privateType, compasLNodeStatus);
        //THEN
        assertThat(result.getType()).contains(privateType);
        assertThat(result.getContent()).containsExactly(compasLNodeStatus);
    }

    @Test
    void removePrivates_should_remove_privates() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.getPrivate().add(privateSCD);
        // When
        PrivateUtils.removePrivates(baseElement, PrivateEnum.COMPAS_SCL_FILE_TYPE);
        // Then
        assertThat(baseElement.isSetPrivate()).isFalse();
    }

    @Test
    void removePrivates_when_no_private_match_type_should_do_nothing() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.getPrivate().add(privateSCD);
        // When
        PrivateUtils.removePrivates(baseElement, PrivateEnum.COMPAS_ICDHEADER);
        // Then
        assertThat(baseElement.getPrivate()).hasSize(1);
    }

    @Test
    @Tag("issue-321")
    void removePrivates_should_remove_privates_of_given_type() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.getPrivate().add(privateSCD);
        TCompasICDHeader tCompasICDHeader = objectFactory.createTCompasICDHeader();
        // When
        baseElement.getPrivate().add(PrivateUtils.createPrivate(tCompasICDHeader));
        // When
        PrivateUtils.removePrivates(baseElement, PrivateEnum.COMPAS_ICDHEADER);
        // Then
        assertThat(baseElement.getPrivate()).hasSize(1);
        TPrivate tPrivate = baseElement.getPrivate().getFirst();
        assertThat(tPrivate.getType()).isEqualTo(privateSCD.getType());
        assertThat(tPrivate.getContent()).hasSize(1).first().isInstanceOf(JAXBElement.class);
        JAXBElement<?> jaxbElement = (JAXBElement<?>) tPrivate.getContent().getFirst();
        assertThat(jaxbElement.isNil()).isFalse();
        assertThat(jaxbElement.getValue()).isEqualTo(TCompasSclFileType.SCD);
    }

    @Test
    void removePrivates_should_not_set_private() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.unsetPrivate();
        // When
        PrivateUtils.removePrivates(baseElement, PrivateEnum.COMPAS_ICDHEADER);
        // Then
        assertThat(baseElement.isSetPrivate()).isFalse();
    }

    @Test
    @Tag("issue-321")
    void createMapICDSystemVersionUuidAndSTDFile_when_no_ICDSystemVersionUUID_should_return_empty_map() {
        //Given
        SCL scl1 = new SCL();
        TIED tied1 = new TIED();
        TCompasICDHeader compasICDHeader1 = new TCompasICDHeader();
        //When
        TPrivate tPrivate1 =  PrivateUtils.createPrivate(compasICDHeader1);
        tied1.getPrivate().add(tPrivate1);
        scl1.getIED().add(tied1);

        //When
        Map<String, PrivateLinkedToStds> stringSCLMap = PrivateUtils.createMapICDSystemVersionUuidAndSTDFile(List.of(scl1));

        //Then
        assertThat(stringSCLMap.keySet()).isEmpty();

    }

    @Test
    @Tag("issue-321")
    void createMapICDSystemVersionUuidAndSTDFile_Should_return_map_with_two_lines() {
        //Given
        SCL scl1 = new SCL();
        SCL scl2 = new SCL();
        SCL scl3 = new SCL();
        TIED tied1 = new TIED();
        TIED tied2 = new TIED();
        TIED tied3 = new TIED();
        TCompasICDHeader compasICDHeader1 = new TCompasICDHeader();
        compasICDHeader1.setICDSystemVersionUUID("UUID-1");
        TCompasICDHeader compasICDHeader2 = new TCompasICDHeader();
        compasICDHeader2.setICDSystemVersionUUID("UUID-2");
        TCompasICDHeader compasICDHeader3 = new TCompasICDHeader();
        compasICDHeader3.setICDSystemVersionUUID("UUID-2");
        //When
        TPrivate tPrivate1 = PrivateUtils.createPrivate(compasICDHeader1);
        //When
        TPrivate tPrivate2 = PrivateUtils.createPrivate(compasICDHeader2);
        //When
        TPrivate tPrivate3 = PrivateUtils.createPrivate(compasICDHeader3);
        tied1.getPrivate().add(tPrivate1);
        tied2.getPrivate().add(tPrivate2);
        tied3.getPrivate().add(tPrivate3);
        scl1.getIED().add(tied1);
        scl2.getIED().add(tied2);
        scl3.getIED().add(tied3);

        //When
        Map<String, PrivateLinkedToStds> stringSCLMap = PrivateUtils.createMapICDSystemVersionUuidAndSTDFile(List.of(scl1,scl2,scl3));

        //Then
        assertThat(stringSCLMap.keySet()).hasSize(2).containsExactly("UUID-1", "UUID-2");
        assertThat(stringSCLMap.get("UUID-2").stdList()).hasSize(2);
    }

    @Test
    @Tag("issue-321")
    void checkSTDCorrespondanceWithLNodeCompasICDHeadershoul_throw_scdEception(){
        //Given
        TCompasICDHeader compasICDHeader1 = new TCompasICDHeader();
        compasICDHeader1.setICDSystemVersionUUID("UUID-1");
        TCompasICDHeader compasICDHeader2 = new TCompasICDHeader();
        compasICDHeader2.setICDSystemVersionUUID("UUID-2");
        compasICDHeader2.setHeaderId("ID-2");
        compasICDHeader2.setHeaderVersion("VER-2");
        compasICDHeader2.setHeaderRevision("REV-2");
        //When
        TPrivate tPrivate1 = PrivateUtils.createPrivate(compasICDHeader1);
        //When
        TPrivate tPrivate2 = PrivateUtils.createPrivate(compasICDHeader2);

        PrivateLinkedToStds privateLinkedToSTDs1 = new PrivateLinkedToStds(tPrivate1,Collections.singletonList(new SCL()));
        PrivateLinkedToStds privateLinkedToSTDs2 = new PrivateLinkedToStds(tPrivate2, Arrays.asList(new SCL(), new SCL()));

        Map<String, PrivateLinkedToStds> stringSCLMap = new HashMap<>();
        stringSCLMap.put("UUID-1", privateLinkedToSTDs1);
        stringSCLMap.put("UUID-2", privateLinkedToSTDs2);

        //When Then
        assertThatThrownBy(() -> PrivateUtils.checkSTDCorrespondanceWithLNodeCompasICDHeader(stringSCLMap))
                .isInstanceOf(ScdException.class)
                .hasMessage("There are several STD files corresponding to headerId = ID-2 headerVersion = VER-2 headerRevision = REV-2 and ICDSystemVersionUUID = UUID-2");

    }

    @Test
    @Tag("issue-321")
    void checkSTDCorrespondanceWithLNodeCompasICDHeader_should_pass(){
        //Given
        TCompasICDHeader compasICDHeader1 = new TCompasICDHeader();
        compasICDHeader1.setICDSystemVersionUUID("UUID-1");
        TCompasICDHeader compasICDHeader2 = new TCompasICDHeader();
        compasICDHeader2.setICDSystemVersionUUID("UUID-2");
        //When
        TPrivate tPrivate1 = PrivateUtils.createPrivate(compasICDHeader1);
        //When
        TPrivate tPrivate2 = PrivateUtils.createPrivate(compasICDHeader2);

        PrivateLinkedToStds privateLinkedToSTDs1 = new PrivateLinkedToStds(tPrivate1,Collections.singletonList(new SCL()));
        PrivateLinkedToStds privateLinkedToSTDs2 = new PrivateLinkedToStds(tPrivate2, Collections.singletonList(new SCL()));

        Map<String, PrivateLinkedToStds> stringSCLMap = new HashMap<>();
        stringSCLMap.put("UUID-1", privateLinkedToSTDs1);
        stringSCLMap.put("UUID-2", privateLinkedToSTDs2);

        //When Then
        assertThatCode(() -> PrivateUtils.checkSTDCorrespondanceWithLNodeCompasICDHeader(stringSCLMap))
                .doesNotThrowAnyException();

    }

    @Test
    @Tag("issue-321")
    void stdCheckFormatExceptionMessage_should_return_formatted_message_with_Private_data() {
        //Given
        TCompasICDHeader compasICDHeader = new TCompasICDHeader();
        compasICDHeader.setHeaderId("ID-1");
        compasICDHeader.setHeaderVersion("VER-1");
        compasICDHeader.setICDSystemVersionUUID("UUID-1");
        TPrivate tPrivate =  PrivateUtils.createPrivate(compasICDHeader);

        //When
        String message = PrivateUtils.stdCheckFormatExceptionMessage(tPrivate);

        //Then
        assertThat(message).isEqualTo("headerId = ID-1 headerVersion = VER-1 headerRevision = null and ICDSystemVersionUUID = UUID-1");

    }

    @Test
    @Tag("issue-321")
    void createMapIEDNameAndPrivate_should_return_map_of_three_items() {
        //Given
        SCL scl = new SCL();
        TLNode tlNode1 = new TLNode();
        TLNode tlNode2 = new TLNode();
        TLNode tlNode3 = new TLNode();
        TCompasICDHeader compasICDHeader1 = new TCompasICDHeader();
        compasICDHeader1.setIEDName("IED-1");
        TCompasICDHeader compasICDHeader2 = new TCompasICDHeader();
        compasICDHeader2.setIEDName("IED-2");
        TCompasICDHeader compasICDHeader3 = new TCompasICDHeader();
        compasICDHeader3.setIEDName("IED-3");
        //When
        TPrivate tPrivate1 = PrivateUtils.createPrivate(compasICDHeader1);
        //When
        TPrivate tPrivate2 = PrivateUtils.createPrivate(compasICDHeader2);
        //When
        TPrivate tPrivate3 = PrivateUtils.createPrivate(compasICDHeader3);
        tlNode1.getPrivate().add(tPrivate1);
        tlNode2.getPrivate().add(tPrivate2);
        tlNode3.getPrivate().add(tPrivate3);
        TFunction tFunction = new TFunction();
        tFunction.getLNode().addAll(Arrays.asList(tlNode1, tlNode2, tlNode3));
        TBay tBay = new TBay();
        tBay.getFunction().add(tFunction);
        TVoltageLevel tVoltageLevel = new TVoltageLevel();
        tVoltageLevel.getBay().add(tBay);
        TSubstation tSubstation = new TSubstation();
        tSubstation.getVoltageLevel().add(tVoltageLevel);
        scl.getSubstation().add(tSubstation);

        //When
        Stream<IcdHeader> tPrivateStream = PrivateUtils.streamIcdHeaders(scl);

        //Then
        assertThat(tPrivateStream.toList())
                .hasSize(3)
                .extracting(IcdHeader::getIedName)
                .containsExactlyInAnyOrder("IED-1", "IED-2", "IED-3");
    }

    @Test
    @Tag("issue-321")
    void createMapIEDNameAndPrivate_when_no_compasIcdHeaderPresent_under_substation_should_return_empty_map() {
        //Given
        SCL scl = new SCL();
        TLNode tlNode1 = new TLNode();
        TCompasBay compasBay = new TCompasBay();
        compasBay.setUUID("UUID");
        //When
        TPrivate tPrivate1 =  PrivateUtils.createPrivate(compasBay);
        tlNode1.getPrivate().add(tPrivate1);
        TFunction tFunction = new TFunction();
        tFunction.getLNode().add(tlNode1);
        TBay tBay = new TBay();
        tBay.getFunction().add(tFunction);
        TVoltageLevel tVoltageLevel = new TVoltageLevel();
        tVoltageLevel.getBay().add(tBay);
        TSubstation tSubstation = new TSubstation();
        tSubstation.getVoltageLevel().add(tVoltageLevel);
        scl.getSubstation().add(tSubstation);

        //When
        Stream<IcdHeader> tPrivateStream = PrivateUtils.streamIcdHeaders(scl);

        //Then
        assertThat(tPrivateStream.toList()).isEmpty();
    }

    @Test
    @Tag("issue-321")
    void comparePrivateCompasICDHeaders_should_return_true_equality_not_check_for_IEDNane_BayLabel_IEDinstance() {
        // Given
        TCompasICDHeader compasICDHeader1 = new TCompasICDHeader();
        compasICDHeader1.setIEDName("IED-1");
        compasICDHeader1.setBayLabel("BAY-1");
        compasICDHeader1.setIEDSubstationinstance(BigInteger.ONE);
        TCompasICDHeader compasICDHeader2 = new TCompasICDHeader();
        // When
        TPrivate tPrivate1 = PrivateUtils.createPrivate(compasICDHeader1);
        // When
        TPrivate tPrivate2 = PrivateUtils.createPrivate(compasICDHeader2);

        // When
        boolean result = PrivateUtils.comparePrivateCompasICDHeaders(tPrivate1,tPrivate2);
        // Then
        assertThat(result).isTrue();
    }

    @Test
    @Tag("issue-321")
    void comparePrivateCompasICDHeaders_should_return_false_equality_not_check_for_IEDNane_BayLabel_IEDinstance() {
        // Given
        TCompasICDHeader compasICDHeader1 = new TCompasICDHeader();
        compasICDHeader1.setIEDName("IED-1");
        compasICDHeader1.setBayLabel("BAY-1");
        compasICDHeader1.setIEDSubstationinstance(BigInteger.ONE);
        compasICDHeader1.setICDSystemVersionUUID("UUID-1");
        TCompasICDHeader compasICDHeader2 = new TCompasICDHeader();
        compasICDHeader2.setICDSystemVersionUUID("UUID-2");
        // When
        TPrivate tPrivate1 = PrivateUtils.createPrivate(compasICDHeader1);
        // When
        TPrivate tPrivate2 = PrivateUtils.createPrivate(compasICDHeader2);

        // When
        boolean result = PrivateUtils.comparePrivateCompasICDHeaders(tPrivate1,tPrivate2);
        // Then
        assertThat(result).isFalse();
    }

    @Test
    @Tag("issue-321")
    void comparePrivateCompasICDHeaders_should_return_true() {
        // Given
        TCompasICDHeader compasICDHeader1 = new TCompasICDHeader();
        compasICDHeader1.setIEDName("IED-1");
        compasICDHeader1.setBayLabel("BAY-1");
        compasICDHeader1.setIEDSubstationinstance(BigInteger.ONE);
        compasICDHeader1.setICDSystemVersionUUID("UUID-1");
        TCompasICDHeader compasICDHeader2 = new TCompasICDHeader();
        compasICDHeader2.setICDSystemVersionUUID("UUID-1");
        // When
        TPrivate tPrivate1 = PrivateUtils.createPrivate(compasICDHeader1);
        // When
        TPrivate tPrivate2 = PrivateUtils.createPrivate(compasICDHeader2);

        // When
        boolean result = PrivateUtils.comparePrivateCompasICDHeaders(tPrivate1,tPrivate2);
        // Then
        assertThat(result).isTrue();
    }

    @Test
    @Tag("issue-321")
    void copyCompasICDHeaderFromLNodePrivateIntoSTDPrivate() {
        // Given
        TCompasICDHeader stdCompasICDHeader = new TCompasICDHeader();
        stdCompasICDHeader.setICDSystemVersionUUID("UUID-1");
        TCompasICDHeader lNodeCompasICDHeader = new TCompasICDHeader();
        lNodeCompasICDHeader.setICDSystemVersionUUID("UUID-2");
        lNodeCompasICDHeader.setIEDName("IED-1");
        lNodeCompasICDHeader.setBayLabel("BAY-1");
        lNodeCompasICDHeader.setIEDSubstationinstance(BigInteger.ONE);
        // When
        TPrivate stdTPrivate = PrivateUtils.createPrivate(stdCompasICDHeader);

        // When
        PrivateUtils.copyCompasICDHeaderFromLNodePrivateIntoSTDPrivate(stdTPrivate, lNodeCompasICDHeader);

        // Then
        TCompasICDHeader result = PrivateUtils.extractCompasICDHeader(stdTPrivate).orElseThrow();
        assertThat(result).extracting(TCompasICDHeader::getICDSystemVersionUUID, TCompasICDHeader::getIEDName,
                        TCompasICDHeader::getIEDSubstationinstance, TCompasICDHeader::getBayLabel)
                .containsExactlyInAnyOrder("UUID-2", "IED-1", BigInteger.ONE, "BAY-1");
    }

    @Test
    void getCompasICDHeaders_should_return_ICDHeaders() {
        //Given
        TIED tied = createTIED();

        //When
        TCompasICDHeader tCompasICDHeader = PrivateUtils.extractCompasPrivate(tied, TCompasICDHeader.class).orElseThrow();

        //Then
        assertThat(tCompasICDHeader)
                .extracting(
                        TCompasICDHeader::getICDSystemVersionUUID,
                        TCompasICDHeader::getIEDType,
                        TCompasICDHeader::getIEDSubstationinstance,
                        TCompasICDHeader::getIEDSystemVersioninstance,
                        TCompasICDHeader::getIEDName,
                        TCompasICDHeader::getVendorName,
                        TCompasICDHeader::getIEDmodel,
                        TCompasICDHeader::getIEDredundancy,
                        TCompasICDHeader::getBayLabel,
                        TCompasICDHeader::getHwRev,
                        TCompasICDHeader::getSwRev,
                        TCompasICDHeader::getHeaderId,
                        TCompasICDHeader::getHeaderVersion,
                        TCompasICDHeader::getHeaderRevision
                )
                .containsExactly(
                        "IED4d4fe1a8cda64cf88a5ee4176a1a0eef",
                        TCompasIEDType.SCU,
                        BigInteger.ONE,
                        BigInteger.ONE,
                        null,
                        "RTE",
                        "ICDfromModeling",
                        TCompasIEDRedundancy.A,
                        null,
                        "01.00.00",
                        "01.00.00",
                        "f8dbc8c1-2db7-4652-a9d6-0b414bdeccfa",
                        "01.00.00",
                        "01.00.00");

    }

    private static TIED createTIED() {
        SCL sclFromFile = SclTestMarshaller.getSCLFromFile("/scd-ied-dtt-com-import-stds/std.xml");
        return sclFromFile.getIED().getFirst();
    }

    @Test
    void createPrivate_compas_Topo_should_succeed(){
        // Given
        TCompasTopo tCompasTopo1 = new TCompasTopo();
        TCompasTopo tCompasTopo2 = new TCompasTopo();
        List<TCompasTopo> compasTopos = List.of(tCompasTopo1, tCompasTopo2);
        // When
        TPrivate result = PrivateUtils.createPrivate(compasTopos);
        // Then
        assertThat(result.getContent())
                .map(JAXBElement.class::cast)
                .map(JAXBElement::getValue)
                .containsExactly(tCompasTopo1, tCompasTopo2);
    }

    @Test
    void extractStringPrivate_should_succeed() {
        // Given
        TIED tied = new TIED();
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("MyCustomType");
        tPrivate.getContent().add("hello World");
        tied.getPrivate().add(tPrivate);
        // When
        Optional<String> result = PrivateUtils.extractStringPrivate(tied, "MyCustomType");
        // Then
        assertThat(result).hasValue("hello World");
    }

    @Test
    void setStringPrivate_when_private_missing_should_create_new_private() {
        // Given
        TIED tied = new TIED();
        // When
        PrivateUtils.setStringPrivate(tied, "privateType", "privateValue");
        // Then
        assertThat(tied.getPrivate())
                .singleElement()
                .extracting(TPrivate::getType, TPrivate::getContent)
                .containsExactly("privateType", List.of("privateValue"));
    }

    @Test
    void setStringPrivate_when_private_is_present_should_update_private() {
        // Given
        TIED tied = new TIED();
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("privateType");
        tPrivate.getContent().add("oldValue");
        tied.getPrivate().add(tPrivate);
        // When
        PrivateUtils.setStringPrivate(tied, "privateType", "newValue");
        // Then
        assertThat(tied.getPrivate())
                .singleElement()
                .extracting(TPrivate::getType, TPrivate::getContent)
                .containsExactly("privateType", List.of("newValue"));
    }
}
