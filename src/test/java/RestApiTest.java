import com.google.gson.Gson;
import com.jayway.restassured.http.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;

public class RestApiTest {
    @Test
    public void shouldReturnSinglePerson() throws Exception {
        given().
            accept(ContentType.JSON).
            pathParam("name", "Ajay").
        when().
            get("/people/{name}").
        then().
            statusCode(200).
            body("name", is("Ajay"));
    }

    @Test
    public void shouldReturnAllPersons() throws Exception {
        given().
            accept(ContentType.JSON).
        when().
            get("/people").
        then().
            statusCode(200).
            body(
                "name", hasSize(2),
                "name", hasItems("Ajay", "Vijay")
            );
    }

    @Before
    public void setUp() throws Exception {
        configureAndStartWebServer();
    }

    @After
    public void tearDown() throws Exception {
        stopWebServer();
    }

    private void configureAndStartWebServer() {
        List<Person> people = asList(new Person("Ajay"), new Person("Vijay"));

        Spark.port(8080);

        Spark.get("/people/:name", "application/json",
            (final Request req, final Response res) -> {
                res.type("application/json");
                return people.stream()
                    .filter(p -> p.getName().equalsIgnoreCase(req.params("name")))
                    .findFirst()
                    .orElseThrow(NotFountException::new);
            }, new Gson()::toJson);

        Spark.get("/people", "application/json",
            (req, res) -> {
                res.type("application/json");
                return people;
            }, new Gson()::toJson);

        Spark.exception(NotFountException.class, (e, req, res) -> res.status(404));

        Spark.awaitInitialization();
    }

    private void stopWebServer() {
        Spark.stop();
    }

    private class NotFountException extends Exception {}

    private class Person {
        private final String name;

        public Person(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
