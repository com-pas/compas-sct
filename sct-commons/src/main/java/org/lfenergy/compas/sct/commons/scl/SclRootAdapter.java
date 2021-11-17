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

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Slf4j
public class SclRootAdapter extends SclElementAdapter<SclRootAdapter, SCL> {

    public static final short RELEASE = 4;
    public static final String REVISION = "B";
    public static final String VERSION = "2007";


    public SclRootAdapter(String hId, String hVersion, String hRevision) throws ScdException {
        super(null);
        currentElem = new SCL();
        currentElem.setRelease(RELEASE);
        currentElem.setVersion(VERSION);
        currentElem.setRevision(REVISION);
        addHeader(hId,hVersion,hRevision);
    }


    public SclRootAdapter(SCL scd) {
        super(null,scd);
        if(scd.getHeader() == null){
            throw new IllegalArgumentException("Invalid SCD: no tag Header found");
        }
    }


    @Override
    protected boolean amChildElementRef() {
        return true;
    }

    public Short getSclRelease(){
        return currentElem.getRelease();
    }

    public String getSclRevision(){
        return currentElem.getRevision();
    }

    public String getSclVersion(){
        return currentElem.getVersion();
    }

    public SubstationAdapter getSubstationAdapter(String ssName) throws ScdException {
        return new SubstationAdapter(this,ssName);
    }

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

    public IEDAdapter addIED(SCL icd, String iedName) throws ScdException {
        if(icd.getIED().isEmpty()){
            throw new ScdException("No IED to import from ICD file");
        }

        if(hasIED(iedName)){
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
        return getIEDAdapter(iedName);
    }

    private boolean hasIED(String iedName) {
        return currentElem.getIED()
                .stream()
                .anyMatch(tied -> tied.getName().equals(iedName));
    }

    public HeaderAdapter getHeaderAdapter() {
        return new HeaderAdapter(this,currentElem.getHeader());
    }

    public DataTypeTemplateAdapter getDataTypeTemplateAdapter(){
        if(currentElem.getDataTypeTemplates() == null){
            currentElem.setDataTypeTemplates(new TDataTypeTemplates());
        }
        return new DataTypeTemplateAdapter(this, currentElem.getDataTypeTemplates());
    }

    public IEDAdapter getIEDAdapter(String iedName) throws ScdException {
        return new IEDAdapter(this,iedName);
    }

    public CommunicationAdapter getCommunicationAdapter(boolean createIfNotExists) throws ScdException {
        if(currentElem.getCommunication() == null && !createIfNotExists){
            throw new ScdException("SCD has no communication tag");
        } else if(currentElem.getCommunication() == null && createIfNotExists){
            currentElem.setCommunication(new TCommunication());
        }
        return new CommunicationAdapter(this,currentElem.getCommunication());
    }

    public List<IEDAdapter> getIEDAdapters() {
        return currentElem.getIED().stream()
                .map(tied -> new IEDAdapter(this,tied))
                .collect(Collectors.toList());
    }
}
