import java.util.Arrays;
import java.util.LinkedList;

public class Main {

    private static final int a = 36789;
    private static final int c = 14168;
    private static final int m = 137921;
    private static double x;

    public static void main(String[] args) {
        sim(1, 5, new int[]{2, 4}, new int[]{3, 5});
    }

    public static void sim(int servers, int capacity, int[] arrival, int[] exit) {
        int totalServers = servers;
        int rowSize = capacity == -1 ? 1000000 : capacity + 1;

        double[] result = new double[rowSize + 2];
        double[] seeds = {1, 2, 3, 4, 5};
        for (int a = 0; a < 5; a++) {
            x = seeds[a];
            double[] timeCount = new double[rowSize];
            int loss = 0;
            int queueSize = 0;
            int randomCount = 0;
            double time = 0;
            servers = totalServers;
            LinkedList<double[]> events = new LinkedList<>();
            events.add(new double[]{0, 3.0});
            System.out.println(Arrays.toString(events.get(0)));

            while (randomCount <= 100000) {
                if (servers > 0 && queueSize > totalServers - servers) {
                    double aux = nextRandom(exit[0], exit[1]);
                    randomCount++;
                    servers--;
                    events.add(new double[]{1, aux + time});
                }

                boolean arriving = false;
                for (double[] event : events) {
                    if (event[0] == 0) {
                        arriving = true;
                        break;
                    }
                }

                if (!arriving) {
                    double aux = nextRandom(arrival[0], arrival[1]);
                    randomCount++;
                    events.add(new double[]{0, aux + time});
                }

                int min = 0;
                for (int i = 0; i < events.size(); i++) {
                    if (events.get(i)[1] < events.get(min)[1]) {
                        min = i;
                    }
                }

                if (events.get(min)[0] == 0.0) {
                    timeCount[queueSize] += events.get(min)[1] - time;
                    time = events.get(min)[1];
                    if (queueSize >= capacity) {
                        loss++;
                    } else {
                        queueSize++;
                    }
                    events.remove(min);
                } else {
                    if (queueSize > 0) {
                        timeCount[queueSize] += events.get(min)[1] - time;
                        queueSize--;
                        servers++;
                        time = events.get(min)[1];
                        events.remove(min);
                    }
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
