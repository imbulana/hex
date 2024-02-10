import heapq

class Puzzle:
  """
  Represents a hex puzzle
    - 'board' is 2D list where board[i] represents the i-th ring from the center
  """

  def __init__(self, board=None, blank_pos=None, shuffle=False):
    self.default_board=[[0],
                   [9,5,6,10,14,13], 
                   [8,4,1,2,3,7,11,15,18,17,16,12]]
    
    self.goal={}
    for i,r in enumerate(self.default_board):
      for j,t in enumerate(r):
        self.goal[t]=(i,j)

    if not board:
      board=self.default_board
      shuffle=True
    self.board=board

    if not blank_pos:
      found=False
      for i in range(len(board)):
        for j in range(len(board[i])):
          if board[i][j]==0:
            blank_pos=(i,j)
            found=True
            break
        if found: break
  
    self.blank_pos=blank_pos
    self.n=len(board) # number of rings

    if shuffle:
      pass

  @property
  def solved(self):
    """
    Check if puzzle is solved
    """
    return self.board==self.default_board

  @property
  def moves(self):
    """
    Returns the set of available moves
    """
    def move(r, t):
      return lambda: self._move(r, t)
    
    moves=[]
    i,j=self.blank_pos
    # current ring
    if i!=0:
      n=len(self.board[i])
      moves.append(move(i, (j+1)%n))
      moves.append(move(i, (j-1)%n))

    # go down one ring
    if i!=0:
      r=i-1
      t=0 if r==0 else j//2
      n=len(self.board[r])
      moves.append(move(r,t))
      if t and j&1: # odd numbered tile
        moves.append(move(r, (t+1)%n))

    # go up one ring
    if i==0:
      moves=[move(1,i) for i in range(len(self.board[1]))]
    elif i!=len(self.board)-1:
      r=i+1
      t=2*j
      n=len(self.board[r])
      moves.append(move(r, t))
      moves.append(move(r, (t-1)%n))
      moves.append(move(r, (t+1)%n))
    return moves

  @property
  def heuristic(self):
    h=0
    board=self.cartesian(self.board)
    goal=self.cartesian(self.default_board)
    for r in self.board:
      for t in r:
        i,j=board[t]
        x,y=goal[t]
        dx=abs(x-i)
        dy=abs(y-j)
        h+=dx+max(dy-dx, 0)/2
    return h
  
  def _move(self, r, t):
    """
    Swap blank tile with board[r][t] and return a new Puzzle instance
    """
    copy=[]
    for ring in self.board:
      copy.append([i for i in ring])
    i,j=self.blank_pos
    copy[i][j], copy[r][t]=copy[r][t], copy[i][j]
    return Puzzle(copy, (r, t))

  def cartesian(self, board):
    """
    Return a representation of the board on the cartesian plane
    """
    # [[0], [9,5,6,10,14,13], [8,4,1,2,3,7,11,15,18,17,16,12]]
    x = [2, 2, 1, 1, 2, 3, 3, 2, 1, 0, 0, 0, 1, 2, 3, 4, 4, 4, 3]
    y = [4, 2, 2, 4, 6, 4, 2, 0, 0, 2, 4, 6, 6, 8, 6, 6, 4, 2, 0]

    d={}
    i=0
    for _,r in enumerate(board):
      for _,t in enumerate(r):
        d[t]=(x[i],y[i])
        i+=1
    return d

  def display(self):
    """
    Print the board to std output
    """
    board=self.cartesian(self.board)
    grid=[[' ' for _ in range(5)] for _ in range(5)]
    for k,(i, j) in board.items():
      grid[i][j//2]=k if k>9 else ' '+str(k)
    i=0
    for row in grid:
      if i%2:
        print(' ', end='')
      i+=1
      print(*row)

  def __str__(self):
    return ''.join(map(str, self))
    
  def __iter__(self):
    for ring in self.board:
      yield from ring

  def __eq__(self, other):
    return self.board==other.board

class Node:
  """
  """
  def __init__(self, puzzle, parent=None):
    self.puzzle=puzzle
    self.parent=parent
    self.g=0 if self.parent==None else self.parent.g+1

  @property
  def h(self):
    return self.puzzle.heuristic

  @property
  def f(self):
    return self.g+self.heuristic

  @property
  def solved(self):
    return self.puzzle.solved
  
  @property
  def path(self):
    p=[]
    u=self
    while u:
      p.append(u)
      u=u.parent
    p.reverse()
    return p
  
  @property
  def moves(self):
    return self.puzzle.moves
  
  def display(self):
    self.puzzle.display()

  def __lt__(self, other):
    return str(self)<str(other)

  def __str__(self):
    return str(self.puzzle)

class PriorityQueue:
  def __init__(self, elements=[]):
    self.elements=elements
  def push(self, k, v):
    heapq.heappush(self.elements, (k, v))
  def pop(self):
    return heapq.heappop(self.elements)[1]
  def empty(self):
    return not self.elements

class Astar:
  """
  Solver class using Astar
    - 'puzzle' is the start puzzle
  """
  def __init__(self, puzzle):
    self.puzzle=puzzle

  def solve(self):
    """
    Run Astar algorithm to solve puzzle
    """
    frontier=PriorityQueue()
    seen=set()
    
    seen.add(str(self.puzzle))    
    frontier.push(0, Node(self.puzzle))
    while frontier:
      u=frontier.pop()
      if u.solved:
        return u.path
      
      for move in u.moves:
        p=move()
        if str(p) not in seen:
          seen.add(str(p))
          v=Node(p, u)
          frontier.push(v.g+v.h, v)
    return [] # no solution
