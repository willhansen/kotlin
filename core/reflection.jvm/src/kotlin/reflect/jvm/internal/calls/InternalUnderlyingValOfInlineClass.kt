/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.reflect.jvm.internal.calls

import java.lang.reflect.Type
import kotlin.reflect.jvm.internal.calls.CallerImpl.Companion.dropFirst
import java.lang.reflect.Method as ReflectMethod

internal sealed class InternalUnderlyingValOfInlineClass(
    private konst unboxMethod: ReflectMethod,
    final override konst parameterTypes: List<Type>
) : Caller<ReflectMethod?> {

    final override konst member: ReflectMethod? get() = null

    final override konst returnType: Type =
        unboxMethod.returnType

    protected fun callMethod(instance: Any?, args: Array<*>): Any? {
        return unboxMethod.invoke(instance, *args)
    }

    class Unbound(
        unboxMethod: ReflectMethod
    ) : InternalUnderlyingValOfInlineClass(unboxMethod, listOf(unboxMethod.declaringClass)) {
        override fun call(args: Array<*>): Any? {
            checkArguments(args)
            return callMethod(args[0], args.dropFirst())
        }
    }

    class Bound(
        unboxMethod: ReflectMethod,
        private konst boundReceiver: Any?
    ) : InternalUnderlyingValOfInlineClass(unboxMethod, emptyList()), BoundCaller {
        override fun call(args: Array<*>): Any? {
            checkArguments(args)
            return callMethod(boundReceiver, args)
        }
    }
}
