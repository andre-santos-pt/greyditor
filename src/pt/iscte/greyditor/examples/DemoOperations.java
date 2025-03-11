package pt.iscte.greyditor.examples;

import pt.iscte.greyditor.Editor;
import pt.iscte.greyditor.Greyditor;
import pt.iscte.greyditor.Selection;

public class DemoOperations {

    public static void main(String[] args) {
        Greyditor configuration = new Greyditor("Demo Operations");

        // default operations
        configuration.addLoadOperation("Load");
        configuration.addSaveOperation("Save");

        // custom operations
        configuration.addOperation("Clear", DemoOperations::clear);
        configuration.addOperation("Square", DemoOperations::square);

        configuration.open("monalisa.jpg");
    }

    /**
     * Operation to clear (paint white) the selected area.
     * If there is no selection, the whole image is cleared.
     *
     * @param image  image pixels that will be modified
     * @param editor editor operations
     * @return null (no new matrix will replace the existing one)
     */
    static int[][] clear(int[][] image, Editor editor) {
        Selection selection = editor.getSelection();
        if (selection == null) {
            selection = new Selection(0, 0, image[0].length, image.length);
            editor.message("whole image will get white!");
        } else
            editor.message("selection will get white!");

        for (int y = selection.y(); y < selection.y() + selection.height(); y++)
            for (int x = selection.x(); x < selection.x() + selection.width(); x++)
                image[y][x] = 255;

        return null;
    }

    /**
     * Operation to replace the image with the top-left corner of the existing image.
     * The side of the corner is prompted to the user.
     *
     * @param image  image pixels that will be read
     * @param editor editor operations
     * @return a new image to replace the current one
     */
    static int[][] square(int[][] image, Editor editor) {
        int side = editor.getInteger("Side");
        side = Math.min(side, image.length);
        side = Math.min(side, image[0].length);

        int[][] square = new int[side][side];
        for (int y = 0; y < side; y++)
            for (int x = 0; x < side; x++)
                square[y][x] = image[y][x];

        return square;
    }
}
