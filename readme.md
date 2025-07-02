# Sa3id – High‑School Bagrut Preparation App

**Author:** Mohammed Badran  
**Platform:** Android Studio
**Backend:** Firebase (Realtime DB, Storage, Firestore, Auth)

---

## 📖 Introduction

_Sa3id_ is a comprehensive mobile platform designed for Israeli high‑school students preparing for the Bagrut exams. It bundles:

- **Exam calendar & reminders** – auto‑imported from MoE CSV → JSON pipeline  
- **Grades calculator** – real‑time tracking of final scores per subject  
- **Bagrut bank** – past exams, official solutions, quizzes  
- **Study materials** – upload/download, admin approval system  
- **Announcements & feedback** – push notifications and two‑way messaging  
- **User authentication** – email/password + Google Sign‑In

---

## ⚙️ Features

1. **User Auth**  
   - Email/password & Google OAuth  
   - Profile editing (picture & username)

2. **Bagrut Subject Selection**  
   - Mandatory subjects with unit‑level radio buttons  
   - Elective majors & “advanced mode” exam ID checklist  

3. **Exam Calendar**  
   - Firebase Realtime‐DB import of official MoE timetable  
   - Local notifications respecting extra‑time entitlement  

4. **Grades Calculator**  
   - Flexible weighting per component  
   - Instant updates & progress tracking  

5. **Materials & Exams Bank**  
   - Browse past exams & solutions  
   - Upload new resources for review  

6. **Announcements & Feedback**  
   - Admin‐driven notices  
   - In‑app feedback system  

7. **Settings & Preferences**  
   - Theme, notification toggles, extra‑time settings  

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Bumblebee or later  
- Kotlin 1.6+  
- Firebase project with:  
  - Realtime Database  
  - Cloud Firestore  
  - Storage  
  - Authentication enabled  



## 🏗️ Architecture & UML

The app follows an MVVM‑inspired structure with:

* **Activities/Fragments** for UI
* **Managers** (e.g., `ExamManager`) for Firebase logic
* **Models** for data classes

### UML Class Diagram


![UML Diagram](docs/uml.pdf)


---

## 🖼️ Screenshots

![Main Screen](docs/main_activity.png)
<p align="center">
  <img src="docs/signin.png" alt="Sign‑In Screen" />
</p>



---

## 📄 License

Distributed under the MIT License. See [LICENSE](LICENSE) for more details.


