// SPDX-FileCopyrightText: 2023 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.api.LnEditor;
import org.lfenergy.compas.sct.commons.domain.*;
import org.lfenergy.compas.sct.commons.util.ActiveStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.lfenergy.compas.sct.commons.util.CommonConstants.MOD_DO_NAME;
import static org.lfenergy.compas.sct.commons.util.CommonConstants.STVAL_DA_NAME;
import static org.lfenergy.compas.sct.commons.util.PrivateUtils.extractStringPrivate;
import static org.lfenergy.compas.sct.commons.util.SclConstructorHelper.newVal;

@Slf4j
public class LnService implements LnEditor {

    private static final DoLinkedToDaFilter DAI_FILTER_MOD_STVAL = DoLinkedToDaFilter.from(MOD_DO_NAME, STVAL_DA_NAME);
    private static final String LNODE_STATUS_PRIVATE_TYPE = "COMPAS-LNodeStatus";

    public Stream<TAnyLN> getAnylns(TLDevice tlDevice) {
        return Stream.concat(Stream.of(tlDevice.getLN0()), tlDevice.getLN().stream());
    }

    public Stream<TAnyLN> getFilteredAnyLns(TLDevice tlDevice, Predicate<TAnyLN> lnPredicate) {
        return getAnylns(tlDevice).filter(lnPredicate);
    }

    public Optional<TAnyLN> findAnyLn(TLDevice tlDevice, Predicate<TAnyLN> lnPredicate) {
        return getFilteredAnyLns(tlDevice, lnPredicate).findFirst();
    }

    public Optional<TAnyLN> findAnyLn(TLDevice tlDevice, String lnClass, String lnInst, String lnPrefix) {
        return findAnyLn(tlDevice, tAnyLN -> matchesLn(tAnyLN, lnClass, lnInst, lnPrefix));
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

    public boolean matchesLn(TAnyLN tAnyLN, String lnClass, String lnInst, String lnPrefix) {
        return switch (tAnyLN) {
            case TLN ln -> lnClass.equals(ln.getLnClass().getFirst())
                           && StringUtils.trimToEmpty(lnInst).equals(StringUtils.trimToEmpty(ln.getInst()))
                           && (StringUtils.trimToEmpty(lnPrefix).equals(StringUtils.trimToEmpty(ln.getPrefix())));
            case LN0 ignored -> lnClass.equals(TLLN0Enum.LLN_0.value());
            default -> throw new IllegalStateException("Unexpected value: " + tAnyLN);
        };
    }

    /**
     * The Lnode status depends on the LN0 status.
     * If Ln COMPAS-LNodeStatus is missing => we take the LN0 status
     * If Ln COMPAS-LNodeStatus is OFF => the status is OFF
     * If Ln COMPAS-LNodeStatus is ON => we take the LN0 status
     *
     * @param tAnyLN the Lnode whose the status is required
     * @param ln0    the LN0
     * @return the Lnode Status
     */
    public ActiveStatus getLnStatus(TAnyLN tAnyLN, TLN0 ln0) {
        Optional<ActiveStatus> ln0Status = getPrivateCompasLNodeStatus(ln0);
        return getPrivateCompasLNodeStatus(tAnyLN).filter(ActiveStatus.OFF::equals)
                .orElseGet(() -> ln0Status.orElse(ActiveStatus.OFF));
    }

    public Optional<ActiveStatus> getPrivateCompasLNodeStatus(TAnyLN tAnyLN) {
        return extractStringPrivate(tAnyLN, LNODE_STATUS_PRIVATE_TYPE).map(ActiveStatus::fromValue);
    }

    public Optional<ActiveStatus> getDaiModStValValue(TAnyLN tAnyLN) {
        return getDaiModStVal(tAnyLN)
                .stream()
                .flatMap(tdai -> tdai.getVal().stream())
                .map(TVal::getValue)
                .findFirst()
                .map(ActiveStatus::fromValue);
    }

    public Optional<TDAI> getDaiModStVal(TAnyLN tAnyLN) {
        return getDOAndDAInstances(tAnyLN, DAI_FILTER_MOD_STVAL);
    }

    @Override
    public Optional<TDAI> getDOAndDAInstances(TAnyLN tAnyLN, DoLinkedToDaFilter doLinkedToDaFilter) {
        List<String> structNamesList = new ArrayList<>(doLinkedToDaFilter.sdoNames());
        structNamesList.add(doLinkedToDaFilter.daName());
        structNamesList.addAll(doLinkedToDaFilter.bdaNames());

        return tAnyLN.getDOI().stream().filter(doi -> doi.getName().equals(doLinkedToDaFilter.doName()))
                .findFirst()
                .flatMap(doi -> {
                    if (structNamesList.size() > 1) {
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
                    } else if (structNamesList.size() == 1) {
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
        createDoiSdiDaiChainIfNotExists(tAnyLN, doLinkedToDa.dataObject(), doLinkedToDa.dataAttribute())
                .ifPresent(tdai -> {
                    List<DaVal> daiVals = doLinkedToDa.dataAttribute().getDaiValues();
                    if (!hasSettingGroup(tdai) && daiVals.size() == 1 && daiVals.getFirst().settingGroup() == null) {
                        String value = daiVals.getFirst().val();
                        tdai.getVal().stream().findFirst()
                                .ifPresentOrElse(tVal -> tVal.setValue(value),
                                        () -> tdai.getVal().add(newVal(value)));
                    } else {
                        for (DaVal daVal : daiVals) {
                            tdai.getVal().stream()
                                    .filter(tValElem -> tValElem.isSetSGroup() && tValElem.getSGroup() == daVal.settingGroup())
                                    .findFirst()
                                    .ifPresentOrElse(tVal -> tVal.setValue(daVal.val()),
                                            () -> tdai.getVal().add(newVal(daVal.val(), daVal.settingGroup())));
                        }
                    }
                });
    }

    @Override
    public DoLinkedToDa getDoLinkedToDaCompletedFromDAI(TIED tied, String ldInst, TAnyLN anyLN, DoLinkedToDa doLinkedToDa) {
        DoLinkedToDa result = doLinkedToDa.deepCopy();
        getDOAndDAInstances(anyLN, doLinkedToDa.toFilter())
                .ifPresent(tdai -> {
                    if (tdai.isSetVal()) {
                        result.dataAttribute().addDaVal(tdai.getVal());
                    }
                    if (result.dataAttribute().getFc() == TFCEnum.SG || result.dataAttribute().getFc() == TFCEnum.SE) {
                        if (hasSettingGroup(tdai)) {
                            boolean isIedHasConfSG = tied.isSetAccessPoint() &&
                                    tied.getAccessPoint().stream()
                                            .filter(tAccessPoint -> tAccessPoint.getServer() != null
                                                    && tAccessPoint.getServer().getLDevice().stream()
                                                    .anyMatch(tlDevice -> tlDevice.getInst().equals(ldInst)))
                                            .anyMatch(tAccessPoint -> tAccessPoint.isSetServices()
                                                    && tAccessPoint.getServices() != null
                                                    && tAccessPoint.getServices().getSettingGroups() != null
                                                    && tAccessPoint.getServices().getSettingGroups().getConfSG() != null);
                            result.dataAttribute().setValImport((!tdai.isSetValImport() || tdai.isValImport()) && isIedHasConfSG);
                        } else {
                            log.warn("Inconsistency in the SCD file - DAI= {} with fc= {} must have a sGroup attribute", tdai.getName(), result.dataAttribute().getFc());
                            result.dataAttribute().setValImport(false);
                        }
                    } else if (tdai.isSetValImport()) {
                        result.dataAttribute().setValImport(tdai.isValImport());
                    }
                });
        return result;
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
                .orElseGet(() -> {
                    TDOI newDOI = new TDOI();
                    newDOI.setName(dataObject.getDoName());
                    tAnyLN.getDOI().add(newDOI);
                    return newDOI;
                });
        if (structInstances.size() > 1) {
            TSDI firstSDI = findOrCreateSDIFromDOI(doi, structInstances.getFirst());
            TSDI lastSDI = findOrCreateSDIByStructName(firstSDI, structInstances);
            if (structInstances.size() == 1) {
                return lastSDI.getSDIOrDAI().stream()
                        .filter(tUnNaming -> tUnNaming.getClass().equals(TDAI.class))
                        .map(TDAI.class::cast)
                        .filter(tdai -> tdai.getName().equals(structInstances.getFirst()))
                        .map(tdai -> {
                            if (tdai.isSetValImport()) {
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
        } else if (structInstances.size() == 1) {
            return doi.getSDIOrDAI().stream()
                    .filter(tUnNaming -> tUnNaming.getClass().equals(TDAI.class))
                    .map(TDAI.class::cast)
                    .filter(tdai -> tdai.getName().equals(structInstances.getFirst()))
                    .map(tdai -> {
                        if (tdai.isSetValImport()) tdai.setValImport(dataAttribute.isValImport());
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
        if (sdiNames.isEmpty()) return tsdi;
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
     * @param sdi        TSDI
     * @param structName list start with sdi name
     * @return already existing TSDI or newly created TSDI from given TSDI
     */
    private TSDI findOrCreateSDIByStructName(TSDI sdi, List<String> structName) {
        structName.removeFirst();
        if (structName.isEmpty() || structName.size() == 1) return sdi;
        return findOrCreateSDIByStructName(findOrCreateSDIFromSDI(sdi, structName.getFirst()), structName);
    }

}
