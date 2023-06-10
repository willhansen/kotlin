/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.light.classes.symbol.base;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.analysis.api.GenerateAnalysisApiTestsKt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("analysis/symbol-light-classes/testData/equikonstentTo")
@TestDataPath("$PROJECT_ROOT")
public class SymbolLightClassesEquikonstentTestGenerated extends AbstractSymbolLightClassesEquikonstentTest {
    @Test
    public void testAllFilesPresentInEquikonstentTo() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/symbol-light-classes/testData/equikonstentTo"), Pattern.compile("^(.+)\\.(kt)$"), null, true);
    }

    @Test
    @TestMetadata("AnnotationClass.kt")
    public void testAnnotationClass() throws Exception {
        runTest("analysis/symbol-light-classes/testData/equikonstentTo/AnnotationClass.kt");
    }

    @Test
    @TestMetadata("AnonymousClass.kt")
    public void testAnonymousClass() throws Exception {
        runTest("analysis/symbol-light-classes/testData/equikonstentTo/AnonymousClass.kt");
    }

    @Test
    @TestMetadata("CompanionProperty.kt")
    public void testCompanionProperty() throws Exception {
        runTest("analysis/symbol-light-classes/testData/equikonstentTo/CompanionProperty.kt");
    }

    @Test
    @TestMetadata("Constructor.kt")
    public void testConstructor() throws Exception {
        runTest("analysis/symbol-light-classes/testData/equikonstentTo/Constructor.kt");
    }

    @Test
    @TestMetadata("EnumClassEntry.kt")
    public void testEnumClassEntry() throws Exception {
        runTest("analysis/symbol-light-classes/testData/equikonstentTo/EnumClassEntry.kt");
    }

    @Test
    @TestMetadata("Function.kt")
    public void testFunction() throws Exception {
        runTest("analysis/symbol-light-classes/testData/equikonstentTo/Function.kt");
    }

    @Test
    @TestMetadata("Interface.kt")
    public void testInterface() throws Exception {
        runTest("analysis/symbol-light-classes/testData/equikonstentTo/Interface.kt");
    }

    @Test
    @TestMetadata("Klass.kt")
    public void testKlass() throws Exception {
        runTest("analysis/symbol-light-classes/testData/equikonstentTo/Klass.kt");
    }

    @Test
    @TestMetadata("TypeParameter.kt")
    public void testTypeParameter() throws Exception {
        runTest("analysis/symbol-light-classes/testData/equikonstentTo/TypeParameter.kt");
    }
}
