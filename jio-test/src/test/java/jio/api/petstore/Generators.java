package jio.api.petstore;

import fun.gen.Combinators;
import jsonvalues.JsStr;
import jsonvalues.gen.JsArrayGen;
import jsonvalues.gen.JsBoolGen;
import jsonvalues.gen.JsInstantGen;
import jsonvalues.gen.JsIntGen;
import jsonvalues.gen.JsLongGen;
import jsonvalues.gen.JsObjGen;
import jsonvalues.gen.JsStrGen;

public class Generators {

  public static final JsObjGen categoryGen = JsObjGen.of(Fields.NAME,
                                                         JsStrGen.alphabetic());

  public static final JsObjGen tagGen = JsObjGen.of(Fields.NAME,
                                                    JsStrGen.alphabetic());

  public static final JsObjGen orderGen = JsObjGen.of(
      Fields.PET_ID,
      JsLongGen.arbitrary()
               .suchThat(petId -> petId.value >= 0),
      Fields.QUANTITY,
      JsIntGen.arbitrary()
              .suchThat(quantity -> quantity.value >= 0),
      Fields.SHIP_DATE,
      JsInstantGen.arbitrary(),
      Fields.STATUS,
      Combinators.oneOf(JsStr.of("placed"),
                        JsStr.of("approved"),
                        JsStr.of("delivered")
                       ),
      Fields.COMPLETE,
      JsBoolGen.arbitrary()
                                                     );

  public static final JsObjGen userGen = JsObjGen.of(Fields.USERNAME,
                                                     JsStrGen.alphabetic(),
                                                     Fields.FIRST_NAME,
                                                     JsStrGen.alphabetic(),
                                                     Fields.LAST_NAME,
                                                     JsStrGen.alphabetic(),
                                                     Fields.EMAIL,
                                                     JsStrGen.alphabetic(),
                                                     Fields.PASSWORD,
                                                     JsStrGen.alphabetic(),
                                                     Fields.PHONE,
                                                     JsStrGen.alphabetic(),
                                                     Fields.USER_STATUS,
                                                     JsIntGen.arbitrary()
                                                    )
                                                 .withReqKeys(Fields.USERNAME)
                                                 .withNonNullValues(Fields.USERNAME);

  public static final JsObjGen petGen = JsObjGen.of(Fields.ID,
                                                    JsLongGen.arbitrary()
                                                             .suchThat(id -> id.value >= 0),
                                                    Fields.CATEGORY,
                                                    Generators.categoryGen,
                                                    Fields.NAME,
                                                    JsStrGen.alphabetic(),
                                                    Fields.PHOTO_URLS,
                                                    JsArrayGen.arbitrary(JsStrGen.alphanumeric(),
                                                                         1,
                                                                         10),
                                                    Fields.TAGS,
                                                    JsArrayGen.arbitrary(Generators.tagGen,
                                                                         0,
                                                                         100),
                                                    Fields.STATUS,
                                                    Combinators.oneOf(JsStr.of("available"),
                                                                      JsStr.of("pending"),
                                                                      JsStr.of("sold")
                                                                     )
                                                   )
                                                .withReqKeys(Fields.REQ_PET_FIELDS)
                                                .withNonNullValues(Fields.REQ_PET_FIELDS);
}
