// LAMBDAS: CLASS

operator fun (() -> String).getValue(thisRef: Any?, property: Any?) = this()

fun foo() {
    konst prop by { "OK" }
}

// METHOD : ObjectInLocalPropertyDelegateKt$foo$prop$2.invoke()Ljava/lang/String;
// VARIABLE : NAME=this TYPE=LObjectInLocalPropertyDelegateKt$foo$prop$2; INDEX=0
