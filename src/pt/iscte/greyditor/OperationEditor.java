package pt.iscte.greyditor;

public interface OperationEditor extends Operation {
    int[][] run(int[][] image, Editor editor);
}