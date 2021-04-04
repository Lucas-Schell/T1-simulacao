public class Main {

    private static final int a = 36789;
    private static final int c = 14168;
    private static final int m = 137921;
    private static double x;
    private static int randomCount = 0;

    public static void main(String[] args) {
        double x = m / 2.0;
    }

    public static double nextRandom(int A, int B){
        if(randomCount == 100000) return -1;
        randomCount++;
        x = (a * x + c) % m;
        return (B - A) * (x / m) + A;
    }
}
