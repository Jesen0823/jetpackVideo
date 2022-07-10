package com.jesen.cod.libnetwork.cache;



import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

// 数据相关联：User表中id列和Cache表中key列相关联, onDelete&onUpdate表示操作相关联
// indices @Index 复合查询，可以加快查询操作，但是会降低insert和update操作速度
/*@Entity(tableName = "cache", foreignKeys =
        {@ForeignKey(entity = User.class, parentColumns = "id", childColumns = "key",
                onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.SET_DEFAULT)}
                ,indices = {@Index(value = {"key", "id"})})*/
// ForeignKey将表cache的id与user表的key字段关联，关联后的删除，升级，user表应该执行什么动作
@Entity(tableName = "cache"/*, foreignKeys = {@ForeignKey(entity = User.class,parentColumns = "id"
        ,childColumns = "key",onDelete = ForeignKey.RESTRICT,onUpdate = ForeignKey.SET_DEFAULT)}*/)
public class Cache implements Serializable {

    //PrimaryKey 必须要有,且不为空,autoGenerate 主键的值是否由Room自动生成,默认false
    @PrimaryKey(autoGenerate = false)
    //@Ignore // Ignore会忽略该字段，不会出现在创建的数据库列名中
    @NonNull
    public String key;

    // 自增主键
   /* @PrimaryKey(autoGenerate = true)
    public int _id;*/

    // 指定数据库表中映射的列的名称
    //@ColumnInfo(name = "_data")
    public byte[] data;

    // 关联查询， Cache表和User表关联，关联字段“id”， 需要关联查询的字段name
    /*@Relation(entity = User.class,parentColumn = "id",entityColumn = "id",projection = {"name"})
    public User mUser;*/

    // 类型转换， 存储到数据库的时候会转换成date2Long, 从数据库读取时会转换成long2Date
   /* @TypeConverters(value = {DataConvert.class})
    public Date mDate;*/
}
