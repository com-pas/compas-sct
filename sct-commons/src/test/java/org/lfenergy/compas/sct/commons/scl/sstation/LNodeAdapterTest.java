// SPDX-FileCopyrightText: 2022 RTE FRANCE
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.sct.commons.scl.sstation;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.lfenergy.compas.scl2007b4.model.*;
import org.lfenergy.compas.sct.commons.scl.PrivateService;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LNodeAdapterTest {

    @Test
    void constructor_should_create_LNode_with_no_parents() {
        // Given
        TLNode tlNode = new TLNode();
        // When
        LNodeAdapter lNodeAdapter = new LNodeAdapter(null, tlNode);
        // Then
        assertThat(lNodeAdapter.getParentAdapter()).isNull();
    }

    @Test
    void constructor_when_LNode_is_child_of_parent_should_succeed() {
        // Given
        TLNode tlNode = new TLNode();
        TFunction tFunction = new TFunction();
        tFunction.getLNode().add(tlNode);
        FunctionAdapter functionAdapter = new FunctionAdapter(null, tFunction);
        // When
        LNodeAdapter lNodeAdapter = new LNodeAdapter(functionAdapter, tlNode);
        // Then
        assertThat(lNodeAdapter.getParentAdapter()).isNotNull();
    }

    @Test
    void constructor_when_LNode_is_not_child_of_parent_should_throw_exception() {
        // Given
        TLNode tlNode = new TLNode();
        TFunction tFunction = new TFunction();
        FunctionAdapter functionAdapter = new FunctionAdapter(null, tFunction);
        // When & Then
        assertThatThrownBy(() -> new LNodeAdapter(functionAdapter, tlNode)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addPrivate_should_succeed() {
        // Given
        TLNode tlNode = new TLNode();
        LNodeAdapter lNodeAdapter = new LNodeAdapter(null, tlNode);
        TPrivate tPrivate = PrivateService.createPrivate(TCompasSclFileType.SCD);
        // When
        lNodeAdapter.addPrivate(tPrivate);
        // Then
        assertThat(tlNode.getPrivate()).hasSize(1);
        assertThat(tlNode.getPrivate().get(0).getContent()).hasSize(1).first().isInstanceOf(JAXBElement.class);
        JAXBElement<?> jaxbElement = (JAXBElement<?>) tlNode.getPrivate().get(0).getContent().get(0);
        assertThat(jaxbElement.getValue()).isEqualTo(TCompasSclFileType.SCD);
    }

    @Test
    void deepCopy_should_succeed() {
        // Given
        TLNode tlNode = new TLNode();
        tlNode.setIedName("iedName1");
        tlNode.setLdInst("ldInst1");
        tlNode.setPrefix("prefix1");
        tlNode.getLnClass().add("lnClass1");
        tlNode.setLnInst("lnInst1");
        tlNode.setLnType("lnType1");
        tlNode.setDesc("Desc1");
        tlNode.getAny().add("any1");
        TText tText = new ObjectFactory().createTText();
        tText.setSource("Text1");
        tText.getContent().add("Text1");
        tText.getOtherAttributes().put(QName.valueOf("Text1_attribute1"), "Text1_value1");
        tlNode.setText(tText);
        tlNode.getPrivate().add(PrivateService.createPrivate(TCompasSclFileType.SCD));
        tlNode.getOtherAttributes().put(QName.valueOf("tlNode_attribute1"), "tlNode_value1");
        LNodeAdapter lNodeAdapter = new LNodeAdapter(null, tlNode);
        // When
        TLNode result = lNodeAdapter.deepCopy();
        // Then
        assertThat(result).usingRecursiveComparison().isEqualTo(tlNode);
        List<Field> fields = ReflectionSupport.findFields(TLNode.class, field -> true, HierarchyTraversalMode.BOTTOM_UP);
        assertThat(fields).isNotEmpty().allSatisfy(
            field -> {
                Optional<Object> optionalValue = ReflectionSupport.tryToReadFieldValue(field, result).toOptional();
                assertThat(optionalValue).isPresent();
                Object value = optionalValue.get();
                if (value instanceof Collection) {
                    assertThat((Collection<?>) value).isNotEmpty();
                } else {
                    assertThat(value).isNotNull();
                }
            }
        );
    }
}
