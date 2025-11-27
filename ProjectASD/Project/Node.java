public class Node {
    int id;
    Node next;    // Jalan normal (+1)
    Node prev;    // Jalan mundur (-1)
    Node jumpTo;  // KONEKSI RANDOM (Bisa naik/tangga atau turun/ular)
    
    int x, y;
    
    public Node(int id) {
        this.id = id;
        this.next = null;
        this.prev = null;
        this.jumpTo = null; // Default tidak ada jalan pintas
    }
}