package io.fair_acc.chartfx.samples;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.collections.ListChangeListener;

import io.fair_acc.chartfx.Chart;
import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.legend.spi.DefaultLegend;
import io.fair_acc.chartfx.plugins.ChartPlugin;
import io.fair_acc.chartfx.plugins.CrosshairIndicator;
import io.fair_acc.chartfx.plugins.EditAxis;
import io.fair_acc.chartfx.plugins.Zoomer;
import io.fair_acc.chartfx.renderer.Renderer;
import io.fair_acc.dataset.DataSet;
import io.fair_acc.dataset.event.UpdatedDataEvent;
import io.fair_acc.dataset.spi.DoubleDataSet;

/**
 * Simple example of how to use chart class
 * 
 * @author rstein
 */
public class SimpleChartSample extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleChartSample.class);
    private static final int N_SAMPLES = 100; // default number of data points

    @Override
    public void start(final Stage primaryStage) {
        final DefaultNumericAxis yAxis = new DefaultNumericAxis();
        yAxis.setAutoRanging(true); // default: true
        yAxis.setAutoRangePadding(0.5); // here: 50% padding on top and bottom of axis

        final XYChart chart = new XYChart(new DefaultNumericAxis(), yAxis);
        chart.setLegendVisible(false); // 隐藏默认图例
        chart.setPadding(new Insets(10)); // 设置图表内边距
        chart.setChartBorderVisible(true); // 显示图表边框
        chart.getPlugins().addAll(new Zoomer(), new CrosshairIndicator(), new EditAxis(), new DraggableLegendPlugin()); // standard plugin, useful for most cases

        final DoubleDataSet dataSet1 = new DoubleDataSet("data set #1");
        final DoubleDataSet dataSet2 = new DoubleDataSet("data set #2");

        // some custom listeners (optional)
        dataSet1.addListener(evt -> LOGGER.atInfo().log("dataSet1 - event: " + evt.toString()));
        dataSet2.addListener(evt -> LOGGER.atInfo().log("dataSet2 - event: " + evt.toString()));

        // chart.getDatasets().add(dataSet1); // for single data set
        chart.getDatasets().addAll(dataSet1, dataSet2); // for two data sets

        final double[] xValues = new double[N_SAMPLES];
        final double[] yValues1 = new double[N_SAMPLES];
        dataSet2.autoNotification().set(false); // to suppress auto notification
        for (int n = 0; n < N_SAMPLES; n++) {
            final double x = n;
            final double y1 = Math.cos(Math.toRadians(10.0 * n));
            final double y2 = Math.sin(Math.toRadians(10.0 * n));
            xValues[n] = x;
            yValues1[n] = y1;
            dataSet2.add(n, y2); // style #1 how to set data, notifies re-draw for every 'add'
        }
        dataSet1.set(xValues, yValues1); // style #2 how to set data, notifies once per set
        // to manually trigger an update (optional):
        dataSet2.autoNotification().set(true); // to suppress auto notification
        dataSet2.invokeListener(new UpdatedDataEvent(dataSet2 /* pointer to update source */, "manual update event"));

        // alternatively (optional via default constructor):
        // final DoubleDataSet dataSet3 = new DoubleDataSet("data set #1", xValues, yValues1, N_SAMPLES, false)

        final Scene scene = new Scene(new StackPane(chart), 800, 600);
        primaryStage.setTitle(getClass().getSimpleName());
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(evt -> Platform.exit());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }

    /**
     * A custom plugin that displays a draggable legend on the chart.
     */
    public static class DraggableLegendPlugin extends ChartPlugin {
        private final DefaultLegend legend = new DefaultLegend();
        private final StackPane container = new StackPane(legend);
        private boolean isUserMoved = false;
        private boolean isDragging = false;
        private double startX, startY;

        public DraggableLegendPlugin() {
            // Matlab 风格：白色背景，黑色细边框
            container.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1px; -fx-border-style: solid; -fx-padding: 5px;");
            
            // 使用 Horizontal FlowPane 但设置极小的 WrapLength 来实现垂直堆叠且高度自适应
            legend.setVertical(false);
            legend.setPrefWrapLength(10);
            
            // 确保图例背景透明，以便显示容器的背景
            legend.setStyle("-fx-background-color: transparent;");

            getChartChildren().add(container);
            makeDraggable(container);

            // 监听 Chart 属性变化以绑定数据
            chartProperty().addListener((obs, oldChart, newChart) -> {
                if (newChart != null) {
                    bindChart(newChart);
                }
            });

            // 监听容器尺寸变化，以便在尺寸确定后重新定位
            container.widthProperty().addListener((obs, o, n) -> reposition());
            container.heightProperty().addListener((obs, o, n) -> reposition());
        }

        private void bindChart(Chart chart) {
            updateLegend(chart); // 初始更新

            ListChangeListener<DataSet> dsListener = c -> updateLegend(chart);
            chart.getDatasets().addListener(dsListener);

            ListChangeListener<Renderer> rListener = c -> updateLegend(chart);
            chart.getRenderers().addListener(rListener);
        }

        private void updateLegend(Chart chart) {
            legend.updateLegend(chart.getDatasets(), chart.getRenderers());
            // 更新后可能大小变了，请求重新布局
            container.requestLayout();
            Platform.runLater(this::reposition);
        }

        private void reposition() {
            if (getChart() == null) return;

            // 强制 autosize，因为 Group 不会调整子 Region 的大小
            container.autosize();

            double chartWidth = getChart().getCanvas().getWidth();
            double legendWidth = container.getWidth();

            if (!isUserMoved) {
                // 只有当宽度有效时才设置
                if (chartWidth > 0 && legendWidth > 0) {
                    container.setLayoutX(chartWidth - legendWidth - 30); // 距离右边 30 像素
                    container.setLayoutY(30); // 距离顶部 30 像素
                }
            }
        }

        private void makeDraggable(Node node) {
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
            node.setOnMouseReleased(mouseEvent -> node.setCursor(Cursor.HAND));
            node.setOnMouseDragged(mouseEvent -> {
                if (Math.abs(mouseEvent.getSceneX() - startX) > 5 || Math.abs(mouseEvent.getSceneY() - startY) > 5) {
                    isDragging = true;
                    isUserMoved = true; // 标记用户已手动移动
                }
                node.setLayoutX(mouseEvent.getSceneX() + dragDelta.x);
                node.setLayoutY(mouseEvent.getSceneY() + dragDelta.y);
                mouseEvent.consume();
            });
            node.setOnMouseEntered(mouseEvent -> node.setCursor(Cursor.HAND));

            // 拦截点击事件，防止拖拽时误触
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
}
