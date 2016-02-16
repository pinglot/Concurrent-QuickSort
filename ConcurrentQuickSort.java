import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentQuickSort {
    private static final AtomicInteger count = new AtomicInteger();
    private final int N_THREADS;
    ExecutorService exec;

    private class QuickSortTask <T extends Comparable <T>> implements Runnable {
        private final T[] tab;
        private final int left;
        private final int right;
        private final AtomicInteger count;

        public QuickSortTask (T[] tab, int left, int right, AtomicInteger count) {
            this.tab = tab;
            this.left = left;
            this.right = right;
            this.count = count;
        }

        @Override
        public void run () {
            synchronized (count) {
                quicksort(left, right);
                if (count.getAndDecrement() == 1)   //Jeżeli stara wartość to 1,to nową wartością będzie 0
                    count.notify();
            }
        }

        private void quicksort (int leftIdx, int rightIdx) {
            if (leftIdx < rightIdx) {
                int pivot = partition(leftIdx, rightIdx);
                if (count.get() >= N_THREADS) {     //Gdy przekraczami liczbędostępnych wątków uciekamy się do rekursji
                    quicksort(leftIdx, (pivot - 1));
                    quicksort((pivot + 1), rightIdx);
                } else {
                    count.getAndAdd(2);
                    exec.execute(new QuickSortTask<T>(tab, leftIdx, (pivot - 1), count));
                    exec.execute(new QuickSortTask<>(tab, (pivot +1), rightIdx, count));
                }
            }
        }

        private int partition (int leftIdx, int rightIdx) {
            T pivotValue = tab[rightIdx];
            int storeIdx = leftIdx;

            for (int i=leftIdx; i<rightIdx; i++) {
                if (tab[i].compareTo(pivotValue) < 0) {
                    swap(i, storeIdx);
                    storeIdx++;
                }
            }
            swap(storeIdx, rightIdx);
            return storeIdx;
        }

        private void swap (int leftIdx, int rightIdx) {
            T tmp = tab[leftIdx];
            tab[leftIdx] = tab[rightIdx];
            tab[rightIdx] = tmp;
        }
    }

    public ConcurrentQuickSort (int nThreads) {
        N_THREADS = nThreads;
        exec = Executors.newFixedThreadPool(N_THREADS);
    }

    public <T extends  Comparable<T>> void quicksort (T[] inputTab) {
        try {
            synchronized (count) {
                count.getAndSet(1);
                exec.execute(new QuickSortTask<T>(inputTab, 0, (inputTab.length - 1), count));
                count.wait();
            }
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        } finally {
            exec.shutdownNow();
        }
    }

    public static void main (String[] args) {
        Random generator = new Random();
        int num1 = 1000000;
        int num2 = 16;
        Integer[] valTab = new Integer[num1];
        Integer[] copy = new Integer[num1];

        for(int i=0; i<num1; i++) {
            valTab[i] = generator.nextInt(num1);
        }

        long startTime, estimatedTime;
        double seconds;
        System.arraycopy(valTab, 0, copy, 0, valTab.length);
        startTime = System.nanoTime();
        Arrays.sort(copy);
        estimatedTime = System.nanoTime() - startTime;
        seconds = (double)estimatedTime / 1000000000.0;
        System.out.println("Czas dla Arrays.sort() wynosi: " + seconds + "[s]");

        System.arraycopy(valTab, 0, copy, 0, valTab.length);
        startTime = System.nanoTime();
        Arrays.parallelSort(copy);
        estimatedTime = System.nanoTime() - startTime;
        seconds = (double)estimatedTime / 1000000000.0;
        System.out.println("Czas dla Arrays.parallelSort() wynosi: " + seconds + "[s]");

        System.out.println("Czasy dla ConcurrentQuickSort:");
        for (int i=0; i<num2; i++) {
            System.arraycopy(valTab, 0, copy, 0, valTab.length);
            startTime = System.nanoTime();
            new ConcurrentQuickSort(i+1).quicksort(copy);
            estimatedTime = System.nanoTime() - startTime;
            seconds = (double)estimatedTime / 1000000000.0;
            System.out.println("Czas dla " + (i+1) + " watku/watkow wynosi: " + seconds + "[s]");
        }
    }
}
