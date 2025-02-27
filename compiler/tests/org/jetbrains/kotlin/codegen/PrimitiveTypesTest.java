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

package org.jetbrains.kotlin.codegen;

import org.jetbrains.kotlin.test.ConfigurationKind;

import java.lang.reflect.Method;

public class PrimitiveTypesTest extends CodegenTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createEnvironmentWithMockJdkAndIdeaAnnotations(ConfigurationKind.JDK_ONLY);
    }

    public void testPlus() throws Exception {
        loadText("fun f(a: Int, b: Int): Int { return a + b }");
        Method main = generateFunction();
        int returnValue = (Integer) main.invoke(null, 37, 5);
        assertEquals(42, returnValue);
    }

    public void testGt() throws Exception {
        loadText("fun foo(f: Int): Boolean { if (f > 0) return true; return false; }");

        Method main = generateFunction();
        assertEquals(true, main.invoke(null, 1));
        assertEquals(false, main.invoke(null, 0));
    }

    public void testDiv() throws Exception {
        binOpTest("fun foo(a: Int, b: Int): Int = a / b", 12, 3, 4);
    }

    public void testMod() throws Exception {
        binOpTest("fun foo(a: Int, b: Int): Int = a % b", 14, 3, 2);
    }

    public void testNE() throws Exception {
        loadText("fun foo(a: Int, b: Int): Int = if (a != b) 1 else 0");
        Method main = generateFunction();
        assertEquals(0, main.invoke(null, 5, 5));
        assertEquals(1, main.invoke(null, 5, 3));
    }

    public void testGE() throws Exception {
        loadText("fun foo(a: Int, b: Int): Int = if (a >= b) 1 else 0");
        Method main = generateFunction();
        assertEquals(1, main.invoke(null, 5, 5));
        assertEquals(0, main.invoke(null, 3, 5));
    }

    public void testReturnCmp() throws Exception {
        loadText("fun foo(a: Int, b: Int): Boolean = a == b");
        Method main = generateFunction();
        assertEquals(true, main.invoke(null, 1, 1));
        assertEquals(false, main.invoke(null, 1, 2));
    }

    public void testLong() throws Exception {
        loadText("fun foo(a: Long, b: Long): Long = a + b");
        Method main = generateFunction();
        long arg = (long) Integer.MAX_VALUE;
        long expected = 2 * (long) Integer.MAX_VALUE;
        assertEquals(expected, main.invoke(null, arg, arg));
    }

    public void testLongCmp() throws Exception {
        loadText("fun foo(a: Long, b: Long): Long = if (a == b) 0xffffffff else 0xfffffffe");
        Method main = generateFunction();
        assertEquals(0xffffffffL, main.invoke(null, 1, 1));
        assertEquals(0xfffffffeL, main.invoke(null, 1, 0));
    }

    public void testShort() throws Exception {
        binOpTest("fun foo(a: Short, b: Short): Int = a + b",
                  Short.konstueOf((short) 32767), Short.konstueOf((short) 32767), 65534);
    }

    public void testShortCmp() throws Exception {
        binOpTest("fun foo(a: Short, b: Short): Boolean = a == b",
                  Short.konstueOf((short) 32767), Short.konstueOf((short) 32767), true);
    }

    public void testByte() throws Exception {
        binOpTest("fun foo(a: Byte, b: Byte): Int = a + b",
                  Byte.konstueOf((byte) 127), Byte.konstueOf((byte) 127), 254);
    }

    public void testByteCmp() throws Exception {
        binOpTest("fun foo(a: Byte, b: Byte): Int = if (a == b) 1 else 0",
                  Byte.konstueOf((byte) 127), Byte.konstueOf((byte) 127), 1);
    }

    public void testByteLess() throws Exception {
        binOpTest("fun foo(a: Byte, b: Byte): Boolean = a < b",
                  Byte.konstueOf((byte) 126), Byte.konstueOf((byte) 127), true);
    }

    public void testBooleanConstant() throws Exception {
        loadText("fun foo(): Boolean = true");
        Method main = generateFunction();
        assertEquals(true, main.invoke(null));
    }

    public void testChar() throws Exception {
        // '+' is not available for Chars
        binOpTest("fun foo(a: Char, b: Char): Int = a - b", 'D', (char) 3, (int) 'A');
    }

    public void testFloat() throws Exception {
        binOpTest("fun foo(a: Float, b: Float): Float = a + b", 1.0f, 2.0f, 3.0f);
    }

    public void testFloatCmp() throws Exception {
        binOpTest("fun foo(a: Float, b: Float): Boolean = a == b", 1.0f, 1.0f, true);
    }

    public void testDouble() throws Exception {
        binOpTest("fun foo(a: Double, b: Double): Double = a + b", 1.0, 2.0, 3.0);
    }

    public void testDoubleCmp() throws Exception {
        binOpTest("fun foo(a: Double, b: Double): Boolean = a == b", 1.0, 2.0, false);
    }

    public void testDoubleToJava() throws Exception {
        loadText("import java.lang.Double as jlDouble; fun foo(d: Double): String? = jlDouble.toString(d)");
        Method main = generateFunction();
        assertEquals("1.0", main.invoke(null, 1.0));
    }

    public void testDoubleToInt() throws Exception {
        loadText("fun foo(a: Double): Int = a.toInt()");
        Method main = generateFunction();
        assertEquals(1, main.invoke(null, 1.0));
    }

    public void testCastConstant() throws Exception {
        loadText("fun foo(): Double = 1.toDouble()");
        Method main = generateFunction();
        assertEquals(1.0, main.invoke(null));
    }

    public void testCastOnStack() throws Exception {
        loadText("fun foo(l: Long): Double = l.toDouble()");
        Class<?> mainClass = generateFacadeClass();
        Method main = mainClass.getDeclaredMethod("foo", long.class);
        double result = (Double) main.invoke(null, 42L);
        assertTrue(Math.abs(42L - result) <= 1e-9);
    }

    public void testNeg() throws Exception {
        loadText("fun foo(a: Int): Int = -a");
        Method main = generateFunction();
        assertEquals(-10, main.invoke(null, 10));
    }

    public void testPreIncrement() throws Exception {
        loadText("fun foo(a: Int): Int { var x = a; ++x; return x;}");
        Method main = generateFunction();
        assertEquals(11, main.invoke(null, 10));
    }

    public void testPreIncrementValue() throws Exception {
        loadText("fun foo(a: Int): Int { var x = a; return ++x;}");
        Method main = generateFunction();
        assertEquals(11, main.invoke(null, 10));
    }

    public void testPreDecrement() throws Exception {
        loadText("fun foo(a0: Int): Int { var a = a0; return --a;}");
        Method main = generateFunction();
        assertEquals(9, main.invoke(null, 10));
    }

    public void testPreIncrementLong() throws Exception {
        loadText("fun foo(a0: Long): Long { var a = a0; return ++a}");
        Method main = generateFunction();
        assertEquals(11L, main.invoke(null, 10L));
    }

    public void testPreIncrementFloat() throws Exception {
        loadText("fun foo(a0: Float): Float { var a = a0; return ++a }");
        Method main = generateFunction();
        assertEquals(2.0f, main.invoke(null, 1.0f));
    }

    public void testPreIncrementDouble() throws Exception {
        loadText("fun foo(a0: Double): Double {var a = a0; return ++a }");
        Method main = generateFunction();
        assertEquals(2.0, main.invoke(null, 1.0));
    }

    public void testShl() throws Exception {
        binOpTest("fun foo(a: Int, b: Int): Int = a shl b", 1, 3, 8);
    }

    public void testShr() throws Exception {
        binOpTest("fun foo(a: Int, b: Int): Int = a shr b", 8, 3, 1);
    }

    public void testBitAnd() throws Exception {
        binOpTest("fun foo(a: Int, b: Int): Int = a and b", 0x77, 0x1f, 0x17);
    }

    public void testBitOr() throws Exception {
        binOpTest("fun foo(a: Int, b: Int): Int = a or b", 0x77, 0x1f, 0x7f);
    }

    public void testBitXor() throws Exception {
        binOpTest("fun foo(a: Int, b: Int): Int = a xor b", 0x70, 0x1f, 0x6f);
    }

    public void testBitInv() throws Exception {
        loadText("fun foo(a: Int): Int = a.inv()");
        Method main = generateFunction();
        assertEquals(0xffff0000, main.invoke(null, 0x0000ffff));
    }

    public void testMixedTypes() throws Exception {
        binOpTest("fun foo(a: Int, b: Long): Long = a + b", 1, 2L, 3L);
    }

    public void testMixedTypes2() throws Exception {
        binOpTest("fun foo(a: Double, b: Int): Double = a + b", 1.0, 2, 3.0);
    }

    public void testPostIncrementTypeInferenceFail() throws Exception {
        loadText("fun foo(a: Int): Int { var x = a; var y = x++; if (y+1 != x) return -1; return x; }");
        Method main = generateFunction();
        assertEquals(6, main.invoke(null, 5));
    }

    public void testPostIncrement() throws Exception {
        loadText("fun foo(a: Int): Int { var x = a; var y = x++; return x*y; }");
        Method main = generateFunction();
        assertEquals(6, main.invoke(null, 2));
    }

    public void testPostIncrementLong() throws Exception {
        loadText("fun foo(a: Long): Long { var x = a; var y = x++; return x*y; }");
        Method main = generateFunction();
        assertEquals(6L, main.invoke(null, 2L));
    }

    private void binOpTest(String text, Object arg1, Object arg2, Object expected) throws Exception {
        loadText(text);
        Method main = generateFunction();
        assertEquals(expected, main.invoke(null, arg1, arg2));
    }
}
