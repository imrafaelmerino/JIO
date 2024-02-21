package jio.api.exp;

import jio.*;
import jio.time.Clock;
import jsonvalues.*;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

public class SignupService implements Lambda<JsObj, JsObj> {

  final Lambda<JsObj, Void> persistLDAP;
  final Lambda<String, JsArray> normalizeAddresses;
  final Lambda<Void, Integer> countUsers;
  final Lambda<JsObj, String> persistMongo;
  final Lambda<JsObj, Void> sendEmail;
  final Lambda<String, Boolean> existsInLDAP;

  final Clock clock;

  public SignupService(Lambda<JsObj, Void> persistLDAP,
                       Lambda<String, JsArray> normalizeAddresses,
                       Lambda<Void, Integer> countUsers,
                       Lambda<JsObj, String> persistMongo,
                       Lambda<JsObj, Void> sendEmail,
                       Lambda<String, Boolean> existsInLDAP,
                       Clock clock
  ) {
    this.persistLDAP = requireNonNull(persistLDAP);
    this.normalizeAddresses = requireNonNull(normalizeAddresses);
    this.countUsers = requireNonNull(countUsers);
    this.persistMongo = requireNonNull(persistMongo);
    this.sendEmail = requireNonNull(sendEmail);
    this.existsInLDAP = requireNonNull(existsInLDAP);
    this.clock = requireNonNull(clock);
  }

  @Override
  public IO<JsObj> apply(JsObj user) {
    String email = user.getStr("email");
    String address = user.getStr("address");

    String context = "signup";
    Lambda<String, String> LDAPFlow = id -> IfElseExp.<String>predicate(existsInLDAP.apply(email))
                                                     .consequence(() -> IO.succeed(id))
                                                     .alternative(() -> PairExp.seq(persistLDAP.apply(user),
                                                                                    sendEmail.apply(user)
                                                     )
                                                                               .debugEach(context)
                                                                               .map(n -> id)
                                                     )
                                                     .debugEach(context);

    return JsObjExp.par("number_users",
                        countUsers.apply(null)
                                  .debug(EventBuilder.of("count_number_users",
                                                         context))
                                  .retry(RetryPolicies.limitRetries(3))
                                  .recover(e -> -1)
                                  .map(JsInt::of),
                        "id",
                        persistMongo.then(LDAPFlow)
                                    .apply(user)
                                    .map(JsStr::of),
                        "addresses",
                        normalizeAddresses.apply(address),
                        "timestamp",
                        IO.lazy(clock)
                          .map(ms -> JsInstant.of(Instant.ofEpochMilli(ms)))
    )
                   .debugEach(context);
  }
}
