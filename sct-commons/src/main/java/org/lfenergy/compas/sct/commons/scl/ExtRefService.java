/*
 * // SPDX-FileCopyrightText: 2023 RTE FRANCE
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.lfenergy.compas.sct.commons.scl;

import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.util.PrivateUtils;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ExtRefService {

    /**
     * List all ExtRefs in this LDevice
     *
     * @return list of ExtRefs. List is modifiable.
     */
    public Stream<TExtRef> getExtRefs(TLDevice tlDevice) {
        return getInputs(tlDevice)
                .filter(TInputs::isSetExtRef)
                .stream()
                .flatMap(tInputs -> tInputs.getExtRef().stream());
    }

    /**
     * List all CompasFlows in this LDevice
     *
     * @return list of ExtRefs. List is modifiable.
     */
    public Stream<TCompasFlow> getCompasFlows(TLDevice tlDevice) {
        return getInputs(tlDevice).stream()
                .flatMap(tInputs -> PrivateUtils.extractCompasPrivates(tlDevice.getLN0().getInputs(), TCompasFlow.class));
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
     * @param tlDevice      LDevice containing CompasFlows and ExtRefs
     * @param tCompasFlow corresponding to Extrefs we are searching
     * @return stream of matching ExtRefs
     */
    public Stream<TExtRef> getMatchingExtRefs(TLDevice tlDevice, TCompasFlow tCompasFlow) {
        return getExtRefs(tlDevice)
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

    /**
     * Checks if two ExtRefs fed by same Control Block
     *
     * @param t1 extref to compare
     * @param t2 extref to compare
     * @return true if the two ExtRef are fed by same Control Block, otherwise false
     */
    public boolean isExtRefFeedBySameControlBlock(TExtRef t1, TExtRef t2) {
        String srcLNClass1 = (t1.isSetSrcLNClass()) ? t1.getSrcLNClass().get(0) : TLLN0Enum.LLN_0.value();
        String srcLNClass2 = (t2.isSetSrcLNClass()) ? t2.getSrcLNClass().get(0) : TLLN0Enum.LLN_0.value();
        return Utils.equalsOrBothBlank(t1.getIedName(), t2.getIedName())
                && Utils.equalsOrBothBlank(t1.getSrcLDInst(), t2.getSrcLDInst())
                && srcLNClass1.equals(srcLNClass2)
                && Utils.equalsOrBothBlank(t1.getSrcLNInst(), t2.getSrcLNInst())
                && Utils.equalsOrBothBlank(t1.getSrcPrefix(), t2.getSrcPrefix())
                && Utils.equalsOrBothBlank(t1.getSrcCBName(), t2.getSrcCBName())
                && Objects.equals(t1.getServiceType(), t2.getServiceType());
    }

    /**
     * Remove ExtRef which are fed by same Control Block
     *
     * @return list ExtRefs without duplication
     */
    public List<TExtRef> filterDuplicatedExtRefs(List<TExtRef> tExtRefs) {
        List<TExtRef> filteredList = new ArrayList<>();
        tExtRefs.forEach(tExtRef -> {
            if (filteredList.stream().noneMatch(t -> isExtRefFeedBySameControlBlock(tExtRef, t)))
                filteredList.add(tExtRef);
        });
        return filteredList;
    }

    private Optional<TInputs> getInputs(TLDevice tlDevice){
        if (!tlDevice.isSetLN0() || !tlDevice.getLN0().isSetInputs()) {
            return Optional.empty();
        }
        return Optional.of(tlDevice.getLN0().getInputs());
    }

}
