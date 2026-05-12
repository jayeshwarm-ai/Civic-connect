/**
 * INDIA_STATES_CITIES
 *
 * Static reference data for the State / City cascading dropdowns used on the
 * service-request submit and edit forms.
 *
 * Coverage: all 28 Indian states and 8 Union Territories. Each entry lists a
 * representative set of major cities/districts (up to 50 per state). This is
 * intentionally a fixed list to keep the form fast and offline-friendly — no
 * external API calls are needed at runtime.
 *
 * To extend or correct the list, just edit the array for the relevant state.
 */

export const INDIA_STATES_CITIES = {
  "Andhra Pradesh": [
    "Visakhapatnam","Vijayawada","Guntur","Nellore","Kurnool","Rajahmundry","Tirupati","Kakinada","Anantapur",
    "Kadapa","Eluru","Ongole","Chittoor","Machilipatnam","Adoni","Hindupur","Proddatur","Bhimavaram","Tenali",
    "Madanapalle","Srikakulam","Vizianagaram","Chilakaluripet","Tadepalligudem","Narasaraopet","Gudivada",
    "Tadipatri","Mangalagiri","Chirala","Anakapalle","Kavali","Palakollu","Bapatla","Nuzvid","Markapur",
    "Ponnur","Salur","Tuni","Bobbili","Sullurpeta","Kandukur","Punganur","Nandyal","Dharmavaram","Gudur",
    "Vinukonda","Narsipatnam","Repalle","Naidupet","Pithapuram"
  ],
  "Arunachal Pradesh": [
    "Itanagar","Naharlagun","Pasighat","Tawang","Bomdila","Ziro","Aalo","Tezu","Khonsa","Roing","Daporijo",
    "Anini","Changlang","Yingkiong","Seppa","Namsai","Longding","Hawai","Koloriang","Yupia","Basar",
    "Tirap","Mechuka","Nampong","Boleng","Likabali","Raga","Hayuliang","Vijaynagar","Miao","Sagalee",
    "Banderdewa","Doimukh","Jairampur","Kalaktang","Kanubari","Mariyang","Rumgong","Yazali","Ngomdir",
    "Roing","Walong","Anjaw","Lower Subansiri","Upper Subansiri","West Siang","East Siang","Lohit",
    "Dibang Valley","Lower Dibang Valley"
  ],
  "Assam": [
    "Guwahati","Silchar","Dibrugarh","Jorhat","Nagaon","Tinsukia","Tezpur","Bongaigaon","Karimganj","Sivasagar",
    "Goalpara","North Lakhimpur","Diphu","Dhubri","Barpeta","Hailakandi","Mangaldoi","Hojai","Lanka","Lumding",
    "Mariani","Marigaon","Nalbari","Rangia","Sibsagar","Tangla","Tihu","Udalguri","Abhayapuri","Amguri",
    "Anand Nagar","Badarpur","Barpathar","Basugaon","Bhuban","Bilasipara","Bokakhat","Chabua","Chapar",
    "Chirang","Dhakuakhana","Digboi","Doom Dooma","Duliajan","Gauripur","Golaghat","Haflong","Howly",
    "Jagiroad","Kampur","Kokrajhar"
  ],
  "Bihar": [
    "Patna","Gaya","Bhagalpur","Muzaffarpur","Darbhanga","Purnia","Arrah","Begusarai","Katihar","Munger",
    "Chapra","Bettiah","Saharsa","Hajipur","Sasaram","Dehri","Siwan","Motihari","Nawada","Bagaha","Buxar",
    "Kishanganj","Sitamarhi","Jamalpur","Jehanabad","Aurangabad","Lakhisarai","Nalanda","Bhabua","Madhubani",
    "Madhepura","Banka","Sheikhpura","Sheohar","Supaul","Vaishali","Araria","Arwal","Banmankhi","Bihar Sharif",
    "Bodh Gaya","Daudnagar","Forbesganj","Gopalganj","Jamui","Khagaria","Kishanganj","Lalganj","Masaurhi","Mokama"
  ],
  "Chhattisgarh": [
    "Raipur","Bhilai","Bilaspur","Korba","Durg","Rajnandgaon","Jagdalpur","Raigarh","Ambikapur","Mahasamund",
    "Dhamtari","Chirmiri","Janjgir","Sakti","Tilda Newra","Mungeli","Manendragarh","Naila Janjgir","Akaltara",
    "Bemetara","Bhatapara","Champa","Dongargarh","Gobranawapara","Kawardha","Khairagarh","Kondagaon",
    "Kumhari","Mahasamund","Mungeli","Pithora","Sarangarh","Simga","Surajpur","Takhatpur","Bagbahara",
    "Balod","Baloda Bazar","Balrampur","Bastar","Bemetra","Bijapur","Dantewada","Gariaband","Janjgir-Champa",
    "Kabirdham","Kanker","Koriya","Narayanpur","Sukma","Surguja"
  ],
  "Goa": [
    "Panaji","Margao","Vasco da Gama","Mapusa","Ponda","Bicholim","Curchorem","Sanquelim","Cuncolim","Quepem",
    "Pernem","Valpoi","Canacona","Sanguem","Aldona","Anjuna","Arambol","Assagao","Benaulim","Calangute",
    "Candolim","Cavelossim","Chapora","Colva","Dona Paula","Loutolim","Majorda","Morjim","Old Goa","Palolem",
    "Patnem","Pilerne","Reis Magos","Salvador do Mundo","Saligao","Siolim","Tiswadi","Utorda","Varca","Velha Goa",
    "Velim","Verem","Vagator","Verna","Agonda","Agassaim","Betalbatim","Betim","Betul","Cansaulim"
  ],
  "Gujarat": [
    "Ahmedabad","Surat","Vadodara","Rajkot","Bhavnagar","Jamnagar","Junagadh","Gandhinagar","Anand","Navsari",
    "Morbi","Nadiad","Surendranagar","Bharuch","Mehsana","Bhuj","Porbandar","Palanpur","Valsad","Vapi","Gondal",
    "Veraval","Godhra","Patan","Kalol","Dahod","Botad","Amreli","Deesa","Jetpur","Sidhpur","Wadhwan","Anjar",
    "Mandvi","Visnagar","Khambhat","Mahuva","Una","Modasa","Limbdi","Padra","Vyara","Songadh","Idar","Lunawada",
    "Sihor","Jamjodhpur","Dhoraji","Kadi","Vijapur"
  ],
  "Haryana": [
    "Faridabad","Gurgaon","Panipat","Ambala","Yamunanagar","Rohtak","Hisar","Karnal","Sonipat","Panchkula",
    "Bhiwani","Sirsa","Bahadurgarh","Jind","Thanesar","Kaithal","Rewari","Palwal","Hansi","Narnaul","Fatehabad",
    "Gohana","Tohana","Narwana","Mandi Dabwali","Charkhi Dadri","Shahbad","Pehowa","Samalkha","Pinjore","Ladwa",
    "Sohna","Safidon","Taraori","Mahendragarh","Ratia","Rania","Sarsod","Pataudi","Bawal","Kalanwali","Kalka",
    "Ellenabad","Gharaunda","Ganaur","Tilpat","Ujjina","Hodal","Hassanpur","Hathin"
  ],
  "Himachal Pradesh": [
    "Shimla","Dharamshala","Solan","Mandi","Palampur","Kullu","Manali","Hamirpur","Una","Bilaspur","Chamba",
    "Kangra","Nahan","Sundernagar","Yol","Paonta Sahib","Sarkaghat","Jogindernagar","Nurpur","Kasauli",
    "Dalhousie","Reckong Peo","Keylong","Baddi","Nalagarh","Parwanoo","Banjar","Bhuntar","Dehra Gopipur",
    "Ghumarwin","Indora","Jawalamukhi","Kasol","Kotkhai","Kumarsain","Naina Devi","Naldera","Narkanda",
    "Nichar","Pangi","Rohru","Sangla","Spiti","Tissa","Theog","Mcleodganj","Bhagsu","Chail","Kufri","Kasauli"
  ],
  "Jharkhand": [
    "Ranchi","Jamshedpur","Dhanbad","Bokaro","Hazaribagh","Deoghar","Giridih","Ramgarh","Phusro","Medininagar",
    "Chaibasa","Dumka","Madhupur","Chirkunda","Pakaur","Chatra","Gumla","Lohardaga","Sahebganj","Chakradharpur",
    "Mihijam","Latehar","Khunti","Simdega","Jamtara","Garhwa","Godda","Koderma","Chakulia","Ghatshila","Mango",
    "Adityapur","Jugsalai","Dhanbad","Jharia","Sindri","Katras","Mussabani","Maithon","Tata Nagar","Kandra",
    "Hazaribagh","Bahragora","Bandgaon","Bardiha","Barhi","Barwadih","Basantpur","Patratu","Manoharpur","Saraikela"
  ],
  "Karnataka": [
    "Bangalore","Mysore","Hubli","Mangalore","Belgaum","Gulbarga","Davangere","Bellary","Bijapur","Shimoga",
    "Tumkur","Raichur","Bidar","Hospet","Gadag","Hassan","Udupi","Robertsonpet","Bhadravati","Chitradurga",
    "Kolar","Mandya","Chikmagalur","Gangawati","Bagalkot","Ranebennuru","Sirsi","Karwar","Yadgir","Sagara",
    "Bijapur","Channapatna","Madikeri","Tiptur","Arsikere","Nipani","Sira","Kanakapura","Lakshmeshwar",
    "Mulbagal","Devanahalli","Nargund","Sandur","Haveri","Sindhanur","Sankeshwara","Sedam","Sandur","Saundatti",
    "Wadi"
  ],
  "Kerala": [
    "Thiruvananthapuram","Kochi","Kozhikode","Thrissur","Kollam","Palakkad","Alappuzha","Kannur","Kottayam",
    "Malappuram","Kasaragod","Pathanamthitta","Idukki","Wayanad","Ernakulam","Munnar","Varkala","Kovalam",
    "Guruvayur","Kalpetta","Sulthanbathery","Manjeri","Tirur","Ponnani","Vatakara","Payyanur","Thalassery",
    "Mananthavady","Cherthala","Mavelikkara","Changanassery","Kayamkulam","Adoor","Kumily","Thekkady",
    "Bekal","Cheruthuruthy","Edappal","Eranakulam","Ettumanoor","Haripad","Irinjalakuda","Kanhangad","Karunagappally",
    "Koyilandy","Kunnamkulam","Muvattupuzha","Nedumkandam","Neyyattinkara","Ottapalam","Pala","Perinthalmanna",
    "Punalur"
  ],
  "Madhya Pradesh": [
    "Bhopal","Indore","Jabalpur","Gwalior","Ujjain","Sagar","Dewas","Satna","Ratlam","Rewa","Murwara","Singrauli",
    "Burhanpur","Khandwa","Bhind","Chhindwara","Guna","Shivpuri","Vidisha","Chhatarpur","Damoh","Mandsaur",
    "Khargone","Neemuch","Pithampur","Hoshangabad","Itarsi","Sehore","Morena","Betul","Seoni","Datia",
    "Nagda","Tikamgarh","Hatta","Khachrod","Maihar","Manawar","Mandla","Mhow","Multai","Narsinghgarh","Nasrullaganj",
    "Niwari","Panagar","Pansemal","Pichhore","Pipariya","Porsa","Rajgarh","Rau","Sailana","Sanawad"
  ],
  "Maharashtra": [
    "Mumbai","Pune","Nagpur","Nashik","Thane","Aurangabad","Solapur","Amravati","Kalyan","Vasai","Navi Mumbai",
    "Kolhapur","Sangli","Malegaon","Jalgaon","Akola","Latur","Dhule","Ahmednagar","Chandrapur","Parbhani",
    "Ichalkaranji","Jalna","Bhusawal","Panvel","Satara","Beed","Yavatmal","Kamptee","Gondia","Barshi","Achalpur",
    "Osmanabad","Nandurbar","Wardha","Udgir","Hinganghat","Palghar","Hingoli","Bhandara","Pandharpur","Lonavla",
    "Karad","Anjangaon","Pen","Pusad","Shrirampur","Ahmadpur","Ambejogai","Manmad"
  ],
  "Manipur": [
    "Imphal","Thoubal","Bishnupur","Churachandpur","Senapati","Ukhrul","Tamenglong","Kakching","Jiribam",
    "Moreh","Mayang Imphal","Lilong","Yairipok","Sugnu","Kangpokpi","Tengnoupal","Pherzawl","Noney","Chandel",
    "Andro","Bishenpur","Kongba","Lamlai","Lamphelpat","Langjing","Langthabal","Mongsangei","Nambol","Patsoi",
    "Porompat","Sagolband","Sangakpham","Singjamei","Thangmeiband","Thiyam Konjin","Wangkhei","Yumnam Khunou",
    "Heingang","Hiyangthang","Khurai","Kongba Khunou","Konsam Leikai","Langol","Mantripukhri","Nongmeibung",
    "Sapam Leikai","Top Chingtha","Wangoi","Yairipok Khundongbam","Kakwa","Thoudam Leikai"
  ],
  "Meghalaya": [
    "Shillong","Tura","Jowai","Nongstoin","Williamnagar","Baghmara","Resubelpara","Mawkyrwat","Khliehriat",
    "Ampati","Mairang","Nongpoh","Cherrapunji","Mawsynram","Mawphlang","Dawki","Bhoi","Mendipathar","Tikrikilla",
    "Phulbari","Selsella","Rongjeng","Songsak","Mahendraganj","Garobadha","Demthring","Umpling","Mawlai",
    "Nongmensong","Pynursla","Sohra","Lawsohtun","Madanrting","Nongkynrih","Smit","Umroi","Bara Bazar","Police Bazar",
    "Iewduh","Rilbong","Pollock","Nongthymmai","Jhalupara","Mawkhar","Laitumkhrah","Lawmali","Nongrim Hills",
    "Mawkasiang","Lummawbah","Rynjah"
  ],
  "Mizoram": [
    "Aizawl","Lunglei","Champhai","Saiha","Kolasib","Serchhip","Mamit","Lawngtlai","Khawzawl","Hnahthial",
    "Saitual","Bairabi","Demagiri","North Vanlaiphai","Vairengte","Zawlnuam","Tlabung","Hnahlan","Khawhai",
    "Reiek","Sakawrdai","Sangau","Tlangnuam","Tuipang","Tuirial","Tuithumhnar","Aibawk","Buhban","Chamring",
    "Chawngte","Darlawn","Dampa","Engkulh","Falkawn","Hortoki","Hriphaw","Khanpui","Kawnpui","Kelsih",
    "Kepran","Khawbung","Khawkawn","Khawlailung","Khawpuar","Khuangleng","Lengpui","Lokicherra","Lungdai",
    "Mualkhang","Mualpheng"
  ],
  "Nagaland": [
    "Kohima","Dimapur","Mokokchung","Tuensang","Wokha","Mon","Phek","Zunheboto","Kiphire","Longleng","Peren",
    "Noklak","Chumukedima","Pfutsero","Tuli","Naginimora","Tizit","Chozuba","Aboi","Akuluto","Aliba","Anaki C",
    "Angetyongpang","Atoizu","Bhandari","Changtongya","Changlangshu","Chare","Chen","Chetheba","Englan",
    "Ghathashi","Jakhama","Jalukie","Kebai Khelma","Kezocha","Khonsa","Kiusam","Kuhuboto","Kushiabill",
    "Lakhuti","Lampong Sheanghah","Longchem","Longkhim","Longmatra","Longphang","Longshen","Longsa","Longtho",
    "Longtsung","Lotsu"
  ],
  "Odisha": [
    "Bhubaneswar","Cuttack","Rourkela","Berhampur","Sambalpur","Puri","Balasore","Bhadrak","Baripada","Jharsuguda",
    "Jeypore","Barbil","Khurda","Sunabeda","Rayagada","Kendujhar","Sundargarh","Paradip","Bargarh","Bolangir",
    "Talcher","Angul","Dhenkanal","Gunupur","Phulbani","Nabarangpur","Koraput","Boudh","Joda","Brajrajnagar",
    "Athagarh","Banki","Bhadrak","Bhanjanagar","Bhawanipatna","Bhuban","Chandbali","Champua","Chhatrapur",
    "Choudwar","Daringbadi","Daspalla","Deogarh","Digapahandi","Dharmagarh","Dhamnagar","Dharamgarh","Dunguripali",
    "Ganjam","Gondia"
  ],
  "Punjab": [
    "Ludhiana","Amritsar","Jalandhar","Patiala","Bathinda","Hoshiarpur","Mohali","Pathankot","Moga","Abohar",
    "Malerkotla","Khanna","Phagwara","Muktsar","Barnala","Rajpura","Firozpur","Kapurthala","Sangrur","Faridkot",
    "Sunam","Mansa","Nabha","Nawanshahr","Tarn Taran","Zirakpur","Kharar","Gurdaspur","Batala","Kotkapura",
    "Jagraon","Sirhind","Samana","Talwandi Bhai","Banur","Anandpur Sahib","Sri Hargobindpur","Doraha","Khanna",
    "Mukerian","Nakodar","Patti","Phillaur","Rampura Phul","Sardulgarh","Talwandi Sabo","Tapa","Urmar Tanda",
    "Zira","Garhshankar"
  ],
  "Rajasthan": [
    "Jaipur","Jodhpur","Udaipur","Kota","Ajmer","Bikaner","Bharatpur","Alwar","Sikar","Pali","Sri Ganganagar",
    "Tonk","Beawar","Hanumangarh","Kishangarh","Bhilwara","Banswara","Dausa","Sawai Madhopur","Churu","Nagaur",
    "Bundi","Chittorgarh","Jhalawar","Barmer","Sujangarh","Makrana","Sardarshahar","Lachhmangarh","Ratangarh",
    "Nokha","Nimbahera","Suratgarh","Rajsamand","Lalsot","Pilani","Phalodi","Jaisalmer","Pratapgarh","Mount Abu",
    "Pushkar","Khairthal","Bayana","Sumerpur","Deeg","Rawatbhata","Reodar","Dhaulpur","Karauli","Gangapur","Sojat"
  ],
  "Sikkim": [
    "Gangtok","Namchi","Geyzing","Mangan","Ravangla","Jorethang","Singtam","Rangpo","Pakyong","Soreng","Yuksom",
    "Pelling","Lachung","Lachen","Rinchenpong","Rongli","Rhenock","Chungthang","Dikchu","Melli","Rabongla",
    "Sombaria","Dzongu","Hee Bermiok","Khanitar","Lava","Legship","Lower Pendam","Majhitar","Mangshila","Martam",
    "Naya Bazar","Nayabazar","Pakyong","Phodong","Rongli","Rhenock","Rorathang","Tashiding","Temi","Tenzing","Tinkitam",
    "Toong","Tumin","Upper Pendam","Yangang","Yuksom","Zuluk","Aritar","Padamchen","Phadamchen"
  ],
  "Tamil Nadu": [
    "Chennai","Coimbatore","Madurai","Tiruchirappalli","Salem","Tirunelveli","Tiruppur","Erode","Vellore","Thoothukudi",
    "Dindigul","Thanjavur","Ranipet","Sivakasi","Karur","Udhagamandalam","Hosur","Nagercoil","Kanchipuram","Kumarakonam",
    "Karaikkudi","Neyveli","Cuddalore","Kumbakonam","Tiruvannamalai","Pollachi","Rajapalayam","Gudiyatham","Pudukkottai",
    "Vaniyambadi","Ambur","Nagapattinam","Velankanni","Tiruchengode","Virudhunagar","Karaikudi","Tiruvallur","Krishnagiri",
    "Theni","Namakkal","Dharmapuri","Mayiladuthurai","Thiruvarur","Sivagangai","Tenkasi","Ramanathapuram","Perambalur",
    "Ariyalur","Nilgiris","Kallakurichi","Chengalpattu"
  ],
  "Telangana": [
    "Hyderabad","Warangal","Nizamabad","Karimnagar","Khammam","Ramagundam","Mahabubnagar","Nalgonda","Adilabad",
    "Suryapet","Miryalaguda","Jagtial","Mancherial","Bhongir","Vikarabad","Wanaparthy","Nagarkurnool","Kothagudem",
    "Bodhan","Sangareddy","Medak","Siddipet","Jangaon","Peddapalli","Kamareddy","Kothapet","Mahbubabad","Narayanpet",
    "Asifabad","Bellampalli","Yellandu","Sircilla","Husnabad","Manuguru","Tandur","Zaheerabad","Pebbair","Banswada",
    "Hanamkonda","Vemulawada","Achampet","Mandamarri","Devarakadra","Chityal","Kalwakurthy","Madhira","Yellareddy",
    "Yellampalli","Madanapalle","Thungathurthi","Nakrekal"
  ],
  "Tripura": [
    "Agartala","Udaipur","Dharmanagar","Kailashahar","Belonia","Khowai","Ambassa","Sabroom","Sonamura","Teliamura",
    "Bishalgarh","Kumarghat","Ranirbazar","Santirbazar","Amarpur","Jirania","Mohanpur","Melaghar","Kamalpur",
    "Panisagar","Vishalgarh","Sidhai Mohanpur","Bishramganj","Boxanagar","Jampui Hills","Karbook","Kathalia",
    "Killa","Manu Bazar","Matabari","Mungiakami","Naitong","Padmabil","Pencharthal","Pratapgarh","Salema",
    "Satchand","Simna","Subarnamani","Takarjala","Teliamura","Udaipur","Hrishyamukh","Damcherra","Jolaibari",
    "Kanchanpur","Karbook","Kathalcherra","Kumarghat","Lefunga"
  ],
  "Uttar Pradesh": [
    "Lucknow","Kanpur","Agra","Varanasi","Meerut","Allahabad","Ghaziabad","Aligarh","Bareilly","Moradabad",
    "Saharanpur","Gorakhpur","Noida","Firozabad","Jhansi","Muzaffarnagar","Mathura","Rampur","Shahjahanpur",
    "Faizabad","Etawah","Mirzapur","Bulandshahr","Sambhal","Amroha","Hardoi","Fatehpur","Raebareli","Orai","Sitapur",
    "Bahraich","Modinagar","Unnao","Jaunpur","Lakhimpur","Hathras","Banda","Pilibhit","Mughalsarai","Barabanki",
    "Khurja","Gonda","Mainpuri","Lalitpur","Etah","Deoria","Ujhani","Ghazipur","Sultanpur","Azamgarh"
  ],
  "Uttarakhand": [
    "Dehradun","Haridwar","Roorkee","Haldwani","Rudrapur","Kashipur","Rishikesh","Pithoragarh","Almora","Mussoorie",
    "Nainital","Pauri","Tehri","Champawat","Ranikhet","Pant Nagar","Manglaur","Bageshwar","Gopeshwar","Kotdwar",
    "Khatima","Doiwala","Joshimath","Gangotri","Yamunotri","Kedarnath","Badrinath","Auli","Lansdowne","Tapovan",
    "Munsiari","Dhanaulti","Chakrata","Lohaghat","Berinag","Kausani","Binsar","Mukteshwar","Harsil","Gauchar",
    "Karnaprayag","Devprayag","Rudraprayag","Chamoli","Vikasnagar","Sahaspur","Bhowali","Lalkuan","Sitarganj",
    "Jaspur"
  ],
  "West Bengal": [
    "Kolkata","Howrah","Durgapur","Asansol","Siliguri","Maheshtala","Rajpur Sonarpur","South Dumdum","Kamarhati",
    "Bardhaman","Chandannagar","Malda","Baharampur","Habra","Jalpaiguri","Kharagpur","Shantipur","Dankuni",
    "Dhulian","Ranaghat","Haldia","Raiganj","Krishnanagar","Nabadwip","Medinipur","Jangipur","Bangaon","Cooch Behar",
    "Bhatpara","Panihati","Naihati","Bardhaman","Berhampore","Balurghat","Basirhat","Bankura","Purulia","Suri",
    "Barasat","Halisahar","Hooghly","Madhyamgram","Nadia","Bongaon","Tamluk","Contai","Diamond Harbour","Kalimpong",
    "Darjeeling","Alipurduar"
  ],

  /* ── Union Territories ─────────────────────────────────────────── */
  "Andaman and Nicobar Islands": [
    "Port Blair","Diglipur","Mayabunder","Rangat","Hut Bay","Car Nicobar","Campbell Bay","Nancowry","Garacharma",
    "Bambooflat","Prothrapur","Ferrargunj","Wimberlygunj","Bakultala","Long Island","Havelock","Neil Island",
    "Chowra","Katchal","Kamorta","Teressa","Tirur","Ograbraj","Wandoor","Burmanallah","Chouldari","Brookshabad",
    "Manglutan","Hobdaypur","Macca Pahar","Hopetown","Sippighat","Manjeri","Sakshetra","Brichgunj","Mannarghat",
    "Stewart Sound","Kalighat","Kishori Nagar","Madhuban","Aerial Bay","Smith Island","Ross Island","Viper Island",
    "Cinque Island","Saddle Peak","Mount Harriet","Anderson Island","Swaraj Dweep","Shaheed Dweep"
  ],
  "Chandigarh": [
    "Chandigarh","Manimajra","Daria","Hallomajra","Burail","Maloya","Kishangarh","Sarangpur","Khuda Lahora",
    "Khuda Jassu","Khuda Ali Sher","Mauli Jagran","Dadu Majra","Sarangpur","Behlana","Kajheri","Palsora",
    "Dhanas","Raipur Khurd","Raipur Kalan","Mauli Complex","Sector 17","Sector 22","Sector 35","Sector 43",
    "Sector 7","Sector 8","Sector 9","Sector 10","Sector 11","Sector 14","Sector 15","Sector 16","Sector 18",
    "Sector 19","Sector 20","Sector 21","Sector 23","Sector 24","Sector 25","Sector 26","Sector 27","Sector 28",
    "Sector 29","Sector 30","Sector 31","Sector 32","Sector 33","Sector 34","Sector 36","Sector 37"
  ],
  "Dadra and Nagar Haveli and Daman and Diu": [
    "Daman","Diu","Silvassa","Vapi","Dadra","Naroli","Khanvel","Amli","Rakholi","Samarvarni","Kharadpada",
    "Velugam","Saily","Athal","Karad","Patlara","Surangi","Galonda","Kachigam","Kadaiya","Bhimpore","Magarwada",
    "Marwad","Moti Daman","Nani Daman","Devka","Jampore","Nagoa","Vanakbara","Ghoghla","Bucharwada","Saudwadi",
    "Fudam","Zolawadi","Bhamti","Kasiwada","Kunta","Mandoni","Masat","Piparia","Surangi","Tinkli","Vasona",
    "Vasona Madhuban","Velugam","Wadipan","Demni","Dudhani","Galonda","Kala"
  ],
  "Delhi": [
    "New Delhi","Old Delhi","North Delhi","South Delhi","East Delhi","West Delhi","Central Delhi","North East Delhi",
    "North West Delhi","South West Delhi","South East Delhi","Shahdara","Najafgarh","Dwarka","Rohini","Pitampura",
    "Janakpuri","Karol Bagh","Connaught Place","Saket","Lajpat Nagar","Kalkaji","Greater Kailash","Hauz Khas",
    "Vasant Kunj","Vasant Vihar","Mayur Vihar","Preet Vihar","Laxmi Nagar","Patparganj","Rajouri Garden","Punjabi Bagh",
    "Tilak Nagar","Uttam Nagar","Paschim Vihar","Pitampura","Ashok Vihar","Civil Lines","Kashmere Gate","Karol Bagh",
    "Daryaganj","Chandni Chowk","Pahar Ganj","Mehrauli","Munirka","R K Puram","Sarojini Nagar","Defence Colony",
    "Vasundhara Enclave","Yamuna Vihar","Seelampur"
  ],
  "Jammu and Kashmir": [
    "Srinagar","Jammu","Anantnag","Baramulla","Sopore","Kathua","Udhampur","Punch","Rajouri","Kupwara","Pulwama",
    "Budgam","Ganderbal","Bandipora","Shopian","Kulgam","Doda","Kishtwar","Ramban","Reasi","Samba","Akhnoor",
    "Bhaderwah","Kishtwar","Bishnah","Hiranagar","Vijaypur","Banihal","Batote","Gulmarg","Pahalgam","Sonamarg",
    "Yusmarg","Aharbal","Daksum","Bhalessa","Gulabgarh","Mahore","Patnitop","Sanasar","Mansar","Surinsar",
    "Banihal","Bhaderwah","Doda","Inderwal","Mendhar","Sunderbani","Surankote","Marh","Khour","Salal"
  ],
  "Ladakh": [
    "Leh","Kargil","Drass","Sankoo","Padum","Diskit","Nubra","Pangong","Tso Moriri","Tso Kar","Hemis","Likir",
    "Lamayuru","Alchi","Spituk","Stok","Shey","Thiksey","Hanle","Nyoma","Chumathang","Mahe","Loma","Demchok",
    "Chushul","Tegazong","Korzok","Tangtse","Durbuk","Shyok","Turtuk","Bogdang","Sumur","Panamik","Hundar","Tirit",
    "Tirisha","Thoise","Saspol","Saspochey","Skurbuchan","Skuru","Sasoma","Tia","Tirido","Trongsa","Wakha","Yarma",
    "Zanskar","Zangla"
  ],
  "Lakshadweep": [
    "Kavaratti","Agatti","Amini","Andrott","Bitra","Chetlat","Kadmat","Kalpeni","Kiltan","Minicoy","Bangaram",
    "Suheli","Tinnakara","Parali I","Parali II","Cheriyam","Kalpitti","Pitti","Cheriapani","Suheli Pitti",
    "Valiyakara","Cheriyakara","Ameenidivi","Androth","Kavaratti Island","Agatti Island","Amini Island",
    "Andrott Island","Bitra Island","Chetlat Island","Kadmat Island","Kalpeni Island","Kiltan Island","Minicoy Island",
    "Bangaram Island","Suheli Island","Tinnakara Island","Parali Island","Cheriyam Island","Kalpitti Island",
    "Pitti Island","Cheriapani Island","Suheli Pitti Island","Valiyakara Island","Cheriyakara Island","Ameenidivi Island",
    "Androth Island","Kavaratti Town","Agatti Town","Minicoy Town"
  ],
  "Puducherry": [
    "Puducherry","Karaikal","Mahe","Yanam","Auroville","Ariyankuppam","Bahour","Mannadipet","Nettapakkam","Ozhukarai",
    "Villianur","Embalam","Kalapet","Lawspet","Madagadipet","Madukkarai","Manaveli","Mannadipet","Murungapakkam",
    "Muthialpet","Nallavadu","Nedungadu","Neravy","Nettapakkam","Pandasozhanallur","Polagam","Poraiyur","Pudupet",
    "Reddiarpalayam","Sedarapet","Seliamedu","Sittankudi","Sorakudi","Suthukeny","Thirubuvanai","Thirukkanur",
    "Thirumigirapuram","Thiruvanthipuram","Thiruvenganur","Uppalam","Veerampattinam","Vellanur","Villianur",
    "Yanam","Annaikkadi","Brahmadesam","Cuddalore Road","Eripakkam","Kalitheerthal Kuppam","Karayambuthur"
  ],
};

/** Sorted list of state names — convenient for the first dropdown. */
export const INDIA_STATES = Object.keys(INDIA_STATES_CITIES).sort();

/** Returns the city list for the given state, or [] if the state is unknown. */
export function getCitiesForState(state) {
  if (!state) return [];
  return INDIA_STATES_CITIES[state] || [];
}
