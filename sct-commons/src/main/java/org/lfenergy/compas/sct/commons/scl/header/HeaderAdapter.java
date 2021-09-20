// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.header;

import org.lfenergy.compas.scl2007b4.model.SCL;
import org.lfenergy.compas.scl2007b4.model.THeader;
import org.lfenergy.compas.scl2007b4.model.THitem;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.springframework.util.Assert;

import java.util.Date;

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
        Assert.notNull(currentElem, "Stored SCD must have Header tag");
        Assert.notNull(currentElem.getId(), "Stored SCD Header must have a unique ID");

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
}
