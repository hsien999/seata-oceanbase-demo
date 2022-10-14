// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Iterator;
import java.sql.SQLException;
import java.util.List;

public class StandardLoadBalanceExceptionChecker implements LoadBalanceExceptionChecker
{
    private List<String> sqlStateList;
    private List<Class<?>> sqlExClassList;
    
    @Override
    public boolean shouldExceptionTriggerFailover(final SQLException ex) {
        final String sqlState = ex.getSQLState();
        if (sqlState != null) {
            if (sqlState.startsWith("08")) {
                return true;
            }
            if (this.sqlStateList != null) {
                final Iterator<String> i = this.sqlStateList.iterator();
                while (i.hasNext()) {
                    if (sqlState.startsWith(i.next().toString())) {
                        return true;
                    }
                }
            }
        }
        if (ex instanceof CommunicationsException) {
            return true;
        }
        if (this.sqlExClassList != null) {
            final Iterator<Class<?>> j = this.sqlExClassList.iterator();
            while (j.hasNext()) {
                if (j.next().isInstance(ex)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public void destroy() {
    }
    
    @Override
    public void init(final Connection conn, final Properties props) throws SQLException {
        this.configureSQLStateList(props.getProperty("loadBalanceSQLStateFailover", null));
        this.configureSQLExceptionSubclassList(props.getProperty("loadBalanceSQLExceptionSubclassFailover", null));
    }
    
    private void configureSQLStateList(final String sqlStates) {
        if (sqlStates == null || "".equals(sqlStates)) {
            return;
        }
        final List<String> states = StringUtils.split(sqlStates, ",", true);
        final List<String> newStates = new ArrayList<String>();
        for (final String state : states) {
            if (state.length() > 0) {
                newStates.add(state);
            }
        }
        if (newStates.size() > 0) {
            this.sqlStateList = newStates;
        }
    }
    
    private void configureSQLExceptionSubclassList(final String sqlExClasses) {
        if (sqlExClasses == null || "".equals(sqlExClasses)) {
            return;
        }
        final List<String> classes = StringUtils.split(sqlExClasses, ",", true);
        final List<Class<?>> newClasses = new ArrayList<Class<?>>();
        for (final String exClass : classes) {
            try {
                final Class<?> c = Class.forName(exClass);
                newClasses.add(c);
            }
            catch (Exception ex) {}
        }
        if (newClasses.size() > 0) {
            this.sqlExClassList = newClasses;
        }
    }
}
