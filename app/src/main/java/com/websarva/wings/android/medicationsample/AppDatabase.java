package com.websarva.wings.android.medicationsample;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

//データベースをアプリ全体で使えるようにする処理
@Database(entities = {Medication.class,HealthCare.class}, version = 5, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract MedicationDao medicationDao();
    public abstract HealthCareDao healthCareDao();

    // シングルトンパターンでデータベースインスタンスを取得
    private static volatile AppDatabase INSTANCE;
//getDatebaseでデータを取得し、データベースが作られていない場合はデータベースを作成、ある場合はそのデータベースを返す処理
public static AppDatabase getDatabase(final Context context){
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")  // 1つのデータベース名に統一
                            .fallbackToDestructiveMigration()  // マイグレーションを行わず、データを破壊
                            .build();

                }
            }
        }
        return INSTANCE;
    }

}
