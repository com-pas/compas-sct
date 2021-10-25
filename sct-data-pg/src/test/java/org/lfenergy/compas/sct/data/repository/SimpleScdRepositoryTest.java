// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.data.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lfenergy.compas.sct.data.model.SimpleScd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
class SimpleScdRepositoryTest {

    @Autowired
    private SimpleScdRepository simpleScdRepository;
    private static final UUID ID = UUID.randomUUID();
    private static final String HEADER_REVISION = "1.0";
    private static final String HEADER_VERSION = "1.0";
    private static final String FILE_NAME = "filename";
    private static final String DUMMY_PAYLOAD = "<SCL/>";

    @Test
    void testSave() {
        SimpleScd simpleScd = createScd(null);
        simpleScd = simpleScdRepository.save(simpleScd);
        assertNotNull(simpleScd.getId());
    }

    @Test
    void testUpdate() {
        SimpleScd simpleScd = createScd(null);
        simpleScd = simpleScdRepository.save(simpleScd);
        assertNotNull(simpleScd.getId());
        simpleScd.setFileName(simpleScd.getFileName() + ".scd");
        simpleScd = simpleScdRepository.update(simpleScd);
        assertTrue(simpleScd.getFileName().endsWith(".scd"));
    }

    @Test
    void testFindById() {

        SimpleScd simpleScd = createScd(null);
        simpleScd = simpleScdRepository.save(simpleScd);
        assertNotNull(simpleScd.getId());

        Optional<SimpleScd> anotherSimpleScd = simpleScdRepository.findById(simpleScd.getId());
        assertTrue(anotherSimpleScd.isPresent());

        anotherSimpleScd = simpleScdRepository.findById(UUID.randomUUID());
        assertFalse(anotherSimpleScd.isPresent());
    }

    @Test
    void testExistsById() {
        SimpleScd simpleScd = createScd(null);
        simpleScd = simpleScdRepository.save(simpleScd);
        assertNotNull(simpleScd.getId());
        assertTrue(simpleScdRepository.existsById(simpleScd.getId()));
        assertFalse(simpleScdRepository.existsById(UUID.randomUUID()));
    }

    @Test
    void testExistsByHeaderId() {
        UUID headerID = UUID.randomUUID();
        final String SCL_CONTENT = "<SCL xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xmlns=\"http://www.iec.ch/61850/2003/SCL\" version=\"2007\" revision=\"B\" release=\"4\">\n" +
                "    <Header id=\"" + headerID.toString() + "\" version=\"version\" revision=\"Revision\"" +
                "            toolID=\"toolID\" nameStructure=\"IEDName\"/>\n" +
                "</SCL>";

        SimpleScd simpleScd = createScd(null);
        simpleScd.setHeaderId(headerID);
        simpleScd.setRawXml(SCL_CONTENT.getBytes(StandardCharsets.UTF_8));
        simpleScd = simpleScdRepository.save(simpleScd);
        assertNotNull(simpleScd.getId());
        assertTrue(simpleScdRepository.existsByHeaderId(headerID));
    }

    @Test
    void testCount() {
        SimpleScd simpleScd = createScd(null);
        simpleScd = simpleScdRepository.save(simpleScd);
        assertNotNull(simpleScd.getId());
        assertEquals(1,simpleScdRepository.count());
    }

    @Test
    void testDeleteById() {
        SimpleScd simpleScd = createScd(null);
        simpleScd = simpleScdRepository.save(simpleScd);
        assertNotNull(simpleScd.getId());
        SimpleScd finalSimpleScd = simpleScd;
        assertDoesNotThrow(() -> simpleScdRepository.deleteById(finalSimpleScd.getId()));
        assertFalse(simpleScdRepository.existsById(finalSimpleScd.getId()));

    }

    private SimpleScd createScd(UUID id){
        SimpleScd scd = new SimpleScd();
        scd.setId(id);
        scd.setRawXml(DUMMY_PAYLOAD.getBytes());
        scd.setFileName(FILE_NAME);
        scd.setHeaderId(id);
        scd.setHeaderRevision(HEADER_REVISION);
        scd.setHeaderVersion(HEADER_VERSION);

        return scd;
    }

}