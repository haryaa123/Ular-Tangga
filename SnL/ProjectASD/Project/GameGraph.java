import java.util.*;

public class GameGraph {
    Node head; 
    Node tail; 
    Node[] nodes = new Node[65]; 

    public GameGraph(int size) {
        // [STRUKTUR DATA]: Linked List Dua Arah
        // Kita sambungkan node satu per satu (next & prev) 
        // supaya pemain bisa jalan MAJU dan MUNDUR (kalau kena efek mundur).
        for (int i = 1; i <= size; i++) {
            nodes[i] = new Node(i);
            if (i > 1) {
                nodes[i-1].next = nodes[i];
                nodes[i].prev = nodes[i-1];
            }
        }
        head = nodes[1];
        tail = nodes[size];
        
        addRandomConnections(5); // Pasang ular & tangga acak
        addRandomScores(10);     // Sebar koin skor acak
    }
    
    public Node getNode(int id) {
        if (id < 1) return nodes[1];
        if (id > 64) return nodes[64];
        return nodes[id];
    }
    
    private void addRandomConnections(int count) {
        // Logika mengacak posisi ular dan tangga biar game tidak membosankan
        Random rand = new Random();
        int created = 0;
        while (created < count) {
            int start = rand.nextInt(62) + 2; 
            int end = rand.nextInt(62) + 2;
            
            // Validasi: Jangan taruh ular/tangga di angka Prima (karena itu kotak spesial)
            if (isPrime(start)) continue; 

            int startRow = (start - 1) / 8;
            int endRow = (end - 1) / 8;
            
            // Validasi: Ular/Tangga gak boleh di baris yang sama, biar gak aneh gambarnya
            if (startRow != endRow && start != end && 
                nodes[start].jumpTo == null && nodes[end].jumpTo == null) {
                
                nodes[start].jumpTo = nodes[end];    // Jadi Tangga (Naik)
                nodes[end].jumpFrom = nodes[start];  // Jadi Ular (Turun)
                created++;
            }
        }
    }
    
    private void addRandomScores(int count) {
        // Menyebar koin bonus di kotak-kotak yang kosong
        Random rand = new Random();
        int placed = 0;
        while (placed < count) {
            int id = rand.nextInt(62) + 2; 
            if (nodes[id].jumpTo == null && nodes[id].jumpFrom == null && nodes[id].bonusScore == 0) {
                int[] scoreOpts = {20, 20, 50, 50, 100};
                nodes[id].bonusScore = scoreOpts[rand.nextInt(scoreOpts.length)];
                placed++;
            }
        }
    }
    
    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) if (n % i == 0) return false;
        return true;
    }

    // [ALGORITMA]: Breadth-First Search (BFS)
    // Gunanya buat cari "Jalan Tikus" atau rute tercepat ke finish.
    // Fitur ini aktif kalau pemain injak kotak Prima.
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
            if (curr.id == 64) { found = true; break; } // Ketemu finish!
            
            // Cek jalan lewat langkah biasa (next)
            if (curr.next != null && !visited.contains(curr.next)) {
                visited.add(curr.next); parentMap.put(curr.next, curr); queue.add(curr.next);
            }
            // Cek jalan lewat jalan pintas (tangga) -> Ini yang bikin cepet
            if (curr.jumpTo != null && !visited.contains(curr.jumpTo)) {
                visited.add(curr.jumpTo); parentMap.put(curr.jumpTo, curr); queue.add(curr.jumpTo);
            }
        }
        
        // Urutkan rute dari awal ke akhir buat dikirim ke layar
        List<Node> path = new ArrayList<>();
        if (found) {
            Node curr = nodes[64];
            while (curr != startNode) { path.add(0, curr); curr = parentMap.get(curr); }
        }
        return path;
    }
}