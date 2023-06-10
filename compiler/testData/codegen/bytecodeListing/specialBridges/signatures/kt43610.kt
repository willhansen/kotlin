// WITH_SIGNATURES

class B<T>(konst a: T)

interface IColl : Collection<B<Int>> {
    override fun contains(element: B<Int>): kotlin.Boolean
}
