//Lucas Schell e Max Franke

import java.io.File;
import java.io.FileNotFoundException;
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

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("run: java -jar simulator <model.yml>");
            System.exit(0);
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Config config = null;
        try {
            config = mapper.readValue(new File(args[0]), Config.class);
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            System.exit(0);
        } catch (Exception e) {
            System.out.println("File is not correct!");
            e.printStackTrace();
            System.exit(0);
        }

        Map<String, Queue> queues = config.generateQueues();
        List<Object[]> arrivals = config.getArrivals();

        //caso no yml tenha uma lista com numeros aleatorios ja gerados as seeds e rndnumbersPerSeed serao ignorados
        Long[] seeds = {1L};
        rndNumbers = config.getRndnumbers();
        useRnd = rndNumbers.size() > 0;
        if (useRnd) {
            maxRand = rndNumbers.size();
        } else {
            seeds = config.getSeeds().toArray(new Long[0]);
            maxRand = config.getRndnumbersPerSeed();
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
                String[] eventType = (String[]) event[0];
                double eventTime = (double) event[1];

                //Object[] event => { {tipo de evento(A|E|M), nome da fila onde tera o evento, fila destino caso evento seja M}, tempo em que o evento ocorrera}
                switch (eventType[0]) {
                    case "A": //evento de chegada
                        //atualiza o tempo das filas e o tempo global
                        for (Queue q : queues.values()) {
                            q.addTime(eventTime - time);
                        }
                        time = eventTime;

                        String arrivalKey = eventType[1];
                        Queue arrivalQueue = queues.get(arrivalKey);

                        if (arrivalQueue.getSize() < arrivalQueue.getCapacity()) { //se tem espaco na fila adiciona adiciona um
                            arrivalQueue.addSize(1);
                            if (arrivalQueue.getSize() <= arrivalQueue.getServers()) { //caso tenha um servidor livre, gera um evento de saida da fila
                                try {
                                    events.add(generateExitEvent(arrivalQueue, arrivalKey, time));
                                } catch (Exception e) {
                                    break simulation;
                                }
                                randomCount++;
                                if (randomCount == maxRand) break simulation;
                            }
                        } else { //adiciona loss caso nao tenha espaco na fila
                            arrivalQueue.addLoss();
                        }

                        //gera um evento de chegada
                        double[] arrival = arrivalQueue.getArrival();
                        events.add(new Object[]{new String[]{"A", arrivalKey}, nextRandom(arrival[0], arrival[1]) + time});
                        randomCount++;
                        break;
                    case "E": //evento de saida
                        //atualiza o tempo das filas e o tempo global
                        for (Queue q : queues.values()) {
                            q.addTime(eventTime - time);
                        }
                        time = eventTime;

                        String exitKey = eventType[1];
                        Queue exitQueue = queues.get(exitKey);

                        exitQueue.addSize(-1);

                        if (exitQueue.getSize() >= exitQueue.getServers()) {//caso tenha um servidor livre, gera um evento de saida da fila
                            try {
                                events.add(generateExitEvent(exitQueue, exitKey, time));
                            } catch (Exception e) {
                                break simulation;
                            }
                            randomCount++;
                        }
                        break;
                    case "M": //evento de passagem
                        //atualiza o tempo das filas e o tempo global
                        for (Queue q : queues.values()) {
                            q.addTime(eventTime - time);
                        }
                        time = eventTime;

                        String outKey = eventType[1];
                        Queue outQueue = queues.get(outKey);
                        String inKey = eventType[2];
                        Queue inQueue = queues.get(inKey);

                        //fila origem da passagem
                        outQueue.addSize(-1); //remove da fila
                        if (outQueue.getSize() >= outQueue.getServers()) { //caso tenha um servidor livre, gera um evento de saida da fila
                            try {
                                events.add(generateExitEvent(outQueue, outKey, time));
                            } catch (Exception e) {
                                break simulation;
                            }
                            randomCount++;
                            if (randomCount == maxRand) break simulation;
                        }

                        //fila destino da passagem
                        if (inQueue.getSize() < inQueue.getCapacity()) { //se tem espaco na fila adiciona adiciona um
                            inQueue.addSize(1);
                            if (inQueue.getSize() <= inQueue.getServers()) { //caso tenha um servidor livre, gera um evento de saida da fila
                                try {
                                    events.add(generateExitEvent(inQueue, inKey, time));
                                } catch (Exception e) {
                                    break simulation;
                                }
                                randomCount++;
                            }
                        } else { //adiciona loss caso nao tenha espaco na fila
                            inQueue.addLoss();
                        }
                        break;
                    default:
                        break simulation;
                }
            }

            for (Queue q : queues.values()) { //informa as filas que uma simulacao acabou
                q.addSimulacao();
            }
        }

        for (Queue q : queues.values()) { //imprime o estado final de cada fila
            q.print();
        }
    }

    public static Object[] generateExitEvent(Queue queue, String queueName, double time) throws Exception {
        String dest = queue.firstRoute(); //recebe o destino do primeiro roteamento ou "exit", para nao gerar um numero aleatorio sem necessidade
        if (queue.hasRoutes()) { //verifica se a fila possui roteamento
            dest = queue.exit(nextRandom(0, 1)); //recebe fila destino da passagem ou "exit" para saida
            randomCount++;
            if (randomCount == maxRand) throw new Exception();
        }
        double[] exit = queue.getExit();
        if (dest.equals("exit")) { //caso destino seja "exit" cria um evento de saida, senao cria um evento de passagem
            return new Object[]{new String[]{"E", queueName}, nextRandom(exit[0], exit[1]) + time};
        } else {
            return new Object[]{new String[]{"M", queueName, dest}, nextRandom(exit[0], exit[1]) + time};
        }
    }

    public static double nextRandom(double A, double B) {
        x = (a * x + c) % m;
        return (B - A) * (useRnd ? rndNumbers.get(randomCount) : (double) x / m) + A;
    }
}
