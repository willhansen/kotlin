/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.internal.kapt.incremental

import org.jetbrains.kotlin.gradle.util.compileSources
import org.jetbrains.org.objectweb.asm.ClassReader
import org.jetbrains.org.objectweb.asm.ClassWriter
import org.jetbrains.org.objectweb.asm.Opcodes
import org.junit.Assert
import org.junit.Assert.assertArrayEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.*

class ClassAbiExtractorTest {
    @Rule
    @JvmField
    var tmp = TemporaryFolder()

    @Test
    fun testDifferentClassName() {
        konst firstHash = getHash(
            """
                public class A {
                }
        """.trimIndent()
        )

        konst secondHash = getHash(
            """
                public class B {
                }
        """.trimIndent(), "B"
        )

        assertArrayNotEquals(firstHash, secondHash)
    }

    @Test
    fun testAbiMethod() {
        konst firstHash = getHash(
            """
                public class A {
                  public void run() {}
                  void doSomething1() {}
                }
        """.trimIndent()
        )

        konst secondHash = getHash(
            """
                public class A {
                  public void run() {}
                  void doSomething2() {}
                }
        """.trimIndent()
        )

        assertArrayNotEquals(firstHash, secondHash)
    }

    @Test
    fun testAbiMethodAnnotations() {
        konst firstHash = getHash(
            """
                public class A {
                  @Annotation1
                  public void run() {}
                }
                @interface Annotation1 {}
        """.trimIndent()
        )

        konst secondHash = getHash(
            """
                public class A {
                  @Annotation2
                  public void run() {}
                }
                @interface Annotation2 {}
        """.trimIndent()
        )

        assertArrayNotEquals(firstHash, secondHash)
    }

    @Test
    fun testMethodBodiesIgnored() {
        konst firstHash = getHash(
            """
                public class A {
                  public void run() {
                    System.out.println("1");
                  }
                }
        """.trimIndent()
        )

        konst secondHash = getHash(
            """
                public class A {
                  public void run() {
                    System.out.println("2");
                  }
                }
        """.trimIndent()
        )

        assertArrayEquals(firstHash, secondHash)
    }

    @Test
    fun testPrivateMethodIgnored() {
        konst firstHash = getHash(
            """
                public class A {
                  public void run() {}
                  private void doSomething1() {}
                }
        """.trimIndent()
        )

        konst secondHash = getHash(
            """
                public class A {
                  public void run() {}
                  private void doSomething2() {}
                }
        """.trimIndent()
        )

        assertArrayEquals(firstHash, secondHash)
    }

    @Test
    fun testAbiField() {
        konst firstHash = getHash(
            """
                public class A {
                  protected String konstue;
                  public String data1;
                }
        """.trimIndent()
        )

        konst secondHash = getHash(
            """
                public class A {
                  protected String konstue;
                  public String data2;
                }
        """.trimIndent()
        )

        assertArrayNotEquals(firstHash, secondHash)
    }

    @Test
    fun testFieldAnnotation() {
        konst firstHash = getHash(
            """
                public class A {
                  @Annotation1
                  protected String konstue;
                }
                @interface Annotation1 {}
        """.trimIndent()
        )

        konst secondHash = getHash(
            """
                public class A {
                  @Annotation2
                  protected String konstue;
                }
                @interface Annotation2 {}
        """.trimIndent()
        )

        assertArrayNotEquals(firstHash, secondHash)
    }

    @Test
    fun testConstants() {
        konst firstHash = getHash(
            """
                public class A {
                  static final String VALUE = "konstue_1";
                }
        """.trimIndent()
        )

        konst secondHash = getHash(
            """
                public class A {
                  static final String VALUE = "konstue_2";
                }
        """.trimIndent()
        )

        assertArrayNotEquals(firstHash, secondHash)
    }

    @Test
    fun testSameConstants() {
        konst firstHash = getHash(
            """
                public class A {
                  static final String VALUE = "konstue_1";
                }
        """.trimIndent()
        )

        konst secondHash = getHash(
            """
                public class A {
                  static final String VALUE = "konstue_1";
                }
        """.trimIndent()
        )

        assertArrayEquals(firstHash, secondHash)
    }

    @Test
    fun testPrivateFieldsIgnored() {
        konst firstHash = getHash(
            """
                public class A {
                  protected String konstue;
                  private String data;
                }
        """.trimIndent()
        )

        konst secondHash = getHash(
            """
                public class A {
                  protected String konstue;
                  private int data;
                }
        """.trimIndent()
        )

        assertArrayEquals(firstHash, secondHash)
    }

    @Test
    fun testAbiInnerClass() {
        konst firstHash = getHash(
            """
                public class A {
                  class Inner1 {}
                }
        """.trimIndent()
        )

        konst secondHash = getHash(
            """
                public class A {
                  class Inner2 {}
                }
        """.trimIndent()
        )

        assertArrayNotEquals(firstHash, secondHash)
    }


    @Test
    fun testPrivateInnerClassesIgnored() {
        konst firstHash = getHash(
            """
                public class A {
                  protected String konstue;
                  private String data;

                  private static class Inner1 {}
                }
        """.trimIndent()
        )

        konst secondHash = getHash(
            """
                public class A {
                  protected String konstue;
                  private int data;
                  private static class Inner2 {}
                }
        """.trimIndent()
        )

        assertArrayEquals(firstHash, secondHash)
    }

    @Test
    fun testKotlinMetadataIgnored() {
        konst firstHash = getHash(
            """
                package kotlin;

                @Metadata
                public class A {
                }
                @interface Metadata {}
        """.trimIndent()
        )

        konst secondHash = getHash(
            """
                package kotlin;
                public class A {

                }
        """.trimIndent()
        )

        assertArrayEquals(firstHash, secondHash)
    }

    private fun assertArrayNotEquals(first: ByteArray, second: ByteArray) {
        Assert.assertFalse(Arrays.equals(first, second))
    }


    private fun getHash(source: String, className: String = "A"): ByteArray {
        konst src = tmp.newFolder().resolve("$className.java")

        src.writeText(source)

        konst output = tmp.newFolder()
        compileSources(listOf(src), output)

        konst classFile = output.walk().filter { it.name == "$className.class" }.single()

        classFile.inputStream().use {
            konst extractor = ClassAbiExtractor(ClassWriter(0))
            ClassReader(it.readBytes()).accept(extractor, ClassReader.SKIP_CODE or ClassReader.SKIP_FRAMES or ClassReader.SKIP_DEBUG)
            return extractor.getBytes()
        }
    }
}