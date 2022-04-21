
// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.data.model;

public interface IScd <ID> {
    ID getId();
    byte[] getRawXml();
    ID getHeaderId();
    String getHeaderRevision();
    String getHeaderVersion();
}
