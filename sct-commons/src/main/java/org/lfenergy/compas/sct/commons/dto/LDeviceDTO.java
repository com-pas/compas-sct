// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.sct.commons.scl.ied.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LN0Adapter;
import org.lfenergy.compas.sct.commons.scl.ied.LNAdapter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        LDeviceDTO lDeviceDTO = new LDeviceDTO();
        lDeviceDTO.ldInst = lDeviceAdapter.getInst();
        lDeviceDTO.ldName = lDeviceAdapter.getLdName();
        List<LNAdapter> lnAdapters = lDeviceAdapter.getLNAdapters();
        lDeviceDTO.lNodes = lnAdapters.stream()
                .map(lnAdapter -> LNodeDTO.from(lnAdapter, options))
                .collect(Collectors.toSet());
        lDeviceDTO.lNodes.add(LNodeDTO.from(lDeviceAdapter.getLN0Adapter(),options));

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
