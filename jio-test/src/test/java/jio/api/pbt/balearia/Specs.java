package jio.api.pbt.balearia;

import jsonvalues.spec.JsObjSpec;
import jsonvalues.spec.JsSpec;
import jsonvalues.spec.JsSpecs;

import static jio.api.pbt.balearia.Fields.*;

public class Specs {

    public static final JsSpec VARCHAR = JsSpecs.str(s -> s.length() <= 255);
    public static final JsSpec NULLABLE_VARCHAR = VARCHAR.nullable();

    public static final JsSpec INTEGER = JsSpecs.integer();
    public static final JsSpec NULLABLE_INTEGER = INTEGER.nullable();
    public static final JsObjSpec CREW_SPEC = JsObjSpec.of(
            PHONE, VARCHAR,
            EMAIL, VARCHAR,
            EXTENSION, VARCHAR
    );
    public static final JsObjSpec SHIP_SPEC = JsObjSpec.of(
                    IMO, INTEGER,
                    STATUS, NULLABLE_INTEGER,
                    CODE, NULLABLE_VARCHAR,
                    SHIP_OWNER, NULLABLE_VARCHAR,
                    INMARSAT, NULLABLE_VARCHAR,
                    ISM_COMPANY, NULLABLE_VARCHAR,
                    GSM, NULLABLE_VARCHAR,
                    NIB, NULLABLE_VARCHAR,
                    P_AND_I, NULLABLE_VARCHAR,
                    TPM, NULLABLE_VARCHAR,
                    BOAT_DRAFT, NULLABLE_VARCHAR,
                    WIDTH, NULLABLE_VARCHAR,
                    GT, NULLABLE_VARCHAR,
                    CLASS, NULLABLE_VARCHAR,
                    CHARTERER, NULLABLE_VARCHAR,
                    PRL_COMPANY, NULLABLE_VARCHAR,
                    REGISTRATION_PORT, NULLABLE_VARCHAR,
                    CALL_SIGN, NULLABLE_VARCHAR,
                    CREW_MANNING, NULLABLE_VARCHAR,
                    SHOR_NUMBER, NULLABLE_VARCHAR,
                    LENGTH, NULLABLE_VARCHAR,
                    SATELLITE, NULLABLE_VARCHAR,
                    KNT, NULLABLE_VARCHAR,
                    ENGINES, NULLABLE_VARCHAR,
                    YEAR, NULLABLE_VARCHAR,
                    WORKING_LANGUAGE, NULLABLE_VARCHAR,
                    MMSI, NULLABLE_VARCHAR,
                    COMPANY, NULLABLE_VARCHAR,
                    COUNTRY_FLAG, NULLABLE_VARCHAR,
                    NAME, VARCHAR,
                    TYPE, VARCHAR,
                    CATEGORY, INTEGER,
                    MAXIMUM_CAPACITY_PEOPLE, NULLABLE_INTEGER,
                    MAXIMUM_CAPACITY_LENGTH_VEHICLES, INTEGER,
                    MINIMUM_CAPACITY_CREW, INTEGER,
                    MAXIMUM_CAPACITY_BABIES, NULLABLE_INTEGER,
                    MAXIMUM_CAPACITY_LOAD, INTEGER,
                    CAPTAIN, CREW_SPEC,
                    CABIN_BOSS, CREW_SPEC
            )
            .withReqKeys(REQ_SHIPS_FIELDS);


}
