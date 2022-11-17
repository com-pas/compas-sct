// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;


import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.PrivateService;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.util.FcdaCandidatesHelper;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.vavr.API.*;
import static io.vavr.Predicates.isIn;
import static io.vavr.Predicates.isNull;
import static org.lfenergy.compas.scl2007b4.model.TServiceType.*;
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
    public static final String MESSAGE_SOURCE_LDEVICE_NOT_FOUND = "The signal ExtRef ExtRefldinst does not match any " +
        "LDevice with same inst attribute in source IED %s";
    public static final String MESSAGE_SOURCE_LN_NOT_FOUND = "The signal ExtRef lninst, doName or daName does not match any " +
        "source in LDevice %s";
    public static final String MESSAGE_SERVICE_TYPE_MISSING_ON_EXTREF = "The signal ExtRef is missing ServiceType attribute";
    public static final String MESSAGE_INVALID_SERVICE_TYPE = "The signal ExtRef ServiceType attribute is unexpected : %s";
    public static final String MESSAGE_IED_IS_MISSING_PRIVATE_COMPAS_BAY_UUID = "IED is missing Private/compas:Bay@UUID attribute";

    @AllArgsConstructor
    @Getter
    private static class ResumedDataTemplateAdapter {
        private final LDeviceAdapter lDeviceAdapter;
        private final Set<ResumedDataTemplate> resumedDataTemplate;
    }

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
        Optional<LDeviceAdapter> optionalSourceLDevice = sourceIed.findLDeviceAdapterByLdInst(extRef.getLdInst());
        if (optionalSourceLDevice.isEmpty()) {
            return warningReportItem(extRef, String.format(MESSAGE_SOURCE_LDEVICE_NOT_FOUND, sourceIed.getXPath()));
        }
        LDeviceAdapter sourceLDevice = optionalSourceLDevice.get();
        if (sourceLDevice.findSourceDA(extRef).isEmpty()) {
            return warningReportItem(extRef, String.format(MESSAGE_SOURCE_LN_NOT_FOUND, optionalSourceLDevice.get().getXPath()));
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

    private IEDAdapter getIedAdapter() {
        return getLDeviceAdapter().getParentAdapter();
    }

    private SclRootAdapter getSclRootAdapter() {
        return getIedAdapter().getParentAdapter();
    }

    public List<SclReportItem> updateAllSourceDataSetsAndControlBlocks() {
        List<TCompasFlow> compasFlows = PrivateService.extractCompasPrivates(currentElem, TCompasFlow.class);
        String currentBayUuid = getIedAdapter().getPrivateCompasBay().map(TCompasBay::getUUID).orElse(null);
        if (StringUtils.isBlank(currentBayUuid)) {
            return List.of(getIedAdapter().buildFatalReportItem(MESSAGE_IED_IS_MISSING_PRIVATE_COMPAS_BAY_UUID));
        }
        return getExtRefs().stream()
            .filter(this::areBindingAttributesPresent)
            .filter(this::isExternalBound)
            //TODO: replace filter below by criteria of issue #93 RSR-389
            // - Active or Untested i.e /IED/LDevice/LN0/Inputs/Private/compas:Flow @FlowStatus="ACTIVE" OR "UNTESTED"
            .filter(extRef -> !getMatchingCompasFlows(extRef, compasFlows).isEmpty())
            .map(extRef -> updateSourceDataSetsAndControlBlocks(extRef, currentBayUuid))
            .flatMap(Optional::stream)
            .collect(Collectors.toList());
    }

    private boolean isExternalBound(TExtRef tExtRef) {
        return !tExtRef.getIedName().equals(getIedAdapter().getName());
    }

    private boolean areBindingAttributesPresent(TExtRef tExtRef) {
        return StringUtils.isNotBlank(tExtRef.getIedName())
            && StringUtils.isNotBlank(tExtRef.getDesc())
            && StringUtils.isNotBlank(tExtRef.getLdInst())
            && tExtRef.getLnClass().stream().findFirst().filter(StringUtils::isNotBlank).isPresent()
            && StringUtils.isNotBlank(tExtRef.getDoName());
    }

    public Try<IEDAdapter> findSourceIed(TExtRef extRef) {
        return Try.of(() -> getSclRootAdapter().getIEDAdapterByName(extRef.getIedName()));
    }

    public Try<ResumedDataTemplateAdapter> findSourceDa(TExtRef extRef, IEDAdapter sourceIed) {
        return sourceIed.findLDeviceAdapterByLdInst(extRef.getLdInst())
            .map(Try::success)
            .orElseGet(() -> Try.failure(new ScdException(extRefXPath(extRef.getDesc()), MESSAGE_SOURCE_LDEVICE_NOT_FOUND, false)))
            .flatMap(sourceLDevice -> {
                Set<ResumedDataTemplate> sourceDA = sourceLDevice.findSourceDA(extRef);
                if (sourceDA.isEmpty()) {
                    return Try.failure(new ScdException(extRefXPath(extRef.getDesc()), MESSAGE_SOURCE_LN_NOT_FOUND, false));
                }
                sourceDA.removeIf(da -> da.getFc() != TFCEnum.ST && da.getFc() != TFCEnum.MX);
                return Try.success(new ResumedDataTemplateAdapter(sourceLDevice, sourceDA));
            });
    }

    private Optional<SclReportItem> updateSourceDataSetsAndControlBlocks(TExtRef extRef, String targetBayUuid) {
        return findSourceIed(extRef)
            .flatMap(sourceIed -> isBayInternal(extRef, targetBayUuid, sourceIed)
                .flatMap(isBayInternal -> findSourceDa(extRef, sourceIed)
                    .flatMap(sourceDa -> printRelevantDa(extRef, sourceDa))
                    .map(notUsed1 -> Optional.<SclReportItem>empty())
                )
            )
            .getOrElseGet(throwable -> {
                if (throwable instanceof ScdException) {
                    return Optional.of(((ScdException) throwable).toSclReportItem());
                } else {
                    return Optional.of(new SclReportItem(extRefXPath(extRef.getDesc()), throwable.getMessage(), true));
                }
            });
    }

    private Try<ResumedDataTemplateAdapter> printRelevantDa(TExtRef extRef, ResumedDataTemplateAdapter sourceDa) {
        Try<Void> sideEffect = Match(extRef.getServiceType()).of(
            Case($(isIn(GOOSE, SMV)), o -> Try.runRunnable(() -> sourceDa.getResumedDataTemplate().removeIf(Predicate.not(FcdaCandidatesHelper.SINGLETON)))),
            //TODO: In issue RSR-608, replace with mxAndStSources.filter(...)
            Case($(REPORT), o -> Try.runRunnable(() -> sourceDa.getResumedDataTemplate().clear())),
            Case($(isNull()), o -> Try.failure(new ScdException(extRefXPath(extRef.getDesc()), MESSAGE_SERVICE_TYPE_MISSING_ON_EXTREF, true))),
            Case($(), o -> Try.failure(new ScdException(extRefXPath(extRef.getDesc()), String.format(MESSAGE_INVALID_SERVICE_TYPE, o), true)))
        );
        return sideEffect.andThen(() -> {
                String daToPrint = sourceDa.getResumedDataTemplate().stream()
                    .map(da -> da.getFc() + "#" + da.getDataAttributes())
                    .collect(Collectors.joining(","));
                System.out.print("," + (daToPrint.isEmpty() ? "--NO_VALID_SOURCE_DA--" : daToPrint));
            })
            .map(unused -> sourceDa);
    }

    private Try<Boolean> isBayInternal(TExtRef extRef, String targetBayUuid, IEDAdapter sourceIed) {
        return sourceIed.getPrivateCompasBay()
            .map(TCompasBay::getUUID)
            .filter(StringUtils::isNotBlank)
            .map(sourceIedBayUuid -> {
                boolean isBayInternal = sourceIedBayUuid.equals(targetBayUuid);
                System.out.print("\nExtRef.desc=" + extRef.getDesc());
                System.out.print(",isBayInternal=" + isBayInternal);
                return isBayInternal;
            })
            .map(Try::success)
            .orElseGet(() -> Try.failure(new ScdException(extRefXPath(extRef.getDesc()), "Source IED is missing Private/compas:Bay@UUID "
                + "attribute", true)));
    }

}
