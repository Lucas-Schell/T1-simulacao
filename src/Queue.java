import java.util.ArrayList;

public class Queue {
    private final int servers, capacity;
    private final double arrivalMin, arrivalMax, exitMin, exitMax;
    private final Object[][] routes;
    private final String name;
    private int size, loss;
    private double time;
    private final ArrayList<Double> queueTime;

    public Queue(String name, int servers, int capacity, double arrivalMin, double arrivalMax, double exitMin, double exitMax, Object[][] routes) {
        this.name = name;
        this.servers = servers;
        this.capacity = capacity;
        this.arrivalMin = arrivalMin;
        this.arrivalMax = arrivalMax;
        this.exitMin = exitMin;
        this.exitMax = exitMax;
        if (routes != null && routes.length == 2 && routes[0].length == routes[1].length) {
            this.routes = routes;
        } else {
            this.routes = new Object[2][0];
        }
        size = loss = 0;
        time = 0.0;
        queueTime = new ArrayList<>();
    }

    private int media = 0;
    public void print() {
        System.out.println(name);
        for (int i = 0; i < queueTime.size(); i++) {
            System.out.printf("%d\t%.4f\t%.2f%%\n", i, queueTime.get(i) / media, (((queueTime.get(i) / media) * 100) / (time / media)));
        }
        for (int i = queueTime.size(); i < capacity + 1; i++) {
            System.out.printf("%d\t%.4f\t%.2f%%\n", i, 0.0, 0.0);
        }

        System.out.println("Loss: " + (getLoss() / media));
        System.out.println("Total time: " + (getTime() / media) + "\n");
    }

    public void addMedia(){
        size = 0;
        media++;
    }

    public void addTime(double time) {
        if (queueTime.size() <= size) {
            queueTime.add(0.0);
        }
        queueTime.set(size, queueTime.get(size) + time);
        this.time += time;
    }

    public String exit(double route) {
        double count = 0.0;
        for (int i = 0; i < routes[0].length; i++) {
            count += (double) routes[0][i];
            if (route < count) {
                return (String) routes[1][i];
            }
        }
        return "exit";
    }

    public void addSize(int add) {
        size += add;
    }

    public void addLoss() {
        loss++;
    }

    public boolean hasRoutes() {
        return routes[0].length > 0 && (double) routes[0][0] < 1.0;
    }

    public String firstRoute() {
        return routes[0].length > 0 ? (String) routes[1][0] : "exit";
    }

    public double[] getArrival() {
        return new double[]{arrivalMin, arrivalMax};
    }

    public double[] getExit() {
        return new double[]{exitMin, exitMax};
    }

    public int getServers() {
        return servers;
    }

    public int getCapacity() {
        return capacity == -1 ? Integer.MAX_VALUE : capacity;
    }

    public int getSize() {
        return size;
    }

    public int getLoss() {
        return loss;
    }

    public double getTime() {
        return time;
    }
}