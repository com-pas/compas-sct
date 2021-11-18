// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TFCDA;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FCDAInfoTest {

    @Test
    void testConstructor(){

        TFCDA tfcda = new TFCDA();
        tfcda.setDoName("doName");
        tfcda.setDaName("daName.bda1.bda2.bda3");
        tfcda.setLdInst("LDInst");
        tfcda.setFc(TFCEnum.CF);
        tfcda.getLnClass().add("LN_Class");
        tfcda.setLnInst("LNInst");
        tfcda.setPrefix("pre");
        tfcda.setIx(1L);

        FCDAInfo fcdaInfo = new FCDAInfo(tfcda);
        assertEquals("daName",fcdaInfo.getDaName().getName());
        assertEquals("doName",fcdaInfo.getDoName().getName());
        assertEquals(tfcda.getDaName(),fcdaInfo.getDaName().toString());
        assertEquals(3,fcdaInfo.getDaName().getStructNames().size());

        FCDAInfo fcdaInfo1 = new FCDAInfo();
        fcdaInfo1.setIx(fcdaInfo.getIx());
        fcdaInfo1.setLdInst(fcdaInfo.getLdInst());
        fcdaInfo1.setLnInst(fcdaInfo.getLnInst());
        fcdaInfo1.setLnClass(fcdaInfo.getLnClass());
        fcdaInfo1.setDaName(fcdaInfo.getDaName());
        fcdaInfo1.setDoName(fcdaInfo.getDoName());
        fcdaInfo1.setFc(fcdaInfo.getFc());
        fcdaInfo1.setPrefix(fcdaInfo.getPrefix());
        assertEquals("daName",fcdaInfo1.getDaName().getName());
        assertEquals("doName",fcdaInfo1.getDoName().getName());
        assertEquals(tfcda.getDaName(),fcdaInfo1.getDaName().toString());
    }
}