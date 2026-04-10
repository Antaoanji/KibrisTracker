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
                Exercise("Isınma: Duvarda Şınav", "Isınma: Duvarda Şınav", 1, "15", 0, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/JlrVJaPn5o4?si=I5FTmjQz1kFGerLH"),
                Exercise("Isınma: Band Pull Apart", "Isınma: Band Pull Apart", 1, "15", 0, "Güne Özel Isınma.", videoUrl = "https://www.youtube.com/shorts/SuvO4TBwSu4"),
                Exercise("Isınma: Machine Chest Press (Hafif)", "Isınma: Machine Chest Press (Hafif)", 1, "15", 0, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/Qu7-ceCvq7w?si=6-d0UPgJM_m8V8k0"),
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
                Exercise("Isınma: Kedi-Deve", "Isınma: Kedi-Deve", 1, "10", 0, "Güne Özel Isınma.", videoUrl = "https://www.youtube.com/shorts/aHZ2O2lM8Zs"),
                Exercise("Isınma: Scapular Push-Up", "Isınma: Scapular Push-Up", 1, "10", 0, "Güne Özel Isınma.", videoUrl = "https://www.youtube.com/shorts/nw-FIMwCkLs"),
                Exercise("Isınma: Bent Over Row (Boş Bar)", "Isınma: Bent Over Row (Boş Bar)", 1, "15", 0, "Güne Özel Isınma.", videoUrl = "https://www.youtube.com/shorts/phVtqawIgbk"),
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
                Exercise("Isınma: Leg Swing", "Isınma: Leg Swing", 1, "30 Saniye", 0, "Güne Özel Isınma.", videoUrl = "https://www.youtube.com/shorts/SzOYrfTZDSM"),
                Exercise("Isınma: Bodyweight Squat", "Isınma: Bodyweight Squat", 1, "20", 0, "Güne Özel Isınma.", videoUrl = "https://www.youtube.com/shorts/-5LhNSMBrEs"),
                Exercise("Isınma: Barbell Squat (Hafif)", "Isınma: Barbell Squat (Hafif)", 1, "10", 0, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/dW3zj79xfrc?si=RVZk3Z4kA1dQtEs8"),
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
                Exercise("Isınma: Duvarda Melek", "Isınma: Duvarda Melek", 1, "10", 0, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/BdEXk-wHyfE?si=UlGCkFs5iHEw-HIo"),
                Exercise("Isınma: Band Pull Apart", "Isınma: Band Pull Apart", 1, "15", 0, "Güne Özel Isınma.", videoUrl = "https://www.youtube.com/shorts/SuvO4TBwSu4"),
                Exercise("Isınma: Incline Dumble Pres", "Isınma: Incline Dumble Pres", 1, "15", 0, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/8fXfwG4ftaQ?si=D8dgZVCMzCeIGEbO"),
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
                Exercise("Isınma: World's Greatest Stretch", "Isınma: World's Greatest Stretch", 1, "10", 0, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/7XheaZERvBQ?si=TC5pt8aobQxZGyFr"),
                Exercise("Isınma: Glute Bridge", "Isınma: Glute Bridge", 1, "15", 0, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/X_IGw8U_e38?si=hYTSnQzxaAGiVL7t"),
                Exercise("Isınma: Deadlift (Hafif)", "Isınma: Deadlift (Hafif)", 1, "5", 0, "Güne Özel Isınma.", videoUrl = "https://youtube.com/shorts/xNwpvDuZJ3k?si=kgUyR4A0bz6QrtOg"),
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
            title = "1. GÜN: PUSH (Güç & Göğüs Odaklı)",
            exercises = listOf(
                Exercise("Isınma: Duvarda şınav", "Isınma: Duvarda şınav", 1, "15", 0, "Güne özel hazırlık.", videoUrl = "https://youtube.com/shorts/JlrVJaPn5o4?si=I5FTmjQz1kFGerLH"),
                Exercise("Isınma: Band Pull Apart", "Isınma: Band Pull Apart", 1, "15", 0, "Omuz mobilizasyonu.", videoUrl = "https://www.youtube.com/shorts/SuvO4TBwSu4"),
                Exercise("Isınma: Boş barla Bench Press", "Isınma: Boş barla Bench Press", 1, "15", 0, "Hareket formuna alışma.", videoUrl = "https://youtube.com/shorts/Qu7-ceCvq7w?si=6-d0UPgJM_m8V8k0"),
                Exercise("Flat Barbell Bench Press (0°)", "Flat Barbell Bench Press (0°)", 5, "5", 180, "Saf güç için ağır kiloyla çalış.\n\nDikkat: Sırtını hafif köprü yap, ayaklarını yere sağlam bas.", videoUrl = "https://youtube.com/shorts/Qu7-ceCvq7w?si=6-d0UPgJM_m8V8k0"),
                Exercise("Incline Dumbbell Press (30°)", "Incline Dumbbell Press (30°)", 3, "10-12", 120, "Üst göğüs odağı.\n\nNasıl: Dirseklerini çok dışa açma, hafifçe içeri (45 derece) al.", videoUrl = "https://youtube.com/shorts/8fXfwG4ftaQ?si=qHCDMGVuYPvYUV2G"),
                Exercise("Dips (Paralel Bar/Sandalye)", "Dips (Paralel Bar/Sandalye)", 3, "MAX", 120, "Göğüs ve arka kol için temel hareket.\n\nDikkat: Öne eğilerek göğüs aktivasyonunu artır.", videoUrl = "https://youtube.com/shorts/dij0l_pL0z8?si=W-aFDIlrKulxDHIj"),
                Exercise("Dumbbell Lateral Raise", "Dumbbell Lateral Raise", 4, "15-20", 60, "Omuz yan başları.\n\nDikkat: Dambılları çok yukarı savurma, omuz hizasında dur.", videoUrl = "https://youtube.com/shorts/Kl3LEzQ5Zqs?si=lWGYxHfNqjO5_dfj"),
                Exercise("Dumbbell Skullcrusher", "Dumbbell Skullcrusher", 3, "12", 90, "Arka kol (Triceps).\n\nNasıl: Dirseklerini sabitle, dambılları alnına değil kulağına doğru indir.", videoUrl = "https://www.youtube.com/shorts/K3mFeNz4e3w")
            )
        ),
        Workout(
            title = "2. GÜN: PULL (Genişlik & Pazu Hacmi)",
            exercises = listOf(
                Exercise("Isınma: Dead Hang", "Isınma: Dead Hang", 1, "20 Saniye", 0, "Omurgayı aç ve tutuşu güçlendir.", videoUrl = "https://www.youtube.com/shorts/XPcT3capkyk"),
                Exercise("Isınma: Scapular Pull-up", "Isınma: Scapular Pull-up", 1, "10", 0, "Sırt kaslarını aktif et.", videoUrl = "https://www.youtube.com/shorts/nw-FIMwCkLs"),
                Exercise("Isınma: Hafif DB Row", "Isınma: Hafif DB Row", 1, "12", 0, "Kanatları hazırla.", videoUrl = "https://www.youtube.com/shorts/0qhkxmnS-9Q"),
                Exercise("Pull-Up (Barfiks)", "Pull-Up (Barfiks)", 3, "MAX", 150, "Geniş bir sırt için temel.\n\nDikkat: Tam salınım yap (full ROM).", videoUrl = "https://www.youtube.com/shorts/ym1V5H35IpA"),
                Exercise("Barbell Bent Over Row", "Barbell Bent Over Row", 4, "8-10", 120, "Sırt kalınlığı.\n\nDikkat: Belini düz tut, barı göbeğine çek.", videoUrl = "https://www.youtube.com/shorts/phVtqawIgbk"),
                Exercise("One Arm Dumbbell Row", "One Arm Dumbbell Row", 3, "12", 90, "Tek taraflı güç.\n\nNasıl: Gövdeni döndürme, sadece kanadını hisset.", videoUrl = "https://www.youtube.com/shorts/yHqqGd0tXcw"),
                Exercise("Dumbbell Rear Delt Fly", "Dumbbell Rear Delt Fly", 3, "15", 60, "Arka omuz.\n\nDikkat: Serçe parmağını tavana doğru çevir.", videoUrl = "https://www.youtube.com/shorts/LsT-bR_zxLo"),
                Exercise("Barbell Curl", "Barbell Curl", 3, "8-10", 90, "Ağır ve kontrollü.\n\nDikkat: Dirseklerini vücuduna yapıştır.", videoUrl = "https://www.youtube.com/shorts/54x2WF1_Suc"),
                Exercise("Dumbbell Hammer Curl", "Dumbbell Hammer Curl", 3, "12", 60, "Pazu kalınlığı ve ön kol.", videoUrl = "https://www.youtube.com/shorts/qmQkt1Y-FX8")
            )
        ),
        Workout(
            title = "3. GÜN: LEGS (Hacim & Detay)",
            exercises = listOf(
                Exercise("Isınma: Leg Swing", "Isınma: Leg Swing", 1, "1 Dakika", 0, "Kalça mobilizasyonu.", videoUrl = "https://www.youtube.com/shorts/SzOYrfTZDSM"),
                Exercise("Isınma: Bodyweight Squat", "Isınma: Bodyweight Squat", 1, "20", 0, "Dizleri ve kalçayı ısıt.", videoUrl = "https://www.youtube.com/shorts/-5LhNSMBrEs"),
                Exercise("Isınma: Boş barla Squat", "Isınma: Boş barla Squat", 1, "15", 0, "Form kontrolü.", videoUrl = "https://youtube.com/shorts/dW3zj79xfrc?si=RVZk3Z4kA1dQtEs8"),
                Exercise("Barbell Squat", "Barbell Squat", 4, "8-10", 180, "Bacak hacmi için temel.\n\nDikkat: Topukların yerden kalkmasın.", videoUrl = "https://youtube.com/shorts/dW3zj79xfrc?si=RVZk3Z4kA1dQtEs8"),
                Exercise("Leg Extension (Ayak Aparatı)", "Leg Extension (Ayak Aparatı)", 3, "12-15", 90, "Ön bacak odağı.\n\nDikkat: Tepe noktada bacağını 1 sn sık.", videoUrl = "https://www.youtube.com/shorts/iQ92TuvBqRo"),
                Exercise("Lying Leg Curl (Ayak Aparatı)", "Lying Leg Curl (Ayak Aparatı)", 4, "12-15", 90, "Arka bacak.\n\nNasıl: Ayak aparatını topuklarına daya.", videoUrl = "https://youtube.com/shorts/IwcLkHuH7iw?si=4C4VNNUmb37LeUoj"),
                Exercise("Dumbbell Walking Lunge", "Dumbbell Walking Lunge", 3, "12 Adım", 120, "Dengeli güç.\n\nNasıl: Her bacak için 12 adım (Toplam 24).", videoUrl = "https://youtube.com/shorts/mJilHWIBWO8?si=WSVoS1QeYLqzTt4D"),
                Exercise("Calf Raise (Sehpada oturarak)", "Calf Raise (Sehpada oturarak)", 4, "20", 60, "Alt bacak.\n\nNasıl: Parmak uçlarında yüksel.", videoUrl = "https://youtube.com/shorts/3CxRItkntZU?si=Zf2ccGIzUcuT1aMa")
            )
        ),
        Workout(
            title = "5. GÜN: UPPER (Estetik & Biceps Zirve)",
            exercises = listOf(
                Exercise("Isınma: Duvarda Melek", "Isınma: Duvarda Melek", 1, "10", 0, "Duruş ve omuz hazırlığı.", videoUrl = "https://youtube.com/shorts/BdEXk-wHyfE?si=UlGCkFs5iHEw-HIo"),
                Exercise("Isınma: Hafif Incline Press", "Isınma: Hafif Incline Press", 1, "15", 0, "Üst göğsü hazırla.", videoUrl = "https://youtube.com/shorts/8fXfwG4ftaQ?si=D8dgZVCMzCeIGEbO"),
                Exercise("Incline Barbell Bench Press (30°)", "Incline Barbell Bench Press (30°)", 4, "8-10", 120, "Üst göğüs hacmi.\n\nNasıl: Barı köprücük kemiğine doğru indir.", videoUrl = "https://youtube.com/shorts/8fXfwG4ftaQ?si=D8dgZVCMzCeIGEbO"),
                Exercise("Chin-Up (Pazu & Sırt)", "Chin-Up (Pazu & Sırt)", 3, "MAX", 120, "Avuç içleri sana bakıyor.\n\nDikkat: Kendini pazu gücüyle yukarı çek.", videoUrl = "https://youtube.com/shorts/Oi3bW9nQmGI?si=amffcv9Oo15VV1fD"),
                Exercise("Seated Dumbbell Shoulder Press (90°)", "Seated Dumbbell Shoulder Press (90°)", 3, "10", 120, "Omuz kütlesi.\n\nNasıl: Sehpayı tam dik (90°) konuma getir.", videoUrl = "https://www.youtube.com/watch?v=k6tzKisR3NY"),
                Exercise("Dumbbell Lateral Raise", "Dumbbell Lateral Raise", 3, "15-20", 60, "Yan omuz odağı.\n\nDikkat: Dambılları yanlara doğru omuz hizasına kadar aç.", videoUrl = "https://youtube.com/shorts/Kl3LEzQ5Zqs?si=lWGYxHfNqjO5_dfj"),
                Exercise("Incline Dumbbell Curl (45°)", "Incline Dumbbell Curl (45°)", 3, "10-12", 90, "Pazu tepe noktası için (Biceps Peak).\n\nNasıl: Sehpayı 45 derece eğime getir ve sırtını yasla.", videoUrl = "https://youtube.com/shorts/0y4tdUNPdlE?si=A1TgSRRq_ASo5gwe"),
                Exercise("Bent Over Rear Delt Fly", "Bent Over Rear Delt Fly", 3, "15", 60, "Arka omuz.\n\nNasıl: Öne iyice eğilerek kolları yan aç.", videoUrl = "https://www.youtube.com/shorts/LsT-bR_zxLo"),
                Exercise("Reverse Grip Dumbbell Curl", "Reverse Grip Dumbbell Curl", 2, "15", 60, "Ön kol ve damarlanma.\n\nNasıl: Avuç içleri yere bakacak şekilde curl yap.", videoUrl = "https://youtube.com/shorts/fN99q1aD6U8?si=Z6Wq-M8-LqP3H9s_"),
                Exercise("SS: Hammer Curl", "SS: Hammer Curl", 2, "12", 0, "SÜPER SET (A): Bitirici pompa başlangıcı.", videoUrl = "https://www.youtube.com/shorts/qmQkt1Y-FX8"),
                Exercise("SS: Bench Dips", "SS: Bench Dips", 2, "12", 120, "SÜPER SET (B): Dinlenmeden arka kola geç.")
            )
        ),
        Workout(
            title = "6. GÜN: LOWER & POWER (Güç & Trapez)",
            exercises = listOf(
                Exercise("Isınma: Glute Bridge", "Isınma: Glute Bridge", 1, "15", 0, "Kalçayı aktif et.", videoUrl = "https://youtube.com/shorts/X_IGw8U_e38?si=hYTSnQzxaAGiVL7t"),
                Exercise("Isınma: World's Greatest Stretch", "Isınma: World's Greatest Stretch", 1, "1 Dakika", 0, "Tüm vücut açma.", videoUrl = "https://youtube.com/shorts/7XheaZERvBQ?si=TC5pt8aobQxZGyFr"),
                Exercise("Deadlift (Klasik)", "Deadlift (Klasik)", 3, "5-6", 180, "Tüm vücut gücü.\n\nDikkat: Sırtını asla bükme, bar bacağına yapışık kalsın.", videoUrl = "https://youtube.com/shorts/xNwpvDuZJ3k?si=kgUyR4A0bz6QrtOg"),
                Exercise("Goblet Squat", "Goblet Squat", 3, "12", 120, "Derin çöküş odaklı.\n\nNasıl: Tek dambılı göğsünde tut.", videoUrl = "https://youtube.com/shorts/lRYBbchqxtI?si=b9Q8TBV6suhit1_d"),
                Exercise("Romanian Deadlift (RDL)", "Romanian Deadlift (RDL)", 3, "10", 120, "Arka bacak & Bel.\n\nNasıl: Kalçayı geriye iterek eğil.", videoUrl = "https://youtube.com/shorts/g5u75sgpn04?si=bKSytaZ7dI3C_Rmu"),
                Exercise("Dumbbell Shrug (Omuz Silkme)", "Dumbbell Shrug (Omuz Silkme)", 3, "12-15", 90, "Trapez gelişimi için.\n\nNasıl: Omuzlarını kulaklarına çek.", videoUrl = "https://www.youtube.com/shorts/rFsSeClGnNA"),
                Exercise("Leg Extension", "Leg Extension", 3, "15", 90, "Ön bacak izolasyonu.", videoUrl = "https://www.youtube.com/shorts/iQ92TuvBqRo"),
                Exercise("Standing Calf Raise", "Standing Calf Raise", 4, "20", 60, "Alt bacak.\n\nNasıl: Parmak ucunda patlayıcı yüksel.", videoUrl = "https://youtube.com/shorts/3CxRItkntZU?si=Zf2ccGIzUcuT1aMa")
            )
        )
    )
}
