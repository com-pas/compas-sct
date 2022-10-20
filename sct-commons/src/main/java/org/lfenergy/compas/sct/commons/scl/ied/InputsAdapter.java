// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;


import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.TCompasFlow;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TInputs;
import org.lfenergy.compas.sct.commons.dto.SclReport;
import org.lfenergy.compas.sct.commons.scl.PrivateService;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A representation of the model object
 * <em><b>{@link InputsAdapter InputsAdapter}</b></em>.
 *
 * @see LN0Adapter
 * @see AbstractLNAdapter
 * @See TInputs
 * @See TExtRef
 */
public class InputsAdapter extends SclElementAdapter<LN0Adapter, TInputs> {

    public static final String MESSAGE_NO_MATCHING_COMPAS_FLOW = "The signal ExtRef has no matching compas:Flow Private";
    public static final String MESSAGE_TOO_MANY_MATCHING_COMPAS_FLOWS = "The signal ExtRef has more than one matching compas:Flow Private";

    /**
     * Constructor
     *
     * @param parentAdapter Parent container reference
     * @param tInputs       Current reference
     */
    public InputsAdapter(LN0Adapter parentAdapter, TInputs tInputs) {
        super(parentAdapter, tInputs);
    }

    /**
     * Check if current element is a child of the parent element
     *
     * @return true if the currentElem is part of the parentAdapter children
     */
    @Override
    protected boolean amChildElementRef() {
        return currentElem == parentAdapter.getCurrentElem().getInputs();
    }

    @Override
    protected String elementXPath() {
        return "Inputs";
    }

    /**
     * Update iedName of all ExtRefs in this "Inputs" element.
     * @return list of encountered errors
     */
    public List<SclReport.ErrorDescription> updateAllExtRefIedNames() {
        List<TCompasFlow> compasFlows = PrivateService.getCompasPrivates(currentElem, TCompasFlow.class);
        return getExtRefs().stream()
            .filter(tExtRef ->
                StringUtils.isNotBlank(tExtRef.getIedName()) && StringUtils.isNotBlank(tExtRef.getDesc()))
            .map(extRef -> updateExtRefIedName(extRef, compasFlows))
            .flatMap(Optional::stream).collect(Collectors.toList());
    }

    /**
     * List all ExtRefs in this Inputs
     * @return list of ExtRefs. List is modifiable.
     */
    private List<TExtRef> getExtRefs() {
        if (!currentElem.isSetExtRef()) {
            return new ArrayList<>();
        }
        return currentElem.getExtRef();
    }

    /**
     * Find matching CompasFlow private and set ExtRef iedName accordingly
     * @param extRef extRef whose iedName will be updated
     * @param compasFlows list of all CompasFlow private in this Inputs
     * @return Error if ExtRef could not be updated
     */
    private Optional<SclReport.ErrorDescription> updateExtRefIedName(TExtRef extRef, final List<TCompasFlow> compasFlows) {
        List<TCompasFlow> matchingCompasFlows = getMatchingCompasFlows(extRef, compasFlows);
        if (matchingCompasFlows.size() == 1) {
            TCompasFlow tCompasFlow = matchingCompasFlows.get(0);
            //TODO: This is not the definitive extRef iedName. The iedName has to be calculated by issue #79(RSR-437)
            extRef.setIedName("matched " + tCompasFlow.getFlowID());
            return Optional.empty();
        } else {
            return Optional.of(buildErrorForExtRef(extRef.getDesc(),
                matchingCompasFlows.isEmpty() ? MESSAGE_NO_MATCHING_COMPAS_FLOW : MESSAGE_TOO_MANY_MATCHING_COMPAS_FLOWS));
        }
    }

    private SclReport.ErrorDescription buildErrorForExtRef(String extRefDesc, String message) {
        return SclReport.ErrorDescription.builder().xpath(getXPath() + String.format("/ExtRef[%s]",
            Utils.xpathAttributeFilter("desc", extRefDesc))).message(message).build();
    }

    /**
     * Find CompasFlows that match given ExtRef
     * @param extRef extRef to match
     * @param compasFlows list of all CompasFlow in which to search
     * @return list of matching CompasFlows
     */
    private List<TCompasFlow> getMatchingCompasFlows(TExtRef extRef, List<TCompasFlow> compasFlows) {
        return compasFlows.stream().filter(compasFlow -> isMatchingExtRef(compasFlow, extRef)).collect(Collectors.toList());
    }

    /**
     * Check if extRef matches CompasFlow
     * @param compasFlow compasFlow
     * @param extRef extRef
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
