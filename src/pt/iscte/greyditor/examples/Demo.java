package pt.iscte.greyditor.examples;

import pt.iscte.greyditor.Editor;
import pt.iscte.greyditor.Greyditor;
import pt.iscte.greyditor.Selection;

public class Demo {

    public static void main(String[] args) {
        Greyditor configuration = new Greyditor("Demo");

        // filter without parameter (check box)
        configuration.addFilter("Invert", Demo::invert);

        // filter with parameter (slider)
        configuration.addFilter("Darken", Demo::darken, 0, 255);

        // effect without parameter (check box)
        configuration.addEffect("Grid", Demo::grid);

        // effect with parameter (slider)
        configuration.addEffect("Lines", Demo::lines, 0, 50);

        // default operations
        configuration.addLoadOperation("Load");
        configuration.addSaveOperation("Save");

        // custom operations
        configuration.addOperation("Square", Demo::square);
        configuration.addOperation("Darken area", Demo::darkenArea);

        configuration.open("monalisa.jpg");
    }

    /**
     * Filter to invert the pixel tones
     *
     * @param tone value of a pixel [0, 255]
     * @return transformed value [0, 255]
     */
    static int invert(int tone) {
        return 255 - tone;
    }

    /**
     * Filter to darken the pixel tones with custom intensity
     *
     * @param tone      intensity of a pixel
     * @param intensity filter intensity
     * @return transformed pixel intensity [0, 255]
     */
    static int darken(int tone, int intensity) {
        return Math.max(0, tone - intensity);
    }

    /**
     * Effect to draw a grid of thirds
     *
     * @param image image pixels to modify
     */
    static void grid(int[][] image) {
        int hspace = (image[0].length + 2) / 3;
        int vspace = (image.length + 2) / 3;

        // horizontal lines
        for (int y = vspace; y < image.length; y += vspace)
            for (int x = 0; x < image[y].length; x++)
                image[y][x] = 200;

        // vertical lines
        for (int x = hspace; x < image[0].length; x += hspace)
            for (int y = 0; y < image.length; y++)
                image[y][x] = 200;
    }

    /**
     * Effect to draw horizontal lines with custom spacing
     *
     * @param image   image pixels to modify
     * @param spacing space between the lines
     */
    static void lines(int[][] image, int spacing) {
        if (spacing > 0)
            for (int y = 0; y < image.length; y += spacing)
                for (int x = 0; x < image[y].length; x++)
                    image[y][x] = 0;
    }

    /**
     * Operation to square the image to side x side, where side is
     * the minimum between the original width and height
     *
     * @param image image pixels that will be read
     * @return a new image to replace the current one
     */
    static int[][] square(int[][] image) {
        int side = Math.min(image.length, image[0].length);
        int[][] square = new int[side][side];
        for (int y = 0; y < side; y++)
            for (int x = 0; x < side; x++)
                square[y][x] = image[y][x];

        return square;
    }

    /**
     * Operation to darken the selected area.
     *
     * @param image  image pixels that will be modified
     * @param editor editor operations
     * @return null (no new matrix will replace the existing one)
     */
    static int[][] darkenArea(int[][] image, Editor editor) {
        Selection selection = editor.getSelection();
        if (selection == null) {
            editor.message("Please select an area of the image.");
        }
        else {
            int factor = editor.getInteger("Intensity?");
            for (int y = selection.y(); y < selection.y() + selection.height(); y++)
                for (int x = selection.x(); x < selection.x() + selection.width(); x++)
                    image[y][x] = Math.max(0, image[y][x]- factor);
        }
        return null;
    }
}
