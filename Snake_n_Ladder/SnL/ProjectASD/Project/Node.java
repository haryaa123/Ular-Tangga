public class Node {
    int id;
    Node next;    // Jalan normal (+1)
    Node prev;    // Jalan mundur (-1)
    Node jumpTo;  // Tujuan shortcut (Naik/Turun)
    Node jumpFrom; // Asal shortcut (Untuk logika mundur)
    
    int x, y;
    
    public Node(int id) {
        this.id = id;
        this.next = null;
        this.prev = null;
        this.jumpTo = null;
        this.jumpFrom = null; 
    }
}