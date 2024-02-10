import java.util.Random;
import java.util.Stack;
import java.util.HashMap;
import java.util.Arrays;

public class Eight {

    //final static int SIZE = 8;
    //final static int LENGTH = 3; // Sqrt(SIZE+1)
    final static int SIZE = 15;
    final static int LENGTH = 4; // Sqrt(SIZE+1)

    int tiles[];
    int blankPos;

    public Eight(int[] x) {
	tiles = Arrays.copyOf(x, x.length);
	for(int i=0; i<=SIZE; i++)
	    if(tiles[i] == 0) {
		blankPos = i;
		return;
	    }
    }

    public Eight(int tiles[], int blankPos) {
	this.tiles = Arrays.copyOf(tiles, tiles.length);
	this.blankPos = blankPos;
    }

    public String toString() {
	String s = "";
	for(int i=0; i<LENGTH; i++) {
	    for(int j=0; j<LENGTH; j++)
		s += String.format(" %2d", tiles[i*LENGTH+j]);
	    s += "\n";
	}
	return s;
    }

    public boolean equals(Object o) {
	Eight r = (Eight)o;
	return blankPos == r.blankPos && Arrays.equals(tiles, r.tiles);
    }

    public int hashCode() { return Arrays.hashCode(tiles); }

    interface MoveAction { boolean valid(); void move(); }

    private MoveAction[] moveActions = new MoveAction[] {
        new MoveAction() { // up
	    public boolean valid() { return blankPos > LENGTH-1; }
	    public void move() { tiles[blankPos] = tiles[blankPos-LENGTH]; blankPos -= LENGTH; tiles[blankPos] = 0;}
	},
        new MoveAction() { // down
	    public boolean valid() { return blankPos < SIZE-LENGTH+1; }
	    public void move() { tiles[blankPos] = tiles[blankPos+LENGTH]; blankPos += LENGTH; tiles[blankPos] = 0;}
	},
        new MoveAction() { // left
	    public boolean valid() { return blankPos % LENGTH != 0; }
	    public void move() { tiles[blankPos] = tiles[blankPos-1]; blankPos -= 1; tiles[blankPos] = 0;}
	},
        new MoveAction() { // right
	    public boolean valid() { return blankPos % LENGTH != LENGTH-1; }
	    public void move() { tiles[blankPos] = tiles[blankPos+1]; blankPos += 1; tiles[blankPos] = 0;}
	}
    };

    private static int[] opp = {1, 0, 3, 2};

    static class Node implements Comparable<Node>, Denumerable {
	public Eight state;
	public Node parent;
	public int g, h;
	public boolean inFrontier;
	public int x;
	Node(Eight state, Node parent, int g, int h) {
	    this.state = state;
	    this.parent = parent;
	    this.g = g;
	    this.h = h;
	    inFrontier = true;
	    x = 0;
	}
	public int compareTo(Node a) {
	    return g + h - a.g - a.h;
	}
	public int getNumber() { return x; }
	public void setNumber(int x) { this.x = x; }
	public String toString() { return state + ""; }
    }
    
    public static void main(String[] args) {
	Random random = new Random();
	int[] x = new int[SIZE+1];
	for(int i=0; i<SIZE; i++)
	    x[i] = i+1;
	x[SIZE] = 0;
	Eight goal = new Eight(x);
	Eight r = new Eight(x);
	for(int i=0; i<1000; i++) {
	    int m = random.nextInt(4);
	    if(r.moveActions[m].valid())
		r.moveActions[m].move();
	}
	// ids(r, goal));
	astar(r, goal);
    }

    public static int ids(Eight r, Eight goal) {
	// This is Iterative Deepening, not IDA*.
	// To change this to IDA*, change the initial value of limit to h(r,goal).
	// Also, keep track of the f-values of nodes that were cut-off during bounded DFS, and use the smallest of those f-values
	// as the new limit.
	for(int limit=0;;limit++) {
	    System.out.print(limit + " ");
	    int result = bdfs(r, goal, limit);
	    if(result != 1) {
		System.out.println();
		return result;
	    }
	}
    }

    public static int bdfs(Eight r, Eight goal, int limit) {
	// returns 0: failure, 1: cutoff, 2: success
	if(r.equals(goal))
	    return 2;
	else if(limit == 0)
	    return 1;
	else {
	    boolean cutoff = false;
	    for(int i=0; i<4; i++) {
		if(r.moveActions[i].valid()) {
		    r.moveActions[i].move();
		    switch(bdfs(r, goal, limit-1)) {
		    case 1: cutoff = true; break;
		    case 2: return 2;
		    default:
		    }
		    r.moveActions[opp[i]].move();
		}
	    }
	    return (cutoff ? 1 : 0);
	}
    }

    public static int h(Eight r, Eight goal) {
	int[] rev = new int[SIZE+1];
	int total = 0;
	for(int i=0; i<=SIZE; i++)
	    rev[goal.tiles[i]] = i;
	for(int i=0; i<=SIZE; i++)
	    if(r.tiles[i] != 0)
		total += Math.abs(i % LENGTH - rev[r.tiles[i]] % LENGTH) + Math.abs(i / LENGTH - rev[r.tiles[i]] / LENGTH);
	return total;
    }

    public static void printAnswer(Node x) {
	Stack<Node> stack = new Stack<>();
	int numMoves = 0;
	for(Node y = x; y != null; y = y.parent) {
	    stack.push(y);
	    numMoves++;
	}
	while(!stack.isEmpty())
	    System.out.println(stack.pop());
	System.out.println((numMoves-1) + " moves.");
    }
    
    public static int astar(Eight start, Eight goal) {
	// returns 0: failure, 2: success
	System.out.println("  f    |frontier|  |explored|");
	int maxF = 0;
	Node z = new Node(start, null, 0, h(start, goal));
	IndexMinPQ<Node> frontier = new IndexMinPQ<>();
	frontier.add(z);
	HashMap<Eight,Node> explored = new HashMap<>();
	explored.put(start, z);
	
	while(true) {
	    if(frontier.isEmpty())
		return 0;
	    Node x = frontier.remove();
	    x.inFrontier = false;
	    if(x.g + x.h > maxF) { maxF = x.g + x.h; System.out.printf("%3d %10d %10d\n", maxF, frontier.size(), explored.size()); }
	    if(x.state.equals(goal)) {
		printAnswer(x);
		return 2;
	    }
	    for(int i=0; i<4; i++) {
		if(x.state.moveActions[i].valid()) {
		    x.state.moveActions[i].move();
		    Node n = explored.get(x.state);
		    if(n == null) {
			Eight s = new Eight(x.state.tiles, x.state.blankPos);
			n = new Node(s, x, x.g+1, h(x.state,goal));
			explored.put(s, n);
			frontier.add(n);
		    }
		    else if(n.inFrontier) {
			if(x.g+1 < n.g) {
			    n.parent = x;
			    n.g = x.g + 1;
			    frontier.update(n);
			}
		    }
		    x.state.moveActions[opp[i]].move();
		}
	    }
	    
	}
    
    }
}
