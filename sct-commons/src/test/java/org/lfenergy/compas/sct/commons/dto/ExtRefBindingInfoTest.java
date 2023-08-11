// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.DOTypeAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.lfenergy.compas.sct.commons.dto.DTO.P_DA;
import static org.lfenergy.compas.sct.commons.dto.DTO.P_DO;

class ExtRefBindingInfoTest {

    @Test
    @Tag("issue-321")
    void constructor_whenCalled_shouldFillValues() {
        ExtRefBindingInfo bindingInfo = DTO.createExtRefBindingInfo_Remote();
        ExtRefBindingInfo bindingInfo_bis = DTO.createExtRefBindingInfo_Remote();
        // When
        ExtRefBindingInfo bindingInfo_ter = new ExtRefBindingInfo(DTO.createExtRef());
        // When
        ExtRefBindingInfo bindingInfo_qt = new ExtRefBindingInfo();

        // Then
        assertThat(bindingInfo_ter).isEqualTo(bindingInfo);
        assertThat(bindingInfo_bis).isEqualTo(bindingInfo);
        assertThat(bindingInfo).isNotNull();
        assertThat(bindingInfo_qt).isNotEqualTo(bindingInfo);
        bindingInfo_qt.setDaName(bindingInfo.getDaName());
        assertThat(bindingInfo_qt).isNotEqualTo(bindingInfo);

        bindingInfo_qt.setDoName(bindingInfo.getDoName());
        assertThat(bindingInfo_qt).isNotEqualTo(bindingInfo);

        bindingInfo_qt.setIedName(bindingInfo.getIedName());
        assertThat(bindingInfo_qt).isNotEqualTo(bindingInfo);

        bindingInfo_qt.setLdInst(bindingInfo.getLdInst());
        assertThat(bindingInfo_qt).isNotEqualTo(bindingInfo);

        bindingInfo_qt.setLnInst(bindingInfo.getLnInst());
        assertThat(bindingInfo_qt).isNotEqualTo(bindingInfo);

        bindingInfo_qt.setLnClass(bindingInfo.getLnClass());
        assertThat(bindingInfo_qt).isNotEqualTo(bindingInfo);

        bindingInfo_qt.setPrefix(bindingInfo.getPrefix());
        assertThat(bindingInfo_qt).isNotEqualTo(bindingInfo);

        bindingInfo_qt.setServiceType(bindingInfo.getServiceType());
        assertThat(bindingInfo_qt).hasSameHashCodeAs(bindingInfo);
    }

    @Test
    @Tag("issue-321")
    void testIsValid() {
        // Given
        ExtRefBindingInfo bindingInfo = DTO.createExtRefBindingInfo_Remote();
        // When Then
        assertThat(bindingInfo.isValid()).isTrue();
        bindingInfo.setLnClass("PIOC");
        bindingInfo.setLnInst("");
        // When Then
        assertThat(bindingInfo.isValid()).isFalse();
        bindingInfo.setDoName(new DoTypeName("do.sdo1.sdoError"));
        // When Then
        assertThat(bindingInfo.isValid()).isFalse();
        bindingInfo.setDaName(new DaTypeName(""));
        // When Then
        assertThat(bindingInfo.isValid()).isFalse();
        bindingInfo.setLnClass("");
        // When Then
        assertThat(bindingInfo.isValid()).isFalse();
        bindingInfo.setLdInst("");
        // When Then
        assertThat(bindingInfo.isValid()).isFalse();
        bindingInfo.setIedName("");
        // When Then
        assertThat(bindingInfo.isValid()).isFalse();
    }

    @Test
    @Tag("issue-321")
    void testIsWrappedIn() {
        // Given
        TExtRef extRef = DTO.createExtRef();
        ExtRefBindingInfo bindingInfo = new ExtRefBindingInfo();
        // When Then
        assertThat(bindingInfo.isWrappedIn(extRef)).isFalse();

        bindingInfo.setIedName(extRef.getIedName());
        // When Then
        assertThat(bindingInfo.isWrappedIn(extRef)).isFalse();

        bindingInfo.setLdInst(extRef.getLdInst());
        // When Then
        assertThat(bindingInfo.isWrappedIn(extRef)).isFalse();

        if (!extRef.getLnClass().isEmpty()) {
            bindingInfo.setLnClass(extRef.getLnClass().get(0));
            // When Then
            assertThat(bindingInfo.isWrappedIn(extRef)).isFalse();

            bindingInfo.setLnInst(extRef.getLnInst());
            // When Then
            assertThat(bindingInfo.isWrappedIn(extRef)).isFalse();

            bindingInfo.setPrefix(extRef.getPrefix());
            // When Then
            assertThat(bindingInfo.isWrappedIn(extRef)).isFalse();
        }
        bindingInfo.setServiceType(extRef.getServiceType());
        // When Then
        assertThat(bindingInfo.isWrappedIn(extRef)).isTrue();
    }

    @Test
    @Tag("issue-321")
    void testIsNull() {
        // Given
        ExtRefBindingInfo bindingInfo = new ExtRefBindingInfo();
        // When Then
        assertThat(bindingInfo.isNull()).isTrue();

        bindingInfo.setServiceType(TServiceType.REPORT);
        // When Then
        assertThat(bindingInfo).isNotNull();
    }

    @Test
    void compareTo_when_ExtRefBindingInfo_the_same_should_return_0() {
        // Given
        ExtRefBindingInfo bindingInfo1 = DTO.createExtRefBindingInfo_Remote();
        ExtRefBindingInfo bindingInfo2 = DTO.createExtRefBindingInfo_Remote();

        // When
        int result = bindingInfo1.compareTo(bindingInfo2);

        // Then
        assertThat(result).isZero();
    }

    @Test
    void compareTo_when_first_ExtRefBindingInfo_is_major_in_alphabetical_order_should_return_positive_int() {
        // Given
        ExtRefBindingInfo bindingInfo1 = DTO.createExtRefBindingInfo_Remote();
        ExtRefBindingInfo bindingInfo2 = DTO.createExtRefBindingInfo_Source();

        // When
        int result = bindingInfo1.compareTo(bindingInfo2);

        // Then
        assertThat(result).isPositive();
    }

    @Test
    void compareTo_when_first_ExtRefBindingInfo_is_minor_in_alphabetical_order_should_return_negative_int() {
        // Given
        ExtRefBindingInfo bindingInfo1 = DTO.createExtRefBindingInfo_Source();
        ExtRefBindingInfo bindingInfo2 = DTO.createExtRefBindingInfo_Remote();

        // When
        int result = bindingInfo1.compareTo(bindingInfo2);

        // Then
        assertThat(result).isNegative();
    }

    @Test
    @Tag("issue-321")
    void findAndUpdateDAInfos_whenDAUnknown_shouldThrowException() {
        //Given
        ExtRefBindingInfo bindingInfo = DTO.createExtRefBindingInfo_Source();
        ExtRefSignalInfo signalInfo = DTO.createExtRefSignalInfo();

        SclRootAdapter sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        sclRootAdapter.getCurrentElem().setDataTypeTemplates(new TDataTypeTemplates());
        //When Then
        DataTypeTemplateAdapter dttAdapter = assertDoesNotThrow(sclRootAdapter::getDataTypeTemplateAdapter);

        //Given
        TDA tda = new TDA();
        tda.setName(P_DA);
        tda.setFc(TFCEnum.CF);
        tda.setBType(TPredefinedBasicTypeEnum.FLOAT_32);

        TDO tdo= new TDO();
        tdo.setName(P_DO);
        tdo.setType("DO1");

        TDOType tdoType = new TDOType();
        tdoType.setId("DO1");
        tdoType.getSDOOrDA().add(tda);

        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("ID");
        tlNodeType.getDO().add(tdo);
        dttAdapter.getCurrentElem().getDOType().add(tdoType);
        DataTypeTemplateAdapter.DOTypeInfo doTypeInfo = new DataTypeTemplateAdapter.DOTypeInfo(new DoTypeName(P_DO), "ID",
                dttAdapter.getDOTypeAdapterById("DO1").get());
        //When Then
        assertThatThrownBy(() -> bindingInfo.updateDAInfos(signalInfo,doTypeInfo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(P_DA+": Unknown DA ("+P_DA+"), or no reference to its type");
    }

    @Test
    @Tag("issue-321")
    void findAndUpdateDAInfos_whenDANotCoherentWithDO_shouldThrowException() {
        //Given
        ExtRefBindingInfo bindingInfo = DTO.createExtRefBindingInfo_Source();
        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setPDO("P_DO");
        signalInfo.setPDA("P_DA");
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        sclRootAdapter.getCurrentElem().setDataTypeTemplates(new TDataTypeTemplates());
        //When Then
        DataTypeTemplateAdapter dttAdapter = assertDoesNotThrow(sclRootAdapter::getDataTypeTemplateAdapter);

        //Given
        TDA tda = new TDA();
        tda.setName("P_DA");
        tda.setFc(TFCEnum.CF);
        tda.setBType(TPredefinedBasicTypeEnum.STRUCT);

        TDO tdo= new TDO();
        tdo.setName("P_DO");
        tdo.setType("DO1");

        TDOType tdoType = new TDOType();
        tdoType.setId("DO1");
        tdoType.getSDOOrDA().add(tda);

        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("ID");
        tlNodeType.getDO().add(tdo);

        dttAdapter.getCurrentElem().getDOType().add(tdoType);
        dttAdapter.getCurrentElem().getLNodeType().add(tlNodeType);

        DOTypeAdapter doTypeAdapter = dttAdapter.getDOTypeAdapterById("DO1").get();
        DataTypeTemplateAdapter.DOTypeInfo doTypeInfo = new DataTypeTemplateAdapter.DOTypeInfo(new DoTypeName("P_DO"),
                "ID", doTypeAdapter);
        //When Then
        assertThatThrownBy(() -> bindingInfo.updateDAInfos(signalInfo,doTypeInfo))
                .isInstanceOf(ScdException.class)
                .hasMessage("Invalid ExtRef signal: no coherence between pDO(P_DO) and pDA(P_DA)");
    }
}