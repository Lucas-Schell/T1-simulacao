//Lucas Schell e Max Franke

import java.util.LinkedList;

public class Main {

    private static final int a = 36789;
    private static final int c = 14168;
    private static final int m = 137921;
    private static double x;

    public static void main(String[] args) {
        int[][] queues = new int[2][];
        queues[0] = new int[]{2, 3, 2, 3, 2, 5}; //servers, capacity, arrivalMin, arrivalMax, exitMin, ExitMax
        queues[1] = new int[]{1, 3, -1, -1, 3, 5};
        sim(queues);
    }

    public static void sim(int[][] queues) {
        int[] rowSize = new int[queues.length];
        int[] servers = new int[queues.length];
        for (int i = 0; i < queues.length; i++) {
            rowSize[i] = queues[i][1] + 1;
            servers[i] = queues[i][0];
        }
        double[][] result = new double[queues.length][rowSize[0] + 2];
        double[] seeds = {1345, 5423, 7863, 4423, 10587};
        for (int a = 0; a < 5; a++) {
            x = seeds[a];
            double[][] timeCount = new double[queues.length][rowSize[0]];
            int[] loss = new int[queues.length];
            int[] queueSize = new int[queues.length];
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
                                events.add(new Object[]{"M-" + arrivalQueue + "-" + (arrivalQueue + 1),
                                        nextRandom(queues[arrivalQueue][4], queues[arrivalQueue][5]) + time});
                                randomCount++;
                            }
                        }

                        events.add(new Object[]{"A-" + arrivalQueue, nextRandom(queues[arrivalQueue][2], queues[arrivalQueue][3]) + time});
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
                            events.add(new Object[]{"E-" + exitQueue, nextRandom(queues[exitQueue][4], queues[exitQueue][5]) + time});
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
                            events.add(new Object[]{"M-" + out + "-" + in, nextRandom(queues[out][4], queues[out][5]) + time});
                            randomCount++;
                        }
                        if (queueSize[in] >= rowSize[in] - 1) {
                            loss[in]++;
                        } else {
                            queueSize[in]++;
                            if (queueSize[in] <= servers[in]) {
                                events.add(new Object[]{"E-" + in, nextRandom(queues[in][4], queues[in][5]) + time});
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
