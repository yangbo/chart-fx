/*****************************************************************************
 * * Chart Common - simple reducing line renderer * * modified: 2019-02-01 Harald Braeuning * *
 ****************************************************************************/

package io.fair_acc.chartfx.renderer.spi;

import static io.fair_acc.dataset.DataSet.DIM_X;
import static io.fair_acc.dataset.DataSet.DIM_Y;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import io.fair_acc.chartfx.Chart;
import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.XYChartCss;
import io.fair_acc.chartfx.axes.Axis;
import io.fair_acc.chartfx.axes.spi.CategoryAxis;
import io.fair_acc.chartfx.renderer.Renderer;
import io.fair_acc.chartfx.renderer.spi.utils.DefaultRenderColorScheme;
import io.fair_acc.chartfx.utils.StyleParser;
import io.fair_acc.dataset.DataSet;
import io.fair_acc.dataset.utils.ProcessingProfiler;

/**
 * Simple, uncomplicated reducing line renderer
 * 
 * @author braeun
 */
public class ReducingLineRenderer extends AbstractDataSetManagement<ReducingLineRenderer> implements Renderer {
    private int maxPoints;
    private boolean drawChartDataSets = true;

    public ReducingLineRenderer() {
        maxPoints = 300;
    }

    public ReducingLineRenderer(final int maxPoints) {
        this.maxPoints = maxPoints;
    }

    @Override
    public Canvas drawLegendSymbol(DataSet dataSet, int dsIndex, int width, int height) {
        final Canvas canvas = new Canvas(width, height);
        final GraphicsContext gc = canvas.getGraphicsContext2D();

        final String style = dataSet.getStyle();
        final Integer layoutOffset = StyleParser.getIntegerPropertyValue(style, XYChartCss.DATASET_LAYOUT_OFFSET);
        final Integer dsIndexLocal = StyleParser.getIntegerPropertyValue(style, XYChartCss.DATASET_INDEX);

        final int dsLayoutIndexOffset = layoutOffset == null ? 0 : layoutOffset;
        final int plottingIndex = dsLayoutIndexOffset + (dsIndexLocal == null ? dsIndex : dsIndexLocal);

        gc.save();
        DefaultRenderColorScheme.setLineScheme(gc, dataSet.getStyle(), plottingIndex);
        DefaultRenderColorScheme.setGraphicsContextAttributes(gc, dataSet.getStyle());

        final double y = height / 2.0;
        gc.strokeLine(1, y, width - 2.0, y);

        gc.restore();
        return canvas;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    /**
     * @return the instance of this ReducingLineRenderer.
     */
    @Override
    protected ReducingLineRenderer getThis() {
        return this;
    }

    @Override
    public List<DataSet> render(final GraphicsContext gc, final Chart chart, final int dataSetOffset,
            final ObservableList<DataSet> datasets) {
        if (!(chart instanceof XYChart)) {
            throw new InvalidParameterException("must be derivative of XYChart for renderer - " + this.getClass().getSimpleName());
        }
        final XYChart xyChart = (XYChart) chart;

        // make local copy and add renderer specific data sets
        final List<DataSet> localDataSetList = drawChartDataSets ? new ArrayList<>(datasets) : new ArrayList<>();
        localDataSetList.addAll(super.getDatasets());

        final long start = ProcessingProfiler.getTimeStamp();
        final Axis xAxis = getFirstAxis(Orientation.HORIZONTAL, xyChart);
        final Axis yAxis = getFirstAxis(Orientation.VERTICAL, xyChart);

        final double xAxisWidth = xAxis.getWidth();
        final double xmin = xAxis.getValueForDisplay(0);
        final double xmax = xAxis.getValueForDisplay(xAxisWidth);
        int index = 0;
        for (final DataSet ds : localDataSetList) {
            if (!ds.isVisible()) {
                continue;
            }
            final int lindex = index;
            ds.lock().readLockGuardOptimistic(() -> {
                // update categories in case of category axes for the first
                // (index == '0') indexed data set
                if (lindex == 0) {
                    if (xyChart.getXAxis() instanceof CategoryAxis) {
                        final CategoryAxis axis = (CategoryAxis) xyChart.getXAxis();
                        axis.updateCategories(ds);
                    }

                    if (xyChart.getYAxis() instanceof CategoryAxis) {
                        final CategoryAxis axis = (CategoryAxis) xyChart.getYAxis();
                        axis.updateCategories(ds);
                    }
                }

                gc.save();
                DefaultRenderColorScheme.setLineScheme(gc, ds.getStyle(), lindex);
                DefaultRenderColorScheme.setGraphicsContextAttributes(gc, ds.getStyle());
                if (ds.getDataCount() > 0) {
                    final int indexMin = Math.max(0, ds.getIndex(DIM_X, xmin));
                    final int indexMax = Math.min(ds.getIndex(DIM_X, xmax) + 1, ds.getDataCount());
                    final int n = Math.abs(indexMax - indexMin);
                    final int d = n / maxPoints;
                    if (d <= 1) {
                        int i = ds.getIndex(DIM_X, xmin);
                        if (i < 0) {
                            i = 0;
                        }
                        double x0 = xAxis.getDisplayPosition(ds.get(DIM_X, i));
                        double y0 = yAxis.getDisplayPosition(ds.get(DIM_Y, i));
                        i++;
                        for (; i < Math.min(ds.getIndex(DIM_X, xmax) + 1, ds.getDataCount()); i++) {
                            final double x1 = xAxis.getDisplayPosition(ds.get(DIM_X, i));
                            final double y1 = yAxis.getDisplayPosition(ds.get(DIM_Y, i));
                            gc.strokeLine(x0, y0, x1, y1);
                            x0 = x1;
                            y0 = y1;
                        }
                    } else {
                        int i = ds.getIndex(DIM_X, xmin);
                        if (i < 0) {
                            i = 0;
                        }
                        double x0 = xAxis.getDisplayPosition(ds.get(DIM_X, i));
                        double y0 = yAxis.getDisplayPosition(ds.get(DIM_Y, i));
                        i++;
                        double x1 = xAxis.getDisplayPosition(ds.get(DIM_X, i));
                        double y1 = yAxis.getDisplayPosition(ds.get(DIM_Y, i));
                        double delta = Math.abs(y1 - y0);
                        i++;
                        int j = d - 2;
                        for (; i < Math.min(ds.getIndex(DIM_X, xmax) + 1, ds.getDataCount()); i++) {
                            if (j > 0) {
                                final double x2 = xAxis.getDisplayPosition(ds.get(DIM_X, i));
                                final double y2 = yAxis.getDisplayPosition(ds.get(DIM_Y, i));
                                if (Math.abs(y2 - y0) > delta) {
                                    x1 = x2;
                                    y1 = y2;
                                    delta = Math.abs(y2 - y0);
                                }
                                j--;
                            } else {
                                gc.strokeLine(x0, y0, x1, y1);
                                x0 = x1;
                                y0 = y1;
                                x1 = xAxis.getDisplayPosition(ds.get(DIM_X, i));
                                y1 = yAxis.getDisplayPosition(ds.get(DIM_Y, i));
                                delta = Math.abs(y1 - y0);
                                j = d - 1;
                            }
                        }
                    }
                }
                gc.restore();
            });
            index++;
        }
        ProcessingProfiler.getTimeDiff(start);

        return localDataSetList;
    }

    public void setMaxPoints(final int maxPoints) {
        this.maxPoints = maxPoints;
    }

    public void setDrawChartDataSets(final boolean state) {
        drawChartDataSets = state;
    }

    public boolean isDrawChartDataSets() {
        return drawChartDataSets;
    }
}
