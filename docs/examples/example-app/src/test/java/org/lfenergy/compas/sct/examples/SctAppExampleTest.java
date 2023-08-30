// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.examples;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import javax.xml.bind.JAXBException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


class SctAppExampleTest {

    @Test
    void initSclWithSclServiceTest() throws JAXBException {
        // Given : Header attributes
        UUID headerId = UUID.randomUUID();
        String headerVersion = SclRootAdapter.VERSION;
        String headerRevision = SclRootAdapter.REVISION;
        // When: Sct Service
        SCL scl = SctAppExample.initSclWithSclService(headerId, headerVersion, headerRevision);
        // Then
        assertThat(scl.getHeader()).isNotNull();
        assertThat(scl.getHeader().getId()).isEqualTo(headerId.toString());
        assertThat(scl.getHeader().getVersion()).isEqualTo(headerVersion);
        assertThat(scl.getHeader().getRevision()).isEqualTo(headerRevision);
        THeader.History history = scl.getHeader().getHistory();
        List<TSubstation> substations = scl.getSubstation();
        TCommunication communication = scl.getCommunication();
        List<TIED> iedList = scl.getIED();
        TDataTypeTemplates dataTypeTemplates = scl.getDataTypeTemplates();
        assertThat(history).isNull();
        assertThat(substations).isEmpty();
        assertThat(communication).isNull();
        assertThat(iedList).isEmpty();
        assertThat(dataTypeTemplates).isNull();
    }

}
