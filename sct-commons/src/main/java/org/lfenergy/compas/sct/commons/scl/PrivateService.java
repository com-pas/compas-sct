// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.NonNull;
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

/**
 * A representation of the <em><b>{@link PrivateService PrivateService}</b></em>.
 * <p>
 * The following features are supported:
 * </p>
 * <ol>
 *  <li>{@link PrivateService#extractCompasPrivate(TPrivate, Class)
 *      <em>Returns the value of the <b>TPrivate </b> reference object By class type</em>}</li>
 *
 *  <li>{@link PrivateService#extractCompasPrivates(TBaseElement, Class)
 *      <em>Returns the value of the <b>TPrivate </b> containment reference list from given <b>TBaseElement </b> By class type</em>}</li>
 *
 *  <li>{@link PrivateService#extractCompasPrivates(List, Class)
 *      <em>Returns the value of the <b>TPrivate </b> containment reference list from given <b>TPrivate </b> elements By class type</em>}
 *   </li>
 * </ol>
 * @see org.lfenergy.compas.scl2007b4.model.TPrivate
 */
public final class PrivateService {

    private PrivateService() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static final ObjectFactory objectFactory = new ObjectFactory();

    /**
     * Extract compas element of class <em>compasClass</em> nested in private elements.
     * @param tPrivates list of privates to look in
     * @param compasClass class of privates to extract
     * @return list of compas objects nested in the privates.
     * @param <T> Inference parameter stands for class <em>compasClass</em>
     * @throws ScdException throws when inconsistency between types
     */
    public static <T> List<T> extractCompasPrivates(List<TPrivate> tPrivates, Class<T> compasClass) throws ScdException {
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
     * Extract compas elements of class <em>compasClass</em> nested in private elements of the given baseElement.
     * @param baseElement element where to look for privates
     * @param compasClass class of privates to extract
     * @return list of compas objects nested in the privates.
     * @param <T> Inference parameter stands for class <em>compasClass</em>
     * @throws ScdException throws when inconsistency between types
     */
    public static <T> List<T> extractCompasPrivates(TBaseElement baseElement, Class<T> compasClass) throws ScdException {
        if (!baseElement.isSetPrivate()) {
            return Collections.emptyList();
        }
        return extractCompasPrivates(baseElement.getPrivate(), compasClass);
    }

    /**
     * Extract a single compas element of class <em>compasClass</em> nested in a private element.
     * Throws an exception when there are more than 1 compas element of given <em>compasClass</em> nested inside the private element.
     * @param tPrivate private where to look in
     * @param compasClass class of privates to extract
     * @return list of compas objects nested in the privates.
     * @param <T> Inference parameter stands for class <em>compasClass</em>
     * @throws ScdException throws when inconsistency between types, or when more than 1 compas element is found
     */
    public static <T> Optional<T> extractCompasPrivate(TPrivate tPrivate, Class<T> compasClass) throws ScdException {
        List<T> compasPrivates = extractCompasPrivates(Collections.singletonList(tPrivate), compasClass);
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
     * Extract a single compas element of class <em>compasClass</em> nested in private elements of a given baseElement.
     * Throws an exception when there are more than 1 compas element of given <em>compasClass</em> nested inside privates of the baseElement.
     * @param baseElement element where to look for privates
     * @param compasClass class of privates to extract
     * @return list of compas objects nested in the privates.
     * @param <T> Inference parameter stands for class <em>compasClass</em>
     * @throws ScdException throws when inconsistency between types, or when more than 1 compas element is found
     */
    public static <T> Optional<T> extractCompasPrivate(TBaseElement baseElement, Class<T> compasClass) throws ScdException {
        List<T> compasPrivates = extractCompasPrivates(baseElement, compasClass);
        if (compasPrivates.size() > 1) {
            throw new ScdException(String.format("Expecting maximum 1 private of type %s with 1 element, but found %d",
                PrivateEnum.fromClass(compasClass).getPrivateType(), compasPrivates.size()));
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
    public static Optional<TCompasICDHeader> extractCompasICDHeader(TPrivate tPrivate) throws ScdException {
        return extractCompasPrivate(tPrivate, TCompasICDHeader.class);
    }

    /**
     * Removes all privates of type <em>privateEnum</em> from <em>baseElement</em>
     * @param baseElement baseElement containing privates
     * @param privateEnum enum type of private to remove
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
