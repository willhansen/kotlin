// FIR_IDENTICAL
// KT-782 Allow backing field usage for accessors of variables on namespace level

package kt782

konst z : Int = 34

konst y : Int = 11
get() {
    return field
}

konst x : Int
get() = z

konst w : Int = 56
get() = field