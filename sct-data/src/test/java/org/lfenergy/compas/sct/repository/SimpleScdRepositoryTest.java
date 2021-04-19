// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.repository;

import org.junit.jupiter.api.Test;
import org.lfenergy.compas.sct.ApplicationContextTest;
import org.lfenergy.compas.sct.exception.CompasDataAccessException;
import org.lfenergy.compas.sct.model.entity.SimpleScd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ApplicationContextTest.Context.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SimpleScdRepositoryTest {

    @Autowired
    SimpleScdRepository simpleScdRepository;

    @Test
    void testShouldReturnOKWhenGetNextID() {
        assertNotNull(simpleScdRepository.getNextID());
    }

    @Test
    void testShouldReturnOKWhenSave() throws CompasDataAccessException {

        SimpleScd simpleScd = createScd(null);
        SimpleScd expectedScd = simpleScdRepository.save(simpleScd);
        assertNotNull(expectedScd.getId());
    }

    @Test
    void testShouldReturnOKWhenUpdate() throws CompasDataAccessException {
        SimpleScd simpleScd = createScd(null);
        SimpleScd savedScd = simpleScdRepository.save(simpleScd);
        assertNotNull(savedScd.getId());

        savedScd.setHeaderRevision("2.0");
        SimpleScd expectedScd = simpleScdRepository.update(simpleScd);
        assertEquals("2.0", savedScd.getHeaderRevision());
    }

    @Test
    void testShouldReturnOKWhenFindById() throws CompasDataAccessException {

        SimpleScd simpleScd = createScd(null);
        SimpleScd savedScd = simpleScdRepository.save(simpleScd);
        assertNotNull(savedScd.getId());

        SimpleScd expectedScd = simpleScdRepository.findById(savedScd.getId())
                .orElseThrow(() -> new CompasDataAccessException("Not found"));
        assertEquals(expectedScd.getId(),savedScd.getId());

    }

    @Test
    void testShouldReturnNOKWhenFindByIdCauseUnknownId() throws CompasDataAccessException {

        SimpleScd simpleScd = createScd(null);
        SimpleScd savedScd = simpleScdRepository.save(simpleScd);
        assertNotNull(savedScd.getId());

        assertThrows( CompasDataAccessException.class, () -> simpleScdRepository.findById(UUID.randomUUID())
                .orElseThrow(() -> new CompasDataAccessException("Not found")));
    }

    @Test
    void existsById() throws CompasDataAccessException {
        SimpleScd simpleScd = createScd(null);
        SimpleScd savedScd = simpleScdRepository.save(simpleScd);
        assertTrue(simpleScdRepository.existsById(savedScd.getId()));
    }

    @Test
    void existsByHeaderId() throws CompasDataAccessException {
        SimpleScd simpleScd = createScd(null);
        SimpleScd savedScd = simpleScdRepository.save(simpleScd);
        assertTrue(simpleScdRepository.existsByHeaderId(savedScd.getHeaderId()));
    }

    @Test
    void count() {
    }

    @Test
    void deleteById() {
    }

    @Test
    void testGetNextID() throws CompasDataAccessException {
        SimpleScd simpleScd = createScd(UUID.randomUUID());
        assertEquals(simpleScd.getHeaderId(),simpleScdRepository.getNextID(simpleScd));

        simpleScd = simpleScdRepository.save(simpleScd);

        UUID id = simpleScdRepository.getNextID(simpleScd);
        assertNotEquals(id,simpleScd.getHeaderId());

        SimpleScd anotherScd = createScd(null);
        id = simpleScdRepository.getNextID(anotherScd);
        assertNotNull(id);
        assertNotEquals(id,simpleScd.getHeaderId());
    }
    private SimpleScd createScd(UUID id){
        SimpleScd scd = new SimpleScd();
        scd.setId(id);
        scd.setRawXml("blablabla".getBytes());
        scd.setFileName("FN");
        scd.setHeaderId(id);
        scd.setHeaderRevision("RV");
        scd.setHeaderVersion("VR");

        return scd;
    }
}