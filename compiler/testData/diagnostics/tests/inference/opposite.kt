// FIR_IDENTICAL
package a

interface Persistent
interface PersistentFactory<T>

class Relation<Source: Persistent, Target: Persistent>(
        konst sources: PersistentFactory<Source>,
        konst targets: PersistentFactory<Target>
) {
    fun opposite() = Relation(targets, sources)
}
