// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.TExtRef;
import org.lfenergy.compas.scl2007b4.model.TLLN0Enum;
import org.lfenergy.compas.sct.commons.dto.ExtrefTarget;
import org.lfenergy.compas.sct.commons.scl.ln.LN0Adapter;
import org.lfenergy.compas.sct.commons.scl.ln.LNAdapter;

/**
 * A representation of the model object
 * <em><b>{@link ObjectReference ObjectReference}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *   <li>Principal functions</li>
 *    <ul>
 *      <li>{@link ObjectReference#getReference() <em>Returns <b>LDName </b> : "name" attribute of IEDName element + "inst" attribute of LDevice element </em>}</li>
 *      <li>{@link ObjectReference#getLdName() <em>Returns <b>LNName </b> : "prefix" + "lnClass" + "lnInst" of LN element </em>}</li>
 *      <li>{@link ObjectReference#getLNodeName() <em>Returns <b>DataName </b></em>}</li>
 *      <li>{@link ObjectReference#getDataAttributes() <em>Returns <b>DataName[.DataName[…]].DataAttributeName[.DAComponentName[ ….]] </b></em>}</li>
 *    </ul>
 * </ol>
 * <br/>
 *  <pre>
 *      <b>ObjectReference</b>: LDName/LNName.DataName[.DataName[…]].DataAttributeName[.DAComponentName[ ….]]
 *      <b>LDName</b> = "name" attribute of IEDName element + "inst" attribute of LDevice element
 *      <b>LNName</b> = "prefix" + "lnClass" + "lnInst"
 *  </pre>
 * @see LNAdapter
 * @see LN0Adapter
 * @see org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter
 */
@Getter
public class ObjectReference {
    private static final String MALFORMED_OBJ_REF = "Malformed ObjRef : %s" ;

    private final String reference;
    //IEDNameLDInst
    private String ldName;
    //LNPrefixLNClassLNInst
    private String lNodeName;
    private String dataAttributes;

    public ObjectReference(String reference) {
        this.reference = reference;
        init();
    }

    public ObjectReference(TExtRef extRef, ExtrefTarget target) {
        this.ldName = extRef.getIedName() + extRef.getLdInst();
        if(target.equals(ExtrefTarget.SRC_REF)) {
            String s1 = extRef.getLnInst();
            String s2 = extRef.getPrefix();
            this.lNodeName = StringUtils.trimToEmpty(s2)
                    + (extRef.isSetLnClass() ? extRef.getLnClass().get(0) : TLLN0Enum.LLN_0.value())
                    + StringUtils.trimToEmpty(s1);
            String s = extRef.getDoName();
            this.dataAttributes = StringUtils.trimToEmpty(s);
        }
        if(target.equals(ExtrefTarget.SRC_CB)) {
            String s1 = extRef.getSrcLNInst();
            String s2 = extRef.getSrcPrefix();
            this.lNodeName = StringUtils.trimToEmpty(s2)
                    + (extRef.isSetSrcLNClass() ? extRef.getSrcLNClass().get(0) : TLLN0Enum.LLN_0.value())
                    + StringUtils.trimToEmpty(s1);
            String s = extRef.getSrcCBName();
            this.dataAttributes = StringUtils.trimToEmpty(s);
        }
        this.reference = this.ldName + "/" + this.lNodeName + "." + this.dataAttributes;
    }

    public final void init(){
        String[] objRefPart = reference.split("/");
        if(objRefPart.length != 2 || StringUtils.isBlank(objRefPart[0]) || StringUtils.isBlank(objRefPart[1])){
            throw new IllegalArgumentException(String.format(MALFORMED_OBJ_REF, reference));
        }
        ldName = objRefPart[0];
        String refCopy = objRefPart[1];
        objRefPart = refCopy.split("[.]", 2);
        if(objRefPart.length != 2 || StringUtils.isBlank(objRefPart[0]) || StringUtils.isBlank(objRefPart[1])){
            throw new IllegalArgumentException(String.format(MALFORMED_OBJ_REF, reference));
        }
        lNodeName = objRefPart[0];
        dataAttributes = objRefPart[1];
    }
}
