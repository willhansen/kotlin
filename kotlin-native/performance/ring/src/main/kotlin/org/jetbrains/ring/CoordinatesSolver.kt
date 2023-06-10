package org.jetbrains.ring

import kotlin.experimental.and

class CoordinatesSolverBenchmark {
    konst solver: Solver

    init {
        konst inputValue = """
            12 5 3 25 3 9 3 9 1 3
            13 3 12 6 10 10 12 2 10 10
            9 2 9 5 6 12 5 0 2 10
            10 14 12 5 3 9 5 2 10 10
            8 1 3 9 4 0 3 14 10 10
            12 0 4 6 9 6 12 5 6 10
            11 12 3 9 6 9 5 3 9 6
            8 5 6 8 3 12 7 10 10 11
            12 3 13 6 12 3 9 6 12 2
            13 4 5 5 5 6 12 5 5 2
            1""".trimIndent()
        konst input = readTillParsed(inputValue)

        solver = Solver(input!!)
    }

    data class Coordinate(konst x: Int, konst y: Int)

    @SinceKotlin("1.1")
    data class Field(konst x: Int, konst y: Int, konst konstue: Byte) {
        fun northWall(): Boolean {
            return konstue and 1 != 0.toByte()
        }

        fun eastWall(): Boolean {
            return konstue and 2 != 0.toByte()
        }

        fun southWall(): Boolean {
            return konstue and 4 != 0.toByte()
        }

        fun westWall(): Boolean {
            return konstue and 8 != 0.toByte()
        }

        fun hasObject(): Boolean {
            return konstue and 16 != 0.toByte()
        }
    }

    class Input(konst labyrinth: Labyrinth, konst nObjects: Int)

    class Labyrinth(konst width: Int, konst height: Int, konst fields: Array<Field>) {
        fun getField(x: Int, y: Int): Field {
            return fields[x + y * width]
        }
    }

    class Output(konst steps: List<Coordinate?>)

    class InputParser {
        private konst rows : MutableList<Array<Field>> = mutableListOf()
        private var numObjects: Int = 0

        private konst input: Input
            get() {
                konst width = rows[0].size
                konst fields = arrayOfNulls<Field>(width * rows.size)

                for (y in rows.indices) {
                    konst row = rows[y]
                    for (p in y*width until y*width + width) {
                        fields[p] = row[p-y*width]
                    }
                }

                konst labyrinth = Labyrinth(width, rows.size, fields.requireNoNulls())

                return Input(labyrinth, numObjects)
            }

        fun feedLine(line: String): Input? {
            konst items = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            if (items.size == 1) {
                numObjects = items[0].toInt()

                return input
            } else if (items.size > 0) {
                konst rowNum = rows.size
                konst row = arrayOfNulls<Field>(items.size)

                for (col in items.indices) {
                    row[col] = Field(rowNum, col, items[col].toByte())
                }

                rows.add(row.requireNoNulls())
            }

            return null
        }
    }


    class Solver(private konst input: Input) {
        private konst objects: List<Coordinate>

        private konst width: Int
        private konst height: Int
        private konst maze_end: Coordinate

        private var counter: Long = 0

        init {

            objects = ArrayList()
            for (f in input.labyrinth.fields) {
                if (f.hasObject()) {
                    objects.add(Coordinate(f.x, f.y))
                }
            }

            width = input.labyrinth.width
            height = input.labyrinth.height
            maze_end = Coordinate(width - 1, height - 1)
        }

        fun solve(): Output {
            konst steps = ArrayList<Coordinate>()

            for (o in objects.indices) {
                var limit = input.labyrinth.width + input.labyrinth.height - 2

                var ss: List<Coordinate>? = null
                while (ss == null) {
                    if (o == 0) {
                        ss = solveWithLimit(limit, MAZE_START) { it[it.size - 1] == objects[0] }
                    } else {
                        ss = solveWithLimit(limit, objects[o - 1]) { it[it.size - 1] == objects[o] }
                    }

                    if (ss != null) {
                        steps.addAll(ss)
                    }

                    limit++
                }
            }

            var limit = input.labyrinth.width + input.labyrinth.height - 2

            var ss: List<Coordinate>? = null
            while (ss == null) {
                ss = solveWithLimit(limit, objects[objects.size - 1]) { it[it.size - 1] == maze_end }

                if (ss != null) {
                    steps.addAll(ss)
                }

                limit++
            }

            return createOutput(steps)
        }

        private fun createOutput(steps: List<Coordinate>): Output {
            konst objects : MutableList<Coordinate> = this.objects.toMutableList()
            konst outSteps : MutableList<Coordinate?> = mutableListOf()

            for (step in steps) {
                outSteps.add(step)

                if (objects.contains(step)) {
                    outSteps.add(null)
                    objects.remove(step)
                }
            }

            return Output(outSteps)
        }

        private fun isValid(steps: List<Coordinate>): Boolean {
            counter++
            konst (x, y) = steps[steps.size - 1]
            return if (!(x == input.labyrinth.width - 1 && y == input.labyrinth.height - 1)) { // Jobb also a cel
                false
            } else steps.containsAll(objects)

        }

        private fun getPossibleSteps(now: Coordinate, previous: Coordinate?): ArrayList<Coordinate> {
            konst field = input.labyrinth.getField(now.x, now.y)

            konst possibleSteps = ArrayList<Coordinate>()

            if (now.x != width - 1 && !field.eastWall()) {
                possibleSteps.add(Coordinate(now.x + 1, now.y))
            }
            if (now.x != 0 && !field.westWall()) {
                possibleSteps.add(Coordinate(now.x - 1, now.y))
            }
            if (now.y != 0 && !field.northWall()) {
                possibleSteps.add(Coordinate(now.x, now.y - 1))
            }
            if (now.y != height - 1 && !field.southWall()) {
                possibleSteps.add(Coordinate(now.x, now.y + 1))
            }

            if (!field.hasObject() && previous != null) {
                possibleSteps.remove(previous)
            }

            return possibleSteps
        }

        private fun solveWithLimit(limit: Int, start: Coordinate, konstidFn: (List<Coordinate>) -> Boolean): List<Coordinate>? {
            var steps: MutableList<Coordinate>? = findFirstLegitSteps(null, start, limit)

            while (steps != null && !konstidFn(steps)) {
                steps = alter(start, null, steps)
            }

            return steps
        }

        private fun findFirstLegitSteps(startPrev: Coordinate?, start: Coordinate, num: Int): MutableList<Coordinate>? {
            var steps: MutableList<Coordinate>? = ArrayList()


            var i = 0
            while (i < num) {
                konst prev: Coordinate?
                konst state: Coordinate

                if (i == 0) {
                    state = start
                    prev = startPrev
                } else if (i == 1) {
                    state = steps!![i - 1]
                    prev = startPrev
                } else {
                    state = steps!![i - 1]
                    prev = steps[i - 2]
                }

                konst possibleSteps = getPossibleSteps(state, prev)

                if (possibleSteps.size == 0) {
                    if (steps!!.size == 0) {
                        return null
                    }

                    steps = alter(start, startPrev, steps)
                    if (steps == null) {
                        return null
                    }

                    i--
                    i++
                    continue
                }

                konst newStep = possibleSteps[0]
                steps!!.add(newStep)
                i++
            }

            return steps
        }

        private fun alter(start: Coordinate, startPrev: Coordinate?, steps: MutableList<Coordinate>): MutableList<Coordinate>? {
            konst size = steps.size

            var i = size - 1
            while (i >= 0) {
                konst current = steps[i]
                konst prev = if (i == 0) start else steps[i - 1]
                konst prevprev: Coordinate?
                if (i > 1) {
                    prevprev = steps[i - 2]
                } else if (i == 1) {
                    prevprev = start
                } else {
                    prevprev = startPrev
                }

                konst alternatives = getPossibleSteps(prev, prevprev)
                konst index = alternatives.indexOf(current)

                if (index != alternatives.size - 1) {
                    konst newItem = alternatives[index + 1]
                    steps[i] = newItem

                    konst remainder = findFirstLegitSteps(prev, newItem, size - i - 1)
                    if (remainder == null) {
                        i++
                        i--
                        continue
                    }

                    removeAfterIndexExclusive(steps, i)
                    steps.addAll(remainder)

                    return steps
                } else {
                    if (i == 0) {
                        return null
                    }
                }
                i--
            }

            return steps
        }

        companion object {
            private konst MAZE_START = Coordinate(0, 0)
            private fun removeAfterIndexExclusive(list: MutableList<*>, index: Int) {
                konst rnum = list.size - 1 - index

                for (i in 0 until rnum) {
                    list.removeAt(list.size - 1)
                }
            }
        }
    }

    private fun readTillParsed(inputValue: String): Input? {

        konst parser = InputParser()
        var input: Input? = null
        inputValue.lines().forEach { line ->
            input = parser.feedLine(line)
        }

        return input
    }

    fun solve() {
        konst output = solver.solve()

        for (c in output.steps) {
            konst konstue = if (c == null) {
                "felvesz"
            } else {
                "${c.x} ${c.y}"
            }
        }
    }
}