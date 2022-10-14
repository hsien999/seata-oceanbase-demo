// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.osgi;

import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.scheduler.SchedulerServiceProviderHolder;
import java.sql.DriverManager;
import java.util.Dictionary;
import com.oceanbase.jdbc.internal.util.constant.Version;
import com.oceanbase.jdbc.Driver;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.BundleActivator;

public class OceanBaseActivator implements BundleActivator
{
    private ServiceRegistration<DataSourceFactory> service;
    
    public void start(final BundleContext context) throws Exception {
        final Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("osgi.jdbc.driver.class", Driver.class.getName());
        properties.put("osgi.jdbc.driver.name", "OceanBase Connector/J");
        properties.put("osgi.jdbc.driver.version", Version.version);
        this.service = (ServiceRegistration<DataSourceFactory>)context.registerService((Class)DataSourceFactory.class, (Object)new OceanBaseDataSourceFactory(), (Dictionary)properties);
    }
    
    public void stop(final BundleContext context) throws Exception {
        if (this.service != null) {
            this.service.unregister();
            this.service = null;
        }
        try {
            DriverManager.getDriver("jdbc:oceanbase:");
            SchedulerServiceProviderHolder.close();
        }
        catch (SQLException ex) {}
    }
}
