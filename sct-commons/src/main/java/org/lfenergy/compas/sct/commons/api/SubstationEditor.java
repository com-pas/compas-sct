// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.api;

import lombok.NonNull;
import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TSubstation;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.sstation.SubstationAdapter;

/**
 * Service class that will be used to create, update or delete elements related to the {@link TSubstation <em>TSubstation</em>} object.
 * <p> The following features are supported: </p>
 * <ul>
 *   <li>{@link SubstationEditor#addSubstation(SCL, SCL) <em>Adds the <b>Substation </b> object from given <b>SCL </b> object</em>}</li>
 * </ul>
 * @see SubstationAdapter
 */
public interface SubstationEditor {

    /**
     * Adds or Updates Substation section in SCL
     * @param scd SCL file in which Substation should be added/updated
     * @param ssd SCL file from which Substation should be copied
     * @throws ScdException throws when SCD contents already another Substation, or with different name, or contents
     * more than one Substation
     */
    void addSubstation(@NonNull SCL scd, @NonNull SCL ssd) throws ScdException;

}
