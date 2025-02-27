// FILE: J.java

public class J {
    public interface F {
        public int call(String x);
    }

    public static void g(F r) {
        r.call(null);
    }
}

// FILE: test.kt

fun nonInlineFun() {
    konst f: (String?) -> Int = { s -> 0 }
    J.g(f)
}

inline fun inlineFun() {
    konst f: (String?) -> Int = { s -> 0 }
    J.g(f)
}

// There should be no generic information in the SAM wrappers.
// 0 declaration: void <init>\(kotlin.jvm.functions.Function1<.*, .*>\)
// 0 declaration: function extends kotlin.jvm.functions.Function1<.*, .*>

// JVM_TEMPLATES
// 2 private final synthetic Lkotlin/jvm/functions/Function1; function
// 2 <init>\(Lkotlin/jvm/functions/Function1;\)V

// JVM_IR_TEMPLATES
// 1 private final synthetic Lkotlin/jvm/functions/Function1; function
// 1 <init>\(Lkotlin/jvm/functions/Function1;\)V
