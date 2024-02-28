// SPDX-FileCopyrightText: 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DataTypeTemplatesServiceTest {

    @Test
    void isDoModAndDaStValExist_when_LNodeType_not_exist_should_return_false() {
        //Given
        SCL scl = new SCL();
        TDataTypeTemplates dtt = new TDataTypeTemplates();
        scl.setDataTypeTemplates(dtt);
        //When
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        boolean result = dataTypeTemplatesService.isDoModAndDaStValExist(dtt, "lnodeTypeId");
        //Then
        assertThat(result).isFalse();
    }


    @Test
    void isDoModAndDaStValExist_when_Do_not_exist_should_return_false() {
        //Given
        SCL scl = new SCL();
        TDataTypeTemplates dtt = new TDataTypeTemplates();
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("lnodeTypeId");
        dtt.getLNodeType().add(tlNodeType);
        scl.setDataTypeTemplates(dtt);
        //When
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        boolean result = dataTypeTemplatesService.isDoModAndDaStValExist(dtt, "lnodeTypeId");
        //Then
        assertThat(result).isFalse();
    }

    @Test
    void isDoModAndDaStValExist_when_DoType_not_exist_should_return_false() {
        //Given
        SCL scl = new SCL();
        TDataTypeTemplates dtt = new TDataTypeTemplates();
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("lnodeTypeId");
        TDO tdo = new TDO();
        tdo.setType("doTypeId");
        tdo.setName("Mod");
        tlNodeType.getDO().add(tdo);
        dtt.getLNodeType().add(tlNodeType);
        //When
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        boolean result = dataTypeTemplatesService.isDoModAndDaStValExist(dtt, "lnodeTypeId");
        //Then
        assertThat(result).isFalse();
    }


    @Test
    void isDoModAndDaStValExist_when_Da_Mod_not_exist_should_return_false() {
        //Given
        SCL scl = new SCL();
        TDataTypeTemplates dtt = new TDataTypeTemplates();
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("lnodeTypeId");
        TDO tdo = new TDO();
        tdo.setType("doTypeId");
        tdo.setName("Mod");
        tlNodeType.getDO().add(tdo);
        dtt.getLNodeType().add(tlNodeType);
        TDOType tdoType = new TDOType();
        tdoType.setId("doTypeId");
        dtt.getDOType().add(tdoType);
        scl.setDataTypeTemplates(dtt);
        //When
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        boolean result = dataTypeTemplatesService.isDoModAndDaStValExist(dtt, "lnodeTypeId");
        //Then
        assertThat(result).isFalse();
    }


    @Test
    void isDoModAndDaStValExist_when_Da_stVal_not_found_should_return_false() {
        //Given
        SCL scl = new SCL();
        TDataTypeTemplates dtt = new TDataTypeTemplates();
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("lnodeTypeId");
        TDO tdo = new TDO();
        tdo.setType("doTypeId");
        tdo.setName("Mod");
        tlNodeType.getDO().add(tdo);
        dtt.getLNodeType().add(tlNodeType);
        TDOType tdoType = new TDOType();
        tdoType.setId("doTypeId");
        TDA tda = new TDA();
        tda.setName("daName");
        tdoType.getSDOOrDA().add(tda);
        dtt.getDOType().add(tdoType);
        scl.setDataTypeTemplates(dtt);
        //When
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        boolean result = dataTypeTemplatesService.isDoModAndDaStValExist(dtt, "lnodeTypeId");
        //Then
        assertThat(result).isFalse();
    }


    @Test
    void isDoModAndDaStValExist_when_DO_Mod_And_DA_stVal_exist_return_true() {
        //Given
        SCL scl = new SCL();
        TDataTypeTemplates dtt = new TDataTypeTemplates();
        TLNodeType tlNodeType = new TLNodeType();
        tlNodeType.setId("lnodeTypeId");
        TDO tdo = new TDO();
        tdo.setType("doTypeId");
        tdo.setName("Mod");
        tlNodeType.getDO().add(tdo);
        dtt.getLNodeType().add(tlNodeType);
        TDOType tdoType = new TDOType();
        tdoType.setId("doTypeId");
        TDA tda = new TDA();
        tda.setName("stVal");
        tdoType.getSDOOrDA().add(tda);
        dtt.getDOType().add(tdoType);
        scl.setDataTypeTemplates(dtt);
        //When
        DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
        boolean result = dataTypeTemplatesService.isDoModAndDaStValExist(dtt, "lnodeTypeId");
        //Then
        assertThat(result).isTrue();
    }
}