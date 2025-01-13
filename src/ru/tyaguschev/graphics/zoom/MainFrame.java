package ru.tyaguschev.graphics.zoom;

import ru.tyaguschev.gui.Palette.CosinusalPalette;
import ru.tyaguschev.gui.FractalPainter;
import ru.tyaguschev.gui.Palette.SinusalPalette;
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
    private BufferedImage image;
    private final FractalPainter fPainter = new FractalPainter(-2.0, 1.0, -1.5, 1.5,
            new CosinusalPalette(255, 255, 255));
    private final JPanel mainPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                int windowWidth = getWidth();
                int windowHeight = getHeight();

                int imageWidth = image.getWidth();
                int imageHeight = image.getHeight();

                int x = (windowWidth - imageWidth) / 2;
                int y = (windowHeight - imageHeight) / 2;

                g.drawImage(image, x, y, this);
            }
        }
    };
    private final AreaSelector selector = new AreaSelector(WIDTH - 16, HEIGHT - 62);
    private final ArrayList<ArrayList<Double>> coordinates= new ArrayList<>();
    private double ratio = 1.0;
    private final ArrayList<BufferedImage> images = new ArrayList<>();

    public MainFrame() {
        this.coordinates.add(new ArrayList<>(List.of(-2.0, 1.0, -1.5, 1.5)));
        mainPanel.setBackground(Color.WHITE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        add(mainPanel);

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

                var curCords = coordinates.getLast();
                var xMin = curCords.get(0);
                var xMax = curCords.get(1);
                var yMax = curCords.get(2);
                var yMin = curCords.get(3);
                fPainter.saveAspectRatio(xMin, xMax, yMin, yMax, ratio);

                fPainter.setWidth(width);
                fPainter.setHeight(height);

                image = fPainter.createImage();
                mainPanel.repaint();

            }
        });

        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                Converter converter = fPainter.getConverter();
                Point center = new Point(e.getPoint());
                var xCenter = converter.xScreenToCartesian(center.x);
                var yCenter = converter.yScreenToCartesian(center.y);
                var lastCords = coordinates.getLast();
                var deltaX = Math.abs(lastCords.get(0) - lastCords.get(1)) / 2;
                var deltaY = Math.abs(lastCords.get(2) - lastCords.get(3)) / 2;

                var xMin = xCenter - deltaX;
                var xMax = xCenter + deltaX;
                var yMin = yCenter - deltaY;
                var yMax = yCenter + deltaY;

                var newCoordinates = new ArrayList<>(List.of(xMin, xMax, yMax, yMin));
                if (!newCoordinates.equals(coordinates.getLast()))
                    coordinates.add(fPainter.updateCoordinates(xMin, xMax, yMin, yMax));

                image = fPainter.createImage();
                images.add(image);
                mainPanel.repaint();
            }

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
                    var newCoordinates = new ArrayList<>(List.of(xMin, xMax, yMax, yMin));
                    if (!newCoordinates.equals(coordinates.getLast()))
                        coordinates.add(fPainter.updateCoordinates(xMin, xMax, yMin, yMax));
//                    fPainter.updateCoordinates(xMin, xMax, yMin, yMax);
//                    printCoordinates();
                    image = fPainter.createImage();
                    images.add(image);
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
        image = fPainter.createImage();
        images.add(image);
        mainPanel.repaint();
    }

    private BufferedImage getScreenShot() {
        BufferedImage img = new BufferedImage(mainPanel.getWidth(), mainPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
        mainPanel.printAll(img.getGraphics());
        return img;
    }

    private void saveImage(String fileName) {
        var screenShot = this.getScreenShot();
        try {
            File outputFile = new File(fileName);
            ImageIO.write(screenShot, "png", outputFile);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void saveFile(String fileName) {
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

    private void openFile(String fileName) {
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
            image = fPainter.createImage();

            mainPanel.repaint();
//            System.out.println(newCords.size());
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void undo() {
        if (coordinates.size() > 1)
            this.coordinates.removeLast();
        var lastCoordinates = this.coordinates.getLast();
        var xMin = lastCoordinates.get(0);
        var xMax = lastCoordinates.get(1);
        var yMax = lastCoordinates.get(2);
        var yMin = lastCoordinates.get(3);
        fPainter.saveAspectRatio(xMin, xMax, yMin, yMax, this.ratio);
        image = fPainter.createImage();
//        image = images.removeLast();
        System.out.println(image);

        mainPanel.repaint();
    }

    private void createGif(String fileName, int steps) throws InterruptedException {
        var interpolated = interpolateCoordinates(steps);

        for (int i = 0; i < interpolated.size(); i++) {
            var curCoordinates = interpolated.get(i);
//        System.out.println(lastCoordinates);
            var xMin = curCoordinates.get(0);
            var xMax = curCoordinates.get(1);
            var yMax = curCoordinates.get(2);
            var yMin = curCoordinates.get(3);
            fPainter.saveAspectRatio(xMin, xMax, yMin, yMax, this.ratio);
            image = fPainter.createImage();

            mainPanel.repaint();
//            TimeUnit.MILLISECONDS.sleep(1000);
//            var image = getScreenShot();
//            saveImage(fileName + i + ".jpg");
        }
    }

    private ArrayList<ArrayList<Double>> interpolateCoordinates(int steps) {
        ArrayList<ArrayList<Double>> interpolated = new ArrayList<>();
        for (int i = 1; i < this.coordinates.size(); i++) {
            double xMinStep = (this.coordinates.get(i - 1).get(0) - this.coordinates.get(i).get(0)) / steps;
            double xMaxStep = (this.coordinates.get(i - 1).get(1) - this.coordinates.get(i).get(1)) / steps;
            double yMinStep = (this.coordinates.get(i - 1).get(2) - this.coordinates.get(i).get(2)) / steps;
            double yMaxStep = (this.coordinates.get(i - 1).get(3) - this.coordinates.get(i).get(3)) / steps;
//            System.out.println(xMinStep + ", " + xMaxStep + ", " + yMinStep + ", " + yMaxStep);
            for (int j = 0; j < steps; j++) {
                interpolated.add(new ArrayList<>(List.of(
                        this.coordinates.get(i - 1).get(0) - j * xMinStep,
                        this.coordinates.get(i - 1).get(1) - j * xMaxStep,
                        this.coordinates.get(i - 1).get(2) - j * yMinStep,
                        this.coordinates.get(i - 1).get(3) - j * yMaxStep
                )));
            }

//            printCoordinates();
        }
        interpolated.add(new ArrayList<>(List.of(
                this.coordinates.getLast().get(0),
                this.coordinates.getLast().get(1),
                this.coordinates.getLast().get(2),
                this.coordinates.getLast().get(3)
        )));

//        for (int i = 0; i < interpolated.size(); i++) {
//            System.out.println(interpolated.get(i));
//        }
        return interpolated;
    }

    private void printCoordinates() {
        for (int i = 0; i < this.coordinates.size() - 1; i++) {
            System.out.print(this.coordinates.get(i) + ", ");
        }
        var lastCords = this.coordinates.getLast();
        System.out.println(lastCords);
//        System.out.println((lastCords.get(1) - lastCords.get(0)) + ", " + (lastCords.get(2) - lastCords.get(3)) + ", " + this.ratio);
    }

    public void setMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu actionsMenu = new JMenu("Actions");

        menuBar.add(fileMenu);
        menuBar.add(actionsMenu);

        JMenu saveMenu = new JMenu("Save");
        JMenuItem saveAsImage = new JMenuItem("As image");
        JMenuItem saveAsFile = new JMenuItem("As file");
        JMenuItem saveAsGif = new JMenuItem("As GIF");
        JMenuItem openFile = new JMenuItem("Open file");

        fileMenu.add(saveMenu);
        saveMenu.add(saveAsImage);
        saveMenu.add(saveAsFile);
        saveMenu.add(saveAsGif);
        fileMenu.add(openFile);

        JMenuItem undo = new JMenuItem("Undo");
        JMenu palette = new JMenu("Select palette");
        actionsMenu.add(undo);
        actionsMenu.add(palette);

        JMenuItem cosPalette = new JMenuItem("Cosinusal palette");
        JMenuItem sinPalette = new JMenuItem("Sinusal palette");
        JMenuItem linearPalette = new JMenuItem("Linear palette");

        palette.add(cosPalette);
        palette.add(sinPalette);
        palette.add(linearPalette);


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
        saveAsGif.addActionListener(_ -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showSaveDialog(null);
            File file = fileChooser.getSelectedFile();
            try {
                createGif(file.getAbsolutePath(), 50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        cosPalette.addActionListener(_ -> {
            fPainter.setPalette(new CosinusalPalette(255, 255, 255));
            image = fPainter.createImage();

            mainPanel.repaint();
        });
        sinPalette.addActionListener(_ -> {
            fPainter.setPalette(new SinusalPalette(255, 255, 255));
            image = fPainter.createImage();

            mainPanel.repaint();
        });


        this.setJMenuBar(menuBar);
    }



    private void keyBindings() {
        String keyStrokeAndKey = "control z";
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyStrokeAndKey);
    }
}