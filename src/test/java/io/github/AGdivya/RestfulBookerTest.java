package io.github.AGdivya;

import jdk.jfr.ContentType;
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

    //Create Booking
    @Test
    public void testCreateBooking() {
        final String firstname = "Divya";
        final UserData userdata = new UserData(firstname, "upadhyay", 430, true, new BookingDates("2018-01-01", "2019-01-01"), "Breakfast");
        given()
                .body(userdata)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .when()
                .log()
                .all()
                .post("http://localhost:3001/booking")
                .then()
                .log()
                .all()
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
                .get("http://localhost:3001/booking/5")
                .then().log().all().statusCode(200);

    }

    //Update Booking
    @Test
    public void testUpdateBooking() {
        final UpdateUserData updateUserData = new UpdateUserData("amit","Ag",340,true,new BookingDates("2018-01-31","2018-02-01"),"dinner");
        given()
                .header("Content-Type", "application/json")
                .header("Cookie","token="+generateAuthToken())
                .body(updateUserData)
                .when()
                .log()
                .all()
                .put("http://localhost:3001/booking/5")
                .then().log().all().body("firstname",equalTo("amit"))
                .body("additionalneeds",equalTo("dinner"));
    }

    //Partial Update Booking
    @Test
    public void testPartialUpdateBooking()
    {
        String additionalneeds = "lunch";
        final PartialUpdateUserData partialUpdateUserData = new PartialUpdateUserData(34,additionalneeds);
        given()
                .header("Content-Type","application/json" )
                .header("Accept", "application/json")
                .header("Cookie","token="+generateAuthToken())
                .body(partialUpdateUserData)
                .when()
                .log()
                .all()
                .patch("http://localhost:3001/booking/5")
                .then()
                .log().all()
                .statusCode(200)
                .body(additionalneeds,is(notNullValue()))
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
    public String generateAuthToken() {
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
