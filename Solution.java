
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Solution {

    private record Point(int row, int column, int energy, int bitstampForCollectedLitter) {}

    private static final int NO_MOVES_ARE_NEEDED = 0;
    private static final int NOT_POSSIBLE_TO_CLEAN_THE_CLASSROOM = -1;

    private static final char EMPTY = '.';
    private static final char START = 'S';
    private static final char LITTER = 'L';
    private static final char OBSTACLE = 'X';
    private static final char CAN_RESTORE_ENERGY = 'R';

    private static final int[] UP = {-1, 0};
    private static final int[] DOWN = {1, 0};
    private static final int[] LEFT = {0, -1};
    private static final int[] RIGHT = {0, 1};

    private static final int[][] MOVES = {UP, DOWN, LEFT, RIGHT};

    private int rows;
    private int columns;
    private int maxEnergy;
    private int bitstampForAllLitter;

    private char[][] classroom;
    private int[][] bitstampForEachLitter;

    public int minMoves(String[] classroomInitialFormat, int energy) {
        rows = classroomInitialFormat.length;
        columns = classroomInitialFormat[0].length();
        maxEnergy = energy;

        classroom = createClassroomAsCharMatrix(classroomInitialFormat);
        bitstampForEachLitter = createBitstampForEachLitter();
        bitstampForAllLitter = createBitstampForAllLitter();
        Point start = findStartPoint();

        return findMinMovesToCollectAllLitter(start);
    }

    private int findMinMovesToCollectAllLitter(Point start) {
        if (bitstampForAllLitter == 0) {
            return NO_MOVES_ARE_NEEDED;
        }

        Queue<Point> queue = new LinkedList<>();
        queue.add(start);

        Map<Integer, Integer>[][] bestEnergyPerBitstampForCollectedLitter = createBestEnergyPerBitstampForCollectedLitter();
        bestEnergyPerBitstampForCollectedLitter[start.row][start.column].put(start.bitstampForCollectedLitter, start.energy);

        int stepsFromStart = 0;

        while (!queue.isEmpty()) {
            ++stepsFromStart;
            int sizeCurrentRoundOfSteps = queue.size();

            while (sizeCurrentRoundOfSteps > 0) {
                Point current = queue.poll();

                for (int[] move : MOVES) {
                    int nextRow = current.row + move[0];
                    int nextColumn = current.column + move[1];
                    if (!isInClassroom(nextRow, nextColumn) || classroom[nextRow][nextColumn] == OBSTACLE) {
                        continue;
                    }

                    int nextEnergy = getNextEnergy(current.energy, classroom[nextRow][nextColumn]);
                    int nextBitstampCollectedLitter = getNextBitstampForCollectedLitter(current, nextRow, nextColumn);

                    if (nextBitstampCollectedLitter == bitstampForAllLitter) {
                        return stepsFromStart;
                    }

                    if (nextEnergy == 0) {
                        continue;
                    }
                    if (bestEnergyPerBitstampForCollectedLitter[nextRow][nextColumn].containsKey(nextBitstampCollectedLitter)
                            && bestEnergyPerBitstampForCollectedLitter[nextRow][nextColumn]
                                    .get(nextBitstampCollectedLitter) >= nextEnergy) {
                        continue;
                    }

                    bestEnergyPerBitstampForCollectedLitter[nextRow][nextColumn].put(nextBitstampCollectedLitter, nextEnergy);
                    queue.add(new Point(nextRow, nextColumn, nextEnergy, nextBitstampCollectedLitter));
                }

                --sizeCurrentRoundOfSteps;
            }
        }

        return NOT_POSSIBLE_TO_CLEAN_THE_CLASSROOM;
    }

    private Map<Integer, Integer>[][] createBestEnergyPerBitstampForCollectedLitter() {
        Map<Integer, Integer>[][] bestEnergyPerBitstampForCollectedLitter = new HashMap[rows][columns];
        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < columns; ++column) {
                bestEnergyPerBitstampForCollectedLitter[row][column] = new HashMap<>();
            }
        }
        return bestEnergyPerBitstampForCollectedLitter;
    }

    private char[][] createClassroomAsCharMatrix(String[] classroomInitialFormat) {
        char[][] classroom = new char[rows][columns];
        for (int row = 0; row < rows; ++row) {
            classroom[row] = classroomInitialFormat[row].toCharArray();
        }
        return classroom;
    }

    private Point findStartPoint() {
        Point start = null;
        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < columns; ++column) {
                if (classroom[row][column] == START) {
                    start = new Point(row, column, maxEnergy, 0);
                    break;
                }
            }
        }
        return start;
    }

    private int createBitstampForAllLitter() {
        int bitstampForAllLitter = 0;
        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < columns; ++column) {
                bitstampForAllLitter |= bitstampForEachLitter[row][column];
            }
        }
        return bitstampForAllLitter;
    }

    private int[][] createBitstampForEachLitter() {
        int counterLitter = 0;
        int[][] bitstamp = new int[rows][columns];

        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < columns; ++column) {
                if (classroom[row][column] == LITTER) {
                    bitstamp[row][column] = 1 << counterLitter;
                    ++counterLitter;
                }
            }
        }
        return bitstamp;
    }

    private int getNextBitstampForCollectedLitter(Point current, int nextRow, int nextColumn) {
        int nextBitstampCollectedLitter = current.bitstampForCollectedLitter;
        if (classroom[nextRow][nextColumn] == LITTER) {
            nextBitstampCollectedLitter |= bitstampForEachLitter[nextRow][nextColumn];
        }
        return nextBitstampCollectedLitter;
    }

    private int getNextEnergy(int currentEnergy, char pointType) {
        if (pointType == CAN_RESTORE_ENERGY) {
            return maxEnergy;
        }
        return currentEnergy - 1;
    }

    private boolean isInClassroom(int row, int column) {
        return row >= 0 && row < rows && column >= 0 && column < columns;
    }
}
