package pt.iscte.greyditor;

import java.util.function.Consumer;

public interface Editor {
    /**
     * Show a popup message
     * @param text text to display
     */
    void message(String text);

    /**
     * Prompts an integer to the user
     * @param text text to display
     * @return the inserted integer
     */
    int getInteger(String text);

    /**
     * Returns the selected area of the image, a point or a rectangle
     * @return the selection, or null if there is no selection
     */
    Selection getSelection();

    /**
     * Returns the image currently visible in the editor
     * @return a non-null well-formed matrix
     */
    int[][] getImage();

    /**
     * Zooms in the image
     * @param factor zoom factor
     */
    void zoom(int factor);

    /**
     * Returns the current zoom factor
     * @return an integer greater than zero
     */
    int getZoomFactor();

    void draw(Consumer<Image> action);
}
