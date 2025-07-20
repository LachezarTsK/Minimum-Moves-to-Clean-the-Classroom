
import java.util.*

class Solution {

    private data class Point(val row: Int, val column: Int, val energy: Int, val bitstampForCollectedLitter: Int) {}

    private companion object {
        const val NO_MOVES_ARE_NEEDED = 0
        const val NOT_POSSIBLE_TO_CLEAN_THE_CLASSROOM = -1

        const val EMPTY = '.'
        const val START = 'S'
        const val LITTER = 'L'
        const val OBSTACLE = 'X'
        const val CAN_RESTORE_ENERGY = 'R'

        val UP = intArrayOf(-1, 0)
        val DOWN = intArrayOf(1, 0)
        val LEFT = intArrayOf(0, -1)
        val RIGHT = intArrayOf(0, 1)

        val MOVES = arrayOf(UP, DOWN, LEFT, RIGHT)
    }

    private var rows: Int = 0
    private var columns: Int = 0
    private var maxEnergy: Int = 0
    private var bitstampForAllLitter: Int = 0

    private lateinit var classroom: Array<String>
    private lateinit var bitstampForEachLitter: Array<IntArray>

    fun minMoves(classroom: Array<String>, energy: Int): Int {
        rows = classroom.size
        columns = classroom[0].length
        maxEnergy = energy

        this.classroom = classroom
        bitstampForEachLitter = createBitstampForEachLitter()
        bitstampForAllLitter = createBitstampForAllLitter()
        val start = findStartPoint()

        return findMinMovesToCollectAllLitter(start)
    }

    private fun findMinMovesToCollectAllLitter(start: Point): Int {
        if (bitstampForAllLitter == 0) {
            return NO_MOVES_ARE_NEEDED
        }

        val queue = LinkedList<Point>()
        queue.add(start)

        val bestEnergyPerBitstampForCollectedLitter = Array(rows){Array(columns) { HashMap<Int, Int>() } }
        bestEnergyPerBitstampForCollectedLitter[start.row][start.column][start.bitstampForCollectedLitter] = start.energy

        var stepsFromStart = 0

        while (!queue.isEmpty()) {
            ++stepsFromStart
            var sizeCurrentRoundOfSteps = queue.size

            while (sizeCurrentRoundOfSteps > 0) {
                val current = queue.poll()

                for (move in MOVES) {
                    val nextRow = current.row + move[0]
                    val nextColumn = current.column + move[1]
                    if (!isInClassroom(nextRow, nextColumn) || classroom[nextRow][nextColumn] == OBSTACLE) {
                        continue
                    }

                    val nextEnergy = getNextEnergy(current.energy, classroom[nextRow][nextColumn])
                    val nextBitstampCollectedLitter = getNextBitstampForCollectedLitter(current, nextRow, nextColumn)

                    if (nextBitstampCollectedLitter == bitstampForAllLitter) {
                        return stepsFromStart
                    }

                    if (nextEnergy == 0) {
                        continue
                    }
                    if (bestEnergyPerBitstampForCollectedLitter[nextRow][nextColumn].containsKey(nextBitstampCollectedLitter) &&
                        bestEnergyPerBitstampForCollectedLitter[nextRow][nextColumn][nextBitstampCollectedLitter]!! >= nextEnergy) {
                        continue
                    }

                    bestEnergyPerBitstampForCollectedLitter[nextRow][nextColumn][nextBitstampCollectedLitter] = nextEnergy
                    queue.add(Point(nextRow, nextColumn, nextEnergy, nextBitstampCollectedLitter))
                }

                --sizeCurrentRoundOfSteps
            }
        }

        return NOT_POSSIBLE_TO_CLEAN_THE_CLASSROOM
    }

    private fun findStartPoint(): Point {
        var start: Point = Point(0,0,0,0)
        for (row in 0..<rows) {
            for (column in 0..<columns) {
                if (classroom[row][column] == START) {
                    start = Point(row, column, maxEnergy, 0)
                    break
                }
            }
        }
        return start
    }

    private fun createBitstampForAllLitter(): Int {
        var bitstampForAllLitter = 0
        for (row in 0..<rows) {
            for (column in 0..<columns) {
                bitstampForAllLitter = bitstampForAllLitter or bitstampForEachLitter[row][column]
            }
        }
        return bitstampForAllLitter
    }

    private fun createBitstampForEachLitter(): Array<IntArray> {
        var counterLitter = 0
        val bitstamp = Array(rows) { IntArray(columns) }

        for (row in 0..<rows) {
            for (column in 0..<columns) {
                if (classroom[row][column] == LITTER) {
                    bitstamp[row][column] = 1 shl counterLitter
                    ++counterLitter
                }
            }
        }
        return bitstamp
    }

    private fun getNextBitstampForCollectedLitter(current: Point, nextRow: Int, nextColumn: Int): Int {
        var nextBitstampCollectedLitter = current.bitstampForCollectedLitter
        if (classroom[nextRow][nextColumn] == LITTER) {
            nextBitstampCollectedLitter = nextBitstampCollectedLitter or bitstampForEachLitter[nextRow][nextColumn]
        }
        return nextBitstampCollectedLitter
    }

    private fun getNextEnergy(currentEnergy: Int, pointType: Char): Int {
        if (pointType == CAN_RESTORE_ENERGY) {
            return maxEnergy
        }
        return currentEnergy - 1
    }

    private fun isInClassroom(row: Int, column: Int): Boolean {
        return row in 0..<rows && column in 0..<columns
    }
}
