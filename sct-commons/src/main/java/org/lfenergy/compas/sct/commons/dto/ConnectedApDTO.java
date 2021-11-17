// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.TConnectedAP;
import org.lfenergy.compas.sct.commons.scl.com.ConnectedAPAdapter;

import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
public class ConnectedApDTO  {
    private String iedName;
    private String apName;

    public ConnectedApDTO(ConnectedAPAdapter connectedAPAdapter) {
        this.iedName = connectedAPAdapter.getIedName();
        this.apName = connectedAPAdapter.getApName();
    }

    public static ConnectedApDTO from(ConnectedAPAdapter connectedAPAdapter) {
        ConnectedApDTO connectedApDTO = new ConnectedApDTO();
        connectedApDTO.iedName = connectedAPAdapter.getIedName();
        connectedApDTO.apName = connectedAPAdapter.getApName();

        return connectedApDTO;
    }
}
