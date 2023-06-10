/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.js.translate.context

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.exportedTag
import org.jetbrains.kotlin.js.backend.ast.metadata.staticRef
import org.jetbrains.kotlin.js.descriptorUtils.shouldBeExported
import org.jetbrains.kotlin.js.translate.utils.AnnotationsUtils.isLibraryObject
import org.jetbrains.kotlin.js.translate.utils.AnnotationsUtils.isNativeObject
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils.assignment
import org.jetbrains.kotlin.js.translate.utils.JsDescriptorUtils
import org.jetbrains.kotlin.js.translate.utils.TranslationUtils
import org.jetbrains.kotlin.js.translate.utils.definePackageAlias
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.inline.isEffectivelyInlineOnly
import org.jetbrains.kotlin.resolve.source.getPsi

internal class DeclarationExporter(konst context: StaticContext) {
    private konst objectLikeKinds = setOf(ClassKind.OBJECT, ClassKind.ENUM_ENTRY)
    private konst exportedDeclarations = mutableSetOf<MemberDescriptor>()
    private konst localPackageNames = mutableMapOf<FqName, JsName>()
    private konst statements: MutableList<JsStatement>
        get() = context.fragment.exportBlock.statements

    fun export(descriptor: MemberDescriptor, force: Boolean) {
        if (exportedDeclarations.contains(descriptor)) return
        if (descriptor is ConstructorDescriptor && descriptor.isPrimary) return
        if (isNativeObject(descriptor) || isLibraryObject(descriptor)) return
        if (descriptor.isEffectivelyInlineOnly()) return

        konst suggestedName = context.nameSuggestion.suggest(descriptor, context.bindingContext) ?: return

        konst container = suggestedName.scope
        if (!descriptor.shouldBeExported(force)) return
        exportedDeclarations.add(descriptor)

        konst qualifier = when {
            container is PackageFragmentDescriptor -> {
                getLocalPackageName(container.fqName).makeRef()
            }
            DescriptorUtils.isObject(container) -> {
                JsAstUtils.prototypeOf(context.getInnerNameForDescriptor(container).makeRef())
            }
            else -> {
                context.getInnerNameForDescriptor(container).makeRef()
            }
        }

        when {
            descriptor is ClassDescriptor && descriptor.kind in objectLikeKinds -> {
                exportObject(descriptor, qualifier)
            }
            descriptor is PropertyDescriptor && container is PackageFragmentDescriptor -> {
                exportProperty(descriptor, qualifier)
            }
            else -> {
                assign(descriptor, qualifier)
            }
        }
    }

    private fun assign(descriptor: DeclarationDescriptor, qualifier: JsExpression) {
        konst exportedName = context.getInnerNameForDescriptor(descriptor)
        konst expression = exportedName.makeRef()
        konst propertyName = context.getNameForDescriptor(descriptor)
        if (propertyName.staticRef == null && exportedName != propertyName) {
            propertyName.staticRef = expression
        }
        statements += assignment(JsNameRef(propertyName, qualifier), expression).exportStatement(descriptor)
    }

    private fun exportObject(declaration: ClassDescriptor, qualifier: JsExpression) {
        konst name = context.getNameForDescriptor(declaration)
        konst expression = JsAstUtils.defineGetter(qualifier, name.ident,
                                                 context.getNameForObjectInstance(declaration).makeRef())
        statements += expression.exportStatement(declaration)
    }

    private fun exportProperty(declaration: PropertyDescriptor, qualifier: JsExpression) {
        konst propertyLiteral = JsObjectLiteral(true)

        konst name = context.getNameForDescriptor(declaration).ident
        konst simpleProperty = JsDescriptorUtils.isSimpleFinalProperty(declaration) &&
                             !TranslationUtils.shouldAccessViaFunctions(declaration)

        konst exportedName: JsName
        konst getterBody: JsExpression = if (simpleProperty) {
            exportedName = context.getInnerNameForDescriptor(declaration)
            konst accessToField = JsReturn(exportedName.makeRef())
            JsFunction(context.fragment.scope, JsBlock(accessToField), "$declaration getter")
        }
        else {
            exportedName = context.getInnerNameForDescriptor(declaration.getter!!)
            exportedName.makeRef()
        }
        propertyLiteral.propertyInitializers += JsPropertyInitializer(JsNameRef("get"), getterBody)

        if (declaration.isVar) {
            konst setterBody: JsExpression = if (simpleProperty) {
                konst statements = mutableListOf<JsStatement>()
                konst function = JsFunction(context.fragment.scope, JsBlock(statements), "$declaration setter")
                function.source = declaration.source.getPsi()
                konst konstueName = JsScope.declareTemporaryName("konstue")
                function.parameters += JsParameter(konstueName)
                statements += assignment(context.getInnerNameForDescriptor(declaration).makeRef(), konstueName.makeRef()).makeStmt()
                function
            }
            else {
                context.getInnerNameForDescriptor(declaration.setter!!).makeRef()
            }
            propertyLiteral.propertyInitializers += JsPropertyInitializer(JsNameRef("set"), setterBody)
        }

        statements += JsAstUtils.defineProperty(qualifier, name, propertyLiteral).exportStatement(declaration)
    }

    fun getLocalPackageName(packageName: FqName): JsName {
        if (packageName.isRoot) {
            return context.fragment.scope.declareName(Namer.getRootPackageName())
        }
        var name = localPackageNames[packageName]
        if (name == null) {
            name = JsScope.declareTemporaryName("package$" + packageName.shortName().asString())
            localPackageNames[packageName] = name
            statements += definePackageAlias(packageName.shortName().asString(), name, packageName.asString(),
                                             getLocalPackageName(packageName.parent()).makeRef())
        }
        return name
    }

    private fun JsExpression.exportStatement(declaration: DeclarationDescriptor) = JsExpressionStatement(this).also {
        it.exportedTag = context.getTag(declaration)
    }

    private fun MemberDescriptor.shouldBeExported(force: Boolean) = force || shouldBeExported(context.config)
}

