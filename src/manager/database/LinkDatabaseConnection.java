/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.database;

import components.disable.DisableGUIInput;
import components.progress.JProgressDisplayMenu;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.function.*;
import javax.swing.JProgressBar;
import javax.swing.table.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import manager.links.LinksListModel;
import org.sqlite.*;
import sql.*;
import sql.util.*;

/**
 * This is a specialized {@code Connection} wrapper that connects to the 
 * database file used by Link Manager to store the links it manages.
 * @author Milo Steier
 */
public class LinkDatabaseConnection extends AbstractDatabaseConnection{
    /**
     * This is the name of the implicit column used to store the row ID. The ID 
     * columns of the tables that specify their primary key are aliases for this 
     * column.
     */
    protected static final String ROW_ID_COLUMN_NAME = "rowid";
    /**
     * This is the commonly used alias used when counting the rows in a given 
     * table. This does not refer to an actual column in a table in the 
     * database.
     */
    protected static final String COUNT_COLUMN_NAME = "count";
    /**
     * This is the commonly used alias used when getting the largest index in a 
     * table that is being used as an ordered list of data. This does not refer 
     * to an actual column in a table in the database.
     */
    protected static final String LAST_INDEX_COLUMN_NAME = "lastIndex";
    /**
     * This is the name of the table storing the database schema.
     */
    protected static final String SCHEMA_TABLE_NAME = "sqlite_schema";
    /**
     * This is the name of the view used to get the list of tables, views, and 
     * indexes in the database.
     */
    protected static final String TABLES_VIEW_NAME = "table_list";
    /**
     * This is the name of the view used to get the list of indexes for a table 
     * in the database.
     */
    protected static final String TABLE_INDEX_VIEW_NAME = "table_indexes";
    /**
     * This is the name of the column in the tables view for the names of the 
     * tables, views, and indexes.
     */
    protected static final String TABLES_NAME_COLUMN_NAME = "name";
    /**
     * This is the name of the column in the tables view for the type of item 
     * for that row. In other words, this is the column that identifies whether 
     * something is a table, a view, or an index.
     */
    protected static final String TABLES_TYPE_COLUMN_NAME = "type";
    /**
     * This is the name of the column in the tables view for the SQL query used 
     * to create the item for that row.
     */
    protected static final String TABLES_STRUCTURE_COLUMN_NAME = "sql";
    /**
     * This is the type stored in the {@link #TABLES_TYPE_COLUMN_NAME types 
     * column} of the tables view that indicates that the entry for that row is 
     * a table in the database.
     */
    protected static final String TABLE_TYPE_TABLE = "table";
    /**
     * This is the type stored in the {@link #TABLES_TYPE_COLUMN_NAME types 
     * column} of the tables view that indicates that the entry for that row is 
     * a view in the database.
     */
    protected static final String TABLE_TYPE_VIEW = "view";
    /**
     * This is the type stored in the {@link #TABLES_TYPE_COLUMN_NAME types 
     * column} of the tables view that indicates that the entry for that row is 
     * an index in the database.
     */
    protected static final String TABLE_TYPE_INDEX = "index";
    /**
     * This is the SQL query for creating the tables view if it does not 
     * currently exist. The tables view is used to simplify querying the schema 
     * for the database to get tables, views, and indexes, since SQLite does not 
     * support the {@code SHOW TABLES}, {@code SHOW VIEWS}, and {@code SHOW 
     * INDEXES} queries. This, in combination with some custom collections is 
     * meant to effectively recreate the results of those queries. This view 
     * excludes itself, the table indexes view, and the schema table.
     */
    protected static final String CREATE_TABLES_VIEW_QUERY = String.format(
            "CREATE VIEW IF NOT EXISTS %s AS SELECT %s, %s, %s FROM %s WHERE "+
                    "(type = '%s' AND name NOT LIKE 'sqlite_%%') OR "+
                    "(type = '%s' AND name != '%s' AND name != '%s') OR "+
                    "(type = '%s');", 
            TABLES_VIEW_NAME,               // The view name
            TABLES_NAME_COLUMN_NAME,        // Item name column
            TABLES_TYPE_COLUMN_NAME,        // Item type column
            TABLES_STRUCTURE_COLUMN_NAME,   // Item creation query column
            SCHEMA_TABLE_NAME,              // The schema table
            TABLE_TYPE_TABLE,               // Include tables in the tables view
            TABLE_TYPE_VIEW,                // Include views in the tables view
            TABLES_VIEW_NAME,               // Exclude this view from itself
            TABLE_INDEX_VIEW_NAME,          // Exclude the index view from this view
            TABLE_TYPE_INDEX                // Include indexes in the tables view
    );
    /**
     * This is the name of the column in the table index view for the names of 
     * the indexes.
     */
    protected static final String TABLE_INDEX_NAME_COLUMN_NAME = "name";
    /**
     * This is the name of the column in the table index view for the names of 
     * the tables for the indexes.
     */
    protected static final String TABLE_INDEX_TABLE_COLUMN_NAME = "tableName";
    /**
     * This is the SQL query for creating the table indexes view if it does not 
     * currently exist. The table indexes view is used to simplify querying the 
     * schema for the database to get the indexes for a table, since SQLite does 
     * not support the {@code SHOW INDEXES FROM table} query. This, in 
     * combination with some custom collections is meant to effectively recreate 
     * the results of those queries.
     */
    protected static final String CREATE_TABLE_INDEX_VIEW_QUERY = String.format(
            "CREATE VIEW IF NOT EXISTS %s AS "+ 
                    "SELECT %s, %s AS %s FROM %s WHERE type = '%s';",
            TABLE_INDEX_VIEW_NAME,
            TABLE_INDEX_NAME_COLUMN_NAME,   // Get the name of the index
                // Get the table for the index and show it as "table"
            "tbl_name",TABLE_INDEX_TABLE_COLUMN_NAME,
            SCHEMA_TABLE_NAME,              // The schema table
            TABLE_TYPE_INDEX                // Only include index views
    );
    /**
     * This is a template for creating the foreign key constraints for a table.
     */
    private static final String FOREIGN_KEY_TEMPLATE = 
            "FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE CASCADE";
    /**
     * This generates the query used to replace each instance of a substring 
     * with another substring for a given value. In other words, this returns 
     * the SQL query equivalent of {@code value.replace(oldStr, newStr)}. 
     * @param value The string (or column) to perform the replacement on.
     * @param oldStr The substring to be found in the original string.
     * @param newStr The replacement string.
     * @return The SQL replace query for replacing every instance of {@code 
     * oldStr} in {@code value} with {@code newStr}.
     */
    private static String replaceQuery(String value, String oldStr, String newStr){
        return String.format("REPLACE(%s, \"%s\", \"%s\")", value, oldStr, newStr);
    }
    /**
     * This is a String containing the characters that need to be escaped with a 
     * backslash ({@code \}) to be used as a normal character in an SQL query 
     * using the {@code LIKE} operator with an {@code ESCAPE} clause using a 
     * backslash ({@code \}) as an escape character.
     */
    public static final String PATTERN_CHARACTERS = "\\%_";
    /**
     * This is a template for the conditions to use when searching for text in 
     * the database. This takes in two string parameters via the {@code 
     * String.format} function, and escapes wildcards in the pattern with 
     * backslashes ({@code \}). It is worth noting that the search is case 
     * insensitive. The parameters are as follows: 
     * 
     * <ol start="0">
     *  <li>(String) The column or text to search through.</li>
     *  <li>(String) The pattern used for the search. Any wildcard characters 
     *      ({@code %} and {@code _}) and any backslashes ({@code \}) not 
     *      intended to be used as a wildcard or escape character must be 
     *      escaped using a backslash ({@code \}) to be used as a normal 
     *      character.</li>
     * </ol>
     * @see PATTERN_CHARACTERS
     */
    private static final String TEXT_SEARCH_TEMPLATE="%s LIKE %s ESCAPE \"\\\"";
    /**
     * This formats a given String to replace any of the {@link 
     * #PATTERN_CHARACTERS pattern characters} with their escaped counterparts, 
     * so that they don't act as wildcards or escape characters when searching 
     * for text in the database using the {@code LIKE} operator and an {@code 
     * ESCAPE} clause using backslash ({@code \}) as an escape character.
     * @param str The String to format (cannot be null).
     * @return The formatted String to be used for the search query.
     * @throws NullPointerException If the string is null.
     * @see #PATTERN_CHARACTERS
     * @see generatePatternSelectColumn
     */
    public static String formatSearchQueryPattern(String str){
            // Check if the String is null
        Objects.requireNonNull(str);
            // Go through the pattern characters
        for (char c : PATTERN_CHARACTERS.toCharArray()){
                //Replace any instance of that character with an escaped version
            str = str.replace(Character.toString(c),"\\"+c);
        }
        return str;
    }
    /**
     * This generates a column to use in a {@code SELECT} statement that will 
     * replace any of the {@link #PATTERN_CHARACTERS pattern characters} with 
     * their escaped counterparts, so that they don't act as wildcards or escape 
     * characters when searching for text in the database using the {@code LIKE} 
     * operator and an {@code ESCAPE} clause using backslash ({@code \}) as an 
     * escape character. In other words, this creates the SQL query equivalent 
     * of applying the {@link formatSearchQueryPattern formatSearchQueryPattern} 
     * method for all the values of a column returned by a {@code SELECT} 
     * statement.
     * @param column The original name for the column (cannot be null).
     * @return The column for a {@code SELECT} that will turn text that may 
     * contain pattern wildcards or escape characters into escaped characters.
     * @throws NullPointerException If the column name is null.
     * @see #PATTERN_CHARACTERS
     * @see #formatSearchQueryPattern 
     */
    public static String generatePatternSelectColumn(String column){
            // Check if the String is null
        Objects.requireNonNull(column);
            // Go through the pattern characters
        for (char c : PATTERN_CHARACTERS.toCharArray()){
                // Create a replace statement that will replace any instance of 
                // the current character with an escaped version
            column = replaceQuery(column,Character.toString(c),"\\"+c);
        }
        return column;
    }
    /**
     * This is the name of the table in the database that stores common prefixes 
     * for the links.
     * @see #PREFIX_TABLE_COLUMN_NAMES
     * @see #PREFIX_TABLE_CREATION_QUERY
     */
    public static final String PREFIX_TABLE_NAME = "prefixes";
    /**
     * This is the name of the column in the {@link PREFIX_TABLE_NAME prefix 
     * table} which stores the prefix IDs for the prefixes. This is also the 
     * name of the column in the {@link #LINK_TABLE_NAME link table} for the 
     * prefix ID for the prefix of a given link.
     * @see #PREFIX_TABLE_NAME
     * @see #LINK_TABLE_NAME
     * @see PREFIX_COLUMN_NAME
     * @see PREFIX_TABLE_COLUMN_NAMES
     */
    public static final String PREFIX_ID_COLUMN_NAME = "prefixID";
    /**
     * This is the name of the column in the {@link PREFIX_TABLE_NAME prefix 
     * table} which stores the prefixes for the links. Prefixes must be unique 
     * and not null.
     * @see #PREFIX_TABLE_NAME
     * @see #PREFIX_ID_COLUMN_NAME
     * @see PREFIX_TABLE_COLUMN_NAMES
     */
    public static final String PREFIX_COLUMN_NAME = "linkPrefix";
    /**
     * This is an array containing the names for the columns in the {@link 
     * PREFIX_TABLE_NAME prefix table}.
     * @see PREFIX_TABLE_NAME
     * @see PREFIX_ID_COLUMN_NAME
     * @see PREFIX_COLUMN_NAME
     */
    public static final String[] PREFIX_TABLE_COLUMN_NAMES = {
        PREFIX_ID_COLUMN_NAME,      // prefixID
        PREFIX_COLUMN_NAME          // linkPrefix
    };
    /**
     * This is the query used to create the {@link PREFIX_TABLE_NAME prefix 
     * table} in the database if it did not previously exist.
     * @see PREFIX_TABLE_NAME
     * @see PREFIX_ID_COLUMN_NAME
     * @see PREFIX_COLUMN_NAME
     * @see PREFIX_TABLE_COLUMN_NAMES
     */
    public static final String PREFIX_TABLE_CREATION_QUERY = String.format(
            "CREATE TABLE IF NOT EXISTS %s ("+ 
                        // Prefix ID column definition. Primary key, cannot be 
                        // null
                    "%s integer NOT NULL PRIMARY KEY, "+ 
                        // Prefix column definition. Prefixes are unique and not 
                        // null
                    "%s text NOT NULL UNIQUE);", 
            PREFIX_TABLE_NAME,
            PREFIX_ID_COLUMN_NAME,
            PREFIX_COLUMN_NAME);
    /**
     * This is the name of the view that is used to search for the prefixes that 
     * match any given String. This view has both the prefixID and prefixes that 
     * the {@link #PREFIX_TABLE_NAME prefix table} has, but this view has an 
     * additional column with the prefixes formatted in such a way that they can 
     * be used as patterns to match with a given String. The prefixes are in the 
     * order of longest to shortest.
     * @see #PREFIX_TABLE_NAME
     * @see PREFIX_PATTERN_VIEW_COLUMN_NAMES
     * @see PREFIX_PATTERN_VIEW_CREATION_QUERY
     */
    public static final String PREFIX_PATTERN_VIEW_NAME = "prefixPatterns";
    /**
     * This is the name of the column in the {@link #PREFIX_PATTERN_VIEW_NAME 
     * prefix pattern view} that stores the prefixes in pattern form. The 
     * prefixes in this column will have the wildcard and escape characters (
     * {@code %}, {@code _}, and {@code \}) escaped with a backslash 
     * ({@code \}) so that they count as normal characters, and there will be an 
     * un-escaped percent sign ({@code %}) wildcard at the end of each prefix to 
     * match any sequence of zero or more characters.
     * @see PREFIX_PATTERN_VIEW_NAME
     * @see #generatePatternSelectColumn 
     */
    public static final String PREFIX_PATTERN_COLUMN_NAME = "prefixPattern";
    /**
     * This is an array containing the names for the columns in the {@link 
     * PREFIX_PATTERN_VIEW_NAME prefix pattern view}.
     * @see PREFIX_PATTERN_VIEW_NAME
     * @see PREFIX_ID_COLUMN_NAME
     * @see PREFIX_COLUMN_NAME
     * @see PREFIX_PATTERN_COLUMN_NAME
     * @see PREFIX_TABLE_NAME
     * @see PREFIX_TABLE_COLUMN_NAMES
     */
    public static final String[] PREFIX_PATTERN_VIEW_COLUMN_NAMES = {
        PREFIX_ID_COLUMN_NAME,      // prefixID
        PREFIX_COLUMN_NAME,         // linkPrefix
        PREFIX_PATTERN_COLUMN_NAME  // prefixPattern
    };
    /**
     * This is the query used to create the {@link PREFIX_PATTERN_VIEW_NAME 
     * prefix pattern view} based off the {@link PREFIX_TABLE_NAME prefix table} 
     * in the database if it did not previously exist.
     * @see PREFIX_PATTERN_VIEW_NAME
     * @see PREFIX_ID_COLUMN_NAME
     * @see PREFIX_COLUMN_NAME
     * @see PREFIX_PATTERN_COLUMN_NAME
     * @see PREFIX_PATTERN_VIEW_COLUMN_NAMES
     * @see PREFIX_TABLE_NAME
     */
    public static final String PREFIX_PATTERN_VIEW_CREATION_QUERY = String.format(
            "CREATE VIEW IF NOT EXISTS %s AS "+
                    "SELECT %s, %s, %s AS %s FROM %s "+ 
                            // This view is sorted by the length of the prefixes 
                            // from longest to shortest
                            "ORDER BY LENGTH(%s) DESC;", 
            PREFIX_PATTERN_VIEW_NAME,
            PREFIX_ID_COLUMN_NAME,
            PREFIX_COLUMN_NAME,
                // This is a copy of the prefix colunm, except the prefixes are 
                // formatted in a way where any unintended wildcard characters 
                // are escaped and there is a catch-all wildcard at the end
            generatePatternSelectColumn(PREFIX_COLUMN_NAME)+"||\"%\"",
                // The alias for the second prefix column is the prefix pattern 
            PREFIX_PATTERN_COLUMN_NAME,     // column
                // This view is based off the prefix table
            PREFIX_TABLE_NAME,
                // The column to sort by
            PREFIX_COLUMN_NAME);
    /**
     * This is the name of the table in the database that stores the links.
     * @see #LINK_TABLE_COLUMN_NAMES
     * @see #LINK_TABLE_CREATION_QUERY
     * @see #FULL_LINK_VIEW_NAME
     */
    public static final String LINK_TABLE_NAME = "links";
    /**
     * This is the name of the column in the {@link LINK_TABLE_NAME link table} 
     * which stores the link IDs for the links. This is also for any table or 
     * view that references links, such as the {@link #LIST_DATA_TABLE_NAME list 
     * data table} and the {@link #FULL_LINK_VIEW_NAME full links view}.
     * @see #LINK_TABLE_NAME
     * @see #FULL_LINK_VIEW_NAME
     * @see #DISTINCT_LINK_VIEW_NAME
     * @see #LIST_DATA_TABLE_NAME
     * @see #LIST_CONTENTS_VIEW_NAME
     * @see #LINK_URL_COLUMN_NAME
     */
    public static final String LINK_ID_COLUMN_NAME = "linkID";
    /**
     * This is the name of the column in the {@link LINK_TABLE_NAME link table} 
     * which stores the suffix for the links, and the column in the {@link 
     * #FULL_LINK_VIEW_NAME full links view} which shows the full URL for the 
     * link. This is also used in the {@link DISTINCT_LINK_VIEW_NAME distinct 
     * links view} and the {@link LIST_CONTENTS_VIEW_NAME list contents view} to 
     * reference the full URL for the links. Links cannot be null.
     * @see #LINK_TABLE_NAME
     * @see #FULL_LINK_VIEW_NAME
     * @see #DISTINCT_LINK_VIEW_NAME
     * @see #LIST_CONTENTS_VIEW_NAME
     * @see #LINK_ID_COLUMN_NAME
     */
    public static final String LINK_URL_COLUMN_NAME = "link";
    /**
     * This is an array containing the names for the columns in the {@link 
     * LINK_TABLE_NAME link table}.
     * @see LINK_TABLE_NAME
     * @see LINK_ID_COLUMN_NAME
     * @see PREFIX_ID_COLUMN_NAME
     * @see LINK_URL_COLUMN_NAME
     */
    public static final String[] LINK_TABLE_COLUMN_NAMES = {
        LINK_ID_COLUMN_NAME,        // linkID
        PREFIX_ID_COLUMN_NAME,      // prefixID
        LINK_URL_COLUMN_NAME        // link
    };
    /**
     * This is the query used to create the {@link LINK_TABLE_NAME link table} 
     * in the database if it did not previously exist.
     * @see LINK_TABLE_NAME
     * @see LINK_ID_COLUMN_NAME
     * @see PREFIX_ID_COLUMN_NAME
     * @see LINK_URL_COLUMN_NAME
     * @see LINK_TABLE_COLUMN_NAMES
     */
    public static final String LINK_TABLE_CREATION_QUERY = String.format(
            "CREATE TABLE IF NOT EXISTS %s ("+ 
                        // Link ID column definition. Primary key, cannot be 
                        // null.
                    "%s integer NOT NULL PRIMARY KEY, "+ 
                        // Prefix ID column definition. Cannot be null.
                    "%s integer NOT NULL,"+
                        // Link URL column defintion. Cannot be null.
                    "%s text NOT NULL, "+ 
                        // Foreign key constraint for the prefix ID
                    FOREIGN_KEY_TEMPLATE+");",
            LINK_TABLE_NAME,
            LINK_ID_COLUMN_NAME,
            PREFIX_ID_COLUMN_NAME,
            LINK_URL_COLUMN_NAME,
                // Foreign key constraint for the prefix ID
            PREFIX_ID_COLUMN_NAME,PREFIX_TABLE_NAME, PREFIX_ID_COLUMN_NAME);
    /**
     * This is the name of the view in the database that shows the full URLs 
     * for the links. This view is used to simplify getting the full URL by 
     * combining the link suffixes from the {@link #LINK_TABLE_NAME link table} 
     * with their corresponding prefix from the {@link #PREFIX_TABLE_NAME prefix 
     * table}. This view uses the name for the {@link LINK_URL_COLUMN_NAME link 
     * suffix column} from the link table as the name for the full URL column.
     * @see #LINK_TABLE_NAME
     * @see #PREFIX_TABLE_NAME
     * @see FULL_LINK_VIEW_COLUMN_NAMES
     * @see FULL_LINK_VIEW_CREATION_QUERY
     */
    public static final String FULL_LINK_VIEW_NAME = "fullLinks";
    /**
     * This is an array containing the names for the columns in the {@link 
     * FULL_LINK_VIEW_NAME full links view}.
     * @see FULL_LINK_VIEW_NAME
     * @see LINK_ID_COLUMN_NAME
     * @see LINK_URL_COLUMN_NAME
     * @see LINK_TABLE_NAME
     */
    public static final String[] FULL_LINK_VIEW_COLUMN_NAMES = {
        LINK_ID_COLUMN_NAME,        // linkID
        LINK_URL_COLUMN_NAME        // link
    };
    /**
     * This is the query used to create the {@link FULL_LINK_VIEW_NAME full 
     * links view} in the database if it did not previously exist. 
     * @see FULL_LINK_VIEW_NAME
     * @see FULL_LINK_VIEW_COLUMN_NAMES
     * @see LINK_ID_COLUMN_NAME
     * @see LINK_URL_COLUMN_NAME
     * @see PREFIX_ID_COLUMN_NAME
     * @see #PREFIX_COLUMN_NAME
     * @see #LINK_TABLE_NAME
     * @see #PREFIX_TABLE_NAME
     */
    public static final String FULL_LINK_VIEW_CREATION_QUERY = String.format(
            "CREATE VIEW IF NOT EXISTS %s AS "+
                    "SELECT %s, %s || %s AS %s FROM %s NATURAL JOIN %s;",
            FULL_LINK_VIEW_NAME,
            LINK_ID_COLUMN_NAME,
                // The full link url column. This appends the corresponding 
                // prefix with the link suffix to get the full link url. This 
                // column shares the name as the link suffix column from the 
                // link table
            PREFIX_COLUMN_NAME,LINK_URL_COLUMN_NAME,LINK_URL_COLUMN_NAME,
               // This is a view of a natural join between the links table and 
                // the prefix table
            LINK_TABLE_NAME,PREFIX_TABLE_NAME);
    /**
     * This is the name of the view in the database that contains only the first 
     * instance of the full URLs of the links, along with a count of the 
     * instances of the links. This view is mainly used to detect and ignore 
     * duplicate links in the {@link #FULL_LINK_VIEW_NAME full links view}. The 
     * {@link #LINK_TABLE_NAME link table} allows duplicate entries, and even if 
     * it didn't, it may be difficult to prevent duplicates where the prefix and 
     * suffix are different but combine to make the same value. This view only 
     * allows one instance of each full link while also providing a count of how 
     * many times that full link appears in the full links view.
     * @see LINK_TABLE_NAME
     * @see #FULL_LINK_VIEW_NAME
     * @see DISTINCT_LINK_VIEW_COLUMN_NAMES
     * @see DISTINCT_LINK_VIEW_CREATION_QUERY
     */
    public static final String DISTINCT_LINK_VIEW_NAME = "distinctLinks";
    /**
     * This is the column in the {@link DISTINCT_LINK_VIEW_NAME distinct links 
     * view} that counts how many times a full URL appears in the {@link 
     * #FULL_LINK_VIEW_NAME full links view}.
     * @see DISTINCT_LINK_VIEW_NAME
     * @see LINK_URL_COLUMN_NAME
     */
    public static final String LINK_COUNT_COLUMN_NAME = "linkCount";
    /**
     * This is an array containing the names for the columns in the {@link 
     * DISTINCT_LINK_VIEW_NAME distinct links view}.
     * @see DISTINCT_LINK_VIEW_NAME
     * @see LINK_ID_COLUMN_NAME
     * @see LINK_URL_COLUMN_NAME
     * @see LINK_COUNT_COLUMN_NAME
     * @see FULL_LINK_VIEW_NAME
     */
    public static final String[] DISTINCT_LINK_VIEW_COLUMN_NAMES = {
        LINK_ID_COLUMN_NAME,        // linkID
        LINK_URL_COLUMN_NAME,       // link
        LINK_COUNT_COLUMN_NAME      // linkCount
    };
    /**
     * This is the query used to create the {@link DISTINCT_LINK_VIEW_NAME 
     * distinct links view} in the database if it did not previously exist. 
     * @see DISTINCT_LINK_VIEW_NAME
     * @see DISTINCT_LINK_VIEW_COLUMN_NAMES
     * @see LINK_ID_COLUMN_NAME
     * @see LINK_URL_COLUMN_NAME
     * @see LINK_COUNT_COLUMN_NAME
     * @see FULL_LINK_VIEW_NAME
     */
    public static final String DISTINCT_LINK_VIEW_CREATION_QUERY = String.format(
            "CREATE VIEW IF NOT EXISTS %s AS "+
                    "SELECT MIN(%s) AS %s, %s, COUNT(%s) AS %s FROM %s "+
                            "GROUP BY %s ORDER BY %s;",
            DISTINCT_LINK_VIEW_NAME,
                // Get the lowest linkID for the link and use that as the linkID
            LINK_ID_COLUMN_NAME,LINK_ID_COLUMN_NAME,
            LINK_URL_COLUMN_NAME,
                // Count how many linkIDs reference this link and show that for 
                // the link count column
            LINK_ID_COLUMN_NAME,LINK_COUNT_COLUMN_NAME,
                // This is a view of the full links view
            FULL_LINK_VIEW_NAME,
                // Group by the full link
            LINK_URL_COLUMN_NAME,
                // Sort by the linkID
            LINK_ID_COLUMN_NAME);
    /**
     * This is the name of the view in the database that shows how many links in 
     * the {@link #LINK_TABLE_NAME link table} are using any given prefix in the 
     * {@link #PREFIX_TABLE_NAME prefix table}. 
     * @see #PREFIX_TABLE_NAME
     * @see #LINK_TABLE_NAME
     * @see PREFIX_COUNT_VIEW_COLUMN_NAMES
     * @see PREFIX_COUNT_VIEW_CREATION_QUERY
     */
    public static final String PREFIX_COUNT_VIEW_NAME = "prefixCount";
    /**
     * This is the column in the {@link PREFIX_COUNT_VIEW_NAME prefix count 
     * view} that counts how many times a prefix appears in the {@link 
     * #LINK_TABLE_NAME link table}.
     * @see #PREFIX_TABLE_NAME
     * @see #LINK_TABLE_NAME
     * @see #PREFIX_ID_COLUMN_NAME
     * @see PREFIX_COUNT_VIEW_NAME
     */
    public static final String PREFIX_COUNT_COLUMN_NAME = "prefixCount";
    /**
     * This is an array containing the names for the columns in the {@link 
     * PREFIX_COUNT_VIEW_NAME prefix count view}.
     * @see PREFIX_COUNT_VIEW_NAME
     * @see PREFIX_ID_COLUMN_NAME
     * @see PREFIX_COLUMN_NAME
     * @see PREFIX_COUNT_COLUMN_NAME
     * @see #PREFIX_TABLE_NAME
     * @see #LINK_TABLE_NAME
     */
    public static final String[] PREFIX_COUNT_VIEW_COLUMN_NAMES = {
        PREFIX_ID_COLUMN_NAME,      // prefixID
        PREFIX_COLUMN_NAME,         // linkPrefix
        PREFIX_COUNT_COLUMN_NAME    // prefixCount
    };
    /**
     * This is the query used to create the {@link PREFIX_COUNT_VIEW_NAME prefix 
     * count view} in the database if it did not previously exist. 
     * @see PREFIX_COUNT_VIEW_NAME
     * @see PREFIX_COUNT_VIEW_COLUMN_NAMES
     * @see PREFIX_ID_COLUMN_NAME
     * @see PREFIX_COLUMN_NAME
     * @see PREFIX_COUNT_COLUMN_NAME
     * @see #PREFIX_TABLE_NAME
     * @see #LINK_TABLE_NAME
     */
    public static final String PREFIX_COUNT_VIEW_CREATION_QUERY = String.format(
            "CREATE VIEW IF NOT EXISTS %s AS "+
                    "SELECT %s.%s, %s, COUNT(%s) AS %s "+ 
                            "FROM %s LEFT JOIN %s ON %s.%s = %s.%s "+ 
                                    "GROUP BY %s.%s;",
            PREFIX_COUNT_VIEW_NAME,
                // PrefixID column from the prefix table
            PREFIX_TABLE_NAME, PREFIX_ID_COLUMN_NAME,
            PREFIX_COLUMN_NAME,
                // A count of the Link IDs with the prefix ID shown as the 
                // prefix count
            LINK_ID_COLUMN_NAME, PREFIX_COUNT_COLUMN_NAME,
                // This is a view of a left join between the prefix table and 
                // the links table
            PREFIX_TABLE_NAME, LINK_TABLE_NAME,
                // Joining on the prefix ID columns of the two tables
            PREFIX_TABLE_NAME, PREFIX_ID_COLUMN_NAME,
            LINK_TABLE_NAME, PREFIX_ID_COLUMN_NAME,
                // Group by the prefix ID column
            PREFIX_TABLE_NAME, PREFIX_ID_COLUMN_NAME);
    /**
     * This is the name of the table in the database that stores information 
     * about the lists that store the links.
     * @see LIST_TABLE_COLUMN_NAMES
     * @see LIST_TABLE_CREATION_QUERY
     */
    public static final String LIST_TABLE_NAME = "lists";
    /**
     * This is the name of the column in the {@link LIST_TABLE_NAME list table} 
     * which stores the list IDs for the lists. This is also for any table or 
     * view that references lists of links, such as the {@link 
     * #LIST_DATA_TABLE_NAME list data table} and the {@link 
     * #LIST_SIZE_VIEW_NAME list size view}.
     * @see LIST_TABLE_NAME
     * @see #LIST_OF_LISTS_TABLE_NAME
     * @see #TOTAL_LIST_SIZE_VIEW_NAME
     * @see #LIST_DATA_TABLE_NAME
     * @see #LIST_CONTENTS_VIEW_NAME
     * @see #LIST_SIZE_VIEW_NAME
     * @see #EXCLUSIVE_LISTS_TABLE_NAME
     * @see LIST_NAME_COLUMN_NAME
     */
    public static final String LIST_ID_COLUMN_NAME = "listID";
    /**
     * This is the name of the column in the {@link LIST_TABLE_NAME list table} 
     * that stores the name of the list. The list name must not contain an 
     * asterisk ({@code *}).
     * @see LIST_ID_COLUMN_NAME
     * @see LIST_TABLE_NAME
     * @see LIST_SIZE_VIEW_NAME
     */
    public static final String LIST_NAME_COLUMN_NAME = "listName";
    /**
     * This is the name of the column in the {@link LIST_TABLE_NAME list table} 
     * that stores when the list was last modified. It is planned for this to 
     * potentially be used to detect out of date lists when loading the 
     * database.
     * @see LIST_TABLE_NAME
     */
    public static final String LIST_LAST_MODIFIED_COLUMN_NAME = "lastMod";
    /**
     * This is the name of the column in the {@link LIST_TABLE_NAME list table} 
     * that stores when the list was initially created. 
     * @see LIST_TABLE_NAME
     */
    public static final String LIST_CREATED_COLUMN_NAME = "listCreated";
    /**
     * This is the name of the column in the {@link LIST_TABLE_NAME list table} 
     * that stores an integer containing the flags for the list. 
     * @see LIST_TABLE_NAME
     * @see LinksListModel#ALLOW_DUPLICATES_FLAG
     * @see LinksListModel#READ_ONLY_FLAG
     */
    public static final String LIST_FLAGS_COLUMN_NAME = "listFlags";
    /**
     * This is the name of the column in the {@link LIST_TABLE_NAME list table} 
     * that stores the optional size limit for the list. When null, the list 
     * does not have a set size. If not null, then this cannot be negative.
     * @see LIST_TABLE_NAME
     * @see LIST_SIZE_VIEW_NAME
     */
    public static final String LIST_SIZE_LIMIT_COLUMN_NAME = "sizeLimit";
    /**
     * This is an array containing the names for the columns in the {@link 
     * LIST_TABLE_NAME list table}.
     * @see LIST_TABLE_NAME
     * @see LIST_ID_COLUMN_NAME
     * @see LIST_NAME_COLUMN_NAME
     * @see LIST_LAST_MODIFIED_COLUMN_NAME
     * @see LIST_CREATED_COLUMN_NAME
     * @see LIST_FLAGS_COLUMN_NAME
     * @see LIST_SIZE_LIMIT_COLUMN_NAME
     */
    public static final String[] LIST_TABLE_COLUMN_NAMES = {
        LIST_ID_COLUMN_NAME,            // listID
        LIST_NAME_COLUMN_NAME,          // listName
        LIST_LAST_MODIFIED_COLUMN_NAME, // lastMod
        LIST_CREATED_COLUMN_NAME,       // listCreated
        LIST_FLAGS_COLUMN_NAME,         // listFlags
        LIST_SIZE_LIMIT_COLUMN_NAME     // sizeLimit
    };
    /**
     * This is the query used to create the {@link LIST_TABLE_NAME list table} 
     * in the database if it did not previously exist.
     * @see LIST_TABLE_NAME
     * @see LIST_TABLE_COLUMN_NAMES
     * @see LIST_ID_COLUMN_NAME
     * @see LIST_NAME_COLUMN_NAME
     * @see LIST_LAST_MODIFIED_COLUMN_NAME
     * @see LIST_CREATED_COLUMN_NAME
     * @see LIST_FLAGS_COLUMN_NAME
     * @see LIST_SIZE_LIMIT_COLUMN_NAME
     */
    public static final String LIST_TABLE_CREATION_QUERY = 
            String.format("CREATE TABLE IF NOT EXISTS %s ("+
                        // List ID column definition. Primary key, cannot be 
                        // null
                    "%s integer NOT NULL PRIMARY KEY, "+
                        // List name column definition. Default is null, cannot 
                        // contain *
                    "%s text DEFAULT NULL CHECK(%s NOT LIKE \"%%*%%\"), "+
                        // List last modified column definition. Cannot be null, 
                        // default is 0.
                    "%s BIGINT NOT NULL DEFAULT 0, "+
                        // List creation time column definition. Cannot be null, 
                        // default is 0.
                    "%s BIGINT NOT NULL DEFAULT 0, "+
                        // List flags column definition. Cannot be null, default 
                        // is 0
                    "%s integer NOT NULL DEFAULT 0, "+
                        // List size limit column definition. Default is null, 
                        // cannot be negative
                    "%s integer DEFAULT NULL CHECK(%s > 0));", 
            LIST_TABLE_NAME,
            LIST_ID_COLUMN_NAME,
            LIST_NAME_COLUMN_NAME,
                // List name check
            LIST_NAME_COLUMN_NAME,
            LIST_LAST_MODIFIED_COLUMN_NAME,
            LIST_CREATED_COLUMN_NAME,
            LIST_FLAGS_COLUMN_NAME,
            LIST_SIZE_LIMIT_COLUMN_NAME,
                // List size limit check
            LIST_SIZE_LIMIT_COLUMN_NAME);
    /**
     * This is the name of the table in the database that stores the order in 
     * which the lists of links should appear in. This table effectively acts as 
     * multiple lists of {@link #LINK_ID_COLUMN_NAME list IDs}, with each list 
     * of lists assigned a {@link #LIST_TYPE_COLUMN_NAME list type} to 
     * distinguish between them. If a given index does not appear for a list 
     * type in this table, and that index is less than the largest index for 
     * that list type, then it can be assumed that a {@code null} value is 
     * stored at that index (ideally there would be no gaps in the indexes).
     * @see LIST_TABLE_NAME
     * @see LIST_TYPE_COLUMN_NAME
     * @see LIST_OF_LISTS_INDEX_NAME
     * @see LIST_OF_LISTS_TABLE_COLUMN_NAMES
     * @see LIST_OF_LISTS_TABLE_CREATION_QUERY
     * @see TOTAL_LIST_SIZE_VIEW_NAME
     */
    public static final String LIST_OF_LISTS_TABLE_NAME = "listOfLists";
    /**
     * This is the name of the column in the {@link LIST_OF_LISTS_TABLE_NAME 
     * list of lists table} which stores which list of lists a row belongs to. 
     * In other words, since the list of lists table contains multiple lists of 
     * lists, this is used to distinguish between lists of lists, with different 
     * lists of lists having different list types. This cannot be null, and the 
     * default for this column is {@value LIST_OF_ALL_LISTS_TYPE}.
     * @see LIST_OF_LISTS_TABLE_NAME
     * @see LIST_OF_LISTS_INDEX_NAME
     * @see LIST_INDEX_COLUMN_NAME
     * @see #LIST_OF_ALL_LISTS_TYPE
     * @see #LIST_OF_SHOWN_LISTS_TYPE
     */
    public static final String LIST_TYPE_COLUMN_NAME = "listType";
    /**
     * This is the list type for the list of lists that shows all the lists, 
     * including hidden lists. This list should ideally contain at least one 
     * instance of all the lists of links.
     * @see LIST_OF_LISTS_TABLE_NAME
     * @see LIST_TYPE_COLUMN_NAME
     * @see LIST_OF_SHOWN_LISTS_TYPE
     */
    public static final int LIST_OF_ALL_LISTS_TYPE = 0;
    /**
     * This is the list type for the list of lists that shows only non-hidden 
     * lists. Any list that does not appear in this list can be considered to be 
     * a hidden list.
     * @see LIST_OF_LISTS_TABLE_NAME
     * @see LIST_TYPE_COLUMN_NAME
     * @see LIST_OF_ALL_LISTS_TYPE
     */
    public static final int LIST_OF_SHOWN_LISTS_TYPE = 1;
    /**
     * This is the name of the column in the {@link LIST_OF_LISTS_TABLE_NAME 
     * list of lists table} which stores the indexes for the lists in a list. 
     * The index for the lists indicates in which order they will appear in the 
     * list of lists. Each index must be unique for a given {@link 
     * LIST_TYPE_COLUMN_NAME list type} (i.e. a list type of 0 cannot have two 
     * indexes that are both set to 3), and cannot be null or negative. Any 
     * missing indexes for a given list type should be considered to have a 
     * {@code null} value stored at the missing indexes.
     * @see LIST_OF_LISTS_TABLE_NAME
     * @see LIST_OF_LISTS_INDEX_NAME
     * @see LIST_TYPE_COLUMN_NAME
     */
    public static final String LIST_INDEX_COLUMN_NAME = "listIndex";
    /**
     * This is an array containing the names for the columns in the {@link 
     * LIST_OF_LISTS_TABLE_NAME list of lists table}.
     * @see LIST_OF_LISTS_TABLE_NAME
     * @see LIST_OF_LISTS_INDEX_NAME
     * @see LIST_TYPE_COLUMN_NAME
     * @see LIST_INDEX_COLUMN_NAME
     * @see LIST_ID_COLUMN_NAME
     */
    public static final String[] LIST_OF_LISTS_TABLE_COLUMN_NAMES = {
        LIST_TYPE_COLUMN_NAME,      // listType
        LIST_INDEX_COLUMN_NAME,     // listIndex
        LIST_ID_COLUMN_NAME         // listID
    };
    /**
     * This is the query used to create the {@link LIST_OF_LISTS_TABLE_NAME 
     * list of lists table} in the database if it did not previously exist.
     * @see LIST_OF_LISTS_TABLE_NAME
     * @see LIST_OF_LISTS_TABLE_COLUMN_NAMES
     * @see LIST_OF_LISTS_INDEX_NAME
     * @see LIST_TYPE_COLUMN_NAME
     * @see LIST_INDEX_COLUMN_NAME
     * @see LIST_ID_COLUMN_NAME
     */
    public static final String LIST_OF_LISTS_TABLE_CREATION_QUERY = String.format(
            "CREATE TABLE IF NOT EXISTS %s ("+
                        // List type column definition. Cannot be null, default 
                        // is the all lists list
                    "%s integer NOT NULL DEFAULT %d, "+
                        // List index column definition. Cannot be null or 
                        // negative
                    "%s integer NOT NULL CHECK(%s >= 0), "+
                        // List ID column definition. Default is null
                    "%s integer DEFAULT NULL, "+
                        // Foreign key constraint for the list ID
                    FOREIGN_KEY_TEMPLATE+", "+
                        // Unique constraint for list type and index
                    "UNIQUE (%s, %s));",
            LIST_OF_LISTS_TABLE_NAME,
            LIST_TYPE_COLUMN_NAME,
                // Default for the list type
            LIST_OF_ALL_LISTS_TYPE,
            LIST_INDEX_COLUMN_NAME,
                // List index check
            LIST_INDEX_COLUMN_NAME,
            LIST_ID_COLUMN_NAME,
                // Foreign key constraint for the list ID
            LIST_ID_COLUMN_NAME,LIST_TABLE_NAME,LIST_ID_COLUMN_NAME,
                // Unique constraint for list type and index
            LIST_TYPE_COLUMN_NAME, LIST_INDEX_COLUMN_NAME);
    /**
     * This is the index applied to the {@link #LIST_OF_LISTS_TABLE_NAME list of 
     * lists table} used sort by the {@link #LIST_TYPE_COLUMN_NAME list type} 
     * and {@link #LIST_INDEX_COLUMN_NAME index}. This is a unique index, 
     * requiring each instance to have a unique type-index combination.
     * @see LIST_OF_LISTS_TABLE_NAME
     * @see LIST_OF_LISTS_INDEX_CREATION_QUERY
     * @see LIST_TYPE_COLUMN_NAME
     * @see LIST_INDEX_COLUMN_NAME
     */
    public static final String LIST_OF_LISTS_INDEX_NAME = "indexOfList";
    /**
     * This is the query used to create the {@link LIST_OF_LISTS_INDEX_NAME list 
     * of lists index} in the database if it did not previously exist.
     * @see LIST_OF_LISTS_INDEX_NAME
     * @see LIST_OF_LISTS_TABLE_NAME
     * @see LIST_OF_LISTS_TABLE_CREATION_QUERY
     * @see LIST_TYPE_COLUMN_NAME
     * @see LIST_INDEX_COLUMN_NAME
     */
    public static final String LIST_OF_LISTS_INDEX_CREATION_QUERY = String.format(
            "CREATE UNIQUE INDEX IF NOT EXISTS %s ON %s(%s, %s);",
                LIST_OF_LISTS_INDEX_NAME,
                    // Applied on the list of lists table
                LIST_OF_LISTS_TABLE_NAME,
                    // First column is the list type
                LIST_TYPE_COLUMN_NAME,
                    // Second column is the list index
                LIST_INDEX_COLUMN_NAME);
    /**
     * This is the name of the table in the database that stores the contents of 
     * the lists of links. This table effectively acts as multiple lists of 
     * {@link #LINK_ID_COLUMN_NAME link IDs} representing links, with each list 
     * of links using the {@link #LINK_ID_COLUMN_NAME list ID} of the 
     * corresponding list in the {@link #LIST_TABLE_NAME list table}. If a given 
     * index does not appear for a list in this table, and that index is less 
     * than the largest index for that list, then it can be assumed that a 
     * {@code null} value is stored at that index (ideally there would be no 
     * gaps in the indexes).
     * @see LIST_TABLE_NAME
     * @see #LINK_TABLE_NAME
     * @see #FULL_LINK_VIEW_NAME
     * @see LIST_DATA_TABLE_COLUMN_NAMES
     * @see LIST_DATA_TABLE_CREATION_QUERY
     * @see LIST_CONTENTS_VIEW_NAME
     * @see LIST_DATA_INDEX_NAME
     * @see LIST_SIZE_VIEW_NAME
     * @see TOTAL_LIST_SIZE_VIEW_NAME
     */
    public static final String LIST_DATA_TABLE_NAME = "listData";
    /**
     * This is the name of the column in the {@link LIST_DATA_TABLE_NAME list  
     * data table} which stores the indexes for the links in a list. The index 
     * for the links indicates in which order they will appear in the list. Each 
     * index must be unique for a given {@link LIST_ID_COLUMN_NAME list ID} 
     * (i.e. a list ID of 0 cannot have two indexes that are both set to 3), and 
     * cannot be null or negative. Any missing indexes for a given list ID 
     * should be considered to have a {@code null} value stored at the missing 
     * indexes.
     * @see LIST_DATA_TABLE_NAME
     * @see LIST_DATA_INDEX_NAME
     * @see LIST_CONTENTS_VIEW_NAME
     * @see LIST_ID_COLUMN_NAME
     */
    public static final String LINK_INDEX_COLUMN_NAME = "linkIndex";
    /**
     * This is an array contains the names for the columns in the {@link 
     * LIST_DATA_TABLE_NAME list data table}.
     * @see LIST_DATA_TABLE_NAME
     * @see LIST_DATA_INDEX_NAME
     * @see LIST_CONTENTS_VIEW_NAME
     * @see LIST_ID_COLUMN_NAME
     * @see LINK_INDEX_COLUMN_NAME
     * @see LINK_ID_COLUMN_NAME
     */
    public static final String[] LIST_DATA_TABLE_COLUMN_NAMES = {
        LIST_ID_COLUMN_NAME,        // listID
        LINK_INDEX_COLUMN_NAME,     // linkIndex
        LINK_ID_COLUMN_NAME         // linkID
    };
    /**
     * This is the query used to create the {@link LIST_DATA_TABLE_NAME list 
     * data table} in the database if it did not previously exist.
     * @see LIST_DATA_TABLE_NAME
     * @see LIST_DATA_TABLE_COLUMN_NAMES
     * @see LIST_DATA_INDEX_NAME
     * @see LIST_CONTENTS_VIEW_NAME
     * @see LIST_ID_COLUMN_NAME
     * @see LINK_INDEX_COLUMN_NAME
     * @see LINK_ID_COLUMN_NAME
     * @see #LIST_TABLE_NAME
     * @see #FULL_LINK_VIEW_NAME
     * @see #LINK_TABLE_NAME
     * @see #PREFIX_TABLE_NAME
     */
    public static final String LIST_DATA_TABLE_CREATION_QUERY = String.format(
            "CREATE TABLE IF NOT EXISTS %s ("+ 
                        // List ID column definition. Cannot be null
                    "%s integer NOT NULL, "+ 
                        // Link index column defintion. Cannot be null or 
                        // negative
                    "%s integer NOT NULL CHECK(%s >= 0), "+
                        // Link ID column definition. Default is null
                    "%s integer DEFAULT NULL, "+ 
                        // Foreign key constraint for the list ID
                    FOREIGN_KEY_TEMPLATE+", "+ 
                        // Foreign key constraint for the link ID
                    FOREIGN_KEY_TEMPLATE+", "+
                        // Unique constraint for list ID and link index
                    "UNIQUE (%s, %s));",
            LIST_DATA_TABLE_NAME,
            LIST_ID_COLUMN_NAME,
            LINK_INDEX_COLUMN_NAME,
                // Link index check
            LINK_INDEX_COLUMN_NAME,
            LINK_ID_COLUMN_NAME,
                // Foreign key constraint for the list ID
            LIST_ID_COLUMN_NAME,LIST_TABLE_NAME,LIST_ID_COLUMN_NAME,
                // Foreign key constraint for the link ID
            LINK_ID_COLUMN_NAME,LINK_TABLE_NAME,LINK_ID_COLUMN_NAME,
                // Unique constraint for list ID and link index
            LIST_ID_COLUMN_NAME,LINK_INDEX_COLUMN_NAME);
    /**
     * This is the index applied to the {@link #LIST_DATA_TABLE_NAME list data 
     * table} used sort by the {@link #LIST_ID_COLUMN_NAME list ID} and {@link 
     * #LINK_INDEX_COLUMN_NAME index}. This is a unique index, requiring each 
     * instance to have a unique ID-index combination.
     * @see LIST_DATA_TABLE_NAME
     * @see LIST_DATA_INDEX_CREATION_QUERY
     * @see LIST_ID_COLUMN_NAME
     * @see LINK_INDEX_COLUMN_NAME
     */
    public static final String LIST_DATA_INDEX_NAME = "listEntry";
    /**
     * This is the query used to create the {@link LIST_DATA_INDEX_NAME list 
     * data index} in the database if it did not previously exist.
     * @see LIST_DATA_INDEX_NAME
     * @see LIST_DATA_TABLE_NAME
     * @see LIST_DATA_TABLE_CREATION_QUERY
     * @see LIST_ID_COLUMN_NAME
     * @see LINK_INDEX_COLUMN_NAME
     */
    public static final String LIST_DATA_INDEX_CREATION_QUERY = String.format(
            "CREATE UNIQUE INDEX IF NOT EXISTS %s ON %s(%s, %s);",
                LIST_DATA_INDEX_NAME,
                    // Applied on the list data table
                LIST_DATA_TABLE_NAME,
                    // First column is the list ID
                LIST_ID_COLUMN_NAME,
                    // Second column is the link index
                LINK_INDEX_COLUMN_NAME);
    /**
     * This is the name of the view in the database that shows the contents of 
     * the lists of links in the {@link LIST_DATA_TABLE_NAME list data table}. 
     * This view is effectively a copy of the list data table, with the 
     * exception that it also shows the full URLs of the links in the lists. In 
     * other words, this view is equivalent to merging the list data table and 
     * the {@link #FULL_LINK_VIEW_NAME full links view}. This view is used to 
     * simplify getting the links stored in a list by providing a way to get the 
     * full link stored at a given index in a list without having to query both 
     * the list data table and the full links view. This view also includes the 
     * {@link #LINK_ID_COLUMN_NAME link IDs} for the links for convenience.
     * @see LIST_CONTENTS_VIEW_COLUMN_NAMES
     * @see LIST_CONTENTS_VIEW_CREATION_QUERY
     * @see #LIST_DATA_TABLE_NAME
     * @see #FULL_LINK_VIEW_NAME
     * @see #LIST_TABLE_NAME
     * @see #LINK_TABLE_NAME
     * @see #PREFIX_TABLE_NAME
     * @see LIST_ID_COLUMN_NAME
     * @see LINK_INDEX_COLUMN_NAME
     * @see LINK_ID_COLUMN_NAME
     * @see LINK_URL_COLUMN_NAME
     */
    public static final String LIST_CONTENTS_VIEW_NAME = "listContents";
    /**
     * This is an array containing the names for the columns in the {@link 
     * LIST_CONTENTS_VIEW_NAME list contents view}.
     * @see LIST_CONTENTS_VIEW_NAME
     * @see LIST_DATA_TABLE_NAME
     * @see LIST_ID_COLUMN_NAME
     * @see LINK_INDEX_COLUMN_NAME
     * @see LINK_ID_COLUMN_NAME
     * @see LINK_URL_COLUMN_NAME
     */
    public static final String[] LIST_CONTENTS_VIEW_COLUMN_NAMES = {
        LIST_ID_COLUMN_NAME,        // listID
        LINK_INDEX_COLUMN_NAME,     // linkIndex
        LINK_ID_COLUMN_NAME,        // linkID
        LINK_URL_COLUMN_NAME        // link
    };
    /**
     * This is the query used to create the {@link LIST_CONTENTS_VIEW_NAME list 
     * contents view} in the database if it did not previously exist.
     * @see LIST_CONTENTS_VIEW_NAME
     * @see LIST_CONTENTS_VIEW_COLUMN_NAMES
     * @see LIST_DATA_TABLE_NAME
     * @see #LIST_TABLE_NAME
     * @see #LINK_TABLE_NAME
     * @see #FULL_LINK_VIEW_NAME
     * @see LIST_ID_COLUMN_NAME
     * @see LINK_INDEX_COLUMN_NAME
     * @see LINK_ID_COLUMN_NAME
     * @see LINK_URL_COLUMN_NAME
     */
    public static final String LIST_CONTENTS_VIEW_CREATION_QUERY = String.format(
            "CREATE VIEW IF NOT EXISTS %s AS "+
                    "SELECT %s, %s, %s, %s FROM %s NATURAL JOIN %s "+
                            "ORDER BY %s, %s;",
            LIST_CONTENTS_VIEW_NAME,
            LIST_ID_COLUMN_NAME,
            LINK_INDEX_COLUMN_NAME,
            LINK_ID_COLUMN_NAME,
            LINK_URL_COLUMN_NAME,
                // This is a view of a natural join between the list data table 
                // and the full links view
            LIST_DATA_TABLE_NAME, FULL_LINK_VIEW_NAME,
                // Sort by the list ID first and the link index second
            LIST_ID_COLUMN_NAME, LINK_INDEX_COLUMN_NAME);
    /**
     * This is the name of the view in the database that shows the size of the 
     * lists in the {@link LIST_TABLE_NAME list table} based off the contents of 
     * the {@link LIST_DATA_TABLE_NAME list data table}.
     * @see LIST_TABLE_NAME
     * @see LIST_DATA_TABLE_NAME
     * @see LIST_SIZE_VIEW_COLUMN_NAMES
     * @see LIST_SIZE_VIEW_CREATION_QUERY
     */
    public static final String LIST_SIZE_VIEW_NAME = "listSize";
    /**
     * This is the column in the {@link LIST_SIZE_VIEW_NAME list size view} that 
     * displays the size of the lists in the {@link LIST_DATA_TABLE_NAME list 
     * data table}.
     * @see LIST_TABLE_NAME
     * @see LIST_DATA_TABLE_NAME
     * @see #LIST_ID_COLUMN_NAME
     * @see LIST_SIZE_VIEW_NAME
     */
    public static final String LIST_SIZE_COLUMN_NAME = "listSize";
    /**
     * This is an array containing the names for the columns in the {@link 
     * LIST_SIZE_VIEW_NAME list size view}.
     * @see LIST_SIZE_VIEW_NAME
     * @see LIST_DATA_TABLE_NAME
     * @see LIST_CONTENTS_VIEW_NAME
     * @see LIST_TABLE_NAME
     * @see LIST_ID_COLUMN_NAME
     * @see LIST_NAME_COLUMN_NAME
     * @see LIST_SIZE_LIMIT_COLUMN_NAME
     * @see LIST_SIZE_COLUMN_NAME
     */
    public static final String[] LIST_SIZE_VIEW_COLUMN_NAMES = {
        LIST_ID_COLUMN_NAME,            // listID
        LIST_NAME_COLUMN_NAME,          // listName
        LIST_SIZE_LIMIT_COLUMN_NAME,    // sizeLimit
        LIST_SIZE_COLUMN_NAME           // listSize
    };
    /**
     * This is the query used to create the {@link LIST_SIZE_VIEW_NAME list size
     * view} in the database if it did not previously exist.
     * @see LIST_SIZE_VIEW_NAME
     * @see LIST_SIZE_VIEW_COLUMN_NAMES
     * @see LIST_DATA_TABLE_NAME
     * @see LIST_CONTENTS_VIEW_NAME
     * @see LIST_TABLE_NAME
     * @see LIST_ID_COLUMN_NAME
     * @see LIST_NAME_COLUMN_NAME
     * @see LIST_SIZE_LIMIT_COLUMN_NAME
     * @see LIST_SIZE_COLUMN_NAME
     */
    public static final String LIST_SIZE_VIEW_CREATION_QUERY = String.format(
            "CREATE VIEW IF NOT EXISTS %s AS "+
                    "SELECT %s.%s, %s, %s, MAX(%s)+1 AS %s "+ 
                            "FROM %s LEFT JOIN %s ON %s.%s = %s.%s "+ 
                                    "GROUP BY %s.%s;",
            LIST_SIZE_VIEW_NAME,
                // List ID column from the list table
            LIST_TABLE_NAME,LIST_ID_COLUMN_NAME,
            LIST_NAME_COLUMN_NAME,
            LIST_SIZE_LIMIT_COLUMN_NAME,
            /* This column gets the largest index for the list plus 1 in order 
            to get the size of the list. This accounts for any gaps in the list 
            by using the last index in the list to determine the size of the 
            list instead of the amount of entries in the table for that list. 
            This column is refered to as the list size column. */
            LINK_INDEX_COLUMN_NAME, LIST_SIZE_COLUMN_NAME,
                // This is a view of a left join between the list table and the 
                // list data table
            LIST_TABLE_NAME, LIST_DATA_TABLE_NAME,
                // Joining on the list ID columns of the two tables
            LIST_TABLE_NAME, LIST_ID_COLUMN_NAME,
            LIST_DATA_TABLE_NAME, LIST_ID_COLUMN_NAME,
                // Group by the list ID column
            LIST_TABLE_NAME, LIST_ID_COLUMN_NAME);
    /**
     * This is the name of the view in the database that shows the size and 
     * total size of the lists of lists in the {@link #LIST_OF_LISTS_TABLE_NAME 
     * list of lists table}.
     * @see #LIST_OF_LISTS_TABLE_NAME
     * @see #LIST_TABLE_NAME
     * @see #LIST_DATA_TABLE_NAME
     * @see #LIST_SIZE_VIEW_NAME
     * @see TOTAL_LIST_SIZE_VIEW_COLUMN_NAMES
     * @see TOTAL_LIST_SIZE_VIEW_CREATION_QUERY
     */
    public static final String TOTAL_LIST_SIZE_VIEW_NAME = "listTotalSize";
    /**
     * This is the column in the {@link TOTAL_LIST_SIZE_VIEW_NAME total list 
     * size view} that counts how many lists are in a list of lists in the 
     * {@link #LIST_OF_LISTS_TABLE_NAME list of lists table}.
     * @see #LIST_OF_LISTS_TABLE_NAME
     * @see #LIST_TABLE_NAME
     * @see #LIST_TYPE_COLUMN_NAME
     * @see TOTAL_LIST_SIZE_VIEW_NAME
     */
    public static final String LIST_COUNT_COLUMN_NAME = "listCount";
    /**
     * This is the column in the {@link TOTAL_LIST_SIZE_VIEW_NAME total list 
     * size view} that shows the sum of the sizes of the lists in a list of 
     * lists in the {@link #LIST_OF_LISTS_TABLE_NAME list of lists table}, based 
     * off the sizes of those lists in the {@link LIST_SIZE_VIEW_NAME list size 
     * view}.
     * @see #LIST_OF_LISTS_TABLE_NAME
     * @see #LIST_TABLE_NAME
     * @see #LIST_DATA_TABLE_NAME
     * @see #LIST_SIZE_VIEW_NAME
     * @see #LIST_TYPE_COLUMN_NAME
     * @see #LIST_ID_COLUMN_NAME
     * @see #LIST_SIZE_COLUMN_NAME
     * @see TOTAL_LIST_SIZE_VIEW_NAME
     */
    public static final String TOTAL_LIST_SIZE_COLUMN_NAME = "totalSize";
    /**
     * This is an array containing the names for the columns in the {@link 
     * TOTAL_LIST_SIZE_VIEW_NAME list total size view}.
     * @see TOTAL_LIST_SIZE_VIEW_NAME
     * @see LIST_OF_LISTS_TABLE_NAME
     * @see LIST_SIZE_VIEW_NAME
     * @see LIST_TYPE_COLUMN_NAME
     * @see LIST_COUNT_COLUMN_NAME
     * @see TOTAL_LIST_SIZE_COLUMN_NAME
     */
    public static final String[] TOTAL_LIST_SIZE_VIEW_COLUMN_NAMES = {
        LIST_TYPE_COLUMN_NAME,          // listType
        LIST_COUNT_COLUMN_NAME,         // listCount
        TOTAL_LIST_SIZE_COLUMN_NAME     // totalSize
    };
    /**
     * This is the query used to create the {@link TOTAL_LIST_SIZE_VIEW_NAME 
     * list total size view} in the database if it did not previously exist.
     * @see TOTAL_LIST_SIZE_VIEW_NAME
     * @see TOTAL_LIST_SIZE_VIEW_COLUMN_NAMES
     * @see LIST_OF_LISTS_TABLE_NAME
     * @see LIST_SIZE_VIEW_NAME
     * @see LIST_TYPE_COLUMN_NAME
     * @see LIST_COUNT_COLUMN_NAME
     * @see TOTAL_LIST_SIZE_COLUMN_NAME
     */
    public static final String TOTAL_LIST_SIZE_VIEW_CREATION_QUERY = String.format(
            "CREATE VIEW IF NOT EXISTS %s AS "+
                    "SELECT %s, MAX(%s)+1 AS %s, SUM(%s) AS %s "+
                            "FROM %s NATURAL JOIN %s GROUP BY %s ORDER BY %s;",
            TOTAL_LIST_SIZE_VIEW_NAME,
            LIST_TYPE_COLUMN_NAME,
            /* This column gets the largest index for the list of lists plus 1 
            in order to get the size of the list. This accounts for any gaps in 
            the list by using the last index in the list to determine the size 
            of the list of lists instead of the amount of entries in the table 
            for that list of lists. This column is refered to as the list count 
            column. */
            LIST_INDEX_COLUMN_NAME, LIST_COUNT_COLUMN_NAME,
            // This column gets the sum of the sizes of the lists in the list. 
            // This column is refered to as the total size of the list of lists.
            LIST_SIZE_COLUMN_NAME, TOTAL_LIST_SIZE_COLUMN_NAME,
                // This is a view of a natural join between the list of lists 
                // table and the list size view
            LIST_OF_LISTS_TABLE_NAME, LIST_SIZE_VIEW_NAME,
                // Group by the list type
            LIST_TYPE_COLUMN_NAME,
                // Sort by the list type
            LIST_TYPE_COLUMN_NAME);
    /**
     * This is the name of the table in the database that stores 
     */
    public static final String EXCLUSIVE_LISTS_TABLE_NAME = "exclusiveLists";
    /**
     * 
     */
    public static final String EXCLUSIVE_LIST_ID_1_COLUMN_NAME = "listID1";
    /**
     * 
     */
    public static final String EXCLUSIVE_LIST_ID_2_COLUMN_NAME = "listID2";
    /**
     * 
     */
    public static final String EXCLUSIVITY_MODE_COLUMN_NAME = "exclusivityMode";
    /**
     * This is the list exclusivity mode that indicates that the list that the 
     * link was most recently added to takes priority
     */
    public static final int MOST_RECENTLY_ADDED_TO_EXCLUSIVITY_MODE = 0;
    /**
     * This is the list exclusivity mode that indicates that the first list will 
     * take priority and get to keep any links shared between the two lists
     */
    public static final int LIST_1_TAKES_PRIORITY_EXCLUSIVITY_MODE = 1;
    /**
     * This is the list exclusivity mode that indicates that the second list will 
     * take priority and get to keep any links shared between the two lists
     */
    public static final int LIST_2_TAKES_PRIORITY_EXCLUSIVITY_MODE = 2;
    /**
     * This is the list exclusivity mode that indicates that the list that 
     * currently contains the link takes priority
     */
    public static final int CURRENTLY_ADDED_TO_EXCLUSIVITY_MODE = 3;
    /**
     * 
     */
    public static final int FIRST_EXCLUSIVITY_MODE = 
            MOST_RECENTLY_ADDED_TO_EXCLUSIVITY_MODE;
    /**
     * 
     */
    public static final int LAST_EXCLUSIVITY_MODE = 
            CURRENTLY_ADDED_TO_EXCLUSIVITY_MODE;
    /**
     * This is an array containing the names for the columns in the {@link 
     * EXCLUSIVE_LISTS_TABLE_NAME exclusive list table}.
     * @see EXCLUSIVE_LISTS_TABLE_NAME
     * @see EXCLUSIVE_LISTS_TABLE_CREATION_QUERY
     * @see LIST_TABLE_NAME
     * @see LIST_ID_COLUMN_NAME
     * @see EXCLUSIVE_LIST_ID_1_COLUMN_NAME
     * @see EXCLUSIVE_LIST_ID_2_COLUMN_NAME
     * @see EXCLUSIVITY_MODE_COLUMN_NAME
     * @see EXCLUSIVE_LISTS_INDEX_NAME
     */
    public static final String[] EXCLUSIVE_LISTS_TABLE_COLUMN_NAMES = {
        EXCLUSIVE_LIST_ID_1_COLUMN_NAME,        // listID1
        EXCLUSIVE_LIST_ID_2_COLUMN_NAME,        // listID2
        EXCLUSIVITY_MODE_COLUMN_NAME            // exclusivityMode
    };
    /**
     * This is the query used to create the {@link EXCLUSIVE_LISTS_TABLE_NAME 
     * exclusive list table} in the database if it did not previously exist.
     * @see EXCLUSIVE_LISTS_TABLE_NAME
     * @see EXCLUSIVE_LISTS_TABLE_COLUMN_NAMES
     * @see EXCLUSIVE_LISTS_INDEX_NAME
     * @see EXCLUSIVE_LISTS_INDEX_CREATION_QUERY
     * @see LIST_TABLE_NAME
     * @see LIST_ID_COLUMN_NAME
     * @see EXCLUSIVE_LIST_ID_1_COLUMN_NAME
     * @see EXCLUSIVE_LIST_ID_2_COLUMN_NAME
     * @see EXCLUSIVITY_MODE_COLUMN_NAME
     * @see FIRST_EXCLUSIVITY_MODE
     * @see LAST_EXCLUSIVITY_MODE
     * @see MOST_RECENTLY_ADDED_TO_EXCLUSIVITY_MODE
     * @see LIST_1_TAKES_PRIORITY_EXCLUSIVITY_MODE
     * @see LIST_2_TAKES_PRIORITY_EXCLUSIVITY_MODE
     * @see CURRENTLY_ADDED_TO_EXCLUSIVITY_MODE
     */
    public static final String EXCLUSIVE_LISTS_TABLE_CREATION_QUERY = String.format(
            "CREATE TABLE IF NOT EXISTS %s ("+
                        // The list ID for the first list. Cannot be null.
                    "%s integer NOT NULL,"+
                        // The list ID for the second list. Cannot be null or 
                        // equal to the first list
                    "%s integer NOT NULL CHECK(%s != %s),"+
                        // The list exclusivity mode. Cannot be null, nor out 
                        // range for the exclusivity modes. The default is the 
                        // most recently added mode
                    "%s integer NOT NULL DEFAULT %d CHECK (%s >= %d AND %s <= %d),"+
                        // Foreign key constraint for the list ID for list 1
                    FOREIGN_KEY_TEMPLATE+", "+ 
                        // Foreign key constraint for the list ID for list 2
                    FOREIGN_KEY_TEMPLATE+", "+
                        // Unique constraint for the list IDs for the two lists
                    "UNIQUE (%s, %s));",
            EXCLUSIVE_LISTS_TABLE_NAME,
            EXCLUSIVE_LIST_ID_1_COLUMN_NAME,
            EXCLUSIVE_LIST_ID_2_COLUMN_NAME,
                // Check to ensure the two list IDs are not the same
            EXCLUSIVE_LIST_ID_1_COLUMN_NAME,EXCLUSIVE_LIST_ID_2_COLUMN_NAME,
            EXCLUSIVITY_MODE_COLUMN_NAME, 
                // Default list exclisivity mode is for the most recently added 
                // list to get the link
            MOST_RECENTLY_ADDED_TO_EXCLUSIVITY_MODE,
                // Check to ensure the list exclusivity mode is not too low
            EXCLUSIVITY_MODE_COLUMN_NAME, FIRST_EXCLUSIVITY_MODE,
                // Check to ensure the list exclusivity mode is not too high
            EXCLUSIVITY_MODE_COLUMN_NAME, LAST_EXCLUSIVITY_MODE,
                // Foreign key constraint for the list ID for list 1
            EXCLUSIVE_LIST_ID_1_COLUMN_NAME,LIST_TABLE_NAME,LIST_ID_COLUMN_NAME,
                // Foreign key constraint for the list ID for list 2
            EXCLUSIVE_LIST_ID_2_COLUMN_NAME,LIST_TABLE_NAME,LIST_ID_COLUMN_NAME,
                // Unique constraint for the list IDs for the two lists
            EXCLUSIVE_LIST_ID_1_COLUMN_NAME, EXCLUSIVE_LIST_ID_2_COLUMN_NAME
    );
    /**
     * This is the index applied to the {@link #EXCLUSIVE_LISTS_TABLE_NAME 
     * exclusive list table} used sort by the two list ID columns. This is a 
     * unique index, requiring each instance to have a unique ID-ID combination.
     * @see EXCLUSIVE_LISTS_TABLE_NAME
     * @see EXCLUSIVE_LISTS_INDEX_CREATION_QUERY
     * @see LIST_ID_COLUMN_NAME
     * @see EXCLUSIVE_LIST_ID_1_COLUMN_NAME
     * @see EXCLUSIVE_LIST_ID_2_COLUMN_NAME
     */
    public static final String EXCLUSIVE_LISTS_INDEX_NAME = "exclusivityEntry";
    /**
     * This is the query used to create the {@link EXCLUSIVE_LISTS_INDEX_NAME 
     * exclusive list index} in the database if it did not previously exist.
     * @see EXCLUSIVE_LISTS_TABLE_NAME
     * @see EXCLUSIVE_LISTS_TABLE_CREATION_QUERY
     * @see LIST_ID_COLUMN_NAME
     * @see EXCLUSIVE_LIST_ID_1_COLUMN_NAME
     * @see EXCLUSIVE_LIST_ID_2_COLUMN_NAME
     */
    public static final String EXCLUSIVE_LISTS_INDEX_CREATION_QUERY = String.format(
            "CREATE UNIQUE INDEX IF NOT EXISTS %s ON %s(%s, %s);",
                EXCLUSIVE_LISTS_INDEX_NAME,
                EXCLUSIVE_LISTS_TABLE_NAME,
                EXCLUSIVE_LIST_ID_1_COLUMN_NAME, 
                EXCLUSIVE_LIST_ID_2_COLUMN_NAME
    );
    /**
     * This is the name of the table used to store the settings for the 
     * database. There are three columns in this table, the {@link 
     * DATABASE_CONFIG_KEY_COLUMN_NAME key} column, the {@link 
     * DATABASE_CONFIG_DEFAULT_COLUMN_NAME default value} column, and the {@link 
     * DATABASE_CONFIG_VALUE_COLUMN_NAME value} column. The key column is the 
     * column used to indicate the setting. The value column stores the value 
     * set for that setting, as a String, or a null if that setting has not been 
     * explicitly set. The default value column stores the value to use for that 
     * setting when no value is explicitly set, as a String, or null if that 
     * setting does not have a default value.
     * @see DATABASE_CONFIG_KEY_COLUMN_NAME
     * @see DATABASE_CONFIG_DEFAULT_COLUMN_NAME
     * @see DATABASE_CONFIG_VALUE_COLUMN_NAME
     * @see DATABASE_CONFIG_TABLE_COLUMN_NAMES
     * @see DATABASE_CONFIG_TABLE_CREATION_QUERY
     */
    public static final String DATABASE_CONFIG_TABLE_NAME = "dbConfig";
    /**
     * This is the column in the {@link DATABASE_CONFIG_TABLE_NAME database 
     * settings table} for the keys for the settings. Each key must be unique 
     * and cannot be null.
     * @see DATABASE_CONFIG_TABLE_NAME
     * @see DATABASE_CONFIG_DEFAULT_COLUMN_NAME
     * @see DATABASE_CONFIG_VALUE_COLUMN_NAME
     */
    public static final String DATABASE_CONFIG_KEY_COLUMN_NAME = "propertyName";
    /**
     * This is the column in the {@link DATABASE_CONFIG_TABLE_NAME database 
     * settings table} for the default value column. This is the String used 
     * when no value is explicitly set for a setting. If a setting does not have 
     * a default value, then this will contain null for that setting.
     * @see DATABASE_CONFIG_TABLE_NAME
     * @see DATABASE_CONFIG_KEY_COLUMN_NAME
     * @see DATABASE_CONFIG_VALUE_COLUMN_NAME
     */
    public static final String DATABASE_CONFIG_DEFAULT_COLUMN_NAME = 
            "propertyDefault";
    /**
     * This is the column in the {@link DATABASE_CONFIG_TABLE_NAME database 
     * settings table} for the value column. This is the value String set for a 
     * given setting. This will be null for settings that do not have a value 
     * set, in which case the {@link DATABASE_CONFIG_DEFAULT_COLUMN_NAME default 
     * value} will be used instead if there is one.
     * @see DATABASE_CONFIG_TABLE_NAME
     * @see DATABASE_CONFIG_KEY_COLUMN_NAME
     * @see DATABASE_CONFIG_DEFAULT_COLUMN_NAME
     */
    public static final String DATABASE_CONFIG_VALUE_COLUMN_NAME = 
            "propertyValue";
    /**
     * This is an array containing the names of the columns in the {@link 
     * DATABASE_CONFIG_TABLE_NAME database settings table}.
     * @see DATABASE_CONFIG_TABLE_NAME
     * @see DATABASE_CONFIG_TABLE_CREATION_QUERY
     * @see DATABASE_CONFIG_KEY_COLUMN_NAME
     * @see DATABASE_CONFIG_DEFAULT_COLUMN_NAME
     * @see DATABASE_CONFIG_VALUE_COLUMN_NAME
     */
    public static final String[] DATABASE_CONFIG_TABLE_COLUMN_NAMES = {
        DATABASE_CONFIG_KEY_COLUMN_NAME,        // propertyName
        DATABASE_CONFIG_DEFAULT_COLUMN_NAME,    // propertyDefault
        DATABASE_CONFIG_VALUE_COLUMN_NAME       // propertyValue
    };
    /**
     * This is the query used to create the {@link DATABASE_CONFIG_TABLE_NAME 
     * database settings table} in the database if it did not previously exist.
     * @see DATABASE_CONFIG_TABLE_NAME
     * @see DATABASE_CONFIG_TABLE_COLUMN_NAMES
     * @see DATABASE_CONFIG_KEY_COLUMN_NAME
     * @see DATABASE_CONFIG_DEFAULT_COLUMN_NAME
     * @see DATABASE_CONFIG_VALUE_COLUMN_NAME
     */
    public static final String DATABASE_CONFIG_TABLE_CREATION_QUERY = String.format(
            "CREATE TABLE IF NOT EXISTS %s ("+
                        // The setting column. Cannot be null and must be unique
                    "%s text NOT NULL UNIQUE, "+
                        // The default value column. Default is null
                    "%s text DEFAULT NULL, "+
                        // The set value column. Default is null
                    "%s text DEFAULT NULL);",
            DATABASE_CONFIG_TABLE_NAME,
            DATABASE_CONFIG_KEY_COLUMN_NAME,
            DATABASE_CONFIG_DEFAULT_COLUMN_NAME,
            DATABASE_CONFIG_VALUE_COLUMN_NAME);
    /**
     * This is an array containing the queries used to create the tables, views, 
     * and indexes in the database.
     * @see PREFIX_TABLE_CREATION_QUERY
     * @see PREFIX_PATTERN_VIEW_CREATION_QUERY
     * @see LINK_TABLE_CREATION_QUERY
     * @see FULL_LINK_VIEW_CREATION_QUERY
     * @see DISTINCT_LINK_VIEW_CREATION_QUERY
     * @see PREFIX_COUNT_VIEW_CREATION_QUERY
     * @see LIST_TABLE_CREATION_QUERY
     * @see LIST_OF_LISTS_TABLE_CREATION_QUERY
     * @see LIST_OF_LISTS_INDEX_CREATION_QUERY
     * @see LIST_DATA_TABLE_CREATION_QUERY
     * @see LIST_DATA_INDEX_CREATION_QUERY
     * @see LIST_CONTENTS_VIEW_CREATION_QUERY
     * @see LIST_SIZE_VIEW_CREATION_QUERY
     * @see TOTAL_LIST_SIZE_VIEW_CREATION_QUERY
     * @see EXCLUSIVE_LISTS_TABLE_CREATION_QUERY
     * @see EXCLUSIVE_LISTS_INDEX_CREATION_QUERY
     * @see DATABASE_CONFIG_TABLE_CREATION_QUERY
     * @see #createTables(Statement) 
     * @see #createTables() 
     */
    public static final String[] TABLE_CREATION_QUERIES = {
        PREFIX_TABLE_CREATION_QUERY,                // Prefix table
        PREFIX_PATTERN_VIEW_CREATION_QUERY,         // Prefix pattern view
        LINK_TABLE_CREATION_QUERY,                  // Link table
        FULL_LINK_VIEW_CREATION_QUERY,              // Full links view
        DISTINCT_LINK_VIEW_CREATION_QUERY,          // Distinct links view
        PREFIX_COUNT_VIEW_CREATION_QUERY,           // Prefix count view
        LIST_TABLE_CREATION_QUERY,                  // List table
        LIST_OF_LISTS_TABLE_CREATION_QUERY,         // List of lists table
        LIST_OF_LISTS_INDEX_CREATION_QUERY,         // List of lists index
        LIST_DATA_TABLE_CREATION_QUERY,             // List data table
        LIST_DATA_INDEX_CREATION_QUERY,             // List data index
        LIST_CONTENTS_VIEW_CREATION_QUERY,          // List contents view
        LIST_SIZE_VIEW_CREATION_QUERY,              // List size view
        TOTAL_LIST_SIZE_VIEW_CREATION_QUERY,        // Total size view
//        EXCLUSIVE_LISTS_TABLE_CREATION_QUERY,       // Exclusive lists table
//        EXCLUSIVE_LISTS_INDEX_CREATION_QUERY,       // Exclusive lists index
        DATABASE_CONFIG_TABLE_CREATION_QUERY        // Database settings table
    };
    
    
    
    
    /**
     * This is an array containing the initial prefixes to use to populate the 
     * prefix map. These are prefixes that are likely to be common. This array 
     * contains the empty prefix, which must be present in the prefix table. 
     * The empty prefix is an empty String and is used when no other prefix 
     * matches a given String. There are no guarantees for the {@link 
     * #PREFIX_ID_COLUMN_NAME prefix IDs} of these prefixes, as prefix IDs are 
     * typically assigned on a first-come-first-served basis. Typically these 
     * will be assigned the first few prefix IDs of a newly created database.
     * @see #PREFIX_TABLE_NAME
     * @see #PREFIX_COLUMN_NAME
     * @see #createTables(Statement) 
     * @see #createTables() 
     * @see PrefixMap#createPrefixesFrom(Collection) 
     */
    protected static final String[] INITIAL_LINK_PREFIXES = {
        "",
        "http://",
        "http://www.",
        "https://",
        "https://www.",
        "https://www.reddit.com/",
        "https://www.youtube.com/",
        "https://www.google.com/"
    };
    /**
     * This is the key for the minimum amount of links that need to share a 
     * given prefix for that prefix to be automatically added to the database.
     * @see #DATABASE_CONFIG_TABLE_NAME
     * @see #getDatabaseProperties() 
     */
    public static final String PREFIX_THRESHOLD_CONFIG_KEY = "PrefixThreshold";
    /**
     * This is the default minimum amount of links that need to share a given 
     * prefix for that prefix to be automatically added to the database.
     * @see #DATABASE_CONFIG_TABLE_NAME
     * @see #getDatabaseProperties() 
     * @see PREFIX_THRESHOLD_CONFIG_KEY
     */
    private static final int PREFIX_THRESHOLD_CONFIG_DEFAULT = 100;
    /**
     * This is the key for the characters at which to split at when creating a 
     * prefix, so as to avoid creating unnecessary prefixes and filling up the 
     * prefix table while only storing 1 letter links.
     * @see #DATABASE_CONFIG_TABLE_NAME
     * @see #getDatabaseProperties() 
     */
    public static final String PREFIX_SEPARATORS_CONFIG_KEY ="PrefixSeparators";
    /**
     * These are the default characters at which to split at when creating a 
     * prefix, so as to avoid creating unnecessary prefixes and filling up the 
     * prefix table while only storing 1 letter links.
     * @see #DATABASE_CONFIG_TABLE_NAME
     * @see #getDatabaseProperties() 
     * @see PREFIX_SEPARATORS_CONFIG_KEY
     */
    private static final String PREFIX_DEFAULT_SEPARATORS = "/.=?&+-_\\";
    /**
     * This is the key for the version of the database.
     */
    public static final String DATABASE_VERSION_CONFIG_KEY = "DBVersion";
    
    protected static final String DEFAULT_DATABASE_VERSION = "0.5.0";
    
//    public static final String DATABASE_LAST_MODIFIED_CONFIG_KEY = "lastModified";
//    
//    private static final String DATABASE_LAST_MODIFIED_CONFIG_DEFAULT = "0";
    
    public static final int DATABASE_MAJOR_VERSION = 3;
    
    public static final int DATABASE_MINOR_VERSION = 1;
    
    public static final int DATABASE_PATCH_VERSION = 0;
    
    public static final String DATABASE_VERSION = String.format("%d.%d.%d", 
            DATABASE_MAJOR_VERSION,
            DATABASE_MINOR_VERSION,
            DATABASE_PATCH_VERSION);
    /**
     * This is the key for the UUID of this database file. This uses UUIDv4.
     */
    public static final String DATABASE_UUID = "DatabaseID";
    /**
     * 0: Property name <br>
     * 1: Property default value <br>
     * 2: Property value
     */
    private static final String[][] CONFIG_DEFAULT_VALUES = {
        {PREFIX_THRESHOLD_CONFIG_KEY, Integer.toString(PREFIX_THRESHOLD_CONFIG_DEFAULT), null},
        {PREFIX_SEPARATORS_CONFIG_KEY, PREFIX_DEFAULT_SEPARATORS, null},
        {DATABASE_VERSION_CONFIG_KEY,DEFAULT_DATABASE_VERSION, DATABASE_VERSION}
    };
    
    protected static final String TABLE_SIZE_QUERY_TEMPLATE = 
            "SELECT COUNT(%s) AS "+COUNT_COLUMN_NAME+" FROM %s";
    
    protected static final String TABLE_CONTAINS_QUERY_TEMPLATE = 
            TABLE_SIZE_QUERY_TEMPLATE+" WHERE %s = ?";
    /**
     * This is the query used to search for prefixes that match a given String. 
     * This searches through the {@link #PREFIX_PATTERN_VIEW_NAME prefix pattern 
     * view} and returns the matching prefixID and prefixes in order of longest 
     * to shortest. It is worth noting that the search is case insensitive, so 
     * the matches returned by this query may not match exactly. The parameters 
     * for a prepared statement are as follows: 
     * 
     * <ol>
     *  <li>(String) The text to get the matching prefixes for.</li>
     * </ol>
     */
    private static final String PREFIX_SEARCH_QUERY = String.format(
            "SELECT %s, %s FROM %s WHERE "+TEXT_SEARCH_TEMPLATE, 
                    // Get the prefixID
                PREFIX_ID_COLUMN_NAME,
                    // Get the prefix
                PREFIX_COLUMN_NAME,
                    // Search the prefix pattern view
                PREFIX_PATTERN_VIEW_NAME,
                    // Look for prefixes like the given string
                "?",
                    // Look through the prefix pattern column
                PREFIX_PATTERN_COLUMN_NAME);
    /**
     * These are the conditions to use to get all the linkIDs that have the same 
     * link, excluding the one that will be used in the end. This is used when 
     * removing the duplicate links and is appended to the filter for the update 
     * and delete queries. The parameters for a prepared statement are as 
     * follows: 
     * <ol>
     *  <li>(String) The link to get the duplicates of.</li>
     *  <li>(Integer) The linkID for the link that will remain after the 
     *      duplicates have been removed. In other words, this is the linkID 
     *      that will be kept.</li>
     * </ol>
     */
    private static final String DUPLICATE_LINK_ID_QUERY_CONDITIONS = String.format(
            "%s IN (SELECT %s FROM %s WHERE %s = ? AND %s != ?)", 
                LINK_ID_COLUMN_NAME,
                LINK_ID_COLUMN_NAME,
                FULL_LINK_VIEW_NAME,
                LINK_URL_COLUMN_NAME,
                LINK_ID_COLUMN_NAME);
    /**
     * This returns a sorted query <p>
     * 
     * SELECT columnNames FROM tableName WHERE conditions ORDER BY sortColumn (DESC) LIMIT limit
     * 
     * 
     * @param columnNames The names of the columns for the query to return.
     * @param tableName The name of the table to query.
     * @param conditions The conditions for the query, or null.
     * @param sortColumn The column(s) by which to sort by.
     * @param descending Whether the results will be in ascending or descending 
     * order ({@code true} for descending, {@code false} for ascending).
     * @param limit The limit on the amount of rows that the query will return.
     * @return 
     */
    protected static String getSortedQuery(String columnNames,String tableName, 
            String conditions,String sortColumn,boolean descending,Integer limit){
        return String.format("SELECT %s FROM %s%s ORDER BY %s%s%s", 
                columnNames,
                tableName,
                    // If any conditions were provided, include them with a 
                    // WHERE clause. Otherwise, leave it blank.
                (conditions!=null)?" WHERE "+conditions:"",
                sortColumn,
                    // If this should be descending, specifiy it. Otherwise, 
                    // leave it blank
                (descending)?" DESC":"",
                    // If there is a limit, specifiy it. Otherwise, leave it 
                    // blank.
                (limit!=null)?" LIMIT "+limit:"");
    }
    
    public static final String LIST_CONTENTS_SEARCH_QUERY_TEMPLATE = String.format(
            "SELECT %s, %s, %s, %s FROM %s WHERE %%s ORDER BY %s, %s", 
                LIST_ID_COLUMN_NAME,
                LINK_INDEX_COLUMN_NAME,
                LINK_ID_COLUMN_NAME,
                LINK_URL_COLUMN_NAME,
                LIST_CONTENTS_VIEW_NAME,
                LIST_ID_COLUMN_NAME,
                LINK_INDEX_COLUMN_NAME);
    
    public static final String LINK_TABLE_LIST_CONTENTS_SEARCH_QUERY_TEMPLATE = 
            String.format(LIST_CONTENTS_SEARCH_QUERY_TEMPLATE, String.format(
                    "%s IN (SELECT %s FROM %s WHERE %%s)", 
                        LINK_ID_COLUMN_NAME,
                        LINK_ID_COLUMN_NAME,
                        LINK_TABLE_NAME)
            );
    
    public static final String USED_PREFIX_SEARCH_QUERY_TEMPLATE = String.format(
            LINK_TABLE_LIST_CONTENTS_SEARCH_QUERY_TEMPLATE,
                PREFIX_ID_COLUMN_NAME+" = ?");
    
    public static String getListContentsSearchQuery(boolean textSearch, 
            boolean prefixSearch){
        if (prefixSearch){
            String query = PREFIX_ID_COLUMN_NAME+" = ?";
            if (textSearch)
                query += " AND "+String.format(TEXT_SEARCH_TEMPLATE, 
                        LINK_URL_COLUMN_NAME,"?");
            return String.format(LINK_TABLE_LIST_CONTENTS_SEARCH_QUERY_TEMPLATE, query);
        } else if (textSearch)
            return String.format(LIST_CONTENTS_SEARCH_QUERY_TEMPLATE, 
                    String.format(TEXT_SEARCH_TEMPLATE,LINK_URL_COLUMN_NAME,"?"));
        else
            return String.format("SELECT %s, %s, %s, %s FROM %s ORDER BY %s, %s", 
                    LIST_ID_COLUMN_NAME,
                    LINK_INDEX_COLUMN_NAME,
                    LINK_ID_COLUMN_NAME,
                    LINK_URL_COLUMN_NAME,
                    LIST_CONTENTS_VIEW_NAME,
                    LIST_ID_COLUMN_NAME,
                    LINK_INDEX_COLUMN_NAME);
    }
    
    private static final String REPLACE_PREFIX_ID_QUERY = String.format(
            "UPDATE %s SET %s = ? WHERE %s = ?",
                LINK_TABLE_NAME,
                PREFIX_ID_COLUMN_NAME,
                PREFIX_ID_COLUMN_NAME);
    
    private static final String REPLACE_PREFIX_ID_IN_LIST_QUERY = String.format(
            "%s AND %s IN (SELECT %s FROM %s WHERE %s = ?)", 
                REPLACE_PREFIX_ID_QUERY,
                LINK_ID_COLUMN_NAME,
                LINK_ID_COLUMN_NAME,
                LIST_DATA_TABLE_NAME,
                LIST_ID_COLUMN_NAME);
    /**
     * The database information used when updating the database prior to the 
     * current version.
     * 
     * First column of each row of each two-dimensional array is the table name. 
     * The remaining columns contain the names of the columns in the table.
     * 
     * <ol start="0">
     *  <li>Version 0.0.0 (Initial release):
     *      <ol start="0">
     *          <li>Links Table (linkID, listID, link index, link)</li>
     *          <li>List Table (listID, index, name, last modified)</li>
     *      </ol>
     *  </li>
     *  <li>Version 0.2.0 (Prior to prefixes):
     *      <ol start="0">
     *          <li>Links Table (linkID, listID, link index, link)</li>
     *          <li>List Table (listID, index, name, last modified, flags)</li>
     *      </ol>
     *  </li>
     *  <li>Version 0.3.0 (Link table has indexes):
     *      <ol start="0">
     *          <li>Links Table (linkID, listID, link index, prefixID, link)</li>
     *          <li>Prefix Table (prefixID, prefix)</li>
     *          <li>List Table (listID, index, name, last modified, flags)</li>
     *      </ol>
     *  </li>
     *  <li>Version 0.5.0 (List data is separate from List table):
     *      <ol start="0">
     *          <li>Prefix Table (prefixID, prefix)</li>
     *          <li>Links Table (linkID, prefixID, link)</li>
     *          <li>List Table (listID, index, name, flags, size limit, last modified)</li>
     *          <li>List Data Table (listID, linkID, link index)</li>
     *          <li>Full Links View (linkID, link)</li>
     *          <li>List Entry Index on the List Data Table (listData, listID, linkID)</li>
     *      </ol>
     *  </li>
     * </ol>
     * 
     */
    private static final String[][][] OLD_DATABASE_TABLES = {
        {           // Version 0.0.0
                // The link table and its columns
            {"links","linkID","listID","listIndex","url"},
                // The list table and its columns
            {"lists","listID","tabIndex","tabName","lastMod"}
        }, {        // Version 0.2.0
                // The link table and its columns
            {"links","linkID","listID","linkIndex","link"},
                // The list table and its columns
            {"lists","listID","listIndex","listName","lastMod","listFlags"}
        }, {        // Version 0.3.0
                // The link table and its columns
            {"links","linkID","listID","linkIndex","prefixID","link"},
                // The prefix table and its columns
            {"prefixes","prefixID","linkPrefix"},
                // The list table and its columns
            {"lists","listID","listIndex","listName","lastMod","listFlags"}
        }, {        // Version 0.5.0
                // The prefix table and its columns
            {"prefixes","prefixID","linkPrefix"},
                // The link table and its columns
            {"links","linkID","prefixID","link"},
                // The list table and its columns
            {"lists","listID","listIndex","listName","listFlags","sizeLimit", "lastMod"},
                // The list data table and its columns
            {"listData","listID","linkID","linkIndex"},
                // The full links view and its columns
            {"fullLinks","linkID","link"},
                // The list entry index on the list data table and the columns 
                // it effects
            {"listEntry","listData","listID","linkID"}
        }
    };
    /**
     * The database definitions used when updating the database prior to the 
     * current version. 
     * 
     * <ol start="0">
     *  <li>Version 0.3.0 - Prefix table is introduced</li>
     *  <li>Version 0.5.0 - List data table and size limit column are introduced</li>
     * </ol>
     * 
     */
    private static final String[][] OLD_DATABASE_DEFINITIONS = {
        {           // Version 0.3.0
                // The prefix table creation query
            String.format(
                    "CREATE TABLE IF NOT EXISTS %s ("+ 
                            "%s integer NOT NULL PRIMARY KEY, "+ 
                            "%s text NOT NULL UNIQUE);", 
                    OLD_DATABASE_TABLES[2][1][0],
                    OLD_DATABASE_TABLES[2][1][1],
                    OLD_DATABASE_TABLES[2][1][2]),
                // The list table creation query
            String.format(
                    "CREATE TABLE IF NOT EXISTS %s ("+
                            "%s integer NOT NULL PRIMARY KEY, "+ 
                            "%s integer, "+
                            "%s text NOT NULL UNIQUE, "+ 
                            "%s BIGINT DEFAULT 0,"+ 
                            "%s integer DEFAULT 0);",
                    OLD_DATABASE_TABLES[2][2][0],
                    OLD_DATABASE_TABLES[2][2][1],
                    OLD_DATABASE_TABLES[2][2][2],
                    OLD_DATABASE_TABLES[2][2][3],
                    OLD_DATABASE_TABLES[2][2][4],
                    OLD_DATABASE_TABLES[2][2][5]),
                // The link table creation query
            String.format(
                    "CREATE TABLE IF NOT EXISTS %s ("+ 
                            "%s integer NOT NULL PRIMARY KEY, "+ 
                            "%s integer, "+ 
                            "%s integer, "+ 
                            "%s integer DEFAULT 1, "+
                            "%s text DEFAULT NULL, "+
                            FOREIGN_KEY_TEMPLATE+", "+ 
                            FOREIGN_KEY_TEMPLATE+");",
                    OLD_DATABASE_TABLES[2][0][0],
                    OLD_DATABASE_TABLES[2][0][1],
                    OLD_DATABASE_TABLES[2][0][2],
                    OLD_DATABASE_TABLES[2][0][3],
                    OLD_DATABASE_TABLES[2][0][4],
                    OLD_DATABASE_TABLES[2][0][5],
                    OLD_DATABASE_TABLES[2][0][2],
                    OLD_DATABASE_TABLES[2][2][0],
                    OLD_DATABASE_TABLES[2][2][1],
                    OLD_DATABASE_TABLES[2][0][4],
                    OLD_DATABASE_TABLES[2][1][0],
                    OLD_DATABASE_TABLES[2][1][1])
        }, {        // Version 0.5.0
                // The list table creation query
            String.format(
                    "CREATE TABLE IF NOT EXISTS %s ("+
                            "%s integer NOT NULL PRIMARY KEY, "+ 
                            "%s integer DEFAULT NULL, "+
                            "%s text NOT NULL UNIQUE, "+ 
                            "%s BIGINT NOT NULL DEFAULT 0,"+ 
                            "%s integer NOT NULL DEFAULT 0,"+
                            "%s integer DEFAULT NULL);",
                    OLD_DATABASE_TABLES[3][2][0],
                    OLD_DATABASE_TABLES[3][2][1],
                    OLD_DATABASE_TABLES[3][2][2],
                    OLD_DATABASE_TABLES[3][2][3],
                    OLD_DATABASE_TABLES[3][2][6],
                    OLD_DATABASE_TABLES[3][2][4],
                    OLD_DATABASE_TABLES[3][2][5]),
                // The prefix table creation query
            String.format(
                    "CREATE TABLE IF NOT EXISTS %s ("+ 
                            "%s integer NOT NULL PRIMARY KEY, "+ 
                            "%s text NOT NULL UNIQUE);", 
                    OLD_DATABASE_TABLES[3][0][0],
                    OLD_DATABASE_TABLES[3][0][1],
                    OLD_DATABASE_TABLES[3][0][2]),
                // The link table creation query
            String.format(
                    "CREATE TABLE IF NOT EXISTS %s ("+ 
                            "%s integer NOT NULL PRIMARY KEY, "+ 
                            "%s integer NOT NULL DEFAULT 1,"+
                            "%s text NOT NULL, "+ 
                            FOREIGN_KEY_TEMPLATE+");",
                    OLD_DATABASE_TABLES[3][1][0],
                    OLD_DATABASE_TABLES[3][1][1],
                    OLD_DATABASE_TABLES[3][1][2],
                    OLD_DATABASE_TABLES[3][1][3],
                    OLD_DATABASE_TABLES[3][1][2],
                    OLD_DATABASE_TABLES[3][0][0], 
                    OLD_DATABASE_TABLES[3][0][1]),
                // The full link view creation query
            String.format(
                    "CREATE VIEW IF NOT EXISTS %s AS SELECT %s, %s || %s AS %s"+
                            " FROM %s NATURAL JOIN %s;", 
                    OLD_DATABASE_TABLES[3][4][0],
                    OLD_DATABASE_TABLES[3][1][1],
                    OLD_DATABASE_TABLES[3][0][2],
                    OLD_DATABASE_TABLES[3][1][3],
                    OLD_DATABASE_TABLES[3][4][2],
                    OLD_DATABASE_TABLES[3][1][0],
                    OLD_DATABASE_TABLES[3][0][0]),
                // The list data table creation query
            String.format(
                    "CREATE TABLE IF NOT EXISTS %s ("+ 
                            "%s integer NOT NULL, "+ 
                            "%s integer NOT NULL, "+ 
                            "%s integer DEFAULT NULL, "+
                            FOREIGN_KEY_TEMPLATE+", "+ 
                            FOREIGN_KEY_TEMPLATE+");",
                    OLD_DATABASE_TABLES[3][3][0],
                    OLD_DATABASE_TABLES[3][3][1],
                    OLD_DATABASE_TABLES[3][3][2],
                    OLD_DATABASE_TABLES[3][3][3],
                    OLD_DATABASE_TABLES[3][3][1],
                    OLD_DATABASE_TABLES[3][2][0],
                    OLD_DATABASE_TABLES[3][2][1],
                    OLD_DATABASE_TABLES[3][3][2],
                    OLD_DATABASE_TABLES[3][1][0],
                    OLD_DATABASE_TABLES[3][1][1]),
                // The listEntry index for the list data table creation query
            String.format(
                    "CREATE UNIQUE INDEX IF NOT EXISTS %s ON %s(%s, %s);",
                    OLD_DATABASE_TABLES[3][5][0],
                    OLD_DATABASE_TABLES[3][5][1],
                    OLD_DATABASE_TABLES[3][5][2],
                    OLD_DATABASE_TABLES[3][5][3])
        }
    };
    /**
     * This is a set view of the tables in the database. This is initially null 
     * and is initialized the first time it is used.
     */
    private TableSet dbTableSet = null;
    /**
     * This is a set view of the views in the database. This is initially null 
     * and is initialized the first time it is used.
     */
    private TableSet dbViewSet = null;
    /**
     * This is a set view of the indexes in the database. This is initially null 
     * and is initialized the first time it is used.
     */
    private TableSet dbIndexSet = null;
    /**
     * This is a map view of the structure of the tables in the database. This 
     * is initially null and is initialized the first time it is used.
     */
    private TableStructureMap dbStructureMap = null;
    /**
     * This is a map view of the prefix table. This is initially null and is 
     * initialized the first time it is used.
     */
    private PrefixMap prefixMap = null;
    /**
     * This is a map view of the link table. This is initially null and is 
     * initialized the first time it is used.
     */
    private LinkMap linkMap = null;
    /**
     * This is a map view of the listID and name columns in the list table. This 
     * is initially null and is initialized the first time it is used.
     */
    private ListNameMap listNameMap = null;
    /**
     * This is a set view of the types of lists. This is initially null and is 
     * initialized the first time it is used.
     */
    private SQLSet<Integer> listTypeSet = null;
    /**
     * This is a map containing the list views of the list IDs. This is 
     * initially null and is initialized the first time it is used. The lists 
     * are populated as they are requested.
     */
    private Map<Integer, ListIDList> listIDLists = null;
    /**
     * This is a map view of the list contents. This is initially null and is 
     * initialized the first time it is used.
     */
    private ListDataMap listDataMap = null;
    /**
     * This is a map view of the properties for the database. This is initially 
     * null and is initialized the first time it is used.
     */
    private DatabasePropertyMap propMap = null;
    /**
     * This is the SQLiteConfig used to construct this connection if one was 
     * provided.
     */
    private SQLiteConfig config;
    /**
     * This constructs a LinkDatabaseConnection that wraps the given connection.
     * @param conn The connection to wrap (cannot be null)
     * @throws NullPointerException If the connection is null.
     */
    protected LinkDatabaseConnection(Connection conn){
        super(conn);
        config = null;
    }
    /**
     * This constructs a LinkDatabaseConnection that connects to the SQL 
     * database located at the given file path.
     * @param fileName The file path for the database to connect to.
     * @throws SQLException If a database access error occurs.
     * @throws NullPointerException If the file name is null.
     */
    public LinkDatabaseConnection(String fileName) throws SQLException{
        this(DriverManager.getConnection("jdbc:sqlite:"+
                Objects.requireNonNull(fileName)));
    }
    /**
     * This constructs a LinkDatabaseConnection that connects to the SQL 
     * database located at the given file path and uses the given properties.
     * @param fileName The file path for the database to connect to.
     * @param properties The properties for the database connection. This is a 
     * list of arbitrary string tag/value pairs to use as connection arguments.
     * @throws SQLException If a database access error occurs.
     * @throws NullPointerException If the file name is null.
     */
    public LinkDatabaseConnection(String fileName, Properties properties) 
            throws SQLException{
        this(DriverManager.getConnection("jdbc:sqlite:"+
                Objects.requireNonNull(fileName), properties));
    }
    /**
     * This constructs a LinkDatabaseConnection that connects to the SQL 
     * database located at the given file path and uses the given SQLite 
     * configuration.
     * @param fileName The file path for the database to connect to.
     * @param config The SQLiteConfig object containing the properties for the 
     * connection.
     * @throws SQLException If a database access error occurs.
     * @throws NullPointerException If the file name is null.
     */
    public LinkDatabaseConnection(String fileName, SQLiteConfig config) 
            throws SQLException{
        this(DriverManager.getConnection("jdbc:sqlite:"+
                Objects.requireNonNull(fileName), config.toProperties()));
        this.config = config;
    }
    /**
     * This constructs a LinkDatabaseConnection that connects to the SQL 
     * database located at the given file.
     * @param file The file for the database to connect to.
     * @throws SQLException If a database access error occurs.
     * @throws NullPointerException If the file is null.
     */
    public LinkDatabaseConnection(File file) throws SQLException{
        this(file.toString());
    }
    /**
     * This constructs a LinkDatabaseConnection that connects to the SQL 
     * database located at the given file and uses the given properties.
     * @param file The file for the database to connect to.
     * @param properties The properties for the database connection. This is a 
     * list of arbitrary string tag/value pairs to use as connection arguments.
     * @throws SQLException If a database access error occurs.
     * @throws NullPointerException If the file is null.
     */
    public LinkDatabaseConnection(File file, Properties properties) 
            throws SQLException{
        this(file.toString(),properties);
    }
    /**
     * This constructs a LinkDatabaseConnection that connects to the SQL 
     * database located at the given file and uses the given SQLite 
     * configuration.
     * @param file The file for the database to connect to.
     * @param config The SQLiteConfig object containing the properties for the 
     * connection.
     * @throws SQLException If a database access error occurs.
     * @throws NullPointerException If the file is null.
     */
    public LinkDatabaseConnection(File file, SQLiteConfig config) 
            throws SQLException{
        this(file.toString(),config);
    }
    /**
     * This constructs a TableModel based off the given ResultSet.
     * @param results The ResultSet to turn into a TableModel.
     * @return The TableModel containing the contents of the ResultSet. If the 
     * ResultSet is null, then this will also be null.
     * @throws SQLException If a database error occurs.
     */
    public static TableModel getTableModelForResultSet(ResultSet results) 
            throws SQLException{
        if (results == null)    // If the result set is null
            return null;
        return new ResultTableModel(results);
    }
    /**
     * This constructs a TableModel based off the results of the given query 
     * using the given statement. This version is allows for the reuse of a 
     * Statement object.
     * @param sql The query to execute.
     * @param stmt The statement to use to execute the query.
     * @return The TableModel containing the results of the query.
     * @throws SQLException If a database error occurs.
     */
    public TableModel getTableModelForQuery(String sql, Statement stmt) 
            throws SQLException{
        return getTableModelForResultSet(stmt.executeQuery(sql));
    }
    /**
     * This constructs a TableModel based off the results of the given query.
     * @param sql The query to execute.
     * @return The TableModel containing the results of the query.
     * @throws SQLException If a database error occurs.
     */
    public TableModel getTableModelForQuery(String sql) throws SQLException{
        TableModel model;   // The TableModel to store the results
            // Create the statement to use to execute the query.
        try (Statement stmt = createStatement()) {
            model = getTableModelForQuery(sql,stmt);
        }
        return model;
    }
    /**
     * This constructs a TableModel based off the results of the given prepared 
     * statement.
     * @param pstmt The prepared statement to use to execute the query.
     * @return The TableModel containing the results of the query.
     * @throws SQLException If a database error occurs.
     */
    public TableModel getTableModelForQuery(PreparedStatement pstmt) throws 
            SQLException{
        return getTableModelForResultSet(pstmt.executeQuery());
    }
    /**
     * This constructs a TableModel based off the results a query on the given 
     * table using the given statement. This version is allows for the reuse of 
     * a Statement object.
     * @param tableName The name of the table in the database to create a 
     * TableModel based off of.
     * @param stmt The statement to use to execute the query.
     * @return The TableModel containing the contents of the database table.
     * @throws SQLException If a database error occurs.
     */
    public TableModel getTableModelForTable(String tableName, Statement stmt) 
            throws SQLException{
        return getTableModelForQuery("SELECT * FROM " + tableName,stmt);
    }
    /**
     * This constructs a TableModel based off the results a query on the given 
     * table using the given statement.
     * @param tableName The name of the table in the database to create a 
     * TableModel based off of.
     * @return The TableModel containing the contents of the database table.
     * @throws SQLException If a database error occurs.
     */
    public TableModel getTableModelForTable(String tableName) throws SQLException{
        TableModel model;   // The TableModel to store the results
            // Create the statement to use to execute the query.
        try (Statement stmt = createStatement()) {
            model = getTableModelForTable(tableName,stmt);
        }
        return model;
    }
    /**
     * This returns SQLiteConfig used to construct this connection if one was 
     * provided.
     * @return The SQLiteConfig for this connection, or null.
     */
    public SQLiteConfig getConfig(){
        return config;
    }
    /**
     * 
     * @return
     * @throws SQLException If a database error occurs.
     */
    public SQLSet<String> showTables()throws SQLException{
            // If the tables set view has not been initialized yet
        if (dbTableSet == null)
            dbTableSet = new TableSet(TABLE_TYPE_TABLE);
        return dbTableSet;
    }
    /**
     * 
     * @return
     * @throws SQLException If a database error occurs.
     */
    public SQLSet<String> showViews()throws SQLException{
            // If the views set view has not been initialized yet
        if (dbViewSet == null)
            dbViewSet = new TableSet(TABLE_TYPE_VIEW);
        return dbViewSet;
    }
    /**
     * 
     * @return
     * @throws SQLException If a database error occurs.
     */
    public SQLSet<String> showIndexes()throws SQLException{
            // If the indexes set view has not been initialized yet
        if (dbIndexSet == null)
            dbIndexSet = new TableSet(TABLE_TYPE_INDEX);
        return dbIndexSet;
    }
    /**
     * 
     * @param tableName
     * @return
     * @throws SQLException If a database error occurs.
     */
    public SQLSet<String> showIndexes(String tableName)throws SQLException{
        return new TableIndexSet(tableName);
    }
    /**
     * 
     * @return
     * @throws SQLException If a database error occurs.
     */
    public SQLMap<String, String> showStructures() throws SQLException{
            // If the structure map view has not been initialized yet
        if (dbStructureMap == null)
            dbStructureMap = new TableStructureMap();
        return dbStructureMap;
    }
    /**
     * 
     * @param tableName
     * @return
     * @throws SQLException If a database error occurs.
     */
    public String showStructure(String tableName) throws SQLException{
        return showStructures().get(tableName);
    }
    /**
     * 
     * @todo: Rewrite this method to actually work
     * 
     * @param stmt
     * @return
     * @throws SQLException 
     */
    protected Boolean getForeignKeysConstraint(Statement stmt) throws SQLException{
        Boolean value = null;
        ResultSet results = stmt.executeQuery("PRAGMA foreign_keys");
        if (results.next()){
            value = results.getBoolean(1);
            if (results.wasNull())
                value = null;
        }
        return value;
    }
    /**
     * 
     * @todo: Rewrite this method to actually work
     * 
     * @param stmt
     * @return
     * @throws SQLException 
     */
    public boolean getForeignKeysSupported(Statement stmt) throws SQLException{
        return getForeignKeysConstraint(stmt) != null;
    }
    /**
     * 
     * @todo: Rewrite this method to actually work
     * 
     * @return
     * @throws SQLException 
     */
    public boolean getForeignKeysSupported() throws SQLException{
        boolean value;
        try (Statement stmt = createStatement()) {
            value = getForeignKeysSupported(stmt);
        }
        return value;
    }
    /**
     * 
     * @todo: Rewrite this method to actually work
     * 
     * @param stmt
     * @return
     * @throws SQLException 
     */
    public boolean isForeignKeysEnabled(Statement stmt) throws SQLException{
        Boolean value = getForeignKeysConstraint(stmt);
        return value != null && value;
    }
    /**
     * 
     * @todo: Rewrite this method to actually work
     * 
     * @return
     * @throws SQLException 
     */
    public boolean isForeignKeysEnabled() throws SQLException{
        boolean value;
        try (Statement stmt = createStatement()) {
            value = isForeignKeysEnabled(stmt);
        }
        return value;
    }
    /**
     * 
     * @todo: Rewrite this method to actually work
     * 
     * @param value
     * @param stmt
     * @throws SQLException 
     */
    public void setForeignKeysEnabled(boolean value, Statement stmt) throws SQLException{
        if (getForeignKeysSupported(stmt))
            stmt.execute("PRAGMA foreign_keys = " + ((value)?"ON":"OFF"));
        else
            throw new UnsupportedOperationException("Database does not support foreign keys");
    }
    /**
     * 
     * @todo: Rewrite this method to actually work
     * 
     * @param value
     * @throws SQLException 
     */
    public void setForeignKeysEnabled(boolean value) throws SQLException{
        try (Statement stmt = createStatement()) {
            setForeignKeysEnabled(value,stmt);
        }
    }
    /**
     * This renames the link column 
     * @param stmt
     * @param program
     * @return
     * @throws SQLException 
     */
    // Version 0.0.0 -> Version 0.0.1
    public boolean updateRenameLinkColumns(Statement stmt, DisableGUIInput program) 
            throws SQLException{
            // Rename the old version 0.0.0 link column to its 0.0.1+ name
        renameColumn(OLD_DATABASE_TABLES[0][0][0], 
                    OLD_DATABASE_TABLES[0][0][4], 
                    OLD_DATABASE_TABLES[1][0][4], stmt);
        return true;
    }
    /**
     * 
     * @param stmt
     * @return
     * @throws SQLException 
     */
    public boolean updateRenameLinkColumns(Statement stmt) throws SQLException{
        return updateRenameLinkColumns(stmt,null);
    }
    /**
     * 
     * @param stmt
     * @param program
     * @return
     * @throws SQLException 
     */
    // Version 0.0.1 -> Version 0.1.0
    public boolean updateRenameInitialColumns(Statement stmt, DisableGUIInput program) 
            throws SQLException{
            // This gets the progress bar from the program if there is one
        JProgressBar progressBar = null;
            // If a program was provided
        if (program != null)
            progressBar = program.getProgressBar();
            // A two-dimensional array containing the indexes for the names of 
            // the columns that need renaming and the tables they are in
        int[][] updates = {
            {0, 3},     // Links table, link index column
            {1, 2},     // List table, list index column
            {1, 3}      // List table, list name column
        };  // If the program has a progress bar
        if (progressBar != null){
            progressBar.setMaximum(updates.length);
            progressBar.setIndeterminate(false);
        }   // Go through the tuples of table and columns that need renaming
        for (int[] update : updates){
                // Rename the column from its 0.0.x name to its 0.1.0+ name
            renameColumn(OLD_DATABASE_TABLES[0][update[0]][0], 
                    OLD_DATABASE_TABLES[0][update[0]][update[1]], 
                    OLD_DATABASE_TABLES[1][update[0]][update[1]], stmt);
                // If the program has a progress bar
            if (progressBar != null)
                progressBar.setValue(progressBar.getValue()+1);
        }
        return true;
    }
    /**
     * 
     * @param stmt
     * @return
     * @throws SQLException 
     */
    public boolean updateRenameInitialColumns(Statement stmt) throws SQLException{
        return updateRenameInitialColumns(stmt,null);
    }
    /**
     * 
     * @param stmt
     * @param program
     * @return
     * @throws SQLException 
     */
    // Version 0.1.0 -> Version 0.2.0
    public boolean updateAddListFlagsColumn(Statement stmt, DisableGUIInput program) 
            throws SQLException{
            // Add the list flags column to the list table
        addColumn(OLD_DATABASE_TABLES[1][1][0], 
                OLD_DATABASE_TABLES[1][1][5], 
                "integer DEFAULT 0", stmt);
        return true;
    }
    /**
     * 
     * @param stmt
     * @return
     * @throws SQLException 
     */
    public boolean updateAddListFlagsColumn(Statement stmt) throws SQLException{
        return updateAddListFlagsColumn(stmt,null);
    }
    /**
     * 
     * @param stmt
     * @param program
     * @return
     * @throws SQLException 
     */
    // Version 0.2.0 -> Version 0.3.0
    public boolean updateAddPrefixTable(Statement stmt, DisableGUIInput program) 
            throws SQLException{
            // Get the current state of the auto-commit
        boolean autoCommit = getAutoCommit();
            // Turn off the auto-commit in order to group the following database 
            // transactions to improve performance
        setAutoCommit(false);
            // This gets the progress bar from the program if there is one
        JProgressBar progressBar = null;
            // If a program was provided
        if (program != null)
            progressBar = program.getProgressBar();
            // This will get a map that maps the linkIDs to the listIDs of the 
            // list containing the link
        TreeMap<Integer,Integer> listIDs = new TreeMap<>();
            // This will get a map that maps the linkIDs to their index in their 
            // list
        TreeMap<Integer,Integer> indexes = new TreeMap<>();
            // This will get a map that maps the linkIDs to the links
        TreeMap<Integer,String> links = new TreeMap<>();
        if (progressBar != null){   // If the program has a progress bar
            progressBar.setMaximum(getTableSize(OLD_DATABASE_TABLES[1][0][0],
                    "*",stmt));
            progressBar.setIndeterminate(false);
        }   // Query the database for the contents of the links table
        ResultSet results = stmt.executeQuery("SELECT * FROM "+OLD_DATABASE_TABLES[1][0][0]);
            // While there are still rows in the links table
        while(results.next()){
                // Get the linkID of the current row
            int linkID = results.getInt(OLD_DATABASE_TABLES[1][0][1]);
                // Get the listID for the current row's list
            listIDs.put(linkID, results.getInt(OLD_DATABASE_TABLES[1][0][2]));
                // Get the index for the current row
            indexes.put(linkID, results.getInt(OLD_DATABASE_TABLES[1][0][3]));
                // Get the current row's link
            links.put(linkID, results.getString(OLD_DATABASE_TABLES[1][0][4]));
            if (progressBar != null)    // If the program has a progress bar
                progressBar.setValue(progressBar.getValue()+1);
        }
        if (progressBar != null){   // If the program has a progress bar
            progressBar.setIndeterminate(true);
            progressBar.setValue(0);
            progressBar.setMaximum(links.size());
        }   // Delete the old links table
        deleteTable(OLD_DATABASE_TABLES[1][0][0],stmt);
        commit();       // Commit the changes to the database
            // Go through the table creation queries for version 0.3.0
        for (String query : OLD_DATABASE_DEFINITIONS[0]){
                // Create the table
            stmt.execute(query);
        }   // A prepared statement to insert the empty prefix into the new 
        try(PreparedStatement pstmt = prepareStatement( // prefix table
                String.format("INSERT INTO %s(%s, %s) VALUES (?, ?)", 
                        OLD_DATABASE_TABLES[2][1][0],
                        OLD_DATABASE_TABLES[2][1][1],
                        OLD_DATABASE_TABLES[2][1][2]))){
                // The prefixID for the empty prefix will be 1
            pstmt.setInt(1, 1);
                // The prefix is empty
            pstmt.setString(2, "");
                // Update the database
            pstmt.executeUpdate();
        }   // Go through the initial prefixes
        for (String prefix : INITIAL_LINK_PREFIXES){
                // If the prefix is empty 
            if (prefix.isEmpty())
                continue;   // Skip it, since we already added the empty prefix
                // Prepare a statement to insert the prefix into the prefix 
            try(PreparedStatement pstmt = prepareStatement( // table
                    String.format("INSERT INTO %s(%s) VALUES (?)", 
                            OLD_DATABASE_TABLES[2][1][0],
                            OLD_DATABASE_TABLES[2][1][2]))){
                pstmt.setString(1, prefix);
                    // Update the database
                pstmt.executeUpdate();
            }
        }
        commit();       // Commit the changes to the database
        if (progressBar != null)    // If the program has a progress bar
            progressBar.setIndeterminate(false);
            // Go through the linkIDs of the links from the database
        for (Integer linkID : links.keySet()){
                // Prepare a statement to insert the current link back into the 
                // link table, now with the empty prefix
            try (PreparedStatement pstmt = prepareStatement(String.format(
                    "INSERT INTO %s(%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)", 
                            OLD_DATABASE_TABLES[2][0][0],
                            OLD_DATABASE_TABLES[2][0][1],
                            OLD_DATABASE_TABLES[2][0][2],
                            OLD_DATABASE_TABLES[2][0][3],
                            OLD_DATABASE_TABLES[2][0][4],
                            OLD_DATABASE_TABLES[2][0][5]))){
                    // Set the linkID
                pstmt.setInt(1, linkID);
                    // Set the listID of the list containing this link
                pstmt.setInt(2, listIDs.get(linkID));
                    // Set the index for this link in its list
                pstmt.setInt(3, indexes.get(linkID));
                    // Set this link's prefix to the empty prefix
                pstmt.setInt(4, 1);
                    // Set the link
                pstmt.setString(5, links.get(linkID));
                    // Execute the update
                pstmt.executeUpdate();
            }
            if (progressBar != null)    // If the program has a progress bar
                progressBar.setValue(progressBar.getValue()+1);
        }
        commit();       // Commit the changes to the database
            // Restore the auto-commit back to what it was set to before
        setAutoCommit(autoCommit);
        return true;
    }
    /**
     * 
     * @param stmt
     * @return
     * @throws SQLException 
     */
    public boolean updateAddPrefixTable(Statement stmt) throws SQLException{
        return updateAddPrefixTable(stmt,null);
    }
    /**
     * 
     * @param stmt
     * @param program
     * @return
     * @throws SQLException 
     */
    // Version 0.3.0 -> Version 0.5.0
    // (Version 0.4.0 reworked the tables, version 0.5.0 added the size limit 
    // column)
    public boolean updateToVersion0_5_0(Statement stmt, DisableGUIInput program) 
            throws SQLException{
            // Get the current state of the auto-commit
        boolean autoCommit = getAutoCommit();
            // Turn off the auto-commit in order to group the following database 
            // transactions to improve performance
        setAutoCommit(false);
            // This gets the progress bar from the program if there is one
        JProgressBar progressBar = null;
        if (program != null)    // If a program was provided
            progressBar = program.getProgressBar();
            // This will get the total size of the tables
        int max = 0;
            // Go through the tables in the database
        for (String[] table : OLD_DATABASE_TABLES[2])
            max += getTableSize(table[0],"*",stmt);
        if (progressBar != null){   // If the program has a progress bar
            progressBar.setMaximum(max);
            progressBar.setIndeterminate(false);
        }   // This will get a map mapping the prefixIDs to the prefixes
        TreeMap<Integer, String> oldPrefixes = new TreeMap<>();
            // This gets the prefixID for the empty prefix
        int emptyPrefixID = 1;
            // This queries the database for the contents of the prefix table
        ResultSet results = stmt.executeQuery("SELECT * FROM "+OLD_DATABASE_TABLES[2][1][0]);
            // While there are still rows in the prefix table
        while (results.next()){
                // Get the prefixID of the current row
            int prefixID = results.getInt(OLD_DATABASE_TABLES[2][1][1]);
                // Get the prefix for the current row
            String prefix = results.getString(OLD_DATABASE_TABLES[2][1][2]);
                // Map the prefixID to its prefix
            oldPrefixes.put(prefixID, prefix);
                // If the current prefix is empty
            if (prefix.isEmpty())
                    // We found the empty prefixID
                emptyPrefixID = prefixID;
            if (progressBar != null)    // If the program has a progress bar
                progressBar.setValue(progressBar.getValue()+1);
        }
            // If the database did not contain the empty prefix
        if (!oldPrefixes.containsValue("")){
                // If there are any prefixes and the prefixID of 1 is already in 
            if (!oldPrefixes.isEmpty() && oldPrefixes.containsKey(1))  // use
                    // Get the last prefixID and offset it by 1
                emptyPrefixID = oldPrefixes.lastKey()+1;
                // Add the empty prefixID to the map of prefixes
            oldPrefixes.put(emptyPrefixID, "");
        }   // This will get a map mapping the linkIDs to the listIDs of the 
            // lists containing the link
        TreeMap<Long, Integer> linkListIDs = new TreeMap<>();
            // This will get a map mapping the linkIDs to their index in their 
            // list
        TreeMap<Long, Integer> linkIndexes = new TreeMap<>();
            // This will get a map mapping the linkIDs to the links
        TreeMap<Long, String> oldLinks = new TreeMap<>();
            // This will get a map mapping the linkIDs to the prefixIDs of the 
            // prefixes for the links
        TreeMap<Long, Integer> oldPrefixIDs = new TreeMap<>();
            // Query the database for the contents of the links table
        results = stmt.executeQuery("SELECT * FROM "+OLD_DATABASE_TABLES[2][0][0]);
            // While the links table still has rows
        while (results.next()){
                // Get the linkID of the current row
            long linkID = results.getLong(OLD_DATABASE_TABLES[2][0][1]);
                // Get the listID for the current row's list
            linkListIDs.put(linkID, results.getInt(OLD_DATABASE_TABLES[2][0][2]));
                // Get the index for the current row
            linkIndexes.put(linkID, results.getInt(OLD_DATABASE_TABLES[2][0][3]));
                // Get the current row's link
            oldLinks.put(linkID, results.getString(OLD_DATABASE_TABLES[2][0][5]));
                // Get the prefixID for the prefix for the current row's link
            int prefixID = results.getInt(OLD_DATABASE_TABLES[2][0][4]);
                // If the prefixID is was not null
            if (!results.wasNull())
                oldPrefixIDs.put(linkID, prefixID);
            if (progressBar != null)    // If the program has a progress bar
                progressBar.setValue(progressBar.getValue()+1);
        }   // This will get a map mapping listIDs to the list names
        TreeMap<Integer, String> listNames = new TreeMap<>();
            // This will get a map mapping listIDs to their tab indexes
        TreeMap<Integer, Integer> listIndexes = new TreeMap<>();
            // This will get a map mapping listIDs to the list's flags
        TreeMap<Integer, Integer> listFlags = new TreeMap<>();
            // Query the database for the contents of the lists table
        results = stmt.executeQuery("SELECT * FROM "+OLD_DATABASE_TABLES[2][2][0]);
            // While the lists table still has rows
        while (results.next()){
                // Get the listID of the current row
            int listID = results.getInt(OLD_DATABASE_TABLES[2][2][1]);
                // Get the list name of the current row
            listNames.put(listID, results.getString(OLD_DATABASE_TABLES[2][2][3]));
                // Get the tab index for the current row
            listIndexes.put(listID, results.getInt(OLD_DATABASE_TABLES[2][2][2]));
                // Get the list flags for the current row
            listFlags.put(listID, results.getInt(OLD_DATABASE_TABLES[2][2][5]));
            if (progressBar != null)    // If the program has a progress bar
                progressBar.setValue(progressBar.getValue()+1);
        }
        if (progressBar != null){       // If the program has a progress bar
            progressBar.setValue(0);
                // Set the maximum to the amount of linkIDs + the amount of 
                // links + the amount of lists + the amount of prefixes + the 
                // amount of tables present in version 0.3.0 + the amount of 
                // queries used to create the tables in version 0.5.0
            progressBar.setMaximum(linkListIDs.size()+
                    oldLinks.size()+
                    oldPrefixes.size()+
                    listNames.size()+
                    OLD_DATABASE_TABLES[2].length+
                    OLD_DATABASE_DEFINITIONS[1].length);
        }   // Go through the tables in version 0.3.0
        for (String[] table : OLD_DATABASE_TABLES[2]){
                // Delete the current table
            deleteTable(table[0],stmt);
            if (progressBar != null)    // If the program has a progress bar
                progressBar.setValue(progressBar.getValue()+1);
        }
        commit();       // Commit the changes to the database
            // Go through the table creation queries for the tables in version 
        for (String query : OLD_DATABASE_DEFINITIONS[1]){   // 0.5.0
            stmt.execute(query);    // Create the table
            if (progressBar != null)    // If the program has a progress bar
                progressBar.setValue(progressBar.getValue()+1);
        }
        commit();       // Commit the changes to the database
            // Go through the prefixes from the database
        for(Map.Entry<Integer, String> entry : oldPrefixes.entrySet()){
                // If the prefix for the current value is not null
            if (entry.getValue() != null)
                    // Prepare a statement to insert the prefix and its prefixID 
                    // into the new prefix table
                try(PreparedStatement pstmt = prepareStatement(
                        String.format("INSERT INTO %s(%s, %s) VALUES (?, ?)", 
                                OLD_DATABASE_TABLES[3][0][0],
                                OLD_DATABASE_TABLES[3][0][1],
                                OLD_DATABASE_TABLES[3][0][2]))){
                        // Set the prefixID
                    pstmt.setInt(1, entry.getKey());
                        // Set the prefix
                    pstmt.setString(2, entry.getValue());
                        // Update the database
                    pstmt.executeUpdate();
                }
            if (progressBar != null)    // If the program has a progress bar
                progressBar.setValue(progressBar.getValue()+1);
        }
        commit();       // Commit the changes to the database
            // Go through the linkIDs of the links from the database
        for (Long linkID : oldLinks.navigableKeySet()){
                // Prepare a statement to insert the link with the current 
                // linkID into the new link table
            try(PreparedStatement pstmt = prepareStatement(String.format(
                    "INSERT INTO %s(%s, %s, %s) VALUES (?, ?, ?)", 
                            OLD_DATABASE_TABLES[3][1][0],
                            OLD_DATABASE_TABLES[3][1][1],
                            OLD_DATABASE_TABLES[3][1][2],
                            OLD_DATABASE_TABLES[3][1][3]))){
                    // Set the linkID of the link
                pstmt.setLong(1, linkID);
                    // Set the prefixID for the link's prefix. If no prefix was 
                    // specified for the link, then use the empty prefix
                pstmt.setInt(2, oldPrefixIDs.getOrDefault(linkID,emptyPrefixID));
                    // Set the link
                pstmt.setString(3, oldLinks.get(linkID));
                    // Update the database
                pstmt.executeUpdate();
            }
            if (progressBar != null)    // If the program has a progress bar
                progressBar.setValue(progressBar.getValue()+1);
        }
        commit();       // Commit the changes to the database
            // Go through the listIDs of the lists from the database
        for (Integer listID : listNames.navigableKeySet()){
                // Prepare a statement to insert the list into the new list 
                // table
            try(PreparedStatement pstmt = prepareStatement(String.format(
                    "INSERT INTO %s(%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)", 
                            OLD_DATABASE_TABLES[3][2][0],
                            OLD_DATABASE_TABLES[3][2][1],
                            OLD_DATABASE_TABLES[3][2][2],
                            OLD_DATABASE_TABLES[3][2][3],
                            OLD_DATABASE_TABLES[3][2][4],
                            OLD_DATABASE_TABLES[3][2][6]))){
                    // Set the listID for the list
                pstmt.setInt(1, listID);
                    // Set the index for the list's tab
                pstmt.setInt(2, listIndexes.get(listID));
                    // Set the name for the list
                pstmt.setString(3, listNames.get(listID));
                    // Set the flags for the list
                pstmt.setInt(4, listFlags.get(listID));
                    // Set the list's last modified time to now
                pstmt.setLong(5, System.currentTimeMillis());
                    // Update the database
                pstmt.executeUpdate();
            }
            if (progressBar != null)    // If the program has a progress bar
                progressBar.setValue(progressBar.getValue()+1);
        }
        commit();       // Commit the changes to the database
            // Go through the linkIDs of the links in the lists
        for (Long linkID : linkListIDs.keySet()){
                // Prepare a statement to insert the linkID and listID into the 
                // new list data table
            try(PreparedStatement pstmt = prepareStatement(
                    String.format("INSERT INTO %s(%s, %s, %s) VALUES (?, ?, ?)", 
                            OLD_DATABASE_TABLES[3][3][0],
                            OLD_DATABASE_TABLES[3][3][1],
                            OLD_DATABASE_TABLES[3][3][2],
                            OLD_DATABASE_TABLES[3][3][3]))){
                    // Set the listID of the list containing the link
                pstmt.setInt(1, linkListIDs.get(linkID));
                    // Set the linkID for the link
                pstmt.setLong(2, linkID);
                    // Set the link's index in the list
                pstmt.setInt(3, linkIndexes.get(linkID));
                    // Update the database
                pstmt.executeUpdate();
            }
            if (progressBar != null)    // If the program has a progress bar
                progressBar.setValue(progressBar.getValue()+1);
        }
        commit();       // Commit the changes to the database
            // Restore the auto-commit back to what it was set to before
        setAutoCommit(autoCommit);
        return true;
    }
    /**
     * 
     * @param stmt
     * @return
     * @throws SQLException 
     */
    public boolean updateToVersion0_5_0(Statement stmt) throws SQLException{
        return updateToVersion0_5_0(stmt,null);
    }
    /**
     * 
     * @param stmt
     * @param program
     * @return
     * @throws SQLException 
     */
    public boolean updateAddListSizeLimitColumn(Statement stmt, 
            DisableGUIInput program) throws SQLException{
            // Add the list size limit column
        addColumn(OLD_DATABASE_TABLES[3][2][0], 
                OLD_DATABASE_TABLES[3][2][5], 
                "integer DEFAULT NULL",stmt);
            return true;
    }
    /**
     * 
     * @param stmt
     * @return
     * @throws SQLException 
     */
    public boolean updateAddListSizeLimitColumn(Statement stmt) throws SQLException{
        return updateAddListSizeLimitColumn(stmt,null);
    }
    /**
     * 
     * @param stmt
     * @param program
     * @return
     * @throws SQLException 
     */
    public boolean updateToVersion1_0_0(Statement stmt, DisableGUIInput program) 
            throws SQLException{
            // Get the current state of the auto-commit
        boolean autoCommit = getAutoCommit();
            // Turn off the auto-commit in order to group the following database 
            // transactions to improve performance
        setAutoCommit(false);
            // This gets the progress bar from the program if there is one
        JProgressBar progressBar = null;
            // If a program was provided
        if (program != null)
            progressBar = program.getProgressBar();
            // This gets the size of all the tables currently in the database
        int max = 0;
            // Go through the tables in the database
        for (int i = 0; i < 4; i++){
            max += getTableSize(OLD_DATABASE_TABLES[3][i][0], 
                    OLD_DATABASE_TABLES[3][i][1],stmt);
        }
        if (progressBar != null){       // If the program has a progress bar
            progressBar.setMaximum(max);
            progressBar.setIndeterminate(false);
        }   // This is a set that will get the prefixes from the database
        Set<String> oldPrefixes = new LinkedHashSet<>();
            // Query the prefix table to get the prefixes
        ResultSet results = stmt.executeQuery("SELECT * FROM "+OLD_DATABASE_TABLES[3][0][0]);
            // While there are still rows in the prefix table to go through
        while (results.next()){
                // Add the prefix to the set
            oldPrefixes.add(results.getString(OLD_DATABASE_TABLES[3][0][2]));
            if (progressBar != null)    // If the program has a progress bar
                progressBar.setValue(progressBar.getValue()+1);
        }   // This is a map that will get the full links from the database
        TreeMap<Long,String> oldLinks = new TreeMap<>();
            // Query the full links view to get the links
        results = stmt.executeQuery("SELECT * FROM "+OLD_DATABASE_TABLES[3][4][0]);
            // While there are still rows in the full links view
        while (results.next()){
                // Map the link to its linkID
            oldLinks.put(results.getLong(OLD_DATABASE_TABLES[3][4][1]), 
                    results.getString(OLD_DATABASE_TABLES[3][4][2]));
            if (progressBar != null)    // If the program has a progress bar
                progressBar.setValue(progressBar.getValue()+1);
        }   // This is a map that will get the names of the lists
        TreeMap<Integer, String> oldListNames = new TreeMap<>();
            // This is a map that will get the listIDs of the lists in order
        ArrayList<Integer> listIndexes = new ArrayList<>();
            // This is a map that will get the list flags
        TreeMap<Integer, Integer> listFlags = new TreeMap<>();
            // This is a map that will get the list size limits
        TreeMap<Integer, Integer> listSizeLimit = new TreeMap<>();
            // This is a map that will get the list data
        TreeMap<Integer, List<Long>> listData = new TreeMap<>();
            // This queries the list table to get the data ordered by indexes
        results = stmt.executeQuery(String.format(
                "SELECT * FROM %s ORDER BY %s NULLS LAST", 
                    OLD_DATABASE_TABLES[3][2][0],
                    OLD_DATABASE_TABLES[3][2][2]));
            // While there are still rows in the list table
        while (results.next()){
                // Get the current row's listID
            int listID = results.getInt(OLD_DATABASE_TABLES[3][2][1]);
                // Add the listID to the list of listIDs
            listIndexes.add(listID);
                // Put the list name into the list name map
            oldListNames.put(listID, results.getString(OLD_DATABASE_TABLES[3][2][3]));
                // Put the list flags into the list flag map
            listFlags.put(listID, results.getInt(OLD_DATABASE_TABLES[3][2][4]));
                // Get the list's size limit
            int temp = results.getInt(OLD_DATABASE_TABLES[3][2][5]);
                // If the list's size limit was not null
            if (!results.wasNull())
                listSizeLimit.put(listID, temp);
                // Create the list to store the list data
            listData.put(listID, new ArrayList<>());
            if (progressBar != null)    // If the program has a progress bar
                progressBar.setValue(progressBar.getValue()+1);
        }   // Query the list data table, sorted by listID and link index
        results = stmt.executeQuery(String.format(
                "SELECT * FROM %s ORDER BY %s, %s NULLS LAST",
                    OLD_DATABASE_TABLES[3][3][0],
                    OLD_DATABASE_TABLES[3][3][1],
                    OLD_DATABASE_TABLES[3][3][3]));
            // While there are still rows in the list data table
        while (results.next()){
                // Get the list with the row's listID and add the linkID to that 
                // list
            listData.get(results.getInt(OLD_DATABASE_TABLES[3][3][1])).
                    add(results.getLong(OLD_DATABASE_TABLES[3][3][2]));
            if (progressBar != null)    // If the program has a progress bar
                progressBar.setValue(progressBar.getValue()+1);
        }
        if (progressBar != null)        // If the program has a progress bar
            progressBar.setIndeterminate(true);
            // Delete the index on the list data table
        deleteIndex(OLD_DATABASE_TABLES[3][5][0],stmt);
            // Delete the full links view
        deleteView(OLD_DATABASE_TABLES[3][4][0],stmt);
            // Go through the remaining tables in reverse order
        for (int i = 3; i>= 0; i--){
                // Delete the current table
            deleteTable(OLD_DATABASE_TABLES[3][i][0],stmt);
        }
        createTables(stmt); // Create the new tables
        commit();       // Commit the changes to the database
        if (progressBar != null)        // If the program has a progress bar
            progressBar.setValue(0);
            // Set the total size back to zero
        max = 0;
            // Go through the list data to get the total size
        for (List<Long> links : listData.values())
            max += links.size();
        if (progressBar != null){       // If the program has a progress bar
                // Set the maximum value for the progress bar to the amount of 
                // prefixes, links, lists (x2) and the total size of those lists
            progressBar.setMaximum(oldPrefixes.size()+
                    oldLinks.size()+
                    oldListNames.size()+
                    listIndexes.size()+max);
            progressBar.setIndeterminate(false);
        }   // Get the prefix map of the new database
        PrefixMap prefixes = getPrefixMap();
            // Go through the prefixes from the old database
        for (String prefix : oldPrefixes){
                // Add the prefix if not already added
            prefixes.addIfAbsent(prefix);
            if (progressBar != null)    // If the program has a progress bar
                progressBar.setValue(progressBar.getValue()+1);
        }
        commit();       // Commit the changes to the database
            // Get the link map of the new database
        LinkMap links = getLinkMap();
            // Go through the links from the old database
        for (String link : oldLinks.values()){
                // Add the link if not already added
            links.addIfAbsent(link);
            if (progressBar != null)    // If the program has a progress bar
                progressBar.setValue(progressBar.getValue()+1);
        }
        commit();       // Commit the changes to the database
            // Get the list name map from the new database
        ListNameMap listNames = getListNameMap();
            // Get the list of listIDs for all the lists
        ListIDList allLists = getAllListIDs();
            // Get the list of shown listIDs
        ListIDList shownLists = getShownListIDs();
            // This is a map for converting the listIDs of the old database to 
            // the listIDs of the new database
        TreeMap<Integer,Integer> listIDConversion = new TreeMap<>();
            // Go through the list names of the old database
        for (Integer oldID : oldListNames.keySet()){
                // Get the list flags, removing the first bit, since its purpose 
                // changed. The first bit use to indicate that a list was hidden
            int flags = listFlags.get(oldID) & (~0x1);
                // Add the list to the new database
            Integer listID = listNames.add(oldListNames.get(oldID), flags, 
                    listSizeLimit.get(oldID));
                // Map the list's new ID to its old ID
            listIDConversion.put(oldID, listID);
            if (progressBar != null)    // If the program has a progress bar
                progressBar.setValue(progressBar.getValue()+1);
        }   // Go through the old list of listIDs 
        for (Integer oldID : listIndexes){
                // Convert the current list's old listID to its new listID
            Integer listID = listIDConversion.get(oldID);
                // If the first bit of the list's old flags was not set (list 
                // was not hidden)
            if ((listFlags.get(oldID) & 0x1) != 1)
                shownLists.add(listID);
            allLists.add(listID);
            if (progressBar != null)    // If the program has a progress bar
                progressBar.setValue(progressBar.getValue()+1);
        }
        commit();       // Commit the changes to the database
            // Go through the old and new listIDs
        for (Map.Entry<Integer,Integer> listID : listIDConversion.entrySet()){
                // Get the old list contents for the list
            List<Long> linkIndexes = listData.get(listID.getKey());
                // Get the list contents for the list in the new database
            ListContents listContents = getListContents(listID.getValue());
                // Go through the old list contents
            for (Long linkID : linkIndexes){
                    // Add the link at the linkID to the new list contents
                listContents.add(oldLinks.get(linkID));
                if (progressBar != null)    // If the program has a progress bar
                    progressBar.setValue(progressBar.getValue()+1);
            }
            commit();       // Commit the changes to the database
        }   // Remove any duplicate links
        links.removeDuplicateRows();
            // Remove any unused links
        links.removeUnusedRows();
            // Set the database version to 1.0.0
        getDatabaseProperties().setProperty(DATABASE_VERSION_CONFIG_KEY, "1.0.0");
        commit();       // Commit the changes to the database
            // Restore the auto-commit back to what it was set to before
        setAutoCommit(autoCommit);
        return true;
    }
    /**
     * 
     * @param stmt
     * @return
     * @throws SQLException 
     */
    public boolean updateToVersion1_0_0(Statement stmt) throws SQLException{
        return updateToVersion1_0_0(stmt,null);
    }
    /**
     * 
     * @param stmt
     * @param program
     * @return
     * @throws SQLException 
     */
    public boolean updateAddConfigTable(Statement stmt, DisableGUIInput program) 
            throws SQLException{
        createTables(stmt); // Create any new tables
            // Set the database version to 1.0.0
        getDatabaseProperties().setProperty(DATABASE_VERSION_CONFIG_KEY, "1.0.0");
        return true;
    }
    /**
     * 
     * @param stmt
     * @return
     * @throws SQLException 
     */
    public boolean updateAddConfigTable(Statement stmt) throws SQLException{
        return updateAddConfigTable(stmt,null);
    }
    /**
     * 
     * @param stmt
     * @param program
     * @param versionStr
     * @return
     * @throws SQLException 
     */
    protected boolean updateOldDatabaseDefinitions(Statement stmt, 
            DisableGUIInput program, String versionStr)
            throws SQLException{
//            // If the version is greater than zero
//        if (version[0] > 0)
//            return true;
            // This gets whether this was successful in updating the database to 
            // version 1.0.0
        boolean updateSuccess = true;
            // Get the current state of the auto-commit
        boolean autoCommit = getAutoCommit();
            // Turn off the auto-commit in order to group the following database 
            // transactions to improve performance
        setAutoCommit(false);
            // Determine which updates to run for this database
        switch(versionStr){
            case("0.0.0"):      // If this is version 0.0.0 (Initial version)
                    // Update to version 0.0.1
                updateSuccess = updateRenameLinkColumns(stmt,program);
                    // If the database was not successfully updated
                if (!updateSuccess)
                    break;
            case("0.0.1"):      // If this is version 0.0.1 or earlier
                    // Update to version 0.1.0
                updateSuccess = updateRenameInitialColumns(stmt,program);
                    // If the database was not successfully updated
                if (!updateSuccess)
                    break;
            case("0.1.0"):      // If this is version 0.1.0 or earlier
                    // Update to version 0.2.0
                updateSuccess = updateAddListFlagsColumn(stmt,program);
                    // If the database was not successfully updated
                if (!updateSuccess)
                    break;
            case("0.2.0"):      // If this is version 0.2.0 or earlier
                    // Update to version 0.3.0
                updateSuccess = updateAddPrefixTable(stmt,program);
                    // If the database was not successfully updated
                if (!updateSuccess)
                    break;
            case("0.3.0"):      // If this is version 0.3.0 or earlier
                    // Update to version 0.5.0
                updateSuccess = updateToVersion0_5_0(stmt,program);
                    // If the database was not successfully updated
                if (!updateSuccess)
                    break;
            case("0.4.0"):      // If this is version 0.4.0 or earlier
                    // If this is version 0.4.0 only
                if ("0.4.0".equals(versionStr)){
                        // Update to version 0.5.0
                    updateSuccess = updateAddListSizeLimitColumn(stmt,program);
                        // If the database was not successfully updated
                    if (!updateSuccess)
                        break;
                }
            case("0.5.0"):      // If this is version 0.5.0 or earlier
                    // Update to version 1.0.0
                updateSuccess = updateToVersion1_0_0(stmt,program);
                break;
            case("0.6.0"):      // If this is version 0.6.0
                    // Update to version 1.0.0
                updateSuccess = updateAddConfigTable(stmt,program);
        }   // Set the database version to 1.0.0
        getDatabaseProperties().setProperty(DATABASE_VERSION_CONFIG_KEY,"1.0.0");
        commit();       // Commit the changes to the database
            // Restore the auto-commit back to what it was set to before
        setAutoCommit(autoCommit);
        return updateSuccess;
    }
    /**
     * This updates the database to version 2.1.0. List names can no longer have 
     * an asterisks.
     * @param stmt
     * @param progressBar
     * @return
     * @throws SQLException 
     */
    protected boolean updateToVersion2_1_0(Statement stmt, 
            JProgressBar progressBar) throws SQLException{
        if (progressBar != null)        // If a progress bar was provided
            progressBar.setIndeterminate(true);
            // This is a table to contain the list names
        TreeMap<Integer, String> listNames = new TreeMap<>();
            // This is a table to contain the list creation times
        HashMap<Integer, Long> listCreated = new HashMap<>();
            // This is a table to contain the list flags
        HashMap<Integer, Integer> listFlags = new HashMap<>();
            // This is a table to contain the list size limits
        HashMap<Integer, Integer> listSizeLimits = new HashMap<>();
            // Query the database for the contents of the list table
        ResultSet rs = stmt.executeQuery(String.format(
                "SELECT %s, %s, %s, %s, %s FROM %s", 
                    LIST_ID_COLUMN_NAME,
                    LIST_NAME_COLUMN_NAME,
                    LIST_CREATED_COLUMN_NAME,
                    LIST_FLAGS_COLUMN_NAME,
                    LIST_SIZE_LIMIT_COLUMN_NAME,
                    LIST_TABLE_NAME));
            // While there are still rows in the list table
        while (rs.next()){
                // Get the listID of the current row
            int listID = rs.getInt(LIST_ID_COLUMN_NAME);
                // Get the name of the current list
            String name = rs.getString(LIST_NAME_COLUMN_NAME);
                // If the list name is not null
            if (name != null)
                    // Replace any asterisks with underscores, since asterisks 
                    // are a forbidden character
                name = name.trim().replace('*', '_');
                // Store the list name
            listNames.put(listID, name);
                // Get the list's creation time
            long created = rs.getLong(LIST_CREATED_COLUMN_NAME);
            if (!rs.wasNull())  // If the list's creation time was not null
                listCreated.put(listID, created);
                // Get the list's flags
            int temp = rs.getInt(LIST_FLAGS_COLUMN_NAME);
            if (!rs.wasNull())  // If the list's flags was not null
                listFlags.put(listID, temp);
                // Get the list's size limit
            temp = rs.getInt(LIST_SIZE_LIMIT_COLUMN_NAME);
            if (!rs.wasNull())  // If the list's size limit was not null
                listSizeLimits.put(listID, temp);
        }   // This is a map that will get the lists of lists, since they will 
            // be erased when the list table is deleted
        Map<Integer, List<Integer>> lists = new TreeMap<>();
            // Go through the list types
        for (Integer listType : getListTypes()){
                // Create a copy of the listID list for that list type
            lists.put(listType, new ArrayList<>(getListIDs(listType)));
        }   // This is a map that contains the list data, since that will be 
            // erased when the list table is deleted
        Map<Integer, List<Long>> data = loadListDataIDs(stmt,progressBar);
            // Get the total size of the lists
        int totalSize = getListDataMap().totalSize();
        if (progressBar != null){       // If a progress bar was provided
            progressBar.setIndeterminate(true);
            progressBar.setValue(0);
            progressBar.setMaximum(listNames.size());
        }   // Delete the old list table
        deleteTable(LIST_TABLE_NAME,stmt);
        createTables(stmt);     // Create the new tables
        if (progressBar != null)        // If a progress bar was provided
            progressBar.setIndeterminate(false);
            // Go through the listIDs of the stored lists
        for (Integer listID : listNames.navigableKeySet()){
                // Prepare a statement to insert the list back into the list table
            try(PreparedStatement pstmt = prepareStatement(String.format(
                    "INSERT INTO %s(%s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?)",
                        LIST_TABLE_NAME,
                        LIST_ID_COLUMN_NAME,
                        LIST_NAME_COLUMN_NAME,
                        LIST_LAST_MODIFIED_COLUMN_NAME,
                        LIST_CREATED_COLUMN_NAME,
                        LIST_FLAGS_COLUMN_NAME,
                        LIST_SIZE_LIMIT_COLUMN_NAME))){
                    // Set the listID
                pstmt.setInt(1, listID);
                    // Set the list name
                pstmt.setString(2, listNames.get(listID));
                    // Get the current time
                long time = System.currentTimeMillis();
                    // Set the last modified time to the current time
                pstmt.setLong(3, time);
                    // Set the list created time to either the old creation time 
                    // or the current time if the list didn't have one before
                pstmt.setLong(4, listCreated.getOrDefault(listID, time));
                    // Set the list flags, defaulting to 0 if there aren't any
                pstmt.setInt(5, listFlags.getOrDefault(listID, 0));
                    // Set the list size limit
                setParameter(pstmt,6,listSizeLimits.get(listID));
                    // Execute the update
                pstmt.executeUpdate();
            }
            if (progressBar != null)    // If a progress bar was provided
                progressBar.setValue(progressBar.getValue()+1);
        }
        if (progressBar != null)        // If a progress bar was provided
            progressBar.setIndeterminate(true);
        commit();       // Commit the changes to the database
        if (progressBar != null){       // If a progress bar was provided
            progressBar.setValue(0);
            progressBar.setMaximum(lists.size());
            progressBar.setIndeterminate(false);
        }   // Go through the stored lists of lists
        for (Map.Entry<Integer,List<Integer>> entry : lists.entrySet()){
                // Get the listID list from the database and add all the listIDs 
                // back to the list in the database
            getListIDs(entry.getKey()).addAll(entry.getValue());
            if (progressBar != null)    // If a progress bar was provided
                progressBar.setValue(progressBar.getValue()+1);
        }
        if (progressBar != null)        // If a progress bar was provided
            progressBar.setIndeterminate(true);
        commit();       // Commit the changes to the database
        if (progressBar != null){       // If a progress bar was provided
            progressBar.setValue(0);
            progressBar.setMaximum(totalSize);
        }   // Repopulate the list data in the database
        repopulateListData(data,progressBar);
        return true;
    }
    /**
     * 
     * @param stmt
     * @param progressBar
     * @return
     * @throws SQLException 
     */
    protected Map<Integer, List<Long>> loadListDataIDs(Statement stmt, 
            JProgressBar progressBar)throws SQLException {
            // A tree map to get the list data from the database
        TreeMap<Integer, List<Long>> listData = new TreeMap<>();
        if (progressBar != null){       // If a progress bar was provided
            progressBar.setValue(0);
            progressBar.setMaximum(getTableSize(LIST_DATA_TABLE_NAME,
                    LINK_ID_COLUMN_NAME,stmt));
            progressBar.setIndeterminate(false);
        }   // Get the contents of the list data table
        ResultSet rs = stmt.executeQuery(String.format(
                "SELECT %s, %s, %s FROM %s ORDER BY %s, %s", 
                    LIST_ID_COLUMN_NAME,
                    LINK_INDEX_COLUMN_NAME,
                    LINK_ID_COLUMN_NAME,
                    LIST_DATA_TABLE_NAME,
                    LIST_ID_COLUMN_NAME,
                    LINK_INDEX_COLUMN_NAME));
            // While there are still rows in the list data table
        while (rs.next()){
                // Get the listID of the current row
            int listID = rs.getInt(LIST_ID_COLUMN_NAME);
                // If the list data map does not currently contain the current 
            if (!listData.containsKey(listID))  // listID
                listData.put(listID, new ArrayList<>());
                // Get the linkID of the current row
            Long linkID = rs.getLong(LINK_ID_COLUMN_NAME);
            if (rs.wasNull())   // If the row's linkID was null
                linkID = null;
                // Get the index of the current row
            int index = rs.getInt(LINK_INDEX_COLUMN_NAME);
                // Get the list storing the contents of the list for the current 
            List<Long> list = listData.get(listID); // listID
                // While the stored list is shorter than the index
            while (list.size() < index){
                    // Pad the list with null
                list.add(null);
            }
            list.add(linkID);   // Add the linkID to the list
            if (progressBar != null)    // If a progress bar was provided
                progressBar.setValue(progressBar.getValue()+1);
        }
        return listData;
    }
    /**
     * 
     * @param listData
     * @param progressBar
     * @throws SQLException 
     */
    protected void repopulateListData(Map<Integer, List<Long>> listData, 
            JProgressBar progressBar) throws SQLException{
            // Go through the list data
        for (Map.Entry<Integer,List<Long>> entry : listData.entrySet()){
                // Repopulate the current list in the database
            repopulateListData(entry.getKey(),entry.getValue(),progressBar);
        }
    }
    /**
     * 
     * @param listID
     * @param linkIDs
     * @param progressBar
     * @throws SQLException 
     */
    protected void repopulateListData(int listID, List<Long> linkIDs, 
            JProgressBar progressBar) throws SQLException{
        if (progressBar != null)    // If a progress bar was provided
            progressBar.setIndeterminate(false);
            // Go through the linkIDs to re-insert into the list
        for (int i = 0; i < linkIDs.size(); i++){
                // Get the linkID at the current index
            Long linkID = linkIDs.get(i);
                // If the linkID is not null
            if (linkID != null)
                try(PreparedStatement pstmt = prepareStatement(String.format(
                        "INSERT INTO %s(%s, %s, %s) VALUES (?, ?, ?)",
                            LIST_DATA_TABLE_NAME,
                            LIST_ID_COLUMN_NAME,
                            LINK_INDEX_COLUMN_NAME,
                            LINK_ID_COLUMN_NAME))){
                        // Set the listID
                    pstmt.setInt(1, listID);
                        // Set the link index
                    pstmt.setInt(2, i);
                        // Set the linkID
                    pstmt.setLong(3, linkID);
                        // Execute the update
                    pstmt.executeUpdate();
                }
            if (progressBar != null)    // If a progress bar was provided
                progressBar.setValue(progressBar.getValue()+1);
        }
        if (progressBar != null)    // If a progress bar was provided
            progressBar.setIndeterminate(true);
        commit();       // Commit the changes to the database
    }
    /**
     * This updates the database to version 3.0.0. Version 3.0.0 updates the 
     * distinct link view and the tables view. It also removes the exclusive 
     * lists table and its respective index. Version 3.0.0 also fixes a bug that 
     * was introduced to the list data table around version 2.0.0 where new 
     * databases would have a broken list data table.
     * @param stmt
     * @param progressBar
     * @return
     * @throws SQLException 
     */
    protected boolean updateToVersion3_0_0(Statement stmt, 
            JProgressBar progressBar) throws SQLException{
        if (progressBar != null)        // If a progress bar was provided
            progressBar.setIndeterminate(true);
            // Delete the old distinct link view
        deleteView(DISTINCT_LINK_VIEW_NAME,stmt);
            // Delete the old tables view
        deleteView(TABLES_VIEW_NAME,stmt);
            // The exclusive lists table was not used and was introduced in an 
            // unfinished state. As such, it is being removed.
            // Delete the exclusive lists index
        deleteIndex(EXCLUSIVE_LISTS_INDEX_NAME,stmt);
            // Delete the exclusive lists table
        deleteTable(EXCLUSIVE_LISTS_TABLE_NAME,stmt);
            // Update the default prefix separators
        getDatabaseProperties().getDefaults().put(PREFIX_SEPARATORS_CONFIG_KEY, 
                PREFIX_DEFAULT_SEPARATORS);
            // Get the structure for the list data table
        String ldDesc = showStructure(LIST_DATA_TABLE_NAME);
            // There was a mistake introduced around version 2.0.0 where the 
            // list data table had its arguments offset by 1, leading to a 
            // broken table. I'm not sure if any databases were created with the 
            // glitch, or if databases could even be created with the glitch, 
            // but a telltale sign would be that the unique constrant at the end 
            // was applied to the linkID and listID columns instead of the 
            // listID and list index columns
        if (ldDesc.endsWith(String.format("UNIQUE (%s, %s))", 
                LINK_ID_COLUMN_NAME,LIST_ID_COLUMN_NAME))){
            // I'm not even sure if this would fix this situation
            commit();       // Commit the changes to the database
                // This is a map containing the list data from the database
            Map<Integer, List<Long>> listData = loadListDataIDs(stmt,progressBar);
            if (progressBar != null)    // If a progress bar was provided
                progressBar.setIndeterminate(true);
                // Delete the bugged list data table
            deleteTable(LIST_DATA_TABLE_NAME,stmt);
                // Create the new tables
            createTables(stmt);
            commit();       // Commit the changes to the database
            if (progressBar != null)    // If a progress bar was provided
                progressBar.setValue(0);
                // This gets the total size of the lists
            int total = 0;
                // Go through the list data to get the total size
            for (List<Long> temp : listData.values()){
                total += temp.size();
            }
            if (progressBar != null)    // If a progress bar was provided
                progressBar.setMaximum(total);
                // Repopulate the list data in the database
            repopulateListData(listData,progressBar);
        } else {
            createTables(stmt); // Create the new tables
        }
        return true;
    }
    /**
     * 
     * @param stmt
     * @param program
     * @return
     * @throws SQLException 
     */
    public boolean updateDatabaseDefinitions(Statement stmt, 
            DisableGUIInput program) throws SQLException{
            // If the database cannot be automatically updated to the current version
        if (!isDatabaseCompatible())
            return false;
            // If the database is currently up to date
        if (!isDatabaseOutdated())
            return true;
            // Get the current state of the auto-commit
        boolean autoCommit = getAutoCommit();
            // Turn off the auto-commit in order to group the following database 
            // transactions to improve performance
        setAutoCommit(false);
            // This gets the database properties map
        DatabasePropertyMap properties = getDatabaseProperties();
            // This gets the database version as a String
        String versionStr = getDatabaseVersionStr();
            // This gets the database version as an array of integers
        int[] version = getDatabaseVersion();
            // This gets the progress bar from the program if there is one
        JProgressBar progressBar = null;
            // This gets the progress display menu from the program if there is 
        JProgressDisplayMenu progressMenu = null;   // one
        if (program != null){   // If a program was provided
            progressMenu = program.getProgressDisplayMenu();
            progressBar = program.getProgressBar();
        }   // The progress text from the progress display menu
        String progressText = null; 
        if (progressMenu != null){  // If the program has a progress display menu
            progressText = progressMenu.getString();
            progressMenu.setString("Updating Database");
        }   // This gets whether foreign keys are enabled (or supported)
        Boolean foreignKeys = null;
            // If foreign keys are supported
        if (getForeignKeysSupported(stmt)){
                // Get whether foreign keys are enabled
            foreignKeys = isForeignKeysEnabled(stmt);
                // Disable foreign keys
            setForeignKeysEnabled(false, stmt);
        }
            // This gets whether this was successful in updating the database
        boolean updateSuccess = true;
//            // If the version is 0.x.x
//        if (version[0] == 0){
//                // Try to update the pre-config database
//            updateSuccess = updateOldDatabaseDefinitions(stmt,program,versionStr);
//                // If the database was not successfully updated to 
//            if (updateSuccess){
//                versionStr = "1.0.0";
//                version = new int[]{1,0,0};
//            }
//        }
            // Determine the updates to apply based off the database version
        switch(versionStr){
            case("1.0.0"):      // If version 1.0.0
                    // Delete the old prefix count view
                deleteView(PREFIX_COUNT_VIEW_NAME,stmt);
                    // Delete the old list size view
                deleteView(LIST_SIZE_VIEW_NAME,stmt);
            case("1.1.0"):      // If version 1.1.0
                    // Update the prefix separators
                properties.getDefaults().put(PREFIX_SEPARATORS_CONFIG_KEY, 
                        PREFIX_DEFAULT_SEPARATORS);
                    // Remove any properties where the value is set to the default
                for (String prop : properties.propertyNameSet()){
                    properties.remove(prop, properties.getDefaults().get(prop));
                }
            case("1.2.0"):      // If version 1.2.0
                    // Delete the list contents view (adding the linkID column 
                    // to the list contents view)
                deleteView(LIST_CONTENTS_VIEW_NAME,stmt);
            case("1.3.0"):      // If version 1.3.0
                    // Delete the tables view
                deleteView(TABLES_VIEW_NAME,stmt);
            case("1.4.0"):      // If version 1.4.0
                    // Change the default database version
                properties.getDefaults().put(DATABASE_VERSION_CONFIG_KEY, 
                        DEFAULT_DATABASE_VERSION);
            case("1.4.1"):      // If version 1.4.1
                    // Delete the tables view (again)
                deleteView(TABLES_VIEW_NAME,stmt);
            case("1.5.0"):      // If version 1.5.0
                    // Delete the list size view again
                deleteView(LIST_SIZE_VIEW_NAME,stmt);
            case("1.5.1"):      // If version 1.5.1
                    // Delete the distinct links view
                deleteView(DISTINCT_LINK_VIEW_NAME,stmt);
            case("1.6.0"):      // If version 1.6.0
                createTables(stmt); // Create the new tables
            case("2.0.0"):      // If version 2.0.0
                    // Update the database to version 2.1.0
                updateSuccess = updateToVersion2_1_0(stmt,progressBar);
                    // If the update was not successful
                if (!updateSuccess)
                    break;
            case("2.1.0"):      // If version 2.1.0
                    // Delete the list contents view
                deleteView(LIST_CONTENTS_VIEW_NAME,stmt);
                    // Create the new tables
                createTables(stmt);
            case("2.2.0"):      // If version 2.2.0
                    // Update the database to version 3.0.0
                updateSuccess = updateToVersion3_0_0(stmt,progressBar);
                    // If the update was not successful
                if (!updateSuccess)
                    break;
            case("3.0.0"):      // If version 3.0.0
                setDatabaseUUIDIfAbsent();
//            case("3.1.0"):      // If version 3.1.0
        }
            // If foreign keys are supported
        if (foreignKeys != null)
                // Restore the foreign keys settings
            setForeignKeysEnabled(foreignKeys, stmt);
            // Update the database version to the current version
        properties.setProperty(DATABASE_VERSION_CONFIG_KEY, DATABASE_VERSION);
        if (progressMenu != null)   // If the program has a progress display menu
            progressMenu.setText(progressText);
        commit();       // Commit the changes to the database
            // Restore the auto-commit back to what it was set to before
        setAutoCommit(autoCommit);
        return updateSuccess;
    }
    /**
     * 
     * @param stmt
     * @return
     * @throws SQLException 
     */
    public boolean updateDatabaseDefinitions(Statement stmt) throws SQLException{
        return updateDatabaseDefinitions(stmt,null);
    }
    /**
     * 
     * @param program
     * @return
     * @throws SQLException 
     */
    public boolean  updateDatabaseDefinitions(DisableGUIInput program) throws 
            SQLException{
            // Whether the update was successful
        boolean success;
            // Create a statement to rename the column
        try(Statement stmt = createStatement()){
            success = updateDatabaseDefinitions(stmt, program);
        }
        return success;
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public boolean updateDatabaseDefinitions() throws SQLException{
        return updateDatabaseDefinitions((DisableGUIInput)null);
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public String getDatabaseVersionStr() throws SQLException{
        return getDatabaseProperties().getProperty(
                DATABASE_VERSION_CONFIG_KEY, DEFAULT_DATABASE_VERSION);
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public int[] getDatabaseVersion() throws SQLException{
        // Implement a check for semantic versioning
            // Get the version from the database, as a String
        String version = getDatabaseVersionStr();
            // Split it at periods
        String[] arr = version.split("[.]");
            // Create an array to contain the numbers from the split up strings
        int[] verArr = new int[arr.length];
            // Go through the split up parts of the version
        for (int i = 0; i < arr.length; i++){
            try{
                verArr[i] = Integer.parseInt(arr[i]);
            } catch(NumberFormatException ex){
                verArr[i] = -1;
            }
        }
        return verArr;
    }
    /**
     * 
     * @param index
     * @return
     * @throws SQLException 
     */
    private int getDatabaseVersionAt(int index) throws SQLException{
        // Implement a check for semantic versioning
            // Get the version from the database, as a String
        String version = getDatabaseVersionStr();
            // Split it at periods
        String[] arr = version.split("[.]");
        try{
            return Integer.parseInt(arr[index]);
        } catch(NumberFormatException ex){
            return -1;
        }
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public int getDatabaseMajorVersion() throws SQLException{
        return getDatabaseVersionAt(0);
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public int getDatabaseMinorVersion() throws SQLException{
        return getDatabaseVersionAt(1);
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public int getDatabasePatchVersion() throws SQLException{
        return getDatabaseVersionAt(2);
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public boolean isDatabaseCompatible() throws SQLException{
        // Implement a check for semantic versioning
            // Get the major version of the database
        int major = getDatabaseMajorVersion();
            // Return whether the database is at least version 1.0.0 and is at 
            // most the current version of the database
        return major >= 1 && major <= DATABASE_MAJOR_VERSION;
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public boolean isDatabaseOutdated() throws SQLException{
        // Implement a check for semantic versioning
            // Get the database version
        int[] version = getDatabaseVersion();
        return version[0] != DATABASE_MAJOR_VERSION ||  
                version[1] < DATABASE_MINOR_VERSION || 
                version[2] < DATABASE_PATCH_VERSION;
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public UUID getDatabaseUUID() throws SQLException{
        String value = getDatabaseProperties().getProperty(DATABASE_UUID);
        if (value == null || value.isBlank())
            return null;
        value = value.trim();
        if (value.length() > 32)
            value = value.substring(0, 32);
        int lowStart = Math.max(value.length() - 16,0);
        long leastSig = 0, mostSig = 0;
        try{
            leastSig = Long.parseUnsignedLong(value.substring(lowStart), 16);
        } catch (NumberFormatException ex){ 
            return null;
        }
        if (lowStart > 0){
            try{
                mostSig = Long.parseUnsignedLong(value.substring(0, lowStart),16);
            } catch (NumberFormatException ex){ 
                return null;
            }
        }
        return new UUID(mostSig,leastSig);
    }
    /**
     * 
     * @param uuid
     * @return 
     */
    private String uuidToString(UUID uuid){
        if (uuid == null)
            return null;
        return String.format("%016X%016X",uuid.getMostSignificantBits(),
                    uuid.getLeastSignificantBits());
    }
    /**
     * 
     * @param uuid
     * @throws SQLException 
     */
    public void setDatabaseUUID(UUID uuid) throws SQLException{
        getDatabaseProperties().setProperty(DATABASE_UUID, uuidToString(uuid));
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public UUID setDatabaseUUID() throws SQLException{
        UUID uuid = UUID.randomUUID();
        setDatabaseUUID(uuid);
        return uuid;
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public UUID setDatabaseUUIDIfAbsent() throws SQLException{
        UUID uuid = getDatabaseUUID();
        if (uuid == null)
            return setDatabaseUUID();
        return uuid;
    }
    /**
     * 
     * @param stmt
     * @throws SQLException 
     */
    protected void createTableView(Statement stmt)throws SQLException{
        stmt.execute(CREATE_TABLES_VIEW_QUERY);
        stmt.execute(CREATE_TABLE_INDEX_VIEW_QUERY);
    }
    /**
     * 
     * @throws SQLException 
     */
    protected void createTableView()throws SQLException{
            // Create a statement to create the tables view
        try(Statement stmt = createStatement()){
            createTableView(stmt);
        }
    }
    /**
     * 
     * @param stmt
     * @throws SQLException 
     */
    public void createTables(Statement stmt) throws SQLException{
        // TODO: Determine if this is either creating a new database or 
        // initializing the database config table for an existing database
        // (i.e. determine if this is a new database or a pre-1.0.0 database).
        // In the case of an existing database without a config table, no value 
        // should be set for the database version
//            // Get whether this database has a database config table 
//        boolean preConfig = showTables().contains(DATABASE_CONFIG_TABLE_NAME);
            // Go through the table creation queries
        for (String query : TABLE_CREATION_QUERIES)
            stmt.execute(query);
            // Add all the initial prefixes to the prefix map if they weren't 
            // present beforehand
        getPrefixMap().addAllIfAbsent(Arrays.asList(INITIAL_LINK_PREFIXES));
            // Go through the default values for the database config properties
        for (String[] defaultValues : CONFIG_DEFAULT_VALUES){
            getDatabaseProperties().setPropertyIfAbsent(defaultValues[0], 
                    defaultValues[2], defaultValues[1]);
        }   // Set the database's UUID if not present
        setDatabaseUUIDIfAbsent();
    }
    /**
     * 
     * @throws SQLException 
     */
    public void createTables() throws SQLException{
            // Create a statement to create the tables, views, and indexes
        try (Statement stmt = createStatement()) {
            createTables(stmt);
        }
    }
    
//    public boolean createTable(String tableCreationQuery, boolean ifNotExists, 
//            boolean withoutRowID){
//        
//    }
    /**
     * 
     * @param oldTableName
     * @param newTableName
     * @param stmt
     * @throws SQLException 
     */
    public void renameTable(String oldTableName, String newTableName, 
            Statement stmt) throws SQLException{
        stmt.execute(String.format("ALTER TABLE %s RENAME TO %s",oldTableName,
                newTableName));
    }
    /**
     * 
     * @param oldTableName
     * @param newTableName
     * @throws SQLException 
     */
    public void renameTable(String oldTableName, String newTableName) throws 
            SQLException{
            // Create a statement to rename the table
        try (Statement stmt = createStatement()) {
            renameTable(oldTableName,newTableName,stmt);
        }
    }
    /**
     * 
     * @param name
     * @param type
     * @param ifExists
     * @param stmt
     * @throws SQLException 
     */
    protected void deleteSQLData(String name, String type, boolean ifExists, 
            Statement stmt) throws SQLException{
        stmt.execute(String.format("DROP %s %s%s", type, 
                    // If this should only delete it if it exists, specify it.
                (ifExists)?"IF EXISTS ":"",name));
    }
    /**
     * 
     * @param tableName
     * @param ifExists
     * @param stmt
     * @throws SQLException 
     */
    public void deleteTable(String tableName, boolean ifExists, Statement stmt) 
            throws SQLException{
        deleteSQLData(tableName,"TABLE",ifExists,stmt);
    }
    /**
     * 
     * @param tableName
     * @param ifExists
     * @throws SQLException 
     */
    public void deleteTable(String tableName, boolean ifExists) throws 
            SQLException{
            // Create a statement to delete the table
        try (Statement stmt = createStatement()) {
            deleteTable(tableName,ifExists,stmt);
        }
    }
    /**
     * 
     * @param tableName
     * @param stmt
     * @throws SQLException 
     */
    public void deleteTable(String tableName, Statement stmt) throws SQLException{
        deleteTable(tableName,true,stmt);
    }
    /**
     * 
     * @param tableName
     * @throws SQLException 
     */
    public void deleteTable(String tableName) throws SQLException{
            // Create a statement to delete the table
        try (Statement stmt = createStatement()) {
            deleteTable(tableName,stmt);
        }
    }
    /**
     * 
     * @param viewName
     * @param ifExists
     * @param stmt
     * @throws SQLException 
     */
    public void deleteView(String viewName, boolean ifExists, Statement stmt) 
            throws SQLException{
        deleteSQLData(viewName,"VIEW",ifExists,stmt);
    }
    /**
     * 
     * @param viewName
     * @param ifExists
     * @throws SQLException 
     */
    public void deleteView(String viewName, boolean ifExists) throws 
            SQLException{
            // Create a statement to delete the view
        try (Statement stmt = createStatement()) {
            deleteView(viewName,ifExists,stmt);
        }
    }
    /**
     * 
     * @param viewName
     * @param stmt
     * @throws SQLException 
     */
    public void deleteView(String viewName, Statement stmt) throws SQLException{
        deleteView(viewName,true,stmt);
    }
    /**
     * 
     * @param viewName
     * @throws SQLException 
     */
    public void deleteView(String viewName) throws SQLException{
            // Create a statement to delete the view
        try (Statement stmt = createStatement()) {
            deleteView(viewName,stmt);
        }
    }
    /**
     * 
     * @param indexName
     * @param ifExists
     * @param stmt
     * @throws SQLException 
     */
    public void deleteIndex(String indexName, boolean ifExists, Statement stmt) 
            throws SQLException{
        deleteSQLData(indexName,"INDEX",ifExists,stmt);
    }
    /**
     * 
     * @param indexName
     * @param ifExists
     * @throws SQLException 
     */
    public void deleteIndex(String indexName, boolean ifExists) throws 
            SQLException{
            // Create a statement to delete the index
        try (Statement stmt = createStatement()) {
            deleteIndex(indexName,ifExists,stmt);
        }
    }
    /**
     * 
     * @param indexName
     * @param stmt
     * @throws SQLException 
     */
    public void deleteIndex(String indexName, Statement stmt) throws SQLException{
        deleteIndex(indexName,true,stmt);
    }
    /**
     * 
     * @param indexName
     * @throws SQLException 
     */
    public void deleteIndex(String indexName) throws SQLException{
            // Create a statement to delete the index
        try (Statement stmt = createStatement()) {
            deleteIndex(indexName,stmt);
        }
    }
    /**
     * 
     * @param tableName
     * @param columnDefinition
     * @param stmt
     * @throws SQLException 
     */
    public void addColumn(String tableName, String columnDefinition, 
            Statement stmt) throws SQLException{
        stmt.execute(String.format("ALTER TABLE %s ADD COLUMN %s", tableName,
                columnDefinition));
    }
    /**
     * 
     * @param tableName
     * @param columnDefinition
     * @throws SQLException 
     */
    public void addColumn(String tableName, String columnDefinition) throws 
            SQLException{
            // Create a statement to add the column
        try (Statement stmt = createStatement()) {
            addColumn(tableName,columnDefinition,stmt);
        }
    }
    /**
     * 
     * @param tableName
     * @param columnName
     * @param columnDefinition
     * @param stmt
     * @throws SQLException 
     */
    public void addColumn(String tableName, String columnName, 
            String columnDefinition, Statement stmt) throws SQLException{
        addColumn(tableName,columnName+" "+columnDefinition,stmt);
    }
    /**
     * 
     * @param tableName
     * @param columnName
     * @param columnDefinition
     * @throws SQLException 
     */
    public void addColumn(String tableName, String columnName, 
            String columnDefinition) throws SQLException{
            // Create a statement to add the column
        try (Statement stmt = createStatement()) {
            addColumn(tableName,columnName,columnDefinition,stmt);
        }
    }
    /**
     * 
     * @param tableName
     * @param oldName
     * @param newName
     * @param stmt
     * @throws SQLException 
     */
    public void renameColumn(String tableName, String oldName, String newName, 
            Statement stmt) throws SQLException{
        stmt.execute(String.format("ALTER TABLE %s RENAME COLUMN %s TO %s", 
                tableName, oldName, newName));
    }
    /**
     * 
     * @param tableName
     * @param oldName
     * @param newName
     * @throws SQLException 
     */
    public void renameColumn(String tableName, String oldName, String newName) 
            throws SQLException{
            // Create a statement to rename the column
        try (Statement stmt = createStatement()) {
            renameColumn(tableName,oldName,newName,stmt);
        }
    }
    
    
    
    /**
     * This returns the size of the given table
     * @param tableName
     * @param columnName
     * @param stmt
     * @return
     * @throws SQLException 
     */
    protected int getTableSize(String tableName, String columnName, Statement stmt) 
            throws SQLException{
            // Query the database for the size of the table
        ResultSet results = stmt.executeQuery(String.format(
                TABLE_SIZE_QUERY_TEMPLATE, 
                    columnName,
                    tableName));
            // If the query has any results
        if (results.next())
            return results.getInt(COUNT_COLUMN_NAME);
        return 0;
    }
    /**
     * This returns the size of the given table
     * @param tableName
     * @param columnName
     * @return
     * @throws SQLException 
     */
    public int getTableSize(String tableName, String columnName) throws 
            SQLException{
            // This will get the table size
        int size;
            // Create a statement to get the table size
        try (Statement stmt = createStatement()) {
            size = getTableSize(tableName,columnName,stmt);
        }
        return size;
    }
    /**
     * 
     * @param tableName
     * @return
     * @throws SQLException 
     */
    public int getTableSize(String tableName) throws SQLException{
        return getTableSize(tableName,ROW_ID_COLUMN_NAME);
    }
    /**
     * 
     * @param tableName
     * @param columnName
     * @return
     * @throws SQLException 
     */
    public boolean isTableEmpty(String tableName, String columnName) throws SQLException{
        return getTableSize(tableName,columnName) == 0;
    }
    /**
     * 
     * @param tableName
     * @return
     * @throws SQLException 
     */
    public boolean isTableEmpty(String tableName) throws SQLException{
        return getTableSize(tableName) == 0;
    }
    /**
     * 
     * @param results
     * @return
     * @throws SQLException 
     */
    protected static boolean containsCountResult(ResultSet results)throws SQLException{
            // If the results contain a value
        if (results.next())
            return results.getInt(COUNT_COLUMN_NAME) > 0;
        return false;
    }
    /**
     * 
     * @param tableName
     * @throws SQLException 
     */
    public void clearTable(String tableName) throws SQLException{
            // Prepare a statement to delete the contents of the table
        try (PreparedStatement pstmt = prepareStatement(
                String.format("DELETE FROM %s", tableName))) {
                // Update the database
            pstmt.executeUpdate();
        }
    }
    /**
     * 
     * @param tableName
     * @param sourceTableName
     * @param idColumnName
     * @return
     * @throws SQLException 
     */
    protected int removeUnusedRows(String tableName, String sourceTableName, 
            String idColumnName)throws SQLException{
            // Prepare a statement to delete the rows in the first table that 
            // are not referenced by the second table
        try(PreparedStatement pstmt = prepareStatement(String.format(
                "DELETE FROM %s WHERE %s NOT IN (SELECT %s FROM %s)", 
                        tableName,
                        idColumnName,
                        idColumnName,
                        sourceTableName))){
                // Update the database
            pstmt.executeUpdate();
            return pstmt.getUpdateCount();
        }
    }
    /**
     * This sets the designated parameter on the given prepared statement to the 
     * given value. If the given value is null, then the parameter is set to 
     * null of the appropriate SQL type. Otherwise, the parameter is set to the 
     * given integer.
     * @param pstmt The prepared statement to set the designated parameter for.
     * @param parameterIndex The index for the parameter. This uses 1-based 
     * indexing, with the first parameter being 1.
     * @param value The parameter value.
     * @throws SQLException If either {@code parameterIndex} does not correspond 
     * to a parameter marker in the SQL statement, a database access error 
     * occurs, or {@code pstmt} is closed.
     * @see PreparedStatement#setInt(int, int) 
     * @see PreparedStatement#setNull(int, int) 
     */
    protected void setParameter(PreparedStatement pstmt, int parameterIndex, 
            Integer value) throws SQLException{
        if (value == null)  // If the given value is null
            pstmt.setNull(parameterIndex, Types.INTEGER);
        else
            pstmt.setInt(parameterIndex, value);
    }
    /**
     * This sets the designated parameter on the given prepared statement to the 
     * given value. If the given value is null, then the parameter is set to 
     * null of the appropriate SQL type. Otherwise, the parameter is set to the 
     * given long integer.
     * @param pstmt The prepared statement to set the designated parameter for.
     * @param parameterIndex The index for the parameter. This uses 1-based 
     * indexing, with the first parameter being 1.
     * @param value The parameter value.
     * @throws SQLException If either {@code parameterIndex} does not correspond 
     * to a parameter marker in the SQL statement, a database access error 
     * occurs, or {@code pstmt} is closed.
     * @see PreparedStatement#setLong(int, long) 
     * @see PreparedStatement#setNull(int, int) 
     */
    protected void setParameter(PreparedStatement pstmt, int parameterIndex, 
            Long value) throws SQLException{
        if (value == null)  // If the given value is null
            pstmt.setNull(parameterIndex, Types.BIGINT);
        else
            pstmt.setLong(parameterIndex, value);
    }
    /**
     * 
     * @param stmt
     * @return 
     */
    private Integer getGeneratedIntegerKey(Statement stmt){
            // This gets the the key that was generated
        Integer key = null;
        try{    // Query the statement for the generated keys
            ResultSet genKeys = stmt.getGeneratedKeys();
                // While non-null generated keys have been found
            while(genKeys.next() && key == null){
                    // Get the next key
                key = genKeys.getInt(1);
                    // If the key was somehow null
                if (genKeys.wasNull())
                    key = null;
            }
        }
        catch(SQLException ex){ }
        return key;
    }
    /**
     * 
     * @param stmt
     * @return 
     */
    private Long getGeneratedLongKey(Statement stmt){
            // This gets the the key that was generated
        Long key = null;
        try{    // Query the statement for the generated keys
            ResultSet genKeys = stmt.getGeneratedKeys();
                // While non-null generated keys have been found
            while(genKeys.next() && key == null){
                    // Get the next key
                key = genKeys.getLong(1);
                    // If the key was somehow null
                if (genKeys.wasNull())
                    key = null;
            }
        }
        catch(SQLException ex){ }
        return key;
    }
    /**
     * 
     * @param tableName
     * @param keyColumnName
     * @param valueColumnName
     * @return
     * @throws SQLException 
     */
    protected PreparedStatement prepareContainsStatement(String tableName, 
            String keyColumnName, String valueColumnName) throws SQLException {
        return prepareStatement(String.format(TABLE_CONTAINS_QUERY_TEMPLATE, 
                keyColumnName, tableName, valueColumnName));
    }
    /**
     * 
     * @param tableName
     * @param columnName
     * @return
     * @throws SQLException 
     */
    protected PreparedStatement prepareContainsStatement(String tableName, 
            String columnName) throws SQLException {
        return prepareContainsStatement(tableName, columnName, columnName);
    }
    /**
     * 
     * @param prefixID
     * @return
     * @throws SQLException 
     */
    protected boolean containsPrefixID(int prefixID) throws SQLException {
            // Prepare a statement to check if the database contains the given 
        try(PreparedStatement pstmt = prepareContainsStatement( // prefixID
                PREFIX_TABLE_NAME,
                PREFIX_ID_COLUMN_NAME)){
            pstmt.setInt(1, prefixID);
            return containsCountResult(pstmt.executeQuery());
        }
    }
    /**
     * 
     * @param linkID
     * @return
     * @throws SQLException 
     */
    protected boolean containsLinkID(long linkID) throws SQLException {
            // Prepare a statement to check if the database contains the given 
        try(PreparedStatement pstmt = prepareContainsStatement( // linkID
                LINK_TABLE_NAME,
                LINK_ID_COLUMN_NAME)){
            pstmt.setLong(1, linkID);
            return containsCountResult(pstmt.executeQuery());
        }
    }
    /**
     * 
     * @param listID
     * @return
     * @throws SQLException 
     */
    protected boolean containsListID(int listID) throws SQLException {
            // Prepare a statement to check if the database contains the given 
        try(PreparedStatement pstmt = prepareContainsStatement( // listID
                LIST_TABLE_NAME,
                LIST_ID_COLUMN_NAME)){
            pstmt.setInt(1, listID);
            return containsCountResult(pstmt.executeQuery());
        }
    }
    /**
     * 
     * @param prefixID
     * @param name
     * @throws SQLException 
     */
    private void checkPrefixID(int prefixID, String name) throws SQLException {
            // If there is no prefix with the given prefixID
        if (!containsPrefixID(prefixID))
            throw new IllegalArgumentException("No prefix with "+name+
                    "prefix ID "+ prefixID);
    }
    /**
     * 
     * @param prefixID
     * @throws SQLException 
     */
    protected void checkPrefixID(int prefixID) throws SQLException {
        checkPrefixID(prefixID,"");
    }
    /**
     * 
     * @param oldPrefixID
     * @param newPrefixID
     * @throws SQLException 
     */
    private void checkPrefixes(int oldPrefixID, int newPrefixID) throws 
            SQLException{
            // Check the old prefixID
        checkPrefixID(oldPrefixID,"old ");
            // Check the new prefixID
        checkPrefixID(newPrefixID,"new ");
    }
    /**
     * 
     * @param listID
     * @throws SQLException 
     */
    protected void checkListID(int listID) throws SQLException {
            // If there is no list with the given listID
        if (!containsListID(listID))
            throw new IllegalArgumentException("No list with list ID "+listID);
    }
    /**
     * 
     * @param linkID
     * @param prefixID
     * @param value
     * @throws SQLException 
     */
    private void updateLink(long linkID, int prefixID, String value) 
            throws SQLException {
            // Prepare a statement to update the link with the given linkID
        try (PreparedStatement pstmt = prepareStatement(
                String.format("UPDATE %s SET %s = ?, %s = ? WHERE %s = ?", 
                        LINK_TABLE_NAME,
                        PREFIX_ID_COLUMN_NAME,
                        LINK_URL_COLUMN_NAME,
                        LINK_ID_COLUMN_NAME))){
                // Set the prefixID for the prefix for the link
            pstmt.setInt(1, prefixID);
                // Set the suffix for the link
            pstmt.setString(2, value);
                // Set the linkID of the link to update
            pstmt.setLong(3, linkID);
                // Update the database
            pstmt.executeUpdate();
        }
    }
    /**
     * 
     * @param listID
     * @return
     * @throws SQLException 
     */
    protected boolean removeList(int listID) throws SQLException{
            // Prepare a statement to remove the list with the given listID
        try (PreparedStatement pstmt = prepareStatement(
                String.format("DELETE FROM %s WHERE %s = ?", 
                        LIST_TABLE_NAME,
                        LIST_ID_COLUMN_NAME))) {
                // Set the listID of the list to be removed
            pstmt.setInt(1, listID);
                // Update the database
            pstmt.executeUpdate();
                // Return whether anything changed in the database
            return pstmt.getUpdateCount() > 0;
        }
    }
    /**
     * 
     * @param listType
     * @return
     * @throws SQLException 
     */
    protected int getTotalSizeOfLists(Integer listType) throws SQLException{
            // If a list type was not specified
        if (listType == null){
                // Prepare a statement to get the sum of the total sizes of the 
                // lists of lists
            try(PreparedStatement pstmt = prepareStatement(String.format(
                    "SELECT SUM(%s) AS %s FROM %s", 
                        TOTAL_LIST_SIZE_COLUMN_NAME,
                        COUNT_COLUMN_NAME,
                        TOTAL_LIST_SIZE_VIEW_NAME))){
                    // Get the results of the query
                ResultSet rs = pstmt.executeQuery();
                    // If there are any results 
                if (rs.next())
                    return rs.getInt(COUNT_COLUMN_NAME);
            }
            return 0;
        }   // Prepare a statement to get the total size of the list of lists 
            // with the given type
        try(PreparedStatement pstmt = prepareStatement(String.format(
                "SELECT %s FROM %s WHERE %s = ?",
                    TOTAL_LIST_SIZE_COLUMN_NAME,
                    TOTAL_LIST_SIZE_VIEW_NAME,
                    LIST_TYPE_COLUMN_NAME))){
                // Set the type of list to get the total size of
            pstmt.setInt(1, listType);
                // Get the results of the query
            ResultSet rs = pstmt.executeQuery();
                // If there are any results 
            if (rs.next()){
                    // Get the total size of the list
                int size = rs.getInt(TOTAL_LIST_SIZE_COLUMN_NAME);
                    // If that total size is not null
                if (!rs.wasNull())
                    return size;
            }
        }
        return 0;
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public int getTotalSizeOfLists() throws SQLException{
            // Prepare a statement to get the total size of all the lists
        try(PreparedStatement pstmt = prepareStatement(String.format(
                "SELECT SUM(%s) AS %s FROM %s", 
                    LIST_SIZE_COLUMN_NAME,
                    COUNT_COLUMN_NAME,
                    LIST_SIZE_VIEW_NAME))){
                // Get the results of the query
            ResultSet rs = pstmt.executeQuery();
                // If there are any results 
            if (rs.next())
                return rs.getInt(COUNT_COLUMN_NAME);
        }
        return 0;
    }
    /**
     * 
     * @param listType
     * @return
     * @throws SQLException 
     */
    protected int getListCount(Integer listType) throws SQLException{
            // If a list type was not specified
        if (listType == null)
                // Return the number of lists in the database
            return getTableSize(LIST_TABLE_NAME,LIST_ID_COLUMN_NAME);
            // Prepare a statement to get the amount of lists in the list of 
            // lists with the given type
        try(PreparedStatement pstmt = prepareStatement(String.format(
                "SELECT %s FROM %s WHERE %s = ?",
                    LIST_COUNT_COLUMN_NAME,
                    TOTAL_LIST_SIZE_VIEW_NAME,
                    LIST_TYPE_COLUMN_NAME))){
                // Set the type of list of lists to get
            pstmt.setInt(1, listType);
                // Get the results of the query
            ResultSet rs = pstmt.executeQuery();
                // If there are any results 
            if (rs.next()){
                    // Get the amount of lists in the list of lists
                int size = rs.getInt(LIST_COUNT_COLUMN_NAME);
                    // If the amount of lists is not null
                if (!rs.wasNull())
                    return size;
            }
        }
        return 0;
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public int getListCount() throws SQLException{
        return getListCount(null);
    }
    /**
     * 
     * @param listID
     * @return
     * @throws SQLException 
     */
    public int getListSize(int listID) throws SQLException{
            // Prepare a statement to get the size of the list with the given 
        try(PreparedStatement pstmt = prepareStatement(     // list ID
                String.format("SELECT %s FROM %s WHERE %s = ?", 
                        LIST_SIZE_COLUMN_NAME,
                        LIST_SIZE_VIEW_NAME,
                        LIST_ID_COLUMN_NAME))){
                // Set the listID of the list to get the size of
            pstmt.setInt(1, listID);
                // Get the results of the query
            ResultSet rs = pstmt.executeQuery();
                // If there are any results 
            if (rs.next()){
                    // Get the size of the list
                int size = rs.getInt(LIST_SIZE_COLUMN_NAME);
                    // If the size of the list is not null
                if (!rs.wasNull())
                    return size;
            }
        }
        return 0;
    }
    /**
     * 
     * @param listID
     * @throws SQLException 
     */
    protected void clearList(Integer listID) throws SQLException{
            // If the listID was not specified
        if (listID == null)
                // Clear the list data table
            clearTable(LIST_DATA_TABLE_NAME);
        else{   // Prepare a statement to empty the list with the given listID
            try (PreparedStatement pstmt = prepareStatement(
                    String.format("DELETE FROM %s WHERE %s = ?", 
                            LIST_DATA_TABLE_NAME,
                            LIST_ID_COLUMN_NAME))) {
                    // Set the listID of the list to clear
                pstmt.setInt(1, listID);
                    // Update the database
                pstmt.executeUpdate();
            }
        }
    }
    
    
    
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public PrefixMap getPrefixMap() throws SQLException{
            // If the prefix map view has not been initialized yet
        if (prefixMap == null)
            prefixMap = new PrefixMapImpl();
        return prefixMap;
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public LinkMap getLinkMap() throws SQLException{
            // If the link map view has not been initialized yet
        if (linkMap == null)
            linkMap = new LinkMapImpl();
        return linkMap;
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public ListNameMap getListNameMap() throws SQLException{
            // If the list name map view has not been initialized yet
        if (listNameMap == null)
            listNameMap = new ListNameMapImpl();
        return listNameMap;
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public SQLSet<Integer> getListTypes() throws SQLException{
            // If the list type set view has not been initialized yet
        if (listTypeSet == null)
            listTypeSet = new ListTypeSet();
        return listTypeSet;
    }
    /**
     * 
     * @param listType
     * @return
     * @throws SQLException 
     */
    public ListIDList getListIDs(int listType) throws SQLException{
            // If the listIDList map has not been initialized yet
        if (listIDLists == null)
            listIDLists = new TreeMap<>();
            // If the listIDList for the given list type has not been 
            // initialized yet
        if (!listIDLists.containsKey(listType))
            listIDLists.put(listType, new ListIDListImpl(listType));
        return listIDLists.get(listType);
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public ListIDList getShownListIDs() throws SQLException{
        return getListIDs(LIST_OF_SHOWN_LISTS_TYPE);
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public ListIDList getAllListIDs() throws SQLException{
        return getListIDs(LIST_OF_ALL_LISTS_TYPE);
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public ListDataMap getListDataMap() throws SQLException{
            // If the list data map view has not been initialized yet
        if (listDataMap == null)
            listDataMap = new ListDataMapImpl();
        return listDataMap;
    }
    /**
     * 
     * @param listType
     * @return
     * @throws SQLException 
     */
    public ListDataList getListDataList(int listType) throws SQLException{
        return new ListDataListImpl(listType);
    }
    /**
     * 
     * @param listID
     * @return
     * @throws SQLException 
     */
    public ListContents getListContents(int listID) throws SQLException{
        checkListID(listID);
        return getListDataMap().get(listID);
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public DatabasePropertyMap getDatabaseProperties() throws SQLException{
            // If the database property map view has not been initialized yet
        if (propMap == null)
            propMap = new DatabasePropertyMapImpl();
        return propMap;
    }
    
    
    
    /**
     * This returns the threshold for the amount of links that need to share a 
     * given prefix for that prefix to be automatically added to the database. 
     * If this fails to load the value from the database, then this will return 
     * {@value PREFIX_THRESHOLD_CONFIG_DEFAULT}.
     * @return The threshold for adding a prefix to the database.
     * @see PREFIX_THRESHOLD_CONFIG_KEY
     * @see getDatabaseProperties()
     * @see PrefixMap#createPrefixesFrom(Collection) 
     */
    protected int getPrefixThreshold(){
            // This will get the String containing the prefix threshold property 
        String value = null;    // loaded from the database
        try {   // Get the threshold from the database
            value = getDatabaseProperties().getProperty(PREFIX_THRESHOLD_CONFIG_KEY);
        } catch (SQLException ex) {}
            // If the threshold property was successfully retrieved from the 
            // database
        if (value != null){
            try{    // Try to parse the value loaded
                return Integer.parseInt(value);
            } catch (NumberFormatException ex){ }
        }   // Fall back to the default
        return PREFIX_THRESHOLD_CONFIG_DEFAULT;
    }
    /**
     * This returns a String containing the characters at which to split at when 
     * creating the prefixes. This is done to avoid creating unnecessary 
     * prefixes and filling up the prefix table while only storing 1 letter 
     * links. If this fails to load the value from the database, then this 
     * returns "{@value PREFIX_DEFAULT_SEPARATORS}".
     * @return The characters at which to split at when creating prefixes.
     * @see PREFIX_SEPARATORS_CONFIG_KEY
     * @see getDatabaseProperties()
     * @see PrefixMap#createPrefixesFrom(Collection) 
    */
    protected String getPrefixSeparators(){
        try {
            return getDatabaseProperties().getProperty(
                    PREFIX_SEPARATORS_CONFIG_KEY,PREFIX_DEFAULT_SEPARATORS);
        } catch (SQLException ex) {
            return PREFIX_DEFAULT_SEPARATORS;
        }
    }
    /**
     * 
     * @param linkID
     * @throws SQLException 
     */
    public void updateLinkData(long linkID) throws SQLException{
            // Get the link for the given linkID
        String link = getLinkMap().get(linkID);
            // If there is no link mapped to the given linkID
        if (link == null)
            throw new IllegalArgumentException("No link with link ID "+linkID);
            // Get the prefixID of the longest matching prefix for the link
        int prefixID = getPrefixMap().getLongestPrefixIDFor(link);
            // Get the link's suffix
        link = getPrefixMap().getSuffix(link, prefixID);
            // Update the link in the database
        updateLink(linkID,prefixID,link);
    }
    /**
     * 
     * @param oldPrefixID
     * @param newPrefixID
     * @return
     * @throws SQLException 
     */
    public boolean changePrefixForLinks(int oldPrefixID, int newPrefixID) throws 
            SQLException{
            // Check the prefixIDs
        checkPrefixes(oldPrefixID, newPrefixID);
            // If the old and new prefixIDs are the same
        if (oldPrefixID == newPrefixID)
            return false;
            // Prepare a statement to update the list last modified times of the 
            // lists with links that use the old prefix
        try(PreparedStatement pstmt = prepareStatement(String.format(
                    // Update the last modified times of the lists with listIDs 
                    // found in the list data table that have links found in the 
                    // links table that have the old prefix
                "UPDATE %s SET %s = ? WHERE %s IN "
                        + "(SELECT DISTINCT %s FROM %s WHERE %s IN "
                            + "(SELECT %s FROM %s WHERE %s = ?))",
                    LIST_TABLE_NAME,
                    LIST_LAST_MODIFIED_COLUMN_NAME,
                    LIST_ID_COLUMN_NAME,
                    LIST_ID_COLUMN_NAME,
                    LIST_DATA_TABLE_NAME,
                    LINK_ID_COLUMN_NAME,
                    LINK_ID_COLUMN_NAME,
                    LINK_TABLE_NAME,
                    PREFIX_ID_COLUMN_NAME))){
                // Set the new last modified time
            pstmt.setLong(1, System.currentTimeMillis());
                // Set the prefixID for the prefix that will be replaced
            pstmt.setInt(2, oldPrefixID);
                // Update the database
            pstmt.executeUpdate();
        }   // Prepare a statement to replace the prefixes for the links with 
            // a given prefix in the links table
        try(PreparedStatement pstmt = prepareStatement(REPLACE_PREFIX_ID_QUERY)){
                // Set the prefixID for the replacement prefix
            pstmt.setInt(1, newPrefixID);
                // Set the prefixID for the prefix to replace
            pstmt.setInt(2, oldPrefixID);
                // Update the database
            pstmt.executeUpdate();
                // Return whether any rows were updated
            return pstmt.getUpdateCount() > 0;
        }
    }
    /**
     * 
     * @param oldPrefixID
     * @param newPrefixID
     * @param listID
     * @return
     * @throws SQLException 
     */
    public boolean changePrefixForLinks(int oldPrefixID, int newPrefixID, 
            int listID) throws SQLException{
            // Check the prefixIDs
        checkPrefixes(oldPrefixID, newPrefixID);
            // Get the list contents for the given listID
        ListContents list = getListContents(listID);
            // If the old and new prefixIDs are the same
        if (oldPrefixID == newPrefixID)
            return false;
            // Prepare a statement to replace the prefixes for the links in a 
            // list with a given prefix in the links table
        try(PreparedStatement pstmt = prepareStatement(REPLACE_PREFIX_ID_IN_LIST_QUERY)){
                // Set the prefixID for the replacement prefix
            pstmt.setInt(1, newPrefixID);
                // Set the prefixID for the prefix to replace
            pstmt.setInt(2, oldPrefixID);
                // Set the listID for the list containing the links to alter
            pstmt.setInt(3, listID);
                // Update the database
            pstmt.executeUpdate();
                // If no rows were updated
            if (pstmt.getUpdateCount() <= 0)
                return false;
        }   // Update the list's last modified time
        list.setLastModified();
        return true;
    }
    /**
     * 
     * @param <K> The type of keys maintained by the map.
     * @param <V> The type of mapped values.
     */
    private abstract class AbstractDatabaseRowMap<K,V> extends 
            AbstractQueryRowMap<K,V> {
        @Override
        public LinkDatabaseConnection getConnection() throws SQLException {
            return LinkDatabaseConnection.this;
        }
    }
    /**
     * 
     * @param <E> The type of elements stored in this list.
     */
    private abstract class AbstractDatabaseList<E> extends AbstractQueryList<E>{
        @Override
        public LinkDatabaseConnection getConnection() throws SQLException {
            return LinkDatabaseConnection.this;
        }
    }
    /**
     * 
     * @param <E> The type of elements stored in this set.
     */
    private abstract class AbstractQuerySet<E> extends AbstractSQLSet<E>{
        /**
         * {@inheritDoc }
         */
        @Override
        public LinkDatabaseConnection getConnection() throws SQLException {
            return LinkDatabaseConnection.this;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean addAllSQL(Collection<? extends E> c)throws SQLException{
                // Get the current state of the auto-commit
            boolean autoCommit = getAutoCommit();
                // Turn off the auto-commit in order to group the following 
                // database transactions to improve performance
            setAutoCommit(false);
                // Add all the elements in the given collection to this set and 
                // get if this set was modified as a result
            boolean modified = super.addAllSQL(c);
            commit();       // Commit the changes to the database
                // Restore the auto-commit back to what it was set to before
            setAutoCommit(autoCommit);
            return modified;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean removeAllSQL(Collection<?> c)throws SQLException{
                // Get the current state of the auto-commit
            boolean autoCommit = getAutoCommit();
                // Turn off the auto-commit in order to group the following 
                // database transactions to improve performance
            setAutoCommit(false);
                // Remove any elements in this set that are also in the given 
                // collection and get if this set was modified as a result
            boolean modified = super.removeAllSQL(c);
            commit();       // Commit the changes to the database
                // Restore the auto-commit back to what it was set to before
            setAutoCommit(autoCommit);
            return modified;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean retainAllSQL(Collection<?> c)throws SQLException{
                // Get the current state of the auto-commit
            boolean autoCommit = getAutoCommit();
                // Turn off the auto-commit in order to group the following 
                // database transactions to improve performance
            setAutoCommit(false);
                // Retain only the elements in this set that are also in the 
                // given collection and get if this set was modified as a result
            boolean modified = super.retainAllSQL(c);
            commit();       // Commit the changes to the database
                // Restore the auto-commit back to what it was set to before
            setAutoCommit(autoCommit);
            return modified;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected abstract boolean containsSQL(Object o) throws SQLException;
        /**
         * {@inheritDoc }
         */
        @Override
        protected abstract boolean removeSQL(Object o) throws SQLException;
        /**
         * 
         * @return
         * @throws SQLException 
         */
        protected abstract Set<E> valueCacheSet() throws SQLException;
        /**
         * 
         * @return
         * @throws SQLException 
         */
        protected Iterator<E> iteratorSQL() throws SQLException{
            return new CacheSetIterator<>(valueCacheSet()){
                @Override
                protected void remove(E value) {
                    AbstractQuerySet.this.remove(value);
                }
            };
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public Iterator<E> iterator(){
            try{
                return iteratorSQL();
            } catch (SQLException ex) {
                appendWarning(ex);
                return Collections.emptyIterator();
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public boolean equals(Object obj){
                // If the given object is this set
            if (obj == this)
                return true;
                // If the given object is a set
            else if (obj instanceof Set){
                    // Create a copy of this set (to reduce the number of 
                Set<E> temp = new HashSet<>(this);  // queries)
                    // Return whether the object matches the copy
                return temp.equals(obj);
            }
            return false;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public int hashCode() {
                // Create a copy of this set (to reduce the number of queries)
            Set<E> temp = new HashSet<>(this);
            return temp.hashCode();
        }
    }
    /**
     * 
     * @param <K> The type of keys maintained by the map.
     * @param <V> The type of mapped values.
     */
    private abstract class AbstractQueryMap<K, V> extends AbstractSQLMap<K, V>{
        /**
         * {@inheritDoc }
         */
        @Override
        public LinkDatabaseConnection getConnection() throws SQLException {
            return LinkDatabaseConnection.this;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected abstract boolean containsKeySQL(Object key) throws SQLException;
        /**
         * {@inheritDoc }
         */
        @Override
        protected abstract V removeSQL(Object key) throws SQLException;
        /**
         * {@inheritDoc }
         */
        @Override
        protected abstract V getSQL(Object key) throws SQLException;
        /**
         * {@inheritDoc }
         */
        @Override
        protected abstract V putSQL(K key, V value) throws SQLException;
        /**
         * 
         * @return
         * @throws SQLException 
         */
        protected abstract Set<Entry<K,V>> entryCacheSet() throws SQLException;
        /**
         * 
         * @return
         * @throws SQLException 
         */
        protected Iterator<Entry<K,V>> entryIteratorSQL() throws SQLException{
            return new CacheSetIterator<>(entryCacheSet()){
                @Override
                protected void remove(Entry<K,V> value) {
                    AbstractQueryMap.this.remove(value.getKey(),value.getValue());
                }
            };
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected Iterator<Entry<K,V>> entryIterator(){
            try{
                return entryIteratorSQL();
            } catch (SQLException ex) {
                appendWarning(ex);
                return Collections.emptyIterator();
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected void putAllSQL(Map<? extends K, ? extends V> m) 
                throws SQLException{
                // Get the current state of the auto-commit
            boolean autoCommit = getAutoCommit();
                // Turn off the auto-commit in order to group the following 
                // database transactions to improve performance
            setAutoCommit(false);
                // Put all the entries in the given map into this map
            super.putAllSQL(m);
            commit();       // Commit the changes to the database
                // Restore the auto-commit back to what it was set to before
            setAutoCommit(autoCommit);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public boolean equals(Object obj){
                // If the given object is this map
            if (obj == this)
                return true;
                // If the given object is a map
            else if (obj instanceof Map){
                    // Create a copy of this map (to reduce the number of 
                Map<K, V> temp = new HashMap<>(this);   // queries)
                    // Return whether the object matches the copy
                return temp.equals(obj);
            }
            return false;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public int hashCode() {
                // Create a copy of this map (to reduce the number of queries)
            Map<K, V> temp = new HashMap<>(this);
            return temp.hashCode();
        }
    }
    /**
     * 
     * @param <V> The type of mapped values.
     */
    private abstract class AbstractDatabaseTypeIDMap<V> extends 
            AbstractNavigableTypeIDMap<V>{
        /**
         * 
         * @param typeIDSet 
         */
        AbstractDatabaseTypeIDMap(NavigableSet<Integer> typeIDSet){
            super(typeIDSet);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public LinkDatabaseConnection getConnection() throws SQLException {
            return LinkDatabaseConnection.this;
        }
    }
    /**
     * 
     */
    private abstract class SchemaViewSet extends AbstractQuerySet<String>{
        /**
         * 
         */
        protected final String type;
        /**
         * 
         * @param type 
         */
        protected SchemaViewSet(String type) {
            this.type = type;
        }
        /**
         * 
         * @return 
         */
        protected String getType(){
            return type;
        }
        /**
         * 
         * @return 
         */
        protected abstract String getViewName();
        /**
         * 
         * @return 
         */
        protected abstract String getNameColumn();
        /**
         * 
         * @return 
         */
        protected abstract String getTypeColumn();
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean removeSQL(Object o) throws SQLException{
            throw new UnsupportedOperationException("remove");
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean containsSQL(Object o) throws SQLException{
                // If the given value is null or not a String
            if (o == null || !(o instanceof String))
                return false;
                // Create the tables view
            createTableView();
                // Prepare a statement to count the instances of the given value 
                // for the given type
            try(PreparedStatement pstmt = prepareStatement(
                    String.format(TABLE_CONTAINS_QUERY_TEMPLATE+" AND %s = ?", 
                            getNameColumn(),
                            getViewName(),
                            getNameColumn(),
                            getTypeColumn()))){
                    // Set the value
                pstmt.setString(1, (String)o);
                    // Set the type
                pstmt.setString(2, type);
                return containsCountResult(pstmt.executeQuery());
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected int sizeSQL() throws SQLException {
            createTableView();  // Create the tables view
                // Prepare a statement to count the items in the database with 
                // the given type
            try(PreparedStatement pstmt = prepareContainsStatement(
                    getViewName(),
                    getNameColumn(),
                    getTypeColumn())){
                    // Set the type
                pstmt.setString(1, type);
                    // Query the database
                ResultSet rs = pstmt.executeQuery();
                    // Get the results of the query
                if (rs.next())
                    return rs.getInt(COUNT_COLUMN_NAME);
            }
            return 0;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected Set<String> valueCacheSet() throws SQLException{
            createTableView();  // Create the tables view
                // Prepare a statement to get the elements in this set from the 
                // database
            try(PreparedStatement pstmt = prepareStatement(
                    String.format("SELECT %s FROM %s WHERE %s = ?",
                            getNameColumn(),
                            getViewName(),
                            getTypeColumn()))){
                    // This is a set used to contain a cache of the contents of 
                    // this set
                Set<String> cache = new LinkedHashSet<>();
                    // Set the type
                pstmt.setString(1, type);
                    // Query the database
                ResultSet rs = pstmt.executeQuery();
                    // While there are results from the query
                while(rs.next())
                    cache.add(rs.getString(getNameColumn()));
                return cache;
            }
        }
    }
    /**
     * 
     */
    private class TableSet extends SchemaViewSet{
        /**
         * 
         * @param type 
         */
        private TableSet(String type) {
            super(type);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getViewName() {
            return TABLES_VIEW_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getNameColumn() {
            return TABLES_NAME_COLUMN_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getTypeColumn() {
            return TABLES_TYPE_COLUMN_NAME;
        }
    }
    /**
     * 
     */
    private class TableStructureMap extends AbstractQueryMap<String, String>{
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean containsKeySQL(Object key) throws SQLException {
                // If the given key is null or not a string
            if (key == null || !(key instanceof String))
                return false;
            createTableView();  // Create the tables view
                // Prepare a statement to check if the given key is a table, 
                // view, or index found in the tables view
            try(PreparedStatement pstmt = prepareContainsStatement(
                    TABLES_VIEW_NAME,
                    TABLES_NAME_COLUMN_NAME)){
                pstmt.setString(1, (String)key);
                return containsCountResult(pstmt.executeQuery());
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean containsValueSQL(Object value) throws SQLException {
                // If the given value is null or not a string
            if (value == null || !(value instanceof String))
                return false;
            createTableView();  // Create the tables view
                // Prepare a statement to check if the given value is the 
                // structure of a table, view, or index found in the tables view
            try(PreparedStatement pstmt = prepareContainsStatement(
                    TABLES_VIEW_NAME,
                    TABLES_STRUCTURE_COLUMN_NAME)){
                pstmt.setString(1, (String)value);
                return containsCountResult(pstmt.executeQuery());
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String removeSQL(Object key) throws SQLException {
            throw new UnsupportedOperationException("remove");
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getSQL(Object key) throws SQLException {
                // If the given key is null or not a string
            if (key == null || !(key instanceof String))
                return null;
            createTableView();  // Create the tables view
                // Prepare a statement to get the structure of the given key
            try(PreparedStatement pstmt = prepareStatement(
                    String.format("SELECT %s FROM %s WHERE %s = ?", 
                            TABLES_STRUCTURE_COLUMN_NAME,
                            TABLES_VIEW_NAME,
                            TABLES_NAME_COLUMN_NAME))){
                    // Set the key of the sturcture to get
                pstmt.setString(1, (String)key);
                    // Query the database for the structure
                ResultSet rs = pstmt.executeQuery();
                    // If the query has a result
                if (rs.next())
                    return rs.getString(TABLES_STRUCTURE_COLUMN_NAME);
            }
            return null;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String putSQL(String key, String value) throws SQLException {
            throw new UnsupportedOperationException("put");
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected Set<Entry<String, String>> entryCacheSet() throws SQLException {
            createTableView();  // Create the tables view
                // A set to get a cache of the entries in this map
            Set<Entry<String, String>> cache = new LinkedHashSet<>();
                // Prepare a statement to go through the names and structures in 
            try(PreparedStatement pstmt = prepareStatement( // this map
                    String.format("SELECT %s, %s FROM %s", 
                            TABLES_NAME_COLUMN_NAME,
                            TABLES_STRUCTURE_COLUMN_NAME,
                            TABLES_VIEW_NAME))){
                    // Query the database for the structure
                ResultSet rs = pstmt.executeQuery();
                    // While there are still rows in the results
                while (rs.next())
                        // Create and add an immutable entry with the name and 
                        // structure of the current row
                    cache.add(new AbstractMap.SimpleImmutableEntry<>(
                            rs.getString(TABLES_NAME_COLUMN_NAME),
                            rs.getString(TABLES_STRUCTURE_COLUMN_NAME)));
            }
            return cache;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected int sizeSQL() throws SQLException {
            createTableView();  // Create the tables view
            return getTableSize(TABLES_VIEW_NAME,TABLES_NAME_COLUMN_NAME);
        }
    }
    /**
     * 
     */
    private class TableIndexSet extends SchemaViewSet{
        /**
         * 
         * @param tableName 
         */
        private TableIndexSet(String tableName) {
            super(tableName);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getViewName() {
            return TABLE_INDEX_VIEW_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getNameColumn() {
            return TABLE_INDEX_NAME_COLUMN_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getTypeColumn() {
            return TABLE_INDEX_TABLE_COLUMN_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean removeSQL(Object o) throws SQLException{
                // If the value to remove is not a string
            if (!(o instanceof String))
                return false;
                // Get the value to remove, as a string
            String name = (String) o;
                // If this does not contain that value
            if (!contains(name))
                return false;
                // Delete the index with the given name
            deleteIndex(name);
            return true;
        }
    }
    /**
     * This is a comparator that compares Strings based off length, with longer 
     * Strings coming before short Strings. This is primarily used by the prefix 
     * map for certain functions.
     */
    private static final Comparator<String> LONG_LENGTH_COMPARATOR = 
            (String o1, String o2) -> {
            // If the length of the strings are the same
        if (o1.length() == o2.length())
            return o1.compareTo(o2);
            // Compare the lengths of the strings, with longer Str
        return Integer.compare(o2.length(), o1.length());
    };
    /**
     * 
     */
    private class PrefixMapImpl extends AbstractDatabaseRowMap<Integer,String> 
            implements PrefixMap {
//        /**
//         * 
//         */
//        private final TreeMap<Integer,String> cache = new TreeMap<>();
        /**
         * {@inheritDoc }
         */
        @Override
        public void syncCache(){
//                // Clear the cache
//            cache.clear();
//                // Create an iterator to go through the entries in this map
//            Iterator<Map.Entry<Integer,String>> itr = super.entryIterator(
//                    true, null, true, true, null, true);
//                // While there are still entries in the iterator
//            while(itr.hasNext()){
//                    // Get the current entry from the iterator
//                Map.Entry<Integer,String> entry = itr.next();
//                cache.put(entry.getKey(), entry.getValue());
//            }
        }
        /**
         * 
         * @param value
         * @throws SQLException 
         */
        protected void checkNewValue(String value) throws SQLException{
                // If this map already contains the given value
            if (containsValue(value))
                throw new IllegalArgumentException("No duplicate prefixes "
                        + "allowed (prefix \""+value+"\" is already mapped to "
                                + "prefixID " + firstKeyFor(value)+")");
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean containsKeySQL(Object key) throws SQLException {
                // If the given key is either null or not an integer
            if (key == null || !(key instanceof Integer))
                return false;
//            if (cache.containsKey((Integer)key))
//                return true;
//            try(PreparedStatement pstmt = prepareContainsStatement(
//                    getTableName(),
//                    getKeyColumn())){
//                setPreparedKeyColumn(pstmt,1,(int)key);
//                return containsCountResult(pstmt.executeQuery());
//            }
            return containsPrefixID((int)key);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean containsValueSQL(Object value) throws SQLException {
                // If the given value is either null or not a string
            if (value == null || !(value instanceof String))
                return false;
//            if (cache.containsValue((String)value))
//                return true;
                // Prepare a statement to check if the prefix table contains the 
                // given value as a prefix
            try(PreparedStatement pstmt = prepareContainsStatement(
                    getTableName(),
                    getKeyColumn(),
                    getValueColumn())){
                setPreparedValue(pstmt,1,(String)value);
                return containsCountResult(pstmt.executeQuery());
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String removeSQL(Object key) throws SQLException {
                // If the given key is either null or not an integer
            if (key == null || !(key instanceof Integer))
                return null;
                // Get the key as an integer
            Integer prefixID = (Integer)key;
                // Get the value currently stored for the given key
            String value = getSQL(prefixID);
                // If nothing is stored for the given key
            if (value == null)
                return null;
                // If the value to remove is empty
            if (value.isEmpty())
                throw new IllegalArgumentException("Cannot remove empty prefix");
//            cache.remove(prefixID);
                // This gets a map containing any prefixes for the prefix being 
                // removed. This is so that we can change the prefixID for the 
                // links that use the prefix that is being removed
            NavigableMap<Integer, String> matches = getPrefixes(value);
                // Get the key for the longest prefix (excluding the one that 
                // will be removed) for the prefix being removed. Due to how the 
                // returned tree is sorted, the prefixID in the map after the 
                // prefix being removed should be the longest prefix not being 
                // removed
            Integer newPrefixID = matches.higherKey(prefixID);
                // If the prefixID for the new prefix is null
            if (newPrefixID == null)
                    // Use the empty prefix
                newPrefixID = getEmptyPrefixID();
                // Prepare a statement to update the links table so that any 
                // links that were using the prefix that is being removed now 
                // use the found new prefix, with the difference being added to 
                // the link's suffix to preserve the links
            try(PreparedStatement pstmt = prepareStatement(
                    String.format("UPDATE %s SET %s = ?, %s = ? || %s WHERE %s = ?", 
                            LINK_TABLE_NAME,
                            PREFIX_ID_COLUMN_NAME, 
                            LINK_URL_COLUMN_NAME,
                            LINK_URL_COLUMN_NAME,
                            PREFIX_ID_COLUMN_NAME))){
                    // Set the prefixID for the replacement prefix
                pstmt.setInt(1, newPrefixID);
                    // Use the difference between the old and new prefixes
                pstmt.setString(2, getSuffix(value,newPrefixID));
                    // Set the prefixID of the prefix being replaced
                pstmt.setInt(3, prefixID);
                    // Update the database
                pstmt.executeUpdate();
            }   // Prepare a statement to remove the prefix from the prefix 
            try (PreparedStatement pstmt = prepareStatement(    // table
                    String.format("DELETE FROM %s WHERE %s = ?", 
                            PREFIX_TABLE_NAME,
                            PREFIX_ID_COLUMN_NAME))) {
                    // Set the prefixID of the prefix to remove
                pstmt.setInt(1, prefixID);
                    // Update the database
                pstmt.executeUpdate();
            }
            return value;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected Integer addSQL(String value) throws SQLException{
                // Check if the value is null
            Objects.requireNonNull(value);
                // Check if the value can be added
            checkNewValue(value);
                // This is the prefixID of the prefix that just was added
            Integer prefixID;
                // Prepare a statement to insert the prefix into the prefix 
            try (PreparedStatement pstmt = prepareStatement(    // table
                    String.format("INSERT INTO %s(%s) VALUES (?)", 
                            PREFIX_TABLE_NAME, 
                            PREFIX_COLUMN_NAME))){
                    // Set the prefix to be added
                pstmt.setString(1, value);
                    // Update the database
                pstmt.executeUpdate();
                    // Get the key that was generated
                prefixID = getGeneratedIntegerKey(pstmt);
            }   // If the prefixID of the added prefix was not found
            if (prefixID == null)
                    // Get the first key for the newly added prefix
                prefixID = firstKeyFor(value);
//            cache.put(prefixID, value);
            return prefixID;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getSQL(Object key) throws SQLException{
                // If the given key is either null or not an integer
            if (key == null || !(key instanceof Integer))
                return null;
                // Get the key as an integer
            Integer prefixID = (Integer)key;
//            if (cache.containsKey(prefixID))
//                return cache.get(prefixID);
                // Prepare a statement to get the value mapped to the given key
            try (PreparedStatement pstmt = prepareStatement(
                    String.format("SELECT %s FROM %s WHERE %s = ?", 
                            PREFIX_COLUMN_NAME,
                            PREFIX_TABLE_NAME,
                            PREFIX_ID_COLUMN_NAME))) {
                    // Set the prefixID of the value to get
                pstmt.setInt(1, prefixID);
                    // Query the database
                ResultSet rs = pstmt.executeQuery();
                    // If there are any results 
                if (rs.next()){
                        // Get the value from the results
                    String value = rs.getString(PREFIX_COLUMN_NAME);
//                    if (value != null)
//                        cache.put(prefixID, value);
//                        putIntoCache(prefixID,value);
                    return value;
                }
            }
            return null;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String putSQL(Integer key, String value) throws SQLException{
                // Check if the key is null
            Objects.requireNonNull(key);
                // Check if the value is null
            Objects.requireNonNull(value);
                // Get the current value stored for the key
            String oldValue = getSQL(key);
                // If the old and new values are the same
            if (value.equals(oldValue))
                return oldValue;
                // Check the new value
            checkNewValue(value);
                // Prepare a statement to either insert or update the value 
                // in the database
            try (PreparedStatement pstmt = prepareStatement(String.format(
                        // If there is no value stored for the given key, insert 
                        // the value into the table. Otherwise, update the value 
                    (oldValue == null) ?    // in the database
                            "INSERT INTO %s(%s, %s) VALUES (?, ?)" : 
                            "UPDATE %s SET %s = ? WHERE %s = ?",
                    PREFIX_TABLE_NAME,
                    PREFIX_COLUMN_NAME,
                    PREFIX_ID_COLUMN_NAME))){
                    // Set the new value
                pstmt.setString(1, value);
                    // Set the key
                pstmt.setInt(2, key);
                    // Update the database
                pstmt.executeUpdate();
            }
//            cache.put(key, value);
            return oldValue;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getTableName() {
            return PREFIX_TABLE_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getKeyColumn() {
            return PREFIX_ID_COLUMN_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getValueColumn() {
            return PREFIX_COLUMN_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected void setPreparedKey(PreparedStatement pstmt, 
                int parameterIndex, Integer key) throws SQLException {
            setParameter(pstmt,parameterIndex,key);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected void setPreparedValue(PreparedStatement pstmt, 
                int parameterIndex, String value) throws SQLException {
            pstmt.setString(parameterIndex, value);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected Integer getKeyFromResults(ResultSet rs) throws SQLException {
                // Get the key from the results
            int key = rs.getInt(PREFIX_ID_COLUMN_NAME);
                // If the key was null, return null. Otherwise, return the key
            return (rs.wasNull()) ? null : key;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getValueFromResults(ResultSet rs) throws SQLException{
            return rs.getString(PREFIX_COLUMN_NAME);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected void removeSQL(boolean fromStart, Integer fromKey, boolean fromInclusive, 
                boolean toEnd, Integer toKey, boolean toInclusive, 
                boolean useValue, String value)throws SQLException{
                // Clear the cache
            clearCache();
            super.removeSQL(fromStart, fromKey, fromInclusive, 
                    toEnd, toKey, toInclusive, useValue, value);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public int getPrefixCount(int prefixID){
                // Prepare a statement to get the amount of links that use the 
                // given prefixID from the prefix count view
            try(PreparedStatement pstmt = prepareStatement(
                    String.format("SELECT %s FROM %s WHERE %s = ?", 
                            PREFIX_COUNT_COLUMN_NAME,
                            PREFIX_COUNT_VIEW_NAME,
                            getKeyColumn()))){
                    // Set the key
                setPreparedKey(pstmt,1,prefixID);
                    // Query the database
                ResultSet rs = pstmt.executeQuery();
                    // If there are any results
                if (rs.next()){
                        // Get the count
                    int count = rs.getInt(PREFIX_COUNT_COLUMN_NAME);
                        // If the count was not null
                    if (!rs.wasNull())
                        return count;
                }
            } catch (SQLException ex) {
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
            throw new IllegalArgumentException("No prefix with prefix ID "+prefixID);
        }
//        /**
//         * {@inheritDoc }
//         */
//        @Override
//        public int getPrefixCount(String value){
//            Objects.requireNonNull(value);
//            try(PreparedStatement pstmt = prepareStatement(
//                    String.format("SELECT %s FROM %s WHERE %s = ?", 
//                            PREFIX_COUNT_COLUMN_NAME,
//                            PREFIX_COUNT_VIEW_NAME,
//                            getValueColumn()))){
//                setPreparedValue(pstmt,1,value);
//                ResultSet rs = pstmt.executeQuery();
//                if (rs.next()){
//                    int count = rs.getInt(PREFIX_COUNT_COLUMN_NAME);
//                    if (!rs.wasNull())
//                        return count;
//                }
//            } catch (SQLException ex) {
//                appendWarning(ex);
//                throw new UncheckedSQLException(ex);
//            }
//            return 0;
//        }
        /**
         * {@inheritDoc }
         */
        @Override
        public NavigableMap<Integer, String> getPrefixes(String value){
                // Check if the value is null
            Objects.requireNonNull(value);
                // Create a copy of this map as it currently is, so that the 
                // prefix length comparator is disconnected from this map
            TreeMap<Integer,String> prefixMap = new TreeMap<>(this);
                // Create a comparator to compare prefix IDs to the length of 
                // the prefix
            Comparator<Integer> prefixLenComp = (Integer o1, Integer o2) -> {
                    // If the two prefixIDs are the same
                if (Objects.equals(o1, o2))
                    return 0;
                    // Get the prefix mapped to the first value
                String str1 = prefixMap.get(o1);
                    // Get the prefix mapped to the second value
                String str2 = prefixMap.get(o2);
                    // Use the length comparator to compare the prefixes
                int c = LONG_LENGTH_COMPARATOR.compare(str1, str2);
                    // If the two prefixes compare the same
                if (c == 0)
                    return Integer.compare(o1, o2);
                return c;
            };  // Create a TreeMap with the prefix length comparator, which 
                // will get the prefixes from this map
            TreeMap<Integer,String> matches = new TreeMap<>(prefixLenComp);
                // Get a prepared statement to find any matching prefixes
            try(PreparedStatement pstmt = prepareStatement(PREFIX_SEARCH_QUERY)){
                    // Set the String to match
                pstmt.setString(1, value);
                    // Get the results of the query
                ResultSet rs = pstmt.executeQuery();
                    // While there are still rows from the query
                while(rs.next()){
                        // Get the prefix from the current row
                    String prefix = rs.getString(PREFIX_COLUMN_NAME);
                        // If the given String actually starts with the prefix 
                        // (the initial search was case insensitive, this check 
                        // makes the search case sensitive)
                    if (value.startsWith(prefix))
                            // Add the prefixID and prefix to the map of matches
                        matches.put(rs.getInt(PREFIX_ID_COLUMN_NAME),prefix);
                }
            } catch(SQLException ex){
                appendWarning(ex);
                throw new UnsupportedOperationException(ex);
            }   // If the map is somehow empty or does not contain the empty prefix
            if (matches.isEmpty() || !matches.containsValue("")){
                    // Add the empty prefix
                matches.put(getEmptyPrefixID(), "");
            }
            return matches;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public Map.Entry<Integer, String> getLongestPrefixEntryFor(String value){
                // Check if the value is null
            Objects.requireNonNull(value);
                // Create a prepared statement to search for any prefixes that 
                // may match the given String
            try(PreparedStatement pstmt = prepareStatement(PREFIX_SEARCH_QUERY)){
                    // Set the String to match
                pstmt.setString(1, value);
                    // Get the results of the query
                ResultSet rs = pstmt.executeQuery();
                    // While there are still rows from the query
                while(rs.next()){
                        // Get the entry from the current row
                    Map.Entry<Integer, String> entry = getEntryFromResults(rs);
                        // If the given String actually starts with the prefix 
                        // from the current row (the initial search was case 
                        // insensitive, this check makes the search case 
                        // sensitive)
                    if (value.startsWith(entry.getValue()))
                        return entry;
                }
            } catch(SQLException ex){
                appendWarning(ex);
                throw new UnsupportedOperationException(ex);
            }   // Return the entry for the empty prefix, since that matches all 
                // Strings
            return getEmptyPrefixEntry();
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public NavigableMap<Integer, String> getSuffixes(String value) {
            return new SuffixMap(getPrefixes(value),value);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean removeUnusedRowsSQL()throws SQLException{
                // Clear the cache
            clearCache();
            return getConnection().removeUnusedRows(
                    PREFIX_TABLE_NAME,
                    LINK_TABLE_NAME,
                    PREFIX_ID_COLUMN_NAME) > 0;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean removeDuplicateRowsSQL()throws SQLException{
            return false;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public DefaultMutableTreeNode createPrefixTree(Collection<String> values){
                // Get a sorted set copy of the collection
            TreeSet<String> sortedValues = new TreeSet<>(values);
                // Create a copy of the values already in this map
            Set<String> prefixes = new HashSet<>(values());
                // Remove any values that are already prefixes in this map
            sortedValues.removeAll(prefixes);
                // Get the prefix tree
            DefaultMutableTreeNode prefixRoot = createPrefixTree(sortedValues,
                    prefixes,getPrefixThreshold(),true);
            // The following code is primarily used to reorder the nodes so that 
            // nodes that allow children will appear before nodes that do not 
            // allow children. This also reorders the existing prefixes so that 
            // they appear in order of their prefixID
                // This will get an inverse of this map
            Map<String,Integer> inverseMap = new LinkedHashMap<>();
                // Go through the entries in this map
            for (Map.Entry<Integer, String> entry : entrySet())
                inverseMap.put(entry.getValue(), entry.getKey());
                // A set to get the nodes containing the existing prefixes, in 
                // order of their prefixIDs
            TreeSet<DefaultMutableTreeNode> prefixNodes = new TreeSet<>(
                    (DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) -> {
                        Integer i1 = inverseMap.getOrDefault(o1.toString(), -1);
                        Integer i2 = inverseMap.getOrDefault(o2.toString(), -1);
                        return Integer.compare(i1, i2);
            }); // Go through the child nodes in the root (these will be the 
                // nodes for existing prefixes
            for (int i = 0; i < prefixRoot.getChildCount(); i++){
                // Get the child at the current index
                TreeNode child = prefixRoot.getChildAt(i);
                    // If the child is a DefaultMutableTreeNode
                if (child instanceof DefaultMutableTreeNode)
                    prefixNodes.add((DefaultMutableTreeNode)child);
            }   // Temporarily remove all the children from the root
            prefixRoot.removeAllChildren();
                // Go through the prefix nodes, now sorted by prefixID
            for (DefaultMutableTreeNode node : prefixNodes)
                prefixRoot.add(node);
                // An iterator to go through the nodes in preorder
            Iterator<TreeNode> itr = prefixRoot.preorderEnumeration().asIterator();
                // A set to contain the nodes to reorganize
            Set<DefaultMutableTreeNode> nodes = new LinkedHashSet<>();
                // While there are nodes to go through
            while (itr.hasNext()){
                    // Get the next node
                TreeNode temp = itr.next();
                    // If the node is a DefaultMutableTreeNode
                if (temp instanceof DefaultMutableTreeNode){
                        // Get the node as a DefaultMutableTreeNode
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)temp;
                        // If the node is not the root and is not a leaf
                    if (!node.isRoot() && !node.isLeaf())
                        nodes.add(node);
                }
            }   // Go through the nodes to be reorganized
            for (DefaultMutableTreeNode node : nodes){
                    // A list to store the nodes that allow children
                List<DefaultMutableTreeNode> folders = new ArrayList<>();
                    // A list to store the nodes that do not allow children
                List<DefaultMutableTreeNode> leaves = new ArrayList<>();
                    // Go through the child nodes in the current node
                for (int i = 0; i < node.getChildCount(); i++){
                        // Get the child at the current index
                    TreeNode child = node.getChildAt(i);
                        // If the child is a DefaultMutableTreeNode
                    if (child instanceof DefaultMutableTreeNode){
                            // If the child allows children
                        if (child.getAllowsChildren())
                            folders.add((DefaultMutableTreeNode)child);
                        else
                            leaves.add((DefaultMutableTreeNode)child);
                    }
                }   // If the node contains only children that allow children 
                    // or only children that do not allow children
                if (leaves.size() == node.getChildCount() || 
                        folders.size() == node.getChildCount())
                        // No need to reorganize this node's children
                    continue;
                    // Temporarily remove all the children from this node
                node.removeAllChildren();
                    // Go through the children that allow children
                for (DefaultMutableTreeNode temp : folders)
                    node.add(temp);
                    // Go through the children that do not allow children
                for (DefaultMutableTreeNode temp : leaves)
                    node.add(temp);
            }
            return prefixRoot;
        }
        /**
         * This returns a copy of this map with all the prefixIDs in the given 
         * collection removed. This is mainly used in the {@link 
         * createPrefixesFrom} method to remove the prefixes that were already 
         * in this map when it was called, so as to get the map containing the 
         * prefixes that were added.
         * @param existingIDs The collection of prefixIDs to remove from the 
         * copy. This will be the collection of prefixIDs that were already in 
         * this map.
         * @return A copy of this map with all the prefixIDs in the given 
         * collection removed.
         * @see createPrefixesFrom
         */
        private TreeMap<Integer,String> getAddedPrefixes(Collection<Integer> existingIDs){
                // Get a copy of this map
            TreeMap<Integer,String> added = new TreeMap<>(this);
                // Remove any prefixID that were already in this map
            added.keySet().removeAll(existingIDs);
            return added;
        }
        @Override
        public NavigableMap<Integer,String> createPrefixesFrom(Collection<String> values) {
                // Make sure the collection is not null
            Objects.requireNonNull(values);
                // Get a copy of the set of prefixIDs that are currently in this 
            Set<Integer> existingIDs = new HashSet<>(keySet()); // map
                // Make sure the initial prefixes are all present and accounted 
            addAllIfAbsent(Arrays.asList(INITIAL_LINK_PREFIXES));   // for
                // If the given collection is empty
            if (values.isEmpty())
                    // Return any prefixes that were added
                return getAddedPrefixes(existingIDs);
                // Get a sorted set copy of the collection
            TreeSet<String> sortedValues = new TreeSet<>(values);
                // Create a copy of the values already in this map
            Set<String> prefixes = new HashSet<>(values());
                // Remove any values that are already prefixes in this map
            sortedValues.removeAll(prefixes);
                // If there are no values remaining (all of them were in this 
            if (sortedValues.isEmpty()) // map as prefixes)
                    // Return any prefixes that were added
                return getAddedPrefixes(existingIDs);
                // Get the threshold for adding a prefix
            int threshold = getPrefixThreshold();
                // This gets a tree to use to get the prefixes that will be 
                // added to this map
            DefaultMutableTreeNode prefixRoot = 
                    createPrefixTree(sortedValues,prefixes,threshold,false);
                // Go through the nodes in the tree in preorder
            Iterator<TreeNode> itr = prefixRoot.preorderEnumeration().asIterator();
                // This maps promising nodes to the amount of links match the 
                // prefix the node represents
            Map<DefaultMutableTreeNode, Integer> prefixNodes = new LinkedHashMap<>();
                // Go through the nodes in the tree
            while (itr.hasNext()){
                    // Get the current node
                TreeNode temp = itr.next();
                    // If the current node is a DefaultMutableTreeNode
                if (temp instanceof DefaultMutableTreeNode){
                        // Get the current node as a DefaultMutableTreeNode
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)temp;
                        // Get how many children the current node has
                    int children = node.getChildCount();
                        // If the current node is at level 2 or greater (skips 
                        // the root and any prefixes already in this map) and 
                        // the current node's child count passes the threshold
                    if (node.getLevel() > 1 && children >= threshold)
                        prefixNodes.put(node, children);
                }
            }   // Remove any nodes that are null, have a null user object, or 
                // have a prefix that is already in this map
            prefixNodes.keySet().removeIf((DefaultMutableTreeNode t) -> 
                    t == null || t.getUserObject() == null || 
                            prefixes.contains(t.toString()));
                // Go through the prefix nodes. This uses a copy of the key set 
                // since the prefix nodes map will be altered by this loop, 
                // which would create issues if we attempted to modify it while 
                // iterating through it. Currently the nodes are mapped to their 
                // child count, which may include nodes that represent other 
                // prefixes. We only want the number of leaves a node has.
            for (DefaultMutableTreeNode node : 
                    new LinkedHashSet<>(prefixNodes.keySet())){
                    // If the current node's parent is a DefaultMutableTreeNode
                if (node.getParent() instanceof DefaultMutableTreeNode){
                        // Get the current node's parent node
                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) 
                            node.getParent();
                        // If the prefix nodes map contains the parent node
                    if (prefixNodes.containsKey(parent))
                            // This node does not count towards its parent's 
                            // child count, as this is a prefix and not a link
                        prefixNodes.put(parent, prefixNodes.get(parent)-1);
                }
            }   // Go through the entries in the promising nodes map
            for (Map.Entry<DefaultMutableTreeNode, Integer> entry : 
                    prefixNodes.entrySet()){
                    // If the node still has enough links to hit the prefix 
                if (entry.getValue() >= threshold)  // threshold
                        // Add the node's prefix to this map
                    addIfAbsent(entry.getKey().toString());
            }   // Return any prefixes that were added
            return getAddedPrefixes(existingIDs);
        }
        /**
         * 
         * @param values The set of values to use to generate any new prefixes. 
         * This set will be altered by this method.
         * @param existingPrefixes A set containing the prefixes already in this 
         * map.
         * @param threshold The threshold for adding a prefix.
         * @param keepAll Whether all of the existing prefixes should appear in 
         * the tree. When false, any existing prefixes that do not match enough 
         * values to pass the threshold will not appear in the tree, along with 
         * the values they match. This is mainly for performance, since these 
         * nodes are not needed when creating the tree used to generate the 
         * prefixes to be added.
         * @return The root of the tree 
         * @see #createMatchMap(TreeSet, int, char[]) 
         * @see #addLeavesToTree(TreeSet, DefaultMutableTreeNode) 
         * @see #createPrefixTree(Map, DefaultMutableTreeNode, int, char[]) 
         * @see #getPrefixSeparators() 
         * @see #getPrefixThreshold() 
         * @see #createPrefixesFrom(Collection) 
         * @see #createPrefixTree(Collection) 
         */
        protected DefaultMutableTreeNode createPrefixTree(TreeSet<String>values, 
                Set<String> existingPrefixes, int threshold, boolean keepAll){
                // This creates a map mapping existing prefixes to the sets of 
                // values matching those prefixes. This map must sort the 
                // prefixes from longest to shortest.
            TreeMap<String, TreeSet<String>> matchMap = new TreeMap<>(
                    LONG_LENGTH_COMPARATOR);
                // Go through the existing prefixes
            for (String prefix : existingPrefixes){
                    // Add a set to contain the values that match the current 
                matchMap.put(prefix, new TreeSet<>());  // prefix
            }   // An iterator to go through the values
            Iterator<String> valueItr = values.iterator();
                // While there are still values to go through
            while (valueItr.hasNext()){
                    // Get the current value
                String value = valueItr.next();
                    // Go through the entries in the match map
                for (Map.Entry<String, TreeSet<String>> entry : matchMap.entrySet()){
                        // If the current value matches the current prefix
                    if (value.startsWith(entry.getKey())){
                            // Add the current value to the prefix's set
                        entry.getValue().add(value);
                            // Remove the current value from the iterator (The 
                            // values that remain after this loop can be assumed 
                            // to match the empty prefix)
                        valueItr.remove();
                        break;
                    }
                }
            }   // Add any remaining values to the set for the empty prefix
            matchMap.get("").addAll(values);
                // If the non-promising sets should be removed
            if (!keepAll)
                    // Remove any set (and prefix) for which the set is smaller 
                    // than the threshold.
                matchMap.values().removeIf((TreeSet<String> t) -> 
                        t.size() < threshold);
                // This is the root of the tree
            DefaultMutableTreeNode root = new DefaultMutableTreeNode();
                // Populate the tree
            createPrefixTree(matchMap,root,threshold,
                    getPrefixSeparators().toCharArray());
            return root;
        }
        /**
         * 
         * @param matchMap A map that maps prefixes to sets containing values 
         * matching those prefixes.
         * @param parentNode The parent node for the nodes that this method 
         * will be creating.
         * @param threshold The threshold for adding a prefix.
         * @param separators The array of separator characters to use to get 
         * prefixes.
         * @see #createMatchMap(TreeSet, int, char[]) 
         * @see #addLeavesToTree(TreeSet, DefaultMutableTreeNode) 
         * @see #createPrefixTree(TreeSet, Set, int, boolean) 
         * @see #getPrefixSeparators() 
         * @see #getPrefixThreshold() 
         * @see #createPrefixesFrom(Collection) 
         * @see #createPrefixTree(Collection) 
         */
        private void createPrefixTree(Map<String, TreeSet<String>> matchMap, 
                DefaultMutableTreeNode parentNode, int threshold, char[] separators){
                // Go through the entries in the match map
            for (Map.Entry<String, TreeSet<String>> entry : matchMap.entrySet()){
                    // Get the entry's set of matches
                TreeSet<String> values = entry.getValue();
                    // If the entry's set does not contain enough elements to 
                    // hit the threshold and the parent node is not the root
                if (values.size() < threshold && !parentNode.isRoot()){
                        // Add the values to the parent node as leaves
                    addLeavesToTree(values,parentNode);
                } else {    // Get the prefix for the current entry
                    String prefix = entry.getKey();
                        // Create the node for the prefix
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode();
                        // Add the node to the parent node
                    parentNode.add(node);
                        // This gets the match map for the entry's set
                    TreeMap<String, TreeSet<String>> map;
                    do{     // Create the entry's set's match map
                        map = createMatchMap(values,prefix.length(),separators);
                            // Remove the set with no shared prefixes (if there 
                            // is one, since these are all leaves anyway
                        TreeSet<String> leaves = map.remove("");
                            // If there are any leaves
                        if (leaves != null)
                                // Add them to the node for the prefix
                            addLeavesToTree(leaves,node);
                            // If there aren't any leaves, the map contains 
                            // only one set, the parent node is not the root, 
                            // and the only set in the map is the same as the 
                            // entry's set
                        else if (map.size() == 1 && !parentNode.isRoot() && 
                                values.equals(map.firstEntry().getValue())){
                            // If this is the case, then all the values in the 
                            // set share a longer prefix than what was 
                            // originally found. As such, we need to create the 
                            // match map again, but this time with a longer 
                            // prefix
                                // Use the first (and only) key in the map for 
                                // the prefix this time
                            prefix = map.firstKey();
                                // Set the map to null to indicate it needs to 
                                // be remade with the new longer prefix
                            map = null;
                        }
                    }   // While this prefix's match map is null (i.e. there is 
                        // a longer prefix that can be used)
                    while (map == null);
                        // Set the node's object to the prefix
                    node.setUserObject(prefix);
                        // If the map is not empty
                    if (!map.isEmpty())
                            // Create the branch for this prefix
                        createPrefixTree(map,node,threshold,separators);
                }
            }
        }
        /**
         * This searches for any of the given separator characters in the given 
         * String, starting at the given index, and returns the first index this 
         * finds. 
         * @param str The String to search through.
         * @param fromIndex The index to start at.
         * @param separators The array of separator characters to search for.
         * @return The first index of a separator character in the given String, 
         * or -1 if none are found.
         * @see createMatchMap
         * @see createPrefixTree(Map, DefaultMutableTreeNode, int, char[]) 
         * @see #getPrefixSeparators() 
         * @see #createPrefixesFrom(Collection) 
         * @see #createPrefixTree(Collection) 
         */
        private int indexOfSeparator(String str, int fromIndex, char[] separators){
                // If the given String is empty
            if (str.isEmpty())
                return -1;
                // This is a sorted set to get the indexes for the separator 
                // characters in the String. This is a set in order to remove 
                // duplicates, and is sorted in order to get the smallest index 
            TreeSet<Integer> indexes = new TreeSet<>();     // found
                // Go through the separator characters
            for (char c : separators)
                    // Search for the first instance of the current separator
                indexes.add(str.indexOf(c, fromIndex));
                // Remove -1, as that indicates a character was not found
            indexes.remove(-1);
                // While the set of indexes is not empty (at least one character 
                // is found) (this is mainly to prevent unboxing any nulls)
            while (!indexes.isEmpty()){
                    // Poll the first index in the set
                Integer index = indexes.pollFirst();
                    // If the index is not null
                if (index != null)
                    return index;
            }   // No character was found
            return -1;
        }
        /**
         * This returns a map which maps any prefixes found within the given set 
         * of values to a set containing the values matching those prefixes. In 
         * other words, this method finds any prefixes in the given set of 
         * values, splits the set up, and maps the separated sets to the 
         * prefixes each value in a set has in common. Any value for which no 
         * matching prefix was found for will be stored in a set mapped to an 
         * empty String. Prefixes are found by looking for the first instance of 
         * a separator character in a String, starting at the given index.
         * @param values The values to search for prefixes in and to split up 
         * into subsets based off the prefixes they match.
         * @param fromIndex The index to start searching for separator 
         * characters in the Strings at.
         * @param separators The array of separator characters to use to get 
         * prefixes.
         * @return A map mapping prefixes found to the values that they match.
         * @see indexOfSeparator
         * @see createPrefixTree(Map, DefaultMutableTreeNode, int, char[]) 
         * @see #getPrefixSeparators() 
         * @see #createPrefixesFrom(Collection) 
         * @see #createPrefixTree(Collection) 
         */
        private TreeMap<String, TreeSet<String>> createMatchMap(
                TreeSet<String> values, int fromIndex, char[] separators){
                // Create a map to store the prefixes and the values they match
            TreeMap<String, TreeSet<String>> map = new TreeMap<>(LONG_LENGTH_COMPARATOR);
                // For all the values in the given set
            for (String temp : values){
                    // Find the index of the first separator character
                int sepIndex = indexOfSeparator(temp, fromIndex, separators);
                    // If there is a separator, get the prefix for the value. 
                    // Otherwise, the value has no prefix
                String prefix = (sepIndex < 0)?"":temp.substring(0,sepIndex+1);
                    // If the map does not already contain the prefix
                if (!map.containsKey(prefix))
                        // Create a new TreeSet to contain the matches for the 
                    map.put(prefix, new TreeSet<>());   // prefix
                    // Add the value to its matching prefix
                map.get(prefix).add(temp);
            }
            return map;
        }
        /**
         * This adds the values in the given set to the given node as leaves 
         * that do not allow children.
         * @param leaves The set containing the values for the leaves.
         * @param node The node to add the leaves to.
         * @see #createPrefixTree(Map, DefaultMutableTreeNode, int, char[]) 
         * @see #createPrefixesFrom(Collection) 
         * @see #createPrefixTree(Collection) 
         */
        private void addLeavesToTree(TreeSet<String> leaves, 
                DefaultMutableTreeNode node){
                // Go through the set of leaves
            for (String temp : leaves){
                    // Add the leaf to the node
                node.add(new DefaultMutableTreeNode(temp,false));
            }
        }
        /**
         * 
         */
        private class SuffixMap extends AbstractMap<Integer,String> implements 
                NavigableMap<Integer, String>{
            /**
             * This is the map containing the prefixes for {@code value}.
             */
            protected final NavigableMap<Integer,String> prefixes;
            /**
             * This is the full String. For any given key, {@code key}, the 
             * expression {@code prefixes.get(key) + get(key)} should result in 
             * this String.
             */
            protected final String value;
            /**
             * This is the set containing the entries for this map. This is 
             * initially null and is initialized the first time it is requested.
             */
            private Set<Entry<Integer, String>> entries = null;
            /**
             * 
             * @param prefix
             * @return 
             */
            private String removePrefix(String prefix){
                    // If the prefix is null or empty
                if (prefix == null || prefix.isEmpty())
                    return value;
                else
                    return value.substring(prefix.length());
            }
            /**
             * 
             * @param entry
             * @return 
             */
            private Entry<Integer, String> getSuffixEntry(Entry<Integer,String> entry){
                    // If the given entry is null
                if (entry == null)
                    return null;
                return new AbstractMap.SimpleImmutableEntry<>(entry.getKey(),
                        removePrefix(entry.getValue()));
            }
            /**
             * 
             * @param prefixes
             * @param value 
             */
            SuffixMap(NavigableMap<Integer,String> prefixes, String value){
                this.prefixes = Objects.requireNonNull(prefixes);
                this.value = Objects.requireNonNull(value);
            }
            @Override
            public boolean containsKey(Object key){
                    // If the key is an integer
                if (key instanceof Integer)
                    return prefixes.containsKey(key);
                    // If the key is null
                else if (key == null)
                    return prefixes.containsKey(null);
                else
                    return false;
            }
            @Override
            public String get(Object key){
                    // If the key is an integer or null
                if ((key instanceof Integer) || key == null){
                        // If the parent map contains the given key
                    if (prefixes.containsKey((Integer)key))
                            // Get the suffix of the value after the prefix 
                            // is removed from it
                        return removePrefix(prefixes.get((Integer)key));
                }
                return null;
            }
            @Override
            public String remove(Object key){
                    // If the key is an integer or null
                if ((key instanceof Integer) || key == null){
                        // If the parent map contains the given key
                    if (prefixes.containsKey((Integer)key))
                            // Remove the prefix from the parent map and return 
                            // the suffix
                        return removePrefix(prefixes.remove((Integer)key));
                }
                return null;
            }
            @Override
            public Set<Entry<Integer, String>> entrySet() {
                    // If the entries set has not been initialized yet
                if (entries == null){
                    entries = new AbstractSet<>(){
                        @Override
                        public Iterator<Entry<Integer, String>> iterator() {
                            return new Iterator<>(){
                                    // An iterator to go through the parent 
                                    // map's entries
                                Iterator<Entry<Integer, String>> itr = 
                                        prefixes.entrySet().iterator();
                                @Override
                                public boolean hasNext() {
                                    return itr.hasNext();
                                }
                                @Override
                                public Entry<Integer, String> next() {
                                    return getSuffixEntry(itr.next());
                                }
                                @Override
                                public void remove() {
                                    itr.remove();
                                }
                            };
                        }
                        @Override
                        public int size() {
                            return prefixes.size();
                        }
                        @Override
                        public void clear(){
                            prefixes.clear();
                        }
                    };
                }
                return entries;
            }
            @Override
            public Entry<Integer, String> lowerEntry(Integer key) {
                return getSuffixEntry(prefixes.lowerEntry(key));
            }
            @Override
            public Integer lowerKey(Integer key) {
                return prefixes.lowerKey(key);
            }
            @Override
            public Entry<Integer, String> floorEntry(Integer key) {
                return getSuffixEntry(prefixes.floorEntry(key));
            }
            @Override
            public Integer floorKey(Integer key) {
                return prefixes.floorKey(key);
            }
            @Override
            public Entry<Integer, String> ceilingEntry(Integer key) {
                return getSuffixEntry(prefixes.ceilingEntry(key));
            }
            @Override
            public Integer ceilingKey(Integer key) {
                return prefixes.ceilingKey(key);
            }
            @Override
            public Entry<Integer, String> higherEntry(Integer key) {
                return getSuffixEntry(prefixes.lowerEntry(key));
            }
            @Override
            public Integer higherKey(Integer key) {
                return prefixes.higherKey(key);
            }
            @Override
            public Entry<Integer, String> firstEntry() {
                return getSuffixEntry(prefixes.firstEntry());
            }
            @Override
            public Entry<Integer, String> lastEntry() {
                return getSuffixEntry(prefixes.lastEntry());
            }
            @Override
            public Entry<Integer, String> pollFirstEntry() {
                return getSuffixEntry(prefixes.pollFirstEntry());
            }
            @Override
            public Entry<Integer, String> pollLastEntry() {
                return getSuffixEntry(prefixes.pollLastEntry());
            }
            @Override
            public NavigableMap<Integer, String> descendingMap() {
                return new SuffixMap(prefixes.descendingMap(),value);
            }
            @Override
            public NavigableSet<Integer> navigableKeySet() {
                return prefixes.navigableKeySet();
            }
            @Override
            public NavigableSet<Integer> descendingKeySet() {
                return prefixes.descendingKeySet();
            }
            @Override
            public NavigableMap<Integer, String> subMap(Integer fromKey, 
                    boolean fromInclusive, Integer toKey, boolean toInclusive) {
                return new SuffixMap(prefixes.subMap(fromKey, fromInclusive, 
                        toKey, toInclusive),value);
            }
            @Override
            public NavigableMap<Integer, String> headMap(Integer toKey, boolean inclusive) {
                return new SuffixMap(prefixes.headMap(toKey,inclusive),value);
            }
            @Override
            public NavigableMap<Integer, String> tailMap(Integer fromKey, boolean inclusive) {
                return new SuffixMap(prefixes.tailMap(fromKey,inclusive),value);
            }
            @Override
            public SortedMap<Integer, String> subMap(Integer fromKey, Integer toKey) {
                return subMap(fromKey,true,toKey,false);
            }
            @Override
            public SortedMap<Integer, String> headMap(Integer toKey) {
                return headMap(toKey,false);
            }
            @Override
            public SortedMap<Integer, String> tailMap(Integer fromKey) {
                return tailMap(fromKey,true);
            }
            /**
             * {@inheritDoc }
             */
            @Override
            public Comparator<? super Integer> comparator() {
                return prefixes.comparator();
            }
            @Override
            public Integer firstKey() {
                return prefixes.firstKey();
            }
            @Override
            public Integer lastKey() {
                return prefixes.lastKey();
            }
        }
    }
    /**
     * 
     */
    private class LinkMapImpl extends AbstractDatabaseRowMap<Long,String> 
            implements LinkMap {
        /**
         * 
         */
        private InverseMap inverseMap = null;
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean containsKeySQL(Object key) throws SQLException {
                // If the given key is null or not a long
            if (key == null || !(key instanceof Long))
                return false;
            return containsLinkID((long)key);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean containsValueSQL(Object value) throws SQLException {
                // If the given value is null or not a String
            if (value == null || !(value instanceof String))
                return false;
                // Prepare a statement to check for the presense of the given 
            try(PreparedStatement pstmt = prepareContainsStatement( // value
                    getDataViewName(),
                    getKeyColumn(),
                    getValueColumn())){
                setPreparedValue(pstmt,1,(String)value);
                return containsCountResult(pstmt.executeQuery());
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String removeSQL(Object key) throws SQLException {
                // If the given key is null or not a long
            if (key == null || !(key instanceof Long))
                return null;
                // Get the key as a long
            Long linkID = (Long)key;
                // Get the old value
            String value = getSQL(linkID);
            // Remove value from the list data table and shift the indexes accordingly?
                // Prepare a statement to remove the entry from the links table
            try (PreparedStatement pstmt = prepareStatement(
                    String.format("DELETE FROM %s WHERE %s = ?", 
                            LINK_TABLE_NAME,
                            LINK_ID_COLUMN_NAME))) {
                    // Set the key to remove
                pstmt.setLong(1, linkID);
                    // Update the database
                pstmt.executeUpdate();
            }
            return value;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected Long addSQL(String value) throws SQLException {
                // Check if the link is null
            Objects.requireNonNull(value);
                // Get the entry for the longest prefix that matches the link
            Map.Entry<Integer, String> prefixEntry = 
                    getPrefixMap().getLongestPrefixEntryFor(value);
                // This gets a set of linkIDs currently in this map for the 
                // given value
            Set<Long> existingIDs = new TreeSet<>(keySetFor(value));
                // This is the listID of the link that just was added
            Long linkID;
                // Remove the prefix from the link
            value = value.substring(prefixEntry.getValue().length());
                // Prepare a statement to insert the link into the link table
            try (PreparedStatement pstmt = prepareStatement(String.format(
                    "INSERT INTO %s(%s, %s) VALUES (?, ?)", 
                            LINK_TABLE_NAME,
                            PREFIX_ID_COLUMN_NAME,
                            LINK_URL_COLUMN_NAME))) {
                    // Set the prefixID for the link's prefix
                pstmt.setInt(1, prefixEntry.getKey());
                    // Set the link's suffix
                pstmt.setString(2, value);
                    // Update the database
                pstmt.executeUpdate();
                    // Get the key that was generated
                linkID = getGeneratedLongKey(pstmt);
            }   // If the linkID of the added link was found, return it. 
                // Otherwise, find it and return the linkID for the added link
            return (linkID != null) ? linkID:getGeneratedKey(value,existingIDs);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getSQL(Object key) throws SQLException {
                // If the given key is null or not a long
            if (key == null || !(key instanceof Long))
                return null;
                // Prepare a statement to request the link with the given key
            try (PreparedStatement pstmt = prepareStatement(String.format(
                    "SELECT %s FROM %s WHERE %s = ?", 
                            LINK_URL_COLUMN_NAME,
                            FULL_LINK_VIEW_NAME,
                            LINK_ID_COLUMN_NAME))) {
                    // Set the key
                pstmt.setLong(1, (Long)key);
                    // Query the database
                ResultSet rs = pstmt.executeQuery();
                    // If the query had any results
                if (rs.next())
                    return rs.getString(LINK_URL_COLUMN_NAME);
            }
            return null;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String putSQL(Long key, String value) throws SQLException {
                // Check if the key is null
            Objects.requireNonNull(key);
                // Check if the value is null
            Objects.requireNonNull(value);
                // Get the old value for the key
            String oldValue = getSQL(key);
                // If the new and old values are the same (there would be no 
            if (Objects.equals(oldValue, value))    // change)
                return oldValue;
                // Get the prefixID for the longest matching prefix for the value
            int prefixID = getPrefixMap().getLongestPrefixIDFor(value);
                // Get the suffix for the value
            value = getPrefixMap().getSuffix(value, prefixID);
                // If there was no value set for the given key
            if (oldValue == null){
                    // Prepare a statement to insert the link into the database
                try (PreparedStatement pstmt = prepareStatement(String.format(
                        "INSERT INTO %s(%s, %s, %s) VALUES (?, ?, ?)", 
                                LINK_TABLE_NAME,
                                LINK_ID_COLUMN_NAME,
                                PREFIX_ID_COLUMN_NAME,
                                LINK_URL_COLUMN_NAME))){
                        // Set the linkID for the link
                    pstmt.setLong(1, key);
                        // Set the prefixID for the prefix for the link
                    pstmt.setInt(2, prefixID);
                        // Set the suffix for the link
                    pstmt.setString(3, value);
                        // Update the database
                    pstmt.executeUpdate();
                }
            } else // Update the link for the given key
                updateLink(key,prefixID,value);
            return oldValue;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getTableName() {
            return LINK_TABLE_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getDataViewName(){
            return FULL_LINK_VIEW_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getKeyColumn() {
            return LINK_ID_COLUMN_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getValueColumn() {
            return LINK_URL_COLUMN_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected void setPreparedKey(PreparedStatement pstmt, 
                int parameterIndex, Long key) throws SQLException {
            setParameter(pstmt,parameterIndex,key);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected void setPreparedValue(PreparedStatement pstmt, 
                int parameterIndex, String value) throws SQLException {
            pstmt.setString(parameterIndex, value);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected Long getKeyFromResults(ResultSet rs) throws SQLException {
                // Get the key from the results
            long key = rs.getLong(LINK_ID_COLUMN_NAME);
                // If the key was null, then return null. Otherwise, return the 
            return (rs.wasNull()) ? null : key;     // key
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getValueFromResults(ResultSet rs) throws SQLException {
            return rs.getString(LINK_URL_COLUMN_NAME);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getRemoveRangeConditions(
                boolean fromStart, boolean fromInclusive, 
                boolean toEnd, boolean toInclusive, 
                boolean useValue, String value){
                // If this is not using the given value
            if (!useValue)
                return getRangeConditions(fromStart,fromInclusive,toEnd,toInclusive);
                // The conditions will need to check the full links view since 
                // the links table stores prefixIDs and suffixes. As such, the 
                // values in the links column would not match the value otherwise
            return String.format("%s IN (SELECT %s FROM %s WHERE %s)", 
                    LINK_ID_COLUMN_NAME,
                    LINK_ID_COLUMN_NAME,
                    FULL_LINK_VIEW_NAME,
                        // Get the conditions for the full links view
                    super.getRemoveRangeConditions(fromStart, fromInclusive, 
                            toEnd, toInclusive,useValue,value));
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected void removeSQL(boolean fromStart, Long fromKey, boolean fromInclusive, 
                boolean toEnd, Long toKey, boolean toInclusive, 
                boolean useValue, String value)throws SQLException{
            super.removeSQL(fromStart, fromKey, fromInclusive, 
                    toEnd, toKey, toInclusive, useValue, value);
            // Update the list data table accordingly?
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean removeDuplicateRowsSQL() throws SQLException{
                // This is a map to contain the first linkID and link of all the 
                // links with two or more entries in this map
            Map<Long, String> duplicateLinks = new TreeMap<>();
                // Get a prepared statement to search through the distinct link 
                // view for any links that occur more than once
            try(PreparedStatement pstmt = prepareStatement(
                    String.format("SELECT %s, %s FROM %s WHERE %s > 1",
                            LINK_ID_COLUMN_NAME,
                            LINK_URL_COLUMN_NAME,
                            DISTINCT_LINK_VIEW_NAME,
                            LINK_COUNT_COLUMN_NAME))){
                    // Go through the results of the query
                ResultSet rs = pstmt.executeQuery();
                    // While there are still rows in the query
                while (rs.next()){
                        // Add the linkID and link from the current row
                    duplicateLinks.put(getKeyFromResults(rs), 
                            getValueFromResults(rs));
                }
            }   // If there are no duplicate links
            if (duplicateLinks.isEmpty())
                return false;
                // Get the current state of the auto-commit
            boolean autoCommit = getAutoCommit();
                // Turn off the auto-commit in order to group the following 
                // database transactions to improve performance
            setAutoCommit(false);
                // Go through the duplicated links
            for (Map.Entry<Long, String> entry : duplicateLinks.entrySet()){
                    // Prepare a statement to set every instance of a 
                    // duplicate's linkID in the list data table to the linkID 
                    // that will be kept
                try(PreparedStatement pstmt = prepareStatement(String.format(
                        "UPDATE %s SET %s = ? WHERE %s", 
                            LIST_DATA_TABLE_NAME,
                            LINK_ID_COLUMN_NAME,
                            DUPLICATE_LINK_ID_QUERY_CONDITIONS))){
                        // Set the linkID that will be kept
                    pstmt.setLong(1, entry.getKey());
                        // Set the link to find the duplicates of
                    pstmt.setString(2, entry.getValue());
                        // Set the linkID that will not be changed (since it is 
                        // the one that will be kept)
                    pstmt.setLong(3, entry.getKey());
                        // Update the database
                    pstmt.executeUpdate();
                }   // A prepared statement to delete all but the kept instance 
                    // of the link from the link table
                try(PreparedStatement pstmt = prepareStatement(String.format(
                        "DELETE FROM %s WHERE %s", 
                            LINK_TABLE_NAME,
                            DUPLICATE_LINK_ID_QUERY_CONDITIONS))){
                        // Set the link to find and remove the duplicates of
                    pstmt.setString(1, entry.getValue());
                        // Set the linkID that will be kept
                    pstmt.setLong(2, entry.getKey());
                        // Update the database
                    pstmt.executeUpdate();
                }
            }
            commit();       // Commit the changes to the database
                // Restore the auto-commit back to what it was set to before
            setAutoCommit(autoCommit);
            return true;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean removeUnusedRowsSQL() throws SQLException{
            return getConnection().removeUnusedRows(
                    LINK_TABLE_NAME,
                    LIST_DATA_TABLE_NAME,
                    LINK_ID_COLUMN_NAME) > 0;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected Long firstKeyForSQL(String value) throws SQLException{
            checkValue(value);  // Check the value
                // Prepare a statement to find the linkID of the given value in 
                // the distinct links view
            try(PreparedStatement pstmt = prepareStatement(
                    String.format("SELECT %s FROM %s WHERE %s = ?", 
                            LINK_ID_COLUMN_NAME,
                            DISTINCT_LINK_VIEW_NAME,
                            LINK_URL_COLUMN_NAME))){
                    // Set the value to search for
                setPreparedValue(pstmt,1,value);
                    // Query the database
                ResultSet rs = pstmt.executeQuery();
                    // If the query had any results
                if (rs.next())
                    return getKeyFromResults(rs);
            }
            return null;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public Map<String, Long> inverse() {
                // If the inverse map has not been initialized yet
            if (inverseMap == null)
                inverseMap = new InverseMap();
            return inverseMap;
        }
        /**
         * This returns a {@code NavigableMap} containing all the links in the 
         * database that matches the given pattern. This is a helper function 
         * for the {@code getStartsWith}, {@code getEndsWith}, and {@code 
         * getContains} methods.
         * @param pattern The pattern to use to filter the links.
         * @return A {@code NavigableMap} with all the links matching the given 
         * pattern.
         * @throws UncheckedSQLException If a database error occurs.
         * @throws NullPointerException If the pattern is null.
         * @see #getStartsWith(java.lang.String, int) 
         * @see #getStartsWith(java.lang.String) 
         * @see #getEndsWith(java.lang.String) 
         * @see #getContains(java.lang.String) 
         */
        private NavigableMap<Long, String> getSearchMap(String pattern){
                // Make sure the pattern is not null
            Objects.requireNonNull(pattern);
                // A TreeMap to get the results of the search
            TreeMap<Long, String> map = new TreeMap<>();
                // Prepare a statement to search for links matching the pattern
            try(PreparedStatement pstmt = prepareStatement(String.format(
                    "SELECT %s, %s FROM %s WHERE "+TEXT_SEARCH_TEMPLATE, 
                        LINK_ID_COLUMN_NAME,
                        LINK_URL_COLUMN_NAME,
                        FULL_LINK_VIEW_NAME,
                        LINK_URL_COLUMN_NAME,
                        "?"))){
                    // Set the pattern to use
                pstmt.setString(1, pattern);
                    // Get the results of the query
                ResultSet rs = pstmt.executeQuery();
                    // While there are still rows from the results
                while(rs.next())
                    map.put(getKeyFromResults(rs), getValueFromResults(rs));
            } catch (SQLException ex) {
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
            return map;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public NavigableMap<Long,String>getStartsWith(String prefix,int offset){
                // Make sure the prefix is not null
            Objects.requireNonNull(prefix);
                // If the offset is negative
            if (offset < 0)
                return new TreeMap<>();
                // Get a map containing the results of the database search for 
                // all the links that match the given prefix (offset by the 
                // given offset)
            NavigableMap<Long, String> map = getSearchMap("_".repeat(offset)+
                    formatSearchQueryPattern(prefix)+"%");
                // Remove any values which don't start with the given prefix 
                // (the search was case insensitive, this makes it case 
            map.values().removeIf((String t) -> {   // sensitive)
                return !t.startsWith(prefix, offset);
            });
            return map;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public NavigableMap<Long, String> getEndsWith(String suffix){
                // Get a map containing the results of the database search for 
                // all the links that match the given suffix
            NavigableMap<Long, String> map = getSearchMap("%"+
                    formatSearchQueryPattern(suffix));
                // Remove any values which don't start with the given prefix 
                // (the search was case insensitive, this makes it case 
            map.values().removeIf((String t) -> {   // sensitive)
                return !t.endsWith(suffix);
            });
            return map;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public NavigableMap<Long, String> getContains(String s){
                // Get a map containing the results of the database search for 
                // all the links that contain the given string
            NavigableMap<Long, String> map = getSearchMap("%"+
                    formatSearchQueryPattern(s)+"%");
                // Remove any values which don't start with the given prefix 
                // (the search was case insensitive, this makes it case 
            map.values().removeIf((String t) -> {   // sensitive)
                return !t.contains(s);
            });
            return map;
        }
        /**
         * This is the inverse map for this {@code LinkMap}. This uses the 
         * values of the parent map as the keys for this map and vice versa.
         */
        private class InverseMap extends AbstractMap<String,Long>{
            /**
             * This is the set containing the entries for this map. This is 
             * initially null and is initialized the first time it is requested.
             */
            private Set<Entry<String,Long>> entries = null;
            /**
             * This returns the parent {@code LinkMap} for this {@code 
             * InverseMap}.
             * @return The parent {@code LinkMap}.
             */
            protected LinkMap getParentMap(){
                return LinkMapImpl.this;
            }
            /**
             * {@inheritDoc }
             */
            @Override
            public boolean containsKey(Object key){
                    // If the given key is not a String
                if (!(key instanceof String))
                    return false;
                    // Return whether the parent map contains the key as a value
                return getParentMap().containsValue((String)key);
            }
            /**
             * {@inheritDoc }
             */
            @Override
            public Long get(Object key){
                    // If the given key is a String or null
                if ((key instanceof String) || key == null){
                        // Get the first key in the parent map for the given 
                    return getParentMap().firstKeyFor((String)key); // key
                }
                return null;
            }
            /**
             * {@inheritDoc }
             */
            @Override
            public Set<Entry<String, Long>> entrySet() {
                    // If the entry set has not been initialized yet
                if (entries == null)
                        // Create a new set for the entry set
                    entries = new AbstractSet<>(){
                        @Override
                        public Iterator<Entry<String, Long>> iterator() {
                                // Prepare a statement to read the linkIDs and 
                                // links from the distinct links view
                            try(PreparedStatement pstmt = prepareStatement(
                                    String.format("SELECT %s, %s FROM %s",
                                            LINK_ID_COLUMN_NAME,
                                            LINK_URL_COLUMN_NAME,
                                            DISTINCT_LINK_VIEW_NAME))){
                                   // Create a linked hash set to act as a cache
                                LinkedHashSet<Entry<String, Long>> cache = 
                                        new LinkedHashSet<>();
                                   // Get the results of the query
                                ResultSet rs = pstmt.executeQuery();
                                    // While there are still rows in the results
                                while(rs.next())
                                        // Add the current row to the cache
                                    cache.add(new AbstractMap.SimpleImmutableEntry<>(
                                            rs.getString(LINK_URL_COLUMN_NAME),
                                            rs.getLong(LINK_ID_COLUMN_NAME)
                                    ));
                                    // Return an iterator to go over the cache
                                return new CacheSetIterator<>(cache){
                                    @Override
                                    protected void remove(Entry<String, Long> value) {
                                        removeInverse(value.getKey(),value.getValue());
                                    }
                                };
                            } catch (SQLException ex) {
                                appendWarning(ex);
                                return Collections.emptyIterator();
                            }
                        }
                        @Override
                        public int size() {
                            try {   //Return the size of the distinct links view
                                return getTableSize(DISTINCT_LINK_VIEW_NAME, 
                                    LINK_ID_COLUMN_NAME);
                            } catch (SQLException ex) {
                                appendWarning(ex);
                            }
                            return 0;
                        }
                    };
                return entries;
            }
        }
        /**
         * 
         * @param key
         * @param value
         * @return 
         * @throws SQLException 
         */
        protected int removeInverseSQL(String key, Long value)throws SQLException {
            // Remove values from the list data table and shift the indexes accordingly?
//            try (PreparedStatement pstmt = prepareStatement(
//                    String.format("DELETE FROM %s WHERE %s = ?", 
//                            LINK_TABLE_NAME,
//                            LINK_URL_COLUMN_NAME))) {
//                pstmt.setString(1, key);
//                pstmt.executeUpdate();
//                return pstmt.getUpdateCount();
//            }
            throw new UnsupportedOperationException("remove");
        }
        /**
         * 
         * @param key
         * @param value 
         * @return 
         */
        protected int removeInverse(String key, Long value){
            try {
                return removeInverseSQL(key, value);
            } catch (SQLException ex) {
                ConnectionBased.throwConstraintException(ex);
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
        }
    }
    /**
     * 
     */
    private class ListNameMapImpl extends AbstractDatabaseRowMap<Integer, String> 
            implements ListNameMap{
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean containsKeySQL(Object key) throws SQLException {
                // If the given key is null or not an integer
            if (key == null || !(key instanceof Integer))
                return false;
            return containsListID((int)key);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean containsValueSQL(Object value) throws SQLException {
                // If the given value is null or not a String
            if (value != null && !(value instanceof String))
                return false;
                // Get the value as a string
            String name = (String) value;
                // Prepare a statement to check if the list table contains a 
                // list with the given name
            try(PreparedStatement pstmt = prepareStatement(
                    String.format(TABLE_SIZE_QUERY_TEMPLATE+" WHERE %s", 
                            getKeyColumn(),
                            getDataViewName(),
                            getValueCondition(name)))){
                    // If the given value is not null
                if (name != null)
                    setPreparedValue(pstmt,1,name);
                return containsCountResult(pstmt.executeQuery());
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected void checkValue(String value){
                // Check the value to see if it is value
            super.checkValue(value);
                // Check if the value contains an asterisk
            if (value.contains("*"))
                throw new IllegalArgumentException("Name cannot contain asterisks (*) (Name: \""+value+"\")");
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String removeSQL(Object key) throws SQLException {
                // If the given key is null or not an integer
            if (key == null || !(key instanceof Integer))
                return null;
                // Get the given key as an integer
            int listID = (Integer)key;
                // Get the value stored for the given key
            String value = getSQL(listID);
                // Remove the list with the given key
            removeList(listID);
            return value;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected Integer addSQL(String value) throws SQLException {
                // This gets a set of listIDs currently in this map
            Set<Integer> existingIDs = new TreeSet<>(keySet());
                // This is the listID of the list that just was added
            Integer listID;
                // Prepare a statement to insert the list name into the list 
                // table, along with the list's last modified and created times
            try(PreparedStatement pstmt = prepareStatement(String.format(
                    "INSERT INTO %s(%s, %s, %s) VALUES (?, ?, ?)", 
                            LIST_TABLE_NAME,
                            LIST_NAME_COLUMN_NAME,
                            LIST_LAST_MODIFIED_COLUMN_NAME,
                            LIST_CREATED_COLUMN_NAME))){
                    // Get the current time
                long time = System.currentTimeMillis();
                    // Set the list's name
                pstmt.setString(1, value);
                    // Set the list's last modified time to the current time
                pstmt.setLong(2, time);
                    // Set the list's created time to the current time
                pstmt.setLong(3, time);
                    // Update the database
                pstmt.executeUpdate();
                    // Get any keys that were generated
                listID = getGeneratedIntegerKey(pstmt);
            }   // If the listID of the created list was found, return it. 
                // Otherwise, find it and return the listID for the list name
            return (listID != null) ? listID:getGeneratedKey(value,existingIDs);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getSQL(Object key) throws SQLException {
                // If the given key is null or not an integer
            if (key == null || !(key instanceof Integer))
                return null;
                // Prepare a statement to query the lists table for the name of 
                // the list with the given key
            try (PreparedStatement pstmt = prepareStatement(
                    String.format("SELECT %s FROM %s WHERE %s = ?", 
                            LIST_NAME_COLUMN_NAME,
                            LIST_TABLE_NAME,
                            LIST_ID_COLUMN_NAME))) {
                    // Set the key for the list to get the name of
                pstmt.setInt(1, (Integer)key);
                    // Query the database
                ResultSet rs = pstmt.executeQuery();
                    // If the query had any results
                if (rs.next())
                    return rs.getString(LIST_NAME_COLUMN_NAME);
            }
            return null;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String putSQL(Integer key, String value) throws SQLException {
                // Check if the key is null
            Objects.requireNonNull(key);
                // Get the current time
            long time = System.currentTimeMillis();
                // If a list with the given listID exists
            if (containsKey(key)){
                    // Get the list's old name
                String oldValue = getSQL(key);
                    // If the old and new names are the same (there would be no 
                if (Objects.equals(oldValue, value))    // change)
                    return oldValue;
                    // Prepare a statement to change the list's name and last 
                    // modified time
                try (PreparedStatement pstmt = prepareStatement(String.format(
                        "UPDATE %s SET %s = ?, %s = ? WHERE %s = ?",
                                LIST_TABLE_NAME,
                                LIST_NAME_COLUMN_NAME,
                                LIST_LAST_MODIFIED_COLUMN_NAME,
                                LIST_ID_COLUMN_NAME))){
                        // Set the list's new name
                    pstmt.setString(1, value);
                        // Set the list's last modified time
                    pstmt.setLong(2, time);
                        // Set the listID of the list to rename
                    pstmt.setInt(3, key);
                        // Update the database
                    pstmt.executeUpdate();
                }
                return oldValue;
            } else{ // Prepare a statement to create a new list with the given 
                    // listID and name
                try(PreparedStatement pstmt = prepareStatement(String.format(
                        "INSERT INTO %s(%s, %s, %s, %s) VALUES (?, ?, ?, ?)", 
                                LIST_TABLE_NAME,
                                LIST_ID_COLUMN_NAME,
                                LIST_NAME_COLUMN_NAME,
                                LIST_LAST_MODIFIED_COLUMN_NAME,
                                LIST_CREATED_COLUMN_NAME))){
                        // Set the listID of the list to create
                    pstmt.setInt(1, key);
                        // Set the name of the new list
                    pstmt.setString(2, value);
                        // Set the list's last modified time
                    pstmt.setLong(3, time);
                        // Set the list's creation time
                    pstmt.setLong(4, time);
                        // Update the database
                    pstmt.executeUpdate();
                }
                return null;
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getTableName() {
            return LIST_TABLE_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getKeyColumn() {
            return LIST_ID_COLUMN_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getValueColumn() {
            return LIST_NAME_COLUMN_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected void setPreparedKey(PreparedStatement pstmt, 
                int parameterIndex, Integer key) throws SQLException {
            setParameter(pstmt,parameterIndex,key);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected void setPreparedValue(PreparedStatement pstmt, 
                int parameterIndex, String value) throws SQLException {
            pstmt.setString(parameterIndex, value);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected Integer getKeyFromResults(ResultSet rs) throws SQLException {
                // Get the key from the results
            int key = rs.getInt(LIST_ID_COLUMN_NAME);
                // If the key was null, return null. Otherwise, return the key
            return (rs.wasNull()) ? null : key;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getValueFromResults(ResultSet rs) throws SQLException{
            return rs.getString(LIST_NAME_COLUMN_NAME);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean canValueBeNull(){
            return true;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public Integer add(String value,long lastMod,long created,int flags,
                Integer sizeLimit){
                // Check the name
            checkValue(value);
                // This gets a set of listIDs currently in this map
            Set<Integer> existingIDs = new TreeSet<>(keySet());
                // This is the listID of the list that was added
            Integer listID = null;
                // Prepare a statement to insert a new list into the list table
            try(PreparedStatement pstmt = prepareStatement(String.format(
                    "INSERT INTO %s(%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)", 
                            LIST_TABLE_NAME,
                            LIST_NAME_COLUMN_NAME,
                            LIST_LAST_MODIFIED_COLUMN_NAME,
                            LIST_CREATED_COLUMN_NAME,
                            LIST_FLAGS_COLUMN_NAME,
                            LIST_SIZE_LIMIT_COLUMN_NAME))){
                    // Set the list name
                pstmt.setString(1, value);
                    // Set the list last modified time
                pstmt.setLong(2, lastMod);
                    // Set the list creation time
                pstmt.setLong(3, created);
                    // Set the list flags
                pstmt.setInt(4, flags);
                    // Set the list size limit
                setParameter(pstmt,5,sizeLimit);
                    // Execute the update
                pstmt.executeUpdate();
                    // Get the listID of the created list
                listID = getGeneratedIntegerKey(pstmt);
            } catch (SQLException ex) {
                ConnectionBased.throwConstraintException(ex);
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }   // If the listID of the created list was found, return it. 
                // Otherwise, find it and return the listID for the list name
            return (listID != null) ? listID:getGeneratedKey(value,existingIDs);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean removeUnusedRowsSQL()throws SQLException{
            return getConnection().removeUnusedRows(
                    LIST_TABLE_NAME,
                    LIST_OF_LISTS_TABLE_NAME,
                    LIST_ID_COLUMN_NAME) > 0;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean removeDuplicateRowsSQL()throws SQLException{
            throw new UnsupportedOperationException("removeDuplicateRows");
        }
    }
    /**
     * 
     */
    private class ListTypeSet extends AbstractQuerySet<Integer>{
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean containsSQL(Object o) throws SQLException {
                // If the given object is null or not an integer
            if (o == null || !(o instanceof Integer))
                return false;
                // Prepare a statement to check if the given object is a list 
                // type found in the list of lists table
            try(PreparedStatement pstmt = prepareContainsStatement(
                    LIST_OF_LISTS_TABLE_NAME,
                    LIST_TYPE_COLUMN_NAME)){
                    // Set the object to find
                setParameter(pstmt,1,(Integer)o);
                return containsCountResult(pstmt.executeQuery());
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean removeSQL(Object o) throws SQLException {
            throw new UnsupportedOperationException("remove");
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected Set<Integer> valueCacheSet() throws SQLException {
                // Prepare a statement to get the list types from the list of 
                // lists table. We only need one of each, so the search is for 
                // distinct instances of the list types
            try(PreparedStatement pstmt = prepareStatement(
                    String.format("SELECT DISTINCT %s FROM %s",
                            LIST_TYPE_COLUMN_NAME,
                            LIST_OF_LISTS_TABLE_NAME))){
                    // A set to cache the results of the query
                Set<Integer> cache = new LinkedHashSet<>();
                    // Query the database
                ResultSet rs = pstmt.executeQuery();
                    // While there are still results from the query to go through
                while(rs.next())
                    cache.add(rs.getInt(LIST_TYPE_COLUMN_NAME));
                return cache;
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected int sizeSQL() throws SQLException {
                // Prepare a statement to count the unique instances of the list 
                // types in the list of lists table
            try(PreparedStatement pstmt = prepareStatement(
                    String.format(TABLE_SIZE_QUERY_TEMPLATE, 
                            "DISTINCT "+LIST_TYPE_COLUMN_NAME,
                            LIST_OF_LISTS_TABLE_NAME))){
                    // Query the database
                ResultSet rs = pstmt.executeQuery();
                    // If there are any results from the query
                if (rs.next())
                    return rs.getInt(COUNT_COLUMN_NAME);
            }
            return 0;
        }
    }
    /**
     * 
     */
    private class ListIDListImpl extends AbstractDatabaseList<Integer> implements 
            ListIDList {
        /**
         * The type of lists in this list
         */
        private final int listType;
        /**
         * 
         * @param listType 
         */
        ListIDListImpl(int listType){
            this.listType = listType;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public int getListType(){
            return listType;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getTableName(){
            return LIST_OF_LISTS_TABLE_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getTypeIDColumn(){
            return LIST_TYPE_COLUMN_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getIndexColumn(){
            return LIST_INDEX_COLUMN_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getElementColumn(){
            return LIST_ID_COLUMN_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected void setPreparedTypeID(PreparedStatement pstmt, 
                int parameterIndex)throws SQLException{
            pstmt.setInt(parameterIndex, getListType());
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected void setPreparedElement(PreparedStatement pstmt, 
                int parameterIndex, Integer element)throws SQLException{
            setParameter(pstmt,parameterIndex,element);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected Integer getElementFromResults(ResultSet rs)throws SQLException{
                // Get the listID from the results
            int listID = rs.getInt(LIST_ID_COLUMN_NAME);
                // If the listID was null or this does not contain the listID
//            return (rs.wasNull() || !containsListID(listID)) ? null : listID;
                // If the listID was null, return null. Otherwise, return the 
            return (rs.wasNull()) ? null : listID;      // listID
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected int indexOfSQL(Object o, boolean descending) throws SQLException {
                // If the object is not null and not an integer
            if (o != null && !(o instanceof Integer))
                return -1;
                // Get the index of the element
            return indexOfElement((Integer)o,descending);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected void checkElement(Integer element)throws SQLException {
                // If the element is not null
            if (element != null)
                    // Check the element as a listID
                checkListID(element);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected int sizeSQL() throws SQLException {
            return getListCount(getListType());
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public int totalSize() {
            try{
                return getTotalSizeOfLists(getListType());
            } catch (SQLException ex) {
                appendWarning(ex);
            }
            return 0;
        }
    }
//    /**
//     * 
//     */
//    private class ListTypeMapImpl extends AbstractDatabaseTypeIDMap<ListIDList>{
//        /**
//         * 
//         */
//        ListTypeMapImpl() {
//            super(getListTypes());
//        }
//        /**
//         * {@inheritDoc }
//         */
//        @Override
//        protected ListIDList createValueSQL(Integer key) throws SQLException {
//            return new ListIDListImpl(key);
//        }
//    }
    /**
     * 
     */
    private class ListTypeMapImpl extends AbstractQueryMap<Integer,ListIDList>{
        /**
         * 
         */
        private final Map<Integer, ListIDList> cache = new TreeMap<>();
        /**
         * 
         */
        private final SQLSet<Integer> listTypeSet;
        /**
         * 
         * @throws SQLException 
         */
        ListTypeMapImpl() throws SQLException{
            this.listTypeSet = getListTypes();
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public LinkDatabaseConnection getConnection() throws SQLException {
            return LinkDatabaseConnection.this;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean containsKeySQL(Object key) throws SQLException{
                // If the given key is null or not an integer
            if (key == null || !(key instanceof Integer))
                return false;
            return listTypeSet.contains((Integer)key);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected ListIDList removeSQL(Object key) throws SQLException{
            throw new UnsupportedOperationException("remove");
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected ListIDList putSQL(Integer key, ListIDList value) 
                throws SQLException{
            throw new UnsupportedOperationException("put");
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected ListIDList getSQL(Object key) throws SQLException{
            if (key == null || !(key instanceof Integer))
                return null;
                // Get the key as an integer
            int listType = (Integer)key;
                // If the type set does not contain the given key
            if (!listTypeSet.contains(listType))
                return null;
                // Return the value for the key, constructing and caching it if 
                // it has not been created yet
            return cache.computeIfAbsent(listType, (Integer t) -> 
                    new ListIDListImpl(t));
        }
        /**
         * 
         * @param key
         * @return 
         */
        private Entry<Integer, ListIDList> getEntry(Integer key){
            if (key == null)    // If the given key is null
                return null;
            return new AbstractMap.SimpleImmutableEntry<>(key,get(key));
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected Set<Entry<Integer, ListIDList>> entryCacheSet() throws SQLException {
                // Create a set to cache the entries
            Set<Entry<Integer, ListIDList>> cacheSet = new LinkedHashSet<>();
                // Go through the keys in the type set
            for (Integer listType : listTypeSet){
                    // Add the current entry to the cache
                cacheSet.add(getEntry(listType));
            }
            return cacheSet;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected int sizeSQL() throws SQLException {
            return listTypeSet.size();
        }
    }
    /**
     * 
     */
    private class ListContentsImpl extends AbstractDatabaseList<String> 
            implements ListContents{
        /**
         * The listID of the list
         */
        private final int listID;
        /**
         * 
         * @param listID The listID of the list
         */
        ListContentsImpl(int listID){
            this.listID = listID;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public int getListID() {
            return listID;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public boolean isHidden(){
            try {
                return !getShownListIDs().contains(listID);
            } catch (SQLException ex) {
                appendWarning(ex);
                return false;
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public boolean exists() {
            try {
                return containsListID(listID);
            } catch (SQLException ex) {
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
        }
        /**
         * This checks whether the list exists and, if not, throws an {@code 
         * IllegalStateException}.
         * @throws SQLException If a database error occurs.
         * @throws IllegalStateException If the list does not exist.
         * @see #exists() 
         * @see #getListID() 
         */
        private void requireListExists() throws SQLException{
                // If this list does not exist
            if (!exists())
                throw new IllegalStateException("List with ID "+listID+" does not exist");
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public String getName() {
            try {
                requireListExists();    // Require the list to exist
                    // Get the list's name
                return getListNameMap().get(listID);
            } catch (SQLException ex) {
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public void setName(String name) {
            try {
                requireListExists();    // Requre the list to exist
                    // Set the list's name
                getListNameMap().put(listID, name);
            } catch (SQLException ex) {
                ConnectionBased.throwConstraintException(ex);
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public long getLastModified() {
            try{
                requireListExists();    // Requre the list to exist
                    // Prepare a statement to get the list's last modified time
                try (PreparedStatement pstmt = prepareStatement(
                        String.format("SELECT %s FROM %s WHERE %s = ?", 
                                LIST_LAST_MODIFIED_COLUMN_NAME,
                                LIST_TABLE_NAME,
                                LIST_ID_COLUMN_NAME))) {
                        // Set the listID of the list to get the last modified 
                    pstmt.setInt(1, listID);    // time of
                        // Query the database
                    ResultSet rs = pstmt.executeQuery();
                        // If the query has any results
                    if (rs.next())
                        return rs.getLong(LIST_LAST_MODIFIED_COLUMN_NAME);
                    return 0;
                }
            } catch (SQLException ex){
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public void setLastModified(long lastMod) {
            try{
                requireListExists();    // Requre the list to exist
                    // Prepare a statement to set the last modified time of the 
                try (PreparedStatement pstmt = prepareStatement(    // list
                        String.format("UPDATE %s SET %s = ? WHERE %s = ?", 
                                LIST_TABLE_NAME,
                                LIST_LAST_MODIFIED_COLUMN_NAME,
                                LIST_ID_COLUMN_NAME))) {
                        // Set the new last modified time
                    pstmt.setLong(1, lastMod);
                        // Set the listID of the list to alter
                    pstmt.setInt(2, listID);
                        // Update the database
                    pstmt.executeUpdate();
                }
            } catch (SQLException ex){
                ConnectionBased.throwConstraintException(ex);
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public long getCreationTime() {
            try{
                requireListExists();    // Requre the list to exist
                    // Prepare a statement to get the list's creation time
                try (PreparedStatement pstmt = prepareStatement(
                        String.format("SELECT %s FROM %s WHERE %s = ?", 
                                LIST_CREATED_COLUMN_NAME,
                                LIST_TABLE_NAME,
                                LIST_ID_COLUMN_NAME))) {
                        // Set the listID of the list to use
                    pstmt.setInt(1, listID);
                        // Query the database
                    ResultSet rs = pstmt.executeQuery();
                        // If the query returned any results
                    if (rs.next())
                        return rs.getLong(LIST_CREATED_COLUMN_NAME);
                    return 0;
                }
            } catch (SQLException ex){
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public void setCreationTime(long time) {
            try{
                requireListExists();    // Requre the list to exist
                    // Prepare a statement to set the list's creation time
                try (PreparedStatement pstmt = prepareStatement(
                        String.format("UPDATE %s SET %s = ? WHERE %s = ?", 
                                LIST_TABLE_NAME,
                                LIST_CREATED_COLUMN_NAME,
                                LIST_ID_COLUMN_NAME))) {
                        // Set the new creation time for the list
                    pstmt.setLong(1, time);
                        // Set the listID of the list to alter
                    pstmt.setInt(2, listID);
                        // Update the database
                    pstmt.executeUpdate();
                }
            } catch (SQLException ex){
                ConnectionBased.throwConstraintException(ex);
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public int getFlags() {
            try{
                requireListExists();    // Requre the list to exist
                    // Prepare a statement to get the list's flags
                try (PreparedStatement pstmt = prepareStatement(
                        String.format("SELECT %s FROM %s WHERE %s = ?", 
                                LIST_FLAGS_COLUMN_NAME,
                                LIST_TABLE_NAME,
                                LIST_ID_COLUMN_NAME))) {
                        // Set the listID of the list to use
                    pstmt.setInt(1, listID);
                        // Query the database
                    ResultSet rs = pstmt.executeQuery();
                        // If the query returned any results
                    if (rs.next())
                        return rs.getInt(LIST_FLAGS_COLUMN_NAME);
                    return 0;
                }
            } catch (SQLException ex){
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public void setFlags(int flags) {
            try{
                requireListExists();    // Requre the list to exist
                    // Prepare a statement to set the list's flags
                try (PreparedStatement pstmt = prepareStatement(
                        String.format("UPDATE %s SET %s = ? WHERE %s = ?", 
                                LIST_TABLE_NAME,
                                LIST_FLAGS_COLUMN_NAME,
                                LIST_ID_COLUMN_NAME))) {
                        // Set the list's new flags
                    pstmt.setInt(1, flags);
                        // Set the listID of the list to alter
                    pstmt.setInt(2, listID);
                        // Update the database
                    pstmt.executeUpdate();
                }
            } catch (SQLException ex){
                ConnectionBased.throwConstraintException(ex);
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public Integer getSizeLimit() {
            try{
                requireListExists();    // Requre the list to exist
                    // Prepare a statement to get the list's size limit
                try (PreparedStatement pstmt = prepareStatement(
                        String.format("SELECT %s FROM %s WHERE %s = ?", 
                                LIST_SIZE_LIMIT_COLUMN_NAME,
                                LIST_TABLE_NAME,
                                LIST_ID_COLUMN_NAME))) {
                        // Set the listID of the list to use
                    pstmt.setInt(1, listID);
                        // Query the database
                    ResultSet rs = pstmt.executeQuery();
                        // If the query returned any results
                    if (rs.next()){
                            // Get the size limit
                        int limit = rs.getInt(LIST_SIZE_LIMIT_COLUMN_NAME);
                            // If the size limit was null, return null. 
                            // Otherwise, return the size limit
                        return (rs.wasNull()) ? null : limit;
                    }
                    return 0;
                }
            } catch (SQLException ex){
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public void setSizeLimit(Integer sizeLimit) {
            try{
                requireListExists();    // Requre the list to exist
                    // Prepare a statement to set the list's size limit
                try (PreparedStatement pstmt = prepareStatement(
                        String.format("UPDATE %s SET %s = ? WHERE %s = ?", 
                                LIST_TABLE_NAME,
                                LIST_SIZE_LIMIT_COLUMN_NAME,
                                LIST_ID_COLUMN_NAME))) {
                        // Set the new size limit for the list
                    setParameter(pstmt,1,sizeLimit);
                        // Set the listID of the list to alter
                    pstmt.setInt(2, listID);
                        // Update the database
                    pstmt.executeUpdate();
                }
            } catch (SQLException ex){
                ConnectionBased.throwConstraintException(ex);
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getTableName(){
            return LIST_DATA_TABLE_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getDataViewName(){
            return LIST_CONTENTS_VIEW_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getTypeIDColumn(){
            return LIST_ID_COLUMN_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getIndexColumn(){
            return LINK_INDEX_COLUMN_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getElementColumn(){
            return LINK_ID_COLUMN_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getDataElementColumn(){
            return LINK_URL_COLUMN_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected void setPreparedTypeID(PreparedStatement pstmt, 
                int parameterIndex)throws SQLException{
            pstmt.setInt(parameterIndex, listID);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected void setPreparedElement(PreparedStatement pstmt, 
                int parameterIndex, String element)throws SQLException{
            pstmt.setString(parameterIndex, element);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected void setReplaceIndexElement(PreparedStatement pstmt, 
                int parameterIndex, String element)throws SQLException{
            setParameter(pstmt,parameterIndex,getLinkMap().addIfAbsent(element));
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getElementFromResults(ResultSet rs)throws SQLException{
            return rs.getString(LINK_URL_COLUMN_NAME);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected int indexOfSQL(Object o, boolean descending) throws SQLException {
            requireListExists();    // Requre the list to exist
                // If the object is not null and not a String
            if (o != null && !(o instanceof String))
                return -1;
            return indexOfElement((String)o,descending);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected int sizeSQL() throws SQLException{
            if (!exists())  // If the list does not exist
                return 0;
            return getListSize(listID);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getSQL(int index) throws SQLException {
            requireListExists();    // Requre the list to exist
            return super.getSQL(index);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected void checkElement(String element)throws SQLException {
            requireListExists();    // Requre the list to exist
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public LinksListModel toModel(LinksListModel model, 
                BiConsumer<Integer,Integer> observer){
            try{    
                requireListExists();    // Require the list to exist
                    // Prepare a statement to get the properties of this list 
                    // from the database
                try(PreparedStatement pstmt = prepareStatement(String.format(
                        "SELECT %s, %s, %s, %s, %s FROM %s WHERE %s = ?", 
                            LIST_NAME_COLUMN_NAME,
                            LIST_FLAGS_COLUMN_NAME,
                            LIST_LAST_MODIFIED_COLUMN_NAME, 
                            LIST_CREATED_COLUMN_NAME,
                            LIST_SIZE_LIMIT_COLUMN_NAME,
                            LIST_TABLE_NAME,
                            LIST_ID_COLUMN_NAME))){
                        // Provide this list's listID
                    pstmt.setInt(1, listID);
                        // Get the results of the query
                    ResultSet rs = pstmt.executeQuery();
                        // If the query had no results
                    if (!rs.next())
                        return model;
                        // Get the name of this list
                    String name = rs.getString(LIST_NAME_COLUMN_NAME);
                        // If the given model is null (this is to create a new 
                    if (model == null)  // model)
                        model = new LinksListModel(name,listID);
                    else{
                        model.setListID(listID);
                        model.setListName(name);
                        model.clear();
                    }   // Set the model's flags to be this list's flags
                    model.setFlags(rs.getInt(LIST_FLAGS_COLUMN_NAME));
                        // Set the model's last modified time
                    model.setLastModified(rs.getLong(LIST_LAST_MODIFIED_COLUMN_NAME));
                        // Set the model's creation time
                    model.setCreationTime(rs.getLong(LIST_CREATED_COLUMN_NAME));
                        // Get the size limit for this list
                    int sizeLimit = rs.getInt(LIST_SIZE_LIMIT_COLUMN_NAME);
                    if (!rs.wasNull())  // If the size limit is not null
                        model.setSizeLimit(sizeLimit);
                }   // Get whether the model's modification limitations are enabled
                boolean modLimit = model.isModificationLimitEnabled();
                    // Disable the model's modification limitations
                model.setModificationLimitEnabled(false);
                    // The size for this list
                int size = size();
                    // Prepare a statement to read the contents of this list 
                    // from the database
                try(PreparedStatement pstmt = prepareStatement(String.format(
                        "SELECT %s%s FROM %s WHERE %s = ? ORDER BY %s", 
                                    // If the model does not allow duplicates, 
                                    // then only get the distict elements
                                (model.getAllowsDuplicates())?"":"DISTINCT ",
                                getDataElementColumn(),
                                getDataViewName(),
                                getTypeIDColumn(),
                                getIndexColumn()))){
                        // Provide this list's listID
                    pstmt.setInt(1, listID);
                        // Get the results of the query
                    ResultSet rs = pstmt.executeQuery();
                        // While there are still results
                    while (rs.next()){
                            // Get the link from the current row
                        String temp = rs.getString(getDataElementColumn());
                            // If the link is not null and not blank
                        if (temp != null && !temp.isBlank()){
                            try{
                                model.add(temp);    // Add the link to the model
                                    // If an observer was provided
                                if (observer != null)
                                    observer.accept(model.size()-1, size);
                            } catch(IllegalArgumentException | IllegalStateException ex){ }
                        }
                    }
                }   // If the model does not allow duplicates
                if (!model.getAllowsDuplicates())
                        // Remove any duplicates from the model
                    model.removeDuplicates();
                    // Restore the model's modification limitations back to what 
                    // it was set to before
                model.setModificationLimitEnabled(modLimit);
                    // Set whether the model is hidden
                model.setHidden(isHidden());
                    // Set the model to be edited if its size does not match 
                model.setEdited(model.size() != size);  // this list's size
            } catch (SQLException ex){
                ConnectionBased.throwConstraintException(ex);
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
            return model;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public void updateContents(LinksListModel model, 
                BiConsumer<Integer,Integer>observer,Map<String,Long> linkIDMap){
                // Check if the model is null
            Objects.requireNonNull(model);
            try{    // Get the current state of the auto-commit
                boolean autoCommit = getAutoCommit();
                    // Turn off the auto-commit in order to group the following 
                    // database transactions to improve performance
                setAutoCommit(false);
                    // Get the link map for this database
                LinkMap linkMap = getLinkMap();
                    // Add any links from the model that are not already in the 
                linkMap.addAllIfAbsent(model); // link map.
                    // If no map of links to linkIDs was provided
                if (linkIDMap == null)
                        // Get the map from the linkMap
                    linkIDMap = linkMap.inverse();
                clear();    // Clear this list
                    // Go through the contents of the model
                for (int index = 0; index < model.size(); index++){
                        // Get the value at the current index in the model
                    String value = model.get(index);
                        // Get the linkID for the value
                    Long linkID = linkIDMap.get(value);
                        // If the map did not provide a linkID for the value
                    if (linkID == null)
                            // Get the linkID from the link map's inverse map
                        linkID = linkMap.inverse().get(value);
                        // If the linkID for the value is still null
                    if (linkID == null)
                            // Try adding the value to the link map and get its 
                        linkID = linkMap.addIfAbsent(value);    // new linkID
                        // Prepare a statement to insert the entry in the list 
                        // into the list data table
                    try(PreparedStatement pstmt = prepareStatement(String.format(
                            "INSERT INTO %s(%s, %s, %s) VALUES (?, ?, ?)", 
                                    LIST_DATA_TABLE_NAME,
                                    LIST_ID_COLUMN_NAME,
                                    LINK_INDEX_COLUMN_NAME,
                                    LINK_ID_COLUMN_NAME))){
                            // Set the listID of the list to insert into
                        pstmt.setInt(1, listID);
                            // Set the index at which to insert at
                        pstmt.setInt(2, index);
                            // Set the linkID of the link to insert
                        pstmt.setLong(3, linkID);
                            // Update the database
                        pstmt.executeUpdate();
                    }   // If an observer was provided
                    if (observer != null)
                        observer.accept(index, model.size());
                }
                commit();       // Commit the changes to the database
                    // Restore the auto-commit back to what it was set to before
                setAutoCommit(autoCommit);
            } catch (SQLException ex){
                ConnectionBased.throwConstraintException(ex);
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }   // Set both this and the model's last modified time to the 
            model.setLastModified(setLastModified());   // current time
                // Clear whether the model has been edited
            model.clearEdited();
        }
    }
    /**
     * 
     */
    private class ListDataMapImpl extends AbstractDatabaseTypeIDMap<ListContents> 
            implements ListDataMap{
        /**
         * 
         * @throws SQLException 
         */
        ListDataMapImpl() throws SQLException {
            super(LinkDatabaseConnection.this.getListNameMap().navigableKeySet());
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected ListContents createValueSQL(Integer key) throws SQLException{
            return new ListContentsImpl(key);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public ListNameMap getListNameMap() {
            try{
                return getConnection().getListNameMap();
            } catch (SQLException ex) {
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public void clearAll() {
            try {
                clearTable(LIST_DATA_TABLE_NAME);
            } catch (SQLException ex) {
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public int totalSize() {
            try{
                return getTotalSizeOfLists();
            } catch (SQLException ex) {
                appendWarning(ex);
                return 0;
            }
        }
    }
    /**
     * 
     */
    private class ListDataListImpl extends ListDataList{
        /**
         * 
         */
        private final ListIDList listIDs;
        /**
         * 
         * @param listType 
         */
        ListDataListImpl(int listType) throws SQLException{
            listIDs = LinkDatabaseConnection.this.getListIDs(listType);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public LinkDatabaseConnection getConnection() throws SQLException {
            return LinkDatabaseConnection.this;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public ListIDList getListIDs() {
            return listIDs;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public ListDataMap getListDataMap() {
            try{
                return getConnection().getListDataMap();
            } catch (SQLException ex){
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
        }
    }
    /**
     * 
     */
    private abstract class AbstractDatabasePropertyMap extends 
            AbstractQueryMap<String,String> implements DatabasePropertyMap{
        /**
         * 
         * @return 
         */
        protected abstract String getValueColumn();
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean containsKeySQL(Object key) throws SQLException {
                // If the given key is null or a String
            if (key == null || !(key instanceof String))
                return false;
                // Prepare a statement to check if the given key is found in the 
                // database config table and is not mapped to a null value
            try(PreparedStatement pstmt = prepareStatement(String.format(
                    TABLE_CONTAINS_QUERY_TEMPLATE+" AND %s IS NOT NULL", 
                            DATABASE_CONFIG_KEY_COLUMN_NAME,
                            DATABASE_CONFIG_TABLE_NAME,
                            DATABASE_CONFIG_KEY_COLUMN_NAME,
                            getValueColumn()))){
                    // Set the key to search for
                pstmt.setString(1, (String)key);
                return containsCountResult(pstmt.executeQuery());
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected boolean containsValueSQL(Object value) throws SQLException {
                // If the given value is null or a String
            if (value == null || !(value instanceof String))
                return false;
                // Prepare a statement to check if the given value is found in 
                // the database config table
            try(PreparedStatement pstmt = prepareContainsStatement(
                    DATABASE_CONFIG_TABLE_NAME,
                    getValueColumn())){
                    // Set the value to search for
                pstmt.setString(1, (String)value);
                return containsCountResult(pstmt.executeQuery());
            }
        }
        /**
         * 
         * @throws SQLException 
         */
        protected void removeUnusedProperties() throws SQLException {
                // Prepare a statement to remove any entries in the database 
                // config table where both the set and default values are null
            try(PreparedStatement pstmt = prepareStatement(String.format(
                    "DELETE FROM %s WHERE %s IS NULL AND %s IS NULL", 
                            DATABASE_CONFIG_TABLE_NAME,
                            DATABASE_CONFIG_DEFAULT_COLUMN_NAME,
                            DATABASE_CONFIG_VALUE_COLUMN_NAME))){
                    // Update the database
                pstmt.executeUpdate();
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String removeSQL(Object key) throws SQLException {
                // If the given key is null or a String
            if (key == null || !(key instanceof String))
                return null;
                // Get the key as a String
            String name = (String) key;
                // Get the old value
            String value = getSQL(name);
                // If the old value is null
            if (value == null)
                return null;
                // Prepare a statement to set the value to null
            try(PreparedStatement pstmt = prepareStatement(
                    String.format("UPDATE %s SET %s = ? WHERE %s = ?", 
                            DATABASE_CONFIG_TABLE_NAME,
                            getValueColumn(),
                            DATABASE_CONFIG_KEY_COLUMN_NAME))){
                    // Set the value to null
                pstmt.setString(1, null);
                    // Set the key for the value
                pstmt.setString(2, name);
                    // Update the database
                pstmt.executeUpdate();
            }   // Remove any properties that are unused
            removeUnusedProperties();
            return value;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getSQL(Object key) throws SQLException {
                // If the given key is null or a String
            if (key == null || !(key instanceof String))
                return null;
                // Prepare a statement to get the value for the key, limit by 1
            try(PreparedStatement pstmt = prepareStatement(
                    String.format("SELECT %s FROM %s WHERE %s = ? LIMIT 1", 
                            getValueColumn(),
                            DATABASE_CONFIG_TABLE_NAME,
                            DATABASE_CONFIG_KEY_COLUMN_NAME))){
                    // Set the key for the value to get
                pstmt.setString(1, (String) key);
                    // Query the database
                ResultSet rs = pstmt.executeQuery();
                    // If there are any results
                if (rs.next())
                    return rs.getString(getValueColumn());
            }
            return null;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected String putSQL(String key, String value) throws SQLException {
                // Check if the key is null
            Objects.requireNonNull(key);
                // Check if the value is null
            Objects.requireNonNull(value);
                // Whether the database contained the given key at all
            boolean containsKey = false;
                // The old value for the key
            String oldValue = null;
                // Prepare a statement to query the database for the current 
                // value and whether the table even contains the key
            try(PreparedStatement pstmt = prepareStatement(String.format(
                    "SELECT %s, COUNT(%s) AS %s FROM %s WHERE %s = ?",
                        getValueColumn(),
                        DATABASE_CONFIG_KEY_COLUMN_NAME,
                        COUNT_COLUMN_NAME,
                        DATABASE_CONFIG_TABLE_NAME,
                        DATABASE_CONFIG_KEY_COLUMN_NAME))){
                    // Set the key to search for
                pstmt.setString(1, key);
                    // Query the database
                ResultSet rs = pstmt.executeQuery();
                    // If there are any results
                if (rs.next()){
                        // If there are any instances of the given key
                    containsKey = rs.getInt(COUNT_COLUMN_NAME) > 0;
                        // Get the old value of the column
                    oldValue = rs.getString(getValueColumn());
                }
            }   // Prepare a statement to either insert or update the value for 
                // the key in the database config table
            try (PreparedStatement pstmt = prepareStatement(String.format(
                        // If the table previously contained the key, update the 
                        // value. Otherwise, insert the key and value into the 
                    (containsKey) ?     // table
                            "UPDATE %s SET %s = ? WHERE %s = ?" : 
                            "INSERT INTO %s(%s, %s) VALUES (?, ?)",
                        DATABASE_CONFIG_TABLE_NAME,
                        getValueColumn(),
                        DATABASE_CONFIG_KEY_COLUMN_NAME))){
                    // Set the value
                pstmt.setString(1, value);
                    // Set the key
                pstmt.setString(2, key);
                    // Update the database
                pstmt.executeUpdate();
            }
            return oldValue;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected int sizeSQL() throws SQLException {
                // Prepare a statement to get the amount of entries in the 
                // database config table where the value is not null
            try (PreparedStatement pstmt = prepareStatement(String.format(
                    TABLE_SIZE_QUERY_TEMPLATE+" WHERE %s IS NOT NULL", 
                        getValueColumn(),
                        DATABASE_CONFIG_TABLE_NAME,
                        getValueColumn()))){
                    // Query the database
                ResultSet rs = pstmt.executeQuery();
                    // If there are any results from the query
                if (rs.next())
                    return rs.getInt(COUNT_COLUMN_NAME);
            }
            return 0;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected void clearSQL() throws SQLException {
                // Prepare a statement to set all the values to null
            try(PreparedStatement pstmt = prepareStatement(
                    String.format("UPDATE %s SET %s = ?", 
                            DATABASE_CONFIG_TABLE_NAME,
                            getValueColumn()))){
                    // Set all values to null
                pstmt.setString(1, null);
                    // Update the database
                pstmt.executeUpdate();
            }   // Remove any properties that are unused
            removeUnusedProperties();
        }
        /**
         * {@inheritDoc }
         */
        @Override
        protected Set<Entry<String, String>> entryCacheSet() throws SQLException {
                // A set to cache the entries from the database
            Set<Entry<String, String>> cache = new LinkedHashSet<>();
                // Prepare a statement to go through the keys and values in the 
                // database config table where the values are not null
            try(PreparedStatement pstmt = prepareStatement(String.format(
                    "SELECT %s, %s FROM %s WHERE %s IS NOT NULL", 
                            DATABASE_CONFIG_KEY_COLUMN_NAME,
                            getValueColumn(),
                            DATABASE_CONFIG_TABLE_NAME,
                            getValueColumn()))){
                    // Query the database
                ResultSet rs = pstmt.executeQuery();
                    // While there are still results to go through
                while (rs.next())
                        // Add the current entry to the cache
                    cache.add(new AbstractMap.SimpleImmutableEntry<>(
                            rs.getString(DATABASE_CONFIG_KEY_COLUMN_NAME),
                            rs.getString(getValueColumn())));
            }
            return cache;
        }
    }
    /**
     * 
     */
    private class DatabasePropertyDefaults extends AbstractDatabasePropertyMap{
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getValueColumn() {
            return DATABASE_CONFIG_DEFAULT_COLUMN_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public DatabasePropertyMap getDefaults() {
            return null;    // This is the default properties map
        }
    }
    /**
     * 
     */
    private class DatabasePropertyMapImpl extends AbstractDatabasePropertyMap{
        /**
         * The default properties map
         */
        private DatabasePropertyMap defaults = null;
        /**
         * {@inheritDoc }
         */
        @Override
        protected String getValueColumn() {
            return DATABASE_CONFIG_VALUE_COLUMN_NAME;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public DatabasePropertyMap getDefaults() {
                // If the default properties map has not been initialized yet
            if (defaults == null)
                defaults = new DatabasePropertyDefaults();
            return defaults;
        }
        /**
         * 
         * @param rs
         * @return
         * @throws SQLException 
         */
        private String getProperty(ResultSet rs) throws SQLException{
                // Get the value from the results
            String value = rs.getString(DATABASE_CONFIG_VALUE_COLUMN_NAME);
                // If the value is not null, return it. Otherwise, get and 
            return (value != null) ? value :    // return the default value
                    rs.getString(DATABASE_CONFIG_DEFAULT_COLUMN_NAME);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public String getProperty(String key) {
            if (key == null)    // If the key is null
                return null;
                // Prepare a statement to get the set and default values from 
                // the database config table for the given key
            try(PreparedStatement pstmt = prepareStatement(
                    String.format("SELECT %s, %s FROM %s WHERE %s = ?", 
                            DATABASE_CONFIG_VALUE_COLUMN_NAME,
                            DATABASE_CONFIG_DEFAULT_COLUMN_NAME,
                            DATABASE_CONFIG_TABLE_NAME,
                            DATABASE_CONFIG_KEY_COLUMN_NAME))){
                    // Set the key for the values to get
                pstmt.setString(1, (String) key);
                    // Query the database
                ResultSet rs = pstmt.executeQuery();
                    // If there are any results for the query
                if (rs.next())
                    return getProperty(rs);
            } catch (SQLException ex) {
                ConnectionBased.throwConstraintException(ex);
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
            return null;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public Set<String> propertyNameSet() {
                // A set to cache the property names from the database
            Set<String> cache = new LinkedHashSet<>();
            try{    // Prepare a statement to get the keys from the database 
                    // where either the value or the default value are not null
                try(PreparedStatement pstmt = prepareStatement(String.format(
                        "SELECT %s FROM %s WHERE %s IS NOT NULL OR %s IS NOT NULL",
                            DATABASE_CONFIG_KEY_COLUMN_NAME,
                            DATABASE_CONFIG_TABLE_NAME,
                            DATABASE_CONFIG_VALUE_COLUMN_NAME,
                            DATABASE_CONFIG_DEFAULT_COLUMN_NAME))){
                        // Query the database
                    ResultSet rs = pstmt.executeQuery();
                        // While there are still results from the query
                    while(rs.next())
                        cache.add(rs.getString(DATABASE_CONFIG_KEY_COLUMN_NAME));
                }
            } catch (SQLException ex) {
                appendWarning(ex);
            }   // Return an unmodifiable version of the set
            return Collections.unmodifiableSet(cache);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public boolean containsPropertyName(String key){
            if (key == null)    // If the key is null
                return false;
            try{    // Prepare a statement to check whether the given key is 
                    // mapped to either a non-null set or default value
                try(PreparedStatement pstmt = prepareStatement(String.format(
                        TABLE_CONTAINS_QUERY_TEMPLATE+" AND (%s IS NOT NULL OR %s IS NOT NULL)", 
                            DATABASE_CONFIG_KEY_COLUMN_NAME,
                            DATABASE_CONFIG_TABLE_NAME,
                            DATABASE_CONFIG_KEY_COLUMN_NAME,
                            DATABASE_CONFIG_VALUE_COLUMN_NAME,
                            DATABASE_CONFIG_DEFAULT_COLUMN_NAME))){
                        // Set the key to check for
                    pstmt.setString(1, key);
                    return containsCountResult(pstmt.executeQuery());
                }
            } catch (SQLException ex) {
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public void setProperty(String key, String value, String defaultValue){
                // Check if the key is not null
            Objects.requireNonNull(key);
            try{    // Whether the database contained the given key at all
                boolean containsKey;
                    // Prepare a statement to see if the database config table 
                    // contains the key at all
                try(PreparedStatement pstmt = prepareContainsStatement(
                        DATABASE_CONFIG_TABLE_NAME,
                        DATABASE_CONFIG_KEY_COLUMN_NAME)){
                        // Set the key to check for
                    pstmt.setString(1, key);
                    containsKey = containsCountResult(pstmt.executeQuery());
                }   // Prepare a statement to either insert or update the values 
                    // for the key in the database config table
                try (PreparedStatement pstmt = prepareStatement(String.format(
                        // If the table previously contained the key, update the 
                        // values. Otherwise, insert the key and values into the 
                        (containsKey) ?     // table
                                "UPDATE %s SET %s = ?, %s = ? WHERE %s = ?" : 
                                "INSERT INTO %s(%s, %s, %s) VALUES (?, ?, ?)",
                            DATABASE_CONFIG_TABLE_NAME,
                            DATABASE_CONFIG_VALUE_COLUMN_NAME,
                            DATABASE_CONFIG_DEFAULT_COLUMN_NAME,
                            DATABASE_CONFIG_KEY_COLUMN_NAME))){
                        // Set the set value for the key
                    pstmt.setString(1, value);
                        // Set the default value for the key
                    pstmt.setString(2, defaultValue);
                        // Set the key
                    pstmt.setString(3, key);
                        // Update the database
                    pstmt.executeUpdate();
                }
            } catch (SQLException ex) {
                ConnectionBased.throwConstraintException(ex);
                appendWarning(ex);
                throw new UncheckedSQLException(ex);
            }
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public Set<Entry<String, String>> propertyEntrySet(){
                // A set to cache the property entries from the database
            Set<Entry<String, String>> cache = new LinkedHashSet<>();
            try{    // Prepare a statement to go through the key and values in 
                    // the database config table where either the set or default 
                    // values are not null
                try(PreparedStatement pstmt = prepareStatement(String.format(
                        "SELECT %s, %s, %s FROM %s WHERE %s IS NOT NULL OR %s IS NOT NULL",
                            DATABASE_CONFIG_KEY_COLUMN_NAME,
                            DATABASE_CONFIG_VALUE_COLUMN_NAME,
                            DATABASE_CONFIG_DEFAULT_COLUMN_NAME,
                            DATABASE_CONFIG_TABLE_NAME,
                            DATABASE_CONFIG_VALUE_COLUMN_NAME,
                            DATABASE_CONFIG_DEFAULT_COLUMN_NAME))){
                        // Query the database
                    ResultSet rs = pstmt.executeQuery();
                        // While there are still results to go through
                    while(rs.next()){
                            // Add the current entry to the cache
                        cache.add(new AbstractMap.SimpleImmutableEntry<>(
                                rs.getString(DATABASE_CONFIG_KEY_COLUMN_NAME),
                                getProperty(rs)));
                    }
                }
            } catch (SQLException ex) {
                appendWarning(ex);
            }   // Return an unmodifiable version of the set
            return Collections.unmodifiableSet(cache);
        }
    }
}
