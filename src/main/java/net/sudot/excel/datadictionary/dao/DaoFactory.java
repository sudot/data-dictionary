package net.sudot.excel.datadictionary.dao;

import net.sudot.excel.datadictionary.annotation.MeteData;
import net.sudot.excel.datadictionary.utils.ComponentScan;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * dao实例化工厂
 *
 * @author tangjialin on 2020-09-11.
 */
public class DaoFactory {
    private static final Map<String, Class<? extends IDao>> CLASS_HASH_MAP = new HashMap<>();

    static {
        Set<Class<?>> classes = ComponentScan.getClasses(IDao.class.getPackage().getName());
        for (Class<?> aClass : classes) {
            @SuppressWarnings("unchecked")
            Class<? extends IDao> c = (Class<? extends IDao>) aClass;
            if (!IDao.class.isAssignableFrom(c)) { continue; }
            MeteData meteData = c.getAnnotation(MeteData.class);
            if (meteData == null) { continue; }
            CLASS_HASH_MAP.put(meteData.databaseProductName().toLowerCase(), c);
        }
    }

    public static IDao newInstance(Connection connection, Set<String> excludeTables) throws SQLException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String databaseProductName = connection.getMetaData().getDatabaseProductName().toLowerCase();
        Class<? extends IDao> aClass = CLASS_HASH_MAP.get(databaseProductName);
        if (aClass == null) {
            throw new IllegalAccessException("未找到与" + databaseProductName + "匹配的处理类");
        }
        return aClass.getConstructor(Connection.class, Set.class).newInstance(connection, excludeTables);
    }
}
