// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;

import static org.junit.jupiter.api.Assertions.*;

public class ResumedDataTemplateTest {

    @Test
    public void testGetObjRef(){
        String expected = "IEDLDTM/prelnclass1.do.sdo1.sdo2.da.bda1.bda2";
        ResumedDataTemplate resumedDataTemplate = DTO.createRTT("pre","lnclass","1");
        String objRef = resumedDataTemplate.getObjRef("IED","LDTM");
        assertEquals(expected,objRef);
    }
    @Test
    public void testCopyFrom() {
        ResumedDataTemplate rDtt = DTO.createRTT("pre","lnclass","1");
        ResumedDataTemplate rDtt_b = ResumedDataTemplate.copyFrom(rDtt);

        assertAll("COPY FROM",
                () -> assertEquals(rDtt_b.getLnClass(), rDtt.getLnClass()),
                () -> assertEquals(rDtt_b.getBType(), rDtt.getBType()),
                () -> assertEquals(rDtt_b.getType(), rDtt.getType()),
                () -> assertEquals(rDtt_b.getPrefix(), rDtt.getPrefix()),
                () -> assertEquals(rDtt_b.getCdc(), rDtt.getCdc()),
                () -> assertEquals(rDtt_b.getDaName(), rDtt.getDaName()),
                () -> assertEquals(rDtt_b.getDoName(), rDtt.getDoName()),
                () -> assertEquals(rDtt_b.getLnInst(), rDtt.getLnInst()),
                () -> assertEquals(rDtt_b.getLnType(), rDtt.getLnType()),
                () -> assertArrayEquals(rDtt_b.getBdaNames().toArray(), rDtt.getBdaNames().toArray()),
                () -> assertArrayEquals(rDtt_b.getSdoNames().toArray(), rDtt.getSdoNames().toArray()),
                () -> assertEquals(rDtt_b.getFc(), rDtt.getFc()),
                () -> assertTrue(rDtt.isValImport()),
                () -> assertArrayEquals(rDtt.getDaRefList().toArray(new String[0]), new String[]{"da","bda1","bda2"})
        );
    }

    @Test
    public void testIsUpdatable(){
        ResumedDataTemplate rDtt = DTO.createRTT("pre","lnclass","1");
        assertTrue(rDtt.isUpdatable());

        rDtt.setFc(TFCEnum.BL);
        assertFalse(rDtt.isUpdatable());
    }
}