// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import org.lfenergy.compas.sct.commons.scl.com.ConnectedAPAdapter;

import java.util.Objects;

/**
 * A representation of the model object <em><b>Connected AP</b></em>.
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ConnectedApDTO#apName()} () <em>Ap Name</em>}</li>
 *   <li>{@link ConnectedApDTO#iedName()} () <em>Ied Name</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TConnectedAP
 */
public record ConnectedApDTO(String iedName, String apName)  {

    /**
     * Convert ConnectedAPAdapter object to dto ConnectedApDTO
     *
     * @param connectedAPAdapter object to convert
     * @return dto ConnectedApDTO
     */
    public static ConnectedApDTO from(ConnectedAPAdapter connectedAPAdapter) {
        return new ConnectedApDTO(connectedAPAdapter.getIedName(), connectedAPAdapter.getApName());
    }

}
