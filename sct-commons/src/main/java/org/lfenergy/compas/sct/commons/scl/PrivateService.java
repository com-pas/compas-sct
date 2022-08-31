// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.lfenergy.compas.scl2007b4.model.*;
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

    private static final ObjectFactory objectFactory = new ObjectFactory();

    /**
     * Converts each item of given list of Private to list of Private element of type <em>compasClass</em>
     * @param tPrivates list of private to convert
     * @param compasClass type in which Privates should be
     * @return list of formatted Private
     * @param <T> Inference parameter stands for wanted type of Privates
     * @throws ScdException throws when inconsistency between types
     */
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

    /**
     * Converts all Private of given <em>TBaseElement</em> of type <em>compasClass</em> given as parameter
     * @param baseElement TBaseElement contenting Privates
     * @param compasClass type in which Privates should be given
     * @return list of formatted Private
     * @param <T> Inference parameter stands for wanted type of Privates
     * @throws ScdException throws when inconsistency between types
     */
    public static <T> List<T> getCompasPrivates(TBaseElement baseElement, Class<T> compasClass) throws ScdException {
        if (!baseElement.isSetPrivate()) {
            return Collections.emptyList();
        }
        return getCompasPrivates(baseElement.getPrivate(), compasClass);
    }

    /**
     * Converts Private of type <em>compasClass</em> given as parameter
     * @param tPrivate Private to check if is in wanted type
     * @param compasClass type in which Privates should be given
     * @return optional of formatted Private
     * @param <T> Inference parameter stands for wanted type of Private
     * @throws ScdException throws when inconsistency between types
     */
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

    /**
     * Gets Private CompasICDHeader from Private
     * @param tPrivate Private contenting object to get as CompasICDHeader
     * @return content of th Private as optional of <em>TCompasICDHeader</em> object
     * @throws ScdException throws when inconsistency between types
     */
    public static Optional<TCompasICDHeader> getCompasICDHeader(TPrivate tPrivate) throws ScdException {
        return getCompasPrivate(tPrivate, TCompasICDHeader.class);
    }

    /**
     * Removes specified Private from <em>TBaseElement</em> Privates
     * @param baseElement BaseElement contenting Privates
     * @param privateEnum enum type of Private to remove
     */
    public static void removePrivates(TBaseElement baseElement, @NonNull PrivateEnum privateEnum) {
        if (baseElement.isSetPrivate()) {
            baseElement.getPrivate().removeIf(tPrivate -> privateEnum.getPrivateType().equals(tPrivate.getType()));
            if (baseElement.getPrivate().isEmpty()) {
                baseElement.unsetPrivate();
            }
        }
    }

    /**
     * Create Private of given type as parameter
     * @param compasBay type of Private to create
     * @return created Private
     */
    public static TPrivate createPrivate(TCompasBay compasBay) {
        return createPrivate(objectFactory.createBay(compasBay));
    }

    /**
     * Create Private of given type as parameter
     * @param compasCriteria type of Private to create
     * @return created Private
     */
    public static TPrivate createPrivate(TCompasCriteria compasCriteria) {
        return createPrivate(objectFactory.createCriteria(compasCriteria));
    }

    /**
     * Create Private of given type as parameter
     * @param compasFlow type of Private to create
     * @return created Private
     */
    public static TPrivate createPrivate(TCompasFlow compasFlow) {
        return createPrivate(objectFactory.createFlow(compasFlow));
    }

    /**
     * Create Private of given type as parameter
     * @param compasFunction type of Private to create
     * @return created Private
     */
    public static TPrivate createPrivate(TCompasFunction compasFunction) {
        return createPrivate(objectFactory.createFunction(compasFunction));
    }

    /**
     * Create Private of given type as parameter
     * @param compasICDHeader type of Private to create
     * @return created Private
     */
    public static TPrivate createPrivate(TCompasICDHeader compasICDHeader) {
        return createPrivate(objectFactory.createICDHeader(compasICDHeader));
    }

    /**
     * Create Private of given type as parameter
     * @param compasLDevice type of Private to create
     * @return created Private
     */
    public static TPrivate createPrivate(TCompasLDevice compasLDevice) {
        return createPrivate(objectFactory.createLDevice(compasLDevice));
    }

    /**
     * Create Private of given type as parameter
     * @param compasSclFileType type of Private to create
     * @return created Private
     */
    public static TPrivate createPrivate(TCompasSclFileType compasSclFileType) {
        return createPrivate(objectFactory.createSclFileType(compasSclFileType));
    }

    /**
     * Create Private of given type as parameter
     * @param compasSystemVersion type of Private to create
     * @return created Private
     */
    public static TPrivate createPrivate(TCompasSystemVersion compasSystemVersion) {
        return createPrivate(objectFactory.createSystemVersion(compasSystemVersion));
    }

    /**
     * Create Private of given type as parameter
     * @param jaxbElement content of Private to create
     * @return created Private
     */
    private static TPrivate createPrivate(JAXBElement<?> jaxbElement) {
        PrivateEnum privateEnum = PrivateEnum.fromClass(jaxbElement.getDeclaredType());
        TPrivate tPrivate = new TPrivate();
        tPrivate.setType(privateEnum.getPrivateType());
        tPrivate.getContent().add(jaxbElement);
        return tPrivate;
    }











    }
