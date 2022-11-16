// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.testhelpers.SclTestMarshaller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DOIAdapterTest {

    @Test
    void testConstructor() {
        LN0 ln0 = new LN0();
        LN0Adapter ln0Adapter = new LN0Adapter(null,ln0);

        TDOI tdoi = new TDOI();
        tdoi.setName("Do");
        ln0.getDOI().add(tdoi);
        // test amChildElement
        DOIAdapter doiAdapter = assertDoesNotThrow(() -> new DOIAdapter(ln0Adapter,tdoi));

        // test tree map
        TSDI tsdi = new TSDI();
        tsdi.setName("sdo2");
        tdoi.getSDIOrDAI().add(tsdi);
        assertDoesNotThrow(() -> doiAdapter.getStructuredDataAdapterByName("sdo2"));
        assertThrows(ScdException.class, () -> doiAdapter.getStructuredDataAdapterByName("sdo3"));
        TDAI tdai = new TDAI();
        tdai.setName("angRef");
        tdoi.getSDIOrDAI().add(tdai);
        assertDoesNotThrow(() -> doiAdapter.getDataAdapterByName("angRef"));
        assertThrows(ScdException.class, () -> doiAdapter.getStructuredDataAdapterByName("bda"));
        assertThrows(ScdException.class, () -> doiAdapter.getDataAdapterByName("bda"));
    }


    @Test
    void testInnerDAIAdapter(){
        final String TOTO = "toto";
        DOIAdapter.DAIAdapter daiAdapter = initInnerDAIAdapter("Do","angRef");

        assertNull(daiAdapter.isValImport());
        daiAdapter.setValImport(true);
        assertTrue(daiAdapter.isValImport());

        // test tree map
        assertThrows(UnsupportedOperationException.class, () -> daiAdapter.getDataAdapterByName(TOTO));
        assertThrows(
                UnsupportedOperationException.class,
                () -> daiAdapter.getStructuredDataAdapterByName(TOTO)
        );

        assertThrows(UnsupportedOperationException.class, () -> daiAdapter.addDAI(TOTO));
        assertThrows(UnsupportedOperationException.class, () -> daiAdapter.addSDOI(TOTO));
    }

    @Test
    void testInnerDAIAdapterTestUpdateWithMapAsArg(){
        final String TOTO = "toto";
        DOIAdapter.DAIAdapter daiAdapter = initInnerDAIAdapter("Do","da");
        daiAdapter.setValImport(true);
        // update DAI val
        final Map<Long,String > vals = Collections.singletonMap(0L,TOTO);
        assertDoesNotThrow(() -> daiAdapter.update(vals));
        assertFalse(daiAdapter.getCurrentElem().getVal().isEmpty());
        TVal tVal = daiAdapter.getCurrentElem().getVal().get(0);
        assertFalse(tVal.isSetSGroup());

        final Map<Long,String > vals2 = new HashMap<>();
        vals2.put(1L,TOTO);
        vals2.put(0L,TOTO);
        assertDoesNotThrow(() -> daiAdapter.update(vals2));
        assertFalse(daiAdapter.getCurrentElem().getVal().isEmpty());
        tVal = daiAdapter.getCurrentElem().getVal().get(0);
        assertFalse(tVal.isSetSGroup());
    }

    @Test
    void testInnerDAIAdapterTestUpdate(){
        final String TOTO = "toto";
        DOIAdapter.DAIAdapter daiAdapter = initInnerDAIAdapter("Do","da");
        daiAdapter.setValImport(false);
        assertThrows(ScdException.class,() -> daiAdapter.update(0L,TOTO) );
        daiAdapter.setValImport(true);
        assertDoesNotThrow(() -> daiAdapter.update(0L,TOTO));

        final Map<Long,String > vals2 = new HashMap<>();
        vals2.put(1L,TOTO);
        vals2.put(2L,TOTO);
        assertDoesNotThrow(() -> daiAdapter.update(vals2));
        vals2.put(2L,TOTO + "1");
        assertDoesNotThrow(() -> daiAdapter.update(vals2));
    }

    @Test
    void testFindDeepestMatch() throws Exception {
        SCL scd = SclTestMarshaller.getSCLFromFile("/ied-test-schema-conf/ied_unit_test.xml");
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iAdapter = assertDoesNotThrow(() -> sclRootAdapter.getIEDAdapterByName("IED_NAME"));
        LDeviceAdapter lDeviceAdapter = assertDoesNotThrow(()-> iAdapter.findLDeviceAdapterByLdInst("LD_INS1").get());
        LN0Adapter ln0Adapter = lDeviceAdapter.getLN0Adapter();
        DOIAdapter doiAdapter = assertDoesNotThrow(()-> ln0Adapter.getDOIAdapterByName("Do"));
        DoTypeName doTypeName = new DoTypeName("Do.sdo1.d");
        DaTypeName daTypeName = new DaTypeName("antRef.bda1.bda2.bda3");
        Pair<? extends IDataAdapter,Integer> pair = doiAdapter.findDeepestMatch(
            doTypeName.getStructNames(),0,false
        );
        SDIAdapter lastSDOIAdapter = (SDIAdapter) pair.getLeft();
        assertEquals(1,pair.getRight());
        assertNotNull(lastSDOIAdapter);
        assertEquals(SDIAdapter.class,lastSDOIAdapter.getClass());

        SDIAdapter firstDAIAdapter = lastSDOIAdapter.getStructuredDataAdapterByName(daTypeName.getName());
        pair = firstDAIAdapter.findDeepestMatch(
            daTypeName.getStructNames(),0,true
        );
        assertEquals(2,pair.getRight());
        assertNotNull(pair.getLeft());
        assertEquals(SDIAdapter.DAIAdapter.class,pair.getLeft().getClass());
    }


    private DOIAdapter.DAIAdapter initInnerDAIAdapter(String doName, String daName){
        TDOI tdoi = new TDOI();
        tdoi.setName(doName);
        DOIAdapter doiAdapter = new DOIAdapter(null,tdoi);

        TDAI tdai = new TDAI();
        tdai.setName(daName);
        tdoi.getSDIOrDAI().add(tdai);
        DOIAdapter.DAIAdapter daiAdapter = assertDoesNotThrow(() -> new DOIAdapter.DAIAdapter(doiAdapter,tdai));

        return daiAdapter;
    }

    @Test
    void addPrivate() {
        DOIAdapter.DAIAdapter daiAdapter = initInnerDAIAdapter("Do","da");
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType("Private Type");
        tPrivate.setSource("Private Source");
        assertTrue(daiAdapter.getCurrentElem().getPrivate().isEmpty());
        daiAdapter.addPrivate(tPrivate);
        assertEquals(1, daiAdapter.getCurrentElem().getPrivate().size());
    }

    @Test
    void elementXPath_doi() {
        // Given
        TDOI tdoi = new TDOI();
        tdoi.setName("doName");
        DOIAdapter doiAdapter = new DOIAdapter(null,new TDOI());
        DOIAdapter namedDoiAdapter = new DOIAdapter(null,tdoi);
        // When
        String elementXPathResult = doiAdapter.elementXPath();
        String namedElementXPathResult = namedDoiAdapter.elementXPath();
        // Then
        assertThat(elementXPathResult).isEqualTo("DOI[not(@name)]");
        assertThat(namedElementXPathResult).isEqualTo("DOI[@name=\"doName\"]");
    }

    @Test
    void elementXPath_dai() {
        // Given
        DOIAdapter.DAIAdapter daiAdapter = initInnerDAIAdapter("Do","da");
        // When
        String result = daiAdapter.elementXPath();
        // Then
        assertThat(result).isEqualTo("DAI[@name=\"da\"]");
    }

}
