// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObjectReferenceTest {

    @Test
    void testConstructor(){
        String ref = "IED_NAME_LD_NAME/PR_LN_INST.doi.sdoi.sdai.bdai.bda";
        ObjectReference objRef = new ObjectReference(ref);
        objRef.init();
        assertEquals("IED_NAME_LD_NAME",objRef.getLdName());
        assertEquals("PR_LN_INST",objRef.getLNodeName());
        assertEquals("doi.sdoi.sdai.bdai.bda",objRef.getDataAttributes());

        String ref0 = "IED_NAME_LD_NAMEPR_LN_INST.doi.sdoi.sdai.bdai.bda";
        assertThrows(IllegalArgumentException.class, () ->new ObjectReference(ref0));


        String ref1 = "IED_NAME_LD_NAME/PR_LN_INST";
        assertThrows(IllegalArgumentException.class, () ->new ObjectReference(ref1));
    }

}