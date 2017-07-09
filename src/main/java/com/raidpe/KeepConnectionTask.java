package com.raidpe;

import cn.nukkit.plugin.Plugin;
import cn.nukkit.scheduler.PluginTask;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class KeepConnectionTask extends PluginTask
{
    public KeepConnectionTask(Plugin owner)
    {
        super(owner);
    }

    @Override
    public void onRun(int i)
    {
        for(int j = 0; j < DBProvider.max_size; j ++)
        {
            try
            {
                Connection conn = DBProvider.get();
                Statement statement = conn.createStatement();
                statement.executeQuery("SELECT 1");
                statement.close();
                DBProvider.close(conn);
            }
            catch(SQLException ex)
            {
                try
                {
                    DBProvider.close(DBProvider.create());
                }
                catch(SQLException exception)
                {
                    getOwner().getLogger().warning("Error trying to create new connections.");
                    exception.printStackTrace();
                    return;
                }
            }
        }
    }
}
