package pt.iscte.greyditor.examples;

import pt.iscte.greyditor.Editor;
import pt.iscte.greyditor.Greyditor;

public class DemoOperationsWithState {

    public static void main(String[] args) {
        Greyditor configuration = new Greyditor("Demo operations with state");
        DemoOperationsWithState demo = new DemoOperationsWithState();
        configuration.addOperation("Line", demo::line);
        configuration.open("monalisa.jpg");
    }


    int step = 20;
    boolean vertical = true;

    int[][] line(int[][] image, Editor editor) {
        if (vertical) {
            if (step < image[0].length)
                for (int y = 0; y < image.length; y++)
                    image[y][step] = 255;
        } else {
            if(step < image.length) {
                for (int x = 0; x < image[0].length; x++)
                    image[step][x] = 255;
                step += 20;
            }
        }
        vertical = !vertical;
        return null;
    }
}
