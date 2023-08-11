// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.com;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CommunicationAdapterTest {

    @Test
    void constructor_whenCalledWithNoRelationBetweenSCLAndCommunication_shouldThrowException() {
        //Given
        SclRootAdapter sclRootAdapter = mock(SclRootAdapter.class);
        when(sclRootAdapter.getCurrentElem()).thenReturn(new SCL());
        TCommunication communication = new TCommunication();
        //When Then
        assertThatCode(() -> new CommunicationAdapter(sclRootAdapter, communication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No relation between SCL parent element and child");
    }

    @Test
    void constructor_whenCalledWithExistingRelationBetweenSCLAndCommunication_shouldNotThrowException() {
        //Given
        SclRootAdapter sclRootAdapter = mock(SclRootAdapter.class);
        SCL scl = new SCL();
        TCommunication communication = new TCommunication();
        scl.setCommunication(communication);
        when(sclRootAdapter.getCurrentElem()).thenReturn(scl);
        //When Then
        assertThatCode(() -> new CommunicationAdapter(sclRootAdapter, communication)).doesNotThrowAnyException();
    }

    @Test
    @Tag("issue-321")
    void testAddSubnetwork() {
        // Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        TIED tied = new TIED();
        tied.setName("IED_NAME");
        TAccessPoint tAccessPoint = new TAccessPoint();
        tAccessPoint.setName("apName");
        TAccessPoint tAccessPoint2 = new TAccessPoint();
        tAccessPoint2.setName("apName2");
        tied.getAccessPoint().add(tAccessPoint);
        tied.getAccessPoint().add(tAccessPoint2);
        sclRootAdapter.getCurrentElem().getIED().add(tied);
        CommunicationAdapter communicationAdapter = sclRootAdapter.getCommunicationAdapter(true);
        // When Then
        assertThatCode(() -> communicationAdapter.addSubnetwork("snName", "snType",
                "IED_NAME", "apName"))
                .doesNotThrowAnyException();
        // When Then
        assertThatCode(() -> communicationAdapter.addSubnetwork("snName", "snType",
                "IED_NAME", "apName2"))
                .doesNotThrowAnyException();
        // When Then
        assertThatCode(() -> communicationAdapter.addSubnetwork("snName", "snType",
                "IED_NAME1", "apName"))
                .isInstanceOf(ScdException.class)
                .hasMessage("IED.name 'IED_NAME1' not found in SCD");
        // When Then
        assertThatCode(() -> communicationAdapter.addSubnetwork("snName", "snType",
                "IED_NAME", "apName1"))
                .isInstanceOf(ScdException.class)
                .hasMessage("Unknown AccessPoint :apName1 in IED :IED_NAME");
    }

    @Test
    void getSubnetworkByName_should_return_SubNetworkAdapter() {
        // Given
        SclRootAdapter sclRootAdapter = new SclRootAdapter("hID","hVersion","hRevision");
        CommunicationAdapter communicationAdapter = sclRootAdapter.getCommunicationAdapter(true);
        TSubNetwork tSubNetwork = new TSubNetwork();
        tSubNetwork.setName("snName");
        communicationAdapter.getCurrentElem().getSubNetwork().add(tSubNetwork);
        // When Then
        assertThat(communicationAdapter.getSubnetworkByName("snName")).isPresent();
        assertThat(communicationAdapter.getSubnetworkByName("snName_NO")).isEmpty();
    }

    @Test
    void getSubNetworkAdapters_should_return_list_of_SubNetworkAdapter(){
        // Given
        CommunicationAdapter communicationAdapter = new CommunicationAdapter(null,new TCommunication());
        assertThat(communicationAdapter.getSubNetworkAdapters()).isEmpty();
        TSubNetwork tSubNetwork = new TSubNetwork();
        tSubNetwork.setName("snName");
        communicationAdapter.getCurrentElem().getSubNetwork().add(tSubNetwork);
        // When Then
        assertThat(communicationAdapter.getSubNetworkAdapters()).isNotEmpty();
    }

    @Test
    void addPrivate_with_type_and_source_should_create_Private() {
        // Given
        CommunicationAdapter communicationAdapter = new CommunicationAdapter(null,new TCommunication());
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertThat(communicationAdapter.getCurrentElem().getPrivate()).isEmpty();
        // When
        communicationAdapter.addPrivate(tPrivate);
        // Then
        assertThat(communicationAdapter.getCurrentElem().getPrivate()).isNotEmpty();
    }

    @Test
    void elementXPath_should_return_expected_xpath_value() {
        // Given
        CommunicationAdapter communicationAdapter = new CommunicationAdapter(null,new TCommunication());
        // When
        String result = communicationAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("Communication");
    }

}