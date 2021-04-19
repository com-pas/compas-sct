// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.model.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl.TFCEnum;
import org.lfenergy.compas.scl.TLLN0Enum;
import org.lfenergy.compas.scl.TPredefinedCDCEnum;
import org.lfenergy.compas.sct.model.dto.ResumedDataTemplate;

import static org.junit.jupiter.api.Assertions.*;

class ResumedDataTemplateTest {


    @Test
    void testResumedDataTemplate(){
        ResumedDataTemplate rDTT = new ResumedDataTemplate();
        rDTT.setCdc(TPredefinedCDCEnum.WYE);
        rDTT.setFc(TFCEnum.CF);
        rDTT.setLnType("LN1");
        rDTT.setLnClass(TLLN0Enum.LLN_0.value());
        rDTT.setDoName("FACntRs1.res");
        rDTT.setDaName("d");

        System.out.println(rDTT);

        assertAll("RDTT",
                () -> assertEquals(TPredefinedCDCEnum.WYE, rDTT.getCdc()),
                () -> assertEquals(TFCEnum.CF, rDTT.getFc()),
                () -> assertEquals("LN1", rDTT.getLnType()),
                () -> assertEquals(TLLN0Enum.LLN_0.value(), rDTT.getLnClass()),
                () -> assertEquals("FACntRs1.res", rDTT.getDoName()),
                () -> assertEquals("d", rDTT.getDaName())
        );
    }
}