// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.ied;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.TLDevice;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.sct.commons.dto.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.scl2007b4.model.TLDevice LDevice}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *    <ul>
 *      <li>{@link LDeviceAdapter#getLNAdapters <em>Returns the value of the <b>LNAdapter </b>containment reference list</em>}</li>
 *      <li>{@link LDeviceAdapter#getLN0Adapter <em>Returns the value of the <b>LN0Adapter </b>containment reference list</em>}</li>
 *      <li>{@link LDeviceAdapter#getLNAdapter <em>Returns the value of the <b>LNAdapter </b>reference object By  LNClass, inst and prefix</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link LDeviceAdapter#getInst <em>Returns the value of the <b>inst </b>attribute</em>}</li>
 *      <li>{@link LDeviceAdapter#getLdName <em>Returns the value of the <b>ldName </b>attribute</em>}</li>
 *      <li>{@link LDeviceAdapter#getExtRefInfo em>Returns the value of the <b>ExtRefInfo </b>containment reference</em>}</li>
 *      <li>{@link LDeviceAdapter#getExtRefBinders <em>Returns the value of the <b>ExtRefBindingInfo </b>containment reference list By <b>ExtRefSignalInfo</b></em>}</li>
 *      <li>{@link LDeviceAdapter#getDAI <em>Returns the value of the <b>ResumedDataTemplate </b>containment reference By filter</em>}</li>
 *      <li>{@link LDeviceAdapter#addPrivate <em>Add <b>TPrivate </b>under this object</em>}</li>
 *    </ul>
 * </ol>
 *
 * @see org.lfenergy.compas.sct.commons.scl.ied.LNAdapter
 * @see org.lfenergy.compas.sct.commons.scl.ied.LN0Adapter
 * @see org.lfenergy.compas.scl2007b4.model.TLDevice
 * @see org.lfenergy.compas.scl2007b4.model.TDOI
 * @see org.lfenergy.compas.scl2007b4.model.TDAI
 * @see <a href="https://github.com/com-pas/compas-sct/issues/32" target="_blank">Issue !32</a>
 */
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
                extRefBindingInfo.setLnClass(lnAdapter.getLNClass());
                extRefBindingInfo.setLnInst(lnAdapter.getLNInst());
                extRefBindingInfo.setPrefix(lnAdapter.getPrefix());

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

    public Set<ResumedDataTemplate> getDAI(ResumedDataTemplate rDtt, boolean updatable) throws ScdException {
        List<AbstractLNAdapter<?>> lnAdapters = new ArrayList<>();
        if(StringUtils.isBlank(rDtt.getLnClass())){
            lnAdapters.add(getLN0Adapter());
            lnAdapters.addAll(getLNAdapters());
        } else if(rDtt.getLnClass().equals(TLLN0Enum.LLN_0.value())){
            lnAdapters.add(getLN0Adapter());
        } else {
            lnAdapters.add(getLNAdapter(rDtt.getLnClass(),rDtt.getLnInst(),rDtt.getPrefix()));
        }

        Set<ResumedDataTemplate> resumedDataTemplateSet =  new HashSet<>();
        for(AbstractLNAdapter<?> lnAdapter : lnAdapters){
            ResumedDataTemplate filter = ResumedDataTemplate.copyFrom(rDtt);
            filter.setLnClass(lnAdapter.getLNClass());
            filter.setLnInst(lnAdapter.getLNInst());
            filter.setPrefix(lnAdapter.getPrefix());
            filter.setLnType(lnAdapter.getLnType());
            resumedDataTemplateSet.addAll(lnAdapter.getDAI(filter, updatable));
        }
        return resumedDataTemplateSet;

    }

}
