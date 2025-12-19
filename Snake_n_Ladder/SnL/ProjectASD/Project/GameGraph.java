import java.util.*;

public class GameGraph {
    Node head; 
    Node tail; 
    Node[] nodes = new Node[65]; 

    public GameGraph(int size) {
        // 1. Buat Node Linear 1-64
        for (int i = 1; i <= size; i++) {
            nodes[i] = new Node(i);
            if (i > 1) {
                Node prevNode = nodes[i-1];
                Node currNode = nodes[i];
                prevNode.next = currNode;
                currNode.prev = prevNode;
            }
        }
        head = nodes[1];
        tail = nodes[size];
        
        // 2. Tambahkan Koneksi Random
        addRandomConnections(5); 
    }
    
    public Node getNode(int id) {
        if (id < 1) return nodes[1];
        if (id > 64) return nodes[64];
        return nodes[id];
    }
    
    private void addRandomConnections(int count) {
        Random rand = new Random();
        int created = 0;
        
        while (created < count) {
            int start = rand.nextInt(62) + 2; // 2 - 63
            int end = rand.nextInt(62) + 2;
            
            // LOGIC REVISI: Start node TIDAK BOLEH bilangan Prima
            if (isPrime(start)) continue; 

            int startRow = (start - 1) / 8;
            int endRow = (end - 1) / 8;
            
            // Syarat: beda baris, tidak sama, dan belum ada koneksi
            if (startRow != endRow && start != end && 
                nodes[start].jumpTo == null && nodes[end].jumpTo == null) {
                
                nodes[start].jumpTo = nodes[end];
                nodes[end].jumpFrom = nodes[start]; // REVISI: Set kebalikan untuk logika mundur
                created++;
            }
        }
    }
    
    // Helper Cek Prima
    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) if (n % i == 0) return false;
        return true;
    }

    // --- FITUR SHORTEST PATH (BFS) ---
    public List<Node> getShortestPath(int startId) {
        if (startId >= 64) return new ArrayList<>();
        Queue<Node> queue = new LinkedList<>();
        Map<Node, Node> parentMap = new HashMap<>(); 
        Set<Node> visited = new HashSet<>();
        Node startNode = nodes[startId];
        queue.add(startNode); visited.add(startNode);
        boolean found = false;
        while (!queue.isEmpty()) {
            Node curr = queue.poll();
            if (curr.id == 64) { found = true; break; }
            if (curr.next != null && !visited.contains(curr.next)) {
                visited.add(curr.next); parentMap.put(curr.next, curr); queue.add(curr.next);
            }
            if (curr.jumpTo != null && !visited.contains(curr.jumpTo)) {
                visited.add(curr.jumpTo); parentMap.put(curr.jumpTo, curr); queue.add(curr.jumpTo);
            }
        }
        List<Node> path = new ArrayList<>();
        if (found) {
            Node curr = nodes[64];
            while (curr != startNode) { path.add(0, curr); curr = parentMap.get(curr); }
        }
        return path;
    }
}