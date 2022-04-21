// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Getter
@NoArgsConstructor
public class IedDTO {
    private String name;

    private Set<LDeviceDTO> lDevices = new HashSet<>();

    public IedDTO(String name){
        this.name = name;
    }

    public static IedDTO from(IEDAdapter iedAdapter, LogicalNodeOptions options) {
        IedDTO iedDTO = new IedDTO();
        iedDTO.name = iedAdapter.getName();
        List<LDeviceAdapter> lDeviceAdapters = iedAdapter.getLDeviceAdapters();
        iedDTO.lDevices = lDeviceAdapters.stream()
                .map(lDeviceAdapter -> LDeviceDTO.from(lDeviceAdapter,options))
                .collect(Collectors.toUnmodifiableSet());

        return iedDTO;
    }

    public void setName(String name){
        this.name = name;
    }

    public Set<LDeviceDTO> getLDevices() {
        return Set.of(lDevices.toArray(new LDeviceDTO[0]));
    }

    public  void addLDevice(LDeviceDTO ld) {
        lDevices.add(ld);
    }

    public LDeviceDTO lDeviceDTOFrom(String inst, String ldName) {
        return new LDeviceDTO(inst,ldName);
    }

    public LDeviceDTO addLDevice(String inst, String ldName) {
        LDeviceDTO lDeviceDTO = new LDeviceDTO(inst,ldName);
        lDevices.add(lDeviceDTO);

        return lDeviceDTO;
    }

    public Optional<LDeviceDTO> getLDeviceDTO(String ldInst){
        return lDevices.stream()
                .filter(lDeviceDTO1 -> lDeviceDTO1.getLdInst().equals(ldInst))
                .findFirst();
    }

}
