package io.fair_acc.chartfx.samples;

import io.fair_acc.chartfx.Chart;
import io.fair_acc.chartfx.legend.spi.DefaultLegend;
import io.fair_acc.chartfx.plugins.ChartPlugin;
import io.fair_acc.chartfx.renderer.Renderer;
import io.fair_acc.dataset.DataSet;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * A custom plugin that displays a draggable legend on the chart.
 * The legend is anchored to the top-right corner by default, and maintains its relative position
 * to the right edge when the chart is resized.
 */
public class DraggableLegendPlugin extends ChartPlugin {
    private final DefaultLegend legend = new DefaultLegend();
    private final StackPane container = new StackPane(legend);
    private boolean isDragging = false;
    private double startX, startY;

    // Relative positioning offsets. 
    // Defaults to 30px from right and 30px from top.
    private double rightOffset = 30.0;
    private double topOffset = 30.0;

    public DraggableLegendPlugin() {
        // Matlab style: white background, black border
        container.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1px; -fx-border-style: solid; -fx-padding: 5px;");

        // Use Horizontal FlowPane with small wrap length to simulate vertical list with auto-height
        legend.setVertical(false);
        legend.setPrefWrapLength(10);

        // Ensure legend background is transparent to show container's background
        legend.setStyle("-fx-background-color: transparent;");

        getChartChildren().add(container);
        makeDraggable(container);

        // Listen to Chart property changes to bind data
        chartProperty().addListener((obs, oldChart, newChart) -> {
            if (newChart != null) {
                bindChart(newChart);
            }
        });

        // Listen to container size changes to reposition
        container.widthProperty().addListener((obs, o, n) -> reposition());
        container.heightProperty().addListener((obs, o, n) -> reposition());
    }

    private void bindChart(Chart chart) {
        updateLegend(chart); // Initial update

        ListChangeListener<DataSet> dsListener = c -> updateLegend(chart);
        chart.getDatasets().addListener(dsListener);

        ListChangeListener<Renderer> rListener = c -> updateLegend(chart);
        chart.getRenderers().addListener(rListener);
    }

    private void updateLegend(Chart chart) {
        legend.updateLegend(chart.getDatasets(), chart.getRenderers());
        // Update size and reposition
        container.requestLayout();
        Platform.runLater(this::reposition);
    }

    private void reposition() {
        if (getChart() == null) return;

        // Force autosize as Group doesn't resize children
        container.autosize();

        double chartWidth = getChart().getCanvas().getWidth();
        double legendWidth = container.getWidth();

        if (chartWidth > 0 && legendWidth > 0) {
            // Anchor to right edge using rightOffset
            container.setLayoutX(chartWidth - legendWidth - rightOffset);
            container.setLayoutY(topOffset);
        }
    }

    private void makeDraggable(Region node) {
        final Delta dragDelta = new Delta();
        node.setOnMousePressed(mouseEvent -> {
            dragDelta.x = node.getLayoutX() - mouseEvent.getSceneX();
            dragDelta.y = node.getLayoutY() - mouseEvent.getSceneY();
            startX = mouseEvent.getSceneX();
            startY = mouseEvent.getSceneY();
            isDragging = false;
            node.setCursor(Cursor.MOVE);
            mouseEvent.consume();
        });

        node.setOnMouseReleased(mouseEvent -> {
            node.setCursor(Cursor.HAND);
            // Recalculate offsets based on new position
            if (getChart() != null) {
                double chartWidth = getChart().getCanvas().getWidth();
                double legendWidth = node.getWidth();
                // Update rightOffset so it stays anchored to the right at this new distance
                rightOffset = chartWidth - (node.getLayoutX() + legendWidth);
                topOffset = node.getLayoutY();
            }
        });

        node.setOnMouseDragged(mouseEvent -> {
            if (Math.abs(mouseEvent.getSceneX() - startX) > 5 || Math.abs(mouseEvent.getSceneY() - startY) > 5) {
                isDragging = true;
            }
            node.setLayoutX(mouseEvent.getSceneX() + dragDelta.x);
            node.setLayoutY(mouseEvent.getSceneY() + dragDelta.y);
            mouseEvent.consume();
        });

        node.setOnMouseEntered(mouseEvent -> node.setCursor(Cursor.HAND));

        // Filter click events to prevent accidental clicks during drag
        node.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (isDragging) {
                event.consume();
                isDragging = false;
            }
        });
    }

    @Override
    public void layoutChildren() {
        super.layoutChildren();
        reposition();
    }

    private static class Delta {
        double x, y;
    }
}
