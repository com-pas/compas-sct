// SPDX-FileCopyrightText: 2020 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.model.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl.TConnectedAP;
import org.lfenergy.compas.sct.model.IConnectedApDTO;

import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
public class ConnectedApDTO implements IConnectedApDTO {
    private String iedName;
    private String apName;

    public ConnectedApDTO(String iedName, String apName) {
        this.iedName = iedName;
        this.apName = apName;
    }

    public ConnectedApDTO(TConnectedAP tConnectedAP) {
        this.iedName = tConnectedAP.getIedName();
        this.apName = tConnectedAP.getApName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != this.getClass()) return false;
        ConnectedApDTO that = (ConnectedApDTO) o;
        return Objects.equals(iedName, that.iedName) &&
                Objects.equals(apName, that.apName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iedName, apName);
    }
}
