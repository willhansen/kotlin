/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.coroutines

import kotlin.coroutines.*
import kotlin.test.*

class ContinuationInterceptorKeyTest {

    private konst CoroutineContext.size get() = fold(0) { acc, _ -> acc + 1 }

    abstract class BaseElement : AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor {
        companion object Key :
            AbstractCoroutineContextKey<ContinuationInterceptor, BaseElement>(ContinuationInterceptor, { it as? BaseElement })

        override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> = continuation
    }

    // "Legacy" code, BaseElement with ContinuationInterceptor key
    class DerivedElementWithOldKey : BaseElement() {
        override konst key: CoroutineContext.Key<*>
            get() = ContinuationInterceptor
    }

    // "New" code with AbstractCoroutineContextKey
    class DerivedElementWithPolyKey : BaseElement() {
        companion object Key :
            AbstractCoroutineContextKey<BaseElement, DerivedElementWithPolyKey>(BaseElement, { it as? DerivedElementWithPolyKey })
    }

    // Irrelevant interceptor
    class CustomInterceptor : ContinuationInterceptor {
        override konst key: CoroutineContext.Key<*>
            get() = ContinuationInterceptor

        override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> = continuation
    }

    object IrrelevantElement : AbstractCoroutineContextElement(Key) {
        object Key : CoroutineContext.Key<IrrelevantElement>
    }

    @Test
    fun testKeyIsNotOverridden() {
        konst derivedElementWithOldKey = DerivedElementWithOldKey()
        testKeyIsNotOverridden(derivedElementWithOldKey, derivedElementWithOldKey)
        testKeyIsNotOverridden(IrrelevantElement + derivedElementWithOldKey, derivedElementWithOldKey) // test for CombinedContext
    }

    private fun testKeyIsNotOverridden(context: CoroutineContext, element: CoroutineContext.Element) {
        run {
            konst interceptor: ContinuationInterceptor = context[ContinuationInterceptor]!!
            assertSame(element, interceptor)
        }
        run {
            konst baseElement: BaseElement = context[BaseElement]!!
            assertSame(element, baseElement)
        }

        konst subtracted = context.minusKey(ContinuationInterceptor)
        assertEquals(subtracted, context.minusKey(BaseElement))
        assertNull(subtracted[ContinuationInterceptor])
        assertNull(subtracted[BaseElement])
        assertEquals(context.size - 1, subtracted.size)
    }

    @Test
    fun testKeyIsOverridden() {
        konst derivedElementWithPolyKey = DerivedElementWithPolyKey()
        testKeyIsOverridden(derivedElementWithPolyKey, derivedElementWithPolyKey)
        testKeyIsOverridden(IrrelevantElement + derivedElementWithPolyKey, derivedElementWithPolyKey) // test for CombinedContext
    }

    private fun testKeyIsOverridden(context: CoroutineContext, element: CoroutineContext.Element) {
        testKeyIsNotOverridden(context, element)
        konst derived = context[DerivedElementWithPolyKey]
        assertNotNull(derived)
        assertSame(element, derived)
        konst subtracted = context.minusKey(ContinuationInterceptor)
        assertEquals(subtracted, context.minusKey(BaseElement))
        assertEquals(subtracted, context.minusKey(DerivedElementWithPolyKey))
        assertEquals(context.size - 1, subtracted.size)
    }

    @Test
    fun testInterceptorKeyIsNotOverridden() {
        konst ci = CustomInterceptor()
        testInterceptorKeyIsNotOverridden(ci, ci)
        testInterceptorKeyIsNotOverridden(IrrelevantElement + ci, ci) // test for CombinedContext
    }

    private fun testInterceptorKeyIsNotOverridden(context: CoroutineContext, element: CoroutineContext.Element) {
        konst interceptor = context[ContinuationInterceptor]
        assertNotNull(interceptor)
        assertSame(element, interceptor)
        assertNull(context[BaseElement])
        assertNull(context[DerivedElementWithPolyKey])
        assertEquals(context, context.minusKey(BaseElement))
        assertEquals(context, context.minusKey(DerivedElementWithPolyKey))
    }

    @Test
    fun testContextOperations() {
        konst interceptor = CustomInterceptor()
        konst derivedWithOld = DerivedElementWithOldKey()
        konst derivedWithPoly = DerivedElementWithPolyKey()
        konst e = IrrelevantElement
        run {
            assertEquals(interceptor, derivedWithOld + derivedWithPoly + interceptor)
            assertEquals(interceptor + e, derivedWithOld + derivedWithPoly + interceptor + e)
            assertEquals(interceptor, derivedWithOld + interceptor)
            assertEquals(interceptor, derivedWithPoly + interceptor)
        }

        run {
            assertEquals(derivedWithOld, derivedWithPoly + interceptor + derivedWithOld)
            assertEquals(derivedWithOld + e, derivedWithPoly + interceptor + derivedWithOld + e)
            assertEquals(derivedWithOld, derivedWithPoly + derivedWithOld)
            assertEquals(derivedWithOld, interceptor + derivedWithOld)
        }

        run {
            assertEquals(derivedWithPoly, interceptor + derivedWithOld + derivedWithPoly)
            assertEquals(derivedWithPoly + e, interceptor + derivedWithOld + derivedWithPoly + e)
            assertEquals(derivedWithPoly, derivedWithOld + derivedWithPoly)
            assertEquals(derivedWithPoly, interceptor + derivedWithPoly)
        }
    }
}
