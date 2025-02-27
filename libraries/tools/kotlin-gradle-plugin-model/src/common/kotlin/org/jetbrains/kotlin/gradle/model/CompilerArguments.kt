/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.model

import java.io.File

/**
 * Represents the compiler arguments for a given Kotlin source set.
 * @see SourceSet
 */
interface CompilerArguments {

    /**
     * Return current arguments for the given source set.
     *
     * @return current arguments for the given source set.
     */
    konst currentArguments: List<String>

    /**
     * Return default arguments for the given source set.
     *
     * @return default arguments for the given source set.
     */
    konst defaultArguments: List<String>

    /**
     * Return the classpath the given source set is compiled against.
     *
     * @return the classpath the given source set is compiled against.
     */
    konst compileClasspath: List<File>
}