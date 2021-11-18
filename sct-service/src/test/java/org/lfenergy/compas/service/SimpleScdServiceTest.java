// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.header.HeaderAdapter;
import org.lfenergy.compas.sct.data.model.IScd;
import org.lfenergy.compas.sct.data.repository.CompasDataAccessException;
import org.lfenergy.compas.sct.data.repository.IScdCrudRepository;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SimpleScdServiceTest {

    @InjectMocks
    private SimpleScdService simpleScdService;
    @Mock
    private IScdCrudRepository scdCrudRepository;

    @BeforeEach
    void init_mocks() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldBeOKWhenGetElement(){
        UUID id = UUID.randomUUID();

        Mockito.when(scdCrudRepository.findById(ArgumentMatchers.any(UUID.class)))
                .thenReturn(Optional.of(createSimpleScd(id)));

        assertDoesNotThrow(() -> simpleScdService.getElement(id));
    }

    @Test
    public void shouldBeNOKWhenGetElementCauseElementNotFound(){
        UUID id = UUID.randomUUID();

        Mockito.when(scdCrudRepository.findById(ArgumentMatchers.any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(CompasDataAccessException.class, () -> simpleScdService.getElement(id));
    }

    @Test
    public void testSaveElement(){
        UUID id = UUID.randomUUID();
        Mockito.when(scdCrudRepository.save(ArgumentMatchers.any(IScd.class)))
                .thenReturn(createSimpleScd(id));
        assertDoesNotThrow(() -> simpleScdService.saveElement(createSimpleScd(id)));
    }

    @Test
    public void testUpdateElement(){
        UUID id = UUID.randomUUID();
        Mockito.when(scdCrudRepository.save(ArgumentMatchers.any(IScd.class)))
                .thenReturn(createSimpleScd(id));
        assertDoesNotThrow( () -> simpleScdService.updateElement(createSimpleScd(id)));
    }

    @Test
    public void testInitScl(){
        Mockito.when(scdCrudRepository.existsByHeaderId(ArgumentMatchers.any(UUID.class)))
                .thenReturn(false);


        UUID id = UUID.randomUUID();
        SclRootAdapter sclRootAdapter = assertDoesNotThrow(
                ()->simpleScdService.initScl(id,"hVersion","hRevision")
        );
        HeaderAdapter headerAdapter = sclRootAdapter.getHeaderAdapter();
        assertEquals(id.toString(), headerAdapter.getHeaderId());
        assertEquals("hVersion",headerAdapter.getHeaderVersion());
        assertEquals("hRevision",headerAdapter.getHeaderRevision());

        Mockito.when(scdCrudRepository.existsByHeaderId(ArgumentMatchers.any(UUID.class)))
                .thenReturn(true);
        assertThrows(
                ScdException.class,
                ()->simpleScdService.initScl(id,"hVersion","hRevision")
        );
    }

    IScd<UUID> createSimpleScd(UUID id){
        return new IScd<>() {
            @Override
            public UUID getId() {
                return id;
            }

            @Override
            public byte[] getRawXml() {
                return "<SCL/>".getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public UUID getHeaderId() {
                return id;
            }

            @Override
            public String getHeaderRevision() {
                return "hRevision";
            }

            @Override
            public String getHeaderVersion() {
                return "hVersion";
            }
        };
    }
}