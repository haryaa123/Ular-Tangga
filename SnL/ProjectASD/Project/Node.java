public class Node {
    int id;         // Nomor kotak (1-64)
    Node next;      // Penunjuk ke kotak berikutnya (buat jalan maju)
    Node prev;      // Penunjuk ke kotak sebelumnya (buat jalan mundur)
    Node jumpTo;    // Kalau kena Tangga, loncat ke node ini
    Node jumpFrom;  // Kalau kena Ular, turun ke node ini
    
    int bonusScore; // Koin skor tambahan di kotak ini
    int x, y;       // Koordinat (posisi gambar di layar)
    
    public Node(int id) {
        this.id = id;
        this.next = null;
        this.prev = null;
        this.jumpTo = null;
        this.jumpFrom = null; 
        this.bonusScore = 0; 
    }
}