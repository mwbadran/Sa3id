import csv
import json
from collections import OrderedDict

# mapping of ID prefixes to Bagrut subjects
PREFIX_SUBJECT_MAP = {
    '899': "מדעי המחשב",
    '816': 'אמנות שימושית',
    '880': "תיירות",
    '845': "מערכות חשמל",
    '838': "מכניקה הנדסית",
    '815': "אלקטרוניקה ומחשבים",
    '585': "רוסית",
    '574': "גרמנית",
    '66': "מדעי המדינה",
    "65": "סוציולוגיה",
    "49": "שיטות מחקר", # EXAM SHARED ACCROSS SUBJECTS: "סוציולוגיה", "תקשורת"..
    "63": "כלכלה",
    '64': "מדעי הסביבה",
    '57': "גאוגרפיה", 
    '47': "דת האסלאם",
    '73': "דת נוצרית",
    '27': "מורשת הדרוזים",
    '48': "מוזיקה",
    '46': "חקלאות",
    '43': "ביולוגיה",
    '41': "חינוך גופן",
    '37': "כימיה",
    '36': 'פיזיקה',
    '35': 'מתמטיקה',
    '34': "אזרחות",
    '30': "היסטוריה ליהודים",
    '29': "היסטוריה ליהודים",
    '25': "היסטוריה לדרוזים",
    '23': "היסטוריה לערבים",
    '22': "היסטוריה ליהודים",
    '21': "ערבית לדרוזים",
    '20': "ערבית לערבים",
    '16': "אנגלית",
    '15': "עברית לדרוזים",
    '14': "עברית לערבים",
    '11': "עברית ליהודים",
}

def detect_subject_by_id(quiz_id):
    # רק מספרים > 9999
    if not quiz_id.isdigit() or int(quiz_id) <= 9999:
        return 'אחר'
    # גאוגרפיה: רק 5 ספרות שמתחילות ב-57
    if quiz_id.startswith('57') and len(quiz_id) == 5:
        return PREFIX_SUBJECT_MAP['57']
    # שאר המקפים
    for prefix in sorted(PREFIX_SUBJECT_MAP, key=len, reverse=True):
        if prefix == '57':
            continue
        if quiz_id.startswith(prefix):
            return PREFIX_SUBJECT_MAP[prefix]
    return 'אחר'


def build_realtime_structure(csv_path):
    # exams -> subject -> date -> quiz_id -> entry
    temp = {}
    with open(csv_path, encoding='utf-8-sig') as sample_file:
        sample = sample_file.read(2048)
        dialect = csv.Sniffer().sniff(sample, delimiters='\t,')

    with open(csv_path, encoding='utf-8-sig') as csv_file:
        reader = csv.DictReader(csv_file, dialect=dialect)
        reader.fieldnames = [fn.strip().lstrip('\ufeff') for fn in reader.fieldnames]
        required = {'תאריך בחינה', 'שאלון'}
        missing = required - set(reader.fieldnames)
        if missing:
            raise ValueError(f"Missing headers: {missing}")

        for raw in reader:
            row = {k.strip(): v.strip() for k, v in raw.items()}
            quiz_id = row.get('שאלון', '')
            date = row.get('תאריך בחינה', '')
            if not quiz_id or not date:
                continue

            date = date.replace('/', '-')

            subject = detect_subject_by_id(quiz_id)
            # if subject == 'no subject':
            #     continue
            entry = OrderedDict([
                ('examName', row.get('שם שאלון', '')),
                ('startHour', row.get('משעה', '')),
                ('endHour', row.get('עד שעה', '')),
                ('duration', row.get('משך בחינה', '')),
                ('endHour25', row.get('שעת סיום + 25% תוספת זמן', '')),
                ('endHour33', row.get('שעת סיום + 33% תוספת זמן', '')),
                ('endHour50', row.get('שעת סיום + 50% תוספת זמן', ''))
            ])
            temp.setdefault(subject, {}).setdefault(date, {})[quiz_id] = entry

    # מיון נושאים ואז תאריכים
    exams = OrderedDict()
    for subject in sorted(temp):
        exams[subject] = OrderedDict()
        for date in sorted(temp[subject]):
            exams[subject][date] = temp[subject][date]

    return {'exams': exams}


if __name__ == '__main__':
    input_csv = 'C:/Users/W10/Desktop/exams.csv'
    output_json = 'C:/Users/W10/Desktop/exams_realtime.json'

    data = build_realtime_structure(input_csv)
    with open(output_json, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print(f"saved json with {len(data['exams'])} subjects, sorted by subject and date.")
