package com.github.jaredwinick;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.PrometheusScrapingHandler;
import io.vertx.micrometer.VertxPrometheusOptions;

public class Server {

    private static Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {

        Integer listenPort = Integer.parseInt(System.getenv("LISTEN_PORT"));

        VertxOptions vertxOptions = new VertxOptions()
                .setWorkerPoolSize(10)
                .setMetricsOptions(
                        new MicrometerMetricsOptions()
                                .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
                                .setEnabled(true)
                );
        Vertx vertx = Vertx.vertx(vertxOptions);

        Router router = Router.router(vertx);
        router.route("/metrics").handler(PrometheusScrapingHandler.create());
        router.get("/read").handler(routingContext -> {
            logger.info("In /read handler");

            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "text/plain");

            // Write to the response and end it
            response.end("Hello, World!");
        });
        HttpServerOptions httpServerOptions = new HttpServerOptions();
        HttpServer server = vertx.createHttpServer(httpServerOptions);
        server.requestHandler(router).listen(listenPort);

        logger.info("HTTP Server listening on port: " + listenPort);
    }
}
