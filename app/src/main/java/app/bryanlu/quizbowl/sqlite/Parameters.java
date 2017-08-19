package app.bryanlu.quizbowl.sqlite;

/**
 * Created by Bryan Lu on 8/17/2017.
 */

public class Parameters {
    private String[] tables;
    private String[] returnColumns;
    private String selection;
    private String[] selectionArgs;

    public Parameters(String[] tables, String[] returnColumns, String selection,
                      String[] selectionArgs) {
        this.tables = tables;
        this.returnColumns = returnColumns;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
    }

    public String[] getTables() {
        return tables;
    }

    public String[] getReturnColumns() {
        return returnColumns;
    }

    public String getSelection() {
        return selection;
    }

    public String[] getSelectionArgs() {
        return selectionArgs;
    }
}
