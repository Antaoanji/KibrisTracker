package com.example.pushuptracker.data

import com.example.pushuptracker.model.Exercise
import com.example.pushuptracker.model.Workout

object WorkoutData {
    // Program 1: Makine + Ağırlık
    val machineWeightProgram: List<Workout> = listOf(
        Workout(
            title = "PUSH (Makine + Ağırlık)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks (Sıçrama)", "jumping jacks", 1, "1 Dakika", 0, "Vücut ısısını artır. Ayaklar bitişik, kollar yanda başla. Zıplayarak ayakları açarken elleri baş üstünde birleştir."),
                Exercise("Isınma: Kol ve Gövde Çevirme", "arm torso circles", 1, "1 Dakika", 0, "Ayaklar sabit, gövdeni sağa sola döndür. Kollarını geniş daireler çizerek çevir."),
                Exercise("Isınma: Duvarda Şınav", "wall pushup", 1, "15", 30, "Elleri duvara dayayıp ayakta şınav çek."),
                Exercise("Isınma: Band Pull Apart", "pull apart", 1, "15", 30, "Kürek kemiklerini sıkıştır."),
                Exercise("Isınma: Machine Chest Press (Hafif Kilo)", "chest press warmup", 1, "15", 60, "Boş veya çok hafif kilo ile eklemleri hazırla."),
                Exercise("Machine Chest Press", "chest press", 3, "8", 120, "Oturma koltuğunu tutacaklar göğüs ucunla aynı hizada olacak şekilde ayarla. Kolları ileri it, dirsekleri kilitlemeden geri sal. Dikkat: Omuzlarını aşağı bastır."),
                Exercise("Push-Up (Şınav)", "push up", 3, "MAX", 45, "Klasik şınav. Dikkat: 40 tekrar kolaysa sırtına ağırlık al veya ayakları yükselt (Decline). Hedef 12-15 zorlu tekrar."),
                Exercise("Pec Deck Fly (Kelebek)", "chest fly", 3, "12", 60, "Dirsekler omuz hizasında. Kolları göğsün ortasında birleştir ve 1 saniye sıkıştır. Dikkat: Göğüs kasının esnediğini hisset."),
                Exercise("Barbell Overhead Press", "overhead press", 3, "8", 120, "Ayakta, barı göğüs üstünde tut. Başının üzerine it. Dikkat: Belini geriye bükme, karnını sık."),
                Exercise("Cable Lateral Raise", "lateral raise", 3, "15", 45, "Alt makarayı kullan. Kolunu yana doğru aç. Dikkat: Dambıldan daha etkilidir çünkü direnç bitmez. Yavaş yap."),
                Exercise("Triceps Pushdown", "triceps pushdown", 3, "12", 60, "Üst makarayı kullan. Dirsekleri gövdene yapıştır. Sadece ön kollarını aşağı it. Dikkat: Dirseklerin sabit kalsın.")
            )
        ),
        Workout(
            title = "PULL (Makine + Ağırlık)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks (Sıçrama)", "jumping jacks", 1, "1 Dakika", 0, "Vücut ısısını artır."),
                Exercise("Isınma: Kol ve Gövde Çevirme", "arm torso circles", 1, "1 Dakika", 0, "Isınma"),
                Exercise("Isınma: Kedi-Deve", "cat cow", 1, "10", 30, "Sırtı ve beli esnet."),
                Exercise("Isınma: Scapular Push-Up", "scapular pushup", 1, "10", 30, "Isınma"),
                Exercise("Isınma: Bent Over Row (Boş Bar)", "bent over row warmup", 1, "15", 60, "Boş barla sırtı hazırla."),
                Exercise("Barbell Bent Over Row", "bent over row", 3, "8-10", 120, "Dizleri kır, gövdeyi 45 derece eğ. Barı göbek deliğine çek. Dikkat: Belini kamburlaştırma, kürek kemiklerini sıkıştır."),
                Exercise("Lat Pulldown (Sırta Çekiş)", "lat pulldown", 3, "10", 90, "Geniş tut. Barı köprücük kemiğine çek. Dikkat: Ensene değil göğsüne çek. Gerekirse yere diz çök (Kneeling)."),
                Exercise("One Arm Dumbbell Row", "dumbbell row", 3, "10-12", 60, "Elini ve dizini sehpaya daya. Dambılı kalçana çek. Dikkat: Gövdeni döndürme."),
                Exercise("Face Pull (Yüze Çekiş)", "face pull", 3, "15", 45, "Halatı alnına doğru çek, dirsekleri dışarı aç. Dikkat: Eller kulakların gerisine gitsin."),
                Exercise("Dumbbell Shrug (Omuz Silkme)", "shrug", 3, "15", 60, "Ağır dambılları al. Omuzları kulaklara çek, 1 sn bekle. Dikkat: Dirsekleri bükme."),
                Exercise("Barbell Curl (Halter Pazu)", "barbell curl", 3, "8-10", 90, "Barı göğse kaldır. Dikkat: Vücudunu sallayarak (cheat) kaldırma, dirsekleri sabitle."),
                Exercise("Dumbbell Hammer Curl", "hammer curl", 3, "12", 45, "Avuç içleri birbirine bakacak şekilde tut. Dikkat: Ön kol kaslarını sıkarak kaldır.")
            )
        ),
        Workout(
            title = "LEGS (Makine + Ağırlık)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks (Sıçrama)", "jumping jacks", 1, "1 Dakika", 0, "Isınma"),
                Exercise("Isınma: Kol ve Gövde Çevirme", "arm torso circles", 1, "1 Dakika", 0, "Isınma"),
                Exercise("Isınma: Leg Swing", "leg swing", 1, "30 Saniye", 30, "Isınma"),
                Exercise("Isınma: Bodyweight Squat", "bodyweight squat", 1, "20", 30, "Isınma"),
                Exercise("Isınma: Barbell Squat (Hafif)", "squat warmup", 1, "10", 60, "Isınma"),
                Exercise("Barbell Squat", "squat", 3, "8", 180, "Kalçayı geriye atarak çök. Dikkat: Dizlerin içe çökmesin, topukların kalkmasın."),
                Exercise("Romanian Deadlift (RDL)", "rdl", 3, "10", 120, "Dizler hafif kırık. Kalçayı geriye iterek eğil. Dikkat: Sırtın masa gibi düz olsun."),
                Exercise("Dumbbell Walking Lunge", "lunge", 3, "12 Adım", 90, "Büyük adımlarla yürü. Dikkat: Arkadaki dizini yere sert vurma."),
                Exercise("Leg Extension (Makine Ön Bacak)", "leg extension", 3, "15", 60, "Bacakları dümdüz kaldır. Dikkat: Tepe noktada uyluk kaslarını (Quadriceps) sık."),
                Exercise("Leg Curl (Makine Arka Bacak)", "leg curl", 3, "12-15", 60, "Topuklarını kalçana çek. Dikkat: Kalçanı sabit tut, sadece dizden altı hareket etsin."),
                Exercise("Standing Calf Raise", "calf raise", 4, "20", 30, "Dambıl elde parmak ucunda yüksel.")
            )
        ),
        Workout(
            title = "UPPER (Makine + Ağırlık)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks (Sıçrama)", "jumping jacks", 1, "1 Dakika", 0, "Isınma"),
                Exercise("Isınma: Kol ve Gövde Çevirme", "arm torso circles", 1, "1 Dakika", 0, "Isınma"),
                Exercise("Isınma: Duvarda Melek", "wall slides", 1, "10", 30, "Isınma"),
                Exercise("Isınma: Band Pull Apart", "pull apart", 1, "15", 30, "Isınma"),
                Exercise("Isınma: Incline Press (Hafif)", "incline warmup", 1, "15", 60, "Isınma"),
                Exercise("Incline Dumbbell Press", "incline press", 3, "10", 90, "Sehpa 30-45 derece. Üst göğüs için pres yap."),
                Exercise("Seated Cable Row", "cable row", 3, "10", 90, "Alt makarayı kullan. Karnına çek. Dikkat: Öne çok eğilme, gövden dik dursun."),
                Exercise("Dumbbell Shoulder Press", "shoulder press", 3, "10", 90, "Dambılları baş üstüne it."),
                Exercise("Dumbbell Lateral Raise", "lateral raise", 3, "15", 45, "Yana açış."),
                Exercise("Lat Pulldown", "lat pulldown", 3, "10", 90, "Hacim için tekrar."),
                Exercise("Süper Set: Cable Biceps Curl", "cable curl ss", 1, "12", 0, "Alt makara - Kısa bar. Bitince HİÇ dinlenme!"),
                Exercise("Süper Set: Triceps Pushdown", "triceps pushdown ss", 1, "12", 90, "Üst makara. Bitince 90 sn dinlen."),
                Exercise("Süper Set: Cable Biceps Curl", "cable curl ss", 1, "12", 0, "2. Tur: Hiç dinlenme!"),
                Exercise("Süper Set: Triceps Pushdown", "triceps pushdown ss", 1, "12", 90, "2. Tur: 90 sn dinlen."),
                Exercise("Süper Set: Cable Biceps Curl", "cable curl ss", 1, "12", 0, "3. Tur: Son tur!"),
                Exercise("Süper Set: Triceps Pushdown", "triceps pushdown ss", 1, "12", 90, "3. Tur: 90 sn dinlen.")
            )
        ),
        Workout(
            title = "LOWER (Makine + Ağırlık)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks (Sıçrama)", "jumping jacks", 1, "1 Dakika", 0, "Isınma"),
                Exercise("Isınma: Kol ve Gövde Çevirme", "arm torso circles", 1, "1 Dakika", 0, "Isınma"),
                Exercise("Isınma: World's Greatest Stretch", "stretch", 1, "10", 30, "Isınma"),
                Exercise("Isınma: Glute Bridge", "glute bridge", 1, "15", 30, "Isınma"),
                Exercise("Isınma: Deadlift (Hafif)", "deadlift warmup", 1, "5", 60, "Hafif ağırlıkla hazırla."),
                Exercise("Deadlift (Klasik Halter)", "deadlift", 3, "6", 180, "Yerden bacaklarla iterek kalk. Dikkat: Belin bükülmesin, en ağır hareket budur."),
                Exercise("Goblet Squat", "goblet squat", 3, "12", 90, "Dambılı kade gibi göğüste tutarak çök. Dikkat: Derin çök."),
                Exercise("Leg Curl (Makine)", "leg curl", 3, "12", 60, "Arka bacak."),
                Exercise("Calf Raise", "calf raise", 4, "20", 30, "Baldır.")
            )
        )
    )

    // Program 2: Calisthenics + Ağırlık
    val calisthenicsProgram: List<Workout> = listOf(
        Workout(
            title = "PUSH (Calisthenics + Ağırlık)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks (Sıçrama)", "jumping jacks", 1, "1 Dakika", 0, "Ayaklar bitişik, kollar yanda başla. Zıplayarak ayakları açarken elleri baş üstünde birleştir."),
                Exercise("Isınma: Kol ve Gövde Çevirme", "arm torso circles", 1, "1 Dakika", 0, "Ayaklar sabit, gövdeni sağa sola döndür. Kollarını geniş daireler çizerek çevir."),
                Exercise("Isınma: Duvarda Şınav", "wall pushup", 1, "15", 30, "Elleri duvara dayayıp ayakta şınav çek."),
                Exercise("Isınma: Band Pull Apart", "pull apart", 1, "15", 30, "Lastiği veya havluyu göğüs hizasında tut, kolları kırmadan yanlara açıp kürek kemiklerini sıkıştır."),
                Exercise("Isınma: Dips Statik Bekleme", "dips static", 1, "15 Saniye", 15, "Dips pozisyonu al, kolları kilitle, havada asılı bekle."),
                Exercise("Isınma: Şınav (Isınma)", "pushup warmup", 1, "10", 60, "Yavaş, kası hisset."),
                Exercise("Dips (Paralel Bar / Sandalye)", "dips", 3, "MAX", 120, "İki barın (veya sandalyenin) üzerine çık. Gövdeni hafifçe öne doğru eğ (bu göğsü vurması için şart). Dirseklerin 90 derece olana kadar in, nefes vererek kendini yukarı it. Püf Nokta: Omuzlarını kulaklarından uzak tut, boynunu içine gömme."),
                Exercise("Decline Push-Up", "decline pushup", 3, "12-15", 90, "Ayaklarını koltuğa/sehpaya koy, ellerin yerde olsun. Vücudun dümdüz bir çizgi halinde olmalı. Göğsün yere değene kadar in ve bas. Püf Nokta: Kalçanı düşürme, karın kaslarını sıkı tut."),
                Exercise("Dumbbell Shoulder Press (Ayakta)", "shoulder press", 3, "8-10", 120, "Ayakta dik dur, karın kaslarını sık. Dambılları kulak hizasında tut. Nefes vererek başının üzerine doğru it ama dambılları birbirine çarptırma. Yavaşça indir. Püf Nokta: Belini aşırı çukurlaştırma."),
                Exercise("Dumbbell Lateral Raise (Yana Açış)", "lateral raise", 3, "15", 60, "Dambılları yanlarda tut. Dirseklerini çok hafif bük. Kolları yanlara doğru (kartal kanadı gibi) omuz hizasına kadar kaldır ve yavaşça indir. Püf Nokta: Serçe parmağın tavanı göstersin. Gövdeni sallayarak güç alma."),
                Exercise("Dumbbell Skullcrusher", "skullcrusher", 3, "10-12", 60, "Yere sırtüstü yat, dambılları gökyüzüne kaldır. Dirseklerinin yerini sabitle, sadece ön kollarını bükerek dambılları kulaklarının yanına kadar indir ve tekrar yukarı it. Püf Nokta: Dirseklerin dışarı açılmasın, hep tavanı göstersin.")
            )
        ),
        Workout(
            title = "PULL (Calisthenics + Ağırlık)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks (Sıçrama)", "jumping jacks", 1, "1 Dakika", 0, "Vücut ısısını artırmak için tempoyu koru."),
                Exercise("Isınma: Kol ve Gövde Çevirme", "arm torso circles", 1, "1 Dakika", 0, "Kollarını geniş daireler çizerek çevir."),
                Exercise("Isınma: Kedi-Deve", "cat cow", 1, "10", 30, "Dört ayak üstünde sırtını kamburlaştır ve çukurlaştır."),
                Exercise("Isınma: Dead Hang (Asılı Bekleme)", "dead hang", 1, "20 Saniye", 30, "Barfiks demirine asıl ve kendini tamamen serbest bırak. Omurgayı açar."),
                Exercise("Isınma: Scapular Pull-Up", "scapular pullup", 1, "10", 30, "Asılıyken dirsekleri bükmeden sadece omuzlarını kulaklarına çek ve bastır."),
                Exercise("Isınma: Hafif Dumbbell Row", "db row warmup", 1, "12", 60, "Sırtı kanatlandır."),
                Exercise("Pull-Up (Barfiks)", "pullup", 3, "MAX", 150, "Avuç içleri karşıya baksın. Göğüsünü bara değdirmeye çalışarak çek. Püf Nokta: Çıkamıyorsan zıplayarak çık, çok yavaş (3-4 saniyede) aşağı in. (Negatif Tekrar)."),
                Exercise("Barbell Bent Over Row (Halterle Çekiş)", "bent over row", 3, "8-10", 120, "Dizleri kır, gövdeyi 45 derece eğ. Bel düz. Barı göbek deliğine çek. Püf Nokta: Çekerken kürek kemiklerini birbirine değdirmeye çalış."),
                Exercise("One Arm Dumbbell Row", "dumbbell row", 3, "10-12", 60, "Elini ve dizini sehpaya daya. Dambılı kalçana doğru geriye çek (Testere gibi). Püf Nokta: Gövdeni döndürme, sadece kolun çalışsın."),
                Exercise("Dumbbell Rear Delt Fly (Arka Omuz)", "rear delt fly", 3, "15", 60, "Öne eğil (Row pozisyonu). Dambılları kuş gibi yanlara aç. Püf Nokta: Kürek kemiklerini değil, sadece arka omuz başlarını hisset."),
                Exercise("Dumbbell Shrug (Omuz Silkme)", "shrug", 3, "15", 60, "Dik dur, ağır dambılları al. Omuzlarını kulaklarına çek, 1 sn bekle, indir. Püf Nokta: Dirseklerini bükme, sadece omuzların yukarı-aşağı insin."),
                Exercise("Barbell Curl (Halter Pazu)", "barbell curl", 3, "8-10", 90, "Ayakta dik dur, barı omuz hizasında tut. Göğse doğru kaldır. Püf Nokta: Dirseklerini vücuduna yapıştır."),
                Exercise("Dumbbell Hammer Curl (Çekiç Pazu)", "hammer curl", 3, "12", 60, "Avuç içleri birbirine bakacak şekilde tut. Omuz başlarına doğru kaldır. Püf Nokta: Bu hareket kolun dışını ve ön kolu doldurur.")
            )
        ),
        Workout(
            title = "LEGS (Calisthenics + Ağırlık)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks (Sıçrama)", "jumping jacks", 1, "1 Dakika", 0, "Vücut ısısını artır."),
                Exercise("Isınma: Kol ve Gövde Çevirme", "arm torso circles", 1, "1 Dakika", 0, "Isınma"),
                Exercise("Isınma: Leg Swing", "leg swing", 1, "30 Saniye", 30, "Bir yerden destek al, bacağını sarkaç gibi salla."),
                Exercise("Isınma: Bodyweight Squat", "bodyweight squat", 1, "20", 30, "Ağırlıksız çök kalk."),
                Exercise("Isınma: Barbell Squat (Isınma Seti)", "squat warmup", 1, "10", 60, "Hafif kilo ile eklemleri hazırla."),
                Exercise("Barbell Squat", "squat", 3, "8", 180, "Bar sırtta. Kalçayı geriye atarak sandalyeye oturur gibi çök. Püf Nokta: Dizlerin içe çökmesin, dışa doğru baskı yap. Topukların yerden kalkmasın."),
                Exercise("Romanian Deadlift (RDL)", "rdl", 3, "10", 120, "Dizleri hafif kırık ve sabit. Kalçayı geriye iterek eğil. Bar diz altına inince kalk. Püf Nokta: Bar bacaklarına hep temas etsin. Sırtın asla kamburlaşmasın."),
                Exercise("Dumbbell Walking Lunge", "lunge", 3, "12 Adım", 90, "Dambılları al, büyük adımlarla yürü. Arkadaki diz yere yaklaşsın. Püf Nokta: Gövden dik dursun."),
                Exercise("Lying Dumbbell Leg Curl (Arka Bacak)", "dumbbell leg curl", 3, "12-15", 90, "Yüzüstü yat. Dambılı iki ayağının tabanı arasına sıkıştır. Topuklarını kalçana çek. Püf Nokta: Kalçanı yerden kaldırma."),
                Exercise("Single Leg Calf Raise", "calf raise", 4, "20", 45, "Tek ayakla basamakta parmak ucunda yüksel. Püf Nokta: Topuğu iyice aşağı esnet.")
            )
        ),
        Workout(
            title = "UPPER (Calisthenics + Ağırlık)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks (Sıçrama)", "jumping jacks", 1, "1 Dakika", 0, "Vücut ısısını artır."),
                Exercise("Isınma: Kol ve Gövde Çevirme", "arm torso circles", 1, "1 Dakika", 0, "Isınma"),
                Exercise("Isınma: Duvarda Melek", "wall slides", 1, "10", 30, "Sırtını duvara yasla, kollarını teslim ol şeklinde yukarı aşağı kaydır."),
                Exercise("Isınma: Band Pull Apart", "pull apart", 1, "15", 30, "Göğüs açma."),
                Exercise("Pike Push-Up (V-Şınav)", "pike pushup", 3, "10-12", 90, "Şınav pozisyonunda kalçayı havaya dik (Ters V). Kafanı yere doğru indir, it. Püf Nokta: Dirsekler geriye baksın."),
                Exercise("Chin-Up (Avuç İçi Bize Bakan Barfiks)", "chinup", 3, "MAX", 120, "Avuç içleri yüzüne baksın. Kendini yukarı çek. Püf Nokta: Bicepsleri daha çok çalıştırır."),
                Exercise("Dumbbell Floor Press (Yerde Pres)", "floor press", 3, "10-12", 90, "Yere sırtüstü yat. Dirsekler yere değene kadar indir, göğsü sıkarak it. Püf Nokta: Dirsekler değince hemen geri it."),
                Exercise("Dumbbell Lateral Raise", "lateral raise", 3, "15", 60, "Omuz hizasına kadar yanlara aç."),
                Exercise("Süper Set: Hammer Curl", "hammer curl ss", 1, "12", 0, "Dambılları çekiç gibi tut ve kaldır. Bitince HİÇ dinlenme!"),
                Exercise("Süper Set: Bench Dips", "bench dips ss", 1, "12", 90, "Eller arkada koltukta, kendini yukarı it. Bitince 90 sn dinlen."),
                Exercise("Süper Set: Hammer Curl", "hammer curl ss", 1, "12", 0, "2. Tur: Hiç dinlenme!"),
                Exercise("Süper Set: Bench Dips", "bench dips ss", 1, "12", 90, "2. Tur: Bitince 90 sn dinlen."),
                Exercise("Süper Set: Hammer Curl", "hammer curl ss", 1, "12", 0, "3. Tur: Son tur!"),
                Exercise("Süper Set: Bench Dips", "bench dips ss", 1, "12", 90, "3. Tur: Bitince 90 sn dinlen.")
            )
        ),
        Workout(
            title = "LOWER & GÜÇ (Calisthenics + Ağırlık)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks (Sıçrama)", "jumping jacks", 1, "1 Dakika", 0, "Vücut ısısını artır."),
                Exercise("Isınma: Kol ve Gövde Çevirme", "arm torso circles", 1, "1 Dakika", 0, "Isınma"),
                Exercise("Isınma: World's Greatest Stretch", "stretch", 1, "10", 30, "Lunge pozisyonunda öndeki bacağın tarafındaki kolunu havaya kaldır."),
                Exercise("Isınma: Glute Bridge", "glute bridge", 1, "15", 30, "Yerde sırtüstü yat, dizleri bük, kalçanı havaya kaldır sık."),
                Exercise("Isınma: Deadlift (Isınma)", "deadlift warmup", 1, "5", 60, "Boş barla form, hafif kilo ile hazırla."),
                Exercise("Deadlift (Klasik)", "deadlift", 3, "6", 180, "Bar ayak bağcıklarının üzerinde. Kalçayı geriye at, eğil, barı tut. Sırt dümdüz. Yeri iterek kalk. Püf Nokta: Bacaklarınla yeri it."),
                Exercise("Goblet Squat", "goblet squat", 3, "12", 90, "Tek dambılı kadeh gibi göğsünde tut. Dik durarak çök. Püf Nokta: Derin çök."),
                Exercise("Lying Dumbbell Leg Curl", "dumbbell leg curl", 3, "12", 90, "Dambılı iki ayağının tabanı arasına sıkıştır. Topuklarını kalçana çek."),
                Exercise("Standing Calf Raise", "calf raise", 4, "20", 45, "Baldır çalışması.")
            )
        )
    )
}
