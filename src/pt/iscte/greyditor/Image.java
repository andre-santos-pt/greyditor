package pt.iscte.greyditor;

public interface Image {
    /**
     * Image width
     * @return positive integer
     */
    int getWidth();

    /**
     * Image height
     * @return positive integer
     */
    int getHeight();

    /**
     * Sets the gray tone to paint.
     * The provided value will be used by default.
     * @param tone an integer in the range [0, 255]
     */
    void setTone(int tone);

    /**
     * Paints a point with the default tone
     * Invalid points cause an error message to be printed in the console.
     * @param x x value
     * @param y y value
     */
    void paint(int x, int y);

    /**
     * Paints a point with a given tone
     * Invalid points cause an error message to be printed in the console.
     * @param x x value
     * @param y y value
     * @param tone an integer in the range [0, 255]
     */
    void paint(int x, int y, int tone);
}