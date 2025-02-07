// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.domain.*;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.ActiveStatus;
import org.lfenergy.compas.sct.commons.util.SclConstructorHelper;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class LnServiceTest {

    private LnService lnService;

    @BeforeEach
    void setUp() {
        lnService = new LnService();
    }

    @Test
    void getAnylns_should_return_lns() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLDevice ldsuied = std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst();

        //When
        List<TAnyLN> tAnyLNS = lnService.getAnylns(ldsuied).toList();

        //Then
        assertThat(tAnyLNS)
                .hasSize(2)
                .extracting(TAnyLN::getLnType, TAnyLN::getClass)
                .containsExactly(Tuple.tuple("RTE_080BBB4D93E4E704CF69E8616CAF1A74_LLN0_V1.0.0", LN0.class),
                        Tuple.tuple("RTE_8884DBCF760D916CCE3EE9D1846CE46F_LPAI_V1.0.0", TLN.class));
    }

    @Test
    void findAnyLn_should_return_ln() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLDevice ldsuied = std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst();

        //When
        Optional<TAnyLN> anyLn = lnService.findAnyLn(ldsuied, tAnyLN -> tAnyLN.getLnType().equals("RTE_080BBB4D93E4E704CF69E8616CAF1A74_LLN0_V1.0.0"));

        //Then
        assertThat(anyLn.orElseThrow())
                .extracting(TAnyLN::getLnType, TAnyLN::getClass)
                .containsExactly("RTE_080BBB4D93E4E704CF69E8616CAF1A74_LLN0_V1.0.0", LN0.class);
    }

    @Test
    void getLns_should_return_lns() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLDevice ldsuied = std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst();

        //When
        List<TLN> tlns = lnService.getLns(ldsuied).toList();

        //Then
        assertThat(tlns)
                .hasSize(1)
                .extracting(TLN::getLnType, TLN::getInst, TLN::getLnClass, TLN::getClass)
                .containsExactly(Tuple.tuple("RTE_8884DBCF760D916CCE3EE9D1846CE46F_LPAI_V1.0.0", "1", List.of("LPAI"), TLN.class));
    }

    @Test
    void getFilteredLns_should_return_lns() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLDevice ldsuied = std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst();

        //When
        List<TLN> tlns = lnService.getFilteredLns(ldsuied, tln -> tln.getLnType().equals("RTE_8884DBCF760D916CCE3EE9D1846CE46F_LPAI_V1.0.0")).toList();

        //Then
        assertThat(tlns)
                .hasSize(1)
                .extracting(TLN::getLnType, TLN::getInst, TLN::getLnClass, TLN::getClass)
                .containsExactly(Tuple.tuple("RTE_8884DBCF760D916CCE3EE9D1846CE46F_LPAI_V1.0.0", "1", List.of("LPAI"), TLN.class));
    }

    @Test
    void findLn_should_return_lns() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLDevice ldsuied = std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst();

        //When
        Optional<TLN> ln = lnService.findLn(ldsuied, tln -> tln.getLnType().equals("RTE_8884DBCF760D916CCE3EE9D1846CE46F_LPAI_V1.0.0"));

        //Then
        assertThat(ln.orElseThrow())
                .extracting(TLN::getLnType, TLN::getInst, TLN::getLnClass, TLN::getClass)
                .containsExactly("RTE_8884DBCF760D916CCE3EE9D1846CE46F_LPAI_V1.0.0", "1", List.of("LPAI"), TLN.class);
    }

    @Test
    void getFilteredAnyLns_should_return_lns() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLDevice ldsuied = std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst();

        //When
        List<TAnyLN> tAnyLNS = lnService.getFilteredAnyLns(ldsuied, tAnyLN -> tAnyLN.getLnType().equals("RTE_080BBB4D93E4E704CF69E8616CAF1A74_LLN0_V1.0.0")).toList();

        //Then
        assertThat(tAnyLNS)
                .hasSize(1)
                .extracting(TAnyLN::getLnType, TAnyLN::getClass)
                .containsExactly(Tuple.tuple("RTE_080BBB4D93E4E704CF69E8616CAF1A74_LLN0_V1.0.0", LN0.class));
    }

    @Test
    void getDaiModStValValue_should_return_status() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLDevice tlDevice = std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst();

        //When
        Optional<ActiveStatus> daiModStval = lnService.getDaiModStValValue(tlDevice.getLN0());

        //Then
        assertThat(daiModStval).contains(ActiveStatus.ON);
    }

    @Test
    void getLnStatus_should_return_status() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLDevice tlDevice = std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst();

        //When
        ActiveStatus lnStatus = lnService.getLnStatus(tlDevice.getLN().getFirst(), tlDevice.getLN0());

        //Then
        assertThat(lnStatus).isEqualTo(ActiveStatus.ON);
    }

    @Test
    void getActiveLns_should_return_lns() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLDevice tlDevice = std.getIED().getFirst().getAccessPoint().getFirst().getServer().getLDevice().getFirst();

        //When
        List<TAnyLN> tAnyLNS = lnService.getActiveLns(tlDevice).toList();

        //Then
        assertThat(tAnyLNS)
                .hasSize(2)
                .extracting(TAnyLN::getLnType, TAnyLN::getDesc)
                .containsExactly(Tuple.tuple("RTE_080BBB4D93E4E704CF69E8616CAF1A74_LLN0_V1.0.0", ""),
                        Tuple.tuple("RTE_8884DBCF760D916CCE3EE9D1846CE46F_LPAI_V1.0.0", ""));
    }

    @Test
    void getDOAndDAInstance_should_return_true_when_DO_and_DA_instances_exists() {
        //Given
        TAnyLN tAnyLN = initDOAndDAInstances(
                new LinkedList<>(List.of("Do","sdo1", "d")),
                new LinkedList<>(List.of("antRef","bda1", "bda2", "bda3")),
                "new value",null
        );
        DoLinkedToDaFilter doLinkedToDaFilter = DoLinkedToDaFilter.from("Do.sdo1.d", "antRef.bda1.bda2.bda3");
        //When
        Optional<TDAI> optionalTDAI = lnService.getDOAndDAInstances(tAnyLN, doLinkedToDaFilter);
        //Then
        assertThat(optionalTDAI).isPresent();
        assertThat(optionalTDAI.get().getName()).isEqualTo("bda3");
        assertThat(optionalTDAI.get().isSetValImport()).isFalse();
    }

    @Test
    void getDOAndDAInstance_should_return_false_when_DO_and_DA_instances_not_exists() {
        //Given
        TAnyLN tAnyLN = initDOAndDAInstances(
                new LinkedList<>(List.of("Do","sdo1", "d")),
                new LinkedList<>(List.of("antRef","bda1", "bda2", "bda3")),
                "new value",null
        );
        DoLinkedToDaFilter doLinkedToDaFilter = DoLinkedToDaFilter.from("Do.sdo1.d", "antRef.unknown.bda2.bda3");
        //When
        Optional<TDAI> optionalTDAI = lnService.getDOAndDAInstances(tAnyLN, doLinkedToDaFilter);
        //Then
        assertThat(optionalTDAI).isEmpty();
    }

    @ParameterizedTest
    @CsvSource(value = {"null:false", "false:false", "true:true"}, delimiter = ':')
    void getCompletedDoLinkedToDaFromDAI_should_getDoLinkedToDaCompleted_when_valImport_set_or_not(boolean existingValImportSet, boolean expectedValImport) {
        //Given
        TIED tied = new TIED();
        TAnyLN tAnyLN = initDOAndDAInstances(
                new LinkedList<>(List.of("Do")),
                new LinkedList<>(List.of("Da")),
                "dai value", existingValImportSet
        );
        DataObject dataObject = new DataObject();
        dataObject.setDoName("Do");
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("Da");
        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);
        //When
        DoLinkedToDa result = lnService.getDoLinkedToDaCompletedFromDAI(tied, "ldInst", tAnyLN, doLinkedToDa);
        //Then
        assertThat(result.dataAttribute().isValImport()).isEqualTo(expectedValImport);
        assertThat(result.dataAttribute().getDaiValues()).isEqualTo(List.of(new DaVal(null, "dai value")));
        assertThat(result).usingRecursiveComparison()
                .ignoringFields("dataAttribute.valImport", "dataAttribute.daiValues")
                .isEqualTo(doLinkedToDa);
    }

    public static Collection<Object> testSettingGroupValuesWithIedHasConfSG() {
        return Arrays.asList(new Object[][] {
                { false, false, false, TFCEnum.SG, false},//FALSE: warning SG(or SE) should require setting group
                { false, true, false, TFCEnum.ST, false},
                { true, false, false, TFCEnum.ST, true},
                { true, false, false, TFCEnum.SE, false},//FALSE: warning SE(or SG) require setting group
                { true, true, false, TFCEnum.SE, false}, //FALSE: SE(or SG) require setting group and IED has ConfSG
                { true, true, true, TFCEnum.SE, true}, //TRUE: SettingGroup exist and IED has ConfSG
                { false, true, true, TFCEnum.SE, false} //FALSE: SettingGroup exist and IED has ConfSG but valImport is not set
        });
    }
    @ParameterizedTest
    @MethodSource("testSettingGroupValuesWithIedHasConfSG")
    void getCompletedDoLinkedToDaFromDAI_should_getDoLinkedToDaCompleted_when_settingGroup_set_or_not(Boolean existingValImportSet,
                                                                                                      Boolean isWithSettingGroup,
                                                                                                      Boolean isIedHasConfSG,
                                                                                                      TFCEnum givenFc,
                                                                                                      Boolean expectedValImport) {
        //Given
        TIED tied = new TIED();
        TAnyLN tAnyLN = new LN0();
        TDAI dai = initDOAndDAInstances(tAnyLN, new LinkedList<>(List.of("Do")), new LinkedList<>(List.of("Da")));
        TVal tVal = new TVal();
        tVal.setValue("dai value");
        dai.getVal().clear();
        dai.getVal().add(tVal);
        //
        dai.setValImport(existingValImportSet);
        if(isWithSettingGroup) tVal.setSGroup(1L);
        if(isIedHasConfSG){
            TAccessPoint tAccessPoint = new TAccessPoint();
            TServer tServer = new TServer();
            TLDevice tlDevice = new TLDevice();
            tlDevice.setInst("ldInst");
            tServer.getLDevice().add(tlDevice);
            tAccessPoint.setServer(tServer);
            TServices svc = new TServices();
            TSettingGroups settingGroups = new TSettingGroups();
            settingGroups.setConfSG(new TSettingGroups.ConfSG());
            svc.setSettingGroups(settingGroups);
            tAccessPoint.setServices(svc);
            tied.getAccessPoint().add(tAccessPoint);
        }

        DataObject dataObject = new DataObject();
        dataObject.setDoName("Do");
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("Da");
        dataAttribute.setFc(givenFc);

        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);

        //When
        DoLinkedToDa result = lnService.getDoLinkedToDaCompletedFromDAI(tied, "ldInst", tAnyLN, doLinkedToDa);
        //Then
        assertThat(result.dataAttribute().isValImport()).isEqualTo(expectedValImport);
        assertThat(result.dataAttribute().getDaiValues()).isEqualTo(List.of(new DaVal(isWithSettingGroup ? 1L : null, "dai value")));
        assertThat(result).usingRecursiveComparison()
                .ignoringFields("dataAttribute.valImport", "dataAttribute.daiValues")
                .isEqualTo(doLinkedToDa);
    }


    @Test
    void getCompletedDoLinkedToDaFromDAI_should_not_getDoLinkedToDaCompleted_when_not_found() {
        //Given
        TIED tied = new TIED();
        TAnyLN tAnyLN = new LN0();
        DataObject dataObject = new DataObject();
        dataObject.setDoName("Do");
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("Da");

        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);
        //When
        DoLinkedToDa result = lnService.getDoLinkedToDaCompletedFromDAI(tied, "ldInst", tAnyLN, doLinkedToDa);
        //Then
        assertThat(result).usingRecursiveComparison().isEqualTo(doLinkedToDa);//initialValue
    }

    @ParameterizedTest
    @CsvSource(value = {"null:false", "false:false", "true:true"}, delimiter = ':')
    void getCompletedDoLinkedToDaFromDAI_should_getDoLinkedToDaCompleted_when_struct(Boolean input, Boolean expected) {
        //Given
        TIED tied = new TIED();
        TAnyLN tAnyLN = initDOAndDAInstances(
                new LinkedList<>(List.of("Do","sdo1", "d")),
                new LinkedList<>(List.of("antRef","bda1", "bda2", "bda3")),
                "dai value", input
        );
        DataObject dataObject = new DataObject();
        dataObject.setDoName("Do");
        dataObject.setSdoNames(List.of("sdo1","d"));
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("antRef");
        dataAttribute.setBdaNames(List.of("bda1","bda2","bda3"));

        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);

        //When
        DoLinkedToDa result = lnService.getDoLinkedToDaCompletedFromDAI(tied, "ldInst", tAnyLN, doLinkedToDa);
        //Then
        assertThat(result.dataAttribute().isValImport()).isEqualTo(expected);
        assertThat(result.dataAttribute().getDaiValues()).isEqualTo(List.of(new DaVal(null, "dai value")));
        assertThat(result).usingRecursiveComparison()
                .ignoringFields("dataAttribute.valImport", "dataAttribute.daiValues")
                .isEqualTo(doLinkedToDa);
    }

    @Test
    void getDoLinkedToDaCompletedFromDAI_should_replace_val() {
        //Given
        TIED tied = new TIED();
        TAnyLN tAnyLN = initDOAndDAInstances(
                new LinkedList<>(List.of("Do")),
                new LinkedList<>(List.of("Da")),
                "dai value", null
        );
        DataObject dataObject = new DataObject();
        dataObject.setDoName("Do");
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("Da");
        dataAttribute.getDaiValues().add(new DaVal(null, "value from dataTypeTemplate"));
        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);
        //When
        DoLinkedToDa result = lnService.getDoLinkedToDaCompletedFromDAI(tied, "ldInst", tAnyLN, doLinkedToDa);
        //Then
        assertThat(result.dataAttribute().getDaiValues()).isEqualTo(List.of(new DaVal(null, "dai value")));
        assertThat(result).usingRecursiveComparison()
                .ignoringFields("dataAttribute.daiValues")
                .isEqualTo(doLinkedToDa);
    }

    @Test
    void getDoLinkedToDaCompletedFromDAI_should_replace_val_with_sGroup() {
        //Given
        TIED tied = new TIED();
        TAnyLN tAnyLN = initDOAndDAInstances(
                new LinkedList<>(List.of("Do")),
                new LinkedList<>(List.of("Da")),
                "ignored", null
        );
        TDAI tdai = (TDAI) tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst();
        tdai.getVal().clear();
        tdai.getVal().add(SclConstructorHelper.newVal("value 2 from DAI", 2L));
        DataObject dataObject = new DataObject();
        dataObject.setDoName("Do");
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("Da");
        dataAttribute.getDaiValues().add(new DaVal(1L, "value 1 from dataTypeTemplate"));
        dataAttribute.getDaiValues().add(new DaVal(2L, "value 2 from dataTypeTemplate"));
        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);
        //When
        DoLinkedToDa result = lnService.getDoLinkedToDaCompletedFromDAI(tied, "ldInst", tAnyLN, doLinkedToDa);
        //Then
        assertThat(result.dataAttribute().getDaiValues()).containsExactly(
                new DaVal(1L, "value 1 from dataTypeTemplate"),
                new DaVal(2L, "value 2 from DAI"));
        assertThat(result).usingRecursiveComparison()
                .ignoringFields("dataAttribute.daiValues")
                .isEqualTo(doLinkedToDa);
    }

    @Test
    void updateOrCreateDOAndDAInstance_should_create_given_DO_and_DA_instances_when_no_struct_and_with_settingGroup() {
        //Given
        TAnyLN tAnyLN = new LN0();

        DataObject dataObject = new DataObject();
        dataObject.setDoName("Mod");
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("stVal");
        dataAttribute.getDaiValues().add(new DaVal(1L, "new value"));
        dataAttribute.getDaiValues().add(new DaVal(2L, "new value 2"));
        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);
        //When
        lnService.updateOrCreateDOAndDAInstances(tAnyLN, doLinkedToDa);
        //Then
        assertThat(tAnyLN.getDOI()).hasSize(1);
        assertThat(tAnyLN.getDOI().getFirst().getName()).isEqualTo("Mod");
        assertThat(tAnyLN.getDOI().getFirst().getSDIOrDAI()).hasSize(1);
        assertThat(((TDAI)tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getName()).isEqualTo("stVal");
        assertThat(((TDAI)tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).isSetVal()).isTrue();
        assertThat(((TDAI)tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getVal()).hasSize(2);
        assertThat(((TDAI)tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getVal().getFirst().getSGroup()).isEqualTo(1L);
        assertThat(((TDAI)tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getVal().getFirst().getValue()).isEqualTo("new value");
        assertThat(((TDAI)tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getVal().get(1).getSGroup()).isEqualTo(2L);
        assertThat(((TDAI)tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getVal().get(1).getValue()).isEqualTo("new value 2");
    }


    @Test
    void updateOrCreateDOAndDAInstance_should_create_given_DO_and_DA_instances_when_struct_and_without_settingGroup() {
        //Given
        TAnyLN tAnyLN = new LN0();

        DataObject dataObject = new DataObject();
        dataObject.setDoName("Do");
        dataObject.setSdoNames(List.of("sdo1", "d"));
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("antRef");
        dataAttribute.setBdaNames(List.of("bda1"));
        dataAttribute.getDaiValues().add(new DaVal(null, "new value"));
        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);

        //When
        lnService.updateOrCreateDOAndDAInstances(tAnyLN, doLinkedToDa);

        //Then
        assertThat(tAnyLN.getDOI()).hasSize(1);
        assertThat(tAnyLN.getDOI().getFirst().getName()).isEqualTo("Do");
        assertThat(tAnyLN.getDOI().getFirst().getSDIOrDAI()).hasSize(1);
        assertThat((( TSDI )tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getName()).isEqualTo("sdo1");
        assertThat((( TSDI )tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getSDIOrDAI()).hasSize(1);
        assertThat((( TSDI )(( TSDI )tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getSDIOrDAI().getFirst()).getName()).isEqualTo("d");
        assertThat((( TSDI )(( TSDI )tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getSDIOrDAI().getFirst()).getSDIOrDAI()).hasSize(1);

        assertThat((( TSDI )(( TSDI )(( TSDI )tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getSDIOrDAI().getFirst())
                .getSDIOrDAI().getFirst()).getName()).isEqualTo("antRef");
        assertThat((( TSDI )(( TSDI )(( TSDI )tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getSDIOrDAI().getFirst())
                .getSDIOrDAI().getFirst()).getSDIOrDAI()).hasSize(1);

        assertThat((( TDAI )(( TSDI )(( TSDI )(( TSDI )tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getSDIOrDAI().getFirst())
                .getSDIOrDAI().getFirst()).getSDIOrDAI().getFirst()).getName()).isEqualTo("bda1");
        assertThat((( TDAI )(( TSDI )(( TSDI )(( TSDI )tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getSDIOrDAI().getFirst())
                .getSDIOrDAI().getFirst()).getSDIOrDAI().getFirst()).getVal()).hasSize(1);
        assertThat((( TDAI )(( TSDI )(( TSDI )(( TSDI )tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getSDIOrDAI().getFirst())
                .getSDIOrDAI().getFirst()).getSDIOrDAI().getFirst()).getVal().getFirst().isSetSGroup()).isFalse();
        assertThat((( TDAI )(( TSDI )(( TSDI )(( TSDI )tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getSDIOrDAI().getFirst())
                .getSDIOrDAI().getFirst()).getSDIOrDAI().getFirst()).getVal().getFirst().getValue()).isEqualTo("new value");
    }

    public static Collection<Object> testValImportValues() {
        return Arrays.asList(new Object[][] {
                { null, true, false, false },
                { null, false, false, false },
                { false, false, true, false },
                { false, true, true, true },
                { true, false, true, false },
                { true, true, true, true }
        });
    }

    @ParameterizedTest
    @MethodSource("testValImportValues")
    void updateOrCreateDOAndDAInstance_should_complete_DO_and_DA_instances_modification(Boolean existingValImportState,
                                                                                        Boolean givenValImportState,
                                                                                        Boolean expectedIsSetValImport,
                                                                                        Boolean expectedIsValImportValue) {
        //Given
        TAnyLN tAnyLN = initDOAndDAInstances(
                new LinkedList<>(List.of("DoName1", "SdoName1")),
                new LinkedList<>(List.of("DaName2", "BdaName1")),
                "dai value", existingValImportState
        );

        DataObject dataObject = new DataObject();
        dataObject.setDoName("DoName1");
        dataObject.setSdoNames(List.of("SdoName1"));
        DataAttribute dataAttribute = new DataAttribute();
        dataAttribute.setDaName("DaName2");
        dataAttribute.setValImport(givenValImportState);
        dataAttribute.setBdaNames(List.of("BdaName1"));
        dataAttribute.getDaiValues().add(new DaVal(null, "new dai value"));

        DoLinkedToDa doLinkedToDa = new DoLinkedToDa(dataObject, dataAttribute);

        //When
        lnService.updateOrCreateDOAndDAInstances(tAnyLN, doLinkedToDa);

        //Then
        // SDI and DAI already exist
        assertThat(tAnyLN.getDOI()).hasSize(1);
        assertThat(tAnyLN.getDOI().getFirst().getName()).isEqualTo("DoName1");
        assertThat(tAnyLN.getDOI().getFirst().getSDIOrDAI()).hasSize(1);
        assertThat((( TSDI )tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getName()).isEqualTo("SdoName1");
        assertThat((( TSDI )tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getSDIOrDAI()).hasSize(1);
        //final DAI
        assertThat((( TSDI )(( TSDI )tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getSDIOrDAI().getFirst()).getName()).isEqualTo("DaName2");
        assertThat((( TSDI )(( TSDI )tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getSDIOrDAI().getFirst()).getSDIOrDAI()).hasSize(1);
        assertThat((( TDAI )(( TSDI )(( TSDI )tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getSDIOrDAI().getFirst())
                .getSDIOrDAI().getFirst()).getName()).isEqualTo("BdaName1");
        // ==> valImport Set
        assertThat((( TDAI )(( TSDI )(( TSDI )tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getSDIOrDAI().getFirst())
                .getSDIOrDAI().getFirst()).isSetValImport()).isEqualTo(expectedIsSetValImport);
        // ==> valImport Value
        if(expectedIsSetValImport) {
            assertThat((( TDAI )(( TSDI )(( TSDI )tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getSDIOrDAI().getFirst())
                    .getSDIOrDAI().getFirst()).isValImport()).isEqualTo(expectedIsValImportValue);
        }
        assertThat((( TDAI )(( TSDI )(( TSDI )tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getSDIOrDAI().getFirst())
                .getSDIOrDAI().getFirst()).getVal()).hasSize(1);
        // ==> DAI value
        assertThat((( TDAI )(( TSDI )(( TSDI )tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getSDIOrDAI().getFirst())
                .getSDIOrDAI().getFirst()).getVal().getFirst().getValue()).isEqualTo("new dai value");
        assertThat((( TDAI )(( TSDI )(( TSDI )tAnyLN.getDOI().getFirst().getSDIOrDAI().getFirst()).getSDIOrDAI().getFirst())
                .getSDIOrDAI().getFirst()).getVal().getFirst().isSetSGroup()).isFalse();
    }


    private TSDI createSDIFromDOI(TDOI doi, String sdiName) {
        return doi.getSDIOrDAI().stream()
                .filter(sdi -> sdi.getClass().equals(TSDI.class))
                .map(TSDI.class::cast)
                .filter(tsdi -> tsdi.getName().equals(sdiName))
                .findFirst()
                .orElseGet(() -> {
                    TSDI tsdi = new TSDI();
                    tsdi.setName(sdiName);
                    doi.getSDIOrDAI().add(tsdi);
                    return tsdi;
                });
    }

    private TSDI createSDIFromSDI(TSDI sdi, String sdiName) {
        return sdi.getSDIOrDAI().stream()
                .filter(unNaming -> unNaming.getClass().equals(TSDI.class))
                .map(TSDI.class::cast)
                .filter(tsdi -> tsdi.getName().equals(sdiName))
                .findFirst()
                .orElseGet(() -> {
                    TSDI tsdi = new TSDI();
                    tsdi.setName(sdiName);
                    sdi.getSDIOrDAI().add(tsdi);
                    return tsdi;
                });
    }

    private TSDI createSDIByStructName(TSDI tsdi, LinkedList<String> structNames) {
        structNames.remove();
        if(structNames.isEmpty() || structNames.size() == 1) return tsdi;
        return createSDIByStructName(createSDIFromSDI(tsdi, structNames.getFirst()), structNames);
    }

    private TAnyLN initDOAndDAInstances(LinkedList<String> doInstances,
                                        LinkedList<String> daInstances,
                                        String daiVal,
                                        Boolean valImport){
        assertThat(doInstances).isNotEmpty();
        assertThat(daInstances).isNotEmpty();
        LinkedList<String> structInstances = new LinkedList<>(doInstances);
        daInstances.forEach(structInstances::addLast);
        TLN0 tln0 = new TLN0();
        TDOI tdoi = new TDOI();
        tdoi.setName(doInstances.getFirst());
        structInstances.remove();
        if(structInstances.size() > 1){
            TSDI firstSDI = createSDIFromDOI(tdoi, structInstances.getFirst());
            TSDI lastSDI = createSDIByStructName(firstSDI, structInstances);
            if(structInstances.size() == 1){
                TDAI dai = new TDAI();
                dai.setName(daInstances.getLast());
                dai.getVal().add(SclConstructorHelper.newVal(daiVal));
                if (valImport != null) dai.setValImport(valImport);
                lastSDI.getSDIOrDAI().add(dai);
            }
        } else
        if(structInstances.size() == 1){
            TDAI dai = new TDAI();
            dai.setName(daInstances.getLast());
            dai.getVal().add(SclConstructorHelper.newVal(daiVal));
            if (valImport != null) dai.setValImport(valImport);
            tdoi.getSDIOrDAI().add(dai);
        }
        tln0.getDOI().add(tdoi);
        return tln0;
    }


    private TDAI initDOAndDAInstances(TAnyLN tAnyLN,
                                     LinkedList<String> doInstances,
                                     LinkedList<String> daInstances){
        assertThat(doInstances).isNotEmpty();
        assertThat(daInstances).isNotEmpty();
        LinkedList<String> structInstances = new LinkedList<>(doInstances);
        daInstances.forEach(structInstances::addLast);
        TDOI tdoi = new TDOI();
        TDAI dai = new TDAI();
        tdoi.setName(doInstances.getFirst());
        structInstances.remove();
        if(structInstances.size() > 1){
            TSDI firstSDI = createSDIFromDOI(tdoi, structInstances.getFirst());
            TSDI lastSDI = createSDIByStructName(firstSDI, structInstances);
            if(structInstances.size() == 1){
                dai.setName(daInstances.getLast());
                TVal tVal = new TVal();
                dai.getVal().add(tVal);
                lastSDI.getSDIOrDAI().add(dai);
                tAnyLN.getDOI().add(tdoi);
            }
        } else
        if(structInstances.size() == 1){
            dai.setName(daInstances.getLast());
            TVal tVal = new TVal();
            dai.getVal().add(tVal);
            tdoi.getSDIOrDAI().add(dai);
            tAnyLN.getDOI().add(tdoi);
        }
        return dai;
    }

    @ParameterizedTest
    @MethodSource("provideLnAttributesForMatchingLns")
    void matchesLn_should_return_True_in_these_LN_cases(String lnClass, String lnInst, String lnPrefix) {
        //GIVEN:
        TLN tln = new TLN();
        tln.getLnClass().add(lnClass);
        tln.setInst(lnInst);
        tln.setPrefix(lnPrefix);
        //WHEN:
        Boolean response = lnService.matchesLn(tln, lnClass, lnInst, lnPrefix);
        //THEN:
        assertThat(response).isTrue();
    }

    private static Stream<Arguments> provideLnAttributesForMatchingLns() {
        return Stream.of(
                Arguments.of("LPHD", "1", "LnPrefix", "LPHD", "1", "LnPrefix"),
                Arguments.of("LPHD", "1", null, "LPHD", "1", null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideLnAttributesForUnMatchingLns")
    void matchesLn_should_return_False_in_these_LN_cases(String lnClass, String lnInst, String lnPrefix, String lnClassString, String lnInstString, String lnPrefixString) {
        //GIVEN:
        TLN tln = new TLN();
        tln.getLnClass().add(lnClass);
        tln.setInst(lnInst);
        tln.setPrefix(lnPrefix);
        //WHEN:
        Boolean response = lnService.matchesLn(tln, lnClassString, lnInstString, lnPrefixString);
        //THEN:
        assertThat(response).isFalse();
    }

    private static Stream<Arguments> provideLnAttributesForUnMatchingLns() {
        return Stream.of(
                Arguments.of("FAKE", "1", "LnPrefix", "LPHD", "1", "LnPrefix"),
                Arguments.of("LPHD", "FAKE", "LnPrefix", "LPHD", "1", "LnPrefix"),
                Arguments.of("LPHD", "1", "FAKE", "LPHD", "1", "LnPrefix"),
                Arguments.of("FAKE", "1", null, "LPHD", "1", null),
                Arguments.of("LPHD", "FAKE", null, "LPHD", "1", null)
        );
    }


    @ParameterizedTest
    @MethodSource("provideLn0AttributesForMatchingLn0s")
    void matchesLn_should_return_True_for_matching_LLN0(String lnClass, String lnInst, String lnPrefix) {
        //GIVEN:
        LN0 ln0 = new LN0();
        ln0.getLnClass().add(lnClass);
        //WHEN:
        Boolean response = lnService.matchesLn(ln0, lnClass, lnInst, lnPrefix);
        //THEN:
        assertThat(response).isTrue();
    }

    private static Stream<Arguments> provideLn0AttributesForMatchingLn0s() {
        return Stream.of(
                Arguments.of("LLN0", "", ""),
                Arguments.of("LLN0", "1", ""),
                Arguments.of("LLN0", "", "LnPrefix")
        );
    }

    @Test
    void matchesLn_should_return_False_for_Unmatching_LLN0() {
        //GIVEN:
        LN0 ln0 = new LN0();
        ln0.getLnClass().add("LLN0");
        //WHEN:
        Boolean response = lnService.matchesLn(ln0, "LPHD", "", "");
        //THEN:
        assertThat(response).isFalse();
    }

    @Test
    void matchesLn_should_throw_an_exception() {
        //GIVEN:
        TLN0 tln0 = new TLN0();
        tln0.getLnClass().add("LLN0");
        //WHEN && //THEN:
        Assertions.assertThatThrownBy(() -> lnService.matchesLn(tln0, "LLN0", "", ""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unexpected value: ");
    }

}
