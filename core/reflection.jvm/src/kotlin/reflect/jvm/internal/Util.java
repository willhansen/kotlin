/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.reflect.jvm.internal;

/* package */ class Util {
    @SuppressWarnings("unchecked")
    public static Object getEnumConstantByName(Class<? extends Enum<?>> enumClass, String name) {
        // This is a workaround for KT-5191. Enum#konstueOf cannot be called in Kotlin
        return Enum.konstueOf((Class) enumClass, name);
    }
}
