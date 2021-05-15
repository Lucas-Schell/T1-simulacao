import java.util.ArrayList;

public class Queue {
    private final int servers, capacity, arrivalMin, arrivalMax, exitMin, exitMax;
    private int size, loss;
    private double[][] routes;
    private ArrayList<Double> queueTime;

    public Queue(int servers, int capacity, int arrivalMin, int arrivalMax, int exitMin, int exitMax, double[][] routes) {
        this.servers = servers;
        this.capacity = capacity;
        this.arrivalMin = arrivalMin;
        this.arrivalMax = arrivalMax;
        this.exitMin = exitMin;
        this.exitMax = exitMax;
        this.routes = routes;
        size = loss = 0;
        queueTime = new ArrayList<>();
    }

    public void addTime(double time) {
        if (queueTime.get(size) == null) {
            queueTime.add(0.0);
        }
        queueTime.set(size, queueTime.get(size) + time);
    }

    public int exit(double route) {
        double count = 0.0;
        for (int i = 0; i < routes[0].length; i++) {
            count += routes[0][i];
            if (route < count) {
                return (int) routes[1][i];
            }
        }
        return -1;
    }

    public int[] getArrival() {
        return new int[]{arrivalMin, arrivalMax};
    }

    public int[] getExit() {
        return new int[]{exitMin, exitMax};
    }

    public int getServers() {
        return servers;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getSize() {
        return size;
    }

    public int getLoss() {
        return loss;
    }
}