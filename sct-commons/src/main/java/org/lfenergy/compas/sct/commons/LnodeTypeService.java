// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.TDataTypeTemplates;
import org.lfenergy.compas.scl2007b4.model.TLNodeType;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class LnodeTypeService {

    private DoTypeService doTypeService = new DoTypeService();

    public Stream<TLNodeType> getLnodeTypes(TDataTypeTemplates tDataTypeTemplates) {
        return tDataTypeTemplates.getLNodeType().stream();
    }

    public Stream<TLNodeType> getFilteredLnodeTypes(TDataTypeTemplates tDataTypeTemplates, Predicate<TLNodeType> tlNodeTypePredicate) {
        return getLnodeTypes(tDataTypeTemplates).filter(tlNodeTypePredicate);
    }

    public Optional<TLNodeType> findLnodeType(TDataTypeTemplates tDataTypeTemplates, Predicate<TLNodeType> tlNodeTypePredicate) {
        return getFilteredLnodeTypes(tDataTypeTemplates, tlNodeTypePredicate).findFirst();
    }

    public Stream<DataAttributeRef> getDataAttributes(TDataTypeTemplates dtt, TLNodeType tlNodeType, DataAttributeRef dataRef)  {
        return tlNodeType.getDO().stream()
                .flatMap(tdo -> {
                    dataRef.setLnType(tlNodeType.getId());
                    if(tlNodeType.isSetLnClass() && !tlNodeType.getLnClass().isEmpty()) dataRef.setLnClass(tlNodeType.getLnClass().get(0));
                    dataRef.getDoName().setName(tdo.getName());
                    return doTypeService.findDoType(dtt, tdoType -> tdoType.getId().equals(tdo.getType()))
                            .stream().flatMap(tdoType -> {
                                dataRef.getDoName().setCdc(tdoType.getCdc());
                                return doTypeService.getDataAttributes(dtt, tdoType, dataRef).stream();
                            });
                });
    }


    public Stream<DataAttributeRef> getFilteredDataAttributes(TDataTypeTemplates dtt, DataAttributeRef dataRef)  {
       return getFilteredLnodeTypes(dtt, tlNodeType -> tlNodeType.getId().equals(dataRef.getLnType()))
                .flatMap(tlNodeType -> tlNodeType.getDO().stream()
                        .flatMap(tdo -> {
                            dataRef.getDoName().setName(tdo.getName());
                            return doTypeService.findDoType(dtt, tdoType -> tdoType.getId().equals(tdo.getType()))
                                    .stream().flatMap(tdoType -> {
                                        dataRef.getDoName().setCdc(tdoType.getCdc());
                                        return doTypeService.getDataAttributes(dtt, tdoType, dataRef).stream();
                                    });
                        }));

    }
}
