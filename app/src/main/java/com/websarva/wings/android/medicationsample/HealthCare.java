package com.websarva.wings.android.medicationsample;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class HealthCare {
    @PrimaryKey(autoGenerate = true)
    public int id;                  //自動生成されるID
    public Date entrydate;           //登録日
    public double temperature;        //体温
    public int pressureUp;            //血圧（上）
    public int pressureDown;            //血圧（下）
    public double weight;              //体重
    public int sugar;
    //足りない項目（登録日時、入力制限）

    public void setTimestamp(Date timestamp) {
        this.entrydate = timestamp;
    }

}
