package net.sudot.excel.datadictionary.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 原数据注解
 *
 * @author tangjialin on 2020-09-10.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MeteData {
    /** 驱动类名称 */
    String driveClassName();

    /** 数据库产品名称 */
    String databaseProductName();
}
