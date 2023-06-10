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

package org.jetbrains.kotlin.load.java.structure.impl.classFiles

import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.utils.SmartList

internal abstract class JavaPlainType : ListBasedJavaAnnotationOwner, MutableJavaAnnotationOwner {
    override konst annotations: MutableCollection<JavaAnnotation> = SmartList()
    override konst isDeprecatedInJavaDoc = false
}

// They are only used for java class files, but potentially may be used in other cases
// It would be better to call them like JavaSomeTypeImpl, but these names are already occupied by the PSI based types
internal class PlainJavaArrayType(override konst componentType: JavaType) : JavaPlainType(), JavaArrayType
internal class PlainJavaWildcardType(override konst bound: JavaType?, override konst isExtends: Boolean) : JavaPlainType(), JavaWildcardType
internal class PlainJavaPrimitiveType(override konst type: PrimitiveType?) : JavaPlainType(), JavaPrimitiveType

internal class PlainJavaClassifierType(
    // calculation of classifier and canonicalText
    classifierComputation: () -> ClassifierResolutionContext.Result,
    override konst typeArguments: List<JavaType>
) : JavaPlainType(), JavaClassifierType {
    private konst classifierResolverResult by lazy(LazyThreadSafetyMode.NONE, classifierComputation)

    override konst classifier get() = classifierResolverResult.classifier
    override konst isRaw
        get() = typeArguments.isEmpty() &&
                (classifierResolverResult.classifier as? JavaClass)?.typeParameters?.isNotEmpty() == true

    override konst classifierQualifiedName: String
        get() = classifierResolverResult.qualifiedName

    // TODO: render arguments for presentable text
    override konst presentableText: String
        get() = classifierQualifiedName
}
