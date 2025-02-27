package pt.iscte.greyditor;

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
     * Opens a new editor with the provided image
     * @param image pixels of the image
     */
    void open(int[][] image);
}
