// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.sct.commons.scl.com.ConnectedAPAdapter;

/**
 * A representation of the model object <em><b>Connected AP</b></em>.
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ConnectedApDTO#getApName() <em>Ap Name</em>}</li>
 *   <li>{@link ConnectedApDTO#getIedName() <em>Ied Name</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TConnectedAP
 */
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
