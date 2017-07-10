package com.raidpe;

import cn.nukkit.plugin.Plugin;
import cn.nukkit.scheduler.PluginTask;

import java.sql.Connection;
import java.sql.SQLException;

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
                DBProvider.close(conn, false);
                Thread.sleep(getOwner().getServer().getPropertyInt("pool.sleep-time", 1000));
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
}
