// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.api.SclElementsProvider;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.com.CommunicationAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.EnumTypeAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.*;
import org.lfenergy.compas.sct.commons.scl.ldevice.LDeviceAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.scl.ln.LnKey;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SclElementsProviderService implements SclElementsProvider {

    private static final String MESSAGE_IED_NAME_NOT_FOUND = "IED.name '%s' not found in SCD";
    private static final String MESSAGE_LDEVICE_INST_NOT_FOUND = "LDevice.inst '%s' not found in IED '%s'";

    private final IedService iedService = new IedService();
    private final LdeviceService ldeviceService = new LdeviceService();
    private final DataTypeTemplatesService dataTypeTemplatesService = new DataTypeTemplatesService();
    private final LnService lnService = new LnService();

    @Override
    public List<SubNetworkDTO> getSubnetwork(SCL scd) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        CommunicationAdapter communicationAdapter = sclRootAdapter.getCommunicationAdapter(false);
        return communicationAdapter.getSubNetworkAdapters().stream()
                .map(SubNetworkDTO::from)
                .toList();
    }

    @Override
    public List<ExtRefInfo> getExtRefInfo(SCL scd, String iedName, String ldInst) throws ScdException {
        LDeviceAdapter lDeviceAdapter = createLDeviceAdapter(scd, iedName, ldInst);
        return lDeviceAdapter.getExtRefInfo();
    }

    @Override
    public List<ExtRefBindingInfo> getExtRefBinders(SCL scd, String iedName, String ldInst, String lnClass, String lnInst, String prefix, ExtRefSignalInfo signalInfo) throws ScdException {
        LDeviceAdapter lDeviceAdapter = createLDeviceAdapter(scd, iedName, ldInst);
        AbstractLNAdapter<?> abstractLNAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(lnClass)
                .withLnInst(lnInst)
                .withLnPrefix(prefix)
                .build();

        // check for signal existence
        abstractLNAdapter.isExtRefExist(signalInfo);

        // find potential binders for the signalInfo
        return lDeviceAdapter.getParentAdapter().getParentAdapter().streamIEDAdapters()
                .map(iedAdapter1 -> iedAdapter1.getExtRefBinders(signalInfo))
                .flatMap(Collection::stream)
                .sorted()
                .toList();
    }

    private LDeviceAdapter createLDeviceAdapter(SCL scd, String iedName, String ldInst) {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        return iedAdapter.getLDeviceAdapterByLdInst(ldInst);
    }

    @Override
    public List<ControlBlock> getExtRefSourceInfo(SCL scd, ExtRefInfo extRefInfo) throws ScdException {

        ExtRefSignalInfo signalInfo = extRefInfo.getSignalInfo();
        if (!signalInfo.isValid()) {
            throw new ScdException("Invalid or missing attributes in ExtRef signal info");
        }
        ExtRefBindingInfo bindingInfo = extRefInfo.getBindingInfo();
        if (!bindingInfo.isValid()) {
            throw new ScdException("Invalid or missing attributes in ExtRef binding info");
        }

        String iedName = extRefInfo.getHolderIEDName();
        if (bindingInfo.getIedName().equals(iedName)) {
            throw new ScdException("Internal binding can't have control block");
        }

        // Get CBs
        return new SclRootAdapter(scd)
                .getIEDAdapterByName(bindingInfo.getIedName())
                .findLDeviceAdapterByLdInst(extRefInfo.getBindingInfo().getLdInst())
                .orElseThrow()
                .getLNAdaptersIncludingLN0()
                .stream()
                .flatMap(lnAdapter -> lnAdapter.getControlBlocksForMatchingFCDA(extRefInfo))
                .toList();
    }

    @Override
    public Set<DataAttributeRef> getDAI(SCL scd, String iedName, String ldInst, DataAttributeRef dataAttributeRef, boolean updatable) throws ScdException {
        return iedService.findIed(scd, tied -> tied.getName().equals(iedName))
                .map(tied1 -> ldeviceService.findLdevice(tied1, tlDevice -> tlDevice.getInst().equals(ldInst))
                        .map(tlDevice -> Stream.concat(tlDevice.getLN().stream(), Stream.of(tlDevice.getLN0()))
                                .filter(anyLN -> tAnyLNPredicate.test(anyLN, dataAttributeRef))
                                .flatMap(tAnyLN -> dataTypeTemplatesService.getFilteredDOAndDA(scd.getDataTypeTemplates(), tAnyLN, DataAttributeRef.updateDataRef(tAnyLN, dataAttributeRef))
                                        .map(dataAttribute -> {
                                            lnService.completeFromDAInstance(tied1, tlDevice.getInst(), tAnyLN, dataAttribute);
                                            return dataAttribute;
                                        }))
                                .filter(dataRef -> !updatable || dataRef.isUpdatable())
                                .collect(Collectors.toSet()))
                        .orElseThrow(() -> new ScdException(String.format(MESSAGE_LDEVICE_INST_NOT_FOUND, ldInst, iedName))))
                .orElseThrow(() -> new ScdException(String.format(MESSAGE_IED_NAME_NOT_FOUND, iedName)));
    }

    private final BiPredicate<TAnyLN, DataAttributeRef> tAnyLNPredicate = (anyLN, dataRef) ->
            StringUtils.isBlank(dataRef.getLnClass())
                    || (anyLN instanceof TLN0
                    && (dataRef.getLnClass() != null && dataRef.getLnClass().equals(TLLN0Enum.LLN_0.value())))
                    || (anyLN instanceof TLN ln
                    && Utils.lnClassEquals(ln.getLnClass(), dataRef.getLnClass())
                    && ln.getInst().equals(dataRef.getLnInst())
                    && Utils.equalsOrBothBlank(dataRef.getPrefix(), ln.getPrefix()));

    @Override
    public Set<EnumValDTO> getEnumTypeValues(SCL scd, String idEnum) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        DataTypeTemplateAdapter dataTypeTemplateAdapter = sclRootAdapter.getDataTypeTemplateAdapter();
        EnumTypeAdapter enumTypeAdapter = dataTypeTemplateAdapter.getEnumTypeAdapterById(idEnum)
                .orElseThrow(() -> new ScdException("Unknown EnumType Id: " + idEnum));
        return enumTypeAdapter.getCurrentElem().getEnumVal().stream()
                .map(tEnumVal -> new EnumValDTO(tEnumVal.getOrd(), tEnumVal.getValue()))
                .collect(Collectors.toSet());
    }

}
