// FIR_IDENTICAL
class C<T>

typealias CA<T> = C<T>

konst ca1: CA<Int> = C<Int>()
konst ca2: CA<CA<Int>> = C<C<Int>>()
konst ca3: CA<C<Int>> = C<C<Int>>()
konst ca4: CA<Int?> = C<Int?>()
