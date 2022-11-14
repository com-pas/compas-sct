// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.scl.PrivateService;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.lfenergy.compas.sct.commons.util.LDeviceStatus.OFF;
import static org.lfenergy.compas.sct.commons.util.LDeviceStatus.ON;

/**
 * A representation of the model object
 * <em><b>{@link InputsAdapter InputsAdapter}</b></em>.
 *
 * @See TInputs
 * @See TExtRef
 * @see LN0Adapter
 * @see AbstractLNAdapter
 */
@Slf4j
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
     *
     * @return list of encountered errors
     */
    public List<SclReportItem> updateAllExtRefIedNames(Map<String, IEDAdapter> icdSystemVersionToIed) {
        Optional<String> optionalLDeviceStatus = getLDeviceAdapter().getLDeviceStatus();
        if (optionalLDeviceStatus.isEmpty()) {
            return List.of(getLDeviceAdapter().buildFatalReportItem("The LDevice status is undefined"));
        }
        String lDeviceStatus = optionalLDeviceStatus.get();
        switch (lDeviceStatus) {
            case ON:
                List<TCompasFlow> compasFlows = PrivateService.extractCompasPrivates(currentElem, TCompasFlow.class);
                return getExtRefs().stream()
                    .filter(tExtRef -> StringUtils.isNotBlank(tExtRef.getIedName()) && StringUtils.isNotBlank(tExtRef.getDesc()))
                    .map(extRef ->
                        updateExtRefIedName(extRef, compasFlows, icdSystemVersionToIed.get(extRef.getIedName())))
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());
            case OFF:
                getExtRefs().forEach(this::clearBinding);
                return Collections.emptyList();
            default:
                return List.of(getLDeviceAdapter()
                    .buildFatalReportItem("The LDevice status is neither \"on\" nor \"off\""));
        }
    }

    /**
     * Find matching CompasFlow private and set ExtRef iedName accordingly
     *
     * @param extRef         extRef whose iedName will be updated
     * @param allCompasFlows list of all CompasFlow private in this Inputs
     * @return Error if ExtRef could not be updated
     */
    private Optional<SclReportItem> updateExtRefIedName(TExtRef extRef, final List<TCompasFlow> allCompasFlows, IEDAdapter sourceIed) {
        List<TCompasFlow> matchingCompasFlows = getMatchingCompasFlows(extRef, allCompasFlows);
        if (!singleMatch(matchingCompasFlows)) {
            return fatalReportItem(extRef,
                matchingCompasFlows.isEmpty() ? MESSAGE_NO_MATCHING_COMPAS_FLOW : MESSAGE_TOO_MANY_MATCHING_COMPAS_FLOWS);
        }
        TCompasFlow compasFlow = matchingCompasFlows.get(0);
        if (compasFlow.getFlowStatus() == TCompasFlowStatus.INACTIVE) {
            clearBinding(extRef);
            return Optional.empty();
        }
        Optional<SclReportItem> sourceValidationError = validateExtRefSource(extRef, sourceIed);
        if (sourceValidationError.isPresent()) {
            clearBinding(extRef);
            return sourceValidationError;
        }
        String sourceIedName = PrivateService.extractCompasPrivate(sourceIed.getCurrentElem(), TCompasICDHeader.class)
            .map(TCompasICDHeader::getIEDName).orElse("");
        extRef.setIedName(sourceIedName);
        compasFlow.setExtRefiedName(sourceIedName);
        log.debug(String.format("extRef.desc=%s, iedName=%s%n", extRef.getDesc(), sourceIedName));
        return Optional.empty();
    }

    /**
     * List all ExtRefs in this Inputs
     *
     * @return list of ExtRefs. List is modifiable.
     */
    private List<TExtRef> getExtRefs() {
        if (!currentElem.isSetExtRef()) {
            return Collections.emptyList();
        }
        return currentElem.getExtRef();
    }

    private Optional<SclReportItem> validateExtRefSource(TExtRef extRef, IEDAdapter sourceIed) {
        if (sourceIed == null) {
            return warningReportItem(extRef, "The signal ExtRef iedName does not match any " +
                "IED/Private/compas:ICDHeader@ICDSystemVersionUUID");
        }
        Optional<LDeviceAdapter> optionalSourceLDevice = sourceIed.getLDeviceAdapterByLdInst(extRef.getLdInst());
        if (optionalSourceLDevice.isEmpty()) {
            return warningReportItem(extRef, String.format("The signal ExtRef ExtRefldinst does not match any " +
                "LDevice with same inst attribute in source IED %s", sourceIed.getXPath()));
        }
        LDeviceAdapter sourceLDevice = optionalSourceLDevice.get();
        if (!hasMatchingLN(extRef, sourceLDevice)) {
            return warningReportItem(extRef, String.format("The signal ExtRef ExtRefldinst does not match any " +
                "LDevice with same inst attribute in source IED %s", sourceIed.getXPath()));
        }
        Optional<String> optionalSourceLDeviceStatus = sourceLDevice.getLDeviceStatus();
        if (optionalSourceLDeviceStatus.isEmpty()) {
            return fatalReportItem(extRef, String.format("The signal ExtRef source LDevice %s status is undefined",
                sourceLDevice.getXPath()));

        }
        return optionalSourceLDeviceStatus.map(sourceLDeviceStatus -> {
            switch (sourceLDeviceStatus) {
                case OFF:
                    return SclReportItem.warning(extRefXPath(extRef.getDesc()), String.format("The signal ExtRef source LDevice %s status is off",
                        sourceLDevice.getXPath()));
                case ON:
                    return null;
                default:
                    return SclReportItem.fatal(extRefXPath(extRef.getDesc()),
                        String.format("The signal ExtRef source LDevice %s status is neither \"on\" nor \"off\"",
                            sourceLDevice.getXPath()));
            }
        });
    }

    private boolean hasMatchingLN(TExtRef extRef, LDeviceAdapter lDeviceAdapter) {
        String extRefLnClass = extRef.getLnClass().stream().findFirst().orElse("");
        ResumedDataTemplate filter = ResumedDataTemplate.builder()
            .lnClass(extRefLnClass)
            .prefix(extRef.getPrefix())
            .lnInst(extRef.getLnInst())
            .doName(new DoTypeName(extRef.getDoName()))
            .daName(new DaTypeName(extRef.getDaName()))
            .build();
        return !lDeviceAdapter.getDAI(filter, false).isEmpty();
    }

    private boolean singleMatch(List<TCompasFlow> matchingCompasFlows) {
        return matchingCompasFlows.size() == 1;
    }

    private void clearBinding(TExtRef extRef) {
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

    private Optional<SclReportItem> warningReportItem(TExtRef extRef, String message) {
        return Optional.of(SclReportItem.warning(extRefXPath(extRef.getDesc()), message));
    }

    private Optional<SclReportItem> fatalReportItem(TExtRef extRef, String message) {
        return Optional.of(SclReportItem.fatal(extRefXPath(extRef.getDesc()), message));
    }

    private String extRefXPath(String extRefDesc) {
        return getXPath() + String.format("/ExtRef[%s]",
            Utils.xpathAttributeFilter("desc", extRefDesc));
    }

    /**
     * Find CompasFlows that match given ExtRef
     *
     * @param extRef      extRef to match
     * @param compasFlows list of all CompasFlow in which to search
     * @return list of matching CompasFlows
     */
    private List<TCompasFlow> getMatchingCompasFlows(TExtRef extRef, List<TCompasFlow> compasFlows) {
        return compasFlows.stream().filter(compasFlow -> isMatchingExtRef(compasFlow, extRef)).collect(Collectors.toList());
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

    private LDeviceAdapter getLDeviceAdapter() {
        return parentAdapter.getParentAdapter();
    }
}
