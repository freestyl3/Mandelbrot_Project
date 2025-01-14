package ru.tyaguschev.presentation;

import ru.tyaguschev.data.Repository;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class FractalFrame extends JFrame {
    private final ViewModel viewModel = new ViewModel(new Repository.Base());
    private BufferedImage innerImage;
    private final AreaSelector selector = new AreaSelector(1920, 1024);
    private final JPanel panel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (innerImage != null) {
                int windowWidth = getWidth();
                int windowHeight = getHeight();

                int imageWidth = innerImage.getWidth();
                int imageHeight = innerImage.getHeight();

                int x = (windowWidth - imageWidth) / 2;
                int y = (windowHeight - imageHeight) / 2;

                g.drawImage(innerImage, x, y, this);
            }
        }
    };

    public FractalFrame() {
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
        saveMenu.add(saveAsImage);
        saveMenu.add(saveAsFile);
        fileMenu.add(openFile);
        saveAsImage.addActionListener(_ -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showSaveDialog(null);
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".png")) {
                file = new File(file.getAbsolutePath() + ".png");
            }
            viewModel.saveImage(file.getAbsolutePath(), innerImage);
        });
        saveAsFile.addActionListener(_ -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showSaveDialog(null);
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".fractalo")) {
                file = new File(file.getAbsolutePath() + ".fractalo");
            }
            viewModel.saveSettings(file.getAbsolutePath());
        });

        openFile.addActionListener(_ -> {
            JFileChooser fileChooser = new JFileChooser();

            FileNameExtensionFilter filter = new FileNameExtensionFilter("My Format Files (*.fractalo)", "fractalo");
            fileChooser.setFileFilter(filter);

            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                if (file.getName().toLowerCase().endsWith(".fractalo")) {
                    viewModel.openFile(
                            panel.getHeight(),
                            panel.getWidth(),
                            file.getAbsolutePath()
                    );
                } else {
                    JOptionPane.showMessageDialog(this, "Неверный формат файла!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }

        });

        setMinimumSize(new Dimension(500, 500));

        setJMenuBar(menuBar);

        JMenuItem undo = new JMenuItem("Undo");
        actionsMenu.add(undo);
        undo.addActionListener(_ -> viewModel.goBack(panel.getHeight(), panel.getWidth()));


        InputMap inputMap = panel.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = panel.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("control Z"), "Undo");

        actionMap.put("Undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewModel.goBack(panel.getHeight(), panel.getWidth());
            }
        });

        add(panel);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton() == MouseEvent.BUTTON3) {
                    var point = e.getPoint();
                    var newPoint = new Point((double) point.x, (double) point.y);
                    viewModel.move(
                            panel.getHeight(),
                            panel.getWidth(),
                            newPoint
                    );
                }
            }
        });


        selector.setColor(Color.BLUE);

        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);

                var width = panel.getWidth();
                var height = panel.getHeight();

                selector.setGraphics(panel.getGraphics());
                selector.updateCoordinates(width, height);

                if (innerImage.getHeight() <= panel.getHeight() ||
                        innerImage.getWidth() <= panel.getWidth())
                    viewModel.print(panel.getHeight(), panel.getWidth());

            }
        });



        panel.addMouseListener(new MouseAdapter() {
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

                    viewModel.zoom(
                            panel.getHeight(),
                            panel.getWidth(),
                            rect
                    );
                }
                selector.clearSelection();
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                selector.paint();
                selector.addPoint(e.getPoint());
                selector.paint();
            }
        });

        viewModel.init(
                panel.getHeight(),
                panel.getWidth(),
                image -> {
                    innerImage = image;
                    panel.repaint();
                }
        );
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
}
