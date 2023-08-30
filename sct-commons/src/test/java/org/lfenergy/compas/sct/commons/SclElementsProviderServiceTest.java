// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.*;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.lfenergy.compas.sct.commons.testhelpers.DataTypeUtils.createDa;
import static org.lfenergy.compas.sct.commons.testhelpers.DataTypeUtils.createDo;

@ExtendWith(MockitoExtension.class)
class SclElementsProviderServiceTest {

    @InjectMocks
    SclElementsProviderService sclElementsProviderService;

    @Test
    void getExtRefBinders_shouldThrowScdException_whenExtRefNotExist() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/scd_get_binders_test.xml");

        ExtRefSignalInfo signalInfo = createSignalInfo("Do11.sdo11", "da11.bda111.bda112.bda113", "INT_ADDR11");
        signalInfo.setPLN("ANCR");
        //When Then
        assertThatThrownBy(
                () -> sclElementsProviderService.getExtRefBinders(scd, "IED_NAME1", "UNKNOWN_LD", "LLN0", "", "", signalInfo))
                .isInstanceOf(ScdException.class);
    }

    @Test
    void getExtRefBinders_shouldReturnSortedListBindingInfo_whenExtRefAndDOExist() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/scd_get_binders_test.xml");

        ExtRefSignalInfo signalInfo = createSignalInfo(
                "Do11.sdo11", "da11.bda111.bda112.bda113", "INT_ADDR11"
        );
        signalInfo.setPLN("ANCR");

        // When
        List<ExtRefBindingInfo> potentialBinders = sclElementsProviderService.getExtRefBinders(scd, "IED_NAME1", "LD_INST11", "LLN0", "", "", signalInfo);

        // Then
        assertThat(potentialBinders).hasSize(4);
        assertThat(potentialBinders)
                .extracting(ExtRefBindingInfo::getIedName)
                .containsExactly("IED_NAME1", "IED_NAME1", "IED_NAME2", "IED_NAME3");
        assertThat(potentialBinders)
                .extracting(ExtRefBindingInfo::getLdInst)
                .containsExactly("LD_INST11", "LD_INST12", "LD_INST22", "LD_INST31");
        assertThat(potentialBinders)
                .extracting(ExtRefBindingInfo::getLnClass)
                .containsExactly("ANCR", "ANCR", "ANCR", "ANCR");
        assertThat(potentialBinders)
                .extracting(ExtRefBindingInfo::getLnInst)
                .containsExactly("1", "1", "2", "3");
    }

    @Test
    void getExtRefSourceInfo_shouldReturnEmptyList_whenExtRefMatchNoFCDA() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/scd_get_cbs_test.xml");
        String iedName = "IED_NAME2";
        String ldInst = "LD_INST21";
        String lnClass = TLLN0Enum.LLN_0.value();
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iedAdapter.findLDeviceAdapterByLdInst(ldInst).get());
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        List<TExtRef> extRefs = ln0Adapter.getExtRefs(null);
        assertFalse(extRefs.isEmpty());

        ExtRefInfo extRefInfo = new ExtRefInfo(extRefs.get(0));

        extRefInfo.setHolderIEDName(iedName);
        extRefInfo.setHolderLDInst(ldInst);
        extRefInfo.setHolderLnClass(lnClass);

        //When
        List<ControlBlock> controlBlocks = sclElementsProviderService.getExtRefSourceInfo(scd, extRefInfo);

        //Then
        assertThat(controlBlocks).isEmpty();
    }

    @Test
    void getExtRefSourceInfo_shouldReturnListOfControlBlocks_whenExtRefMatchFCDA() {
        //Given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-scd-extref-cb/issue_175_scd_get_cbs_test.xml");
        String iedName = "IED_NAME2";
        String ldInst = "LD_INST21";
        String lnClass = TLLN0Enum.LLN_0.value();
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(() -> iedAdapter.findLDeviceAdapterByLdInst(ldInst).get());
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        List<TExtRef> extRefs = ln0Adapter.getExtRefs(null);
        assertFalse(extRefs.isEmpty());

        ExtRefInfo extRefInfo = new ExtRefInfo(extRefs.get(0));

        extRefInfo.setHolderIEDName(iedName);
        extRefInfo.setHolderLDInst(ldInst);
        extRefInfo.setHolderLnClass(lnClass);

        //When
        List<ControlBlock> controlBlocks = sclElementsProviderService.getExtRefSourceInfo(scd, extRefInfo);

        //Then
        assertThat(controlBlocks).hasSize(1);
        assertThat(controlBlocks.get(0).getName()).isEqualTo("goose2");
    }

    private ExtRefSignalInfo createSignalInfo(String pDO, String pDA, String intAddr) {

        final String DESC = "DESC";
        final String P_LN = TLLN0Enum.LLN_0.value();
        final String P_SERV_T = "Report";

        ExtRefSignalInfo signalInfo = new ExtRefSignalInfo();
        signalInfo.setDesc(DESC);
        signalInfo.setPDA(pDA);
        signalInfo.setPDO(pDO);
        signalInfo.setPLN(P_LN);
        signalInfo.setPServT(TServiceType.fromValue(P_SERV_T));
        signalInfo.setIntAddr(intAddr);

        return signalInfo;
    }

    @Test
    void getDAI_should_return_all_dai() {
        // given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");

        // when
        Set<DataAttributeRef> allResults = sclElementsProviderService.getDAI(scd, "IED_NAME1", "LD_INST12", new DataAttributeRef(), true);

        // then
        assertThat(allResults).hasSize(733);

        List<DataAttributeRef> resultsWithDa = allResults.stream().filter(rdt -> StringUtils.isNotBlank(rdt.getDaRef())).collect(Collectors.toList());
        assertThat(resultsWithDa).hasSize(733);

        List<DataAttributeRef> resultsWithNoBda = allResults.stream().filter(rdt -> rdt.getBdaNames().isEmpty()).collect(Collectors.toList());
        assertThat(resultsWithNoBda).hasSize(3);
        List<DataAttributeRef> resultsWithBdaDepth1 = allResults.stream().filter(rdt -> rdt.getBdaNames().size() == 1).collect(Collectors.toList());
        assertThat(resultsWithBdaDepth1).isEmpty();
        List<DataAttributeRef> resultsWithBdaDepth2 = allResults.stream().filter(rdt -> rdt.getBdaNames().size() == 2).collect(Collectors.toList());
        assertThat(resultsWithBdaDepth2).hasSize(1);
        List<DataAttributeRef> resultsWithBdaDepth3 = allResults.stream().filter(rdt -> rdt.getBdaNames().size() == 3).collect(Collectors.toList());
        assertThat(resultsWithBdaDepth3).hasSize(729);


        List<DataAttributeRef> resultsWithDo = allResults.stream().filter(rdt -> StringUtils.isNotBlank(rdt.getDoRef())).collect(Collectors.toList());
        assertThat(resultsWithDo).hasSize(733);

        List<DataAttributeRef> resultsWithNoSdo = allResults.stream().filter(rdt -> rdt.getSdoNames().isEmpty()).collect(Collectors.toList());
        assertThat(resultsWithNoSdo).hasSize(3);
        List<DataAttributeRef> resultsWithSdoDepth1 = allResults.stream().filter(rdt -> rdt.getSdoNames().size() == 1).collect(Collectors.toList());
        assertThat(resultsWithSdoDepth1).isEmpty();
        List<DataAttributeRef> resultsWithSdoDepth2 = allResults.stream().filter(rdt -> rdt.getSdoNames().size() == 2).collect(Collectors.toList());
        assertThat(resultsWithSdoDepth2).hasSize(730);
        List<DataAttributeRef> resultsWithSdoDepth3 = allResults.stream().filter(rdt -> rdt.getSdoNames().size() == 3).collect(Collectors.toList());
        assertThat(resultsWithSdoDepth3).isEmpty();
    }

    @Test
    void getDAI_should_aggregate_attribute_from_DAI() {
        // given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_test_aggregate_DAI.xml");

        // when
        Set<DataAttributeRef> dais = sclElementsProviderService.getDAI(scd, "VirtualBCU", "LDMODEXPF", new DataAttributeRef(), false);

        // then
        DataAttributeRef lln0 = DataAttributeRef.builder().prefix("").lnType("lntype1").lnClass("LLN0").lnInst("").build();
        DataAttributeRef lln0DoA = lln0.toBuilder().doName(createDo("DoA", TPredefinedCDCEnum.DPL)).build();
        DataAttributeRef lln0DoB = lln0.toBuilder().doName(createDo("DoB", TPredefinedCDCEnum.ACD)).build();

        assertThat(dais).containsExactlyInAnyOrder(
                lln0DoA.toBuilder().daName(createDa("daNotInDai", TFCEnum.CF, false, Map.of(0L, "0"))).build(),
                lln0DoA.toBuilder().daName(createDa("daNotInDai2", TFCEnum.CF, true, Map.of())).build(),
                lln0DoA.toBuilder().daName(createDa("daiOverrideVal", TFCEnum.CF, false, Map.of(0L, "1"))).build(),
                lln0DoA.toBuilder().daName(createDa("daiOverrideValImport", TFCEnum.CF, true, Map.of())).build(),
                lln0DoA.toBuilder().daName(createDa("daiOverrideValImport2", TFCEnum.CF, false, Map.of())).build(),

                lln0DoB.toBuilder().daName(createDa("structDa.daNotInDai", TFCEnum.ST, false, Map.of(0L, "0"))).build(),
                lln0DoB.toBuilder().daName(createDa("structDa.daNotInDai2", TFCEnum.ST, true, Map.of())).build(),
                lln0DoB.toBuilder().daName(createDa("structDa.daiOverrideVal", TFCEnum.ST, false, Map.of(0L, "1"))).build(),
                lln0DoB.toBuilder().daName(createDa("structDa.daiOverrideValImport", TFCEnum.ST, true, Map.of())).build(),
                lln0DoB.toBuilder().daName(createDa("structDa.daiOverrideValImport2", TFCEnum.ST, false, Map.of())).build(),

                DataAttributeRef.builder().prefix("").lnType("lntype2").lnClass("LPHD").lnInst("0")
                        .doName(createDo("PhyNam", TPredefinedCDCEnum.DPS))
                        .daName(createDa("aDa", TFCEnum.BL, false, Map.of())).build()
        );
    }

    @Test
    void getDAI_when_LDevice_not_found_should_throw_exception() {
        // given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");

        // when & then
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        assertThrows(ScdException.class,
                () -> sclElementsProviderService.getDAI(scd, "IED_NAME1", "UNKNOWNLD", dataAttributeRef, true));
    }

    @Test
    void getDAI_should_filter_updatable_DA() {
        // given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_test_updatable_DAI.xml");

        // when
        Set<DataAttributeRef> dais = sclElementsProviderService.getDAI(scd, "VirtualBCU", "LDMODEXPF", new DataAttributeRef(), true);

        // then
        assertThat(dais).isNotNull();
        List<String> resultSimpleDa = dais.stream()
                .filter(dataAttributeRef -> dataAttributeRef.getBdaNames().isEmpty()) // test only simple DA
                .map(DataAttributeRef::getLNRef).collect(Collectors.toList());
        assertThat(resultSimpleDa).containsExactlyInAnyOrder(
                // ...AndTrueInDai : If ValImport is True in DAI, DA is updatable
                "LLN0.DoA.valImportNotSetAndTrueInDai",
                "LLN0.DoA.valImportTrueAndTrueInDai",
                "LLN0.DoA.valImportFalseAndTrueInDai",
                // valImportTrue : If ValImport is True in DA and DAI does not exist, DA is updatable
                "LLN0.DoA.valImportTrue",
                // valImportTrueAndNotSetInDai : If ValImport is True in DA and DAI exists but DAI ValImport is not set, DA is updatable
                "LLN0.DoA.valImportTrueAndNotSetInDai",
                // Only these FC are updatable
                "LLN0.DoA.fcCF",
                "LLN0.DoA.fcDC",
                "LLN0.DoA.fcSG",
                "LLN0.DoA.fcSP",
                "LLN0.DoA.fcST",
                "LLN0.DoA.fcSE"
        );
    }

    @Test
    void getDAI_should_filter_updatable_BDA() {
        // given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_test_updatable_DAI.xml");

        // when
        Set<DataAttributeRef> dais = sclElementsProviderService.getDAI(scd, "VirtualBCU", "LDMODEXPF", new DataAttributeRef(), true);

        // then
        assertThat(dais).isNotNull();
        List<String> resultStructDa = dais.stream()
                .filter(dataAttributeRef -> !dataAttributeRef.getBdaNames().isEmpty()) // test only struct DA
                .map(DataAttributeRef::getLNRef).collect(Collectors.toList());
        assertThat(resultStructDa).containsExactlyInAnyOrder(
                // ...AndTrueInDai : If ValImport is True in DAI, BDA is updatable
                "LLN0.DoB.structValImportNotSet.bValImportFalseAndTrueInDai",
                "LLN0.DoB.structValImportNotSet.bValImportNotSetAndTrueInDai",
                "LLN0.DoB.structValImportNotSet.bValImportTrueAndTrueInDai",
                "LLN0.DoB.structValImportTrue.bValImportFalseAndTrueInDai",
                "LLN0.DoB.structValImportTrue.bValImportNotSetAndTrueInDai",
                "LLN0.DoB.structValImportTrue.bValImportTrueAndTrueInDai",
                "LLN0.DoB.structValImportFalse.bValImportFalseAndTrueInDai",
                "LLN0.DoB.structValImportFalse.bValImportNotSetAndTrueInDai",
                "LLN0.DoB.structValImportFalse.bValImportTrueAndTrueInDai",
                // bValImportTrue : If ValImport is True in BDA and DAI does not exist, BDA is updatable
                "LLN0.DoB.structValImportFalse.bValImportTrue",
                "LLN0.DoB.structValImportTrue.bValImportTrue",
                "LLN0.DoB.structValImportNotSet.bValImportTrue",
                // bValImportTrueAndNotSetInDai : If ValImport is True in BDA and DAI exists but DAI ValImport is not set, BDA is updatable
                "LLN0.DoB.structValImportTrue.bValImportTrueAndNotSetInDai",
                "LLN0.DoB.structValImportNotSet.bValImportTrueAndNotSetInDai",
                "LLN0.DoB.structValImportFalse.bValImportTrueAndNotSetInDai",
                // Only these FC are updatable
                "LLN0.DoB.structWithFcCF.bda1",
                "LLN0.DoB.structWithFcDC.bda1",
                "LLN0.DoB.structWithFcSG.bda1",
                "LLN0.DoB.structWithFcSP.bda1",
                "LLN0.DoB.structWithFcST.bda1",
                "LLN0.DoB.structWithFcSE.bda1"
        );
    }

    @Test
    void getDAI_should_filter_updatable_DA_with_sGroup_Val() {
        // given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_test_updatable_DAI.xml");

        // when
        Set<DataAttributeRef> dais = sclElementsProviderService.getDAI(scd, "VirtualBCU", "LDCAP", new DataAttributeRef(), true);

        // then
        assertThat(dais).isNotNull();
        List<String> resultSimpleDa = dais.stream()
                .filter(dataAttributeRef -> dataAttributeRef.getBdaNames().isEmpty()) // test only simple DA
                .map(DataAttributeRef::getLNRef).collect(Collectors.toList());
        assertThat(resultSimpleDa).containsExactlyInAnyOrder(
                "LLN0.DoD.sGroupValImportNotSet",
                "LLN0.DoD.sGroupValImportTrue"
        );
    }

    @Test
    void getDAI_should_filter_updatable_DA_with_sGroup_Val_without_ConfSg() {
        // given
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_test_updatable_DAI.xml");

        // when
        Set<DataAttributeRef> dais = sclElementsProviderService.getDAI(scd, "VirtualBCU", "LDMOD", new DataAttributeRef(), true);

        // then
        assertThat(dais)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void testGetEnumTypeValues() {
        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_1_test.xml");
        assertThrows(ScdException.class, () -> sclElementsProviderService.getEnumTypeValues(scd, "unknwnID"));
        var enumList = assertDoesNotThrow(() -> sclElementsProviderService.getEnumTypeValues(scd, "RecCycModKind"));
        assertFalse(enumList.isEmpty());
    }

}
