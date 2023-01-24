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
import org.lfenergy.compas.sct.commons.scl.dtt.DATypeAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.DOTypeAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A representation of the model object <em><b>ExtRef Binding Information</b></em>.
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ExtRefBindingInfo#getIedName <em>Ied Name</em>}</li>
 *   <li>{@link ExtRefBindingInfo#getLdInst <em>Ld Inst</em>}</li>
 *   <li>{@link ExtRefBindingInfo#getLnClass <em>Ln Class</em>}</li>
 *   <li>{@link ExtRefBindingInfo#getLnInst <em>Ln Inst</em>}</li>
 *   <li>{@link ExtRefBindingInfo#getPrefix <em>Prefix</em>}</li>
 *   <li>{@link ExtRefBindingInfo#getLnType <em>Ln Type</em>}</li>
 *   <li>{@link ExtRefBindingInfo#getDaName <em>Refers To {@link org.lfenergy.compas.sct.commons.dto.DaTypeName}</em>}</li>
 *   <li>{@link ExtRefBindingInfo#getDoName <em>Refers To {@link org.lfenergy.compas.sct.commons.dto.DoTypeName}</em>}</li>
 *   <li>{@link ExtRefBindingInfo#getServiceType <em>Service Type</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.scl2007b4.model.TExtRef
 */
@Getter
@Setter
@NoArgsConstructor
public class ExtRefBindingInfo implements Comparable<ExtRefBindingInfo> {

    public static final Comparator<ExtRefBindingInfo> EXT_REF_BINDING_INFO_COMPARATOR = Comparator.comparing(ExtRefBindingInfo::getIedName)
            .thenComparing(ExtRefBindingInfo::getLdInst)
            .thenComparing(ExtRefBindingInfo::getLnClass)
            .thenComparing(ExtRefBindingInfo::getLnInst);
    private String iedName;
    private String ldInst;
    private String prefix;
    private String lnClass;
    private String lnInst;
    private String lnType;
    private DoTypeName doName;
    private DaTypeName daName;
    private TServiceType serviceType;

    /**
     * Constructor
     *
     * @param tExtRef input
     */
    public ExtRefBindingInfo(TExtRef tExtRef) {
        iedName = tExtRef.getIedName();
        ldInst = tExtRef.getLdInst();
        prefix = tExtRef.getPrefix();
        if (!tExtRef.getLnClass().isEmpty()) {
            this.lnClass = tExtRef.getLnClass().get(0);
        }
        lnInst = tExtRef.getLnInst();
        if (tExtRef.getDoName() != null) {
            doName = new DoTypeName(tExtRef.getDoName());
        }
        if (tExtRef.getDaName() != null) {
            daName = new DaTypeName(tExtRef.getDaName());
        }
        if (tExtRef.getServiceType() != null) {
            serviceType = tExtRef.getServiceType();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        ExtRefBindingInfo that = (ExtRefBindingInfo) o;
        return Objects.equals(iedName, that.iedName) &&
                Objects.equals(ldInst, that.ldInst) &&
                Objects.equals(prefix, that.prefix) &&
                Objects.equals(lnClass, that.lnClass) &&
                Objects.equals(lnInst, that.lnInst) &&
                Objects.equals(doName, that.doName) &&
                Objects.equals(daName, that.daName) &&
                serviceType == that.serviceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(iedName, ldInst, prefix, lnClass, lnInst, doName, daName, serviceType);
    }

    /**
     * Check validity of ExtRef binding information
     *
     * @return validity state
     */
    public boolean isValid() {
        final String validationRegex = DaTypeName.VALIDATION_REGEX;
        String doRef = doName == null ? "" : doName.toString();

        Pattern pattern = Pattern.compile(validationRegex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(doRef);
        matcher.find();

        if (!StringUtils.isBlank(doRef) && doRef.length() != matcher.end()) {
            return false;
        }
        return !StringUtils.isBlank(iedName) &&
                !StringUtils.isBlank(ldInst) &&
                !StringUtils.isBlank(lnClass) &&
                (TLLN0Enum.LLN_0.value().equals(lnClass) || !StringUtils.isBlank(lnInst));
    }

    /**
     * Check dependency between ExtRef binding information and ExtRef
     *
     * @param tExtRef object containing ExtRef data's'
     * @return dependency state
     */
    public boolean isWrappedIn(TExtRef tExtRef) {
        return Objects.equals(iedName, tExtRef.getIedName()) &&
                Objects.equals(ldInst, tExtRef.getLdInst()) &&
                Objects.equals(prefix, tExtRef.getPrefix()) &&
                Objects.equals(lnInst, tExtRef.getLnInst()) &&
                Utils.lnClassEquals(tExtRef.getLnClass(), lnClass) &&
                (tExtRef.getServiceType() == null || Objects.equals(serviceType, tExtRef.getServiceType()));
    }

    /**
     * Check nullability of ExtRef binding information
     *
     * @return nullability state
     */
    public boolean isNull() {
        return iedName == null &&
                ldInst == null &&
                prefix == null &&
                lnInst == null &&
                lnClass == null &&
                doName == null &&
                daName == null &&
                serviceType == null;
    }

    /**
     * Define the way to compare 2 objects ExtRefBindingInfo in order to allow sorting items
     *
     * @param o the object to be compared.
     * @return the comparaison's result (a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second)
     */
    public int compareTo(ExtRefBindingInfo o) {
        return EXT_REF_BINDING_INFO_COMPARATOR.compare(this, o);
    }

    @Override
    public String toString() {
        String sType = serviceType != null ? serviceType.value() : null;
        return "ExtRefBindingInfo{" +
                "iedName='" + iedName + '\'' +
                ", ldInst='" + ldInst + '\'' +
                ", prefix='" + prefix + '\'' +
                ", lnClass='" + lnClass + '\'' +
                ", lnInst='" + lnInst + '\'' +
                ", lnType='" + lnType + '\'' +
                ", serviceType=" +  sType +
                '}';

    }

    /**
     * updates bindingInfo from given data
     * @param signalInfo contains DAType info to set
     * @param doTypeInfo contains DOType info leading to DA
     * @throws ScdException thrown when inconsistency between DOType and DAType
     */
    public void updateDAInfos(ExtRefSignalInfo signalInfo, DataTypeTemplateAdapter.DOTypeInfo doTypeInfo) throws ScdException {
        DaTypeName daTypeName = new DaTypeName(signalInfo.getPDA());
        String extDaName = daTypeName.getName();
        DOTypeAdapter lastDoTypeAdapter = doTypeInfo.getDoTypeAdapter();
        TDA da = lastDoTypeAdapter.getDAByName(extDaName).orElseThrow(() ->
                new ScdException(String.format("%s: Unknown DA (%s) in DOType (%s) ", doTypeInfo.getDoTypeName(), extDaName, doTypeInfo.getDoTypeId())));
        if (da.getBType() != TPredefinedBasicTypeEnum.STRUCT && !daTypeName.getStructNames().isEmpty()
                || da.getBType() == TPredefinedBasicTypeEnum.STRUCT && daTypeName.getStructNames().isEmpty()) {
            throw new ScdException(String.format("Invalid ExtRef signal: no coherence between pDO(%s) and pDA(%s)",
                    signalInfo.getPDO(), signalInfo.getPDA()));
        }
        String daTypeId = da.getType();
        DATypeAdapter daTypeAdapter = lastDoTypeAdapter.getParentAdapter().getDATypeAdapterById(daTypeId).orElseThrow(() ->
                new IllegalArgumentException(String.format("%s: Unknown DA (%s), or no reference to its type", daTypeName, extDaName)));
        daTypeAdapter.check(daTypeName);
        daTypeName.setFc(da.getFc());
        this.daName = daTypeName;
    }
}
