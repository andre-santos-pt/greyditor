package pt.iscte.greyditor;

public interface FilterValue extends Effect {
    int transform(int tone, int value);
}