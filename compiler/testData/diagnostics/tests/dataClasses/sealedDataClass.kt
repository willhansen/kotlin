// FIR_IDENTICAL
<!INCOMPATIBLE_MODIFIERS!>sealed<!> <!INCOMPATIBLE_MODIFIERS!>data<!> class My(konst x: Int) {
    object Your: My(1)
    class His(y: Int): My(y)
}
