package pt.iscte.greyditor;

public record Selection(int x, int y, int width, int height) {
    public boolean isSingle() {
        return width == -1 && height == -1;
    }

    public Selection translate(int x, int y) {
        return new Selection(this.x + x, this.y + y, width, height);
    }

    public Selection scaleDown(int f) {
        return new Selection(x / f, y / f, width / f, height / f);
    }
}