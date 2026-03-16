package com.example.visitormanagemet;

public class VisitorRequest {
    private String visitorName, residentName, status, purpose, key;
    private String receptionistMobile; // Stores the mobile of the receptionist who sent the request

    public VisitorRequest() {} // Required for Firebase

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getVisitorName() { return visitorName; }
    public void setVisitorName(String visitorName) { this.visitorName = visitorName; }

    public String getResidentName() { return residentName; }
    public void setResidentName(String residentName) { this.residentName = residentName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getReceptionistMobile() { return receptionistMobile; }
    public void setReceptionistMobile(String receptionistMobile) { this.receptionistMobile = receptionistMobile; }
}