// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.util.ControlBlockEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * A representation of the model object <em><b>ControlBlock</b></em>.
 * - {@link org.lfenergy.compas.sct.commons.dto.GooseControlBlock <em>GooseControlBlock</em>},
 * - {@link org.lfenergy.compas.sct.commons.dto.ReportControlBlock <em>ReportControlBlock</em>},
 * - {@link org.lfenergy.compas.sct.commons.dto.SMVControlBlock <em>SMVControlBlock</em>},
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ControlBlock#getId() <em>appID, smvID or rptID</em>}</li>
 *   <li>{@link ControlBlock#getName() <em>Name</em>}</li>
 *   <li>{@link ControlBlock#getDataSetRef() <em>dataSetRef</em>}</li>
 *   <li>{@link ControlBlock#getDesc() <em>Desc</em>}</li>
 *   <li>{@link ControlBlock#getConfRev() <em>Refers To confRev</em>}</li>
 *   <li>{@link ControlBlock#getTargets() <em>Refers To IedNames</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TControlBlock
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class ControlBlock extends LNodeMetaDataEmbedder {

    protected String id; /// appID or smvID or rptID
    protected String name;
    protected String dataSetRef;
    protected String desc;
    protected Long confRev = 10000L;
    protected List<ControlBlockTarget> targets = new ArrayList<>();

    /**
     * Get ServiceType
     * @return ServiceType enum object
     * @deprecated TServiceType provides  REPORT, SMV, GOOSE and POLL. It does not provide "LOG" for LogControl and provides "POLL" which is not a valid
     * ControlBlock type. Use getControlBlockEnum() instead.
     * @see ControlBlock#getControlBlockEnum()
     */
    @Deprecated(since = "16/01/2023")
    public TServiceType getServiceType() {
        return switch (getControlBlockEnum()){
            case GSE -> TServiceType.GOOSE;
            case SAMPLED_VALUE -> TServiceType.SMV;
            case REPORT -> TServiceType.REPORT;
            default -> throw new IllegalArgumentException("Unsupported ControlBlockEnum : " + getControlBlockEnum());
        };
    }

    /**
     * Get ControlBlockEnum
     * @return ControlBlockEnum of the instance
     */
    public abstract ControlBlockEnum getControlBlockEnum();

    /**
     * Map the object to TControl
     * @return new instance of TControl element with all the attributes copied from the current object
     */
    public abstract TControl toTControl();

    /**
     * Validate Control block structure
     * @throws ScdException when ControlBlock structure is not valid
     */
    public void validateCB() throws ScdException {

        if(id == null || id.isBlank()){
            throw new ScdException("A required field is missing: ID ");
        }

        if(name == null || name.isBlank()){
            throw new ScdException("A required field is missing:  name");
        }

        if(targets.stream().anyMatch(
            target -> target == null || StringUtils.isBlank(target.iedName()) ||
            StringUtils.isBlank(target.ldInst()))) {
            throw new ScdException("Control block destination IEDs are not well defined");
        }
    }

    /**
     * Check if Security is enabled in IED (Services)
     * @param iedAdapter IED adapter containing IED data
     * @throws ScdException when ControlBlock Security is not consistent
     */
    public final void validateSecurityEnabledValue(IEDAdapter iedAdapter) throws ScdException {
        TServices tServices = iedAdapter.getServices();
        validateSecurityEnabledValue(tServices);
    }

    /**
     * Get ConfRev value.
     *  ConfRev is 0 for newly created control blocks without data set reference
     * @return confRev long value
     */
    public Long getConfRev() {
        if(StringUtils.isBlank(dataSetRef)){
            return 0L;
        }
        return confRev;
    }

    /**
     * Abstract method to check validity of Security Enable state for Control blocks
     * @param tServices Service object
     * @throws ScdException when ControlBlock Security is not consistent
     */
    protected abstract void validateSecurityEnabledValue(TServices tServices) throws ScdException;

    /**
     * Get Control block settings from Service
     * @param tServices Service object
     * @return ServiceSettingsNoDynEnum enum value
     */
    public TServiceSettingsNoDynEnum getControlBlockServiceSetting(TServices tServices){
        if(tServices == null) {
            return TServiceSettingsNoDynEnum.FIX;
        }
        return switch (getControlBlockEnum()){
            case GSE -> (tServices.getGSESettings() != null) ? tServices.getGSESettings().getCbName() : TServiceSettingsNoDynEnum.FIX;
            case SAMPLED_VALUE -> (tServices.getSMVSettings() != null) ? tServices.getSMVSettings().getCbName() : TServiceSettingsNoDynEnum.FIX;
            case REPORT -> (tServices.getReportSettings() != null) ? tServices.getReportSettings().getCbName() : TServiceSettingsNoDynEnum.FIX;
            default -> TServiceSettingsNoDynEnum.FIX;
        };
    }

    /**
     * Add the ControlBlock to an LN or LN0 element
     * @param tAnyLN tLN or tLNO element
     * @return the added TControl
     */
    public abstract TControl addToLN(TAnyLN tAnyLN);

    /**
     * Copy a TProtocol object
     * @param tProtocol input instance
     * @return new instance of tProtocol with the same fields as input
     */
    protected static TProtocol copyProtocol(TProtocol tProtocol){
        if (tProtocol == null) {
            return null;
        }
        TProtocol newTProtocol = new TProtocol();
        newTProtocol.setValue(tProtocol.getValue());
        newTProtocol.setMustUnderstand(tProtocol.isMustUnderstand());
        return newTProtocol;
    }

}
