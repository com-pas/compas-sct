// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TFCEnum;
import org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.scl2007b4.model.TPredefinedCDCEnum;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.*;

public class DaTypeNameTest {

    @Test
    void testConstructor(){
        DaTypeName daTypeName = new DaTypeName("da.bda1.bda2");
        daTypeName.setFc(TFCEnum.CF);
        daTypeName.setBType(TPredefinedBasicTypeEnum.CHECK);
        daTypeName.setType("IfExist_Its_EnumType");
        assertEquals("da",daTypeName.getName());
        assertEquals(TFCEnum.CF,daTypeName.getFc());
        assertThat(daTypeName.getStructNames(), contains("bda1","bda2"));

        DaTypeName daTypeName1 = new DaTypeName("da","bda1.bda2");
        daTypeName1.setFc(TFCEnum.CF);
        daTypeName1.setBType(TPredefinedBasicTypeEnum.CHECK);
        daTypeName1.setType("IfExist_Its_EnumType");
        assertEquals(daTypeName,daTypeName1);
        assertEquals(daTypeName.hashCode(),daTypeName1.hashCode());

        daTypeName1.setType("toto");
        assertNotEquals(daTypeName,daTypeName1);

        daTypeName1.setBType(TPredefinedBasicTypeEnum.DBPOS);
        assertNotEquals(daTypeName,daTypeName1);

        daTypeName1.setFc(TFCEnum.BL);
        assertNotEquals(daTypeName,daTypeName1);

    }
}