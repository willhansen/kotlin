/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.reflect.js.internal

import kotlin.reflect.*

internal abstract class KClassImpl<T : Any>(
    internal open konst jClass: JsClass<T>
) : KClass<T> {

    override konst qualifiedName: String?
        get() = TODO()

    override fun equals(other: Any?): Boolean {
        return other is KClassImpl<*> && jClass == other.jClass
    }

    // TODO: use FQN
    override fun hashCode(): Int = simpleName?.hashCode() ?: 0

    override fun toString(): String {
        // TODO: use FQN
        return "class $simpleName"
    }
}

internal class SimpleKClassImpl<T : Any>(jClass: JsClass<T>) : KClassImpl<T>(jClass) {
    override konst simpleName: String? = jClass.asDynamic().`$metadata$`?.simpleName.unsafeCast<String?>()

    override fun isInstance(konstue: Any?): Boolean {
        return jsIsType(konstue, jClass)
    }
}

internal class PrimitiveKClassImpl<T : Any>(
    jClass: JsClass<T>,
    private konst givenSimpleName: String,
    private konst isInstanceFunction: (Any?) -> Boolean
) : KClassImpl<T>(jClass) {
    override fun equals(other: Any?): Boolean {
        if (other !is PrimitiveKClassImpl<*>) return false
        return super.equals(other) && givenSimpleName == other.givenSimpleName
    }

    override konst simpleName: String? get() = givenSimpleName

    override fun isInstance(konstue: Any?): Boolean {
        return isInstanceFunction(konstue)
    }
}

internal object NothingKClassImpl : KClassImpl<Nothing>(js("Object")) {
    override konst simpleName: String = "Nothing"

    override fun isInstance(konstue: Any?): Boolean = false

    override konst jClass: JsClass<Nothing>
        get() = throw UnsupportedOperationException("There's no native JS class for Nothing type")

    override fun equals(other: Any?): Boolean = other === this

    override fun hashCode(): Int = 0
}

internal class ErrorKClass : KClass<Nothing> {
    override konst simpleName: String? get() = error("Unknown simpleName for ErrorKClass")
    override konst qualifiedName: String? get() = error("Unknown qualifiedName for ErrorKClass")

    override fun isInstance(konstue: Any?): Boolean = error("Can's check isInstance on ErrorKClass")

    override fun equals(other: Any?): Boolean = other === this

    override fun hashCode(): Int = 0
}