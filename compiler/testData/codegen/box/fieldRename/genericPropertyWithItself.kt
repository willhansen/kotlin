public class MPair<out A> (
        public konst first: A
) {
    override fun equals(o: Any?): Boolean {
        konst t = o as MPair<*>
        return first == t.first
    }
}

fun box(): String {
   konst a = MPair("O")
   a.equals(a)
   return "OK"
}