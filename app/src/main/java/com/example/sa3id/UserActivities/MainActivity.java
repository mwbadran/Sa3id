package com.example.sa3id.UserActivities;

import static com.example.sa3id.AnnouncementAdapter.openAnnouncementAsActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

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


    SharedPreferences announcementsSP, userDetailsSP;
    SharedPreferences.Editor annoucementsEditor, userDetailsEditor;

    ListView announcementsListView;
    ArrayList<Announcement> announcementsList;
    AnnouncementAdapter adapter;

    LinearLayout announcementsButton, calenderButton, uploadMaterialsButton, examsButton, materialsButton, booksButton, contactUsButton, calculatorButton, whatsappButton, donateButton;
    NavigationView navigationView;
    GridLayout gridLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Initialize SharedPreferences and editor
        announcementsSP = getSharedPreferences("AnnouncementsSP", MODE_PRIVATE);
        annoucementsEditor = announcementsSP.edit();
        userDetailsSP = getSharedPreferences("UserDetailsSP", MODE_PRIVATE);
        userDetailsEditor = userDetailsSP.edit();

        initViews();

        populateGridLayout();

        saveAnnouncementsToSP();
    }

    private void initViews() {

        navigationView = findViewById(R.id.nav_view);
        gridLayout = findViewById(R.id.grid_layout);

        announcementsListView = findViewById(R.id.announcementsListView);
        announcementsList = new ArrayList<>();
        announcementsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                openAnnouncementAsActivity(MainActivity.this, announcementsList.get(i).getTitle(),announcementsList.get(i).getDescription(),announcementsList.get(i).getImageResource());
            }
        });

        int abedwRevenge = R.drawable.abedw_revenge;
        int nizarImage = R.drawable.nizar;
        int abedwImage = R.drawable.abedw_skillz;
        int abedwomarImage = R.drawable.abedwomar;

        //announcementsList.add(new Announcement("أطمستم هويتي بعد ظلمي!", "هذا ما قاله الشاعر والزمار الكبير عبد الرحمن بعد ما برز شخص ينفخ في اليرغول في اليوم التراثي في مدرسة جت الثانوية، وضح الشاعر والزمار الكبير عبد الرحمن من خلال حديثه ان بعد ظلمه من ناحية التطوع ومن ناحية المهمة التطبيقية وضح ان تم طمس هويته من خلال ظهور الزمار في اليوم تراث، غرد الزمار والشاعر الكبير عبد الرحمن خارج السرب", abedwRevenge));
        announcementsList.add(new Announcement("ظلم في تصليح بجروت علم الحاسوب: طلاب يعانون من إجراءات غير عادلة", "واجه الطالب عمر محمد وتد ظلماً واضحاً في تصليح امتحان بجروت علم الحاسوب، حيث شعر أن درجاته لم تعكس جهوده وأدائه الفعلي في الامتحان. امتحان بجروت علم الحاسوب (رقم 899271) للصف الثاني عشر،  هذه الحالات تسلط الضوء على التحديات التي يواجهها الطلاب في النظام التعليمي وتأثيرها على مستقبلهم الأكاديمي.", abedwomarImage));
        //announcementsList.add(new Announcement("عبد الرحمن نمر وتد وظُلمه في المهمة التطبيقية لبجروت المدنيات", "عبد الرحمن نمر وتد، طالب مجتهد تعرض لظلم واضح في المهمة التطبيقية لبجروت المدنيات الداخلي، والتي كان من المفترض أن تكون أسهل من الامتحان الخارجي. تحت إشراف الأستاذ نزار غرة، حصل عبد الرحمن على علامة 85، وهي أقل بكثير من زملائه في المجموعة الذين حصلوا على 95، دون أي مبرر واضح لهذا الفارق. لم يكن عبد الرحمن الوحيد الذي شعر بالظلم، إذ يعاني العديد من الطلاب الآخرين من التمييز في العلامات، في ما يبدو أنه تفريق على أساس المحاباة والانحياز الشخصي.", nizarImage));
        announcementsList.add(new Announcement("الطالب عبد الرحمن نمر وتد يحقق إنجازاً غير مسبوق في بجروت الأدب", "حقق الطالب عبد الرحمن نمر وتد إنجازاً استثنائياً في امتحان بجروت الأدب المصيري، حيث أحرز علامة 95، وهي علامة غير مسبوقة على مستوى المنطقة والمدرسة. يعتبر هذا النجاح تقديراً لجهوده الكبيرة في مواجهة تحديات هذا الموضوع الصعب، ويستحق تكريماً خاصاً نظراً للتفوق والتميز الذي أظهره.", abedwImage));


        //show only latest 2 items
        ArrayList<Announcement> filteredAnnouncements = new ArrayList<>();
        int size = announcementsList.size();
        if (size > 2) {
            filteredAnnouncements.add(announcementsList.get(size - 2));
            filteredAnnouncements.add(announcementsList.get(size - 1));
        } else {
            filteredAnnouncements.addAll(announcementsList);
        }

        adapter = new AnnouncementAdapter(this, 0, 0, filteredAnnouncements);
        announcementsListView.setAdapter(adapter);

        announcementsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Toast.makeText(MainActivity.this,"i love shrek", Toast.LENGTH_SHORT).show();
//                DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://sa3idsite-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users");
//                databaseReference.child("ssssss").setValue("shrek");

            }
        });


    }

    private void initMenuButtonsListeners() {
//        announcementsButton = findViewById(R.id.arabic_button);
//        calenderButton = findViewById(R.id.english_button);
//        //examsButton = findViewById(R.id.exams_bank_button);
//        materialsButton = findViewById(R.id.materials_page_button);
//        booksButton = findViewById(R.id.our_books_button);
//        uploadMaterialsButton = findViewById(R.id.upload_materials_button);
//        contactUsButton = findViewById(R.id.contact_us_button);
//        calculatorButton = findViewById(R.id.grades_calculator_button);
//        whatsappButton = findViewById(R.id.whatsapp_groups_button);
//        donateButton = findViewById(R.id.donate_button);

//        announcementsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                navigationView.getMenu().performIdentifierAction(R.id.nav_annoucements, 0);
//            }
//        });
//
//        calculatorButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                navigationView.getMenu().performIdentifierAction(R.id.nav_calender, 0);
//            }
//        });
//
//        uploadMaterialsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                navigationView.getMenu().performIdentifierAction(R.id.nav_upload_materials, 0);
//            }
//        });
//
////        examsButton.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                navigationView.getMenu().performIdentifierAction(R.id.nav_exams_bank, 0);
////            }
////        });
//
//        materialsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                navigationView.getMenu().performIdentifierAction(R.id.nav_materials, 0);
//
//            }
//        });
//
//        booksButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                navigationView.getMenu().performIdentifierAction(R.id.nav_our_books, 0);
//            }
//        });
//
//        contactUsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                navigationView.getMenu().performIdentifierAction(R.id.nav_contact_us, 0);
//            }
//        });
//
//        calculatorButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                navigationView.getMenu().performIdentifierAction(R.id.nav_grades_calculator, 0);
//            }
//        });
//
//        whatsappButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                navigationView.getMenu().performIdentifierAction(R.id.nav_whatsapp_groups, 0);
//            }
//        });
//
//        donateButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                navigationView.getMenu().performIdentifierAction(R.id.nav_donate, 0);
//            }
//        });


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

        //tvEmail.setText(userDetailsSP.getString("userEmail", "Guest"));

        NavigationView navigationView = findViewById(R.id.nav_view);
        for (int i = 0; i < navigationView.getMenu().size(); i++) {
            navigationView.getMenu().getItem(i).setChecked(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void populateGridLayout() {
        int columnCount = 3; // Number of columns in the grid

        // Convert 20dp to pixels
        int paddingInPixels = (int) (20 * getResources().getDisplayMetrics().density);

        // Calculate available width after padding
        int screenWidth = getResources().getDisplayMetrics().widthPixels - (2 * paddingInPixels);
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        // Calculate grid item size based on available width
        int gridItemSize = (screenWidth / columnCount) - 32; // Adjust for padding/margins

        // Calculate icon size dynamically (40% of grid item size)
        int iconSize = (int) (gridItemSize * 0.5);

        ArrayList<GridItem> items = new ArrayList<>();
        items.add(new GridItem(R.drawable.announcements, getString(R.string.announcements), R.id.nav_annoucements));
        items.add(new GridItem(R.drawable.upcoming_exams, getString(R.string.upcoming_exams), R.id.nav_exams_bank));
        items.add(new GridItem(R.drawable.materials, getString(R.string.materials_page), R.id.nav_materials));
        items.add(new GridItem(R.drawable.our_books, getString(R.string.our_books), R.id.nav_our_books));
        items.add(new GridItem(R.drawable.calc, getString(R.string.grades_calculator), R.id.nav_grades_calculator));
        items.add(new GridItem(R.drawable.upload_materials, getString(R.string.upload_materials), R.id.nav_upload_materials));
        items.add(new GridItem(R.drawable.whatsapp_logo, getString(R.string.whatsapp_groups), R.id.nav_whatsapp_groups));
        items.add(new GridItem(R.drawable.baseline_feedback_24, getString(R.string.feedback), R.id.nav_contact_us));
        items.add(new GridItem(R.drawable.baseline_donate_24, getString(R.string.donate), R.id.nav_donate));

        gridLayout.setColumnCount(columnCount);
        gridLayout.removeAllViews();

        // Calculate text size based on screen density
        float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
        float textSizeInSp = 14f * (screenWidth/976f); //got this constant from experimenting



        for (GridItem item : items) {
            LinearLayout layout = new LinearLayout(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = gridItemSize;
            params.height = gridItemSize;
            params.setMargins(16, 16, 16, 16);
            layout.setLayoutParams(params);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER);
            layout.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_square));
            layout.setPadding(10, 10, 10, 10);
            layout.setElevation(6f);

            // ImageView with dynamic size
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(iconSize, iconSize);
            imageParams.setMargins(0, 0, 0, 8); // Add bottom margin
            imageView.setLayoutParams(imageParams);
            imageView.setImageResource(item.imageResId);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            // TextView with dynamic size
            TextView textView = new TextView(this);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(textParams);
            textView.setText(item.label);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(textSizeInSp);
            textView.setTypeface(ResourcesCompat.getFont(this, R.font.rpt_bold));
            textView.setTextColor(ContextCompat.getColor(this, R.color.black));
            textView.setMaxLines(2);
            textView.setEllipsize(TextUtils.TruncateAt.END);

            layout.addView(imageView);
            layout.addView(textView);

            layout.setOnClickListener(view -> navigationView.getMenu().performIdentifierAction(item.navId, 0));

            gridLayout.addView(layout);
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    private static class GridItem {
        int imageResId;
        String label;
        int navId;

        GridItem(int imageResId, String label, int navId) {
            this.imageResId = imageResId;
            this.label = label;
            this.navId = navId;
        }
    }
}
