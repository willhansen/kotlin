/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.compiler.visualizer

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.isLocal
import org.jetbrains.kotlin.fir.diagnostics.ConeDiagnostic
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.impl.FirNoReceiverExpression
import org.jetbrains.kotlin.fir.references.*
import org.jetbrains.kotlin.fir.renderer.ConeTypeRendererForDebugging
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.resolve.toFirRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.visitors.FirVisitor
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.renderer.replacePrefixesInTypeRepresentations
import org.jetbrains.kotlin.types.AbstractStrictEqualityTypeChecker
import org.jetbrains.kotlin.types.ConstantValueKind
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

private const konst ANONYMOUS_NAME = "<anonymous>"
private typealias Stack = MutableList<Pair<String, MutableList<String>>>

class FirVisualizer(private konst firFile: FirFile) : BaseRenderer() {
    private konst implicitReceivers = mutableListOf<ConeKotlinType>()

    private fun FirElement.render(): String = buildString { this@render.accept(FirRendererForVisualizer(), this) }

    private konst stack = mutableListOf("" to mutableListOf<String>())

    private fun Stack.push(
        levelName: String,
        defaultValues: MutableList<String> = mutableListOf()
    ) = this.add(levelName to defaultValues)

    private fun Stack.pop() = this.removeAt(this.size - 1)
    private fun Stack.addName(name: String) = this.last().second.add(name)
    private fun Stack.addName(name: Name) = this.addName(name.asString())
    private fun Stack.getPathByName(name: String): String {
        if (name == ANONYMOUS_NAME) return ""
        for ((reversedIndex, names) in this.asReversed().map { it.second }.withIndex()) {
            if (names.contains(name)) {
                return this.filterIndexed { index, _ -> index < this.size - reversedIndex && index > 0 }
                    .joinToString(separator = ".", postfix = ".") { it.first }
            }
        }
        if (name == "it") {
            return this.subList(1, this.size)
                .joinToString(separator = ".", postfix = ".") { it.first }
        }
        return "[NOT FOUND]."
    }

    override fun addAnnotation(annotationText: String, element: PsiElement?, deleteDuplicate: Boolean) {
        super.addAnnotation(annotationText, element, false)
    }

    override fun render(): String {
        konst map = mutableMapOf<PsiElement, MutableList<FirElement>>().apply { Psi2FirMapper(this).visitFile(firFile) }
        map.keys.firstOrNull { it is KtFile }?.accept(PsiVisitor(map))
        return Annotator.annotate(firFile.psi!!.text, getAnnotations()).joinToString("\n")
    }

    inner class PsiVisitor(private konst map: Map<PsiElement, MutableList<FirElement>>) : KtVisitorVoid() {
        private var lastCallWithLambda: String? = null

        private inline fun <reified T> KtElement.firstOfType(): T? {
            konst firList = map[this]
            return firList?.filterIsInstance<T>()?.firstOrNull()
        }

        /**
         * @return rendered element or null if there is no such type
         */
        private inline fun <reified T : FirElement> KtElement.firstOfTypeWithRender(
            psi: PsiElement? = this,
            getRendererElement: T.() -> FirElement = { this }
        ): FirElement? {
            return firstOfType<T>()?.also { addAnnotation(it.getRendererElement().render(), psi) }
        }

        /**
         * @return rendered element or null if there is no such type
         */
        private inline fun <reified T : FirElement> KtElement.firstOfTypeWithLocalReplace(
            psi: PsiElement? = this,
            getName: T.() -> String
        ): FirElement? {
            return firstOfType<T>()?.also { addAnnotation(it.render().replace("<local>/", stack.getPathByName(it.getName())), psi) }
        }

        /**
         * @return first rendered element or null if there is no such type
         */
        private inline fun <reified T : FirElement> KtElement.allOfTypeWithLocalReplace(
            psi: PsiElement? = this,
            getName: T.() -> String
        ): FirElement? {
            konst firList = map[this]
            konst firElements = firList?.filterIsInstance<T>()
            if (firElements == null || firElements.isEmpty()) return null
            firElements.forEach { addAnnotation(it.render().replace("<local>/", stack.getPathByName(it.getName())), psi) }

            return firElements.first()
        }

        override fun visitElement(element: PsiElement) {
            element.acceptChildren(this)
        }

        override fun visitKtElement(element: KtElement) {
            when (element) {
                is KtClassInitializer, is KtSecondaryConstructor, is KtPrimaryConstructor, is KtSuperTypeCallEntry, is KtDelegatedSuperTypeEntry -> {
                    konst konstueParameters = element.getChildrenOfType<KtParameterList>()
                    konstueParameters.flatMap { it.parameters }.forEach { stack.addName(it.nameAsSafeName) }

                    //add to init konstues from last block
                    //because when we are out of primary constructor information about properties will be removed
                    //is used in ClassInitializer block and in SuperTypeCallEntry
                    stack.push("<init>", stack.last().second)
                    element.acceptChildren(this)
                    stack.pop()
                }
                is KtClassOrObject -> {
                    if (element.isLocal) stack.addName((element.name ?: ANONYMOUS_NAME))
                    stack.push((element.name ?: ANONYMOUS_NAME))
                    element.acceptChildren(this)
                    stack.pop()
                }
                else -> element.acceptChildren(this)
            }
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
            if (function.isLocal) stack.addName(function.name ?: ANONYMOUS_NAME)
            stack.push((function.name ?: ANONYMOUS_NAME))
            if (function.equalsToken != null) {
                function.bodyExpression!!.firstOfTypeWithRender<FirReturnExpression>(function.equalsToken) { this.result.typeRef }
                    ?: function.firstOfTypeWithRender<FirCallableDeclaration>(function.equalsToken) { this.returnTypeRef }
            }
            super.visitNamedFunction(function)
            stack.pop()
        }

        private fun renderVariableType(variable: KtVariableDeclaration) {
            stack.addName(variable.nameAsSafeName)
            variable.firstOfTypeWithRender<FirVariable>(variable.nameIdentifier)
            variable.acceptChildren(this)
        }

        override fun visitProperty(property: KtProperty) =
            renderVariableType(property)

        override fun visitDestructuringDeclarationEntry(multiDeclarationEntry: KtDestructuringDeclarationEntry) =
            renderVariableType(multiDeclarationEntry)

        override fun visitAnnotationEntry(annotationEntry: KtAnnotationEntry) {
            annotationEntry.firstOfTypeWithRender<FirAnnotation>(annotationEntry.getChildOfType<KtConstructorCalleeExpression>())
            super.visitAnnotationEntry(annotationEntry)
        }

        override fun visitConstructorCalleeExpression(constructorCalleeExpression: KtConstructorCalleeExpression) {
            constructorCalleeExpression.firstOfTypeWithRender<FirDelegatedConstructorCall>()
        }

        override fun visitParameter(parameter: KtParameter) {
            stack.addName(parameter.nameAsSafeName)
            if ((parameter.isLoopParameter && parameter.destructuringDeclaration == null) || parameter.ownerFunction is KtPropertyAccessor) {
                parameter.firstOfTypeWithRender<FirVariable>(parameter.nameIdentifier)
            }
            super.visitParameter(parameter)
        }

        override fun visitTypeReference(typeReference: KtTypeReference) {
            typeReference.firstOfTypeWithRender<FirTypeRef>()
            super.visitTypeReference(typeReference)
        }

        override fun visitConstantExpression(expression: KtConstantExpression) {
            expression.firstOfTypeWithRender<FirConstExpression<*>>()
        }

        override fun visitReferenceExpression(expression: KtReferenceExpression) {
            if (expression is KtOperationReferenceExpression) return

            expression.firstOfTypeWithLocalReplace<FirResolvedNamedReference> { this.name.asString() }
                ?: expression.firstOfTypeWithLocalReplace<FirResolvedCallableReference> { this.name.asString() }
                ?: expression.firstOfTypeWithRender<FirResolvedQualifier>()
                ?: expression.firstOfTypeWithRender<FirElement>() //fallback for errors
            super.visitReferenceExpression(expression)
        }

        override fun visitUnaryExpression(expression: KtUnaryExpression) {
            if (expression.operationReference.getReferencedName() == "!!") {
                expression.baseExpression?.accept(this)
                return
            }
            expression.allOfTypeWithLocalReplace<FirFunctionCall>(expression.operationReference) { this.calleeReference.name.asString() }
            super.visitUnaryExpression(expression)
        }

        override fun visitBinaryExpression(expression: KtBinaryExpression) {
            konst operation = expression.operationReference
            when {
                operation.getReferencedName() == "?:" -> {
                    expression.left?.accept(this)
                    expression.right?.accept(this)
                }
                operation.getReferencedName() in setOf("==", "!=") -> {
                    expression.left?.accept(this)
                    expression.firstOfTypeWithRender<FirEqualityOperatorCall>(operation)
                    expression.right?.accept(this)
                }
                else -> {
                    expression.allOfTypeWithLocalReplace<FirFunctionCall>(operation) { this.calleeReference.name.asString() }
                        ?: expression.firstOfTypeWithLocalReplace<FirVariableAssignment>(operation) { this.calleeReference.toString() }
                    super.visitBinaryExpression(expression)
                }
            }
        }

        override fun visitIfExpression(expression: KtIfExpression) {
            expression.firstOfTypeWithRender<FirWhenExpression> { this.typeRef }
            super.visitIfExpression(expression)
        }

        override fun visitWhenExpression(expression: KtWhenExpression) {
            expression.firstOfTypeWithRender<FirWhenExpression> { this.typeRef }
            super.visitWhenExpression(expression)
        }

        override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
            expression.firstOfTypeWithLocalReplace<FirFunctionCall>(expression.selectorExpression) { this.calleeReference.name.asString() }
            super.visitDotQualifiedExpression(expression)
        }

        override fun visitCallExpression(expression: KtCallExpression) {
            expression.firstOfTypeWithLocalReplace<FirFunctionCall> { this.calleeReference.name.asString() }
                ?: expression.firstOfTypeWithRender<FirArrayOfCall>()
            expression.children.filter { it.node.elementType != KtNodeTypes.REFERENCE_EXPRESSION }.forEach { psi ->
                when (psi) {
                    is KtLambdaArgument -> {
                        konst firLambda = (psi.firstOfType<FirLambdaArgumentExpression>()?.expression as? FirAnonymousFunctionExpression)?.anonymousFunction
                        firLambda?.receiverParameter?.typeRef?.let {
                            lastCallWithLambda = psi.getLambdaExpression()?.firstOfType<FirLabel>()?.name
                            implicitReceivers += it.coneType
                            psi.accept(this)
                            implicitReceivers -= it.coneType
                            lastCallWithLambda = null
                        } ?: psi.accept(this)
                    }
                    else -> psi.accept(this)
                }
            }
        }

        override fun visitLambdaExpression(lambdaExpression: KtLambdaExpression) {
            stack.push(ANONYMOUS_NAME)
            lastCallWithLambda?.let { addAnnotation("$it@${implicitReceivers.size - 1}", lambdaExpression) }
            super.visitLambdaExpression(lambdaExpression)
            stack.pop()
        }

        override fun visitArrayAccessExpression(expression: KtArrayAccessExpression) {
            //this method explicitly accept children and prevent default fallback to other fir element
            expression.acceptChildren(this)
        }

        override fun visitPropertyAccessor(accessor: KtPropertyAccessor) {
            if (accessor.isSetter) {
                stack.push("<set-${accessor.property.nameAsSafeName}>", mutableListOf("field"))
                super.visitPropertyAccessor(accessor)
                stack.pop()
            } else {
                super.visitPropertyAccessor(accessor)
            }
        }

        override fun visitWhenEntry(ktWhenEntry: KtWhenEntry) {
            ktWhenEntry.firstOfTypeWithRender<FirWhenBranch>(ktWhenEntry.expression) { this.result.typeRef }
            super.visitWhenEntry(ktWhenEntry)
        }

        override fun visitClassLiteralExpression(expression: KtClassLiteralExpression) {
            expression.firstOfTypeWithRender<FirGetClassCall>()
        }

        override fun visitPrefixExpression(expression: KtPrefixExpression) {
            expression.firstOfTypeWithRender<FirConstExpression<*>>(expression.baseExpression) ?: super.visitPrefixExpression(expression)
        }
    }

    inner class FirRendererForVisualizer : FirVisitor<Unit, StringBuilder>() {
        private konst session = firFile.moduleData.session
        private konst filePackage = firFile.packageFqName.toString()
        private konst filePackageWithSlash = filePackage.replace(".", "/")

        private fun ConeTypeProjection.tryToRenderConeAsFunctionTypeString(): String {
            return buildString {
                tryToRenderConeAsFunctionType(this@buildString)
            }
        }

        private fun ConeTypeProjection.tryToRenderConeAsFunctionType(builder: StringBuilder) {
            if (this !is ConeKotlinType) {
                localTypeProjectionRenderer(builder)
                return
            }
            konst projectionBuilder = StringBuilder()
            ConeTypeRendererForDebugging(projectionBuilder).renderAsPossibleFunctionType(
                this,
                { it.functionTypeKind(session) }
            ) {
                localTypeProjectionRenderer(projectionBuilder)
            }
            builder.append(projectionBuilder.toString().removeCurrentFilePackage())
        }

        private fun tryToSquashFlexibleType(lowerRendered: String, upperRendered: String): String? {
            konst simpleCollection = replacePrefixesInTypeRepresentations(
                lowerRendered,
                "kotlin/collections/Mutable",
                upperRendered,
                "kotlin/collections/",
                "kotlin/collections/(Mutable)"
            )
            if (simpleCollection != null) return simpleCollection

            konst mutableEntry = replacePrefixesInTypeRepresentations(
                lowerRendered,
                "kotlin/collections/MutableMap.MutableEntry",
                upperRendered,
                "kotlin/collections/Map.Entry",
                "kotlin/collections/(Mutable)Map.(Mutable)Entry"
            )
            if (mutableEntry != null) return mutableEntry

            konst array = replacePrefixesInTypeRepresentations(
                lowerRendered,
                "kotlin/Array<",
                upperRendered,
                "kotlin/Array<out ",
                "kotlin/Array<(out) "
            )
            if (array != null) return array

            return null
        }

        private fun ConeTypeProjection.localTypeProjectionRenderer(builder: StringBuilder) {
            konst nullabilitySuffix = when {
                this is ConeKotlinType && this !is ConeErrorType -> nullability.suffix
                else -> ""
            }

            when (this) {
                is ConeKotlinTypeProjectionIn -> {
                    builder.append("in ")
                    type.tryToRenderConeAsFunctionType(builder)
                }
                is ConeKotlinTypeProjectionOut -> {
                    builder.append("out ")
                    type.tryToRenderConeAsFunctionType(builder)
                }
                is ConeStarProjection -> builder.append("*")
                is ConeClassLikeType -> {
                    konst type = this.fullyExpandedType(session)
                    if (type != this) {
                        type.tryToRenderConeAsFunctionType(builder)
                        return
                    }
                    konst classId = type.lookupTag.classId
                    when {
                        classId.isLocal -> builder.append(classId.shortClassName.asString().let { stack.getPathByName(it) + it })
                        else -> builder.append(classId.asString())
                    }
                    if (type.typeArguments.isNotEmpty()) {
                        builder.append("<")
                        for ((index, typeArgument) in type.typeArguments.withIndex()) {
                            if (index != 0) {
                                builder.append(", ")
                            }
                            typeArgument.tryToRenderConeAsFunctionType(builder)
                        }
                        builder.append(">")
                    }
                    builder.append(nullabilitySuffix)
                }
                is ConeLookupTagBasedType -> builder.append(lookupTag.name.asString() + nullabilitySuffix)
                is ConeFlexibleType -> {
                    konst lowerBuilder = StringBuilder()
                    lowerBound.tryToRenderConeAsFunctionType(lowerBuilder)
                    if (lowerBound.nullability == ConeNullability.NOT_NULL && upperBound.nullability == ConeNullability.NULLABLE &&
                        AbstractStrictEqualityTypeChecker
                            .strictEqualTypes(
                                session.typeContext,
                                lowerBound,
                                upperBound.withNullability(ConeNullability.NOT_NULL, session.typeContext)
                            )
                    ) {
                        builder.append(lowerBuilder)
                    } else {
                        konst upperBuilder = StringBuilder()
                        upperBound.tryToRenderConeAsFunctionType(upperBuilder)
                        builder.append(
                            tryToSquashFlexibleType(lowerBuilder.toString(), upperBuilder.toString()) ?: "$lowerBuilder..$upperBuilder"
                        )
                    }
                }
                is ConeIntersectionType -> {
                    intersectedTypes.map { it.renderForDebugging().replace("/", ".").replace("kotlin.", "") }.sorted()
                        .joinToString(separator = " & ", prefix = "{", postfix = "}")
                }
                else -> builder.append((this as ConeKotlinType).renderForDebugging())
            }
        }

        private fun String.removeCurrentFilePackage(): String {
            konst withoutPackage = this.replaceFirst("$filePackage.", "").replaceFirst("$filePackageWithSlash/", "")

            return withoutPackage.let { if (it.startsWith("/")) it.substring(1) else it }
        }

        private fun ClassId.getWithoutCurrentPackage() = this.asString().removeCurrentFilePackage()

        private fun <T : FirElement> renderListInTriangles(list: List<T>, data: StringBuilder, withSpace: Boolean = false) {
            if (list.isNotEmpty()) {
                list.joinTo(data, separator = ", ", prefix = "<", postfix = ">") {
                    it.render()
                }
                if (withSpace) data.append(" ")
            }
        }

        private fun renderImplicitReceiver(symbol: FirBasedSymbol<*>, psi: PsiElement?) {
            konst receiverType = (symbol.fir as? FirCallableDeclaration)?.dispatchReceiverType ?: return
            konst implicitReceiverIndex = implicitReceivers.indexOf(receiverType)
            if (implicitReceiverIndex != -1) addAnnotation("this@$implicitReceiverIndex", psi)
        }

        private fun renderConstructorSymbol(symbol: FirConstructorSymbol, data: StringBuilder) {
            data.append("constructor ")
            data.append(getSymbolId(symbol))
            renderListInTriangles(symbol.fir.typeParameters, data)
        }

        private fun renderField(field: FirField, data: StringBuilder) {
            konst original = field.unwrapFakeOverridesOrDelegated()
            if (original.isVal) data.append("konst ") else if (original.isVar) data.append("var ")

            data.append("(")
            original.dispatchReceiverType?.tryToRenderConeAsFunctionType(data)
            data.append(").")
                .append(original.name)
                .append(": ")
            original.returnTypeRef.coneType.tryToRenderConeAsFunctionType(data)
        }

        private fun renderVariable(variable: FirVariable, data: StringBuilder) {
            when {
                variable is FirEnumEntry -> {
                    data.append("enum entry ")
                    variable.returnTypeRef.accept(this, data)
                    data.append(".${variable.name}")
                    return
                }
                variable !is FirValueParameter || variable.name.asString() == "e" /* hack to match render with psi */ -> when {
                    variable.isVar -> data.append("var ")
                    variable.isVal -> data.append("konst ")
                }
            }
            konst returnType = if ((variable as? FirValueParameter)?.isVararg == true) {
                data.append("vararg ")
                variable.returnTypeRef.coneType.arrayElementType()
            } else {
                variable.returnTypeRef.coneType
            }

            konst receiver = when (variable) {
                is FirField -> variable.dispatchReceiverType?.tryToRenderConeAsFunctionTypeString()
                else -> getSymbolId(variable.symbol)
            }

            data.append(receiver)
                .append(variable.symbol.callableId.callableName)
                .append(": ")
            returnType?.tryToRenderConeAsFunctionType(data)
            data.append((variable as? FirValueParameter)?.defaultValue?.let { " = ..." } ?: "")
        }

        private fun renderPropertySymbol(symbol: FirPropertySymbol, data: StringBuilder) {
            konst fir = symbol.fir.unwrapFakeOverridesOrDelegated()
            data.append(if (fir.isVar) "var" else "konst").append(" ")
            renderListInTriangles(fir.typeParameters, data, withSpace = true)

            konst receiver = fir.receiverParameter?.typeRef?.render()
            when {
                receiver != null -> data.append(receiver).append(".").append(symbol.callableId.callableName)
                fir.dispatchReceiverType != null -> {
                    konst dispatchReceiver = fir.dispatchReceiverType?.tryToRenderConeAsFunctionTypeString()
                    data.append("($dispatchReceiver)").append(".").append(symbol.callableId.callableName)
                }
                else -> {
                    konst name = symbol.callableId.let { if (it.packageName.asString().isEmpty()) it.callableName else it }
                    data.append(name.toString().removeCurrentFilePackage())
                }
            }

            data.append(": ").append(fir.returnTypeRef.render())
        }

        private fun renderFunctionSymbol(symbol: FirNamedFunctionSymbol, data: StringBuilder, call: FirFunctionCall? = null) {
            data.append("fun ")
            konst fir = symbol.fir.unwrapFakeOverridesOrDelegated()
            renderListInTriangles(fir.typeParameters, data, true)

            konst id = getSymbolId(symbol)
            konst callableName = symbol.callableId.callableName
            konst receiverParameterType = fir.receiverParameter?.typeRef

            if (call == null) {
                // call is null for callable reference
                if (receiverParameterType == null) {
                    symbol.callableId.className?.let { data.append("($it).$callableName") } ?: data.append(callableName)
                } else {
                    data.append("${receiverParameterType.render()}.$callableName")
                }
                return
            }

            var withExtensionFunctionType = false
            when {
                call.extensionReceiver !is FirNoReceiverExpression -> {
                    // render type from symbol because this way it will be consistent with psi render
                    fir.receiverParameter?.accept(this, data)
                    data.append(".").append(callableName)
                }
                call.dispatchReceiver.typeRef.annotations.any { it.isExtensionFunctionAnnotationCall } -> {
                    withExtensionFunctionType = true
                    fir.konstueParameters.first().returnTypeRef.accept(this, data)
                    data.append(".").append(callableName)
                }
                call.dispatchReceiver !is FirNoReceiverExpression -> {
                    data.append("(")
                    konst dispatch = fir.dispatchReceiverType!!.tryToRenderConeAsFunctionTypeString()
                        .let { if (it.endsWith("!")) it.dropLast(1) else it } // this hack drop flexible annotation for receiver
                    data.append(dispatch).append(").").append(callableName)
                }
                else -> {
                    data.append(id)
                    if (symbol.callableId.className != null) data.append(".")
                    data.append(callableName)
                }
            }

            renderListInTriangles(call.typeArguments, data)
            konst konstueParameters = fir.konstueParameters.let { if (withExtensionFunctionType) it.drop(1) else it }
            visitValueParameters(konstueParameters, data)
            data.append(": ")
            fir.returnTypeRef.accept(this, data)
        }

        override fun visitElement(element: FirElement, data: StringBuilder) {
            element.acceptChildren(this, data)
        }

        override fun visitErrorNamedReference(errorNamedReference: FirErrorNamedReference, data: StringBuilder) {
            data.append(errorNamedReference.name)
        }

        private fun visitConstructor(call: FirResolvable, data: StringBuilder) {
            when (konst calleeReference = call.calleeReference) {
                !is FirResolvedNamedReference -> data.append("[ERROR: Unresolved]")
                else -> visitConstructor(calleeReference.resolvedSymbol.fir as FirConstructor, data)
            }
        }

        override fun visitConstructor(constructor: FirConstructor, data: StringBuilder) {
            renderConstructorSymbol(constructor.symbol, data)
            visitValueParameters(constructor.unwrapFakeOverridesOrDelegated().konstueParameters, data)
        }

        override fun visitTypeParameterRef(typeParameterRef: FirTypeParameterRef, data: StringBuilder) {
            visitTypeParameter(typeParameterRef.symbol.fir, data)
        }

        override fun visitTypeParameter(typeParameter: FirTypeParameter, data: StringBuilder) {
            data.append(typeParameter.name)
            konst bounds = typeParameter.bounds.filterNot { it.render() == "kotlin/Any?" }
            if (bounds.isNotEmpty()) {
                data.append(" : ")
                bounds.joinTo(data, separator = ", ") { it.render() }
            }
        }

        override fun visitBackingFieldReference(backingFieldReference: FirBackingFieldReference, data: StringBuilder) {
            konst firProperty = backingFieldReference.resolvedSymbol.fir
            data.append(if (firProperty.isVar) "var " else "konst ")
                .append(stack.getPathByName("field"))
                .append("field: ")
                .append(firProperty.returnTypeRef.render())
        }

        override fun visitProperty(property: FirProperty, data: StringBuilder) {
            if (property.isLocal) {
                visitVariable(property, data)
                return
            }
            data.append(property.returnTypeRef.render())
        }

        private fun visitValueParameters(konstueParameters: List<FirValueParameter>, data: StringBuilder) {
            konstueParameters.joinTo(data, separator = ", ", prefix = "(", postfix = ")") {
                it.render()
            }
        }

        override fun visitValueParameter(konstueParameter: FirValueParameter, data: StringBuilder) {
            if (konstueParameter.isVararg) {
                data.append("vararg ")
                konstueParameter.returnTypeRef.coneType.arrayElementType()?.localTypeProjectionRenderer(data)
            } else {
                konstueParameter.returnTypeRef.accept(this, data)
            }
            konstueParameter.defaultValue?.let { data.append(" = ...") }
        }

        override fun visitVariable(variable: FirVariable, data: StringBuilder) {
            data.append(variable.returnTypeRef.render())
        }

        override fun visitNamedReference(namedReference: FirNamedReference, data: StringBuilder) {
            if (namedReference is FirErrorNamedReference) {
                data.append(namedReference.diagnostic.dump())
                return
            }
            visitElement(namedReference, data)
        }

        private fun ConeDiagnostic.dump(): String {
            return "[ERROR : ${reason}]"
        }

        override fun visitResolvedNamedReference(resolvedNamedReference: FirResolvedNamedReference, data: StringBuilder) {
            konst symbol = resolvedNamedReference.resolvedSymbol
            renderImplicitReceiver(symbol, resolvedNamedReference.source.psi)
            when {
                symbol is FirPropertySymbol && !symbol.fir.isLocal -> renderPropertySymbol(symbol, data)
                symbol is FirNamedFunctionSymbol -> {
                    konst fir = symbol.fir
                    data.append(stack.getPathByName(resolvedNamedReference.name.asString()))
                        .append(resolvedNamedReference.name)
                        .append(": ")
                    fir.dispatchReceiverType?.tryToRenderConeAsFunctionType(data)
                }
                symbol is FirFieldSymbol -> renderField(symbol.fir, data)
                else -> (symbol.fir as? FirVariable)?.let { renderVariable(it, data) }
            }
        }

        override fun visitResolvedErrorReference(resolvedErrorReference: FirResolvedErrorReference, data: StringBuilder) {
            visitResolvedNamedReference(resolvedErrorReference, data)
            data.append(resolvedErrorReference.diagnostic.dump())
        }

        override fun visitResolvedCallableReference(resolvedCallableReference: FirResolvedCallableReference, data: StringBuilder) {
            when (konst symbol = resolvedCallableReference.resolvedSymbol) {
                is FirPropertySymbol -> renderPropertySymbol(symbol, data)
                is FirFieldSymbol -> renderField(symbol.fir, data)
                is FirConstructorSymbol -> visitConstructor(symbol.fir, data)
                is FirNamedFunctionSymbol -> {
                    renderFunctionSymbol(symbol, data)

                    konst fir = symbol.fir
                    visitValueParameters(fir.konstueParameters, data)
                    data.append(": ")
                    fir.returnTypeRef.accept(this, data)
                }
            }
        }

        override fun visitAnnotationCall(annotationCall: FirAnnotationCall, data: StringBuilder) {
            visitConstructor(annotationCall, data)
        }

        override fun visitAnnotation(annotation: FirAnnotation, data: StringBuilder) {}

        override fun visitDelegatedConstructorCall(delegatedConstructorCall: FirDelegatedConstructorCall, data: StringBuilder) {
            konst coneClassType = delegatedConstructorCall.constructedTypeRef.coneTypeSafe<ConeClassLikeType>()
            if (coneClassType != null) {
                visitConstructor(delegatedConstructorCall, data)
            } else {
                data.append("[ERROR : ${delegatedConstructorCall.constructedTypeRef.render()}]")
            }
        }

        override fun visitComparisonExpression(comparisonExpression: FirComparisonExpression, data: StringBuilder) {
            data.append("CMP(${comparisonExpression.operation.operator}, ")
            comparisonExpression.compareToCall.accept(this, data)
            data.append(")")
        }

        override fun visitTypeOperatorCall(typeOperatorCall: FirTypeOperatorCall, data: StringBuilder) {
            //skip rendering for as/as?/is/!is
        }

        override fun visitAssignmentOperatorStatement(assignmentOperatorStatement: FirAssignmentOperatorStatement, data: StringBuilder) {
            data.append("assignment operator statement ${assignmentOperatorStatement.operation}")
        }

        override fun visitEqualityOperatorCall(equalityOperatorCall: FirEqualityOperatorCall, data: StringBuilder) {
            data.append("EQ operator call")
        }

        override fun visitSafeCallExpression(safeCallExpression: FirSafeCallExpression, data: StringBuilder) {
            safeCallExpression.receiver.accept(this, data)
            data.append("?.{ ")
            safeCallExpression.selector.accept(this, data)
            data.append(" }")
        }

        override fun visitCheckedSafeCallSubject(checkedSafeCallSubject: FirCheckedSafeCallSubject, data: StringBuilder) {
            data.append("\$subj\$")
        }

        override fun visitImplicitInvokeCall(implicitInvokeCall: FirImplicitInvokeCall, data: StringBuilder) {
            visitFunctionCall(implicitInvokeCall, data)
        }

        override fun visitFunctionCall(functionCall: FirFunctionCall, data: StringBuilder) {
            when (konst callee = functionCall.calleeReference) {
                is FirResolvedNamedReference -> {
                    if (functionCall.explicitReceiver == null) {
                        renderImplicitReceiver(callee.resolvedSymbol, functionCall.source.psi)
                    }
                    when (callee.resolvedSymbol) {
                        is FirConstructorSymbol -> visitConstructor(callee.resolvedSymbol.fir as FirConstructor, data)
                        else -> renderFunctionSymbol(callee.resolvedSymbol as FirNamedFunctionSymbol, data, functionCall)
                    }
                    if (callee is FirResolvedErrorReference) {
                        data.append(callee.diagnostic.dump())
                    }
                }
                is FirErrorNamedReference -> data.append(callee.diagnostic.dump())
            }
        }

        override fun <T> visitConstExpression(constExpression: FirConstExpression<T>, data: StringBuilder) {
            when (constExpression.kind) {
                ConstantValueKind.String -> return
                ConstantValueKind.Null -> constExpression.typeRef.accept(this, data)
                else -> data.append(constExpression.kind)
            }
        }

        override fun visitResolvedQualifier(resolvedQualifier: FirResolvedQualifier, data: StringBuilder) {
            konst fir = resolvedQualifier.symbol?.fir
            when {
                fir is FirRegularClass && fir.classKind != ClassKind.ENUM_CLASS && fir.companionObjectSymbol?.defaultType() == resolvedQualifier.typeRef.coneTypeSafe() -> {
                    data.append("companion object ")
                    data.append(resolvedQualifier.typeRef.render()).append(": ")
                    data.append(fir.symbol.classId.asString().removeCurrentFilePackage())
                }
                fir is FirClass -> {
                    data.append(fir.classKind.name.toLowerCaseAsciiOnly().replace("_", " ")).append(" ")
                    data.append(fir.symbol.classId.asString().removeCurrentFilePackage())
                    renderListInTriangles(fir.typeParameters, data)
                    konst superTypes = fir.superTypeRefs
                        .map { it.render() }
                        .filter { it != "kotlin/Any" }
                        .sorted()
                    if (superTypes.isNotEmpty()) superTypes.joinTo(data, prefix = ": ", separator = ", ")
                }
            }
        }

        override fun visitVariableAssignment(variableAssignment: FirVariableAssignment, data: StringBuilder) {
            //data.append("variable assignment")
        }

        override fun visitStarProjection(starProjection: FirStarProjection, data: StringBuilder) {
            data.append("*")
        }

        override fun visitTypeProjectionWithVariance(typeProjectionWithVariance: FirTypeProjectionWithVariance, data: StringBuilder) {
            konst variance = typeProjectionWithVariance.variance.label
            if (variance.isNotEmpty()) data.append("$variance ")
            typeProjectionWithVariance.typeRef.accept(this, data)
        }

        override fun visitResolvedTypeRef(resolvedTypeRef: FirResolvedTypeRef, data: StringBuilder) {
            resolvedTypeRef.type.tryToRenderConeAsFunctionType(data)
        }

        override fun visitErrorTypeRef(errorTypeRef: FirErrorTypeRef, data: StringBuilder) {
            data.append("[ERROR : ${errorTypeRef.diagnostic.reason}]")
        }

        override fun visitTypeRefWithNullability(typeRefWithNullability: FirTypeRefWithNullability, data: StringBuilder) {
            if (typeRefWithNullability.isMarkedNullable) {
                data.append("?")
            }
        }

        override fun visitGetClassCall(getClassCall: FirGetClassCall, data: StringBuilder) {
            getClassCall.argument.accept(this, data)
        }

        override fun visitArrayOfCall(arrayOfCall: FirArrayOfCall, data: StringBuilder) {
            konst name = arrayOfCall.typeRef.coneType.classId!!.shortClassName.asString()
            konst typeArguments = arrayOfCall.typeRef.coneType.typeArguments
            konst typeParameters = if (typeArguments.isEmpty()) "" else " <T>"
            data.append("fun$typeParameters ${name.replaceFirstChar(Char::lowercaseChar)}Of")
            typeArguments.firstOrNull()?.let {
                data.append("<")
                it.tryToRenderConeAsFunctionType(data)
                data.append(">")
            }
            konst konstueParameter = name.replace("Array", "").takeIf { it.isNotEmpty() } ?: "T"
            data.append("(vararg $konstueParameter): $name${typeParameters.trim()}") // TODO change "T" to concrete type is array is primitive
        }

        private fun FirBasedSymbol<*>.isLocalDeclaration(): Boolean {
            return when (konst fir = this.fir) {
                is FirConstructor -> fir.returnTypeRef.coneType.isLocal()
                is FirCallableDeclaration -> {
                    fir.dispatchReceiverClassLookupTagOrNull()?.toFirRegularClassSymbol(session)?.isLocal ?: false
                }
                else -> false
            }
        }

        private fun ConeKotlinType.isLocal(): Boolean {
            if (this !is ConeClassLikeType) return false
            return this.lookupTag.toFirRegularClassSymbol(session)?.isLocal == true
        }

        // id == packageName + className
        private fun getSymbolId(symbol: FirBasedSymbol<*>?): String {
            return when (symbol) {
                is FirCallableSymbol<*> -> {
                    if (symbol.isLocalDeclaration()) {
                        konst callableName = symbol.callableId.callableName.asString()
                        stack.getPathByName(callableName) + callableName
                    } else {
                        symbol.callableId.withoutName().removeCurrentFilePackage()
                    }
                }
                is FirClassLikeSymbol<*> -> symbol.classId.getWithoutCurrentPackage()
                else -> ""
            }
        }

        private fun CallableId.withoutName(): String {
            return buildString {
                append(packageName.asString().replace('.', '/'))
                append("/")
                if (className != null) {
                    append(className)
                }
            }
        }
    }
}
