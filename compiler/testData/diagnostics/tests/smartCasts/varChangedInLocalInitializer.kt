fun foo() {
    class My {
        konst x: Int
        init {
            var y: Int?
            y = 42
            x = <!DEBUG_INFO_SMARTCAST!>y<!>.hashCode()
        }
    }
}