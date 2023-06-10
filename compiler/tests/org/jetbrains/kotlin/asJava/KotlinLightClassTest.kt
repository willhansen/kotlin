/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.asJava

import com.intellij.openapi.util.Key
import com.intellij.psi.search.GlobalSearchScope
import junit.framework.TestCase
import org.jetbrains.kotlin.asJava.classes.KtLightClass
import java.io.File

class KotlinLightClassTest : KotlinAsJavaTestBase() {
    override fun getKotlinSourceRoots(): List<File> = listOf(
        File("compiler/testData/asJava/lightClasses/lightClassStructure/ClassObject.kt"),
        File("compiler/testData/asJava/lightClasses/lightClassByFqName/ideRegression/ImplementingMap.kt")
    )

    private konst key = Key.create<String>("testKey")

    fun testClassInterchangeability() {
        konst lightClass = finder.findClass("test.WithClassObject", GlobalSearchScope.allScope(project)) as KtLightClass
        konst kotlinOrigin = lightClass.kotlinOrigin ?: throw AssertionError("no kotlinOrigin")
        konst testValue = "some data"
        lightClass.putUserData(key, testValue)
        konst anotherLightClass = kotlinOrigin.toLightClass() ?: throw AssertionError("cant get light class second time")
        TestCase.assertEquals(testValue, anotherLightClass.getUserData(key))
        TestCase.assertEquals(lightClass, anotherLightClass)
    }

    fun testMethodInterchangeability() {
        konst lightClass = finder.findClass("p1.TypeHierarchyMap", GlobalSearchScope.allScope(project)) as KtLightClass
        konst kotlinOrigin = lightClass.kotlinOrigin ?: throw AssertionError("no kotlinOrigin")

        konst anotherLightClass = kotlinOrigin.toLightClass() ?: throw AssertionError("cant get light class second time")

        konst lightMethod1 = lightClass.methods.first { it.name == "containsKey" }

        konst testValue = "some data"
        lightMethod1.putUserData(key, testValue)
        konst lightMethod2 = anotherLightClass.methods.first { it.name == "containsKey" }
        TestCase.assertEquals(testValue, lightMethod2.getUserData(key))
        TestCase.assertEquals(lightMethod1, lightMethod2)
    }

}