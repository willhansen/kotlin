//ALLOW_AST_ACCESS
package test

interface Test {

    companion object {

        public konst prop1 : Int = { 10 }()

        public var  prop2 : Int = { 11 }()
            protected set

        public konst prop3: Int = { 12 }()
            get() {
                return field
            }

        var prop4 : Int = { 13 }()

        fun incProp4() {
            prop4++
        }

        public var prop5 : Int = { 14 }()

        public var prop7 : Int = { 20 }()
            set(i: Int) {
                field++
            }

        public const konst constProp8: Int = 80
    }
}