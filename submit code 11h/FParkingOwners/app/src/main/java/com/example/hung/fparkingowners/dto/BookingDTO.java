package com.example.hung.fparkingowners.dto;

public class BookingDTO {
    private int bookingID;
    private int parkingID;
    private int carID;
    private String status;
    private String checkinTime;
    private String checkoutTime;
    private String licensePlate;
    private String type;
    private double price;

    public BookingDTO() {

    }

    public BookingDTO(int bookingID, int parkingID, int carID, String status, String checkinTime, String checkoutTime, String licensePlate, String type, double price) {

    }

    public int getBookingID() {
        return bookingID;
    }

    public int getParkingID() {
        return parkingID;
    }

    public int getCarID() {
        return carID;
    }

    public String getStatus() {
        return status;
    }

    public String getCheckinTime() {
        return checkinTime;
    }

    public String getCheckoutTime() {
        return checkoutTime;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getTypeCar() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public void setBookingID(int bookingID) {
        this.bookingID = bookingID;
    }

    public void setParkingID(int parkingID) {
        this.parkingID = parkingID;
    }

    public void setCarID(int carID) {
        this.carID = carID;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCheckinTime(String checkinTime) {
        this.checkinTime = checkinTime;
    }

    public void setCheckoutTime(String checkoutTime) {
        this.checkoutTime = checkoutTime;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public void setTypeCar(String type) {
        this.type = type;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
