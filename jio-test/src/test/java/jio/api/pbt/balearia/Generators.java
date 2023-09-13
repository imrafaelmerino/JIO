package jio.api.pbt.balearia;

import fun.gen.Combinators;
import fun.gen.Gen;
import jsonvalues.*;
import jsonvalues.gen.*;

import java.util.List;
import java.util.stream.Stream;

import static jio.api.pbt.balearia.Fields.*;

public class Generators {


    public final static Gen<JsStr> VARCHAR_GEN = JsStrGen.biased(0, 255);
    public final static Gen<JsInt> INT_GEN = JsIntGen.biased();
    public final static Gen<JsStr> EMAIL_GEN = JsStrGen.biased(0, 60)
            .map(s -> s.map(v -> v + "@balearia.com"));
    public final static JsObjGen CREW_VALID_GEN = JsObjGen.of(
            PHONE, VARCHAR_GEN,
            EMAIL, EMAIL_GEN,
            EXTENSION, VARCHAR_GEN
    );
    public final static List<JsStr> SHIPS_TYPE = Stream
            .of("BUQUE", "EAV", "FERRY", "SFF")
            .map(JsStr::of).toList();
    public final static List<JsInt> SHIPS_CATEGORY = Stream
            .of(1, 3, 5)
            .map(JsInt::of)
            .toList();
    public final static Gen<JsInt> SHIPS_CATEGORY_GEN = Combinators.oneOf(SHIPS_CATEGORY);
    public final static Gen<JsStr> SHIPS_TYPE_GEN = Combinators.oneOf(SHIPS_TYPE);
    public final static JsObjGen VALID_SHIPS_GENERATOR = JsObjGen.of(
                    IMO, INT_GEN,
                    NAME, VARCHAR_GEN,
                    CLASS, VARCHAR_GEN,
                    STATUS, INT_GEN,
                    CODE, VARCHAR_GEN,
                    SHIP_OWNER, VARCHAR_GEN,
                    GSM, VARCHAR_GEN,
                    TPM, VARCHAR_GEN,
                    P_AND_I, VARCHAR_GEN,
                    COUNTRY_FLAG, VARCHAR_GEN,
                    COMPANY, VARCHAR_GEN,
                    TYPE, SHIPS_TYPE_GEN,
                    CATEGORY, SHIPS_CATEGORY_GEN,
                    MAXIMUM_CAPACITY_PEOPLE, INT_GEN,
                    MAXIMUM_CAPACITY_LENGTH_VEHICLES, INT_GEN,
                    MINIMUM_CAPACITY_CREW, INT_GEN,
                    MAXIMUM_CAPACITY_BABIES, INT_GEN,
                    MAXIMUM_CAPACITY_LOAD, INT_GEN,
                    CAPTAIN, CREW_VALID_GEN,
                    CABIN_BOSS, CREW_VALID_GEN,
                    INMARSAT, VARCHAR_GEN,
                    SATELLITE, VARCHAR_GEN,
                    ENGINES, VARCHAR_GEN,
                    ISM_COMPANY, VARCHAR_GEN,
                    WORKING_LANGUAGE, VARCHAR_GEN,
                    LENGTH, VARCHAR_GEN,
                    CALL_SIGN, VARCHAR_GEN,
                    CREW_MANNING, VARCHAR_GEN,
                    YEAR, VARCHAR_GEN,
                    MMSI, VARCHAR_GEN,
                    WIDTH, VARCHAR_GEN,
                    PRL_COMPANY, VARCHAR_GEN,
                    SHOR_NUMBER, VARCHAR_GEN,
                    BOAT_DRAFT, VARCHAR_GEN,
                    KNT, VARCHAR_GEN,
                    REGISTRATION_PORT, VARCHAR_GEN,
                    CHARTERER, VARCHAR_GEN,
                    NIB, VARCHAR_GEN,
                    GT, VARCHAR_GEN
            )
            .withNonNullValues(REQ_SHIPS_FIELDS)
            .withReqKeys(REQ_SHIPS_FIELDS);


}
