// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.api.SclElementsProvider;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.com.CommunicationAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.EnumTypeAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SclElementsProviderService implements SclElementsProvider {

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

        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);

        // Get CBs
        IEDAdapter srcIEDAdapter = sclRootAdapter.getIEDAdapterByName(bindingInfo.getIedName());
        LDeviceAdapter srcLDeviceAdapter = srcIEDAdapter.findLDeviceAdapterByLdInst(extRefInfo.getBindingInfo().getLdInst())
                .orElseThrow();

        List<AbstractLNAdapter<?>> aLNAdapters = srcLDeviceAdapter.getLNAdaptersIncludingLN0();

        return aLNAdapters.stream()
                .map(abstractLNAdapter1 -> abstractLNAdapter1.getControlBlocksForMatchingFCDA(extRefInfo))
                .flatMap(Collection::stream)
                .toList();
    }

    @Override
    public Set<DataAttributeRef> getDAI(SCL scd, String iedName, String ldInst, DataAttributeRef dataAttributeRef, boolean updatable) throws ScdException {
        SclRootAdapter sclRootAdapter = new SclRootAdapter(scd);
        IEDAdapter iedAdapter = sclRootAdapter.getIEDAdapterByName(iedName);
        LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(ldInst);
        return lDeviceAdapter.getDAI(dataAttributeRef, updatable);
    }

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
