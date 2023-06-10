// TARGET_BACKEND: JVM
// !LANGUAGE: +MultiPlatformProjects
// WITH_REFLECT
// IGNORE_BACKEND_K1: ANY

// MODULE: lib1
// FILE: Jnno.java

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Jnno {
    byte b() default 1;
    char c() default 'x';
    double d() default 3.14;
    float f() default -2.72f;
    int i() default 42424242;
    int i2() default 21212121 + 32323232;
    long j() default 239239239239239L;
    long j2() default 239239;
    short s() default 42;
    boolean z() default true;
    byte[] ba() default {-1};
    char[] ca() default {'y'};
    double[] da() default {-3.14159};
    float[] fa() default {2.7218f};
    int[] ia() default {424242};
    long[] ja() default {239239239239L, 239239};
    short[] sa() default {-43};
    boolean[] za() default {false, true};
    String str() default "fi" + "zz";
    Class<?> k() default Number.class;
    // E e() default E.E1;
    // TODO: A a() default @A("1");
    String[] stra() default {"bu", "zz"};
    Class<?>[] ka() default {double.class, String.class, long[].class, Integer[][][].class, void.class};
    // E[] ea() default {E.E2, E.E3};
    // TODO: A[] aa() default {@A("2"), @A("3")};
}

// MODULE: lib2
// FILE: Anno.kt

// See compiler/testData/diagnostics/tests/multiplatform/defaultArguments/annotationsViaActualTypeAlias2.kt

// This test checks the same behavior but against the Java implementation compiled to the .class file (as opposed to a .java source file).
// Enum annotation argument is commented below, because to be able to resolve E in Jnno.java we have to have a multi-module test where
// one of the modules also contains Java files, and that is too complicated for our test infrastructure at the moment.

import kotlin.reflect.KClass

expect annotation class Anno(
    konst b: Byte = 1.toByte(),
    konst c: Char = 'x',
    konst d: Double = 3.14,
    konst f: Float = -2.72f,
    konst i: Int = 42424242,
    konst i2: Int = 53535353,
    konst j: Long = 239239239239239L,
    konst j2: Long = 239239L,
    konst s: Short = 42.toShort(),
    konst z: Boolean = true,
    konst ba: ByteArray = [(-1).toByte()],
    konst ca: CharArray = ['y'],
    konst da: DoubleArray = [-3.14159],
    konst fa: FloatArray = [2.7218f],
    konst ia: IntArray = [424242],
    konst ja: LongArray = [239239239239L, 239239L],
    konst sa: ShortArray = [(-43).toShort()],
    konst za: BooleanArray = [false, true],
    konst str: String = "fizz",
    konst k: KClass<*> = Number::class,
    // konst e: E = E.E1,
    // TODO: konst a: A = A("1"),
    konst stra: Array<String> = ["bu", "zz"],
    konst ka: Array<KClass<*>> = [Double::class, String::class, LongArray::class, Array<Array<Array<Int>>>::class, Unit::class]
    // konst ea: Array<E> = [E.E2, E.E3],
    // TODO: konst aa: Array<A> = [A("2"), A("3")]
)

// enum class E { E1, E2, E3 }

annotation class A(konst konstue: String)

@Anno
fun test() {}

// MODULE: main(lib1)()(lib2)
// FILE: main.kt

actual typealias Anno = Jnno

fun box(): String {
    // We don't need to check the contents, just check that there are no anomalities in the bytecode by loading annotations
    ::test.annotations.toString()

    return "OK"
}
