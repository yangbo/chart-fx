# ChartFX 中文使用手册

本手册旨在帮助开发者快速上手 ChartFX 库，并通过一个简单的示例 `SimpleChartSample` 来介绍核心功能。

## 1. 快速上手

下面我们将分解 `SimpleChartSample` 示例，一步步教您如何创建一个基本的图表。

### 1.1 创建图表和坐标轴

一个图表至少需要 X 和 Y 两个坐标轴。

```java
// 创建一个默认的 Y 轴
final DefaultNumericAxis yAxis = new DefaultNumericAxis();
yAxis.setAutoRanging(true); // 启用自动范围
yAxis.setAutoRangePadding(0.5); // 设置顶部和底部的留白比例

// 创建一个 XYChart 实例，并传入 X 轴和 Y 轴
// 这里我们为 X 轴动态创建了一个新的 DefaultNumericAxis
final XYChart chart = new XYChart(new DefaultNumericAxis(), yAxis);
```

### 1.2 创建和填充数据集

图表的数据存储在 `DataSet` 对象中。`DoubleDataSet` 是其中一种常用的实现。

```java
// 创建两个数据集，分别命名
final DoubleDataSet dataSet1 = new DoubleDataSet("data set #1");
final DoubleDataSet dataSet2 = new DoubleDataSet("data set #2");

// --- 填充数据的方式 1: 逐点添加 ---
// 适合实时数据更新的场景
for (int n = 0; n < 100; n++) {
    final double x = n;
    final double y = Math.sin(Math.toRadians(10.0 * n));
    dataSet2.add(x, y); // 每次调用 add 都会触发图表重绘
}

// --- 填充数据的方式 2: 批量设置 ---
// 适合一次性加载大量数据，性能更高
final double[] xValues = new double[100];
final double[] yValues1 = new double[100];
for (int n = 0; n < 100; n++) {
    xValues[n] = n;
    yValues1[n] = Math.cos(Math.toRadians(10.0 * n));
}
dataSet1.set(xValues, yValues1); // 只在 set 调用结束后触发一次重绘
```

### 1.3 将数据集添加到图表

创建好的数据集需要添加到图表的 `datasets` 列表中才能被渲染。

```java
// 将数据集添加到图表
chart.getDatasets().addAll(dataSet1, dataSet2);
```

### 1.4 将图表放入场景并显示

最后，将 `chart` 对象像普通 JavaFX 节点一样放入场景中即可。

```java
// 将图表放入一个布局容器中，并创建场景
final Scene scene = new Scene(new StackPane(chart), 800, 600);
primaryStage.setTitle("SimpleChartSample");
primaryStage.setScene(scene);
primaryStage.show();
```

---

## 2. 常用功能配置

### 2.1 添加交互插件

ChartFX 通过插件机制提供丰富的交互功能。

```java
// 添加缩放、十字准星和坐标轴编辑插件
chart.getPlugins().addAll(new Zoomer(), new CrosshairIndicator(), new EditAxis());
```

### 2.2 显示/隐藏图表边框

您可以轻松控制图表绘图区域是否拥有一个完整的矩形边框。

```java
// 显示一个完整的、类似 Matlab 风格的矩形边框
chart.setChartBorderVisible(true); 

// 隐藏边框（默认行为）
chart.setChartBorderVisible(false);
```

### 2.3 设置图表内边距

为了让图表的数据不直接顶着边框，可以设置内边距（Padding），在边框和绘图内容之间留出空白区域。

```java
// 导入 Insets 类
import javafx.geometry.Insets;

// 设置图表四周的内边距为 10 像素
chart.setPadding(new Insets(10));
```

### 2.4 使用浮动图例 (DraggableLegendPlugin)

如果您希望图例能够像 Matlab 那样悬浮在图表内部、支持鼠标拖拽，并且在窗口缩放时能自动吸附右上角，可以使用 `DraggableLegendPlugin`。

#### 2.4.1 使用方法

首先，您需要隐藏图表默认的固定图例，然后添加 `DraggableLegendPlugin` 插件。

```java
// 导入插件类
import io.fair_acc.chartfx.samples.DraggableLegendPlugin;

// 1. 隐藏默认图例
chart.setLegendVisible(false);

// 2. 添加浮动图例插件
chart.getPlugins().add(new DraggableLegendPlugin());
```

#### 2.4.2 功能特性

*   **自动定位**：图例初始会吸附在图表绘图区域的右上角。
*   **鼠标拖拽**：用户可以使用鼠标随意拖拽图例到任意位置。
*   **右侧锚定**：当调整窗口宽度时，图例会自动移动，以保持与右侧边框的相对距离不变（类似 Matlab 的行为）。
*   **紧凑布局**：图例采用垂直列表布局，且高度会根据内容自适应，不会占用多余空间。
*   **样式**：默认提供白色背景和黑色边框，确保图例清晰可见。

### 2.5 坐标轴刻度配置

为了获得更清爽、或者更符合特定需求的视觉效果（类似 Matlab），您可以对坐标轴的刻度进行细致的配置。

#### 2.5.1 强制设置固定刻度间隔

默认情况下，ChartFX 会根据数据范围和轴的长度自动计算最佳的刻度间隔。如果您希望强制使用固定的刻度间隔（例如每隔 50 一个刻度），可以使用 `autoTickUnit` 属性。

```java
// 禁用自动刻度间隔计算
axis.setAutoTickUnit(false);

// 设置固定的刻度间隔为 50
axis.setTickUnit(50.0);
```

#### 2.5.2 隐藏小刻度

如果您觉得图表上的小刻度线（Minor Ticks）显得过于杂乱，可以将其隐藏。

```java
// 隐藏小刻度
axis.setMinorTickVisible(false);

// 或者通过将小刻度数量设置为 0
axis.setMinorTickCount(0);
```

#### 2.5.3 调整主刻度密度

除了强制固定间隔外，您还可以通过限制主刻度的最大数量来让标签显得更稀疏。系统会自动计算一个较大的间隔来满足这个数量限制。

```java
// 将主刻度标签的最大数量限制为 5 个（默认通常为 20）
// 这样可以使刻度标签更加稀疏、清爽
axis.setMaxMajorTickLabelCount(5);
```

### 2.6 自动范围与留白配置

在自动范围模式下，您可以控制坐标轴两端的留白以及是否将范围取整到刻度。

#### 2.6.1 设置留白与取整

```java
// 启用自动范围（默认开启）
axis.setAutoRanging(true);

// 开启取整模式：范围边界会自动扩展到刻度的倍数
axis.setAutoRangeRounding(true);

// 设置留白比例：0.1 表示在数据范围的基础上左右各扩展 10%
axis.setAutoRangePadding(0.1);
```

#### 2.6.2 允许跨越零点 (autoRangeClampToZero)

默认情况下，ChartFX 会允许自动范围跨越零点（例如，即使数据从 0 开始，如果有留白，轴可能会从负数开始）。这是由 `autoRangeClampToZero` 属性控制的。

*   **默认行为 (`false`)**：允许跨越零点。如果设置了 padding，轴的最小值可能会小于 0，从而实现左侧留白。
*   **限制行为 (`true`)**：如果数据在零的一侧（例如全部为正），轴的范围会被强制限制在 0，不会跨越到负数。

```java
// 允许跨越零点（默认值），实现左右对称留白
axis.setAutoRangeClampToZero(false);

// 强制限制在零点，不显示负数刻度（如果数据全为正）
axis.setAutoRangeClampToZero(true);
```

### 2.7 多坐标轴与多渲染器配置

ChartFX 支持在同一个图表上显示多个坐标轴（例如左侧和右侧 Y 轴），并支持使用多个渲染器来分别控制不同数据集的绘制。

#### 2.7.1 添加右侧 Y 轴

要使用右侧 Y 轴，您需要创建一个新的坐标轴对象，并将其侧边属性设置为 `Side.RIGHT`。

```java
import io.fair_acc.chartfx.ui.geometry.Side;

// 创建右侧 Y 轴
final DefaultNumericAxis yAxisRight = new DefaultNumericAxis("Right Y Axis");
yAxisRight.setSide(Side.RIGHT);
yAxisRight.setAutoRanging(true); // 同样支持自动范围
```

#### 2.7.2 使用特定的渲染器

为了让某些数据集显示在特定的轴上（例如右侧 Y 轴），或者为了使用不同的渲染效果（例如无误差线的折线图），您需要创建一个新的渲染器实例，并将轴和数据集绑定到它上面。

`ReducingLineRenderer` 是一个高性能的无误差线渲染器。

```java
import io.fair_acc.chartfx.renderer.spi.ReducingLineRenderer;

// 创建一个新的渲染器
final ReducingLineRenderer renderer2 = new ReducingLineRenderer();

// 1. 绑定右侧 Y 轴（必须在添加到 Chart 之前设置）
renderer2.getAxes().add(yAxisRight);

// 2. 添加专属的数据集
renderer2.getDatasets().add(dataSet2);

// 3. 配置渲染器不绘制图表的主数据集（避免重复绘制）
// ReducingLineRenderer 默认会绘制 Chart 中所有的主数据集。
// 如果您只想让它绘制自己列表中的数据集，需要关闭此选项。
renderer2.setDrawChartDataSets(false);

// 4. 将渲染器添加到图表
chart.getRenderers().add(renderer2);
```

**注意**：如果不设置 `setDrawChartDataSets(false)`，`renderer2` 可能会同时绘制 Chart 的主数据集（通常显示在左侧轴）和它自己的数据集，导致左侧轴的数据被错误地在右侧轴上再次绘制一遍。

---

## 3. 样式定制

### 3.1 通过 CSS 自定义边框样式

图表边框的样式由 CSS 控制，方便您进行统一的样式管理。

在您的 CSS 文件中（例如 `chart.css`），您可以定义 `.chart-border-lines` 样式类来修改边框的外观。

```css
.chart-border-lines {
    /* 设置边框颜色为深灰色 */
    -fx-stroke: dimgray;
    
    /* 设置边框宽度为 1 像素 */
    -fx-stroke-width: 1px;
}
```

## 4. Latex 支持

目前 X、Y 轴的名称不支持使用 LaTex 公式，但对于简单的数学符号（如上标、下标、希腊字母），您可以使用 Unicode 字符。
例如：

平方：x² (Unicode \u00B2)
微米：µm (Unicode \u00B5)
阿尔法：α (Unicode \u03B1)
温度：℃

