package org.com.ui

import kotlin.math.*

data class PlaceNode(
    val name: String,
    val district: String,
    val city: String,
    val lat: Double,
    val lng: Double,
    val category: String,
    val tags: List<String> = emptyList()
)

data class PlaceSuggestionItem(
    val name: String,
    val subtitle: String,
    val lat: Double,
    val lng: Double,
    val category: String,
    val isRelated: Boolean = false,
    val hasCoordinates: Boolean = true
)

data class PlaceSuggestionBundle(
    val primary: List<PlaceSuggestionItem>,
    val related: List<PlaceSuggestionItem>,
    val isPopularMode: Boolean = false
)

object PlaceSuggestionEngine {

    private val places: List<PlaceNode> = listOf(
        // Kinondoni & Peninsula
        PlaceNode("Kinondoni", "Kinondoni", "Dar es Salaam", -6.7834, 39.2665, "district", listOf("kinondoni studio", "kinondoni manyanya", "kinondoni biafra", "kinondoni mkwajuni", "mikocheni", "msasani", "masaki", "oysterbay", "ada estate", "hana nasif", "mwananyamala", "kijitonyama", "makumbusho", "mwenge", "kawe", "mbezi beach")),
        PlaceNode("Kinondoni Studio", "Kinondoni", "Dar es Salaam", -6.7892, 39.2683, "subarea", listOf("kinondoni", "manyanya", "biafra", "morocco", "ada estate", "kawawa road")),
        PlaceNode("Kinondoni Manyanya", "Kinondoni", "Dar es Salaam", -6.7858, 39.2636, "subarea", listOf("kinondoni", "studio", "mwananyamala", "mkwajuni", "kawawa road")),
        PlaceNode("Kinondoni Biafra", "Kinondoni", "Dar es Salaam", -6.7875, 39.2704, "subarea", listOf("kinondoni", "studio", "ada estate", "morocco", "kawawa road")),
        PlaceNode("Ada Estate", "Kinondoni", "Dar es Salaam", -6.7819, 39.2728, "area", listOf("kinondoni", "oysterbay", "morocco", "msasani")),
        PlaceNode("Morocco", "Kinondoni", "Dar es Salaam", -6.7826, 39.2643, "area", listOf("kinondoni", "bagamoyo road", "makumbusho", "ada estate", "kijitonyama")),
        PlaceNode("Mwananyamala", "Kinondoni", "Dar es Salaam", -6.7848, 39.2549, "area", listOf("kinondoni", "hospital", "makumbusho", "kijitonyama", "manyanya")),
        PlaceNode("Hana Nasif", "Kinondoni", "Dar es Salaam", -6.7944, 39.2634, "area", listOf("kinondoni", "mkwajuni", "magomeni", "studio")),
        PlaceNode("Kigogo", "Kinondoni", "Dar es Salaam", -6.8142, 39.2478, "area", listOf("kinondoni", "magomeni", "ilala", "mabibo", "manzese")),

        // Mikocheni & Landmarks
        PlaceNode("Mikocheni", "Kinondoni", "Dar es Salaam", -6.7689, 39.2512, "area", listOf("mikocheni a", "mikocheni b", "kinondoni", "msasani", "kijitonyama", "mwenge", "kawe", "shoppers plaza", "palm village", "kairuki", "regent estate")),
        PlaceNode("Mikocheni A", "Kinondoni", "Dar es Salaam", -6.7712, 39.2564, "subarea", listOf("mikocheni", "kinondoni", "kairuki hospital", "shoppers plaza", "regent estate", "victoria", "msasani")),
        PlaceNode("Mikocheni B", "Kinondoni", "Dar es Salaam", -6.7618, 39.2454, "subarea", listOf("mikocheni", "kinondoni", "palm village", "mwenge", "kawe", "kijitonyama")),
        PlaceNode("Regent Estate, Mikocheni", "Kinondoni", "Dar es Salaam", -6.7745, 39.2598, "subarea", listOf("mikocheni", "mikocheni a", "victoria", "kijitonyama", "morocco")),
        PlaceNode("Palm Village, Mikocheni", "Kinondoni", "Dar es Salaam", -6.7575, 39.2535, "landmark", listOf("mikocheni", "mikocheni b", "mall", "msasani", "kawe")),
        PlaceNode("Shoppers Plaza, Mikocheni", "Kinondoni", "Dar es Salaam", -6.7694, 39.2615, "landmark", listOf("mikocheni", "mikocheni a", "mall", "kairuki", "msasani")),
        PlaceNode("Kairuki Hospital, Mikocheni", "Kinondoni", "Dar es Salaam", -6.7720, 39.2571, "landmark", listOf("mikocheni", "hospital", "hkmu", "kijitonyama")),

        // Msasani / Masaki / Oysterbay
        PlaceNode("Msasani", "Kinondoni", "Dar es Salaam", -6.7614, 39.2708, "area", listOf("peninsula", "masaki", "oysterbay", "mikocheni", "namanga", "slipway", "beach", "kinondoni")),
        PlaceNode("Masaki", "Kinondoni", "Dar es Salaam", -6.7518, 39.2798, "area", listOf("peninsula", "msasani", "oysterbay", "slipway", "sea cliff", "beach", "kinondoni")),
        PlaceNode("Oysterbay", "Kinondoni", "Dar es Salaam", -6.7764, 39.2792, "area", listOf("msasani", "masaki", "coco beach", "ada estate", "namanga", "kinondoni")),
        PlaceNode("Namanga", "Kinondoni", "Dar es Salaam", -6.7747, 39.2699, "subarea", listOf("msasani", "oysterbay", "mikocheni", "kinondoni")),

        // Kijitonyama / Makumbusho / Victoria / Mwenge
        PlaceNode("Kijitonyama", "Kinondoni", "Dar es Salaam", -6.7785, 39.2422, "area", listOf("kinondoni", "sinza", "makumbusho", "mikocheni", "mwenge", "sayansi", "victoria", "dar village")),
        PlaceNode("Makumbusho", "Kinondoni", "Dar es Salaam", -6.7834, 39.2471, "area", listOf("kinondoni", "bus stand", "kijitonyama", "mwananyamala", "victoria", "sinza")),
        PlaceNode("Victoria", "Kinondoni", "Dar es Salaam", -6.7793, 39.2546, "area", listOf("kinondoni", "kijitonyama", "mikocheni", "regent estate", "morocco")),
        PlaceNode("Mwenge", "Kinondoni", "Dar es Salaam", -6.7689, 39.2286, "area", listOf("kinondoni", "mlimani city", "survey", "udsm", "sinza", "kijitonyama", "mikocheni", "kawe", "makongo")),

        // Sinza & Sub-areas
        PlaceNode("Sinza", "Ubungo", "Dar es Salaam", -6.7866, 39.2275, "area", listOf("sinza mori", "sinza palestina", "sinza mugabe", "sinza kijiweni", "sinza kwa remmy", "ubungo", "kijitonyama", "mwenge", "mlimani city", "udsm", "shekilango")),
        PlaceNode("Sinza Mori", "Ubungo", "Dar es Salaam", -6.7824, 39.2341, "subarea", listOf("sinza", "kijitonyama", "shekilango", "ubungo")),
        PlaceNode("Sinza Palestina", "Ubungo", "Dar es Salaam", -6.7885, 39.2268, "subarea", listOf("sinza", "hospital", "mugabe", "kijiweni", "ubungo")),
        PlaceNode("Sinza Mugabe", "Ubungo", "Dar es Salaam", -6.7908, 39.2225, "subarea", listOf("sinza", "palestina", "ubungo", "shekilango")),
        PlaceNode("Sinza Kijiweni", "Ubungo", "Dar es Salaam", -6.7848, 39.2252, "subarea", listOf("sinza", "kwa remmy", "mlimani city", "mwenge")),
        PlaceNode("Sinza Kwa Remmy", "Ubungo", "Dar es Salaam", -6.7832, 39.2289, "subarea", listOf("sinza", "kijiweni", "mori", "mlimani city")),

        // Universities / Mlimani / Survey / Makongo
        PlaceNode("University of Dar es Salaam (UDSM)", "Ubungo", "Dar es Salaam", -6.7795, 39.2055, "university", listOf("udsm", "chuo kikuu", "mlimani", "survey", "mwenge", "sinza", "ubungo", "makongo juu", "changanyikeni", "ardhi", "hostel", "student")),
        PlaceNode("Mlimani City", "Ubungo", "Dar es Salaam", -6.7731, 39.2212, "landmark", listOf("mall", "sam nujoma", "survey", "udsm", "mwenge", "sinza", "ubungo")),
        PlaceNode("Survey", "Kinondoni", "Dar es Salaam", -6.7681, 39.2165, "area", listOf("ardhi university", "aru", "udsm", "mlimani city", "mwenge", "makongo", "changanyikeni", "hostel")),
        PlaceNode("Ardhi University (ARU)", "Kinondoni", "Dar es Salaam", -6.7644, 39.2131, "university", listOf("aru", "survey", "udsm", "makongo juu", "mwenge", "student", "hostel")),
        PlaceNode("Makongo Juu", "Kinondoni", "Dar es Salaam", -6.7485, 39.2006, "area", listOf("makongo", "ardhi", "survey", "udsm", "changanyikeni", "goba", "mwenge", "kawe")),
        PlaceNode("Changanyikeni", "Ubungo", "Dar es Salaam", -6.7597, 39.1932, "area", listOf("udsm", "makongo juu", "survey", "ardhi", "goba", "ubungo")),

        // Kawe / Mbezi Beach / Goba / Tegeta / Boko / Bunju
        PlaceNode("Kawe", "Kinondoni", "Dar es Salaam", -6.7379, 39.2289, "area", listOf("kinondoni", "kawe beach", "lugalo", "mbezi beach", "mikocheni", "mwenge", "makongo")),
        PlaceNode("Mbezi Beach", "Kinondoni", "Dar es Salaam", -6.7068, 39.2148, "area", listOf("mbezi", "africana", "makonde", "tanki bovu", "kawe", "goba", "tegeta", "kunduchi", "salasala", "kinondoni", "beach")),
        PlaceNode("Mbezi Beach Africana", "Kinondoni", "Dar es Salaam", -6.6956, 39.2124, "subarea", listOf("mbezi beach", "mbezi", "salasala", "kunduchi", "tegeta")),
        PlaceNode("Mbezi Tanki Bovu", "Kinondoni", "Dar es Salaam", -6.7224, 39.2185, "subarea", listOf("mbezi beach", "mbezi", "kawe", "goba", "makonde")),
        PlaceNode("Goba", "Ubungo", "Dar es Salaam", -6.7166, 39.1674, "area", listOf("mbezi beach", "makongo juu", "madale", "salasala", "kimara", "mbezi luis", "ubungo")),
        PlaceNode("Salasala", "Kinondoni", "Dar es Salaam", -6.6902, 39.1886, "area", listOf("mbezi beach", "africana", "tegeta", "goba", "madale")),
        PlaceNode("Tegeta", "Kinondoni", "Dar es Salaam", -6.6552, 39.1932, "area", listOf("kunduchi", "boko", "bahari beach", "salasala", "mbezi beach", "madale", "kinondoni")),
        PlaceNode("Kunduchi", "Kinondoni", "Dar es Salaam", -6.6665, 39.2178, "area", listOf("beach", "tegeta", "mbezi beach", "bahari beach")),
        PlaceNode("Bahari Beach", "Kinondoni", "Dar es Salaam", -6.6479, 39.2121, "area", listOf("tegeta", "kunduchi", "boko", "ununio", "beach")),
        PlaceNode("Boko", "Kinondoni", "Dar es Salaam", -6.6258, 39.1765, "area", listOf("tegeta", "bunju", "bahari beach", "ununio")),
        PlaceNode("Bunju", "Kinondoni", "Dar es Salaam", -6.5889, 39.1462, "area", listOf("boko", "bagamoyo road", "kinondoni")),
        PlaceNode("Madale", "Kinondoni", "Dar es Salaam", -6.6805, 39.1419, "area", listOf("wazo", "goba", "salasala", "tegeta", "kinondoni")),

        // Ubungo / Kimara / Mbezi Luis / Kibamba / Mabibo / Magomeni
        PlaceNode("Ubungo", "Ubungo", "Dar es Salaam", -6.7924, 39.2083, "district", listOf("riverside", "external", "sinza", "kimara", "manzese", "mabibo", "udsm", "mlimani city", "mbezi luis", "goba")),
        PlaceNode("Ubungo Riverside", "Ubungo", "Dar es Salaam", -6.8001, 39.2055, "subarea", listOf("ubungo", "external", "mabibo", "mandela road", "kimara")),
        PlaceNode("Kimara", "Ubungo", "Dar es Salaam", -6.7918, 39.1665, "area", listOf("kimara mwisho", "kimara korogwe", "kimara baruti", "kimara suka", "ubungo", "mbezi luis", "goba", "tabata")),
        PlaceNode("Kimara Korogwe", "Ubungo", "Dar es Salaam", -6.7926, 39.1815, "subarea", listOf("kimara", "ubungo", "baruti", "mwisho", "morogoro road")),
        PlaceNode("Kimara Mwisho", "Ubungo", "Dar es Salaam", -6.7905, 39.1562, "subarea", listOf("kimara", "mbezi luis", "ubungo", "baruti", "morogoro road")),
        PlaceNode("Kimara Baruti", "Ubungo", "Dar es Salaam", -6.7932, 39.1904, "subarea", listOf("kimara", "ubungo", "korogwe", "morogoro road")),
        PlaceNode("Mbezi Luis (Mbezi Mwisho)", "Ubungo", "Dar es Salaam", -6.7867, 39.1128, "area", listOf("mbezi", "magufuli", "kimara", "kibamba", "goba", "ubungo")),
        PlaceNode("Kibamba", "Ubungo", "Dar es Salaam", -6.7896, 39.0672, "area", listOf("mbezi luis", "kiluvya", "luguruni", "ubungo", "morogoro road")),
        PlaceNode("Kiluvya", "Ubungo", "Dar es Salaam", -6.7930, 39.0372, "area", listOf("kibamba", "kibaha", "mbezi luis", "morogoro road")),
        PlaceNode("Mabibo", "Ubungo", "Dar es Salaam", -6.8034, 39.2227, "area", listOf("mabibo hostel", "nit", "ubungo", "manzese", "sinza", "udsm")),
        PlaceNode("Manzese", "Ubungo", "Dar es Salaam", -6.8012, 39.2368, "area", listOf("ubungo", "sinza", "magomeni", "mabibo")),
        PlaceNode("Magomeni", "Kinondoni", "Dar es Salaam", -6.8065, 39.2545, "area", listOf("mapipa", "kagera", "kinondoni", "kariakoo", "manzese", "upanga")),

        // Ilala / CBD / Kariakoo / Upanga / Kisutu / Tabata / Kinyerezi / Airport
        PlaceNode("Ilala", "Ilala", "Dar es Salaam", -6.8277, 39.2571, "district", listOf("kariakoo", "upanga", "posta", "cbd", "kisutu", "buguruni", "tabata", "segerea", "kinyerezi", "ukonga")),
        PlaceNode("Posta (Dar es Salaam CBD)", "Ilala", "Dar es Salaam", -6.8161, 39.2894, "area", listOf("cbd", "city center", "kisutu", "kivukoni", "ifm", "cbe", "dit", "upanga", "kariakoo", "ferry")),
        PlaceNode("Kariakoo", "Ilala", "Dar es Salaam", -6.8211, 39.2747, "area", listOf("ilala", "market", "gerezani", "mnazi mmoja", "upanga", "posta", "magomeni")),
        PlaceNode("Kisutu", "Ilala", "Dar es Salaam", -6.8168, 39.2839, "area", listOf("posta", "cbd", "upanga", "kariakoo", "mnazi mmoja", "ilala")),
        PlaceNode("Upanga", "Ilala", "Dar es Salaam", -6.8068, 39.2798, "area", listOf("muhimbili", "muhas", "aga khan", "posta", "kariakoo", "kisutu", "ilala")),
        PlaceNode("Muhimbili (MUHAS)", "Ilala", "Dar es Salaam", -6.8066, 39.2740, "university", listOf("muhas", "hospital", "upanga", "ilala", "kariakoo", "student")),
        PlaceNode("Tabata", "Ilala", "Dar es Salaam", -6.8286, 39.2163, "area", listOf("tabata bima", "tabata segerea", "tabata relini", "tabata kimanga", "tabata liwiti", "kinyerezi", "buguruni", "ilala")),
        PlaceNode("Tabata Bima", "Ilala", "Dar es Salaam", -6.8258, 39.2104, "subarea", listOf("tabata", "segerea", "kimanga", "ilala")),
        PlaceNode("Tabata Segerea", "Ilala", "Dar es Salaam", -6.8351, 39.1932, "subarea", listOf("tabata", "segerea", "kinyerezi", "ukonga", "ilala")),
        PlaceNode("Kinyerezi", "Ilala", "Dar es Salaam", -6.8469, 39.1704, "area", listOf("tabata", "segerea", "kimara", "ukonga", "ilala")),
        PlaceNode("Buguruni", "Ilala", "Dar es Salaam", -6.8339, 39.2471, "area", listOf("ilala", "tabata", "tazara")),
        PlaceNode("Kipawa", "Ilala", "Dar es Salaam", -6.8716, 39.2105, "area", listOf("airport", "jnia", "ukonga", "banana", "ilala")),
        PlaceNode("Ukonga", "Ilala", "Dar es Salaam", -6.8672, 39.1818, "area", listOf("airport", "jnia", "gongo la mboto", "banana", "segerea", "kipawa")),
        PlaceNode("Julius Nyerere Airport (JNIA)", "Ilala", "Dar es Salaam", -6.8781, 39.2026, "landmark", listOf("airport", "kipawa", "ukonga", "banana", "gongo la mboto")),
        PlaceNode("Gongo la Mboto", "Ilala", "Dar es Salaam", -6.8834, 39.1539, "area", listOf("ukonga", "airport", "kiu", "pugu", "chanika", "ilala")),

        // Temeke & Kigamboni
        PlaceNode("Temeke", "Temeke", "Dar es Salaam", -6.8604, 39.2618, "district", listOf("changombe", "kurasini", "tandika", "mbagala", "chamazi", "kijichi", "duce", "tia")),
        PlaceNode("Chang'ombe", "Temeke", "Dar es Salaam", -6.8473, 39.2675, "area", listOf("temeke", "duce", "taifa", "stadium", "kurasini", "student")),
        PlaceNode("Kurasini", "Temeke", "Dar es Salaam", -6.8548, 39.2861, "area", listOf("temeke", "tia", "port", "changombe", "kigamboni bridge")),
        PlaceNode("Kijichi", "Temeke", "Dar es Salaam", -6.9052, 39.2882, "area", listOf("mbagala", "temeke", "chamazi", "kigamboni")),
        PlaceNode("Mbagala", "Temeke", "Dar es Salaam", -6.9061, 39.2654, "area", listOf("rangi tatu", "zakhem", "chamazi", "kijichi", "temeke")),
        PlaceNode("Kigamboni", "Kigamboni", "Dar es Salaam", -6.8428, 39.3264, "district", listOf("ferry", "kivukoni", "kibada", "mjimwema", "gezaulole", "kisota", "tuangoma", "beach", "mnma")),
        PlaceNode("Kigamboni Ferry", "Kigamboni", "Dar es Salaam", -6.8246, 39.3029, "subarea", listOf("kigamboni", "kivukoni", "posta", "mjimwema", "mnma")),
        PlaceNode("Kibada, Kigamboni", "Kigamboni", "Dar es Salaam", -6.8856, 39.3324, "area", listOf("kigamboni", "kisota", "tuangoma", "mjimwema", "gezaulole")),
        PlaceNode("Kisota, Kigamboni", "Kigamboni", "Dar es Salaam", -6.8745, 39.3210, "area", listOf("kigamboni", "kibada", "ferry", "mjimwema")),
        PlaceNode("Mjimwema, Kigamboni", "Kigamboni", "Dar es Salaam", -6.8567, 39.3547, "area", listOf("kigamboni", "beach", "ferry", "gezaulole", "kibada")),
        PlaceNode("Gezaulole, Kigamboni", "Kigamboni", "Dar es Salaam", -6.8794, 39.3924, "area", listOf("kigamboni", "mjimwema", "kibada", "beach")),

        // Major Tanzania Cities & Towns
        PlaceNode("Arusha", "Arusha", "Arusha", -3.3869, 36.6830, "city", listOf("njiro", "sakina", "uzunguni", "clock tower", "iaa")),
        PlaceNode("Njiro, Arusha", "Arusha", "Arusha", -3.4041, 36.7068, "area", listOf("arusha", "iaa", "kijenge")),
        PlaceNode("Dodoma", "Dodoma", "Dodoma", -6.1630, 35.7516, "city", listOf("udom", "area d", "area c", "kisasa", "makulu", "cbe")),
        PlaceNode("Mwanza", "Nyamagana", "Mwanza", -2.5164, 32.9175, "city", listOf("nyamagana", "ilemela", "capri point", "nyegezi", "saut", "bugando")),
        PlaceNode("Zanzibar (Stone Town)", "Mjini", "Zanzibar", -6.1659, 39.1990, "city", listOf("unguja", "stone town", "mbweni", "nungwi", "paje", "suza")),
        PlaceNode("Morogoro", "Morogoro", "Morogoro", -6.8278, 37.6591, "city", listOf("sua", "mzumbe", "kihonda", "mazimbu")),
        PlaceNode("Mbeya", "Mbeya", "Mbeya", -8.9094, 33.4608, "city", listOf("must", "uyole", "forest", "mwanjelwa")),
        PlaceNode("Moshi", "Moshi", "Kilimanjaro", -3.3349, 37.3404, "city", listOf("kilimanjaro", "kcmc", "shanty town", "mocu")),
        PlaceNode("Tanga", "Tanga", "Tanga", -5.0689, 39.0988, "city", listOf("raskazone", "chumbageni", "donge")),
        PlaceNode("Kibaha", "Kibaha", "Pwani", -6.7667, 38.9167, "city", listOf("pwani", "maili moja", "tumbi", "kongowe", "morogoro road")),
        PlaceNode("Bagamoyo", "Bagamoyo", "Pwani", -6.4422, 38.9042, "city", listOf("pwani", "kaole", "kiromo", "beach")),
        PlaceNode("Kigoma", "Kigoma", "Kigoma", -4.8769, 29.6267, "city", listOf("ujiji", "mwanga", "lake tanganyika")),
        PlaceNode("Iringa", "Iringa", "Iringa", -7.7700, 35.6900, "city", listOf("gangilonga", "kihesa", "ruaha")),
        PlaceNode("Tabora", "Tabora", "Tabora", -5.0162, 32.8266, "city", listOf("tabora municipal")),
        PlaceNode("Bukoba", "Bukoba", "Kagera", -1.3317, 31.8122, "city", listOf("kagera", "lake victoria")),
        PlaceNode("Mtwara", "Mtwara", "Mtwara", -10.2667, 40.1833, "city", listOf("mikindani", "shangani"))
    )

    private val popularHubNames = setOf(
        "Mikocheni", "Sinza", "Masaki", "Mwenge", "Mbezi Beach",
        "University of Dar es Salaam (UDSM)", "Kariakoo", "Kigamboni"
    )

    fun suggest(rawQuery: String, extraAreas: List<String> = emptyList()): PlaceSuggestionBundle {
        val q = rawQuery.trim().lowercase()
        if (q.isEmpty()) {
            val popular = places
                .filter { it.name in popularHubNames }
                .map {
                    PlaceSuggestionItem(
                        name = it.name,
                        subtitle = if (it.district == it.city) "${it.city}, Tanzania" else "${it.district}, ${it.city}",
                        lat = it.lat,
                        lng = it.lng,
                        category = it.category
                    )
                }
            return PlaceSuggestionBundle(primary = popular, related = emptyList(), isPopularMode = true)
        }

        val tokens = q.split(Regex("[\\s,]+")).filter { it.isNotBlank() }
        data class Scored(val node: PlaceNode, val score: Int, val reason: String)

        val scored = mutableListOf<Scored>()
        for (p in places) {
            val nameL = p.name.lowercase()
            val distL = p.district.lowercase()
            val cityL = p.city.lowercase()
            val tagsL = p.tags.joinToString(" ").lowercase()
            val words = nameL.split(Regex("[\\s,()\\-]+")).filter { it.isNotBlank() }
            val combined = "$nameL $distL $cityL $tagsL"

            var score = 0
            var reason = ""
            when {
                nameL == q -> { score = 130; reason = "exact" }
                nameL.startsWith(q) -> { score = 110; reason = "prefix" }
                words.any { it.startsWith(q) } -> { score = 96; reason = "word-prefix" }
                nameL.contains(q) -> { score = 82; reason = "substring" }
                tokens.size > 1 && tokens.all { combined.contains(it) } -> { score = 78; reason = "multi" }
                distL.startsWith(q) || cityL.startsWith(q) -> { score = 68; reason = "district" }
                tagsL.contains(q) -> { score = 60; reason = "tag" }
                q.length >= 4 -> {
                    val maxErr = if (q.length >= 6) 2 else 1
                    val minDist = words.filter { it.length >= 3 }.minOfOrNull { w ->
                        val cmp = if (w.length > q.length + 1) w.substring(0, q.length + 1) else w
                        levenshtein(q, cmp)
                    } ?: 99
                    if (minDist <= maxErr) {
                        score = 52 - minDist * 8
                        reason = "fuzzy"
                    }
                }
            }
            if (score > 0) scored.add(Scored(p, score, reason))
        }

        scored.sortByDescending { it.score }

        val directNodes = mutableListOf<PlaceNode>()
        val relatedItems = mutableListOf<PlaceSuggestionItem>()
        val seenNames = mutableSetOf<String>()

        for (s in scored) {
            val key = s.node.name.lowercase()
            if (s.reason == "tag" || (s.reason == "district" && directNodes.size >= 4)) {
                if (seenNames.add(key)) {
                    relatedItems.add(
                        PlaceSuggestionItem(
                            name = s.node.name,
                            subtitle = "${s.node.district}, ${s.node.city}",
                            lat = s.node.lat,
                            lng = s.node.lng,
                            category = s.node.category,
                            isRelated = true
                        )
                    )
                }
            } else if (seenNames.add(key)) {
                directNodes.add(s.node)
            }
        }

        // Geographic & tag sibling expansion around top match
        val anchor = directNodes.firstOrNull() ?: scored.firstOrNull()?.node
        if (anchor != null) {
            val anchorTags = anchor.tags.map { it.lowercase() }.toSet()
            val anchorShort = anchor.name.substringBefore(",").substringBefore("(").trim()
            val nearby = places.mapNotNull { cand ->
                val candKey = cand.name.lowercase()
                if (candKey in seenNames) return@mapNotNull null
                val distKm = haversineKm(anchor.lat, anchor.lng, cand.lat, cand.lng)
                val sharesTag = candKey in anchorTags || cand.tags.any { it.equals(anchor.name, ignoreCase = true) }
                val sameDistrict = cand.district == anchor.district
                if (distKm <= 4.2 || sharesTag || (anchor.category == "district" && sameDistrict)) {
                    val relScore = (if (sharesTag) 40.0 else 0.0) + (if (sameDistrict) 20.0 else 0.0) + max(0.0, 30.0 - distKm * 6.0)
                    val distLabel = if (distKm < 1.0) "< 1 km" else "${((distKm * 10.0).roundToInt() / 10.0)} km"
                    relScore to PlaceSuggestionItem(
                        name = cand.name,
                        subtitle = "$distLabel from $anchorShort · ${cand.district}",
                        lat = cand.lat,
                        lng = cand.lng,
                        category = cand.category,
                        isRelated = true
                    )
                } else null
            }.sortedByDescending { it.first }.take(6)

            for ((_, item) in nearby) {
                if (seenNames.add(item.name.lowercase())) {
                    relatedItems.add(item)
                }
            }
        }

        val primaryItems = directNodes.take(8).map {
            PlaceSuggestionItem(
                name = it.name,
                subtitle = if (it.district == it.city) "${it.city}, Tanzania" else "${it.district}, ${it.city}",
                lat = it.lat,
                lng = it.lng,
                category = it.category
            )
        }.toMutableList()

        // Merge dynamic backend areas that match query and aren't already listed (ignoring raw coordinates)
        for (rawArea in extraAreas) {
            val trimmed = rawArea.trim()
            if (trimmed.isBlank()) continue
            val lower = trimmed.lowercase()

            // Discard raw coordinates strings e.g. "Coordinates: -6.78, 39.2" or "-6.78, 39.2"
            val isCoords = lower.startsWith("coordinates:") ||
                Regex("^-?\\d+(\\.\\d+)?[,\\s]+-?\\d+(\\.\\d+)?$").matches(lower.removePrefix("coordinates:").trim()) ||
                Regex("^-?[0-9.,\\s]+$").matches(trimmed)

            val effectiveName = if (isCoords) {
                // If it contains coordinates, resolve to real place name
                val cleaned = lower.removePrefix("coordinates:").trim()
                val parts = cleaned.split(Regex("[,\\s]+")).filter { it.isNotBlank() }
                if (parts.size >= 2) {
                    val pLat = parts[0].toDoubleOrNull() ?: 0.0
                    val pLng = parts[1].toDoubleOrNull() ?: 0.0
                    val nearest = findNearestPlace(pLat, pLng)
                    nearest?.name
                } else null
            } else {
                trimmed
            } ?: continue

            val effLower = effectiveName.lowercase()
            if (effLower in seenNames) continue
            if (effLower.startsWith(q) || effLower.contains(q) || (tokens.size > 1 && tokens.all { effLower.contains(it) })) {
                seenNames.add(effLower)
                primaryItems.add(
                    PlaceSuggestionItem(
                        name = effectiveName,
                        subtitle = "Active Roomify Area",
                        lat = 0.0,
                        lng = 0.0,
                        category = "area",
                        isRelated = false,
                        hasCoordinates = false
                    )
                )
            }
        }

        return PlaceSuggestionBundle(
            primary = primaryItems.take(10),
            related = relatedItems.take(6),
            isPopularMode = false
        )
    }

    /**
     * Resolves the closest known location node in Tanzania for given GPS coordinates.
     */
    fun findNearestPlace(lat: Double, lng: Double, maxKm: Double = 35.0): PlaceNode? {
        if (lat == 0.0 && lng == 0.0) return null
        var bestNode: PlaceNode? = null
        var bestDist = Double.MAX_VALUE
        for (p in places) {
            val dist = haversineKm(lat, lng, p.lat, p.lng)
            if (dist < bestDist && dist <= maxKm) {
                bestDist = dist
                bestNode = p
            }
        }
        return bestNode
    }

    /**
     * Ensures human-readable place names (e.g. "Mikocheni, Dar es Salaam", "Sinza, Ubungo")
     * are returned instead of raw GPS coordinates.
     */
    fun resolvePlaceName(
        address: String?,
        lat: Double = 0.0,
        lng: Double = 0.0,
        neighbourhoodPlaces: List<String> = emptyList()
    ): String {
        val raw = address?.trim()
        val isCoordAddress = raw != null && (
            raw.startsWith("Coordinates:", ignoreCase = true) ||
            Regex("^-?\\d+(\\.\\d+)?[,\\s]+-?\\d+(\\.\\d+)?$").matches(raw.removePrefix("Coordinates:").trim()) ||
            Regex("^-?[0-9.,\\s]+$").matches(raw)
        )

        if (!raw.isNullOrBlank() && !isCoordAddress) {
            val clean = raw.removePrefix("Coordinates:").trim()
            if (clean.isNotBlank() && !Regex("^-?[0-9.,\\s]+$").matches(clean)) {
                return clean
            }
        }

        // Try extracting latitude and longitude
        var parsedLat = lat
        var parsedLng = lng
        if ((parsedLat == 0.0 && parsedLng == 0.0) && raw != null && isCoordAddress) {
            val coordsClean = raw.removePrefix("Coordinates:").trim()
            val parts = coordsClean.split(Regex("[,\\s]+")).filter { it.isNotBlank() }
            if (parts.size >= 2) {
                parsedLat = parts[0].toDoubleOrNull() ?: 0.0
                parsedLng = parts[1].toDoubleOrNull() ?: 0.0
            }
        }

        if (parsedLat != 0.0 || parsedLng != 0.0) {
            val nearest = findNearestPlace(parsedLat, parsedLng)
            if (nearest != null) {
                return if (nearest.district == nearest.city) "${nearest.name}, ${nearest.city}"
                else "${nearest.name}, ${nearest.district}"
            }
        }

        val cleanNeighbourhood = neighbourhoodPlaces.firstOrNull { nb ->
            val trimmed = nb.trim()
            trimmed.isNotBlank() && !trimmed.startsWith("Coordinates:", ignoreCase = true) && !Regex("^-?[0-9.,\\s]+$").matches(trimmed)
        }
        if (cleanNeighbourhood != null) {
            return cleanNeighbourhood.trim()
        }

        return "Dar es Salaam, Tanzania"
    }

    private fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371.0
        val dLat = (lat2 - lat1) * PI / 180.0
        val dLng = (lng2 - lng1) * PI / 180.0
        val sinDlat = sin(dLat / 2)
        val sinDlng = sin(dLng / 2)
        val a = sinDlat * sinDlat + cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) * sinDlng * sinDlng
        return r * 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        return dp[a.length][b.length]
    }
}
