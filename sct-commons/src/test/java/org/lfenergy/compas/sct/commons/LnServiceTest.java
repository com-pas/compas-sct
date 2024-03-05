// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.ActiveStatus;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.lfenergy.compas.sct.commons.testhelpers.DataTypeUtils.*;

class LnServiceTest {

    @Test
    void getAnylns_should_return_lns() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLDevice tlDevice = std.getIED().get(0).getAccessPoint().get(0).getServer().getLDevice().get(0);
        LnService lnService = new LnService();

        //When
        List<TAnyLN> tAnyLNS = lnService.getAnylns(tlDevice).toList();

        //Then
        assertThat(tAnyLNS)
                .hasSize(2)
                .extracting(TAnyLN::getLnType)
                .containsExactly("RTE_080BBB4D93E4E704CF69E8616CAF1A74_LLN0_V1.0.0", "RTE_8884DBCF760D916CCE3EE9D1846CE46F_LPAI_V1.0.0");
    }

    @Test
    void getDaiModStval_should_return_status() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLDevice tlDevice = std.getIED().get(0).getAccessPoint().get(0).getServer().getLDevice().get(0);
        LnService lnService = new LnService();

        //When
        Optional<ActiveStatus> daiModStval = lnService.getDaiModStval(tlDevice.getLN0());

        //Then
        assertThat(daiModStval).contains(ActiveStatus.ON);
    }

    @Test
    void getLnStatus_should_return_status() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLDevice tlDevice = std.getIED().get(0).getAccessPoint().get(0).getServer().getLDevice().get(0);
        LnService lnService = new LnService();

        //When
        ActiveStatus lnStatus = lnService.getLnStatus(tlDevice.getLN().get(0), tlDevice.getLN0());

        //Then
        assertThat(lnStatus).isEqualTo(ActiveStatus.ON);
    }

    @Test
    void getActiveLns_should_return_lns() {
        //Given
        SCL std = SclTestMarshaller.getSCLFromFile("/std/std_sample.std");
        TLDevice tlDevice = std.getIED().get(0).getAccessPoint().get(0).getServer().getLDevice().get(0);
        LnService lnService = new LnService();

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
    void isDOAndDAInstanceExists_should_return_true_when_DO_and_DA_instances_exists() {
        //Given
        TAnyLN tAnyLN = initDOAndDAInstances(
                new LinkedList<>(List.of("Do","sdo1", "d")),
                new LinkedList<>(List.of("antRef","bda1", "bda2", "bda3")),
                "new value",null
        );
        DoTypeName doTypeName = new DoTypeName("Do.sdo1.d");
        DaTypeName daTypeName = new DaTypeName("antRef.bda1.bda2.bda3");
        //When
        LnService lnService = new LnService();
        Optional<TDAI> optionalTDAI = lnService.isDOAndDAInstancesExist(tAnyLN, doTypeName, daTypeName);
        //Then
        assertThat(optionalTDAI).isPresent();
    }

    @Test
    void isDOAndDAInstanceExists_should_return_false_when_DO_and_DA_instances_not_exists() {
        //Given
        TAnyLN tAnyLN = initDOAndDAInstances(
                new LinkedList<>(List.of("Do","sdo1", "d")),
                new LinkedList<>(List.of("antRef","bda1", "bda2", "bda3")),
                "new value",null
        );
        DoTypeName doTypeName = new DoTypeName("Do.sdo1.d");
        DaTypeName daTypeName = new DaTypeName("antRef.unknown.bda2.bda3");
        //When
        LnService lnService = new LnService();
        Optional<TDAI> optionalTDAI = lnService.isDOAndDAInstancesExist(tAnyLN, doTypeName, daTypeName);
        //Then
        assertThat(optionalTDAI).isPresent();
    }

    @ParameterizedTest
    @CsvSource(value = {"null:false", "false:false", "true:true"}, delimiter = ':')
    void completeFromDataAttributeInstance_should_complete_when_valImport_set_or_not(Boolean existingValImportSet,
                                                                                     Boolean expectedValImport) {
        //Given
        TIED tied = new TIED();
        TAnyLN tAnyLN = initDOAndDAInstances(
                new LinkedList<>(List.of("Do")),
                new LinkedList<>(List.of("Da")),
                "new value", existingValImportSet
        );
        DoTypeName doTypeName = new DoTypeName("Do");
        DaTypeName daTypeName = new DaTypeName("Da");
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setDaName(daTypeName);
        dataAttributeRef.setDoName(doTypeName);
        //When
        LnService lnService = new LnService();
        lnService.completeFromDAInstance(tied, "ldInst", tAnyLN, dataAttributeRef);
        //Then
        assertThat(dataAttributeRef.isValImport()).isEqualTo(expectedValImport);
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
    void completeFromDataAttributeInstance_should_complete_when_settingGroup_set_or_not(Boolean existingValImportSet,
                                                                                        Boolean isWithSettingGroup,
                                                                                        Boolean isIedHasConfSG,
                                                                                        TFCEnum givenFc,
                                                                                        Boolean expectedValImport) {
        //Given
        TIED tied = new TIED();
        TAnyLN tAnyLN = new LN0();
        TDAI dai = initDOAndDAInstances(tAnyLN, new LinkedList<>(List.of("Do")), new LinkedList<>(List.of("Da")));
        TVal tVal = new TVal();
        tVal.setValue("dailue");
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

        DoTypeName doTypeName = new DoTypeName("Do");
        DaTypeName daTypeName = new DaTypeName("Da");
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setDaName(daTypeName);
        dataAttributeRef.setDoName(doTypeName);
        dataAttributeRef.setFc(givenFc);

        //When
        LnService lnService = new LnService();
        lnService.completeFromDAInstance(tied, "ldInst", tAnyLN, dataAttributeRef);
        //Then
        assertThat(dataAttributeRef.isValImport()).isEqualTo(expectedValImport);
    }


    @Test
    void completeFromDataAttributeInstance__should_not_complete_when_not_found() {
        //Given
        TIED tied = new TIED();
        TAnyLN tAnyLN = new LN0();
        DoTypeName doTypeName = new DoTypeName("Do");
        DaTypeName daTypeName = new DaTypeName("Da");
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setDaName(daTypeName);
        dataAttributeRef.setDoName(doTypeName);
        //When
        LnService lnService = new LnService();
        lnService.completeFromDAInstance(tied, "ldInst",  tAnyLN, dataAttributeRef);
        //Then
        assertThat(dataAttributeRef.isValImport()).isFalse();//initialValue
    }

    @ParameterizedTest
    @CsvSource(value = {"null:false", "false:false", "true:true"}, delimiter = ':')
    void completeFromDataAttributeInstance__should_complete_when_struct(Boolean input, Boolean expected) {
        //Given
        TIED tied = new TIED();
        TAnyLN tAnyLN = initDOAndDAInstances(
                new LinkedList<>(List.of("Do","sdo1", "d")),
                new LinkedList<>(List.of("antRef","bda1", "bda2", "bda3")),
                "new value", input
        );
        DoTypeName doTypeName = new DoTypeName("Do.sdo1.d");
        DaTypeName daTypeName = new DaTypeName("antRef.bda1.bda2.bda3");
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.setDaName(daTypeName);
        dataAttributeRef.setDoName(doTypeName);
        //When
        LnService lnService = new LnService();
        lnService.completeFromDAInstance(tied, "ldInst",  tAnyLN, dataAttributeRef);
        //Then
        assertThat(dataAttributeRef.isValImport()).isEqualTo(expected);
    }

    @Test
    void updateOrCreateDOAndDAInstances_should_create_given_DO_and_DA_instances_when_no_struct_and_with_settingGroup() {
        //Given
        TAnyLN tAnyLN = new LN0();

        DoTypeName doTypeName = new DoTypeName("Mod");
        DaTypeName daTypeName = new DaTypeName("stVal");
        daTypeName.getDaiValues().put(1L, "new value");
        daTypeName.getDaiValues().put(2L, "new value 2");
        DataAttributeRef dataAttributeRef = createDataAttributeRef(doTypeName, daTypeName);

        //When
        LnService lnService = new LnService();
        lnService.updateOrCreateDOAndDAInstances(tAnyLN, dataAttributeRef);
        //Then
        assertThat(tAnyLN.getDOI()).hasSize(1);
        assertThat(tAnyLN.getDOI().get(0).getName()).isEqualTo("Mod");
        assertThat(tAnyLN.getDOI().get(0).getSDIOrDAI()).hasSize(1);
        assertThat(((TDAI)tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getName()).isEqualTo("stVal");
        assertThat(((TDAI)tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).isSetVal()).isTrue();
        assertThat(((TDAI)tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getVal()).hasSize(2);
        assertThat(((TDAI)tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getVal().get(0).getSGroup()).isEqualTo(1L);
        assertThat(((TDAI)tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getVal().get(0).getValue()).isEqualTo("new value");
        assertThat(((TDAI)tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getVal().get(1).getSGroup()).isEqualTo(2L);
        assertThat(((TDAI)tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getVal().get(1).getValue()).isEqualTo("new value 2");
    }


    @Test
    void updateOrCreateDOAndDAInstances_should_create_given_DO_and_DA_instances_when_struct_and_without_settingGroup() {
        //Given
        TAnyLN tAnyLN = new LN0();
        DoTypeName doTypeName = new DoTypeName();
        doTypeName.setName("Do");
        doTypeName.setStructNames(List.of("sdo1", "d"));
        DaTypeName daTypeName = new DaTypeName();
        daTypeName.setName("antRef");
        daTypeName.setStructNames(List.of("bda1"));
        daTypeName.getDaiValues().put(0L, "new value");
        DataAttributeRef dataAttributeRef = createDataAttributeRef(doTypeName, daTypeName);

        //When
        LnService lnService = new LnService();
        lnService.updateOrCreateDOAndDAInstances(tAnyLN, dataAttributeRef);

        //Then
        assertThat(tAnyLN.getDOI()).hasSize(1);
        assertThat(tAnyLN.getDOI().get(0).getName()).isEqualTo("Do");
        assertThat(tAnyLN.getDOI().get(0).getSDIOrDAI()).hasSize(1);
        assertThat((( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getName()).isEqualTo("sdo1");
        assertThat((( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI()).hasSize(1);
        assertThat((( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0)).getName()).isEqualTo("d");
        assertThat((( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0)).getSDIOrDAI()).hasSize(1);

        assertThat((( TSDI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0))
                .getSDIOrDAI().get(0)).getName()).isEqualTo("antRef");
        assertThat((( TSDI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0))
                .getSDIOrDAI().get(0)).getSDIOrDAI()).hasSize(1);

        assertThat((( TDAI )(( TSDI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0))
                .getSDIOrDAI().get(0)).getSDIOrDAI().get(0)).getName()).isEqualTo("bda1");
        assertThat((( TDAI )(( TSDI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0))
                .getSDIOrDAI().get(0)).getSDIOrDAI().get(0)).getVal()).hasSize(1);
        assertThat((( TDAI )(( TSDI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0))
                .getSDIOrDAI().get(0)).getSDIOrDAI().get(0)).getVal().get(0).isSetSGroup()).isFalse();
        assertThat((( TDAI )(( TSDI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0))
                .getSDIOrDAI().get(0)).getSDIOrDAI().get(0)).getVal().get(0).getValue()).isEqualTo("new value");
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
    void updateOrCreateDOAndDAInstances_should_complete_DO_and_DA_instances_modification(Boolean existingValImportState,
                                                                                         Boolean givenValImportState,
                                                                                         Boolean expectedIsSetValImport,
                                                                                         Boolean expectedIsValImportValue) {
        //Given
        TAnyLN tAnyLN = initDOAndDAInstances(
                new LinkedList<>(List.of("DoName1", "SdoName1")),
                new LinkedList<>(List.of("DaName2", "BdaName1")),
                "dai value",existingValImportState
        );
        DoTypeName doTypeName = new DoTypeName("DoName1.SdoName1");
        DaTypeName daTypeName = new DaTypeName("DaName2.BdaName1");
        daTypeName.getDaiValues().put(0L, "new dai value");
        daTypeName.setValImport(givenValImportState);
        DataAttributeRef dataAttributeRef = createDataAttributeRef(doTypeName, daTypeName);
        //When
        LnService lnService = new LnService();
        lnService.updateOrCreateDOAndDAInstances(tAnyLN, dataAttributeRef);

        //Then
        // SDI and DAI already exist
        assertThat(tAnyLN.getDOI()).hasSize(1);
        assertThat(tAnyLN.getDOI().get(0).getName()).isEqualTo("DoName1");
        assertThat(tAnyLN.getDOI().get(0).getSDIOrDAI()).hasSize(1);
        assertThat((( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getName()).isEqualTo("SdoName1");
        assertThat((( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI()).hasSize(1);
        //final DAI
        assertThat((( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0)).getName()).isEqualTo("DaName2");
        assertThat((( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0)).getSDIOrDAI()).hasSize(1);
        assertThat((( TDAI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0))
                .getSDIOrDAI().get(0)).getName()).isEqualTo("BdaName1");
        // ==> valImport Set
        assertThat((( TDAI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0))
                .getSDIOrDAI().get(0)).isSetValImport()).isEqualTo(expectedIsSetValImport);
        // ==> valImport Value
        if(expectedIsSetValImport) {
            assertThat((( TDAI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0))
                    .getSDIOrDAI().get(0)).isValImport()).isEqualTo(expectedIsValImportValue);
        }
        assertThat((( TDAI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0))
                .getSDIOrDAI().get(0)).getVal()).hasSize(1);
        // ==> DAI value
        assertThat((( TDAI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0))
                .getSDIOrDAI().get(0)).getVal().get(0).getValue()).isEqualTo("new dai value");
        assertThat((( TDAI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0))
                .getSDIOrDAI().get(0)).getVal().get(0).isSetSGroup()).isFalse();
    }


    @Test
    void updateOrCreateDOAndDAInstances_should_complete_DO_and_DA_instances_creation() {
        //Given
        TAnyLN tAnyLN = initDOAndDAInstances(
                new LinkedList<>(List.of("DoName1","SdoName1", "SdoName2")),
                new LinkedList<>(List.of("DaName1","BdaName1", "BdaName2")),
                "dai value",true
        );
        DoTypeName doTypeName = new DoTypeName("DoName1.SdoName1");
        DaTypeName daTypeName = new DaTypeName("DaName2.BdaName1");
        daTypeName.getDaiValues().put(0L, "new dai value");
        DataAttributeRef dataAttributeRef = createDataAttributeRef(doTypeName, daTypeName);
        //When
        LnService lnService = new LnService();
        lnService.updateOrCreateDOAndDAInstances(tAnyLN, dataAttributeRef);
        //Then
        // SDI and  DAI already exist
        assertThat(tAnyLN.getDOI()).hasSize(1);
        assertThat(tAnyLN.getDOI().get(0).getName()).isEqualTo("DoName1");
        assertThat(tAnyLN.getDOI().get(0).getSDIOrDAI()).hasSize(1);
        assertThat((( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getName()).isEqualTo("SdoName1");
        assertThat((( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI()).hasSize(2);//new SDI
        assertThat((( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0)).getName()).isEqualTo("SdoName2");
        assertThat((( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0)).getSDIOrDAI()).hasSize(1);
        assertThat((( TSDI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0))
                .getSDIOrDAI().get(0)).getName()).isEqualTo("DaName1");
        assertThat((( TSDI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0))
                .getSDIOrDAI().get(0)).getSDIOrDAI()).hasSize(1);
        assertThat((( TSDI )(( TSDI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0))
                .getSDIOrDAI().get(0)).getSDIOrDAI().get(0)).getName()).isEqualTo("BdaName1");
        assertThat((( TSDI )(( TSDI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0))
                .getSDIOrDAI().get(0)).getSDIOrDAI().get(0)).getSDIOrDAI()).hasSize(1);
        assertThat((( TDAI )(( TSDI )(( TSDI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0))
                .getSDIOrDAI().get(0)).getSDIOrDAI().get(0)).getSDIOrDAI().get(0)).getName()).isEqualTo("BdaName2");
        assertThat((( TDAI )(( TSDI )(( TSDI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0))
                .getSDIOrDAI().get(0)).getSDIOrDAI().get(0)).getSDIOrDAI().get(0)).getVal()).hasSize(1);
        assertThat((( TDAI )(( TSDI )(( TSDI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0))
                .getSDIOrDAI().get(0)).getSDIOrDAI().get(0)).getSDIOrDAI().get(0)).getVal().get(0).getValue()).isEqualTo("dai value");
        assertThat((( TDAI )(( TSDI )(( TSDI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(0))
                .getSDIOrDAI().get(0)).getSDIOrDAI().get(0)).getSDIOrDAI().get(0)).getVal().get(0).isSetSGroup()).isFalse();
        //New SDI nad DAI
        assertThat((( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(1)).getName()).isEqualTo("DaName2");
        assertThat((( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(1)).getSDIOrDAI()).hasSize(1);
        assertThat((( TDAI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(1))
                .getSDIOrDAI().get(0)).getName()).isEqualTo("BdaName1");
        assertThat((( TDAI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(1))
                .getSDIOrDAI().get(0)).getVal()).hasSize(1);
        assertThat((( TDAI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(1))
                .getSDIOrDAI().get(0)).getVal().get(0).getValue()).isEqualTo("new dai value");
        assertThat((( TDAI )(( TSDI )(( TSDI )tAnyLN.getDOI().get(0).getSDIOrDAI().get(0)).getSDIOrDAI().get(1))
                .getSDIOrDAI().get(0)).getVal().get(0).isSetSGroup()).isFalse();
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
        tdoi.setName(doInstances.get(0));
        structInstances.remove();
        if(structInstances.size() > 1){
            TSDI firstSDI = createSDIFromDOI(tdoi, structInstances.get(0));
            TSDI lastSDI = createSDIByStructName(firstSDI, structInstances);
            if(structInstances.size() == 1){
                TDAI dai = new TDAI();
                dai.setName(daInstances.get(daInstances.size() - 1));
                TVal tVal = new TVal();
                tVal.setValue(daiVal);
                dai.getVal().add(tVal);
                if (valImport != null) dai.setValImport(valImport);
                lastSDI.getSDIOrDAI().add(dai);
            }
        } else
        if(structInstances.size() == 1){
            TDAI dai = new TDAI();
            dai.setName(daInstances.get(daInstances.size() - 1));
            TVal tVal = new TVal();
            tVal.setValue(daiVal);
            dai.getVal().add(tVal);
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
        tdoi.setName(doInstances.get(0));
        structInstances.remove();
        if(structInstances.size() > 1){
            TSDI firstSDI = createSDIFromDOI(tdoi, structInstances.get(0));
            TSDI lastSDI = createSDIByStructName(firstSDI, structInstances);
            if(structInstances.size() == 1){
                dai.setName(daInstances.get(daInstances.size() - 1));
                TVal tVal = new TVal();
                dai.getVal().add(tVal);
                lastSDI.getSDIOrDAI().add(dai);
                tAnyLN.getDOI().add(tdoi);
            }
        } else
        if(structInstances.size() == 1){
            dai.setName(daInstances.get(daInstances.size() - 1));
            TVal tVal = new TVal();
            dai.getVal().add(tVal);
            tdoi.getSDIOrDAI().add(dai);
            tAnyLN.getDOI().add(tdoi);
        }
        return dai;
    }


}