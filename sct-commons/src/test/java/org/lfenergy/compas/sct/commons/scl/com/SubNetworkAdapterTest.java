// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.com;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.lfenergy.compas.scl2007b4.model.TCommunication;
import org.lfenergy.compas.scl2007b4.model.TConnectedAP;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.scl2007b4.model.TSubNetwork;
import org.lfenergy.compas.sct.commons.dto.DTO;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubNetworkAdapterTest {

    @Test
    void constructor_whenCalledWithNoRelationBetweenCommunicationAndSubNetwork_shouldThrowException() {
        //Given
        CommunicationAdapter communicationAdapter = mock(CommunicationAdapter.class);
        when(communicationAdapter.getCurrentElem()).thenReturn(new TCommunication());
        TSubNetwork subNetwork = new TSubNetwork();
        //When Then
        assertThatCode(() -> new SubNetworkAdapter(communicationAdapter,subNetwork))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No relation between SCL parent element and child");
    }

    @Test
    void constructor_whenCalledWithExistingRelationBetweenCommunicationAndSubNetwork_shouldNotThrowException() {
        //Given
        CommunicationAdapter communicationAdapter = mock(CommunicationAdapter.class);
        TCommunication communication = new TCommunication();
        TSubNetwork subNetwork = new TSubNetwork();
        communication.getSubNetwork().add(subNetwork);
        when(communicationAdapter.getCurrentElem()).thenReturn(communication);
        //When Then
        assertThatCode(() -> new SubNetworkAdapter(communicationAdapter, subNetwork))
                .doesNotThrowAnyException();
    }

    @Test
    @Tag("issue-321")
    void addConnectedAP_should_update_list_of_connectedAp() {
        // Given
        SubNetworkAdapter subNetworkAdapter = new SubNetworkAdapter(null, new TSubNetwork());
        assertThat(subNetworkAdapter.getCurrentElem().getConnectedAP()).isEmpty();
        // When
        subNetworkAdapter.addConnectedAP(DTO.HOLDER_IED_NAME,DTO.AP_NAME);
        // Then
        assertThat(subNetworkAdapter.getCurrentElem().getConnectedAP()).hasSize(1);
        TConnectedAP tConnectedAP = subNetworkAdapter.getCurrentElem().getConnectedAP().get(0);
        assertThat(tConnectedAP.getIedName()).isEqualTo(DTO.HOLDER_IED_NAME);
        assertThat(tConnectedAP.getApName()).isEqualTo(DTO.AP_NAME);
        // When
        subNetworkAdapter.addConnectedAP(DTO.HOLDER_IED_NAME,DTO.AP_NAME);
        // Then
        assertThat(subNetworkAdapter.getCurrentElem().getConnectedAP()).hasSize(1);
    }

    @Test
    @Tag("issue-321")
    void getConnectedAPAdapters_should_return_all_ConnectedAP() {
        // Given
        SubNetworkAdapter subNetworkAdapter = new SubNetworkAdapter(null, new TSubNetwork());
        // When
        subNetworkAdapter.addConnectedAP(DTO.HOLDER_IED_NAME, DTO.AP_NAME);
        // When
        subNetworkAdapter.addConnectedAP(DTO.HOLDER_IED_NAME_2, DTO.AP_NAME_2);
        // When
        List<ConnectedAPAdapter> connectedAPAdapter = subNetworkAdapter.getConnectedAPAdapters();
        // Then
        assertThat(connectedAPAdapter).extracting(ConnectedAPAdapter::getIedName, ConnectedAPAdapter::getApName)
            .containsExactly(
                Tuple.tuple(DTO.HOLDER_IED_NAME, DTO.AP_NAME),
                Tuple.tuple(DTO.HOLDER_IED_NAME_2, DTO.AP_NAME_2)
            );
    }

    @Test
    @Tag("issue-321")
    void getConnectedAPAdapter_should_get_element() {
        // Given
        SubNetworkAdapter subNetworkAdapter = new SubNetworkAdapter(null, new TSubNetwork());
        // When
        subNetworkAdapter.addConnectedAP(DTO.HOLDER_IED_NAME,DTO.AP_NAME);
        // When
        ConnectedAPAdapter connectedAPAdapter = subNetworkAdapter.getConnectedAPAdapter(DTO.HOLDER_IED_NAME, DTO.AP_NAME);
        // Then
        assertThat(connectedAPAdapter).extracting(ConnectedAPAdapter::getIedName, ConnectedAPAdapter::getApName)
                .containsExactly(DTO.HOLDER_IED_NAME, DTO.AP_NAME);
    }

    @Test
    void getConnectedAPAdapter_when_not_found_should_throw_exception() {
        // Given
        SubNetworkAdapter subNetworkAdapter = new SubNetworkAdapter(null, new TSubNetwork());
        // When & Then
        Assertions.assertThatThrownBy(() -> subNetworkAdapter.getConnectedAPAdapter(DTO.HOLDER_IED_NAME, DTO.AP_NAME))
            .isInstanceOf(ScdException.class);
    }

    @Test
    @Tag("issue-321")
    void findConnectedAPAdapter_should_get_element() {
        // Given
        SubNetworkAdapter subNetworkAdapter = new SubNetworkAdapter(null, new TSubNetwork());
        // When
        subNetworkAdapter.addConnectedAP(DTO.HOLDER_IED_NAME,DTO.AP_NAME);
        // When
        Optional<ConnectedAPAdapter> connectedAPAdapter = subNetworkAdapter.findConnectedAPAdapter(DTO.HOLDER_IED_NAME, DTO.AP_NAME);
        // Then
        assertThat(connectedAPAdapter).get().extracting(ConnectedAPAdapter::getIedName, ConnectedAPAdapter::getApName)
                .containsExactly(DTO.HOLDER_IED_NAME, DTO.AP_NAME);
    }

    @Test
    void findConnectedAPAdapter_when_not_found_should_return_empty() {
        // Given
        SubNetworkAdapter subNetworkAdapter = new SubNetworkAdapter(null, new TSubNetwork());
        // When
        Optional<ConnectedAPAdapter> connectedAPAdapter = subNetworkAdapter.findConnectedAPAdapter(DTO.HOLDER_IED_NAME, DTO.AP_NAME);
        // Then
        assertThat(connectedAPAdapter).isEmpty();
    }

    @Test
    void addPrivate_with_type_and_source_should_create_Private() {
        //Given
        SubNetworkAdapter subNetworkAdapter = new SubNetworkAdapter(null, new TSubNetwork());
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertThat(subNetworkAdapter.getCurrentElem().getPrivate()).isEmpty();
        //When
        subNetworkAdapter.addPrivate(tPrivate);
        //Then
        assertThat(subNetworkAdapter.getCurrentElem().getPrivate()).isNotEmpty();
    }

    @ParameterizedTest
    @CsvSource(value = {"sName;sType;SubNetwork[@name=\"sName\"]", ";;SubNetwork[not(@name)]"}
            , delimiter = ';')
    void elementXPath_should_return_expected_xpath_value(String sName, String sType, String message) {
        // Given
        TSubNetwork tSubNetwork = new TSubNetwork();
        tSubNetwork.setName(sName);
        tSubNetwork.setType(sType);
        SubNetworkAdapter subNetworkAdapter = new SubNetworkAdapter(null, tSubNetwork);
        // When
        String elementXPath = subNetworkAdapter.elementXPath();
        // Then
        assertThat(elementXPath).isEqualTo(message);
    }

}
