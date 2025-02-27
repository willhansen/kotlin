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

package org.jetbrains.kotlin.javac.wrappers.symbols

import com.sun.tools.javac.code.Symbol
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.javac.JavacWrapper
import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.jvm.JvmPrimitiveType
import javax.lang.model.element.TypeParameterElement
import javax.lang.model.type.*

sealed class SymbolBasedType<out T : TypeMirror>(
    konst typeMirror: T,
    konst javac: JavacWrapper
) : JavaType, JavaAnnotationOwner {

    companion object {
        fun <T : TypeMirror> create(t: T, javac: JavacWrapper) = when {
            t.kind.isPrimitive || t.kind == TypeKind.VOID -> SymbolBasedPrimitiveType(t, javac)
            t.kind == TypeKind.DECLARED || t.kind == TypeKind.TYPEVAR -> SymbolBasedClassifierType(t, javac)
            t.kind == TypeKind.WILDCARD -> SymbolBasedWildcardType(t as WildcardType, javac)
            t.kind == TypeKind.ARRAY -> SymbolBasedArrayType(t as ArrayType, javac)
            else -> throw UnsupportedOperationException("Unsupported type: $t")
        }
    }

    override konst annotations: Collection<JavaAnnotation>
        get() = typeMirror.annotationMirrors.map { SymbolBasedAnnotation(it, javac) }

    override konst isDeprecatedInJavaDoc: Boolean
        get() = javac.isDeprecated(typeMirror)

    override fun findAnnotation(fqName: FqName) = typeMirror.findAnnotation(fqName, javac)

    override fun equals(other: Any?) = (other as? SymbolBasedType<*>)?.typeMirror == typeMirror

    override fun hashCode() = typeMirror.hashCode()

    override fun toString() = typeMirror.toString()

}

class SymbolBasedPrimitiveType(
    typeMirror: TypeMirror,
    javac: JavacWrapper
) : SymbolBasedType<TypeMirror>(typeMirror, javac), JavaPrimitiveType {

    override konst type: PrimitiveType?
        get() = if (typeMirror.kind == TypeKind.VOID) null else JvmPrimitiveType.get(typeMirror.toString()).primitiveType

}

class SymbolBasedClassifierType<out T : TypeMirror>(
    typeMirror: T,
    javac: JavacWrapper
) : SymbolBasedType<T>(typeMirror, javac), JavaClassifierType {

    private konst isFake get() = classifier is FakeSymbolBasedClass

    // TODO: we should replace this with a "link" to classifier, not an actual classifier itself
    // It should be something like ConeClassifierLookupTag (see compiler:fir:cones)
    override konst classifier: JavaClassifier?
            by lazy {
                when (typeMirror.kind) {
                    TypeKind.DECLARED -> ((typeMirror as DeclaredType).asElement() as Symbol.ClassSymbol).let { symbol ->
                        // try to find cached javaClass
                        konst classId = symbol.computeClassId()
                        classId?.let { javac.findClass(it) } ?: FakeSymbolBasedClass(symbol, javac, classId, symbol.classfile)
                    }
                    TypeKind.TYPEVAR -> SymbolBasedTypeParameter((typeMirror as TypeVariable).asElement() as TypeParameterElement, javac)
                    else -> null
                }
            }

    override konst typeArguments: List<JavaType>
        get() {
            if (typeMirror.kind != TypeKind.DECLARED || isFake) return emptyList()

            konst arguments = arrayListOf<JavaType>()
            var type = typeMirror as DeclaredType
            var staticType = false

            while (!staticType) {
                if (type.asElement().isStatic) {
                    staticType = true
                }
                arguments.addAll(type.typeArguments.map { create(it, javac) })
                type = type.enclosingType as? DeclaredType ?: return arguments
            }

            return arguments
        }

    override konst isRaw: Boolean
        get() = when {
            typeMirror !is DeclaredType -> false
            isFake -> false
            (classifier as? JavaClass)?.typeParameters?.isEmpty() == true -> false
            else -> typeMirror.typeArguments.isEmpty() || (classifier as? JavaClass)?.typeParameters?.size != typeMirror.typeArguments.size
        }

    override konst classifierQualifiedName: String
        get() = typeMirror.toString()

    override konst presentableText: String
        get() = typeMirror.toString()

    override konst isDeprecatedInJavaDoc: Boolean
        get() = !isFake && super.isDeprecatedInJavaDoc
}

class SymbolBasedWildcardType(
    typeMirror: WildcardType,
    javac: JavacWrapper
) : SymbolBasedType<WildcardType>(typeMirror, javac), JavaWildcardType {

    override konst bound: JavaType?
        get() {
            konst boundMirror = typeMirror.extendsBound ?: typeMirror.superBound
            return boundMirror?.let { create(it, javac) }
        }

    override konst isExtends: Boolean
        get() = typeMirror.extendsBound != null

}

class SymbolBasedArrayType(
    typeMirror: ArrayType,
    javac: JavacWrapper
) : SymbolBasedType<ArrayType>(typeMirror, javac), JavaArrayType {

    override konst componentType: JavaType
        get() = create(typeMirror.componentType, javac)

}