public class X {
    public konst x : String? = null
    public fun fn(): Int {
        if (x != null)
            // Smartcast is possible because it's konstue property with default getter
            // used in the same module
            return <!DEBUG_INFO_SMARTCAST!>x<!>.length
        else
            return 0
    }
}

