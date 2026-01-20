package com.example.pushuptracker.data

import com.example.pushuptracker.model.Exercise
import com.example.pushuptracker.model.Workout

object WorkoutData {
    // Program 1: Makine + Ağırlık
    val machineWeightProgram: List<Workout> = listOf(
        Workout(
            title = "PUSH (Makine + Ağırlık)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks (Sıçrama)", "jumping jacks", 1, "1 Dakika", 30, "Vücut ısısını artırmak için tempoyu koru.", ""),
                Exercise("Isınma: Öne Kol Çevirme", "arm circles forward", 1, "30 Saniye", 15, "Omuz eklemlerini öne doğru dairesel hareketlerle ısıt.", ""),
                Exercise("Isınma: Arkaya Kol Çevirme", "arm circles backward", 1, "30 Saniye", 15, "Omuz eklemlerini arkaya doğru dairesel hareketlerle ısıt.", ""),
                Exercise("Isınma: Gövde Döndürme (Torso Twist)", "torso twist", 1, "30 Saniye", 15, "Omurganı ve bel bölgesini nazikçe döndürerek ısıt.", ""),
                Exercise("Isınma: Duvarda Şınav", "wall pushup", 1, "15", 45, "Omuzları aç ve hazırla.", ""),
                Exercise("Isınma: Band/Havlu Pull Apart", "pull apart", 1, "15", 45, "Sırtı sabitle ve kürek kemiklerini hisset.", ""),
                Exercise("Isınma: Chest Press", "chest press warmup", 1, "15+5", 60, "Boş veya çok hafif kilo ile 15 tekrar yap. Sonra %50 ağırlıkla 5 tekrar yap.", ""),
                Exercise("Machine Chest Press", "chest press", 3, "8", 120, "Strateji: Ağırlığı artır, 8 tekrarda göğsün patlayacak gibi olsun.", ""),
                Exercise("Push-Up (Şınav)", "push up", 3, "MAX", 45, "Göğüs kasları sıcakken kalan son enerjiyi burada bitiriyoruz.", ""),
                Exercise("Pec Deck Fly", "chest fly", 3, "12", 60, "Kolları kapatırken göğsünü sıkıştır ve 1 saniye bekle.", ""),
                Exercise("Barbell Overhead Press", "overhead press", 3, "8", 120, "Güç hareketi olduğu için sayı düşük, ağırlık yüksek.", ""),
                Exercise("Cable Lateral Raise", "lateral raise", 3, "15", 45, "Omuzlar düşük ağırlık ve yüksek tekrar sever.", ""),
                Exercise("Triceps Pushdown", "triceps pushdown", 3, "12", 60, "Dirsekler vücuda yapışık olsun.")
            )
        ),
        Workout(
            title = "PULL (Makine + Ağırlık)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks (Sıçrama)", "jumping jacks", 1, "1 Dakika", 30, "Vücut ısısını artırmak için tempoyu koru.", ""),
                Exercise("Isınma: Öne Kol Çevirme", "arm circles forward", 1, "30 Saniye", 15, "Omuzları ısıt.", ""),
                Exercise("Isınma: Arkaya Kol Çevirme", "arm circles backward", 1, "30 Saniye", 15, "Omuzları ısıt.", ""),
                Exercise("Isınma: Gövde Döndürme (Torso Twist)", "torso twist", 1, "30 Saniye", 15, "Beli ısıt.", ""),
                Exercise("Isınma: Kedi-Deve (Yerde esneme)", "cat cow", 1, "10", 45, "Sırtı ve beli esnet.", ""),
                Exercise("Isınma: Scapular Push-Up", "scapular pushup", 1, "10", 45, "Dirsek kırmadan kürek kemiklerini hareket ettir.", ""),
                Exercise("Isınma: Bent Over Row", "bent over row warmup", 1, "15", 60, "Sadece boş barla 15 tekrar yap.", ""),
                Exercise("Barbell Bent Over Row", "bent over row", 3, "8", 120, "Belini korumak ve sırtı kalınlaştırmak için ağır gir.", ""),
                Exercise("Kneeling Lat Pulldown", "lat pulldown", 3, "10", 90, "İyice gerdirerek yap.", ""),
                Exercise("One Arm Dumbbell Row", "dumbbell row", 3, "10", 60, "Testere çeker gibi dirseği geriye süpür.", ""),
                Exercise("Face Pull", "face pull", 3, "15", 45, "Arka omuz küçük kastır, yüksek tekrar sever.", ""),
                Exercise("Barbell Curl", "bicep curl", 3, "8", 90, "Pazulara kütle koymak için ağır.", ""),
                Exercise("Dumbbell Hammer Curl", "hammer curl", 3, "12", 45, "Kolu kalınlaştırır.")
            )
        ),
        Workout(
            title = "LEGS (Makine + Ağırlık)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks (Sıçrama)", "jumping jacks", 1, "1 Dakika", 30, "Vücut ısısını artırmak için tempoyu koru.", ""),
                Exercise("Isınma: Öne Kol Çevirme", "arm circles forward", 1, "30 Saniye", 15, "Omuzları ısıt.", ""),
                Exercise("Isınma: Arkaya Kol Çevirme", "arm circles backward", 1, "30 Saniye", 15, "Omuzları ısıt.", ""),
                Exercise("Isınma: Gövde Döndürme (Torso Twist)", "torso twist", 1, "30 Saniye", 15, "Beli ısıt.", ""),
                Exercise("Isınma: Leg Swing (Bacak Sallama)", "leg swing", 1, "30 Saniye", 45, "15 sn sağ, 15 sn sol.", ""),
                Exercise("Isınma: Bodyweight Squat", "bodyweight squat", 1, "20", 45, "Kendi kilonla eklemleri hazırla.", ""),
                Exercise("Isınma: Squat", "squat warmup", 1, "10+5", 60, "Boş barla 10 tekrar. %50 ağırlıkla 5 tekrar.", ""),
                Exercise("Barbell Squat", "squat", 3, "8", 180, "Bacakların büyümesi için en ideal sayı.", ""),
                Exercise("Romanian Deadlift", "rdl", 3, "10", 120, "Arka bacağı esneterek, acele etmeden.", ""),
                Exercise("Dumbbell Walking Lunge", "lunge", 3, "12 Adım", 90, "Adım atarken gövden dik olsun.", ""),
                Exercise("Leg Extension", "leg extension", 3, "15", 60, "En tepede bacakları kilitle ve sık.", ""),
                Exercise("Standing Calf Raise", "calf raise", 4, "20", 30, "Baldır zor yorulur, 20 tekrar.")
            )
        ),
        Workout(
            title = "UPPER (Makine + Ağırlık)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks (Sıçrama)", "jumping jacks", 1, "1 Dakika", 30, "Vücut ısısını artırmak için tempoyu koru.", ""),
                Exercise("Isınma: Öne Kol Çevirme", "arm circles forward", 1, "30 Saniye", 15, "Omuzları ısıt.", ""),
                Exercise("Isınma: Arkaya Kol Çevirme", "arm circles backward", 1, "30 Saniye", 15, "Omuzları ısıt.", ""),
                Exercise("Isınma: Gövde Döndürme (Torso Twist)", "torso twist", 1, "30 Saniye", 15, "Beli ısıt.", ""),
                Exercise("Isınma: Duvarda Melek (Wall Slides)", "wall slides", 1, "10", 45, "Omuz mobilitesi için.", ""),
                Exercise("Isınma: Band Pull Apart", "pull apart", 1, "15", 45, "Sırtı hazırla.", ""),
                Exercise("Isınma: Incline Press", "incline press warmup", 1, "12-15", 60, "Hafif dambıllarla eklemleri yağla.", ""),
                Exercise("Incline Dumbbell Press", "incline press", 3, "10", 90, "Bugün hacim odaklı çalışıyoruz.", ""),
                Exercise("Seated Cable Row", "cable row", 3, "10", 90, "Karnına doğru çekiş yap.", ""),
                Exercise("Dumbbell Shoulder Press", "shoulder press", 3, "10", 90, "Dambılları tepede birbirine çarptırma.", ""),
                Exercise("Dumbbell Lateral Raise", "lateral raise", 3, "15", 45, "Kartal kanadı gibi aç.", ""),
                Exercise("Lat Pulldown (Kneeling)", "lat pulldown", 3, "10", 90, "Dizlerin üzerinde göğse çekiş.", ""),
                Exercise("Süper Set (Kollar)", "super set", 3, "12", 90, "Ara vermeden yap.")
            )
        ),
        Workout(
            title = "LOWER (Makine + Ağırlık)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks (Sıçrama)", "jumping jacks", 1, "1 Dakika", 30, "Vücut ısısını artırmak için tempoyu koru.", ""),
                Exercise("Isınma: Öne Kol Çevirme", "arm circles forward", 1, "30 Saniye", 15, "Omuzları ısıt.", ""),
                Exercise("Isınma: Arkaya Kol Çevirme", "arm circles backward", 1, "30 Saniye", 15, "Omuzları ısıt.", ""),
                Exercise("Isınma: Gövde Döndürme (Torso Twist)", "torso twist", 1, "30 Saniye", 15, "Beli ısıt.", ""),
                Exercise("Isınma: World's Greatest Stretch", "stretch", 1, "10", 45, "Kalçayı müthiş açar.", ""),
                Exercise("Isınma: Bodyweight Squat", "bodyweight squat", 1, "15", 45, "Eklemleri hazırla.", ""),
                Exercise("Isınma: Deadlift", "deadlift warmup", 1, "5+5", 60, "Boş barla form tekrarı.", ""),
                Exercise("Deadlift (Klasik)", "deadlift", 3, "6", 180, "Güç ve kütle için 'Altın Sayı'.", ""),
                Exercise("Goblet Squat (Dambıllla)", "goblet squat", 3, "12", 90, "Kontrollü iniş.", ""),
                Exercise("Leg Curl", "leg curl", 3, "12", 60, "İndirirken çok yavaş sal.", ""),
                Exercise("Calf Raise", "calf raise", 4, "20", 30, "Kalf yüksek tekrar.")
            )
        )
    )

    // Program 2: Calisthenics + Ağırlık
    val calisthenicsProgram: List<Workout> = listOf(
        Workout(
            title = "PUSH (Calisthenics + Ağırlık)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks (Sıçrama)", "jumping jacks", 1, "1 Dakika", 30, "Vücut ısısını artırmak için tempoyu koru.", ""),
                Exercise("Isınma: Öne Kol Çevirme", "arm circles forward", 1, "30 Saniye", 15, "Isınma", ""),
                Exercise("Isınma: Arkaya Kol Çevirme", "arm circles backward", 1, "30 Saniye", 15, "Isınma", ""),
                Exercise("Isınma: Gövde Döndürme (Torso Twist)", "torso twist", 1, "30 Saniye", 15, "Isınma", ""),
                Exercise("Isınma: Duvarda Şınav", "wall pushup", 1, "15", 30, "Isınma", ""),
                Exercise("Isınma: Band Pull Apart", "pull apart", 1, "15", 30, "Isınma", ""),
                Exercise("Isınma: Dips Statik Bekleme", "dips static", 1, "15 Saniye", 15, "Kolları kilitle, havada asılı bekle.", ""),
                Exercise("Isınma: Dips (Paralel Bar)", "dips warmup", 1, "5", 60, "Isınma: Yavaş ve kontrollü.", ""),
                Exercise("Dips (Paralel Bar)", "dips", 3, "MAX", 120, "Gövdeni hafifçe öne eğ. Dirsekler 90 dereceye kadar in.", ""),
                Exercise("Decline Push-Up", "decline pushup", 3, "12-15", 90, "Ayaklar yüksekte (koltuk/sehpa), vücut dümdüz.", ""),
                Exercise("Isınma: Shoulder Press", "shoulder press warmup", 1, "10", 60, "Hafif dambılla omuzları hazırla.", ""),
                Exercise("Dumbbell Shoulder Press", "shoulder press", 3, "8-10", 120, "Ayakta dik dur, dambılları başının üzerine it.", ""),
                Exercise("Dumbbell Lateral Raise", "lateral raise", 3, "15", 60, "Gövdeni sallamadan omuz hizasına kadar kaldır.", ""),
                Exercise("Dumbbell Skullcrusher", "skullcrusher", 3, "10-12", 60, "Dirsekleri sabitle, dambılları kulak yanına indir.")
            )
        ),
        Workout(
            title = "PULL (Calisthenics + Ağırlık)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks (Sıçrama)", "jumping jacks", 1, "1 Dakika", 30, "Isınma", ""),
                Exercise("Isınma: Öne Kol Çevirme", "arm circles forward", 1, "30 Saniye", 15, "Isınma", ""),
                Exercise("Isınma: Arkaya Kol Çevirme", "arm circles backward", 1, "30 Saniye", 15, "Isınma", ""),
                Exercise("Isınma: Gövde Döndürme (Torso Twist)", "torso twist", 1, "30 Saniye", 15, "Isınma", ""),
                Exercise("Isınma: Kedi-Deve (Esneme)", "cat cow", 1, "10", 30, "Sırtı ve beli esnet.", ""),
                Exercise("Isınma: Dead Hang (Asılma)", "dead hang", 1, "20 Saniye", 30, "Barfiks demirine asıl ve serbest bırak.", ""),
                Exercise("Isınma: Scapular Pull-Up", "scapular pullup", 1, "10", 30, "Dirsekleri bükmeden omuzları kulaklarına çek.", ""),
                Exercise("Isınma: Pull-Up (Barfiks)", "pullup warmup", 1, "5", 60, "Isınma: Sandalye desteğiyle.", ""),
                Exercise("Pull-Up (Barfiks)", "pullup", 3, "MAX", 150, "Göğsünü bara değdirmeye çalış. İnerken yavaş in.", ""),
                Exercise("Isınma: Bent Over Row", "bent over row warmup", 1, "12", 60, "Boş barla form tekrarı.", ""),
                Exercise("Barbell Bent Over Row", "bent over row", 3, "8-10", 120, "Belin dümdüz olsun, barı göbeğine doğru çek.", ""),
                Exercise("One Arm Dumbbell Row", "dumbbell row", 3, "10-12", 60, "Testere çeker gibi dirseği geriye süpür.", ""),
                Exercise("Dumbbell Rear Delt Fly", "rear delt fly", 3, "15", 60, "Row pozisyonunda kolları yanlara aç.", ""),
                Exercise("Barbell Curl (Pazu)", "barbell curl", 3, "8-10", 90, "Dirsekleri vücuda yapıştır, belden güç alma.")
            )
        ),
        Workout(
            title = "LEGS (Calisthenics + Ağırlık)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks (Sıçrama)", "jumping jacks", 1, "1 Dakika", 30, "Isınma", ""),
                Exercise("Isınma: Öne Kol Çevirme", "arm circles forward", 1, "30 Saniye", 15, "Isınma", ""),
                Exercise("Isınma: Arkaya Kol Çevirme", "arm circles backward", 1, "30 Saniye", 15, "Isınma", ""),
                Exercise("Isınma: Gövde Döndürme (Torso Twist)", "torso twist", 1, "30 Saniye", 15, "Isınma", ""),
                Exercise("Isınma: Leg Swing (Bacak Sallama)", "leg swing", 1, "30 Saniye", 30, "Isınma", ""),
                Exercise("Isınma: Bodyweight Squat", "bodyweight squat", 1, "20", 30, "Isınma", ""),
                Exercise("Isınma: Squat", "squat warmup", 1, "10+5", 60, "Boş barla 10, %50 kiloyla 5 tekrar.", ""),
                Exercise("Barbell Squat", "squat", 3, "8", 180, "Kalçayı geriye iterek çök. Topuklara basarak kalk.", ""),
                Exercise("Isınma: RDL", "rdl warmup", 1, "8", 60, "Boş barla arka bacağı hazırla.", ""),
                Exercise("Romanian Deadlift", "rdl", 3, "10", 120, "Dizleri hafif kır ve sabitle. Arka bacağı hisset.", ""),
                Exercise("Dumbbell Walking Lunge", "lunge", 3, "12 Adım", 90, "Öne büyük adım at, arka dizi yere yaklaştır.", ""),
                Exercise("Single Leg Calf Raise", "calf raise", 4, "20", 45, "Basamakta tek ayakla yüksel.")
            )
        ),
        Workout(
            title = "UPPER (Calisthenics + Ağırlık)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks (Sıçrama)", "jumping jacks", 1, "1 Dakika", 30, "Isınma", ""),
                Exercise("Isınma: Öne Kol Çevirme", "arm circles forward", 1, "30 Saniye", 15, "Isınma", ""),
                Exercise("Isınma: Arkaya Kol Çevirme", "arm circles backward", 1, "30 Saniye", 15, "Isınma", ""),
                Exercise("Isınma: Gövde Döndürme (Torso Twist)", "torso twist", 1, "30 Saniye", 15, "Isınma", ""),
                Exercise("Isınma: Duvarda Melek (Wall Slides)", "wall slides", 1, "10", 30, "Omuz mobilitesi.", ""),
                Exercise("Isınma: Band Pull Apart", "pull apart", 1, "15", 30, "Göğüs açma.", ""),
                Exercise("Pike Push-Up (V-Şınav)", "pike pushup", 3, "10-12", 90, "Ters V duruşunda kafanı yere indir ve it.", ""),
                Exercise("Chin-Up (Pazu Barfiks)", "chinup", 3, "MAX", 120, "Avuç içleri sana baksın. Kendini yukarı çek.", ""),
                Exercise("Dumbbell Floor Press", "floor press", 3, "10-12", 90, "Yerde sırtüstü yat, göğüs presi yap.", ""),
                Exercise("Dumbbell Lateral Raise", "lateral raise", 3, "15", 60, "Yana açış.", ""),
                Exercise("Hammer Curl (Çekiç Pazu)", "hammer curl", 3, "12", 45, "Avuç içleri birbirine baksın."),
                Exercise("Bench Dips (Arka Kol)", "bench dips", 3, "12", 45, "Eller arkada koltukta, kendini yukarı it.")
            )
        ),
        Workout(
            title = "LOWER (Calisthenics + Ağırlık)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks (Sıçrama)", "jumping jacks", 1, "1 Dakika", 30, "Isınma", ""),
                Exercise("Isınma: Öne Kol Çevirme", "arm circles forward", 1, "30 Saniye", 15, "Isınma", ""),
                Exercise("Isınma: Arkaya Kol Çevirme", "arm circles backward", 1, "30 Saniye", 15, "Isınma", ""),
                Exercise("Isınma: Gövde Döndürme (Torso Twist)", "torso twist", 1, "30 Saniye", 15, "Isınma", ""),
                Exercise("Isınma: World's Greatest Stretch", "stretch", 1, "10", 30, "Isınma", ""),
                Exercise("Isınma: Glute Bridge (Köprü)", "glute bridge", 1, "15", 30, "Kalçayı havada sık.", ""),
                Exercise("Isınma: Deadlift", "deadlift warmup", 1, "5+5", 60, "Hafif ağırlıkla hazırla.", ""),
                Exercise("Deadlift (Klasik)", "deadlift", 3, "6", 180, "Sırtın dümdüz olsun. Yerden güç alarak kalk.", ""),
                Exercise("Goblet Squat (Dambıllla)", "goblet squat", 3, "12", 90, "Dambılı kadeh gibi tut ve çök.", ""),
                Exercise("Dumbbell RDL", "dumbbell rdl", 3, "12", 90, "Dambıllarla arka bacak esnetme.", ""),
                Exercise("Standing Calf Raise", "calf raise", 4, "20", 45, "Baldır çalışması.")
            )
        )
    )
}
