package pt.iscte.greyditor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EditorWindow implements Editor {
    private static final int VALUE_OFF = new Color(255, 0, 0).getRGB();
    private static final Color[] PALETTE_COLOR = new Color[256];
    private static final int[] PALETTE = new int[256];
    private static final int PADDING = 20;

    static {
        for (int i = 0; i < 256; i++) {
            PALETTE_COLOR[i] = new Color(i, i, i);
            PALETTE[i] = PALETTE_COLOR[i].getRGB();
        }
    }

    private Greyditor editor;
    private int[][] image;
    private final JFrame frame;
    private final ImagePanel imagePanel;
    private final JLabel sizeLabel;
    private final JLabel pointLabel;
    private final JLabel toneLabel;

    private final Map<String, EffectMinMax> effects;
    private final Map<Effect, Supplier<Integer>> effectsSupplier;

    private final Map<String, Operation> operations;

    record EffectMinMax(Effect effect, int min, int max) {
    }

    public EditorWindow(Greyditor editor, String title, Map<String, EffectMinMax> effects, Map<String, Operation> operations) {
        this.editor = editor;
        this.effects = effects;
        this.operations = operations;
        effectsSupplier = new HashMap<>();

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

        JPanel toolsPanel = new JPanel();
        toolsPanel.setLayout(new BoxLayout(toolsPanel, BoxLayout.Y_AXIS));
        toolsPanel.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        frame.add(toolsPanel, BorderLayout.EAST);

        addEffects(toolsPanel, effects);
        addOperations(toolsPanel, operations);
    }


    private void addEffects(JPanel toolsPanel, Map<String, EffectMinMax> effects) {

        for (Map.Entry<String, EffectMinMax> e : effects.entrySet()) {
            EffectMinMax r = e.getValue();
            Effect effect = r.effect;
            if (effect instanceof EffectSimple) {
                JCheckBox check = new JCheckBox(e.getKey());
                check.setAlignmentX(Component.LEFT_ALIGNMENT);
                check.addItemListener(ev -> {
                    imagePanel.refresh();
                });
                toolsPanel.add(check);
                effectsSupplier.put(effect, () -> check.isSelected() ? 1 : null);
            } else if (effect instanceof EffectValue) {
                JPanel panel = new JPanel();
                panel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
                JLabel label = new JLabel(e.getKey());
                panel.add(label);
                JSlider slider = new JSlider(r.min, r.max);
                slider.setMajorTickSpacing(10);
                slider.setSnapToTicks(true);
                slider.setMinorTickSpacing(1);
                slider.setPaintTicks(true);
                slider.setValue(0);
                slider.addChangeListener(_ -> imagePanel.refresh());
                panel.add(slider);
                toolsPanel.add(panel);
                effectsSupplier.put(effect, () -> slider.getValue());
            }
        }
    }

    private void addOperations(JPanel toolsPanel, Map<String, Operation> effects) {
        for (Map.Entry<String, Operation> e : operations.entrySet()) {
            JButton button = new JButton(e.getKey());
            button.addActionListener(_ -> {
                int[][] newImage = e.getValue().run(image, this);
                if (newImage != null)
                    image = newImage;
                imagePanel.clearSelection();
                imagePanel.refresh();
            });
            toolsPanel.add(button);
        }
    }

    JFrame newWindow(int[][] image) {
        if (isWellFormed(image)) {
            this.image = image;
            imagePanel.refresh();
            frame.pack();
            frame.setResizable(false);
            frame.setVisible(true);
            return frame;
        }
        else
            return null;
    }


    @Override
    public void open(int[][] image) {
        editor.open(image);
    }

    @Override
    public int[][] getImage() {
        return applyEffects(image);
    }

    private boolean isWellFormed(int[][] matrix) {
        for (int i = 0; i < matrix.length - 1; i++)
            if (matrix[i] == null || matrix[i + 1] == null || matrix[i].length != matrix[i + 1].length)
                return false;

        return true;
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
                    if (p.x >= PADDING && p.y >= PADDING &&
                            p.x < localImg[0].length + PADDING && p.y < localImg.length + PADDING) {
                        int x = p.x - PADDING;
                        int y = p.y - PADDING;
                        pointLabel.setText("x: " + x + "  y: " + y);
                        toneLabel.setBackground(PALETTE_COLOR[localImg[y][x]]);
                        int fg = localImg[y][x] < 128 ? 255 : 0;
                        toneLabel.setForeground(PALETTE_COLOR[fg]);
                        toneLabel.setText("tone: " + localImg[y][x]);
                    } else {
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
            localImg = applyEffects(image);
            setPreferredSize(new Dimension(this.localImg[0].length + PADDING * 2, this.localImg.length + PADDING * 2));
            repaint();
            sizeLabel.setText(localImg[0].length + " x " + localImg.length);
        }

        public void clearSelection() {
            from = null;
            to = null;
        }
    }


    private int[][] applyEffects(int[][] image) {
        int[][] copy = deepCopy(image);
        for (Map.Entry<String, EffectMinMax> f : effects.entrySet()) {
            EffectMinMax e = f.getValue();
            Supplier<Integer> sup = effectsSupplier.get(e.effect);
            if (e.effect instanceof EffectSimple && sup.get() != null)
                ((EffectSimple) e.effect).apply(copy);
            else if (e.effect instanceof EffectValue)
                ((EffectValue) e.effect).apply(copy, sup.get());
        }

        return copy;
    }

    private static int[][] deepCopy(int[][] original) {
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = new int[original[i].length];
            System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
        }
        return copy;
    }

    static BufferedImage matrixToImage(int[][] matrix) {
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


    private boolean valid(int tone) {
        return tone >= 0 && tone <= 255;
    }
}
