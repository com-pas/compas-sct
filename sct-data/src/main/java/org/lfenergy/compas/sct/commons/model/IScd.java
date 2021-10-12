
// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.model;

public interface IScd <ID> {
    ID getId();
    byte[] getRawXml();
    ID getHeaderId();
}
