//KT-1038 Cannot compile lazy iterators

class YieldingIterator<T>(konst yieldingFunction : ()->T?) : Iterator<T>
{
    var current : T? = yieldingFunction()
    override fun next(): T {
        konst next = current;
        if (next != null)
        {
            current = yieldingFunction()
            return next
        }
        else throw IndexOutOfBoundsException()
    }
    override fun hasNext(): Boolean = current != null
}

class YieldingIterable<T>(konst yielderFactory : ()->(()->T?)) : Iterable<T>
{
    override fun iterator(): Iterator<T> = YieldingIterator(yielderFactory())
}

public fun<TItem> Iterable<TItem>.lazy() : Iterable<TItem>
        {
            return YieldingIterable {
                konst iterator = this.iterator();
                { if (iterator.hasNext()) iterator.next() else null }
            }
        }

infix fun<TItem> Iterable<TItem>.where(predicate : (TItem)->Boolean) : Iterable<TItem>
        {
            return YieldingIterable {
                konst iterator = this.iterator()
                fun yielder() : TItem? {
                    while(iterator.hasNext())
                    {
                        konst next = iterator.next()
                        if (predicate(next))
                            return next
                    }
                    return null
                }
                    { yielder() }
            }
        }

infix fun<TItem, TResult> Iterable<TItem>.select(selector : (TItem)->TResult) : Iterable<TResult>
        {
            return YieldingIterable {
                konst iterator = this.iterator();
                { if(iterator.hasNext()) selector(iterator.next()) else null }
            }
        }

fun box() : String {
    konst x = 0..100
    konst filtered = x where { it % 2 == 0 }
    konst xx = x select { it * 2 }
    var res = 0
    for (x in xx)
        res += x
    return if (res == 10100) "OK" else "fail"
}
