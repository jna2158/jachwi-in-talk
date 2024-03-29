package com.example.project_test.Modify;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.project_test.Api;
import com.example.project_test.MySpinnerAdapter;
import com.example.project_test.R;
import com.example.project_test.Write;
import com.example.project_test.Writing.MeetWritingActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeetModifyActivity extends AppCompatActivity {
    Toolbar toolbar;
    Button writing;
    EditText tedit, cedit, nedit, wedit, dedit;
    TextView tv0;
    String post_title, post_con, meet_lct, meet_day, meet_tag;
    int post_code, meet_p;

    private AlertDialog dialog;

    Spinner spinner;

    Calendar calendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener datepicker = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel(R.id.dedit);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_meet);

        writing = findViewById(R.id.writing);
        tedit = findViewById(R.id.tedit);
        cedit = findViewById(R.id.cedit);
        wedit = findViewById(R.id.wedit);
        nedit = findViewById(R.id.nedit);
        dedit = findViewById(R.id.dedit);
        dedit.setFocusable(false);
        tv0 = findViewById(R.id.tv0);

        spinner = findViewById(R.id.meetSpinner);

        //분류 가져오기
        final ArrayList<String> items = new ArrayList<>();

        Api api = Api.Factory.INSTANCE.create();
        api.getModifyCategory(33).enqueue(new Callback<ModifyCategoryData>() {
            @Override
            public void onResponse(Call<ModifyCategoryData> call, Response<ModifyCategoryData> response) {
                Log.i("abced","성공");

                ModifyCategoryData cgList = response.body();
                List<Category> cg = cgList.items;

                //리스트에 넣기
                for (Category s:cg) {
                    items.add(s.tag);
                }
                items.add("선택");

                MySpinnerAdapter adapter = new MySpinnerAdapter(MeetModifyActivity.this, android.R.layout.simple_spinner_item,items);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                spinner.setSelection(adapter.getCount());

                int cnt=0;
                for (String i:items) {
                    if(i.equals(meet_tag)) {
                        spinner.setSelection(cnt);
                    }
                    cnt++;
                }
            }

            @Override
            public void onFailure(Call<ModifyCategoryData> call, Throwable t) {
                Log.i("abced",t.getMessage());
            }
        });

        //상단탭
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // ↓툴바의 홈버튼의 이미지를 변경(기본 이미지는 뒤로가기 화살표)
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.backbtn);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        tv0.setText("수정하기");

        //글 제목, 내용
        Intent intent = getIntent();
        post_title = intent.getStringExtra("제목");
        post_con = intent.getStringExtra("내용");
        meet_tag = intent.getStringExtra("태그");
        meet_lct = intent.getStringExtra("위치");
        meet_p = intent.getIntExtra("인원", 0);
        meet_day = intent.getStringExtra("날짜");
        post_code = intent.getIntExtra("게시글코드", 0);

        //set
        tedit.setText(post_title);
        cedit.setText(post_con);
        nedit.setText(meet_p+"");
        wedit.setText(meet_lct);
        dedit.setText(meet_day);

        //달력 날짜 선택
        dedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MeetModifyActivity.this, datepicker, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //글쓰기 _올리기
        int request = getIntent().getIntExtra("request", -1);
        Log.i("modifyrequest", String.valueOf(request));
        switch (request) {
//            case 0: AlertDialog.Builder builder = new AlertDialog.Builder(RecipeModifyActivity.this);
//                dialog = builder.setMessage("수정 오류").setNegativeButton("확인", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        finish();
//                    }
//                }).create();
//                dialog.show(); break;
            case 1000:
        writing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                post_title = tedit.getText().toString();
                post_con = cedit.getText().toString();
                meet_lct = wedit.getText().toString();
                String p = nedit.getText().toString();
                meet_tag = spinner.getSelectedItem().toString();
                meet_day = dedit.getText().toString();


                if (post_title.equals("") || post_con.equals("") || meet_lct.equals("") || p.equals("") || meet_day.equals("") || meet_tag.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MeetModifyActivity.this);
                    dialog = builder.setMessage("글 작성이 완료되지 않았습니다.").setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                } else {
                    meet_p = Integer.parseInt(p);

                    final Api api = Api.Factory.INSTANCE.create();

                    api.Modify(post_title, post_con, post_code).enqueue(new Callback<Write>() {
                        public void onResponse(Call<Write> call, Response<Write> response) {

                            Write write = response.body();

                            api.ModifyMeet(meet_tag, meet_lct, meet_p, meet_day, post_code).enqueue(new Callback<Write>() {
                                @Override
                                public void onResponse(Call<Write> call, Response<Write> response) {
                                    Write write = response.body();

                                    AlertDialog.Builder builder = new AlertDialog.Builder(MeetModifyActivity.this);
                                    dialog = builder.setMessage("수정 완료됨").setNegativeButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            returnResult();
                                            finish();
                                        }
                                    })
                                            .create();
                                    dialog.show();
                                }

                                @Override
                                public void onFailure(Call<Write> call, Throwable t) {
                                    Log.i("수정실패", t.getMessage());
                                }
                            });
                        }

                        public void onFailure(Call<Write> call, Throwable t) {
                            Log.i("수정실패", t.getMessage());
                        }

                    });
                }

            }
        });break;}

    }

    private void returnResult() {
        post_title = tedit.getText().toString();
        post_con = cedit.getText().toString();
        meet_lct = wedit.getText().toString();
        String p = nedit.getText().toString();
        meet_p = Integer.parseInt(p);

        meet_tag = spinner.getSelectedItem().toString();

        Intent intent = new Intent();
        intent.putExtra("title", post_title);
        intent.putExtra("con", post_con);
        intent.putExtra("tag", meet_tag);
        intent.putExtra("lct", meet_lct);
        intent.putExtra("pnum", meet_p);

        setResult(RESULT_OK, intent);
        Log.i("meetmodifyact", "수정");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                AlertDialog.Builder logout = new AlertDialog.Builder(MeetModifyActivity.this);
                logout.setTitle("수정취소");
                logout.setMessage("작성을 취소하시겠습니까?");
                logout.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                logout.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                logout.show();

                return true;
        }
        return true;

    }

    private void updateLabel(int id) {
        String myFormat = "yyyy-MM-dd";  //출력형식
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.KOREA);

        EditText edit = findViewById(id);
        edit.setText(sdf.format(calendar.getTime()));
    }
}
