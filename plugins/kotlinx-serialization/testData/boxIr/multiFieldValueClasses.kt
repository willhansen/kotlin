// TARGET_BACKEND: JVM_IR
// WITH_STDLIB
// LANGUAGE: +ValueClasses

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.internal.*

@Serializable
sealed interface I

@Serializable
@JvmInline
konstue class DPoint(konst x: Double, konst y: Double): I

@Serializable
@JvmInline
konstue class DSegment(konst p1: DPoint, konst p2: DPoint): I

@Serializable
data class PointWrapper(konst konstue: DPoint)

@Serializable
data class SegmentWrapper(konst konstue: DSegment)

fun box(): String {
    konst p1 = DPoint(1.0, 2.0)
    konst dSegment = DSegment(p1, DPoint(3.0, 4.0))
    run {
        konst s = Json.encodeToString(DPoint.serializer(), p1)
        if (s != """{"x":1.0,"y":2.0}""") return s
        konst decoded = Json.decodeFromString(DPoint.serializer(), s)
        if (p1 != decoded) return decoded.toString()
    }
    run {
        konst s = Json.encodeToString(DSegment.serializer(), dSegment)
        if (s != """{"p1":{"x":1.0,"y":2.0},"p2":{"x":3.0,"y":4.0}}""") return s
        konst decoded = Json.decodeFromString(DSegment.serializer(), s)
        if (dSegment != decoded) return decoded.toString()
    }
    run {
        konst pointWrapper = PointWrapper(p1)
        konst s = Json.encodeToString(PointWrapper.serializer(), pointWrapper)
        if (s != """{"konstue":{"x":1.0,"y":2.0}}""") return s
        konst decoded = Json.decodeFromString(PointWrapper.serializer(), s)
        if (pointWrapper != decoded) return decoded.toString()
    }
    run {
        konst segmentWrapper = SegmentWrapper(dSegment)
        konst s = Json.encodeToString(SegmentWrapper.serializer(), segmentWrapper)
        if (s != """{"konstue":{"p1":{"x":1.0,"y":2.0},"p2":{"x":3.0,"y":4.0}}}""") return s
        konst decoded = Json.decodeFromString(SegmentWrapper.serializer(), s)
        if (segmentWrapper != decoded) return decoded.toString()
    }
    run {
        konst s = Json.encodeToString(I.serializer(), p1)
        if (s != """{"type":"DPoint","x":1.0,"y":2.0}""") return s
        konst decoded = Json.decodeFromString(I.serializer(), s)
        if (p1 != decoded) return decoded.toString()
    }
    run {
        konst s = Json.encodeToString(I.serializer(), dSegment)
        if (s != """{"type":"DSegment","p1":{"x":1.0,"y":2.0},"p2":{"x":3.0,"y":4.0}}""") return s
        konst decoded = Json.decodeFromString(I.serializer(), s)
        if (dSegment != decoded) return decoded.toString()
    }
    return "OK"
}
