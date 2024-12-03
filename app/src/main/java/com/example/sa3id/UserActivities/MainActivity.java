package com.example.sa3id.UserActivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sa3id.Announcement;
import com.example.sa3id.AnnouncementAdapter;
import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    //FirebaseUser firebaseUser;
    FirebaseAuth mAuth;
    TextView tvEmail;
    SharedPreferences announcementsSP, userDetailsSP;
    SharedPreferences.Editor annoucementsEditor, userDetailsEditor;

    ListView announcementsListView;
    ArrayList<Announcement> announcementsList;
    AnnouncementAdapter adapter;

    LinearLayout announcementsButton, calenderButton, uploadMaterialsButton, examsButton, materialsButton, booksButton, contactUsButton, calculatorButton, whatsappButton, donateButton;
    NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences and editor
        announcementsSP = getSharedPreferences("AnnouncementsSP", MODE_PRIVATE);
        annoucementsEditor = announcementsSP.edit();
        userDetailsSP = getSharedPreferences("UserDetailsSP", MODE_PRIVATE);
        userDetailsEditor = userDetailsSP.edit();

        initViews();
        initMenuButtonsListeners();

        saveAnnouncementsToSP();
    }

    private void initViews() {
        tvEmail = findViewById(R.id.tvEmail);

        tvEmail.setText(userDetailsSP.getString("userEmail", "Guest"));
        navigationView = findViewById(R.id.nav_view);

        announcementsListView = findViewById(R.id.announcementsListView);
        announcementsList = new ArrayList<>();

        int abedwRevenge = R.drawable.abedw_revenge;
        int nizarImage = R.drawable.nizar;
        int abedwImage = R.drawable.abedw_skillz;
        int abedwomarImage = R.drawable.abedwomar;

        announcementsList.add(new Announcement("أطمستم هويتي بعد ظلمي!", "هذا ما قاله الشاعر والزمار الكبير عبد الرحمن بعد ما برز شخص ينفخ في اليرغول في اليوم التراثي في مدرسة جت الثانوية، وضح الشاعر والزمار الكبير عبد الرحمن من خلال حديثه ان بعد ظلمه من ناحية التطوع ومن ناحية المهمة التطبيقية وضح ان تم طمس هويته من خلال ظهور الزمار في اليوم تراث، غرد الزمار والشاعر الكبير عبد الرحمن خارج السرب", abedwRevenge));
        announcementsList.add(new Announcement("ظلم في تصليح بجروت علم الحاسوب: طلاب يعانون من إجراءات غير عادلة", "واجه الطالب عمر محمد وتد ظلماً واضحاً في تصليح امتحان بجروت علم الحاسوب، حيث شعر أن درجاته لم تعكس جهوده وأدائه الفعلي في الامتحان. امتحان بجروت علم الحاسوب (رقم 899271) للصف الثاني عشر،  هذه الحالات تسلط الضوء على التحديات التي يواجهها الطلاب في النظام التعليمي وتأثيرها على مستقبلهم الأكاديمي.", abedwomarImage));
        announcementsList.add(new Announcement("عبد الرحمن نمر وتد وظُلمه في المهمة التطبيقية لبجروت المدنيات", "عبد الرحمن نمر وتد، طالب مجتهد تعرض لظلم واضح في المهمة التطبيقية لبجروت المدنيات الداخلي، والتي كان من المفترض أن تكون أسهل من الامتحان الخارجي. تحت إشراف الأستاذ نزار غرة، حصل عبد الرحمن على علامة 85، وهي أقل بكثير من زملائه في المجموعة الذين حصلوا على 95، دون أي مبرر واضح لهذا الفارق. لم يكن عبد الرحمن الوحيد الذي شعر بالظلم، إذ يعاني العديد من الطلاب الآخرين من التمييز في العلامات، في ما يبدو أنه تفريق على أساس المحاباة والانحياز الشخصي.", nizarImage));
        announcementsList.add(new Announcement("الطالب عبد الرحمن نمر وتد يحقق إنجازاً غير مسبوق في بجروت الأدب", "حقق الطالب عبد الرحمن نمر وتد إنجازاً استثنائياً في امتحان بجروت الأدب المصيري، حيث أحرز علامة 95، وهي علامة غير مسبوقة على مستوى المنطقة والمدرسة. يعتبر هذا النجاح تقديراً لجهوده الكبيرة في مواجهة تحديات هذا الموضوع الصعب، ويستحق تكريماً خاصاً نظراً للتفوق والتميز الذي أظهره.", abedwImage));


        adapter = new AnnouncementAdapter(this, 0, 0, announcementsList);
        announcementsListView.setAdapter(adapter);


        announcementsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Toast.makeText(MainActivity.this,"i love shrek", Toast.LENGTH_SHORT).show();
//                DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://sa3idsite-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users");
//                databaseReference.child("ssssss").setValue("shrek");

            }
        });

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Toast.makeText(MainActivity.this, "Signed out", Toast.LENGTH_SHORT);
            }
        });
        tvEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SignIn.class));
                finish();
            }
        });

    }

    private void initMenuButtonsListeners() {
        announcementsButton = findViewById(R.id.arabic_button);
        calenderButton = findViewById(R.id.english_button);
        //examsButton = findViewById(R.id.exams_bank_button);
        materialsButton = findViewById(R.id.materials_page_button);
        booksButton = findViewById(R.id.our_books_button);
        uploadMaterialsButton = findViewById(R.id.upload_materials_button);
        contactUsButton = findViewById(R.id.contact_us_button);
        calculatorButton = findViewById(R.id.grades_calculator_button);
        whatsappButton = findViewById(R.id.whatsapp_groups_button);
        donateButton = findViewById(R.id.donate_button);

        announcementsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationView.getMenu().performIdentifierAction(R.id.nav_annoucements, 0);
            }
        });

        calculatorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigationView.getMenu().performIdentifierAction(R.id.nav_calender, 0);
            }
        });

        uploadMaterialsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationView.getMenu().performIdentifierAction(R.id.nav_upload_materials, 0);
            }
        });

//        examsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                navigationView.getMenu().performIdentifierAction(R.id.nav_exams_bank, 0);
//            }
//        });

        materialsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationView.getMenu().performIdentifierAction(R.id.nav_materials, 0);

            }
        });

        booksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationView.getMenu().performIdentifierAction(R.id.nav_our_books, 0);
            }
        });

        contactUsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationView.getMenu().performIdentifierAction(R.id.nav_contact_us, 0);
            }
        });

        calculatorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationView.getMenu().performIdentifierAction(R.id.nav_grades_calculator, 0);
            }
        });

        whatsappButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationView.getMenu().performIdentifierAction(R.id.nav_whatsapp_groups, 0);
            }
        });

        donateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationView.getMenu().performIdentifierAction(R.id.nav_donate, 0);
            }
        });



    }


    private void saveAnnouncementsToSP() {
        // Store the number of announcements
        annoucementsEditor.putInt("announcementCount", announcementsList.size());

        // Store each announcement individually
        for (int i = 0; i < announcementsList.size(); i++) {
            Announcement announcement = announcementsList.get(i);
            annoucementsEditor.putString("announcement_title_" + i, announcement.getTitle());
            annoucementsEditor.putString("announcement_description_" + i, announcement.getDescription());
            annoucementsEditor.putInt("announcement_image_" + i, announcement.getImageResource());
        }

        annoucementsEditor.apply();
    }


    @Override
    protected void onResume() {
        super.onResume();

        tvEmail.setText(userDetailsSP.getString("userEmail", "Guest"));

        NavigationView navigationView = findViewById(R.id.nav_view);
        for (int i = 0; i < navigationView.getMenu().size(); i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        tvEmail.setText(userDetailsSP.getString("userEmail", "Guest"));

    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

}