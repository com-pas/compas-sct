// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.scl.ln.AbstractLNAdapter;
import org.lfenergy.compas.sct.commons.util.SclConstructorHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.lfenergy.compas.sct.commons.util.CommonConstants.MOD_DO_NAME;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.STVAL_DA_NAME;


/**
 * A representation of the model object <em><b>DataAttributeRef</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link DataAttributeRef#lnInst <em>LN Inst</em>}</li>
 *   <li>{@link DataAttributeRef#lnClass <em>LN Class</em>}</li>
 *   <li>{@link DataAttributeRef#lnType <em>LN Type</em>}</li>
 *   <li>{@link DataAttributeRef#prefix <em>Prefix</em>}</li>
 *   <li>{@link org.lfenergy.compas.sct.commons.dto.DoTypeName <em>Refers To DoTypeName</em>}</li>
 *   <li>{@link org.lfenergy.compas.sct.commons.dto.DaTypeName <em>Refers To DaTypeName</em>}</li>
 * </ul>
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class DataAttributeRef {

    private String prefix;
    private String lnType;
    private String lnClass;
    private String lnInst;
    @Builder.Default
    @NonNull
    private DoTypeName doName = new DoTypeName("");
    @Builder.Default
    @NonNull
    private DaTypeName daName = new DaTypeName("");

    /**
     * Constructor
     */

    public DataAttributeRef(AbstractLNAdapter<?> lnAdapter, DoTypeName doName, DaTypeName daName) {
        this.lnClass = lnAdapter.getLNClass();
        this.lnInst = lnAdapter.getLNInst();
        this.prefix = lnAdapter.getPrefix();
        this.lnType = lnAdapter.getLnType();
        this.doName = doName;
        this.daName = daName;
    }

    public DataAttributeRef(TFCDA fcda) {
        this.lnClass = fcda.isSetLnClass() ? fcda.getLnClass().get(0) : null;
        this.lnInst = fcda.getLnInst();
        this.prefix = fcda.getPrefix();
        this.lnType = null;
        this.doName = new DoTypeName(StringUtils.trimToEmpty(fcda.getDoName()));
        this.daName = new DaTypeName(StringUtils.trimToEmpty(fcda.getDaName()));
        if (daName.isDefined()) this.setFc(fcda.getFc());
    }

    /**
     * Copies summarized DataTypeTemplate information to another one
     *
     * @param source input
     * @return Updated DataAttributeRef object
     */
    public static DataAttributeRef copyFrom(DataAttributeRef source) {
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.prefix = source.prefix;
        dataAttributeRef.lnClass = source.lnClass;
        dataAttributeRef.lnInst = source.lnInst;
        dataAttributeRef.lnType = source.lnType;
        dataAttributeRef.doName = DoTypeName.from(source.getDoName());
        dataAttributeRef.daName = DaTypeName.from(source.getDaName());

        return dataAttributeRef;
    }

    public static DataAttributeRef updateDataRef(TAnyLN anyLN, DataAttributeRef dataAttributeRef) {
        DataAttributeRef filter = copyFrom(dataAttributeRef);
        if(anyLN.isSetLnType()) {
            filter.setLnType(anyLN.getLnType());
        }
        switch (anyLN){
            case TLN0 tln0 -> {
                if(tln0.isSetInst()) filter.setLnInst(tln0.getInst());
                if(tln0.isSetLnClass()) filter.setLnClass(tln0.getLnClass().get(0));
                filter.setPrefix(StringUtils.EMPTY);
            }
            case TLN tln -> {
                if(tln.isSetInst()) filter.setLnInst(tln.getInst());
                if(tln.isSetLnClass()) filter.setLnClass(tln.getLnClass().get(0));
                if(tln.isSetPrefix()) {
                    filter.setPrefix(tln.getPrefix());
                } else  {
                    filter.setPrefix(StringUtils.EMPTY);
                }
            }
            default -> throw new RuntimeException("not possible");
        }
        return filter;
    }

    /**
     * Checks if DA/DO is updatable
     *
     * @return true if updatable, false otherwise
     */
    public boolean isUpdatable() {
        return isDOModDAstVal() || daName.isDefined() && daName.isUpdatable();
    }

    /**
     * Checks if DO is Mod and DA is stVal
     *
     * @return true if DO is "Mod" and DA is "stVal", false otherwise
     */
    private boolean isDOModDAstVal() {
        return doName.getName().equals(MOD_DO_NAME) && daName.getName().equals(STVAL_DA_NAME);
    }

    @JsonIgnore
    public String getObjRef(String iedName, String ldInst) {
        //LDName
        return iedName + ldInst + "/" + getLNRef();
    }

    /**
     * Gets LNode reference information
     *
     * @return String LNode information concatenated
     */
    @JsonIgnore
    public String getLNRef() {
        StringBuilder stringBuilder = new StringBuilder();
        if (TLLN0Enum.LLN_0.value().equals(lnClass)) {
            stringBuilder.append(TLLN0Enum.LLN_0.value());
        } else {
            stringBuilder.append(prefix)
                    .append(lnClass)
                    .append(lnInst);
        }
        stringBuilder.append('.')
                .append(getDoRef())
                .append('.')
                .append(getDaRef());

        return stringBuilder.toString();
    }

    /**
     * Gets Data Attributes value
     *
     * @return String Data Attributes reference by concatenated DO reference and DA reference
     */
    @JsonIgnore
    public String getDataAttributes() {
        return getDoRef() + "." + getDaRef();
    }

    /**
     * Gets DO reference value
     *
     * @return String DO (Data Object) reference value
     */
    @JsonIgnore
    public String getDoRef() {
        return isDoNameDefined() ? doName.toString() : "";
    }

    /**
     * Gets DA (Data Attribut) reference value
     *
     * @return DA reference value
     */
    @JsonIgnore
    public String getDaRef() {
        return isDaNameDefined() ? daName.toString() : "";
    }

    /**
     * Gets FC (Functional Constraints) reference value
     *
     * @return
     */
    @JsonIgnore
    public TFCEnum getFc() {
        return daName.isDefined() ? daName.getFc() : null;
    }

    /**
     * Sets DA/DO FC's value
     *
     * @param fc input
     */
    @JsonIgnore
    public void setFc(TFCEnum fc) {
        if (isDaNameDefined()) {
            daName.setFc(fc);
        } else {
            throw new IllegalArgumentException("Cannot set functional constrain for undefined DA");
        }
    }

    /**
     * Gets DA/DO CDC's value
     *
     * @return CDC enum value
     */
    @JsonIgnore
    public TPredefinedCDCEnum getCdc() {
        return daName.isDefined() ? doName.getCdc() : null;
    }

    /**
     * Sets DA/DO CDC's value
     *
     * @param cdc input
     */
    @JsonIgnore
    public void setCdc(TPredefinedCDCEnum cdc) {
        if (isDoNameDefined()) {
            doName.setCdc(cdc);
        } else {
            throw new IllegalArgumentException("Cannot set CDC for undefined DOType");
        }
    }

    /**
     * Gets SDO names'
     *
     * @return List of SDO name
     */
    @JsonIgnore
    public List<String> getSdoNames() {
        if (!isDoNameDefined()) return new ArrayList<>();
        return List.of(doName.getStructNames().toArray(new String[0]));
    }

    /**
     * GEts BDA names'
     *
     * @return List of BDA name
     */
    @JsonIgnore
    public List<String> getBdaNames() {
        if (!isDaNameDefined()) return new ArrayList<>();
        return List.of(daName.getStructNames().toArray(new String[0]));
    }

    /**
     * Adds DO Structure name
     *
     * @param structName input
     */
    public void addDoStructName(String structName) {
        if (isDoNameDefined()) {
            doName.addStructName(structName);
        } else {
            throw new IllegalArgumentException("DO name must be defined before adding DO StructName");
        }
    }

    /**
     * Adds DA Structure name
     *
     * @param structName input
     */
    public void addDaStructName(String structName) {
        if (isDaNameDefined()) {
            daName.addStructName(structName);
        } else {
            throw new IllegalArgumentException("DA name must be defined before adding DA StructName");
        }
    }

    /**
     * Checks if DO name is defined
     *
     * @return definition state
     */
    public boolean isDoNameDefined() {
        return doName != null && doName.isDefined();
    }

    /**
     * Checks if DA name is defined
     *
     * @return definition state
     */
    public boolean isDaNameDefined() {
        return daName != null && daName.isDefined();
    }

    /**
     * Gets DA Basic Type value
     *
     * @return Basic Type enum value
     */
    @JsonIgnore
    public TPredefinedBasicTypeEnum getBType() {
        return daName != null ? daName.getBType() : null;
    }

    /**
     * Sets DA type
     *
     * @param type input
     */
    @JsonIgnore
    public void setType(String type) {
        if (isDaNameDefined()) {
            daName.setType(type);
        } else {
            throw new IllegalArgumentException("Cannot define type for undefined BDA");
        }
    }

    /**
     * Gets DA type
     *
     * @return string DA type
     */
    @JsonIgnore
    public String getType() {
        return daName != null ? daName.getType() : null;
    }

    /**
     * Sets DA Basic Type value
     *
     * @param bType input
     */
    @JsonIgnore
    public void setBType(String bType) {
        if (isDaNameDefined()) {
            daName.setBType(TPredefinedBasicTypeEnum.fromValue(bType));
        } else {
            throw new IllegalArgumentException("Cannot define Basic type for undefined DA or BDA");
        }
    }

    /**
     * Set DO name
     *
     * @param doName input
     */
    public void setDoName(DoTypeName doName) {
        if (doName != null) {
            this.doName = DoTypeName.from(doName);
        }
    }

    /**
     * Sets DA name
     *
     * @param daName input
     */
    public void setDaName(DaTypeName daName) {
        if (daName != null) {
            this.daName = DaTypeName.from(daName);
        }
    }

    /**
     * Adds DAI values to DA
     *
     * @param values input
     */
    @JsonIgnore
    public void setDaiValues(List<TVal> values) {
        if (isDaNameDefined()) {
            daName.addDaiValues(values);
        }
    }

    /**
     * Set DA ValImport value
     *
     * @param valImport input
     */
    @JsonIgnore
    public void setValImport(boolean valImport) {
        if (isDaNameDefined()) {
            daName.setValImport(valImport);
        }
    }

    /**
     * Checks ValImport value
     *
     * @return ValImport value
     */
    public boolean isValImport() {
        return daName.isValImport();
    }

    /**
     * Set Val of DA
     *
     * @param daiValue daiValue to set
     * @return this
     */
    public DataAttributeRef setVal(String daiValue) {
        this.setDaiValues(List.of(SclConstructorHelper.newVal(daiValue)));
        return this;
    }

    /**
     * Retrieve value of the DAI, if present. If multiples values are found,
     * the value with the lowest index is returned.
     *
     * @return Return the DAI value with the lowest key from getDaName().getdaiValues() map,
     * or empty Optional if this DAI has no value.
     */
    public Optional<String> findFirstValue() {
        return getDaName().getDaiValues().keySet().stream()
                .min(Long::compareTo)
                .map(key -> getDaName().getDaiValues().get(key));
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof DataAttributeRef)) return false;
        final DataAttributeRef other = (DataAttributeRef) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$prefix = this.getPrefix();
        final Object other$prefix = other.getPrefix();
        if (this$prefix == null ? other$prefix != null : !this$prefix.equals(other$prefix)) return false;
        final Object this$lnType = this.getLnType();
        final Object other$lnType = other.getLnType();
        if (this$lnType == null ? other$lnType != null : !this$lnType.equals(other$lnType)) return false;
        final Object this$lnClass = this.getLnClass();
        final Object other$lnClass = other.getLnClass();
        if (this$lnClass == null ? other$lnClass != null : !this$lnClass.equals(other$lnClass)) return false;
        final Object this$lnInst = this.getLnInst();
        final Object other$lnInst = other.getLnInst();
        if (this$lnInst == null ? other$lnInst != null : !this$lnInst.equals(other$lnInst)) return false;
        final Object this$doName = this.getDoName();
        final Object other$doName = other.getDoName();
        if (this$doName == null ? other$doName != null : !this$doName.equals(other$doName)) return false;
        final Object this$daName = this.getDaName();
        final Object other$daName = other.getDaName();
        if (this$daName == null ? other$daName != null : !this$daName.equals(other$daName)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof DataAttributeRef;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $prefix = this.getPrefix();
        result = result * PRIME + ($prefix == null ? 43 : $prefix.hashCode());
        final Object $lnType = this.getLnType();
        result = result * PRIME + ($lnType == null ? 43 : $lnType.hashCode());
        final Object $lnClass = this.getLnClass();
        result = result * PRIME + ($lnClass == null ? 43 : $lnClass.hashCode());
        final Object $lnInst = this.getLnInst();
        result = result * PRIME + ($lnInst == null ? 43 : $lnInst.hashCode());
        final Object $doName = this.getDoName();
        result = result * PRIME + ($doName == null ? 43 : $doName.hashCode());
        final Object $daName = this.getDaName();
        result = result * PRIME + ($daName == null ? 43 : $daName.hashCode());
        return result;
    }
}
