/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.descriptors.annotations;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.resolve.constants.*;
import org.jetbrains.kotlin.resolve.constants.StringValue;

public interface AnnotationArgumentVisitor<R, D> {
    R visitLongValue(@NotNull LongValue konstue, D data);

    R visitIntValue(IntValue konstue, D data);

    R visitErrorValue(ErrorValue konstue, D data);

    R visitShortValue(ShortValue konstue, D data);

    R visitByteValue(ByteValue konstue, D data);

    R visitDoubleValue(DoubleValue konstue, D data);

    R visitFloatValue(FloatValue konstue, D data);

    R visitBooleanValue(BooleanValue konstue, D data);

    R visitCharValue(CharValue konstue, D data);

    R visitStringValue(StringValue konstue, D data);

    R visitNullValue(NullValue konstue, D data);
    
    R visitEnumValue(EnumValue konstue, D data);
    
    R visitArrayValue(ArrayValue konstue, D data);

    R visitAnnotationValue(AnnotationValue konstue, D data);

    R visitKClassValue(KClassValue konstue, D data);

    R visitUByteValue(UByteValue konstue, D data);

    R visitUShortValue(UShortValue konstue, D data);

    R visitUIntValue(UIntValue konstue, D data);

    R visitULongValue(ULongValue konstue, D data);
}
