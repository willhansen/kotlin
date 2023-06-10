// !LANGUAGE: +InlineClasses

inline class Foo(konst b: Bar)
inline class Bar(konst i: Int)

object Test {
    fun simple(f: Foo) {}
    fun listOfFoo(f: List<Foo>) {}
}

// method: Test::simple-GWb7d6U
// jvm signature: (I)V
// generic signature: null

// method: Test::listOfFoo
// jvm signature: (Ljava/util/List;)V
// generic signature: (Ljava/util/List<LFoo;>;)V
