package io.github.unapplicable.hangman;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RequestParameter;
import io.vertx.ext.web.RequestParameters;
import io.vertx.rx.java.RxHelper;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.designdriven.OpenAPI3RouterFactory;
import rx.Single;

public class HangmanVerticle extends io.vertx.rxjava.core.AbstractVerticle {
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        createRouterFactory().subscribe(
            rf -> {
                Router router = registerHandlers(rf).getRouter();
                vertx
                    .createHttpServer()
                    .requestHandler(router::accept)
                    .rxListen(8080)
                    .toCompletable()
                    .subscribe(RxHelper.toSubscriber(startFuture));
            }, startFuture::fail);
    }

    private Single<OpenAPI3RouterFactory> createRouterFactory() {
        try {
            String filename = HangmanVerticle.class.getResource("hangmanapi.yaml").toURI().toURL().toString();

            return OpenAPI3RouterFactory.rxCreateRouterFactoryFromURL(vertx, filename);
        } catch (Exception ex) {
            return Single.error(ex);
        }
    }

    private OpenAPI3RouterFactory registerHandlers(OpenAPI3RouterFactory rf) {
        rf.addHandlerByOperationId("fetchPlayerInfo", this::fetchPlayerInfo, this::handleFailure);
        rf.addHandlerByOperationId("createPlayer", this::createPlayer, this::handleFailure);
        return rf;
    }

    private void fetchPlayerInfo(RoutingContext ctx) {
        ctx.response().end("yep");
    }

    private void createPlayer(RoutingContext ctx) {
        RequestParameters params = ctx.get("parsedParameters");
        RequestParameter body = params.body();
        if (body != null) {
            JsonObject jsonBody = body.getJsonObject();
        }

        ctx.response().end("yep");
    }

    private void handleFailure(RoutingContext ctx) {
        Throwable failure = ctx.failure();
        ctx.response().setStatusCode(400).end(failure.getMessage());
    }
}
