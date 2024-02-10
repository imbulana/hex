import heapq

n,m=map(int, input().split())
A=[[] for _ in range(n+1)]
for _ in range(m):
  u,v,w=map(int, input().split())
  A[u].append((v,w))

class PriorityQueue():
  def __init__(self):
    self.elements=[]
  def push(self, k, v):
    heapq.heappush(self.elements, (k,v))
  def pop(self):
    return heapq.heappop(self.elements)[1]
  def empty(self):
    return not self.elements
  
# an admissible heuristic function (does not overestimate distance to target=>optimal sol)
# to implement based on use case
def h(v): 
  return 0

# get shortest path and distance from s to t
def astar(A, s, t): 
  frontier=PriorityQueue()
  dist=[-1]*(n+1)
  prev=[0]*(n+1)

  dist[s]=0
  prev[s]=-1
  frontier.push(0, s)
  while not frontier.empty():
    u=frontier.pop()
    if u==t: # target found
      break
    for v,w in A[u]:
      d=dist[u]+w
      if prev[v]==0 or d<dist[v]:
        dist[v]=d
        prev[v]=u
        frontier.push(d+h(v), v) # priority is d+heuristic(v)
  
  # get path
  if prev[t]==0: # no path to t
    return [],-1
  path=[]
  u=t
  while u!=s:
    path.append(u)
    u=prev[u]
  path.append(s)
  path.reverse()
  return path, dist[t]

for i in range(1,n+1):
  print(astar(A, 1, i))
