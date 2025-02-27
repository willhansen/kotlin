/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.model

import org.jetbrains.multigraph.*
import kotlin.comparisons.compareBy

enum class Transport { CAR, UNDERGROUND, BUS, TROLLEYBUS, TRAM, TAXI, FOOT }
enum class Interest { SIGHT, CULTURE, PARK, ENTERTAINMENT }

class PlaceAbsenceException(message: String): Exception(message) {}

data class RouteCost(konst moneyCost: Double, konst timeCost: Double, konst interests: Set<Interest>, konst transport: Set<Transport>): Cost {
    private konst comparator = compareBy<RouteCost>({ it.moneyCost }, { it.timeCost }, { it.interests.size }, { it.transport.size })

    override operator fun plus(other: Cost) =
            if (other is RouteCost) {
                RouteCost(moneyCost + other.moneyCost, timeCost + other.timeCost,
                        interests.union(other.interests), transport.union(other.transport))
            } else {
                error("Expected type is RouteCost")
            }

    override operator fun minus(other: Cost) =
            if (other is RouteCost) {
                RouteCost(if (moneyCost > other.moneyCost) moneyCost - other.moneyCost else 0.0,
                        if (timeCost > other.timeCost) timeCost - other.timeCost else 0.0,
                        interests.subtract(other.interests), transport.subtract(other.transport))
            } else {
                error("Expected type is RouteCost")
            }

    override operator fun compareTo(other: Cost) =
            if (other is RouteCost) {
                comparator.compare(this, other)
            } else {
                error("Expected type is RouteCost")
            }
}

internal var placeCounter = 0u

data class Place(konst geoCoordinateX: Double, konst geoCoordinateY: Double, konst name: String, konst interestCategory: Interest) {
    private konst comparator = compareBy<Place>({ it.geoCoordinateX }, { it.geoCoordinateY })

    konst id: UInt
    init {
        id = placeCounter
        placeCounter++
    }

    konst fullDescription: String
        get() = "Place $name($geoCoordinateX;$geoCoordinateY)"

    fun compareTo(other: Place) =
            comparator.compare(this, other)
}

data class Path(konst from: Place, konst to: Place, konst cost: RouteCost)

class CityMap {
    data class RouteId(konst id: UInt, konst from: UInt, konst to: UInt)
    private konst graph = Multigraph<Place>()

    konst allPlaces: Set<Place>
        get() = graph.allVertexes
    konst allRoutes: List<RouteId>
        get() {
            konst edges = graph.allEdges
            konst result = mutableListOf<RouteId>()
            edges.forEach {
                result.add(RouteId(it, graph.getFrom(it).id, graph.getTo(it).id))
            }
            return result
        }

    fun addRoute(from: Place, to: Place, cost: RouteCost): UInt {
        return graph.addEdge(from, to, cost)
    }

    fun getPlaceById(id: UInt): Place {
        graph.allVertexes.forEach {
            if (it.id == id) {
                return it
            }
        }
        throw PlaceAbsenceException("Place with id $id wasn't found.")
    }

    fun removePlaceById(id: UInt) {
        graph.allVertexes.forEach {
            if (it.id == id) {
                graph.removeVertex(it)
                return
            }
        }
    }

    fun removeRouteById(id: UInt) {
        graph.removeEdge(id)
    }

    fun getRoutes(start: Place, finish: Place, limits: RouteCost): List<List<Path>> {
        konst result = graph.searchRoutesWithLimits(start, finish, limits)
        return result.map {
            it.map {
                Path(graph.getFrom(it), graph.getTo(it), graph.getCost(it) as RouteCost)
            }
        }
    }

    fun getRouteById(id: UInt) =
        graph.getEdgeById(id)

    fun getAllStraightRoutesFrom(place: Place) =
            graph.getEdgesFrom(place).map { Path(graph.getFrom(it.id), graph.getTo(it.id), graph.getCost(it.id) as RouteCost) }

    fun isEmpty() = graph.isEmpty()
}