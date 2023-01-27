// SPDX-FileCopyrightText: 2022 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.examples;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import javax.xml.bind.JAXBException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


public class SctAppExampleTest {

    @Test
    public void initSclWithSclServiceTest() throws JAXBException {
        // Given : Header attributes
        Optional<UUID> headerId = Optional.of(UUID.randomUUID());
        String headerVersion = SclRootAdapter.VERSION;
        String headerRevision = SclRootAdapter.REVISION;
        // When: Sct Service
        SclRootAdapter scl = SctAppExample
                .initSclWithSclService(headerId, headerVersion, headerRevision);
        // Then
        assertNotNull(scl.getCurrentElem().getHeader());
        assertEquals(headerId.get().toString(), scl.getCurrentElem().getHeader().getId());
        assertEquals(headerVersion, scl.getCurrentElem().getHeader().getVersion());
        assertEquals(headerRevision, scl.getCurrentElem().getHeader().getRevision());
        THeader.History history = scl.getCurrentElem().getHeader().getHistory();
        List<TSubstation> substations = scl.getCurrentElem().getSubstation();
        TCommunication communication = scl.getCurrentElem().getCommunication();
        List<TIED> iedList = scl.getCurrentElem().getIED();
        TDataTypeTemplates dataTypeTemplates = scl.getCurrentElem().getDataTypeTemplates();
        assertNull(history);
        assertEquals(0, substations.size());
        assertNull(communication);
        assertEquals(0, iedList.size());
        assertNull(dataTypeTemplates);
    }

}
