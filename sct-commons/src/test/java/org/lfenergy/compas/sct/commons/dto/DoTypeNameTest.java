// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TPredefinedCDCEnum;

import static org.junit.jupiter.api.Assertions.*;

class DoTypeNameTest {

    @Test
    void testConstructor(){
        DoTypeName doTypeName = new DoTypeName("do.Sdo1.Sdo2");
        doTypeName.setCdc(TPredefinedCDCEnum.WYE);
        assertEquals("do",doTypeName.getName());
        assertEquals(TPredefinedCDCEnum.WYE,doTypeName.getCdc());
        assertThat(doTypeName.getStructNames(), contains("Sdo1","Sdo2"));

        DoTypeName doTypeName2 = new DoTypeName("do","Sdo1.Sdo2");
        doTypeName2.setCdc(TPredefinedCDCEnum.WYE);
        assertEquals(doTypeName,doTypeName2);
        assertEquals(doTypeName.hashCode(),doTypeName2.hashCode());

        doTypeName2.setCdc(TPredefinedCDCEnum.ACD);
        assertNotEquals(doTypeName,doTypeName2);
    }

}