package com.websarva.wings.android.medicationsample;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Medication {
    @PrimaryKey(autoGenerate = true)
    public int id;             // 自動生成されるID
    public String name;        // 薬の名前
    public int dosage;         // 服用量
    public int frequency;      // 服用頻度
    public long startdate;     // 服用開始日（Unixタイムスタンプ）
    public long enddate;       // 服用終了日（Unixタイムスタンプ）
    public String memo;
    public boolean reminder;   // リマインダー情報
}
