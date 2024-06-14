// SPDX-FileCopyrightText: 2023 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.api.LnEditor;
import org.lfenergy.compas.sct.commons.domain.*;
import org.lfenergy.compas.sct.commons.util.ActiveStatus;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.*;
import java.util.stream.Stream;

import static org.lfenergy.compas.sct.commons.util.CommonConstants.MOD_DO_NAME;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.STVAL_DA_NAME;
import static org.lfenergy.compas.sct.commons.util.SclConstructorHelper.newVal;

@Slf4j
public class LnService implements LnEditor {

    public Stream<TAnyLN> getAnylns(TLDevice tlDevice) {
        return Stream.concat(Stream.of(tlDevice.getLN0()), tlDevice.getLN().stream());
    }

    public Stream<TAnyLN> getFilteredAnyLns(TLDevice tlDevice, Predicate<TAnyLN> lnPredicate) {
        return getAnylns(tlDevice).filter(lnPredicate);
    }

    public Optional<TAnyLN> findAnyLn(TLDevice tlDevice, Predicate<TAnyLN> lnPredicate) {
        return getFilteredAnyLns(tlDevice, lnPredicate).findFirst();
    }

    public Stream<TLN> getLns(TLDevice tlDevice) {
        return tlDevice.getLN().stream();
    }

    public Stream<TLN> getFilteredLns(TLDevice tlDevice, Predicate<TLN> lnPredicate) {
        return getLns(tlDevice).filter(lnPredicate);
    }

    public Optional<TLN> findLn(TLDevice tlDevice, Predicate<TLN> lnPredicate) {
        return getFilteredLns(tlDevice, lnPredicate).findFirst();
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

    @Override
    public Optional<TDAI> getDOAndDAInstances(TAnyLN tAnyLN, DoLinkedToDaFilter doLinkedToDaFilter) {
        List<String> structNamesList = new ArrayList<>(doLinkedToDaFilter.sdoNames());
        structNamesList.add(doLinkedToDaFilter.daName());
        structNamesList.addAll(doLinkedToDaFilter.bdaNames());

        return tAnyLN.getDOI().stream().filter(doi -> doi.getName().equals(doLinkedToDaFilter.doName()))
                .findFirst()
                .flatMap(doi -> {
                    if(structNamesList.size() > 1) {
                        String firstSDIName = structNamesList.removeFirst();
                        return this.getSdiByName(doi, firstSDIName)
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
                                    return Optional.empty();
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
    public void updateOrCreateDOAndDAInstances(TAnyLN tAnyLN, DoLinkedToDa doLinkedToDa) {
        createDoiSdiDaiChainIfNotExists(tAnyLN, doLinkedToDa.getDataObject(), doLinkedToDa.getDataAttribute())
                .ifPresent(tdai -> {
                    List<DaVal> daiVals = doLinkedToDa.getDataAttribute().getDaiValues();
                    if(!hasSettingGroup(tdai) && daiVals.size() == 1 && daiVals.getFirst().settingGroup() == null) {
                        String value = daiVals.getFirst().val();
                        tdai.getVal().stream().findFirst()
                                .ifPresentOrElse(tVal -> tVal.setValue(value),
                                        () -> tdai.getVal().add(newVal(value)));
                    } else {
                        for (DaVal daVal: daiVals) {
                            tdai.getVal().stream()
                                    .filter(tValElem -> tValElem.isSetSGroup() && tValElem.getSGroup() == daVal.settingGroup())
                                    .findFirst()
                                    .ifPresentOrElse(tVal -> tVal.setValue(daVal.val()),
                                            () -> tdai.getVal().add(newVal(daVal.val(), daVal.settingGroup())));
                        }
                    }
                });
    }

    public void completeFromDAInstance(TIED tied, String ldInst, TAnyLN anyLN, DoLinkedToDa doLinkedToDa) {
        getDOAndDAInstances(anyLN, doLinkedToDa.toFilter())
                .ifPresent(tdai -> {
                    if(tdai.isSetVal()) {
                        doLinkedToDa.getDataAttribute().addDaVal(tdai.getVal());
                    }
                    if(doLinkedToDa.getDataAttribute().getFc() == TFCEnum.SG || doLinkedToDa.getDataAttribute().getFc() == TFCEnum.SE) {
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
                            doLinkedToDa.getDataAttribute().setValImport((!tdai.isSetValImport() || tdai.isValImport()) && isIedHasConfSG);
                        } else {
                            log.warn(String.format("Inconsistency in the SCD file - DAI= %s with fc= %s must have a sGroup attribute", tdai.getName(), doLinkedToDa.getDataAttribute().getFc()));
                            doLinkedToDa.getDataAttribute().setValImport(false);
                         }
                    } else if(tdai.isSetValImport()) {
                        doLinkedToDa.getDataAttribute().setValImport(tdai.isValImport());
                    }
                });
    }

    private boolean hasSettingGroup(TDAI tdai) {
        return tdai.isSetVal() && tdai.getVal().stream().anyMatch(tVal -> tVal.isSetSGroup() && tVal.getSGroup() > 0);
    }

    private Optional<TDAI> createDoiSdiDaiChainIfNotExists(TAnyLN tAnyLN, DataObject dataObject, DataAttribute dataAttribute) {
        List<String> structInstances = new ArrayList<>(dataObject.getSdoNames());
        structInstances.add(dataAttribute.getDaName());
        structInstances.addAll(dataAttribute.getBdaNames());

        TDOI doi = tAnyLN.getDOI().stream().filter(doi1 -> doi1.getName().equals(dataObject.getDoName()))
                .findFirst()
                .orElseGet(()-> {
                    TDOI newDOI = new TDOI();
                    newDOI.setName(dataObject.getDoName());
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
                            if(tdai.isSetValImport()) {
                                tdai.setValImport(dataAttribute.isValImport());
                            }
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
                        if(tdai.isSetValImport()) tdai.setValImport(dataAttribute.isValImport());
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

    private TSDI findSDIByStructName(TSDI tsdi, List<String> sdiNames) {
        if(sdiNames.isEmpty()) return tsdi;
        return this.getSdiByName(tsdi, sdiNames.getFirst())
                .map(sdi1 -> {
                    sdiNames.removeFirst();
                    return findSDIByStructName(sdi1, sdiNames);
                })
                .orElse(tsdi);
    }

    private TSDI findOrCreateSDIFromDOI(TDOI doi, String sdiName) {
        return this.getSdiByName(doi, sdiName)
                .orElseGet(() -> {
                    TSDI tsdi = new TSDI();
                    tsdi.setName(sdiName);
                    doi.getSDIOrDAI().add(tsdi);
                    return tsdi;
                });
    }

    private TSDI findOrCreateSDIFromSDI(TSDI sdi, String sdiName) {
        return this.getSdiByName(sdi, sdiName)
                .orElseGet(() -> {
                    TSDI tsdi = new TSDI();
                    tsdi.setName(sdiName);
                    sdi.getSDIOrDAI().add(tsdi);
                    return tsdi;
                });
    }

    private Optional<TSDI> getSdiByName(TDOI doi, String sdiName) {
        return doi.getSDIOrDAI().stream()
                .filter(unNaming -> unNaming.getClass().equals(TSDI.class))
                .map(TSDI.class::cast)
                .filter(tsdi -> tsdi.getName().equals(sdiName))
                .findFirst();
    }


    private Optional<TSDI> getSdiByName(TSDI sdi, String sdiName) {
        return sdi.getSDIOrDAI().stream()
                .filter(unNaming -> unNaming.getClass().equals(TSDI.class))
                .map(TSDI.class::cast)
                .filter(tsdi -> tsdi.getName().equals(sdiName))
                .findFirst();
    }

    /**
     *
     * @param sdi TSDI
     * @param structName list start with sdi name
     * @return already existing TSDI or newly created TSDI from given TSDI
     */
    private TSDI findOrCreateSDIByStructName(TSDI sdi, List<String> structName) {
        structName.removeFirst();
        if(structName.isEmpty() || structName.size() == 1) return sdi;
        return findOrCreateSDIByStructName(findOrCreateSDIFromSDI(sdi, structName.getFirst()), structName);
    }

}
