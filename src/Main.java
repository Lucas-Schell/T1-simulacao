import java.util.LinkedList;

public class Main {

    private static final int a = 36789;
    private static final int c = 14168;
    private static final int m = 137921;
    private static double x;
    private static int randomCount = 0;

    public static void main(String[] args) {
        int servers = 1;
        int capacity = args.length > 1 ? -1 : 5;
        int[] arrival = {2, 4};
        int[] exit = {3, 5};
        x = 59541;

        double[] timeCount = new double[capacity == -1 ? 1000000 : capacity + 1];

        int loss = 0;
        int queueSize = 0;
        double time = 0;

        LinkedList<double[]> events = new LinkedList<>();
        events.add(new double[]{0, 2.0});

        while (randomCount <= 100000) {
            if (servers > 0 && queueSize > 0) {
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
            } else {
                if (queueSize > 0) {
                    timeCount[queueSize] += events.get(min)[1] - time;
                    queueSize--;
                    servers++;
                    time = events.get(min)[1];
                }
            }
            events.remove(min);

        }

        for (int i = 0; i < timeCount.length; i++) {
            System.out.println(i + " " + timeCount[i]);
        }
        System.out.println("\nLoss: " + loss);
        System.out.println("Total time: " + time);
    }

    public static double nextRandom(int A, int B) {
        x = (a * x + c) % m;
        return (B - A) * (x / m) + A;
    }
}
