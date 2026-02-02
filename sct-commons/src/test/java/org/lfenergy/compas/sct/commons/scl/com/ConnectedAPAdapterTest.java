// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.com;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DTO;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;
import org.lfenergy.compas.sct.commons.util.SclConstructorHelper;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.lfenergy.compas.sct.commons.util.SclConstructorHelper.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConnectedAPAdapterTest {

    @Test
    void constructor_whenCalledWithNoRelationBetweenSubNetworkAndConnectedAP_shouldThrowException() {
        //Given
        SubNetworkAdapter subNetworkAdapter = mock(SubNetworkAdapter.class);
        when(subNetworkAdapter.getCurrentElem()).thenReturn(new TSubNetwork());
        TConnectedAP connectedAP = new TConnectedAP();
        //When Then
        assertThatCode(() -> new ConnectedAPAdapter(subNetworkAdapter, connectedAP))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No relation between SCL parent element and child");
    }

    @Test
    void constructor_whenCalledWithExistingRelationBetweenSubNetworkAndConnectedAP_shouldNotThrowException() {
        //Given
        SubNetworkAdapter subNetworkAdapter = mock(SubNetworkAdapter.class);
        TSubNetwork subNetwork = new TSubNetwork();
        TConnectedAP connectedAP = new TConnectedAP();
        subNetwork.getConnectedAP().add(connectedAP);
        when(subNetworkAdapter.getCurrentElem()).thenReturn(subNetwork);
        //When Then
        assertThatCode(() -> new ConnectedAPAdapter(subNetworkAdapter, connectedAP))
                .doesNotThrowAnyException();
    }

    @Test
    void addPrivate_with_type_and_source_should_create_Private() {
        //Given
        SubNetworkAdapter subNetworkAdapter = mock(SubNetworkAdapter.class);
        TSubNetwork subNetwork = new TSubNetwork();
        TConnectedAP connectedAP = new TConnectedAP();
        subNetwork.getConnectedAP().add(connectedAP);
        when(subNetworkAdapter.getCurrentElem()).thenReturn(subNetwork);
        ConnectedAPAdapter connectedAPAdapter = new ConnectedAPAdapter(subNetworkAdapter, connectedAP);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertThat(connectedAPAdapter.getCurrentElem().getPrivate()).isEmpty();
        //When
        connectedAPAdapter.addPrivate(tPrivate);
        //Then
        assertThat(connectedAPAdapter.getCurrentElem().getPrivate()).isNotEmpty();
    }

    @Test
    void copyAddressAndPhysConnFromIcd_when_apName_exist_in_ICD_Communication_should_update_subNetwork() {
        // GIVEN
        SubNetworkAdapter subNetworkAdapter = mock(SubNetworkAdapter.class);
        TSubNetwork subNetwork = new TSubNetwork();
        TConnectedAP connectedAP = newConnectedAp(DTO.HOLDER_IED_NAME, DTO.AP_NAME);
        subNetwork.getConnectedAP().add(connectedAP);
        when(subNetworkAdapter.getCurrentElem()).thenReturn(subNetwork);
        ConnectedAPAdapter connectedAPAdapter = new ConnectedAPAdapter(subNetworkAdapter, connectedAP);
        SCL icd = SclTestMarshaller.getSCLFromResource("scl-srv-import-ieds/ied_with_filled_communication.xml");
        SclRootAdapter icdRootAdapter = new SclRootAdapter(icd);
        // WHEN
        connectedAPAdapter.copyAddressAndPhysConnFromIcd(icdRootAdapter.getCurrentElem());
        // THEN
        assertThat(connectedAPAdapter.getCurrentElem().getAddress()).isNotNull();
        assertThat(connectedAPAdapter.getCurrentElem().getPhysConn()).isNotEmpty();
        assertThat(connectedAPAdapter.getCurrentElem().getGSE()).isEmpty();
    }

    @Test
    void copyAddressAndPhysConnFromIcd_when_apName_not_exist_in_ICD_Communication_should_not_update_subNetwork() {
        // GIVEN
        SubNetworkAdapter subNetworkAdapter = mock(SubNetworkAdapter.class);
        TSubNetwork subNetwork = new TSubNetwork();
        TConnectedAP connectedAP = new TConnectedAP();
        subNetwork.getConnectedAP().add(connectedAP);
        when(subNetworkAdapter.getCurrentElem()).thenReturn(subNetwork);
        ConnectedAPAdapter connectedAPAdapter = new ConnectedAPAdapter(subNetworkAdapter, connectedAP);
        SCL icd = SclTestMarshaller.getSCLFromResource("scl-srv-import-ieds/ied_with_filled_communication.xml");
        SclRootAdapter icdRootAdapter = new SclRootAdapter(icd);
        // WHEN
        connectedAPAdapter.copyAddressAndPhysConnFromIcd(icdRootAdapter.getCurrentElem());
        // THEN
        assertThat(connectedAPAdapter.getCurrentElem().getAddress()).isNull();
        assertThat(connectedAPAdapter.getCurrentElem().getPhysConn()).isEmpty();
        assertThat(connectedAPAdapter.getCurrentElem().getGSE()).isEmpty();
    }

    @Test
    void copyAddressAndPhysConnFromIcd_whenCalledWithEmptyIcd_shouldNotUpdateSubNetwork() {
        // GIVEN
        SubNetworkAdapter subNetworkAdapter = mock(SubNetworkAdapter.class);
        TSubNetwork subNetwork = new TSubNetwork();
        TConnectedAP connectedAP = newConnectedAp(DTO.HOLDER_IED_NAME, DTO.AP_NAME);
        subNetwork.getConnectedAP().add(connectedAP);
        when(subNetworkAdapter.getCurrentElem()).thenReturn(subNetwork);
        ConnectedAPAdapter connectedAPAdapter = new ConnectedAPAdapter(subNetworkAdapter, connectedAP);
        // WHEN
        connectedAPAdapter.copyAddressAndPhysConnFromIcd(null);
        // THEN
        assertThat(connectedAPAdapter.getCurrentElem().getAddress()).isNull();
        assertThat(connectedAPAdapter.getCurrentElem().getPhysConn()).isEmpty();
        assertThat(connectedAPAdapter.getCurrentElem().getGSE()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource(value = {"IED_NAME;AP_NAME;ConnectedAP[@apName=\"AP_NAME\" and @iedName=\"IED_NAME\"]", ";;ConnectedAP[not(@apName) and not(@iedName)]"}
            , delimiter = ';')
    void elementXPath_should_return_expected_xpath_value(String iedName, String apName, String message) {
        // Given
        ConnectedAPAdapter connectedAPAdapter = newConnectedApAdapter(iedName, apName);
        // When
        String elementXPath = connectedAPAdapter.elementXPath();
        // Then
        assertThat(elementXPath).isEqualTo(message);
    }

    @Test
    void updateGseOrCreateIfNotExists_should_create_GSE(){
        // Given
        ConnectedAPAdapter connectedAPAdapter = newConnectedApAdapter("IED_NAME", "AP_NAME");
        // When
        connectedAPAdapter.updateGseOrCreateIfNotExists("ldinst", "cbName", List.of(SclConstructorHelper.newP("APPID", "0001")),
            newDurationInMilliSec(5), newDurationInMilliSec(10));
        // Then
        assertThat(connectedAPAdapter.getCurrentElem().getGSE()).hasSize(1);
        TGSE gse = connectedAPAdapter.getCurrentElem().getGSE().getFirst();
        assertThat(gse.getLdInst()).isEqualTo("ldinst");
        assertThat(gse.getMinTime()).extracting(TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier, TDurationInMilliSec::getValue)
            .containsExactly("s", "m", new BigDecimal("5"));
        assertThat(gse.getMaxTime()).extracting(TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier, TDurationInMilliSec::getValue)
            .containsExactly("s", "m", new BigDecimal("10"));
        assertThat(gse.getAddress().getP()).extracting(TP::getType, TP::getValue)
            .containsExactly(Tuple.tuple("APPID", "0001"));
    }

    @Test
    @Tag("issue-321")
    void updateGseOrCreateIfNotExists_when_exists_should_update_GSE(){
        // Given
        ConnectedAPAdapter connectedAPAdapter = newConnectedApAdapter("IED_NAME", "AP_NAME");
        // When
        connectedAPAdapter.updateGseOrCreateIfNotExists("ldinst", "cbName", List.of(SclConstructorHelper.newP("APPID", "0001")),
            newDurationInMilliSec(5), newDurationInMilliSec(10));
        // When
        connectedAPAdapter.updateGseOrCreateIfNotExists("ldinst", "cbName", List.of(SclConstructorHelper.newP("APPID", "0004")),
            newDurationInMilliSec(30), newDurationInMilliSec(50));
        // Then
        assertThat(connectedAPAdapter.getCurrentElem().getGSE()).hasSize(1);
        TGSE gse = connectedAPAdapter.getCurrentElem().getGSE().getFirst();
        assertThat(gse.getLdInst()).isEqualTo("ldinst");
        assertThat(gse.getMinTime()).extracting(TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier, TDurationInMilliSec::getValue)
            .containsExactly("s", "m", new BigDecimal("30"));
        assertThat(gse.getMaxTime()).extracting(TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier, TDurationInMilliSec::getValue)
            .containsExactly("s", "m", new BigDecimal("50"));
        assertThat(gse.getAddress().getP()).extracting(TP::getType, TP::getValue)
            .containsExactly(Tuple.tuple("APPID", "0004"));
    }

    @Test
    void updateSmvOrCreateIfNotExists_should_create_SMV(){
        // Given
        ConnectedAPAdapter connectedAPAdapter = newConnectedApAdapter("IED_NAME", "AP_NAME");
        // When
        connectedAPAdapter.updateSmvOrCreateIfNotExists("ldinst", "cbName", List.of(SclConstructorHelper.newP("APPID", "0001")));
        // Then
        assertThat(connectedAPAdapter.getCurrentElem().getSMV()).hasSize(1);
        TSMV smv = connectedAPAdapter.getCurrentElem().getSMV().getFirst();
        assertThat(smv.getLdInst()).isEqualTo("ldinst");
        assertThat(smv.getAddress().getP()).extracting(TP::getType, TP::getValue)
            .containsExactly(Tuple.tuple("APPID", "0001"));
    }

    @Test
    void updateSmvOrCreateIfNotExists_when_exists_should_update_SMV(){
        // Given
        ConnectedAPAdapter connectedAPAdapter = newConnectedApAdapter("IED_NAME", "AP_NAME");
        TSMV newSmv = new TSMV();
        newSmv.setLdInst("ldinst");
        newSmv.setCbName("cbName");
        newSmv.setAddress(newAddress(List.of(SclConstructorHelper.newP("APPID", "0001"))));
        connectedAPAdapter.getCurrentElem().getSMV().add(newSmv);
        // When
        connectedAPAdapter.updateSmvOrCreateIfNotExists("ldinst", "cbName", List.of(SclConstructorHelper.newP("APPID", "0004")));
        // Then
        assertThat(connectedAPAdapter.getCurrentElem().getSMV()).hasSize(1);
        TSMV smv = connectedAPAdapter.getCurrentElem().getSMV().getFirst();
        assertThat(smv.getLdInst()).isEqualTo("ldinst");
        assertThat(smv.getAddress().getP()).extracting(TP::getType, TP::getValue)
            .containsExactly(Tuple.tuple("APPID", "0004"));
    }

    private ConnectedAPAdapter newConnectedApAdapter(String iedName, String apName){
        return new ConnectedAPAdapter(null, newConnectedAp(iedName, apName));
    }

}
