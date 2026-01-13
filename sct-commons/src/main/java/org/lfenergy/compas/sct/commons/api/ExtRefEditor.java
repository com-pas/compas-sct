// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.api;

import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.sct.commons.dto.ExtRefInfo;
import org.lfenergy.compas.sct.commons.exception.ScdException;

/**
 * Service class that will be used to create, update or delete elements related to the {@link TExtRef <em>TExtRef</em>} object.
 * <p> The following features are supported: </p>
 * <ul>
 *   <li>ExtRef features</li>
 *   <ol>
 *      <li>{@link ExtRefEditor#updateExtRefBinders <em>Update the <b>TExtRef </b> reference object for given <b>ExtRefBindingInfo </b> model</em>}</li>
 *      <li>{@link ExtRefEditor#updateExtRefSource <em>Update the <b>TExtRef </b> reference object for given <b>ExtRefSourceInfo </b> model</em>}</li>
 *   </ol>
 * </ul>
 */
public interface ExtRefEditor {

    /**
     * Updates ExtRef binding data related to given ExtRef (<em>extRefInfo</em>) in given SCL file
     *
     * @param scd        SCL file in which ExtRef to update should be found
     * @param extRefInfo ExtRef signal for which we should find possible binders in SCL file
     * @throws ScdException throws when mandatory data of ExtRef are missing
     */
    void updateExtRefBinders(SCL scd, ExtRefInfo extRefInfo) throws ScdException;

    /**
     * Updates ExtRef source binding data's based on given data in <em>extRefInfo</em>
     *
     * @param scd        SCL file in which ExtRef source data's to update should be found
     * @param extRefInfo new data for ExtRef source binding data
     * @return <em>TExtRef</em> object as update ExtRef with new source binding data
     * @throws ScdException throws when mandatory data of ExtRef are missing
     */
    TExtRef updateExtRefSource(SCL scd, ExtRefInfo extRefInfo) throws ScdException;

}
