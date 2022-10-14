// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.util;

import java.sql.SQLException;
import java.sql.ResultSetMetaData;
import java.sql.ResultSet;

public class ResultSetUtil
{
    public static StringBuilder appendResultSetSlashGStyle(final StringBuilder appendTo, final ResultSet rs) throws SQLException {
        final ResultSetMetaData rsmd = rs.getMetaData();
        final int numFields = rsmd.getColumnCount();
        int maxWidth = 0;
        final String[] fieldNames = new String[numFields];
        for (int i = 0; i < numFields; ++i) {
            fieldNames[i] = rsmd.getColumnLabel(i + 1);
            if (fieldNames[i].length() > maxWidth) {
                maxWidth = fieldNames[i].length();
            }
        }
        int rowCount = 1;
        while (rs.next()) {
            appendTo.append("*************************** ");
            appendTo.append(rowCount++);
            appendTo.append(". row ***************************\n");
            for (int j = 0; j < numFields; ++j) {
                for (int leftPad = maxWidth - fieldNames[j].length(), k = 0; k < leftPad; ++k) {
                    appendTo.append(" ");
                }
                appendTo.append(fieldNames[j]);
                appendTo.append(": ");
                final String stringVal = rs.getString(j + 1);
                if (stringVal != null) {
                    appendTo.append(stringVal);
                }
                else {
                    appendTo.append("NULL");
                }
                appendTo.append("\n");
            }
            appendTo.append("\n");
        }
        return appendTo;
    }
}
