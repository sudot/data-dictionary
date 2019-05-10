# 数据库字典数据生成工具

### 数据库支持情况
- MySql
- Oracle

### 拉取源码在开发工具运行
1. 拉取项目代码
    ```
    git clone git@github.com:sudot/data-dictionary.git
    ```
2. 修改文件`src/main/resources/in-parameter.txt`相关信息
3. 运行`net.sudot.excel.datadictionary.Appcation`类的`main`方法

### 使用编译包运行
1. [点此下载运行包](https://github.com/sudot/data-dictionary/releases/download/v1.0/data-dictionary-1.0-release.zip)
2. 解压后修改文件`in-parameter.txt`
3. 双击加压后`run.bat`脚本即可生成数据字典

#### 结果预览
![image](images/data-dictionary.gif)

示例表结构
```
DROP TABLE IF EXISTS `table_name_01`;
CREATE TABLE `table_name_01` (
`id`  bigint NOT NULL AUTO_INCREMENT COMMENT '主键' ,
`name`  varchar(255) NULL COMMENT '姓名' ,
`memo`  varchar(255) NULL COMMENT '演示一个有很长很长的备注字段。假设有一个性别字段值和释义的映射为->1：男性，0：女性' ,
PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT='测试表-01'
;

DROP TABLE IF EXISTS `table_name_02`;
CREATE TABLE `table_name_02` (
`id`  bigint NOT NULL AUTO_INCREMENT COMMENT '主键' ,
`name`  varchar(255) NULL COMMENT '姓名' ,
`memo`  varchar(255) NULL COMMENT '演示一个有很长很长的备注字段。假设有一个性别字段值和释义的映射为->1：男性，0：女性' ,
PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT='测试表-02'
;

DROP TABLE IF EXISTS `table_name_03`;
CREATE TABLE `table_name_03` (
`id`  bigint NOT NULL AUTO_INCREMENT COMMENT '主键' ,
`name`  varchar(255) NULL COMMENT '姓名' ,
`memo`  varchar(255) NULL COMMENT '演示一个有很长很长的备注字段。假设有一个性别字段值和释义的映射为->1：男性，0：女性' ,
PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT='测试表-03'
;

DROP TABLE IF EXISTS `table_name_04`;
CREATE TABLE `table_name_04` (
`id`  bigint NOT NULL AUTO_INCREMENT COMMENT '主键' ,
`name`  varchar(255) NULL COMMENT '姓名' ,
`memo`  varchar(255) NULL COMMENT '演示一个有很长很长的备注字段。假设有一个性别字段值和释义的映射为->1：男性，0：女性' ,
PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT='测试表-04'
;
```
