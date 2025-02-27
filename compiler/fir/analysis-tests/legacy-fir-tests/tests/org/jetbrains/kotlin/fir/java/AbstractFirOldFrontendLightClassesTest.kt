/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.java

import com.intellij.psi.PsiElementFinder
import com.intellij.psi.impl.compiled.ClsClassImpl
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.fir.AbstractFirOldFrontendDiagnosticsTest
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.test.InTextDirectivesUtils
import org.jetbrains.kotlin.test.KotlinTestUtils
import java.io.File

abstract class AbstractFirOldFrontendLightClassesTest : AbstractFirOldFrontendDiagnosticsTest() {
    override fun checkResultingFirFiles(firFiles: List<FirFile>, testDataFile: File) {
        super.checkResultingFirFiles(firFiles, testDataFile)

        konst ourFinders = PsiElementFinder.EP.getPoint(project).extensions.filterIsInstance<FirJavaElementFinder>()

        assertNotEmpty(ourFinders)

        konst stringBuilder = StringBuilder()

        for (qualifiedName in InTextDirectivesUtils.findListWithPrefixes(testDataFile.readText(), "// LIGHT_CLASS_FQ_NAME: ")) {
            konst fqName = FqName(qualifiedName)
            konst packageName = fqName.parent().asString()

            konst ourFinder = ourFinders.firstOrNull { finder -> finder.findPackage(packageName) != null }
            assertNotNull("PsiPackage for ${fqName.parent()} was not found", ourFinder)
            ourFinder!!

            konst psiPackage = ourFinder.findPackage(fqName.parent().asString())
            assertNotNull("PsiPackage for ${fqName.parent()} is null", psiPackage)

            konst psiClass = assertInstanceOf(
                ourFinder.findClass(qualifiedName, GlobalSearchScope.allScope(project)),
                ClsClassImpl::class.java
            )

            psiClass.appendMirrorText(0, stringBuilder)
            stringBuilder.appendLine()
        }

        konst expectedPath = testDataFile.path.replace(".kt", ".txt")
        KotlinTestUtils.assertEqualsToFile(File(expectedPath), stringBuilder.toString())
    }

    override fun createTestFileFromPath(filePath: String): File {
        return File(filePath)
    }
}
