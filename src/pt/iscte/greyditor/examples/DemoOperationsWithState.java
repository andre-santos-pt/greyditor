package pt.iscte.greyditor.examples;

import pt.iscte.greyditor.Editor;
import pt.iscte.greyditor.Greyditor;

public class DemoOperationsWithState {

    int vstep = 20;
    int hstep = 20;
    boolean vertical = true;

    int[][] vline(int[][] image, Editor editor) {
        if (vstep < image[0].length)
            for (int y = 0; y < image.length; y++)
                image[y][vstep] = 255;
        vstep += 20;
        return null;
    }

    int[][] hline(int[][] image, Editor editor) {
        if (hstep < image.length) {
            for (int x = 0; x < image[0].length; x++)
                image[hstep][x] = 255;
            hstep += 20;
        }
        return null;
    }

    int[][] alternate(int[][] image, Editor editor) {
        if(vertical)
            vline(image, editor);
        else
            hline(image, editor);
        vertical = !vertical;
        return null;
    }

    int[][] reset(int[][] image, Editor editor) {
        vstep = 20;
        hstep = 20;
        vertical = true;
        return null;
    }

    public static void main(String[] args) {
        Greyditor configuration = new Greyditor("Demo operations with state");
        DemoOperationsWithState demo = new DemoOperationsWithState();
        configuration.addOperation("Vertical Line", demo::vline);
        configuration.addOperation("Horizontal Line", demo::hline);
        configuration.addOperation("Alternate Line", demo::alternate);
        configuration.addOperation("Reset", demo::reset);
        configuration.open("monalisa.jpg");
        configuration.open("monalisa.jpg");
    }
}
