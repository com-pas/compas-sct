// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;


import lombok.Getter;
import lombok.Setter;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.com.CommunicationAdapter;
import org.lfenergy.compas.sct.commons.scl.dtt.DataTypeTemplateAdapter;
import org.lfenergy.compas.sct.commons.scl.header.HeaderAdapter;
import org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter;
import org.lfenergy.compas.sct.commons.scl.sstation.SubstationAdapter;
import org.springframework.lang.NonNull;

import java.util.List;



@Getter
@Setter
public class SclRootAdapter extends SclElementAdapter<SclRootAdapter, SCL> {

    private static final short RELEASE = 4;
    private static final String REVISION = "B";
    private static final String VERSION = "2007";

    public SclRootAdapter() {
        super(null);
        init();
    }

    public SclRootAdapter(SCL currentElem) {
        super(null, currentElem);
    }

    public SclRootAdapter(String hId, String hVersion, String hRevision) throws ScdException {
        super(null);
        init();
        addHeader(hId,hVersion,hRevision);
    }

    protected final void init(){
        currentElem = new SCL();
        currentElem.setRelease(RELEASE);
        currentElem.setVersion(VERSION);
        currentElem.setRevision(REVISION);
    }

    @Override
    protected boolean amChildElementRef() {
        return true;
    }

    public Short getRelease(){
        return currentElem.getRelease();
    }

    public String getRevision(){
        return currentElem.getRevision();
    }

    public String getVersion(){
        return currentElem.getVersion();
    }

    public SubstationAdapter getSubstationAdapter(String ssName) throws ScdException {
        return new SubstationAdapter(this,ssName);
    }

    public HeaderAdapter addHeader(@NonNull String hId, @NonNull String hVersion, @NonNull String hRevision) throws ScdException {

        if(currentElem.getHeader() != null){
            throw new ScdException("SCL already contains header");
        }

        THeader tHeader = new THeader();
        tHeader.setRevision(hRevision);
        tHeader.setVersion(hVersion);
        tHeader.setId(hId);
        tHeader.setToolID(HeaderAdapter.DEFAULT_TOOL_ID);
        currentElem.setHeader(tHeader);
        return new HeaderAdapter(this,tHeader);

    }

    public HeaderAdapter getHeaderAdapter() {
        return new HeaderAdapter(this,currentElem.getHeader());
    }

    public DataTypeTemplateAdapter getDataTypeTemplateAdapter(){
        return new DataTypeTemplateAdapter(this, currentElem.getDataTypeTemplates());
    }

    public IEDAdapter getIEDAdapter(String iedName) throws ScdException {
        return new IEDAdapter(this,iedName);
    }

    public CommunicationAdapter getCommunicationAdapter() {
        return new CommunicationAdapter(this,currentElem.getCommunication());
    }
}
