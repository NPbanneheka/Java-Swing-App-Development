package com.gatepasspro;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

class User implements Serializable {
    String id;
    String fullName;
    String username;
    String password;
    String role;
    String phone;
    String photoPath;
    String status = "Active";

    User(String id, String fullName, String username, String password, String role, String phone) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.role = role;
        this.phone = phone;
    }
}

class GatePass implements Serializable {
    String id;
    String vehicleNo;
    String driverName;
    String driverPhone;
    String destination;
    String invoiceNo;
    String routeName;
    String goodsDescription;
    String issuedBy;
    LocalDate issueDate;
    LocalTime issueTime;
    String status;
    String remarks;

    GatePass(String id, String vehicleNo, String driverName, String driverPhone, String destination,
             String invoiceNo, String routeName, String goodsDescription, String issuedBy, String remarks) {
        this.id = id;
        this.vehicleNo = vehicleNo;
        this.driverName = driverName;
        this.driverPhone = driverPhone;
        this.destination = destination;
        this.invoiceNo = invoiceNo;
        this.routeName = routeName;
        this.goodsDescription = goodsDescription;
        this.issuedBy = issuedBy;
        this.issueDate = LocalDate.now();
        this.issueTime = LocalTime.now().withNano(0);
        this.status = "Issued";
        this.remarks = remarks;
    }
}

class ScheduleRecord implements Serializable {
    String id;
    String gatePassId;
    String invoiceNo;
    String vehicleNo;
    String driverName;
    String checkedBy;
    LocalDate checkedDate;
    LocalTime checkedTime;
    boolean invoiceSealed;
    boolean goodsChecked;
    String status;
    String remarks;

    ScheduleRecord(String id, String gatePassId, String invoiceNo, String vehicleNo, String driverName,
                   String checkedBy, boolean invoiceSealed, boolean goodsChecked, String status, String remarks) {
        this.id = id;
        this.gatePassId = gatePassId;
        this.invoiceNo = invoiceNo;
        this.vehicleNo = vehicleNo;
        this.driverName = driverName;
        this.checkedBy = checkedBy;
        this.checkedDate = LocalDate.now();
        this.checkedTime = LocalTime.now().withNano(0);
        this.invoiceSealed = invoiceSealed;
        this.goodsChecked = goodsChecked;
        this.status = status;
        this.remarks = remarks;
    }
}
