// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import lombok.Getter;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.Objects;
import java.util.Optional;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.sct.commons.scl.dtt.AbstractDataAttributeAdapter AbstractDataAttributeAdapter}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *   <ul>
 *       <li>{@link AbstractDataAttributeAdapter#getDataTypeTemplateAdapter <em>get DataTypeTemplateAdapter</em>}</li>
 *       <li>{@link AbstractDataAttributeAdapter#getDATypeAdapter <em>get DATypeAdapter</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link AbstractDataAttributeAdapter#addPrivate <em>Add <b>TPrivate </b>under this object</em>}</li>
 *    </ul>
 *   <li>Checklist functions</li>
 *    <ul>
 *       <li>{@link AbstractDataAttributeAdapter#hasSameContentAs <em>Compare Two SCL element</em>}</li>
 *       <li>{@link AbstractDataAttributeAdapter#check <em>Check structData from DaTypeName</em>}</li>
 *    </ul>
 * </ol>
 * @see org.lfenergy.compas.sct.commons.scl.SclElementAdapter
 * @see org.lfenergy.compas.scl2007b4.model.TAbstractDataAttribute
 * @see org.lfenergy.compas.sct.commons.scl.dtt.IDataTemplate
 * @see org.lfenergy.compas.sct.commons.scl.dtt.IDTTComparable
 */
@Getter
public abstract class AbstractDataAttributeAdapter<P extends SclElementAdapter,T extends TAbstractDataAttribute>
        extends SclElementAdapter<P,T>
        implements IDataTemplate, IDTTComparable<T>{

    protected final boolean tail;

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currentElem Current reference
     */
    protected AbstractDataAttributeAdapter(P parentAdapter, T currentElem) {
        super(parentAdapter, currentElem);
        tail = getBType() != TPredefinedBasicTypeEnum.STRUCT;
    }

    /**
     * Gets Type
     * @return Type
     */
    public String getType(){
        return currentElem.getType();
    }

    /**
     * Gets Basic Type
     * @return Basic Type enum value
     */
    public TPredefinedBasicTypeEnum getBType(){
        return currentElem.getBType();
    }

    /**
     * Gets Name
     * @return Name
     */
    public String getName(){
        return currentElem.getName();
    }

    /**
     * Gets DATypeAdapter
     * @return DATypeAdapter object
     */
    public Optional<DATypeAdapter> getDATypeAdapter() {
        if(isTail()){
            return Optional.empty();
        }
        return getDataTypeTemplateAdapter().getDATypeAdapterById(getType());
    }

    /**
     * Cheeks if DataAttributes have the same contents
     * @param data input
     * @return Equality state
     */
    public boolean hasSameContentAs(T data) {
        if(!Objects.equals(getName(),data.getName())
                || !Objects.equals(getBType(),data.getBType())
                || !Objects.equals(getType(),data.getType())
                || !Objects.equals(currentElem.getSAddr(), data.getSAddr())
                || !Objects.equals(currentElem.getValKind(), data.getValKind())
                || currentElem.isValImport() != data.isValImport()){
            return false;
        }
        if(data.getClass().equals(TDA.class) ) {
            TDA tda = (TDA)data;
            TDA thisTda = (TDA)currentElem;
            if(!Objects.equals(thisTda.getFc(),tda.getFc())
                    || thisTda.isDchg() != tda.isDchg()
                    || thisTda.isDupd() != tda.isDupd()
                    || thisTda.isQchg() != tda.isQchg()){
                return false;
            }
        }
        if (!Utils.equalsOrNotSet(currentElem, data, TAbstractDataAttribute::isSetCount, TAbstractDataAttribute::getCount)){
            return false;
        }


        if((getBType() == TPredefinedBasicTypeEnum.ENUM ||
                getBType() == TPredefinedBasicTypeEnum.STRUCT)
                && !Objects.equals(getType(),data.getType())) {
            return false;
        }

        for(TVal prdVal : data.getVal()){
            if(currentElem.isSetVal() && currentElem.getVal().stream()
                .noneMatch(rcvVal -> rcvVal.getValue().equals(prdVal.getValue()) &&
                        Utils.equalsOrNotSet(rcvVal, prdVal, TVal::isSetSGroup, TVal::getSGroup))) {
                return false;
            }
        }

        if(data.getClass().equals(TDA.class)) {
            for (TProtNs prdProtNs : ((TDA)data).getProtNs()) {
                boolean hasSameVal = ((TDA)data).getProtNs().stream()
                        .anyMatch(rcvProtNs -> rcvProtNs.getValue().equals(prdProtNs.getValue()) &&
                                Objects.equals(rcvProtNs.getType(), prdProtNs.getType()));
                if (!hasSameVal) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Gets DataTypeTemplateAdapter
     * @return DataTypeTemplateAdapter object
     */
    @Override
    public DataTypeTemplateAdapter getDataTypeTemplateAdapter() {
        return ((IDataTemplate)parentAdapter).getDataTypeTemplateAdapter();
    }

    /**
     * Updates DA Type Name
     * @param daTypeName DA Type Name to update
     * @throws ScdException
     */
    public void check(DaTypeName daTypeName) throws ScdException {

        if(getBType() == TPredefinedBasicTypeEnum.ENUM){
            EnumTypeAdapter enumTypeAdapter = getDataTypeTemplateAdapter().getEnumTypeAdapterById(getType())
                    .orElseThrow(
                            () -> new ScdException("")
                    );
            String val = daTypeName.getDaiValues().values().stream().findFirst().orElse(null);
            if(val != null && !enumTypeAdapter.hasValue(val)){
                throw new ScdException(
                        String.format(
                                "Unknown EnumVal(%s) in EnumType(%s) referenced by BDA(%s) in '%s'",
                                val,getType(), getName(), daTypeName
                        )
                );
            }
            daTypeName.setType(getType());
        }
        daTypeName.setBType(getBType());
        if(daTypeName.getDaiValues().isEmpty()) {
            daTypeName.addDaiValues(currentElem.getVal());
        }
        daTypeName.setValImport(currentElem.isValImport());
    }

    /**
     * Checks valImport state
     * @return boolean value of valImport
     */
    public boolean isValImport() {
        return currentElem.isValImport();
    }
}
