import java.awt.Color;
import java.util.*;

// 1. CLASS NODE (Petak)
class Node {
    int id;
    Node next, prev;     // Linked List path
    Node jumpTo;         // Untuk Tangga/Ular/Jalan Pintas
    Node jumpFrom;       // Penanda jika ini adalah ekor ular/tangga
    int bonusScore;      // FITUR: Random Score di dalam node
    int x, y;            // Koordinat visual

    public Node(int id) {
        this.id = id;
        this.bonusScore = 0;
    }
}

// 2. CLASS PLAYER
class Player {
    String name;
    Color color;
    int position;
    int score;
    int totalWins; // Untuk History kemenangan

    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
        reset();
    }

    public void reset() {
        this.position = 1; // Mulai dari 1
        this.score = 0;
    }
}

// 3. CLASS GAME GRAPH (Logic Papan)
class GameGraph {
    Node[] nodes;
    int size;

    public GameGraph(int size) {
        this.size = size;
        nodes = new Node[size + 1];

        // Buat Node Linear
        for (int i = 1; i <= size; i++) {
            nodes[i] = new Node(i);
            if (i > 1) {
                nodes[i - 1].next = nodes[i];
                nodes[i].prev = nodes[i - 1];
            }
        }

        // Tambah Koneksi Random (Ular/Tangga)
        addRandomConnections(5);
        
        // FITUR: Random Score di Node
        addRandomScores(10); // Ada 10 node yang punya bonus score
    }

    public Node getNode(int id) {
        if (id < 1) return nodes[1];
        if (id > size) return nodes[size];
        return nodes[id];
    }

    private void addRandomConnections(int count) {
        Random rand = new Random();
        int created = 0;
        while (created < count) {
            int start = rand.nextInt(size - 2) + 2;
            int end = rand.nextInt(size - 2) + 2;
            
            // Logic simple: jangan di baris yang sama, jangan prima (opsional)
            if (start != end && Math.abs(start - end) > 5 && nodes[start].jumpTo == null) {
                nodes[start].jumpTo = nodes[end];
                nodes[end].jumpFrom = nodes[start];
                created++;
            }
        }
    }

    private void addRandomScores(int count) {
        Random rand = new Random();
        for(int i=0; i<count; i++) {
            int idx = rand.nextInt(size) + 1;
            // Kasih skor acak antara 10 - 50
            nodes[idx].bonusScore = (rand.nextInt(5) + 1) * 10; 
        }
    }
    
    // Fitur Shortest Path (BFS) - Masih disimpan kalau butuh
    public List<Node> getShortestPath(int startId) {
        if (startId >= size) return new ArrayList<>();
        Queue<Node> queue = new LinkedList<>();
        Map<Node, Node> parentMap = new HashMap<>(); 
        Set<Node> visited = new HashSet<>();
        Node startNode = nodes[startId];
        
        queue.add(startNode); visited.add(startNode);
        boolean found = false;
        
        while (!queue.isEmpty()) {
            Node curr = queue.poll();
            if (curr.id == size) { found = true; break; }
            
            // Cek Next, Prev, dan Jump
            Node[] neighbors = {curr.next, curr.jumpTo};
            for (Node neighbor : neighbors) {
                if (neighbor != null && !visited.contains(neighbor)) {
                    visited.add(neighbor); 
                    parentMap.put(neighbor, curr); 
                    queue.add(neighbor);
                }
            }
        }
        
        List<Node> path = new ArrayList<>();
        if (found) {
            Node curr = nodes[size];
            while (curr != startNode) { path.add(0, curr); curr = parentMap.get(curr); }
        }
        return path;
    }
}