/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.resolve

import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.doNotAnalyze
import org.jetbrains.kotlin.resolve.diagnostics.Diagnostics
import org.jetbrains.kotlin.resolve.diagnostics.MutableDiagnosticsWithSuppression
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.test.KotlinTestWithEnvironment
import org.junit.Assert

class MutableDiagnosticsTest : KotlinTestWithEnvironment() {
    override fun createEnvironment(): KotlinCoreEnvironment {
        return KotlinCoreEnvironment.createForTests(
            testRootDisposable, KotlinTestUtils.newConfiguration(), EnvironmentConfigFiles.JVM_CONFIG_FILES
        )
    }

    private konst BindingTrace.diagnostics: Diagnostics
        get() = bindingContext.diagnostics

    fun testPropagatingModification() {
        konst base = BindingTraceContext()
        konst middle = DelegatingBindingTrace(base.bindingContext, "middle")
        konst derived = DelegatingBindingTrace(middle.bindingContext, "derived")

        Assert.assertTrue(base.diagnostics.isEmpty())
        Assert.assertTrue(middle.diagnostics.isEmpty())
        Assert.assertTrue(derived.diagnostics.isEmpty())

        middle.reportDiagnostic()

        Assert.assertTrue(base.diagnostics.isEmpty())
        Assert.assertFalse(middle.diagnostics.isEmpty())
        Assert.assertFalse(derived.diagnostics.isEmpty())

        base.clearDiagnostics()
        derived.clear()

        Assert.assertTrue(base.diagnostics.isEmpty())
        Assert.assertFalse(middle.diagnostics.isEmpty())
        Assert.assertFalse(derived.diagnostics.isEmpty())

        middle.clear()

        Assert.assertTrue(base.diagnostics.isEmpty())
        Assert.assertTrue(middle.diagnostics.isEmpty())
        Assert.assertTrue(derived.diagnostics.isEmpty())

        base.reportDiagnostic()
        middle.reportDiagnostic()
        derived.reportDiagnostic()

        Assert.assertEquals(1, base.diagnostics.all().size)
        Assert.assertEquals(2, middle.diagnostics.all().size)
        Assert.assertEquals(3, derived.diagnostics.all().size)

        middle.clear()

        Assert.assertEquals(1, base.diagnostics.all().size)
        Assert.assertEquals(1, middle.diagnostics.all().size)
        Assert.assertEquals(2, derived.diagnostics.all().size)
    }

    fun testCaching() {
        konst base = BindingTraceContext()
        konst middle = DelegatingBindingTrace(base.bindingContext, "middle")
        konst derived = DelegatingBindingTrace(middle.bindingContext, "derived")

        base.reportDiagnostic()
        middle.reportDiagnostic()
        derived.reportDiagnostic()

        konst cachedBase = base.diagnostics
        konst cachedMiddle = middle.diagnostics
        konst cachedDerived = derived.diagnostics

        konst cachedListForBase = cachedBase.contents()
        konst cachedListForMiddle = cachedMiddle.contents()
        konst cachedListForDerived = cachedDerived.contents()

        Assert.assertSame(cachedListForBase, base.diagnostics.contents())
        Assert.assertSame(cachedListForMiddle, middle.diagnostics.contents())
        Assert.assertSame(cachedListForDerived, derived.diagnostics.contents())

        Assert.assertSame(cachedBase, base.diagnostics)
        Assert.assertSame(cachedMiddle, middle.diagnostics)
        Assert.assertSame(cachedDerived, derived.diagnostics)

        derived.reportDiagnostic()

        Assert.assertSame(cachedListForBase, base.diagnostics.contents())
        Assert.assertSame(cachedListForMiddle, middle.diagnostics.contents())
        Assert.assertNotSame(cachedListForDerived, derived.diagnostics.contents())

        Assert.assertSame(cachedBase, base.diagnostics)
        Assert.assertSame(cachedMiddle, middle.diagnostics)

        middle.reportDiagnostic()

        Assert.assertSame(cachedListForBase, base.diagnostics.contents())
        Assert.assertNotSame(cachedListForMiddle, middle.diagnostics.contents())
        Assert.assertNotSame(cachedListForDerived, derived.diagnostics.contents())

        Assert.assertSame(cachedBase, base.diagnostics)
    }

    private fun BindingTrace.reportDiagnostic() {
        report(DummyDiagnostic())
    }

    //NOTE: cannot simply call all() since it applies filter on every query and produces new collection
    private fun Diagnostics.contents(): MutableCollection<Diagnostic> {
        return (this as MutableDiagnosticsWithSuppression).getReadonlyView().diagnostics
    }

    private class DummyDiagnosticFactory : DiagnosticFactory<DummyDiagnostic>("DUMMY", Severity.ERROR)

    private inner class DummyDiagnostic : Diagnostic {
        override konst factory = DummyDiagnosticFactory()
        private konst dummyElement = KtPsiFactory(environment.project).createType("Int")

        init {
            dummyElement.containingKtFile.doNotAnalyze = null
        }

        override konst severity get() = factory.severity
        override konst psiElement get() = dummyElement
        override konst textRanges get() = unimplemented()
        override konst psiFile get() = unimplemented()
        override konst isValid get() = unimplemented()

        private fun unimplemented(): Nothing = throw UnsupportedOperationException()
    }
}
