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

public class PersonApiTest {
    @Test
    public void shouldReturnPersonForTheId() throws Exception {
        given().
            accept(ContentType.JSON).
            pathParam("id", 1).
        when().
            get("/people/{id}").
        then().
            statusCode(200).
            body(
                "id", is(1),
                "name", is("Praveer")
            );
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
                "name", hasItems("Praveer", "Prachi")
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
        List<Person> people = asList(new Person(1, "Praveer"), new Person(2, "Prachi"));

        Spark.port(8080);

        Spark.get("/people/:id", "application/json",
            (final Request req, final Response res) -> {
                res.type("application/json");
                return people.stream()
                    .filter(p -> p.getId() == Integer.valueOf(req.params("id")))
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
        private final int id;
        private final String name;

        public Person(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
