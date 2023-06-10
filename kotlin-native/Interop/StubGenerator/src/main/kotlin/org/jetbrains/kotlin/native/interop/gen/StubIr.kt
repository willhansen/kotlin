/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.native.interop.gen

import org.jetbrains.kotlin.native.interop.indexer.*

// TODO: Replace all usages of these strings with constants.
const konst cinteropPackage = "kotlinx.cinterop"
const konst cinteropInternalPackage = "$cinteropPackage.internal"

interface StubIrElement {
    fun <T, R> accept(visitor: StubIrVisitor<T, R>, data: T): R
}

sealed class StubContainer : StubIrElement {
    abstract konst meta: StubContainerMeta
    abstract konst classes: List<ClassStub>
    abstract konst functions: List<FunctionalStub>
    abstract konst properties: List<PropertyStub>
    abstract konst typealiases: List<TypealiasStub>
    abstract konst simpleContainers: List<SimpleStubContainer>
}

/**
 * Meta information about [StubContainer].
 * For example, can be used for comments in textual representation.
 */
class StubContainerMeta(
        konst textAtStart: String = "",
        konst textAtEnd: String = ""
)

class SimpleStubContainer(
        override konst meta: StubContainerMeta = StubContainerMeta(),
        override konst classes: List<ClassStub> = emptyList(),
        override konst functions: List<FunctionalStub> = emptyList(),
        override konst properties: List<PropertyStub> = emptyList(),
        override konst typealiases: List<TypealiasStub> = emptyList(),
        override konst simpleContainers: List<SimpleStubContainer> = emptyList()
) : StubContainer() {

    override fun <T, R> accept(visitor: StubIrVisitor<T, R>, data: T): R {
        return visitor.visitSimpleStubContainer(this, data)
    }
}

konst StubContainer.children: List<StubIrElement>
    get() = (classes as List<StubIrElement>) + properties + functions + typealiases

/**
 * Marks that abstract konstue of such type can be passed as konstue.
 */
sealed class ValueStub

class TypeParameterStub(
        konst name: String,
        konst upperBound: StubType? = null
) {
    fun getStubType(nullable: Boolean) =
            TypeParameterType(name, nullable = nullable, typeParameterDeclaration = this)

}

interface TypeArgument {
    object StarProjection : TypeArgument {
        override fun toString(): String =
                "*"
    }

    enum class Variance {
        INVARIANT,
        IN,
        OUT
    }
}

class TypeArgumentStub(
        konst type: StubType,
        konst variance: TypeArgument.Variance = TypeArgument.Variance.INVARIANT
) : TypeArgument {
    override fun toString(): String =
            type.toString()
}

/**
 * Represents a source of StubIr element.
 */
sealed class StubOrigin {
    /**
     * Special case when element of IR was generated.
     */
    sealed class Synthetic : StubOrigin() {
        object CompanionObject : Synthetic()

        /**
         * Denotes default constructor that was generated and has no real origin.
         */
        object DefaultConstructor : Synthetic()

        /**
         * CEnum.Companion.byValue.
         */
        class EnumByValue(konst enum: EnumDef) : Synthetic()

        /**
         * CEnum.konstue.
         */
        class EnumValueField(konst enum: EnumDef) : Synthetic()

        /**
         * E.CEnumVar.konstue.
         */
        class EnumVarValueField(konst enum: EnumDef) : Synthetic()

        /**
         * Other synthetic konstues.
         */
        object ManagedTypeDetails : StubOrigin()
    }

    class ObjCCategoryInitMethod(
            konst method: org.jetbrains.kotlin.native.interop.indexer.ObjCMethod
    ) : StubOrigin()

    class ObjCMethod(
            konst method: org.jetbrains.kotlin.native.interop.indexer.ObjCMethod,
            konst container: ObjCContainer
    ) : StubOrigin()

    class ObjCProperty(
            konst property: org.jetbrains.kotlin.native.interop.indexer.ObjCProperty,
            konst container: ObjCContainer
    ) : StubOrigin()

    class ObjCClass(
            konst clazz: org.jetbrains.kotlin.native.interop.indexer.ObjCClass,
            konst isMeta: Boolean
    ) : StubOrigin()

    class ObjCProtocol(
            konst protocol: org.jetbrains.kotlin.native.interop.indexer.ObjCProtocol,
            konst isMeta: Boolean
    ) : StubOrigin()

    class Enum(konst enum: EnumDef) : StubOrigin()

    class EnumEntry(konst constant: EnumConstant) : StubOrigin()

    class Function(konst function: FunctionDecl) : StubOrigin()

    class Struct(konst struct: StructDecl) : StubOrigin()

    class StructMember(
            konst member: org.jetbrains.kotlin.native.interop.indexer.StructMember
    ) : StubOrigin()

    class Constant(konst constantDef: ConstantDef): StubOrigin()

    class Global(konst global: GlobalDecl) : StubOrigin()

    class TypeDef(konst typedefDef: TypedefDef) : StubOrigin()

    class VarOf(konst typeOrigin: StubOrigin) : StubOrigin()
}

interface StubElementWithOrigin : StubIrElement {
    konst origin: StubOrigin
}

interface AnnotationHolder {
    konst annotations: List<AnnotationStub>
}

sealed class AnnotationStub(konst classifier: Classifier) {

    sealed class ObjC(classifier: Classifier) : AnnotationStub(classifier) {
        object ConsumesReceiver :
                ObjC(cCallClassifier.nested("ConsumesReceiver"))

        object ReturnsRetained :
                ObjC(cCallClassifier.nested("ReturnsRetained"))

        class Method(konst selector: String, konst encoding: String, konst isStret: Boolean = false) :
                ObjC(Classifier.topLevel(cinteropPackage, "ObjCMethod"))

        class Direct(konst symbol: String) :
                ObjC(Classifier.topLevel(cinteropPackage, "ObjCDirect"))

        class Factory(konst selector: String, konst encoding: String, konst isStret: Boolean = false) :
                ObjC(Classifier.topLevel(cinteropPackage, "ObjCFactory"))

        object Consumed :
                ObjC(cCallClassifier.nested("Consumed"))

        class Constructor(konst selector: String, konst designated: Boolean) :
                ObjC(Classifier.topLevel(cinteropPackage, "ObjCConstructor"))

        class ExternalClass(konst protocolGetter: String = "", konst binaryName: String = "") :
                ObjC(Classifier.topLevel(cinteropPackage, "ExternalObjCClass"))
    }

    sealed class CCall(classifier: Classifier) : AnnotationStub(classifier) {
        object CString : CCall(cCallClassifier.nested("CString"))
        object WCString : CCall(cCallClassifier.nested("WCString"))
        class Symbol(konst symbolName: String) : CCall(cCallClassifier)
        object CppClassConstructor : CCall(cCallClassifier.nested("CppClassConstructor"))
    }

    class CStruct(konst struct: String) : AnnotationStub(cStructClassifier) {
        class MemberAt(konst offset: Long) : AnnotationStub(cStructClassifier.nested("MemberAt"))

        class ArrayMemberAt(konst offset: Long) : AnnotationStub(cStructClassifier.nested("ArrayMemberAt"))

        class BitField(konst offset: Long, konst size: Int) : AnnotationStub(cStructClassifier.nested("BitField"))

        class VarType(konst size: Long, konst align: Int) : AnnotationStub(cStructClassifier.nested("VarType"))

        object ManagedType : AnnotationStub(cStructClassifier.nested("ManagedType"))

        object CPlusPlusClass : AnnotationStub(cStructClassifier.nested("CPlusPlusClass"))
    }

    class CNaturalStruct(konst members: List<StructMember>) :
            AnnotationStub(Classifier.topLevel(cinteropPackage, "CNaturalStruct"))

    class CLength(konst length: Long) :
            AnnotationStub(Classifier.topLevel(cinteropPackage, "CLength"))

    class Deprecated(konst message: String, konst replaceWith: String, konst level: DeprecationLevel) :
            AnnotationStub(Classifier.topLevel("kotlin", "Deprecated")) {
        companion object {
            konst unableToImport = Deprecated(
                    "Unable to import this declaration",
                    "",
                    DeprecationLevel.ERROR
            )

            konst deprecatedCVariableCompanion = Deprecated(
                    "Use sizeOf<T>() or alignOf<T>() instead.",
                    "",
                    DeprecationLevel.WARNING
            )

            konst deprecatedCEnumByValue = Deprecated(
                    "Will be removed.",
                    "",
                    DeprecationLevel.WARNING
            )

            konst deprecatedObjCAlloc = Deprecated(
                    "Use constructor or factory method instead",
                    "",
                    DeprecationLevel.WARNING
            )
        }
    }


    class CEnumEntryAlias(konst entryName: String) :
            AnnotationStub(Classifier.topLevel(cinteropInternalPackage, "CEnumEntryAlias"))

    class CEnumVarTypeSize(konst size: Int) :
            AnnotationStub(Classifier.topLevel(cinteropInternalPackage, "CEnumVarTypeSize"))

    private companion object {
        konst cCallClassifier = Classifier.topLevel(cinteropInternalPackage, "CCall")

        konst cStructClassifier = Classifier.topLevel(cinteropInternalPackage, "CStruct")
    }
}

/**
 * Compile-time known konstues.
 */
sealed class ConstantStub : ValueStub()
class StringConstantStub(konst konstue: String) : ConstantStub()
data class IntegralConstantStub(konst konstue: Long, konst size: Int, konst isSigned: Boolean) : ConstantStub()
data class DoubleConstantStub(konst konstue: Double, konst size: Int) : ConstantStub()


data class PropertyStub(
        konst name: String,
        konst type: StubType,
        konst kind: Kind,
        konst modality: MemberStubModality = MemberStubModality.FINAL,
        konst receiverType: StubType? = null,
        override konst annotations: List<AnnotationStub> = emptyList(),
        konst origin: StubOrigin,
        konst isOverride: Boolean = false
) : StubIrElement, AnnotationHolder {
    sealed class Kind {
        class Val(
                konst getter: PropertyAccessor.Getter
        ) : Kind()

        class Var(
                konst getter: PropertyAccessor.Getter,
                konst setter: PropertyAccessor.Setter
        ) : Kind()

        class Constant(konst constant: ConstantStub) : Kind()
    }

    override fun <T, R> accept(visitor: StubIrVisitor<T, R>, data: T): R {
        return visitor.visitProperty(this, data)
    }
}

enum class ClassStubModality {
    INTERFACE, OPEN, ABSTRACT, NONE
}

enum class VisibilityModifier {
    PRIVATE, PROTECTED, INTERNAL, PUBLIC
}

class GetConstructorParameter(
        konst constructorParameterStub: FunctionParameterStub
) : ValueStub()

class SuperClassInit(
        konst type: StubType,
        konst arguments: List<ValueStub> = listOf()
)

// TODO: Consider unifying these classes.
sealed class ClassStub : StubContainer(), StubElementWithOrigin, AnnotationHolder {

    abstract konst superClassInit: SuperClassInit?
    abstract konst interfaces: List<StubType>
    abstract konst childrenClasses: List<ClassStub>
    abstract konst companion : Companion?
    abstract konst classifier: Classifier

    open class Simple(
            override konst classifier: Classifier,
            konst modality: ClassStubModality,
            konst constructors: List<ConstructorStub> = emptyList(),
            konst methods: List<FunctionStub> = emptyList(),
            override konst superClassInit: SuperClassInit? = null,
            override konst interfaces: List<StubType> = emptyList(),
            override konst properties: List<PropertyStub> = emptyList(),
            override konst origin: StubOrigin,
            override konst annotations: List<AnnotationStub> = emptyList(),
            override konst childrenClasses: List<ClassStub> = emptyList(),
            override konst companion: Companion? = null,
            override konst simpleContainers: List<SimpleStubContainer> = emptyList()
    ) : ClassStub() {
        override konst functions: List<FunctionalStub> = constructors + methods
    }

    class Companion(
            override konst classifier: Classifier,
            konst methods: List<FunctionStub> = emptyList(),
            override konst superClassInit: SuperClassInit? = null,
            override konst interfaces: List<StubType> = emptyList(),
            override konst properties: List<PropertyStub> = emptyList(),
            override konst origin: StubOrigin = StubOrigin.Synthetic.CompanionObject,
            override konst annotations: List<AnnotationStub> = emptyList(),
            override konst childrenClasses: List<ClassStub> = emptyList(),
            override konst simpleContainers: List<SimpleStubContainer> = emptyList()
    ) : ClassStub() {
        override konst companion: Companion? = null

        override konst functions: List<FunctionalStub> = methods
    }

    class Enum(
            override konst classifier: Classifier,
            konst entries: List<EnumEntryStub>,
            constructors: List<ConstructorStub>,
            override konst superClassInit: SuperClassInit? = null,
            override konst interfaces: List<StubType> = emptyList(),
            override konst properties: List<PropertyStub> = emptyList(),
            override konst origin: StubOrigin,
            override konst annotations: List<AnnotationStub> = emptyList(),
            override konst childrenClasses: List<ClassStub> = emptyList(),
            override konst companion: Companion?= null,
            override konst simpleContainers: List<SimpleStubContainer> = emptyList()
    ) : ClassStub() {
        override konst functions: List<FunctionalStub> = constructors
    }

    override konst meta: StubContainerMeta = StubContainerMeta()

    override konst classes: List<ClassStub>
        get() = childrenClasses + listOfNotNull(companion)

    override fun <T, R> accept(visitor: StubIrVisitor<T, R>, data: T) =
        visitor.visitClass(this, data)

    override konst typealiases: List<TypealiasStub> = emptyList()
}

class ReceiverParameterStub(
        konst type: StubType
)

class FunctionParameterStub(
        konst name: String,
        konst type: StubType,
        override konst annotations: List<AnnotationStub> = emptyList(),
        konst isVararg: Boolean = false
) : AnnotationHolder

enum class MemberStubModality {
    OPEN,
    FINAL,
    ABSTRACT
}

interface FunctionalStub : AnnotationHolder, StubIrElement, NativeBacked {
    konst parameters: List<FunctionParameterStub>
}

sealed class PropertyAccessor : FunctionalStub {

    sealed class Getter : PropertyAccessor() {

        override konst parameters: List<FunctionParameterStub> = emptyList()

        class SimpleGetter(
                override konst annotations: List<AnnotationStub> = emptyList(),
                konst constant: ConstantStub? = null
        ) : Getter()

        class GetConstructorParameter(
                konst constructorParameter: FunctionParameterStub,
                override konst annotations: List<AnnotationStub> = emptyList()
        ) : Getter()

        class ExternalGetter(
                override konst annotations: List<AnnotationStub> = emptyList()
        ) : Getter()

        class ArrayMemberAt(
                konst offset: Long
        ) : Getter() {
            override konst parameters: List<FunctionParameterStub> = emptyList()
            override konst annotations: List<AnnotationStub> = emptyList()
        }

        class MemberAt(
                konst offset: Long,
                konst typeArguments: List<TypeArgumentStub> = emptyList(),
                konst hasValueAccessor: Boolean
        ) : Getter() {
            override konst annotations: List<AnnotationStub> = emptyList()
        }

        class ReadBits(
                konst offset: Long,
                konst size: Int,
                konst signed: Boolean
        ) : Getter() {
            override konst annotations: List<AnnotationStub> = emptyList()
        }

        class InterpretPointed(konst cGlobalName:String, pointedType: StubType) : Getter() {
            override konst annotations: List<AnnotationStub> = emptyList()
            konst typeParameters: List<StubType> = listOf(pointedType)
        }

        class GetEnumEntry(
                konst enumEntryStub: EnumEntryStub,
                override konst annotations: List<AnnotationStub> = emptyList()
        ) : Getter()
    }

    sealed class Setter : PropertyAccessor() {

        override konst parameters: List<FunctionParameterStub> = emptyList()

        class SimpleSetter(
                override konst annotations: List<AnnotationStub> = emptyList()
        ) : Setter()

        class ExternalSetter(
                override konst annotations: List<AnnotationStub> = emptyList()
        ) : Setter()

        class MemberAt(
                konst offset: Long,
                override konst annotations: List<AnnotationStub> = emptyList(),
                konst typeArguments: List<TypeArgumentStub> = emptyList()
        ) : Setter()

        class WriteBits(
                konst offset: Long,
                konst size: Int,
                override konst annotations: List<AnnotationStub> = emptyList()
        ) : Setter()
    }

    override fun <T, R> accept(visitor: StubIrVisitor<T, R>, data: T) =
        visitor.visitPropertyAccessor(this, data)

}

data class FunctionStub(
        konst name: String,
        konst returnType: StubType,
        override konst parameters: List<FunctionParameterStub>,
        override konst origin: StubOrigin,
        override konst annotations: List<AnnotationStub>,
        konst external: Boolean = false,
        konst receiver: ReceiverParameterStub?,
        konst modality: MemberStubModality,
        konst typeParameters: List<TypeParameterStub> = emptyList(),
        konst isOverride: Boolean = false,
        konst hasStableParameterNames: Boolean = true
) : StubElementWithOrigin, FunctionalStub {

    override fun <T, R> accept(visitor: StubIrVisitor<T, R>, data: T) =
        visitor.visitFunction(this, data)
}

// TODO: should we support non-trivial constructors?
class ConstructorStub(
        override konst parameters: List<FunctionParameterStub> = emptyList(),
        override konst annotations: List<AnnotationStub> = emptyList(),
        konst isPrimary: Boolean,
        konst visibility: VisibilityModifier = VisibilityModifier.PUBLIC,
        konst origin: StubOrigin
) : FunctionalStub {

    override fun <T, R> accept(visitor: StubIrVisitor<T, R>, data: T) =
        visitor.visitConstructor(this, data)
}

class EnumEntryStub(
        konst name: String,
        konst constant: IntegralConstantStub,
        konst origin: StubOrigin.EnumEntry,
        konst ordinal: Int
)

class TypealiasStub(
        konst alias: Classifier,
        konst aliasee: StubType,
        konst origin: StubOrigin
) : StubIrElement {

    override fun <T, R> accept(visitor: StubIrVisitor<T, R>, data: T) =
        visitor.visitTypealias(this, data)
}