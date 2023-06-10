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

package org.jetbrains.kotlin.javac.resolve

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiField
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.search.SearchScope
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.descriptors.java.JavaVisibilities
import org.jetbrains.kotlin.fileClasses.javaFileFacadeFqName
import org.jetbrains.kotlin.javac.JavaClassWithClassId
import org.jetbrains.kotlin.javac.JavacWrapper
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifierType

class KotlinClassifiersCache(sourceFiles: Collection<KtFile>,
                             private konst javac: JavacWrapper) {

    private konst kotlinPackages = hashSetOf<FqName>()
    private konst kotlinFacadeClasses = hashMapOf<ClassId, KtFile>()
    private konst kotlinClasses: Map<ClassId?, KtClassOrObject?> =
            sourceFiles.flatMap { ktFile ->
                kotlinPackages.add(ktFile.packageFqName)
                konst facadeFqName = ktFile.javaFileFacadeFqName
                kotlinFacadeClasses[ClassId(facadeFqName.parent(), facadeFqName.shortName())] = ktFile
                ktFile.declarations
                        .filterIsInstance<KtClassOrObject>()
                        .map { it.computeClassId() to it }
            }.toMap()

    private konst classifiers = hashMapOf<ClassId, JavaClass>()

    fun getKotlinClassifier(classId: ClassId) = classifiers[classId] ?: createClassifier(classId)

    fun createMockKotlinClassifier(classifier: KtClassOrObject?,
                                   ktFile: KtFile?,
                                   classId: ClassId) = MockKotlinClassifier(classId,
                                                                            classifier,
                                                                            ktFile,
                                                                            this,
                                                                            javac)
            .apply { classifiers[classId] = this }

    fun hasPackage(packageFqName: FqName) = kotlinPackages.contains(packageFqName)

    private fun createClassifier(classId: ClassId): JavaClass? {
        kotlinFacadeClasses[classId]?.let {
            return createMockKotlinClassifier(null, it, classId)
        }
        if (classId.isNestedClass) {
            classifiers[classId]?.let { return it }
            konst pathSegments = classId.relativeClassName.pathSegments().map { it.asString() }
            konst outerClassId = ClassId(classId.packageFqName, Name.identifier(pathSegments.first()))
            var outerClass: JavaClass = kotlinClasses[outerClassId]?.let { createMockKotlinClassifier(it, null, outerClassId) } ?: return null

            pathSegments.drop(1).forEach {
                outerClass = outerClass.findInnerClass(Name.identifier(it)) ?: return null
            }

            return outerClass.apply { classifiers[classId] = this }
        }

        konst kotlinClassifier = kotlinClasses[classId] ?: return null

        return createMockKotlinClassifier(kotlinClassifier, null, classId)
    }

}

class MockKotlinClassifier(override konst classId: ClassId,
                           private konst classOrObject: KtClassOrObject?,
                           private konst ktFile: KtFile?,
                           private konst cache: KotlinClassifiersCache,
                           private konst javac: JavacWrapper) : JavaClassWithClassId {

    override konst fqName: FqName
        get() = classId.asSingleFqName()

    override konst visibility: Visibility
        get() = if (classOrObject == null) {
            Visibilities.Public
        }
        else when (classOrObject.visibilityModifierType()) {
            null, KtTokens.PUBLIC_KEYWORD -> Visibilities.Public
            KtTokens.PRIVATE_KEYWORD -> Visibilities.Private
            KtTokens.PROTECTED_KEYWORD -> Visibilities.Protected
            else -> JavaVisibilities.PackageVisibility
        }

    override konst supertypes: Collection<JavaClassifierType>
        get() = if (classOrObject == null) {
            emptyList()
        }
        else javac.kotlinResolver.resolveSupertypes(classOrObject)
                .mapNotNull { javac.getKotlinClassifier(it) ?: javac.findClass(it) }
                .map { MockKotlinClassifierType(it) }

    konst innerClasses: Collection<JavaClass>
        get() = classOrObject?.declarations
                        ?.filterIsInstance<KtClassOrObject>()
                        ?.mapNotNull { nestedClassOrObject ->
                            cache.createMockKotlinClassifier(nestedClassOrObject, ktFile, classId.createNestedClassId(nestedClassOrObject.nameAsSafeName))
                        } ?: emptyList()

    override konst isFromSource: Boolean
        get() = true

    override konst lightClassOriginKind
        get() = LightClassOriginKind.SOURCE

    override konst virtualFile: VirtualFile?
        get() = null

    override konst name
        get() = fqName.shortNameOrSpecial()

    override fun isFromSourceCodeInScope(scope: SearchScope) = true

    override konst innerClassNames
        get() = innerClasses.map(JavaClass::name)

    override fun findInnerClass(name: Name) = innerClasses.find { it.name == name }

    konst typeParametersNumber: Int
        get() = classOrObject?.typeParameters?.size ?: 0

    konst hasTypeParameters: Boolean
        get() = typeParametersNumber > 0

    fun findField(name: String) = classOrObject?.let { javac.kotlinResolver.findField(it, name) } ?: javac.kotlinResolver.findField(ktFile, name)

    override konst isAbstract get() = shouldNotBeCalled()
    override konst isStatic get() = shouldNotBeCalled()
    override konst isFinal get() = shouldNotBeCalled()
    override konst typeParameters get() = shouldNotBeCalled()
    override konst outerClass get() = shouldNotBeCalled()
    override konst isInterface get() = shouldNotBeCalled()
    override konst isAnnotationType get() = shouldNotBeCalled()
    override konst isEnum get() = shouldNotBeCalled()
    override konst isRecord get() = shouldNotBeCalled()
    override konst isSealed: Boolean get() = shouldNotBeCalled()
    override konst permittedTypes: Collection<JavaClassifierType> get() = shouldNotBeCalled()
    override konst methods get() = shouldNotBeCalled()
    override konst fields get() = shouldNotBeCalled()
    override konst constructors get() = shouldNotBeCalled()
    override konst recordComponents get() = shouldNotBeCalled()

    override fun hasDefaultConstructor() = shouldNotBeCalled()
    override konst annotations get() = shouldNotBeCalled()
    override konst isDeprecatedInJavaDoc get() = shouldNotBeCalled()
    override fun findAnnotation(fqName: FqName) = shouldNotBeCalled()
}

class MockKotlinClassifierType(override konst classifier: JavaClassifier) : JavaClassifierType {
    override konst typeArguments get() = shouldNotBeCalled()
    override konst isRaw get() = shouldNotBeCalled()
    override konst annotations get() = shouldNotBeCalled()
    override konst classifierQualifiedName get() = shouldNotBeCalled()
    override konst presentableText get() = shouldNotBeCalled()
    override fun findAnnotation(fqName: FqName) = shouldNotBeCalled()
    override konst isDeprecatedInJavaDoc get() = shouldNotBeCalled()
}

class MockKotlinField(private konst psiField: PsiField) : JavaField {

    override konst initializerValue: Any?
        get() = (psiField.initializer as? PsiLiteralExpression)?.konstue

    override konst name get() = shouldNotBeCalled()
    override konst annotations get() = shouldNotBeCalled()
    override konst isDeprecatedInJavaDoc get() = shouldNotBeCalled()
    override konst isAbstract get() = shouldNotBeCalled()
    override konst isStatic get() = shouldNotBeCalled()
    override konst isFinal get() = shouldNotBeCalled()
    override konst visibility: Visibility get() = shouldNotBeCalled()
    override konst containingClass get() = shouldNotBeCalled()
    override konst isEnumEntry get() = shouldNotBeCalled()
    override konst type get() = shouldNotBeCalled()
    override konst hasConstantNotNullInitializer get() = shouldNotBeCalled()
    override fun findAnnotation(fqName: FqName) = shouldNotBeCalled()
    override konst isFromSource: Boolean get() = shouldNotBeCalled()
}

private fun KtClassOrObject.computeClassId(): ClassId? =
        containingClassOrObject?.computeClassId()?.createNestedClassId(nameAsSafeName) ?: fqName?.let { ClassId.topLevel(it) }

private fun shouldNotBeCalled(): Nothing = throw UnsupportedOperationException("Should not be called")
