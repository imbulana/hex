import java.util.NoSuchElementException;
import java.util.ArrayList;

// adapted from Sedgewick.

public class IndexMinPQ<Key extends Denumerable & Comparable<Key> > {

    private ArrayList<Key> pq; 
    private int N = 0;    

    public IndexMinPQ(int cap) {
        pq = new ArrayList<Key>(cap + 1);
        pq.add(null);
    }

    public IndexMinPQ() { this(1); }

    public IndexMinPQ(Key[] keys) {
	this(keys.length);
        N = keys.length;
        for (int i = 0; i < N; i++) {
            pq.add(keys[i]);
	    keys[i].setNumber(i+1);
	}
        for (int k = N/2; k >= 1; k--)
            sink(k);
    }

    public boolean isEmpty() { return N == 0; }

    public int size() { return N; }

    public Key min() {
        if (isEmpty()) throw new NoSuchElementException("Priority queue underflow");
        return pq.get(1);
    }

    public void add(Key x) {
        N++;
        pq.add(x);
        swim(N);
    }

    public Key remove() {
        if (isEmpty()) throw new NoSuchElementException("Priority queue underflow");
        Key min = pq.get(1);
	swap(pq, 1, N--);
        sink(1);
        pq.remove(N+1); 
	min.setNumber(0);
        return min;
    }

    private void swim(int k) {
        Key x = pq.get(k);
        while (k > 1 && pq.get(k/2).compareTo(x) > 0) {
            pq.set(k, pq.get(k/2));
	    pq.get(k/2).setNumber(k);
            k = k/2;
        }
        pq.set(k, x);
	x.setNumber(k);
    }

    private void sink(int k) {
        Key x = pq.get(k);
        while (2*k <= N) {
            int j = 2*k;
            if (j < N && pq.get(j).compareTo(pq.get(j+1)) > 0) j++;
            if (x.compareTo(pq.get(j)) <= 0) break;
            pq.set(k, pq.get(j));
	    pq.get(j).setNumber(k);
            k = j;
        }
        pq.set(k, x);
	x.setNumber(k);
    }

    private static <Key extends Denumerable> void swap(ArrayList<Key> pq, int i, int j) {
        Key t = pq.get(i); 
	pq.set(i,pq.get(j)); 
	pq.set(j,t);
	pq.get(i).setNumber(i);
	pq.get(j).setNumber(j);
    }

    public void update(Key x) {
	swim(x.getNumber());
    }
}
