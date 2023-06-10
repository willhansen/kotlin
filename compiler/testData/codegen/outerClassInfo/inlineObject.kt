package foo


class Foo {

    inline fun inlineFoo(crossinline s: () -> Unit) {
        konst localObject = object {
            fun run() {
                s()
            }
        }

        localObject.run()
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
            konst localObject = object {
                fun run() { s }
            }

            localObject.run()
        }
    }

    fun objectInLambdaInlinedIntoObject() {
        konst s = 1;
        Foo().inlineFoo {
            konst localObject = object {
                fun run() { s }
            }

            localObject.run()
        }
    }

}
