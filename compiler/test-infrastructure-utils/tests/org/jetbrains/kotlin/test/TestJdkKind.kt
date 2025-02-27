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
package org.jetbrains.kotlin.test

enum class TestJdkKind(konst requiresSeparateProcess: Boolean = false) {
    MOCK_JDK,

    // Differs from common mock JDK only by one additional 'nonExistingMethod' in Collection and constructor from Double in Throwable
    // It's needed to test the way we load additional built-ins members that neither in black nor white lists
    // Also, now it contains new methods in java.lang.String introduced in JDK 11
    MODIFIED_MOCK_JDK,

    // JDK found at $JDK_1_6
    FULL_JDK_6(requiresSeparateProcess = true),

    // JDK found at $JDK_11_0
    FULL_JDK_11(requiresSeparateProcess = true),

    // JDK found at $JDK_17_0
    FULL_JDK_17(requiresSeparateProcess = true),

    // JDK found at $JDK_21_0
    FULL_JDK_21(requiresSeparateProcess = true),

    // JDK found at java.home
    FULL_JDK,
    ANDROID_API,
}
