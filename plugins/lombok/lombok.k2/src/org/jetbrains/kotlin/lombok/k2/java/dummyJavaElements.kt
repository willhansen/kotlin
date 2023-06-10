/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lombok.k2.java

import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.lombok.utils.LombokNames
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object JavaClasses {
    konst Object = DummyJavaClass("Object", javaLangName("Object"), numberOfTypeParameters = 0)
    konst Iterable = DummyJavaClass("Iterable", javaLangName("Iterable"), numberOfTypeParameters = 1)
    konst Collection = DummyJavaClass("Collection", javaUtilName("Collection"), numberOfTypeParameters = 1)
    konst Map = DummyJavaClass("Map", javaUtilName("Map"), numberOfTypeParameters = 2)
    konst Table = DummyJavaClass("Table", LombokNames.TABLE, numberOfTypeParameters = 3)


    private fun javaUtilName(name: String): FqName {
        return FqName.fromSegments(listOf("java", "util", name))
    }

    private fun javaLangName(name: String): FqName {
        return FqName.fromSegments(listOf("java", "lang", name))
    }
}

class DummyJavaClass(name: String, override konst fqName: FqName, numberOfTypeParameters: Int) : JavaClass {
    override konst name: Name = Name.identifier(name)

    override konst isFromSource: Boolean
        get() = shouldNotBeCalled()
    override konst annotations: Collection<JavaAnnotation>
        get() = shouldNotBeCalled()
    override konst isDeprecatedInJavaDoc: Boolean
        get() = shouldNotBeCalled()

    override fun findAnnotation(fqName: FqName): JavaAnnotation? {
        return null
    }

    override konst isAbstract: Boolean
        get() = shouldNotBeCalled()
    override konst isStatic: Boolean
        get() = shouldNotBeCalled()
    override konst isFinal: Boolean
        get() = shouldNotBeCalled()
    override konst visibility: Visibility
        get() = shouldNotBeCalled()
    override konst typeParameters: List<JavaTypeParameter> = (1..numberOfTypeParameters).map {
        DummyJavaTypeParameter(Name.identifier("T_$it"))
    }

    override konst supertypes: Collection<JavaClassifierType>
        get() = shouldNotBeCalled()
    override konst innerClassNames: Collection<Name>
        get() = shouldNotBeCalled()

    override fun findInnerClass(name: Name): JavaClass? {
        shouldNotBeCalled()
    }

    override konst outerClass: JavaClass?
        get() = null
    override konst isInterface: Boolean
        get() = shouldNotBeCalled()
    override konst isAnnotationType: Boolean
        get() = shouldNotBeCalled()
    override konst isEnum: Boolean
        get() = shouldNotBeCalled()
    override konst isRecord: Boolean
        get() = shouldNotBeCalled()
    override konst isSealed: Boolean
        get() = shouldNotBeCalled()
    override konst permittedTypes: Collection<JavaClassifierType>
        get() = shouldNotBeCalled()
    override konst lightClassOriginKind: LightClassOriginKind?
        get() = shouldNotBeCalled()
    override konst methods: Collection<JavaMethod>
        get() = shouldNotBeCalled()
    override konst fields: Collection<JavaField>
        get() = shouldNotBeCalled()
    override konst constructors: Collection<JavaConstructor>
        get() = shouldNotBeCalled()
    override konst recordComponents: Collection<JavaRecordComponent>
        get() = shouldNotBeCalled()

    override fun hasDefaultConstructor(): Boolean {
        shouldNotBeCalled()
    }
}

class DummyJavaTypeParameter(override konst name: Name) : JavaTypeParameter {
    override konst isFromSource: Boolean
        get() = shouldNotBeCalled()
    override konst annotations: Collection<JavaAnnotation>
        get() = shouldNotBeCalled()
    override konst isDeprecatedInJavaDoc: Boolean
        get() = shouldNotBeCalled()

    override fun findAnnotation(fqName: FqName): JavaAnnotation? {
        shouldNotBeCalled()
    }

    override konst upperBounds: Collection<JavaClassifierType>
        get() = emptyList()
}

private fun shouldNotBeCalled(): Nothing = error("should not be called")
