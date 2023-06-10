// !DIAGNOSTICS: -UNUSED_PARAMETER -CONFLICTING_JVM_DECLARATIONS
// NI_EXPECTED_FILE

fun foo(i: Int) = "$i"
fun foo(s: String) = s

fun bar(s: String) = s

fun qux(i: Int, j: Int, k: Int): Int = i + j + k
fun qux(a: String, b: String, c: String, d: String) {}

fun fn1(x: Int, f1: (Int) -> String, f2: (String) -> String) = f2(f1(x))

fun fn2(f1: (Int) -> String,    f2: (String) -> String  ) = f2(f1(0))
fun fn2(f1: (Int) -> Int,       f2: (Int) -> String     ) = f2(f1(0))
fun fn2(f1: (String) -> String, f2: (String) -> String  ) = f2(f1(""))

fun fn3(i: Int, f: (Int, Int, Int) -> Int): Int = f(i, i, i)

konst x1 = fn1(1, ::foo, ::foo)
konst x2 = fn1(1, ::foo, ::bar)

konst x3 = fn2(::bar, ::foo)
konst x4 = <!OVERLOAD_RESOLUTION_AMBIGUITY!>fn2<!>(::<!DEBUG_INFO_MISSING_UNRESOLVED!>foo<!>, ::bar)
konst x5 = <!OVERLOAD_RESOLUTION_AMBIGUITY!>fn2<!>(::<!DEBUG_INFO_MISSING_UNRESOLVED!>foo<!>, ::<!DEBUG_INFO_MISSING_UNRESOLVED!>foo<!>)

konst x6 = fn3(1, ::qux)
