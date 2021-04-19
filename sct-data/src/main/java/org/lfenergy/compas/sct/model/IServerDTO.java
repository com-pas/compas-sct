// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.model;

import java.util.Set;

public interface IServerDTO {
    <T extends ILDeviceDTO> Set<T> getLDevices();
}
