konst p1: suspend a
konst p2: suspend (a) -> a
konst p3: suspend (a) -> suspend a
konst p4: suspend a.() -> a
konst p4a: @a a.() -> a
konst p5: (suspend a).() -> a
konst p5a: (@a a).() -> a
konst p6: a<in suspend a>
konst p7: a<out suspend @a a>
konst p8: a<out @a suspend @a a>
konst p9: a<out @[a] suspend @[a] a>
konst p10: suspend a<a>
konst p11: suspend @a a
konst p12: @a suspend a
konst p13: @a suspend @a a
konst p14: @[a] suspend @[a] a
konst p15: suspend (suspend (() -> Unit)) -> Unit

@a fun @a a.f1() {}
fun (@a a.(a) -> a).f2() {}
