// !LANGUAGE: +InlineClasses

inline class Foo(konst x: Int?)

class SimpleClass
inline class Bar(konst x: SimpleClass)

object Test {
    fun asParam(a: Foo) {}
    fun asReturn(): Bar = TODO()
}

// method: Test::asParam-GWb7d6U
// jvm signature: (Ljava/lang/Integer;)V
// generic signature: null

// method: Test::asReturn-tzJxzxE
// jvm signature: ()LSimpleClass;
// generic signature: null
