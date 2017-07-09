package com.raidpe;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBProvider extends cn.nukkit.plugin.PluginBase
{
    static int max_size, min_size;

    private static String username, password, url;

    private static List<Connection> connections = new ArrayList<>();

    private static final String driverClassName = "com.mysql.jdbc.Driver";

    @Override
    public void onLoad()
    {
        try
        {
            Class.forName(driverClassName);

            // Reading settings from server.properties
            username = getServer().getPropertyString("db.username", "root");
            password = getServer().getPropertyString("db.password", "");
            max_size = getServer().getPropertyInt("pool.max-size", 20);
            min_size = getServer().getPropertyInt("pool.min-size", 5);

            url = "jdbc:mysql://"
                    + getServer().getPropertyString("db.server", "localhost") + ":"
                    + getServer().getPropertyString("db.port", "3306") + "/"
                    + getServer().getPropertyString("db.database", "raid") + "?useSSL="
                    + getServer().getPropertyString("db.useSSL", "false");

            while(connections.size() < max_size) connections.add(create());
            getLogger().info("Connections available: " + connections.size());
            return;
        }
        catch(ClassNotFoundException ex)
        {
            ex.printStackTrace();
            getLogger().critical("Couldn't find required driver, shutting down.");
        }
        catch(SQLException ex)
        {
            ex.printStackTrace();
            getLogger().critical("Error while initializing database connections, shutting down.");
        }
        getServer().shutdown();
    }

    @Override
    public void onEnable()
    {
        getServer().getScheduler().scheduleDelayedRepeatingTask(new KeepConnectionTask(this), getServer().getPropertyInt("pool.kc-period", 6000), getServer().getPropertyInt("pool.kc-period", 6000), true);
    }

    public static Connection create() throws SQLException
    {
        return DriverManager.getConnection(url, username, password);
    }

    public static Connection get() throws SQLException
    {
        if(connections.size() <= min_size) return create();
        Connection conn =  connections.remove(0);
        return isConnectionAlive(conn) ? conn : create();
    }

    public static void close(Connection conn)
    {
        close(conn, true);
    }

    public static void close(Connection conn, boolean check)
    {
        try
        {
            if(check && !isConnectionAlive(conn)) return;
            if(connections.size() >= max_size)
            {
                conn.close();
                return;
            }
            connections.add(conn);
        }
        catch(SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    private static boolean isConnectionAlive(Connection conn)
    {
        try
        {
            if(conn == null || conn.isClosed()) return false;
            Statement statement = conn.createStatement();
            statement.executeQuery("SELECT 1");
            statement.close();
            return true;
        }
        catch(SQLException ex)
        {
            return false;
        }
    }
}