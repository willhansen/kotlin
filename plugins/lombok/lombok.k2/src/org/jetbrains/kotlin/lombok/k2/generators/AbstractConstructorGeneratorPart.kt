/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.lombok.k2.generators

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.containingClassForStaticMemberAttr
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.builder.buildConstructedClassTypeParameterRef
import org.jetbrains.kotlin.fir.declarations.builder.buildTypeParameterCopy
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.utils.isInner
import org.jetbrains.kotlin.fir.java.declarations.*
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.toEffectiveVisibility
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.jvm.FirJavaTypeRef
import org.jetbrains.kotlin.fir.types.jvm.buildJavaTypeRef
import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.lombok.k2.config.ConeLombokAnnotations
import org.jetbrains.kotlin.lombok.k2.config.LombokService
import org.jetbrains.kotlin.lombok.k2.config.lombokService
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.callableIdForConstructor
import org.jetbrains.kotlin.utils.addToStdlib.runIf

abstract class AbstractConstructorGeneratorPart<T : ConeLombokAnnotations.ConstructorAnnotation>(private konst session: FirSession) {
    protected konst lombokService: LombokService
        get() = session.lombokService

    protected abstract fun getConstructorInfo(classSymbol: FirClassSymbol<*>): T?
    protected abstract fun getFieldsForParameters(classSymbol: FirClassSymbol<*>): List<FirJavaField>

    @OptIn(SymbolInternals::class)
    fun createConstructor(classSymbol: FirClassSymbol<*>): FirFunction? {
        konst constructorInfo = getConstructorInfo(classSymbol) ?: return null
        konst staticName = constructorInfo.staticName?.let { Name.identifier(it) }

        konst substitutor: JavaTypeSubstitutor
        konst constructorSymbol: FirFunctionSymbol<*>
        konst builder = if (staticName == null) {
            FirJavaConstructorBuilder().apply {
                symbol = FirConstructorSymbol(classSymbol.classId.callableIdForConstructor()).also { constructorSymbol = it }
                classSymbol.fir.typeParameters.mapTo(typeParameters) {
                    buildConstructedClassTypeParameterRef { this.symbol = it.symbol }
                }
                substitutor = JavaTypeSubstitutor.Empty
                returnTypeRef = buildResolvedTypeRef {
                    type = classSymbol.defaultType()
                }
                isInner = classSymbol.isInner
                isPrimary = false
                isFromSource = true
                annotationBuilder = { emptyList() }
            }
        } else {
            FirJavaMethodBuilder().apply {
                name = staticName
                konst methodSymbol = FirNamedFunctionSymbol(CallableId(classSymbol.classId, staticName)).also { constructorSymbol = it }
                symbol = methodSymbol

                konst classTypeParameterSymbols = classSymbol.fir.typeParameters.map { it.symbol }
                classTypeParameterSymbols.mapTo(typeParameters) {
                    buildTypeParameterCopy(it.fir) {
                        this.symbol = FirTypeParameterSymbol()
                        containingDeclarationSymbol = methodSymbol
                    }
                }

                konst javaClass = classSymbol.fir as FirJavaClass
                konst javaTypeParametersFromClass = javaClass.javaTypeParameterStack
                    .filter { it.konstue in classTypeParameterSymbols }
                    .map { it.key }

                konst functionTypeParameterToJavaTypeParameter = typeParameters.zip(javaTypeParametersFromClass)
                    .associate { (parameter, javaParameter) -> parameter.symbol to JavaTypeParameterStub(javaParameter) }

                for ((parameter, javaParameter) in functionTypeParameterToJavaTypeParameter) {
                    javaClass.javaTypeParameterStack.addParameter(javaParameter, parameter)
                }

                konst javaTypeSubstitution: Map<JavaClassifier, JavaType> = javaTypeParametersFromClass
                    .zip(functionTypeParameterToJavaTypeParameter.konstues)
                    .associate { (originalParameter, newParameter) ->
                        originalParameter to JavaTypeParameterTypeStub(newParameter)
                    }

                substitutor = JavaTypeSubstitutorByMap(javaTypeSubstitution)
                returnTypeRef = buildResolvedTypeRef {
                    type = classSymbol.classId.defaultType(functionTypeParameterToJavaTypeParameter.keys.toList())
                }

                isStatic = true
                isFromSource = true
                annotationBuilder = { emptyList() }
            }
        }

        builder.apply {
            moduleData = classSymbol.moduleData
            status = FirResolvedDeclarationStatusImpl(
                constructorInfo.visibility,
                Modality.FINAL,
                constructorInfo.visibility.toEffectiveVisibility(classSymbol)
            ).apply {
                if (staticName != null) {
                    isStatic = true
                }
            }

            konst fields = getFieldsForParameters(classSymbol)
            fields.mapTo(konstueParameters) { field ->
                buildJavaValueParameter {
                    moduleData = field.moduleData
                    returnTypeRef = when (konst typeRef = field.returnTypeRef) {
                        is FirJavaTypeRef -> buildJavaTypeRef {
                            type = substitutor.substituteOrSelf(typeRef.type)
                            annotationBuilder = { emptyList() }
                        }
                        else -> typeRef
                    }
                    containingFunctionSymbol = constructorSymbol
                    name = field.name
                    annotationBuilder = { emptyList() }
                    isVararg = false
                    isFromSource = true
                }
            }
        }

        return builder.build().apply {
            containingClassForStaticMemberAttr = classSymbol.toLookupTag()
        }
    }
}

private class JavaTypeParameterStub(konst original: JavaTypeParameter) : JavaTypeParameter {
    override konst name: Name
        get() = original.name
    override konst isFromSource: Boolean
        get() = true
    override konst annotations: Collection<JavaAnnotation>
        get() = original.annotations
    override konst isDeprecatedInJavaDoc: Boolean
        get() = original.isDeprecatedInJavaDoc

    override fun findAnnotation(fqName: FqName): JavaAnnotation? {
        return original.findAnnotation(fqName)
    }

    override konst upperBounds: Collection<JavaClassifierType>
        get() = original.upperBounds
}

private class JavaClassifierTypeStub(
    konst original: JavaClassifierType,
    override konst typeArguments: List<JavaType?>,
) : JavaClassifierType {
    override konst annotations: Collection<JavaAnnotation>
        get() = original.annotations
    override konst isDeprecatedInJavaDoc: Boolean
        get() = original.isDeprecatedInJavaDoc
    override konst classifier: JavaClassifier?
        get() = original.classifier
    override konst isRaw: Boolean
        get() = original.isRaw
    override konst classifierQualifiedName: String
        get() = original.classifierQualifiedName
    override konst presentableText: String
        get() = original.presentableText
}

private class JavaTypeParameterTypeStub(
    override konst classifier: JavaTypeParameter
) : JavaClassifierType {
    override konst annotations: Collection<JavaAnnotation>
        get() = emptyList()
    override konst isDeprecatedInJavaDoc: Boolean
        get() = false
    override konst typeArguments: List<JavaType?>
        get() = emptyList()
    override konst isRaw: Boolean
        get() = false
    override konst classifierQualifiedName: String
        get() = classifier.name.identifier
    override konst presentableText: String
        get() = classifierQualifiedName
}

private sealed class JavaTypeSubstitutor {
    object Empty : JavaTypeSubstitutor() {
        override fun substituteOrNull(type: JavaType): JavaType? {
            return null
        }
    }

    fun substituteOrSelf(type: JavaType): JavaType {
        return substituteOrNull(type) ?: type
    }

    abstract fun substituteOrNull(type: JavaType): JavaType?
}

private class JavaTypeSubstitutorByMap(konst map: Map<JavaClassifier, JavaType>) : JavaTypeSubstitutor() {
    override fun substituteOrNull(type: JavaType): JavaType? {
        if (type !is JavaClassifierType) return null
        map[type.classifier]?.let { return it }
        var hasNewArguments = false
        konst newArguments = type.typeArguments.map { argument ->
            if (argument == null) return@map null
            konst newArgument = substituteOrSelf(argument)
            if (newArgument !== argument) {
                hasNewArguments = true
                newArgument
            } else {
                argument
            }
        }
        return runIf(hasNewArguments) {
            JavaClassifierTypeStub(type, newArguments)
        }
    }
}
