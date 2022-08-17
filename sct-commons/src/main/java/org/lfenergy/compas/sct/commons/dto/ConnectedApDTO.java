// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.sct.commons.scl.com.ConnectedAPAdapter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConnectedApDTO  {
    private String iedName;
    private String apName;

    /**
     * Create ConnectedApDTO from constructor
     * @param connectedAPAdapter object containing data to use
     */
    public ConnectedApDTO(ConnectedAPAdapter connectedAPAdapter) {
        this.iedName = connectedAPAdapter.getIedName();
        this.apName = connectedAPAdapter.getApName();
    }

    /**
     * Convert ConnectedAPAdapter object to dto ConnectedApDTO
     * @param connectedAPAdapter object to convert
     * @return dto ConnectedApDTO
     */
    public static ConnectedApDTO from(ConnectedAPAdapter connectedAPAdapter) {
        ConnectedApDTO connectedApDTO = new ConnectedApDTO();
        connectedApDTO.iedName = connectedAPAdapter.getIedName();
        connectedApDTO.apName = connectedAPAdapter.getApName();

        return connectedApDTO;
    }
}
