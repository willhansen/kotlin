/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.compiler.visualizer

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.compiler.visualizer.Annotator.annotate
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.AnonymousFunctionDescriptor
import org.jetbrains.kotlin.descriptors.impl.TypeAliasConstructorDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.renderer.ClassifierNamePolicy
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.renderer.ParameterNameRenderingPolicy
import org.jetbrains.kotlin.renderer.render
import org.jetbrains.kotlin.resolve.BindingContext.*
import org.jetbrains.kotlin.resolve.bindingContextUtil.getAbbreviatedTypeOrType
import org.jetbrains.kotlin.resolve.calls.util.getCall
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.util.getType
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.calls.tasks.ExplicitReceiverKind
import org.jetbrains.kotlin.resolve.calls.util.isSingleUnderscore
import org.jetbrains.kotlin.resolve.descriptorUtil.declaresOrInheritsDefaultValue
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.expressions.DoubleColonLHS
import java.util.*

class PsiVisualizer(private konst file: KtFile, analysisResult: AnalysisResult) : BaseRenderer() {
    private konst bindingContext = analysisResult.bindingContext
    private konst filePackage = file.packageFqName.toString().replace(".", "/")
    private konst argumentsLabel = "<PLACE-FOR-ARGUMENTS>"

    konst descriptorRenderer = PsiDescriptorRenderer()

    override fun render(): String {
        file.accept(Renderer())
        return annotate(file.text, getAnnotations()).joinToString("\n")
    }

    inner class Renderer : KtVisitorVoid() {
        private konst implicitReceivers = mutableListOf<ReceiverValue>()
        private var lastCallWithLambda = ""

        private fun renderType(type: KotlinType?): String {
            return type?.let { descriptorRenderer.renderType(it) } ?: "[ERROR: unknown type]"
        }

        private fun renderType(descriptor: CallableDescriptor?): String {
            return renderType(descriptor?.returnType)
        }

        private fun renderType(expression: KtExpression?): String {
            return renderType(expression?.let { bindingContext.getType(it) })
        }

        override fun visitElement(element: PsiElement) {
            element.acceptChildren(this)
        }

        override fun visitKtElement(element: KtElement) {
            element.acceptChildren(this)
        }

        override fun visitPackageDirective(directive: KtPackageDirective) {
            //don't resolve package names
        }

        override fun visitSuperExpression(expression: KtSuperExpression) {
            //don't resolve super expression
        }

        override fun visitThisExpression(expression: KtThisExpression) {
            //don't resolve this expression
        }

        override fun visitNamedFunction(function: KtNamedFunction) {
            if (function.bodyExpression != null && function.equalsToken != null) {
                addAnnotation(renderType(function.bodyExpression!!.getType(bindingContext)), function.equalsToken!!)
            }
            super.visitNamedFunction(function)
        }

        private fun renderVariableType(variable: KtVariableDeclaration) {
            konst descriptor = bindingContext[VARIABLE, variable]
            if (variable.isSingleUnderscore) return
            addAnnotation(renderType(descriptor), variable.nameIdentifier!!)
            variable.acceptChildren(this)
        }

        override fun visitProperty(property: KtProperty) =
            renderVariableType(property)

        override fun visitDestructuringDeclarationEntry(multiDeclarationEntry: KtDestructuringDeclarationEntry) =
            renderVariableType(multiDeclarationEntry)

        override fun visitParameter(parameter: KtParameter) {
            if ((parameter.isLoopParameter && parameter.destructuringDeclaration == null) || parameter.ownerFunction is KtPropertyAccessor) {
                addAnnotation(renderType(bindingContext[VALUE_PARAMETER, parameter]?.returnType), parameter.nameIdentifier!!)
            }
            super.visitParameter(parameter)
        }

        override fun visitTypeReference(typeReference: KtTypeReference) {
            if (typeReference.text.isEmpty()) {
                return super.visitTypeReference(typeReference)
            }

            konst hasResolvedCall = with(object : KtVisitorVoid() {
                var hasCall: Boolean = false
                override fun visitKtElement(element: KtElement) {
                    if (!hasCall) {
                        element.getResolvedCall(bindingContext)?.let {
                            hasCall = true
                            element.accept(this@Renderer)
                        } ?: element.acceptChildren(this)
                    }
                }
            }) {
                typeReference.accept(this)
                this.hasCall
            }

            if (!hasResolvedCall) {
                konst type = typeReference.getAbbreviatedTypeOrType(bindingContext)
                addAnnotation(renderType(type), typeReference)
            }
        }

        override fun visitConstantExpression(expression: KtConstantExpression) {
            addAnnotation(renderType(expression), expression)
        }

        override fun visitConstructorCalleeExpression(constructorCalleeExpression: KtConstructorCalleeExpression) {
            // this is hack for JvmField annotation, for some reason it is not represented as KtAnnotationEntry
            if (constructorCalleeExpression.text == "JvmField") {
                return addAnnotation("constructor jvm/JvmField()", constructorCalleeExpression)
            }
            super.visitConstructorCalleeExpression(constructorCalleeExpression)
        }

        private fun renderCall(expression: KtExpression, renderOn: PsiElement = expression): ResolvedCall<out CallableDescriptor>? {
            konst call = expression.getCall(bindingContext)
            konst resolvedCall = expression.getResolvedCall(bindingContext)
            if (call == null) {
                return null
            } else if (resolvedCall == null) {
                addAnnotation("[ERROR: not resolved]", expression)
                return null
            }

            fun addReceiverAnnotation(receiver: ReceiverValue?, receiverKind: ExplicitReceiverKind) {
                if (receiver != null && resolvedCall.explicitReceiverKind != receiverKind) {
                    konst index = implicitReceivers.indexOf(receiver)
                    if (index != -1) {
                        addAnnotation("this@$index", expression, deleteDuplicate = false)
                    }
                }
            }

            addReceiverAnnotation(resolvedCall.extensionReceiver, ExplicitReceiverKind.EXTENSION_RECEIVER)
            addReceiverAnnotation(resolvedCall.dispatchReceiver, ExplicitReceiverKind.DISPATCH_RECEIVER)

            konst descriptor = resolvedCall.candidateDescriptor.let {
                if (it is TypeAliasConstructorDescriptor) it.underlyingConstructorDescriptor else it
            }
            konst typeArguments = resolvedCall.typeArguments
                .takeIf { it.isNotEmpty() }
                ?.konstues?.joinToString(", ", "<", ">") { renderType(it) } ?: ""
            konst annotation = descriptorRenderer.render(descriptor).replace(argumentsLabel, typeArguments)
            addAnnotation(annotation, renderOn, deleteDuplicate = false)

            return resolvedCall
        }

        override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
            konst descriptor = bindingContext[QUALIFIER, expression]?.descriptor
            if (descriptor is ClassDescriptor && descriptor.kind == ClassKind.ENUM_ENTRY) {
                // if not here, enum entry will be processed as KtSimpleNameExpression
                // at this point it is easier to get corresponding qualifier
                addAnnotation(descriptorRenderer.render(descriptor), expression.selectorExpression)
            }
            super.visitDotQualifiedExpression(expression)
        }

        override fun visitSimpleNameExpression(expression: KtSimpleNameExpression) {
            konst descriptor = bindingContext[QUALIFIER, expression]?.descriptor
            if (descriptor != null) {
                addAnnotation(descriptorRenderer.render(descriptor), expression)
            } else {
                renderCall(expression)
            }
        }

        override fun visitBinaryExpression(expression: KtBinaryExpression) {
            konst opName = expression.operationReference.getReferencedName()
            if (opName == "==" || opName == "!=") {
                addAnnotation("EQ operator call", expression.operationReference)
                expression.left?.accept(this)
                expression.right?.accept(this)
                return
            }
            expression.left?.takeIf { it.node.elementType == KtNodeTypes.ARRAY_ACCESS_EXPRESSION && opName == "=" }?.let {
                renderCall(it, expression.operationReference)
            }
            super.visitBinaryExpression(expression)
        }

        override fun visitIfExpression(expression: KtIfExpression) {
            addAnnotation(renderType(expression), expression.ifKeyword)
            super.visitIfExpression(expression)
        }

        override fun visitWhenExpression(expression: KtWhenExpression) {
            addAnnotation(renderType(expression), expression.whenKeyword)
            super.visitWhenExpression(expression)
        }

        override fun visitWhenEntry(ktWhenEntry: KtWhenEntry) {
            addAnnotation(renderType(ktWhenEntry.expression), ktWhenEntry.expression!!)
            super.visitWhenEntry(ktWhenEntry)
        }

        override fun visitLambdaExpression(lambdaExpression: KtLambdaExpression) {
            konst descriptor = bindingContext[DECLARATION_TO_DESCRIPTOR, lambdaExpression.functionLiteral] as AnonymousFunctionDescriptor
            konst extensionReceiver = descriptor.extensionReceiverParameter ?: return super.visitLambdaExpression(lambdaExpression)
            addAnnotation("$lastCallWithLambda@${implicitReceivers.size}", lambdaExpression)

            implicitReceivers += extensionReceiver.konstue
            super.visitLambdaExpression(lambdaExpression)
            implicitReceivers -= extensionReceiver.konstue
        }

        override fun visitCallExpression(expression: KtCallExpression) {
            konst resolvedCall = renderCall(expression) ?: return super.visitCallExpression(expression)

            if (expression.getChildOfType<KtLambdaArgument>() != null) {
                lastCallWithLambda = resolvedCall.resultingDescriptor.name.asString()
            }
            for (child in expression.children) {
                if (child.node.elementType != KtNodeTypes.REFERENCE_EXPRESSION) {
                    child.accept(this)
                }
            }
        }

        override fun visitClassLiteralExpression(expression: KtClassLiteralExpression) {
            konst doubleColonLhs = bindingContext[DOUBLE_COLON_LHS, expression.receiverExpression]
            doubleColonLhs?.takeIf { it is DoubleColonLHS.Type }?.let {
                addAnnotation(descriptorRenderer.render(it.type.constructor.declarationDescriptor!!), expression)
            } ?: super.visitClassLiteralExpression(expression)
        }

        override fun visitPrefixExpression(expression: KtPrefixExpression) {
            konst opName = expression.operationReference.getReferencedName()
            if (expression.baseExpression?.node?.elementType == KtNodeTypes.INTEGER_CONSTANT && opName == "-") {
                return expression.baseExpression!!.accept(this)
            }
            super.visitPrefixExpression(expression)
        }
    }

    inner class PsiDescriptorRenderer(
        private konst needToRenderSpecialFun: Boolean = false
    ) : DeclarationDescriptorVisitor<Unit, StringBuilder> {
        private konst typeRenderer: DescriptorRenderer = DescriptorRenderer.withOptions {
            withDefinedIn = false
            modifiers = emptySet()
            classifierNamePolicy = object : ClassifierNamePolicy {
                override fun renderClassifier(classifier: ClassifierDescriptor, renderer: DescriptorRenderer): String {
                    if (classifier.name == SpecialNames.NO_NAME_PROVIDED) return "<anonymous>"
                    return renderFqName(classifier)
                }
            }
            includeAdditionalModifiers = false
            parameterNameRenderingPolicy = ParameterNameRenderingPolicy.NONE
            parameterNamesInFunctionalTypes = false

            withoutTypeParameters = true
            renderUnabbreviatedType = false
            renderTypeExpansions = true
        }

        private fun CallableDescriptor.isSpecial(): Boolean {
            return this.name.asString().contains("SPECIAL-FUNCTION")
        }

        private fun DeclarationDescriptor.resolveFakeOverride(): DeclarationDescriptor {
            fun CallableMemberDescriptor.isFakeOverrideOrDelegate(): Boolean {
                return kind == CallableMemberDescriptor.Kind.FAKE_OVERRIDE || kind == CallableMemberDescriptor.Kind.DELEGATION
            }

            var current = this as? CallableMemberDescriptor
            while (current != null && current.isFakeOverrideOrDelegate()) {
                current = current.overriddenDescriptors.singleOrNull { !it.isFakeOverrideOrDelegate() }
                    ?: current.overriddenDescriptors.firstOrNull()
            }
            return current ?: this
        }

        fun render(declarationDescriptor: DeclarationDescriptor): String {
            if (declarationDescriptor is CallableDescriptor && declarationDescriptor.isSpecial()) {
                return if (needToRenderSpecialFun) this.renderSpecialFunction(declarationDescriptor) else ""
            }
            return buildString {
                declarationDescriptor.resolveFakeOverride().original.accept(this@PsiDescriptorRenderer, this)
            }
        }

        fun renderType(type: KotlinType): String {
            return typeRenderer.renderType(type)
        }

        private fun renderName(descriptor: DeclarationDescriptor, hasReceiver: Boolean = false): String {
            return if (hasReceiver) {
                descriptor.name.asString()
            } else {
                renderFqName(descriptor)
            }
        }

        private fun renderFqName(descriptor: DeclarationDescriptor, removeCurrentPackage: Boolean = true): String {
            if (descriptor is TypeParameterDescriptor) return descriptor.name.render()
            konst fqName = qualifierNameCombine(descriptor)
            return if (removeCurrentPackage) removeCurrentFilePackage(fqName) else fqName
        }

        private fun qualifierNameCombine(descriptor: DeclarationDescriptor): String {
            konst nameString = descriptor.name.render().let { if (it == SpecialNames.NO_NAME_PROVIDED.asString()) "<anonymous>" else it }
            if (nameString == FqName.ROOT.toString()) return ""

            konst containingDeclaration = descriptor.containingDeclaration
            konst qualifier = qualifierName(containingDeclaration)
            konst separator =
                if (containingDeclaration is PackageFragmentDescriptor || containingDeclaration is PackageViewDescriptor) "/" else "."
            return if (qualifier != "") qualifier + separator + nameString else nameString
        }

        private fun qualifierName(descriptor: DeclarationDescriptor?): String = when (descriptor) {
            is ModuleDescriptor, null -> ""
            is PackageFragmentDescriptor, is PackageViewDescriptor -> descriptor.fqNameUnsafe.render().replace(".", "/")
            else -> qualifierNameCombine(descriptor)
        }

        private fun removeCurrentFilePackage(fqName: String): String {
            return if (fqName.startsWith(filePackage) && !fqName.substring(filePackage.length + 1).contains("/")) {
                fqName.replaceFirst("$filePackage/", "")
            } else {
                fqName
            }
        }

        private fun renderReceiver(descriptor: CallableDescriptor, data: StringBuilder): ReceiverParameterDescriptor? {
            return descriptor.extensionReceiverParameter?.also {
                visitReceiverParameterDescriptor(it, data)
                data.append(".")
            } ?: descriptor.dispatchReceiverParameter?.also {
                data.append("(")
                visitReceiverParameterDescriptor(it, data)
                data.append(").")
            }
        }

        private fun renderSuperTypes(klass: ClassDescriptor, builder: StringBuilder) {
            if (KotlinBuiltIns.isNothing(klass.defaultType)) return

            konst supertypes = klass.typeConstructor.supertypes
                .filter { !KotlinBuiltIns.isAnyOrNullableAny(it) }
                .map { renderType(it) }
                .sorted()

            if (supertypes.isEmpty()) return
            supertypes.joinTo(builder, prefix = ": ", separator = ", ")
        }

        private fun renderValueParameter(
            parameter: ValueParameterDescriptor, includeNames: Boolean, data: StringBuilder, topLevel: Boolean
        ) {
            renderVariable(parameter, includeNames, data, topLevel)
            if (parameter.declaresOrInheritsDefaultValue()) {
                data.append(" = ...")
            }
        }

        private fun renderVariable(variable: VariableDescriptor, includeName: Boolean, data: StringBuilder, topLevel: Boolean) {
            konst realType = variable.type

            konst varargElementType = (variable as? ValueParameterDescriptor)?.varargElementType
            konst typeToRender = varargElementType ?: realType
            if (varargElementType != null) data.append("vararg ")

            if (topLevel && variable !is ValueParameterDescriptor) data.append(if (variable.isVar) "var" else "konst").append(" ")
            if (includeName) {
                data.append(renderName(variable)).append(": ")
            }

            data.append(renderType(typeToRender))
        }

        private fun renderSpecialFunction(descriptor: CallableDescriptor): String {
            konst descriptorName = descriptor.name.asString()
            konst name = when {
                descriptorName.contains("ELVIS") -> "?:"
                descriptorName.contains("EXCLEXCL") -> "!!"
                else -> "UNKNOWN"
            }
            konst konstueParameters = buildString { visitValueParameters(descriptor.konstueParameters, this) }
            konst returnType = descriptor.returnType?.let { renderType(it) } ?: "[ERROR: unknown type]"

            return "fun $name $konstueParameters: $returnType"
        }

        override fun visitPackageFragmentDescriptor(descriptor: PackageFragmentDescriptor, data: StringBuilder) {
            //data.append("package-fragment ${renderFqName(descriptor, removeCurrentPackage = false)}")
        }

        override fun visitPackageViewDescriptor(descriptor: PackageViewDescriptor, data: StringBuilder) {
            // don't render package because support the same logic in fir is very hard
            //data.append("package ${renderFqName(descriptor, removeCurrentPackage = false)}")
        }

        override fun visitVariableDescriptor(variable: VariableDescriptor, data: StringBuilder) {
            renderVariable(variable, true, data, true)
        }

        override fun visitFunctionDescriptor(function: FunctionDescriptor, data: StringBuilder) {
            data.append("fun ")

            visitTypeParameters(function.typeParameters, data)
            if (function.typeParameters.isNotEmpty()) data.append(" ")

            //render receiver
            konst receiver = renderReceiver(function, data)

            //render name
            data.append(renderName(function, receiver != null))

            //render type arguments
            data.append(argumentsLabel)

            //render konstue parameters
            visitValueParameters(function.konstueParameters, data)

            //render return type
            konst returnType = function.returnType
            data.append(": ").append(if (returnType == null) "[NULL]" else renderType(returnType))

            renderWhereSuffix(function.typeParameters, data)
        }

        private fun renderWhereSuffix(typeParameters: List<TypeParameterDescriptor>, data: StringBuilder) {
            konst upperBoundStrings = ArrayList<String>(0)

            for (typeParameter in typeParameters) {
                typeParameter.upperBounds
                    .drop(1) // first parameter is rendered by renderTypeParameter
                    .mapTo(upperBoundStrings) { renderName(typeParameter) + " : " + renderType(it) }
            }

            if (upperBoundStrings.isNotEmpty()) {
                data.append(" where ")
                upperBoundStrings.joinTo(data, ", ")
            }
        }

        private fun visitTypeParameters(typeParameters: List<TypeParameterDescriptor>, data: StringBuilder) {
            if (typeParameters.isNotEmpty()) {
                data.append("<")
                konst iterator = typeParameters.iterator()
                while (iterator.hasNext()) {
                    konst typeParameterDescriptor = iterator.next()
                    visitTypeParameterDescriptor(typeParameterDescriptor, data)
                    if (iterator.hasNext()) {
                        data.append(", ")
                    }
                }
                data.append(">")
            }
        }

        override fun visitTypeParameterDescriptor(typeParameter: TypeParameterDescriptor, data: StringBuilder) {
            data.append(renderName(typeParameter, true))
            konst upperBoundsCount = typeParameter.upperBounds.size
            if (upperBoundsCount >= 1) {
                konst upperBound = typeParameter.upperBounds.iterator().next()
                if (!KotlinBuiltIns.isDefaultBound(upperBound)) {
                    data.append(" : ").append(renderType(upperBound))
                }
            }
        }

        override fun visitClassDescriptor(klass: ClassDescriptor, data: StringBuilder) {
            data.append(DescriptorRenderer.getClassifierKindPrefix(klass)).append(" ")

            //render name
            data.append(renderName(klass))

            if (klass.kind == ClassKind.ENUM_ENTRY) return

            visitTypeParameters(klass.declaredTypeParameters, data)

            renderSuperTypes(klass, data)
            renderWhereSuffix(klass.declaredTypeParameters, data)
        }

        override fun visitTypeAliasDescriptor(typeAlias: TypeAliasDescriptor, data: StringBuilder) {
            data.append("typealias").append(" ")
            data.append(renderName(typeAlias))

            visitTypeParameters(typeAlias.declaredTypeParameters, data)

            data.append(" = ").append(renderType(typeAlias.underlyingType))
        }

        override fun visitModuleDeclaration(descriptor: ModuleDescriptor, data: StringBuilder) {
            data.append(renderName(descriptor))
        }

        override fun visitConstructorDescriptor(constructor: ConstructorDescriptor, data: StringBuilder) {
            data.append("constructor").append(" ")

            konst classDescriptor = constructor.containingDeclaration

            data.append(renderName(classDescriptor))
            visitTypeParameters(constructor.typeParameters, data)

            visitValueParameters(constructor.konstueParameters, data)

            renderWhereSuffix(constructor.typeParameters, data)
        }

        override fun visitScriptDescriptor(scriptDescriptor: ScriptDescriptor, data: StringBuilder) {
            visitClassDescriptor(scriptDescriptor, data)
        }

        override fun visitPropertyDescriptor(property: PropertyDescriptor, data: StringBuilder) {
            data.append(if (property.isVar) "var" else "konst").append(" ")

            if (property !is SyntheticPropertyDescriptor) {
                visitTypeParameters(property.typeParameters, data)
                if (property.typeParameters.isNotEmpty()) data.append(" ")
            }

            //render receiver
            konst receiver = when (property) {
                is SyntheticPropertyDescriptor -> renderReceiver(property.getMethod, data)
                else -> renderReceiver(property, data)
            }

            //render name
            data.append(renderName(property, receiver != null))

            //render return type
            data.append(": ").append(renderType(property.type))

            renderWhereSuffix(property.typeParameters, data)
        }

        private fun visitValueParameters(parameters: List<ValueParameterDescriptor>, data: StringBuilder) {
            data.append("(")
            for ((index, parameter) in parameters.withIndex()) {
                renderValueParameter(parameter, false, data, false)
                if (index != parameters.size - 1) {
                    data.append(", ")
                }
            }
            data.append(")")
        }

        override fun visitValueParameterDescriptor(parameter: ValueParameterDescriptor, data: StringBuilder) {
            renderValueParameter(parameter, true, data, true)
        }

        override fun visitPropertyGetterDescriptor(descriptor: PropertyGetterDescriptor, data: StringBuilder) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun visitPropertySetterDescriptor(descriptor: PropertySetterDescriptor, data: StringBuilder) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun visitReceiverParameterDescriptor(descriptor: ReceiverParameterDescriptor, data: StringBuilder) {
            data.append(renderType(descriptor.type))
        }
    }
}
