package io.github.unapplicable.hangman.api;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class HangmanVerticleTest {
    private Vertx vertx;
    private int port;
    private String host;
    private String rootPath;

    @Before
    public void setUp(TestContext ctx) {
        host = "localhost";
        port = 8080;
        rootPath = "/hangman/v1";
        vertx = Vertx.vertx();
        vertx.deployVerticle(HangmanVerticle.class.getName(),
            ctx.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext ctx) {
        vertx.close(ctx.asyncAssertSuccess());
    }

    @Test
    public void testFetchPlayerInfo_NonExistentPlayer_ReturnsNotFound(TestContext ctx) {
        final Async async = ctx.async();
        WebClient client = WebClient.create(vertx);
        client.get(port, host, rootPath + "/player/1234").send(
            ar -> {
                ctx.assertTrue(ar.succeeded());
                HttpResponse<Buffer> response = ar.result();
                ctx.assertEquals(response.statusCode(), 404);
                async.complete();
            });
    }

    @Test
    public void testCreatePlayer_UnusedName_Succeeds(TestContext ctx) throws Exception {
        final Async async = ctx.async();
        WebClient client = WebClient.create(vertx);
        String testName = "player2";
        int testAge = 1;

        client.post(port, host, rootPath + "/player")
            .sendJsonObject(
                new JsonObject()
                    .put("name", testName)
                    .put("age", testAge),
                ar -> {
                    ctx.assertTrue(ar.succeeded());
                    HttpResponse<Buffer> response = ar.result();
                    ctx.assertEquals(response.statusCode(), 200);

                    try {
                        JsonObject body = response.bodyAsJsonObject();
                        assertPlayer(ctx, body, testName, testAge);
                        async.complete();
                    } catch (Exception ex) {
                        ctx.fail(ex);
                    }
                });
    }

    @Test
    public void testCreatePlayer_UsedName_Fails(TestContext ctx) throws Exception {
        final Async async = ctx.async();
        WebClient client = WebClient.create(vertx);
        String testName = "player1";
        int testAge = 1;

        client.post(port, host, rootPath + "/player")
            .sendJsonObject(
                new JsonObject()
                    .put("name", testName)
                    .put("age", testAge),
                ar -> {
                    ctx.assertTrue(ar.succeeded());
                    HttpResponse<Buffer> response = ar.result();
                    ctx.assertEquals(response.statusCode(), 400);
                    async.complete();

                });
    }

    private void assertPlayer(TestContext ctx, JsonObject body, String testName, int testAge) {
        String name = body.getString("name");
        ctx.assertEquals(testName, name);

        Integer age = body.getInteger("age");
        ctx.assertEquals(testAge, age);
    }

    @Test
    public void testFetchPlayerInfo_ExistingPlayer_Succeeds(TestContext ctx) {
        final Async async = ctx.async();
        WebClient client = WebClient.create(vertx);
        client.get(port, host, rootPath + "/player/player1").send(
            ar -> {
                ctx.assertTrue(ar.succeeded());
                HttpResponse<Buffer> response = ar.result();
                ctx.assertEquals(response.statusCode(), 200);
                try {
                    JsonObject body = response.bodyAsJsonObject();
                    assertPlayer(ctx, body, "player1", 1);
                    async.complete();
                } catch (Exception ex) {
                    ctx.fail(ex);
                }
            });
    }

    @Test
    public void testListPlayers_Succeeds(TestContext ctx) {
        final Async async = ctx.async();
        WebClient client = WebClient.create(vertx);
        client.get(port, host, rootPath + "/player").send(
            ar -> {
                ctx.assertTrue(ar.succeeded());
                HttpResponse<Buffer> response = ar.result();
                ctx.assertEquals(response.statusCode(), 200);
                try {
                    JsonArray body = response.bodyAsJsonArray();
                    ctx.assertTrue(body.size() > 0, "response has player objects");
                    async.complete();
                } catch (Exception ex) {
                    ctx.fail(ex);
                }
            });
    }

    @Test
    public void testStartGame_NonExistentPlayer_Fails(TestContext ctx) throws Exception {
        final Async async = ctx.async();
        WebClient client = WebClient.create(vertx);
        String testName = "no-such-player";
        int testAge = 1;

        client.post(port, host, rootPath + "/game")
            .sendJsonObject(
                new JsonObject()
                    .put("name", testName)
                    .put("age", testAge),
                ar -> {
                    ctx.assertTrue(ar.succeeded());
                    HttpResponse<Buffer> response = ar.result();
                    ctx.assertEquals(response.statusCode(), 400);
                    async.complete();
                });
    }

    @Test
    public void testStartGame_ExistingPlayer_Succeeds(TestContext ctx) throws Exception {
        final Async async = ctx.async();
        WebClient client = WebClient.create(vertx);
        String testName = "player1";
        int testAge = 1;

        client.post(port, host, rootPath + "/game")
            .sendJsonObject(
                new JsonObject()
                    .put("name", testName)
                    .put("age", testAge),
                ar -> {
                    ctx.assertTrue(ar.succeeded());
                    HttpResponse<Buffer> response = ar.result();
                    ctx.assertEquals(response.statusCode(), 200);
                    try {
                        JsonObject body = response.bodyAsJsonObject();
                        ctx.assertEquals(testName, body.getJsonObject("player").getString("name"));
                        ctx.assertEquals("ongoing", body.getString("gameStatus"));
                        ctx.assertEquals(0, body.getInteger("guesses"));
                        ctx.assertEquals(6, body.getInteger("guessesLeft"));
                        ctx.assertEquals("", body.getString("incorrectLetters"));
                        async.complete();
                    } catch (Exception ex) {
                        ctx.fail(ex);
                    }
                });
    }
}
