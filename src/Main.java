//Lucas Schell e Max Franke

import java.util.Comparator;
import java.util.PriorityQueue;

public class Main {

    private static final int a = 36789;
    private static final int c = 14168;
    private static final int m = 137921;
    private static double x;
    private static int randomCount, maxRand;

    public static void main(String[] args) {
        Queue q0 = new Queue("F1", 1, -1, 1, 4, 1, 1.5, new double[][]{{0.8, 0.2}, {1, 2}});
        Queue q1 = new Queue("F2", 3, 5, 0, 0, 5, 10, new double[][]{{0.3, 0.5}, {0, 2}});
        Queue q2 = new Queue("F3", 2, 8, 0, 0, 10, 20, new double[][]{{0.7}, {1}});
        Queue[] queues = {q0, q1, q2};
        sim(queues);
    }

    public static void sim(Queue[] queues) {
        double[] seeds = {1345, 5423, 7863, 4423, 10587};
        maxRand = 100000;

        for (int a = 0; a < 1; a++) {
            x = seeds[a];
            randomCount = 0;
            double time = 0;

            Comparator<Object[]> comparator = (s1, s2) -> {
                double aux = (double) s1[1] - (double) s2[1];
                if (aux < 0) return -1;
                if (aux > 0) return 1;
                return 0;
            };
            PriorityQueue<Object[]> events = new PriorityQueue<>(comparator);
            events.add(new Object[]{"A-0", 1.0});

            simulation:
            while (randomCount < maxRand) {
                Object[] event = events.poll();

                assert event != null;
                String[] eventType = event[0].toString().split("-");
                double eventTime = (double) event[1];

                switch (eventType[0]) {
                    case "A":
                        for (Queue q : queues) {
                            q.addTime(eventTime - time);
                        }
                        time = eventTime;

                        int arrivalPos = Integer.parseInt(eventType[1]);
                        Queue arrivalQueue = queues[arrivalPos];

                        if (arrivalQueue.getSize() < arrivalQueue.getCapacity()) {
                            arrivalQueue.addSize(1);
                            if (arrivalQueue.getSize() <= arrivalQueue.getServers()) {
                                try {
                                    events.add(generateExitEvent(arrivalQueue, arrivalPos, time));
                                } catch (Exception e) {
                                    break simulation;
                                }
                                randomCount++;
                                if (randomCount == maxRand) break simulation;
                            }
                        } else {
                            arrivalQueue.addLoss();
                        }
                        double[] arrival = arrivalQueue.getArrival();
                        events.add(new Object[]{"A-" + arrivalPos, nextRandom(arrival[0], arrival[1]) + time});
                        randomCount++;
                        break;
                    case "E":
                        for (Queue q : queues) {
                            q.addTime(eventTime - time);
                        }
                        time = eventTime;

                        int exitPos = Integer.parseInt(eventType[1]);
                        Queue exitQueue = queues[exitPos];

                        exitQueue.addSize(-1);

                        if (exitQueue.getSize() >= exitQueue.getServers()) {
                            try {
                                events.add(generateExitEvent(exitQueue, exitPos, time));
                            } catch (Exception e) {
                                break simulation;
                            }
                            randomCount++;
                        }
                        break;
                    case "M":
                        for (Queue q : queues) {
                            q.addTime(eventTime - time);
                        }
                        time = eventTime;

                        int outPos = Integer.parseInt(eventType[1]);
                        Queue outQueue = queues[outPos];
                        int inPos = Integer.parseInt(eventType[2]);
                        Queue inQueue = queues[inPos];

                        outQueue.addSize(-1);

                        if (outQueue.getSize() >= outQueue.getServers()) {
                            try {
                                events.add(generateExitEvent(outQueue, outPos, time));
                            } catch (Exception e) {
                                break simulation;
                            }
                            randomCount++;
                            if (randomCount == maxRand) break simulation;
                        }

                        if (inQueue.getSize() < inQueue.getCapacity()) {
                            inQueue.addSize(1);
                            if (inQueue.getSize() <= inQueue.getServers()) {
                                try {
                                    events.add(generateExitEvent(inQueue, inPos, time));
                                } catch (Exception e) {
                                    break simulation;
                                }
                                randomCount++;
                            }
                        } else {
                            inQueue.addLoss();
                        }
                        break;
                    default:
                        break;
                }
            }

            for (Queue q : queues) {
                q.print();
            }
        }

    }

    public static Object[] generateExitEvent(Queue queue, int queuePos, double time) throws Exception {
        int dest = queue.firstRoute();
        if (queue.hasRoutes()) {
            dest = queue.exit(nextRandom(0, 1));
            randomCount++;
            if (randomCount == maxRand) throw new Exception();
        }
        double[] exit = queue.getExit();
        if (dest == -1) {
            return new Object[]{"E-" + queuePos, nextRandom(exit[0], exit[1]) + time};
        } else {
            return new Object[]{"M-" + queuePos + "-" + dest, nextRandom(exit[0], exit[1]) + time};
        }
    }

    //static double[] r = {0.2176, 0.0103, 0.1109, 0.3456, 0.9910, 0.2323, 0.9211, 0.0322, 0.1211, 0.5131, 0.7208, 0.9172, 0.9922, 0.8324, 0.5011, 0.2931};
    //static double[] r = {0.9921, 0.0004, 0.5534, 0.2761, 0.3398, 0.8963, 0.9023, 0.0132, 0.4569, 0.5121, 0.9208, 0.0171, 0.2299, 0.8545, 0.6001, 0.2921};

    public static double nextRandom(double A, double B) {
        x = (a * x + c) % m;
        return (B - A) * (x / m) + A;
    }
}
