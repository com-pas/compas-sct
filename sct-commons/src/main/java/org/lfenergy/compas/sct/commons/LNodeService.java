// SPDX-FileCopyrightText: 2023 2024 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lfenergy.compas.scl2007b4.model.*;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Slf4j
public class LNodeService {

    public Stream<TLNode> getLNodes(TBay tBay) {
        return tBay.getFunction().stream()
                .flatMap(tFunction -> tFunction.getLNode().stream());
    }

    public Stream<TLNode> getFilteredLNodes(TBay tBay, Predicate<TLNode> tlNodePredicate) {
        return getLNodes(tBay).filter(tlNodePredicate);
    }

    public Optional<TLNode> findLNode(TBay tBay, Predicate<TLNode> tlNodePredicate) {
        return getFilteredLNodes(tBay, tlNodePredicate).findFirst();
    }

    public boolean matchesLnode(TLNode tlNode, String iedName, String ldInst, TAnyLN tAnyLN) {
        return switch (tAnyLN) {
            case TLN ln -> matchesLnode(tlNode, iedName, ldInst, ln.getLnClass().isEmpty() ? "" : ln.getLnClass().getFirst(), ln.getInst(), ln.getPrefix());
            case LN0 ln0 -> matchesLnode(tlNode, iedName, ldInst, TLLN0Enum.LLN_0.value(), ln0.getInst(), "");
            default -> throw new IllegalStateException("Unexpected value: " + tAnyLN);
        };
    }

    public boolean matchesLnode(TLNode tlNode, String iedName, String ldInst, String lnClass, String lnInst, String lnPrefix) {
        return tlNode.getIedName().equals(iedName)
               && tlNode.getLdInst().equals(ldInst)
               && tlNode.getLnClass().contains(lnClass)
               && StringUtils.trimToEmpty(lnInst).equals(StringUtils.trimToEmpty(tlNode.getLnInst()))
               && StringUtils.trimToEmpty(lnPrefix).equals(StringUtils.trimToEmpty(tlNode.getPrefix()));
    }

}
