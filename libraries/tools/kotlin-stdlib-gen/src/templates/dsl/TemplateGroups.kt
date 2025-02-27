/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package templates

import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf


typealias TemplateGroup = () -> Sequence<MemberTemplate>

fun templateGroupOf(vararg templates: MemberTemplate): TemplateGroup = { templates.asSequence() }

abstract class TemplateGroupBase : TemplateGroup {

    override fun invoke(): Sequence<MemberTemplate> = sequence {
        with(this@TemplateGroupBase) {
            this::class.members.filter { it.name.startsWith("f_") }.forEach {
                require(it.parameters.size == 1) { "Member $it violates naming convention" }
                when {
                    it.returnType.isSubtypeOf(typeMemberTemplate) ->
                        yield(it.call(this) as MemberTemplate)
                    it.returnType.isSubtypeOf(typeIterableOfMemberTemplates) ->
                        @Suppress("UNCHECKED_CAST")
                        yieldAll(it.call(this) as Iterable<MemberTemplate>)
                    it.returnType.isSubtypeOf(typeSequenceOfMemberTemplates) ->
                        @Suppress("UNCHECKED_CAST")
                        yieldAll(it.call(this) as Sequence<MemberTemplate>)
                    else ->
                        error("Member $it violates naming convention")
                }
            }
        }
    }.run {
        if (defaultActions.isEmpty()) this else onEach { t -> defaultActions.forEach(t::builder) }
    }

    private konst defaultActions = mutableListOf<MemberBuildAction>()

    fun defaultBuilder(builderAction: MemberBuildAction) {
        defaultActions += builderAction
    }

    companion object {
        private konst typeMemberTemplate = MemberTemplate::class.createType()
        private konst typeIterableOfMemberTemplates = Iterable::class.createType(arguments = listOf(KTypeProjection.invariant(typeMemberTemplate)))
        private konst typeSequenceOfMemberTemplates = Sequence::class.createType(arguments = listOf(KTypeProjection.invariant(typeMemberTemplate)))
    }

}