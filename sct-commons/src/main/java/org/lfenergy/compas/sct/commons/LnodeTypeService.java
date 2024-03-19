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

    private final DoTypeService doTypeService = new DoTypeService();

    public Stream<TLNodeType> getLnodeTypes(TDataTypeTemplates tDataTypeTemplates) {
        return tDataTypeTemplates.getLNodeType().stream();
    }

    public Stream<TLNodeType> getFilteredLnodeTypes(TDataTypeTemplates tDataTypeTemplates, Predicate<TLNodeType> tlNodeTypePredicate) {
        return getLnodeTypes(tDataTypeTemplates).filter(tlNodeTypePredicate);
    }

    public Optional<TLNodeType> findLnodeType(TDataTypeTemplates tDataTypeTemplates, Predicate<TLNodeType> tlNodeTypePredicate) {
        return getFilteredLnodeTypes(tDataTypeTemplates, tlNodeTypePredicate).findFirst();
    }

    public Stream<DataAttributeRef> getAllDOAndDA(TDataTypeTemplates dtt, TLNodeType tlNodeType, DataAttributeRef dataRef)  {
        dataRef.setLnType(tlNodeType.getId());
        if(tlNodeType.isSetLnClass() && !tlNodeType.getLnClass().isEmpty()) dataRef.setLnClass(tlNodeType.getLnClass().get(0));
        return tlNodeType.getDO()
                .stream()
                .map(tdo -> {
                    dataRef.getDoName().setName(tdo.getName());
                    return doTypeService.findDoType(dtt, tdoType -> tdoType.getId().equals(tdo.getType()));
                })
                .filter(Optional::isPresent)
                .map(Optional::orElseThrow)
                .flatMap(tdoType -> {
                    dataRef.getDoName().setCdc(tdoType.getCdc());
                    return doTypeService.getAllSDOAndDA(dtt, tdoType, dataRef).stream();
                });
    }


    public Stream<DataAttributeRef> getFilteredDOAndDA(TDataTypeTemplates dtt, DataAttributeRef filter)  {
       return findLnodeType(dtt, tlNodeType -> tlNodeType.getId().equals(filter.getLnType()))
               .stream()
               .flatMap(tlNodeType -> tlNodeType.getDO()
                        .stream()
                        .flatMap(tdo -> {
                            filter.getDoName().setName(tdo.getName());
                            return doTypeService.findDoType(dtt, tdoType -> tdoType.getId().equals(tdo.getType()))
                                    .stream()
                                    .flatMap(tdoType -> {
                                        filter.getDoName().setCdc(tdoType.getCdc());
                                        return doTypeService.getAllSDOAndDA(dtt, tdoType, filter).stream();
                                    });
                        }));

    }
}
