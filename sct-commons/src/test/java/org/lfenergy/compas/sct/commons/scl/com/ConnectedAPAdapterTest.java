// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.com;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.lfenergy.compas.sct.commons.util.SclConstructorHelper.*;

class ConnectedAPAdapterTest {

    private SubNetworkAdapter subNetworkAdapter;

    @BeforeEach
    void setUp() {
        subNetworkAdapter = new SubNetworkAdapter(null, new TSubNetwork());
        TConnectedAP tConnectedAP = newConnectedAp(DTO.HOLDER_IED_NAME, DTO.AP_NAME);
        subNetworkAdapter.getCurrentElem().getConnectedAP().add(tConnectedAP);
    }

    @Test
    void testAmChildElementRef() {
        ConnectedAPAdapter connectedAPAdapter = assertDoesNotThrow(
                () -> subNetworkAdapter.getConnectedAPAdapter(DTO.HOLDER_IED_NAME, DTO.AP_NAME)
        );

        assertEquals(DTO.HOLDER_IED_NAME, connectedAPAdapter.getIedName());
        assertEquals(DTO.AP_NAME, connectedAPAdapter.getApName());
    }

    @Test
    void addPrivate() {
        ConnectedAPAdapter connectedAPAdapter = subNetworkAdapter.getConnectedAPAdapter(DTO.HOLDER_IED_NAME, DTO.AP_NAME);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertTrue(connectedAPAdapter.getCurrentElem().getPrivate().isEmpty());
        connectedAPAdapter.addPrivate(tPrivate);
        assertEquals(1, connectedAPAdapter.getCurrentElem().getPrivate().size());
    }

    @Test
    void testCopyAddressAndPhysConnFromIcd_withFilledCommunication() {
        // GIVEN
        ConnectedAPAdapter connectedAPAdapter = assertDoesNotThrow(
                () -> subNetworkAdapter.getConnectedAPAdapter(DTO.HOLDER_IED_NAME, DTO.AP_NAME)
        );

        SCL icd = SclTestMarshaller.getSCLFromFile("/scl-srv-import-ieds/ied_with_filled_communication.xml");
        SclRootAdapter icdRootAdapter = new SclRootAdapter(icd);

        // WHEN
        connectedAPAdapter.copyAddressAndPhysConnFromIcd(icdRootAdapter.getCurrentElem());

        // THEN
        assertThat(connectedAPAdapter.getCurrentElem().getAddress()).isNotNull();
        assertThat(connectedAPAdapter.getCurrentElem().getPhysConn()).isNotNull();
        assertThat(connectedAPAdapter.getCurrentElem().getGSE()).isEmpty();
    }

    @Test
    void testCopyAddressAndPhysConnFromIcd_withEmptyIcd() {
        // GIVEN
        ConnectedAPAdapter connectedAPAdapter = assertDoesNotThrow(
                () -> subNetworkAdapter.getConnectedAPAdapter(DTO.HOLDER_IED_NAME, DTO.AP_NAME)
        );

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
    void elementXPath(String iedName, String apName, String message) {
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
        TGSE gse = connectedAPAdapter.getCurrentElem().getGSE().get(0);
        assertThat(gse.getLdInst()).isEqualTo("ldinst");
        assertThat(gse.getMinTime()).extracting(TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier, TDurationInMilliSec::getValue)
            .containsExactly("s", "m", new BigDecimal("5"));
        assertThat(gse.getMaxTime()).extracting(TDurationInMilliSec::getUnit, TDurationInMilliSec::getMultiplier, TDurationInMilliSec::getValue)
            .containsExactly("s", "m", new BigDecimal("10"));
        assertThat(gse.getAddress().getP()).extracting(TP::getType, TP::getValue)
            .containsExactly(Tuple.tuple("APPID", "0001"));
    }

    @Test
    void updateGseOrCreateIfNotExists_when_exists_should_update_GSE(){
        // Given
        ConnectedAPAdapter connectedAPAdapter = newConnectedApAdapter("IED_NAME", "AP_NAME");
        connectedAPAdapter.updateGseOrCreateIfNotExists("ldinst", "cbName", List.of(SclConstructorHelper.newP("APPID", "0001")),
            newDurationInMilliSec(5), newDurationInMilliSec(10));
        // When
        connectedAPAdapter.updateGseOrCreateIfNotExists("ldinst", "cbName", List.of(SclConstructorHelper.newP("APPID", "0004")),
            newDurationInMilliSec(30), newDurationInMilliSec(50));
        // Then
        assertThat(connectedAPAdapter.getCurrentElem().getGSE()).hasSize(1);
        TGSE gse = connectedAPAdapter.getCurrentElem().getGSE().get(0);
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
        TSMV smv = connectedAPAdapter.getCurrentElem().getSMV().get(0);
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
        TSMV smv = connectedAPAdapter.getCurrentElem().getSMV().get(0);
        assertThat(smv.getLdInst()).isEqualTo("ldinst");
        assertThat(smv.getAddress().getP()).extracting(TP::getType, TP::getValue)
            .containsExactly(Tuple.tuple("APPID", "0004"));
    }

    private ConnectedAPAdapter newConnectedApAdapter(String iedName, String apName){
        return new ConnectedAPAdapter(null, newConnectedAp(iedName, apName));
    }

}
