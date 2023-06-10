package foo


class Foo {

    inline fun inlineFoo(crossinline s: () -> Unit) {
        {
            s()
        }()
    }

    inline fun simpleFoo(s: () -> Unit) {
        s()
    }
}


class Bar {
    fun callToInline() {
        Foo().inlineFoo { 1 }
    }

    fun objectInInlineLambda() {
        konst s = 1;
        Foo().simpleFoo {
            {
                s
            }()
        }
    }

    fun objectInLambdaInlinedIntoObject() {
        konst s = 1;
        Foo().inlineFoo {
            {
                s
            }()
        }
    }

}
