package jio.test.pbt;

import jsonvalues.JsArray;
import jsonvalues.JsObj;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class GroupReport {

    List<Report> reports;

    String groupName;

    GroupReport(List<Report> reports, String groupName) {
        this.reports = reports;
        this.groupName = groupName;
    }


    public void assertAllSuccess() {

        reports.forEach(Report::assertAllSuccess);
    }

    public void assertNoFailures() {

        reports.forEach(Report::assertNoFailures);

    }


    public void assertThat(Predicate<Report> condition,
                           Supplier<String> message
                          ) {

        reports.forEach(r -> r.assertThat(condition, message));

    }

    public JsObj toJson() {
        return reports.stream().reduce(
                JsObj.empty().set(groupName, JsObj.empty()),
                (json, report) -> json.set(groupName, json.getObj(groupName).set(report.getPropName(), report.toJson())),
                (a, b) -> JsObj.of(groupName, a.getObj(groupName).union(b.getObj(groupName), JsArray.TYPE.LIST)
                                  )
                                      );
    }

    public String toString() {
        return toJson().toString();
    }



}
