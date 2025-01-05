package ru.tyaguschev.graphics.zoom;

import ru.tyaguschev.gui.FractalPainter;
import ru.tyaguschev.gui.Rect;
import ru.tyaguschev.math.converter.Converter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainFrame extends JFrame {
    private final FractalPainter fPainter = new FractalPainter(-2.0, 1.0, -1.5, 1.5);
    private final JPanel mainPanel = new JPanel() {
        @Override
        public void paint(Graphics g) {
            fPainter.paint(g);
        }
    };
    private final AreaSelector selector = new AreaSelector();

    public MainFrame() {
        mainPanel.setBackground(Color.WHITE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 600));
        add(mainPanel);

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
}