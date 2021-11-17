// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.MarshallerWrapper;
import org.lfenergy.compas.sct.commons.testhelpers.marshaller.SclTestMarshaller;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class SclRootAdapterTest {


    @Test
    public void testConstruction() {
        AtomicReference<SclRootAdapter> sclRootAdapter = new AtomicReference<>();
        assertDoesNotThrow(() ->
                sclRootAdapter.set(new SclRootAdapter("hID", "hVersion", "hRevision"))
        );

        assertThrows(ScdException.class,
                () ->  sclRootAdapter.get().addHeader("hID1","hVersion1","hRevision1"));
    }

    @Test
    void addIED() throws Exception {

        SCL scd = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/add_ied_test.xml");
        SCL icd1 = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/icd1_to_add_test.xml");
        SCL icd2 = SclTestMarshaller.getSCLFromFile("/scl-root-test-schema-conf/icd2_to_add_test.xml");

        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        assertDoesNotThrow(() -> sclRootAdapter.addIED(icd1, "IED_NAME1"));
        assertThrows(ScdException.class, () -> sclRootAdapter.addIED(icd1, "IED_NAME1"));
        assertDoesNotThrow(() -> sclRootAdapter.addIED(icd2, "IED_NAME2"));

        MarshallerWrapper marshallerWrapper = SclTestMarshaller.createWrapper();
        System.out.println(marshallerWrapper.marshall(sclRootAdapter.getCurrentElem()));
    }
}