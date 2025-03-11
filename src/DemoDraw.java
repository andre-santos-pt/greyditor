import pt.iscte.greyditor.Editor;
import pt.iscte.greyditor.GreyImage;
import pt.iscte.greyditor.Greyditor;

public class DemoDraw {

    public static void main() {
        Editor e = Greyditor.create(256, 50);
        e.zoom(3);
        e.draw(img -> {
            int t = 0;
            for (int x = 0; x < img.getWidth(); x++) {
                img.setTone(t+30);
                t++;
                for (int y = 0; y < img.getHeight(); y++)
                    img.paint(x, y);
            }
        });
    }
}
