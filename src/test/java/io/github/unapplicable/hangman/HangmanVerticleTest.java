package io.github.unapplicable.hangman;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class HangmanVerticleTest {
    private Vertx vertx;
    private int port;

    @Before
    public void setUp(TestContext ctx) {
        port = 8080;
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

        vertx.createHttpClient().getNow(port, "localhost", "/player/1234",
            response -> {
                ctx.assertEquals(response.statusCode(), 404);
                async.complete();
            });
    }
}
