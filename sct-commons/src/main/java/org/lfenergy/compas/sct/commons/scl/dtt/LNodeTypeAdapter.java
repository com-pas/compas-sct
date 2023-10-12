// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.dtt;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.lfenergy.compas.scl2007b4.model.TDO;
import org.lfenergy.compas.scl2007b4.model.TLNodeType;
import org.lfenergy.compas.scl2007b4.model.TPredefinedBasicTypeEnum;
import org.lfenergy.compas.sct.commons.dto.DaTypeName;
import org.lfenergy.compas.sct.commons.dto.DataAttributeRef;
import org.lfenergy.compas.sct.commons.dto.DoTypeName;
import org.lfenergy.compas.sct.commons.dto.ExtRefSignalInfo;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.scl2007b4.model.TLNodeType LNodeType}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *    <ul>
 *       <li>{@link LNodeTypeAdapter#getDataTypeTemplateAdapter() <em>Returns the value of the <b>DataTypeTemplateAdapter </b>reference object</em>}</li>
 *       <li>{@link LNodeTypeAdapter#getDOAdapterByName <em>Returns the value of the <b>DOAdapter </b> by <b>DO </b> name </em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link LNodeTypeAdapter#addPrivate <em>Add <b>TPrivate </b>under this object</em>}</li>
 *      <li>{@link LNodeTypeAdapter#getDOTypeId <em>Returns the value of the <b>type </b>attribute By DOType Id</em>}</li>
 *      <li>{@link LNodeTypeAdapter#getId() <em>Returns the value of the <b>id </b>attribute</em>}</li>
 *      <li>{@link LNodeTypeAdapter#getLNClass <em>Returns the value of the <b>lnClass </b>attribute</em>}</li>
 *      <li>{@link LNodeTypeAdapter#getDataAttributeRefs(DataAttributeRef)}  <em>Returns <b>DataAttributeRef </b> list</em>}</li>
 *      <li>{@link LNodeTypeAdapter#getDataAttributeRefs(String)}  <em>Returns <b>DataAttributeRef </b> list</em>}</li>
 *    </ul>
 *   <li>Checklist functions</li>
 *    <ul>
 *       <li>{@link LNodeTypeAdapter#hasSameContentAs <em>Compare Two TLNodeType</em>}</li>
 *       <li>{@link LNodeTypeAdapter#containsDOWithDOTypeId <em>Check whether TLNodeType contain TDO By Id</em>}</li>
 *    </ul>
 * </ol>
 */
@Slf4j
public class LNodeTypeAdapter
        extends SclElementAdapter<DataTypeTemplateAdapter, TLNodeType>
        implements IDataTemplate, IDTTComparable<TLNodeType> {

    /**
     * Constructor
     *
     * @param parentAdapter Parent container reference
     * @param currentElem   Current reference
     */
    public LNodeTypeAdapter(DataTypeTemplateAdapter parentAdapter, TLNodeType currentElem) {
        super(parentAdapter, currentElem);
    }

    /**
     * Check if node is child of the reference node
     *
     * @return link parent child existence
     */
    @Override
    protected boolean amChildElementRef() {
        return parentAdapter.getCurrentElem().getLNodeType().contains(currentElem);
    }

    @Override
    protected String elementXPath() {
        return String.format("LNodeType[%s and %s]",
                Utils.xpathAttributeFilter("id", currentElem.isSetId() ? currentElem.getId() : null),
                Utils.xpathAttributeFilter("lnClass", currentElem.isSetLnClass() ? currentElem.getLnClass() : null));
    }

    /**
     * Compares current LNodeType and given LNodeType
     *
     * @param tlNodeType LNodeType to compare with
     * @return <em>Boolean</em> value of comparison result
     */
    @Override
    public boolean hasSameContentAs(TLNodeType tlNodeType) {

        if (!DataTypeTemplateAdapter.hasSamePrivates(currentElem, tlNodeType)) {
            return false;
        }

        if (Objects.equals(
                currentElem.getLnClass().toArray(new String[0]),
                tlNodeType.getLnClass().toArray(new String[0])
        ) || !Objects.equals(currentElem.getIedType(), tlNodeType.getIedType())) {
            return false;
        }

        List<TDO> thisTDOs = currentElem.getDO();
        List<TDO> inTDOs = tlNodeType.getDO();
        if (thisTDOs.size() != inTDOs.size()) {
            return false;
        }
        for (int i = 0; i < inTDOs.size(); i++) {
            // the order in which DOs appears matter
            TDO inTDO = inTDOs.get(i);
            TDO thisTDO = thisTDOs.get(i);
            if (!thisTDO.getType().equals(inTDO.getType())
                    || !thisTDO.getName().equals(inTDO.getName())
                    || thisTDO.isTransient() != inTDO.isTransient()
                    || !Objects.equals(thisTDO.getAccessControl(), inTDO.getAccessControl())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if current LNodeType contains DO with specific DOTYpe ID
     *
     * @param doTypeId ID of DOType in DO to check
     * @return <em>Boolean</em> value of check result
     */
    public boolean containsDOWithDOTypeId(String doTypeId) {
        return currentElem.getDO().stream()
                .anyMatch(tdo -> tdo.getType().equals(doTypeId));
    }

    /**
     * Gets LnClass value
     *
     * @return LnClass Value
     */
    public String getLNClass() {
        if (!currentElem.getLnClass().isEmpty()) {
            return currentElem.getLnClass().get(0);
        }
        return null;
    }

    /**
     * Gets DOType ID from current LNodeType
     *
     * @param doName name of DO for which ID is search
     * @return optional of <em>Boolean</em> value
     */
    public Optional<String> getDOTypeId(String doName) {
        return currentElem.getDO()
                .stream()
                .filter(tdo -> doName.equals(Utils.removeTrailingDigits(tdo.getName())))
                .map(TDO::getType)
                .findFirst();
    }

    /**
     * return a list of summarized Data Attribute References beginning from given this LNodeType.
     *
     * @param filter filter for LNodeType
     * @return list of completed Data Attribute References beginning from this LNodeType.
     * @apiNote This method doesn't check relationship between DO/SDO and DA. Check should be done by caller
     */
    public List<DataAttributeRef> getDataAttributeRefs(@NonNull DataAttributeRef filter) {

        List<DataAttributeRef> dataAttributeRefs = new ArrayList<>();
        if (filter.isDaNameDefined()) {
            try {
                check(filter.getDoName(), filter.getDaName());
            } catch (ScdException e) {
                log.error(e.getMessage());
                return dataAttributeRefs;
            }
        }
        DataAttributeRef rootDataAttributeRef = new DataAttributeRef();
        rootDataAttributeRef.setLnType(currentElem.getId());
        rootDataAttributeRef.setLnClass(filter.getLnClass());
        rootDataAttributeRef.setLnInst(filter.getLnInst());
        rootDataAttributeRef.setPrefix(filter.getPrefix());

        for (TDO tdo : currentElem.getDO()) {
            if (filter.isDoNameDefined() &&
                    !filter.getDoName().getName().equals(tdo.getName())) {
                continue;
            }

            parentAdapter.getDOTypeAdapterById(tdo.getType()).ifPresent(
                    doTypeAdapter -> {
                        DataAttributeRef currentDataAttributeRef = DataAttributeRef.copyFrom(rootDataAttributeRef);
                        currentDataAttributeRef.getDoName().setName(tdo.getName());
                        currentDataAttributeRef.getDoName().setCdc(doTypeAdapter.getCdc());
                        dataAttributeRefs.addAll(doTypeAdapter.getDataAttributeRefs(currentDataAttributeRef, filter));
                    }
            ); // else this should never happen or the scd won't be built in the first place and we'd never be here
        }
        return dataAttributeRefs;
    }

    /**
     * Return a list of summarized Data Attribute References beginning from given this LNodeType.
     * The key point in the algorithm is to find where the DO/SDO part ends and the DA/BDA part begins.
     * Once it is found, we can use the usual method DOTypeAdapter#getDataAttributeRefs to retrieve the DataAttributeRef
     * For example, with input "Do1.da1", we want to find the "da1" which is the DA/BDA part and also the DOTypeAdapter of "Do1" (to call DOTypeAdapter#getDataAttributeRefs)
     * Other example, with input "Do1.sdo1.da1.bda1", we want to find the "da1.bda1" which is the DA/BDA part and also the DOTypeAdapter of "sdo1" (to call DOTypeAdapter#getDataAttributeRefs)
     * To do that we put the input in a Queue ["Do1", "sdo1", "da1", "bda1"]
     * 1. The first element "Do1" must be a DO in the curent LNodeTypeAdapter. We retrieve its DOTypeAdapter doTypeAdapterOfDo1.
     * 2a. The next element "sdo1" can be a SDO or a DA. We call doTypeAdapterOfDo1.getDOTypeAdapterBySdoName, and we get the DOTypeAdapter for sdo1 doTypeAdapterOfSdo1. We found it so it really is a SDO.
     * 2b. The next element "da1" can be a SDO or a DA. We call doTypeAdapterOfSdo1.getDOTypeAdapterBySdoName, and we get an empty Optional. So no it is not a SDO.
     * We stop here because we have what we want : doTypeAdapterOfSdo1 and the DA/BDA part "da1.bda1"
     * 3. we call doTypeAdapterOfSdo1#getDataAttributeRefs as usual with "da1.bda1" filter
     *
     * @param dataRef complete reference of Data Attribute including DO name, SDO names (optional), DA name, BDA names (optional).
     *                Ex: Do.sdo1.sdo2.da.bda1.bda2
     * @return list of completed Data Attribute References beginning from this LNodeType.
     * @apiNote This method doesn't check relationship between DO/SDO and DA. Check should be done by caller
     */
    public DataAttributeRef getDataAttributeRefs(@NonNull String dataRef) {
        LinkedList<String> dataRefList = new LinkedList<>(Arrays.asList(dataRef.split("\\.")));
        if (dataRefList.size() < 2) {
            throw new ScdException("Invalid data reference %s. At least DO name and DA name are required".formatted(dataRef));
        }
        // 1. Get the DO
        String doName = dataRefList.remove();
        return getDOAdapterByName(doName)
                .flatMap(doAdapter -> getDataTypeTemplateAdapter().getDOTypeAdapterById(doAdapter.getType()))
                .map(doTypeAdapter -> {
                    // 2. find the SDOs, if any
                    List<String> sdoAccumulator = new ArrayList<>();
                    List<String> daAccumulator = new ArrayList<>();
                    DOTypeAdapter deepestDo = dataRefList.stream()
                            .reduce(doTypeAdapter, // initial value is the DO
                                    (lastDoTypeAdapter, name) -> {
                                        if (daAccumulator.isEmpty()) { // We did not reach the DA/BDA part yet
                                            Optional<DOTypeAdapter> optSdo = lastDoTypeAdapter.getDOTypeAdapterBySdoName(name);
                                            if (optSdo.isPresent()) {
                                                // case name is a SDO name
                                                sdoAccumulator.add(name);
                                                return optSdo.get(); // return the found SDO
                                            }
                                        }
                                        // case name is a DA or a BDA name
                                        daAccumulator.add(name);
                                        return lastDoTypeAdapter; // return the same as input
                                    },
                                    (doTypeAdapter1, doTypeAdapter2) -> {
                                        throw new ScdException("This reduction cannot be parallel");
                                    });
                    // 3. Find the DA/BDA by calling usual DOTypeAdapter.getDataAttributeRefs
                    DoTypeName doTypeName = new DoTypeName(doName);
                    doTypeName.getStructNames().addAll(sdoAccumulator);
                    doTypeName.setCdc(doTypeAdapter.getCdc());
                    DataAttributeRef rootDataAttributeRef = new DataAttributeRef();
                    rootDataAttributeRef.setDoName(doTypeName);
                    DataAttributeRef filter = new DataAttributeRef();
                    filter.setDaName(new DaTypeName(String.join(".", daAccumulator)));
                    List<DataAttributeRef> dataAttributeRefs = deepestDo.getDataAttributeRefs(rootDataAttributeRef, filter);
                    // We want exactly one result
                    if (dataAttributeRefs.size() > 1) {
                        throw new ScdException("Multiple Data Attribute found for this data reference %s in LNodeType.lnClass=%s, LNodeType.id=%s. Found DA : %s ".formatted(dataRef, getLNClass(), getId(), dataAttributeRefs.stream().map(DataAttributeRef::getDataAttributes).collect(Collectors.joining(", "))));
                    }
                    if (dataAttributeRefs.isEmpty() || !dataRef.equals(dataAttributeRefs.get(0).getDataAttributes())) {
                        return null;
                    }
                    return dataAttributeRefs.get(0);
                }).orElseThrow(() ->
                        new ScdException("No Data Attribute found with this reference %s for LNodeType.lnClass=%s, LNodeType.id=%s ".formatted(dataRef, getLNClass(), getId())));
    }

    /**
     * Gets linked DataTypeTemplateAdapter as parent
     *
     * @return <em>DataTypeTemplateAdapter</em> object
     */
    @Override
    public DataTypeTemplateAdapter getDataTypeTemplateAdapter() {
        return parentAdapter;
    }

    /**
     * Gets DO from current LNodeType
     *
     * @param name name of DO to find
     * @return optional of <em>DOAdapter</em> adapter
     */
    public Optional<DOAdapter> getDOAdapterByName(String name) {
        for (TDO tdo : currentElem.getDO()) {
            if (tdo.getName().equals(name)) {
                return Optional.of(new DOAdapter(this, tdo));
            }
        }
        return Optional.empty();
    }

    /**
     * Find path from a DO to DA (defined by names)
     *
     * @param doName DO from which find a path
     * @param daName DA for which find a path to
     * @return pair of DO name and  DOType.
     * @throws ScdException when inconsistency are found in th SCL's
     *                      DataTypeTemplate (unknown reference for example). Which should normally not happens.
     */
    Pair<String, DOTypeAdapter> findPathFromDo2DA(String doName, String daName) throws ScdException {
        DOAdapter doAdapter = getDOAdapterByName(doName).orElseThrow();
        DOTypeAdapter doTypeAdapter = doAdapter.getDoTypeAdapter().orElseThrow();
        if (doTypeAdapter.containsDAWithDAName(doName)) {
            return Pair.of(doName, doTypeAdapter);
        }
        return doTypeAdapter.findPathDoTypeToDA(daName);
    }


    /**
     * Check if DoTypeName and DaTypeName are correct and coherent with this LNodeTypeAdapter
     *
     * @param doTypeName DO/SDO to check
     * @param daTypeName DA/BDA to check
     * @throws ScdException when inconsistency are found in th SCL's
     *                      DataTypeTemplate (unknown reference for example). Which should normally not happens.
     */
    public void check(@NonNull DoTypeName doTypeName, @NonNull DaTypeName daTypeName) throws ScdException {
        if (!doTypeName.isDefined() || !daTypeName.isDefined()) {
            throw new ScdException("Invalid Data: data attributes information are missing");
        }
        // check Data Object information
        DOAdapter doAdapter = this.getDOAdapterByName(doTypeName.getName()).orElseThrow(
                () -> new ScdException(
                        String.format("Unknown DO(%s) in LNodeType(%s)", doTypeName.getName(), currentElem.getId())
                )
        );

        DOTypeAdapter doTypeAdapter = doAdapter.getDoTypeAdapter().orElseThrow(
                () -> new ScdException("Corrupted SCL DataTypeTemplate, Unknown DOType id: " + doAdapter.getType())
        );

        Pair<String, DOTypeAdapter> adapterPair = doTypeAdapter.checkAndCompleteStructData(doTypeName)
                .orElse(null);

        // check coherence between Data Object and Data Attributes information
        DOTypeAdapter lastDoTypeAdapter;
        if (adapterPair == null) {
            adapterPair = findPathFromDo2DA(doTypeName.getName(), daTypeName.getName());
            lastDoTypeAdapter = adapterPair.getValue();
        } else {
            if (adapterPair.getRight().containsDAWithDAName(daTypeName.getName())) {
                lastDoTypeAdapter = adapterPair.getValue();
            } else {
                adapterPair = adapterPair.getRight().findPathDoTypeToDA(daTypeName.getName());
                lastDoTypeAdapter = adapterPair.getValue();
            }
        }

        DAAdapter daAdapter = lastDoTypeAdapter.getDAAdapterByName(daTypeName.getName())
                .orElseThrow(
                        () -> new ScdException(
                                String.format("Unknown DA (%s) in DOType (%s) ", daTypeName.getName(), "leafSdoId")
                        )
                );

        // check Data Attributes
        if (!daTypeName.getStructNames().isEmpty() && daAdapter.getBType() != TPredefinedBasicTypeEnum.STRUCT) {
            throw new ScdException("Invalid DA chain" + daTypeName);
        }

        if (daTypeName.getStructNames().isEmpty()) {
            daAdapter.check(daTypeName);
        } else {
            daTypeName.setFc(daAdapter.getCurrentElem().getFc());
            DATypeAdapter daTypeAdapter = parentAdapter.getDATypeAdapterById(daAdapter.getType()).orElseThrow(
                    () -> new ScdException(
                            String.format("Unknown DAType (%s) referenced by DA(%s)", daAdapter.getType(), daAdapter.getName())
                    )
            );
            daTypeAdapter.check(daTypeName);
        }
    }

    /**
     * Gets list of summarized data type template from DaTypeName
     *
     * @param daTypeName DaTypeName from which summarized data type templates are created
     * @return list of <em>DataAttributeRef</em> object
     */
    public List<DataAttributeRef> getDataAttributeRefByDaName(DaTypeName daTypeName) throws ScdException {
        Optional<DataAttributeRef> opDataAttributeRef;
        List<DataAttributeRef> dataAttributeRefs = new ArrayList<>();
        for (TDO tdo : currentElem.getDO()) {
            DOAdapter doAdapter = new DOAdapter(this, tdo);
            DOTypeAdapter doTypeAdapter = doAdapter.getDoTypeAdapter().orElseThrow();
            DataAttributeRef dataAttributeRef = new DataAttributeRef();
            dataAttributeRef.setLnType(currentElem.getId());
            dataAttributeRef.getDoName().setName(doAdapter.getCurrentElem().getName());

            opDataAttributeRef = doTypeAdapter.getDataAttributeRefByDaName(daTypeName, dataAttributeRef);
            opDataAttributeRef.ifPresent(dataAttributeRefs::add);
        }
        return dataAttributeRefs;
    }

    /**
     * Gets list of summarized data type template from DoTypeName
     *
     * @param doTypeName DoTypeName from which summarized data type templates are created
     * @return list of <em>DataAttributeRef</em> object
     */
    public List<DataAttributeRef> getDataAttributeRefByDoName(DoTypeName doTypeName) {


        DOAdapter doAdapter = getDOAdapterByName(doTypeName.getName()).orElseThrow();
        DataAttributeRef dataAttributeRef = new DataAttributeRef();
        dataAttributeRef.getDoName().setName(doTypeName.getName());
        DOTypeAdapter doTypeAdapter = doAdapter.getDoTypeAdapter().orElseThrow();
        return doTypeAdapter.getDataAttributeRefByDoName(doTypeName, 0, dataAttributeRef);

    }

    /**
     * Gets current LNodeType ID
     *
     * @return LNodeType ID
     */
    public String getId() {
        return currentElem.getId();
    }

    /**
     * Find bound DOType info
     *
     * @param signalInfo extRef signal info for binding
     * @return DOType info as object containing name, id and adapter
     * @throws ScdException throws when DO unknown
     */
    public DataTypeTemplateAdapter.DOTypeInfo findMatchingDOType(ExtRefSignalInfo signalInfo) throws ScdException {
        DoTypeName doName = new DoTypeName(signalInfo.getPDO());
        String extDoName = Utils.removeTrailingDigits(doName.getName());
        String doTypeId = getDOTypeId(extDoName).orElseThrow(() ->
                new IllegalArgumentException("Unknown doName :" + signalInfo.getPDO()));
        DOTypeAdapter doTypeAdapter = this.getParentAdapter().getDOTypeAdapterById(doTypeId).orElseThrow(() ->
                new IllegalArgumentException(String.format("%s: No referenced to DO id : %s, scl file not valid", doName, doTypeId)));
        doTypeAdapter.checkAndCompleteStructData(doName);
        return new DataTypeTemplateAdapter.DOTypeInfo(doName, doTypeId, doTypeAdapter);
    }
}
