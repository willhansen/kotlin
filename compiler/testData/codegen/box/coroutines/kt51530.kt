// WITH_STDLIB
// IGNORE_BACKEND: JVM

import kotlin.coroutines.*

interface Flow<out T> {
    suspend fun collect(collector: FlowCollector<T>)
}

fun interface FlowCollector<in T> {
    suspend fun emit(konstue: T)
}

inline fun <T, R> Flow<T>.transform(
    crossinline transform: suspend FlowCollector<R>.(konstue: T) -> Unit
): Flow<R> = object : Flow<R> {
    override suspend fun collect(collector: FlowCollector<R>) {
        this@transform.collect a@{ konstue ->
            return@a collector.transform(konstue)
        }
    }
}

public inline fun <T, R: Any> Flow<T>.mapNotNull(crossinline transform: suspend (konstue: T) -> R?): Flow<R> = transform { konstue ->
    konst transformed = transform(konstue) ?: return@transform
    return@transform emit(transformed)
}

internal fun Flow<List<Data>>.aggregate(aggregation: (List<Double>) -> Double): Flow<Data> = mapNotNull {
    Data(
        p1	=	aggregation(it.mapNotNull { it.	p1	}),
        p2	=	aggregation(it.mapNotNull { it.	p2	}),
        p3	=	aggregation(it.mapNotNull { it.	p3	}),
        p4	=	aggregation(it.mapNotNull { it.	p4	}),
        p5	=	aggregation(it.mapNotNull { it.	p5	}),
        p6	=	aggregation(it.mapNotNull { it.	p6	}),
        p7	=	aggregation(it.mapNotNull { it.	p7	}),
        p8	=	aggregation(it.mapNotNull { it.	p8	}),
        p9	=	aggregation(it.mapNotNull { it.	p9	}),
        p10	=	aggregation(it.mapNotNull { it.	p10	}),
        p11	=	aggregation(it.mapNotNull { it.	p11	}),
        p12	=	aggregation(it.mapNotNull { it.	p12	}),
        p13	=	aggregation(it.mapNotNull { it.	p13	}),
        p14	=	aggregation(it.mapNotNull { it.	p14	}),
        p15	=	aggregation(it.mapNotNull { it.	p15	}),
        p16	=	aggregation(it.mapNotNull { it.	p16	}),
        p17	=	aggregation(it.mapNotNull { it.	p17	}),
        p18	=	aggregation(it.mapNotNull { it.	p18	}),
        p19	=	aggregation(it.mapNotNull { it.	p19	}),
        p20	=	aggregation(it.mapNotNull { it.	p20	}),
        p21	=	aggregation(it.mapNotNull { it.	p21	}),
        p22	=	aggregation(it.mapNotNull { it.	p22	}),
        p23	=	aggregation(it.mapNotNull { it.	p23	}),
        p24	=	aggregation(it.mapNotNull { it.	p24	}),
        p25	=	aggregation(it.mapNotNull { it.	p25	}),
        p26	=	aggregation(it.mapNotNull { it.	p26	}),
        p27	=	aggregation(it.mapNotNull { it.	p27	}),
        p28	=	aggregation(it.mapNotNull { it.	p28	}),
        p29	=	aggregation(it.mapNotNull { it.	p29	}),
        p30	=	aggregation(it.mapNotNull { it.	p30	}),
        p31	=	aggregation(it.mapNotNull { it.	p31	}),
        p32	=	aggregation(it.mapNotNull { it.	p32	}),
        p33	=	aggregation(it.mapNotNull { it.	p33	}),
        p34	=	aggregation(it.mapNotNull { it.	p34	}),
        p35	=	aggregation(it.mapNotNull { it.	p35	}),
        p36	=	aggregation(it.mapNotNull { it.	p36	}),
        p37	=	aggregation(it.mapNotNull { it.	p37	}),
        p38	=	aggregation(it.mapNotNull { it.	p38	}),
        p39	=	aggregation(it.mapNotNull { it.	p39	}),
        p40	=	aggregation(it.mapNotNull { it.	p40	}),
        p41	=	aggregation(it.mapNotNull { it.	p41	}),
        p42	=	aggregation(it.mapNotNull { it.	p42	}),
        p43	=	aggregation(it.mapNotNull { it.	p43	}),
        p44	=	aggregation(it.mapNotNull { it.	p44	}),
        p45	=	aggregation(it.mapNotNull { it.	p45	}),
        p46	=	aggregation(it.mapNotNull { it.	p46	}),
        p47	=	aggregation(it.mapNotNull { it.	p47	}),
        p48	=	aggregation(it.mapNotNull { it.	p48	}),
        p49	=	aggregation(it.mapNotNull { it.	p49	}),
        p50	=	aggregation(it.mapNotNull { it.	p50	}),
        p51	=	aggregation(it.mapNotNull { it.	p51	}),
        p52	=	aggregation(it.mapNotNull { it.	p52	}),
        p53	=	aggregation(it.mapNotNull { it.	p53	}),
        p54	=	aggregation(it.mapNotNull { it.	p54	}),
        p55	=	aggregation(it.mapNotNull { it.	p55	}),
        p56	=	aggregation(it.mapNotNull { it.	p56	}),
        p57	=	aggregation(it.mapNotNull { it.	p57	}),
        p58	=	aggregation(it.mapNotNull { it.	p58	}),
        p59	=	aggregation(it.mapNotNull { it.	p59	}),
        p60	=	aggregation(it.mapNotNull { it.	p60	}),
        p61	=	aggregation(it.mapNotNull { it.	p61	}),
        p62	=	aggregation(it.mapNotNull { it.	p62	}),
        p63	=	aggregation(it.mapNotNull { it.	p63	}),
        p64	=	aggregation(it.mapNotNull { it.	p64	}),
        p65	=	aggregation(it.mapNotNull { it.	p65	}),
        p66	=	aggregation(it.mapNotNull { it.	p66	}),
        p67	=	aggregation(it.mapNotNull { it.	p67	}),
        p68	=	aggregation(it.mapNotNull { it.	p68	}),
        p69	=	aggregation(it.mapNotNull { it.	p69	}),
        p70	=	aggregation(it.mapNotNull { it.	p70	}),
        p71	=	aggregation(it.mapNotNull { it.	p71	}),
        p72	=	aggregation(it.mapNotNull { it.	p72	}),
        p73	=	aggregation(it.mapNotNull { it.	p73	}),
        p74	=	aggregation(it.mapNotNull { it.	p74	}),
        p75	=	aggregation(it.mapNotNull { it.	p75	}),
        p76	=	aggregation(it.mapNotNull { it.	p76	}),
        p77	=	aggregation(it.mapNotNull { it.	p77	}),
        p78	=	aggregation(it.mapNotNull { it.	p78	}),
        p79	=	aggregation(it.mapNotNull { it.	p79	}),
        p80	=	aggregation(it.mapNotNull { it.	p80	}),
        p81	=	aggregation(it.mapNotNull { it.	p81	}),
        p82	=	aggregation(it.mapNotNull { it.	p82	}),
        p83	=	aggregation(it.mapNotNull { it.	p83	}),
        p84	=	aggregation(it.mapNotNull { it.	p84	}),
        p85	=	aggregation(it.mapNotNull { it.	p85	}),
        p86	=	aggregation(it.mapNotNull { it.	p86	}),
    )
}

data class Data(
    konst	p1	: Double?,
    konst	p2	: Double?,
    konst	p3	: Double?,
    konst	p4	: Double?,
    konst	p5	: Double?,
    konst	p6	: Double?,
    konst	p7	: Double?,
    konst	p8	: Double?,
    konst	p9	: Double?,
    konst	p10	: Double?,
    konst	p11	: Double?,
    konst	p12	: Double?,
    konst	p13	: Double?,
    konst	p14	: Double?,
    konst	p15	: Double?,
    konst	p16	: Double?,
    konst	p17	: Double?,
    konst	p18	: Double?,
    konst	p19	: Double?,
    konst	p20	: Double?,
    konst	p21	: Double?,
    konst	p22	: Double?,
    konst	p23	: Double?,
    konst	p24	: Double?,
    konst	p25	: Double?,
    konst	p26	: Double?,
    konst	p27	: Double?,
    konst	p28	: Double?,
    konst	p29	: Double?,
    konst	p30	: Double?,
    konst	p31	: Double?,
    konst	p32	: Double?,
    konst	p33	: Double?,
    konst	p34	: Double?,
    konst	p35	: Double?,
    konst	p36	: Double?,
    konst	p37	: Double?,
    konst	p38	: Double?,
    konst	p39	: Double?,
    konst	p40	: Double?,
    konst	p41	: Double?,
    konst	p42	: Double?,
    konst	p43	: Double?,
    konst	p44	: Double?,
    konst	p45	: Double?,
    konst	p46	: Double?,
    konst	p47	: Double?,
    konst	p48	: Double?,
    konst	p49	: Double?,
    konst	p50	: Double?,
    konst	p51	: Double?,
    konst	p52	: Double?,
    konst	p53	: Double?,
    konst	p54	: Double?,
    konst	p55	: Double?,
    konst	p56	: Double?,
    konst	p57	: Double?,
    konst	p58	: Double?,
    konst	p59	: Double?,
    konst	p60	: Double?,
    konst	p61	: Double?,
    konst	p62	: Double?,
    konst	p63	: Double?,
    konst	p64	: Double?,
    konst	p65	: Double?,
    konst	p66	: Double?,
    konst	p67	: Double?,
    konst	p68	: Double?,
    konst	p69	: Double?,
    konst	p70	: Double?,
    konst	p71	: Double?,
    konst	p72	: Double?,
    konst	p73	: Double?,
    konst	p74	: Double?,
    konst	p75	: Double?,
    konst	p76	: Double?,
    konst	p77	: Double?,
    konst	p78	: Double?,
    konst	p79	: Double?,
    konst	p80	: Double?,
    konst	p81	: Double?,
    konst	p82	: Double?,
    konst	p83	: Double?,
    konst	p84	: Double?,
    konst	p85	: Double?,
    konst	p86	: Double?,
)

fun box(): String {
    object : Flow<List<Data>> {
        override suspend fun collect(collector: FlowCollector<List<Data>>) {
        }
    }.aggregate { 1.0 }
    return "OK"
}