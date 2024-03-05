// SPDX-FileCopyrightText: 2023 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.api.LNEditor;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.util.ActiveStatus;

import java.util.*;
import java.util.stream.Stream;

import static org.lfenergy.compas.sct.commons.util.CommonConstants.MOD_DO_NAME;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.STVAL_DA_NAME;
import static org.lfenergy.compas.sct.commons.util.SclConstructorHelper.newVal;

@Slf4j
public class LnService implements LNEditor {

    public Stream<TAnyLN> getAnylns(TLDevice tlDevice) {
        return Stream.concat(Stream.of(tlDevice.getLN0()), tlDevice.getLN().stream());
    }

    /**
     * The Lnode status depends on the LN0 status.
     * If Ln stVAl = null => we take the LN0 status
     * If Ln stVAl = OFF => the status is OFF
     * If Ln stVAl = ON => we take the LN0 status
     *
     * @param tAnyLN the Lnode whose the status is required
     * @param ln0    the LN0
     * @return the Lnode Status
     */
    public ActiveStatus getLnStatus(TAnyLN tAnyLN, LN0 ln0) {
        Optional<ActiveStatus> ln0Status = getDaiModStval(ln0);
        return getDaiModStval(tAnyLN).filter(ActiveStatus.OFF::equals).orElseGet(() -> ln0Status.orElse(ActiveStatus.OFF));
    }

    public Optional<ActiveStatus> getDaiModStval(TAnyLN tAnyLN) {
        return tAnyLN
                .getDOI()
                .stream()
                .filter(tdoi -> MOD_DO_NAME.equals(tdoi.getName()))
                .findFirst()
                .flatMap(tdoi -> tdoi.getSDIOrDAI()
                        .stream()
                        .filter(dai -> dai.getClass().equals(TDAI.class))
                        .map(TDAI.class::cast)
                        .filter(tdai -> STVAL_DA_NAME.equals(tdai.getName()))
                        .map(TDAI::getVal)
                        .flatMap(Collection::stream)
                        .findFirst()
                        .map(TVal::getValue))
                .map(ActiveStatus::fromValue);
    }
    public Stream<TAnyLN> getActiveLns(TLDevice tlDevice) {
        LN0 ln0 = tlDevice.getLN0();
        Stream<TLN> tlnStream = tlDevice.getLN()
                .stream()
                .filter(tln -> ActiveStatus.ON.equals(getLnStatus(tln, ln0)));
        Stream<LN0> ln0Stream = Stream.of(ln0).filter(ln02 -> getDaiModStval(ln02).map(ActiveStatus.ON::equals).orElse(false));
        return Stream.concat(ln0Stream, tlnStream);
    }

    public Optional<TDAI> isDOAndDAInstancesExist(TAnyLN anyLN, DoTypeName doTypeName, DaTypeName daTypeName) {
        LinkedList<String> structNamesList = new LinkedList<>(doTypeName.getStructNames());
        structNamesList.addLast(daTypeName.getName());
        daTypeName.getStructNames().forEach(structNamesList::addLast);

        return anyLN.getDOI().stream().filter(doi -> doTypeName.getName().equals(doi.getName()))
                .findFirst()
                .flatMap(doi -> {
                    if(structNamesList.size() > 1) {
                        String firstSDIName = structNamesList.remove();
                        return doi.getSDIOrDAI().stream()
                                .filter(sdi -> sdi.getClass().equals(TSDI.class))
                                .map(TSDI.class::cast)
                                .filter(tsdi -> tsdi.getName().equals(firstSDIName))
                                .findFirst()
                                .map(intermediateSdi -> findSDIByStructName(intermediateSdi, structNamesList))
                                .stream()
                                .findFirst()
                                .flatMap(lastDsi -> {
                                    if (structNamesList.size() == 1) {
                                        return lastDsi.getSDIOrDAI().stream()
                                                .filter(dai -> dai.getClass().equals(TDAI.class))
                                                .map(TDAI.class::cast)
                                                .filter(dai -> dai.getName().equals(structNamesList.getFirst()))
                                                .findFirst();
                                    }
                                    return Optional.of(new TDAI());
                                })
                                .stream().findFirst();
                    } else if(structNamesList.size() == 1){
                        return doi.getSDIOrDAI().stream()
                                .filter(unNaming -> unNaming.getClass().equals(TDAI.class))
                                .map(TDAI.class::cast)
                                .filter(dai -> dai.getName().equals(structNamesList.getFirst()))
                                .findFirst();
                    }
                    return Optional.empty();
                });
    }

    @Override
    public void updateOrCreateDOAndDAInstances(TAnyLN tAnyLN, DataAttributeRef dataAttributeRef) {
        createDoiSdiDaiChainIfNotExists(tAnyLN, dataAttributeRef.getDoName(), dataAttributeRef.getDaName())
                .ifPresent(tdai -> {
                    Map<Long,String> daiValMap = dataAttributeRef.getDaName().getDaiValues();
                    // Here it's a convention criteria for creating or changing DAI value with settings group
                    // If 0 given as key that means No Settings Group list to add in DAI, only one value for that key(0) will be added or updated
                    // for more details see DaTypeName#addDaiValues
                    if(!hasSettingGroup(tdai) && daiValMap.size() == 1 && daiValMap.containsKey(0L)) {
                        String value = daiValMap.values().stream().findFirst().get();
                        tdai.getVal().stream().findFirst()
                                .ifPresentOrElse(tVal -> tVal.setValue(value),
                                        () -> tdai.getVal().add(newVal(value)));
                    } else {
                        for (Map.Entry<Long, String> mapVal: daiValMap.entrySet()) {
                            tdai.getVal().stream()
                                    .filter(tValElem -> tValElem.isSetSGroup() && mapVal.getKey().equals(tValElem.getSGroup()))
                                    .findFirst()
                                    .ifPresentOrElse(tVal -> tVal.setValue(mapVal.getValue()),
                                            () -> tdai.getVal().add(newVal(mapVal.getValue(), mapVal.getKey())));
                        }
                    }
        });
    }

    public void completeFromDAInstance(TIED tied, String ldInst, TAnyLN anyLN, DataAttributeRef dataRef) {
        isDOAndDAInstancesExist(anyLN, dataRef.getDoName(), dataRef.getDaName())
                .ifPresent(tdai -> {
                    if(tdai.isSetVal()) dataRef.setDaiValues(tdai.getVal());
                    if(dataRef.getFc() == TFCEnum.SG || dataRef.getFc() == TFCEnum.SE) {
                        if(hasSettingGroup(tdai)) {
                            boolean isIedHasConfSG = tied.isSetAccessPoint() &&
                                    tied.getAccessPoint().stream()
                                            .filter(tAccessPoint -> tAccessPoint.getServer() != null
                                                    && tAccessPoint.getServer().getLDevice().stream()
                                                    .anyMatch(tlDevice -> tlDevice.getInst().equals(ldInst)))
                                            .anyMatch(tAccessPoint -> tAccessPoint.isSetServices()
                                                    && tAccessPoint.getServices() != null
                                                    && tAccessPoint.getServices().getSettingGroups() != null
                                                    && tAccessPoint.getServices().getSettingGroups().getConfSG() != null);
                            dataRef.getDaName().setValImport((!tdai.isSetValImport() || tdai.isValImport()) && isIedHasConfSG);
                        } else {
                            log.warn(String.format("Inconsistency in the SCD file - DAI= %s with fc= %s must have a sGroup attribute", tdai.getName(), dataRef.getFc()));
                            dataRef.getDaName().setValImport(false);
                         }
                    } else if(tdai.isSetValImport()) dataRef.getDaName().setValImport(tdai.isValImport());
                });
    }

    private boolean hasSettingGroup(TDAI tdai) {
        return tdai.isSetVal() && tdai.getVal().stream().anyMatch(tVal -> tVal.isSetSGroup() && tVal.getSGroup() > 0);
    }

    private Optional<TDAI> createDoiSdiDaiChainIfNotExists(TAnyLN tAnyLN, DoTypeName doTypeName, DaTypeName daTypeName) {
        LinkedList<String> structInstances = new LinkedList<>(doTypeName.getStructNames());
        structInstances.addLast(daTypeName.getName());
        daTypeName.getStructNames().forEach(structInstances::addLast);
        TDOI doi = tAnyLN.getDOI().stream().filter(doi1 -> doi1.getName().equals(doTypeName.getName()))
                .findFirst()
                .orElseGet(()-> {
                    TDOI newDOI = new TDOI();
                    newDOI.setName(doTypeName.getName());
                    tAnyLN.getDOI().add(newDOI);
                    return newDOI;
                });
        if(structInstances.size() > 1){
            TSDI firstSDI = findOrCreateSDIFromDOI(doi, structInstances.getFirst());
            TSDI lastSDI = findOrCreateSDIByStructName(firstSDI, structInstances);
            if(structInstances.size() == 1){
              return lastSDI.getSDIOrDAI().stream()
                      .filter(tUnNaming -> tUnNaming.getClass().equals(TDAI.class))
                      .map(TDAI.class::cast)
                      .filter(tdai -> tdai.getName().equals(structInstances.getFirst()))
                      .map(tdai -> {
                          if(tdai.isSetValImport()) tdai.setValImport(daTypeName.isValImport());
                          return tdai;
                      })
                      .findFirst()
                      .or(() -> {
                          TDAI newDAI = new TDAI();
                          newDAI.setName(structInstances.getFirst());
                          lastSDI.getSDIOrDAI().add(newDAI);
                          return Optional.of(newDAI);
                      });
            }
        } else if(structInstances.size() == 1){
            return doi.getSDIOrDAI().stream()
                    .filter(tUnNaming -> tUnNaming.getClass().equals(TDAI.class))
                    .map(TDAI.class::cast)
                    .filter(tdai -> tdai.getName().equals(structInstances.getFirst()))
                    .map(tdai -> {
                        if(tdai.isSetValImport()) tdai.setValImport(daTypeName.isValImport());
                        return tdai;
                    })
                    .findFirst()
                    .or(() -> {
                        TDAI newDAI = new TDAI();
                        newDAI.setName(structInstances.getFirst());
                        doi.getSDIOrDAI().add(newDAI);
                        return Optional.of(newDAI);
                    });
        }
        return Optional.empty();
    }

    private TSDI findSDIByStructName(TSDI tsdi, LinkedList<String> sdiNames) {
        if(sdiNames.isEmpty()) return tsdi;
        return tsdi.getSDIOrDAI().stream()
                .filter(sdi -> sdi.getClass().equals(TSDI.class))
                .map(TSDI.class::cast)
                .filter(sdi -> sdi.getName().equals(sdiNames.getFirst()))
                .findFirst()
                .map(sdi1 -> {
                    sdiNames.remove();
                    return findSDIByStructName(sdi1, sdiNames);
                })
                .orElse(tsdi);
    }

    private TSDI findOrCreateSDIFromDOI(TDOI doi, String sdiName) {
        return doi.getSDIOrDAI().stream()
                .filter(tsdi -> tsdi.getClass().equals(TSDI.class))
                .map(TSDI.class::cast)
                .filter(tsdi -> tsdi.getName().equals(sdiName))
                .findFirst()
                .orElseGet(() -> {
                    TSDI tsdi = new TSDI();
                    tsdi.setName(sdiName);
                    doi.getSDIOrDAI().add(tsdi);
                    return tsdi;
                });
    }

    private TSDI findOrCreateSDIFromSDI(TSDI sdi, String sdiName) {
        return sdi.getSDIOrDAI().stream()
                .filter(unNaming -> unNaming.getClass().equals(TSDI.class))
                .map(TSDI.class::cast)
                .filter(tsdi -> tsdi.getName().equals(sdiName))
                .findFirst()
                .orElseGet(() -> {
                    TSDI tsdi = new TSDI();
                    tsdi.setName(sdiName);
                    sdi.getSDIOrDAI().add(tsdi);
                    return tsdi;
                });
    }

    /**
     *
     * @param sdi TSDI
     * @param structName linked list start with doi name
     * @return already existing TSDI or newly created TSDI from given TSDI
     */
    private TSDI findOrCreateSDIByStructName(TSDI sdi, LinkedList<String> structName) {
        structName.remove();
        if(structName.isEmpty() || structName.size() == 1) return sdi;
        return findOrCreateSDIByStructName(findOrCreateSDIFromSDI(sdi, structName.getFirst()), structName);
    }

}
