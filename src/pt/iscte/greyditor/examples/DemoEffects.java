package pt.iscte.greyditor.examples;

import pt.iscte.greyditor.Greyditor;

public class DemoEffects {

    public static void main(String[] args) {
        Greyditor configuration = new Greyditor("Demo Effects");

        // effect without parameter (check box)
        configuration.addEffect("Grid", DemoEffects::grid);

        // effect with parameter (slider)
        configuration.addEffect("Lines", DemoEffects::lines, 0, 50);

        configuration.open("monalisa.jpg");
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
}
