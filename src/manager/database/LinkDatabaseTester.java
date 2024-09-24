/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package manager.database;

import java.io.File;
import java.sql.*;
import org.sqlite.*;
import org.sqlite.core.Codes;

/**
 *
 * @author Milo Steier
 */
public interface LinkDatabaseTester {
    
    public static final String TEST_DATABASE_FOLDER = 
            "C:\\Users\\Milo Steier\\OneDrive\\Documents\\Programs\\Java\\My Programs\\LinkManager\\Development Stuff\\links";
    
    public static final String TEST_DATABASE_FILE = 
            "TestLinkDatabase.db";
    
    public static final String[] TEST_DATABASE_FILES = {
        TEST_DATABASE_FILE,
        "LinkManager.db",
        "LinkManager - Copy.db",
        "LinkManager (2).db",
        "LinkManager (2) - Copy.db",
        "LinkManager (3).db",
        "LinkManager (4).db"
    };
    
    public default SQLiteConfig createSQLConfig(){
        SQLiteConfig sqlConfig = new SQLiteConfig();
        sqlConfig.enforceForeignKeys(true);
        return sqlConfig;
    }
    
    public SQLiteConfig getSQLConfig();
    
    public default LinkDatabaseConnection connect(String file) throws SQLException{
        return new LinkDatabaseConnection(file, getSQLConfig());
    }
    public default LinkDatabaseConnection connect(File file) throws SQLException{
        return connect(file.toString());
    }
    public default LinkDatabaseConnection connectToTest(String file) throws SQLException{
        return connect(new File(TEST_DATABASE_FOLDER,file));
    }
    public default LinkDatabaseConnection connect(int dbIndex) throws SQLException{
        return connectToTest(TEST_DATABASE_FILES[dbIndex]);
    }
    public default LinkDatabaseConnection connect() throws SQLException{
        return connect(0);
    }
    
//    public default void createBac
    
    public static void throwException(int excType) throws SQLException{
        switch(excType){
            case(1):
                throw new SQLException();
            case(2):
                throw new SQLException("Constraint Exception",null,Codes.SQLITE_CONSTRAINT);
        }
    }
}
