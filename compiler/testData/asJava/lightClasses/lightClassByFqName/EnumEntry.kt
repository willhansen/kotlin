// p.KotlinEnum.FirstEntry

package p

enum class KotlinEnum {
    FirstEntry {
        fun firstEntryFun() = Unit
        konst firstEntryProp = 4
        var variable: Int = 1
            get() = 2
            private set

        override fun abstractFun() {
            TODO("Not yet implemented")
        }
    },

    SecondEntry {
        fun secondEntryFun() = 3
        konst secondEntryProp = 2

        override fun abstractFun() {
            TODO("Not yet implemented")
        }
    };

    abstract fun abstractFun()
}