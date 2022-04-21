// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.sct.commons.Utils;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LNAdapter;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Getter
@NoArgsConstructor
public class LDeviceDTO {
    private String ldInst;
    private String ldName;
    private Set<LNodeDTO> lNodes = new HashSet<>();

    public LDeviceDTO(String inst, String ldName) {
        this.ldInst = inst;
        this.ldName = ldName;
    }

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

    public void setLdInst(String ldInst){
        this.ldInst = ldInst;
    }

    public void setLdName(String ldName){
        this.ldName = ldName;
    }

    public Set<LNodeDTO> getLNodes() {
        return Set.of(lNodes.toArray(new LNodeDTO[0]));
    }

    public  void addLNode(LNodeDTO ln) {
        lNodes.add(ln);
    }

    public LNodeDTO addLNode(String lnClass, String inst, String lnPrefix, String lnType) {
        LNodeDTO lNodeDTO = new LNodeDTO(inst,lnClass,lnPrefix, lnType);
        lNodes.add(lNodeDTO);
        return lNodeDTO;
    }

    public void addAll(Set<LNodeDTO> lns) {
        lNodes.addAll(lns);
    }
}
