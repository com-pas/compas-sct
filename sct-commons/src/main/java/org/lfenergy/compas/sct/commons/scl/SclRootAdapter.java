// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;


import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.com.CommunicationAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;
import org.lfenergy.compas.sct.commons.scl.header.HeaderAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.sstation.SubstationAdapter;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.scl2007b4.model.SCL SCL}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Adapter</li>
 *    <ul>
 *      <li>{@link SclRootAdapter#getHeaderAdapter <em>Returns the value of the <b>HeaderAdapter </b> reference object</em>}</li>
 *      <li>{@link SclRootAdapter#getSubstationAdapter(String) <em>Returns the value of the <b>SubstationAdapter </b> reference object By nme</em>}</li>
 *      <li>{@link SclRootAdapter#getCommunicationAdapter(boolean) <em>Returns or Adds the value of the <b>CommunicationAdapter </b> reference object</em>}</li>
 *      <li>{@link SclRootAdapter#streamIEDAdapters() <em>Returns the value of the <b>IEDAdapter </b> containment reference list</em>}</li>
 *      <li>{@link SclRootAdapter#getIEDAdapterByName(String) <em>Returns the value of the <b>IEDAdapter </b> reference object By name</em>}</li>
 *      <li>{@link SclRootAdapter#getDataTypeTemplateAdapter() <em>Returns the value of the <b>DataTypeTemplateAdapter </b> containment reference list</em>}</li>
 *    </ul>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link SclRootAdapter#addPrivate <em>Add <b>Private </b>under this object</em>}</li>
 *      <li>{@link SclRootAdapter#addHeader(String, String, String) <em>Adds <b>Header </b> describing the children under this object</em>}</li>
 *      <li>{@link SclRootAdapter#addIED(SCL, String) <em>Adds <b>IED </b> describing the children under this object</em>}</li>
 *      <li>{@link SclRootAdapter#addPrivate(TPrivate) <em>Add  given <b>TPrivate </b> describing the children under this object</em>}</li>
 *      <li>{@link SclRootAdapter#getSclRevision() <em>Returns the value of the <b>revision </b> attribute</em>}</li>
 *      <li>{@link SclRootAdapter#getSclRelease() <em>Returns the value of the <b>release </b> attribute</em>}</li>
 *      <li>{@link SclRootAdapter#getSclVersion() <em>Returns the value of the <b>version </b> attribute</em>}</li>
 *    </ul>
 *   <li>Checklist functions</li>
 *    <ul>
 *        <li>{@link SclRootAdapter#checkObjRef(String) <em>Check the value of <b>ObjectReference </b> object</em>}</li>
 *    </ul>
 * </ol>
 */
@Getter
@Setter
@Slf4j
public class SclRootAdapter extends SclElementAdapter<SclRootAdapter, SCL> {

    public static final short RELEASE = 4;
    public static final String REVISION = "B";
    public static final String VERSION = "2007";
    private static final String MESSAGE_IED_NAME_NOT_FOUND = "IED.name '%s' not found in SCD";

    /**
     * Constructor
     * @param hId SCL Header ID
     * @param hVersion SCL Header Version
     * @param hRevision SCL Header Revision
     * @throws ScdException throws when inconsistenc in SCL file
     */
    public SclRootAdapter(String hId, String hVersion, String hRevision) throws ScdException {
        super(null);
        currentElem = new SCL();
        currentElem.setRelease(RELEASE);
        currentElem.setVersion(VERSION);
        currentElem.setRevision(REVISION);
        addHeader(hId,hVersion,hRevision);
    }

    /**
     * Constructor
     * @param scd SCL for which adapter is created
     */
    public SclRootAdapter(SCL scd) {
        super(null,scd);
        if(scd.getHeader() == null){
            throw new IllegalArgumentException("Invalid SCD: no tag Header found");
        }
    }

    /**
     * Check if node is child of the reference node
     * @return <em>true</em>
     */
    @Override
    protected boolean amChildElementRef() {
        return true;
    }

    /**
     * Returns XPath path to current element
     * @return <em>"SCL</em>>
     */
    @Override
    protected String elementXPath() {
        return "SCL";
    }

    /**
     * Gets SCL Release value
     * @return Release value
     */
    public Short getSclRelease(){
        return currentElem.getRelease();
    }

    /**
     * Gets SCL Revision value
     * @return Revision value
     */
    public String getSclRevision(){
        return currentElem.getRevision();
    }

    /**
     * Gets SCL Version value
     * @return Version value
     */
    public String getSclVersion(){
        return currentElem.getVersion();
    }

    /**
     * Gets Substation from SCL root node by Substation name
     * @param ssName name of wanted Substation
     * @return <em>SubstationAdapter</em> object
     * @throws ScdException throws when unknown Substation
     */
    public SubstationAdapter getSubstationAdapter(String ssName) throws ScdException {
        return new SubstationAdapter(this,ssName);
    }

    /**
     * Gets the first Substation from SCL root node
     * @return <em>SubstationAdapter</em> object
     * @throws ScdException throws when unknown Substation
     */
    public SubstationAdapter getSubstationAdapter() throws ScdException {
        TSubstation tSubstation = currentElem.getSubstation()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ScdException("No Substation in SCL file"));
        return new SubstationAdapter(this, tSubstation);
    }

    /**
     * Add Header to SCL root node
     * @param hId SCL Header ID
     * @param hVersion SCL Header Version
     * @param hRevision SCL Header Revision
     * @throws ScdException throws when header already exists in SCL
     */
    protected void addHeader(@NonNull String hId, @NonNull String hVersion, @NonNull String hRevision) throws ScdException {

        if(currentElem.getHeader() != null){
            throw new ScdException("SCL already contains header");
        }

        THeader tHeader = new THeader();
        tHeader.setRevision(hRevision);
        tHeader.setVersion(hVersion);
        tHeader.setId(hId);
        tHeader.setToolID(HeaderAdapter.DEFAULT_TOOL_ID);
        currentElem.setHeader(tHeader);
    }

    /**
     * Adds IED and updates DataTypeTemplate of current SCL
     * @param icd ICD containing IED to add and related DataTypeTemplate
     * @param iedName name of IED to add in SCL
     * @return <em>IEDAdapter</em> as added IED
     * @throws ScdException throws when inconsistency between IED to add and SCL file content
     */
    public IEDAdapter addIED(SCL icd, String iedName) throws ScdException {
        if(icd.getIED().isEmpty()){
            throw new ScdException("No IED to import from ICD file");
        }

        if(findIedAdapterByName(iedName).isPresent()){
            String msg = "SCL file already contains IED: " + iedName;
            log.error(msg);
            throw new ScdException(msg);
        }

        // import DTT
        DataTypeTemplateAdapter rcvDttAdapter = getDataTypeTemplateAdapter();
        SclRootAdapter prvSclRootAdapter = new SclRootAdapter(icd);
        DataTypeTemplateAdapter prvDttAdapter = prvSclRootAdapter.getDataTypeTemplateAdapter();
        var pairOldNewId = rcvDttAdapter.importDTT(iedName,prvDttAdapter);

        IEDAdapter prvIEDAdapter = new IEDAdapter(prvSclRootAdapter, icd.getIED().get(0));
        prvIEDAdapter.setIEDName(iedName);
        prvIEDAdapter.updateLDeviceNodesType(pairOldNewId);
        //add IED
        currentElem.getIED().add(prvIEDAdapter.currentElem);
        return getIEDAdapterByName(iedName);
    }

    /**
     * Gets Header from current SCL
     * @return <em>HeaderAdapter</em> object as Header of SCL
     */
    public HeaderAdapter getHeaderAdapter() {
        return new HeaderAdapter(this,currentElem.getHeader());
    }

    /**
     * Gets DataTypeTemplates from current SCL
     * @return <em>DataTypeTemplateAdapter</em> object as DataTypeTemplates of SCL
     */
    public DataTypeTemplateAdapter getDataTypeTemplateAdapter(){
        if(currentElem.getDataTypeTemplates() == null){
            currentElem.setDataTypeTemplates(new TDataTypeTemplates());
        }
        return new DataTypeTemplateAdapter(this, currentElem.getDataTypeTemplates());
    }

    /**
     * Gets IED by name from SCL
     * @param iedName name of IED to find in SCL
     * @return <em>IEDAdapter</em> object as IED of SCL
     * @throws ScdException throws when unknown IED
     */
    public IEDAdapter getIEDAdapterByName(String iedName) throws ScdException {
        return findIedAdapterByName(iedName)
            .orElseThrow(() -> new ScdException(String.format(MESSAGE_IED_NAME_NOT_FOUND, iedName)));
    }

    /**
     * Find an IED by name from SCL
     * @param iedName name of IED to find in SCL
     * @return <em>Optional<IEDAdapter></em> of the first IED with a matching name
     */
    public Optional<IEDAdapter> findIedAdapterByName(String iedName) throws ScdException {
        return currentElem.getIED().stream()
            .filter(ied -> Objects.equals(ied.getName(), iedName))
            .findFirst()
            .map(ied -> new IEDAdapter(this, ied));
    }

    /**
     * Gets Communication from SCL
     * @param createIfNotExists true create Communication node if not exist, false do not create communication
     * @return <em>CommunicationAdapter</em> object as IED of SCL
     * @throws ScdException throws when no Communication in SCL and <em>createIfNotExists == false</em>
     */
    public CommunicationAdapter getCommunicationAdapter(boolean createIfNotExists) throws ScdException {
        if(currentElem.getCommunication() == null && !createIfNotExists){
            throw new ScdException("SCD has no communication tag");
        } else if(currentElem.getCommunication() == null && createIfNotExists){
            currentElem.setCommunication(new TCommunication());
        }
        return new CommunicationAdapter(this,currentElem.getCommunication());
    }

    /**
     * Gets all IEDs from SCL
     * @return Stream of <em>IEDAdapter</em> object as IEDs of SCL
     */
    public Stream<IEDAdapter> streamIEDAdapters() {
        return currentElem.getIED().stream()
            .map(tied -> new IEDAdapter(this, tied));
    }

    /**
     * Checks if given reference matches with at least one IED of SCL
     * @param val reference to check
     * @return <em>IEDAdapter</em> object as IED of SCL
     * @throws ScdException throws when no IED matches
     */
    public IEDAdapter checkObjRef(String val) throws ScdException {
        ObjectReference objRef = new ObjectReference(val);
        for(TIED tied : currentElem.getIED()){
            IEDAdapter iedAdapter = new IEDAdapter(this,tied);
            if(iedAdapter.matches(objRef)){
                return iedAdapter;
            }
        }
        throw new ScdException("Invalid ObjRef: " + val);
    }

}
