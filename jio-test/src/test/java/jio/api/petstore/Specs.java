package jio.api.petstore;

import jsonvalues.spec.JsObjSpec;
import jsonvalues.spec.JsSpecs;

public class Specs {

  public static final JsObjSpec apiResponseSpec = JsObjSpec.of("code",
                                                               JsSpecs.integer(),
                                                               "type",
                                                               JsSpecs.str(),
                                                               "message",
                                                               JsSpecs.str()
  );

  public static final JsObjSpec categorySpec = JsObjSpec.of("id",
                                                            JsSpecs.longInteger(),
                                                            "name",
                                                            JsSpecs.str()
  );

  public static final JsObjSpec userSpec = JsObjSpec.of("id",
                                                        JsSpecs.longInteger(),
                                                        "username",
                                                        JsSpecs.str(),
                                                        "firstName",
                                                        JsSpecs.str(),
                                                        "lastName",
                                                        JsSpecs.str(),
                                                        "email",
                                                        JsSpecs.str(),
                                                        "password",
                                                        JsSpecs.str(),
                                                        "phone",
                                                        JsSpecs.str(),
                                                        "userStatus",
                                                        JsSpecs.integer()
  );

  public static final JsObjSpec orderSpec = JsObjSpec.of("id",
                                                         JsSpecs.longInteger(),
                                                         "petId",
                                                         JsSpecs.longInteger(),
                                                         "quantity",
                                                         JsSpecs.integer(),
                                                         "shipDate",
                                                         JsSpecs.str(),
                                                         "status",
                                                         JsSpecs.oneStringOf("placed",
                                                                             "approved",
                                                                             "delivered"
                                                         ),
                                                         "complete",
                                                         JsSpecs.bool()
  );

  public static final JsObjSpec tagSpec = JsObjSpec.of("id",
                                                       JsSpecs.longInteger(),
                                                       "name",
                                                       JsSpecs.str()
  );

  public static final JsObjSpec petSpec = JsObjSpec.of("id",
                                                       JsSpecs.longInteger(),
                                                       "category",
                                                       categorySpec,
                                                       // Reference to the "Category" definition
                                                       "name",
                                                       JsSpecs.str(),
                                                       "photoUrls",
                                                       JsSpecs.arrayOfStr(),
                                                       "tags",
                                                       JsSpecs.arrayOfSpec(JsObjSpec.of("id",
                                                                                        JsSpecs.longInteger(),
                                                                                        "name",
                                                                                        JsSpecs.str()
                                                       )
                                                       ),
                                                       // Reference to the "Tag" definition
                                                       "status",
                                                       JsSpecs.oneStringOf("available",
                                                                           "pending",
                                                                           "sold"))
                                                   .

                                                   withReqKeys(Fields.REQ_PET_FIELDS);

}
