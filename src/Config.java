import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Config {
    public Map<String, Double> arrivals = new HashMap<>();
    public Map<String, Queues> queues = new HashMap<>();
    public List<Route> network = new LinkedList<>();
    public List<Long> seeds = new LinkedList<>();
    public int rndnumbersPerSeed = 100000;
    public List<Double> rndnumbers = new LinkedList<>();

    public static class Queues {
        public int servers;
        public int capacity = -1;
        public double minArrival;
        public double maxArrival;
        public double minService;
        public double maxService;
        public List<String> target = new LinkedList<>();
        public List<Double> probability = new LinkedList<>();
    }

    public static class Route {
        public String source;
        public String target;
        public double probability;
    }

    public Map<String, Queue> generateQueues() {
        generateRouting();
        Map<String, Queue> queues = new HashMap<>();
        for (Map.Entry<String, Queues> entry : this.queues.entrySet()) {
            String key = entry.getKey();
            Queues value = entry.getValue();
            queues.put(key, new Queue(key, value.servers, value.capacity, value.minArrival, value.maxArrival,
                    value.minService, value.maxService, new Object[][]{value.probability.toArray(), value.target.toArray()}));
        }
        return queues;
    }

    private void generateRouting() {
        for (Route r : network) {
            queues.get(r.source).target.add(r.target);
            queues.get(r.source).probability.add(r.probability);
        }
    }

    public List<Object[]> getArrivals() {
        List<Object[]> list = new LinkedList<>();
        for (Map.Entry<String, Double> entry : this.arrivals.entrySet()) {
            list.add(new Object[]{new String[]{"A", entry.getKey()}, entry.getValue()});
        }
        return list;
    }

    public List<Long> getSeeds() {
        return seeds;
    }

    public List<Double> getRndnumbers() {
        return rndnumbers;
    }

    public int getRndnumbersPerSeed() {
        return rndnumbersPerSeed;
    }
}
