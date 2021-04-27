//Lucas Schell e Max Franke

import java.util.LinkedList;

public class Main {

    private static final int a = 36789;
    private static final int c = 14168;
    private static final int m = 137921;
    private static double x;

    public static void main(String[] args) {
        int servers = 2;
        int capacity = 3;
        sim(servers, capacity, new int[]{2, 3}, new int[]{3, 5}, 2, 3, 1, new int[]{2, 5});
    }

    public static void sim(int server, int capacity, int[] arrival, int[] exit, int queueCount, int capacity2, int server2, int[] move) {
        int[] rowSize = new int[]{capacity + 1, capacity2 + 1}; //todo
        int[] servers = new int[]{server, server2}; //todo
        double[][] result = new double[queueCount][rowSize[0] + 2]; //todo
        double[] seeds = {1345, 5423, 7863, 4423, 10587};
        for (int a = 0; a < 5; a++) {
            x = seeds[a];
            double[][] timeCount = new double[queueCount][rowSize[0]]; //todo
            int[] loss = new int[queueCount];
            int[] queueSize = new int[queueCount];
            int randomCount = 0;
            double time = 0;
            LinkedList<Object[]> events = new LinkedList<>();
            events.add(new Object[]{"A-0", 2.5});

            while (randomCount <= 100000) {
                int min = 0;
                for (int i = 0; i < events.size(); i++) {
                    if ((double) events.get(i)[1] < (double) events.get(min)[1]) {
                        min = i;
                    }
                }

                String[] event = events.get(min)[0].toString().split("-");
                switch (event[0]) {
                    case "A":
                        for (int i = 0; i < timeCount.length; i++) {
                            timeCount[i][queueSize[i]] += (double) events.get(min)[1] - time;
                        }
                        int arrivalQueue = Integer.parseInt(event[1]);
                        time = (double) events.get(min)[1];
                        if (queueSize[arrivalQueue] >= rowSize[arrivalQueue] - 1) {
                            loss[arrivalQueue]++;
                        } else {
                            queueSize[arrivalQueue]++;
                            if (queueSize[arrivalQueue] <= servers[arrivalQueue]) {
                                events.add(new Object[]{"M-" + arrivalQueue + "-" + (arrivalQueue + 1), nextRandom(move[0], move[1]) + time});
                                randomCount++;
                            }
                        }

                        events.add(new Object[]{"A-" + arrivalQueue, nextRandom(arrival[0], arrival[1]) + time});
                        randomCount++;

                        events.remove(min);
                        break;
                    case "E":
                        for (int i = 0; i < timeCount.length; i++) {
                            timeCount[i][queueSize[i]] += (double) events.get(min)[1] - time;
                        }
                        int exitQueue = Integer.parseInt(event[1]);
                        queueSize[exitQueue]--;
                        time = (double) events.get(min)[1];

                        if (queueSize[exitQueue] >= servers[exitQueue]) {
                            events.add(new Object[]{"E-" + exitQueue, nextRandom(exit[0], exit[1]) + time});
                            randomCount++;
                        }
                        events.remove(min);
                        break;
                    case "M":
                        for (int i = 0; i < timeCount.length; i++) {
                            timeCount[i][queueSize[i]] += (double) events.get(min)[1] - time;
                        }
                        int out = Integer.parseInt(event[1]);
                        int in = Integer.parseInt(event[2]);
                        time = (double) events.get(min)[1];

                        queueSize[out]--;
                        if (queueSize[out] >= servers[out]) {
                            events.add(new Object[]{"M-" + out + "-" + in, nextRandom(move[0], move[1]) + time});
                            randomCount++;
                        }
                        if (queueSize[in] >= rowSize[in] - 1) {
                            loss[in]++;
                        } else {
                            queueSize[in]++;
                            if (queueSize[in] <= servers[in]) {
                                events.add(new Object[]{"E-" + in, nextRandom(exit[0], exit[1]) + time});
                                randomCount++;
                            }
                        }
                        events.remove(min);
                        break;
                    default:
                        break;
                }
            }

            for (int i = 0; i < timeCount.length; i++) {
                for (int j = 0; j < timeCount[0].length; j++) {
                    result[i][j] += timeCount[i][j];
                }
                result[i][result[i].length - 2] += time;
                result[i][result[i].length - 1] += loss[i];
            }
        }

        for (int i = 0; i < rowSize.length; i++) {
            printRes(result[i], rowSize[i]);
        }
    }

    public static void printRes(double[] result, int rowSize) {
        for (int i = 0; i < rowSize; i++) {
            result[i] = result[i] / 5;
        }
        result[result.length - 2] = result[result.length - 2] / 5;
        result[result.length - 1] = result[result.length - 1] / 5;

        for (int i = 0; i < rowSize; i++) {
            System.out.printf("%d\t%.4f\t%.2f%%\n", i, result[i], ((result[i] * 100) / result[result.length - 2]));
        }
        System.out.println("\nLoss: " + result[result.length - 1]);
        System.out.println("Total time: " + result[result.length - 2]);
    }

    public static double nextRandom(int A, int B) {
        x = (a * x + c) % m;
        return (B - A) * (x / m) + A;
    }
}
