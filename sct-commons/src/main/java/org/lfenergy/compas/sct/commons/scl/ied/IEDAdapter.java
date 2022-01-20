// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.TIED;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;
import org.lfenergy.compas.scl2007b4.model.TServices;
import org.lfenergy.compas.sct.commons.Utils;
import org.lfenergy.compas.sct.commons.dto.ControlBlock;
import org.lfenergy.compas.sct.commons.dto.ExtRefBindingInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.exception.ScdException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
public class IEDAdapter extends SclElementAdapter<SclRootAdapter, TIED> {

    public IEDAdapter(SclRootAdapter parentAdapter) {
        super(parentAdapter);
    }

    public IEDAdapter(SclRootAdapter parentAdapter, TIED currentElem) {
        super(parentAdapter, currentElem);
    }
    public IEDAdapter(SclRootAdapter parentAdapter, String iedName) throws ScdException {
        super(parentAdapter);
        TIED ied = parentAdapter.getCurrentElem().getIED()
                .stream()
                .filter(tied -> tied.getName().equals(iedName))
                .findFirst()
                .orElseThrow(() -> new ScdException("Unknown IED name :" + iedName));
        setCurrentElem(ied);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getIED().contains(currentElem);
    }

    public void setIEDName(String iedName) {
        currentElem.setName(iedName);
    }

    public List<LDeviceAdapter> getLDeviceAdapters(){
        return currentElem.getAccessPoint()
                .stream()
                .filter(tAccessPoint -> tAccessPoint.getServer() != null)
                .map(tAccessPoint -> tAccessPoint.getServer().getLDevice())
                .flatMap(Collection::stream)
                .map(tlDevice -> new LDeviceAdapter(this,tlDevice))
                .collect(Collectors.toList());
    }

    public Optional<LDeviceAdapter> getLDeviceAdapterByLdInst(String ldInst){
        return currentElem.getAccessPoint()
                .stream()
                .filter(tAccessPoint -> tAccessPoint.getServer() != null)
                .map(tAccessPoint -> tAccessPoint.getServer().getLDevice())
                .flatMap(Collection::stream)
                .filter(tlDevice -> ldInst.equals(tlDevice.getInst()))
                .map(tlDevice -> new LDeviceAdapter(this,tlDevice))
                .findFirst();
    }

    public void updateLDeviceNodesType(Map<String, String> pairOldNewId) throws ScdException {
        // renaming ldName
        for(LDeviceAdapter lDeviceAdapter : getLDeviceAdapters()) {
            lDeviceAdapter.updateLDName();
            String lnType = lDeviceAdapter.getCurrentElem().getLN0().getLnType();
            if(pairOldNewId.containsKey(lnType)){
                lDeviceAdapter.getCurrentElem().getLN0().setLnType(pairOldNewId.get(lnType));
            }
            lDeviceAdapter.getCurrentElem()
                    .getLN()
                    .stream()
                    .forEach(tln -> {
                        if(pairOldNewId.containsKey(tln.getLnType())) {
                            tln.setLnType(pairOldNewId.get(tln.getLnType()));
                        }
                    });

        }
    }

    public TServices getServices(){
        return currentElem.getServices();
    }
    public String getName(){
        return currentElem.getName();
    }

    public boolean findAccessPointByName(String apName) {
        return currentElem.getAccessPoint()
                .stream()
                .anyMatch(tAccessPoint -> tAccessPoint.getName().equals(apName));
    }

    public List<ExtRefBindingInfo> getExtRefBinders(ExtRefSignalInfo signalInfo) throws ScdException {
        if(!signalInfo.isValid()){
            throw new ScdException("Invalid ExtRef signal (pDO,pDA or intAddr))");
        }
        List<ExtRefBindingInfo> potentialBinders = new ArrayList<>();
        List<LDeviceAdapter> lDeviceAdapters = getLDeviceAdapters();
        for (LDeviceAdapter lDeviceAdapter : lDeviceAdapters) {
            potentialBinders.addAll(lDeviceAdapter.getExtRefBinders(signalInfo));
        }
        return potentialBinders;
    }

    public List<ControlBlock<?>> getControlBlocksByBindingInfo(ExtRefInfo extRefInfo) {
        log.debug(Utils.entering());
        long startTime = System.nanoTime();
        if(extRefInfo.getBindingInfo() == null) {
            throw new IllegalArgumentException("ExtRef binding information are missing");
        }
        var cbs = getLDeviceAdapters()
                .stream()
                .map(lDeviceAdapter -> {
                    List<AbstractLNAdapter<?>> lnAdapters = new ArrayList<>();
                    if(extRefInfo.getBindingInfo().getLnClass() == null){
                        lnAdapters.add(lDeviceAdapter.getLN0Adapter());
                        lnAdapters.addAll(lDeviceAdapter.getLNAdapters());
                    } else if(TLLN0Enum.LLN_0.value().equals(extRefInfo.getBindingInfo().getLnClass())){
                        lnAdapters.add(lDeviceAdapter.getLN0Adapter());
                    } else {
                        try {
                            lnAdapters.add(
                                    lDeviceAdapter.getLNAdapter(
                                            extRefInfo.getBindingInfo().getLnClass(),
                                            extRefInfo.getBindingInfo().getLnInst(),
                                            extRefInfo.getBindingInfo().getPrefix())
                            );
                        } catch (ScdException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                    return lnAdapters;
                })
                .flatMap(Collection::stream)
                .map(lnAdapter -> lnAdapter.getControlSetByExtRefInfo(extRefInfo))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        log.debug(Utils.leaving(startTime));
        return cbs;
    }
}
