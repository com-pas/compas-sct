// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.model;

public interface IConnectedApDTO {
    String getIedName();
    String getApName();

    void setIedName(String name);
    void setApName(String name);
}
