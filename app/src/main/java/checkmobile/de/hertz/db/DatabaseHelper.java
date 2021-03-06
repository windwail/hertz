package checkmobile.de.hertz.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import checkmobile.de.hertz.entity.Image;
import checkmobile.de.hertz.entity.Process;
import checkmobile.de.hertz.entity.ProcessGroup;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    // name of the database file for your application -- change to something appropriate for your app
    private static final String DATABASE_NAME = "helloAndroid.db";
    // any time you make changes to your database objects, you may have to increase the database version
    private static final int DATABASE_VERSION = 77;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private RuntimeExceptionDao processDAO = null;
    private RuntimeExceptionDao processGroupDAO = null;
    private RuntimeExceptionDao imageDAO = null;
    private RuntimeExceptionDao infleetStartDAO = null;

    /**
     * This is called when the database is first created. Usually you should call createTable statements here to create
     * the tables that will store your data.
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, ProcessGroup.class);
            TableUtils.createTable(connectionSource, Process.class);
            TableUtils.createTable(connectionSource, Image.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
     * the various data to match the new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onUpgrade");
            TableUtils.dropTable(connectionSource, Image.class, true);
            TableUtils.dropTable(connectionSource, Process.class, true);
            TableUtils.dropTable(connectionSource, ProcessGroup.class, true);
            // after we drop the old databases, we create the new ones
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }


    public RuntimeExceptionDao getProcessGroupDAO(){
        if(processGroupDAO == null){
            processGroupDAO = getRuntimeExceptionDao(ProcessGroup.class);
        }
        return processGroupDAO;

    }

    public RuntimeExceptionDao getProcessDAO(){
        if(processDAO == null){
            processDAO = getRuntimeExceptionDao(Process.class);
        }
        return processDAO;

    }

    public RuntimeExceptionDao getImageDAO(){
        if(imageDAO == null){
            imageDAO = getRuntimeExceptionDao(Image.class);
        }
        return imageDAO;
    }

    public int queryLast() {
        QueryBuilder<ProcessGroup, Integer> qb = getProcessGroupDAO().queryBuilder().selectRaw("max(id)");
        String[] columns = new String[0];
        try {
            GenericRawResults<String[]> results = getProcessGroupDAO().queryRaw(qb.prepareStatementString());
            columns = results.getFirstResult();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (columns.length == 0) {
            // NOTE: there are not any rows in table
            return 0;
        }
        return Integer.parseInt(columns[0]);
    }

}