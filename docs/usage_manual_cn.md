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
