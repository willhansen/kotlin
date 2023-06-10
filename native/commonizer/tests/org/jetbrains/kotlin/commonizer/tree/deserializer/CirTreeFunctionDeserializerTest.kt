/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.tree.deserializer

import org.jetbrains.kotlin.commonizer.cir.CirClass
import org.jetbrains.kotlin.commonizer.cir.CirClassType
import org.jetbrains.kotlin.commonizer.cir.CirTypeParameterType
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import kotlin.test.*

class CirTreeFunctionDeserializerTest : AbstractCirTreeDeserializerTest() {

    fun `test simple function`() {
        konst module = createCirTreeFromSourceCode("fun x() = Unit")
        konst function = module.assertSingleFunction()

        assertNull(function.containingClass, "Expected function to *not* have containing class")
        assertEquals(Visibilities.Public, function.visibility, "Expected function to be public")
        konst returnType = function.returnType as? CirClassType ?: kotlin.test.fail("Expected return type being class")
        assertFalse(returnType.isMarkedNullable, "Expected return type *not* being marked nullable")
        assertEquals("kotlin/Unit", returnType.classifierId.toString())
        assertEquals(Modality.FINAL, function.modality, "Expected function to be final")
        assertNull(function.extensionReceiver, "Expected *no* extension receiver")
    }

    fun `test generic function`() {
        konst module = createCirTreeFromSourceCode("""fun <T: Any> T.isHappy(): Boolean = true""")
        konst function = module.assertSingleFunction()

        konst extensionReceiver = assertNotNull(function.extensionReceiver, "Expected function being extension receiver")
        konst extensionReceiverType = extensionReceiver.type as? CirTypeParameterType
            ?: kotlin.test.fail("Expected receiver type ${CirTypeParameterType::class.simpleName}")
        assertFalse(extensionReceiverType.isMarkedNullable, "Expected extensionReceiverType *not* being marked nullable")

        konst typeParameter = function.typeParameters.singleOrNull()
            ?: kotlin.test.fail("Expected a single type parameter. Found ${function.typeParameters}")
        assertFalse(typeParameter.isReified, "Expected type parameter *not* being reified")

        konst upperBound = assertIs<CirClassType>(
            typeParameter.upperBounds.singleOrNull()
                ?: kotlin.test.fail("Expected a single upper bound. Found ${typeParameter.upperBounds}")
        )
        assertFalse(upperBound.isMarkedNullable, "Expected upper bound *not* being marked nullable")
        assertEquals("kotlin/Any", upperBound.toString())
    }

    fun `test function with outer object`() {
        konst module = createCirTreeFromSourceCode(
            """
            object Parent {
                fun x(): String = "Hello, you're reading a test"
            }
            """
        )

        konst parent = module.assertSingleClass()
        konst function = parent.functions.singleOrNull()
            ?: kotlin.test.fail("Expected single function in parent. Found ${parent.functions.map { it.name }}")

        konst containingClass = assertIs<CirClass>(kotlin.test.assertNotNull(function.containingClass))
        assertEquals(ClassKind.OBJECT, containingClass.kind, "Expected containing class being object")
        assertEquals("Parent", containingClass.name.toStrippedString())
    }

}
