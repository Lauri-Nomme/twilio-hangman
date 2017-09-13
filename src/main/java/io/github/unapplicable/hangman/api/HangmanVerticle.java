package io.github.unapplicable.hangman.api;

import io.github.unapplicable.hangman.service.*;
import io.github.unapplicable.hangman.service.error.BaseError;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RequestParameter;
import io.vertx.ext.web.RequestParameters;
import io.vertx.rx.java.RxHelper;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.designdriven.OpenAPI3RouterFactory;
import javafx.util.Pair;
import rx.Observable;
import rx.Single;
import rx.observables.SyncOnSubscribe;

import java.io.IOException;

public class HangmanVerticle extends io.vertx.rxjava.core.AbstractVerticle {
    private PlayerService playerService;
    private GameService gameService;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        PlayerRepository playerRepository = new MemoryPlayerRepository();
        playerRepository.create(new Player("player1", 1));
        playerService = new PlayerServiceImpl(playerRepository);
        WordList wordList = createWordList();
        GameRepository gameRepository = new MemoryGameRepository();
        gameService = new GameServiceImpl(playerRepository, gameRepository, wordList);

        createRouterFactory().subscribe(
            rf -> {
                Router hangmanApiRouter = registerHandlers(rf).getRouter();
                Router router = Router
                    .router(vertx)
                    .mountSubRouter("/hangman/v1", hangmanApiRouter);
                router.route("/hangman/v1/*").failureHandler(this::handleFailure);
                vertx
                    .createHttpServer()
                    .requestHandler(router::accept)
                    .rxListen(8080)
                    .toCompletable()
                    .subscribe(RxHelper.toSubscriber(startFuture));
            }, startFuture::fail);
    }

    private WordList createWordList() throws IOException {
        return new WordList(HangmanVerticle.class.getResourceAsStream("/wordlist.txt"));
    }

    private Single<OpenAPI3RouterFactory> createRouterFactory() {
        try {
            String filename = HangmanVerticle.class.getResource("/hangmanapi.yaml").toURI().toURL().toString();

            return OpenAPI3RouterFactory.rxCreateRouterFactoryFromURL(vertx, filename);
        } catch (Exception ex) {
            return Single.error(ex);
        }
    }

    private OpenAPI3RouterFactory registerHandlers(OpenAPI3RouterFactory rf) {
        rf.addHandlerByOperationId("fetchPlayerInfo", this::fetchPlayerInfo, this::handleFailure);
        rf.addHandlerByOperationId("createPlayer", this::createPlayer, this::handleFailure);
        rf.addHandlerByOperationId("listPlayers", this::listPlayers, this::handleFailure);

        rf.addHandlerByOperationId("startGame", this::startGame, this::handleFailure);
        rf.addHandlerByOperationId("guess", this::guess, this::handleFailure);
        return rf;
    }

    private void startGame(RoutingContext ctx) {
        RequestParameters params = ctx.get("parsedParameters");
        RequestParameter body = params.body();
        JsonObject jsonBody = body.getJsonObject();
        String playerName = jsonBody.getString("name");

        Single<Pair<String, Game>> gameS = gameService.start(playerName);
        HttpServerResponse response = ctx.response();
        gameS.subscribe(game -> {
            String gamePath = ctx.request().absoluteURI() + "/" + game.getKey();
            response.putHeader("Location", gamePath);
            respondJsonObject(response, game.getValue());
        }, ctx::fail);
    }

    private void guess(RoutingContext ctx) {
        RequestParameters params = ctx.get("parsedParameters");
        RequestParameter body = params.body();
        JsonObject jsonBody = body.getJsonObject();
        String gameId = params.pathParameter("gameId").getString();
        String letter = jsonBody.getString("letter");

        Single<Game> gameS = gameService.guess(gameId, letter);
        HttpServerResponse response = ctx.response();
        gameS.subscribe(game -> respondJsonObject(response, game), ctx::fail);
    }

    private void listPlayers(RoutingContext ctx) {
        Observable<Player> playersO = playerService.list();

        streamJsonObjectArray(playersO, ctx);
    }

    private void fetchPlayerInfo(RoutingContext ctx) {
        RequestParameters params = ctx.get("parsedParameters");
        String playerId = params.pathParameter("playerId").getString();

        Single<Player> playerS = playerService.fetch(playerId);
        HttpServerResponse response = ctx.response();
        playerS.subscribe(player -> respondJsonObject(response, player), ctx::fail);
    }

    private void createPlayer(RoutingContext ctx) {
        RequestParameters params = ctx.get("parsedParameters");
        RequestParameter body = params.body();
        JsonObject jsonBody = body.getJsonObject();
        Player requestPlayer = new Player(jsonBody.getString("name"), jsonBody.getInteger("age"));

        Single<Player> playerS = playerService.create(requestPlayer);
        HttpServerResponse response = ctx.response();
        playerS.subscribe(player -> respondJsonObject(response, player), ctx::fail);
    }

    private void handleFailure(RoutingContext ctx) {
        Throwable failure = ctx.failure();
        HttpServerResponse response = ctx.response();
        Error error = new Error(failure instanceof BaseError ? ((BaseError) failure).getCode() : 500, failure.getMessage());
        respondError(response, error);
    }

    private void respondError(HttpServerResponse response, Error error) {
        response.setStatusCode(error.getCode());
        respondJsonObject(response, error);
    }

    private void respondJsonObject(HttpServerResponse response, Object object) {
        response.putHeader("Content-Type", "application/json");
        response.end(JsonObject.mapFrom(object).encodePrettily());
    }

    private <O> void streamJsonObjectArray(Observable<O> objectsO, RoutingContext ctx) {
        HttpServerResponse response = ctx.response();
        response.setChunked(true);
        response.putHeader("Content-Type", "application/json");
        Observable<String> joinerO = Observable.create(SyncOnSubscribe.createStateful(() -> "[", (s, o) -> {
            o.onNext(s);
            return ",";
        }));
        joinerO
            .zipWith(objectsO, (joiner, object) -> joiner.concat(JsonObject.mapFrom(object).encodePrettily()))
            .subscribe(
                response::write,
                ctx::fail,
                () -> response.end("]"));
    }
}
