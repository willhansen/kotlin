/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.load.java.structure

import org.jetbrains.kotlin.builtins.PrimitiveType

interface JavaType : ListBasedJavaAnnotationOwner

interface JavaArrayType : JavaType {
    konst componentType: JavaType
}

interface JavaClassifierType : JavaType {
    konst classifier: JavaClassifier?
    konst typeArguments: List<JavaType?>

    konst isRaw: Boolean

    konst classifierQualifiedName: String
    konst presentableText: String
}

interface JavaPrimitiveType : JavaType {
    /** `null` means the `void` type. */
    konst type: PrimitiveType?
}

interface JavaWildcardType : JavaType {
    konst bound: JavaType?
    konst isExtends: Boolean
}

fun JavaType?.isSuperWildcard(): Boolean = (this as? JavaWildcardType)?.let { it.bound != null && !it.isExtends } ?: false
