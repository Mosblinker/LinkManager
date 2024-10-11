/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.database;

import java.sql.*;
import java.util.*;
import javax.swing.tree.*;
import static manager.database.LinkDatabaseConnection.*;
import sql.*;

/**
 *
 * @author Milo Steier
 */
class PrefixMapImpl extends AbstractQueryRowMap<Integer,String> implements PrefixMap {
    /**
     * This is a comparator that compares Strings based off length, with longer 
     * Strings coming before short Strings. This is primarily used by the prefix 
     * map for certain functions.
     */
    protected static final Comparator<String> LONG_LENGTH_COMPARATOR = 
            (String o1, String o2) -> {
            // If the length of the strings are the same
        if (o1.length() == o2.length())
            return o1.compareTo(o2);
            // Compare the lengths of the strings, with longer Strings coming 
            // before shorter strings
        return Integer.compare(o2.length(), o1.length());
    };
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
     * This constructs a PrefixMapImpl with the given connection to the database
     * @param conn The connection to the database (cannot be null).
     */
    public PrefixMapImpl(LinkDatabaseConnection conn){
        super(conn);
    }
        // TODO: Implement caching
//    /**
//     * 
//     */
//    private final TreeMap<Integer,String> cache = new TreeMap<>();
    /**
     * {@inheritDoc }
     */
    @Override
    public void syncCache(){
        // TODO: Implement caching
//            // Clear the cache
//        cache.clear();
//            // Create an iterator to go through the entries in this map
//        Iterator<Map.Entry<Integer,String>> itr = super.entryIterator(
//                true, null, true, true, null, true);
//            // While there are still entries in the iterator
//        while(itr.hasNext()){
//                // Get the current entry from the iterator
//            Map.Entry<Integer,String> entry = itr.next();
//            cache.put(entry.getKey(), entry.getValue());
//        }
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
        // TODO: Implement caching
//        if (cache.containsKey((Integer)key))
//            return true;
//        try(PreparedStatement pstmt = getConnection().prepareContainsStatement(
//                getTableName(),
//                getKeyColumn())){
//            setPreparedKeyColumn(pstmt,1,(int)key);
//            return containsCountResult(pstmt.executeQuery());
//        }
        return getConnection().containsPrefixID((int)key);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected boolean containsValueSQL(Object value) throws SQLException {
            // If the given value is either null or not a string
        if (value == null || !(value instanceof String))
            return false;
        // TODO: Implement caching
//        if (cache.containsValue((String)value))
//            return true;
            // Prepare a statement to check if the prefix table contains the 
            // given value as a prefix
        try(PreparedStatement pstmt = getConnection().prepareContainsStatement(
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
        // TODO: Implement caching
//        cache.remove(prefixID);
            // This gets a map containing any prefixes for the prefix being 
            // removed. This is so that we can change the prefixID for the links 
            // that use the prefix that is being removed
        NavigableMap<Integer, String> matches = getPrefixes(value);
            // Get the key for the longest prefix (excluding the one that will 
            // be removed) for the prefix being removed. Due to how the returned 
            // tree is sorted, the prefixID in the map after the prefix being 
            // removed should be the longest prefix not being removed
        Integer newPrefixID = matches.higherKey(prefixID);
            // If the prefixID for the new prefix is null
        if (newPrefixID == null)
                // Use the empty prefix
            newPrefixID = getEmptyPrefixID();
            // Prepare a statement to update the links table so that any links 
            // that were using the prefix that is being removed now use the 
            // found new prefix, with the difference being added to the link's 
            // suffix to preserve the links
        try(PreparedStatement pstmt = getConnection().prepareStatement(
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
        }   // Prepare a statement to remove the prefix from the prefix table
        try (PreparedStatement pstmt = getConnection().prepareStatement(
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
            // Prepare a statement to insert the prefix into the prefix table
        try (PreparedStatement pstmt = getConnection().prepareStatement(
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
        // TODO: Implement caching
//        cache.put(prefixID, value);
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
            // TODO: Implement caching
//        if (cache.containsKey(prefixID))
//            return cache.get(prefixID);
            // Prepare a statement to get the value mapped to the given key
        try (PreparedStatement pstmt = getConnection().prepareStatement(
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
                    // TODO: Implement caching
//                if (value != null)
//                    cache.put(prefixID, value);
//                    putIntoCache(prefixID,value);
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
        try (PreparedStatement pstmt = getConnection().prepareStatement(
                String.format(
                    // If there is no value stored for the given key, insert the 
                    // value into the table. Otherwise, update the value in the 
                (oldValue == null) ?    // database
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
        }   // TODO: Implement caching
//        cache.put(key, value);
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
    protected String getUsedTableName() {
        return LINK_TABLE_NAME;
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
            boolean toEnd, Integer toKey, boolean toInclusive, boolean useValue, 
            String value)throws SQLException{
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
        try(PreparedStatement pstmt = getConnection().prepareStatement(
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
    // TODO: Re-add this function to PrefixMap or remove it?
//    /**
//     * {@inheritDoc }
//     */
//    @Override
//    public int getPrefixCount(String value){
//        Objects.requireNonNull(value);
//        try(PreparedStatement pstmt = getConnection().prepareStatement(
//                String.format("SELECT %s FROM %s WHERE %s = ?", 
//                        PREFIX_COUNT_COLUMN_NAME,
//                        PREFIX_COUNT_VIEW_NAME,
//                        getValueColumn()))){
//            setPreparedValue(pstmt,1,value);
//            ResultSet rs = pstmt.executeQuery();
//            if (rs.next()){
//                int count = rs.getInt(PREFIX_COUNT_COLUMN_NAME);
//                if (!rs.wasNull())
//                    return count;
//            }
//        } catch (SQLException ex) {
//            appendWarning(ex);
//            throw new UncheckedSQLException(ex);
//        }
//        return 0;
//    }
    /**
     * {@inheritDoc }
     */
    @Override
    public NavigableMap<Integer, String> getPrefixes(String value){
            // Check if the value is null
        Objects.requireNonNull(value);
            // Create a copy of this map as it currently is, so that the prefix 
            // length comparator is disconnected from this map
        TreeMap<Integer,String> prefixMap = new TreeMap<>(this);
            // Create a comparator to compare prefix IDs to the length of the 
            // prefix
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
        };  // Create a TreeMap with the prefix length comparator, which will 
            // get the prefixes from this map
        TreeMap<Integer,String> matches = new TreeMap<>(prefixLenComp);
            // Get a prepared statement to find any matching prefixes
        try(PreparedStatement pstmt = getConnection().prepareStatement(PREFIX_SEARCH_QUERY)){
                // Set the String to match
            pstmt.setString(1, value);
                // Get the results of the query
            ResultSet rs = pstmt.executeQuery();
                // While there are still rows from the query
            while(rs.next()){
                    // Get the prefix from the current row
                String prefix = rs.getString(PREFIX_COLUMN_NAME);
                    // If the given String actually starts with the prefix (the 
                    // initial search was case insensitive, this check makes the 
                    // search case sensitive)
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
            // Create a prepared statement to search for any prefixes that may 
            // match the given String
        try(PreparedStatement pstmt = getConnection().prepareStatement(PREFIX_SEARCH_QUERY)){
                // Set the String to match
            pstmt.setString(1, value);
                // Get the results of the query
            ResultSet rs = pstmt.executeQuery();
                // While there are still rows from the query
            while(rs.next()){
                    // Get the entry from the current row
                Map.Entry<Integer, String> entry = getEntryFromResults(rs);
                    // If the given String actually starts with the prefix from 
                    // the current row (the initial search was case insensitive, 
                    // this check makes the search case sensitive)
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
    protected boolean removeDuplicateRowsSQL()throws SQLException{
        return false;
    }
    /**
     * 
     * @param values
     * @return
     * @throws SQLException 
     */
    protected DefaultMutableTreeNode createPrefixTreeSQL(Collection<String> values)
            throws SQLException{
            // Get a sorted set copy of the collection
        TreeSet<String> sortedValues = new TreeSet<>(values);
            // Create a copy of the values already in this map
        Set<String> prefixes = new HashSet<>(values());
            // Remove any values that are already prefixes in this map
        sortedValues.removeAll(prefixes);
            // Get the prefix tree
        DefaultMutableTreeNode prefixRoot = createPrefixTree(sortedValues,
                prefixes,getConnection().getPrefixThreshold(),true);
        // The following code is primarily used to reorder the nodes so that 
        // nodes that allow children will appear before nodes that do not allow 
        // children. This also reorders the existing prefixes so that they 
        // appear in order of their prefixID
            // This will get an inverse of this map
        Map<String,Integer> inverseMap = new LinkedHashMap<>();
            // Go through the entries in this map
        for (Map.Entry<Integer, String> entry : entrySet())
            inverseMap.put(entry.getValue(), entry.getKey());
            // A set to get the nodes containing the existing prefixes, in order 
            // of their prefixIDs
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
            }   // If the node contains only children that allow children or 
                // only children that do not allow children
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
     * {@inheritDoc }
     */
    @Override
    public DefaultMutableTreeNode createPrefixTree(Collection<String> values){
        try{
            return createPrefixTreeSQL(values);
        } catch(SQLException ex){
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * This returns a copy of this map with all the prefixIDs in the given 
     * collection removed. This is mainly used in the {@link createPrefixesFrom} 
     * method to remove the prefixes that were already in this map when it was 
     * called, so as to get the map containing the prefixes that were added.
     * @param existingIDs The collection of prefixIDs to remove from the copy. 
     * This will be the collection of prefixIDs that were already in this map.
     * @return A copy of this map with all the prefixIDs in the given collection 
     * removed.
     * @see createPrefixesFrom
     */
    private TreeMap<Integer,String> getAddedPrefixes(Collection<Integer> existingIDs){
            // Get a copy of this map
        TreeMap<Integer,String> added = new TreeMap<>(this);
            // Remove any prefixID that were already in this map
        added.keySet().removeAll(existingIDs);
        return added;
    }
    /**
     * 
     * @param values
     * @return
     * @throws SQLException 
     */
    protected NavigableMap<Integer,String> createPrefixesFromSQL(Collection<String> values)
            throws SQLException{
            // Make sure the collection is not null
        Objects.requireNonNull(values);
            // Get a copy of the set of prefixIDs that are currently in this map
        Set<Integer> existingIDs = new HashSet<>(keySet());
            // Make sure the initial prefixes are all present and accounted for
        addAllIfAbsent(Arrays.asList(INITIAL_LINK_PREFIXES));
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
            // If there are no values remaining (all of them were in this map as 
        if (sortedValues.isEmpty()) // prefixes)
                // Return any prefixes that were added
            return getAddedPrefixes(existingIDs);
            // Get the threshold for adding a prefix
        int threshold = getConnection().getPrefixThreshold();
            // This gets a tree to use to get the prefixes that will be added to 
            // this map
        DefaultMutableTreeNode prefixRoot = createPrefixTree(sortedValues,
                prefixes,threshold,false);
            // Go through the nodes in the tree in preorder
        Iterator<TreeNode> itr = prefixRoot.preorderEnumeration().asIterator();
            // This maps promising nodes to the amount of links match the prefix 
            // the node represents
        Map<DefaultMutableTreeNode, Integer> prefixNodes =new LinkedHashMap<>();
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
                    // If the current node is at level 2 or greater (skips the 
                    // root and any prefixes already in this map) and the 
                    // current node's child count passes the threshold
                if (node.getLevel() > 1 && children >= threshold)
                    prefixNodes.put(node, children);
            }
        }   // Remove any nodes that are null, have a null user object, or have 
            // a prefix that is already in this map
        prefixNodes.keySet().removeIf((DefaultMutableTreeNode t) -> 
                t == null || t.getUserObject() == null || prefixes.contains(t.toString()));
            // Go through the prefix nodes. This uses a copy of the key set 
            // since the prefix nodes map will be altered by this loop, which 
            // would create issues if we attempted to modify it while iterating 
            // through it. Currently the nodes are mapped to their child count, 
            // which may include nodes that represent other prefixes. We only 
            // want the number of leaves a node has.
        for (DefaultMutableTreeNode node : new LinkedHashSet<>(prefixNodes.keySet())){
                // If the current node's parent is a DefaultMutableTreeNode
            if (node.getParent() instanceof DefaultMutableTreeNode){
                    // Get the current node's parent node
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) 
                        node.getParent();
                    // If the prefix nodes map contains the parent node
                if (prefixNodes.containsKey(parent))
                        // This node does not count towards its parent's child 
                        // count, as this is a prefix and not a link
                    prefixNodes.put(parent, prefixNodes.get(parent)-1);
            }
        }   // Go through the entries in the promising nodes map
        for (Map.Entry<DefaultMutableTreeNode, Integer> entry : prefixNodes.entrySet()){
                // If the node still has enough links to hit the prefix 
            if (entry.getValue() >= threshold)  // threshold
                    // Add the node's prefix to this map
                addIfAbsent(entry.getKey().toString());
        }   // Return any prefixes that were added
        return getAddedPrefixes(existingIDs);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public NavigableMap<Integer,String> createPrefixesFrom(Collection<String> values) {
        try{
            return createPrefixesFromSQL(values);
        } catch(SQLException ex){
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * 
     * @param values The set of values to use to generate any new prefixes. This 
     * set will be altered by this method.
     * @param existingPrefixes A set containing the prefixes already in this 
     * map.
     * @param threshold The threshold for adding a prefix.
     * @param keepAll Whether all of the existing prefixes should appear in the 
     * tree. When false, any existing prefixes that do not match enough values 
     * to pass the threshold will not appear in the tree, along with the values 
     * they match. This is mainly for performance, since these nodes are not 
     * needed when creating the tree used to generate the prefixes to be added.
     * @return The root of the tree 
     * @throws SQLException 
     * @see #createMatchMap(TreeSet, int, char[]) 
     * @see #addLeavesToTree(TreeSet, DefaultMutableTreeNode) 
     * @see #createPrefixTree(Map, DefaultMutableTreeNode, int, char[]) 
     * @see #getPrefixSeparators() 
     * @see #getPrefixThreshold() 
     * @see #createPrefixesFrom(Collection) 
     * @see #createPrefixTree(Collection) 
     */
    protected DefaultMutableTreeNode createPrefixTree(TreeSet<String>values, 
            Set<String> existingPrefixes, int threshold, boolean keepAll) 
            throws SQLException{
            // This creates a map mapping existing prefixes to the sets of 
            // values matching those prefixes. This map must sort the prefixes 
            // from longest to shortest.
        TreeMap<String, TreeSet<String>> matchMap = new TreeMap<>(LONG_LENGTH_COMPARATOR);
            // Go through the existing prefixes
        for (String prefix : existingPrefixes){
                // Add a set to contain the values that match the current prefix
            matchMap.put(prefix, new TreeSet<>());
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
                        // values that remain after this loop can be assumed to 
                        // match the empty prefix)
                    valueItr.remove();
                    break;
                }
            }
        }   // Add any remaining values to the set for the empty prefix
        matchMap.get("").addAll(values);
            // If the non-promising sets should be removed
        if (!keepAll)
                // Remove any set (and prefix) for which the set is smaller than 
                // the threshold.
            matchMap.values().removeIf((TreeSet<String> t) -> 
                    t.size() < threshold);
            // This is the root of the tree
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
            // Populate the tree
        createPrefixTree(matchMap,root,threshold,
                getConnection().getPrefixSeparators().toCharArray());
        return root;
    }
    /**
     * 
     * @param matchMap A map that maps prefixes to sets containing values 
     * matching those prefixes.
     * @param parentNode The parent node for the nodes that this method will be 
     * creating.
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
                // If the entry's set does not contain enough elements to hit 
                // the threshold and the parent node is not the root
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
                        // Remove the set with no shared prefixes (if there is 
                        // one, since these are all leaves anyway
                    TreeSet<String> leaves = map.remove("");
                        // If there are any leaves
                    if (leaves != null)
                            // Add them to the node for the prefix
                        addLeavesToTree(leaves,node);
                        // If there aren't any leaves, the map contains only one 
                        // set, the parent node is not the root, and the only 
                        // set in the map is the same as the entry's set
                    else if (map.size() == 1 && !parentNode.isRoot() && 
                            values.equals(map.firstEntry().getValue())){
                        // If this is the case, then all the values in the set 
                        // share a longer prefix than what was originally found. 
                        // As such, we need to create the match map again, but 
                        // this time with a longer prefix
                            // Use the first (and only) key in the map for the 
                            // prefix this time
                        prefix = map.firstKey();
                            // Set the map to null to indicate it needs to be 
                            // remade with the new longer prefix
                        map = null;
                    }
                }   // While this prefix's match map is null (i.e. there is a 
                    // longer prefix that can be used)
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
     * @return The first index of a separator character in the given String, or 
     * -1 if none are found.
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
            // While the set of indexes is not empty (at least one character is 
            // found) (this is mainly to prevent unboxing any nulls)
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
     * This returns a map which maps any prefixes found within the given set of 
     * values to a set containing the values matching those prefixes. In other 
     * words, this method finds any prefixes in the given set of values, splits 
     * the set up, and maps the separated sets to the prefixes each value in a 
     * set has in common. Any value for which no matching prefix was found for 
     * will be stored in a set mapped to an empty String. Prefixes are found by 
     * looking for the first instance of a separator character in a String, 
     * starting at the given index.
     * @param values The values to search for prefixes in and to split up into 
     * subsets based off the prefixes they match.
     * @param fromIndex The index to start searching for separator characters in 
     * the Strings at.
     * @param separators The array of separator characters to use to get 
     * prefixes.
     * @return A map mapping prefixes found to the values that they match.
     * @see indexOfSeparator
     * @see createPrefixTree(Map, DefaultMutableTreeNode, int, char[]) 
     * @see #getPrefixSeparators() 
     * @see #createPrefixesFrom(Collection) 
     * @see #createPrefixTree(Collection) 
     */
    private TreeMap<String, TreeSet<String>> createMatchMap(TreeSet<String> values, 
            int fromIndex, char[] separators){
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
     * This adds the values in the given set to the given node as leaves that do 
     * not allow children.
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
     * {@inheritDoc }
     */
    @Override
    public Map<String, Map.Entry<Integer, String>>getLongestPrefixesFor(
            Collection<? extends String> values){
            // If the given collection is empty
        if (values.isEmpty())
            return new LinkedHashMap<>();
            // If the given collection is not a set
        if (!(values instanceof Set))
                // Turn it into a set
            values = new LinkedHashSet<>(values);
            // This will get the entries for the longest matching prefixes for 
            // the values
        LinkedHashMap<String, Map.Entry<Integer, String>> prefixes = 
                new LinkedHashMap<>();
            // Create a copy of this prefix map
        TreeMap<Integer,String> prefixMap = new TreeMap<>(this);
            // Go through the values in the collection
        for (String value : values){
                // This will get the longest matching prefix
            Map.Entry<Integer, String> prefix = null;
                // Go through the prefixes
            for (Map.Entry<Integer, String> entry : prefixMap.entrySet()){
                    // If the current value starts with the current prefix
                if (value.startsWith(entry.getValue())){
                        // If the longest prefix is null or shorter than the 
                        // current prefix
                    if (prefix == null || prefix.getValue().length() < entry.getValue().length())
                        prefix = entry;
                }
            }   // If there was no longest matchin prefix
            if (prefix == null)
                prefix = getEmptyPrefixEntry();
            prefixes.put(value, prefix);
        }
        return prefixes;
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
         * expression {@code prefixes.get(key) + get(key)} should result in this 
         * String.
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
                        // Get the suffix of the value after the prefix is 
                        // removed from it
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
                        // Remove the prefix from the parent map and return the 
                        // suffix
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
