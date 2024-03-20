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
    private final DoService doService = new DoService();

    public Stream<TLNodeType> getLnodeTypes(TDataTypeTemplates tDataTypeTemplates) {
        return tDataTypeTemplates.getLNodeType().stream();
    }

    public Stream<TLNodeType> getFilteredLnodeTypes(TDataTypeTemplates tDataTypeTemplates, Predicate<TLNodeType> tlNodeTypePredicate) {
        return getLnodeTypes(tDataTypeTemplates).filter(tlNodeTypePredicate);
    }

    public Optional<TLNodeType> findLnodeType(TDataTypeTemplates tDataTypeTemplates, Predicate<TLNodeType> tlNodeTypePredicate) {
        return getFilteredLnodeTypes(tDataTypeTemplates, tlNodeTypePredicate).findFirst();
    }

    public Stream<DataAttributeRef> getAllDOAndDA(TDataTypeTemplates dtt, String tlNodeTypeId)  {
        DataAttributeRef dataAttributeRef = new  DataAttributeRef();
        dataAttributeRef.setLnType(tlNodeTypeId);
        return findLnodeType(dtt, tlNodeType -> tlNodeType.getId().equals(tlNodeTypeId))
                .map(tlNodeType -> {
                   if(tlNodeType.isSetLnClass() && !tlNodeType.getLnClass().isEmpty()) {
                       dataAttributeRef.setLnClass(tlNodeType.getLnClass().get(0));
                   }
                  return tlNodeType.getDO()
                          .stream()
                          .map(tdo -> {
                              dataAttributeRef.getDoName().setName(tdo.getName());
                              return doTypeService.findDoType(dtt, tdoType -> tdoType.getId().equals(tdo.getType()));
                          })
                          .filter(Optional::isPresent)
                          .map(Optional::orElseThrow)
                          .flatMap(tdoType -> {
                              dataAttributeRef.getDoName().setCdc(tdoType.getCdc());
                              return doTypeService.getAllSDOAndDA(dtt, tdoType, dataAttributeRef).stream();
                          });
                }).orElseThrow();
    }

    public Stream<DataAttributeRef> getFilteredDOAndDA(TDataTypeTemplates dtt, DataAttributeRef filter)  {
       return findLnodeType(dtt, tlNodeType -> tlNodeType.getId().equals(filter.getLnType()))
               .stream()
                .flatMap(tlNodeType -> doService.getFilteredDos(tlNodeType, tdo -> !filter.isDoNameDefined()
                                || filter.getDoName().getName().equals(tdo.getName()))
                        .flatMap(tdo -> {
                            DataAttributeRef dataAttributeRef = new DataAttributeRef();
                            dataAttributeRef.setPrefix(filter.getPrefix());
                            dataAttributeRef.setLnClass(filter.getLnClass());
                            dataAttributeRef.setLnInst(filter.getLnInst());
                            dataAttributeRef.setLnType(tlNodeType.getId());
                            dataAttributeRef.getDoName().setName(tdo.getName());
                            return doTypeService.findDoType(dtt, tdoType -> tdoType.getId().equals(tdo.getType()))
                                    .stream()
                                    .flatMap(tdoType -> {
                                        dataAttributeRef.getDoName().setCdc(tdoType.getCdc());
                                        return doTypeService.getAllSDOAndDA(dtt, tdoType, dataAttributeRef)
                                                .stream()
                                                .filter(dataAttributeRefToFilter -> dataAttributeRefToFilter.getDoRef().startsWith(filter.getDoRef())
                                                        && dataAttributeRefToFilter.getDaRef().startsWith(filter.getDaRef()));
                                    });
                        }));
    }

}
