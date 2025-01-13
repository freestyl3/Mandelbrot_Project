package ru.tyaguschev.graphics.zoom;

import ru.tyaguschev.gui.Palette.ColorPalette;
import ru.tyaguschev.gui.Palette.CosinusalPalette;
import ru.tyaguschev.gui.FractalPainter;
import ru.tyaguschev.gui.Palette.LinearPalette;
import ru.tyaguschev.gui.Palette.SinusalPalette;
import ru.tyaguschev.gui.Rect;
import ru.tyaguschev.math.Pair.Pair;
import ru.tyaguschev.math.converter.Converter;
import ru.tyaguschev.math.coordinates.Coordinates;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class MainFrame extends JFrame {
    private final int WIDTH = 617;
    private final int HEIGHT = 663;
    private BufferedImage image;
    private final AreaSelector selector = new AreaSelector(WIDTH - 16, HEIGHT - 62);
    private final ArrayList<Coordinates> coordinates= new ArrayList<>();
    private double ratio = 1.0;
    private final FractalPainter fPainter = new FractalPainter(new Coordinates(-2.0, 1.0, -1.5, 1.5),
            new CosinusalPalette(2, 3, 5), WIDTH - 14, HEIGHT - 60);
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


    public MainFrame() {
        this.coordinates.add(new Coordinates(-2.0, 1.0, 1.5, -1.5));
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
                var xMin = curCords.xMin;
                var xMax = curCords.xMax;
                var yMax = curCords.yMax;
                var yMin = curCords.yMin;

                fPainter.saveAspectRatio(new Coordinates(xMin, xMax, yMin, yMax), ratio);

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
                var deltaX = Math.abs(lastCords.xMin - lastCords.xMax) / 2;
                var deltaY = Math.abs(lastCords.yMin - lastCords.yMax) / 2;

                var xMin = xCenter - deltaX;
                var xMax = xCenter + deltaX;
                var yMin = yCenter - deltaY;
                var yMax = yCenter + deltaY;

                var newCoordinates = new Coordinates(xMin, xMax, yMax, yMin);
                if (!newCoordinates.equals(coordinates.getLast()))
                    coordinates.add(fPainter.updateCoordinates(newCoordinates));

                image = fPainter.createImage();
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
                    var newCoordinates = new Coordinates(xMin, xMax, yMax, yMin);
                    if (!newCoordinates.equals(coordinates.getLast()))
                        coordinates.add(fPainter.updateCoordinates(new Coordinates(xMin, xMax, yMin, yMax)));
                    image = fPainter.createImage();
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
        mainPanel.getActionMap().put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undo();
            }
        });
        mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl Z"), "undo");
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
            fileWriter.write(curCords.xMin + "\n" +
                                 curCords.xMax + "\n" +
                                 curCords.yMin + "\n" +
                                 curCords.yMax + "\n");
            fileWriter.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void openFile(String fileName) {
        try {
            File file = new File(fileName);
            Scanner scanner = new Scanner(file);
            var cords = new ArrayList<Double>();
            while (scanner.hasNextLine()) {
                cords.add(Double.parseDouble(scanner.nextLine()));
            }
            var xMin = cords.get(0);
            var xMax = cords.get(1);
            var yMax = cords.get(2);
            var yMin = cords.get(3);
            this.coordinates.removeAll(this.coordinates);

            Coordinates newCords = new Coordinates(xMin, xMax, yMin, yMax);

            this.coordinates.add(newCords);
            fPainter.updateCoordinates(newCords);
            image = fPainter.createImage();

            mainPanel.repaint();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void undo() {
        if (coordinates.size() > 1)
            this.coordinates.removeLast();
        fPainter.saveAspectRatio(this.coordinates.getLast(), this.ratio);
        image = fPainter.createImage();
        mainPanel.repaint();
    }

    private void createGif(String fileName, int steps) throws InterruptedException {
        var interpolated = interpolateCoordinates(steps);

        for (int i = 0; i < interpolated.size(); i++) {
            fPainter.saveAspectRatio(interpolated.get(i), this.ratio);
            image = fPainter.createImage();

            mainPanel.repaint();
            var img = getScreenShot();
            saveImage(fileName + i + ".png");
        }
    }

    private ArrayList<Coordinates> interpolateCoordinates(int steps) {
        ArrayList<Coordinates> interpolated = new ArrayList<>();
        for (int i = 1; i < this.coordinates.size(); i++) {
            double xMinStep = (this.coordinates.get(i - 1).xMin - this.coordinates.get(i).xMin) / steps;
            double xMaxStep = (this.coordinates.get(i - 1).xMax - this.coordinates.get(i).xMax) / steps;
            double yMinStep = (this.coordinates.get(i - 1).yMax - this.coordinates.get(i).yMax) / steps;
            double yMaxStep = (this.coordinates.get(i - 1).yMin - this.coordinates.get(i).yMin) / steps;
            for (int j = 0; j < steps; j++) {
                interpolated.add(new Coordinates(
                        this.coordinates.get(i - 1).xMin - j * xMinStep,
                        this.coordinates.get(i - 1).xMax - j * xMaxStep,
                        this.coordinates.get(i - 1).yMax - j * yMinStep,
                        this.coordinates.get(i - 1).yMin - j * yMaxStep
                ));
            }
        }
        interpolated.add(new Coordinates(
                this.coordinates.getLast().xMin,
                this.coordinates.getLast().xMax,
                this.coordinates.getLast().yMax,
                this.coordinates.getLast().yMin
        ));
        return interpolated;
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

        JMenuItem cosPalette = new JMenu("Cosinusal palette");
        JMenuItem cos1 = new JMenuItem("Palette 1");
        JMenuItem cos2 = new JMenuItem("Palette 2");
        JMenuItem cos3 = new JMenuItem("Palette 3");
        JMenuItem sinPalette = new JMenu("Sinusal palette");
        JMenuItem sin1 = new JMenuItem("Palette 1");
        JMenuItem sin2 = new JMenuItem("Palette 2");
        JMenuItem sin3 = new JMenuItem("Palette 3");
        JMenuItem linearPalette = new JMenu("Linear palette");
        JMenuItem lin1 = new JMenuItem("Palette 1");
        JMenuItem lin2 = new JMenuItem("Palette 2");
        JMenuItem lin3 = new JMenuItem("Palette 3");

        palette.add(cosPalette);
        cosPalette.add(cos1);
        cosPalette.add(cos2);
        cosPalette.add(cos3);
        palette.add(sinPalette);
        sinPalette.add(sin1);
        sinPalette.add(sin2);
        sinPalette.add(sin3);
        palette.add(linearPalette);
        linearPalette.add(lin1);
        linearPalette.add(lin2);
        linearPalette.add(lin3);

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
                createGif(file.getAbsolutePath(), 10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        cos1.addActionListener(_ -> setPalette(new CosinusalPalette(2, 3, 5)));
        cos2.addActionListener(_ -> setPalette(new CosinusalPalette(3, 2, 5)));
        cos3.addActionListener(_ -> setPalette(new CosinusalPalette(3, 4, 5)));
        sin1.addActionListener(_ -> setPalette(new SinusalPalette(2, 3, 5)));
        sin2.addActionListener(_ -> setPalette(new SinusalPalette(1, 2, 2)));
        sin3.addActionListener(_ -> setPalette(new SinusalPalette(1, 3, 2)));
        lin1.addActionListener(_ -> setPalette(new LinearPalette(
                new Pair(-220.022, 250.022), new Pair(-223.0223, 229.0223), new Pair(57.0057, 23.9943))));
        lin2.addActionListener(_ -> setPalette(new LinearPalette(
                new Pair(196.0196, 17.9804), new Pair(90.009, 41.991), new Pair(84.0084, 15.9916))));
        lin3.addActionListener(_ -> setPalette(new LinearPalette(
                new Pair(-103.0103, 162.0103), new Pair(164.0164, 24.9836), new Pair(205.0205, 6.9795))));

        this.setJMenuBar(menuBar);
    }

    private void setPalette(ColorPalette palette) {
        fPainter.setPalette(palette);
        image = fPainter.createImage();

        mainPanel.repaint();
    }
}