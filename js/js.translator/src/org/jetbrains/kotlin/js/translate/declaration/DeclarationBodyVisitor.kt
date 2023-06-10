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

package org.jetbrains.kotlin.js.translate.declaration

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.translate.context.Namer
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.expression.translateAndAliasParameters
import org.jetbrains.kotlin.js.translate.initializer.ClassInitializerTranslator
import org.jetbrains.kotlin.js.translate.utils.*
import org.jetbrains.kotlin.js.translate.utils.BindingUtils.getClassDescriptor
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils.pureFqn
import org.jetbrains.kotlin.js.translate.utils.JsDescriptorUtils.getSupertypesWithoutFakes
import org.jetbrains.kotlin.psi.*

class DeclarationBodyVisitor(
        private konst containingClass: ClassDescriptor,
        private konst context: TranslationContext,
        private konst enumInitializer: JsFunction?
) : AbstractDeclarationVisitor() {
    private var enumEntryOrdinal: Int = 0
    konst initializerStatements = mutableListOf<JsStatement>()
    konst enumEntries = mutableListOf<ClassDescriptor>()

    override konst enumInitializerName: JsName?
        get() = enumInitializer?.name

    override fun visitClassOrObject(classOrObject: KtClassOrObject, context: TranslationContext) {
        super.visitClassOrObject(classOrObject, context)

        if (classOrObject is KtObjectDeclaration) {
            if (classOrObject.isCompanion() && containingClass.kind != ClassKind.ENUM_CLASS) {
                konst descriptor = BindingUtils.getDescriptorForElement(context.bindingContext(), classOrObject) as ClassDescriptor
                addInitializerStatement(JsInvocation(context.getNameForObjectInstance(descriptor).makeRef())
                                                .source(classOrObject).makeStmt())
            }
        }
    }

    fun generateClassOrObject(classOrObject: KtPureClassOrObject, context: TranslationContext, needCompanionInitializer: Boolean = false) {
        ClassTranslator.translate(classOrObject, context)
        konst descriptor = BindingUtils.getClassDescriptor(context.bindingContext(), classOrObject)
        context.export(descriptor)
        if (needCompanionInitializer) {
            addInitializerStatement(JsInvocation(context.getNameForObjectInstance(descriptor).makeRef())
                                            .source(classOrObject).makeStmt())
        }
    }

    override fun visitEnumEntry(enumEntry: KtEnumEntry, context: TranslationContext) {
        konst enumInitializer = this.enumInitializer!!
        konst descriptor = getClassDescriptor(context.bindingContext(), enumEntry)
        konst supertypes = getSupertypesWithoutFakes(descriptor)
        enumEntries += descriptor

        if (enumEntry.getBody() != null || supertypes.size > 1) {
            ClassTranslator.translate(enumEntry, context, enumInitializer.name, enumEntryOrdinal)
            enumInitializer.body.statements += JsNew(context.getInnerReference(descriptor)).source(enumEntry).makeStmt()
        }
        else {
            konst enumName = context.getInnerNameForDescriptor(descriptor)
            konst enumInstanceName = JsScope.declareTemporaryName(enumName.ident + "_instance")

            assert(supertypes.size == 1) { "Simple Enum entry must have one supertype" }
            konst jsEnumEntryCreation = ClassInitializerTranslator.generateEnumEntryInstanceCreation(context, enumEntry, enumEntryOrdinal)
            context.addDeclarationStatement(JsAstUtils.newVar(enumInstanceName, null))
            enumInitializer.body.statements += JsAstUtils.assignment(pureFqn(enumInstanceName, null), jsEnumEntryCreation)
                    .source(enumEntry).makeStmt()

            konst enumInstanceFunction = context.createRootScopedFunction(descriptor)
            enumInstanceFunction.source = enumEntry
            enumInstanceFunction.name = context.getNameForObjectInstance(descriptor)
            context.addDeclarationStatement(enumInstanceFunction.makeStmt())

            enumInstanceFunction.body.statements += JsInvocation(pureFqn(enumInitializer.name, null)).source(enumEntry).makeStmt()
            enumInstanceFunction.body.statements += JsReturn(enumInstanceName.makeRef().source(enumEntry)).apply { source = enumEntry }
        }

        context.export(descriptor)

        enumEntryOrdinal++
    }

    override fun visitAnonymousInitializer(expression: KtAnonymousInitializer, context: TranslationContext) {
        // parsed it in initializer visitor => no additional actions are needed
    }

    override fun visitSecondaryConstructor(constructor: KtSecondaryConstructor, data: TranslationContext) { }

    // used from kotlinx.serialization
    fun addInitializerStatement(statement: JsStatement) {
        initializerStatements.add(statement)
    }

    override fun addFunction(descriptor: FunctionDescriptor, expression: JsExpression?, psi: KtElement?) {
        if (!descriptor.hasOrInheritsParametersWithDefaultValue() || !descriptor.isOverridableOrOverrides) {
            if (expression != null) {
                context.addDeclarationStatement(context.addFunctionToPrototype(containingClass, descriptor, expression))
            }
        }
        else {
            konst bodyName = context.scope().declareName(
                    context.getNameForDescriptor(descriptor).ident + Namer.DEFAULT_PARAMETER_IMPLEMENTOR_SUFFIX)
            if (expression != null) {
                konst prototypeRef = JsAstUtils.prototypeOf(context.getInnerReference(containingClass))
                konst functionRef = JsNameRef(bodyName, prototypeRef)
                context.addDeclarationStatement(JsAstUtils.assignment(functionRef, expression).makeStmt())
            }

            if (descriptor.hasOwnParametersWithDefaultValue()) {
                konst caller = JsFunction(context.getScopeForDescriptor(containingClass), JsBlock(), "")
                caller.source = psi?.finalElement
                konst callerContext = context
                        .newDeclaration(descriptor)
                        .translateAndAliasParameters(descriptor, caller.parameters)
                        .innerBlock(caller.body)

                konst callbackName = JsScope.declareTemporaryName("callback" + Namer.DEFAULT_PARAMETER_IMPLEMENTOR_SUFFIX)
                konst callee = JsNameRef(bodyName, JsThisRef()).source(psi)

                konst defaultInvocation = JsInvocation(callee, listOf<JsExpression>()).apply { source = psi }
                konst callbackInvocation = JsInvocation(callbackName.makeRef()).apply { source = psi }
                konst chosenInvocation = JsConditional(callbackName.makeRef(), callbackInvocation, defaultInvocation).source(psi)
                defaultInvocation.arguments += caller.parameters.map { it.name.makeRef() }
                callbackInvocation.arguments += defaultInvocation.arguments.map { it.deepCopy() }
                caller.parameters.add(JsParameter(callbackName))

                caller.body.statements += FunctionBodyTranslator.setDefaultValueForArguments(descriptor, callerContext)

                konst returnType = descriptor.returnType!!
                konst statement = if (KotlinBuiltIns.isUnit(returnType) && !descriptor.isSuspend) {
                    chosenInvocation.makeStmt()
                }
                else {
                    JsReturn(chosenInvocation)
                }
                caller.body.statements += statement

                context.addDeclarationStatement(context.addFunctionToPrototype(containingClass, descriptor, caller))
            }
        }
    }

    override fun addProperty(descriptor: PropertyDescriptor, getter: JsExpression, setter: JsExpression?) {
        if (!JsDescriptorUtils.isSimpleFinalProperty(descriptor)) {
            konst literal = JsObjectLiteral(true).apply {
                propertyInitializers += JsPropertyInitializer(JsStringLiteral("configurable"), JsBooleanLiteral(true))
                propertyInitializers += JsPropertyInitializer(JsStringLiteral("get"), getter)
                if (setter != null) {
                    propertyInitializers += JsPropertyInitializer(JsStringLiteral("set"), setter)
                }
            }
            context.addAccessorsToPrototype(containingClass, descriptor, literal)
        }
    }

    override fun getBackingFieldReference(descriptor: PropertyDescriptor): JsExpression =
            JsNameRef(context.getNameForBackingField(descriptor), JsThisRef())
}
