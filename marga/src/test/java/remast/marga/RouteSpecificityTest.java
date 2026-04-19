package remast.marga;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RouteSpecificityTest {

    @Test
    void moreStaticSegmentsWins() {
        var router = new Router();
        router.GET("/a/${x}/${y}", request -> Response.ok("A"));
        router.GET("/a/b/${x}", request -> Response.ok("B"));

        var response = router.handleRequest("GET", "/a/b/c");
        assertEquals("B", response.getBody());
    }

    @Test
    void exactRouteBeatsParameterizedRoute() {
        var router = new Router();
        router.GET("/users/${id}", request -> Response.ok("PARAM"));
        router.GET("/users/me", request -> Response.ok("EXACT"));

        var response = router.handleRequest("GET", "/users/me");
        assertEquals("EXACT", response.getBody());
    }

    @Test
    void parameterizedRouteStillMatchesOtherPaths() {
        var router = new Router();
        router.GET("/users/${id}", request -> Response.ok("PARAM:" + request.pathParam("id")));
        router.GET("/users/me", request -> Response.ok("EXACT"));

        var response = router.handleRequest("GET", "/users/42");
        assertEquals("PARAM:42", response.getBody());
    }

    @Test
    void methodMismatchReturns405WithAllow() {
        var router = new Router();
        router.POST("/users/${id}", request -> Response.ok("created"));
        router.PUT("/users/${id}", request -> Response.ok("updated"));

        var response = router.handleRequest("GET", "/users/42");
        assertEquals(405, response.getStatusCode());
        var allow = response.getHeader(HttpHeader.ALLOW);
        assertEquals("POST, PUT", allow);
    }

    @Test
    void earlierParameterPositionIsLessSpecific() {
        var router = new Router();
        router.GET("/${x}/b/c", request -> Response.ok("EARLY"));
        router.GET("/a/${x}/c", request -> Response.ok("MIDDLE"));

        var response = router.handleRequest("GET", "/a/b/c");
        assertEquals("MIDDLE", response.getBody());
    }
}
