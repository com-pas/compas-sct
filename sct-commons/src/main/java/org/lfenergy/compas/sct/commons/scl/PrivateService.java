// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.TAnyContentFromOtherNamespace;
import org.lfenergy.compas.scl2007b4.model.TBaseElement;
import org.lfenergy.compas.scl2007b4.model.TCompasICDHeader;
import org.lfenergy.compas.scl2007b4.model.TPrivate;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.util.PrivateEnum;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public final class PrivateService {

    private PrivateService() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static <T> List<T> getCompasPrivates(List<TPrivate> tPrivates, Class<T> compasClass) throws ScdException {
        PrivateEnum privateEnum = PrivateEnum.fromClass(compasClass);
        List<Object> compasElements = tPrivates.stream().filter(tPrivate -> privateEnum.getPrivateType().equals(tPrivate.getType()))
            .map(TAnyContentFromOtherNamespace::getContent).flatMap(List::stream)
            .filter(JAXBElement.class::isInstance).map(JAXBElement.class::cast)
            .filter(Predicate.not(JAXBElement::isNil))
            .map(JAXBElement::getValue).collect(Collectors.toList());

        List<T> result = new ArrayList<>();
        for (Object compasElement : compasElements) {
            if (compasClass.isInstance(compasElement)) {
                result.add(compasClass.cast(compasElement));
            } else {
                throw new ScdException(String.format("Private is inconsistent. It has type=%s which expect JAXBElement<%s> content, " +
                        "but got JAXBElement<%s>",
                    privateEnum.getPrivateType(), privateEnum.getCompasClass().getName(), compasElement.getClass().getName()));
            }
        }
        return result;
    }

    public static <T> List<T> getCompasPrivates(TBaseElement baseElement, Class<T> compasClass) throws ScdException {
        if (!baseElement.isSetPrivate()) {
            return Collections.emptyList();
        }
        return getCompasPrivates(baseElement.getPrivate(), compasClass);
    }

    public static <T> Optional<T> getCompasPrivate(TPrivate tPrivate, Class<T> compasClass) throws ScdException {
        List<T> compasPrivates = getCompasPrivates(Collections.singletonList(tPrivate), compasClass);
        if (compasPrivates.size() > 1) {
            throw new ScdException(String.format("Expecting maximum 1 element of type %s in private %s, but got %d",
                compasClass.getName(), tPrivate.getType(), compasPrivates.size()));
        }
        if (compasPrivates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(compasPrivates.get(0));
    }

    public static Optional<TCompasICDHeader> getCompasICDHeader(TPrivate tPrivate) throws ScdException {
        return getCompasPrivate(tPrivate, TCompasICDHeader.class);
    }

    public static TPrivate createPrivate(Object compasElement) {
        String privateType = PrivateEnum.fromClass(compasElement.getClass()).getPrivateType();
        JAXBElement<Object> privateContent = PrivateEnum.createJaxbElement(compasElement);
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType(privateType);
        tPrivate.getContent().add(privateContent);
        return tPrivate;
    }

    public static void removePrivates(TBaseElement baseElement, @NonNull PrivateEnum privateEnum) {
        if (baseElement.isSetPrivate()) {
            baseElement.getPrivate().removeIf(tPrivate -> privateEnum.getPrivateType().equals(tPrivate.getType()));
            if (baseElement.getPrivate().isEmpty()) {
                baseElement.unsetPrivate();
            }
        }
    }

}
