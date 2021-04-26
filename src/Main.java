//Lucas Schell e Max Franke

import java.util.LinkedList;

public class Main {

    private static final int a = 36789;
    private static final int c = 14168;
    private static final int m = 137921;
    private static double x;

    public static void main(String[] args) {
        int servers = 2;
        int capacity = 5;
        sim(servers, capacity, new int[]{2, 4}, new int[]{3, 5});
    }

    public static void sim(int servers, int capacity, int[] arrival, int[] exit) {
        int rowSize = capacity == -1 ? 1000000 : capacity + 1;

        double[] result = new double[rowSize + 2];
        double[] seeds = {1345, 5423, 7863, 4423, 10587};
        for (int a = 0; a < 5; a++) {
            x = seeds[a];
            double[] timeCount = new double[rowSize];
            int loss = 0;
            int queueSize = 0;
            int randomCount = 0;
            double time = 0;
            LinkedList<Object[]> events = new LinkedList<>();
            events.add(new Object[]{"A0", 3.0});

            while (randomCount <= 100000) {
                int min = 0;
                for (int i = 0; i < events.size(); i++) {
                    if ((double) events.get(i)[1] < (double) events.get(min)[1]) {
                        min = i;
                    }
                }

                switch (events.get(min)[0].toString().charAt(0)) {
                    case 'A':
                        timeCount[queueSize] += (double) events.get(min)[1] - time;
                        time = (double) events.get(min)[1];
                        if (queueSize >= capacity) {
                            loss++;
                        } else {
                            queueSize++;
                        }
                        events.remove(min);

                        events.add(new Object[]{"A0", nextRandom(arrival[0], arrival[1]) + time});
                        randomCount++;

                        if (queueSize <= servers) {
                            events.add(new Object[]{"E0", nextRandom(exit[0], exit[1]) + time});
                            randomCount++;
                        }
                        break;
                    case 'E':
                        timeCount[queueSize] += (double) events.get(min)[1] - time;
                        queueSize--;
                        time = (double) events.get(min)[1];
                        events.remove(min);

                        if (queueSize >= servers) {
                            events.add(new Object[]{"E0", nextRandom(exit[0], exit[1]) + time});
                            randomCount++;
                        }
                        break;
                    case 'M':
                        System.out.println('M');
                        break;
                    default:
                        break;
                }
            }

            for (int i = 0; i < timeCount.length; i++) {
                result[i] += timeCount[i];
            }
            result[result.length - 2] += time;
            result[result.length - 1] += loss;
        }

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
