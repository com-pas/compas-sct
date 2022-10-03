// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.header;

import org.lfenergy.compas.scl2007b4.model.THeader;
import org.lfenergy.compas.scl2007b4.model.THitem;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.sct.commons.scl.SclElementAdapter;
import org.lfenergy.compas.sct.commons.scl.SclRootAdapter;
import org.lfenergy.compas.sct.commons.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A representation of the model object
 * <em><b>{@link org.lfenergy.compas.scl2007b4.model.THeader Header}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link HeaderAdapter#addHistoryItem <em>add History</em>}</li>
 *   <li>{@link HeaderAdapter#updateVersion <em>update Version</em>}</li>
 *   <li>{@link HeaderAdapter#updateRevision <em>update Revision</em>}</li>
 *   <li>{@link HeaderAdapter#addPrivate <em>Add <b>TPrivate </b>under this object</em>}</li>
 * </ul>
 *
 * @see org.lfenergy.compas.sct.commons.scl.SclRootAdapter
 * @see org.lfenergy.compas.scl2007b4.model.THeader
 * @see org.lfenergy.compas.scl2007b4.model.THitem
 * @see <a href="https://github.com/com-pas/compas-sct/issues/88" target="_blank">Issue !88</a>
 */
public class HeaderAdapter extends SclElementAdapter<SclRootAdapter, THeader> {

    /**
     * The default value of the {@link THeader#getToolID() <em>ToolID</em>} attribute.
     * @see org.lfenergy.compas.scl2007b4.model.THeader#getToolID()
     */
    public static final String DEFAULT_TOOL_ID = "COMPAS";

    /**
     * Constructor
     * @param parentAdapter Parent container reference
     * @param currentElem Current reference
     */
    public HeaderAdapter(SclRootAdapter parentAdapter, THeader currentElem) {
        super(parentAdapter, currentElem);
    }

    @Override
    protected boolean amChildElementRef() {
        return currentElem == parentAdapter.getCurrentElem().getHeader();
    }

    @Override
    protected String elementXPath() {
        return String.format("Header[%s and %s and %s]",
                Utils.xpathAttributeFilter("id", currentElem.isSetId() ? currentElem.getId() : null),
                Utils.xpathAttributeFilter("version", currentElem.isSetVersion() ? currentElem.getVersion() : null),
                Utils.xpathAttributeFilter("revision", currentElem.isSetRevision() ? currentElem.getRevision() : null));
    }

    /**
     * @param who input
     * @param what input
     * @param why input
     * @return {@link HeaderAdapter}
     *
     * @see <a href="https://github.com/com-pas/compas-sct/issues/6">Issue !6</a>
     * @see <a href="https://github.com/com-pas/compas-sct/issues/71">Issue !71</a>
     */
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

    /**
     * Returns the value of the <em><b>id</b></em> attribute.
     * @return the value of the <em><b>id</b></em> attribute.
     */
    public String getHeaderId() {
        return currentElem.getId();
    }

    /**
     * Returns the value of the <em><b>Revision</b></em> attribute.
     * @return the value of the <em><b>Revision</b></em> attribute.
     */
    public String getHeaderRevision() {
        return currentElem.getRevision();
    }

    /**
     * Returns the value of the <em><b>Version</b></em> attribute.
     * @return the value of the <em><b>Version</b></em> attribute.
     */
    public String getHeaderVersion() {
        return currentElem.getVersion();
    }

    /**
     * Returns the value of the <em><b>History</b></em> containment reference list.
     * The list contents are of type {@link org.lfenergy.compas.scl2007b4.model.THitem}.
     * @return the value of the <em><b>History</b></em> containment reference list.
     */
    public List<THitem> getHistoryItems() {
        if(currentElem.getHistory() == null) return new ArrayList<>();
        return currentElem.getHistory().getHitem();
    }

    /**
     * Update the value of the <em><b>Version</b></em> attribute
     * @param hVersion input
     */
    public void updateVersion(String hVersion) {
        currentElem.setVersion(hVersion);
    }

    /**
     * Update the value of the <em><b>Revision</b></em> attribute
     * @param hRevision input
     */
    public void updateRevision(String hRevision) {
        currentElem.setRevision(hRevision);
    }
}
