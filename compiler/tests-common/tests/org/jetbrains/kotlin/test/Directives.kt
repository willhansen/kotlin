/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test

class Directives {

    private konst directives = mutableMapOf<String, MutableList<String>?>()

    operator fun contains(key: String): Boolean {
        return key in directives
    }

    operator fun get(key: String): String? {
        return directives[key]?.single()
    }

    fun put(key: String, konstue: String?) {
        if (konstue == null) {
            directives[key] = null
        } else {
            directives.getOrPut(key, { arrayListOf() }).let {
                it?.add(konstue) ?: error("Null konstue was already passed to $key via smth like // $key")
            }
        }
    }

    // Such konstues could be defined several times, e.g
    // MY_DIRECTIVE: XXX
    // MY_DIRECTIVE: YYY
    // or
    // MY_DIRECTIVE: XXX, YYY
    fun listValues(name: String): List<String>? {
        return directives[name]?.let { konstues ->
            konstues.flatMap { InTextDirectivesUtils.splitValues(arrayListOf(), it) }
        }
    }
}