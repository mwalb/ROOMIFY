package org.com.i18n

interface RoomifyStrings {
    val appName: String
    val explore: String
    val dashboard: String
    val savedRooms: String
    val myBookings: String
    val messages: String
    val ownerDashboard: String
    val postARoom: String
    val adminPanel: String
    val account: String
    val search: String
    val filters: String
    val profile: String
    val logout: String
    val loginRegister: String
    val searchPlaceholder: String
    val monthlyRent: String
    val beds: String
    val baths: String
    val area: String
    val available: String
    val pending: String
    val rented: String
    val propertyDetails: String
    val contactOwner: String
    val success: String
    val failure: String
    val language: String
    val selectLanguage: String
    val tagline: String
    val loading: String
    val listYourProperty: String
    val fillDetailsToGetStarted: String
    val ownerInformation: String
    val propertyLocation: String
    val basicInformation: String
    val amenities: String
    val rulesAndContact: String
    val media: String
    val addPhotos: String
    val video: String
    val contract: String
    val postProperty: String
    val ownerFullName: String
    val mapLocation: String
    val enterManually: String
    val streetAddress: String
    val latitude: String
    val longitude: String
    val useCurrentLocation: String
    val searchAndShowOnMap: String
    val propertyTitle: String
    val description: String
    val price: String
    val propertyType: String
    val rooms: String
    val bathrooms: String
    val houseRulesOptional: String
    val phoneNumber: String
    val emailAddressOptional: String
    val postingYourProperty: String
    val mySearches: String
    val imageUnavailable: String
    val noPropertyImages: String
    val noMatchingProperties: String
    fun noMatchingPropertiesIn(area: String): String
    val invalidCoordinatesWarning: String

    // Auth & Onboarding
    val welcomeBack: String
    val signInToAccount: String
    val emailAddress: String
    val email: String
    val password: String
    val confirmPassword: String
    val forgotPassword: String
    val forgot: String
    val login: String
    val signIn: String
    val or: String
    val continueWithGoogle: String
    val guest: String
    val signUp: String
    val back: String
    val findYourNextHome: String
    val enterDetailsToContinue: String
    val createAccount: String
    val joinRoomify: String
    val fullName: String
    val confirmYourPassword: String
    val registering: String
    val alreadyHaveAccount: String
    val selectYourRole: String
    val iAmTenant: String
    val iAmOwner: String
    val iAmDalali: String
    val tenantRole: String
    val ownerRole: String
    val dalaliRole: String
    val welcomeToRoomify: String
    val chooseHowToContinue: String
    val propertyOwner: String
    val dalaliAgent: String

    // Sidebar & Preferences & Navigation
    val myDashboard: String
    val overview: String
    val favourites: String
    val management: String
    val dalaliTools: String
    val administration: String
    val nearbyMe: String
    val preferences: String
    val location: String
    val whatAreYouLookingFor: String
    val maxBudget: String
    val status: String
    val applyFilter: String
    val clearFilter: String
    val clearFilters: String
    val noPropertiesFound: String
    val tryChangingFilters: String
    val furnitureHub: String
    val furnishYourSpace: String
    val accountSettings: String
    val yourLivingSpacePartner: String

    // Profile & Settings
    val accountDetails: String
    val editProfileInfo: String
    val namePhoneBio: String
    val securitySettings: String
    val changePassword: String
    val helpSupport: String
    val editProfile: String
    val saveChanges: String
    val cancel: String
    val bio: String
    val role: String

    // Property & Booking & Chat
    val bookNow: String
    val amenitiesAndFeatures: String
    val houseRules: String
    val reviews: String
    val ratings: String
    val writeAReview: String
    val share: String
    val save: String
    val unsave: String
    val bookingDetails: String
    val moveInDate: String
    val duration: String
    val months: String
    val totalAmount: String
    val confirmBooking: String
    val typeMessage: String
    val send: String

    // Furniture & Shops & AI Planner
    val furnitureCatalog: String
    val postFurniture: String
    val postShop: String
    val spacePlanner: String
    val aiRoomArranger: String
    val arrangeRoom: String
    val dimensions: String
    val itemTitle: String
    val category: String

    // Dashboard & Analytics & Actions
    val totalRevenue: String
    val activeListings: String
    val totalTenants: String
    val totalBookings: String
    val recentActivity: String
    val viewAll: String
    val action: String
    val approve: String
    val reject: String
    val delete: String
    val edit: String
    val all: String
}

object EnStrings : RoomifyStrings {
    override val appName = "ROOMIFY"
    override val explore = "Explore"
    override val dashboard = "Dashboard"
    override val savedRooms = "Favourites"
    override val myBookings = "My Bookings"
    override val messages = "Messages"
    override val ownerDashboard = "Owner Dashboard"
    override val postARoom = "Post a Room"
    override val adminPanel = "Admin Panel"
    override val account = "ACCOUNT"
    override val search = "SEARCH"
    override val filters = "Filters"
    override val profile = "My Profile"
    override val logout = "Logout"
    override val loginRegister = "Login / Register"
    override val searchPlaceholder = "Search by area, property name or location"
    override val monthlyRent = "Monthly Rent"
    override val beds = "Beds"
    override val baths = "Baths"
    override val area = "Area"
    override val available = "Available"
    override val pending = "Pending"
    override val rented = "Rented"
    override val propertyDetails = "Property Details"
    override val contactOwner = "Contact Owner"
    override val success = "Success"
    override val failure = "Failure"
    override val language = "Language"
    override val selectLanguage = "Select Language"
    override val tagline = "Smart Room Solutions"
    override val loading = "Loading..."
    override val listYourProperty = "List Your Property"
    override val fillDetailsToGetStarted = "Fill in the details below to get started"
    override val ownerInformation = "Owner Information"
    override val propertyLocation = "Property Location"
    override val basicInformation = "Basic Information"
    override val amenities = "Amenities"
    override val rulesAndContact = "Rules & Contact"
    override val media = "Media"
    override val addPhotos = "Add Photos"
    override val video = "Video"
    override val contract = "Contract"
    override val postProperty = "Post Property"
    override val ownerFullName = "Owner full name"
    override val mapLocation = "Map Location"
    override val enterManually = "Enter Manually"
    override val streetAddress = "Street address"
    override val latitude = "Latitude"
    override val longitude = "Longitude"
    override val useCurrentLocation = "Use My Current Location"
    override val searchAndShowOnMap = "Search & Show on Map"
    override val propertyTitle = "Property title"
    override val description = "Description"
    override val price = "Price"
    override val propertyType = "Property Type"
    override val rooms = "Rooms"
    override val bathrooms = "Baths"
    override val houseRulesOptional = "House rules (optional)"
    override val phoneNumber = "Phone number"
    override val emailAddressOptional = "Email address (optional)"
    override val postingYourProperty = "Posting your property…"
    override val mySearches = "My Searches"
    override val imageUnavailable = "Image unavailable"
    override val noPropertyImages = "No property images available"
    override val noMatchingProperties = "No matching properties found."
    override fun noMatchingPropertiesIn(area: String) = "No matching properties found in $area."
    override val invalidCoordinatesWarning = "Matching properties found, but with invalid location coordinates."

    // Auth & Onboarding
    override val welcomeBack = "Welcome Back"
    override val signInToAccount = "Sign in to your account"
    override val emailAddress = "Email Address"
    override val email = "Email"
    override val password = "Password"
    override val confirmPassword = "Confirm Password"
    override val forgotPassword = "Forgot Password?"
    override val forgot = "Forgot?"
    override val login = "LOG IN"
    override val signIn = "Sign In"
    override val or = "OR"
    override val continueWithGoogle = "Continue with Google"
    override val guest = "Guest"
    override val signUp = "Sign Up"
    override val back = "Back"
    override val findYourNextHome = "Find your next home"
    override val enterDetailsToContinue = "Enter your details to continue."
    override val createAccount = "Create Account"
    override val joinRoomify = "Join Roomify today"
    override val fullName = "Full Name"
    override val confirmYourPassword = "Confirm your password"
    override val registering = "Registering..."
    override val alreadyHaveAccount = "Already have an account?"
    override val selectYourRole = "Select Your Role"
    override val iAmTenant = "I am looking for a room"
    override val iAmOwner = "I am a property owner"
    override val iAmDalali = "I am a real estate agent (Dalali)"
    override val tenantRole = "Tenant"
    override val ownerRole = "Owner"
    override val dalaliRole = "Dalali"
    override val welcomeToRoomify = "Welcome to Roomify"
    override val chooseHowToContinue = "Choose how you want to continue"
    override val propertyOwner = "Property Owner"
    override val dalaliAgent = "Dalali / Agent"

    // Sidebar & Preferences & Navigation
    override val myDashboard = "My Dashboard"
    override val overview = "Overview"
    override val favourites = "Favourites"
    override val management = "Management"
    override val dalaliTools = "Dalali Tools"
    override val administration = "Administration"
    override val nearbyMe = "Nearby me"
    override val preferences = "Preferences"
    override val location = "Location"
    override val whatAreYouLookingFor = "What are you looking for?"
    override val maxBudget = "Max budget"
    override val status = "Status"
    override val applyFilter = "Apply Filter"
    override val clearFilter = "Clear Filter"
    override val clearFilters = "Clear Filters"
    override val noPropertiesFound = "No properties found"
    override val tryChangingFilters = "Try changing your location, property type, or budget filters."
    override val furnitureHub = "Furniture Hub"
    override val furnishYourSpace = "Furnish Your Space"
    override val accountSettings = "Account Settings"
    override val yourLivingSpacePartner = "Your living space partner"

    // Profile & Settings
    override val accountDetails = "Account Details"
    override val editProfileInfo = "Edit Profile Info"
    override val namePhoneBio = "Name, Phone, Bio"
    override val securitySettings = "Security & Settings"
    override val changePassword = "Change Password"
    override val helpSupport = "Help & Support"
    override val editProfile = "Edit Profile"
    override val saveChanges = "Save Changes"
    override val cancel = "Cancel"
    override val bio = "Bio"
    override val role = "Role"

    // Property & Booking & Chat
    override val bookNow = "Book Now"
    override val amenitiesAndFeatures = "Amenities & Features"
    override val houseRules = "House Rules"
    override val reviews = "Reviews"
    override val ratings = "Ratings"
    override val writeAReview = "Write a Review"
    override val share = "Share"
    override val save = "Save"
    override val unsave = "Unsave"
    override val bookingDetails = "Booking Details"
    override val moveInDate = "Move-in Date"
    override val duration = "Duration"
    override val months = "Months"
    override val totalAmount = "Total Amount"
    override val confirmBooking = "Confirm Booking"
    override val typeMessage = "Type a message..."
    override val send = "Send"

    // Furniture & Shops & AI Planner
    override val furnitureCatalog = "Furniture Catalog"
    override val postFurniture = "Post Furniture"
    override val postShop = "Post Shop"
    override val spacePlanner = "Space Planner"
    override val aiRoomArranger = "AI Room Arranger"
    override val arrangeRoom = "Arrange Room"
    override val dimensions = "Dimensions"
    override val itemTitle = "Item Title"
    override val category = "Category"

    // Dashboard & Analytics & Actions
    override val totalRevenue = "Total Revenue"
    override val activeListings = "Active Listings"
    override val totalTenants = "Total Tenants"
    override val totalBookings = "Total Bookings"
    override val recentActivity = "Recent Activity"
    override val viewAll = "View All"
    override val action = "Action"
    override val approve = "Approve"
    override val reject = "Reject"
    override val delete = "Delete"
    override val edit = "Edit"
    override val all = "All"
}

object SwStrings : RoomifyStrings {
    override val appName = "ROOMIFY"
    override val explore = "Gundua"
    override val dashboard = "Dashibodi"
    override val savedRooms = "Vyumba Vilivyohifadhiwa"
    override val myBookings = "Viwango Vyangu"
    override val messages = "Ujumbe"
    override val ownerDashboard = "Dashibodi ya Mmiliki"
    override val postARoom = "Weka Chumba"
    override val adminPanel = "Jopo la Utawala"
    override val account = "AKAUNTI"
    override val search = "TAFUTA"
    override val filters = "Vichujio"
    override val profile = "Wasifu Wangu"
    override val logout = "Ondoka"
    override val loginRegister = "Ingia / Jisajili"
    override val searchPlaceholder = "Tafuta kwa eneo, jina la nyumba au mahali"
    override val monthlyRent = "Kodi ya Mwezi"
    override val beds = "Vitanda"
    override val baths = "Bafu"
    override val area = "Eneo"
    override val available = "Inapatikana"
    override val pending = "Inasubiri"
    override val rented = "Imepangishwa"
    override val propertyDetails = "Maelezo ya Nyumba"
    override val contactOwner = "Wasiliana na Mmiliki"
    override val success = "Mafanikio"
    override val failure = "Imeshindwa"
    override val language = "Lugha"
    override val selectLanguage = "Chagua Lugha"
    override val tagline = "Ufumbuzi wa Vyumba Mahiri"
    override val loading = "Inapakia..."
    override val listYourProperty = "Orodhesha Nyumba Yako"
    override val fillDetailsToGetStarted = "Jaza maelezo hapa chini ili kuanza"
    override val ownerInformation = "Habari za Mmiliki"
    override val propertyLocation = "Mahali pa Nyumba"
    override val basicInformation = "Habari za Msingi"
    override val amenities = "Vifaa"
    override val rulesAndContact = "Sheria na Mawasiliano"
    override val media = "Picha na Video"
    override val addPhotos = "Ongeza Picha"
    override val video = "Video"
    override val contract = "Mkataba"
    override val postProperty = "Weka Nyumba"
    override val ownerFullName = "Jina kamili la mmiliki"
    override val mapLocation = "Mahali kwenye Ramani"
    override val enterManually = "Ingiza kwa Mkono"
    override val streetAddress = "Anwani ya mtaa"
    override val latitude = "Latitudo"
    override val longitude = "Longitudo"
    override val useCurrentLocation = "Tumia Mahali Pangu"
    override val searchAndShowOnMap = "Tafuta na Onyesha kwenye Ramani"
    override val propertyTitle = "Kichwa cha nyumba"
    override val description = "Maelezo"
    override val price = "Bei"
    override val propertyType = "Aina ya Nyumba"
    override val rooms = "Vyumba"
    override val bathrooms = "Bafu"
    override val houseRulesOptional = "Sheria za nyumba (hiari)"
    override val phoneNumber = "Namba ya simu"
    override val emailAddressOptional = "Anwani ya barua pepe (hiari)"
    override val postingYourProperty = "Inaweka nyumba yako…"
    override val mySearches = "Utafutaji Wangu"
    override val imageUnavailable = "Picha haipatikani"
    override val noPropertyImages = "Hakuna picha za nyumba zinazopatikana"
    override val noMatchingProperties = "Hakuna nyumba inayolingana na vigezo ulivyochagua."
    override fun noMatchingPropertiesIn(area: String) = "Hakuna nyumba inayolingana na vigezo katika $area."
    override val invalidCoordinatesWarning = "Kuna nyumba zinazolingana, lakini hazina anwani sahihi za ramani."

    // Auth & Onboarding
    override val welcomeBack = "Karibu Tena"
    override val signInToAccount = "Ingia kwenye akaunti yako"
    override val emailAddress = "Anwani ya Barua Pepe"
    override val email = "Barua Pepe"
    override val password = "Nenosiri"
    override val confirmPassword = "Thibitisha Nenosiri"
    override val forgotPassword = "Umesahau Nenosiri?"
    override val forgot = "Umesahau?"
    override val login = "INGIA"
    override val signIn = "Ingia"
    override val or = "AU"
    override val continueWithGoogle = "Endelea na Google"
    override val guest = "Mgeni"
    override val signUp = "Jisajili"
    override val back = "Rudi"
    override val findYourNextHome = "Tafuta nyumba yako ijayo"
    override val enterDetailsToContinue = "Ingiza maelezo yako ili kuendelea."
    override val createAccount = "Fungua Akaunti"
    override val joinRoomify = "Jiunge na Roomify leo"
    override val fullName = "Jina Kamili"
    override val confirmYourPassword = "Thibitisha nenosiri yako"
    override val registering = "Inajisajili..."
    override val alreadyHaveAccount = "Je, tayari una akaunti?"
    override val selectYourRole = "Chagua Jukumu Lako"
    override val iAmTenant = "Natafuta chumba"
    override val iAmOwner = "Mimi ni mmiliki wa nyumba"
    override val iAmDalali = "Mimi ni dalali wa nyumba"
    override val tenantRole = "Mpangaji"
    override val ownerRole = "Mmiliki"
    override val dalaliRole = "Dalali"
    override val welcomeToRoomify = "Karibu Roomify"
    override val chooseHowToContinue = "Chagua jinsi unavyotaka kuendelea"
    override val propertyOwner = "Mmiliki wa Nyumba"
    override val dalaliAgent = "Dalali / Wakala"

    // Sidebar & Preferences & Navigation
    override val myDashboard = "Dashibodi Yangu"
    override val overview = "Muhtasari"
    override val favourites = "Vipendwa"
    override val management = "Usimamizi"
    override val dalaliTools = "Zana za Dalali"
    override val administration = "Utawala"
    override val nearbyMe = "Karibu nami"
    override val preferences = "Vipendeleo"
    override val location = "Mahali"
    override val whatAreYouLookingFor = "Unatafuta nini?"
    override val maxBudget = "Bajeti ya juu"
    override val status = "Hali"
    override val applyFilter = "Tumia Chujio"
    override val clearFilter = "Futa Chujio"
    override val clearFilters = "Futa Vichujio"
    override val noPropertiesFound = "Hakuna nyumba zilizopatikana"
    override val tryChangingFilters = "Jaribu kubadilisha mahali, aina ya nyumba, au vichujio vya bajeti."
    override val furnitureHub = "Kituo cha Samani"
    override val furnishYourSpace = "Pamba Nafasi Yako"
    override val accountSettings = "Mipangilio ya Akaunti"
    override val yourLivingSpacePartner = "Mshirika wako wa nafasi ya kuishi"

    // Profile & Settings
    override val accountDetails = "Maelezo ya Akaunti"
    override val editProfileInfo = "Hariri Wasifu"
    override val namePhoneBio = "Jina, Simu, Maelezo"
    override val securitySettings = "Usalama na Mipangilio"
    override val changePassword = "Badilisha Nenosiri"
    override val helpSupport = "Msaada na Usaidizi"
    override val editProfile = "Hariri Wasifu"
    override val saveChanges = "Hifadhi Mabadiliko"
    override val cancel = "Ghairi"
    override val bio = "Maelezo binafsi"
    override val role = "Jukumu"

    // Property & Booking & Chat
    override val bookNow = "Weka Sasa"
    override val amenitiesAndFeatures = "Vifaa na Sifa"
    override val houseRules = "Sheria za Nyumba"
    override val reviews = "Mapitio"
    override val ratings = "Tathmini"
    override val writeAReview = "Andika Mapitio"
    override val share = "Shiriki"
    override val save = "Hifadhi"
    override val unsave = "Ondoa Vipendwa"
    override val bookingDetails = "Maelezo ya Booking"
    override val moveInDate = "Tarehe ya Kuhama"
    override val duration = "Muda"
    override val months = "Miezi"
    override val totalAmount = "Jumla ya Kiasi"
    override val confirmBooking = "Thibitisha Booking"
    override val typeMessage = "Andika ujumbe..."
    override val send = "Tuma"

    // Furniture & Shops & AI Planner
    override val furnitureCatalog = "Katalogi ya Samani"
    override val postFurniture = "Weka Samani"
    override val postShop = "Weka Duka"
    override val spacePlanner = "Mpangaji wa Nafasi"
    override val aiRoomArranger = "Mpangaji wa Chumba wa AI"
    override val arrangeRoom = "Panga Chumba"
    override val dimensions = "Vipimo"
    override val itemTitle = "Jina la Kitu"
    override val category = "Kipengele"

    // Dashboard & Analytics & Actions
    override val totalRevenue = "Mapato Yote"
    override val activeListings = "Orodha Zinazofanya Kazi"
    override val totalTenants = "Wapangaji Wote"
    override val totalBookings = "Bookings Zote"
    override val recentActivity = "Shughuli za Hivi Karibuni"
    override val viewAll = "Tazama Zote"
    override val action = "Hatua"
    override val approve = "Thibitisha"
    override val reject = "Kataa"
    override val delete = "Futa"
    override val edit = "Hariri"
    override val all = "Zote"
}
