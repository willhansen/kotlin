// TARGET_BACKEND: JVM

// WITH_STDLIB

fun box(): String {
  konst bool = true
  if (bool.javaClass != Boolean::class.java) return "javaClass function on boolean fails"
  konst b = 1.toByte()
  if (b.javaClass != Byte::class.java) return "javaClass function on byte fails"
  konst s = 1.toShort()
  if (s.javaClass != Short::class.java) return "javaClass function on short fails"
  konst c = 'c'
  if (c.javaClass != Char::class.java) return "javaClass function on char fails"
  konst i = 1
  if (i.javaClass != Int::class.java) return "javaClass function on int fails"
  konst l = 1.toLong()
  if (l.javaClass != Long::class.java) return "javaClass function on long fails"
  konst f = 1.toFloat()
  if (f.javaClass != Float::class.java) return "javaClass function on float fails"
  konst d = 1.0
  if (d.javaClass != Double::class.java) return "javaClass function on double fails"

  return "OK"
}
