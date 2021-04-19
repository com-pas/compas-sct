package org.lfenergy.compas.sct.model;

import lombok.NonNull;

import java.util.Set;

public interface IIedDTO {
    String getName();
    void setName(String name);
    <T extends ILDeviceDTO> void addLDevice(T ld);
    <T extends ILDeviceDTO> @NonNull Set<T> getLDevices();
}
