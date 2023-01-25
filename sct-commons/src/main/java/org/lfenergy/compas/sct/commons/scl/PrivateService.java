// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl;

import lombok.NonNull;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.exception.ScdException;
import org.lfenergy.compas.sct.commons.util.PrivateEnum;

import javax.xml.bind.JAXBElement;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.lfenergy.compas.sct.commons.util.CommonConstants.*;
import static org.lfenergy.compas.sct.commons.util.PrivateEnum.COMPAS_ICDHEADER;

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
            .map(JAXBElement::getValue).toList();

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


    /**
     * Sorts in map of ICD_SYSTEM_VERSION_UUID and related Private coupled with all corresponding STD for all given STD
     *
     * @param stds list of STD to short
     * @return map of ICD_SYSTEM_VERSION_UUID attribute in IED/Private:COMPAS-ICDHeader and related Private coupled with
     * all corresponding STD
     */
    public static Map<String, PrivateLinkedToSTDs> createMapICDSystemVersionUuidAndSTDFile(Set<SCL> stds) {
        Map<String, PrivateLinkedToSTDs> stringSCLMap = new HashMap<>();
        stds.forEach(std -> std.getIED().forEach(ied -> ied.getPrivate().forEach(tp ->
                PrivateService.extractCompasICDHeader(tp).map(TCompasICDHeader::getICDSystemVersionUUID).ifPresent(icdSysVer -> {
                    PrivateLinkedToSTDs privateLinkedToSTDs = stringSCLMap.get(icdSysVer);
                    List<SCL> list = privateLinkedToSTDs != null ? privateLinkedToSTDs.stdList() : new ArrayList<>();
                    list.add(std);
                    stringSCLMap.put(icdSysVer, new PrivateLinkedToSTDs(tp, list));
                })
        )));
        return stringSCLMap;
    }


    public record PrivateLinkedToSTDs (TPrivate tPrivate, List<SCL> stdList) {
    }


    /**
     * Checks SCD and STD compatibilities by checking if there is at least one ICD_SYSTEM_VERSION_UUID in
     * Substation/../LNode/Private COMPAS-ICDHeader of SCL not present in IED/Private COMPAS-ICDHeader of STD
     *
     * @param mapICDSystemVersionUuidAndSTDFile map of ICD_SYSTEM_VERSION_UUID and list of corresponding STD
     * @throws ScdException throws when there are several STD files corresponding to <em>ICD_SYSTEM_VERSION_UUID</em>
     *                      from Substation/../LNode/Private COMPAS-ICDHeader of SCL
     */
    public static void checkSTDCorrespondanceWithLNodeCompasICDHeader(Map<String, PrivateLinkedToSTDs> mapICDSystemVersionUuidAndSTDFile) throws ScdException {
        mapICDSystemVersionUuidAndSTDFile.values().stream()
                .filter(privateLinkedToSTDs -> privateLinkedToSTDs.stdList().size() != 1)
                .findFirst()
                .ifPresent(pToStd -> {
                    throw new ScdException("There are several STD files corresponding to " + stdCheckFormatExceptionMessage(pToStd.tPrivate()));
                });
    }

    /**
     * Creates formatted message including data's of Private for Exception
     *
     * @param key Private causing exception
     * @return formatted message
     * @throws ScdException throws when parameter not present in Private
     */
    public static String stdCheckFormatExceptionMessage(TPrivate key) throws ScdException {
        Optional<TCompasICDHeader> optionalCompasICDHeader = PrivateService.extractCompasICDHeader(key);
        return  HEADER_ID + " = " + optionalCompasICDHeader.map(TCompasICDHeader::getHeaderId).orElse(null) + " " +
                HEADER_VERSION + " = " + optionalCompasICDHeader.map(TCompasICDHeader::getHeaderVersion).orElse(null) + " " +
                HEADER_REVISION + " = " + optionalCompasICDHeader.map(TCompasICDHeader::getHeaderRevision).orElse(null) +
                " and " + ICD_SYSTEM_VERSION_UUID + " = " + optionalCompasICDHeader.map(TCompasICDHeader::getICDSystemVersionUUID).orElse(null);
    }


    /**
     * Creates map of IEDName and related Private for all Privates COMPAS-ICDHeader in /Substation of SCL
     *
     * @param scdRootAdapter SCL file in which Private should be found
     * @return map of Private and its IEDName parameter
     */
    public static Stream<TPrivate> createMapIEDNameAndPrivate(SclRootAdapter scdRootAdapter) {
        return scdRootAdapter.getCurrentElem().getSubstation().get(0).getVoltageLevel().stream()
                .map(TVoltageLevel::getBay).flatMap(Collection::stream)
                .map(TBay::getFunction).flatMap(Collection::stream)
                .map(TFunction::getLNode).flatMap(Collection::stream)
                .map(TLNode::getPrivate).flatMap(Collection::stream)
                .filter(tPrivate ->
                        tPrivate.getType().equals(COMPAS_ICDHEADER.getPrivateType())
                                && PrivateService.extractCompasICDHeader(tPrivate).isPresent()
                                && PrivateService.extractCompasICDHeader(tPrivate).get().getIEDName() != null);
    }


    /**
     * Compares if two Private:COMPAS-ICDHeader have all attributes equal except IEDNane, BayLabel and IEDinstance
     *
     * @param iedPrivate Private of IED from STD to compare
     * @param scdPrivate Private of LNode fro SCD to compare
     * @return <em>Boolean</em> value of check result
     * @throws ScdException throws when Private is not COMPAS_ICDHEADER one
     */
    public static boolean comparePrivateCompasICDHeaders(TPrivate iedPrivate, TPrivate scdPrivate) throws ScdException {
        TCompasICDHeader iedCompasICDHeader = PrivateService.extractCompasICDHeader(iedPrivate)
                .orElseThrow(() -> new ScdException(COMPAS_ICDHEADER + "not found in IED Private "));
        TCompasICDHeader scdCompasICDHeader = PrivateService.extractCompasICDHeader(scdPrivate)
                .orElseThrow(() -> new ScdException(COMPAS_ICDHEADER + "not found in LNode Private "));
        return Objects.equals(iedCompasICDHeader.getIEDType(), scdCompasICDHeader.getIEDType())
                && Objects.equals(iedCompasICDHeader.getICDSystemVersionUUID(), scdCompasICDHeader.getICDSystemVersionUUID())
                && Objects.equals(iedCompasICDHeader.getVendorName(), scdCompasICDHeader.getVendorName())
                && Objects.equals(iedCompasICDHeader.getIEDredundancy(), scdCompasICDHeader.getIEDredundancy())
                && Objects.equals(iedCompasICDHeader.getIEDmodel(), scdCompasICDHeader.getIEDmodel())
                && Objects.equals(iedCompasICDHeader.getHwRev(), scdCompasICDHeader.getHwRev())
                && Objects.equals(iedCompasICDHeader.getSwRev(), scdCompasICDHeader.getSwRev())
                && Objects.equals(iedCompasICDHeader.getHeaderId(), scdCompasICDHeader.getHeaderId())
                && Objects.equals(iedCompasICDHeader.getHeaderRevision(), scdCompasICDHeader.getHeaderRevision())
                && Objects.equals(iedCompasICDHeader.getHeaderVersion(), scdCompasICDHeader.getHeaderVersion());
    }

    /**
     * Copy Private COMPAS_ICDHEADER from LNode of SCD into Private COMPAS_ICDHEADER from IED of STD
     *
     * @param stdPrivate   Private of IED from STD in which to copy new data
     * @param lNodePrivate Private of IED from STD from which new data are taken
     * @throws ScdException throws when Private is not COMPAS_ICDHEADER one
     */
    public static void copyCompasICDHeaderFromLNodePrivateIntoSTDPrivate(TPrivate stdPrivate, TPrivate lNodePrivate) throws ScdException {
        TCompasICDHeader lNodeCompasICDHeader = extractCompasICDHeader(lNodePrivate)
                .orElseThrow(() -> new ScdException(COMPAS_ICDHEADER + " not found in LNode Private "));
        stdPrivate.getContent().clear();
        stdPrivate.getContent().add(objectFactory.createICDHeader(lNodeCompasICDHeader));
    }


    }
