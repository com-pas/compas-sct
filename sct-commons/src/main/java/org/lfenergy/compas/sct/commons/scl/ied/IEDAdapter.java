// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.ControlBlock;
import org.lfenergy.compas.sct.commons.dto.DataSetInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefBindingInfo;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.ObjectReference;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;

import java.util.*;
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
                .filter(tlDevice -> Objects.equals(ldInst,tlDevice.getInst()))
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

    public boolean matches(ObjectReference objRef){
        if(!objRef.getLdName().startsWith(getName())) {
            return false;
        }
        Optional<TLDevice> opLD = currentElem.getAccessPoint()
                .stream()
                .filter(tAccessPoint -> tAccessPoint.getServer() != null)
                .map(tAccessPoint -> tAccessPoint.getServer().getLDevice())
                .flatMap(Collection::stream)
                .filter(tlDevice -> objRef.getLdName().equals(getName() + tlDevice.getInst()))
                .findFirst();
        if(opLD.isEmpty()) {
            return false;
        }
        LDeviceAdapter lDeviceAdapter = new LDeviceAdapter(this,opLD.get());
        if(TLLN0Enum.LLN_0.value().equals(objRef.getLNodeName())) {
            return lDeviceAdapter.getLN0Adapter().matches(objRef);
        }

        return lDeviceAdapter.getLNAdapters()
                .stream()
                .anyMatch(lnAdapter -> objRef.getLNodeName().equals(lnAdapter.getLNodeName())
                        && lnAdapter.matches(objRef));
    }

    public boolean findAccessPointByName(String apName) {
        return currentElem.getAccessPoint()
                .stream()
                .anyMatch(tAccessPoint -> tAccessPoint.getName().equals(apName));
    }

    @Override
    protected void addPrivate(TPrivate tPrivate) {
        currentElem.getPrivate().add(tPrivate);
    }

    public List<ExtRefBindingInfo> getExtRefBinders(@NonNull ExtRefSignalInfo signalInfo) throws ScdException {
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

    public boolean isSettingConfig(String ldInst)  {
        TAccessPoint accessPoint = currentElem.getAccessPoint().stream()
                .filter(tAccessPoint ->
                        (tAccessPoint.getServer() != null) &&
                                tAccessPoint.getServer().getLDevice().stream()
                                        .anyMatch(tlDevice -> tlDevice.getInst().equals(ldInst))

                )
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                String.format("LD (%s) is unknown in %s", ldInst, getName())
                        )
                );

        TServices srv = accessPoint.getServices();
        return srv != null && srv.getSettingGroups() != null && srv.getSettingGroups().getConfSG() != null;
    }

    public void createDataSet(DataSetInfo dataSetInfo) throws ScdException {
        if(!hasDataSetCreationCapability()){
            throw new ScdException("The capability of IED is not allowing DataSet creation");
        }

        LDeviceAdapter lDeviceAdapter = getLDeviceAdapterByLdInst(dataSetInfo.getHolderLDInst())
            .orElseThrow(
                () -> new ScdException(
                    String.format(
                        "Unknow LDevice(%s) in IED(%s)",dataSetInfo.getHolderLDInst(),currentElem.getName()                                )
                )
            );
        AbstractLNAdapter<?> lNodeAdapter = AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(dataSetInfo.getHolderLnClass())
                .withLnInst(dataSetInfo.getHolderLnInst())
                .withLnPrefix(dataSetInfo.getHolderLnPrefix())
                .build();
        lNodeAdapter.addDataSet(dataSetInfo);
    }

    protected boolean hasDataSetCreationCapability() {
        if(currentElem.getServices() == null){
            return false ;
        }
        boolean hasCapability = false;
        if(currentElem.getServices().getLogSettings() != null){
            hasCapability = (TServiceSettingsEnum.CONF.equals(currentElem.getServices().getLogSettings().getDatSet()) ||
                        TServiceSettingsEnum.DYN.equals(currentElem.getServices().getLogSettings().getDatSet()));
        }
        if(currentElem.getServices().getGSESettings() != null){
            hasCapability = hasCapability ||
                    (TServiceSettingsEnum.CONF.equals(currentElem.getServices().getGSESettings().getDatSet()) ||
                        TServiceSettingsEnum.DYN.equals(currentElem.getServices().getGSESettings().getDatSet()));

        }
        if(currentElem.getServices().getReportSettings() != null){
            hasCapability = hasCapability ||
                    (TServiceSettingsEnum.CONF.equals(currentElem.getServices().getReportSettings().getDatSet()) ||
                            TServiceSettingsEnum.DYN.equals(currentElem.getServices().getReportSettings().getDatSet()));
        }
        if(currentElem.getServices().getSMVSettings() != null){
            hasCapability = hasCapability ||
                    (TServiceSettingsEnum.CONF.equals(currentElem.getServices().getSMVSettings().getDatSet()) ||
                            TServiceSettingsEnum.DYN.equals(currentElem.getServices().getSMVSettings().getDatSet()));
        }
        return hasCapability ;
    }

    public ControlBlock<? extends ControlBlock> createControlBlock(ControlBlock<? extends ControlBlock> controlBlock)
            throws ScdException {

        TServices tServices = currentElem.getServices();
        TServiceSettingsNoDynEnum cbNameAtt = controlBlock.getControlBlockServiceSetting(tServices);

        if(cbNameAtt != TServiceSettingsNoDynEnum.CONF ){
            throw new ScdException("The IED doesn't support ControlBlock's creation or modification");
        }



        controlBlock.validateCB();
        controlBlock.validateSecurityEnabledValue(this);
        controlBlock.validateDestination(this.parentAdapter);

        LDeviceAdapter lDeviceAdapter =getLDeviceAdapterByLdInst(controlBlock.getHolderLDInst()).orElseThrow();
        AbstractLNAdapter<?> lnAdapter =  AbstractLNAdapter.builder()
                .withLDeviceAdapter(lDeviceAdapter)
                .withLnClass(controlBlock.getHolderLnClass())
                .withLnInst(controlBlock.getHolderLnInst())
                .withLnPrefix(controlBlock.getHolderLnPrefix())
                .build();

        if(lnAdapter.hasControlBlock(controlBlock)){
            throw new ScdException(
                    String.format(
                        "Control block %s already exist in LNode %s%s%s",
                        controlBlock.getName(),lnAdapter.getPrefix(),lnAdapter.getLNClass(),lnAdapter.getLNInst()
                    )
            );
        }

        if(lnAdapter.getDataSetByRef(controlBlock.getDataSetRef()).isEmpty()){
            throw new ScdException(
                    String.format(
                            "Control block %s references unknown dataSet in LNode %s%s%s",
                            controlBlock.getName(),lnAdapter.getPrefix(),lnAdapter.getLNClass(),lnAdapter.getLNInst()
                    )
            );
        }
        lnAdapter.addControlBlock(controlBlock);

        return controlBlock;

    }
}
