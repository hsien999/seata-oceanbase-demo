// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.extend.datatype;

public class ZONEIDMAP
{
    private static TableClass zoneid;
    protected static final int INV_ZONEID = -1;
    
    public static int getID(final String timezone) {
        final Integer timezoneId = ZONEIDMAP.zoneid.getID(timezone);
        int zoneId;
        if (timezoneId == null) {
            zoneId = -1;
        }
        else {
            zoneId = timezoneId;
        }
        return zoneId;
    }
    
    public static String getRegion(final int region) {
        String zone = ZONEIDMAP.zoneid.getZone(region);
        if (zone == null) {
            zone = ZONEIDMAP.zoneid.getOldZone(region);
        }
        return zone;
    }
    
    public static boolean isValidID(final int region) {
        return getRegion(region) != null;
    }
    
    public static boolean isValidRegion(final String region) {
        return getID(region) != -1;
    }
    
    static {
        (ZONEIDMAP.zoneid = new TableClass(544, 0.99f)).put("Africa/Asmera", 46);
        ZONEIDMAP.zoneid.put("Africa/Bamako", 58);
        ZONEIDMAP.zoneid.put("Africa/Bangui", 37);
        ZONEIDMAP.zoneid.put("Africa/Banjul", 49);
        ZONEIDMAP.zoneid.put("Africa/Bissau", 52);
        ZONEIDMAP.zoneid.put("Africa/Blantyre", 57);
        ZONEIDMAP.zoneid.put("Africa/Bujumbura", 35);
        ZONEIDMAP.zoneid.put("Africa/Cairo", 44);
        ZONEIDMAP.zoneid.put("Africa/Casablanca", 61);
        ZONEIDMAP.zoneid.put("Africa/Conakry", 51);
        ZONEIDMAP.zoneid.put("Africa/Dakar", 69);
        ZONEIDMAP.zoneid.put("Africa/Dar_es_Salaam", 75);
        ZONEIDMAP.zoneid.put("Africa/Djibouti", 43);
        ZONEIDMAP.zoneid.put("Africa/Douala", 36);
        ZONEIDMAP.zoneid.put("Africa/Freetown", 70);
        ZONEIDMAP.zoneid.put("Africa/Gaborone", 33);
        ZONEIDMAP.zoneid.put("Africa/Harare", 80);
        ZONEIDMAP.zoneid.put("Africa/Johannesburg", 72);
        ZONEIDMAP.zoneid.put("Africa/Kampala", 78);
        ZONEIDMAP.zoneid.put("Africa/Khartoum", 73);
        ZONEIDMAP.zoneid.put("Africa/Kigali", 67);
        ZONEIDMAP.zoneid.put("Africa/Kinshasa", 39);
        ZONEIDMAP.zoneid.put("Africa/Lagos", 66);
        ZONEIDMAP.zoneid.put("Africa/Libreville", 48);
        ZONEIDMAP.zoneid.put("Africa/Lome", 76);
        ZONEIDMAP.zoneid.put("Africa/Luanda", 31);
        ZONEIDMAP.zoneid.put("Africa/Lubumbashi", 40);
        ZONEIDMAP.zoneid.put("Africa/Lusaka", 79);
        ZONEIDMAP.zoneid.put("Africa/Malabo", 45);
        ZONEIDMAP.zoneid.put("Africa/Maputo", 63);
        ZONEIDMAP.zoneid.put("Africa/Maseru", 54);
        ZONEIDMAP.zoneid.put("Africa/Mbabane", 74);
        ZONEIDMAP.zoneid.put("Africa/Mogadishu", 71);
        ZONEIDMAP.zoneid.put("Africa/Monrovia", 55);
        ZONEIDMAP.zoneid.put("Africa/Nairobi", 53);
        ZONEIDMAP.zoneid.put("Africa/Ndjamena", 38);
        ZONEIDMAP.zoneid.put("Africa/Niamey", 65);
        ZONEIDMAP.zoneid.put("Africa/Nouakchott", 60);
        ZONEIDMAP.zoneid.put("Africa/Ouagadougou", 34);
        ZONEIDMAP.zoneid.put("Africa/Porto-Novo", 32);
        ZONEIDMAP.zoneid.put("Africa/Sao_Tome", 68);
        ZONEIDMAP.zoneid.put("Africa/Timbuktu", 59);
        ZONEIDMAP.zoneid.put("Africa/Tripoli", 56);
        ZONEIDMAP.zoneid.put("Africa/Tunis", 77);
        ZONEIDMAP.zoneid.put("Africa/Windhoek", 64);
        ZONEIDMAP.zoneid.put("America/Adak", 108);
        ZONEIDMAP.zoneid.put("America/Anchorage", 106);
        ZONEIDMAP.zoneid.put("America/Anguilla", 146);
        ZONEIDMAP.zoneid.put("America/Antigua", 147);
        ZONEIDMAP.zoneid.put("America/Aruba", 181);
        ZONEIDMAP.zoneid.put("America/Asuncion", 200);
        ZONEIDMAP.zoneid.put("America/Barbados", 149);
        ZONEIDMAP.zoneid.put("America/Belize", 150);
        ZONEIDMAP.zoneid.put("America/Bogota", 195);
        ZONEIDMAP.zoneid.put("America/Buenos_Aires", 175);
        ZONEIDMAP.zoneid.put("America/Argentina/Buenos_Aires", 687);
        ZONEIDMAP.zoneid.put("America/Caracas", 205);
        ZONEIDMAP.zoneid.put("America/Cayenne", 198);
        ZONEIDMAP.zoneid.put("America/Cayman", 151);
        ZONEIDMAP.zoneid.put("America/Chicago", 101);
        ZONEIDMAP.zoneid.put("America/Costa_Rica", 152);
        ZONEIDMAP.zoneid.put("America/Cuiaba", 189);
        ZONEIDMAP.zoneid.put("America/Curacao", 196);
        ZONEIDMAP.zoneid.put("America/Dawson_Creek", 131);
        ZONEIDMAP.zoneid.put("America/Denver", 102);
        ZONEIDMAP.zoneid.put("America/Dominica", 154);
        ZONEIDMAP.zoneid.put("America/Edmonton", 129);
        ZONEIDMAP.zoneid.put("America/El_Salvador", 156);
        ZONEIDMAP.zoneid.put("America/Fortaleza", 185);
        ZONEIDMAP.zoneid.put("America/Godthab", 207);
        ZONEIDMAP.zoneid.put("America/Grand_Turk", 172);
        ZONEIDMAP.zoneid.put("America/Grenada", 157);
        ZONEIDMAP.zoneid.put("America/Guadeloupe", 158);
        ZONEIDMAP.zoneid.put("America/Guatemala", 159);
        ZONEIDMAP.zoneid.put("America/Guayaquil", 197);
        ZONEIDMAP.zoneid.put("America/Guyana", 199);
        ZONEIDMAP.zoneid.put("America/Halifax", 120);
        ZONEIDMAP.zoneid.put("America/Havana", 153);
        ZONEIDMAP.zoneid.put("America/Indianapolis", 111);
        ZONEIDMAP.zoneid.put("America/Jamaica", 162);
        ZONEIDMAP.zoneid.put("America/La_Paz", 182);
        ZONEIDMAP.zoneid.put("America/Lima", 201);
        ZONEIDMAP.zoneid.put("America/Los_Angeles", 103);
        ZONEIDMAP.zoneid.put("America/Managua", 165);
        ZONEIDMAP.zoneid.put("America/Manaus", 192);
        ZONEIDMAP.zoneid.put("America/Martinique", 163);
        ZONEIDMAP.zoneid.put("America/Mazatlan", 144);
        ZONEIDMAP.zoneid.put("America/Mexico_City", 141);
        ZONEIDMAP.zoneid.put("America/Miquelon", 170);
        ZONEIDMAP.zoneid.put("America/Montevideo", 204);
        ZONEIDMAP.zoneid.put("America/Montreal", 122);
        ZONEIDMAP.zoneid.put("America/Montserrat", 164);
        ZONEIDMAP.zoneid.put("America/Nassau", 148);
        ZONEIDMAP.zoneid.put("America/New_York", 100);
        ZONEIDMAP.zoneid.put("America/Noronha", 183);
        ZONEIDMAP.zoneid.put("America/Panama", 166);
        ZONEIDMAP.zoneid.put("America/Paramaribo", 202);
        ZONEIDMAP.zoneid.put("America/Phoenix", 109);
        ZONEIDMAP.zoneid.put("America/Port-au-Prince", 160);
        ZONEIDMAP.zoneid.put("America/Port_of_Spain", 203);
        ZONEIDMAP.zoneid.put("America/Porto_Acre", 193);
        ZONEIDMAP.zoneid.put("America/Puerto_Rico", 167);
        ZONEIDMAP.zoneid.put("America/Regina", 127);
        ZONEIDMAP.zoneid.put("America/Santiago", 194);
        ZONEIDMAP.zoneid.put("America/Santo_Domingo", 155);
        ZONEIDMAP.zoneid.put("America/Sao_Paulo", 188);
        ZONEIDMAP.zoneid.put("America/Scoresbysund", 206);
        ZONEIDMAP.zoneid.put("America/St_Johns", 118);
        ZONEIDMAP.zoneid.put("America/St_Kitts", 168);
        ZONEIDMAP.zoneid.put("America/St_Lucia", 169);
        ZONEIDMAP.zoneid.put("America/St_Thomas", 174);
        ZONEIDMAP.zoneid.put("America/St_Vincent", 171);
        ZONEIDMAP.zoneid.put("America/Tegucigalpa", 161);
        ZONEIDMAP.zoneid.put("America/Thule", 208);
        ZONEIDMAP.zoneid.put("America/Tijuana", 145);
        ZONEIDMAP.zoneid.put("America/Tortola", 173);
        ZONEIDMAP.zoneid.put("America/Vancouver", 130);
        ZONEIDMAP.zoneid.put("America/Winnipeg", 126);
        ZONEIDMAP.zoneid.put("PST", 2151);
        ZONEIDMAP.zoneid.put("EST", 211);
        ZONEIDMAP.zoneid.put("CST", 1637);
        ZONEIDMAP.zoneid.put("Antarctica/Casey", 230);
        ZONEIDMAP.zoneid.put("Antarctica/DumontDUrville", 233);
        ZONEIDMAP.zoneid.put("Antarctica/Mawson", 232);
        ZONEIDMAP.zoneid.put("Antarctica/McMurdo", 236);
        ZONEIDMAP.zoneid.put("Antarctica/Palmer", 235);
        ZONEIDMAP.zoneid.put("Asia/Aden", 302);
        ZONEIDMAP.zoneid.put("Asia/Amman", 268);
        ZONEIDMAP.zoneid.put("Asia/Anadyr", 312);
        ZONEIDMAP.zoneid.put("Asia/Aqtau", 271);
        ZONEIDMAP.zoneid.put("Asia/Aqtobe", 270);
        ZONEIDMAP.zoneid.put("Asia/Ashkhabad", 297);
        ZONEIDMAP.zoneid.put("Asia/Baghdad", 265);
        ZONEIDMAP.zoneid.put("Asia/Bahrain", 243);
        ZONEIDMAP.zoneid.put("Asia/Baku", 242);
        ZONEIDMAP.zoneid.put("Asia/Bangkok", 296);
        ZONEIDMAP.zoneid.put("Asia/Beirut", 277);
        ZONEIDMAP.zoneid.put("Asia/Bishkek", 272);
        ZONEIDMAP.zoneid.put("Asia/Brunei", 246);
        ZONEIDMAP.zoneid.put("Asia/Calcutta", 260);
        ZONEIDMAP.zoneid.put("Asia/Colombo", 293);
        ZONEIDMAP.zoneid.put("Asia/Dacca", 244);
        ZONEIDMAP.zoneid.put("Asia/Damascus", 294);
        ZONEIDMAP.zoneid.put("Asia/Dubai", 298);
        ZONEIDMAP.zoneid.put("Asia/Dushanbe", 295);
        ZONEIDMAP.zoneid.put("Asia/Hong_Kong", 254);
        ZONEIDMAP.zoneid.put("Asia/Irkutsk", 307);
        ZONEIDMAP.zoneid.put("Asia/Jakarta", 261);
        ZONEIDMAP.zoneid.put("Asia/Jayapura", 263);
        ZONEIDMAP.zoneid.put("Asia/Jerusalem", 266);
        ZONEIDMAP.zoneid.put("Asia/Kabul", 240);
        ZONEIDMAP.zoneid.put("Asia/Kamchatka", 311);
        ZONEIDMAP.zoneid.put("Asia/Karachi", 284);
        ZONEIDMAP.zoneid.put("Asia/Katmandu", 282);
        ZONEIDMAP.zoneid.put("Asia/Krasnoyarsk", 306);
        ZONEIDMAP.zoneid.put("Asia/Kuala_Lumpur", 278);
        ZONEIDMAP.zoneid.put("Asia/Kuwait", 275);
        ZONEIDMAP.zoneid.put("Asia/Macao", 256);
        ZONEIDMAP.zoneid.put("Asia/Magadan", 310);
        ZONEIDMAP.zoneid.put("Asia/Manila", 286);
        ZONEIDMAP.zoneid.put("Asia/Muscat", 283);
        ZONEIDMAP.zoneid.put("Asia/Nicosia", 257);
        ZONEIDMAP.zoneid.put("Asia/Novosibirsk", 305);
        ZONEIDMAP.zoneid.put("Asia/Phnom_Penh", 248);
        ZONEIDMAP.zoneid.put("Asia/Pyongyang", 274);
        ZONEIDMAP.zoneid.put("Asia/Qatar", 287);
        ZONEIDMAP.zoneid.put("Asia/Rangoon", 247);
        ZONEIDMAP.zoneid.put("Asia/Riyadh", 288);
        ZONEIDMAP.zoneid.put("Asia/Saigon", 301);
        ZONEIDMAP.zoneid.put("Asia/Seoul", 273);
        ZONEIDMAP.zoneid.put("Asia/Shanghai", 250);
        ZONEIDMAP.zoneid.put("Asia/Singapore", 292);
        ZONEIDMAP.zoneid.put("Asia/Taipei", 255);
        ZONEIDMAP.zoneid.put("Asia/Tashkent", 300);
        ZONEIDMAP.zoneid.put("Asia/Tbilisi", 258);
        ZONEIDMAP.zoneid.put("Asia/Tehran", 264);
        ZONEIDMAP.zoneid.put("Asia/Thimbu", 245);
        ZONEIDMAP.zoneid.put("Asia/Tokyo", 267);
        ZONEIDMAP.zoneid.put("Asia/Ujung_Pandang", 262);
        ZONEIDMAP.zoneid.put("Asia/Ulan_Bator", 793);
        ZONEIDMAP.zoneid.put("Asia/Vientiane", 276);
        ZONEIDMAP.zoneid.put("Asia/Vladivostok", 309);
        ZONEIDMAP.zoneid.put("Asia/Yakutsk", 308);
        ZONEIDMAP.zoneid.put("Asia/Yekaterinburg", 303);
        ZONEIDMAP.zoneid.put("Asia/Yerevan", 241);
        ZONEIDMAP.zoneid.put("Atlantic/Azores", 336);
        ZONEIDMAP.zoneid.put("Atlantic/Bermuda", 330);
        ZONEIDMAP.zoneid.put("Atlantic/Canary", 338);
        ZONEIDMAP.zoneid.put("Atlantic/Cape_Verde", 339);
        ZONEIDMAP.zoneid.put("Atlantic/Faeroe", 333);
        ZONEIDMAP.zoneid.put("Atlantic/Jan_Mayen", 335);
        ZONEIDMAP.zoneid.put("Atlantic/Reykjavik", 334);
        ZONEIDMAP.zoneid.put("Atlantic/South_Georgia", 332);
        ZONEIDMAP.zoneid.put("Atlantic/St_Helena", 340);
        ZONEIDMAP.zoneid.put("Atlantic/Stanley", 331);
        ZONEIDMAP.zoneid.put("Australia/Adelaide", 349);
        ZONEIDMAP.zoneid.put("Australia/Brisbane", 347);
        ZONEIDMAP.zoneid.put("Australia/Darwin", 345);
        ZONEIDMAP.zoneid.put("Australia/Perth", 346);
        ZONEIDMAP.zoneid.put("Australia/Sydney", 352);
        ZONEIDMAP.zoneid.put("Australia/ACT", 864);
        ZONEIDMAP.zoneid.put("EET", 368);
        ZONEIDMAP.zoneid.put("GMT", 513);
        ZONEIDMAP.zoneid.put("UTC", 5121);
        ZONEIDMAP.zoneid.put("MET", 367);
        ZONEIDMAP.zoneid.put("MST", 212);
        ZONEIDMAP.zoneid.put("HST", 213);
        ZONEIDMAP.zoneid.put("Europe/Amsterdam", 396);
        ZONEIDMAP.zoneid.put("Europe/Andorra", 373);
        ZONEIDMAP.zoneid.put("Europe/Athens", 385);
        ZONEIDMAP.zoneid.put("Europe/Belgrade", 412);
        ZONEIDMAP.zoneid.put("Europe/Berlin", 383);
        ZONEIDMAP.zoneid.put("Europe/Brussels", 376);
        ZONEIDMAP.zoneid.put("Europe/Bucharest", 400);
        ZONEIDMAP.zoneid.put("Europe/Budapest", 386);
        ZONEIDMAP.zoneid.put("Europe/Chisinau", 393);
        ZONEIDMAP.zoneid.put("Europe/Copenhagen", 379);
        ZONEIDMAP.zoneid.put("Europe/Dublin", 371);
        ZONEIDMAP.zoneid.put("Europe/Gibraltar", 384);
        ZONEIDMAP.zoneid.put("Europe/Guernsey", 2417);
        ZONEIDMAP.zoneid.put("Europe/Helsinki", 381);
        ZONEIDMAP.zoneid.put("Isle_of_Man", 2929);
        ZONEIDMAP.zoneid.put("Europe/Istanbul", 407);
        ZONEIDMAP.zoneid.put("Europe/Jersey", 1905);
        ZONEIDMAP.zoneid.put("Europe/Kaliningrad", 401);
        ZONEIDMAP.zoneid.put("Europe/Kiev", 408);
        ZONEIDMAP.zoneid.put("Europe/Lisbon", 399);
        ZONEIDMAP.zoneid.put("Europe/London", 369);
        ZONEIDMAP.zoneid.put("Europe/Luxembourg", 391);
        ZONEIDMAP.zoneid.put("Europe/Madrid", 404);
        ZONEIDMAP.zoneid.put("Europe/Malta", 392);
        ZONEIDMAP.zoneid.put("Europe/Mariehamn", 893);
        ZONEIDMAP.zoneid.put("Europe/Minsk", 375);
        ZONEIDMAP.zoneid.put("Europe/Monaco", 395);
        ZONEIDMAP.zoneid.put("Europe/Moscow", 402);
        ZONEIDMAP.zoneid.put("Europe/Oslo", 397);
        ZONEIDMAP.zoneid.put("Europe/Paris", 382);
        ZONEIDMAP.zoneid.put("Europe/Podgorica", 2972);
        ZONEIDMAP.zoneid.put("Europe/Prague", 378);
        ZONEIDMAP.zoneid.put("Europe/Riga", 388);
        ZONEIDMAP.zoneid.put("Europe/Rome", 387);
        ZONEIDMAP.zoneid.put("Europe/Samara", 403);
        ZONEIDMAP.zoneid.put("Europe/Simferopol", 411);
        ZONEIDMAP.zoneid.put("Europe/Sofia", 377);
        ZONEIDMAP.zoneid.put("Europe/Stockholm", 405);
        ZONEIDMAP.zoneid.put("Europe/Tallinn", 380);
        ZONEIDMAP.zoneid.put("Europe/Tirane", 372);
        ZONEIDMAP.zoneid.put("Europe/Vaduz", 389);
        ZONEIDMAP.zoneid.put("Europe/Vienna", 374);
        ZONEIDMAP.zoneid.put("Europe/Vilnius", 390);
        ZONEIDMAP.zoneid.put("Europe/Volgograd", 413);
        ZONEIDMAP.zoneid.put("Europe/Warsaw", 398);
        ZONEIDMAP.zoneid.put("Europe/Zurich", 406);
        ZONEIDMAP.zoneid.put("Indian/Antananarivo", 438);
        ZONEIDMAP.zoneid.put("Indian/Chagos", 436);
        ZONEIDMAP.zoneid.put("Indian/Christmas", 439);
        ZONEIDMAP.zoneid.put("Indian/Cocos", 440);
        ZONEIDMAP.zoneid.put("Indian/Comoro", 441);
        ZONEIDMAP.zoneid.put("Indian/Kerguelen", 435);
        ZONEIDMAP.zoneid.put("Indian/Mahe", 442);
        ZONEIDMAP.zoneid.put("Indian/Maldives", 437);
        ZONEIDMAP.zoneid.put("Indian/Mauritius", 443);
        ZONEIDMAP.zoneid.put("Indian/Mayotte", 444);
        ZONEIDMAP.zoneid.put("Indian/Reunion", 445);
        ZONEIDMAP.zoneid.put("Pacific/Apia", 479);
        ZONEIDMAP.zoneid.put("Pacific/Auckland", 471);
        ZONEIDMAP.zoneid.put("Pacific/Chatham", 472);
        ZONEIDMAP.zoneid.put("Pacific/Easter", 451);
        ZONEIDMAP.zoneid.put("Pacific/Efate", 488);
        ZONEIDMAP.zoneid.put("Pacific/Enderbury", 460);
        ZONEIDMAP.zoneid.put("Pacific/Fakaofo", 482);
        ZONEIDMAP.zoneid.put("Pacific/Fiji", 454);
        ZONEIDMAP.zoneid.put("Pacific/Funafuti", 484);
        ZONEIDMAP.zoneid.put("Pacific/Galapagos", 452);
        ZONEIDMAP.zoneid.put("Pacific/Gambier", 455);
        ZONEIDMAP.zoneid.put("Pacific/Guadalcanal", 481);
        ZONEIDMAP.zoneid.put("Pacific/Guam", 458);
        ZONEIDMAP.zoneid.put("Pacific/Honolulu", 450);
        ZONEIDMAP.zoneid.put("Pacific/Kiritimati", 461);
        ZONEIDMAP.zoneid.put("Pacific/Kosrae", 468);
        ZONEIDMAP.zoneid.put("Pacific/Majuro", 463);
        ZONEIDMAP.zoneid.put("Pacific/Marquesas", 456);
        ZONEIDMAP.zoneid.put("Pacific/Nauru", 469);
        ZONEIDMAP.zoneid.put("Pacific/Niue", 473);
        ZONEIDMAP.zoneid.put("Pacific/Norfolk", 474);
        ZONEIDMAP.zoneid.put("Pacific/Noumea", 470);
        ZONEIDMAP.zoneid.put("Pacific/Pago_Pago", 478);
        ZONEIDMAP.zoneid.put("Pacific/Palau", 475);
        ZONEIDMAP.zoneid.put("Pacific/Pitcairn", 477);
        ZONEIDMAP.zoneid.put("Pacific/Ponape", 467);
        ZONEIDMAP.zoneid.put("Pacific/Port_Moresby", 476);
        ZONEIDMAP.zoneid.put("Pacific/Rarotonga", 453);
        ZONEIDMAP.zoneid.put("Pacific/Saipan", 462);
        ZONEIDMAP.zoneid.put("Pacific/Tahiti", 457);
        ZONEIDMAP.zoneid.put("Pacific/Tarawa", 459);
        ZONEIDMAP.zoneid.put("Pacific/Tongatapu", 483);
        ZONEIDMAP.zoneid.put("Pacific/Truk", 466);
        ZONEIDMAP.zoneid.put("Pacific/Wake", 487);
        ZONEIDMAP.zoneid.put("Pacific/Wallis", 489);
        ZONEIDMAP.zoneid.put("Africa/Brazzaville", 41);
        ZONEIDMAP.zoneid.put("Egypt", 556);
        ZONEIDMAP.zoneid.put("Africa/Ceuta", 81);
        ZONEIDMAP.zoneid.put("Africa/El_Aaiun", 62);
        ZONEIDMAP.zoneid.put("Libya", 568);
        ZONEIDMAP.zoneid.put("America/Atka", 620);
        ZONEIDMAP.zoneid.put("US/Aleutian", 1132);
        ZONEIDMAP.zoneid.put("US/Alaska", 618);
        ZONEIDMAP.zoneid.put("America/Araguaina", 186);
        ZONEIDMAP.zoneid.put("America/Belem", 184);
        ZONEIDMAP.zoneid.put("America/Boa_Vista", 191);
        ZONEIDMAP.zoneid.put("America/Boise", 110);
        ZONEIDMAP.zoneid.put("America/Cambridge_Bay", 135);
        ZONEIDMAP.zoneid.put("America/Cancun", 140);
        ZONEIDMAP.zoneid.put("America/Catamarca", 179);
        ZONEIDMAP.zoneid.put("CST6CDT", 215);
        ZONEIDMAP.zoneid.put("US/Central", 613);
        ZONEIDMAP.zoneid.put("America/Chihuahua", 142);
        ZONEIDMAP.zoneid.put("America/Cordoba", 177);
        ZONEIDMAP.zoneid.put("America/Dawson", 139);
        ZONEIDMAP.zoneid.put("America/Shiprock", 1638);
        ZONEIDMAP.zoneid.put("MST7MDT", 216);
        ZONEIDMAP.zoneid.put("Navajo", 614);
        ZONEIDMAP.zoneid.put("US/Mountain", 1126);
        ZONEIDMAP.zoneid.put("America/Detroit", 116);
        ZONEIDMAP.zoneid.put("US/Michigan", 628);
        ZONEIDMAP.zoneid.put("Canada/Mountain", 641);
        ZONEIDMAP.zoneid.put("America/Glace_Bay", 121);
        ZONEIDMAP.zoneid.put("America/Goose_Bay", 119);
        ZONEIDMAP.zoneid.put("Canada/Atlantic", 632);
        ZONEIDMAP.zoneid.put("Cuba", 665);
        ZONEIDMAP.zoneid.put("America/Hermosillo", 143);
        ZONEIDMAP.zoneid.put("America/Indiana/Knox", 113);
        ZONEIDMAP.zoneid.put("America/Knox_IN", 625);
        ZONEIDMAP.zoneid.put("US/Indiana-Starke", 1137);
        ZONEIDMAP.zoneid.put("America/Indiana/Marengo", 112);
        ZONEIDMAP.zoneid.put("America/Indiana/Vevay", 114);
        ZONEIDMAP.zoneid.put("America/Fort_Wayne", 623);
        ZONEIDMAP.zoneid.put("America/Indiana/Indianapolis", 1647);
        ZONEIDMAP.zoneid.put("America/Indiana/Vincennes", 209);
        ZONEIDMAP.zoneid.put("America/Indiana/Petersburg", 210);
        ZONEIDMAP.zoneid.put("US/East-Indiana", 1135);
        ZONEIDMAP.zoneid.put("America/Inuvik", 137);
        ZONEIDMAP.zoneid.put("America/Iqaluit", 133);
        ZONEIDMAP.zoneid.put("Jamaica", 674);
        ZONEIDMAP.zoneid.put("America/Jujuy", 178);
        ZONEIDMAP.zoneid.put("America/Juneau", 104);
        ZONEIDMAP.zoneid.put("PST8PDT", 217);
        ZONEIDMAP.zoneid.put("US/Pacific", 615);
        ZONEIDMAP.zoneid.put("US/Pacific-New", 1639);
        ZONEIDMAP.zoneid.put("America/Louisville", 115);
        ZONEIDMAP.zoneid.put("America/Maceio", 187);
        ZONEIDMAP.zoneid.put("Brazil/West", 704);
        ZONEIDMAP.zoneid.put("Mexico/BajaSur", 656);
        ZONEIDMAP.zoneid.put("America/Mendoza", 180);
        ZONEIDMAP.zoneid.put("America/Menominee", 117);
        ZONEIDMAP.zoneid.put("Mexico/General", 653);
        ZONEIDMAP.zoneid.put("Canada/Eastern", 634);
        ZONEIDMAP.zoneid.put("EST5EDT", 214);
        ZONEIDMAP.zoneid.put("US/Eastern", 612);
        ZONEIDMAP.zoneid.put("America/Nipigon", 124);
        ZONEIDMAP.zoneid.put("America/Nome", 107);
        ZONEIDMAP.zoneid.put("Brazil/DeNoronha", 695);
        ZONEIDMAP.zoneid.put("America/Pangnirtung", 132);
        ZONEIDMAP.zoneid.put("US/Arizona", 621);
        ZONEIDMAP.zoneid.put("Brazil/Acre", 705);
        ZONEIDMAP.zoneid.put("America/Porto_Velho", 190);
        ZONEIDMAP.zoneid.put("America/Rainy_River", 125);
        ZONEIDMAP.zoneid.put("America/Rankin_Inlet", 134);
        ZONEIDMAP.zoneid.put("Canada/East-Saskatchewan", 639);
        ZONEIDMAP.zoneid.put("Canada/Saskatchewan", 1151);
        ZONEIDMAP.zoneid.put("America/Rosario", 176);
        ZONEIDMAP.zoneid.put("Chile/Continental", 706);
        ZONEIDMAP.zoneid.put("Brazil/East", 700);
        ZONEIDMAP.zoneid.put("Canada/Newfoundland", 630);
        ZONEIDMAP.zoneid.put("America/Virgin", 686);
        ZONEIDMAP.zoneid.put("America/Swift_Current", 128);
        ZONEIDMAP.zoneid.put("America/Thunder_Bay", 123);
        ZONEIDMAP.zoneid.put("America/Ensenada", 657);
        ZONEIDMAP.zoneid.put("Mexico/BajaNorte", 1169);
        ZONEIDMAP.zoneid.put("Canada/Pacific", 642);
        ZONEIDMAP.zoneid.put("America/Whitehorse", 138);
        ZONEIDMAP.zoneid.put("Canada/Yukon", 650);
        ZONEIDMAP.zoneid.put("Canada/Central", 638);
        ZONEIDMAP.zoneid.put("America/Yakutat", 105);
        ZONEIDMAP.zoneid.put("America/Yellowknife", 136);
        ZONEIDMAP.zoneid.put("Antarctica/Davis", 231);
        ZONEIDMAP.zoneid.put("Antarctica/South_Pole", 748);
        ZONEIDMAP.zoneid.put("Antarctica/Syowa", 234);
        ZONEIDMAP.zoneid.put("Asia/Almaty", 269);
        ZONEIDMAP.zoneid.put("Asia/Chungking", 251);
        ZONEIDMAP.zoneid.put("Asia/Dili", 259);
        ZONEIDMAP.zoneid.put("Asia/Gaza", 285);
        ZONEIDMAP.zoneid.put("Asia/Harbin", 249);
        ZONEIDMAP.zoneid.put("Hongkong", 766);
        ZONEIDMAP.zoneid.put("Asia/Hovd", 280);
        ZONEIDMAP.zoneid.put("Asia/Istanbul", 1431);
        ZONEIDMAP.zoneid.put("Asia/Tel_Aviv", 778);
        ZONEIDMAP.zoneid.put("Israel", 1290);
        ZONEIDMAP.zoneid.put("Asia/Kashgar", 253);
        ZONEIDMAP.zoneid.put("Asia/Kuching", 279);
        ZONEIDMAP.zoneid.put("Asia/Omsk", 304);
        ZONEIDMAP.zoneid.put("Asia/Riyadh87", 289);
        ZONEIDMAP.zoneid.put("Mideast/Riyadh87", 801);
        ZONEIDMAP.zoneid.put("Asia/Riyadh88", 290);
        ZONEIDMAP.zoneid.put("Mideast/Riyadh88", 802);
        ZONEIDMAP.zoneid.put("Asia/Riyadh89", 291);
        ZONEIDMAP.zoneid.put("Mideast/Riyadh89", 803);
        ZONEIDMAP.zoneid.put("Asia/Samarkand", 299);
        ZONEIDMAP.zoneid.put("ROK", 785);
        ZONEIDMAP.zoneid.put("PRC", 762);
        ZONEIDMAP.zoneid.put("Singapore", 804);
        ZONEIDMAP.zoneid.put("ROC", 767);
        ZONEIDMAP.zoneid.put("Iran", 776);
        ZONEIDMAP.zoneid.put("Japan", 779);
        ZONEIDMAP.zoneid.put("Asia/Ulaanbaatar", 281);
        ZONEIDMAP.zoneid.put("Asia/Urumqi", 252);
        ZONEIDMAP.zoneid.put("Atlantic/Madeira", 337);
        ZONEIDMAP.zoneid.put("Iceland", 846);
        ZONEIDMAP.zoneid.put("Australia/South", 861);
        ZONEIDMAP.zoneid.put("Australia/Queensland", 859);
        ZONEIDMAP.zoneid.put("Australia/Broken_Hill", 353);
        ZONEIDMAP.zoneid.put("Australia/Yancowinna", 865);
        ZONEIDMAP.zoneid.put("Australia/North", 857);
        ZONEIDMAP.zoneid.put("Australia/Hobart", 350);
        ZONEIDMAP.zoneid.put("Australia/Tasmania", 862);
        ZONEIDMAP.zoneid.put("Australia/Lindeman", 348);
        ZONEIDMAP.zoneid.put("Australia/Lord_Howe", 354);
        ZONEIDMAP.zoneid.put("Australia/LHI", 866);
        ZONEIDMAP.zoneid.put("Australia/Melbourne", 351);
        ZONEIDMAP.zoneid.put("Australia/Victoria", 863);
        ZONEIDMAP.zoneid.put("Australia/West", 858);
        ZONEIDMAP.zoneid.put("Australia/Canberra", 1376);
        ZONEIDMAP.zoneid.put("Australia/NSW", 1888);
        ZONEIDMAP.zoneid.put("CET", 366);
        ZONEIDMAP.zoneid.put("Etc/GMT", 1);
        ZONEIDMAP.zoneid.put("Etc/GMT+0", 1025);
        ZONEIDMAP.zoneid.put("Etc/GMT-0", 2049);
        ZONEIDMAP.zoneid.put("Etc/GMT0", 3073);
        ZONEIDMAP.zoneid.put("Etc/Greenwich", 4097);
        ZONEIDMAP.zoneid.put("GMT+0", 1537);
        ZONEIDMAP.zoneid.put("GMT-0", 2561);
        ZONEIDMAP.zoneid.put("GMT0", 3585);
        ZONEIDMAP.zoneid.put("Greenwich", 4609);
        ZONEIDMAP.zoneid.put("Etc/GMT+1", 16);
        ZONEIDMAP.zoneid.put("Etc/GMT+10", 25);
        ZONEIDMAP.zoneid.put("Etc/GMT+11", 26);
        ZONEIDMAP.zoneid.put("Etc/GMT+12", 27);
        ZONEIDMAP.zoneid.put("Etc/GMT+2", 17);
        ZONEIDMAP.zoneid.put("Etc/GMT+3", 18);
        ZONEIDMAP.zoneid.put("Etc/GMT+4", 19);
        ZONEIDMAP.zoneid.put("Etc/GMT+5", 20);
        ZONEIDMAP.zoneid.put("Etc/GMT+6", 21);
        ZONEIDMAP.zoneid.put("Etc/GMT+7", 22);
        ZONEIDMAP.zoneid.put("Etc/GMT+8", 23);
        ZONEIDMAP.zoneid.put("Etc/GMT+9", 24);
        ZONEIDMAP.zoneid.put("Etc/GMT-1", 15);
        ZONEIDMAP.zoneid.put("Etc/GMT-10", 6);
        ZONEIDMAP.zoneid.put("Etc/GMT-11", 5);
        ZONEIDMAP.zoneid.put("Etc/GMT-12", 4);
        ZONEIDMAP.zoneid.put("Etc/GMT-13", 3);
        ZONEIDMAP.zoneid.put("Etc/GMT-14", 2);
        ZONEIDMAP.zoneid.put("Etc/GMT-2", 14);
        ZONEIDMAP.zoneid.put("Etc/GMT-3", 13);
        ZONEIDMAP.zoneid.put("Etc/GMT-4", 12);
        ZONEIDMAP.zoneid.put("Etc/GMT-5", 11);
        ZONEIDMAP.zoneid.put("Etc/GMT-6", 10);
        ZONEIDMAP.zoneid.put("Etc/GMT-7", 9);
        ZONEIDMAP.zoneid.put("Etc/GMT-8", 8);
        ZONEIDMAP.zoneid.put("Etc/GMT-9", 7);
        ZONEIDMAP.zoneid.put("Europe/Belfast", 370);
        ZONEIDMAP.zoneid.put("Europe/Ljubljana", 924);
        ZONEIDMAP.zoneid.put("Europe/Sarajevo", 1436);
        ZONEIDMAP.zoneid.put("Europe/Skopje", 1948);
        ZONEIDMAP.zoneid.put("Europe/Zagreb", 2460);
        ZONEIDMAP.zoneid.put("Eire", 883);
        ZONEIDMAP.zoneid.put("Turkey", 919);
        ZONEIDMAP.zoneid.put("Portugal", 911);
        ZONEIDMAP.zoneid.put("GB", 881);
        ZONEIDMAP.zoneid.put("GB-Eire", 1393);
        ZONEIDMAP.zoneid.put("W-SU", 914);
        ZONEIDMAP.zoneid.put("Europe/Bratislava", 890);
        ZONEIDMAP.zoneid.put("Europe/San_Marino", 1411);
        ZONEIDMAP.zoneid.put("Europe/Vatican", 899);
        ZONEIDMAP.zoneid.put("Europe/Tiraspol", 394);
        ZONEIDMAP.zoneid.put("Europe/Uzhgorod", 409);
        ZONEIDMAP.zoneid.put("Poland", 910);
        ZONEIDMAP.zoneid.put("Europe/Zaporozhye", 410);
        ZONEIDMAP.zoneid.put("NZ", 983);
        ZONEIDMAP.zoneid.put("NZ-CHAT", 984);
        ZONEIDMAP.zoneid.put("Chile/EasterIsland", 963);
        ZONEIDMAP.zoneid.put("US/Hawaii", 962);
        ZONEIDMAP.zoneid.put("Pacific/Johnston", 485);
        ZONEIDMAP.zoneid.put("Pacific/Kwajalein", 464);
        ZONEIDMAP.zoneid.put("Kwajalein", 976);
        ZONEIDMAP.zoneid.put("Pacific/Midway", 486);
        ZONEIDMAP.zoneid.put("Pacific/Samoa", 1502);
        ZONEIDMAP.zoneid.put("US/Samoa", 990);
        ZONEIDMAP.zoneid.put("Pacific/Yap", 465);
        ZONEIDMAP.zoneid.put("WET", 365);
        ZONEIDMAP.zoneid.putOld("EST", new Integer(1636));
        ZONEIDMAP.zoneid.putOld("UTC", new Integer(540));
        ZONEIDMAP.zoneid.putOld("MST", new Integer(2662));
        ZONEIDMAP.zoneid.putOld("HST", new Integer(1474));
        ZONEIDMAP.zoneid.putOld("CST6CDT", new Integer(1125));
        ZONEIDMAP.zoneid.putOld("MST7MDT", new Integer(2150));
        ZONEIDMAP.zoneid.putOld("PST8PDT", new Integer(1127));
        ZONEIDMAP.zoneid.putOld("EST5EDT", new Integer(1124));
    }
}