
class Util {
    static NO_MOVES_ARE_NEEDED = 0;
    static NOT_POSSIBLE_TO_CLEAN_THE_CLASSROOM = -1;

    static EMPTY = '.';
    static START = 'S';
    static LITTER = 'L';
    static OBSTACLE = 'X';
    static CAN_RESTORE_ENERGY = 'R';

    static UP = [-1, 0];
    static DOWN = [1, 0];
    static LEFT = [0, -1];
    static RIGHT = [0, 1];

    static MOVES = [Util.UP, Util.DOWN, Util.LEFT, Util.RIGHT];

    /**
     * @param {string[]} classroom
     * @param {number} energy
     */
    constructor(classroom, energy) {
        this.rows = classroom.length;
        this.columns = classroom[0].length;
        this.classroom = classroom;
        this.maxEnergy = energy;
        this.bitstampForEachLitter = null;
        this.bitstampForAllLitter = null;
    }
}

let util;

/**
 * @param {string[]} classroom
 * @param {number} energy
 * @return {number}
 */
var minMoves = function (classroom, energy) {
    util = new Util(classroom, energy);
    util.bitstampForEachLitter = createBitstampForEachLitter();
    util.bitstampForAllLitter = createBitstampForAllLitter();
    const start = findStartPoint();

    return findMinMovesToCollectAllLitter(start);
};

/**
 * @param {number} row
 * @param {number} column
 * @param {number} energy
 * @param {number} bitstampForCollectedLitter
 */
function Point(row, column, energy, bitstampForCollectedLitter) {
    this.row = row;
    this.column = column;
    this.energy = energy;
    this.bitstampForCollectedLitter = bitstampForCollectedLitter;
}

/**
 * @param {Point} start
 * @return {number}
 */
function findMinMovesToCollectAllLitter(start) {
    if (util.bitstampForAllLitter === 0) {
        return Util.NO_MOVES_ARE_NEEDED;
    }

    // const {Queue} = require('@datastructures-js/queue');
    /*
     Queue is internally included in the solution file on leetcode.
     When running the code on leetcode it should stay commented out. 
     It is mentioned here just for information about the external library 
     that is applied for this data structure.
     */
    const queue = new Queue();
    queue.enqueue(start);

    const bestEnergyPerBitstampForCollectedLitter = createBestEnergyPerBitstampForCollectedLitter();
    bestEnergyPerBitstampForCollectedLitter[start.row][start.column].set(start.bitstampForCollectedLitter, start.energy);

    let stepsFromStart = 0;

    while (!queue.isEmpty()) {
        ++stepsFromStart;
        let sizeCurrentRoundOfSteps = queue.size();

        while (sizeCurrentRoundOfSteps > 0) {
            const current = queue.dequeue();

            for (let move of Util.MOVES) {
                const nextRow = current.row + move[0];
                const nextColumn = current.column + move[1];
                if (!isInClassroom(nextRow, nextColumn) || util.classroom[nextRow][nextColumn] === Util.OBSTACLE) {
                    continue;
                }

                const nextEnergy = getNextEnergy(current.energy, util.classroom[nextRow][nextColumn]);
                const nextBitstampCollectedLitter = getNextBitstampForCollectedLitter(current, nextRow, nextColumn);

                if (nextBitstampCollectedLitter === util.bitstampForAllLitter) {
                    return stepsFromStart;
                }

                if (nextEnergy === 0) {
                    continue;
                }
                if (bestEnergyPerBitstampForCollectedLitter[nextRow][nextColumn].has(nextBitstampCollectedLitter)
                        && bestEnergyPerBitstampForCollectedLitter[nextRow][nextColumn].get(nextBitstampCollectedLitter) >= nextEnergy) {
                    continue;
                }

                bestEnergyPerBitstampForCollectedLitter[nextRow][nextColumn].set(nextBitstampCollectedLitter, nextEnergy);
                queue.enqueue(new Point(nextRow, nextColumn, nextEnergy, nextBitstampCollectedLitter));
            }

            --sizeCurrentRoundOfSteps;
        }
    }

    return Util.NOT_POSSIBLE_TO_CLEAN_THE_CLASSROOM;
}

/**
 * @return {Map<number, number>[][]}
 */
function createBestEnergyPerBitstampForCollectedLitter() {
    const bestEnergyPerBitstampForCollectedLitter = Array.from(new Array(util.rows), () => new Array(util.columns));
    for (let row = 0; row < util.rows; ++row) {
        for (let column = 0; column < util.columns; ++column) {
            bestEnergyPerBitstampForCollectedLitter[row][column] = new Map();
        }
    }
    return bestEnergyPerBitstampForCollectedLitter;
}

/**
 * @return {Point}
 */
function findStartPoint() {
    let start = null;
    for (let row = 0; row < util.rows; ++row) {
        for (let column = 0; column < util.columns; ++column) {
            if (util.classroom[row][column] === Util.START) {
                start = new Point(row, column, util.maxEnergy, 0);
                break;
            }
        }
    }
    return start;
}

/**
 * @return {number}
 */
function createBitstampForAllLitter() {
    let bitstampForAllLitter = 0;

    for (let row = 0; row < util.rows; ++row) {
        for (let column = 0; column < util.columns; ++column) {
            bitstampForAllLitter |= util.bitstampForEachLitter[row][column];

        }
    }
    return bitstampForAllLitter;
}

/**
 * @return {number[][]}
 */
function createBitstampForEachLitter() {
    let counterLitter = 0;
    const bitstamp = Array.from(new Array(util.rows), () => new Array(util.columns));

    for (let row = 0; row < util.rows; ++row) {
        for (let column = 0; column < util.columns; ++column) {
            if (util.classroom[row][column] === Util.LITTER) {
                bitstamp[row][column] = 1 << counterLitter;
                ++counterLitter;

            }
        }
    }
    return bitstamp;
}

/**
 * @param {Point} current
 * @param {number} nextRow
 * @param {number} nextColumn
 * @return {number}
 */
function getNextBitstampForCollectedLitter(current, nextRow, nextColumn) {
    let nextBitstampCollectedLitter = current.bitstampForCollectedLitter;
    if (util.classroom[nextRow][nextColumn] === Util.LITTER) {
        nextBitstampCollectedLitter |= util.bitstampForEachLitter[nextRow][nextColumn];
    }
    return nextBitstampCollectedLitter;
}

/**
 * @param {number} currentEnergy
 * @param {string} pointType
 * @return {number}
 */
function getNextEnergy(currentEnergy, pointType) {
    if (pointType === Util.CAN_RESTORE_ENERGY) {
        return util.maxEnergy;
    }
    return currentEnergy - 1;
}

/**
 * @param {number} row
 * @param {number} column
 * @return {boolean}
 */
function isInClassroom(row, column) {
    return row >= 0 && row < util.rows && column >= 0 && column < util.columns;
}
