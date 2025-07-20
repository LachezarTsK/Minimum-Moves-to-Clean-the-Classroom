
package main

const NO_MOVES_ARE_NEEDED = 0
const NOT_POSSIBLE_TO_CLEAN_THE_CLASSROOM = -1

const EMPTY = '.'
const START = 'S'
const LITTER = 'L'
const OBSTACLE = 'X'
const CAN_RESTORE_ENERGY = 'R'

var UP = [2]int{-1, 0}
var DOWN = [2]int{1, 0}
var LEFT = [2]int{0, -1}
var RIGHT = [2]int{0, 1}
var MOVES = [4][2]int{UP, DOWN, LEFT, RIGHT}

var rows int
var columns int
var maxEnergy int
var bitstampForAllLitter int

var classroom []string
var bitstampForEachLitter [][]int

type Point struct {
    row                        int
    column                     int
    energy                     int
    bitstampForCollectedLitter int
}

func NewPoint(row int, column int, energy int, bitstampForCollectedLitter int) Point {
    point := Point{
        row:                        row,
        column:                     column,
        energy:                     energy,
        bitstampForCollectedLitter: bitstampForCollectedLitter,
    }
    return point
}

func minMoves(_classroom []string, energy int) int {
    rows = len(_classroom)
    columns = len(_classroom[0])
    maxEnergy = energy

    classroom = _classroom
    bitstampForEachLitter = createBitstampForEachLitter()
    bitstampForAllLitter = createBitstampForAllLitter()
    start := findStartPoint()

    return findMinMovesToCollectAllLitter(start)
}

func findMinMovesToCollectAllLitter(start Point) int {
    if bitstampForAllLitter == 0 {
        return NO_MOVES_ARE_NEEDED
    }

    queue := make([]Point, 0)
    queue = append(queue, start)

    var bestEnergyPerBitstampForCollectedLitter [][]map[int]int = make([][]map[int]int, rows)
    for row := range rows {
        bestEnergyPerBitstampForCollectedLitter[row] = make([]map[int]int, columns)
        for column := range columns {
            bestEnergyPerBitstampForCollectedLitter[row][column] = map[int]int{}
        }
    }

    bestEnergyPerBitstampForCollectedLitter[start.row][start.column][start.bitstampForCollectedLitter] = start.energy

    var stepsFromStart = 0

    for len(queue) > 0 {
        stepsFromStart++
        sizeCurrentRoundOfSteps := len(queue)

        for sizeCurrentRoundOfSteps > 0 {
            current := queue[0]
            queue = queue[1:]

            for _, move := range MOVES {
                nextRow := current.row + move[0]
                nextColumn := current.column + move[1]
                if !isInClassroom(nextRow, nextColumn) || classroom[nextRow][nextColumn] == OBSTACLE {
                    continue
                }

                nextEnergy := getNextEnergy(current.energy, rune(classroom[nextRow][nextColumn]))
                nextBitstampCollectedLitter := getNextBitstampForCollectedLitter(&current, nextRow, nextColumn)

                if nextBitstampCollectedLitter == bitstampForAllLitter {
                    return stepsFromStart
                }

                if nextEnergy == 0 {
                    continue
                }

                if _, has := bestEnergyPerBitstampForCollectedLitter[nextRow][nextColumn][nextBitstampCollectedLitter]; has &&
                    bestEnergyPerBitstampForCollectedLitter[nextRow][nextColumn][nextBitstampCollectedLitter] >= nextEnergy {
                    continue
                }

                bestEnergyPerBitstampForCollectedLitter[nextRow][nextColumn][nextBitstampCollectedLitter] = nextEnergy
                queue = append(queue, NewPoint(nextRow, nextColumn, nextEnergy, nextBitstampCollectedLitter))
            }

            sizeCurrentRoundOfSteps--
        }
    }

    return NOT_POSSIBLE_TO_CLEAN_THE_CLASSROOM
}

func findStartPoint() Point {
    var start Point
    for row := range rows {
        for column := range columns {
            if classroom[row][column] == START {
                start = NewPoint(row, column, maxEnergy, 0)
                break
            }
        }
    }
    return start
}

func createBitstampForAllLitter() int {
    bitstampForAllLitter := 0
    for row := range rows {
        for column := range columns {
            bitstampForAllLitter |= bitstampForEachLitter[row][column]
        }
    }
    return bitstampForAllLitter
}

func createBitstampForEachLitter() [][]int {
    counterLitter := 0
    bitstamp := make([][]int, rows)

    for row := range rows {
        bitstamp[row] = make([]int, columns)

        for column := range columns {
            if classroom[row][column] == LITTER {
                bitstamp[row][column] = 1 << counterLitter
                counterLitter++
            }
        }
    }
    return bitstamp
}

func getNextBitstampForCollectedLitter(current *Point, nextRow int, nextColumn int) int {
    nextBitstampCollectedLitter := current.bitstampForCollectedLitter
    if classroom[nextRow][nextColumn] == LITTER {
        nextBitstampCollectedLitter |= bitstampForEachLitter[nextRow][nextColumn]
    }
    return nextBitstampCollectedLitter
}

func getNextEnergy(currentEnergy int, pointType rune) int {
    if pointType == CAN_RESTORE_ENERGY {
        return maxEnergy
    }
    return currentEnergy - 1
}

func isInClassroom(row int, column int) bool {
    return row >= 0 && row < rows && column >= 0 && column < columns
}
