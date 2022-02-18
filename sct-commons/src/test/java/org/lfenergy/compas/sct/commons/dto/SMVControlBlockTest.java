// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.TMcSecurity;
import org.lfenergy.compas.scl2007b4.model.TPredefinedTypeOfSecurityEnum;
import org.lfenergy.compas.scl2007b4.model.TProtocol;
import org.lfenergy.compas.scl2007b4.model.TSMVSettings;
import org.lfenergy.compas.scl2007b4.model.TSampledValueControl;
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

class SMVControlBlockTest {

    private static final String ID = UUID.randomUUID().toString();
    private static final String DATASET_REF = "DATASET_REF";
    private static final String NAME = "NAME";
    private static final String DESC = "DESCRIPTION";
    @Test
    void testInit(){
        SMVControlBlock smvControlBlock = create();
        assertAll("INIT",
                () -> assertEquals(ID,smvControlBlock.getId()),
                () -> assertEquals(DATASET_REF,smvControlBlock.getDataSetRef()),
                () -> assertEquals(NAME,smvControlBlock.getName()),
                () -> assertEquals(DESC,smvControlBlock.getDesc()),
                () -> assertNotNull(smvControlBlock.getSmvOpts()),
                () -> assertNotNull(smvControlBlock.getProtocol()),
                () -> assertEquals("PROTO",smvControlBlock.getProtocol().getValue()),
                () -> assertTrue(smvControlBlock.getProtocol().isMustUnderstand()),
                () -> assertEquals(TPredefinedTypeOfSecurityEnum.NONE,smvControlBlock.getSecurityEnable())
        );
    }


    @Test
    void TestGetClassType() {
        SMVControlBlock smvControlBlock = new SMVControlBlock();
        assertEquals(SMVControlBlock.class, smvControlBlock.getClassType());
    }

    @Test
    void testGetServiceType() {
        SMVControlBlock smvControlBlock = new SMVControlBlock();
        assertEquals(TServiceType.SMV, smvControlBlock.getServiceType());
    }

    @Test
    void testValidateSecurityEnabledValue() {
        SMVControlBlock smvControlBlock = create();

        assertThrows(ScdException.class, () ->smvControlBlock.validateSecurityEnabledValue((TServices)null));

        final TServices tServices = new TServices();
        assertThrows(ScdException.class, () -> smvControlBlock.validateSecurityEnabledValue(tServices));
        tServices.setSMVSettings(new TSMVSettings());
        assertThrows(ScdException.class, () -> smvControlBlock.validateSecurityEnabledValue(tServices));
        tServices.getSMVSettings().setMcSecurity(new TMcSecurity());
        assertDoesNotThrow(() -> smvControlBlock.validateSecurityEnabledValue(tServices));
        tServices.getSMVSettings().getMcSecurity().setSignature(false);
        smvControlBlock.setSecurityEnable(TPredefinedTypeOfSecurityEnum.SIGNATURE);
        assertThrows(ScdException.class, () -> smvControlBlock.validateSecurityEnabledValue(tServices));
        tServices.getSMVSettings().getMcSecurity().setSignature(true);
        assertDoesNotThrow(() -> smvControlBlock.validateSecurityEnabledValue(tServices));
    }

    @Test
    void testCreateControlBlock() {
        SMVControlBlock smvControlBlock = create();
        TSampledValueControl sampledValueControl = smvControlBlock.createControlBlock();

        assertAll("CREATE CB",
                () -> assertEquals(ID,sampledValueControl.getSmvID()),
                () -> assertEquals(DATASET_REF,sampledValueControl.getDatSet()),
                () -> assertEquals(NAME,sampledValueControl.getName()),
                () -> assertEquals(10000,sampledValueControl.getConfRev()),
                () -> assertEquals(DESC,sampledValueControl.getDesc())
        );

        SMVControlBlock smvControlBlock1 = new SMVControlBlock(sampledValueControl);
        assertAll("INIT",
                () -> assertEquals(ID,smvControlBlock1.getId()),
                () -> assertEquals(DATASET_REF,smvControlBlock1.getDataSetRef()),
                () -> assertEquals(NAME,smvControlBlock1.getName()),
                () -> assertEquals(DESC,smvControlBlock1.getDesc()),
                () -> assertNotNull(smvControlBlock1.getSmvOpts()),
                () -> assertNotNull(smvControlBlock1.getProtocol()),
                () -> assertEquals("PROTO",smvControlBlock1.getProtocol().getValue()),
                () -> assertTrue(smvControlBlock1.getProtocol().isMustUnderstand()),
                () -> assertEquals(TPredefinedTypeOfSecurityEnum.NONE,smvControlBlock1.getSecurityEnable())
        );
    }

    private SMVControlBlock create(){

        SMVControlBlock smvControlBlock = new SMVControlBlock();
        smvControlBlock.setId(ID);
        smvControlBlock.setDataSetRef(DATASET_REF);
        smvControlBlock.setConfRev(1L);
        smvControlBlock.setName(NAME);
        TSampledValueControl.SmvOpts smvOpts = new TSampledValueControl.SmvOpts();

        smvControlBlock.setSmvOpts(smvOpts);
        TProtocol protocol = new TProtocol();
        protocol.setValue("PROTO");
        protocol.setMustUnderstand(true);
        smvControlBlock.setProtocol(protocol);

        smvControlBlock.setDesc(DESC);

        return smvControlBlock;
    }
}