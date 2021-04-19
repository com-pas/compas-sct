// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.model.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl.TExtRef;
import org.lfenergy.compas.scl.TInputs;
import org.lfenergy.compas.scl.TLN;
import org.lfenergy.compas.sct.testhelper.DTO;

import static org.junit.jupiter.api.Assertions.*;

class LNodeDTOTest {


    @Test
    public void testConstruction(){
        LNodeDTO lNodeDTO = DTO.createLNodeDTO();
        lNodeDTO.addExtRef(DTO.createExtRefDTO());
        lNodeDTO.addResumedDataTemplate(new ResumedDataTemplate());

        assertAll("LNodeDTO",
            () -> assertEquals(DTO.LN_INST, lNodeDTO.getInst()),
            () -> assertEquals(DTO.LN_CLASS, lNodeDTO.getLNodeClass()),
            () -> assertEquals(DTO.LN_TYPE, lNodeDTO.getLNodeType()),
            () -> assertFalse(lNodeDTO.getExtRefs().isEmpty()),
            () -> assertFalse(lNodeDTO.getResumedDataTemplates().isEmpty())
        );
        LNodeDTO lNodeDTO1 = new LNodeDTO();
        lNodeDTO1.setInst(DTO.LN_INST);
        lNodeDTO1.setLNodeClass(DTO.LN_CLASS);
        lNodeDTO1.setLNodeType(DTO.LN_TYPE);
        lNodeDTO1.setExtRefs(lNodeDTO.getExtRefs());
        lNodeDTO1.setResumedDataTemplates(lNodeDTO.getResumedDataTemplates());

        assertAll("LNodeDTO1",
                () -> assertEquals(DTO.LN_INST, lNodeDTO1.getInst()),
                () -> assertEquals(DTO.LN_CLASS, lNodeDTO1.getLNodeClass()),
                () -> assertEquals(DTO.LN_TYPE, lNodeDTO1.getLNodeType()),
                () -> assertFalse(lNodeDTO1.getExtRefs().isEmpty()),
                () -> assertFalse(lNodeDTO1.getResumedDataTemplates().isEmpty())
        );
    }

    @Test
    public void testExtractData(){
        TLN tln = new TLN();
        tln.setInst(DTO.LN_INST);
        tln.setLnType(DTO.LN_TYPE);
        tln.getLnClass().add(DTO.LN_CLASS);
        TExtRef tExtRef = DTO.createExtRef();
        TInputs tInputs = new TInputs();
        tInputs.getExtRef().add(tExtRef);
        tln.setInputs(tInputs);

        LNodeDTO lNodeDTO = LNodeDTO.extractData(tln);
        assertAll("LNodeDTO",
                () -> assertEquals(DTO.LN_INST, lNodeDTO.getInst()),
                () -> assertEquals(DTO.LN_CLASS, lNodeDTO.getLNodeClass()),
                () -> assertEquals(DTO.LN_TYPE, lNodeDTO.getLNodeType())
        );
        assertTrue(!lNodeDTO.getExtRefs().isEmpty());
    }
}