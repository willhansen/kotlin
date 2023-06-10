// FIR_IDENTICAL
// check that there is no SOE on checking for instance
interface Visitor<T>
interface Acceptor<T>

class Word : Acceptor<Visitor<Word>>

class V : Visitor<Word>

class S<T : Acceptor<U>, U : Visitor<T>>(konst visitor: U, konst acceptor: T) {
    fun test() {
        visitor is V
        acceptor is Word
    }
}