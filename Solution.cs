
using System;
using System.Collections.Generic;

public class Solution
{
    private record Point(int row, int column, int energy, int bitstampForCollectedLitter){}

    private static readonly int NO_MOVES_ARE_NEEDED = 0;
    private static readonly int NOT_POSSIBLE_TO_CLEAN_THE_CLASSROOM = -1;

    private static readonly char EMPTY = '.';
    private static readonly char START = 'S';
    private static readonly char LITTER = 'L';
    private static readonly char OBSTACLE = 'X';
    private static readonly char CAN_RESTORE_ENERGY = 'R';

    private static readonly int[] UP = { -1, 0 };
    private static readonly int[] DOWN = { 1, 0 };
    private static readonly int[] LEFT = { 0, -1 };
    private static readonly int[] RIGHT = { 0, 1 };

    private static readonly int[][] MOVES = { UP, DOWN, LEFT, RIGHT };

    private int rows;
    private int columns;
    private int maxEnergy;
    private int bitstampForAllLitter;

    private string[] classroom;
    private int[][] bitstampForEachLitter;

    public int MinMoves(string[] classroom, int energy)
    {
        rows = classroom.Length;
        columns = classroom[0].Length;
        maxEnergy = energy;

        this.classroom = classroom;
        bitstampForEachLitter = CreateBitstampForEachLitter();
        bitstampForAllLitter = CreateBitstampForAllLitter();
        Point start = FindStartPoint();

        return FindMinMovesToCollectAllLitter(start);
    }

    private int FindMinMovesToCollectAllLitter(Point start)
    {
        if (bitstampForAllLitter == 0)
        {
            return NO_MOVES_ARE_NEEDED;
        }

        Queue<Point> queue = new Queue<Point>();
        queue.Enqueue(start);

        Dictionary<int, int>[][] bestEnergyPerBitstampForCollectedLitter = CreateBestEnergyPerBitstampForCollectedLitter();
        bestEnergyPerBitstampForCollectedLitter[start.row][start.column].Add(start.bitstampForCollectedLitter, start.energy);

        int stepsFromStart = 0;

        while (queue.Count > 0)
        {
            ++stepsFromStart;
            int sizeCurrentRoundOfSteps = queue.Count;

            while (sizeCurrentRoundOfSteps > 0)
            {
                Point current = queue.Dequeue();

                foreach (int[] move in MOVES)
                {
                    int nextRow = current.row + move[0];
                    int nextColumn = current.column + move[1];
                    if (!IsInClassroom(nextRow, nextColumn) || classroom[nextRow][nextColumn] == OBSTACLE)
                    {
                        continue;
                    }

                    int nextEnergy = GetNextEnergy(current.energy, classroom[nextRow][nextColumn]);
                    int nextBitstampCollectedLitter = GetNextBitstampForCollectedLitter(current, nextRow, nextColumn);

                    if (nextBitstampCollectedLitter == bitstampForAllLitter)
                    {
                        return stepsFromStart;
                    }

                    if (nextEnergy == 0)
                    {
                        continue;
                    }
                    if (bestEnergyPerBitstampForCollectedLitter[nextRow][nextColumn].ContainsKey(nextBitstampCollectedLitter)
                            && bestEnergyPerBitstampForCollectedLitter[nextRow][nextColumn][nextBitstampCollectedLitter] >= nextEnergy)
                    {
                        continue;
                    }

                    bestEnergyPerBitstampForCollectedLitter[nextRow][nextColumn].TryAdd(nextBitstampCollectedLitter, nextEnergy);
                    bestEnergyPerBitstampForCollectedLitter[nextRow][nextColumn][nextBitstampCollectedLitter] = nextEnergy;
                    queue.Enqueue(new Point(nextRow, nextColumn, nextEnergy, nextBitstampCollectedLitter));
                }

                --sizeCurrentRoundOfSteps;
            }
        }

        return NOT_POSSIBLE_TO_CLEAN_THE_CLASSROOM;
    }

    private Dictionary<int, int>[][] CreateBestEnergyPerBitstampForCollectedLitter()
    {
        Dictionary<int, int>[][] bestEnergyPerBitstampForCollectedLitter = new Dictionary<int, int>[rows][];
        for (int row = 0; row < rows; ++row)
        {
            bestEnergyPerBitstampForCollectedLitter[row] = new Dictionary<int, int>[columns];
            for (int column = 0; column < columns; ++column)
            {
                bestEnergyPerBitstampForCollectedLitter[row][column] = new Dictionary<int, int>();
            }
        }
        return bestEnergyPerBitstampForCollectedLitter;
    }

    private Point FindStartPoint()
    {
        Point start = null;
        for (int row = 0; row < rows; ++row)
        {
            for (int column = 0; column < columns; ++column)
            {
                if (classroom[row][column] == START)
                {
                    start = new Point(row, column, maxEnergy, 0);
                    break;
                }
            }
        }
        return start;
    }

    private int CreateBitstampForAllLitter()
    {
        int bitstampForAllLitter = 0;
        for (int row = 0; row < rows; ++row)
        {
            for (int column = 0; column < columns; ++column)
            {
                bitstampForAllLitter |= bitstampForEachLitter[row][column];
            }
        }
        return bitstampForAllLitter;
    }

    private int[][] CreateBitstampForEachLitter()
    {
        int counterLitter = 0;
        int[][] bitstamp = new int[rows][];

        for (int row = 0; row < rows; ++row)
        {
            bitstamp[row] = new int[columns];
            for (int column = 0; column < columns; ++column)
            {
                if (classroom[row][column] == LITTER)
                {
                    bitstamp[row][column] = 1 << counterLitter;
                    ++counterLitter;
                }
            }
        }
        return bitstamp;
    }

    private int GetNextBitstampForCollectedLitter(Point current, int nextRow, int nextColumn)
    {
        int nextBitstampCollectedLitter = current.bitstampForCollectedLitter;
        if (classroom[nextRow][nextColumn] == LITTER)
        {
            nextBitstampCollectedLitter |= bitstampForEachLitter[nextRow][nextColumn];
        }
        return nextBitstampCollectedLitter;
    }

    private int GetNextEnergy(int currentEnergy, char pointType)
    {
        if (pointType == CAN_RESTORE_ENERGY)
        {
            return maxEnergy;
        }
        return currentEnergy - 1;
    }

    private bool IsInClassroom(int row, int column)
    {
        return row >= 0 && row < rows && column >= 0 && column < columns;
    }
}
