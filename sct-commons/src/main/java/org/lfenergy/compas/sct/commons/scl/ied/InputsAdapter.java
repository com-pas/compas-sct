// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.model.da_comm.TFCDA;
import org.lfenergy.compas.sct.commons.scl.ExtRefService;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.LN0Adapter;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.*;
import java.util.function.Predicate;

import static org.lfenergy.compas.sct.commons.util.CommonConstants.*;

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

    private static final String MESSAGE_SOURCE_LDEVICE_NOT_FOUND = "The signal ExtRef ExtRefldinst does not match any " +
            "LDevice with same inst attribute in source IED %s";
    private static final String MESSAGE_SOURCE_LN_NOT_FOUND = "The signal ExtRef lninst, doName or daName does not match any " +
            "source in LDevice %s";
    private static final String MESSAGE_SERVICE_TYPE_MISSING = "The signal ExtRef is missing ServiceType attribute";
    private static final String MESSAGE_INVALID_SERVICE_TYPE = "The signal ExtRef ServiceType attribute is unexpected : %s";
    private static final String MESSAGE_IED_MISSING_COMPAS_BAY_UUID = "IED is missing Private/compas:Bay@UUID attribute";
    private static final String MESSAGE_EXTREF_DESC_MALFORMED = "ExtRef.serviceType=Report but ExtRef.desc attribute is malformed";
    private static final String MESSAGE_SOURCE_IED_NOT_FOUND = "Source IED not found in SCD";
    private static final String MESSAGE_SOURCE_IED_MISSING_COMPAS_BAY_UUID = "Source IED is missing Private/compas:Bay@UUID attribute";
    private static final String MESSAGE_UNABLE_TO_CREATE_DATASET_OR_CONTROLBLOCK = "Could not create DataSet or ControlBlock for this ExtRef : ";
    private static final String MESSAGE_POLL_SERVICE_TYPE_NOT_SUPPORTED = "only GOOSE, SMV and REPORT ServiceType are allowed";

    private static final int EXTREF_DESC_DA_NAME_POSITION = -2;

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

    private Optional<SclReportItem> warningReportItem(TExtRef extRef, String message) {
        return Optional.of(SclReportItem.warning(extRefXPath(extRef.getDesc()), message));
    }

    private Optional<SclReportItem> fatalReportItem(TExtRef extRef, String message) {
        return Optional.of(SclReportItem.error(extRefXPath(extRef.getDesc()), message));
    }

    private String extRefXPath(String extRefDesc) {
        return getXPath() + String.format("/ExtRef[%s]",
                Utils.xpathAttributeFilter("desc", extRefDesc));
    }

    private LDeviceAdapter getLDeviceAdapter() {
        return parentAdapter.getParentAdapter();
    }

    private AbstractLNAdapter<?> getLNAdapter() {
        return parentAdapter;
    }

    public List<SclReportItem> updateAllSourceDataSetsAndControlBlocks(List<TFCDA> allowedFcdas) {
        String currentBayUuid = getIedAdapter().getPrivateCompasBay().map(TCompasBay::getUUID).orElse(null);
        if (StringUtils.isBlank(currentBayUuid)) {
            return List.of(getIedAdapter().buildFatalReportItem(MESSAGE_IED_MISSING_COMPAS_BAY_UUID));
        }
        return currentElem.getExtRef().stream()
                .filter(this::areBindingAttributesPresent)
                .filter(this::isExternalBound)
                .map(extRef -> updateSourceDataSetsAndControlBlocks(extRef, currentBayUuid, allowedFcdas))
                .flatMap(Optional::stream)
                .toList();
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

    private Optional<SclReportItem> updateSourceDataSetsAndControlBlocks(TExtRef extRef, String targetBayUuid, List<TFCDA> allowedFcdas) {
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
        Set<DataAttributeRef> sourceDas = sourceLDevice.findSourceDA(extRef);
        if (sourceDas.isEmpty()) {
            return warningReportItem(extRef, String.format(MESSAGE_SOURCE_LN_NOT_FOUND, optionalSourceLDevice.get().getXPath()));
        }

        Optional<SclReportItem> sclReportItem = removeFilteredSourceDas(extRef, sourceDas, allowedFcdas);
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

    private void createDataSetWithFCDA(TExtRef extRef, LDeviceAdapter sourceLDevice, DataAttributeRef sourceDa, String dataSetName) {
        DataSetAdapter dataSetAdapter = sourceLDevice.getLN0Adapter().createDataSetIfNotExists(dataSetName, ControlBlockEnum.from(extRef.getServiceType()));
        String fcdaDaName = extRef.getServiceType() == TServiceType.REPORT ? null : sourceDa.getDaRef();
        String fcdaLnClass = extRef.getLnClass().stream().findFirst().orElse(null);
        dataSetAdapter.createFCDAIfNotExists(extRef.getLdInst(), extRef.getPrefix(), fcdaLnClass, extRef.getLnInst(),
                sourceDa.getDoRef(),
                fcdaDaName,
                sourceDa.getFc());
    }

    private void createControlBlockWithTarget(TExtRef extRef, LDeviceAdapter sourceLDevice, DataAttributeRef sourceDa, String cbName, String datSet) {
        String sourceLDName = sourceLDevice.getLdName();
        String cbId = getParentAdapter().generateControlBlockId(sourceLDName, cbName);
        ControlBlockAdapter controlBlockAdapter = sourceLDevice.getLN0Adapter().createControlBlockIfNotExists(cbName, cbId, datSet, ControlBlockEnum.from(extRef.getServiceType()));
        if (sourceDa.getFc() != TFCEnum.ST && controlBlockAdapter.getCurrentElem() instanceof TReportControl tReportControl) {
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

    private static String generateDataSetSuffix(TExtRef extRef, DataAttributeRef sourceDa, boolean isBayInternal) {
        return extRef.getLdInst().toUpperCase(Locale.ENGLISH) + ATTRIBUTE_VALUE_SEPARATOR
                + switch (extRef.getServiceType()) {
            case GOOSE -> "G" + ((sourceDa.getFc() == TFCEnum.ST) ? "S" : "M");
            case SMV -> "SV";
            case REPORT -> (sourceDa.getFc() == TFCEnum.ST) ? "DQC" : "CYC";
            case POLL -> throw new IllegalArgumentException(MESSAGE_POLL_SERVICE_TYPE_NOT_SUPPORTED);
        }
                + (isBayInternal ? "I" : "E");
    }

    private Optional<SclReportItem> removeFilteredSourceDas(TExtRef extRef, final Set<DataAttributeRef> sourceDas, List<TFCDA> allowedFcdas) {
        sourceDas.removeIf(da -> da.getFc() != TFCEnum.MX && da.getFc() != TFCEnum.ST);
        return switch (extRef.getServiceType()) {
            case GOOSE, SMV -> {
                sourceDas.removeIf(Predicate.not(dataAttributeRef -> isFcdaAllowed(dataAttributeRef, allowedFcdas)));
                yield Optional.empty();
            }
            case REPORT -> removeFilterSourceDaForReport(extRef, sourceDas);
            default -> fatalReportItem(extRef, String.format(MESSAGE_INVALID_SERVICE_TYPE, extRef.getServiceType()));
        };
    }

    private boolean isFcdaAllowed(DataAttributeRef dataAttributeRef, List<TFCDA> allowedFcdas) {
        String lnClass = dataAttributeRef.getLnClass();
        String doName = dataAttributeRef.getDoName().toStringWithoutInst();
        String daName = dataAttributeRef.getDaName().toString();
        String fc = dataAttributeRef.getFc().value();
        if (StringUtils.isBlank(lnClass) || StringUtils.isBlank(doName) || StringUtils.isBlank(daName) || StringUtils.isBlank(fc)) {
            throw new IllegalArgumentException("parameters must not be blank");
        }
        return allowedFcdas.stream().anyMatch(tfcda -> tfcda.getDoName().equals(doName)
                && tfcda.getDaName().equals(daName)
                && tfcda.getLnClass().equals(lnClass)
                && tfcda.getFc().value().equals(fc));
    }

    private Optional<SclReportItem> removeFilterSourceDaForReport(TExtRef extRef, Set<DataAttributeRef> sourceDas) {
        String daName = Utils.extractField(extRef.getDesc(), ATTRIBUTE_VALUE_SEPARATOR, EXTREF_DESC_DA_NAME_POSITION);
        if (StringUtils.isBlank(daName)) {
            return fatalReportItem(extRef, MESSAGE_EXTREF_DESC_MALFORMED);
        }
        sourceDas.removeIf(dataAttributeRef -> !dataAttributeRef.getDaName().toString().equals(daName));
        return Optional.empty();
    }

    private IEDAdapter getIedAdapter() {
        return getLDeviceAdapter().getParentAdapter();
    }

    private SclRootAdapter getSclRootAdapter() {
        return getIedAdapter().getParentAdapter();
    }

    /**
     * Remove ExtRef which are fed by same Control Block
     *
     * @return list ExtRefs without duplication
     */
    public List<TExtRef> filterDuplicatedExtRefs() {
      return new ExtRefService().filterDuplicatedExtRefs(currentElem.getExtRef());
    }

}
