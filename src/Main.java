//Lucas Schell e Max Franke

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Main {

    private static final int a = 36789;
    private static final int c = 14168;
    private static final int m = 137921;
    private static long x;
    private static int randomCount, maxRand;
    private static List<Double> rndNumbers;
    private static boolean useRnd;

    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Config config = mapper.readValue(new File("model.yml"), Config.class);

        Map<String, Queue> queues = config.generateQueues();
        List<Object[]> arrivals = config.getArrivals();

        Long[] seeds = {1L};
        rndNumbers = config.getRndNumbers();
        useRnd = rndNumbers.size() > 0;
        if (useRnd) {
            maxRand = rndNumbers.size();
        } else {
            seeds = config.getSeeds().toArray(new Long[0]);
            maxRand = config.getRndNumbersPerSeed();
        }

        sim(queues, arrivals, seeds);
    }

    public static void sim(Map<String, Queue> queues, List<Object[]> arrivals, Long[] seeds) {

        for (Long seed : seeds) {
            x = seed;
            randomCount = 0;
            double time = 0;

            Comparator<Object[]> comparator = (s1, s2) -> {
                double aux = (double) s1[1] - (double) s2[1];
                if (aux < 0) return -1;
                if (aux > 0) return 1;
                return 0;
            };
            PriorityQueue<Object[]> events = new PriorityQueue<>(comparator);
            events.addAll(arrivals);

            simulation:
            while (randomCount < maxRand) {
                Object[] event = events.poll();

                assert event != null;
                String[] eventType = event[0].toString().split("-");
                double eventTime = (double) event[1];

                switch (eventType[0]) {
                    case "A":
                        for (Queue q : queues.values()) {
                            q.addTime(eventTime - time);
                        }
                        time = eventTime;

                        String arrivalKey = eventType[1];
                        Queue arrivalQueue = queues.get(arrivalKey);

                        if (arrivalQueue.getSize() < arrivalQueue.getCapacity()) {
                            arrivalQueue.addSize(1);
                            if (arrivalQueue.getSize() <= arrivalQueue.getServers()) {
                                try {
                                    events.add(generateExitEvent(arrivalQueue, arrivalKey, time));
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
                        events.add(new Object[]{"A-" + arrivalKey, nextRandom(arrival[0], arrival[1]) + time});
                        randomCount++;
                        break;
                    case "E":
                        for (Queue q : queues.values()) {
                            q.addTime(eventTime - time);
                        }
                        time = eventTime;

                        String exitKey = eventType[1];
                        Queue exitQueue = queues.get(exitKey);

                        exitQueue.addSize(-1);

                        if (exitQueue.getSize() >= exitQueue.getServers()) {
                            try {
                                events.add(generateExitEvent(exitQueue, exitKey, time));
                            } catch (Exception e) {
                                break simulation;
                            }
                            randomCount++;
                        }
                        break;
                    case "M":
                        for (Queue q : queues.values()) {
                            q.addTime(eventTime - time);
                        }
                        time = eventTime;

                        String outKey = eventType[1];
                        Queue outQueue = queues.get(outKey);
                        String inKey = eventType[2];
                        Queue inQueue = queues.get(inKey);

                        outQueue.addSize(-1);

                        if (outQueue.getSize() >= outQueue.getServers()) {
                            try {
                                events.add(generateExitEvent(outQueue, outKey, time));
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
                                    events.add(generateExitEvent(inQueue, inKey, time));
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

            for (Queue q : queues.values()) {
                q.addMedia();
            }
        }

        for (Queue q : queues.values()) {
            q.print();
        }
    }

    public static Object[] generateExitEvent(Queue queue, String queueName, double time) throws Exception {
        String dest = queue.firstRoute();
        if (queue.hasRoutes()) {
            dest = queue.exit(nextRandom(0, 1));
            randomCount++;
            if (randomCount == maxRand) throw new Exception();
        }
        double[] exit = queue.getExit();
        if (dest.equals("exit")) {
            return new Object[]{"E-" + queueName, nextRandom(exit[0], exit[1]) + time};
        } else {
            return new Object[]{"M-" + queueName + "-" + dest, nextRandom(exit[0], exit[1]) + time};
        }
    }

    public static double nextRandom(double A, double B) {
        x = (a * x + c) % m;
        return (B - A) * (useRnd ? rndNumbers.get(randomCount) : (double) x / m) + A;
    }
}
