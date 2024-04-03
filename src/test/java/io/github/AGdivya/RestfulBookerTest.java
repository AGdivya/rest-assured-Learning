package io.github.AGdivya;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class RestfulBookerTest {
    /*
    Record classes are special classes that act as transparent carriers for immutable data.
    They are immutable classes (all fields are final) and are implicitly final classes, which means they can’t be extended.

    There are some restrictions that we need to keep in mind when writing a record class:

    We cannot add the extends clause to the declaration
    since every record class implicitly extends the abstract class Record, and Java doesn’t allow multiple inheritance
    We cannot declare instance variables or instance initializers in a record class
    We cannot declare native methods in a record class
    A record class declaration includes a name, type parameters (if the record is generic), a header containing the record’s components, and a body:
     */
    private record UserData(String firstname, String lastname, int totalprice, boolean depositpaid,
                            BookingDates bookingdates, String additionalneeds) {
        //  int a; instance field is not allowed in record class
    }

    private record UpdateUserData(String firstname, String lastname, int totalprice, boolean depositpaid,
                            BookingDates bookingdates, String additionalneeds) {
        //  int a; instance field is not allowed in record class
    }

    private record PartialUpdateUserData(int totalprice, String additionalneeds)
    {

    }
    private record BookingDates(String checkin, String checkout) {
    }

    private record AuthData(String username, String password) {

    }

    private static RequestSpecification specBuilder()
    {
        final RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();
        requestSpecBuilder.setBaseUri("http://localhost:3001/")
                .addHeader("Content-Type", "application/json")
                .addHeader("Cookie","token="+generateAuthToken())
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter());

        return requestSpecBuilder.build();

    }

    //Create Booking
    @Test
    public void testCreateBooking() {
        final String firstname = "Divya";
        final UserData userdata = new UserData(firstname, "upadhyay", 430, true, new BookingDates("2018-01-01", "2019-01-01"), "Breakfast");
        given()
                .spec(specBuilder())
                .body(userdata)
                .header("Accept", "application/json")
                .when()
                .post("booking")
                .then()
                .statusCode(200)
                .body("bookingid", is(notNullValue()))
                .body("booking.firstname", equalTo(firstname))
                .body("booking.bookingdates.checkin", equalTo("2018-01-01"));
    }

    //Get Booking with Id
    @Test
    public void testGetBooking() {
        given()
                .header("Content-Type", "application/json")
                .when()
                .log().all()
                .get("http://localhost:3001/booking/11")
                .then().log().all().statusCode(200);

    }

    //Update Booking
    @Test
    public void testUpdateBooking() {
        final UpdateUserData updateUserData = new UpdateUserData("amit","Ag",340,true,new BookingDates("2018-01-31","2018-02-01"),"dinner");
        given().spec(specBuilder())
                .body(updateUserData)
                .when()
                .put("booking/11")
                .then().body("firstname",equalTo("amit"))
                .body("additionalneeds",equalTo("dinner"));
    }

    //Partial Update Booking
    @Test
    public void testPartialUpdateBooking()
    {
        String additionalneeds = "lunch";
        final PartialUpdateUserData partialUpdateUserData = new PartialUpdateUserData(34,additionalneeds);
        given().spec(specBuilder())
                .header("Accept", "application/json")
                .body(partialUpdateUserData)
                .when()
                .patch("booking/11")
                .then()
                .statusCode(200)
                .body("additionalneeds",is(notNullValue()))
                .body("additionalneeds",is(equalTo(additionalneeds)));

    }

    //Delete Booking
    @Test
    public void testDeleteBooking()
    {
        given()
                .header("Content-Type","application/json")
                .header("Cookie","token="+generateAuthToken())
                .when()
                .log().all()
                .delete("http://localhost:3001/booking/5")
                .then()
                .log().all().statusCode(201);
    }

    //validated deleted booking with id
    @Test
    public void testBookingIdIsDeleted()
    {
        given()
                .header("Content-Type","application/json")
                .header("Cookie","token="+generateAuthToken())
                .when()
                .log().all()
                .get("http://localhost:3001/booking/5")
                .then()
                .log().all().statusCode(404);
    }

    //create token
    public static String generateAuthToken() {
        final AuthData authdata = new AuthData("admin", "password123");

        return given()
                .header("Content-Type", "application/json")
                .body(authdata)
                .when()
                .log()
                .all()
                .post("http://localhost:3001/auth")
                .then().log().all()
                .statusCode(200)
                .body("token", is(notNullValue()))
                .extract()
                .path("token");
    }

}
