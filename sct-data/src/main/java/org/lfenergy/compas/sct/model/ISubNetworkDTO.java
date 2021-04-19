// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.model;

import java.util.Set;

public interface ISubNetworkDTO {
    String getName();
    String getType();
    <T extends IConnectedApDTO> Set<T> getConnectedAPs();
    <T extends IConnectedApDTO> void addConnectedAPs(T cap);

    void setName(String name);
    void setType(String type);
}
