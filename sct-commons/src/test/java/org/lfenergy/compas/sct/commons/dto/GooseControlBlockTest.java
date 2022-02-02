// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TGSEControl;
import org.lfenergy.compas.scl2007b4.model.TGSESettings;
import org.lfenergy.compas.scl2007b4.model.TMcSecurity;
import org.lfenergy.compas.scl2007b4.model.TPredefinedTypeOfSecurityEnum;
import org.lfenergy.compas.scl2007b4.model.TProtocol;
import org.lfenergy.compas.scl2007b4.model.TServiceType;
import org.lfenergy.compas.scl2007b4.model.TServices;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GooseControlBlockTest {
    private static final String ID = UUID.randomUUID().toString();
    private static final String DATASET_REF = "DATASET_REF";
    private static final String NAME = "NAME";
    private static final String DESC = "DESCRIPTION";

    @Test
    void testInit(){
        GooseControlBlock gooseControlBlock = create();
        assertAll("INIT",
                () -> assertEquals(ID,gooseControlBlock.getId()),
                () -> assertEquals(DATASET_REF,gooseControlBlock.getDataSetRef()),
                () -> assertEquals(NAME,gooseControlBlock.getName()),
                () -> assertEquals(DESC,gooseControlBlock.getDesc()),
                () -> assertNotNull(gooseControlBlock.getProtocol()),
                () -> assertEquals("PROTO",gooseControlBlock.getProtocol().getValue()),
                () -> assertTrue(gooseControlBlock.getProtocol().isMustUnderstand()),
                () -> assertEquals(TPredefinedTypeOfSecurityEnum.NONE,gooseControlBlock.getSecurityEnable())
        );
    }

    @Test
    void testGetClassType() {
        GooseControlBlock gooseControlBlock = new GooseControlBlock();
        assertEquals(GooseControlBlock.class, gooseControlBlock.getClassType());
    }

    @Test
    void testGetServiceType() {
        GooseControlBlock gooseControlBlock = new GooseControlBlock();
        assertEquals(TServiceType.GOOSE, gooseControlBlock.getServiceType());
    }

    @Test
    void testValidateSecurityEnabledValue() {
        GooseControlBlock gooseControlBlock = create();

        assertThrows(ScdException.class, () ->gooseControlBlock.validateSecurityEnabledValue((TServices) null));

        final TServices tServices = new TServices();
        assertThrows(ScdException.class, () -> gooseControlBlock.validateSecurityEnabledValue(tServices));
        tServices.setGSESettings(new TGSESettings());
        assertThrows(ScdException.class, () -> gooseControlBlock.validateSecurityEnabledValue(tServices));
        tServices.getGSESettings().setMcSecurity(new TMcSecurity());
        assertDoesNotThrow(() -> gooseControlBlock.validateSecurityEnabledValue(tServices));
        tServices.getGSESettings().getMcSecurity().setSignature(false);
        gooseControlBlock.setSecurityEnable(TPredefinedTypeOfSecurityEnum.SIGNATURE);
        assertThrows(ScdException.class, () -> gooseControlBlock.validateSecurityEnabledValue(tServices));
        tServices.getGSESettings().getMcSecurity().setSignature(true);
        assertDoesNotThrow(() -> gooseControlBlock.validateSecurityEnabledValue(tServices));

    }

    @Test
    void ShouldReturnOKWhenCreateControlBlock() {
        GooseControlBlock gooseControlBlock = create();
        TGSEControl tgseControl = gooseControlBlock.createControlBlock();
        assertAll("CREATE CB",
                () -> assertEquals(ID,tgseControl.getAppID()),
                () -> assertEquals(DATASET_REF,tgseControl.getDatSet()),
                () -> assertEquals(NAME,tgseControl.getName()),
                () -> assertEquals(10000,tgseControl.getConfRev()),
                () -> assertEquals(DESC,tgseControl.getDesc())
        );

        GooseControlBlock gooseControlBlock1 = new GooseControlBlock(tgseControl);
        assertAll("INIT",
                () -> assertEquals(ID,gooseControlBlock1.getId()),
                () -> assertEquals(DATASET_REF,gooseControlBlock1.getDataSetRef()),
                () -> assertEquals(NAME,gooseControlBlock1.getName()),
                () -> assertEquals(DESC,gooseControlBlock1.getDesc()),
                () -> assertNotNull(gooseControlBlock1.getProtocol()),
                () -> assertEquals("PROTO",gooseControlBlock1.getProtocol().getValue()),
                () -> assertTrue(gooseControlBlock1.getProtocol().isMustUnderstand()),
                () -> assertEquals(TPredefinedTypeOfSecurityEnum.NONE,gooseControlBlock1.getSecurityEnable())
        );
    }


    private GooseControlBlock create(){
        GooseControlBlock gooseControlBlock = new GooseControlBlock();

        gooseControlBlock.setId(ID);
        gooseControlBlock.setDataSetRef(DATASET_REF);
        gooseControlBlock.setConfRev(1L);
        gooseControlBlock.setName(NAME);

        gooseControlBlock.setFixedOffs(false);
        TProtocol protocol = new TProtocol();
        protocol.setValue("PROTO");
        protocol.setMustUnderstand(true);
        gooseControlBlock.setProtocol(protocol);

        gooseControlBlock.setDesc(DESC);

        return gooseControlBlock;
    }
}