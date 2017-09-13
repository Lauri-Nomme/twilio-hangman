package io.github.unapplicable.hangman.api;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.RxHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import rx.functions.Func1;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

@RunWith(VertxUnitRunner.class)
public class HangmanVerticleTest {
    private static final String PLAYER1_NAME = "player1";
    private static final int PLAYER1_AGE = 1;
    private Vertx vertx;
    private int port;
    private String host;
    private String rootPath;
    private String word;

    @Before
    public void setUp(TestContext ctx) {
        host = "localhost";
        port = getAvailablePort();
        rootPath = "/hangman/v1";

        word = "hangman";

        vertx = Vertx.vertx();
        DeploymentOptions options = new DeploymentOptions()
            .setConfig(new JsonObject()
                .put("http.port", port)
                .put("wordlist", new JsonArray(new ArrayList<String>() {{
                    add(word);
                }}))
            );
        vertx.deployVerticle(HangmanVerticle.class.getName(), options, ctx.asyncAssertSuccess());
    }

    private Integer getAvailablePort() {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            port = 8080;
        }

        return port;
    }

    @After
    public void tearDown(TestContext ctx) {
        vertx.close(ctx.asyncAssertSuccess());
    }

    @Test
    public void testFetchPlayerInfo_NonExistentPlayer_ReturnsNotFound(TestContext ctx) {
        final Async async = ctx.async();
        String playerName = "1234";
        fetchPlayerInfo(playerName)
            .subscribe(response -> {
                ctx.assertEquals(response.statusCode(), 404);
                async.complete();
            }, ctx::fail);
    }

    @Test
    public void testCreatePlayer_UnusedName_Succeeds(TestContext ctx) throws Exception {
        final Async async = ctx.async();
        String testName = "player2";
        int testAge = 1;

        createPlayer(testName, testAge)
            .subscribe(response -> assertPlayer1AndComplete(ctx, async, response, response.statusCode(), testName, testAge), ctx::fail);
    }

    @Test
    public void testCreatePlayer_UsedName_Fails(TestContext ctx) throws Exception {
        final Async async = ctx.async();
        createPlayer1()
            .flatMap(response -> createPlayer1())
            .subscribe(response -> {
                ctx.assertEquals(response.statusCode(), 400);
                async.complete();
            }, ctx::fail);
    }

    @Test
    public void testFetchPlayerInfo_ExistingPlayer_Succeeds(TestContext ctx) {
        final Async async = ctx.async();
        String playerName = "player1";
        createPlayer1()
            .flatMap(response1 -> fetchPlayerInfo(playerName))
            .subscribe(response -> assertPlayer1AndComplete(ctx, async, response, response.statusCode(), PLAYER1_NAME, PLAYER1_AGE), ctx::fail);
    }

    @Test
    public void testListPlayers_Succeeds(TestContext ctx) {
        final Async async = ctx.async();
        createPlayer1()
            .flatMap(response1 -> listPlayers())
            .subscribe(response -> {
                ctx.assertEquals(response.statusCode(), 200);
                try {
                    JsonArray body = response.bodyAsJsonArray();
                    ctx.assertTrue(body.size() > 0, "response has player objects");
                    async.complete();
                } catch (Exception ex) {
                    ctx.fail(ex);
                }
            }, ctx::fail);
    }

    @Test
    public void testStartGame_NonExistentPlayer_Fails(TestContext ctx) throws Exception {
        final Async async = ctx.async();
        String testName = "no-such-player";

        startGame(testName)
            .subscribe(response -> {
                ctx.assertEquals(response.statusCode(), 404);
                async.complete();
            });
    }

    @Test
    public void testStartGame_ExistingPlayer_Succeeds(TestContext ctx) throws Exception {
        final Async async = ctx.async();
        createPlayer1()
            .flatMap(response1 -> startGame(PLAYER1_NAME))
            .subscribe(response -> {
                ctx.assertEquals(response.statusCode(), 200);
                try {
                    JsonObject body = response.bodyAsJsonObject();
                    ctx.assertEquals(PLAYER1_NAME, body.getJsonObject("player").getString("name"));
                    ctx.assertEquals("ongoing", body.getString("gameStatus"));
                    ctx.assertEquals(0, body.getInteger("guesses"));
                    ctx.assertEquals(6, body.getInteger("guessesLeft"));
                    ctx.assertEquals("", body.getString("incorrectLetters"));
                    async.complete();
                } catch (Exception ex) {
                    ctx.fail(ex);
                }
            }, ctx::fail);
    }

    @Test
    public void testGuess_IncorrectLetter_ChangesStateCorrectly(TestContext ctx) throws Exception {
        final Async async = ctx.async();
        String incorrectLetter = "b";

        createPlayer1()
            .flatMap(createPlayerResponse -> startGame(PLAYER1_NAME))
            .map(gameIdExtractor(ctx))
            .flatMap(gameId -> guess(gameId, incorrectLetter))
            .subscribe(response -> {
                ctx.assertEquals(response.statusCode(), 200);
                try {
                    JsonObject body = response.bodyAsJsonObject();
                    ctx.assertEquals("ongoing", body.getString("gameStatus"));
                    ctx.assertEquals(1, body.getInteger("guesses"));
                    ctx.assertEquals(5, body.getInteger("guessesLeft"));
                    ctx.assertEquals(incorrectLetter, body.getString("incorrectLetters"));
                    async.complete();
                } catch (Exception ex) {
                    ctx.fail(ex);
                }
            }, ctx::fail);
    }

    @Test
    public void testGuess_SixIncorrectLetters_LosesGame(TestContext ctx) throws Exception {
        final Async async = ctx.async();

        createPlayer1()
            .flatMap(createPlayerResponse -> startGame(PLAYER1_NAME))
            .map(gameIdExtractor(ctx))
            .flatMap(gameId -> guess(gameId, "1")
                .flatMap(guess1Response -> guess(gameId, "2"))
                .flatMap(guess2Response -> guess(gameId, "3"))
                .flatMap(guess2Response -> guess(gameId, "4"))
                .flatMap(guess2Response -> guess(gameId, "5"))
                .flatMap(guess2Response -> guess(gameId, "6")))
            .subscribe(response -> {
                ctx.assertEquals(response.statusCode(), 200);
                try {
                    JsonObject body = response.bodyAsJsonObject();
                    ctx.assertEquals("lost", body.getString("gameStatus"));
                    ctx.assertEquals(6, body.getInteger("guesses"));
                    ctx.assertEquals(0, body.getInteger("guessesLeft"));
                    async.complete();
                } catch (Exception ex) {
                    ctx.fail(ex);
                }
            }, ctx::fail);
    }

    @Test
    public void testGuess_CorrectLetters_WinsGame(TestContext ctx) throws Exception {
        final Async async = ctx.async();

        createPlayer1()
            .flatMap(createPlayerResponse -> startGame(PLAYER1_NAME))
            .map(gameIdExtractor(ctx))
            .flatMap(gameId -> guess(gameId, "n")
                .flatMap(guess1Response -> guess(gameId, "h"))
                .flatMap(guess2Response -> guess(gameId, "a"))
                .flatMap(guess2Response -> guess(gameId, "g"))
                .flatMap(guess2Response -> guess(gameId, "m")))
            .subscribe(response -> {
                ctx.assertEquals(response.statusCode(), 200);
                try {
                    JsonObject body = response.bodyAsJsonObject();
                    ctx.assertEquals("won", body.getString("gameStatus"));
                    ctx.assertEquals(5, body.getInteger("guesses"));
                    ctx.assertEquals(6, body.getInteger("guessesLeft"));
                    async.complete();
                } catch (Exception ex) {
                    ctx.fail(ex);
                }
            }, ctx::fail);
    }

    @Test
    public void testGiveUp_RevealsWordLosesGame(TestContext ctx) throws Exception {
        final Async async = ctx.async();

        createPlayer1()
            .flatMap(createPlayerResponse -> startGame(PLAYER1_NAME))
            .map(gameIdExtractor(ctx))
            .flatMap(this::giveUp)
            .subscribe(response -> {
                ctx.assertEquals(response.statusCode(), 200);
                try {
                    JsonObject body = response.bodyAsJsonObject();
                    ctx.assertEquals("lost", body.getString("gameStatus"));
                    ctx.assertEquals(word, body.getString("guessedWord"));
                    async.complete();
                } catch (Exception ex) {
                    ctx.fail(ex);
                }
            }, ctx::fail);
    }

    private Func1<HttpResponse<Buffer>, String> gameIdExtractor(TestContext ctx) {
        return startGameResponse -> {
            ctx.assertEquals(startGameResponse.statusCode(), 200);
            String location = startGameResponse.getHeader("Location");
            return location.substring(location.lastIndexOf('/') + 1);
        };
    }

    private ObservableFuture<HttpResponse<Buffer>> createPlayer1() {
        return createPlayer(PLAYER1_NAME, PLAYER1_AGE);
    }

    private ObservableFuture<HttpResponse<Buffer>> createPlayer(String testName, int testAge) {
        WebClient client = WebClient.create(vertx);
        ObservableFuture<HttpResponse<Buffer>> createPlayerF = RxHelper.observableFuture();
        client
            .post(port, host, rootPath + "/player")
            .sendJsonObject(
                new JsonObject()
                    .put("name", testName)
                    .put("age", testAge),
                createPlayerF.toHandler());
        return createPlayerF;
    }

    private ObservableFuture<HttpResponse<Buffer>> fetchPlayerInfo(String playerName) {
        WebClient client = WebClient.create(vertx);
        ObservableFuture<HttpResponse<Buffer>> fetchPlayerInfoF = RxHelper.observableFuture();
        client
            .get(port, host, rootPath + "/player/" + playerName)
            .send(fetchPlayerInfoF.toHandler());
        return fetchPlayerInfoF;
    }

    private ObservableFuture<HttpResponse<Buffer>> listPlayers() {
        WebClient client = WebClient.create(vertx);
        ObservableFuture<HttpResponse<Buffer>> listPlayersF = RxHelper.observableFuture();
        client
            .get(port, host, rootPath + "/player")
            .send(listPlayersF.toHandler());
        return listPlayersF;
    }

    private ObservableFuture<HttpResponse<Buffer>> startGame(String playerName) {
        WebClient client = WebClient.create(vertx);
        ObservableFuture<HttpResponse<Buffer>> startGameF = RxHelper.observableFuture();
        client
            .post(port, host, rootPath + "/game")
            .sendJsonObject(
                new JsonObject()
                    .put("name", playerName)
                    .put("age", 123),
                startGameF.toHandler());
        return startGameF;
    }

    private ObservableFuture<HttpResponse<Buffer>> guess(String gameId, String letter) {
        WebClient client = WebClient.create(vertx);
        ObservableFuture<HttpResponse<Buffer>> guessF = RxHelper.observableFuture();
        client
            .put(port, host, rootPath + "/game/" + gameId)
            .sendJsonObject(
                new JsonObject()
                    .put("letter", letter),
                guessF.toHandler());
        return guessF;
    }

    private ObservableFuture<HttpResponse<Buffer>> giveUp(String gameId) {
        WebClient client = WebClient.create(vertx);
        ObservableFuture<HttpResponse<Buffer>> giveUpF = RxHelper.observableFuture();
        client
            .delete(port, host, rootPath + "/game/" + gameId)
            .send(giveUpF.toHandler());
        return giveUpF;
    }

    private void assertPlayer1AndComplete(TestContext ctx, Async async, HttpResponse<Buffer> response, int o, String player1Name, int player1Age) {
        ctx.assertEquals(o, 200);
        try {
            JsonObject body = response.bodyAsJsonObject();
            assertPlayer(ctx, body, player1Name, player1Age);
            async.complete();
        } catch (Exception ex) {
            ctx.fail(ex);
        }
    }

    private void assertPlayer(TestContext ctx, JsonObject body, String testName, int testAge) {
        String name = body.getString("name");
        ctx.assertEquals(testName, name);

        Integer age = body.getInteger("age");
        ctx.assertEquals(testAge, age);
    }
}
