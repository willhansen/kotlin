/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.utils.serialization

// TODO: is it OK to just use UTF_8?
konst SerializationCharset = Charsets.UTF_8

object StatementIds {
    const konst RETURN = 0
    const konst THROW = 1
    const konst BREAK = 2
    const konst CONTINUE = 3
    const konst DEBUGGER = 4
    const konst EXPRESSION = 5
    const konst VARS = 6
    const konst BLOCK = 7
    const konst COMPOSITE_BLOCK = 8
    const konst LABEL = 9
    const konst IF = 10
    const konst SWITCH = 11
    const konst WHILE = 12
    const konst DO_WHILE = 13
    const konst FOR = 14
    const konst FOR_IN = 15
    const konst TRY = 16
    const konst EMPTY = 17
    const konst SINGLE_LINE_COMMENT = 18
    const konst MULTI_LINE_COMMENT = 19
    const konst IMPORT = 20
    const konst EXPORT = 21
}

object ImportType {
    const konst ALL = 0
    const konst ITEMS = 1
    const konst DEFAULT = 2
}

object ExportType {
    const konst ALL = 0
    const konst ITEMS = 1
}

object ExpressionIds {
    const konst THIS_REF = 0
    const konst NULL = 1
    const konst TRUE_LITERAL = 2
    const konst FALSE_LITERAL = 3
    const konst STRING_LITERAL = 4
    const konst REG_EXP = 5
    const konst INT_LITERAL = 6
    const konst DOUBLE_LITERAL = 7
    const konst ARRAY_LITERAL = 8
    const konst OBJECT_LITERAL = 9
    const konst FUNCTION = 10
    const konst DOC_COMMENT = 11
    const konst BINARY_OPERATION = 12
    const konst PREFIX_OPERATION = 13
    const konst POSTFIX_OPERATION = 14
    const konst CONDITIONAL = 15
    const konst ARRAY_ACCESS = 16
    const konst NAME_REFERENCE = 17
    const konst SIMPLE_NAME_REFERENCE = 18
    const konst PROPERTY_REFERENCE = 19
    const konst INVOCATION = 20
    const konst NEW = 21
    const konst CLASS = 22
    const konst SUPER_REF = 23
}