// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LNAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A representation of the model object <em><b>LDevice</b></em>.
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link LDeviceDTO#getLdName <em>LD Name</em>}</li>
 *   <li>{@link LDeviceDTO#getLdInst <em>LD Inst</em>}</li>
 *   <li>{@link LDeviceDTO#getLNodes <em>Refers To LNode</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TIED
 */
@Slf4j
@Getter
@NoArgsConstructor
public class LDeviceDTO {
    private String ldInst;
    private String ldName;
    private Set<LNodeDTO> lNodes = new HashSet<>();

    /**
     * Constructor
     * @param inst input
     * @param ldName input
     */
    public LDeviceDTO(String inst, String ldName) {
        this.ldInst = inst;
        this.ldName = ldName;
    }


    /**
     * Initializes LDeviceDTO
     * @param lDeviceAdapter input
     * @param options input
     * @return LDevice DTO object
     */
    public static LDeviceDTO from(LDeviceAdapter lDeviceAdapter, LogicalNodeOptions options) {
        log.info(Utils.entering());
        LDeviceDTO lDeviceDTO = new LDeviceDTO();
        if(lDeviceAdapter != null) {
            lDeviceDTO.ldInst = lDeviceAdapter.getInst();
            lDeviceDTO.ldName = lDeviceAdapter.getLdName();
            lDeviceDTO.lNodes.add(LNodeDTO.from(lDeviceAdapter.getLN0Adapter(), options));
            List<LNAdapter> lnAdapters = lDeviceAdapter.getLNAdapters();
            lDeviceDTO.lNodes = lnAdapters.stream()
                    .map(lnAdapter -> LNodeDTO.from(lnAdapter, options))
                    .collect(Collectors.toSet());
        }
        log.info(Utils.leaving());
        return lDeviceDTO;
    }

    /**
     * Sets LDevice Inst value
     * @param ldInst input
     */
    public void setLdInst(String ldInst){
        this.ldInst = ldInst;
    }

    /**
     * Sets LDevice name value
     * @param ldName input
     */
    public void setLdName(String ldName){
        this.ldName = ldName;
    }

    /**
     * Gets list of LNodes DTO
     * @return Set of LNodeDTO object
     */
    public Set<LNodeDTO> getLNodes() {
        return Set.of(lNodes.toArray(new LNodeDTO[0]));
    }

    /**
     * Adds LNode
     * @param ln input
     */
    public  void addLNode(LNodeDTO ln) {
        lNodes.add(ln);
    }

    /**
     * Adds LNode
     * @param lnClass input
     * @param inst input
     * @param lnPrefix input
     * @param lnType input
     * @return LNodeDTO object
     */
    public LNodeDTO addLNode(String lnClass, String inst, String lnPrefix, String lnType) {
        LNodeDTO lNodeDTO = new LNodeDTO(inst,lnClass,lnPrefix, lnType);
        lNodes.add(lNodeDTO);
        return lNodeDTO;
    }

    /**
     * Adds Set of LNode DTO
     * @param lns input
     */
    public void addAll(Set<LNodeDTO> lns) {
        lNodes.addAll(lns);
    }
}
