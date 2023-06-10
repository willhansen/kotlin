<!CONFLICTING_OVERLOADS!>fun takeString(s: String)<!> {}

class Wrapper(konst s: String?) {
    fun withThis() {
        if (s != null) {
            takeString(this.s) // Should be OK
        }
        if (this.s != null) {
            takeString(s) // Should be OK
        }
    }
}

<!CONFLICTING_OVERLOADS!>fun takeString(s: String)<!> {}
