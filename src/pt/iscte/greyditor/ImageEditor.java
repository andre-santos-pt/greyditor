package pt.iscte.greyditor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Supplier;

public class ImageEditor implements Editor {
    private static final int VALUE_OFF = new Color(255, 0, 0).getRGB();
    private static final Color[] PALETTE_COLOR = new Color[256];
    private static final int[] PALETTE = new int[256];
    private static final int PADDING = 20;
    private static final ArrayList<ImageEditor> ALL_WINDOWS = new ArrayList<>();

    static {
        for (int i = 0; i < 256; i++) {
            PALETTE_COLOR[i] = new Color(i, i, i);
            PALETTE[i] = PALETTE_COLOR[i].getRGB();
        }
    }

    private int[][] image;
    private final JFrame frame;
    private final ImagePanel imagePanel;
    private final JLabel sizeLabel;
    private final JLabel pointLabel;
    private final JLabel toneLabel;
    private final JPanel effectsPanel;
    private final ArrayList<FilterWrapper> effects = new ArrayList<>();
    private final JPanel operationsPanel;
    private final ArrayList<OperationWrapper> operations = new ArrayList<>();

    public ImageEditor(String title) {
        frame = new JFrame(title);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        imagePanel = new ImagePanel();
        frame.add(imagePanel, BorderLayout.CENTER);

        sizeLabel = new JLabel();
        JPanel sizePanel = new JPanel();
        sizePanel.add(sizeLabel);
        frame.add(sizePanel, BorderLayout.NORTH);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 10));
        pointLabel = new JLabel();
        footer.add(pointLabel);
        toneLabel = new JLabel(" ");
        toneLabel.setOpaque(true);
        footer.add(toneLabel);
        frame.add(footer, BorderLayout.SOUTH);

        effectsPanel = new JPanel();
        effectsPanel.setLayout(new BoxLayout(effectsPanel, BoxLayout.Y_AXIS));
        effectsPanel.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        frame.add(effectsPanel, BorderLayout.EAST);

        operationsPanel = new JPanel();
        operationsPanel.setLayout(new BoxLayout(operationsPanel, BoxLayout.Y_AXIS));
        operationsPanel.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));

        frame.add(operationsPanel, BorderLayout.WEST);

        final ImageEditor instance = this;
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ALL_WINDOWS.remove(instance);
                if (ALL_WINDOWS.isEmpty()) {
                    System.exit(0);
                }
                frame.setVisible(false);
            }
        });
    }

    private int[][] loadImage(File inputFile) {
        try {
            BufferedImage image = ImageIO.read(inputFile);
            if (image.getHeight() > 1000 || image.getWidth() > 1000)
                JOptionPane.showMessageDialog(frame, "Tamanho de imagem demasiado grande (máximo: 1000x1000)");
            else if(image.getHeight() < 1 || image.getWidth() < 1)
                JOptionPane.showMessageDialog(frame, "Imagem tem que ter pelo menos dimensão 1x1");
            else
                return getImageDataGray(image);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Ficheiro não encontrado: " + inputFile.getAbsolutePath());
        }
        return null;
    }

    private boolean isWellFormed(int[][] matrix) {
        for (int i = 0; i < matrix.length - 1; i++)
            if (matrix[i] == null || matrix[i + 1] == null || matrix[i].length != matrix[i + 1].length)
                return false;

        return true;
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

    public void addSaveOperation(String text) {
        addOperation(text, this::save);
    }

    public void addLoadOperation(String text) {
        addOperation(text, this::load);
    }

    private void open() {
        imagePanel.setPreferredSize(new Dimension(
                Math.max(300, this.image[0].length + PADDING * 2),
                Math.max(300, this.image.length + PADDING * 2)));
        imagePanel.refresh();
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        ALL_WINDOWS.add(this);
    }

    public void open(int[][] image) {
        if (image == null) {
            System.err.println("Argumento não pode ser nulo");
            return;
        }
        if (!isWellFormed(image)) {
            System.err.println("Matriz mal formada");
            return;
        }

        ImageEditor editor = new ImageEditor("Vintage Editor");
        copyFiltersAndOperations(editor);
        editor.image = image;
        editor.open();
    }

    public void open(int width, int height) {
        if (width < 1 || height < 1)
            System.err.println("Dimensões inválidas: " + width + " x " + height);
        else
            open(new int[width][height]);
    }

    public void open(String imagePath) {
        int[][] image = loadImage(new File(imagePath));
        if (image != null)
            if (image.length < 1000 && image[0].length < 1000)
                open(image);
            else
                JOptionPane.showMessageDialog(frame, "Tamanho de imagem demasiado grande (máximo: 1000x1000)");
    }

    public void message(String text) {
        JOptionPane.showMessageDialog(frame, text);
    }

    public int getInteger(String text) {
        String input = null;
        do {
            input = JOptionPane.showInputDialog(frame, text);
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                message("Valor não é um inteiro");
                input = null;
            }
        } while (input == null);
        return 0;
    }


    private void copyFiltersAndOperations(ImageEditor editor) {
        for (FilterWrapper f : effects) {
            if (f.effect instanceof EffectSimple)
                editor.addEffect(f.text, (EffectSimple) f.effect);
            else if (f.effect instanceof EffectValue)
                editor.addEffect(f.text, (EffectValue) f.effect, f.min, f.max);
        }
        editor.operations.clear();
        for (OperationWrapper o : operations)
            editor.addOperation(o.text, o.operation);
    }

    public Selection getSelection() {
        return imagePanel.getSelection();
    }

    private class ImagePanel extends JPanel {
        Point from;
        Point to;
        int[][] localImg = image == null ? new int[200][200] : deepCopy(image);

        ImagePanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (from == null) {
                        from = e.getPoint();
                        from.translate(-PADDING, -PADDING);
                    } else if (to == null) {
                        to = e.getPoint();
                        to.translate(-PADDING, -PADDING);
                    } else {
                        from = null;
                        to = null;
                    }
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    pointLabel.setText("");
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    Point p = e.getPoint();
                    if(p.x >= PADDING && p.y >= PADDING &&
                            p.x < localImg[0].length + PADDING && p.y < localImg.length + PADDING) {
                        int x = p.x - PADDING;
                        int y = p.y - PADDING;
                        pointLabel.setText("x: " + x + "  y: " + y);
                        toneLabel.setBackground(PALETTE_COLOR[localImg[y][x]]);
                        int fg = localImg[y][x] < 128 ? 255 : 0;
                        toneLabel.setForeground(PALETTE_COLOR[fg]);
                        toneLabel.setText("tone: " + localImg[y][x]);
                    }
                    else {
                        pointLabel.setText("");
                        toneLabel.setText(" ");
                        toneLabel.setBackground(null);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(matrixToImage(localImg), PADDING, PADDING, null);
            g2d.setStroke(new BasicStroke(3));
            g2d.setColor(Color.CYAN);
            if (from != null && to != null) {
                Selection sel = getSelection();
                if (sel != null)
                    g2d.drawRect(sel.x() + PADDING, sel.y() + PADDING, sel.width(), sel.height());
            } else if (from != null) {
                g2d.fillRect(from.x + PADDING - 2, from.y + PADDING - 2, 3, 3);
            }
        }

        Selection getSelection() {
            if (from == null)
                return null;
            else if (to == null)
                return new Selection(from.x, from.y, -1, -1);
            else
                return new Selection(Math.min(from.x, to.x), Math.min(from.y, to.y),
                        Math.abs(to.x - from.x), Math.abs(to.y - from.y));
        }

        public void refresh() {
            localImg = deepCopy(image);
            for (FilterWrapper f : effects)
                f.apply(localImg);
            setPreferredSize(new Dimension(this.localImg[0].length + PADDING * 2, this.localImg.length + PADDING * 2));
            repaint();
            sizeLabel.setText(localImg[0].length + " x " + localImg.length);
        }

        public void clearSelection() {
            from = null;
            to = null;
        }
    }

    private static int[][] deepCopy(int[][] original) {
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = new int[original[i].length];
            System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
        }
        return copy;
    }

    private static BufferedImage matrixToImage(int[][] matrix) {
        int width = matrix[0].length;
        int height = matrix.length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int constrain = Math.max(0, Math.min(matrix[y][x], 255));
                if (constrain != matrix[y][x])
                    image.setRGB(x, y, VALUE_OFF);
                else
                    image.setRGB(x, y, PALETTE[constrain]);
            }
        }
        return image;
    }

    public int[][] save(int[][] image, Editor editor) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Gravar imagem");
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
                    ImageIO.write(matrixToImage(image), "PNG",
                            selectedFile);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(frame, "Erro a gravar o ficheiro");
                }
            }
        }
        return null;
    }

    public int[][] load(int[][] image, Editor editor) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Abrir imagem");
        int result = fileChooser.showOpenDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            editor.open(loadImage(selectedFile));
            return null;
        }
        return null;
    }

    private boolean valid(int tone) {
        return tone >= 0 && tone <= 255;
    }

    private record FilterWrapper(String text, Effect effect,
                                 Supplier<Integer> supplier, int min, int max) {
        void apply(int[][] image) {
            if (effect instanceof EffectSimple && supplier.get() != null)
                ((EffectSimple) effect).apply(image);
            else if (effect instanceof EffectValue)
                ((EffectValue) effect).apply(image, supplier.get());
        }
    }

    public void addFilter(String text, FilterSimple filter) {
        EffectSimple f = (image) -> {
            for (int y = 0; y < image.length; y++)
                for (int x = 0; x < image[y].length; x++)
                    if (valid(image[y][x]))
                        image[y][x] = filter.transform(image[y][x]);
        };
        addEffect(text, f);
    }


    public void addFilter(String text, FilterValue filter, int min, int max) {
        EffectValue f = (image, value) -> {
            for (int y = 0; y < image.length; y++)
                for (int x = 0; x < image[y].length; x++)
                    if (valid(image[y][x]))
                        image[y][x] = filter.transform(image[y][x], value);
        };
        addEffect(text, f, min, max);
    }

    public void addEffect(String text, EffectSimple filter) {
        JCheckBox check = new JCheckBox(text);
        check.setAlignmentX(Component.LEFT_ALIGNMENT);
        check.addChangeListener(e -> {
            imagePanel.refresh();
        });
        effectsPanel.add(check);
        effects.add(new FilterWrapper(text, filter, () -> check.isSelected() ? 1 : null, 0, 0));
    }


    public void addEffect(String text, EffectValue filter, int min, int max) {
        JPanel panel = new JPanel();
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        JLabel label = new JLabel(text);
        panel.add(label);
        JSlider slider = new JSlider(min, max);
        slider.setMajorTickSpacing(10);
        slider.setSnapToTicks(true);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setValue(0);
        slider.addChangeListener(_ -> imagePanel.refresh());
        panel.add(slider);
        effectsPanel.add(panel);
        effects.add(new FilterWrapper(text, filter, slider::getValue, min, max));
    }

    public void addOperation(String text, Operation operation) {
        JButton button = new JButton(text);
        button.addActionListener(_ -> {
            int[][] newImage = operation.run(image, this);
            if (newImage != null)
                image = newImage;
            imagePanel.clearSelection();
            imagePanel.refresh();
        });
        operationsPanel.add(button);
        operations.add(new OperationWrapper(text, operation));
    }

    private record OperationWrapper(String text, Operation operation) { }
}
