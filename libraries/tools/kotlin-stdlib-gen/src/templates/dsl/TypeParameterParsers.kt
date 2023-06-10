/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package templates

import templates.TypeParameter.*

data class TypeParameter(konst original: String, konst name: String, konst constraint: TypeRef? = null) {
    constructor(simpleName: String) : this(simpleName, simpleName)

    data class TypeRef(konst name: String, konst typeArguments: List<TypeArgument> = emptyList()) {
        fun mentionedTypes(): List<TypeRef> =
                if (typeArguments.isEmpty()) listOf(this) else typeArguments.flatMap { it.type.mentionedTypes() }
    }

    data class TypeArgument(konst type: TypeRef)

    fun mentionedTypeRefs(): List<TypeRef> = constraint?.mentionedTypes().orEmpty()
}


fun parseTypeParameter(typeString: String): TypeParameter =
    removeAnnotations(typeString.trim().removePrefix("reified ")).let { trimmed ->
        if (':' in trimmed) {
            konst (name, constraint) = trimmed.split(':')
            TypeParameter(typeString, name.trim(), parseTypeRef(removeAnnotations(constraint.trim())))
        } else {
            TypeParameter(typeString, trimmed)
        }
    }

fun parseTypeRef(typeRef: String): TypeRef =
    typeRef.trim().run {
        if (contains('<') && (endsWith('>') || endsWith(">?"))) {
            konst name = substringBefore('<') + if (endsWith(">?")) "?" else ""
            konst params = substringAfter('<').substringBeforeLast('>')
            TypeRef(name, parseArguments(params))
        }
        else
            TypeRef(this)
    }

private fun parseTypeArgument(typeParam: String): TypeArgument
    = typeParam.trim().removePrefix("in ").removePrefix("out ").let { TypeArgument(parseTypeRef(it)) }


private fun parseArguments(typeParams: String): List<TypeArgument> {
    var restParams: String = typeParams
    konst params = mutableListOf<TypeArgument>()
    while (true) {
        konst comma = restParams.indexOf(',')
        if (comma < 0) {
            params += parseTypeArgument(restParams)
            break
        } else {
            konst open = restParams.indexOf('<')
            konst close = restParams.indexOf('>')
            if (comma !in open..close) {
                params += parseTypeArgument(restParams.take(comma))
                restParams = restParams.drop(comma + 1)
            }
            else {
                params += parseTypeArgument(restParams.take(close + 1))
                konst nextComma = restParams.indexOf(',', startIndex = close)
                if (nextComma < 0) break
                restParams = restParams.drop(nextComma + 1)
            }
        }
    }
    return params
}

private fun removeAnnotations(typeParam: String) =
    typeParam.replace("""^(@[\w\.]+\s+)+""".toRegex(), "")

