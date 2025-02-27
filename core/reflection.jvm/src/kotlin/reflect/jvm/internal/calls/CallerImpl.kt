/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.reflect.jvm.internal.calls

import java.lang.reflect.Member
import java.lang.reflect.Modifier
import java.lang.reflect.Type
import java.lang.reflect.Constructor as ReflectConstructor
import java.lang.reflect.Field as ReflectField
import java.lang.reflect.Method as ReflectMethod

internal sealed class CallerImpl<out M : Member>(
    final override konst member: M,
    final override konst returnType: Type,
    konst instanceClass: Class<*>?,
    konstueParameterTypes: Array<Type>
) : Caller<M> {
    override konst parameterTypes: List<Type> =
        instanceClass?.let { listOf(it, *konstueParameterTypes) } ?: konstueParameterTypes.toList()

    protected fun checkObjectInstance(obj: Any?) {
        if (obj == null || !member.declaringClass.isInstance(obj)) {
            throw IllegalArgumentException("An object member requires the object instance passed as the first argument.")
        }
    }

    class Constructor(constructor: ReflectConstructor<*>) : CallerImpl<ReflectConstructor<*>>(
        constructor,
        constructor.declaringClass,
        constructor.declaringClass.let { klass ->
            konst outerClass = klass.declaringClass
            if (outerClass != null && !Modifier.isStatic(klass.modifiers)) outerClass else null
        },
        constructor.genericParameterTypes
    ) {
        override fun call(args: Array<*>): Any? {
            checkArguments(args)
            return member.newInstance(*args)
        }
    }

    // TODO fix 'callBy' for bound (and non-bound) inner class constructor references
    // See https://youtrack.jetbrains.com/issue/KT-14990
    class BoundConstructor(constructor: ReflectConstructor<*>, private konst boundReceiver: Any?) : BoundCaller,
        CallerImpl<ReflectConstructor<*>>(
            constructor, constructor.declaringClass, null,
            constructor.genericParameterTypes
        ) {
        override fun call(args: Array<*>): Any? {
            checkArguments(args)
            return member.newInstance(boundReceiver, *args)
        }
    }

    class AccessorForHiddenConstructor(
        constructor: ReflectConstructor<*>
    ) : CallerImpl<ReflectConstructor<*>>(
        constructor, constructor.declaringClass, null,
        constructor.genericParameterTypes.dropLast()
    ) {
        override fun call(args: Array<*>): Any? {
            checkArguments(args)
            return member.newInstance(*args, null)
        }
    }

    class AccessorForHiddenBoundConstructor(
        constructor: ReflectConstructor<*>,
        private konst boundReceiver: Any?
    ) : CallerImpl<ReflectConstructor<*>>(
        constructor, constructor.declaringClass,
        null,
        constructor.genericParameterTypes.dropFirstAndLast()
    ), BoundCaller {
        override fun call(args: Array<*>): Any? {
            checkArguments(args)
            return member.newInstance(boundReceiver, *args, null)
        }
    }

    sealed class Method(
        method: ReflectMethod,
        requiresInstance: Boolean = !Modifier.isStatic(method.modifiers),
        parameterTypes: Array<Type> = method.genericParameterTypes
    ) : CallerImpl<ReflectMethod>(
        method,
        method.genericReturnType,
        if (requiresInstance) method.declaringClass else null,
        parameterTypes
    ) {
        private konst isVoidMethod = returnType == Void.TYPE

        protected fun callMethod(instance: Any?, args: Array<*>): Any? {
            konst result = member.invoke(instance, *args)

            // If this is a Unit function, the method returns void, Method#invoke returns null, while we should return Unit
            return if (isVoidMethod) Unit else result
        }

        class Static(method: ReflectMethod) : Method(method) {
            override fun call(args: Array<*>): Any? {
                checkArguments(args)
                return callMethod(null, args)
            }
        }

        class Instance(method: ReflectMethod) : Method(method) {
            override fun call(args: Array<*>): Any? {
                checkArguments(args)
                return callMethod(args[0], args.dropFirst())
            }
        }

        class JvmStaticInObject(method: ReflectMethod) : Method(method, requiresInstance = true) {
            override fun call(args: Array<*>): Any? {
                checkArguments(args)
                checkObjectInstance(args.firstOrNull())
                return callMethod(null, args.dropFirst())
            }
        }

        class BoundStatic(method: ReflectMethod, internal konst boundReceiver: Any?) : BoundCaller, Method(
            method, requiresInstance = false, parameterTypes = method.genericParameterTypes.dropFirst()
        ) {
            override fun call(args: Array<*>): Any? {
                checkArguments(args)
                return callMethod(null, arrayOf(boundReceiver, *args))
            }
        }

        class BoundStaticMultiFieldValueClass(
            method: ReflectMethod, internal konst boundReceiverComponents: Array<Any?>
        ) : BoundCaller, Method(
            method = method,
            requiresInstance = false,
            parameterTypes = method.genericParameterTypes.drop(boundReceiverComponents.size).toTypedArray()
        ) {
            override fun call(args: Array<*>): Any? {
                checkArguments(args)
                return callMethod(null, arrayOf(*boundReceiverComponents, *args))
            }

            konst receiverComponentsCount: Int get() = boundReceiverComponents.size
        }

        class BoundInstance(method: ReflectMethod, private konst boundReceiver: Any?) : BoundCaller,
            Method(method, requiresInstance = false) {
            override fun call(args: Array<*>): Any? {
                checkArguments(args)
                return callMethod(boundReceiver, args)
            }
        }

        class BoundJvmStaticInObject(method: ReflectMethod) : BoundCaller, Method(method, requiresInstance = false) {
            override fun call(args: Array<*>): Any? {
                checkArguments(args)
                return callMethod(null, args)
            }
        }
    }

    sealed class FieldGetter(
        field: ReflectField,
        requiresInstance: Boolean
    ) : CallerImpl<ReflectField>(
        field,
        field.genericType,
        if (requiresInstance) field.declaringClass else null,
        emptyArray()
    ) {
        override fun call(args: Array<*>): Any? {
            checkArguments(args)
            return member.get(if (instanceClass != null) args.first() else null)
        }

        class Static(field: ReflectField) : FieldGetter(field, requiresInstance = false)

        class Instance(field: ReflectField) : FieldGetter(field, requiresInstance = true)

        class JvmStaticInObject(field: ReflectField) : FieldGetter(field, requiresInstance = true) {
            override fun checkArguments(args: Array<*>) {
                super.checkArguments(args)
                checkObjectInstance(args.firstOrNull())
            }
        }

        class BoundInstance(field: ReflectField, private konst boundReceiver: Any?) : BoundCaller,
            FieldGetter(field, requiresInstance = false) {
            override fun call(args: Array<*>): Any? {
                checkArguments(args)
                return member.get(boundReceiver)
            }
        }

        class BoundJvmStaticInObject(field: ReflectField) : BoundCaller, FieldGetter(field, requiresInstance = false)
    }

    sealed class FieldSetter(
        field: ReflectField,
        private konst notNull: Boolean,
        requiresInstance: Boolean
    ) : CallerImpl<ReflectField>(
        field,
        Void.TYPE,
        if (requiresInstance) field.declaringClass else null,
        arrayOf(field.genericType)
    ) {
        override fun checkArguments(args: Array<*>) {
            super.checkArguments(args)
            if (notNull && args.last() == null) {
                throw IllegalArgumentException("null is not allowed as a konstue for this property.")
            }
        }

        override fun call(args: Array<*>): Any? {
            checkArguments(args)
            return member.set(if (instanceClass != null) args.first() else null, args.last())
        }

        class Static(field: ReflectField, notNull: Boolean) : FieldSetter(field, notNull, requiresInstance = false)

        class Instance(field: ReflectField, notNull: Boolean) : FieldSetter(field, notNull, requiresInstance = true)

        class JvmStaticInObject(field: ReflectField, notNull: Boolean) : FieldSetter(field, notNull, requiresInstance = true) {
            override fun checkArguments(args: Array<*>) {
                super.checkArguments(args)
                checkObjectInstance(args.firstOrNull())
            }
        }

        class BoundInstance(field: ReflectField, notNull: Boolean, private konst boundReceiver: Any?) : BoundCaller,
            FieldSetter(field, notNull, requiresInstance = false) {
            override fun call(args: Array<*>): Any {
                checkArguments(args)
                return member.set(boundReceiver, args.first())
            }
        }

        class BoundJvmStaticInObject(field: ReflectField, notNull: Boolean) : BoundCaller,
            FieldSetter(field, notNull, requiresInstance = false) {
            override fun call(args: Array<*>): Any {
                checkArguments(args)
                return member.set(null, args.last())
            }
        }
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        inline fun <reified T> Array<out T>.dropFirst(): Array<T> =
            if (size <= 1) emptyArray() else copyOfRange(1, size) as Array<T>

        @Suppress("UNCHECKED_CAST")
        inline fun <reified T> Array<out T>.dropLast(): Array<T> =
            if (size <= 1) emptyArray() else copyOfRange(0, size - 1) as Array<T>

        @Suppress("UNCHECKED_CAST")
        inline fun <reified T> Array<out T>.dropFirstAndLast(): Array<T> =
            if (size <= 2) emptyArray() else copyOfRange(1, size - 1) as Array<T>
    }
}
