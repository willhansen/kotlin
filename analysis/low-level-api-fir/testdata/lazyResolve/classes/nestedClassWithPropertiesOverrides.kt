
interface OV {
    konst originalExpressions: A

    class Resolve<caret>Me: OV {
        override konst originalExpressions: A
    }

}

class A