package com.example.pushuptracker.data

import com.example.pushuptracker.model.Exercise
import com.example.pushuptracker.model.Workout

object WorkoutData {
    // Program 1: Makine + Ağırlık (Machine Focus) - MASTER PROGRAM
    val machineWeightProgram: List<Workout> = listOf(
        Workout(
            title = "1. GÜN: PUSH (Göğüs, Omuz, Arka Kol)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks", "Isınma: Jumping Jacks", 1, "1 Dakika", 0, "Genel Hazırlık: Antrenmana başlamadan önce nabzı yükselt.", videoUrl = "https://www.youtube.com/shorts/yg3KQQn3QWg"),
                Exercise("Isınma: Kol ve Gövde Çevirme", "Isınma: Kol ve Gövde Çevirme", 1, "1 Dakika", 0, "Genel Hazırlık: Eklemleri mobilize et.", videoUrl = "https://youtube.com/shorts/XTbPqeswd-Y?si=iFCQ3OLnreaISGHs"),
                Exercise("Isınma: Duvarda Şınav", "Isınma: Duvarda Şınav", 1, "15", 30, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/JlrVJaPn5o4?si=I5FTmjQz1kFGerLH"),
                Exercise("Isınma: Band Pull Apart", "Isınma: Band Pull Apart", 1, "15", 30, "Güne Özel Isınma.", videoUrl = "https://www.youtube.com/shorts/SuvO4TBwSu4"),
                Exercise("Isınma: Machine Chest Press (Hafif)", "Isınma: Machine Chest Press (Hafif)", 1, "15", 60, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/Qu7-ceCvq7w?si=6-d0UPgJM_m8V8k0"),
                Exercise("Machine Chest Press", "Machine Chest Press", 3, "8", 120, "Nasıl: İstasyonun oturma koltuğunu öyle ayarla ki, tutacaklar göğüs ucunla aynı hizada olsun. Sırtını arkaya yapıştır. Nefes vererek kolları ileri it, dirsekleri kilitlemeden yavaşça geri sal.\n\nDikkat: Omuzlarını yukarı (kulaklarına) kaldırma, aşağı bastır.", videoUrl = "https://youtube.com/shorts/Qu7-ceCvq7w?si=6-d0UPgJM_m8V8k0"),
                Exercise("Push-Up (Şınav)", "Push-Up (Şınav)", 3, "MAX", 45, "Nasıl: Klasik şınav.\n\nDikkat: Senin için 40 tekrar kolaysa, sırtına ağırlık plakası al veya ayaklarını koltuğa koyarak (Decline) yap. Tekrar sayısı 12-15'e düşsün, zorlan.", videoUrl = "https://youtube.com/shorts/4Bc1tPaYkOo?si=Rv42xhYUB6J5Njcn"),
                Exercise("Pec Deck Fly (Kelebek)", "Pec Deck Fly (Kelebek)", 3, "12", 60, "Nasıl: İstasyonun kelebek aparatına otur. Dirseklerin omuz hizasında olsun. Kolları göğüsünün ortasında birleştir ve 1 saniye sıkıştır.\n\nDikkat: Kolları geriye açarken göğüs kasının esnediğini hisset ama omuz eklemini zorlayacak kadar geriye götürme.", videoUrl = "https://youtube.com/shorts/a9vQ_hwIksU?si=xJwxeKNymumzvX4d"),
                Exercise("Barbell Overhead Press", "Barbell Overhead Press", 3, "8", 120, "Nasıl: Ayakta, barı göğüs üstünde tut. Başının üzerine dimdik it. İndirirken chneni sıyırarak indir.\n\nDikkat: Belini geriye doğru bükme (yaylanma), karın kaslarını sık.", videoUrl = "https://youtube.com/shorts/zoN5EH50Dro?si=s23erUT09mzws4DO"),
                Exercise("Cable Lateral Raise", "Cable Lateral Raise", 3, "15", 45, "Nasıl: İstasyonun Alt Makarasını kullan. D şeklindeki kulpu tak. Yan dön, makaraya uzak olan elinle kulpu çek ve kolunu yana doğru aç.\n\nDikkat: Dambıldan daha etkilidir çünkü inerken de direnç bitmez. Hareketi yavaş yap.", videoUrl = "https://youtube.com/shorts/9ilIKuy6B0g?si=jAQfJuDXp34oTCjF"),
                Exercise("Triceps Pushdown", "Triceps Pushdown", 3, "12", 60, "Nasıl: İstasyonun Üst Makarasını kullan. Kısa barı veya halatı tak. Dirseklerini gövdene yapıştır. Sadece ön kollarını aşağı iterek kilitle.\n\nDikkat: Dirseklerin ileri-geri oynamasın, menteşe gibi sabit kalsın.", videoUrl = "https://youtube.com/shorts/1FjkhpZsaxc?si=LbZPNuVXeD_AaU3S")
            )
        ),
        Workout(
            title = "2. GÜN: PULL (Sırt, Trapez, Arka Omuz, Pazu)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks", "Isınma: Jumping Jacks", 1, "1 Dakika", 0, "Genel Hazırlık.", videoUrl = "https://www.youtube.com/shorts/yg3KQQn3QWg"),
                Exercise("Isınma: Kol ve Gövde Çevirme", "Isınma: Kol ve Gövde Çevirme", 1, "1 Dakika", 0, "Genel Hazırlık.", videoUrl = "https://youtube.com/shorts/XTbPqeswd-Y?si=iFCQ3OLnreaISGHs"),
                Exercise("Isınma: Kedi-Deve", "Isınma: Kedi-Deve", 1, "10", 30, "Güne Özel Isınma.", videoUrl = "https://www.youtube.com/shorts/aHZ2O2lM8Zs"),
                Exercise("Isınma: Scapular Push-Up", "Isınma: Scapular Push-Up", 1, "10", 30, "Güne Özel Isınma.", videoUrl = "https://www.youtube.com/shorts/nw-FIMwCkLs"),
                Exercise("Isınma: Bent Over Row (Boş Bar)", "Isınma: Bent Over Row (Boş Bar)", 1, "15", 60, "Güne Özel Isınma.", videoUrl = "https://www.youtube.com/shorts/phVtqawIgbk"),
                Exercise("Barbell Bent Over Row", "Barbell Bent Over Row", 3, "8-10", 120, "Nasıl: Dizleri kır, gövdeyi 45 derece eğ. Bel düz. Barı göbek deliğine çek.\n\nDikkat: Belini kamburlaştırma. Çekerken kürek kemiklerini birbirine değdirmeye çalış.", videoUrl = "https://www.youtube.com/shorts/phVtqawIgbk"),
                Exercise("Lat Pulldown (Sırta Çekiş)", "Lat Pulldown (Sırta Çekiş)", 3, "10", 90, "Nasıl: İstasyonun Üst Makarasını ve uzun barı kullan. Geniş tut. Göğsünü yukarı doğru kabartarak barı köprücük kemiğine çek.\n\nDikkat: Barı ensene değil, göğsüne çek.", videoUrl = "https://youtube.com/shorts/5s6KGLTMgoI?si=Z4yqR1wB0IwUDYr9"),
                Exercise("One Arm Dumbbell Row", "One Arm Dumbbell Row", 3, "10-12", 60, "Nasıl: Elini ve dizini sehpaya daya. Dambılı kalçana doğru geriye çek.\n\nDikkat: Gövdeni döndürme, sadece kolun çalışsın.", videoUrl = "https://www.youtube.com/shorts/yHqqGd0tXcw"),
                Exercise("Face Pull (Yüze Çekiş)", "Face Pull (Yüze Çekiş)", 3, "15", 45, "Nasıl: İstasyonun Üst Makarasını kullan. Halatı tak. Dirseklerini dışarı açarak halatın ortasını alnına doğru çek.\n\nDikkat: Çekerken ellerin kulaklarının gerisine gitsin.", videoUrl = "https://youtube.com/shorts/IeOqdw9WI90?si=j90ejN73PaXegIYr"),
                Exercise("Dumbbell Shrug (Omuz Silkme)", "Dumbbell Shrug (Omuz Silkme)", 3, "15", 60, "Nasıl: Eline ağır dambılları al. Kollar dümdüz. Omuzlarını kulaklarına çek, 1 saniye bekle, indir.\n\nDikkat: Dirseklerini bükme.", videoUrl = "https://www.youtube.com/shorts/rFsSeClGnNA"),
                Exercise("Barbell Curl (Halter Pazu)", "Barbell Curl (Halter Pazu)", 3, "8-10", 90, "Nasıl: Ayakta dik dur, barı omuz hizasında tut. Göğse doğru kaldır.\n\nDikkat: Vücudunu sallayarak kaldırma.", videoUrl = "https://www.youtube.com/shorts/54x2WF1_Suc"),
                Exercise("Dumbbell Hammer Curl", "Dumbbell Hammer Curl", 3, "12", 45, "Nasıl: Avuç içleri birbirine bakacak şekilde tut.\n\nDikkat: Ön kol kaslarını sıkarak kaldır.", videoUrl = "https://www.youtube.com/shorts/qmQkt1Y-FX8")
            )
        ),
        Workout(
            title = "3. GÜN: LEGS (Bacak Hacmi)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks", "Isınma: Jumping Jacks", 1, "1 Dakika", 0, "Genel Hazırlık.", videoUrl = "https://www.youtube.com/shorts/yg3KQQn3QWg"),
                Exercise("Isınma: Kol ve Gövde Çevirme", "Isınma: Kol ve Gövde Çevirme", 1, "1 Dakika", 0, "Genel Hazırlık.", videoUrl = "https://youtube.com/shorts/XTbPqeswd-Y?si=iFCQ3OLnreaISGHs"),
                Exercise("Isınma: Leg Swing", "Isınma: Leg Swing", 1, "30 Saniye", 30, "Güne Özel Isınma.", videoUrl = "https://www.youtube.com/shorts/SzOYrfTZDSM"),
                Exercise("Isınma: Bodyweight Squat", "Isınma: Bodyweight Squat", 1, "20 Tekrar", 30, "Güne Özel Isınma.", videoUrl = "https://www.youtube.com/shorts/-5LhNSMBrEs"),
                Exercise("Isınma: Barbell Squat (Hafif)", "Isınma: Barbell Squat (Hafif)", 1, "10", 60, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/dW3zj79xfrc?si=RVZk3Z4kA1dQtEs8"),
                Exercise("Barbell Squat", "Barbell Squat", 3, "8", 180, "Nasıl: Bar sırtta. Kalçayı geriye atarak çök. Paralel olunca kalk.\n\nDikkat: Dizlerin içe çökmesin. Topukların yerden kalkmasın.", videoUrl = "https://youtube.com/shorts/dW3zj79xfrc?si=RVZk3Z4kA1dQtEs8"),
                Exercise("Romanian Deadlift (RDL)", "Romanian Deadlift (RDL)", 3, "10", 120, "Nasıl: Dizler hafif kırık ve sabit. Kalçayı geriye itarak eğil. Bar diz altına inince kalk.\n\nDikkat: Sırtın masa gibi düz olsun.", videoUrl = "https://youtube.com/shorts/g5u75sgpn04?si=bKSytaZ7dI3C_Rmu"),
                Exercise("Dumbbell Walking Lunge", "Dumbbell Walking Lunge", 3, "12 Adım", 90, "Nasıl: Dambılları al, büyük adımlarla yürü.\n\nDikkat: Arkadaki dizini yere sert vurma.", videoUrl = "https://youtube.com/shorts/mJilHWIBWO8?si=WSVoS1QeYLqzTt4D"),
                Exercise("Leg Extension (Ön Bacak)", "Leg Extension (Ön Bacak)", 3, "15", 60, "Nasıl: İstasyonun bacak aparatına otur. Bacaklarını dümdüz olana kadar kaldır.\n\nDikkat: Tepe noktada uyluk kaslarını sık. Hızlı indirip kaldırma.", videoUrl = "https://www.youtube.com/shorts/iQ92TuvBqRo"),
                Exercise("Makine Leg Curl (Arka Bacak)", "Makine Leg Curl (Arka Bacak)", 3, "12-15", 60, "Nasıl: Topuklarını kalçana doğru çek.\n\nDikkat: Kalçanı sabit tut, sadece dizden altını hareket ettir.", videoUrl = "https://youtube.com/shorts/IwcLkHuH7iw?si=4C4VNNUmb37LeUoj"),
                Exercise("Standing Calf Raise", "Standing Calf Raise", 4, "20", 30, "Nasıl: Parmak ucunda yüksel.", videoUrl = "https://youtube.com/shorts/3CxRItkntZU?si=Zf2ccGIzUcuT1aMa")
            )
        ),
        Workout(
            title = "5. GÜN: UPPER (Hacim & Estetik)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks", "Isınma: Jumping Jacks", 1, "1 Dakika", 0, "Genel Hazırlık.", videoUrl = "https://www.youtube.com/shorts/yg3KQQn3QWg"),
                Exercise("Isınma: Kol ve Gövde Çevirme", "Isınma: Kol ve Gövde Çevirme", 1, "1 Dakika", 0, "Genel Hazırlık.", videoUrl = "https://youtube.com/shorts/XTbPqeswd-Y?si=iFCQ3OLnreaISGHs"),
                Exercise("Isınma: Duvarda Melek", "Isınma: Duvarda Melek", 1, "10", 30, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/BdEXk-wHyfE?si=UlGCkFs5iHEw-HIo"),
                Exercise("Isınma: Band Pull Apart", "Isınma: Band Pull Apart", 1, "15", 30, "Güne Özel Isınma.", videoUrl = "https://www.youtube.com/shorts/SuvO4TBwSu4"),
                Exercise("Isınma: Incline Dumble Pres", "Isınma: Incline Dumble Pres", 1, "15", 60, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/8fXfwG4ftaQ?si=D8dgZVCMzCeIGEbO"),
                Exercise("Incline Dumble Press", "Incline Dumble Press", 3, "10", 90, "Nasıl: Sehpanı 30-45 derece eğime getir. Üst göğüs için pres yap.\n\nDikkat: Düz bench'ten biraz daha hafif kilo kullanman normaldir.", videoUrl = "https://youtube.com/shorts/8fXfwG4ftaQ?si=D8dgZVCMzCeIGEbO"),
                Exercise("Seated Cable Row", "Seated Cable Row", 3, "10", 90, "Nasıl: İstasyonun Alt Makarasını kullan. Kısa barı karnına çek.\n\nDikkat: Gövden dik dursun, belinden güç alma.", videoUrl = "https://youtube.com/shorts/qD1WZ5pSuvk?si=18FaHJS9iG1xsI0W"),
                Exercise("Dumbbell Shoulder Press", "Dumbbell Shoulder Press", 3, "10", 90, "Nasıl: Baş üstüne it.", videoUrl = "https://www.youtube.com/watch?v=k6tzKisR3NY"),
                Exercise("Dumbbell Lateral Raise", "Dumbbell Lateral Raise", 3, "15", 45, "Yana açış.", videoUrl = "https://youtube.com/shorts/Kl3LEzQ5Zqs?si=lWGYxHfNqjO5_dfj"),
                Exercise("Lat Pulldown", "Lat Pulldown", 3, "10", 90, "Hacim için tekrar.", videoUrl = "https://youtube.com/shorts/5s6KGLTMgoI?si=Z4yqR1wB0IwUDYr9"),
                Exercise("SS: Preacher Curl", "SS: Preacher Curl", 3, "12", 0, "SÜPER SET (A): Dinlenmeden Arka Kola geç.", videoUrl = "https://youtube.com/shorts/0y4tdUNPdlE?si=A1TgSRRq_ASo5gwe"),
                Exercise("SS: Triceps Pushdown", "SS: Triceps Pushdown", 3, "12", 90, "SÜPER SET (B): İkisi bitince 90sn dinlen.", videoUrl = "https://youtube.com/shorts/1FjkhpZsaxc?si=LbZPNuVXeD_AaU3S")
            )
        ),
        Workout(
            title = "6. GÜN: LOWER & GÜÇ (Saf Güç)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks", "Isınma: Jumping Jacks", 1, "1 Dakika", 0, "Genel Hazırlık.", videoUrl = "https://www.youtube.com/shorts/yg3KQQn3QWg"),
                Exercise("Isınma: Kol ve Gövde Çevirme", "Isınma: Kol ve Gövde Çevirme", 1, "1 Dakika", 0, "Genel Hazırlık.", videoUrl = "https://youtube.com/shorts/XTbPqeswd-Y?si=iFCQ3OLnreaISGHs"),
                Exercise("Isınma: World's Greatest Stretch", "Isınma: World's Greatest Stretch", 1, "10", 30, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/7XheaZERvBQ?si=TC5pt8aobQxZGyFr"),
                Exercise("Isınma: Glute Bridge", "Isınma: Glute Bridge", 1, "15", 30, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/X_IGw8U_e38?si=hYTSnQzxaAGiVL7t"),
                Exercise("Isınma: Deadlift (Hafif)", "Isınma: Deadlift (Hafif)", 1, "5", 60, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/xNwpvDuZJ3k?si=kgUyR4A0bz6QrtOg"),
                Exercise("Deadlift (Klasik Halter)", "Deadlift (Klasik Halter)", 3, "6", 180, "Nasıl: Bar yerde. Sırt düz, yeri iterek kalk.\n\nDikkat: Belini asla bükülmesin. Programın en ağır hareketi budur.", videoUrl = "https://youtube.com/shorts/xNwpvDuZJ3k?si=kgUyR4A0bz6QrtOg"),
                Exercise("Goblet Squat", "Goblet Squat", 3, "12", 90, "Nasıl: Tek dambılı göğsünde tut. Dik durarak çök.", videoUrl = "https://youtube.com/shorts/lRYBbchqxtI?si=b9Q8TBV6suhit1_d"),
                Exercise("Makine Leg Curl (Arka Bacak)", "Makine Leg Curl (Arka Bacak)", 3, "12", 60, "Arka bacak.", videoUrl = "https://youtube.com/shorts/IwcLkHuH7iw?si=4C4VNNUmb37LeUoj"),
                Exercise("Calf Raise", "Calf Raise", 4, "20", 30, "Baldır.", videoUrl = "https://youtube.com/shorts/sNqa1ad2qIQ?si=8jmom5GpjgLiqRq2")
            )
        )
    )

    // Program 2: Calisthenics + Ağırlık - MASTER PROGRAM
    val calisthenicsProgram: List<Workout> = listOf(
        Workout(
            title = "1. GÜN: PUSH (Göğüs, Omuz, Arka Kol)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks", "Isınma: Jumping Jacks", 1, "1 Dakika", 0, "Nasıl: Ayaklar bitişik, kollar yanda başla. Zıplayarak ayakları açarken elleri baş üstünde birleştir.", videoUrl = "https://www.youtube.com/shorts/yg3KQQn3QWg"),
                Exercise("Isınma: Kol ve Gövde Çevirme", "Isınma: Kol ve Gövde Çevirme", 1, "1 Dakika", 0, "Nasıl: Ayaklar sabit, gövdeni sağa sola döndür. Kollarını geniş daireler çizerek çevir.", videoUrl = "https://youtube.com/shorts/XTbPqeswd-Y?si=iFCQ3OLnreaISGHs"),
                Exercise("Isınma: Duvarda Şınav", "Isınma: Duvarda Şınav", 1, "15", 30, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/JlrVJaPn5o4?si=I5FTmjQz1kFGerLH"),
                Exercise("Isınma: Band Pull Apart", "Isınma: Band Pull Apart", 1, "15", 30, "Güne Özel Isınma.", videoUrl = "https://www.youtube.com/shorts/SuvO4TBwSu4"),
                Exercise("Dips Statik Bekleme", "Dips Statik Bekleme", 1, "15sn", 15, "Püf Nokta: Sadece havada asılı kal, inme.", videoUrl = "https://youtube.com/shorts/IeLVdVg4Ccw?si=X0Ql8Kt4XgPankIv"),
                Exercise("Şınav (Isınma)", "Şınav (Isınma)", 1, "10", 60, "Püf Nokta: Yavaş, kası hisset.", videoUrl = "https://youtube.com/shorts/4Bc1tPaYkOo?si=Rv42xhYUB6J5Njcn"),
                Exercise("Dips (Paralel Bar / Sandalye)", "Dips (Paralel Bar / Sandalye)", 3, "MAX", 120, "Nasıl: İki barın/sandalyenin üzerine çık. Gövdeni öne doğru eğ. Dirsekler 90 derece olana kadar in, nefes vererek it.\n\nPüf Nokta: Omuzlarını kulaklarından uzak tut, boynunu içine gömme.", videoUrl = "https://youtube.com/shorts/dij0l_pL0z8?si=W-aFDIlrKulxDHIj"),
                Exercise("Decline Push-Up", "Decline Push-Up", 3, "12-15", 90, "Nasıl: Ayaklarını koltuğa koy, ellerin yerde. Vücudun dümdüz olsun. Göğsün yere değene kadar in.\n\nPüf Nokta: Kalçanı düşürme, karın kaslarını sıkı tut.", videoUrl = "https://youtu.be/5QFjmotLfW4?si=qhQ8rO2Tws69jahV"),
                Exercise("Dumbbell Shoulder Press (Ayakta)", "Dumbbell Shoulder Press (Ayakta)", 3, "8-10", 120, "Nasıl: Ayakta dik dur, dambılları kulak hizasında tut. Başının üzerine doğru it.\n\nPüf Nokta: Belini aşırı çukurlaştırma. Dambılları tepede birbirine çarptırma.", videoUrl = "https://www.youtube.com/watch?v=k6tzKisR3NY"),
                Exercise("Dumbbell Lateral Raise", "Dumbbell Lateral Raise", 3, "15", 60, "Nasıl: Dambılları yanlarda tut. Dirsekleri hafif kır. Kolları yanlara omuz hizasına kadar aç.\n\nPüf Nokta: Serçe parmağın tavanı göstersin. Gövdeni sallayarak hile yapma.", videoUrl = "https://youtube.com/shorts/Kl3LEzQ5Zqs?si=lWGYxHfNqjO5_dfj"),
                Exercise("Dumbbell Skullcrusher", "Dumbbell Skullcrusher", 3, "10-12", 60, "Nasıl: Yere sırtüstü yat, dambılları havaya kaldır. Dirseklerin yerini sabitle, sadece ön kollarını bükerek dambılları kulak yanına indir.\n\nPüf Nokta: Dirseklerin dışarı açılmasın, hep tavanı göstersin.", videoUrl = "https://www.youtube.com/shorts/K3mFeNz4e3w")
            )
        ),
        Workout(
            title = "2. GÜN: PULL (Sırt, Trapez, Arka Omuz, Pazu)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks", "Isınma: Jumping Jacks", 1, "1 Dakika", 0, "Genel Hazırlık.", videoUrl = "https://www.youtube.com/shorts/yg3KQQn3QWg"),
                Exercise("Isınma: Kol ve Gövde Çevirme", "Isınma: Kol ve Gövde Çevirme", 1, "1 Dakika", 0, "Genel Hazırlık.", videoUrl = "https://youtube.com/shorts/XTbPqeswd-Y?si=iFCQ3OLnreaISGHs"),
                Exercise("Isınma: Kedi-Deve", "Isınma: Kedi-Deve", 1, "10", 30, "Güne Özel Isınma.", videoUrl = "https://www.youtube.com/shorts/aHZ2O2lM8Zs"),
                Exercise("Dead Hang (Asılı Bekleme)", "Dead Hang (Asılı Bekleme)", 1, "20sn", 30, "Püf Nokta: Omurgayı açar.", videoUrl = "https://www.youtube.com/shorts/XPcT3capkyk"),
                Exercise("Scapular Pull-Up", "Scapular Pull-Up", 1, "10", 30, "Püf Nokta: Dirsek kırmadan kendini omuzdan yukarı çek.", videoUrl = "https://www.youtube.com/shorts/nw-FIMwCkLs"),
                Exercise("Hafif Dumbbell Row", "Hafif Dumbbell Row", 1, "12", 60, "Püf Nokta: Sırtı kanatlandır.", videoUrl = "https://www.youtube.com/shorts/0qhkxmnS-9Q"),
                Exercise("Pull-Up (Barfiks)", "Pull-Up (Barfiks)", 3, "MAX", 150, "Nasıl: Avuç içleri karşıya baksın. Göğsünü bara değdirmeye çalışarak çek.\n\nPüf Nokta: Çıkamıyorsan zıplayarak çık, çok yavaş (3-4 saniyede) aşağı in (Negatif Tekrar).", videoUrl = "https://www.youtube.com/shorts/ym1V5H35IpA"),
                Exercise("Barbell Bent Over Row", "Barbell Bent Over Row", 3, "8-10", 120, "Nasıl: Dizleri kır, gövdeyi 45 derece eğ. Bel düz. Barı göbek deliğine çek.\n\nPüf Nokta: Çekerken kürek kemiklerini birbirine değdirmeye çalış.", videoUrl = "https://www.youtube.com/shorts/phVtqawIgbk"),
                Exercise("One Arm Dumbbell Row", "One Arm Dumbbell Row", 3, "10-12", 60, "Nasıl: Elini ve dizini sehpaya daya. Dambılı kalçana doğru geriye çek.\n\nPüf Nokta: Gövdeni döndürme, sadece kolun çalışsın.", videoUrl = "https://www.youtube.com/shorts/yHqqGd0tXcw"),
                Exercise("Dumbbell Rear Delt Fly", "Dumbbell Rear Delt Fly", 3, "15", 60, "Nasıl: Öne eğil. Dambılları kuş gibi yanlara aç.\n\nPüf Nokta: Kürek kemiklerini değil, sadece arka omuz başlarını hisset.", videoUrl = "https://www.youtube.com/shorts/LsT-bR_zxLo"),
                Exercise("Dumbbell Shrug (Omuz Silkme)", "Dumbbell Shrug (Omuz Silkme)", 3, "15", 60, "Nasıl: Dik dur, ağır dambılları al. Omuzlarını kulaklarına çek, 1 sn bekle, indir.\n\nPüf Nokta: Dirseklerini bükme.", videoUrl = "https://www.youtube.com/shorts/rFsSeClGnNA"),
                Exercise("Barbell Curl (Halter Pazu)", "Barbell Curl (Halter Pazu)", 3, "8-10", 90, "Nasıl: Ayakta dik dur, barı omuz hizasında tut. Göğse doğru kaldır.\n\nPüf Nokta: Dirseklerini vücuduna yapıştır, ileri-geri oynatma.", videoUrl = "https://www.youtube.com/shorts/54x2WF1_Suc"),
                Exercise("Dumbbell Hammer Curl", "Dumbbell Hammer Curl", 3, "12", 60, "Nasıl: Avuç içleri birbirine bakacak şekilde tut. Omuz başlarına doğru kaldır.\n\nPüf Nokta: Bu hareket kolun dışını ve ön kolu doldurur, yavaş yap.", videoUrl = "https://www.youtube.com/shorts/qmQkt1Y-FX8")
            )
        ),
        Workout(
            title = "3. GÜN: LEGS (Bacak Hacmi)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks", "Isınma: Jumping Jacks", 1, "1 Dakika", 0, "Genel Hazırlık.", videoUrl = "https://www.youtube.com/shorts/yg3KQQn3QWg"),
                Exercise("Isınma: Kol ve Gövde Çevirme", "Isınma: Kol ve Gövde Çevirme", 1, "1 Dakika", 0, "Genel Hazırlık.", videoUrl = "https://youtube.com/shorts/XTbPqeswd-Y?si=iFCQ3OLnreaISGHs"),
                Exercise("Isınma: Leg Swing", "Isınma: Leg Swing", 1, "30 Saniye", 30, "Güne Özel Isınma.", videoUrl = "https://www.youtube.com/shorts/SzOYrfTZDSM"),
                Exercise("Isınma: Bodyweight Squat", "Isınma: Bodyweight Squat", 1, "20 Tekrar", 30, "Güne Özel Isınma.", videoUrl = "https://www.youtube.com/shorts/-5LhNSMBrEs"),
                Exercise("Barbell Squat (Isınma)", "Barbell Squat (Isınma)", 1, "10", 60, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/dW3zj79xfrc?si=RVZk3Z4kA1dQtEs8"),
                Exercise("Barbell Squat", "Barbell Squat", 3, "8", 180, "Nasıl: Bar sırtta. Kalçayı geriye atarak sandalyeye oturur gibi çök. Paralel olunca kalk.\n\nPüf Nokta: Dizlerin içe çökmesin, dışa doğru baskı yap. Topukların yerden kalkmasın.", videoUrl = "https://youtube.com/shorts/dW3zj79xfrc?si=RVZk3Z4kA1dQtEs8"),
                Exercise("Romanian Deadlift (RDL)", "Romanian Deadlift (RDL)", 3, "10", 120, "Nasıl: Dizler hafif kırık ve sabit. Kalçayı geriye iterek eğil. Bar diz altına inince kalk.\n\nPüf Nokta: Bar bacaklarına hep temas etsin. Sırtın asla kamburlaşmasın.", videoUrl = "https://youtube.com/shorts/g5u75sgpn04?si=bKSytaZ7dI3C_Rmu"),
                Exercise("Dumbbell Walking Lunge", "Dumbbell Walking Lunge", 3, "12 Adım", 90, "Nasıl: Dambılları al, büyük adımlarla yürü. Arkadaki diz yere yaklaşsın.\n\nPüf Nokta: Gövden dik dursun, öne yığılma.", videoUrl = "https://youtube.com/shorts/mJilHWIBWO8?si=WSVoS1QeYLqzTt4D"),
                Exercise("Lying Dumbbell Leg Curl", "Lying Dumbbell Leg Curl", 3, "12-15", 90, "Nasıl: Yüzüstü yat. Dambılı iki ayağının tabanı arasına sıkıştır. Topuklarını kalçana çek.\n\nPüf Nokta: Kalçanı yerden kalkmasın. Hareketi yavaş yap.", videoUrl = "https://youtube.com/shorts/u3vE9ErSlLQ?si=-fKQAQ3eQ25y93CJ"),
                Exercise("Single Leg Calf Raise", "Single Leg Calf Raise", 4, "20", 45, "Nasıl: Tek ayakla basamakta parmak ucunda yüksel.\n\nPüf Nokta: Topuğu iyice aşağı esnet, sonra en tepeye çık.", videoUrl = "https://youtube.com/shorts/3CxRItkntZU?si=Zf2ccGIzUcuT1aMa")
            )
        ),
        Workout(
            title = "5. GÜN: UPPER (Estetik & Omuz Başları)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks", "Isınma: Jumping Jacks", 1, "1 Dakika", 0, "Genel Hazırlık.", videoUrl = "https://www.youtube.com/shorts/yg3KQQn3QWg"),
                Exercise("Isınma: Kol ve Gövde Çevirme", "Isınma: Kol ve Gövde Çevirme", 1, "1 Dakika", 0, "Genel Hazırlık.", videoUrl = "https://youtube.com/shorts/XTbPqeswd-Y?si=iFCQ3OLnreaISGHs"),
                Exercise("Isınma: Duvarda Melek", "Isınma: Duvarda Melek", 1, "10", 30, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/BdEXk-wHyfE?si=UlGCkFs5iHEw-HIo"),
                Exercise("Isınma: Band Pull Apart", "Isınma: Band Pull Apart", 1, "15", 30, "Güne Özel Isınma.", videoUrl = "https://www.youtube.com/shorts/SuvO4TBwSu4"),
                Exercise("Pike Push-Up (V-Şınav)", "Pike Push-Up (V-Şınav)", 3, "10-12", 90, "Nasıl: Şınav pozisyonunda kalçayı havaya dik (Ters V). Kafanı yere doğru indir, it.\n\nPüf Nokta: Dirsekler dışarı değil, geriye baksın. Omuzlarını hisset.", videoUrl = "https://youtube.com/shorts/_VoWb1Tn2kc?si=1OVhXioq4T4miOQr"),
                Exercise("Chin-Up", "Chin-Up", 3, "MAX", 120, "Nasıl: Avuç içleri yüzüne baksın. Kendini yukarı çek.\n\nPüf Nokta: Bu tutuş bicepsleri daha çok çalıştırır. Tepe noktada bekle.", videoUrl = "https://youtube.com/shorts/Oi3bW9nQmGI?si=amffcv9Oo15VV1fD"),
                Exercise("Dumbbell Floor Press", "Dumbbell Floor Press", 3, "10-12", 90, "Nasıl: Yere sırtüstü yat. Dirsekler yere değene kadar indir, göğsü sıkarak it.\n\nPüf Nokta: Dirsekler yere değdiğinde dinlenme, hemen geri it.", videoUrl = "https://youtube.com/shorts/WbCEvFA0NJs?si=tAVOjMUNYO70j71v"),
                Exercise("Dumbbell Lateral Raise", "Dumbbell Lateral Raise", 3, "15", 60, "Pazartesi günüyle aynı teknik.", videoUrl = "https://youtube.com/shorts/Kl3LEzQ5Zqs?si=lWGYxHfNqjO5_dfj"),
                Exercise("SS: Hammer Curl", "SS: Hammer Curl", 3, "12", 0, "SÜPER SET (A): Dinlenmeden Bench Dips'e geç.", videoUrl = "https://www.youtube.com/shorts/qmQkt1Y-FX8"),
                Exercise("SS: Bench Dips", "SS: Bench Dips", 3, "12", 90, "SÜPER SET (B): Tur bitince 90sn dinlen.\n\nPüf Nokta: Kanı kola hapsedeceğiz.", videoUrl = "https://youtube.com/shorts/dij0l_pL0z8?si=W-aFDIlrKulxDHIj")
            )
        ),
        Workout(
            title = "6. GÜN: LOWER & GÜÇ (Saf Güç)",
            exercises = listOf(
                Exercise("Isınma: Jumping Jacks", "Isınma: Jumping Jacks", 1, "1 Dakika", 0, "Genel Hazırlık.", videoUrl = "https://www.youtube.com/shorts/yg3KQQn3QWg"),
                Exercise("Isınma: Kol ve Gövde Çevirme", "Isınma: Kol ve Gövde Çevirme", 1, "1 Dakika", 0, "Genel Hazırlık.", videoUrl = "https://youtube.com/shorts/XTbPqeswd-Y?si=iFCQ3OLnreaISGHs"),
                Exercise("Isınma: World's Greatest Stretch", "Isınma: World's Greatest Stretch", 1, "10", 30, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/7XheaZERvBQ?si=TC5pt8aobQxZGyFr"),
                Exercise("Isınma: Glute Bridge", "Isınma: Glute Bridge", 1, "15", 30, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/X_IGw8U_e38?si=hYTSnQzxaAGiVL7t"),
                Exercise("Deadlift (Isınma)", "Deadlift (Isınma)", 1, "5", 60, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/xNwpvDuZJ3k?si=kgUyR4A0bz6QrtOg"),
                Exercise("Deadlift (Klasik)", "Deadlift (Klasik)", 3, "6", 180, "Nasıl: Bar ayak bağcıklarının üzerinde. Kalçayı geriye at, eğil, barı tut. Sırt dümdüz. Yeri iterek kalk.\n\nPüf Nokta: Barı kollarınla çekme, bacaklarınla yeri it. Kalkınca kalçanı sık.", videoUrl = "https://youtube.com/shorts/xNwpvDuZJ3k?si=kgUyR4A0bz6QrtOg"),
                Exercise("Goblet Squat", "Goblet Squat", 3, "12", 90, "Nasıl: Tek dambılı kadeh gibi göğsünde tut. Dik durarak çök.\n\nPüf Nokta: Dirseklerin dizlerinin içine girmesine izin ver. Derin çök.", videoUrl = "https://youtube.com/shorts/lRYBbchqxtI?si=b9Q8TBV6suhit1_d"),
                Exercise("Lying Dumbbell Leg Curl", "Lying Dumbbell Leg Curl", 3, "12", 90, "Çarşamba günüyle aynı teknik. Deadlift sonrası beli yormamak için.", videoUrl = "https://youtube.com/shorts/u3vE9ErSlLQ?si=-fKQAQ3eQ25y93CJ"),
                Exercise("Standing Calf Raise", "Standing Calf Raise", 4, "20", 45, "Baldır.", videoUrl = "https://youtube.com/shorts/sNqa1ad2qIQ?si=8jmom5GpjgLiqRq2")
            )
        )
    )
}
