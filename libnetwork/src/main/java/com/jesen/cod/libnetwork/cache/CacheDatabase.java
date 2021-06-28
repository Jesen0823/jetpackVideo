package com.jesen.cod.libnetwork.cache;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.jesen.cod.libcommon.JetAppGlobal;

import org.jetbrains.annotations.NotNull;


/*
exportSchema = true会生成数据库的json文件，生成路径在gradle文件中指定:
javaCompileOptions{
        annotationProcessorOptions{
        arguments=["room.schemaLocation":"$projectDir/schema".toString()]
        }
     }
*/

@Database(entities = {Cache.class}, version = 1, exportSchema = true)
//数据读取、存储时数据转换器,比如将写入时将Date转换成Long存储，读取时把Long转换Date返回
//@TypeConverters(DateConverter.class)
public abstract class CacheDatabase extends RoomDatabase {

    private static final CacheDatabase DATABASE;

    static {

        // 创建一个内存数据库，数据只存在于内存中，进程被杀数据会丢失
        //Room.inMemoryDatabaseBuilder();

        DATABASE = Room.databaseBuilder(JetAppGlobal.getApplication(), CacheDatabase.class, "jetpackVideo_cache")
                .allowMainThreadQueries() // 是否允许在主线程进行查询
                // 数据库创建和打开后的回调
                //.addCallback()
                //设置查询数据的线程池
                //.setQueryExecutor()
                // room的日志模式
                //.setJournalMode()
                // 数据库升级异常之后回滚重新创建数据库
                //.fallbackToDestructiveMigration()
                // 数据升级异常根据指定版本进行回滚
                //.fallbackToDestructiveMigrationFrom()
                // 数据库升级的入口
                //.addMigrations(CacheDatabase.mMigration)
                .build();


    }

    public static CacheDatabase get() {
        return DATABASE;
    }

    static Migration mMigration = new Migration(1, 3) {
        @Override
        public void migrate(@NonNull @NotNull SupportSQLiteDatabase database) {
            database.execSQL("alter table teacher rename to student");
            database.execSQL("alter table teacher add column teacher_old INTEGER NOT NULL default 0");
        }
    };

    public abstract CacheDao getCache();
}
