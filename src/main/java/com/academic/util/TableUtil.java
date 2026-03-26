package com.academic.util;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class for table operations including search, filtering, and sorting.
 * Provides UI components and logic for searching table contents by column.
 */
public class TableUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableUtil.class);

    /**
     * Represents a filterable column in a table.
     * 
     * @param <T> The table row type
     */
    public static class FilterableColumn<T> {
        private final String displayName;
        private final Function<T, String> extractor;

        public FilterableColumn(String displayName, Function<T, String> extractor) {
            this.displayName = displayName;
            this.extractor = extractor;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String extractValue(T item) {
            String value = extractor.apply(item);
            return value == null ? "" : value;
        }
    }

    /**
     * Creates a search UI component (HBox) for filtering a table by column.
     * Returns a container with search field, column selector, and clear button.
     * 
     * @param <T> The table row type
     * @param table The TableView to search
     * @param allData The complete unfiltered data list
     * @param columns List of filterable columns
     * @return VBox containing search/sort controls and pagination controls
     */
    public static <T> VBox createSearchPanel(TableView<T> table, Supplier<List<T>> dataSupplier,
                                              List<FilterableColumn<T>> columns) {
        VBox container = new VBox(8);
        container.setStyle("-fx-padding: 10; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        HBox searchPanel = new HBox(10);
        searchPanel.setAlignment(Pos.CENTER_LEFT);

        // Column selector
        Label columnLabel = new Label("Search by:");
        columnLabel.setStyle("-fx-font-weight: bold;");
        
        ComboBox<String> columnSelector = new ComboBox<>();
        columnSelector.setPromptText("Select column...");
        columnSelector.setPrefWidth(150);
        
        List<String> columnNames = columns.stream()
            .map(FilterableColumn::getDisplayName)
            .collect(Collectors.toList());
        columnNames.add(0, "All Columns");
        columnSelector.setItems(FXCollections.observableArrayList(columnNames));
        columnSelector.setValue("All Columns");

        // Search input field
        Label searchLabel = new Label("Search:");
        searchLabel.setStyle("-fx-font-weight: bold;");
        
        TextField searchField = new TextField();
        searchField.setPromptText("Type to filter...");
        searchField.setPrefWidth(250);

        // Clear button
        Button clearBtn = new Button("Clear");
        clearBtn.setStyle("-fx-padding: 5 15 5 15; -fx-font-size: 12;");

        // Pagination controls
        HBox paginationPanel = new HBox(10);
        paginationPanel.setAlignment(Pos.CENTER_LEFT);

        Label pageSizeLabel = new Label("Rows:");
        pageSizeLabel.setStyle("-fx-font-weight: bold;");

        ComboBox<Integer> pageSizeSelector = new ComboBox<>();
        pageSizeSelector.setPrefWidth(90);
        pageSizeSelector.setItems(FXCollections.observableArrayList(10, 25, 50, 100));
        pageSizeSelector.setValue(25);

        Button prevBtn = new Button("Prev");
        prevBtn.setStyle("-fx-padding: 4 12 4 12; -fx-font-size: 12;");
        Button nextBtn = new Button("Next");
        nextBtn.setStyle("-fx-padding: 4 12 4 12; -fx-font-size: 12;");

        Label pageInfoLabel = new Label("Page 1 / 1");
        Label resultInfoLabel = new Label("0 results");

        int[] currentPage = {0};

        // Sort controls
        Label sortLabel = new Label("Sort by:");
        sortLabel.setStyle("-fx-font-weight: bold;");

        ComboBox<String> sortColumnSelector = new ComboBox<>();
        sortColumnSelector.setPromptText("Select column...");
        sortColumnSelector.setPrefWidth(150);

        List<String> sortableColumnNames = table.getColumns().stream()
            .map(TableColumn::getText)
            .filter(name -> name != null && !name.isBlank())
            .collect(Collectors.toList());
        sortableColumnNames.add(0, "None");
        sortColumnSelector.setItems(FXCollections.observableArrayList(sortableColumnNames));
        sortColumnSelector.setValue("None");

        ComboBox<String> sortDirectionSelector = new ComboBox<>();
        sortDirectionSelector.setPrefWidth(110);
        sortDirectionSelector.setItems(FXCollections.observableArrayList("Ascending", "Descending"));
        sortDirectionSelector.setValue("Ascending");

        // Search logic
        Runnable applyFilterSortAndPaginate = () -> {
            List<T> allData = dataSupplier.get();
            if (allData == null) {
                allData = List.of();
            }
            List<T> workingData = new ArrayList<>(allData);

            String searchText = searchField.getText().toLowerCase().trim();
            String selectedColumn = columnSelector.getValue();
            String selectedSortColumn = sortColumnSelector.getValue();
            String selectedDirection = sortDirectionSelector.getValue();

            if (!searchText.isEmpty()) {
                if ("All Columns".equals(selectedColumn)) {
                    workingData = workingData.stream()
                        .filter(item -> columns.stream()
                            .anyMatch(col -> col.extractValue(item)
                                .toLowerCase()
                                .contains(searchText)))
                        .collect(Collectors.toList());
                } else {
                    FilterableColumn<T> selectedCol = columns.stream()
                        .filter(col -> col.getDisplayName().equals(selectedColumn))
                        .findFirst()
                        .orElse(null);

                    if (selectedCol != null) {
                        workingData = workingData.stream()
                            .filter(item -> selectedCol.extractValue(item)
                                .toLowerCase()
                                .contains(searchText))
                            .collect(Collectors.toList());
                    }
                }
            }

            if (selectedSortColumn != null && !"None".equals(selectedSortColumn)) {
                TableColumn<T, ?> sortColumn = table.getColumns().stream()
                    .filter(col -> selectedSortColumn.equals(col.getText()))
                    .findFirst()
                    .orElse(null);

                if (sortColumn != null) {
                    table.getSortOrder().clear();
                    table.getSortOrder().add(sortColumn);
                    boolean descending = "Descending".equals(selectedDirection);
                    sortColumn.setSortType(descending
                        ? TableColumn.SortType.DESCENDING
                        : TableColumn.SortType.ASCENDING);

                    Comparator<T> comparator = (a, b) -> {
                        Object valueA = sortColumn.getCellObservableValue(a) != null
                            ? sortColumn.getCellObservableValue(a).getValue()
                            : null;
                        Object valueB = sortColumn.getCellObservableValue(b) != null
                            ? sortColumn.getCellObservableValue(b).getValue()
                            : null;

                        if (valueA == null && valueB == null) return 0;
                        if (valueA == null) return 1;
                        if (valueB == null) return -1;

                        if (valueA instanceof Comparable<?> comparableA && valueA.getClass().isInstance(valueB)) {
                            @SuppressWarnings("unchecked")
                            Comparable<Object> castA = (Comparable<Object>) comparableA;
                            return castA.compareTo(valueB);
                        }

                        return valueA.toString().compareToIgnoreCase(valueB.toString());
                    };

                    if (descending) {
                        comparator = comparator.reversed();
                    }

                    workingData.sort(comparator);
                }
            } else {
                table.getSortOrder().clear();
            }

            int totalResults = workingData.size();
            int pageSize = pageSizeSelector.getValue() == null ? 25 : pageSizeSelector.getValue();
            int totalPages = Math.max(1, (int) Math.ceil((double) totalResults / pageSize));
            currentPage[0] = Math.max(0, Math.min(currentPage[0], totalPages - 1));

            int fromIndex = Math.min(currentPage[0] * pageSize, totalResults);
            int toIndex = Math.min(fromIndex + pageSize, totalResults);
            List<T> pageData = workingData.subList(fromIndex, toIndex);

            table.setItems(FXCollections.observableArrayList(pageData));

            pageInfoLabel.setText("Page " + (currentPage[0] + 1) + " / " + totalPages);
            resultInfoLabel.setText(totalResults + " results");
            prevBtn.setDisable(currentPage[0] <= 0);
            nextBtn.setDisable(currentPage[0] >= totalPages - 1);

            LOGGER.debug(
                "Table filter/sort/paginate: column='{}', text='{}', sort='{} {}', page={}/{}, pageSize={}, results={}",
                selectedColumn,
                searchText,
                selectedSortColumn,
                selectedDirection,
                currentPage[0] + 1,
                totalPages,
                pageSize,
                totalResults
            );
        };

        Runnable resetToFirstPageAndApply = () -> {
            currentPage[0] = 0;
            applyFilterSortAndPaginate.run();
        };

        prevBtn.setOnAction(e -> {
            if (currentPage[0] > 0) {
                currentPage[0]--;
                applyFilterSortAndPaginate.run();
            }
        });

        nextBtn.setOnAction(e -> {
            currentPage[0]++;
            applyFilterSortAndPaginate.run();
        });

        // Bind search on text change and column change
        searchField.textProperty().addListener((obs, oldVal, newVal) -> resetToFirstPageAndApply.run());
        columnSelector.valueProperty().addListener((obs, oldVal, newVal) -> resetToFirstPageAndApply.run());
        sortColumnSelector.valueProperty().addListener((obs, oldVal, newVal) -> resetToFirstPageAndApply.run());
        sortDirectionSelector.valueProperty().addListener((obs, oldVal, newVal) -> resetToFirstPageAndApply.run());
        pageSizeSelector.valueProperty().addListener((obs, oldVal, newVal) -> resetToFirstPageAndApply.run());

        // Clear button action
        clearBtn.setOnAction(e -> {
            searchField.clear();
            columnSelector.setValue("All Columns");
            sortColumnSelector.setValue("None");
            sortDirectionSelector.setValue("Ascending");
            pageSizeSelector.setValue(25);
            table.getSortOrder().clear();
            LOGGER.debug("Search filter cleared");
        });

        searchPanel.getChildren().addAll(
            columnLabel, columnSelector,
            searchLabel, searchField,
            sortLabel, sortColumnSelector, sortDirectionSelector,
            clearBtn
        );

        paginationPanel.getChildren().addAll(
            pageSizeLabel, pageSizeSelector,
            prevBtn, nextBtn,
            pageInfoLabel,
            resultInfoLabel
        );

        container.getChildren().addAll(searchPanel, paginationPanel);

        // Initial render
        applyFilterSortAndPaginate.run();

        return container;
    }

    /**
     * Enables column sorting UI hints (already built-in to JavaFX TableView).
     * This method ensures all columns are sortable and shows visual feedback.
     * 
     * @param <T> The table row type
     * @param table The TableView to configure for sorting
     */
    public static <T> void enableColumnSorting(TableView<T> table) {
        // JavaFX TableView already supports sorting by clicking column headers
        // This method ensures it's enabled and provides logging
        table.getSortOrder().clear();
        
        // Make all columns sortable
        for (TableColumn<T, ?> column : table.getColumns()) {
            column.setSortable(true);
        }
        
        LOGGER.debug("Column sorting enabled for table");
    }

    /**
     * Sets a default sort column for the table.
     * 
     * @param <T> The table row type
     * @param table The TableView to configure
     * @param column The column to sort by
     * @param ascending Whether to sort in ascending order
     */
    public static <T> void setDefaultSort(TableView<T> table, TableColumn<T, ?> column, boolean ascending) {
        column.setSortable(true);
        table.getSortOrder().clear();
        table.getSortOrder().add(column);
        column.setSortType(ascending ? TableColumn.SortType.ASCENDING : TableColumn.SortType.DESCENDING);
        table.sort();
    }

    /**
     * Logs table statistics for debugging.
     * 
     * @param <T> The table row type
     * @param tableName Name of the table (for logging)
     * @param table The TableView
     */
    public static <T> void logTableStats(String tableName, TableView<T> table) {
        int totalRows = table.getItems() != null ? table.getItems().size() : 0;
        int visibleRows = table.getVisibleLeafColumns().size();
        LOGGER.debug("Table '{}': {} rows, {} visible columns",
            tableName, totalRows, visibleRows);
    }
}
