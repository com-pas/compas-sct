// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.header;


import org.lfenergy.compas.scl2007b4.model.THeader;
import org.lfenergy.compas.scl2007b4.model.THitem;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HeaderAdapter extends SclElementAdapter<SclRootAdapter, THeader> {
    public static final String DEFAULT_TOOL_ID = "COMPAS";

    public HeaderAdapter(SclRootAdapter parentAdapter, THeader currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected boolean amChildElementRef() {
        return currentElem == parentAdapter.getCurrentElem().getHeader();
    }

    public HeaderAdapter addHistoryItem(String who, String what, String why){
        THitem tHitem = new THitem();
        tHitem.setRevision(currentElem.getRevision());
        tHitem.setVersion(currentElem.getVersion());
        tHitem.setWho(who);
        tHitem.setWhat(what);
        tHitem.setWhen((new Date()).toString());
        tHitem.setWhy(why);

        THeader.History history = currentElem.getHistory();
        if(history == null) {
            history = new THeader.History();
            currentElem.setHistory(history);
        }
        history.getHitem().add(tHitem);

        return this;
    }

    public String getHeaderId() {
        return currentElem.getId();
    }

    public String getHeaderRevision() {
        return currentElem.getRevision();
    }

    public String getHeaderVersion() {
        return currentElem.getVersion();
    }

    public List<? extends THitem> getHistoryItems() {
        if(currentElem.getHistory() == null) return new ArrayList<>();
        return currentElem.getHistory().getHitem();
    }

    public void updateVersion(String hVersion) {
        currentElem.setVersion(hVersion);
    }

    public void updateRevision(String hRevision) {
        currentElem.setRevision(hRevision);
    }
}
