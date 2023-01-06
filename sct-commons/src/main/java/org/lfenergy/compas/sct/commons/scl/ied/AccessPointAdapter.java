/*
 * // SPDX-FileCopyrightText: 2023 RTE FRANCE
 * //
 * // SPDX-License-Identifier: Apache-2.0
 */

package org.lfenergy.compas.sct.commons.scl.ied;

import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.dto.SclReportItem;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.ServicesConfigEnum;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.scl2007b4.model.TAccessPoint AccessPoint}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Functions</li>
 *    <ul>
 *      <li>{@link AccessPointAdapter#checkFCDALimitations <em>Returns the value of the <b>name </b>attribute</em>}</li>
 *      <li>{@link AccessPointAdapter#checkControlsLimitation Returns the value of the <b>Service </b>object</em>}</li>
 *    </ul>
 * </ol>
 *
 */

public class AccessPointAdapter extends SclElementAdapter<IEDAdapter, TAccessPoint> {

    public static final long MAX_OCCURRENCE_NO_LIMIT_VALUE = -1L;
    private static final String CLIENT_IED_NAME = "The Client IED ";

    /**
     * Constructor
     *
     * @param parentAdapter Parent container reference
     * @param tAccessPoint       Current reference
     */
    public AccessPointAdapter(IEDAdapter parentAdapter, TAccessPoint tAccessPoint) {
        super(parentAdapter, tAccessPoint);
    }

    /**
     * Check if current element is a child of the parent element
     *
     * @return true if the currentElem is part of the parentAdapter children
     */
    @Override
    protected boolean amChildElementRef() {
       return parentAdapter.getCurrentElem().getAccessPoint().stream()
               .anyMatch(tAccessPoint -> tAccessPoint.getName().equals(currentElem.getName()));
    }

    @Override
    protected String elementXPath() {
        return String.format("AccessPoint[%s]", Utils.xpathAttributeFilter("name", currentElem.isSetName() ? currentElem.getName() : null));
    }

    /**
     * Gets XPath path to current element from parent element
     * @return path to current element
     */
    @Override
    public String getXPath(){
        String parentXpath = (parentAdapter != null) ? parentAdapter.getXPath() : "";
        return parentXpath + "/" + elementXPath();
    }

    /**
     * Gets all LDevice from AccessPoint
     * @return Stream of <em>LDeviceAdapter</em> object as IEDs of SCL
     */
    private Stream<LDeviceAdapter> streamLDeviceAdapters() {
        if(!currentElem.isSetServer()) return Stream.empty();
        return currentElem.getServer().getLDevice().stream()
                .map(tlDevice -> new LDeviceAdapter(getParentAdapter(), tlDevice));
    }

    /**
     * Checks FCDA number limitation into each LDevice of AccessPoint
     * @return List of errors encountered for LDevices
     */
    public List<SclReportItem> checkFCDALimitations(){
        long max  = getMaxInstanceAutorized(ServicesConfigEnum.FCDA);
        if(currentElem.getServer() == null) return new ArrayList<>();
        return currentElem.getServer().getLDevice().stream()
                .map(tlDevice -> new LDeviceAdapter(parentAdapter, tlDevice))
                .map(lDeviceAdapter ->
                        lDeviceAdapter.getLNAdaptersInclundigLN0().stream()
                                .map(abstractLNAdapter -> abstractLNAdapter.getCurrentElem().getDataSet())
                                .flatMap(Collection::stream)
                                .filter(tDataSet -> max == MAX_OCCURRENCE_NO_LIMIT_VALUE || tDataSet.getFCDA().size() > max)
                                .map(tDataSet -> SclReportItem.warning(getXPath(), String.format("There are too much FCDA for the DataSet %S for the LDevice %S in IED %S",
                                        tDataSet.getName(), lDeviceAdapter.getInst(), parentAdapter.getName()))).toList()
                ).flatMap(Collection::stream).toList();
    }

    /**
     * Checks if occurrences of specified tpe (DataSet, Controls) exceeds config limitation
     * @param servicesConfigEnum type of element for which limitation is checked
     * @param msg error message to display
     * @return Optional of encountered error or empty
     */
    public  Optional<SclReportItem> checkControlsLimitation(ServicesConfigEnum servicesConfigEnum, String msg){
        long max = getMaxInstanceAutorized(servicesConfigEnum);
        long value = getNumberOfItems(servicesConfigEnum);
        return max == MAX_OCCURRENCE_NO_LIMIT_VALUE || value <= max ? Optional.empty() : Optional.of(SclReportItem.warning(getXPath(), String.format("%s %s", msg, parentAdapter.getName())));
    }

    /**
     * Counts all occurence of Control into AccessPoint
     * @param servicesConfigEnum type (GOOSE, Report, SampledValue, DataSet)
     * @return number of occurrence
     */
    private long getNumberOfItems(ServicesConfigEnum servicesConfigEnum) {
        if (!currentElem.isSetServer()) return 0L;
        return currentElem.getServer().getLDevice().stream()
                .map(tlDevice -> new LDeviceAdapter(parentAdapter, tlDevice))
                .map(LDeviceAdapter::getLNAdaptersInclundigLN0)
                .flatMap(Collection::stream)
                .map(abstractLNAdapter -> {
                    if(servicesConfigEnum == ServicesConfigEnum.DATASET) return abstractLNAdapter.getCurrentElem().getDataSet();
                    else return abstractLNAdapter.getTControlsByType(getControlTypeClass(servicesConfigEnum));})
                .mapToLong(Collection::size)
                .sum();
    }

    private Class<? extends TControl> getControlTypeClass(ServicesConfigEnum servicesConfigEnum) {
        return switch (servicesConfigEnum) {
            case REPORT -> TReportControl.class;
            case GSE -> TGSEControl.class;
            case SMV -> TSampledValueControl.class;
            default -> null;
        };
    }

    /**
     * Gets max number authorized in configuration of each elements DataSets, FCDAs, Control Blocks) into an AccessPoint
     * @param servicesConfigEnum element type
     * @return max number authorized by config
     */
    private long getMaxInstanceAutorized(ServicesConfigEnum servicesConfigEnum){
        if(currentElem.getServices() == null) return MAX_OCCURRENCE_NO_LIMIT_VALUE;
        TServices tServices = currentElem.getServices();

        return switch (servicesConfigEnum) {
            case DATASET -> (tServices.getConfDataSet() != null && tServices.getConfDataSet().isSetMax()) ? tServices.getConfDataSet().getMax() : MAX_OCCURRENCE_NO_LIMIT_VALUE;
            case FCDA -> (tServices.getConfDataSet() != null && tServices.getConfDataSet().isSetMaxAttributes()) ? tServices.getConfDataSet().getMaxAttributes() : MAX_OCCURRENCE_NO_LIMIT_VALUE;
            case REPORT -> (tServices.getConfReportControl() != null && tServices.getConfReportControl().isSetMax()) ? tServices.getConfReportControl().getMax() : MAX_OCCURRENCE_NO_LIMIT_VALUE;
            case GSE -> (tServices.getGOOSE() != null && tServices.getGOOSE().isSetMax()) ? tServices.getGOOSE().getMax() : MAX_OCCURRENCE_NO_LIMIT_VALUE;
            case SMV -> (tServices.getSMVsc() != null && tServices.getSMVsc().isSetMax()) ? tServices.getSMVsc().getMax() : MAX_OCCURRENCE_NO_LIMIT_VALUE;
        };
    }


    /**
     * Checks FCDA number limitation for binded IED
     * @param msg message to display hen error occured
     * @return Optional of encountered error or empty
     */
    public Optional<SclReportItem> checkLimitationForBindedIEDFCDAs(Set<TExtRef> tExtRefs, String msg){
        long value  = tExtRefs.stream()
                .map( tExtRef -> {
                            IEDAdapter iedAdapter = getParentAdapter().getParentAdapter().getIEDAdapterByName(tExtRef.getIedName());
                            LDeviceAdapter lDeviceAdapter;
                            if (tExtRef.getSrcLDInst() != null)
                                lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(tExtRef.getSrcLDInst());
                            else lDeviceAdapter = iedAdapter.getLDeviceAdapterByLdInst(tExtRef.getLdInst());

                            AbstractLNAdapter abstractLNAdapter;
                            if (!tExtRef.isSetSrcLNClass() || tExtRef.getSrcLNClass().contains(TLLN0Enum.LLN_0.value()))
                                abstractLNAdapter = lDeviceAdapter.getLN0Adapter();
                            else
                                abstractLNAdapter = lDeviceAdapter.getLNAdapter(tExtRef.getSrcLNClass().get(0), tExtRef.getSrcLNInst(), tExtRef.getSrcPrefix());

                   return abstractLNAdapter.getFCDAs(tExtRef);
                }).flatMap(Collection::stream)
                .toList()
                .size();
        long max;
        if(currentElem.getServices() == null) {
            max = AccessPointAdapter.MAX_OCCURRENCE_NO_LIMIT_VALUE;
        } else {
            TClientServices tClientServices = currentElem.getServices().getClientServices();
            max = tClientServices != null && tClientServices.isSetMaxAttributes() ? tClientServices.getMaxAttributes() : AccessPointAdapter.MAX_OCCURRENCE_NO_LIMIT_VALUE;
        }

       return max == AccessPointAdapter.MAX_OCCURRENCE_NO_LIMIT_VALUE || value <= max ? Optional.empty() :
             Optional.of(SclReportItem.warning(getParentAdapter().getXPath(),  msg));

    }

    /**
     *
     * @param sclReportItems
     * @param tExtRefSet
     */
    record ExtRefAnalyzeRecord(List<SclReportItem> sclReportItems, Set<TExtRef> tExtRefSet){
    }

    /**
     * Returns all ExtRef of the AccessPoint which have SrcCBName set and ServiceType provided
     * @return ExtRefAnalyzeRecord object containing Set of ExtRefs and errors list for ExtRefs with SrcCBName and without ServiceType provided
     */
    public ExtRefAnalyzeRecord getAllCoherentExtRefForAnalyze() {
        List<SclReportItem> sclReportItems = new ArrayList<>();
        Set<TExtRef> tExtRefSet= new HashSet<>();
        streamLDeviceAdapters().map(lDeviceAdapter -> {
            Set<TExtRef> tExtRefs = lDeviceAdapter.getLN0Adapter().getExtRefs().stream().filter(TExtRef::isSetSrcCBName).collect(Collectors.toSet());
            sclReportItems.addAll(checkExtRefWithoutServiceType(tExtRefs));
            tExtRefs.removeIf(tExtRef -> !tExtRef.isSetServiceType());
            return tExtRefs;
        }).flatMap(Collection::stream).forEach(tExtRef -> {
            if(tExtRefSet.isEmpty())
                tExtRefSet.add(tExtRef);
            if(tExtRefSet.stream().noneMatch(t -> extrefEquals(tExtRef,t)))
                tExtRefSet.add(tExtRef);
        });
        return new ExtRefAnalyzeRecord(sclReportItems, tExtRefSet);
    }

    /**
     * Checks all ExtRefs with SrcCBName and without ServiceType provided
     * @param tExtRefs Set of ExtRefs to check
     * @return errors list
     */
    private List<SclReportItem> checkExtRefWithoutServiceType(Set<TExtRef> tExtRefs) {
        return tExtRefs.stream()
                .filter(tExtRef -> !tExtRef.isSetServiceType())
                .map(tExtRef ->
                       SclReportItem.warning(getXPath(), "ExtRef signal without ServiceType : " + tExtRef.getDesc()))
                .toList();
    }

    /**
     * Checks if two ExtRefs fed by same Control Block for SCL limits analyze
     * Nota : this equality is only for checking limitation check
     * @param t1 extref to compare
     * @param t2 extref to compare
     * @return true if the two ExtRef are fed by same Control Block, otherwise false
     */
    private static boolean extrefEquals(TExtRef t1, TExtRef t2) {
        if(!t1.isSetSrcLNClass()) t1.getSrcLNClass().add(TLLN0Enum.LLN_0.value());
        if(!t2.isSetSrcLNClass()) t2.getSrcLNClass().add(TLLN0Enum.LLN_0.value());
        return  Utils.equalsOrBothBlank(t1.getIedName(), t2.getIedName())
                && Utils.equalsOrBothBlank(t1.getSrcLDInst(), t2.getSrcLDInst())
                && (t1.getSrcLNClass().equals(t2.getSrcLNClass()))
                && Utils.equalsOrBothBlank(t1.getSrcLNInst(), t2.getSrcLNInst())
                && Utils.equalsOrBothBlank(t1.getSrcPrefix(),t2.getSrcPrefix())
                && t1.getServiceType().equals(t2.getServiceType());
    }

    /**
     * Checks Control Blocks (Report, Goose, SMV) number limitation for binded IED
     * @return List of errors encountered
     */
    public List<SclReportItem> checkLimitationForBindedIEDControls(Set<TExtRef> tExtRefs){
        Map<TServiceType, Set<TExtRef>> extRefsByServiceType = tExtRefs.stream()
                .filter(TExtRef::isSetServiceType)
                .collect(Collectors.groupingBy(TExtRef::getServiceType, Collectors.toSet()));
        return extRefsByServiceType.keySet().stream()
                .map(tServiceType-> {
                    Set<TExtRef> tExtRefSet = extRefsByServiceType.get(tServiceType);
                    return switch (tServiceType) {
                        case REPORT -> checkLimitationForOneControlType( tExtRefSet, CLIENT_IED_NAME+getParentAdapter().getName()+" subscribes to too much REPORT Control Blocks.", ServicesConfigEnum.REPORT);
                        case GOOSE -> checkLimitationForOneControlType( tExtRefSet, CLIENT_IED_NAME+getParentAdapter().getName()+" subscribes to too much GOOSE Control Blocks.", ServicesConfigEnum.GSE);
                        case SMV -> checkLimitationForOneControlType( tExtRefSet, CLIENT_IED_NAME+getParentAdapter().getName()+" subscribes to too much SMV Control Blocks.", ServicesConfigEnum.SMV);
                        default -> throw new ScdException("Unsupported value: " + tServiceType);
                    };
                }).toList().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * Checks Control Block number limitation for binded IED
     * @param tExtRefs list of ExtRefs referenced same ied
     * @param msg message to display hen error occured
     * @param servicesConfigEnum type of Control Block for which check is done
     * @return Optional of encountered error or empty
     */
    private Optional<SclReportItem> checkLimitationForOneControlType(Set<TExtRef> tExtRefs, String msg, ServicesConfigEnum servicesConfigEnum) {
        long max = getMaxInstanceAutorizedForBindedIED(servicesConfigEnum);
        long value = tExtRefs.size();
        return max == AccessPointAdapter.MAX_OCCURRENCE_NO_LIMIT_VALUE || value <= max ? Optional.empty() : Optional.of(SclReportItem.warning(getParentAdapter().getXPath(), msg));
    }

    public static Class<? extends TControl> getControlTypeClass(TServiceType tServiceType) {
        if(tServiceType == null) throw new IllegalArgumentException("TServiceType of ExtRef should not be null");
        return switch (tServiceType) {
            case REPORT -> TReportControl.class;
            case GOOSE -> TGSEControl.class;
            case SMV -> TSampledValueControl.class;
            case POLL ->  throw new ScdException("Unsupported value: " + tServiceType);
        };
    }

    /**
     * Gets max number authorized in configuration of each element DataSets, FCDAs, Control Blocks) into an AccessPoint
     * @param servicesConfigEnum element type
     * @return max number authorized by config
     */
    private long getMaxInstanceAutorizedForBindedIED(ServicesConfigEnum servicesConfigEnum) {
        if (currentElem.getServices() == null) {
            return AccessPointAdapter.MAX_OCCURRENCE_NO_LIMIT_VALUE;
        } else {
            TClientServices tClientServices = currentElem.getServices().getClientServices();
            return switch (servicesConfigEnum) {
                case DATASET, FCDA -> tClientServices != null && tClientServices.isSetMaxAttributes() ? tClientServices.getMaxAttributes() : AccessPointAdapter.MAX_OCCURRENCE_NO_LIMIT_VALUE;
                case REPORT -> tClientServices != null && tClientServices.isSetMaxReports() ? tClientServices.getMaxReports() : AccessPointAdapter.MAX_OCCURRENCE_NO_LIMIT_VALUE;
                case GSE -> tClientServices != null && tClientServices.isSetMaxGOOSE() ? tClientServices.getMaxGOOSE() : AccessPointAdapter.MAX_OCCURRENCE_NO_LIMIT_VALUE;
                case SMV -> tClientServices != null && tClientServices.isSetMaxSMV() ? tClientServices.getMaxSMV() : AccessPointAdapter.MAX_OCCURRENCE_NO_LIMIT_VALUE;
            };
        }
    }



}
