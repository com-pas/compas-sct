// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.TClientLN;
import org.lfenergy.compas.scl2007b4.model.TControl;
import org.lfenergy.compas.scl2007b4.model.TControlWithIEDName;
import org.lfenergy.compas.scl2007b4.model.TPredefinedTypeOfSecurityEnum;
import org.lfenergy.compas.scl2007b4.model.TServiceSettingsNoDynEnum;
import org.lfenergy.compas.scl2007b4.model.TServiceType;
import org.lfenergy.compas.scl2007b4.model.TServices;
import org.lfenergy.compas.sct.commons.Utils;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.LDeviceAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
@NoArgsConstructor
public abstract class ControlBlock<T extends ControlBlock> extends LNodeMetaDataEmbedder {

    protected String id; /// appID or smvID
    protected String name;
    protected String dataSetRef;
    protected String desc;
    protected Long confRev;
    protected List<TControlWithIEDName.IEDName> iedNames = new ArrayList<>();
    protected TPredefinedTypeOfSecurityEnum securityEnable = TPredefinedTypeOfSecurityEnum.NONE;

    protected abstract Class<T> getClassType();
    public abstract TServiceType getServiceType();

    public abstract <U extends TControl> U createControlBlock();

    public T cast(Object obj){
        if (!obj.getClass().isAssignableFrom(getClassType())) {
            throw new UnsupportedOperationException("Cannot cast object to " + getClassType());
        }
        return (T) obj;
    }

    public void validateCB() throws ScdException {

        if(id == null || id.isBlank()){
            throw new ScdException("A required field is missing: ID ");
        }

        if(name == null || name.isBlank()){
            throw new ScdException("A required field is missing:  name");
        }

        if(iedNames.stream().anyMatch( iedName -> iedName == null ||
                iedName.getValue() == null || iedName.getValue().isBlank() ||
                iedName.getLdInst() == null || iedName.getLdInst().isBlank())) {
            throw new ScdException("Control block destination IEDs are not well defined");
        }
    }

    public void validateDestination(SclRootAdapter sclRootAdapter) throws ScdException {
        for(TControlWithIEDName.IEDName iedName : iedNames){
            IEDAdapter iedAdapter =sclRootAdapter.getIEDAdapterByName(iedName.getValue());
            LDeviceAdapter lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(iedName.getLdInst())
                    .orElseThrow(
                            () -> new ScdException(
                                    String.format(
                                            "Control block destination: Unknown LDevice [%s] in IED [%s]",
                                            iedName.getLdInst(),iedName.getValue()
                                    )
                            )
                    );
            if(!iedName.getLnClass().isEmpty()) {
                try {
                    lDeviceAdapter.getLNAdapter(iedName.getLnClass().get(0), iedName.getLnInst(), iedName.getPrefix());
                } catch (ScdException e){
                    throw new ScdException("Control block destination: " + e.getMessage());
                }
            } else {
               Utils.setField(iedName,"lnClass",null);
            }
        }
    }

    public final void validateSecurityEnabledValue(IEDAdapter iedAdapter) throws ScdException {
        TServices tServices = iedAdapter.getServices();
        validateSecurityEnabledValue(tServices);
    }

    protected Long getConfRev() {
        if(dataSetRef == null || dataSetRef.isBlank()){
            return 0L;
        }
        return 10000L ;
    }

    protected abstract void validateSecurityEnabledValue(TServices tServices) throws ScdException;

    public static TControlWithIEDName.IEDName toIEDName(TClientLN clientLN){

        TControlWithIEDName.IEDName iedName = new TControlWithIEDName.IEDName();
        iedName.setValue(clientLN.getIedName());
        iedName.setApRef(clientLN.getApRef());
        iedName.setLdInst(clientLN.getLdInst());
        iedName.setPrefix(clientLN.getPrefix());
        iedName.setLnInst(clientLN.getLnInst());
        iedName.getLnClass().addAll(clientLN.getLnClass());

        return iedName;
    }

    public TServiceSettingsNoDynEnum getControlBlockServiceSetting(TServices tServices){
        if(tServices == null) {
            return TServiceSettingsNoDynEnum.FIX;
        }

        if (getServiceType() == TServiceType.GOOSE && tServices.getGSESettings() != null){
            return tServices.getGSESettings().getCbName();
        }
        if(getServiceType() == TServiceType.SMV && tServices.getSMVSettings() != null){
            return tServices.getSMVSettings().getCbName();
        }
        if(getServiceType() == TServiceType.REPORT && tServices.getReportSettings() != null){
            return tServices.getReportSettings().getCbName();
        }
        return TServiceSettingsNoDynEnum.FIX;
    }

    @Override
    public String toString() {
        String values = iedNames.stream().map(TControlWithIEDName.IEDName::getValue)
                .collect(Collectors.joining(","));
        return "ControlBlock{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", dataSetRef='" + dataSetRef + '\'' +
                ", desc='" + desc + '\'' +
                ", confRev=" + confRev +
                ", iedNames=" + values +
                ", securityEnable=" + securityEnable +
                '}';
    }
}
