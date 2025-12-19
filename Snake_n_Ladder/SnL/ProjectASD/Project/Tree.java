import java.util.ArrayList;
import java.util.List;

public class Tree {
    Node root;
    
    public Tree(Node r){
        this.root = r;
    }
    
    // --- Logic Traversal Lama ---
    public void preOrderTraversal(Node r){
        if (r == null) return;
        System.out.print(r.data + " ");
        preOrderTraversal(r.left);
        preOrderTraversal(r.right);
    }
    
    public void inOrderTraversal(Node r){
        if (r == null) return;
        inOrderTraversal(r.left);
        System.out.print(r.data + " ");
        inOrderTraversal(r.right);
    }
    
    public void postOrderTraversal(Node r){
        if (r == null) return;
        postOrderTraversal(r.left);
        postOrderTraversal(r.right);
        System.out.print(r.data + " ");
    }

    // --- FITUR BARU: Mencari Jalur (Pathfinding) ---
    public List<Node> findPath(String targetData) {
        List<Node> path = new ArrayList<>();
        if (root == null) return path;
        
        if (findPathRecursive(root, targetData, path)) {
            return path;
        } else {
            return new ArrayList<>(); // Return list kosong jika tidak ketemu
        }
    }

    private boolean findPathRecursive(Node curr, String target, List<Node> path) {
        if (curr == null) return false;

        // Tambahkan node saat ini ke jalur sementara
        path.add(curr);

        // Cek apakah node ini adalah tujuan?
        if (curr.data.equals(target)) {
            return true;
        }

        // Cek kiri
        if (findPathRecursive(curr.left, target, path)) {
            return true;
        }

        // Cek kanan
        if (findPathRecursive(curr.right, target, path)) {
            return true;
        }

        // Jika tidak ditemukan di kiri maupun kanan, hapus dari jalur (backtrack)
        path.remove(path.size() - 1);
        return false;
    }
}