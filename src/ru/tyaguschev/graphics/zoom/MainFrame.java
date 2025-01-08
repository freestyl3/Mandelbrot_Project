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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainFrame extends JFrame {
    private final int WIDTH = 617;
    private final int HEIGHT = 663;
    private final FractalPainter fPainter = new FractalPainter(-2.0, 1.0, -1.5, 1.5);
    private final JPanel mainPanel = new JPanel() {
        @Override
        public void paint(Graphics g) {
            fPainter.paint(g);
        }
    };
    private final AreaSelector selector = new AreaSelector(WIDTH - 16, HEIGHT - 62);
    private final ArrayList<ArrayList<Double>> coordinates= new ArrayList<>();
    private double ratio = 1.0;

    public MainFrame() {
        this.coordinates.add(new ArrayList<>(List.of(-2.0, 1.0, -1.5, 1.5)));
        mainPanel.setBackground(Color.WHITE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        add(mainPanel);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu actionsMenu = new JMenu("Actions");

        menuBar.add(fileMenu);
        menuBar.add(actionsMenu);

        JMenu saveMenu = new JMenu("Save");
        JMenuItem saveAsImage = new JMenuItem("As image");
        JMenuItem saveAsFile = new JMenuItem("As file");
        JMenuItem openFile = new JMenuItem("Open file");

        fileMenu.add(saveMenu);
        fileMenu.add(openFile);
        saveMenu.add(saveAsImage);
        saveMenu.add(saveAsFile);

        JMenuItem undo = new JMenuItem("Undo");
        actionsMenu.add(undo);


        saveAsImage.addActionListener(_ -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showSaveDialog(null);
            File file = fileChooser.getSelectedFile();
            saveImage(file.getAbsolutePath());
        });
        saveAsFile.addActionListener(_ -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showSaveDialog(null);
            File file = fileChooser.getSelectedFile();
            saveFile(file.getAbsolutePath());
        });
        openFile.addActionListener(_ -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showOpenDialog(null);
            File file = fileChooser.getSelectedFile();
            openFile(file.getAbsolutePath());
        });
        undo.addActionListener(_ -> undo());

        this.setJMenuBar(menuBar);

        selector.setColor(Color.BLUE);

        mainPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                var width = mainPanel.getWidth();
                var height = mainPanel.getHeight();
                ratio = (double) width / (double) height;
                selector.setGraphics(mainPanel.getGraphics());
                selector.updateCoordinates(width, height);

//                Converter converter = fPainter.getConverter();


                var curCords = coordinates.getLast();
                var xMin = curCords.get(0);
                var xMax = curCords.get(1);
                var yMax = curCords.get(2);
                var yMin = curCords.get(3);

                System.out.println(ratio);
                System.out.println(fPainter.getConverter().getYMax());
                System.out.println();


//                coordinates.removeLast();
//                xMin = fPainter.getConverter().getXMin();
//                xMax = fPainter.getConverter().getXMax();
//                yMin = fPainter.getConverter().getYMin();
//                yMax = fPainter.getConverter().getYMax();

//                coordinates.add(new ArrayList<>(List.of(xMin, xMax, yMin, yMax)));
//                fPainter.updateCoordinates(xMin, xMax, yMin, yMax, ratio);

                fPainter.saveAspectRatio(xMin, xMax, yMin, yMax);
                fPainter.updateRatio(ratio);
                fPainter.updateCoordinates(xMin, xMax, yMin, yMax);

                fPainter.setWidth(width);
                fPainter.setHeight(height);
//                System.out.println(fPainter.getConverter().getYMin());
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
                    var newCoordinates = new ArrayList<>(List.of(xMin, xMax, yMax * ratio, yMin * ratio));
                    if (!newCoordinates.equals(coordinates.getLast()))
                        coordinates.add(newCoordinates);
//                    printCoordinates();
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

    public void saveFile(String fileName) {
        try {
            File outputFile = new File(fileName);
            FileWriter fileWriter = new FileWriter(outputFile.getAbsolutePath());
            var curCords = this.coordinates.getLast();
            for (Double curCord : curCords) fileWriter.write(curCord + "\n");
            fileWriter.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void openFile(String fileName) {
        try {
            File file = new File(fileName);
            Scanner scanner = new Scanner(file);
            var newCords = new ArrayList<Double>();
//            var i = 0;
            while (scanner.hasNextLine()) {
                newCords.add(Double.parseDouble(scanner.nextLine()));
//                System.out.println(newCords.get(i));
//                i++;
            }
            var xMin = newCords.get(0);
            var xMax = newCords.get(1);
            var yMax = newCords.get(2);
            var yMin = newCords.get(3);
            this.coordinates.removeAll(this.coordinates);
            this.coordinates.add(newCords);
//            printCoordinates();
            fPainter.updateCoordinates(xMin, xMax, yMin, yMax);
            mainPanel.repaint();
//            System.out.println(newCords.size());
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void undo() {
        if (coordinates.size() > 1)
            this.coordinates.removeLast();
        var lastCoordinates = this.coordinates.getLast();
//        System.out.println(lastCoordinates);
        var xMin = lastCoordinates.get(0);
        var xMax = lastCoordinates.get(1);
        var yMax = lastCoordinates.get(2);
        var yMin = lastCoordinates.get(3);
        fPainter.updateCoordinates(xMin, xMax, yMin, yMax);
        mainPanel.repaint();
    }

    public void printCoordinates() {
        for (int i = 0; i < this.coordinates.size() - 1; i++) {
            System.out.print(this.coordinates.get(i) + ", ");
        }
        System.out.println(this.coordinates.getLast());
    }

    public void keyBindings() {
        String keyStrokeAndKey = "control z";
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeAndKey);
    }
}