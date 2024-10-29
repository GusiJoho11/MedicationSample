package com.websarva.wings.android.medicationsample.ui.notifications;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.websarva.wings.android.medicationsample.AppDatabase;
import com.websarva.wings.android.medicationsample.HealthCare;
import com.websarva.wings.android.medicationsample.HealthCareDao;
import com.websarva.wings.android.medicationsample.Medication;
import com.websarva.wings.android.medicationsample.MedicationDao;
import com.websarva.wings.android.medicationsample.R;
import com.websarva.wings.android.medicationsample.databinding.FragmentNotificationsBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationsFragment extends Fragment {

    private AppDatabase db;
    private MedicationDao medicationDao;
    private FragmentNotificationsBinding binding;
    //入力欄の宣言
    private EditText medicationNameInput;
    private EditText medicationDosageInput;
    private EditText medicationFrequencyInput;
    private EditText medicationStartDateInput;
    private EditText medicationEndDateInput;
    private EditText medicationMemoInput;
    private Switch medicationReminderInput;
    private TextView medicationList;
    //足りない項目（錠・包）

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = AppDatabase.getDatabase(requireContext());
        medicationDao = db.medicationDao();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // ここでデータを表示する処理を行う
        displayMedications();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // MedicationのUI要素の取得
        medicationNameInput = view.findViewById(R.id.medication_name);
        medicationDosageInput = view.findViewById(R.id.medication_dosage);
        medicationFrequencyInput = view.findViewById(R.id.medication_frequency);
        medicationStartDateInput = view.findViewById(R.id.medication_startdate);
        medicationEndDateInput = view.findViewById(R.id.medication_enddate);
        medicationReminderInput = view.findViewById(R.id.medication_reminder);
        medicationMemoInput = view.findViewById(R.id.medication_memo);
        Button saveButton = view.findViewById(R.id.medication_save_button);
        medicationList = view.findViewById(R.id.medication_list);

        //各項目の入力制限
        medicationDosageInput.setFilters(new InputFilter[]{ new medicationDosageFilter() });
        medicationFrequencyInput.setFilters(new InputFilter[]{ new medicationFrequencyFilter() });

        // 薬データベースとDAOの取得
        AppDatabase db = AppDatabase.getDatabase(getActivity());
        medicationDao = db.medicationDao();

        // 服用開始日入力欄をクリックしたときにDatePickerを表示
        medicationStartDateInput.setOnClickListener(v -> showDatePickerDialog(medicationStartDateInput));

        // 服用終了日入力欄をクリックしたときにDatePickerを表示
        medicationEndDateInput.setOnClickListener(v -> showDatePickerDialog(medicationEndDateInput));

        // 薬の保存ボタンのクリックリスナー
        saveButton.setOnClickListener(v -> saveMedication());

        // 保存された薬のリストを表示
        displayMedications();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private long convertDateToTimestamp(String dateStr) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        Date date = dateFormat.parse(dateStr);
        return date != null ? date.getTime() : 0;  // nullの場合は0を返す
    }


    private void saveMedication() {
        try {
            // 名前、服用量,メモの入力値を取得
            String name = medicationNameInput.getText().toString();
            int dosage = Integer.parseInt(medicationDosageInput.getText().toString());
            int frequency = Integer.parseInt(medicationFrequencyInput.getText().toString());
            String memo = medicationMemoInput.getText().toString();

            // 開始日と終了日の文字列を取得
            String startDateStr = medicationStartDateInput.getText().toString();
            String endDateStr = medicationEndDateInput.getText().toString();
            // 日付をタイムスタンプ(long)に変換する
            long startDateLong = convertDateToTimestamp(startDateStr);
            long endDateLong = convertDateToTimestamp(endDateStr);

            // リマインダーのチェック状態を取得
            boolean reminder = medicationReminderInput.isChecked();

            // Medication オブジェクトを作成して保存
            Medication medication = new Medication();
            medication.name = name;
            medication.dosage = dosage;
            medication.frequency = frequency;
            medication.startdate = startDateLong;
            medication.enddate = endDateLong;
            medication.memo = memo;
            medication.reminder = reminder;  // リマインダー設定

        // データベースに薬情報を挿入（バックグラウンドスレッドで処理）
        new Thread(() -> {
            medicationDao.insertMedication(medication);
//            runOnUiThread(this::displayMedications);  // メインスレッドでリストを更新
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::displayMedications);
            }
        }).start();
        } catch (NumberFormatException e) {
            if (getActivity() != null) {
                Toast.makeText(requireContext(), "全ての項目に値を入力してください。", Toast.LENGTH_LONG).show();
            }
        } catch (ParseException e) {
            if (getActivity() != null) {
                Toast.makeText(requireContext(), "日付の形式が正しくありません。", Toast.LENGTH_LONG).show();
            }
        }
    }

    // DatePickerDialogを表示し、選択した日付をEditTextにセットする
    private void showDatePickerDialog(EditText dateInput) {
        // 現在の日付を取得
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // DatePickerDialogを表示
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year1, month1, dayOfMonth) -> {
            // 選択された日付を "yyyy/MM/dd" の形式でEditTextにセット
            String selectedDate = year1 + "/" + (month1 + 1) + "/" + dayOfMonth;
            dateInput.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void displayMedications() {         //薬のデータを取得し画面に表示
        // データベースからすべての薬情報を取得（バックグラウンドスレッド）
        new Thread(() -> {
            List<Medication> medications = medicationDao.getAllMedications();
            StringBuilder displayText = new StringBuilder();
            for (Medication medication : medications) {
                displayText.append("名前: ").append(medication.name)
                        .append(", 服用量: ").append(medication.dosage).append("\n")
                        .append(", 服用回数: ").append(medication.frequency).append("\n")
                        .append(", 服用開始: ").append(medication.startdate).append("\n")
                        .append(", 服用終了: ").append(medication.enddate).append("\n")
                        .append(", メモ: ").append(medication.memo).append("\n")
                        .append(", リマインダー: ").append(medication.reminder).append("\n");


            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> medicationList.setText(displayText.toString()));
            }

        }).start();
    }

    //  InputFilterを使って服用量の入力制限
    public class medicationDosageFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            String newInput = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());

            // 整数部1桁 (例: "2"など)
            if (newInput.matches("^\\d{0,1}")) {
                return null;  // 入力が有効な場合は変更なし
            }
            return "";  // 無効な入力を制限
        }
    }

    //  InputFilterを使って服用回数の入力制限
    public class medicationFrequencyFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            String newInput = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());

            // 整数部1桁 (例: "2"など)
            if (newInput.matches("^\\d{0,1}")) {
                return null;  // 入力が有効な場合は変更なし
            }
            return "";  // 無効な入力を制限
        }
    }
}