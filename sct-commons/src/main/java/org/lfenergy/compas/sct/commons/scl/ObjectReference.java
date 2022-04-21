// SPDX-FileCopyrightText: 2021 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;


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
