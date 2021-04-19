package org.lfenergy.compas.sct.service;

import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.compas.commons.MarshallerWrapper;
import org.lfenergy.compas.exception.ScdException;
import org.lfenergy.compas.scl.SCL;
import org.lfenergy.compas.scl.THeader;
import org.lfenergy.compas.scl.THitem;
import org.lfenergy.compas.sct.exception.CompasDataAccessException;
import org.lfenergy.compas.sct.model.dto.ConnectedApDTO;
import org.lfenergy.compas.sct.model.dto.ExtRefDTO;
import org.lfenergy.compas.sct.model.dto.IedDTO;
import org.lfenergy.compas.sct.model.dto.ScdDTO;
import org.lfenergy.compas.sct.model.dto.SubNetworkDTO;
import org.lfenergy.compas.sct.model.entity.SimpleScd;
import org.lfenergy.compas.sct.repository.SimpleScdRepository;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
class SqlScdServiceAndDBTest {
    private final String FILE_NAME = "file.scd";
    private final String H_VERSION = "1.0";
    private final String H_REVISION = "1.0";
    private static final String WHO = "who";
    private static final String WHAT = "what";
    private static final String WHY = "why";
    private final UUID ID = UUID.randomUUID();

    private static final String IED_NAME = "IED_NAME";

    private static final String SCD_IED_RCV_FILE = "scl/SCD/scd_ied_receiver_test.xml";
    private static final String SCD_WITH_IEDS_FILE = "scl/SCD/scd_with_ieds_test.xml";
    private static final String ICD_IMPORT_FILE = "scl/IEDImportHelper/icd_import_ied_test.xml";

    @InjectMocks
    private SimpleSqlScdService simpleSqlScdService;

    @MockBean
    private SimpleScdRepository simpleScdRepository;


    private MarshallerWrapper marshallerWrapper;

    private DefaultResourceLoader defaultResourceLoader;


    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks( this );

        try {
            marshallerWrapper = (new MarshallerWrapper.Builder()).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        simpleSqlScdService = new SimpleSqlScdService(simpleScdRepository,marshallerWrapper);
        defaultResourceLoader = new DefaultResourceLoader();
    }

    @Test
    public void shouldReturnOKWhenInitiateSCD() throws CompasDataAccessException, ScdException {

        SimpleScd expected = new SimpleScd();
        expected.setFileName(FILE_NAME);
        expected.setRawXml(createSCD(ID, H_REVISION, H_VERSION));

        SimpleScd result = simpleSqlScdService.initiateSCD(FILE_NAME, H_VERSION, H_REVISION);

        SCL scd = marshallerWrapper.unmarshall(result.getRawXml());

        assertEquals(FILE_NAME,result.getFileName());
        assertEquals(H_REVISION,scd.getHeader().getRevision());
        assertEquals(H_VERSION,scd.getHeader().getVersion());
    }

    @Test
    public void shouldReturnOKWhenAddHistoryItem() throws ScdException, CompasDataAccessException {
        SimpleScd storedSimpleSCD = new SimpleScd();
        storedSimpleSCD.setFileName(FILE_NAME);
        storedSimpleSCD.setId(ID);
        storedSimpleSCD.setRawXml(createSCD(ID, H_REVISION, H_VERSION));

        SCL storedScd = marshallerWrapper.unmarshall(createSCD(ID, H_REVISION, H_VERSION));

        SimpleScd expected = new SimpleScd();
        expected.setFileName(FILE_NAME);
        expected.setId(ID);

        SCL expectedScd = marshallerWrapper.unmarshall(createSCD(ID, H_REVISION, H_VERSION));

        THeader.History history = new THeader.History();
        THitem tHitem = new THitem();
        tHitem.setVersion("1.0");
        tHitem.setRevision("1.0");
        tHitem.setWhat("what");
        tHitem.setWho("who");
        tHitem.setWhy("why");
        tHitem.setWhen(LocalDateTime.now().toString());
        history.getHitem().add(tHitem);
        expectedScd.getHeader().setHistory(history);
        expected.setRawXml(marshallerWrapper.marshall(expectedScd).getBytes());

        Mockito.when(simpleScdRepository.findById(ID)).thenReturn(Optional.of(storedSimpleSCD));
        Mockito.when(simpleScdRepository.save(any(SimpleScd.class))).thenReturn(expected);


        SimpleScd result = simpleSqlScdService.addHistoryItem(ID,"who","what", "why");
        assertNotNull(result);
        SCL resultScd = marshallerWrapper.unmarshall(result.getRawXml());

        assertAll(
                () -> assertNotNull(resultScd),
                () -> assertNotNull(resultScd.getHeader()),
                () -> assertNotNull(resultScd.getHeader().getHistory())
        );
        List<THitem> hItems = resultScd.getHeader().getHistory().getHitem();

        assertFalse(hItems.isEmpty());
        THitem hItem = hItems.get(0);
        assertAll(
                () -> assertNotNull(hItem),
                () -> assertEquals("who",hItem.getWho()),
                () -> assertEquals("what",hItem.getWhat()),
                () -> assertEquals("why",hItem.getWhy())
        );
    }


    @Test
    public void shouldReturnOKWhenInitiateScdAndAddHistory() throws ScdException {
        SimpleScd result = simpleSqlScdService.initiateSCD(FILE_NAME, H_VERSION, H_REVISION);
        SCL resultScd = marshallerWrapper.unmarshall(result.getRawXml());

        assertNotNull(result);
        assertNotNull(resultScd.getHeader());
        assertNotNull(UUID.fromString(resultScd.getHeader().getId()));

        result = simpleSqlScdService.addHistoryItem(result,"who","what","why");
        resultScd = marshallerWrapper.unmarshall(result.getRawXml());
        THeader.History history = resultScd.getHeader().getHistory();
        assertNotNull(history);
        assertFalse(history.getHitem().isEmpty());
        THitem tHitem = history.getHitem().get(0);
        assertEquals(H_REVISION,tHitem.getRevision());
        assertEquals(H_VERSION,tHitem.getVersion());
        assertEquals(WHO,tHitem.getWho());
    }
    @Test
    void testAddIED() throws IOException, ScdException {

        SCL iedProvider = getSCLFromFile(ICD_IMPORT_FILE);
        SimpleScd scdObj = new SimpleScd();
        scdObj.setRawXml(getFileContent(SCD_IED_RCV_FILE));

        scdObj = simpleSqlScdService.addIED(scdObj,IED_NAME,iedProvider);
        SCL res_receiver = marshallerWrapper.unmarshall(scdObj.getRawXml());

        assertNotNull(res_receiver);
        assertFalse(res_receiver.getIED().isEmpty());

    }

    @Test
    void testGetSubnetworks() throws IOException, ScdException {
        SimpleScd scdObj = new SimpleScd();
        scdObj.setRawXml(getFileContent(SCD_WITH_IEDS_FILE));

        Set<SubNetworkDTO> subnetworks = simpleSqlScdService.getSubnetwork(scdObj);
        assertEquals(1,subnetworks.size());
    }

    @Test
    void testExtractExtRefs() throws IOException, ScdException {
        SimpleScd scdObj = new SimpleScd();
        scdObj.setRawXml(getFileContent(SCD_WITH_IEDS_FILE));

        IedDTO iedDTO = simpleSqlScdService.extractExtRefs(scdObj,IED_NAME,"LDPO");
        assertEquals(IED_NAME,iedDTO.getName());
        assertEquals(1,iedDTO.getLDevices().size());
    }

    @Test
    public void testAddSubnetwork() throws IOException, ScdException {
        SimpleScd scdObj = new SimpleScd();
        scdObj.setRawXml(getFileContent(SCD_WITH_IEDS_FILE));

        SubNetworkDTO subNetworkDTO = new SubNetworkDTO("SN","IP");
        subNetworkDTO.addConnectedAPs(new ConnectedApDTO(IED_NAME,"AP_NAME"));

        scdObj = simpleSqlScdService.addSubnetworks(scdObj,Set.of(subNetworkDTO));
        SCL res_receiver = marshallerWrapper.unmarshall(scdObj.getRawXml());
        assertNotNull(res_receiver.getCommunication());
        assertFalse(res_receiver.getCommunication().getSubNetwork().isEmpty());
    }

    @Test
    void testExtractExtRefSources() throws IOException, ScdException {

        SimpleScd scdObj = new SimpleScd();
        scdObj.setRawXml(getFileContent(SCD_WITH_IEDS_FILE));
        ExtRefDTO extRef = new ExtRefDTO();
        extRef.setIntAddr("INTADDR3");
        extRef.setPDO("Op.res");
        extRef.setDesc("DESC3");

        Set<IedDTO> iedDTOs = simpleSqlScdService.extractExtRefSources(scdObj,IED_NAME, "LDPO", extRef);
        assertFalse(iedDTOs.isEmpty());

    }



    private byte[] createSCD(UUID uuid, String hRevision, String hVersion){
        SCL scd = new SCL();
        scd.setVersion("2007");
        scd.setRevision("B");
        scd.setRelease((short) 4);
        THeader tHeader = new THeader();
        tHeader.setRevision(hRevision);
        tHeader.setVersion(hVersion);
        tHeader.setId(uuid.toString());
        scd.setHeader(tHeader);

        return marshallerWrapper.marshall(scd).getBytes(StandardCharsets.UTF_8);

    }

    private byte[] getFileContent(String filename) throws IOException {
        Resource resource = defaultResourceLoader.getResource("classpath:" + filename);
        File file = resource.getFile();
        return Files.readAllBytes(file.toPath());
    }

    private SCL getSCLFromFile(String filename) throws IOException {
        return marshallerWrapper.unmarshall(getFileContent(filename));
    }
}