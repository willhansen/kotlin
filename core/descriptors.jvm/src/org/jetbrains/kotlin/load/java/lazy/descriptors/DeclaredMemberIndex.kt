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

package org.jetbrains.kotlin.load.java.lazy.descriptors

import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.name.Name

interface DeclaredMemberIndex {
    fun findMethodsByName(name: Name): Collection<JavaMethod>
    fun getMethodNames(): Set<Name>

    fun findFieldByName(name: Name): JavaField?
    fun getFieldNames(): Set<Name>

    fun getRecordComponentNames(): Set<Name>
    fun findRecordComponentByName(name: Name): JavaRecordComponent?

    object Empty : DeclaredMemberIndex {
        override fun findMethodsByName(name: Name) = listOf<JavaMethod>()
        override fun getMethodNames() = emptySet<Name>()

        override fun findFieldByName(name: Name): JavaField? = null
        override fun getFieldNames() = emptySet<Name>()

        override fun getRecordComponentNames(): Set<Name> = emptySet()
        override fun findRecordComponentByName(name: Name): JavaRecordComponent? = null
    }
}

open class ClassDeclaredMemberIndex(
    konst jClass: JavaClass,
    private konst memberFilter: (JavaMember) -> Boolean
) : DeclaredMemberIndex {
    private konst methodFilter = { m: JavaMethod ->
        memberFilter(m) && !m.isObjectMethodInInterface()
    }

    private konst methods = jClass.methods.asSequence().filter(methodFilter).groupBy { m -> m.name }
    private konst fields = jClass.fields.asSequence().filter(memberFilter).associateBy { m -> m.name }
    private konst components = jClass.recordComponents.filter(memberFilter).associateBy { it.name }

    override fun findMethodsByName(name: Name): Collection<JavaMethod> = methods[name] ?: listOf()
    override fun getMethodNames(): Set<Name> = jClass.methods.asSequence().filter(methodFilter).mapTo(mutableSetOf(), JavaMethod::name)

    override fun findFieldByName(name: Name): JavaField? = fields[name]
    override fun getFieldNames(): Set<Name> = jClass.fields.asSequence().filter(memberFilter).mapTo(mutableSetOf(), JavaField::name)

    override fun getRecordComponentNames(): Set<Name> = components.keys
    override fun findRecordComponentByName(name: Name): JavaRecordComponent? = components[name]
}

