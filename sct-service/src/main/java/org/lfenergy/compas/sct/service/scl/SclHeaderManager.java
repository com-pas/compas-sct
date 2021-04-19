package org.lfenergy.compas.sct.service.scl;

import org.lfenergy.compas.exception.ScdException;
import org.lfenergy.compas.scl.SCL;
import org.lfenergy.compas.scl.THeader;
import org.lfenergy.compas.scl.THitem;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.Date;

public class SclHeaderManager {

    public static final String TOOL_ID = "COMPAS";

    private SCL receiver;

    public SclHeaderManager(SCL receiver) {
        this.receiver = receiver;
    }

    public SCL addHeader(@NonNull String hId, @NonNull String hVersion, @NonNull String hRevision) throws ScdException {
        if(receiver.getHeader() != null){
            throw new ScdException("SCL already contains header");
        }

        THeader tHeader = new THeader();
        tHeader.setRevision(hRevision);
        tHeader.setVersion(hVersion);
        tHeader.setId(hId);
        tHeader.setToolID(TOOL_ID);
        receiver.setHeader(tHeader);

        return receiver;
    }

    public SCL addHistoryItem(String who, String what, String why){

        THitem tHitem = new THitem();
        tHitem.setRevision(receiver.getHeader().getRevision());
        tHitem.setVersion(receiver.getHeader().getVersion());
        tHitem.setWho(who);
        tHitem.setWhat(what);
        tHitem.setWhen((new Date()).toString());
        tHitem.setWhy(why);

        THeader tHeader = receiver.getHeader();
        Assert.notNull(tHeader, "Stored SCD must have Header tag");
        Assert.notNull(tHeader.getId(), "Stored SCD Header must have a unique ID");

        THeader.History history = tHeader.getHistory();
        if(history == null) {
            history = new THeader.History();
            tHeader.setHistory(history);
        }
        history.getHitem().add(tHitem);

        return receiver;
    }
}
