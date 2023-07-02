/*******************************************************************************************************
 *
 * ChartJFreeChartOutputPie.java, in msi.gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.2).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.outputs.layers.charts;

import java.awt.Point;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.DefaultPolarItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYDataset;

import msi.gama.common.interfaces.IDisplaySurface;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.runtime.IScope;
import msi.gaml.expressions.IExpression;

/**
 * The Class ChartJFreeChartOutputPie.
 */
public class ChartJFreeChartOutputPie extends ChartJFreeChartOutput {

	/**
	 * Instantiates a new chart J free chart output pie.
	 *
	 * @param scope
	 *            the scope
	 * @param name
	 *            the name
	 * @param typeexp
	 *            the typeexp
	 */
	public ChartJFreeChartOutputPie(final IScope scope, final String name, final IExpression typeexp) {
		super(scope, name, typeexp);
		// TODO Auto-generated constructor stubs

	}

	@Override
	public void createChart(final IScope scope) {
		super.createChart(scope);
		if (IKeyword.THREE_D.equals(style)) {
			chart = ChartFactory.createPieChart3D(getName(), null, false, true, false);
		} else if (IKeyword.RING.equals(style)) {
			chart = ChartFactory.createRingChart(getName(), null, false, true, false);
		} else {
			chart = ChartFactory.createPieChart(getName(), null, false, true, false);
		}
	}

	@Override
	public void initdataset() {
		super.initdataset();
		chartdataset.setCommonXSeries(true);
		chartdataset.setByCategory(true);
	}

	@Override
	public void setDefaultPropertiesFromType(final IScope scope, final ChartDataSource source, final int type_val) {
		

		switch (type_val) {
			case ChartDataSource.DATA_TYPE_LIST_DOUBLE_N:
			case ChartDataSource.DATA_TYPE_LIST_LIST_DOUBLE_N:
			case ChartDataSource.DATA_TYPE_LIST_LIST_DOUBLE_12:
			case ChartDataSource.DATA_TYPE_LIST_POINT:
			case ChartDataSource.DATA_TYPE_MATRIX_DOUBLE:
			case ChartDataSource.DATA_TYPE_LIST_DOUBLE_3:
			case ChartDataSource.DATA_TYPE_LIST_LIST_DOUBLE_3:
			default: {
				source.setCumulative(scope, false); // never cumulative by default
				source.setUseSize(scope, false);
			}
		}

	}

	@Override
	public void initChart(final IScope scope, final String chartname) {
		super.initChart(scope, chartname);

		final PiePlot<?> pp = (PiePlot<?>) chart.getPlot();
		pp.setShadowXOffset(0);
		pp.setShadowYOffset(0);
		if (!"none".equals(this.series_label_position)) {
			pp.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} = {1} ({2})"));
			if (axesColor != null) { pp.setLabelLinkPaint(axesColor); }
			pp.setLabelFont(getTickFont());
			if (labelTextColor != null) { pp.setLabelPaint(labelTextColor); }
			if (labelBackgroundColor != null) { pp.setLabelBackgroundPaint(this.labelBackgroundColor); }

		}
		if ("none".equals(this.series_label_position)) {
			pp.setLabelLinksVisible(false);
			pp.setLabelGenerator(null);

		}
	}

	@Override
	protected AbstractRenderer createRenderer(final IScope scope, final String serieid) {

		final String style = this.getChartdataset().getDataSeries(scope, serieid).getStyle(scope);
		AbstractRenderer newr;
		switch (style) {
			case IKeyword.STACK:
			case IKeyword.THREE_D:
			case IKeyword.WHISKER:
			case IKeyword.AREA:
			case IKeyword.BAR:
			case IKeyword.STEP:
			case IKeyword.RING:
			case IKeyword.EXPLODED:
			default: {
				newr = new DefaultPolarItemRenderer(); // useless, piechart doesn't
														// use renderers...
				break;

			}
		}
		return newr;
	}

	/**
	 * Reset renderer.
	 *
	 * @param scope
	 *            the scope
	 * @param serieid
	 *            the serieid
	 */
	protected void resetRenderer(final IScope scope, final String serieid) {
		final ChartDataSeries myserie = this.getChartdataset().getDataSeries(scope, serieid);
		// final int myrow = IdPosition.get(serieid);
		if (myserie.getMycolor() != null) {
			((PiePlot<?>) this.getJFChart().getPlot()).setSectionPaint(serieid, myserie.getMycolor());
		}

	}

	@SuppressWarnings ("unchecked")
	@Override
	protected void clearDataSet(final IScope scope) {
		
		super.clearDataSet(scope);
		final PiePlot<?> plot = (PiePlot) this.chart.getPlot();
		jfreedataset.clear();
		DefaultPieDataset dd = new DefaultPieDataset<>();
		jfreedataset.add(0, dd);
		plot.setDataset(dd);
		idPosition.clear();
		nbseries = 0;
	}

	@Override
	protected void createNewSerie(final IScope scope, final String serieid) {
		// final ChartDataSeries dataserie = chartdataset.getDataSeries(scope,
		// serieid);
		if (!idPosition.containsKey(serieid)) {
			@SuppressWarnings ("unchecked") final PiePlot<String> plot = (PiePlot<String>) this.chart.getPlot();

			// final DefaultPieDataset firstdataset = (DefaultPieDataset)
			// plot.getDataset();

			nbseries++;
			idPosition.put(serieid, nbseries - 1);
			if (IKeyword.EXPLODED.equals(getStyle())) { plot.setExplodePercent(serieid, 0.20); }
		}
		// DEBUG.LOG("new serie"+serieid+" at
		// "+IdPosition.get(serieid)+" jfds "+jfreedataset.size()+" datasc "+"
		// nbse "+nbseries);
	}

	@Override
	protected void resetSerie(final IScope scope, final String serieid) {
		

		final ChartDataSeries dataserie = chartdataset.getDataSeries(scope, serieid);
		@SuppressWarnings ("unchecked") final DefaultPieDataset<String> serie =
				(DefaultPieDataset<String>) jfreedataset.get(0);
		final ArrayList<Double> YValues = dataserie.getYValues(scope);

		if (YValues.size() > 0) {
			// TODO Hack to speed up, change!!!
			serie.setValue(serieid, YValues.get(YValues.size() - 1));
		}
		this.resetRenderer(scope, serieid);

	}

	@Override
	protected void initRenderer(final IScope scope) {
		

	}

	@Override
	public void getModelCoordinatesInfo(final int xOnScreen, final int yOnScreen, final IDisplaySurface g,
			final Point positionInPixels, final StringBuilder sb) {
		final int x = xOnScreen - positionInPixels.x;
		final int y = yOnScreen - positionInPixels.y;
		final ChartEntity entity = info.getEntityCollection().getEntity(x, y);
		// getChart().handleClick(x, y, info);
		if (entity instanceof XYItemEntity) {
			final XYDataset data = ((XYItemEntity) entity).getDataset();
			final int index = ((XYItemEntity) entity).getItem();
			final int series = ((XYItemEntity) entity).getSeriesIndex();
			final double xx = data.getXValue(series, index);
			final double yy = data.getYValue(series, index);
			final XYPlot plot = (XYPlot) getJFChart().getPlot();
			final ValueAxis xAxis = plot.getDomainAxis(series);
			final ValueAxis yAxis = plot.getRangeAxis(series);
			final boolean xInt = xx % 1 == 0;
			final boolean yInt = yy % 1 == 0;
			String xTitle = xAxis.getLabel();
			if (StringUtils.isBlank(xTitle)) { xTitle = "X"; }
			String yTitle = yAxis.getLabel();
			if (StringUtils.isBlank(yTitle)) { yTitle = "Y"; }
			sb.append(xTitle).append(" ").append(xInt ? (int) xx : String.format("%.2f", xx));
			sb.append(" | ").append(yTitle).append(" ").append(yInt ? (int) yy : String.format("%.2f", yy));
			return;
		}
		if (entity instanceof PieSectionEntity) {
			final String title = ((PieSectionEntity) entity).getSectionKey().toString();
			final PieDataset<?> data = ((PieSectionEntity) entity).getDataset();
			final int index = ((PieSectionEntity) entity).getSectionIndex();
			final double xx = data.getValue(index).doubleValue();
			final boolean xInt = xx % 1 == 0;
			sb.append(title).append(" ").append(xInt ? (int) xx : String.format("%.2f", xx));
		} else if (entity instanceof CategoryItemEntity) {
			final Comparable<?> columnKey = ((CategoryItemEntity) entity).getColumnKey();
			final String title = columnKey.toString();
			final CategoryDataset data = ((CategoryItemEntity) entity).getDataset();
			final Comparable<?> rowKey = ((CategoryItemEntity) entity).getRowKey();
			final double xx = data.getValue(rowKey, columnKey).doubleValue();
			final boolean xInt = xx % 1 == 0;
			sb.append(title).append(" ").append(xInt ? (int) xx : String.format("%.2f", xx));
		}
	}

}
