package org.lfenergy.compas.sct.model;

import java.util.Set;

public interface IServerDTO {
    <T extends ILDeviceDTO> Set<T> getLDevices();
}
