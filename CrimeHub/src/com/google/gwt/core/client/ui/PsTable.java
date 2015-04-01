package com.google.gwt.core.client.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.google.gwt.gen2.table.client.AbstractColumnDefinition;
import com.google.gwt.gen2.table.client.CachedTableModel;
import com.google.gwt.gen2.table.client.DefaultRowRenderer;
import com.google.gwt.gen2.table.client.DefaultTableDefinition;
import com.google.gwt.gen2.table.client.FixedWidthGridBulkRenderer;
import com.google.gwt.gen2.table.client.MutableTableModel;
import com.google.gwt.gen2.table.client.PagingOptions;
import com.google.gwt.gen2.table.client.PagingScrollTable;
import com.google.gwt.gen2.table.client.ScrollTable;
import com.google.gwt.gen2.table.client.TableDefinition;
import com.google.gwt.gen2.table.client.TableModel;
import com.google.gwt.gen2.table.client.AbstractScrollTable.SortPolicy;
import com.google.gwt.gen2.table.client.SelectionGrid.SelectionPolicy;
import com.google.gwt.gen2.table.client.TableModelHelper.Request;
import com.google.gwt.gen2.table.client.TableModelHelper.Response;
import com.google.gwt.sample.crimehub.client.Crime;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Simple example that shows the usage of GWT-incubator's {@link PagingScrollTable}
 * with a simple table model object.  The table also supports sorting via a 
 * custom set of {@link Comparator} I whipped up.  Nothing fancy or exciting.
 */
public class PsTable extends Composite {
	
	private CachedTableModel<Crime> cachedTableModel = null;
	private DefaultTableDefinition<Crime> tableDefinition = null;
	private PagingScrollTable<Crime> pagingScrollTable = null;
	private DataSourceTableModel tableModel = null;
	private Label countLabel = new Label("There are no crimes to display.");
	
	private VerticalPanel vPanel = new VerticalPanel();
	private FlexTable flexTable = new FlexTable();
	
	/**
	 * Constructor
	 */
	public PsTable() {
		super();	
		pagingScrollTable = createScrollTable();
		pagingScrollTable.setHeight("500px");
		pagingScrollTable.setWidth("100%");
		PagingOptions pagingOptions = new PagingOptions(pagingScrollTable);
		
		flexTable.setWidget(0, 0, pagingScrollTable);
		flexTable.getFlexCellFormatter().setColSpan(0, 0, 2);
		flexTable.setWidget(1, 0, pagingOptions);

		countLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		countLabel.setStyleName("countLabel");
		vPanel.add(countLabel);
		vPanel.add(flexTable);
		
		vPanel.setWidth("100%");
		flexTable.setWidth("100%");
		
		super.initWidget(vPanel);
	}
	
	/**
	 * Allows consumers of this class to stuff a new {@link ArrayList} of {@link Crime}
	 * into the table -- overwriting whatever was previously there.
	 * 
	 * @param list the list of messages to show
	 */
	public void showCrimes(List<Crime> list) {
		countLabel.setText("There are " + list.size() + " crimes being displayed.");
		// reset the table model data
		tableModel.setData(list);
		// reset the table model row count
		tableModel.setRowCount(list.size());
		// clear the cache
		cachedTableModel.clearCache();
		// reset the cached model row count
		cachedTableModel.setRowCount(list.size());
		// force to page zero with a reload
		pagingScrollTable.gotoPage(0, true);
	}
	
	/**
	 * Initializes the scroll table
	 * @return
	 */
	private PagingScrollTable<Crime> createScrollTable() {
		// create our own table model
		tableModel = new DataSourceTableModel();
		// add it to cached table model
		cachedTableModel = createCachedTableModel(tableModel);
		
		// create the table definition
		TableDefinition<Crime> tableDef = createTableDefinition();
		
		// create the paging scroll table
		PagingScrollTable<Crime> pagingScrollTable = new PagingScrollTable<Crime>(cachedTableModel, tableDef);
		pagingScrollTable.setPageSize(50);
		pagingScrollTable.getDataTable().setSelectionPolicy(SelectionPolicy.ONE_ROW);
		
		// setup the bulk renderer
		FixedWidthGridBulkRenderer<Crime> bulkRenderer = new FixedWidthGridBulkRenderer<Crime>(pagingScrollTable.getDataTable(), pagingScrollTable);
		pagingScrollTable.setBulkRenderer(bulkRenderer);
		
		// setup the formatting
		pagingScrollTable.setCellPadding(3);
		pagingScrollTable.setCellSpacing(0);
		pagingScrollTable.setResizePolicy(ScrollTable.ResizePolicy.FILL_WIDTH);
		
		pagingScrollTable.setSortPolicy(SortPolicy.SINGLE_CELL);
//		pagingScrollTable.getDataTable().addRowSelectionHandler(rowSelectionHandler);
		
		return pagingScrollTable;
	}
	
	/**
	 * Create the {@link CachedTableModel}
	 * @param tableModel 
	 * @return
	 */
	private CachedTableModel<Crime> createCachedTableModel(DataSourceTableModel tableModel) {
		CachedTableModel<Crime> tm = new CachedTableModel<Crime>(tableModel);
		tm.setPreCachedRowCount(50);
		tm.setPostCachedRowCount(50);
		tm.setRowCount(1000);
		return tm;
	}
	
	private DefaultTableDefinition<Crime> createTableDefinition() {
		tableDefinition = new DefaultTableDefinition<Crime>();
		
		// set the row renderer
		final String[] rowColors = new String[] { "#FFFFDD", "EEEEEE" };
		tableDefinition.setRowRenderer(new DefaultRowRenderer<Crime>(rowColors));
		
		// type
		{
			CrimeTypeColumnDefinition columnDef = new CrimeTypeColumnDefinition();
			columnDef.setColumnSortable(false);
			columnDef.setColumnTruncatable(false);
			columnDef.setPreferredColumnWidth(100);
			columnDef.setHeader(0, new HTML("Crime Type"));
			columnDef.setHeaderCount(1);
			columnDef.setHeaderTruncatable(false);
			tableDefinition.addColumnDefinition(columnDef);
		}
		// year
		{
			YearColumnDefinition columnDef = new YearColumnDefinition();
			columnDef.setColumnSortable(false);
			columnDef.setColumnTruncatable(false);
			columnDef.setPreferredColumnWidth(100);
			columnDef.setHeader(0, new HTML("Year"));
			columnDef.setHeaderCount(1);
			columnDef.setHeaderTruncatable(false);
			tableDefinition.addColumnDefinition(columnDef);
		}
		// month
		{
			MonthColumnDefinition columnDef = new MonthColumnDefinition();
			columnDef.setColumnSortable(false);
			columnDef.setColumnTruncatable(false);
			columnDef.setPreferredColumnWidth(100);
			columnDef.setHeader(0, new HTML("Month"));
			columnDef.setHeaderCount(1);
			columnDef.setHeaderTruncatable(false);
			tableDefinition.addColumnDefinition(columnDef);
		}
		// address
		{
			AddressColumnDefinition columnDef = new AddressColumnDefinition();
			columnDef.setColumnSortable(false);
			columnDef.setColumnTruncatable(false);
			columnDef.setPreferredColumnWidth(100);
			columnDef.setHeader(0, new HTML("Address"));
			columnDef.setHeaderCount(1);
			columnDef.setHeaderTruncatable(false);
			tableDefinition.addColumnDefinition(columnDef);
		}
		return tableDefinition;
	}
	
	/**
	 * Extension of {@link MutableTableModel} for our own {@link Crime} type.
	 */
	private class DataSourceTableModel extends MutableTableModel<Crime> {
 		
 		// we keep a map so we can index by id
 		private Map<String, Crime> map;
 		
 		/**
 		 * Set the data on the model.  Overwrites prior data.
 		 * @param list
 		 */
 		public void setData(List<Crime> list) {
 			// toss the list, index by id in a map.
 			map = new HashMap<String, Crime>(list.size()); 
 				for (Crime c : list) {
 					map.put(c.getId(), c);
 			}

 		}
 		
 		/**
 		 * Fetch a {@link Crime} by its id.
 		 * @param id
 		 * @return
 		 */
 		public Crime getCrimeById(long id) {
 			return map.get(id);
 		}
 
 		@Override
 		protected boolean onRowInserted(int beforeRow) {
 			return true;
 		}
 
 		@Override
 		protected boolean onRowRemoved(int row) {
 			return true;
 		}
 
 		@Override
 		protected boolean onSetRowValue(int row, Crime rowValue) {
 			return true;
 		}

 		@Override
 		public void requestRows(
 				final Request request,
 				TableModel.Callback<Crime> callback) {
 			
 			callback.onRowsReady(request, new Response<Crime>(){
 
 				@Override
 				public Iterator<Crime> getRowValues() {
 					return map.values().iterator();
 				}
 				});
		}
		
	}
	
	
	private final class CrimeTypeColumnDefinition extends AbstractColumnDefinition<Crime, String> {
		@Override
		public String getCellValue(Crime rowValue) {
			return rowValue.getType();
		}
		@Override
		public void setCellValue(Crime rowValue, String cellValue) { }
	}
	
	
	private final class YearColumnDefinition extends AbstractColumnDefinition<Crime, String> {
		@Override
		public String getCellValue(final Crime rowValue) {
			return rowValue.getYear();
		}

		@Override
		public void setCellValue(final Crime rowValue, final String cellValue) {}
	}
	
	
	private final class MonthColumnDefinition extends AbstractColumnDefinition<Crime, String> {
		@Override
		public String getCellValue(Crime rowValue) {
			return rowValue.getMonth();
		}
		@Override
		public void setCellValue(Crime rowValue, String cellValue) { }
	}
	
	
	private final class AddressColumnDefinition extends AbstractColumnDefinition<Crime, String> {
		@Override
		public String getCellValue(Crime rowValue) {
			return rowValue.getAddress();
		}
		@Override
		public void setCellValue(Crime rowValue, String cellValue) { }
	}

}
