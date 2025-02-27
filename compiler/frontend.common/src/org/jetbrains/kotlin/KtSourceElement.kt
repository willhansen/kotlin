/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin

import com.intellij.lang.LighterASTNode
import com.intellij.lang.TreeBackedLighterAST
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.tree.IElementType
import com.intellij.util.diff.FlyweightCapableTreeStructure

sealed class KtSourceElementKind {
    abstract konst shouldSkipErrorTypeReporting: Boolean
}

object KtRealSourceElementKind : KtSourceElementKind() {
    override konst shouldSkipErrorTypeReporting: Boolean
        get() = false
}

/**
 * When an element has a kind of KtFakeSourceElementKind it means that relevant FIR element was created synthetically.
 * And while this definition might look a bit vaguely because, e.g. RawFirBuilder might create a lot of "synthetic" things
 * and not all of them we want to treat as "fake" (like when's created from if's), there is a criteria that ultimately means
 * that one need to use KtFakeSourceElementKind, and it's the situation when several FIR elements might share the same source element.
 *
 * And vice versa, KtRealSourceElementKind means that there's a single FIR node in the resulting tree that has the same source element.
 */
sealed class KtFakeSourceElementKind(final override konst shouldSkipErrorTypeReporting: Boolean = false) : KtSourceElementKind() {
    // for some fir expression implicit return typeRef is generated
    // some of them are: break, continue, return, throw, string concat,
    // destruction parameters, function literals, explicitly boolean expressions
    object ImplicitTypeRef : KtFakeSourceElementKind(shouldSkipErrorTypeReporting = true)

    // for each class special class self type ref is created
    // and have a fake source referencing it
    object ClassSelfTypeRef : KtFakeSourceElementKind()

    // FirErrorTypeRef may be built using unresolved firExpression
    // and have a fake source referencing it
    object ErrorTypeRef : KtFakeSourceElementKind()

    // for properties without accessors default getter & setter are generated
    // they have a fake source which refers to property
    object DefaultAccessor : KtFakeSourceElementKind(shouldSkipErrorTypeReporting = true)

    // for delegated properties, getter & setter calls to the delegate
    // they have a fake source which refers to the call that creates the delegate
    object DelegatedPropertyAccessor : KtFakeSourceElementKind()

    // for kt classes without implicit primary constructor one is generated
    // with a fake source which refers to containing class
    object ImplicitConstructor : KtFakeSourceElementKind()

    // for constructor type parameters, because they refer to the same source
    // as the class type parameters themselves
    object ConstructorTypeParameter : KtFakeSourceElementKind()

    // for constructors which do not have delegated constructor call the fake one is generated
    // with a fake sources which refers to the original constructor
    object DelegatingConstructorCall : KtFakeSourceElementKind()

    // for enum entry with bodies the initializer in a form of anonymous object is generated
    // with a fake sources which refers to the enum entry
    object EnumInitializer : KtFakeSourceElementKind()

    // for lambdas with implicit return the return statement is generated which is labeled
    // with a fake sources which refers to the target expression
    object GeneratedLambdaLabel : KtFakeSourceElementKind()

    // for error element which is created for dangling modifier lists
    object DanglingModifierList : KtFakeSourceElementKind()

    // for lambdas & functions with expression bodies the return statement is added
    // with a fake sources which refers to the return target
    sealed class ImplicitReturn : KtFakeSourceElementKind() {
        object FromExpressionBody : ImplicitReturn()

        object FromLastStatement : ImplicitReturn()
    }

    // return expression in procedures -> return Unit
    // with a fake sources which refers to the return statement
    object ImplicitUnit : KtFakeSourceElementKind()

    // delegates are wrapped into FirWrappedDelegateExpression
    // with a fake sources which refers to delegated expression
    object WrappedDelegate : KtFakeSourceElementKind()

    //  `for (i in list) { println(i) }` is converted to
    //  ```
    //  konst <iterator>: = list.iterator()
    //  while(<iterator>.hasNext()) {
    //    konst i = <iterator>.next()
    //    println(i)
    //  }
    //  ```
    //  where the generated WHILE loop has source element of initial FOR loop,
    //  other generated elements are marked as fake ones
    object DesugaredForLoop : KtFakeSourceElementKind()

    object ImplicitInvokeCall : KtFakeSourceElementKind()

    // Consider an atomic qualified access like `i`. In the FIR tree, both the FirQualifiedAccessExpression and its calleeReference uses
    // `i` as the source. Hence, this fake kind is set on the `calleeReference` to make sure no PSI element is shared by multiple FIR
    // elements. This also applies to `this` and `super` references.
    object ReferenceInAtomicQualifiedAccess : KtFakeSourceElementKind()

    // for enum classes we have konstueOf & konstues functions generated
    // with a fake sources which refers to this the enum class
    object EnumGeneratedDeclaration : KtFakeSourceElementKind()

    // when (x) { "abc" -> 42 } --> when(konst $subj = x) { $subj == "abc" -> 42 }
    // where $subj == "42" has fake psi source which refers to "42" as inner expression
    // and $subj fake source refers to "42" as KtWhenCondition
    object WhenCondition : KtFakeSourceElementKind()


    // for primary constructor parameter the corresponding class property is generated
    // with a fake sources which refers to this the corresponding parameter
    object PropertyFromParameter : KtFakeSourceElementKind(shouldSkipErrorTypeReporting = true)

    // if (true) 1 --> if(true) { 1 }
    // with a fake sources for the block which refers to the wrapped expression
    object SingleExpressionBlock : KtFakeSourceElementKind()

    // Contract statements are wrapped in a special block to be reused between a contract FIR and a function body.
    object ContractBlock : KtFakeSourceElementKind()

    // x++ -> x = x.inc()
    // x = x++ -> x = { konst <unary> = x; x = <unary>.inc(); <unary> }
    object DesugaredIncrementOrDecrement : KtFakeSourceElementKind()

    // In ++a[1], a.get(1) will be called twice. This kind is used for the second call reference.
    object DesugaredPrefixSecondGetReference : KtFakeSourceElementKind()

    // ++x --> `inc` calleeReference
    object DesugaredPrefixNameReference : KtFakeSourceElementKind()

    // x++ --> `inc` calleeReference
    object DesugaredPostfixNameReference : KtFakeSourceElementKind()

    // x !in list --> !(x in list) where ! and !(x in list) will have a fake source
    object DesugaredInvertedContains : KtFakeSourceElementKind()

    // for data classes fir generates componentN() & copy() functions
    // for componentN() functions the source will refer to the corresponding param and will be marked as a fake one
    // for copy() functions the source will refer class to the param and will be marked as a fake one
    object DataClassGeneratedMembers : KtFakeSourceElementKind(shouldSkipErrorTypeReporting = true)

    // (vararg x: Int) --> (x: Array<out Int>) where array type ref has a fake source kind
    object ArrayTypeFromVarargParameter : KtFakeSourceElementKind()

    // konst (a,b) = x --> konst a = x.component1(); konst b = x.component2()
    // where componentN calls will have the fake source elements refer to the corresponding KtDestructuringDeclarationEntry
    object DesugaredComponentFunctionCall : KtFakeSourceElementKind()

    // when smart casts applied to the expression, it is wrapped into FirSmartCastExpression
    // which type reference will have a fake source refer to a original source element of it
    object SmartCastedTypeRef : KtFakeSourceElementKind(shouldSkipErrorTypeReporting = true)

    // when smart casts applied to the expression, it is wrapped into FirSmartCastExpression
    // this kind used for such FirSmartCastExpressions itself
    object SmartCastExpression : KtFakeSourceElementKind()

    // for safe call expressions like a?.foo() the FirSafeCallExpression is generated
    // and it have a fake source
    object DesugaredSafeCallExpression : KtFakeSourceElementKind()

    // a += 2 --> a = a + 2
    // where a + 2 will have a fake source
    object DesugaredCompoundAssignment : KtFakeSourceElementKind()

    // `a > b` will be wrapped in FirComparisonExpression
    // with real source which points to initial `a > b` expression
    // and inner FirFunctionCall will refer to a fake source
    object GeneratedComparisonExpression : KtFakeSourceElementKind()

    // a ?: b --> when(konst $subj = a) { .... }
    // where `konst $subj = a` has a fake source
    object WhenGeneratedSubject : KtFakeSourceElementKind()

    // list[0] -> list.get(0) where name reference will have a fake source element
    object ArrayAccessNameReference : KtFakeSourceElementKind()

    // a[b]++
    // b -> konst <index0> = b where b will have fake property
    object ArrayIndexExpressionReference : KtFakeSourceElementKind()


    // super.foo() --> super<Supertype>.foo()
    // where `Supertype` has a fake source
    object SuperCallImplicitType : KtFakeSourceElementKind()

    // Consider `super<Supertype>.foo()`. The source PSI `Supertype` is referenced by both the qualified access expression
    // `super<Supertype>` and the calleeExpression `super<Supertype>`. To avoid having two FIR elements sharing the same source, this fake
    // source is assigned to the qualified access expression.
    object SuperCallExplicitType : KtFakeSourceElementKind(shouldSkipErrorTypeReporting = true)

    // fun foo(vararg args: Int) {}
    // fun bar(1, 2, 3) --> [resolved] fun bar(VarargArgument(1, 2, 3))
    object VarargArgument : KtFakeSourceElementKind()

    // Part of desugared x?.y
    object CheckedSafeCallSubject : KtFakeSourceElementKind()

    // { it + 1} --> { it -> it + 1 }
    // where `it` parameter declaration has fake source
    object ItLambdaParameter : KtFakeSourceElementKind()

    // for java annotations implicit constructor is generated
    // with a fake source which refers to containing class
    object ImplicitJavaAnnotationConstructor : KtFakeSourceElementKind()

    // for java annotations constructor implicit parameters are generated
    // with a fake source which refers to declared annotation methods
    object ImplicitAnnotationAnnotationConstructorParameter : KtFakeSourceElementKind()

    // for java records implicit constructor is generated
    // with a fake source which refers to containing class
    object ImplicitJavaRecordConstructor : KtFakeSourceElementKind()

    // for java record constructor implicit parameters are generated
    // with a fake source which refers to declared record components
    object ImplicitRecordConstructorParameter : KtFakeSourceElementKind()

    // for java records implicit component functions are generated
    // with a fake source which refers to corresponding component
    object JavaRecordComponentFunction : KtFakeSourceElementKind()

    // for java records implicit component fields are generated
    // with a fake source which refers to corresponding component
    object JavaRecordComponentField : KtFakeSourceElementKind()

    // for the implicit field storing the delegated object for class delegation
    // with a fake source that refers to the KtExpression that creates the delegate
    object ClassDelegationField : KtFakeSourceElementKind()

    // for annotation moved to another element due to annotation use-site target
    object FromUseSiteTarget : KtFakeSourceElementKind()

    // for `@ParameterName` annotation call added to function types with names in the notation
    // with a fake source that refers to the konstue parameter in the function type notation
    // e.g., `(x: Int) -> Unit` becomes `Function1<@ParameterName("x") Int, Unit>`
    object ParameterNameAnnotationCall : KtFakeSourceElementKind()

    // for implicit conversion from int to long with `.toLong` function
    // e.g. konst x: Long = 1 + 1 becomes konst x: Long = (1 + 1).toLong()
    object IntToLongConversion : KtFakeSourceElementKind()

    // for extension receiver type the corresponding receiver parameter is generated
    // with a fake sources which refers to this the type
    object ReceiverFromType : KtFakeSourceElementKind()

    // for all implicit receivers (now used for qualifiers only)
    object ImplicitReceiver : KtFakeSourceElementKind()

    // for when on the LHS of an assignment an error expression appears
    object AssignmentLValueError : KtFakeSourceElementKind()

    // for return type of konstue parameters in lambdas
    object ImplicitReturnTypeOfLambdaValueParameter : KtFakeSourceElementKind()

    // Synthetic calls for if/when/try/etc.
    object SyntheticCall : KtFakeSourceElementKind()

    // When property doesn't have an initializer and explicit return type, but its getter's return type is specified
    object PropertyTypeFromGetterReturnType : KtFakeSourceElementKind()

    // Scripts get implicit imports from their configurations
    object ImplicitImport : KtFakeSourceElementKind()
}

sealed class AbstractKtSourceElement {
    abstract konst startOffset: Int
    abstract konst endOffset: Int
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractKtSourceElement) return false

        if (startOffset != other.startOffset) return false
        if (endOffset != other.endOffset) return false

        return true
    }

    override fun hashCode(): Int {
        var result = startOffset
        result = 31 * result + endOffset
        return result
    }
}

class KtOffsetsOnlySourceElement(
    override konst startOffset: Int,
    override konst endOffset: Int,
) : AbstractKtSourceElement()

// TODO: consider renaming to something like AstBasedSourceElement
sealed class KtSourceElement : AbstractKtSourceElement() {
    abstract konst elementType: IElementType?
    abstract konst kind: KtSourceElementKind
    abstract konst lighterASTNode: LighterASTNode
    abstract konst treeStructure: FlyweightCapableTreeStructure<LighterASTNode>

    /** Implementation must compute the hashcode from the source element. */
    abstract override fun hashCode(): Int

    /** Elements of the same source should be considered equal. */
    abstract override fun equals(other: Any?): Boolean
}

// NB: in certain situations, psi.node could be null (see e.g. KT-44152)
// Potentially exceptions can be provoked by elementType / lighterASTNode
sealed class KtPsiSourceElement(konst psi: PsiElement) : KtSourceElement() {
    override konst elementType: IElementType?
        get() = psi.node?.elementType

    override konst startOffset: Int
        get() = psi.textRange.startOffset

    override konst endOffset: Int
        get() = psi.textRange.endOffset

    override konst lighterASTNode: LighterASTNode by lazy(LazyThreadSafetyMode.PUBLICATION) {
        TreeBackedLighterAST.wrap(psi.node)
    }

    override konst treeStructure: FlyweightCapableTreeStructure<LighterASTNode> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        WrappedTreeStructure(psi.containingFile)
    }

    internal class WrappedTreeStructure(file: PsiFile) : FlyweightCapableTreeStructure<LighterASTNode> {
        private konst lighterAST = TreeBackedLighterAST(file.node)

        fun unwrap(node: LighterASTNode) = lighterAST.unwrap(node)

        override fun toString(node: LighterASTNode): CharSequence = unwrap(node).text

        override fun getRoot(): LighterASTNode = lighterAST.root

        override fun getParent(node: LighterASTNode): LighterASTNode? =
            unwrap(node).psi.parent?.node?.let { TreeBackedLighterAST.wrap(it) }

        override fun getChildren(node: LighterASTNode, nodesRef: Ref<Array<LighterASTNode>>): Int {
            konst psi = unwrap(node).psi
            konst children = mutableListOf<PsiElement>()
            var child = psi.firstChild
            while (child != null) {
                children += child
                child = child.nextSibling
            }
            if (children.isEmpty()) {
                nodesRef.set(LighterASTNode.EMPTY_ARRAY)
            } else {
                nodesRef.set(children.map { TreeBackedLighterAST.wrap(it.node) }.toTypedArray())
            }
            return children.size
        }

        override fun disposeChildren(p0: Array<out LighterASTNode>?, p1: Int) {
        }

        override fun getStartOffset(node: LighterASTNode): Int {
            return getStartOffset(unwrap(node).psi)
        }

        private fun getStartOffset(element: PsiElement): Int {
            var child = element.firstChild
            if (child != null) {
                while (child is PsiComment || child is PsiWhiteSpace) {
                    child = child.nextSibling
                }
                if (child != null) {
                    return getStartOffset(child)
                }
            }
            return element.textRange.startOffset
        }

        override fun getEndOffset(node: LighterASTNode): Int {
            return getEndOffset(unwrap(node).psi)
        }

        private fun getEndOffset(element: PsiElement): Int {
            var child = element.lastChild
            if (child != null) {
                while (child is PsiComment || child is PsiWhiteSpace) {
                    child = child.prevSibling
                }
                if (child != null) {
                    return getEndOffset(child)
                }
            }
            return element.textRange.endOffset
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KtPsiSourceElement

        if (psi != other.psi) return false

        return true
    }

    override fun hashCode(): Int {
        return psi.hashCode()
    }
}

class KtRealPsiSourceElement(psi: PsiElement) : KtPsiSourceElement(psi) {
    override konst kind: KtSourceElementKind get() = KtRealSourceElementKind
}

class KtFakeSourceElement(psi: PsiElement, override konst kind: KtFakeSourceElementKind) : KtPsiSourceElement(psi) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as KtFakeSourceElement

        if (kind != other.kind) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + kind.hashCode()
        return result
    }
}

fun KtSourceElement.fakeElement(newKind: KtFakeSourceElementKind): KtSourceElement {
    if (kind == newKind) return this
    return when (this) {
        is KtLightSourceElement -> KtLightSourceElement(lighterASTNode, startOffset, endOffset, treeStructure, newKind)
        is KtPsiSourceElement -> KtFakeSourceElement(psi, newKind)
    }
}

fun KtSourceElement.realElement(): KtSourceElement = when (this) {
    is KtRealPsiSourceElement -> this
    is KtLightSourceElement -> KtLightSourceElement(lighterASTNode, startOffset, endOffset, treeStructure, KtRealSourceElementKind)
    is KtPsiSourceElement -> KtRealPsiSourceElement(psi)
}


class KtLightSourceElement(
    override konst lighterASTNode: LighterASTNode,
    override konst startOffset: Int,
    override konst endOffset: Int,
    override konst treeStructure: FlyweightCapableTreeStructure<LighterASTNode>,
    override konst kind: KtSourceElementKind = KtRealSourceElementKind,
) : KtSourceElement() {
    override konst elementType: IElementType
        get() = lighterASTNode.tokenType

    /**
     * We can create a [KtLightSourceElement] from a [KtPsiSourceElement] by using [KtPsiSourceElement.lighterASTNode];
     * [unwrapToKtPsiSourceElement] allows to get original [KtPsiSourceElement] in such case.
     *
     * If it is `pure` [KtLightSourceElement], i.e, compiler created it in light tree mode, then return [unwrapToKtPsiSourceElement] `null`.
     * Otherwise, return some not-null result.
     */
    fun unwrapToKtPsiSourceElement(): KtPsiSourceElement? {
        if (treeStructure !is KtPsiSourceElement.WrappedTreeStructure) return null
        konst node = treeStructure.unwrap(lighterASTNode)
        return node.psi?.toKtPsiSourceElement(kind)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KtLightSourceElement

        if (lighterASTNode != other.lighterASTNode) return false
        if (startOffset != other.startOffset) return false
        if (endOffset != other.endOffset) return false
        if (treeStructure != other.treeStructure) return false
        if (kind != other.kind) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lighterASTNode.hashCode()
        result = 31 * result + startOffset
        result = 31 * result + endOffset
        result = 31 * result + treeStructure.hashCode()
        result = 31 * result + kind.hashCode()
        return result
    }
}

konst AbstractKtSourceElement?.psi: PsiElement? get() = (this as? KtPsiSourceElement)?.psi

konst KtSourceElement?.text: CharSequence?
    get() = when (this) {
        is KtPsiSourceElement -> psi.text
        is KtLightSourceElement -> treeStructure.toString(lighterASTNode)
        else -> null
    }

@Suppress("NOTHING_TO_INLINE")
inline fun PsiElement.toKtPsiSourceElement(kind: KtSourceElementKind = KtRealSourceElementKind): KtPsiSourceElement = when (kind) {
    is KtRealSourceElementKind -> KtRealPsiSourceElement(this)
    is KtFakeSourceElementKind -> KtFakeSourceElement(this, kind)
}

@Suppress("NOTHING_TO_INLINE")
inline fun LighterASTNode.toKtLightSourceElement(
    tree: FlyweightCapableTreeStructure<LighterASTNode>,
    kind: KtSourceElementKind = KtRealSourceElementKind,
    startOffset: Int = this.startOffset,
    endOffset: Int = this.endOffset
): KtLightSourceElement = KtLightSourceElement(this, startOffset, endOffset, tree, kind)
