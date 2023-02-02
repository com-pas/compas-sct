// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.ResumedDataTemplate;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.PrivateService;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;
import org.lfenergy.compas.sct.commons.util.FcdaCandidates;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.*;
import java.util.function.Predicate;

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

    private static final String MESSAGE_NO_MATCHING_COMPAS_FLOW = "The signal ExtRef has no matching compas:Flow Private";
    private static final String MESSAGE_TOO_MANY_MATCHING_COMPAS_FLOWS = "The signal ExtRef has more than one matching compas:Flow Private";
    private static final String MESSAGE_SOURCE_LDEVICE_NOT_FOUND = "The signal ExtRef ExtRefldinst does not match any " +
        "LDevice with same inst attribute in source IED %s";
    private static final String MESSAGE_SOURCE_LN_NOT_FOUND = "The signal ExtRef lninst, doName or daName does not match any " +
        "source in LDevice %s";
    private static final String MESSAGE_SERVICE_TYPE_MISSING = "The signal ExtRef is missing ServiceType attribute";
    private static final String MESSAGE_INVALID_SERVICE_TYPE = "The signal ExtRef ServiceType attribute is unexpected : %s";
    private static final String MESSAGE_IED_MISSING_COMPAS_BAY_UUID = "IED is missing Private/compas:Bay@UUID attribute";
    private static final String MESSAGE_EXTREF_DESC_MALFORMED = "ExtRef.serviceType=Report but ExtRef.desc attribute is malformed";
    private static final String MESSAGE_LDEVICE_STATUS_UNDEFINED = "The LDevice status is undefined";
    private static final String MESSAGE_LDEVICE_STATUS_NEITHER_ON_NOR_OFF = "The LDevice status is neither \"on\" nor \"off\"";
    private static final String MESSAGE_EXTREF_IEDNAME_DOES_NOT_MATCH_ANY_SYSTEM_VERSION_UUID = "The signal ExtRef iedName does not match any " +
        "IED/Private/compas:ICDHeader@ICDSystemVersionUUID";
    private static final String MESSAGE_SOURCE_LDEVICE_STATUS_UNDEFINED = "The signal ExtRef source LDevice %s status is undefined";
    private static final String MESSAGE_SOURCE_LDEVICE_STATUS_NEITHER_ON_NOR_OFF = "The signal ExtRef source LDevice %s status is neither \"on\" nor \"off\"";
    private static final String MESSAGE_SOURCE_LDEVICE_STATUS_OFF = "The signal ExtRef source LDevice %s status is off";
    private static final String MESSAGE_SOURCE_IED_NOT_FOUND = "Source IED not found in SCD";
    private static final String MESSAGE_SOURCE_IED_MISSING_COMPAS_BAY_UUID = "Source IED is missing Private/compas:Bay@UUID attribute";
    private static final String MESSAGE_UNABLE_TO_CREATE_DATASET_OR_CONTROLBLOCK = "Could not create DataSet or ControlBlock for this ExtRef : ";
    private static final String MESSAGE_POLL_SERVICE_TYPE_NOT_SUPPORTED = "only GOOSE, SMV and REPORT ServiceType are allowed";

    private static final int EXTREF_DESC_DA_NAME_POSITION = -2;
    private static final String ATTRIBUTE_VALUE_SEPARATOR = "_";
    private static final String DATASET_NAME_PREFIX = "DS" + ATTRIBUTE_VALUE_SEPARATOR;
    private static final String CONTROLBLOCK_NAME_PREFIX = "CB" + ATTRIBUTE_VALUE_SEPARATOR;

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
            return List.of(getLDeviceAdapter().buildFatalReportItem(MESSAGE_LDEVICE_STATUS_UNDEFINED));
        }
        String lDeviceStatus = optionalLDeviceStatus.get();
        return switch (lDeviceStatus) {
            case ON -> {
                List<TCompasFlow> compasFlows = PrivateService.extractCompasPrivates(currentElem, TCompasFlow.class);
                yield getExtRefs().stream()
                    .filter(tExtRef -> StringUtils.isNotBlank(tExtRef.getIedName()) && StringUtils.isNotBlank(tExtRef.getDesc()))
                    .map(extRef ->
                        updateExtRefIedName(extRef, compasFlows, icdSystemVersionToIed.get(extRef.getIedName())))
                    .flatMap(Optional::stream)
                    .toList();
            }
            case OFF -> {
                getExtRefs().forEach(this::clearBinding);
                yield Collections.emptyList();
            }
            default -> List.of(getLDeviceAdapter().buildFatalReportItem(MESSAGE_LDEVICE_STATUS_NEITHER_ON_NOR_OFF));
        };
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
            return warningReportItem(extRef, MESSAGE_EXTREF_IEDNAME_DOES_NOT_MATCH_ANY_SYSTEM_VERSION_UUID);
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
            return fatalReportItem(extRef, String.format(MESSAGE_SOURCE_LDEVICE_STATUS_UNDEFINED,
                sourceLDevice.getXPath()));
        }
        return optionalSourceLDeviceStatus.map(sourceLDeviceStatus ->
            switch (sourceLDeviceStatus) {
                case OFF -> SclReportItem.warning(extRefXPath(extRef.getDesc()), String.format(MESSAGE_SOURCE_LDEVICE_STATUS_OFF,
                    sourceLDevice.getXPath()));
                case ON -> null;
                default -> SclReportItem.fatal(extRefXPath(extRef.getDesc()),
                    String.format(MESSAGE_SOURCE_LDEVICE_STATUS_NEITHER_ON_NOR_OFF,
                        sourceLDevice.getXPath()));
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
        return compasFlows.stream().filter(compasFlow -> isMatchingExtRef(compasFlow, extRef)).toList();
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

    private AbstractLNAdapter<?> getLNAdapter(){
        return parentAdapter;
    }

    public List<SclReportItem> updateAllSourceDataSetsAndControlBlocks() {
        List<TCompasFlow> compasFlows = PrivateService.extractCompasPrivates(currentElem, TCompasFlow.class);
        String currentBayUuid = getIedAdapter().getPrivateCompasBay().map(TCompasBay::getUUID).orElse(null);
        if (StringUtils.isBlank(currentBayUuid)) {
            return List.of(getIedAdapter().buildFatalReportItem(MESSAGE_IED_MISSING_COMPAS_BAY_UUID));
        }
        return getExtRefs().stream()
            .filter(this::areBindingAttributesPresent)
            .filter(this::isExternalBound)
            .filter(extRef -> matchingCompasFlowIsActiveOrUntested(extRef, compasFlows))
            .map(extRef -> updateSourceDataSetsAndControlBlocks(extRef, currentBayUuid))
            .flatMap(Optional::stream)
            .toList();
    }

    private boolean matchingCompasFlowIsActiveOrUntested(TExtRef extRef, List<TCompasFlow> compasFlows) {
        return getMatchingCompasFlows(extRef, compasFlows).stream().findFirst()
            .map(TCompasFlow::getFlowStatus)
            .filter(flowStatus -> flowStatus == TCompasFlowStatus.ACTIVE || flowStatus == TCompasFlowStatus.UNTESTED)
            .isPresent();
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

    private Optional<SclReportItem> updateSourceDataSetsAndControlBlocks(TExtRef extRef, String targetBayUuid) {
        if (extRef.getServiceType() == null) {
            return fatalReportItem(extRef, MESSAGE_SERVICE_TYPE_MISSING);
        }
        Optional<IEDAdapter> optionalSrcIedAdapter = getSclRootAdapter().findIedAdapterByName(extRef.getIedName());
        if (optionalSrcIedAdapter.isEmpty()) {
            return fatalReportItem(extRef, MESSAGE_SOURCE_IED_NOT_FOUND);
        }
        IEDAdapter sourceIed = optionalSrcIedAdapter.get();
        Optional<String> sourceIedBayUuid = sourceIed.getPrivateCompasBay()
            .map(TCompasBay::getUUID)
            .filter(StringUtils::isNotBlank);
        if (sourceIedBayUuid.isEmpty()) {
            return fatalReportItem(extRef, MESSAGE_SOURCE_IED_MISSING_COMPAS_BAY_UUID);
        }

        boolean isBayInternal = targetBayUuid.equals(sourceIedBayUuid.get());

        Optional<LDeviceAdapter> optionalSourceLDevice = sourceIed.findLDeviceAdapterByLdInst(extRef.getLdInst());
        if (optionalSourceLDevice.isEmpty()) {
            return warningReportItem(extRef, String.format(MESSAGE_SOURCE_LDEVICE_NOT_FOUND, sourceIed.getXPath()));
        }
        LDeviceAdapter sourceLDevice = optionalSourceLDevice.get();
        Set<ResumedDataTemplate> sourceDas = sourceLDevice.findSourceDA(extRef);
        if (sourceDas.isEmpty()) {
            return warningReportItem(extRef, String.format(MESSAGE_SOURCE_LN_NOT_FOUND, optionalSourceLDevice.get().getXPath()));
        }

        Optional<SclReportItem> sclReportItem = removeFilteredSourceDas(extRef, sourceDas);
        if (sclReportItem.isPresent()) {
            return sclReportItem;
        }

        try {
            sourceDas.forEach(sourceDa -> {
                String datasetSuffix = generateDataSetSuffix(extRef, sourceDa, isBayInternal);
                String dataSetName = DATASET_NAME_PREFIX + datasetSuffix;
                String cbName = CONTROLBLOCK_NAME_PREFIX + datasetSuffix;
                createDataSetWithFCDA(extRef, sourceLDevice, sourceDa, dataSetName);
                createControlBlockWithTarget(extRef, sourceLDevice, sourceDa, cbName, dataSetName);
                setExtRefSrcAttributes(extRef, cbName);
            });
        } catch (ScdException e) {
            // ScdException can be thrown if AccessPoint does not have DataSet/ControlBlock creation capability
            log.error(e.getMessage(), e);
            return fatalReportItem(extRef, MESSAGE_UNABLE_TO_CREATE_DATASET_OR_CONTROLBLOCK + e.getMessage());
        }
        return Optional.empty();
    }

    private void createDataSetWithFCDA(TExtRef extRef, LDeviceAdapter sourceLDevice, ResumedDataTemplate sourceDa, String dataSetName) {
        DataSetAdapter dataSetAdapter = sourceLDevice.getLN0Adapter().createDataSetIfNotExists(dataSetName, ControlBlockEnum.from(extRef.getServiceType()));
        String fcdaDaName = extRef.getServiceType() == TServiceType.REPORT ? null : sourceDa.getDaRef();
        String fcdaLnClass = extRef.getLnClass().stream().findFirst().orElse(null);
        dataSetAdapter.createFCDAIfNotExists(extRef.getLdInst(), extRef.getPrefix(), fcdaLnClass, extRef.getLnInst(),
            sourceDa.getDoRef(),
            fcdaDaName,
            sourceDa.getFc());
    }

    private void createControlBlockWithTarget(TExtRef extRef, LDeviceAdapter sourceLDevice, ResumedDataTemplate sourceDa, String cbName, String datSet) {
        String cbId = generateControlBlockId(cbName, sourceLDevice.getLdName(), getParentAdapter());
        ControlBlockAdapter controlBlockAdapter = sourceLDevice.getLN0Adapter().createControlBlockIfNotExists(cbName, cbId, datSet, ControlBlockEnum.from(extRef.getServiceType()));
        if (sourceDa.getFc() != TFCEnum.ST && controlBlockAdapter.getCurrentElem() instanceof TReportControl tReportControl){
            tReportControl.getTrgOps().setDchg(false);
            tReportControl.getTrgOps().setQchg(false);
        }
        controlBlockAdapter.addTargetIfNotExists(getLNAdapter());
    }

    private void setExtRefSrcAttributes(TExtRef extRef, String cbName) {
        extRef.setSrcCBName(cbName);
        extRef.setSrcLDInst(extRef.getLdInst());
        // srcPrefix, srcLNInst and srcLNClass are set to empty because ControlBlock is created in LN0
        extRef.setSrcPrefix(null);
        extRef.setSrcLNInst(null);
        extRef.unsetSrcLNClass();
    }

    private static String generateDataSetSuffix(TExtRef extRef, ResumedDataTemplate sourceDa, boolean isBayInternal) {
        return extRef.getLdInst() + ATTRIBUTE_VALUE_SEPARATOR
            + switch (extRef.getServiceType()) {
            case GOOSE -> "G" + ((sourceDa.getFc() == TFCEnum.ST) ? "S" : "M");
            case SMV -> "SV";
            case REPORT -> (sourceDa.getFc() == TFCEnum.ST) ? "DQC" : "CYC";
            case POLL -> throw new IllegalArgumentException(MESSAGE_POLL_SERVICE_TYPE_NOT_SUPPORTED);
        }
            + (isBayInternal ? "I" : "E");
    }

    private String generateControlBlockId(String cbName, String sourceLDName, AbstractLNAdapter<?> targetLn) {
        return Utils.emptyIfBlank(sourceLDName)
            + "/"
            + Utils.emptyIfBlank(targetLn.getPrefix())
            + Objects.requireNonNullElse(targetLn.getLNClass(), "")
            + Utils.emptyIfBlank(targetLn.getLNInst())
            + "."
            + cbName;
    }

    private Optional<SclReportItem> removeFilteredSourceDas(TExtRef extRef, final Set<ResumedDataTemplate> sourceDas) {
        sourceDas.removeIf(da -> da.getFc() != TFCEnum.MX && da.getFc() != TFCEnum.ST);
        return switch (extRef.getServiceType()) {
            case GOOSE, SMV -> {
                sourceDas.removeIf(Predicate.not(FcdaCandidates.SINGLETON::contains));
                yield Optional.empty();
            }
            case REPORT -> removeFilterSourceDaForReport(extRef, sourceDas);
            default -> fatalReportItem(extRef, String.format(MESSAGE_INVALID_SERVICE_TYPE, extRef.getServiceType()));
        };
    }

    private Optional<SclReportItem> removeFilterSourceDaForReport(TExtRef extRef, Set<ResumedDataTemplate> sourceDas) {
        String daName = Utils.extractField(extRef.getDesc(), ATTRIBUTE_VALUE_SEPARATOR, EXTREF_DESC_DA_NAME_POSITION);
        if (StringUtils.isBlank(daName)) {
            return fatalReportItem(extRef, MESSAGE_EXTREF_DESC_MALFORMED);
        }
        sourceDas.removeIf(resumedDataTemplate -> !resumedDataTemplate.getDaName().toString().equals(daName));
        return Optional.empty();
    }

    private IEDAdapter getIedAdapter() {
        return getLDeviceAdapter().getParentAdapter();
    }

    private SclRootAdapter getSclRootAdapter() {
        return getIedAdapter().getParentAdapter();
    }

}
