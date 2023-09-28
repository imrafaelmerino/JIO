package jio.test.junit;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class Debugger implements BeforeEachCallback, AfterEachCallback, AfterAllCallback {

    StubDebugger stubDebugger;
    MongoDebugger mongoEventDebugger;
    ExpDebugger expEventDebugger;
    HttpClientDebugger httpClientDebugger;
    HttpServerDebugger httpServerDebugger;

    @Override
    public void afterEach(ExtensionContext context) {
        if (stubDebugger != null) stubDebugger.awaitTermination();
        if (mongoEventDebugger != null) mongoEventDebugger.awaitTermination();
        if (expEventDebugger != null) expEventDebugger.awaitTermination();
        if (httpClientDebugger != null) httpClientDebugger.awaitTermination();
        if (httpServerDebugger != null) httpServerDebugger.awaitTermination();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        extracted(context);
    }

    private void extracted(ExtensionContext context) {
        DebugExp debugExp = context.getRequiredTestMethod()
                                   .getAnnotation(DebugExp.class);
        if (debugExp == null)
            debugExp = context.getRequiredTestClass()
                              .getAnnotation(DebugExp.class);
        if (debugExp != null) {
            expEventDebugger = new ExpDebugger(debugExp.conf());
            expEventDebugger.startAsync(debugExp.duration());
            System.out.println("Registered expressions debugger for " + debugExp.duration() + " ms");
        }

        DebugStub debugSub = context.getRequiredTestMethod()
                                    .getAnnotation(DebugStub.class);
        if (debugSub == null)
            debugSub = context.getRequiredTestClass()
                              .getAnnotation(DebugStub.class);
        if (debugSub != null) {
            stubDebugger = new StubDebugger(debugSub.conf());
            stubDebugger.startAsync(debugSub.duration());
            System.out.println("Registered stub debugger for " + debugSub.duration() + " ms");
        }

        DebugHttpClient debugHttpClient = context.getRequiredTestMethod()
                                                 .getAnnotation(DebugHttpClient.class);
        if (debugHttpClient == null)
            debugHttpClient = context.getRequiredTestClass()
                                     .getAnnotation(DebugHttpClient.class);
        if (debugHttpClient != null) {
            httpClientDebugger = new HttpClientDebugger(debugHttpClient.conf());
            httpClientDebugger.startAsync(debugHttpClient.duration());
            System.out.println("Registered Http client debugger for " + debugHttpClient.duration() + " ms");
        }


        DebugHttpServer debugHttpServer = context.getRequiredTestMethod()
                                                 .getAnnotation(DebugHttpServer.class);
        if (debugHttpServer == null)
            debugHttpServer = context.getRequiredTestClass()
                                     .getAnnotation(DebugHttpServer.class);
        if (debugHttpServer != null) {
            httpServerDebugger = new HttpServerDebugger(debugHttpServer.conf());
            httpServerDebugger.startAsync(debugHttpServer.duration());
            System.out.println("Registered Http server debugger for " + debugHttpServer.duration() + " ms");
        }

        DebugMongoClient debugMongoClient = context.getRequiredTestMethod()
                                                   .getAnnotation(DebugMongoClient.class);
        if (debugMongoClient == null)
            debugMongoClient = context.getRequiredTestClass()
                                      .getAnnotation(DebugMongoClient.class);
        if (debugMongoClient != null) {
            mongoEventDebugger = new MongoDebugger(debugMongoClient.conf());
            mongoEventDebugger.startAsync(debugMongoClient.duration());
            System.out.println("Registered MongoDB client debugger for " + debugMongoClient.duration() + " ms");
        }
    }


    void start() {

    }

    void close() {
        if (stubDebugger != null) stubDebugger.close();
        if (mongoEventDebugger != null) mongoEventDebugger.close();
        if (expEventDebugger != null) expEventDebugger.close();
        if (httpClientDebugger != null) httpClientDebugger.close();
        if (httpServerDebugger != null) httpServerDebugger.close();
    }


    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        close();
    }
}
