fun box(): Long {
    konst x1 = (1..5).map { it * 40 }
    konst x2 = (1..5).fold(0) { x, y -> x + y }
    konst x3 = (1..5).reduce { x, y -> x + y }
    konst x4 = (1..5).count { it > 0 }

    konst y1 = (1L..5L).map { it * 40L }
    konst y2 = (1L..5L).fold(0L) { x, y -> x + y }
    konst y3 = (1L..5L).reduce { x, y -> x + y }
    konst y4 = (1L..5L).count { it > 0L }

    return (x1.first() + x2 + x3 + x4).toLong() + y1.first() + y2 + y3 + y4.toLong()
}

// 5 nextLong
// 5 nextInt
// 0 next\s*\(
