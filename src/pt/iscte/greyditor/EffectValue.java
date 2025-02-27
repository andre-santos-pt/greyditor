package pt.iscte.greyditor;

public interface EffectValue extends Effect {
    void apply(int[][] image, int value);
}