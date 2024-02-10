import java.util.Random;
import java.util.Stack;
import java.util.HashMap;
import java.util.Arrays;


public class HexPuzzle{

    final static int SIZE = 19;
    final static int BLANK = 0;

    int[] board;
    int blankPos;

	//creating the board that copies the array and sets the board to be the copy
    public HexPuzzle(int[] board) {
        this.board = Arrays.copyOf(board, board.length);
        for (int i = 0; i < this.board.length; i++) {
            if (this.board[i] == BLANK) {
                blankPos = i;
                return;
            }
        }
    }
    
        //creating the board that copies the array and sets the board to be the copy
    public HexPuzzle(int[] board, int blankPos) {
        this.board = Arrays.copyOf(board, board.length);
        this.blankPos = blankPos;
    }
    
//    //goal position with tiles in order so that the heuristic can be calculated with the goal state, then used in the astar algorithm
//	public HexPuzzle() {
//		board = new int[SIZE];
//		for (int i = 0; i < SIZE; i++) {
//			board[i] = i + 1;
//		}
//		 // index of the blank tile at goal will always be 10 in a 3-4-5-4-3 hexagon board
//		blankPos = 10;
//		board[blankPos] = BLANK;
//	}
	

    public String toString() {
        StringBuilder sb = new StringBuilder();
          for (int i = 0; i < SIZE; i++) {
              if (i == 3 || i == 7 || i == 12 || i == 16) {
                  sb.append("\n");
              }
              sb.append(board[i] + " ");
          }
          return sb.toString();

  }

    //creating the equals and hashcode methods to compare the board and the blank position
    public boolean equals(Object o) {
        HexPuzzle r = (HexPuzzle)o;
        return blankPos == r.blankPos && Arrays.equals(board, r.board);
    }

    public int hashCode() {
        return Arrays.hashCode(board);
    }


    public int getNumber() {
        return 0;
    }

    public void setNumber(int x) {
    }

    /*
     	creating the movements for 6 directions 0- left 1-right 2-upLeft 3-downRight 4-upRight 5-downLeft since its working with a hexagonal board
		interface for the movements
		Logic for moving, if the move is valid, then it will move the blank tile to the new position and return a new board
		if its in the top row it cannot move up, if its in the bottom row it cannot move down, if its in the leftmost column it cannot move left, if its in the rightmost column it cannot move right
		if its in the top left corner it cannot move upLeft, if its in the bottom right corner it cannot move downRight, if its in the top right corner it cannot move upRight, if its in the bottom left corner it cannot move downLeft
		if its in the middle of the board it can move in any direction
 */
    interface MoveAction {
        boolean valid();
        void move();
    }

    MoveAction[] moveActions = new MoveAction[] {
            // Move left
            new MoveAction() {
                public boolean valid() {
                    return !(blankPos == 0 || blankPos == 3 || blankPos == 7 || blankPos == 12 || blankPos == 16); // indices 0, 3, 7, 12, 16 are the leftmost tiles cannot move left
                }
                public void move() {
                    swapTiles(blankPos - 1);
                }
            },
// Move right
            new MoveAction() {
                public boolean valid() {
                    return !(blankPos == 2 || blankPos == 6 || blankPos == 11 || blankPos == 15 || blankPos == 18); // indices 2, 6, 11, 15, 18 are the rightmost tiles cannot move right
                }
                public void move() {
                    swapTiles(blankPos + 1);
                }

            },
            // Move up-left
            new MoveAction() {
                public boolean valid() {
                    return !(blankPos < 3 || blankPos == 3 || blankPos == 7 || blankPos == 12); // indices 0, 1, 2, 3, 7 are the topmost tiles cannot move up-left
                }
                public void move() {
                    swapTiles(blankPos >= 11 ? blankPos - 4 : blankPos - 3);
                }
            },
            // Move down-right
            new MoveAction() {
                public boolean valid() {
                    return !(blankPos > 14 || blankPos == 5 || blankPos == 9 || blankPos == 13); // indices 16, 17, 18, 15, 11 are the bottommost tiles cannot move down-right
                }
                public void move() {
                    swapTiles(blankPos <= 6 ? blankPos + 5 : blankPos + 4);
                }

            },
            // Move up-right
            new MoveAction() {
                public boolean valid() {
                    return !(blankPos < 3 || blankPos == 6 || blankPos == 11 || blankPos == 15); // indices 2, 6, 11 are the topmost tiles cannot move up-right
                }
                public void move() {
                    swapTiles(blankPos >= 11 ? blankPos - 3 : blankPos - 4);
                }

            },
            // Move down-left
            new MoveAction() {
                public boolean valid() {
                    return !(blankPos > 14 || blankPos == 2 || blankPos == 6 || blankPos == 10); // indices 15, 16, 17, 18 are the bottommost tiles cannot move down-left
                }
                public void move() {
                    swapTiles(blankPos <= 6 ? blankPos + 4 : blankPos + 3); 
                }
            }
    };



    
    private static final int[] opp = {1, 0, 3, 2, 5, 4};
    private void swapTiles(int tileIndex) {
        if (tileIndex >= 0 && tileIndex < SIZE) {
            // Swap the current blank tile with the tile at the new position
            int temp = board[blankPos];
            board[blankPos] = board[tileIndex];
            board[tileIndex] = temp;

            // Update the blank tile position
            blankPos = tileIndex;
        }
    }


    static class Node implements Comparable<Node>, Denumerable {
        public HexPuzzle state;
        public Node parent;
        public int g, h;
        public boolean inFrontier;
        public int x;

        Node(HexPuzzle state, Node parent, int g, int h) {
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

        public int getNumber() {
            return x;
        }

        public void setNumber(int x) {
            this.x = x;
        }

        public String toString() {
            return state + "";
        }
    }

    public static void main(String[] args) {
        Random random = new Random();
        int[] goalState = {
                1,2,3,
                4,5,6,7,
                8,9,0,10,11,
                12,13,14,15,
                16,17,18};
        int[] puzzle1  = {
                2, 4, 3,
                1, 0, 7, 10,
                8, 9, 6, 5, 11,
                12, 13, 14, 15,
                16, 17, 18
        };
//        int[] puzzle2 = {
//                1, 5, 10,
//                0, 16, 2, 3,
//                4, 14, 13, 17, 11,
//                12, 8, 6, 18,
//                7, 15, 9
//        };

        HexPuzzle goal = new HexPuzzle(goalState);
        HexPuzzle r = new HexPuzzle(puzzle1);

        for(int i=0; i<1000; i++) {
            int m = random.nextInt(6);
            if(r.moveActions[m].valid())
                r.moveActions[m].move();
        }
        ids(r, goal);
        //astar(r, goal);
    }

    //Used from hint to simulate 2d array provided by simon
    public static int h(HexPuzzle r, HexPuzzle goal) {
        // Assuming x and y arrays represent the coordinates in a form that can be used for calculation.
        int[] x = {0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4};
        int[] y = {2, 4, 6, 1, 3, 5, 7, 0, 2, 4, 6, 8, 1, 3, 5, 7, 2, 4, 6};
        int totalDistance = 0;

        // Create an array to hold the goal position of each tile for quick lookup
        int[] goalPositions = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            goalPositions[goal.board[i]] = i; // Set the goal position of each tile
        }

        // Iterate over each tile in the current puzzle state
        for (int i = 0; i < SIZE; i++) {
            if (r.board[i] != 0) { // Skip the blank tile
                int tile = r.board[i];
                int goalPosition = goalPositions[tile]; // Get the goal position for this tile

                // Calculate the "distance" based on the x and y arrays
                int dx = Math.abs(x[i] - x[goalPosition]);
                int dy = Math.abs(y[i] - y[goalPosition]);

                // Sum the distances for all tiles
                totalDistance += dx + dy;
            }
        }

        return totalDistance;
    }
    
//    public static int h(HexPuzzle r, HexPuzzle goal) {
//        // Arrays representing the coordinates in a hexagonal grid for calculation
//        int[] x = {0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4};
//        int[] y = {2, 4, 6, 1, 3, 5, 7, 0, 2, 4, 6, 8, 1, 3, 5, 7, 2, 4, 6};
//        int totalDistance = 0;
//
//        // Mapping from tile numbers to their goal positions for quick lookup
//        HashMap<Integer, Integer> goalPositions = new HashMap<>();
//        for (int i = 0; i < HexPuzzle.SIZE; i++) {
//            goalPositions.put(goal.board[i], i);
//        }
//
//        // Iterate over each tile in the current puzzle state
//        for (int i = 0; i < HexPuzzle.SIZE; i++) {
//            if (r.board[i] != HexPuzzle.BLANK) { // Skip the blank tile
//                int tile = r.board[i];
//                // Find the goal position for the current tile
//                int goalPosition = goalPositions.get(tile);
//
//                // Calculate the "distance" based on the x and y arrays
//                int dx = Math.abs(x[i] - x[goalPosition]);
//                int dy = Math.abs(y[i] - y[goalPosition]);
//
//                // Sum the distances for all tiles
//                totalDistance += dx + dy;
//            }
//        }
//
//        return totalDistance;
//    }



    public static int ids(HexPuzzle r, HexPuzzle goal){
        for(int limit=0;;limit++) {
            System.out.print(limit + " ");
            int result = bdfs(r, goal, limit);
            if(result != 1) {
                System.out.println();
                return result;
            }
        }
    }

    public static int bdfs(HexPuzzle r, HexPuzzle goal, int limit) {
        if(r.equals(goal))
            return 2;
        else if(limit == 0)
            return 1;
        else {
            boolean cutoff = false;
            for(int i=0; i<6; i++) {
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

    public static int astar(HexPuzzle start, HexPuzzle goal) {
        System.out.println("  f    |frontier|  |explored|");
        Node startNode = new Node(start, null, 0, h(start, goal));
        IndexMinPQ<Node> frontier = new IndexMinPQ<>(); // Assuming IndexMinPQ supports update operations
        frontier.add(startNode); // Add the start node with its f-value as priority
        HashMap<HexPuzzle, Node> explored = new HashMap<>();
        explored.put(start, startNode);

        while (!frontier.isEmpty()) {
            Node currentNode = frontier.remove(); // Remove the node with the lowest f-value
            currentNode.inFrontier = false;

            if (currentNode.state.equals(goal)) { // Goal check
                printAnswer(currentNode);
                return 2; // Success
            }

            for (int i = 0; i < 6; i++) { // Explore all moves
                if (currentNode.state.moveActions[i].valid()) {
                    currentNode.state.moveActions[i].move(); // Apply move
                    HexPuzzle newState = new HexPuzzle(currentNode.state.board, currentNode.state.blankPos);

                    // Create a temporary node for newState to check if it's already in the frontier or explored
                    Node newNode = new Node(newState, currentNode, (currentNode.g + 1), h(newState, goal));

                    if (!explored.containsKey(newState) || newNode.g < explored.get(newState).g) {
                        // If newState is not explored or a better path is found
                        explored.put(newState, newNode); // Update or add to explored

                        if (frontier.contains(newNode)) {
                            frontier.update(newNode); // Update existing node in the frontier
                        } else {
                            frontier.add(newNode); // Add new node to the frontier
                        }
                    }

                    currentNode.state.moveActions[opp[i]].move(); // Undo move to restore state for next iteration
                }
            }
        }

        return 0; // Failure if no solution found
    }
