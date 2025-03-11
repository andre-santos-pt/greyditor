package pt.iscte.greyditor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Greyditor {
    private static final ArrayList<JFrame> ALL_WINDOWS = new ArrayList<>();

    private final Map<String, EditorWindow.EffectMinMax> effects = new LinkedHashMap<>();
    private final Map<String, Operation> operations = new LinkedHashMap<>();

    private String name;

    public static Editor create(int width, int height) {
        return new Greyditor("Greyditor").open(width, height);
    }

    public static Editor create(String fileName) {
        return new Greyditor("Greyditor: " + fileName).open(fileName);
    }

    public static Editor create(int[][] image) {
        return new Greyditor("Greyditor").open(image);
    }

    public Greyditor(String name) {
        this.name = name;
    }

    public void addFilter(String text, FilterSimple filter) {
        effects.put(text, new EditorWindow.EffectMinMax(
                new FilterAdapter(filter), 0, 0));
    }

    public void addFilter(String text, FilterValue filter, int min, int max) {
        effects.put(text, new EditorWindow.EffectMinMax(new FilterValueAdapter(filter), min, max));
    }

    public void addEffect(String text, EffectSimple filter) {
        effects.put(text, new EditorWindow.EffectMinMax(filter, 0, 0));
    }

    public void addEffect(String text, EffectValue filter, int min, int max) {
        effects.put(text, new EditorWindow.EffectMinMax(filter, min, max));
    }

    public void addOperation(String text, Operation operation) {
        operations.put(text, operation);
    }

    public void addSaveOperation(String text) {
        operations.put(text, this::save);
    }

    public void addLoadOperation(String text) {
        operations.put(text, this::load);
    }

    public void addZoomInOperation(String text) {
        operations.put(text, this::zoomIn);
    }

    public void addZoomOutOperation(String text) {
        operations.put(text, this::zoomOut);
    }

    private class FilterAdapter implements EffectSimple {
        FilterSimple f;

        FilterAdapter(FilterSimple f) {
            this.f = f;
        }

        @Override
        public void apply(int[][] image) {
            for (int y = 0; y < image.length; y++)
                for (int x = 0; x < image[y].length; x++)
                    image[y][x] = f.transform(image[y][x]);
        }
    }

    private class FilterValueAdapter implements EffectValue {
        FilterValue f;

        FilterValueAdapter(FilterValue f) {
            this.f = f;
        }

        @Override
        public void apply(int[][] image, int value) {
            for (int y = 0; y < image.length; y++)
                for (int x = 0; x < image[y].length; x++)
                    image[y][x] = f.transform(image[y][x], value);
        }
    }



    public Editor open(int[][] image) {
        EditorWindow e = new EditorWindow(this, name, effects, operations);
        JFrame frame = e.newWindow(image);
        ALL_WINDOWS.add(frame);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ALL_WINDOWS.remove(frame);
                if (ALL_WINDOWS.isEmpty()) {
                    System.exit(0);
                }
                frame.setVisible(false);
            }
        });
        return e;
    }

    public Editor open(String fileName) {
        int[][] image = loadImage(new File(fileName));
        if(image != null)
            return open(image);
        else
            return null;
    }

    public Editor open(int width, int height) {
        if(width < 1 || height < 1) {
            JOptionPane.showMessageDialog(null, "Invalid dimension");
            return null;
        }
        else
            return open(new int[height][width]);
    }

    private int[][] save(int[][] image, Editor editor) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Gravar imagem");
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toLowerCase().endsWith(".png");
            }

            @Override
            public String getDescription() {
                return "PNG files (*.png)";
            }
        };
        fileChooser.setFileFilter(filter);
        int result = fileChooser.showSaveDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            int overwriteOption = selectedFile.exists() ? JOptionPane.showConfirmDialog(
                    null,
                    "O ficheiro existe, de certeza?",
                    "Sobrepor ficheiro?",
                    JOptionPane.YES_NO_OPTION
            ) : JOptionPane.YES_OPTION;

            if (overwriteOption == JOptionPane.YES_OPTION) {
                try {
                    ImageIO.write(EditorWindow.matrixToImage(editor.getImage(), 1), "PNG",
                            selectedFile);
                } catch (IOException e) {
                    editor.message("Erro a gravar o ficheiro");
                }
            }
        }
        return null;
    }


    private int[][] loadImage(File inputFile) {
        try {
            BufferedImage image = ImageIO.read(inputFile);
            if (image.getHeight() > 1000 || image.getWidth() > 1000)
                JOptionPane.showMessageDialog(null, "Tamanho de imagem demasiado grande (máximo: 1000x1000)");
            else if (image.getHeight() < 1 || image.getWidth() < 1)
                JOptionPane.showMessageDialog(null, "Imagem tem que ter pelo menos dimensão 1x1");
            else
                return getImageDataGray(image);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Ficheiro não encontrado: " + inputFile.getAbsolutePath());
        }
        return null;
    }

    private static int[][] getImageDataGray(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] imageData = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                int gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
                imageData[y][x] = gray;
            }
        }
        return imageData;
    }

    private int[][] load(int[][] image, Editor editor) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Abrir imagem");
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            open(selectedFile.getAbsolutePath());
            return null;
        }
        return null;
    }

    private int[][] zoomIn(int[][] image, Editor editor) {
        editor.zoom(editor.getZoomFactor() + 1);
        //((EditorWindow) editor).frame.pack();
        return null;
    }

    private int[][] zoomOut(int[][] image, Editor editor) {
        editor.zoom(editor.getZoomFactor() - 1);
        ((EditorWindow) editor).frame.pack();
        return null;
    }
}
