// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;


import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.TLDevice;
import org.lfenergy.compas.sct.commons.dto.ExtRefBindingInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.dto.LNodeDTO;
import org.lfenergy.compas.sct.commons.dto.LogicalNodeOptions;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class LDeviceAdapter extends SclElementAdapter<IEDAdapter, TLDevice> {

    public LDeviceAdapter(IEDAdapter parentAdapter, TLDevice currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getAccessPoint()
                .stream()
                .filter(tAccessPoint -> tAccessPoint.getServer() != null)
                .map(tAccessPoint -> tAccessPoint.getServer().getLDevice())
                .flatMap(Collection::stream)
                .anyMatch(tlDevice -> currentElem.getInst().equals(tlDevice.getInst()));
    }

    public void updateLDName() throws ScdException {
        String newLdName = parentAdapter.getCurrentElem().getName() + currentElem.getInst();
        if(newLdName.length() > 33){
            throw new ScdException(newLdName + "(IED.name + LDevice.inst) has more than 33 characters");
        }
        // renaming ldName
        currentElem.setLdName(newLdName);
    }

    public String getInst(){
        return currentElem.getInst();
    }

    public String getLdName() {
        return currentElem.getLdName();
    }

    public LN0Adapter getLN0Adapter(){
        return new LN0Adapter(this, currentElem.getLN0());
    }

    public List<LNAdapter> getLNAdapters(){
        return currentElem.getLN()
                .stream()
                .map(tln -> new LNAdapter(this,tln))
                .collect(Collectors.toList());
    }

    public LNAdapter getLNAdapter(String lnClass, String lnInst, String prefix) throws ScdException {
        return currentElem.getLN()
                .stream()
                .filter(tln -> tln.getLnClass().contains(lnClass) &&
                        tln.getInst().equals(lnInst) &&
                        ( (prefix == null && tln.getPrefix().isEmpty()) ||
                                prefix.equals(tln.getPrefix()))
                )
                .map(tln -> new LNAdapter(this,tln))
                .findFirst()
                .orElseThrow(
                        ()-> new ScdException(
                            String.format(
                                "LDevice [%s] has no LN [%s,%s,%s]", currentElem.getInst(),lnClass,lnInst,prefix)
                            )
                        );

    }

    public List<ExtRefBindingInfo> getExtRefBinders(ExtRefSignalInfo signalInfo) {
        DataTypeTemplateAdapter dttAdapter = parentAdapter.getParentAdapter().getDataTypeTemplateAdapter();
        List<ExtRefBindingInfo> potentialBinders = new ArrayList<>();

        List<AbstractLNAdapter<?>> lnAdapters = new ArrayList<>();
        lnAdapters.add(getLN0Adapter());
        lnAdapters.addAll(getLNAdapters());
        for(AbstractLNAdapter<?> lnAdapter : lnAdapters) {
            String lnType = lnAdapter.getLnType();
            try {
                ExtRefBindingInfo extRefBindingInfo = dttAdapter.getBinderResumedDTT(lnType,signalInfo);
                extRefBindingInfo.setIedName(parentAdapter.getName());
                extRefBindingInfo.setLdInst(currentElem.getInst());

                potentialBinders.add(extRefBindingInfo);
            } catch (ScdException e) {
                log.debug("ExRef filtered out: {}", e.getLocalizedMessage());
            }
        }
        return potentialBinders;
    }

    public List<ExtRefInfo> getExtRefInfo() {
        List<ExtRefInfo> extRefInfos = new ArrayList<>();
        List<AbstractLNAdapter<?>> lnAdapters = new ArrayList<>();
        lnAdapters.add(getLN0Adapter());
        lnAdapters.addAll(getLNAdapters());
        LogicalNodeOptions logicalNodeOptions = new LogicalNodeOptions();
        logicalNodeOptions.setWithExtRef(true);
        for(AbstractLNAdapter<?> lnAdapter : lnAdapters) {
            LNodeDTO lNodeDTO = LNodeDTO.from(lnAdapter,logicalNodeOptions);
            extRefInfos.addAll(lNodeDTO.getExtRefs());
        }
        return extRefInfos;
    }
}
