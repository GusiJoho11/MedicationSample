package com.websarva.wings.android.medicationsample.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.websarva.wings.android.medicationsample.AppDatabase;
import com.websarva.wings.android.medicationsample.HealthCare;
import com.websarva.wings.android.medicationsample.HealthCareDao;
import com.websarva.wings.android.medicationsample.MainActivity;
import com.websarva.wings.android.medicationsample.R;
import com.websarva.wings.android.medicationsample.databinding.FragmentDashboardBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private AppDatabase db;
    private HealthCareDao healthCareDao;
    private FragmentDashboardBinding binding;
    //入力欄を作成
    private EditText healthcareTemperatureInput;
    private EditText healthcarePressureUpInput;
    private EditText healthcarePressureDownInput;
    private EditText healthcareWeightInput;
    private EditText healthcareSugerInput;
    private TextView healthcareList;


    @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            db = AppDatabase.getDatabase(requireContext());
            healthCareDao = db.healthCareDao();
        }

    @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            DashboardViewModel dashboardViewModel =
                    new ViewModelProvider(this).get(DashboardViewModel.class);

            //bindingインフレーターを使って紐づけされている
            binding = FragmentDashboardBinding.inflate(inflater, container, false);
            View root = binding.getRoot();

            //ここでデータを表示する処理を行う
            displayHealthCares();
            return root;
    }

    @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // HealthCareのUI要素の取得
            healthcareTemperatureInput = view.findViewById(R.id.healthcare_temperature);
            healthcarePressureUpInput = view.findViewById(R.id.healthcare_pressure_up);
            healthcarePressureDownInput = view.findViewById(R.id.healthcare_pressure_down);
            healthcareWeightInput = view.findViewById(R.id.healthcare_weight);
            healthcareSugerInput = view.findViewById(R.id.healthcare_sugar);
            Button hc_saveButton = view.findViewById(R.id.healthcare_save_button);
            healthcareList = view.findViewById(R.id.healthcare_list);

            // 体調データベースとDAOの取得
            AppDatabase db = AppDatabase.getDatabase(getActivity());
            healthCareDao = db.healthCareDao();

            // 健康管理の保存ボタンのクリックリスナー
            hc_saveButton.setOnClickListener(v -> saveHealthCare());

            // 保存された健康管理のリストを表示
            displayHealthCares();

        }

    @Override
        public void onDestroyView() {
            super.onDestroyView();
            binding = null;
        }


        private void saveHealthCare() {
            try {
                //　体温、血圧、体重、血糖値の入力値を取得
                double temperature = Double.parseDouble(healthcareTemperatureInput.getText().toString());
                int pressureUp = Integer.parseInt(healthcarePressureUpInput.getText().toString());
                int pressureDown = Integer.parseInt(healthcarePressureDownInput.getText().toString());
                double weight = Double.parseDouble(healthcareWeightInput.getText().toString());
                int sugar = Integer.parseInt(healthcareSugerInput.getText().toString());

                // HealthCare オブジェクトを作成して保存
                HealthCare healthcare = new HealthCare();
                healthcare.entrydate = new Date();
                healthcare.temperature = temperature;
                healthcare.pressureUp = pressureUp;
                healthcare.pressureDown = pressureDown;
                healthcare.weight = weight;
                healthcare.sugar = sugar;


                // データベースに健康管理情報を挿入（バックグラウンドスレッドで処理）
                new Thread(() -> {
                    healthCareDao.insertHealthCare(healthcare);
                }).start();
            } catch (NumberFormatException e) {
                // 無効な入力のハンドリング
                if (getActivity() != null) {
                    Toast.makeText(requireContext(),"無効な入力があります。全てのフィールドに正しい値を入力してください。",Toast.LENGTH_LONG).show();
                }
            }
        }

    private void displayHealthCares() {         //ヘルスケアのデータを取得し画面に表示
        // データベースからすべての健康管理情報を取得（バックグラウンドスレッド）
        new Thread(() -> {
            List<HealthCare> healthCare = healthCareDao.getAllHealthCare();
            StringBuilder displayText = new StringBuilder();
            // 日付フォーマットを指定（YYYY/MM/DD形式）
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

            for (HealthCare healthcare : healthCare) {

                // 日付のフォーマットを適用
                String formattedDate = healthcare.entrydate != null ? dateFormat.format(healthcare.entrydate) : "日時不明";

                displayText.append("体調: ").append(healthcare.temperature)
                        .append(", 血圧(上): ").append(healthcare.pressureUp).append("\n")
                        .append(", 血圧(下): ").append(healthcare.pressureDown).append("\n")
                        .append(", 体重: ").append(healthcare.weight).append("\n")
                        .append(", 血糖値: ").append(healthcare.sugar).append("\n")
                        .append("登録日時: ").append(formattedDate).append("\n\n");
            }
            // メインスレッドで表示を更新
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> healthcareList.setText(displayText.toString()));
            }
        }).start();
    }
}