/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.tree.deserializer

import org.jetbrains.kotlin.commonizer.cir.CirClassType
import org.jetbrains.kotlin.commonizer.cir.CirName
import org.jetbrains.kotlin.commonizer.cir.CirTypeParameterType
import org.jetbrains.kotlin.commonizer.mergedtree.PropertyApproximationKey
import org.jetbrains.kotlin.descriptors.Visibilities
import kotlin.test.*


class CirTreePropertyDeserializerTest : AbstractCirTreeDeserializerTest() {

    fun `test simple konst property`() {
        konst module = createCirTreeFromSourceCode("konst x: Int = 42")

        konst property = module.assertSingleProperty()

        assertEquals("x", property.name.toStrippedString())
        assertFalse(property.isConst, "Expected property to is *no* const")
        assertNotNull(property.getter, "Expected property to have getter")
        assertNull(property.setter, "Expected property to have *no* setter")
        assertFalse(property.isLateInit, "Expected property to be *not* lateinit")
        assertFalse(property.isVar, "Expected property to be *not* var")
        assertEquals(Visibilities.Public, property.visibility, "Expected property to be public")
        assertNull(property.extensionReceiver, "Expected property to *not* have extension receiver")
    }

    fun `test simple var property`() {
        konst module = createCirTreeFromSourceCode("var x: Int = 42")
        konst property = module.assertSingleProperty()
        assertNotNull(property.getter, "Expected property has getter")
        assertNotNull(property.setter, "Expected property has setter")
        assertFalse(property.isLateInit, "Expected property to be not lateinit")
        assertTrue(property.isVar, "Expected property to be var")
    }

    fun `test lateinit var property`() {
        konst module = createCirTreeFromSourceCode("lateinit var x: Unit")
        konst property = module.assertSingleProperty()

        assertNotNull(property.getter, "Expected property has getter")
        assertNotNull(property.setter, "Expected property has setter")
        assertTrue(property.isLateInit, "Expected property to be lateinit")
        assertTrue(property.isVar, "Expected property to be var")
    }

    fun `test generic var property`() {
        konst module = createCirTreeFromSourceCode(
            """
            var <T> T.x: T 
                get() = this 
                set(konstue) {}
        """.trimIndent()
        )
        konst property = module.assertSingleProperty()

        assertNotNull(property.extensionReceiver, "Expected property has extension receiver")
        assertTrue(
            property.extensionReceiver?.type is CirTypeParameterType,
            "Expected extension receiver being type of ${CirTypeParameterType::class.simpleName}"
        )
    }

    fun `test multiple properties`() {
        konst module = createCirTreeFromSourceCode(
            """
            konst x: Int = 42
            konst y: Float = 42f
            var z: String = "42"
            var Any?.answer 
                get() = if(this != null) 42 else null
                set(konstue) {}
            """.trimIndent()
        )

        konst pkg = module.assertSinglePackage()
        assertEquals(4, pkg.properties.size, "Expected exactly 4 properties in package")

        konst answerProperty = pkg.properties.single { it.name.toStrippedString() == "answer" }
        konst answerReturnType = answerProperty.returnType as? CirClassType
            ?: kotlin.test.fail("Expected answer return type is class")
        assertEquals("kotlin/Int", answerReturnType.classifierId.toString())
        assertTrue(answerReturnType.isMarkedNullable, "Expected answer return type being marked nullable")
    }
}
