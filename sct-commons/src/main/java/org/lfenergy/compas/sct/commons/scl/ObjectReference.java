// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;


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
 * @see org.lfenergy.compas.sct.commons.scl.ied.LNAdapter
 * @see org.lfenergy.compas.sct.commons.scl.ied.LN0Adapter
 * @see org.lfenergy.compas.sct.commons.scl.ied.IEDAdapter
 */
@Getter
public class ObjectReference {
    private static final String MALFORMED_OBJ_REF = "Malformed ObjRef : %s" ;

    private final String reference;
    //IEDName.LDInst
    private String ldName;
    //LNPrefixLNClassLNInst
    private String lNodeName;
    private String dataAttributes;

    public ObjectReference(String reference) {
        this.reference = reference;
        init();
    }

    public final void init(){
        String refCopy = reference;
        String[] objRefPart = reference.split("[/]");
        if(objRefPart.length != 2 || StringUtils.isBlank(objRefPart[0]) || StringUtils.isBlank(objRefPart[1])){
            throw new IllegalArgumentException(String.format(MALFORMED_OBJ_REF, reference));
        }
        ldName = objRefPart[0];
        refCopy = objRefPart[1];
        objRefPart = refCopy.split("[.]", 2);
        if(objRefPart.length != 2 || StringUtils.isBlank(objRefPart[0]) || StringUtils.isBlank(objRefPart[1])){
            throw new IllegalArgumentException(String.format(MALFORMED_OBJ_REF, reference));
        }
        lNodeName = objRefPart[0];
        dataAttributes = objRefPart[1];
    }
}
