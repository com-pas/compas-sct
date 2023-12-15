/*
 * // SPDX-FileCopyrightText: 2023 RTE FRANCE
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.lfenergy.compas.sct.commons.scl;

import org.lfenergy.compas.scl2007b4.model.TCompasFlow;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TInputs;
import org.lfenergy.compas.sct.commons.util.PrivateUtils;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.stream.Stream;

public class ExtRefService {

    /**
     * List all ExtRefs in this Inputs
     *
     * @return list of ExtRefs. List is modifiable.
     */
    public Stream<TExtRef> getExtRefs(TInputs inputs) {
        if (inputs == null || !inputs.isSetExtRef()) {
            return Stream.empty();
        }
        return inputs.getExtRef().stream();
    }

    /**
     * Find CompasFlows that match given ExtRef
     *
     * @param inputs  inputs containing Privates CompasFlow and TExtRefs
     * @param tExtRef corresponding to CompasFlow we are searching
     * @return list of matching CompasFlows
     */
    public Stream<TCompasFlow> getMatchingCompasFlows(TInputs inputs, TExtRef tExtRef) {
        return PrivateUtils.extractCompasPrivates(inputs, TCompasFlow.class)
                .filter(compasFlow -> isMatchingExtRef(compasFlow, tExtRef));
    }

    /**
     * Retrieves ExtRefs corresponding to given CompasFlow
     *
     * @param inputs      node containing CompasFlows and ExtRefs
     * @param tCompasFlow corresponding to Extrefs we are searching
     * @return stream of matching ExtRefs
     */
    public Stream<TExtRef> getMatchingExtRef(TInputs inputs, TCompasFlow tCompasFlow) {
        return getExtRefs(inputs)
                .filter(tExtRef -> isMatchingExtRef(tCompasFlow, tExtRef));
    }

    /**
     * Debind ExtRef
     *
     * @param extRef to debind
     */
    public void clearExtRefBinding(TExtRef extRef) {
        extRef.setIedName(null);
        extRef.setLdInst(null);
        extRef.setPrefix(null);
        extRef.setLnInst(null);
        extRef.setDoName(null);
        extRef.setDaName(null);
        extRef.setServiceType(null);
        extRef.setSrcLDInst(null);
        extRef.setSrcPrefix(null);
        extRef.setSrcLNInst(null);
        extRef.setSrcCBName(null);
        extRef.unsetLnClass();
        extRef.unsetSrcLNClass();
    }

    /**
     * Debind CompasFlow
     *
     * @param tCompasFlow to debind
     */
    public void clearCompasFlowBinding(TCompasFlow tCompasFlow) {
        tCompasFlow.setExtRefiedName(null);
        tCompasFlow.setExtRefldinst(null);
        tCompasFlow.setExtReflnClass(null);
        tCompasFlow.setExtReflnInst(null);
        tCompasFlow.setExtRefprefix(null);
    }

    /**
     * Check if extRef matches CompasFlow
     *
     * @param compasFlow compasFlow
     * @param extRef     extRef
     * @return true if all required attributes matches. Note that empty string, whitespaces only string and null values are considered as matching
     * (missing attributes matches attribute with empty string value or whitespaces only). Return false otherwise.
     */
    private boolean isMatchingExtRef(TCompasFlow compasFlow, TExtRef extRef) {
        String extRefLnClass = extRef.isSetLnClass() ? extRef.getLnClass().get(0) : null;
        return Utils.equalsOrBothBlank(compasFlow.getDataStreamKey(), extRef.getDesc())
                && Utils.equalsOrBothBlank(compasFlow.getExtRefiedName(), extRef.getIedName())
                && Utils.equalsOrBothBlank(compasFlow.getExtRefldinst(), extRef.getLdInst())
                && Utils.equalsOrBothBlank(compasFlow.getExtRefprefix(), extRef.getPrefix())
                && Utils.equalsOrBothBlank(compasFlow.getExtReflnClass(), extRefLnClass)
                && Utils.equalsOrBothBlank(compasFlow.getExtReflnInst(), extRef.getLnInst());
    }
}
