// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.sct.model.IIedDTO;
import org.lfenergy.compas.sct.model.ILDeviceDTO;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
public class IedDTO implements IIedDTO {
    private String name;
    private Set<LDeviceDTO> lDevices = new HashSet<>();

    @Override
    public Set<LDeviceDTO> getLDevices() {
        return Set.of(lDevices.toArray(new LDeviceDTO[0]));
    }

    @Override
    public <T extends ILDeviceDTO> void addLDevice(T ld) {
        lDevices.add((LDeviceDTO)ld);
    }
}
