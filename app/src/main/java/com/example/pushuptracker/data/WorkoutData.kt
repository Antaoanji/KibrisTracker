package com.example.pushuptracker.data

import com.example.pushuptracker.model.Exercise
import com.example.pushuptracker.model.Workout

object WorkoutData {
    val pplulProgram: List<Workout> = listOf(
        Workout(
            title = "PPLUL - GÜN 1: PUSH (Pazartesi)",
            exercises = listOf(
                Exercise("Machine Chest Press", "chest press", 3, "8-10", 120, "En ağır girdiğin hareket. Dirseklerini omuz hizasından biraz aşağıda tut.", ""),
                Exercise("Push-Up (Şınav)", "push up", 3, "Tükeniş", 45, "Göğüs kasları sıcakken kalan son enerjiyi burada bitiriyoruz.", ""),
                Exercise("Pec Deck Fly", "chest fly", 3, "12-15", 60, "Kolları kapatırken göğsünü sıkıştır ve 1 saniye bekle.", ""),
                Exercise("Barbell Overhead Press", "overhead press", 3, "8-10", 120, "Ayaktaysan belini sıkı tut. Barı çeneni sıyırarak kaldır.", ""),
                Exercise("Cable Lateral Raise", "lateral raise", 3, "12-15", 45, "Alt makarayı kullan. Vücudunu hafif yana yatırarak omzu izole et.", ""),
                Exercise("Triceps Pushdown", "triceps pushdown", 3, "12-15", 60, "Dirsekler vücuda yapışık, sadece ön kollar hareket etsin.", "")
            )
        ),
        Workout(
            title = "PPLUL - GÜN 2: PULL (Salı)",
            exercises = listOf(
                Exercise("Barbell Bent Over Row", "bent over row", 3, "8-10", 120, "Sırtın yere 45 derece eğik olsun. Barı göbek deliğine çek.", ""),
                Exercise("Kneeling Lat Pulldown", "lat pulldown", 3, "10-12", 90, "Makinenin koltuğuna oturma! Yere minder koyup dizlerinin üzerine çök.", ""),
                Exercise("One Arm Dumbbell Row", "dumbbell row", 3, "10-12", 60, "Testere çeker gibi dirseği geriye süpür.", ""),
                Exercise("Face Pull", "face pull", 3, "12-15", 45, "Halatı alnına çek, dirsekleri dışarı aç. Duruş bozukluğunu düzeltir.", ""),
                Exercise("Barbell Curl", "bicep curl", 3, "8-10", 90, "Sallanmadan, sadece pazularla kaldır.", ""),
                Exercise("Dumbbell Hammer Curl", "hammer curl", 3, "Tükeniş", 45, "Avuç içleri birbirine baksın. Kolu kalınlaştırır.", "")
            )
        ),
        Workout(
            title = "PPLUL - GÜN 3: LEGS (Çarşamba)",
            exercises = listOf(
                Exercise("Barbell Squat", "squat", 3, "8-10", 180, "Topuklarına basarak kalk. Dizlerin içe çökmesin.", ""),
                Exercise("Romanian Deadlift", "rdl", 3, "10-12", 120, "Dizler hafif kırık, kalçayı geriye iterek halteri diz altına kadar indir.", ""),
                Exercise("Dumbbell Walking Lunge", "lunge", 3, "10-12", 90, "Adım atarken gövden dik olsun.", ""),
                Exercise("Leg Extension", "leg extension", 3, "12-15", 60, "En tepede bacakları kilitle ve sık.", ""),
                Exercise("Standing Calf Raise", "calf raise", 4, "15-20", 30, "Elde dambıl parmak ucu.", "")
            )
        ),
        Workout(
            title = "PPLUL - GÜN 5: UPPER (Cuma)",
            exercises = listOf(
                Exercise("Incline Dumbbell Press", "incline press", 3, "10-12", 90, "Sehpayı 30-45 derece yap. Üst göğsü doldurur.", ""),
                Exercise("Seated Cable Row", "cable row", 3, "10-12", 90, "Ayaklarını makineye daya, yere otur. Karnına çekiş yap.", ""),
                Exercise("Dumbbell Shoulder Press", "shoulder press", 3, "10-12", 90, "Dambılları tepede birbirine çarptırma.", ""),
                Exercise("Dumbbell Lateral Raise", "lateral raise", 3, "12-15", 45, "Omuzları genişletmek için kritik. Kartal kanadı gibi aç.", ""),
                Exercise("Lat Pulldown (Kneeling)", "lat pulldown", 3, "10-12", 90, "Dizlerin üzerinde göğse çekiş.", ""),
                Exercise("Süper Set: Barbell Curl + Lying Triceps Extension", "super set", 3, "12", 90, "Ara vermeden yap.", "")
            )
        ),
        Workout(
            title = "PPLUL - GÜN 6: LOWER (Cumartesi)",
            exercises = listOf(
                Exercise("Deadlift (Klasik)", "deadlift", 3, "6-8", 180, "En önemli hareketin. Belini düz tut. Barı yerden söküp al.", ""),
                Exercise("Goblet Squat", "squat", 3, "12-15", 90, "Dambıl göğüste kontrollü iniş.", ""),
                Exercise("Leg Curl", "leg curl", 3, "12-15", 60, "İndirirken çok yavaş sal.", ""),
                Exercise("Calf Raise", "calf raise", 4, "20", 30, "Kalf yüksek tekrar.", "")
            )
        )
    )
}
