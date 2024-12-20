// SPDX-FileCopyrightText: 2023 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.domain.DataAttribute;
import org.lfenergy.compas.sct.commons.domain.DoLinkedToDa;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DoTypeService {

    final DaTypeService daTypeService = new DaTypeService();
    final SDOOrDAService sdoOrDAService = new SDOOrDAService();
    final BDAService bdaService = new BDAService();

    public Stream<TDOType> getDoTypes(TDataTypeTemplates tDataTypeTemplates) {
        return tDataTypeTemplates.getDOType().stream();
    }

    public Stream<TDOType> getFilteredDoTypes(TDataTypeTemplates tDataTypeTemplates, Predicate<TDOType> tdoTypePredicate) {
        return getDoTypes(tDataTypeTemplates).filter(tdoTypePredicate);
    }

    public Optional<TDOType> findDoType(TDataTypeTemplates tDataTypeTemplates, Predicate<TDOType> tdoTypePredicate) {
        return getFilteredDoTypes(tDataTypeTemplates, tdoTypePredicate).findFirst();
    }

    public List<DoLinkedToDa> getAllSDOLinkedToDa(TDataTypeTemplates dtt, TDOType tdoType, DoLinkedToDa doLinkedToDaTemplate) {
        List<DoLinkedToDa> result = new ArrayList<>();
        // DA -> BDA -> BDA..
        sdoOrDAService.getDAs(tdoType).forEach(tda -> {
            DoLinkedToDa doLinkedToDa = DoLinkedToDa.copyFrom(doLinkedToDaTemplate);
            doLinkedToDa.dataAttribute().setDaName(tda.getName());
            if (tda.isSetFc()) {
                doLinkedToDa.dataAttribute().setFc(tda.getFc());
            }

            // STRUCT type (BType=STRUCT) refer to BDA, otherwise it is DA
            if (TPredefinedBasicTypeEnum.STRUCT.equals(tda.getBType())) {
                daTypeService.findDaType(dtt, tda.getType())
                        .ifPresent(nextDaType -> result.addAll(getDaLinkedToBDA(dtt, nextDaType, doLinkedToDa).toList()));
            } else {
                DataAttribute dataAttribute = updateDataAttributeFromDaOrBda(tda, doLinkedToDa.dataAttribute());
                result.add(new DoLinkedToDa(doLinkedToDa.dataObject(), dataAttribute));
            }
        });
        // SDO -> SDO -> SDO..
        sdoOrDAService.getSDOs(tdoType)
                .forEach(tsdo -> {
                    if (tsdo.isSetType()) {
                        findDoType(dtt, tdoType1 -> tdoType1.getId().equals(tsdo.getType()))
                                .ifPresent(nextDoType -> {
                                    DoLinkedToDa newDoLinkedToDa = DoLinkedToDa.copyFrom(doLinkedToDaTemplate);
                                    newDoLinkedToDa.dataObject().getSdoNames().add(tsdo.getName());
                                    if (nextDoType.isSetCdc()) {
                                        newDoLinkedToDa.dataObject().setCdc(nextDoType.getCdc());
                                    }
                                    result.addAll(getAllSDOLinkedToDa(dtt, nextDoType, newDoLinkedToDa));
                                });
                    }
                });
        return result;
    }

    private Stream<DoLinkedToDa> getDaLinkedToBDA(TDataTypeTemplates dtt, TDAType tdaType1, DoLinkedToDa doLinkedToDaTemplate) {
        // BDA -> BDA -> BDA..
        return bdaService.getBDAs(tdaType1)
                .flatMap(tbda -> {
                    DoLinkedToDa newDoLinkedToDa = DoLinkedToDa.copyFrom(doLinkedToDaTemplate);
                    newDoLinkedToDa.dataAttribute().getBdaNames().add(tbda.getName());

                    // STRUCT type (BType=STRUCT) refer to complex BDA object, otherwise it is kind of DA object
                    if (TPredefinedBasicTypeEnum.STRUCT.equals(tbda.getBType())) {
                        return daTypeService.findDaType(dtt, tbda.getType())
                                .stream().flatMap(nextDaType -> getDaLinkedToBDA(dtt, nextDaType, newDoLinkedToDa));
                    } else {
                        DataAttribute dataAttribute = updateDataAttributeFromDaOrBda(tbda, newDoLinkedToDa.dataAttribute());
                        return Stream.of(new DoLinkedToDa(newDoLinkedToDa.dataObject(), dataAttribute));
                    }
                });
    }

    private DataAttribute updateDataAttributeFromDaOrBda(TAbstractDataAttribute daOrBda, DataAttribute dataAttribute) {
        if (daOrBda.isSetType()) dataAttribute.setType(daOrBda.getType());
        if (daOrBda.isSetBType()) dataAttribute.setBType(daOrBda.getBType());
        if (daOrBda.isSetValImport()) dataAttribute.setValImport(daOrBda.isValImport());
        if (daOrBda.isSetVal()) dataAttribute.addDaVal(daOrBda.getVal());
        return dataAttribute;
    }

}
