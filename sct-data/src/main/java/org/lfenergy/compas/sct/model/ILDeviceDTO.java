// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.model;

import org.springframework.lang.NonNull;

import java.util.Set;

public interface ILDeviceDTO {
    String getLdInst();
    String getLdName();

    void setLdName(String ldName);
    void setLdInst(String inst);

    <T extends ILNodeDTO> void addLNode(@NonNull T ln);
    <T extends ILNodeDTO> Set<T> getLNodes();
}
