// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.support.ReflectionSupport;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.util.PrivateEnum;

import javax.xml.bind.JAXBElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PrivateServiceTest {

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
        Constructor<?>[] constructors = PrivateService.class.getDeclaredConstructors();
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
        // When
        List<TCompasSclFileType> result = PrivateService.extractCompasPrivates(List.of(privateSCD, privateICD),
            TCompasSclFileType.class);
        //Then
        assertThat(result)
            .hasSize(2)
            .containsExactly(TCompasSclFileType.SCD, TCompasSclFileType.ICD);
    }

    @Test
    void extractCompasPrivates_when_no_privates_match_class_should_return_empty_list() {
        // Given : setUp
        // When
        List<TCompasICDHeader> result = PrivateService.extractCompasPrivates(List.of(privateSCD), TCompasICDHeader.class);
        //Then
        assertThat(result).isEmpty();
    }

    @Test
    void extractCompasPrivates_when_class_is_not_compas_class_should_throw_exception() {
        // Given : setUp
        List<TPrivate> privates = List.of(privateSCD);
        // When & Then
        assertThatThrownBy(() -> PrivateService.extractCompasPrivates(privates, Object.class)).isInstanceOf(
            NoSuchElementException.class);
    }

    @Test
    void extractCompasPrivates_when_content_not_match_private_type_should_throw_exception() {
        // Given : setUp
        privateSCD.setType(PrivateEnum.COMPAS_BAY.getPrivateType());
        Class<?> compasClass = PrivateEnum.COMPAS_BAY.getCompasClass();
        List<TPrivate> privates = List.of(privateSCD);
        // When & Then
        assertThatThrownBy(() -> PrivateService.extractCompasPrivates(privates, compasClass)).isInstanceOf(
            ScdException.class);
    }

    @Test
    void extractCompasPrivates_on_base_element_should_return_privates_value() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.getPrivate().add(privateSCD);
        baseElement.getPrivate().add(privateICD);
        // When
        List<TCompasSclFileType> result = PrivateService.extractCompasPrivates(baseElement, TCompasSclFileType.class);
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
        PrivateService.extractCompasPrivates(baseElement, TCompasSclFileType.class);
        // Then
        assertThat(baseElement.isSetPrivate()).isFalse();
    }

    @Test
    void extractCompasPrivate_should_return_private_value() {
        // Given : setUp
        // When
        Optional<TCompasSclFileType> result = PrivateService.extractCompasPrivate(privateSCD,
            TCompasSclFileType.class);
        //Then
        assertThat(result).isPresent()
            .hasValue(TCompasSclFileType.SCD);
    }

    @Test
    void extractCompasPrivate_should_return_empty() {
        // Given : setUp
        // When
        Optional<TCompasBay> result = PrivateService.extractCompasPrivate(privateSCD,
            TCompasBay.class);
        //Then
        assertThat(result).isNotPresent();
    }

    @Test
    void extractCompasPrivate_should_throw_exception() {
        // Given : setUp
        privateSCD.getContent().add(objectFactory.createSclFileType(TCompasSclFileType.ICD));
        Class<?> compasClass = PrivateEnum.COMPAS_SCL_FILE_TYPE.getCompasClass();
        // When & Then
        assertThatThrownBy(() -> PrivateService.extractCompasPrivate(privateSCD, compasClass)).isInstanceOf(
            ScdException.class);
    }

    @Test
    void extractCompasPrivate_on_base_element_should_return_private_value() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.getPrivate().add(privateSCD);
        // When
        Optional<TCompasSclFileType> result = PrivateService.extractCompasPrivate(baseElement,
            TCompasSclFileType.class);
        //Then
        assertThat(result).isPresent()
            .hasValue(TCompasSclFileType.SCD);
    }

    @Test
    void extractCompasPrivate_on_base_element_should_return_empty() {
        // Given
        TBaseElement baseElement = new SCL();
        // When
        Optional<TCompasBay> result = PrivateService.extractCompasPrivate(baseElement,
            TCompasBay.class);
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
        assertThatThrownBy(() -> PrivateService.extractCompasPrivate(baseElement, TCompasSclFileType.class)).isInstanceOf(
            ScdException.class);
    }

    @Test
    void extractCompasICDHeader_should_return_private_value() {
        // Given
        privateSCD = objectFactory.createTPrivate();
        privateSCD.setType(PrivateEnum.COMPAS_ICDHEADER.getPrivateType());
        TCompasICDHeader tCompasICDHeader = objectFactory.createTCompasICDHeader();
        privateSCD.getContent().add(objectFactory.createICDHeader(tCompasICDHeader));
        // When
        Optional<TCompasICDHeader> optionalResult = PrivateService.extractCompasICDHeader(privateSCD);
        //Then
        assertThat(optionalResult).isPresent().get().matches(result -> result == tCompasICDHeader);
    }

    @ParameterizedTest
    @MethodSource("createPrivateTestSources")
    void createPrivate_should_return_private_new_private(Object compasElement) throws InvocationTargetException, IllegalAccessException {
        // Given
        PrivateEnum privateEnum = PrivateEnum.fromClass(compasElement.getClass());
        assertThat(privateEnum).isNotNull();
        Optional<Method> optionalCreatePrivateMethod = ReflectionSupport.findMethod(PrivateService.class, "createPrivate", compasElement.getClass());
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
        JAXBElement<?> content = (JAXBElement<?>) resultPrivate.getContent().get(0);
        assertThat(content.isNil()).isFalse();
        assertThat(content.getValue()).isNotNull().isInstanceOf(compasElement.getClass())
            .isEqualTo(compasElement);
    }

    public static Stream<Object> createPrivateTestSources() {
        return Stream.of(new TCompasBay(),
            new TCompasCriteria(),
            new TCompasFlow(),
            new TCompasFunction(),
            new TCompasICDHeader(),
            new TCompasLDevice(),
            TCompasSclFileType.SCD,
            new TCompasSystemVersion());
    }

    @Test
    void removePrivates_should_remove_privates() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.getPrivate().add(privateSCD);
        // When
        PrivateService.removePrivates(baseElement, PrivateEnum.COMPAS_SCL_FILE_TYPE);
        // Then
        assertThat(baseElement.isSetPrivate()).isFalse();
    }

    @Test
    void removePrivates_should_do_nothing_when_no_private_match_type() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.getPrivate().add(privateSCD);
        // When
        PrivateService.removePrivates(baseElement, PrivateEnum.COMPAS_ICDHEADER);
        // Then
        assertThat(baseElement.getPrivate()).hasSize(1);
    }

    @Test
    void removePrivates_should_remove_privates_of_given_type() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.getPrivate().add(privateSCD);
        TCompasICDHeader tCompasICDHeader = objectFactory.createTCompasICDHeader();
        baseElement.getPrivate().add(PrivateService.createPrivate(tCompasICDHeader));
        // When
        PrivateService.removePrivates(baseElement, PrivateEnum.COMPAS_ICDHEADER);
        // Then
        assertThat(baseElement.getPrivate()).hasSize(1);
        TPrivate tPrivate = baseElement.getPrivate().get(0);
        assertThat(tPrivate.getType()).isEqualTo(privateSCD.getType());
        assertThat(tPrivate.getContent()).hasSize(1).first().isInstanceOf(JAXBElement.class);
        JAXBElement<?> jaxbElement = (JAXBElement<?>) tPrivate.getContent().get(0);
        assertThat(jaxbElement.isNil()).isFalse();
        assertThat(jaxbElement.getValue()).isEqualTo(TCompasSclFileType.SCD);
    }

    @Test
    void removePrivates_should_not_set_private() {
        // Given : setUp
        TBaseElement baseElement = new SCL();
        baseElement.unsetPrivate();
        // When
        PrivateService.removePrivates(baseElement, PrivateEnum.COMPAS_ICDHEADER);
        // Then
        assertThat(baseElement.isSetPrivate()).isFalse();
    }

    @Test
    void createMapICDSystemVersionUuidAndSTDFile_Should_return_empty_map_when_no_ICDSystemVersionUUID() {
        //Given
        SCL scl1 = new SCL();
        TIED tied1 = new TIED();
        TCompasICDHeader compasICDHeader1 = new TCompasICDHeader();
        TPrivate tPrivate1 =  PrivateService.createPrivate(compasICDHeader1);
        tied1.getPrivate().add(tPrivate1);
        scl1.getIED().add(tied1);

        //When
        Map<String, PrivateService.PrivateLinkedToSTDs> stringSCLMap = PrivateService.createMapICDSystemVersionUuidAndSTDFile(Set.of(scl1));

        //Then
        assertThat(stringSCLMap.keySet()).isEmpty();

    }

    @Test
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
        TPrivate tPrivate1 =  PrivateService.createPrivate(compasICDHeader1);
        TPrivate tPrivate2 =  PrivateService.createPrivate(compasICDHeader2);
        TPrivate tPrivate3 =  PrivateService.createPrivate(compasICDHeader3);
        tied1.getPrivate().add(tPrivate1);
        tied2.getPrivate().add(tPrivate2);
        tied3.getPrivate().add(tPrivate3);
        scl1.getIED().add(tied1);
        scl2.getIED().add(tied2);
        scl3.getIED().add(tied3);

        //When
        Map<String, PrivateService.PrivateLinkedToSTDs> stringSCLMap = PrivateService.createMapICDSystemVersionUuidAndSTDFile(Set.of(scl1,scl2,scl3));

        //Then
        assertThat(stringSCLMap.keySet()).hasSize(2).containsExactly("UUID-1", "UUID-2");
        assertThat(stringSCLMap.get("UUID-2").stdList()).hasSize(2);
    }

    @Test
    void checkSTDCorrespondanceWithLNodeCompasICDHeadershoul_throw_scdEception(){
        //Given
        TCompasICDHeader compasICDHeader1 = new TCompasICDHeader();
        compasICDHeader1.setICDSystemVersionUUID("UUID-1");
        TCompasICDHeader compasICDHeader2 = new TCompasICDHeader();
        compasICDHeader2.setICDSystemVersionUUID("UUID-2");
        compasICDHeader2.setHeaderId("ID-2");
        compasICDHeader2.setHeaderVersion("VER-2");
        compasICDHeader2.setHeaderRevision("REV-2");
        TPrivate tPrivate1 =  PrivateService.createPrivate(compasICDHeader1);
        TPrivate tPrivate2 =  PrivateService.createPrivate(compasICDHeader2);


        PrivateService.PrivateLinkedToSTDs privateLinkedToSTDs1 = new PrivateService.PrivateLinkedToSTDs(tPrivate1,Collections.singletonList(new SCL()));
        PrivateService.PrivateLinkedToSTDs privateLinkedToSTDs2 = new PrivateService.PrivateLinkedToSTDs(tPrivate2, Arrays.asList(new SCL(), new SCL()));


        Map<String, PrivateService.PrivateLinkedToSTDs> stringSCLMap = new HashMap<>();
        stringSCLMap.put("UUID-1", privateLinkedToSTDs1);
        stringSCLMap.put("UUID-2", privateLinkedToSTDs2);

        //When Then
        assertThatThrownBy(() -> PrivateService.checkSTDCorrespondanceWithLNodeCompasICDHeader(stringSCLMap))
                .isInstanceOf(ScdException.class)
                .hasMessage("There are several STD files corresponding to headerId = ID-2 headerVersion = VER-2 headerRevision = REV-2 and ICDSystemVersionUUID = UUID-2");

    }

    @Test
    void checkSTDCorrespondanceWithLNodeCompasICDHeader_should_pass(){
        //Given
        TCompasICDHeader compasICDHeader1 = new TCompasICDHeader();
        compasICDHeader1.setICDSystemVersionUUID("UUID-1");
        TCompasICDHeader compasICDHeader2 = new TCompasICDHeader();
        compasICDHeader2.setICDSystemVersionUUID("UUID-2");
        TPrivate tPrivate1 =  PrivateService.createPrivate(compasICDHeader1);
        TPrivate tPrivate2 =  PrivateService.createPrivate(compasICDHeader2);


        PrivateService.PrivateLinkedToSTDs privateLinkedToSTDs1 = new PrivateService.PrivateLinkedToSTDs(tPrivate1,Collections.singletonList(new SCL()));
        PrivateService.PrivateLinkedToSTDs privateLinkedToSTDs2 = new PrivateService.PrivateLinkedToSTDs(tPrivate2, Collections.singletonList(new SCL()));


        Map<String, PrivateService.PrivateLinkedToSTDs> stringSCLMap = new HashMap<>();
        stringSCLMap.put("UUID-1", privateLinkedToSTDs1);
        stringSCLMap.put("UUID-2", privateLinkedToSTDs2);

        //When Then
        assertDoesNotThrow(() -> PrivateService.checkSTDCorrespondanceWithLNodeCompasICDHeader(stringSCLMap));

    }

    @Test
    void stdCheckFormatExceptionMessage_should_return_formatted_message_with_Private_data() {
        //Given
        TCompasICDHeader compasICDHeader = new TCompasICDHeader();
        compasICDHeader.setHeaderId("ID-1");
        compasICDHeader.setHeaderVersion("VER-1");
        compasICDHeader.setICDSystemVersionUUID("UUID-1");
        TPrivate tPrivate =  PrivateService.createPrivate(compasICDHeader);

        //When
        String message = PrivateService.stdCheckFormatExceptionMessage(tPrivate);

        //Then
        assertThat(message).isEqualTo("headerId = ID-1 headerVersion = VER-1 headerRevision = null and ICDSystemVersionUUID = UUID-1");

    }

    @Test
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
        TPrivate tPrivate1 =  PrivateService.createPrivate(compasICDHeader1);
        TPrivate tPrivate2 =  PrivateService.createPrivate(compasICDHeader2);
        TPrivate tPrivate3 =  PrivateService.createPrivate(compasICDHeader3);
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
        SclRootAdapter sclRootAdapter = SclService.initScl(Optional.empty(), "hv", "hr");
        sclRootAdapter.setCurrentElem(scl);
        Stream<TPrivate> tPrivateStream = PrivateService.createMapIEDNameAndPrivate(sclRootAdapter);

        //Then
        assertThat(tPrivateStream.toList()).hasSize(3)
                .extracting(tPrivate -> PrivateService.extractCompasICDHeader(tPrivate).get().getIEDName())
                .containsExactlyInAnyOrder("IED-1", "IED-2", "IED-3");
    }

    @Test
    void createMapIEDNameAndPrivate_should_return_empty_map_when_no_compasicdheader_present_under_substation() {
        //Given
        SCL scl = new SCL();
        TLNode tlNode1 = new TLNode();
        TCompasBay compasBay = new TCompasBay();
        compasBay.setUUID("UUID");
        TPrivate tPrivate1 =  PrivateService.createPrivate(compasBay);
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
        SclRootAdapter sclRootAdapter = SclService.initScl(Optional.empty(), "hv", "hr");
        sclRootAdapter.setCurrentElem(scl);
        Stream<TPrivate> tPrivateStream = PrivateService.createMapIEDNameAndPrivate(sclRootAdapter);

        //Then
        assertThat(tPrivateStream.toList()).isEmpty();
    }

    @Test
    void comparePrivateCompasICDHeaders_should_return_true_equality_not_check_for_IEDNane_BayLabel_IEDinstance() {
        // Given
        TCompasICDHeader compasICDHeader1 = new TCompasICDHeader();
        compasICDHeader1.setIEDName("IED-1");
        compasICDHeader1.setBayLabel("BAY-1");
        compasICDHeader1.setIEDinstance("1");
        TCompasICDHeader compasICDHeader2 = new TCompasICDHeader();
        TPrivate tPrivate1 =  PrivateService.createPrivate(compasICDHeader1);
        TPrivate tPrivate2 =  PrivateService.createPrivate(compasICDHeader2);

        // When
        boolean result = PrivateService.comparePrivateCompasICDHeaders(tPrivate1,tPrivate2);
        // Then
        assertThat(result).isTrue();
    }

    @Test
    void comparePrivateCompasICDHeaders_should_return_false_equality_not_check_for_IEDNane_BayLabel_IEDinstance() {
        // Given
        TCompasICDHeader compasICDHeader1 = new TCompasICDHeader();
        compasICDHeader1.setIEDName("IED-1");
        compasICDHeader1.setBayLabel("BAY-1");
        compasICDHeader1.setIEDinstance("1");
        compasICDHeader1.setICDSystemVersionUUID("UUID-1");
        TCompasICDHeader compasICDHeader2 = new TCompasICDHeader();
        compasICDHeader2.setICDSystemVersionUUID("UUID-2");
        TPrivate tPrivate1 =  PrivateService.createPrivate(compasICDHeader1);
        TPrivate tPrivate2 =  PrivateService.createPrivate(compasICDHeader2);

        // When
        boolean result = PrivateService.comparePrivateCompasICDHeaders(tPrivate1,tPrivate2);
        // Then
        assertThat(result).isFalse();
    }

    @Test
    void comparePrivateCompasICDHeaders_should_return_true() {
        // Given
        TCompasICDHeader compasICDHeader1 = new TCompasICDHeader();
        compasICDHeader1.setIEDName("IED-1");
        compasICDHeader1.setBayLabel("BAY-1");
        compasICDHeader1.setIEDinstance("1");
        compasICDHeader1.setICDSystemVersionUUID("UUID-1");
        TCompasICDHeader compasICDHeader2 = new TCompasICDHeader();
        compasICDHeader2.setICDSystemVersionUUID("UUID-1");
        TPrivate tPrivate1 =  PrivateService.createPrivate(compasICDHeader1);
        TPrivate tPrivate2 =  PrivateService.createPrivate(compasICDHeader2);

        // When
        boolean result = PrivateService.comparePrivateCompasICDHeaders(tPrivate1,tPrivate2);
        // Then
        assertThat(result).isTrue();
    }

    @Test
    void copyCompasICDHeaderFromLNodePrivateIntoSTDPrivate() {
        // Given
        TCompasICDHeader stdCompasICDHeader = new TCompasICDHeader();
        stdCompasICDHeader.setICDSystemVersionUUID("UUID-1");
        TCompasICDHeader lNodeCompasICDHeader = new TCompasICDHeader();
        lNodeCompasICDHeader.setICDSystemVersionUUID("UUID-2");
        lNodeCompasICDHeader.setIEDName("IED-1");
        lNodeCompasICDHeader.setBayLabel("BAY-1");
        lNodeCompasICDHeader.setIEDinstance("1");
        TPrivate stdTPrivate =  PrivateService.createPrivate(stdCompasICDHeader);
        TPrivate lNodePrivate =  PrivateService.createPrivate(lNodeCompasICDHeader);

        // When
        assertThat(stdTPrivate).isNotEqualTo(lNodePrivate);
        PrivateService.copyCompasICDHeaderFromLNodePrivateIntoSTDPrivate(stdTPrivate,lNodePrivate);
        // Then
        TCompasICDHeader result = PrivateService.extractCompasICDHeader(stdTPrivate).get();
        assertThat(result).extracting(TCompasICDHeader::getICDSystemVersionUUID, TCompasICDHeader::getIEDName,
                TCompasICDHeader::getIEDinstance, TCompasICDHeader::getBayLabel)
                .containsExactlyInAnyOrder("UUID-2", "IED-1", "1", "BAY-1");
    }
}
