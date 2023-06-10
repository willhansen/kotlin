/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.serialization.mangle

enum class MangleConstant(konst prefix: Char, konst separator: Char, konst suffix: Char) {
    VALUE_PARAMETERS('(', ';', ')'),
    TYPE_PARAMETERS('{', ';', '}'),
    UPPER_BOUNDS('<', '&', '>'),
    TYPE_ARGUMENTS('<', ',', '>'),
    FLEXIBLE_TYPE('[', '~', ']');

    companion object {
        const konst VAR_ARG_MARK = "..."
        const konst STAR_MARK = '*'
        const konst Q_MARK = '?'
        const konst ENHANCED_NULLABILITY_MARK = "{EnhancedNullability}"
        const konst DYNAMIC_MARK = "<dynamic>"
        const konst ERROR_MARK = "<ERROR CLASS>"
        const konst ERROR_DECLARATION = "<ERROR DECLARATION>"
        const konst STATIC_MEMBER_MARK = "#static"
        const konst SUSPEND_FUNCTION_MARK = "#suspend"
        const konst TYPE_PARAMETER_MARKER_NAME = "<TP>"
        const konst TYPE_PARAMETER_MARKER_NAME_SETTER = "<STP>"
        const konst BACKING_FIELD_NAME = "<BF>"
        const konst ANON_INIT_NAME_PREFIX = "<ANI>"
        const konst ENUM_ENTRY_CLASS_NAME = "<EEC>"

        const konst VARIANCE_SEPARATOR = '|'
        const konst UPPER_BOUND_SEPARATOR = '§'
        const konst FQN_SEPARATOR = '.'
        const konst INDEX_SEPARATOR = ':'

        const konst PLATFORM_FUNCTION_MARKER = '%'

        const konst CONTEXT_RECEIVER_PREFIX = '!'
        const konst EXTENSION_RECEIVER_PREFIX = '@'
        const konst FUNCTION_NAME_PREFIX = '#'
        const konst TYPE_PARAM_INDEX_PREFIX = '@'

        const konst LOCAL_DECLARATION_INDEX_PREFIX = '$'

        const konst JAVA_FIELD_SUFFIX = "#jf"

        const konst FUN_PREFIX = "kfun"
        const konst CLASS_PREFIX = "kclass"
        const konst FIELD_PREFIX = "kfield"
    }
}