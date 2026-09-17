import java.util.concurrent.atomic.DoubleAdder;

public class Summa {
    public static final int A = 0;
    public static final int B = 12;
    public static final int SIZE = 10000000;
    public static final int THREADS = 6;
    public static final double STEP = (double)(B-A)/SIZE;
    public static final double ITEMS_PER_THREAD = (double)(B-A)/THREADS;
    static class Acc{
        volatile double acc = 0.0;
        synchronized public void addToAcc(double n){
            acc += n;
        }
    }

    public static double function(double x){
        return Math.sin(x) * Math.cos(x * x);
    }

    public static Thread taskThread(int n, double[] schedule, double[] results){
        return new Thread(()-> {
            var start = schedule[n];
            var finish = schedule[n] + ITEMS_PER_THREAD;
            double acc = 0.0;

            int steps = (int) Math.ceil((finish - start) / STEP);

            for (int k = 0; k < steps; k++) {
                double i = start + k * STEP;
                double y = function(i);
                acc += y * STEP;
            }
            results[n] = acc;
        });
    }
    public static Thread taskMonitorThread(int n, double[] schedule, Acc acc){
        return new Thread(()-> {
            var start = schedule[n];
            var finish = schedule[n] + ITEMS_PER_THREAD;
            int steps = (int) Math.ceil((finish - start) / STEP);
            for (int k = 0; k < steps; k++) {
                double i = start + k * STEP;
                double y = function(i);
                acc.addToAcc(y * STEP);
            }
        });
    }
    public static Thread taskAtomicThread(int n, double[] schedule, DoubleAdder acc){
        return new Thread(()-> {
            var start = schedule[n];
            var finish = schedule[n] + ITEMS_PER_THREAD;
            int steps = (int) Math.ceil((finish - start) / STEP);
            for (int k = 0; k < steps; k++) {
                double i = start + k * STEP;
                double y = function(i);
                acc.add(y * STEP);
            }
        });
    }

    public static void measureP() throws InterruptedException {

        var threadsStart = new double[THREADS];

        var threads = new Thread[THREADS];

        for (int i = 0; i < THREADS; i++) {
            threadsStart[i] = A + i * ITEMS_PER_THREAD;
        }

        double[] results = new double[THREADS];

        var pStart = System.nanoTime();

        for (int i = 0; i < THREADS; i++) {
            threads[i] = taskThread(i, threadsStart, results);
        }

        for (int i = 0; i < THREADS; i++)
            threads[i].start();

        for (int i = 0; i < THREADS; i++)
            threads[i].join();

        double pResult = 0.0;
        for (int i = 0; i < THREADS; i++) {
            pResult += results[i];
        }
        var pFinish = System.nanoTime();
        System.out.println("Parallel result");
        System.out.println(pResult);
        System.out.println("Parallel time (ms)");
        System.out.println((double)(pFinish - pStart)/1000000);
    }

    public static void measureMon() throws InterruptedException {

        var threadsStart = new double[THREADS];

        var threads = new Thread[THREADS];

        for (int i = 0; i < THREADS; i++) {
            threadsStart[i] = A + i * ITEMS_PER_THREAD;
        }

        double[] results = new double[THREADS];

        var pStart = System.nanoTime();
        Acc acc = new Acc();
        for (int i = 0; i < THREADS; i++) {
            threads[i] = taskMonitorThread(i, threadsStart, acc);
        }

        for (int i = 0; i < THREADS; i++)
            threads[i].start();

        for (int i = 0; i < THREADS; i++)
            threads[i].join();

        var pResult = acc.acc;
        var pFinish = System.nanoTime();
        System.out.println("Monitor result");
        System.out.println(pResult);
        System.out.println("Monitor time (ms)");
        System.out.println((double)(pFinish - pStart)/1000000);
    }

    public static void measureAtomic() throws InterruptedException {

        var threadsStart = new double[THREADS];

        var threads = new Thread[THREADS];

        for (int i = 0; i < THREADS; i++) {
            threadsStart[i] = A + i * ITEMS_PER_THREAD;
        }

        double[] results = new double[THREADS];

        var pStart = System.nanoTime();
        DoubleAdder acc = new DoubleAdder();
        for (int i = 0; i < THREADS; i++) {
            threads[i] = taskAtomicThread(i, threadsStart, acc);
        }

        for (int i = 0; i < THREADS; i++)
            threads[i].start();

        for (int i = 0; i < THREADS; i++)
            threads[i].join();

        double pResult = acc.sum();
        var pFinish = System.nanoTime();
        System.out.println("Atomic result");
        System.out.println(pResult);
        System.out.println("Atomic time (ms)");
        System.out.println((double)(pFinish - pStart)/1000000);
    }

    public static void main(String[] args) throws InterruptedException {

        var threadsStart = new double[THREADS];

        var threads = new Thread[THREADS];

        for (int i = 0; i < THREADS; i++) {
            threadsStart[i] = A + i * ITEMS_PER_THREAD;
        }

        int[] results = new int[THREADS];


        var start = System.nanoTime();
        double acc = 0.0;

        int steps = (int) Math.ceil((B-A) / STEP);

        for (int k = 0; k < steps; k++) {
            double i = A + k * STEP;
            double y = function(i);
            acc += y * STEP;
        }

        var finish = System.nanoTime();

        System.out.println("Sequential result");
        System.out.println(acc);
        System.out.println("Sequential time (ms)");
        System.out.println((double)(finish - start)/1000000);

        measureP();
        measureAtomic();
        measureMon();
    }
}
