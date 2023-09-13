package jio.api.pbt.balearia;

import java.util.List;

public class Fields {
    public final static String IMO = "imo";
    public final static String NAME = "name";
    public final static String CLASS = "class";
    public final static String STATUS = "status";
    public final static String CODE = "code";
    public final static String SHIP_OWNER = "shipOwner";
    public final static String GSM = "gsm";
    public final static String TPM = "tpm";
    public final static String P_AND_I = "pAndI";
    public final static String COUNTRY_FLAG = "countryFlag";
    public final static String COMPANY = "company";
    public final static String TYPE = "type";
    public final static String CATEGORY = "category";
    public final static String MAXIMUM_CAPACITY_PEOPLE = "maximumCapacityPeople";
    public final static String MAXIMUM_CAPACITY_LENGTH_VEHICLES = "maximumCapacityLengthVehicles";
    public final static String MINIMUM_CAPACITY_CREW = "minimumCapacityCrew";
    public final static String MAXIMUM_CAPACITY_BABIES = "maximumCapacityBabies";
    public final static String MAXIMUM_CAPACITY_LOAD = "maximumCapacityLoad";
    public final static String CAPTAIN = "captain";
    public final static String CABIN_BOSS = "cabinBoss";
    public final static String INMARSAT = "inmarsat";
    public final static String SATELLITE = "satellite";
    public final static String ENGINES = "engines";
    public final static String ISM_COMPANY = "ismCompany";
    public final static String WORKING_LANGUAGE = "workingLanguage";
    public final static String LENGTH = "length";
    public final static String CALL_SIGN = "callSign";
    public final static String CREW_MANNING = "crewManning";
    public final static String YEAR = "year";
    public final static String MMSI = "mmsi";
    public final static String WIDTH = "width";
    public final static String PRL_COMPANY = "prlCompany";
    public final static String SHOR_NUMBER = "shorNumber";
    public final static String BOAT_DRAFT = "boatDraft";
    public final static String KNT = "knt";
    public final static String REGISTRATION_PORT = "registrationPort";
    public final static String CHARTERER = "charterer";
    public final static String NIB = "nib";
    public final static String GT = "gt";
    public final static String PHONE = "phone";
    public final static String EMAIL = "email";
    public final static String EXTENSION = "extension";

    public static final List<String> REQ_SHIPS_FIELDS = List.of(
            NAME,
            TYPE,
            CATEGORY,
            MAXIMUM_CAPACITY_LENGTH_VEHICLES,
            MINIMUM_CAPACITY_CREW,
            MAXIMUM_CAPACITY_PEOPLE,
            CAPTAIN,
            CABIN_BOSS
    );



    public static final List<String> OPTIONAL_SHIP_FIELDS = List.of(
            IMO,
            CODE,
            SHIP_OWNER,
            INMARSAT,
            ISM_COMPANY,
            GSM,
            NIB,
            P_AND_I,
            TPM,
            BOAT_DRAFT,
            WIDTH,
            GT,
            CLASS,
            CHARTERER,
            PRL_COMPANY,
            REGISTRATION_PORT,
            CALL_SIGN,
            CREW_MANNING,
            SHOR_NUMBER,
            LENGTH,
            SATELLITE,
            KNT,
            ENGINES,
            YEAR,
            WORKING_LANGUAGE,
            MMSI
    );

    public final static List<String> REQ_CREW_FIELDS = List.of(
            PHONE, EMAIL, EXTENSION
    );

}
