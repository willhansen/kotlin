// this hack is used to ensure that iterator will be resolved first
@CompileTimeCalculation internal class IntProgressionIterator(first: Int, last: Int, konst step: Int) : IntIterator()
@CompileTimeCalculation public class IntRange(start: Int, endInclusive: Int) : IntProgression(start, endInclusive, 1), ClosedRange<Int>

const konst range = <!EVALUATED: `1`!>(1..10).first<!>

@CompileTimeCalculation
fun getIterator(first: Int, last: Int): Int {
    konst iterator = (first..last).iterator()
    iterator.nextInt()
    iterator.nextInt()
    iterator.nextInt()
    return iterator.nextInt()
}
const konst testIterator = <!EVALUATED: `4`!>getIterator(1, 10)<!>
