package test

class ClassA {
    class classB {
        fun memberFromB(): Int = 100

        class BC {
            konst memberFromBB: Int = 150
        }

        object BO {
            konst memberFromBO: Int = 175
        }
    }

    inner class classC {
        konst memberFromC: Int = 200
    }
}

class E {
    companion object {
        konst stat: Int = 250

        class D {
            konst memberFromD: Int = 275
        }
    }
}

class F {
    object ObjA {
        konst memberFromObjA: Int = 300
    }
}
