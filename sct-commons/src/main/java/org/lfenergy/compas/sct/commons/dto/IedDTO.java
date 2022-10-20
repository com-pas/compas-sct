// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A representation of the model object <em><b>IED</b></em>.
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link IedDTO#getName <em>Ied Name</em>}</li>
 *   <li>{@link IedDTO#getLDevices <em>Refers to LDevice</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TIED
 */
@Getter
@NoArgsConstructor
public class IedDTO {
    private String name;

    private Set<LDeviceDTO> lDevices = new HashSet<>();

    /**
     * Constructor
     * @param name input
     */
    public IedDTO(String name){
        this.name = name;
    }

    /**
     * Initializes IedDTO
     * @param iedAdapter input
     * @param options input
     * @return IedDTO object
     */
    public static IedDTO from(IEDAdapter iedAdapter, LogicalNodeOptions options) {
        IedDTO iedDTO = new IedDTO();
        iedDTO.name = iedAdapter.getName();
        iedDTO.lDevices = iedAdapter.streamLDeviceAdapters()
                .map(lDeviceAdapter -> LDeviceDTO.from(lDeviceAdapter,options))
                .collect(Collectors.toUnmodifiableSet());

        return iedDTO;
    }

    /**
     * Sets IED name
     * @param name input
     */
    public void setName(String name){
        this.name = name;
    }

    /**
     * Gets LDevice's' DTO
     * @return Set of LDeviceDTO object
     */
    public Set<LDeviceDTO> getLDevices() {
        return Set.of(lDevices.toArray(new LDeviceDTO[0]));
    }

    /**
     * Adds LDevice in LDevice's' list
     * @param ld input
     */
    public  void addLDevice(LDeviceDTO ld) {
        lDevices.add(ld);
    }

    /**
     * Initializes LDeviceDTO
     * @param inst LDevice inst value
     * @param ldName LDevice name value
     * @return LDeviceDTO object
     */
    public LDeviceDTO lDeviceDTOFrom(String inst, String ldName) {
        return new LDeviceDTO(inst,ldName);
    }

    public LDeviceDTO addLDevice(String inst, String ldName) {
        LDeviceDTO lDeviceDTO = new LDeviceDTO(inst,ldName);
        lDevices.add(lDeviceDTO);

        return lDeviceDTO;
    }

    /**
     * Gets LDeviiceDTO by LDevice inst value
     * @param ldInst LDevice inst value
     * @return Optional LDevice object value
     */
    public Optional<LDeviceDTO> getLDeviceDTO(String ldInst){
        return lDevices.stream()
                .filter(lDeviceDTO1 -> lDeviceDTO1.getLdInst().equals(ldInst))
                .findFirst();
    }

}
