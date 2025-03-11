package pt.iscte.greyditor.examples;

import pt.iscte.greyditor.Greyditor;

public class DemoFilters {

    public static void main(String[] args) {
        Greyditor configuration = new Greyditor("Demo Filters");

        // filter without parameter (check box)
        configuration.addFilter("Invert", DemoFilters::invert);

        // filter with parameter (slider)
        configuration.addFilter("Darken", DemoFilters::darken, 0, 255);

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
}
