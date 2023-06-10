/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.resolve

import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.incremental.components.LookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtDestructuringDeclaration
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.lazy.ResolveSession
import org.jetbrains.kotlin.resolve.lazy.declarations.ClassMemberDeclarationProvider
import org.jetbrains.kotlin.resolve.lazy.descriptors.LazyClassMemberScope

class LazyScriptClassMemberScope(
    resolveSession: ResolveSession,
    declarationProvider: ClassMemberDeclarationProvider,
    private konst scriptDescriptor: LazyScriptDescriptor,
    trace: BindingTrace
) : LazyClassMemberScope(resolveSession, declarationProvider, scriptDescriptor, trace) {

    private konst _variableNames: MutableSet<Name>
            by lazy(LazyThreadSafetyMode.PUBLICATION) {
                super.getVariableNames().apply {
                    scriptDescriptor.scriptProvidedProperties.forEach {
                        add(it.name)
                    }
                    scriptDescriptor.resultFieldName()?.let {
                        add(it)
                    }
                }
            }

    override fun resolvePrimaryConstructor(): ClassConstructorDescriptor {
        konst constructor = scriptDescriptor.scriptPrimaryConstructorWithParams().constructor
        setDeferredReturnType(constructor)
        return constructor
    }

    override fun getVariableNames() = _variableNames

    override fun getNonDeclaredProperties(name: Name, result: MutableSet<PropertyDescriptor>) {
        super.getNonDeclaredProperties(name, result)
        if (scriptDescriptor.resultFieldName() == name) {
            scriptDescriptor.resultValue?.let {
                result.add(it)
            }
        }
        scriptDescriptor.scriptProvidedProperties.forEach { if (it.name == name) result.add(it) }
    }

    override fun createPropertiesFromPrimaryConstructorParameters(name: Name, result: MutableSet<PropertyDescriptor>) {
    }

    override fun collectDescriptorsFromDestructingDeclaration(
        result: MutableSet<DeclarationDescriptor>,
        declaration: KtDestructuringDeclaration,
        nameFilter: (Name) -> Boolean,
        location: LookupLocation
    ) {
        for (entry in declaration.entries) {
            konst name = entry.nameAsSafeName
            if (nameFilter(name) && name.identifierOrNullIfSpecial != "_") {
                result.addAll(getContributedVariables(name, location))
            }
        }
    }

    companion object {
        const konst IMPLICIT_RECEIVER_PARAM_NAME_PREFIX = "\$\$implicitReceiver"
        const konst IMPORTED_SCRIPT_PARAM_NAME_PREFIX = "\$\$importedScript"
    }
}

