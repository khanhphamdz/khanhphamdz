package com.datn.teeshirt.DTO;

public class ShippingAddressDTO {
    private String provinceId;
    private String districtId;
    private String wardId;
    private String specificAddress;
    private String phone;
    private String name;
    private String note;
    // Getter & Setter
    public String getProvinceId() { return provinceId; }
    public void setProvinceId(String provinceId) { this.provinceId = provinceId; }
    public String getDistrictId() { return districtId; }
    public void setDistrictId(String districtId) { this.districtId = districtId; }
    public String getWardId() { return wardId; }
    public void setWardId(String wardId) { this.wardId = wardId; }
    public String getSpecificAddress() { return specificAddress; }
    public void setSpecificAddress(String specificAddress) { this.specificAddress = specificAddress; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
} 