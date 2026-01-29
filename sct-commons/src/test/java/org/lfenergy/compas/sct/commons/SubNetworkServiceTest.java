// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TCommunication;
import org.lfenergy.compas.scl2007b4.model.TSubNetwork;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class SubNetworkServiceTest {

    private final SubNetworkService subNetworkService = new SubNetworkService();

    @Test
    void getSubNetworks_from_scd_should_succeed() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("scd-ied-dtt-com-import-stds/std.xml");
        // When
        List<TSubNetwork> tSubNetworks =  subNetworkService.getSubNetworks(scd).toList();
        // Then
        assertThat(tSubNetworks).hasSize(2);
    }

    @Test
    void getSubNetworks_from_communication_should_succeed() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("scd-ied-dtt-com-import-stds/std.xml");
        TCommunication communication = scd.getCommunication();

        // When
        List<TSubNetwork> tSubNetworks =  subNetworkService.getSubNetworks(communication).toList();
        // Then
        assertThat(tSubNetworks).hasSize(2);
    }

    @Test
    void findSubNetwork_when_SubNetwork_Exist_should_succeed() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("scd-ied-dtt-com-import-stds/std.xml");
        // When Then
        assertThatCode(() -> subNetworkService.findSubNetwork(scd, tSubNetwork -> "RSPACE_PROCESS_NETWORK".equals(tSubNetwork.getName())).orElseThrow())
                .doesNotThrowAnyException();
    }

    @Test
    void findSubNetwork_when_SubNetwork_not_Exist_should_return_empty() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("scd-ied-dtt-com-import-stds/std.xml");
        // When Then
        assertThat(subNetworkService.findSubNetwork(scd, tSubNetwork -> "unknown".equals(tSubNetwork.getName())))
                .isEmpty();
    }

    @Test
    void getFilteredSubNetworks() {
        // Given
        SCL scd = SclTestMarshaller.getSCLFromResource("scd-ied-dtt-com-import-stds/std.xml");
        // When
        List<TSubNetwork> tSubNetworks =  subNetworkService.getFilteredSubNetworks(scd, tSubNetwork -> "RSPACE_PROCESS_NETWORK".equals(tSubNetwork.getName())).toList();
        // Then
        assertThat(tSubNetworks).hasSize(1);
    }
}
