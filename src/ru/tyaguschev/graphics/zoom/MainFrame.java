package ru.tyaguschev.graphics.zoom;

import ru.tyaguschev.gui.FractalPainter;
import ru.tyaguschev.gui.Rect;
import ru.tyaguschev.math.converter.Converter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainFrame extends JFrame {
    private final int WIDTH = 617;
    private final int HEIGHT = 640;
    private final FractalPainter fPainter = new FractalPainter(-2.0, 1.0, -1.5, 1.5);
    private final JPanel mainPanel = new JPanel() {
        @Override
        public void paint(Graphics g) {
            fPainter.paint(g);
        }
    };
    private final AreaSelector selector = new AreaSelector(WIDTH - 16, HEIGHT - 39);

    public MainFrame() {
        mainPanel.setBackground(Color.WHITE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        add(mainPanel);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        JMenuItem saveAsImage = new JMenuItem("Save");
        fileMenu.add(saveAsImage);


        saveAsImage.addActionListener(_ -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showSaveDialog(null);
            File file = fileChooser.getSelectedFile();
            saveImage(file.getAbsolutePath());
        });

//        this.setJMenuBar(menuBar);

        selector.setColor(Color.BLUE);

        mainPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                selector.setGraphics(mainPanel.getGraphics());
                fPainter.setWidth(mainPanel.getWidth());
                fPainter.setHeight(mainPanel.getHeight());
            }
        });

        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                selector.addPoint(e.getPoint());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                selector.paint();
                Rect rect = selector.getRect();
                if (rect != null) {
                    Converter converter = fPainter.getConverter();
                    var xMin = converter.xScreenToCartesian(rect.getStartPoint().x);
                    var yMin = converter.yScreenToCartesian(rect.getStartPoint().y);
                    var xMax = converter.xScreenToCartesian(rect.getStartPoint().x + rect.getWidth());
                    var yMax = converter.yScreenToCartesian(rect.getStartPoint().y + rect.getHeigth());
//                    System.out.println(xMin + ", " + yMin + ", " + xMax + ", " + yMax);
                    fPainter.updateCoordinates(xMin, xMax, yMin, yMax);
                    mainPanel.repaint();
                }
                selector.clearSelection();
            }
        });
        mainPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                selector.paint();
                selector.addPoint(e.getPoint());
                selector.paint();
            }
        });
    }

    public BufferedImage getScreenShot() {
        BufferedImage img = new BufferedImage(mainPanel.getWidth(), mainPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
        mainPanel.printAll(img.getGraphics());
        return img;
    }

    public void saveImage(String fileName) {
        var screenShot = this.getScreenShot();
        try {
            File outputFile = new File(fileName);
            ImageIO.write(screenShot, "png", outputFile);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}