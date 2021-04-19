package org.lfenergy.compas.sct.service.scl;


import org.junit.jupiter.api.Test;
import org.lfenergy.compas.commons.MarshallerWrapper;
import org.lfenergy.compas.exception.ScdException;
import org.lfenergy.compas.scl.SCL;
import org.lfenergy.compas.scl.TAccessPoint;
import org.lfenergy.compas.scl.TDataTypeTemplates;
import org.lfenergy.compas.scl.TExtRef;
import org.lfenergy.compas.scl.TIED;
import org.lfenergy.compas.scl.TLDevice;
import org.lfenergy.compas.scl.TLLN0Enum;
import org.lfenergy.compas.scl.TLN;
import org.lfenergy.compas.sct.model.dto.ExtRefDTO;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class SclIEDManagerTest {

    private static final String SCD_IED_RCV_FILE = "scl/SCD/scd_ied_receiver_test.xml";
    private static final String SCD_WITH_IEDS_FILE = "scl/SCD/scd_with_ieds_test.xml";
    private static final String ICD_IMPORT_FILE = "scl/IEDImportHelper/icd_import_ied_test.xml";

    private static final String IED_NAME = "IED_NAME";


    @Test
    void addIed() {
    }

    @Test
    void shouldReturnOKWhenAddIed() throws ScdException {
        SCL iedReceiver = getSCLFromFile(SCD_IED_RCV_FILE);
        SCL iedProvider = getSCLFromFile(ICD_IMPORT_FILE);
        //check prerequisite
        assertFalse(iedReceiver.getIED().stream().anyMatch(tied -> tied.getName().equals(IED_NAME)));
        Optional<TIED> opIED = iedProvider.getIED().stream().filter(tied -> tied.getName().equals(IED_NAME)).findFirst();
        assertTrue(opIED.isPresent());
        TIED ied = opIED.get();
        assertFalse(ied.getAccessPoint().isEmpty());
        TAccessPoint accessPoint = ied.getAccessPoint().get(0);
        assertEquals(1, accessPoint.getServer().getLDevice().size() );
        TLDevice lDevice = accessPoint.getServer().getLDevice().get(0);
        assertEquals("LN2",lDevice.getLN0().getLnType());
        assertEquals(2,lDevice.getLN().size() );
        TLN ln = lDevice.getLN().get(0);
        assertEquals("LN1",ln.getLnType() );

        // import IED
        SclIEDManager sclIEDManager = new SclIEDManager(iedReceiver);

        SclDataTemplateManager sclDataTemplateManager = new SclDataTemplateManager();
        sclDataTemplateManager.importDTT(iedProvider,iedReceiver,IED_NAME);
        Map<String,String> lNodeTypeTracker = sclDataTemplateManager.getLNodeTypeTracker();
        assertFalse(lNodeTypeTracker.isEmpty());
        iedReceiver = sclIEDManager.addIed(iedProvider,IED_NAME,lNodeTypeTracker);

        opIED = iedReceiver.getIED().stream().filter(tied -> tied.getName().equals(IED_NAME)).findFirst();
        assertTrue(opIED.isPresent());
        ied = opIED.get();
        assertFalse(ied.getAccessPoint().isEmpty());
        accessPoint = ied.getAccessPoint().get(0);
        assertNotNull(accessPoint.getServer());
        assertEquals(1, accessPoint.getServer().getLDevice().size());
        lDevice = accessPoint.getServer().getLDevice().get(0);
        assertNotNull(lDevice.getLN0());
        assertEquals(IED_NAME + "_LN2",lDevice.getLN0().getLnType());
        assertEquals(2,lDevice.getLN().size());
        ln = lDevice.getLN().get(0);
        assertEquals(IED_NAME + "_LN1", ln.getLnType());
    }

    @Test
    void ShouldReturnNOKWhenAddIedCauseIEDAlreadyExistsInReceiver() {
        SCL receiver = createMinimalSCL();
        TIED tied = new TIED();
        tied.setName(IED_NAME);
        receiver.getIED().add(tied);
        SCL iedProvider = getSCLFromFile(ICD_IMPORT_FILE);

        SclIEDManager sclIEDManager = new SclIEDManager(receiver);


        assertThrows(ScdException.class, () -> sclIEDManager.addIed(iedProvider,IED_NAME,new HashMap<>()));

    }

    @Test
    void ShouldReturnNOKWhenAddIedCauseReceiverContainsDTTisNULL() {
        SCL receiver = createMinimalSCL();
        SCL iedProvider = getSCLFromFile(ICD_IMPORT_FILE);

        SclIEDManager sclIEDManager = new SclIEDManager(receiver);

        assertThrows(ScdException.class, () -> sclIEDManager.addIed(iedProvider,IED_NAME,new HashMap<>()));
    }

    @Test
    void ShouldReturnNOKWhenAddIedCauseReceiverContainsDTTHasNoLNodeType() {

        SCL receiver = createMinimalSCL();
        TDataTypeTemplates tDataTypeTemplates = new TDataTypeTemplates();
        receiver.setDataTypeTemplates(tDataTypeTemplates);
        SCL iedProvider = getSCLFromFile(ICD_IMPORT_FILE);

        SclIEDManager sclIEDManager = new SclIEDManager(receiver);

        assertThrows(ScdException.class, () -> sclIEDManager.addIed(iedProvider,IED_NAME,new HashMap<>()));
    }

    @Test
    void testGetLDevice(){
        SCL scd = getSCLFromFile(SCD_WITH_IEDS_FILE);
        assertFalse(scd.getIED().isEmpty());
        TIED ied = scd.getIED().get(0);

        List<TLDevice> lds = SclIEDManager.getIEDLDevice(ied);
        assertFalse(lds.isEmpty());
    }
    @Test
    void testGetSpecificLDevice(){
        SCL scd = getSCLFromFile(SCD_WITH_IEDS_FILE);
        assertFalse(scd.getIED().isEmpty());

        TIED ied = scd.getIED()
                .stream()
                .filter(tied -> tied.getName().equals(IED_NAME))
                .findFirst()
                .orElse(null);
        assertNotNull(ied);

        TLDevice ld = SclIEDManager.getIEDLDevice(ied,"LDPO").orElse(null);
        assertNotNull(ld);
    }

    @Test
    void ShouldReturnOKWhenUpdateLN() throws ScdException {
        TLN ln = new TLN();
        ln.setInst("lnInst");
        ln.setLnType("lnType");
        String newLnType = "newLNType";
        Set<String> receiverLNodeTypeIds = new HashSet<>();
        receiverLNodeTypeIds.add(newLnType);
        Map<String, String> tracker = new HashMap<>();
        tracker.put(ln.getLnType(),newLnType);
        SclIEDManager sclIEDManager = new SclIEDManager(new SCL());

        ln = (TLN) sclIEDManager.updateLN(ln,receiverLNodeTypeIds,tracker);
        assertEquals(newLnType,ln.getLnType());

    }

    @Test
    void ShouldReturnNOKWhenUpdateLNCauseUnknownLNodeType() {
        TLN ln = new TLN();
        ln.setInst("lnInst");
        ln.setLnType("lnType");
        Set<String> receiverLNodeTypeIds = new HashSet<>();
        Map<String, String> tracker = new HashMap<>();
        SclIEDManager sclIEDManager = new SclIEDManager(new SCL());

        assertThrows(ScdException.class, () -> sclIEDManager.updateLN(ln,receiverLNodeTypeIds,tracker));

    }

    @Test
    void ShouldReturnNOKWhenUpdateLNCauseLNodeTypeBadlyRenamed() {
        TLN ln = new TLN();
        ln.setInst("lnInst");
        ln.setLnType("lnType");
        String badNewLNType = "badNewLNType";
        Set<String> receiverLNodeTypeIds = new HashSet<>();
        Map<String, String> tracker = new HashMap<>();
        tracker.put(ln.getLnType(),badNewLNType);
        SclIEDManager sclIEDManager = new SclIEDManager(new SCL());

        assertThrows(ScdException.class, () -> sclIEDManager.updateLN(ln,receiverLNodeTypeIds,tracker));

    }

    @Test
    void testShouldReturnOKWhenGetExtRef() throws ScdException {
        SCL scd = getSCLFromFile(SCD_WITH_IEDS_FILE);
        SclIEDManager sclIEDManager = new SclIEDManager(scd);
        ExtRefDTO simpleExtRef = new ExtRefDTO();
        simpleExtRef.setIntAddr("INTADDR2");
        simpleExtRef.setPLN(TLLN0Enum.LLN_0.value());
        simpleExtRef.setPDO("Op");
        TExtRef extRef = sclIEDManager.getExtRef(IED_NAME,"LDPO",simpleExtRef);
        assertEquals("DESC2",extRef.getDesc());

    }

    @Test
    void testShouldReturnNOKWhenGetExtRefNoThingFound() throws ScdException {
        SCL scd = getSCLFromFile(SCD_WITH_IEDS_FILE);
        SclIEDManager sclIEDManager = new SclIEDManager(scd);
        ExtRefDTO simpleExtRef = new ExtRefDTO();
        simpleExtRef.setIntAddr("INTADDR1");
        simpleExtRef.setPLN(TLLN0Enum.LLN_0.value());
        simpleExtRef.setPDO("Op");
        assertThrows(ScdException.class, () -> sclIEDManager.getExtRef(IED_NAME,"LDPO",simpleExtRef));
    }

    @Test
    void testShouldReturnNOKWhenGetExtRefUnknownLD() throws ScdException {
        SCL scd = getSCLFromFile(SCD_WITH_IEDS_FILE);
        SclIEDManager sclIEDManager = new SclIEDManager(scd);
        ExtRefDTO simpleExtRef = new ExtRefDTO();
        simpleExtRef.setIntAddr("INTADDR1");
        simpleExtRef.setPLN(TLLN0Enum.LLN_0.value());
        simpleExtRef.setPDO("Op");
        assertThrows(ScdException.class, () -> sclIEDManager.getExtRef(IED_NAME,"LDPO1",simpleExtRef));
    }
    @Test
    void testExtractLN0ExtRefs(){

        SCL scd = getSCLFromFile(SCD_WITH_IEDS_FILE);
        SclIEDManager sclIEDManager = new SclIEDManager(scd);
        //prerequisite
        assertFalse(scd.getIED().isEmpty());
        assertFalse(scd.getIED().get(0).getAccessPoint().isEmpty());
        assertNotNull(scd.getIED().get(0).getAccessPoint().get(0).getServer());
        assertFalse(scd.getIED().get(0).getAccessPoint().get(0).getServer().getLDevice().isEmpty());
        TLDevice lDevice = scd.getIED().get(0).getAccessPoint().get(0).getServer().getLDevice().get(0);
        List<TExtRef> extRefs = sclIEDManager.extractLN0ExtRefs(lDevice, null);

        assertFalse(extRefs.isEmpty());
    }

    @Test
    void testExtractLNExtRefs() throws ScdException {

        SCL scd = getSCLFromFile(SCD_WITH_IEDS_FILE);
        SclIEDManager sclIEDManager = new SclIEDManager(scd);
        //prerequisite
        assertFalse(scd.getIED().isEmpty());
        assertFalse(scd.getIED().get(0).getAccessPoint().isEmpty());
        assertNotNull(scd.getIED().get(0).getAccessPoint().get(0).getServer());
        assertFalse(scd.getIED().get(0).getAccessPoint().get(0).getServer().getLDevice().isEmpty());
        TLDevice lDevice = scd.getIED().get(0).getAccessPoint().get(0).getServer().getLDevice().get(0);
        assertEquals(2,lDevice.getLN().size());
        TLN tln = lDevice.getLN().get(0);
        assertFalse(tln.getLnClass().isEmpty());
        String lnClass = tln.getLnClass().get(0);
        List<TExtRef> extRefs = sclIEDManager.extractLNExtRefs(lDevice,lnClass,tln.getInst(),null);

        assertFalse(extRefs.isEmpty());
    }

    private SCL createMinimalSCL(){
        return SclManager.initialize("hID","hVersion","hRevision");
    }
    private MarshallerWrapper createWrapper() throws Exception {
        return (new MarshallerWrapper.Builder()).build();
    }

    private SCL getSCLFromFile(String filename){
        MarshallerWrapper marshallerWrapper = null;
        try {
            marshallerWrapper = createWrapper();
        } catch (Exception e) {
            fail("XML marshaller can't be created!");
        }
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        return marshallerWrapper.unmarshall(is);
    }
}