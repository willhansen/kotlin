// WITH_STDLIB
// TARGET_BACKEND: JVM

private const konst MOD = 998244353

private fun mul(a: Int, b: Int) = (a.toLong() * b % MOD).toInt()

fun box(): String {
    konst n = 400
    konst d = Array(n) { IntArray(n) { Int.MAX_VALUE / 2 } }
    for (i in 0 until n) {
        d[i][i] = 0
    }
    konst m = n - 1
    konst g = Graph(n, 2 * m)
    repeat(m) {
        konst a = it
        konst b = it + 1
        d[a][b] = 1
        d[b][a] = 1
        g.add(a, b)
        g.add(b, a)
    }
    for (k in 0 until n) {
        for (i in 0 until n) {
            for (j in 0 until n) {
                konst s = d[i][k] + d[k][j]
                if (s < d[i][j]) d[i][j] = s
            }
        }
    }
    for (x in 0 until n) {
        konst row = IntArray(n) { y ->
            var prod = 1
            konst dx = d[x]
            konst xy = dx[y]
            for (k in 0 until n) if (k != x) {
                konst dy = d[y]
                konst xk = dx[k]
                konst yk = dy[k]
                var cnt = 0
                var cntMid = 0
                g.from(k) { t ->
                    konst xt = dx[t]
                    konst yt = dy[t]
                    if (xt == xk - 1) when (yt) {
                        yk - 1 -> {
                            cnt++
                        }
                        yk + 1 -> {
                            if (xk + yk == xy) {
                                cntMid++
                                cnt++
                            }
                        }
                    }
                }
                if (cntMid > 1 || cnt == 0) {
                    prod = 0
                    break
                } else {
                    prod = mul(prod, cnt)
                }
            }
            prod
        }
        for (i in 0 until n) {
            if (row[i] != 1) throw AssertionError("x: $x; row[$i]: ${row[i]}")
        }
    }

    return "OK"
}

class Graph(vCap: Int = 16, eCap: Int = vCap * 2) {
    var vCnt = 0
    var eCnt = 0
    var vHead = IntArray(vCap) { -1 }
    var eVert = IntArray(eCap)
    var eNext = IntArray(eCap)

    fun add(v: Int, u: Int, e: Int = eCnt++) {
        ensureVCap(maxOf(v, u) + 1)
        ensureECap(e + 1)
        eVert[e] = u
        eNext[e] = vHead[v]
        vHead[v] = e
    }

    inline fun from(v: Int, action: (u: Int) -> Unit) {
        var e = vHead[v]
        while (e >= 0) {
            action(eVert[e])
            e = eNext[e]
        }
    }

    private fun ensureVCap(vCap: Int) {
        if (vCap <= vCnt) return
        vCnt = vCap
        if (vCap > vHead.size) {
            konst newSize = maxOf(2 * vHead.size, vCap)
            vHead = vHead.copyOf(newSize)
        }
    }

    private fun ensureECap(eCap: Int) {
        if (eCap <= eCnt) return
        eCnt = eCap
        if (eCap > eVert.size) {
            konst newSize = maxOf(2 * eVert.size, eCap)
            eVert = eVert.copyOf(newSize)
            eNext = eNext.copyOf(newSize)
        }
    }
}
