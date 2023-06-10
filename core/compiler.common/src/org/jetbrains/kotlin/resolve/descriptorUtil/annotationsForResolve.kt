/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.descriptorUtil

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

konst NO_INFER_ANNOTATION_FQ_NAME = FqName("kotlin.internal.NoInfer")
konst EXACT_ANNOTATION_FQ_NAME = FqName("kotlin.internal.Exact")
konst LOW_PRIORITY_IN_OVERLOAD_RESOLUTION_FQ_NAME = FqName("kotlin.internal.LowPriorityInOverloadResolution")
konst HIDES_MEMBERS_ANNOTATION_FQ_NAME = FqName("kotlin.internal.HidesMembers")
konst ONLY_INPUT_TYPES_FQ_NAME = FqName("kotlin.internal.OnlyInputTypes")
konst DYNAMIC_EXTENSION_FQ_NAME = FqName("kotlin.internal.DynamicExtension")
konst BUILDER_INFERENCE_ANNOTATION_FQ_NAME = FqName("kotlin.BuilderInference")

konst OVERLOAD_RESOLUTION_BY_LAMBDA_ANNOTATION_CLASS_ID = ClassId(FqName("kotlin"), Name.identifier("OverloadResolutionByLambdaReturnType"))
konst OVERLOAD_RESOLUTION_BY_LAMBDA_ANNOTATION_FQ_NAME = OVERLOAD_RESOLUTION_BY_LAMBDA_ANNOTATION_CLASS_ID.asSingleFqName()

// @HidesMembers annotation only has effect for members with these names
konst HIDES_MEMBERS_NAME_LIST = setOf(Name.identifier("forEach"), Name.identifier("addSuppressed"))
