package com.okamidev.api

import com.okamidev.philosopher.PhilosopherInteractor
import com.okamidev.presenter.PhilosopherResponse
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.CorsHandler

@Suppress("unused")
class MainVerticle : AbstractVerticle() {

    val philosopherInteractor = PhilosopherInteractor()

    override fun start(startFuture: Future<Void>) {
        val router = createRouter()

        vertx.createHttpServer()
                .requestHandler { router.accept(it) }
                .listen(config().getInteger("http.port", 9090)) { result ->
                    if (result.succeeded()) {
                        startFuture.complete()
                    } else {
                        startFuture.fail(result.cause())
                    }
                }
    }

    private fun createRouter() = Router.router(vertx).apply {
        route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST))
        get("/").handler(handlerPhilosopher)
        get("/:id").handler(handlerPhilosopherDetails)
    }

    val handlerPhilosopher = Handler<RoutingContext> { req ->
        req.response().endWithJson(philosopherInteractor.listPhilosopher().map { PhilosopherResponse(it) })
    }

    val handlerPhilosopherDetails = Handler<RoutingContext> { req ->
        run {
            val philosopherId = req.request().getParam("id")
            val response = PhilosopherResponse(philosopherInteractor.getById(philosopherId))
            req.response().endWithJson(response)
        }
    }

    fun HttpServerResponse.endWithJson(obj: Any) {
        this.putHeader("Content-Type", "application/json; charset=utf-8").end(Json.encodePrettily(obj))
    }
}