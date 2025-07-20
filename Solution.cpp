
#include <array>
#include <queue>
#include <string>
#include <vector>
#include <unordered_map>
using namespace std;

class Solution {

    struct Point {
        int row;
        int column;
        int energy;
        int bitstampForCollectedLitter;

        Point() = default;

        Point(int row, int column, int energy, int bitstampForCollectedLitter) :
              row{ row }, column{ column }, energy{ energy }, bitstampForCollectedLitter{ bitstampForCollectedLitter }{}
    };

    static const int NO_MOVES_ARE_NEEDED = 0;
    static const int NOT_POSSIBLE_TO_CLEAN_THE_CLASSROOM = -1;

    static const char EMPTY = '.';
    static const char START = 'S';
    static const char LITTER = 'L';
    static const char OBSTACLE = 'X';
    static const char CAN_RESTORE_ENERGY = 'R';

    static constexpr array<int, 2> UP = { -1, 0 };
    static constexpr array<int, 2> DOWN = { 1, 0 };
    static constexpr array<int, 2> LEFT = { 0, -1 };
    static constexpr array<int, 2> RIGHT = { 0, 1 };

    static constexpr array<array<int, 2>, 4> MOVES = { UP, DOWN, LEFT, RIGHT };

    int rows;
    int columns;
    int maxEnergy;
    int bitstampForAllLitter;

    unique_ptr<vector<string>> classroom;
    vector<vector<int>> bitstampForEachLitter;

public:
    int minMoves(vector<string>& classroom, int energy) {
        rows = classroom.size();
        columns = classroom[0].size();
        maxEnergy = energy;

        this->classroom = make_unique<vector<string>>(classroom);
        bitstampForEachLitter = createBitstampForEachLitter();
        bitstampForAllLitter = createBitstampForAllLitter();
        Point start = findStartPoint();

        return findMinMovesToCollectAllLitter(start);
    }

private:
    int findMinMovesToCollectAllLitter(Point start) {
        if (bitstampForAllLitter == 0) {
            return NO_MOVES_ARE_NEEDED;
        }

        queue<Point> queue;
        queue.push(start);

        vector<vector<unordered_map<int, int>>> bestEnergyPerBitstampForCollectedLitter = createBestEnergyPerBitstampForCollectedLitter();
        bestEnergyPerBitstampForCollectedLitter[start.row][start.column][start.bitstampForCollectedLitter] = start.energy;

        int stepsFromStart = 0;

        while (!queue.empty()) {
            ++stepsFromStart;
            int sizeCurrentRoundOfSteps = queue.size();

            while (sizeCurrentRoundOfSteps > 0) {
                Point current = queue.front();
                queue.pop();

                for (const auto& move : MOVES) {
                    int nextRow = current.row + move[0];
                    int nextColumn = current.column + move[1];
                    if (!isInClassroom(nextRow, nextColumn) || (*classroom)[nextRow][nextColumn] == OBSTACLE) {
                        continue;
                    }

                    int nextEnergy = getNextEnergy(current.energy, (*classroom)[nextRow][nextColumn]);
                    int nextBitstampCollectedLitter = getNextBitstampForCollectedLitter(current, nextRow, nextColumn);

                    if (nextBitstampCollectedLitter == bitstampForAllLitter) {
                        return stepsFromStart;
                    }

                    if (nextEnergy == 0) {
                        continue;
                    }
                    if (bestEnergyPerBitstampForCollectedLitter[nextRow][nextColumn].contains(nextBitstampCollectedLitter)
                        && bestEnergyPerBitstampForCollectedLitter[nextRow][nextColumn][nextBitstampCollectedLitter] >= nextEnergy) {
                        continue;
                    }

                    bestEnergyPerBitstampForCollectedLitter[nextRow][nextColumn][nextBitstampCollectedLitter] = nextEnergy;
                    queue.emplace(nextRow, nextColumn, nextEnergy, nextBitstampCollectedLitter);
                }

                --sizeCurrentRoundOfSteps;
            }
        }

        return NOT_POSSIBLE_TO_CLEAN_THE_CLASSROOM;
    }

    vector<vector<unordered_map<int, int>>> createBestEnergyPerBitstampForCollectedLitter() {
        vector<vector<unordered_map<int, int>>> bestEnergyPerBitstampForCollectedLitter(rows, vector<unordered_map<int, int>>(columns));
        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < columns; ++column) {
                bestEnergyPerBitstampForCollectedLitter[row][column] = unordered_map<int, int>();
            }
        }
        return bestEnergyPerBitstampForCollectedLitter;
    }

    Point findStartPoint() const {
        Point start;
        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < columns; ++column) {
                if ((*classroom)[row][column] == START) {
                    start = Point(row, column, maxEnergy, 0);
                    break;
                }
            }
        }
        return start;
    }

    int createBitstampForAllLitter() const {
        int bitstampForAllLitter = 0;
        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < columns; ++column) {
                bitstampForAllLitter |= bitstampForEachLitter[row][column];
            }
        }
        return bitstampForAllLitter;
    }

    vector<vector<int>> createBitstampForEachLitter() const {
        int counterLitter = 0;
        vector<vector<int>> bitstamp(rows, vector<int>(columns));

        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < columns; ++column) {
                if ((*classroom)[row][column] == LITTER) {
                    bitstamp[row][column] = 1 << counterLitter;
                    ++counterLitter;
                }
            }
        }
        return bitstamp;
    }

    int getNextBitstampForCollectedLitter(const Point& current, int nextRow, int nextColumn) const {
        int nextBitstampCollectedLitter = current.bitstampForCollectedLitter;
        if ((*classroom)[nextRow][nextColumn] == LITTER) {
            nextBitstampCollectedLitter |= bitstampForEachLitter[nextRow][nextColumn];
        }
        return nextBitstampCollectedLitter;
    }

    int getNextEnergy(int currentEnergy, char pointType) const {
        if (pointType == CAN_RESTORE_ENERGY) {
            return maxEnergy;
        }
        return currentEnergy - 1;
    }

    bool isInClassroom(int row, int column) const {
        return row >= 0 && row < rows && column >= 0 && column < columns;
    }
};
