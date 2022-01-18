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

        ref = "IED_NAME_LD_NAMEPR_LN_INST.doi.sdoi.sdai.bdai.bda";
        ObjectReference objRef1 = new ObjectReference(ref);
        assertThrows(IllegalArgumentException.class, () -> objRef1.init());

        ref = "IED_NAME_LD_NAME/PR_LN_INST";
        ObjectReference objRef2 = new ObjectReference(ref);
        assertThrows(IllegalArgumentException.class, () -> objRef2.init());
    }

}