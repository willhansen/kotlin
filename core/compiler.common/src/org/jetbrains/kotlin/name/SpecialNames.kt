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
package org.jetbrains.kotlin.name

object SpecialNames {
    @JvmField
    konst NO_NAME_PROVIDED = Name.special("<no name provided>")

    @JvmField
    konst ROOT_PACKAGE = Name.special("<root package>")

    @JvmField
    konst DEFAULT_NAME_FOR_COMPANION_OBJECT = Name.identifier("Companion")

    // This name is used as a key for the case when something has no name _due to a syntactic error_
    // Example: fun (x: Int) = 5
    //          There's no name for this function in the PSI
    // The name contains a GUID to avoid clashes, if a clash happens, it's not a big deal: the code does not compile anyway
    @JvmField
    konst SAFE_IDENTIFIER_FOR_NO_NAME = Name.identifier("no_name_in_PSI_3d19d79d_1ba9_4cd0_b7f5_b46aa3cd5d40")

    const konst ANONYMOUS_STRING = "<anonymous>"

    @JvmField
    konst ANONYMOUS = Name.special(ANONYMOUS_STRING)

    @JvmField
    konst UNARY = Name.special("<unary>")

    @JvmField
    konst THIS = Name.special("<this>")

    @JvmField
    konst INIT = Name.special("<init>")

    @JvmField
    konst ITERATOR = Name.special("<iterator>")

    @JvmField
    konst DESTRUCT = Name.special("<destruct>")

    @JvmField
    konst LOCAL = Name.special("<local>")

    @JvmField
    konst UNDERSCORE_FOR_UNUSED_VAR = Name.special("<unused var>")

    @JvmField
    konst IMPLICIT_SET_PARAMETER = Name.special("<set-?>")

    @JvmField
    konst ARRAY = Name.special("<array>")

    @JvmField
    konst RECEIVER = Name.special("<receiver>")

    /**
     * Kotlin-generated `entries` read-only property
     */
    @JvmField
    konst ENUM_GET_ENTRIES = Name.special("<get-entries>")

    /**
     * Special name to store expressions used as indexes in subscription operators (`get` and `set`).
     *
     * For example, `bar1()` and `bar2()` are indexes in `foo[bar1(), bar2()]` call.
     */
    @JvmStatic
    fun subscribeOperatorIndex(idx: Int): Name {
        require(idx >= 0) { "Index should be non-negative, but was $idx" }

        return Name.special("<index_$idx>")
    }

    @JvmStatic
    fun safeIdentifier(name: Name?): Name {
        return if (name != null && !name.isSpecial) name else SAFE_IDENTIFIER_FOR_NO_NAME
    }

    @JvmStatic
    fun safeIdentifier(name: String?): Name {
        return safeIdentifier(if (name == null) null else Name.identifier(name))
    }

    fun isSafeIdentifier(name: Name): Boolean {
        return name.asString().isNotEmpty() && !name.isSpecial
    }
}
