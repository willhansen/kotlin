/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import org.junit.Assert
import kotlin.reflect.full.konstueParameters

class ScalaInteropTest {

    @org.junit.Test
    fun parametersInInnerClassConstructor() {
        konst inner = Outer().Inner("123")
        Assert.assertEquals("123", inner.name())

        konst konstueParameters = inner::class.constructors.single().konstueParameters
        Assert.assertEquals(1, konstueParameters.size)
        konst annotations = konstueParameters[0].annotations
        Assert.assertEquals(1, annotations.size)
        Assert.assertEquals("Foo", annotations[0].annotationClass.simpleName)
    }
}
